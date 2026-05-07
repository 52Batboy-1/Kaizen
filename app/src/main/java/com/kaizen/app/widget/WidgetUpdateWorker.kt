package com.kaizen.app.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.*
import java.util.concurrent.TimeUnit

class WidgetUpdateWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val manager = GlanceAppWidgetManager(applicationContext)
        listOf(KaizenTodayWidget(), KaizenStreaksWidget(), KaizenWinsWidget(), KaizenGoalsWidget()).forEach { w ->
            manager.getGlanceIds(w::class.java).forEach { gid ->
                runCatching { w.update(applicationContext, gid) }
            }
        }
        return Result.success()
    }

    companion object {
        private const val TAG = "kaizen_widget_refresh"

        fun schedule(context: Context) {
            val req = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(15, TimeUnit.MINUTES)
                .addTag(TAG)
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(false).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                req,
            )
        }
    }
}
