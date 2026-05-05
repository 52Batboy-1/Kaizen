package com.kaizen.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.kaizen.app.MainActivity
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

const val CHANNEL_MORNING = "kaizen_morning"
const val CHANNEL_EVENING = "kaizen_evening"

fun createNotificationChannels(context: Context) {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    listOf(
        NotificationChannel(CHANNEL_MORNING, "Morning Check-in", NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = "Daily morning habit & workout reminder" },
        NotificationChannel(CHANNEL_EVENING, "Evening Check-in", NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = "Daily evening habit reminder" },
    ).forEach { nm.createNotificationChannel(it) }
}

class MorningReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(
            1001,
            NotificationCompat.Builder(applicationContext, CHANNEL_MORNING)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("☀️ Good morning, Kaizen!")
                .setContentText("Check your recovery, log your Whoop data, and start your day.")
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build(),
        )
        return Result.success()
    }
}

class EveningReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(
            1002,
            NotificationCompat.Builder(applicationContext, CHANNEL_EVENING)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🌙 Evening habits")
                .setContentText("Don't forget your evening habits before bed!")
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build(),
        )
        return Result.success()
    }
}

object ReminderScheduler {
    fun schedule(context: Context, morningHour: Int = 8, eveningHour: Int = 20) {
        val wm = WorkManager.getInstance(context)
        wm.cancelUniqueWork("kaizen_morning")
        wm.cancelUniqueWork("kaizen_evening")

        fun minutesUntil(hour: Int): Long {
            val now    = LocalDateTime.now()
            var target = now.withHour(hour).withMinute(0).withSecond(0).withNano(0)
            if (!target.isAfter(now)) target = target.plusDays(1)
            return java.time.Duration.between(now, target).toMinutes()
        }

        wm.enqueueUniquePeriodicWork(
            "kaizen_morning",
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<MorningReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(minutesUntil(morningHour), TimeUnit.MINUTES)
                .build(),
        )
        wm.enqueueUniquePeriodicWork(
            "kaizen_evening",
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<EveningReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(minutesUntil(eveningHour), TimeUnit.MINUTES)
                .build(),
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).apply {
            cancelUniqueWork("kaizen_morning")
            cancelUniqueWork("kaizen_evening")
        }
    }
}
