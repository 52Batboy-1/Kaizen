package com.kaizen.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.kaizen.app.notifications.ReminderScheduler
import com.kaizen.app.widget.WidgetUpdateWorker
import com.openwearables.health.sdk.OpenWearablesHealthSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KaizenApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel("kaizen_morning", "Morning Check-in", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Daily morning habit & workout reminder" }
        )
        nm.createNotificationChannel(
            NotificationChannel("kaizen_evening", "Evening Check-in", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Daily evening habit reminder" }
        )

        // Open Wearables SDK — unified Health Connect sync
        val ow = OpenWearablesHealthSDK.initialize(this)
        ow.configure(host = BuildConfig.OW_BASE_URL)
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                ow.signIn(
                    userId       = "kaizen-jordan",
                    accessToken  = null,
                    refreshToken = null,
                    apiKey       = BuildConfig.OW_API_KEY,
                )
            }
        }

        ReminderScheduler.schedule(this)
        WidgetUpdateWorker.schedule(this)
    }
}
