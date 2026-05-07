package com.kaizen.app.data

import android.util.Log
import com.kaizen.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class OWHealthClient {

    private val base   = BuildConfig.OW_BASE_URL
    private val apiKey = BuildConfig.OW_API_KEY
    private val userId = BuildConfig.OW_USER_ID
    private val fmt    = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)

    suspend fun readTodayData(): GarminHealthData = withContext(Dispatchers.IO) {
        runCatching {
            val today = LocalDate.now()
            val start = enc(fmt.format(today.atStartOfDay(ZoneOffset.UTC)))
            val end   = enc(fmt.format(today.plusDays(1).atStartOfDay(ZoneOffset.UTC)))

            val url = "$base/api/v1/users/$userId/timeseries" +
                "?start_time=$start&end_time=$end" +
                "&types=steps&types=resting_heart_rate&types=heart_rate_variability_rmssd" +
                "&limit=100"

            val data = get(url).getJSONArray("data")

            var steps: Int? = null
            var restingHr: Int? = null
            var hrv: Float? = null

            for (i in 0 until data.length()) {
                val item = data.getJSONObject(i)
                val value = item.getDouble("value")
                when (item.getString("type")) {
                    "steps"                        -> steps = (steps ?: 0) + value.toInt()
                    "resting_heart_rate"           -> restingHr = value.toInt()
                    "heart_rate_variability_rmssd" -> hrv = value.toFloat()
                }
            }

            GarminHealthData(steps = steps, restingHr = restingHr, hrv = hrv)
        }.getOrElse { e ->
            Log.e("OWHealthClient", "readTodayData failed: ${e.message}")
            GarminHealthData()
        }
    }

    private fun get(url: String): JSONObject {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout    = 15_000
            setRequestProperty("X-Open-Wearables-API-Key", apiKey)
            setRequestProperty("Accept", "application/json")
        }
        val code = conn.responseCode
        check(code in 200..299) { "OW API HTTP $code at $url" }
        return JSONObject(conn.inputStream.bufferedReader().readText())
    }

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
}
