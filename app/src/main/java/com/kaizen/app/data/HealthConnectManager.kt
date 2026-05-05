package com.kaizen.app.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.LocalDate
import java.time.ZoneOffset

data class GarminHealthData(
    val steps: Int?     = null,
    val restingHr: Int? = null,
    val hrv: Float?     = null,
)

class HealthConnectManager(private val ctx: Context) {

    val permissions = setOf(
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
    )

    private val client by lazy { HealthConnectClient.getOrCreate(ctx) }

    fun isAvailable(): Boolean =
        HealthConnectClient.getSdkStatus(ctx) == HealthConnectClient.SDK_AVAILABLE

    suspend fun hasPermissions(): Boolean = runCatching {
        client.permissionController.getGrantedPermissions().containsAll(permissions)
    }.getOrElse { false }

    suspend fun readTodayData(): GarminHealthData = runCatching {
        val today  = LocalDate.now()
        val start  = today.atStartOfDay().toInstant(ZoneOffset.UTC)
        val end    = today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        val filter = TimeRangeFilter.between(start, end)

        val steps = client
            .readRecords(ReadRecordsRequest(StepsRecord::class, filter))
            .records.sumOf { it.count }
            .takeIf { it > 0 }?.toInt()

        val restingHr = client
            .readRecords(ReadRecordsRequest(RestingHeartRateRecord::class, filter))
            .records.lastOrNull()?.beatsPerMinute?.toInt()

        val hrv = client
            .readRecords(ReadRecordsRequest(HeartRateVariabilityRmssdRecord::class, filter))
            .records.lastOrNull()?.heartRateVariabilityMillis?.toFloat()

        GarminHealthData(steps = steps, restingHr = restingHr, hrv = hrv)
    }.getOrElse { GarminHealthData() }
}
