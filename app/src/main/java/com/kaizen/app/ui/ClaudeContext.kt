package com.kaizen.app.ui

import com.kaizen.app.BuildConfig
import com.kaizen.app.data.GoalStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.time.LocalDate
import javax.net.ssl.HttpsURLConnection

fun buildCoachSystemPrompt(state: KaizenUiState): String = buildString {
    appendLine("You are Kaizen Coach, a personal bodyweight training assistant built into Jordan's fitness app.")
    appendLine("Today: ${LocalDate.now()} — Week ${state.currentWeek}/24, ${state.currentTier.label} tier (${state.currentTier.split})")
    appendLine()

    appendLine("WHOOP:")
    if (state.hasWhoopData) {
        state.whoopRecovery?.let { appendLine("  Recovery: $it% → ${state.whoopZone.label}") }
        state.whoopSuggestedStrain?.let { appendLine("  Suggested strain: ${"%.1f".format(it)}/21") }
        state.whoopStrain?.let { appendLine("  Actual strain: ${"%.1f".format(it)}/21") }
        appendLine("  Scaled difficulty: ${state.whoopScaledDifficulty.name}")
    } else {
        appendLine("  No WHOOP data logged today.")
    }
    appendLine()

    appendLine("RECENT WORKOUTS (${state.recentWorkouts.size} total):")
    if (state.recentWorkouts.isEmpty()) appendLine("  None logged yet.")
    else state.recentWorkouts.take(8).forEach { log ->
        val rec = log.whoopRecovery?.let { " (rec $it%)" } ?: ""
        appendLine("  ${log.date}: ${log.workoutType.label}$rec")
    }
    appendLine()

    appendLine("PERSONAL RECORDS:")
    if (state.personalRecords.isEmpty()) appendLine("  None logged yet.")
    else state.personalRecords.take(8).forEach { pr ->
        appendLine("  ${pr.exerciseName}: ${pr.repsOrDuration} — ${pr.difficulty.name}")
    }
    appendLine()

    val latestBW = state.bodyweightEntries.firstOrNull()
    val avgSleep = if (state.sleepEntries.isEmpty()) null
    else state.sleepEntries.take(7).map { it.hoursSlept }.average()
    val avgHRV = state.sleepEntries.take(7).mapNotNull { it.hrv }
        .let { if (it.isEmpty()) null else it.average().toInt() }
    appendLine("BODY METRICS:")
    latestBW?.let { appendLine("  Bodyweight: ${"%.1f".format(it.weightKg)}kg (${it.date})") }
    avgSleep?.let { appendLine("  7d avg sleep: ${"%.1f".format(it)}h") }
    avgHRV?.let   { appendLine("  7d avg HRV: ${it}ms") }
    if (latestBW == null && avgSleep == null) appendLine("  No data logged yet.")
    appendLine()

    appendLine("ACTIVE GOALS:")
    val activeGoals = state.goals.filter { it.status == GoalStatus.ACTIVE }
    if (activeGoals.isEmpty()) appendLine("  None set.")
    else activeGoals.take(5).forEach { g ->
        val deadline = if (g.targetDate.isNotBlank()) " — by ${g.targetDate}" else ""
        appendLine("  • ${g.title}$deadline")
    }
    appendLine()

    appendLine("RECENT JOURNAL (last 3 entries):")
    if (state.journalEntries.isEmpty()) appendLine("  No entries yet.")
    else state.journalEntries.take(3).forEach { e ->
        val preview = e.text.take(100) + if (e.text.length > 100) "…" else ""
        appendLine("  ${e.date} [mood ${e.mood}/5]: $preview")
    }
    appendLine()

    appendLine("WINS:")
    if (state.wins.isEmpty()) appendLine("  None logged yet.")
    else state.wins.take(3).forEach { w -> appendLine("  🏆 ${w.title}") }
    appendLine()

    appendLine("ACTIVE INJURIES:")
    if (state.activeInjuries.isEmpty()) appendLine("  None.")
    else state.activeInjuries.forEach { inj ->
        val note = if (inj.notes.isNotBlank()) " — ${inj.notes}" else ""
        appendLine("  ⚠ ${inj.side.label} ${inj.bodyPart.label}: ${inj.type.label} ${inj.severity}/5$note")
    }
    appendLine()

    val donePct = if (state.habits.isNotEmpty())
        "${state.habits.count { it.isCompletedOn(state.today) }}/${state.habits.size}" else "0/0"
    appendLine("HABITS TODAY: $donePct completed")
    appendLine()

    appendLine("Be a direct, knowledgeable coach. Reference Jordan's actual data when relevant. Keep responses concise (2-4 sentences) unless a detailed breakdown is explicitly requested. No markdown — plain text only.")
}

suspend fun callClaudeChat(systemPrompt: String, messages: List<ChatMessage>): String =
    withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("model", "claude-sonnet-4-6")
                put("max_tokens", 500)
                put("system", systemPrompt)
                put("messages", JSONArray().apply {
                    messages.forEach { msg ->
                        put(JSONObject().apply {
                            put("role", msg.role)
                            put("content", msg.content)
                        })
                    }
                })
            }
            val conn = (URL("https://api.anthropic.com/v1/messages").openConnection() as HttpsURLConnection).apply {
                requestMethod = "POST"; doOutput = true; connectTimeout = 10000; readTimeout = 25000
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("anthropic-version", "2023-06-01")
                setRequestProperty("x-api-key", BuildConfig.ANTHROPIC_API_KEY)
            }
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            val response = conn.inputStream.bufferedReader().readText()
            JSONObject(response).getJSONArray("content").getJSONObject(0).getString("text").trim()
        } catch (e: Exception) {
            "Sorry, couldn't connect. Check your network and try again."
        }
    }
