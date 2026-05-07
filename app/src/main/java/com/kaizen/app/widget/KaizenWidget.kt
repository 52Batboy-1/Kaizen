package com.kaizen.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.action.*
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.kaizen.app.MainActivity
import com.kaizen.app.data.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.first

private fun tabIntent(context: Context, tab: String) =
    Intent(context, MainActivity::class.java).apply {
        putExtra(MainActivity.EXTRA_TAB, tab)
        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

// ── Shared colours ────────────────────────────────────────────────────────────

private val BG     = Color(0xFF04040C)
private val BG2    = Color(0xFF0D0D18)
private val GOLD   = Color(0xFFC9A84C)
private val TEXT   = Color(0xFFF0ECDF)
private val MUTED  = Color(0xFF5A5470)
private val GREEN  = Color(0xFF00DBA8)
private val ORANGE = Color(0xFFFF9F43)
private val BLUE   = Color(0xFF60A5FA)
private val RED    = Color(0xFFF87171)

private fun sp(v: Float) = androidx.compose.ui.unit.TextUnit(v, androidx.compose.ui.unit.TextUnitType.Sp)

// ── 1. Today Widget ───────────────────────────────────────────────────────────

class KaizenTodayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        var errorMsg: String? = null
        var workouts: List<WorkoutLog> = emptyList()
        var latestBW: com.kaizen.app.data.BodyweightEntry? = null
        var habits: List<com.kaizen.app.data.HabitWithCompletions> = emptyList()
        var tier = KaizenTier.FOUNDATION
        var currentWeek = 1
        try {
            val db    = KaizenDatabase.getInstance(context)
            val dao   = db.dao()
            val prefs = UserPrefs(context)
            workouts    = dao.allWorkouts()
            latestBW    = dao.latestBodyweight()
            habits      = dao.allHabitsWithCompletions().first()
            tier        = prefs.currentTier.first()
            currentWeek = prefs.currentWeek.first()
        } catch (e: Exception) {
            errorMsg = e.javaClass.simpleName + ": " + (e.message?.take(80) ?: "?")
        }

        val date      = LocalDate.now().toString()
        val today     = LocalDate.now()
        val todayLog  = workouts.firstOrNull { it.date == date }
        val sessions  = workouts.count {
            runCatching { ChronoUnit.DAYS.between(LocalDate.parse(it.date), today) <= 30 }.getOrDefault(false)
        }
        val scheduled = scheduledWorkoutForWidget(today, tier, currentWeek)
        val doneToday = habits.count { hwc -> hwc.completions.any { it.date == date } }
        val tap = actionStartActivity(tabIntent(context, "TODAY"))

        provideContent {
            if (errorMsg != null) {
                Box(modifier = GlanceModifier.fillMaxSize().background(BG).cornerRadius(20.dp).clickable(tap)) {
                    Text(errorMsg!!, style = TextStyle(color = ColorProvider(RED), fontSize = sp(10f)), modifier = GlanceModifier.padding(12.dp))
                }
                return@provideContent
            }
            Box(
                modifier         = GlanceModifier
                    .fillMaxSize()
                    .background(BG)
                    .cornerRadius(20.dp)
                    .clickable(tap),
                contentAlignment = Alignment.TopStart,
            ) {
                Column(
                    modifier          = GlanceModifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("K", style = TextStyle(color = ColorProvider(GOLD), fontSize = sp(20f), fontWeight = FontWeight.Bold))
                        Text("aizen", style = TextStyle(color = ColorProvider(TEXT), fontSize = sp(20f), fontWeight = FontWeight.Bold))
                        Spacer(GlanceModifier.defaultWeight())
                        Text(
                            today.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                            style = TextStyle(color = ColorProvider(MUTED), fontSize = sp(11f))
                        )
                    }

                    Spacer(GlanceModifier.height(10.dp))

                    if (scheduled != null) {
                        Text(
                            "${scheduled.emoji}  ${scheduled.label} Day",
                            style = TextStyle(color = ColorProvider(TEXT), fontSize = sp(15f), fontWeight = FontWeight.Bold)
                        )
                        Spacer(GlanceModifier.height(3.dp))
                        Text(scheduled.muscles, style = TextStyle(color = ColorProvider(MUTED), fontSize = sp(10f)))
                        Spacer(GlanceModifier.height(8.dp))
                        Text(
                            if (todayLog != null) "▶ IN PROGRESS" else "TAP TO START →",
                            style = TextStyle(
                                color      = ColorProvider(if (todayLog != null) GREEN else BG),
                                fontSize   = sp(11f),
                                fontWeight = FontWeight.Bold,
                            ),
                            modifier = GlanceModifier
                                .background(if (todayLog != null) Color(0x2600DBA8) else GOLD)
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                                .cornerRadius(10.dp),
                        )
                    } else {
                        Text("Rest day 😴", style = TextStyle(color = ColorProvider(MUTED), fontSize = sp(14f)))
                    }

                    Spacer(GlanceModifier.defaultWeight())

                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Column(modifier = GlanceModifier.defaultWeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$doneToday/${habits.size}", style = TextStyle(color = ColorProvider(GREEN), fontSize = sp(17f), fontWeight = FontWeight.Bold))
                            Text("habits", style = TextStyle(color = ColorProvider(MUTED), fontSize = sp(9f)))
                        }
                        Column(modifier = GlanceModifier.defaultWeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$sessions", style = TextStyle(color = ColorProvider(ORANGE), fontSize = sp(17f), fontWeight = FontWeight.Bold))
                            Text("sessions", style = TextStyle(color = ColorProvider(MUTED), fontSize = sp(9f)))
                        }
                        Column(modifier = GlanceModifier.defaultWeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (latestBW != null) "${"%.1f".format(latestBW.weightKg)}kg" else "—",
                                style = TextStyle(color = ColorProvider(BLUE), fontSize = sp(17f), fontWeight = FontWeight.Bold)
                            )
                            Text("weight", style = TextStyle(color = ColorProvider(MUTED), fontSize = sp(9f)))
                        }
                    }
                }
            }
        }
    }
}

class KaizenWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KaizenTodayWidget()
}

// ── 2. Streaks Widget ─────────────────────────────────────────────────────────

class KaizenStreaksWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        var errorMsg: String? = null
        var habits: List<com.kaizen.app.data.HabitWithCompletions> = emptyList()
        try {
            val dao = KaizenDatabase.getInstance(context).dao()
            habits = dao.allHabitsWithCompletions().first()
                .sortedByDescending { it.habit.streak }
                .take(5)
        } catch (e: Exception) {
            errorMsg = e.javaClass.simpleName + ": " + (e.message?.take(80) ?: "?")
        }

        val date = LocalDate.now().toString()
        val tap = actionStartActivity(tabIntent(context, "HABITS"))

        provideContent {
            if (errorMsg != null) {
                Box(modifier = GlanceModifier.fillMaxSize().background(BG).cornerRadius(20.dp).clickable(tap)) {
                    Text(errorMsg!!, style = TextStyle(color = ColorProvider(RED), fontSize = sp(10f)), modifier = GlanceModifier.padding(12.dp))
                }
                return@provideContent
            }
            Box(
                modifier         = GlanceModifier
                    .fillMaxSize()
                    .background(BG)
                    .cornerRadius(20.dp)
                    .clickable(tap),
                contentAlignment = Alignment.TopStart,
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥", style = TextStyle(fontSize = sp(16f)))
                        Spacer(GlanceModifier.width(6.dp))
                        Text("Streaks", style = TextStyle(color = ColorProvider(GOLD), fontSize = sp(16f), fontWeight = FontWeight.Bold))
                    }

                    Spacer(GlanceModifier.height(10.dp))

                    if (habits.isEmpty()) {
                        Text("No habits yet — add some in the app.", style = TextStyle(color = ColorProvider(MUTED), fontSize = sp(11f)))
                    } else {
                        habits.forEach { hwc ->
                            val doneToday = hwc.completions.any { it.date == date }
                            val streakColor = when {
                                hwc.habit.streak >= 14 -> ORANGE
                                hwc.habit.streak >= 7  -> GOLD
                                else                   -> BLUE
                            }
                            Row(
                                modifier          = GlanceModifier.fillMaxWidth().padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    if (doneToday) "✓" else "·",
                                    style = TextStyle(
                                        color      = ColorProvider(if (doneToday) GREEN else MUTED),
                                        fontSize   = sp(13f),
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    modifier = GlanceModifier.width(16.dp),
                                )
                                Text(
                                    hwc.habit.name,
                                    style    = TextStyle(color = ColorProvider(if (doneToday) TEXT else MUTED), fontSize = sp(12f)),
                                    modifier = GlanceModifier.defaultWeight(),
                                )
                                Text(
                                    "${hwc.habit.streak}d",
                                    style = TextStyle(color = ColorProvider(streakColor), fontSize = sp(12f), fontWeight = FontWeight.Bold),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class KaizenStreaksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KaizenStreaksWidget()
}

// ── 3. Wins & Lessons Widget ──────────────────────────────────────────────────

class KaizenWinsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        var errorMsg: String? = null
        var latestWin: Win? = null
        var latestLesson: Win? = null
        try {
            val all = KaizenDatabase.getInstance(context).dao().wins().first()
            latestWin    = all.lastOrNull { it.type == WinType.WIN }
            latestLesson = all.lastOrNull { it.type == WinType.LOSS }
        } catch (e: Exception) {
            errorMsg = e.javaClass.simpleName + ": " + (e.message?.take(80) ?: "?")
        }
        val tap = actionStartActivity(tabIntent(context, "COACH"))

        provideContent {
            if (errorMsg != null) {
                Box(modifier = GlanceModifier.fillMaxSize().background(BG).cornerRadius(20.dp).clickable(tap)) {
                    Text(errorMsg!!, style = TextStyle(color = ColorProvider(RED), fontSize = sp(10f)), modifier = GlanceModifier.padding(12.dp))
                }
                return@provideContent
            }
            Box(
                modifier         = GlanceModifier
                    .fillMaxSize()
                    .background(BG)
                    .cornerRadius(20.dp)
                    .clickable(tap),
                contentAlignment = Alignment.TopStart,
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    // Row 1: header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏆", style = TextStyle(fontSize = sp(15f)))
                        Spacer(GlanceModifier.width(6.dp))
                        Text("Wins & Lessons", style = TextStyle(color = ColorProvider(GOLD), fontSize = sp(15f), fontWeight = FontWeight.Bold))
                    }
                    // Row 2: spacer
                    Spacer(GlanceModifier.height(10.dp))
                    // Row 3: WIN section (nested column, max 4 children)
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        Text("WIN", style = TextStyle(color = ColorProvider(GREEN), fontSize = sp(8f), fontWeight = FontWeight.Bold))
                        if (latestWin != null) {
                            Text(
                                "${latestWin.title}  ·  ${latestWin.date}",
                                style = TextStyle(color = ColorProvider(TEXT), fontSize = sp(12f), fontWeight = FontWeight.Bold),
                            )
                            if (latestWin.description.isNotBlank()) {
                                Text(latestWin.description, style = TextStyle(color = ColorProvider(MUTED), fontSize = sp(10f)))
                            }
                        } else {
                            Text("No wins logged yet", style = TextStyle(color = ColorProvider(MUTED), fontSize = sp(11f)))
                        }
                    }
                    // Row 4: spacer
                    Spacer(GlanceModifier.height(8.dp))
                    // Row 5: LESSON section (nested column, max 4 children)
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        Text("LESSON", style = TextStyle(color = ColorProvider(BLUE), fontSize = sp(8f), fontWeight = FontWeight.Bold))
                        if (latestLesson != null) {
                            Text(
                                "${latestLesson.title}  ·  ${latestLesson.date}",
                                style = TextStyle(color = ColorProvider(TEXT), fontSize = sp(12f), fontWeight = FontWeight.Bold),
                            )
                            if (latestLesson.description.isNotBlank()) {
                                Text(latestLesson.description, style = TextStyle(color = ColorProvider(MUTED), fontSize = sp(10f)))
                            }
                        } else {
                            Text("No lessons logged yet", style = TextStyle(color = ColorProvider(MUTED), fontSize = sp(11f)))
                        }
                    }
                    // Row 6: fill weight
                    Spacer(GlanceModifier.defaultWeight())
                    // Row 7: footer
                    Text("+ Log in app →", style = TextStyle(color = ColorProvider(GOLD), fontSize = sp(10f), fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

class KaizenWinsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KaizenWinsWidget()
}

// ── 4. Goals Countdown Widget ─────────────────────────────────────────────

class KaizenGoalsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        var errorMsg: String? = null
        var goals: List<Pair<com.kaizen.app.data.Goal, Int>> = emptyList()
        try {
            val today = java.time.LocalDate.now()
            goals = KaizenDatabase.getInstance(context).dao().goals().first()
                .filter { it.status == com.kaizen.app.data.GoalStatus.ACTIVE && it.targetDate.isNotBlank() }
                .mapNotNull { g ->
                    runCatching {
                        val days = java.time.temporal.ChronoUnit.DAYS.between(today, java.time.LocalDate.parse(g.targetDate)).toInt()
                        if (days >= 0) g to days else null
                    }.getOrNull()
                }
                .sortedBy { it.second }
                .take(4)
        } catch (e: Exception) {
            errorMsg = e.javaClass.simpleName + ": " + (e.message?.take(80) ?: "?")
        }
        val tap = actionStartActivity(tabIntent(context, "COACH"))

        provideContent {
            if (errorMsg != null) {
                Box(modifier = GlanceModifier.fillMaxSize().background(BG).cornerRadius(20.dp).clickable(tap)) {
                    Text(errorMsg!!, style = TextStyle(color = ColorProvider(RED), fontSize = sp(10f)), modifier = GlanceModifier.padding(12.dp))
                }
                return@provideContent
            }
            Box(
                modifier         = GlanceModifier
                    .fillMaxSize()
                    .background(BG)
                    .cornerRadius(20.dp)
                    .clickable(tap),
                contentAlignment = Alignment.TopStart,
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎯", style = TextStyle(fontSize = sp(15f)))
                        Spacer(GlanceModifier.width(6.dp))
                        Text("Goal Countdown", style = TextStyle(color = ColorProvider(GOLD), fontSize = sp(15f), fontWeight = FontWeight.Bold))
                    }

                    Spacer(GlanceModifier.height(10.dp))

                    if (goals.isEmpty()) {
                        Text("No active goals with deadlines.", style = TextStyle(color = ColorProvider(MUTED), fontSize = sp(11f)))
                    } else {
                        goals.forEach { (goal, days) ->
                            val urgentColor = when {
                                days <= 7  -> RED
                                days <= 30 -> ORANGE
                                else       -> GOLD
                            }
                            Row(
                                modifier          = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier         = GlanceModifier
                                        .width(44.dp)
                                        .background(urgentColor.copy(alpha = 0.18f))
                                        .cornerRadius(8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = GlanceModifier.padding(4.dp)) {
                                        Text("$days", style = TextStyle(color = ColorProvider(urgentColor), fontSize = sp(16f), fontWeight = FontWeight.Bold))
                                        Text(if (days == 1) "day" else "days", style = TextStyle(color = ColorProvider(urgentColor.copy(alpha = 0.7f)), fontSize = sp(7f)))
                                    }
                                }
                                Spacer(GlanceModifier.width(10.dp))
                                Text(goal.title, style = TextStyle(color = ColorProvider(TEXT), fontSize = sp(12f), fontWeight = FontWeight.Medium), modifier = GlanceModifier.defaultWeight())
                            }
                        }
                    }
                }
            }
        }
    }
}

class KaizenGoalsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KaizenGoalsWidget()
}

// ── Shared scheduling helper ──────────────────────────────────────────────────

private fun scheduledWorkoutForWidget(date: LocalDate, tier: KaizenTier, currentWeek: Int): WorkoutType? {
    val epoch     = LocalDate.of(2024, 1, 1)
    val dayOffset = ChronoUnit.DAYS.between(epoch, date).toInt()
    val dow       = date.dayOfWeek
    return when (tier) {
        KaizenTier.FOUNDATION -> when (dow) {
            java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.WEDNESDAY, java.time.DayOfWeek.FRIDAY -> WorkoutType.FULL_BODY
            else -> null
        }
        KaizenTier.DEVELOPMENT -> when (dow) {
            java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.THURSDAY -> WorkoutType.PUSH
            java.time.DayOfWeek.TUESDAY, java.time.DayOfWeek.FRIDAY  -> WorkoutType.LEGS
            else -> null
        }
        KaizenTier.STRENGTH -> {
            val ppl = listOf(WorkoutType.PUSH, WorkoutType.PULL, WorkoutType.LEGS, null, null)
            ppl[((dayOffset % 5) + 5) % 5]
        }
        KaizenTier.MASTERY -> {
            val cycle = listOf(WorkoutType.PUSH, WorkoutType.PULL, WorkoutType.LEGS, WorkoutType.FULL_BODY, null, null)
            cycle[((dayOffset % 6) + 6) % 6]
        }
    }
}
