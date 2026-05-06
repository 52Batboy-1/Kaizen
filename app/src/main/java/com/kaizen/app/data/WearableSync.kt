package com.kaizen.app.data

// ══ Garmin Health API — OAuth 1.0a ════════════════════════════════════════
//
// Provides richer data than Health Connect: body battery, stress score,
// sleep scores, VO2 max, training load.
//
// To enable:
//   1. Register at developer.garmin.com → create an app → get Consumer Key + Secret
//   2. Complete the OAuth 1.0a flow once to get your own Access Token + Secret:
//        https://developer.garmin.com/connect-iq/connect-iq-basics/getting-started/
//   3. Add to local.properties (never commit these):
//        GARMIN_CONSUMER_KEY=...
//        GARMIN_CONSUMER_SECRET=...
//        GARMIN_ACCESS_TOKEN=...
//        GARMIN_ACCESS_SECRET=...
//   4. Uncomment the object body below
//   5. Uncomment the call sites in KaizenRepository and KaizenViewModel
//
// ══════════════════════════════════════════════════════════════════════════

object WearableSync {

    /*

    private const val BASE = "https://healthapi.garmin.com/wellness-api/rest"

    private val consumerKey    get() = BuildConfig.GARMIN_CONSUMER_KEY
    private val consumerSecret get() = BuildConfig.GARMIN_CONSUMER_SECRET
    private val accessToken    get() = BuildConfig.GARMIN_ACCESS_TOKEN
    private val accessSecret   get() = BuildConfig.GARMIN_ACCESS_SECRET

    // ── Public entry point ────────────────────────────────────────────────
    // Returns a GarminEntry merged from dailies + sleeps + activities for
    // the given date string ("yyyy-MM-dd"). Returns null on any network error.

    suspend fun syncToday(date: String): GarminEntry? =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            runCatching {
                val day   = java.time.LocalDate.parse(date)
                val start = day.atStartOfDay(java.time.ZoneOffset.UTC).toEpochSecond()
                val end   = start + 86399L

                val daily    = fetchDailies(start, end)
                val sleep    = fetchSleeps(start, end)
                // val activity = fetchActivities(start, end)  // VO2 max / training load

                GarminEntry(
                    date        = date,
                    bodyBattery = daily.optInt("bodyBatteryMostRecentValue").takeIf { it != 0 },
                    stressScore = daily.optInt("averageStressLevel").takeIf { it != 0 },
                    steps       = daily.optInt("steps").takeIf { it != 0 },
                    restingHr   = daily.optInt("restingHeartRate").takeIf { it != 0 },
                    hrv         = sleep.optDouble("averageHRV", 0.0).toFloat().takeIf { it != 0f },
                    updatedAt   = System.currentTimeMillis(),
                )
            }.getOrNull()
        }

    // ── Endpoint fetchers ─────────────────────────────────────────────────

    // GET /dailies → bodyBattery, steps, stressLevel, restingHR
    private fun fetchDailies(start: Long, end: Long): org.json.JSONObject {
        val params = mapOf(
            "uploadStartTimeInSeconds" to start.toString(),
            "uploadEndTimeInSeconds"   to end.toString(),
        )
        val arr = get("$BASE/dailies", params)
        return arr.optJSONObject(0) ?: org.json.JSONObject()
    }

    // GET /sleeps → durationInSeconds, sleepScores, avgSleepStress, averageHRV, averageRRInterval
    private fun fetchSleeps(start: Long, end: Long): org.json.JSONObject {
        val params = mapOf(
            "uploadStartTimeInSeconds" to start.toString(),
            "uploadEndTimeInSeconds"   to end.toString(),
        )
        val arr = get("$BASE/sleeps", params)
        return arr.optJSONObject(0) ?: org.json.JSONObject()
    }

    // GET /activities → trainingLoad, vo2MaxValue (populated after runs)
    private fun fetchActivities(start: Long, end: Long): org.json.JSONObject {
        val params = mapOf(
            "uploadStartTimeInSeconds" to start.toString(),
            "uploadEndTimeInSeconds"   to end.toString(),
        )
        val arr = get("$BASE/activities", params)
        // Most recent activity of the day
        return if (arr.length() > 0) arr.getJSONObject(arr.length() - 1)
               else org.json.JSONObject()
    }

    // ── HTTP ──────────────────────────────────────────────────────────────

    private fun get(endpoint: String, params: Map<String, String>): org.json.JSONArray {
        val query   = params.entries.joinToString("&") { "${it.key}=${it.value}" }
        val fullUrl = if (params.isEmpty()) endpoint else "$endpoint?$query"
        val auth    = buildOAuthHeader("GET", endpoint, params)

        val conn = (java.net.URL(fullUrl).openConnection() as javax.net.ssl.HttpsURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout    = 15_000
            setRequestProperty("Authorization", auth)
            setRequestProperty("Accept",        "application/json")
        }

        val code = conn.responseCode
        check(code in 200..299) { "Garmin API $endpoint: HTTP $code" }
        return org.json.JSONArray(conn.inputStream.bufferedReader().readText())
    }

    // ── OAuth 1.0a signing ────────────────────────────────────────────────

    private fun buildOAuthHeader(
        method: String,
        baseUrl: String,
        queryParams: Map<String, String>,
    ): String {
        val timestamp = (System.currentTimeMillis() / 1000L).toString()
        val nonce     = java.util.UUID.randomUUID().toString().replace("-", "")

        val oauthParams = linkedMapOf(
            "oauth_consumer_key"     to consumerKey,
            "oauth_nonce"            to nonce,
            "oauth_signature_method" to "HMAC-SHA1",
            "oauth_timestamp"        to timestamp,
            "oauth_token"            to accessToken,
            "oauth_version"          to "1.0",
        )

        // Signature base string: all params sorted, percent-encoded
        val allParams = (oauthParams + queryParams)
            .entries
            .sortedBy { pct(it.key) }
            .joinToString("&") { "${pct(it.key)}=${pct(it.value)}" }

        val signingKey = "${pct(consumerSecret)}&${pct(accessSecret)}"
        val baseStr    = "${method.uppercase()}&${pct(baseUrl)}&${pct(allParams)}"
        val signature  = hmacSha1(signingKey, baseStr)

        val headerParts = (oauthParams + mapOf("oauth_signature" to signature))
            .entries
            .sortedBy { it.key }
            .joinToString(", ") { """${it.key}="${pct(it.value)}"""" }

        return "OAuth $headerParts"
    }

    private fun hmacSha1(key: String, data: String): String {
        val mac = javax.crypto.Mac.getInstance("HmacSHA1")
        mac.init(javax.crypto.spec.SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA1"))
        return android.util.Base64.encodeToString(
            mac.doFinal(data.toByteArray(Charsets.UTF_8)),
            android.util.Base64.NO_WRAP,
        )
    }

    // RFC 3986 percent-encoding (stricter than URLEncoder)
    private fun pct(s: String): String =
        java.net.URLEncoder.encode(s, "UTF-8")
            .replace("+",   "%20")
            .replace("*",   "%2A")
            .replace("%7E", "~")

    */
}
