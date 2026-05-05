package com.kaizen.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.kaizen.app.notifications.ReminderScheduler

class KaizenApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Create notification channels inline to avoid duplicate function conflict
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel("kaizen_morning", "Morning Check-in", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Daily morning habit & workout reminder" }
        )
        nm.createNotificationChannel(
            NotificationChannel("kaizen_evening", "Evening Check-in", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Daily evening habit reminder" }
        )

        ReminderScheduler.schedule(this)
    }
}
