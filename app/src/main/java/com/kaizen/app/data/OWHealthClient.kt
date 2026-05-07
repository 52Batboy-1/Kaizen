package com.kaizen.app.data

import android.util.Log
import com.kaizen.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

class OWHealthClient {

    private val base   = BuildConfig.OW_BASE_URL
    private val apiKey = BuildConfig.OW_API_KEY
    private val userId = BuildConfig.OW_USER_ID
    private val fmt    = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)

    // ── Read ──────────────────────────────────────────────────────────────────

    suspend fun readTodayData(): GarminHealthData = withContext(Dispatchers.IO) {
        runCatching {
            val today = LocalDate.now()
            val start = enc(fmt.format(today.atStartOfDay(ZoneOffset.UTC)))
            val end   = enc(fmt.format(today.plusDays(1).atStartOfDay(ZoneOffset.UTC)))

            val url = "$base/api/v1/users/$userId/timeseries" +
                "?start_time=$start&end_time=$end" +
                "&types=steps&types=resting_heart_rate&types=heart_rate_variability_rmssd" +
                "&types=garmin_body_battery&types=garmin_stress_level" +
                "&limit=100"

            val data = get(url).getJSONArray("data")

            var steps: Int? = null
            var restingHr: Int? = null
            var hrv: Float? = null
            var bodyBattery: Int? = null
            var stressScore: Int? = null

            for (i in 0 until data.length()) {
                val item  = data.getJSONObject(i)
                val value = item.getDouble("value")
                when (item.getString("type")) {
                    "steps"                        -> steps = (steps ?: 0) + value.toInt()
                    "resting_heart_rate"           -> restingHr = value.toInt()
                    "heart_rate_variability_rmssd" -> hrv = value.toFloat()
                    "garmin_body_battery"          -> bodyBattery = value.toInt()
                    "garmin_stress_level"          -> stressScore = value.toInt()
                }
            }

            GarminHealthData(
                steps       = steps,
                restingHr   = restingHr,
                hrv         = hrv,
                bodyBattery = bodyBattery,
                stressScore = stressScore,
            )
        }.getOrElse { e ->
            Log.e("OWHealthClient", "readTodayData failed: ${e.message}")
            GarminHealthData()
        }
    }

    // ── Write (manual body battery / stress) ─────────────────────────────────

    suspend fun postBodyBattery(value: Int) = postManualMetric("GARMIN_BODY_BATTERY", value.toDouble(), "percent")

    suspend fun postStressScore(value: Int)  = postManualMetric("GARMIN_STRESS_LEVEL", value.toDouble(), "score")

    private suspend fun postManualMetric(type: String, value: Double, unit: String) = withContext(Dispatchers.IO) {
        runCatching {
            val now = fmt.format(Instant.now())
            val record = JSONObject().apply {
                put("id",        UUID.randomUUID().toString())
                put("type",      type)
                put("startDate", now)
                put("endDate",   now)
                put("value",     value)
                put("unit",      unit)
                put("source",    JSONObject().apply {
                    put("appId",      "com.kaizen.app")
                    put("deviceName", "Kaizen Android")
                })
            }
            val body = JSONObject().apply {
                put("provider",      "google")
                put("sdkVersion",    "0.11.0")
                put("syncTimestamp", now)
                put("data", JSONObject().apply {
                    put("records",  JSONArray().put(record))
                    put("workouts", JSONArray())
                    put("sleep",    JSONArray())
                })
            }
            post("$base/api/v1/sdk/users/$userId/sync", body)
        }.onFailure { Log.e("OWHealthClient", "postManualMetric $type failed: ${it.message}") }
    }

    // ── HTTP ──────────────────────────────────────────────────────────────────

    private fun get(url: String): JSONObject {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout    = 15_000
            setRequestProperty("X-Open-Wearables-API-Key", apiKey)
            setRequestProperty("Accept", "application/json")
        }
        check(conn.responseCode in 200..299) { "OW GET HTTP ${conn.responseCode}" }
        return JSONObject(conn.inputStream.bufferedReader().readText())
    }

    private fun post(url: String, body: JSONObject) {
        val bytes = body.toString().toByteArray(Charsets.UTF_8)
        val conn  = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 8_000
            readTimeout    = 15_000
            doOutput       = true
            setRequestProperty("X-Open-Wearables-API-Key", apiKey)
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }
        conn.outputStream.use { it.write(bytes) }
        check(conn.responseCode in 200..299) { "OW POST HTTP ${conn.responseCode}" }
    }

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
}
