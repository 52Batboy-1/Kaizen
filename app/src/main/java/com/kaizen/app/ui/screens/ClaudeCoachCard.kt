package com.kaizen.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kaizen.app.BuildConfig
import com.kaizen.app.data.*
import com.kaizen.app.ui.KaizenUiState
import com.kaizen.app.ui.theme.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

private suspend fun fetchCoachAdvice(state: KaizenUiState, workoutType: WorkoutType?, habitsDone: Int): String =
    withContext(Dispatchers.IO) {
        try {
            val recentSessions = state.recentWorkouts.take(12)
            val topPRs = state.personalRecords.take(6)
            val latestBW = state.bodyweightEntries.firstOrNull()
            val avgSleep = if (state.sleepEntries.isEmpty()) null
                else state.sleepEntries.take(7).map { it.hoursSlept }.average()
            val avgHRV = state.sleepEntries.take(7).mapNotNull { it.hrv }
                .let { if (it.isEmpty()) null else it.average().toInt() }

            val prompt = buildString {
                appendLine("You are Kaizen Coach inside a personal bodyweight training app. Be direct, specific, and motivating. Max 3 sentences. No markdown.")
                appendLine()
                appendLine("USER DATA:")
                state.whoopRecovery?.let { appendLine("- Recovery: $it%  (${state.whoopZone.label})") }
                state.whoopSuggestedStrain?.let { appendLine("- Whoop suggested strain: ${"%.1f".format(it)}/21") }
                state.whoopStrain?.let { appendLine("- Actual strain today: ${"%.1f".format(it)}/21") }
                workoutType?.let { appendLine("- Today: ${it.label} Day (${it.muscles})") }
                appendLine("- Difficulty tier: ${state.whoopScaledDifficulty.name}")
                appendLine("- Habits done today: $habitsDone/${state.habits.size}")
                avgSleep?.let { appendLine("- Avg sleep 7d: ${"%.1f".format(it)} hrs") }
                avgHRV?.let { appendLine("- Avg HRV 7d: ${it} ms") }
                latestBW?.let { appendLine("- Bodyweight: ${"%.1f".format(it.weightKg)} kg") }
                if (state.activeInjuries.isNotEmpty()) {
                    appendLine("- ACTIVE INJURIES:")
                    for (injury in state.activeInjuries) {
                        val noteStr = if (injury.notes.isNotBlank()) " (${injury.notes})" else ""
                        appendLine("  * ${injury.side.label} ${injury.bodyPart.label}: ${injury.type.label} severity ${injury.severity}/5$noteStr")
                    }
                    appendLine("  Account for these injuries - suggest modifications.")
                }
                appendLine()
                if (recentSessions.isNotEmpty()) {
                    appendLine("RECENT TRAINING (last ${recentSessions.size} sessions):")
                    recentSessions.groupBy { it.workoutType }.forEach { (type, logs) ->
                        appendLine("- ${type.label}: ${logs.size} sessions, avg recovery ${logs.mapNotNull { it.whoopRecovery }.average().let { if (it.isNaN()) "?" else it.toInt().toString() }}%")
                    }
                    appendLine()
                }
                if (topPRs.isNotEmpty()) {
                    appendLine("RECENT PRs:")
                    topPRs.forEach { pr -> appendLine("- ${pr.exerciseName}: ${pr.repsOrDuration} (${pr.difficulty.name})") }
                    appendLine()
                }
                appendLine("Based on this data, give ONE specific coaching recommendation for today. If recovery is low, focus on quality/recovery. If high, push progression. Reference their actual PRs or recent training if relevant.")
            }

            val body = JSONObject().apply {
                put("model", "claude-sonnet-4-6")
                put("max_tokens", 200)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply { put("role", "user"); put("content", prompt) })
                })
            }

            val conn = (URL("https://api.anthropic.com/v1/messages").openConnection() as HttpsURLConnection).apply {
                requestMethod = "POST"; doOutput = true; connectTimeout = 8000; readTimeout = 15000
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("anthropic-version", "2023-06-01")
                setRequestProperty("x-api-key", BuildConfig.ANTHROPIC_API_KEY)
            }
            conn.outputStream.use { it.write(body.toString().toByteArray()) }
            val response = conn.inputStream.bufferedReader().readText()
            JSONObject(response).getJSONArray("content").getJSONObject(0).getString("text").trim()
        } catch (e: Exception) {
            "Claude Coach unavailable. Keep training."
        }
    }

@Composable
fun ClaudeCoachCard(
    state: KaizenUiState,
    workoutType: WorkoutType?,
    habitsDoneToday: Int,
    modifier: Modifier = Modifier,
) {
    var advice  by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope   = rememberCoroutineScope()

    fun fetch() {
        loading = true; advice = null
        scope.launch {
            advice  = fetchCoachAdvice(state, workoutType, habitsDoneToday)
            loading = false
        }
    }

    LaunchedEffect(state.whoopRecovery, state.whoopSuggestedStrain) {
        if (state.hasWhoopData && advice == null && !loading) fetch()
    }

    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), color = Color(0xFF0A0A20), border = BorderStroke(1.dp, K.Gold.copy(0.3f))) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(8.dp), color = K.Gold.copy(0.15f), modifier = Modifier.size(30.dp)) {
                        Box(contentAlignment = Alignment.Center) { Text("◈", fontSize = 16.sp, color = K.Gold) }
                    }
                    Column {
                        Text("Claude Coach", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = K.Gold)
                        Text(
                            buildString {
                                append("${state.recentWorkouts.size} sessions")
                                if (state.personalRecords.isNotEmpty()) append(" · ${state.personalRecords.size} PRs")
                                if (state.bodyweightEntries.isNotEmpty()) append(" · ${"%.1f".format(state.bodyweightEntries.first().weightKg)}kg")
                            },
                            fontSize = 10.sp, color = K.Muted
                        )
                    }
                }
                TextButton(onClick = { fetch() }, enabled = !loading) {
                    Text(if (loading) "..." else "↻", color = K.Gold, fontSize = 16.sp)
                }
            }

            AnimatedContent(targetState = when { loading -> "loading"; advice != null -> "advice"; else -> "prompt" },
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) }) { s ->
                when (s) {
                    "loading" -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                        LoadingDots(K.Gold); Text("Analysing your training data...", fontSize = 12.sp, color = K.Muted)
                    }
                    "advice"  -> Text(advice ?: "", fontSize = 14.sp, color = K.Text, lineHeight = 20.sp)
                    else      -> Text(
                        if (!state.hasWhoopData) "Enter your Whoop recovery above to get coaching." else "Tap ↻ to get today's coaching advice.",
                        fontSize = 12.sp, color = K.Muted, lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingDots(color: Color) {
    val inf = rememberInfiniteTransition()
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { i ->
            val alpha by inf.animateFloat(0.2f, 1f, infiniteRepeatable(tween(600, i * 200), RepeatMode.Reverse))
            Box(Modifier.size(6.dp).clip(RoundedCornerShape(100.dp)).background(color.copy(alpha)))
        }
    }
}
