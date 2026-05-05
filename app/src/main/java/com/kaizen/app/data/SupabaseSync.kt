package com.kaizen.app.data

import com.kaizen.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object SupabaseSync {

    private val url   get() = BuildConfig.SUPABASE_URL
    private val key   get() = BuildConfig.SUPABASE_ANON_KEY

    suspend fun upsertJournal(entry: JournalEntry) = upsert(
        table = "journal_entries",
        body  = JSONObject().apply {
            put("remote_id",  entry.remoteId)
            put("date",       entry.date)
            put("text",       entry.text)
            put("mood",       entry.mood)
            put("tags",       entry.tags)
            put("updated_at", entry.updatedAt)
        }
    )

    suspend fun upsertGoal(goal: Goal) = upsert(
        table = "goals",
        body  = JSONObject().apply {
            put("remote_id",   goal.remoteId)
            put("title",       goal.title)
            put("description", goal.description)
            put("target_date", goal.targetDate)
            put("status",      goal.status.name)
            put("updated_at",  goal.updatedAt)
        }
    )

    suspend fun upsertWin(win: Win) = upsert(
        table = "wins",
        body  = JSONObject().apply {
            put("remote_id",   win.remoteId)
            put("title",       win.title)
            put("description", win.description)
            put("date",        win.date)
            put("type",        win.type.name)
            put("updated_at",  win.updatedAt)
        }
    )

    suspend fun deleteJournal(remoteId: String) = delete("journal_entries", remoteId)
    suspend fun deleteGoal(remoteId: String)    = delete("goals", remoteId)
    suspend fun deleteWin(remoteId: String)     = delete("wins", remoteId)

    suspend fun fetchJournalEntries() = fetch("journal_entries")
    suspend fun fetchGoals()          = fetch("goals")
    suspend fun fetchWins()           = fetch("wins")

    private suspend fun upsert(table: String, body: JSONObject) = withContext(Dispatchers.IO) {
        runCatching {
            val conn = openConn("$url/rest/v1/$table", "POST").apply {
                setRequestProperty("Prefer", "resolution=merge-duplicates,return=minimal")
            }
            conn.outputStream.use { it.write(JSONArray().put(body).toString().toByteArray()) }
            conn.responseCode
        }
    }

    private suspend fun delete(table: String, remoteId: String) = withContext(Dispatchers.IO) {
        runCatching {
            val conn = openConn("$url/rest/v1/$table?remote_id=eq.$remoteId", "DELETE")
            conn.responseCode
        }
    }

    private suspend fun fetch(table: String): List<JSONObject> = withContext(Dispatchers.IO) {
        runCatching {
            val conn = (URL("$url/rest/v1/$table?order=updated_at.desc&limit=500").openConnection() as HttpsURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout    = 15000
                setRequestProperty("apikey",        key)
                setRequestProperty("Authorization", "Bearer $key")
            }
            val body = conn.inputStream.bufferedReader().readText()
            val arr  = JSONArray(body)
            (0 until arr.length()).map { arr.getJSONObject(it) }
        }.getOrElse { emptyList() }
    }

    private fun openConn(endpoint: String, method: String): HttpsURLConnection =
        (URL(endpoint).openConnection() as HttpsURLConnection).apply {
            requestMethod  = method
            doOutput       = method != "DELETE"
            connectTimeout = 8000
            readTimeout    = 10000
            setRequestProperty("Content-Type",  "application/json")
            setRequestProperty("apikey",        key)
            setRequestProperty("Authorization", "Bearer $key")
        }
}
