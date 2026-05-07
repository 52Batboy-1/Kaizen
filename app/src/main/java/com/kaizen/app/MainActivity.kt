package com.kaizen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.health.connect.client.PermissionController
import com.kaizen.app.data.*
import com.kaizen.app.ui.*
import com.kaizen.app.ui.screens.*
import com.kaizen.app.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import com.kaizen.app.widget.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db           = KaizenDatabase.getInstance(this)
        val repo         = KaizenRepository(db.dao())
        val prefs        = UserPrefs(this)
        val healthConnect = HealthConnectManager(this)
        setContent { KaizenTheme { KaizenApp(repo = repo, prefs = prefs, healthConnect = healthConnect) } }

        lifecycleScope.launch {
            runCatching {
                val manager = GlanceAppWidgetManager(this@MainActivity)
                listOf(KaizenTodayWidget(), KaizenStreaksWidget(), KaizenWinsWidget(), KaizenGoalsWidget()).forEach { w ->
                    manager.getGlanceIds(w::class.java).forEach { gid -> w.update(this@MainActivity, gid) }
                }
            }
        }
    }
}

private enum class Tab(val label: String, val icon: String) {
    TODAY("Today", "⬡"), WORKOUTS("Workouts", "◈"), HABITS("Habits", "≡"), STATS("Stats", "◉"), PROGRESS("Progress", "🏆"), COACH("Coach", "✦"),
}

@Composable
fun KaizenApp(repo: KaizenRepository, prefs: UserPrefs, healthConnect: HealthConnectManager) {
    val vm: KaizenViewModel = viewModel(factory = KaizenViewModelFactory(repo, prefs, healthConnect))

    val garminPermLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { _ -> vm.onGarminPermissionsGranted() }
    val state       by vm.state.collectAsStateWithLifecycle()
    val form        by vm.form.collectAsStateWithLifecycle()
    val journalForm by vm.journalForm.collectAsStateWithLifecycle()
    val goalForm    by vm.goalForm.collectAsStateWithLifecycle()
    val winForm     by vm.winForm.collectAsStateWithLifecycle()
    val chatInput   by vm.chatInput.collectAsStateWithLifecycle()
    var activeTab by remember { mutableStateOf(Tab.TODAY) }
    val dateLabel = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d")) }

    LaunchedEffect(Unit) {
        while (true) {
            val slot = if (LocalTime.now().hour >= 17) TimeSlot.EVENING else TimeSlot.MORNING
            vm.selectSlot(slot)
            delay(60_000L)
        }
    }

    if (!state.onboardingDone) {
        OnboardingScreen(onComplete = { name, week -> vm.completeOnboarding(name, week) })
        return
    }

    Scaffold(
        containerColor      = K.Bg,
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar           = { KaizenBottomBar(activeTab) { activeTab = it } },
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            KaizenHeader(dateLabel, state, activeTab) { vm.selectSlot(it) }
            when (activeTab) {
                Tab.TODAY -> TodayScreen(
                    state             = state,
                    habitsForSlot     = remember(state.habits, state.selectedSlot, state.today) { vm.habitsForSlot() },
                    scheduledWorkout  = remember(state.today) { vm.scheduledWorkoutToday() },
                    onToggleHabit     = { vm.toggleHabit(it) },
                    onStartWorkout    = { vm.openActiveWorkout(it) },
                    onCoreSetTapped   = { exName, count -> vm.coreSetTapped(exName, count) },
                    onRecoveryChange  = { vm.setWhoopRecovery(it) },
                    onStrainChange    = { vm.setWhoopStrain(it) },
                    onSuggestedChange = { vm.setWhoopSuggestedStrain(it) },
                    onEditHabit       = { vm.openEditHabit(it) },
                    onAddInjury             = { vm.openInjurySheet() },
                    onResolveInjury         = { inj -> vm.resolveInjury(inj) },
                    onAddHabit              = { vm.openAddHabit() },
                    onRequestGarminConnect  = { garminPermLauncher.launch(vm.garminPermissions) },
                    onRefreshGarmin         = { vm.refreshGarminData() },
                    onBodyBattery           = { vm.saveBodyBattery(it) },
                    onStressScore           = { vm.saveStressScore(it) },
                )
                Tab.WORKOUTS -> WorkoutsScreen(
                    state          = state,
                    onStartWorkout = { vm.openActiveWorkout(it) },
                    onSetTier      = { vm.setTier(it) },
                )
                Tab.HABITS   -> HabitsScreen(state = state, onDelete = { vm.deleteHabit(it) }, onAddClick = { vm.openAddHabit() })
                Tab.STATS    -> StatsScreen(state = state, weekDays = remember(state.habits, state.recentWorkouts) { vm.weeklyDays() },
                    onLogBodyweight = { kg, notes -> vm.logBodyweight(kg, notes) },
                    onLogSleep      = { hrs, hrv, q -> vm.logSleep(hrs, hrv, q) })
                Tab.PROGRESS -> ProgressionScreen(state = state, currentWeek = state.currentWeek,
                    onWeekChange = { vm.setCurrentWeek(it) }, checkedStandards = state.checkedStandards,
                    onToggleStandard = { vm.toggleStandard(it) })
                Tab.COACH    -> CoachScreen(
                    state             = state,
                    journalForm       = journalForm,
                    goalForm          = goalForm,
                    winForm           = winForm,
                    chatInput         = chatInput,
                    chatMessages      = state.chatMessages,
                    chatLoading       = state.chatLoading,
                    onChatInput       = { vm.setChatInput(it) },
                    onSendChat        = { vm.sendChat() },
                    onClearChat       = { vm.clearChat() },
                    onJournalText     = { vm.journalText(it) },
                    onJournalMood     = { vm.journalMood(it) },
                    onJournalTags     = { vm.journalTags(it) },
                    onSubmitJournal   = { vm.submitJournal() },
                    onOpenAddJournal  = { vm.openAddJournal() },
                    onOpenEditJournal = { vm.openEditJournal(it) },
                    onDismissJournal  = { vm.dismissJournal() },
                    onDeleteJournal   = { vm.deleteJournal(it) },
                    onGoalTitle       = { vm.goalTitle(it) },
                    onGoalDescription = { vm.goalDescription(it) },
                    onGoalTargetDate  = { vm.goalTargetDate(it) },
                    onSubmitGoal      = { vm.submitGoal() },
                    onOpenAddGoal     = { vm.openAddGoal() },
                    onOpenEditGoal    = { vm.openEditGoal(it) },
                    onDismissGoal     = { vm.dismissGoal() },
                    onCompleteGoal    = { vm.completeGoal(it) },
                    onDeleteGoal      = { vm.deleteGoal(it) },
                    onWinTitle        = { vm.winTitle(it) },
                    onWinDescription  = { vm.winDescription(it) },
                    onWinType         = { vm.winType(it) },
                    onSubmitWin       = { vm.submitWin() },
                    onOpenAddWin      = { vm.openAddWin() },
                    onDismissWin      = { vm.dismissWin() },
                    onDeleteWin       = { vm.deleteWin(it) },
                    onSyncToCloud     = { vm.syncToCloud() },
                )
            }
        }
    }

    if (state.showActiveWorkout && state.activeWorkoutType != null) {
        ActiveWorkoutSheet(
            workoutType     = state.activeWorkoutType!!,
            userDifficulty  = if (state.hasWhoopData) state.whoopScaledDifficulty else Difficulty.INTERMEDIATE,
            defaultSets     = if (state.hasWhoopData) state.whoopScaledSets else 4,
            defaultRestSecs = if (state.hasWhoopData) state.whoopScaledRestSeconds else 90,
            onLogPR         = { name, diff, reps -> state.activeWorkoutType?.let { vm.logPR(name, it, diff, reps) } },
            onDismiss       = { vm.closeActiveWorkout() },
        )
    }

    if (state.showInjurySheet) {
        AddInjurySheet(
            onSubmit  = { bp, side, type, sev, notes, date -> vm.logInjury(bp, side, type, sev, notes, date) },
            onDismiss = { vm.closeInjurySheet() },
        )
    }

    if (state.showAddHabitSheet) {
        val isEditing = state.editingHabit != null
        AddHabitSheet(
            form             = form,
            isEditing        = isEditing,
            onNameChange     = { vm.formName(it) },
            onCategoryChange = { vm.formCategory(it) },
            onSlotChange     = { vm.formSlot(it) },
            onSubmit         = { if (isEditing) vm.submitEditHabit() else vm.submitHabit() },
            onDelete         = if (isEditing) ({ vm.deleteHabit(state.editingHabit!!); vm.dismissAddHabit() }) else null,
            onDismiss        = { vm.dismissAddHabit() },
        )
    }
}

