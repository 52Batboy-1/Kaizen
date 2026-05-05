package com.kaizen.app.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.action.*
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.kaizen.app.MainActivity
import com.kaizen.app.data.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import androidx.compose.ui.graphics.Color

class KaizenWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db   = KaizenDatabase.getInstance(context)
        val dao  = db.dao()
        val date = LocalDate.now().toString()

        val workouts  = dao.allWorkouts()
        val latestBW  = dao.latestBodyweight()
        val todayLog  = workouts.firstOrNull { it.date == date }
        val sessions  = workouts.filter {
            val d = runCatching { LocalDate.parse(it.date) }.getOrNull() ?: return@filter false
            ChronoUnit.DAYS.between(d, LocalDate.now()) <= 30
        }.size

        val epoch     = LocalDate.of(2024, 1, 1)
        val dayOffset = ChronoUnit.DAYS.between(epoch, LocalDate.now()).toInt()
        val rotation  = listOf(WorkoutType.PUSH, WorkoutType.PULL, WorkoutType.LEGS, null)
        val scheduled = rotation[((dayOffset % rotation.size) + rotation.size) % rotation.size]

        provideContent {
            Box(
                modifier         = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF04040C))
                    .cornerRadius(20)
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.TopStart,
            ) {
                Column(
                    modifier          = GlanceModifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    // Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Kaizen", style = TextStyle(
                            color = ColorProvider(Color(0xFFC9A84C)),
                            fontSize = androidx.compose.ui.unit.TextUnit(22f, androidx.compose.ui.unit.TextUnitType.Sp),
                            fontWeight = FontWeight.Bold,
                        ))
                        Spacer(GlanceModifier.defaultWeight())
                        Text(
                            LocalDate.now().dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF5A5470)),
                                fontSize = androidx.compose.ui.unit.TextUnit(11f, androidx.compose.ui.unit.TextUnitType.Sp),
                            )
                        )
                    }

                    Spacer(GlanceModifier.height(12.dp))

                    // Workout
                    if (scheduled != null) {
                        Text(
                            "${scheduled.emoji}  ${scheduled.label} Day",
                            style = TextStyle(
                                color      = ColorProvider(Color(0xFFF0ECDF)),
                                fontSize   = androidx.compose.ui.unit.TextUnit(16f, androidx.compose.ui.unit.TextUnitType.Sp),
                                fontWeight = FontWeight.Bold,
                            )
                        )
                        Spacer(GlanceModifier.height(4.dp))
                        Text(
                            scheduled.muscles,
                            style = TextStyle(
                                color    = ColorProvider(Color(0xFF5A5470)),
                                fontSize = androidx.compose.ui.unit.TextUnit(10f, androidx.compose.ui.unit.TextUnitType.Sp),
                            )
                        )
                        Spacer(GlanceModifier.height(10.dp))
                        Text(
                            if (todayLog != null) "▶ IN PROGRESS" else "TAP TO START →",
                            style = TextStyle(
                                color      = ColorProvider(if (todayLog != null) Color(0xFF00DBA8) else Color(0xFF04040C)),
                                fontSize   = androidx.compose.ui.unit.TextUnit(11f, androidx.compose.ui.unit.TextUnitType.Sp),
                                fontWeight = FontWeight.Bold,
                            ),
                            modifier = GlanceModifier
                                .background(if (todayLog != null) Color(0x2600DBA8) else Color(0xFFC9A84C))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .cornerRadius(10),
                        )
                    } else {
                        Text("Rest day 😴", style = TextStyle(
                            color = ColorProvider(Color(0xFF5A5470)),
                            fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp),
                        ))
                    }

                    Spacer(GlanceModifier.defaultWeight())

                    // Stats row
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Column(modifier = GlanceModifier.defaultWeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$sessions", style = TextStyle(
                                color = ColorProvider(Color(0xFFFF9F43)),
                                fontSize = androidx.compose.ui.unit.TextUnit(18f, androidx.compose.ui.unit.TextUnitType.Sp),
                                fontWeight = FontWeight.Bold,
                            ))
                            Text("sessions", style = TextStyle(
                                color = ColorProvider(Color(0xFF5A5470)),
                                fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp),
                            ))
                        }
                        Column(modifier = GlanceModifier.defaultWeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (latestBW != null) "${"%.1f".format(latestBW.weightKg)}kg" else "—",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF60A5FA)),
                                    fontSize = androidx.compose.ui.unit.TextUnit(18f, androidx.compose.ui.unit.TextUnitType.Sp),
                                    fontWeight = FontWeight.Bold,
                                )
                            )
                            Text("bodyweight", style = TextStyle(
                                color = ColorProvider(Color(0xFF5A5470)),
                                fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp),
                            ))
                        }
                    }
                }
            }
        }
    }
}

class KaizenWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KaizenWidget()
}