@Composable
private fun KaizenHeader(dateLabel: String, state: KaizenUiState, activeTab: Tab, onSlotChange: (TimeSlot) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(dateLabel.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 3.sp), color = K.GoldDim)
            Spacer(Modifier.height(2.dp))
            Row {
                Text("K", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = K.Gold, letterSpacing = (-1).sp)
                Text("aizen", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = K.Text, letterSpacing = (-1).sp)
            }
        }
        if (activeTab == Tab.TODAY) SlotPill(state.selectedSlot, onSlotChange)
    }
}

@Composable
private fun SlotPill(selected: TimeSlot, onSelect: (TimeSlot) -> Unit) {
    Surface(shape = RoundedCornerShape(100.dp), color = K.Card2, border = BorderStroke(1.dp, K.Border)) {
        Row(Modifier.padding(3.dp)) {
            listOf(TimeSlot.MORNING to "☀️ Day", TimeSlot.EVENING to "🌙 Night").forEach { (slot, lbl) ->
                val active = selected == slot
                val bg = if (active) (if (slot == TimeSlot.MORNING) K.Morning else K.Night) else Color.Transparent
                val fg = if (active) (if (slot == TimeSlot.MORNING) Color(0xFF03382D) else Color(0xFF1A0060)) else K.Muted
                Surface(modifier = Modifier.clip(RoundedCornerShape(100.dp)).clickable { onSelect(slot) }, shape = RoundedCornerShape(100.dp), color = bg) {
                    Text(lbl, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = fg)
                }
            }
        }
    }
}

@Composable
private fun KaizenBottomBar(activeTab: Tab, onSelect: (Tab) -> Unit) {
    Surface(color = K.Card2, border = BorderStroke(1.dp, K.Border)) {
        Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(vertical = 8.dp)) {
            Tab.values().forEach { tab ->
                val active = tab == activeTab
                val color by animateColorAsState(if (active) K.Gold else K.Muted, tween(200), label = "tabColor")
                Column(modifier = Modifier.weight(1f).clickable { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(tab.icon, fontSize = if (tab == Tab.COACH) 17.sp else 19.sp, color = color)
                    Text(tab.label, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp, color = color)
                }
            }
        }
    }
}
