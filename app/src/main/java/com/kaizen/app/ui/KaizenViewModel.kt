package com.kaizen.app.ui

import androidx.lifecycle.*
import com.kaizen.app.data.*
import com.kaizen.app.data.GarminEntry
import com.kaizen.app.data.HealthConnectManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import java.time.temporal.ChronoUnit

data class WeekDay(val date: String, val label: String, val habitPct: Float, val hadWorkout: Boolean, val isToday: Boolean)

data class ChatMessage(val role: String, val content: String)

data class JournalForm(val text: String = "", val mood: Int = 3, val tags: String = "")
data class GoalForm(val title: String = "", val description: String = "", val targetDate: String = "")
data class WinForm(val title: String = "", val description: String = "", val type: WinType = WinType.WIN)
data class SlipForm(val body: String = "")

data class KaizenUiState(
    val habits: List<HabitWithCompletions>      = emptyList(),
    val recentWorkouts: List<WorkoutLog>         = emptyList(),
    val todayWorkoutLog: WorkoutLog?             = null,
    val selectedSlot: TimeSlot                   = if (LocalTime.now().hour >= 17) TimeSlot.EVENING else TimeSlot.MORNING,
    val today: String                            = LocalDate.now().toString(),
    val whoopRecoveryInput: String               = "",
    val whoopStrainInput: String                 = "",
    val whoopSuggestedStrainInput: String        = "",
    val showAddHabitSheet: Boolean               = false,
    val showActiveWorkout: Boolean               = false,
    val activeWorkoutType: WorkoutType?          = null,
    val editingHabit: HabitWithCompletions?      = null,
    val isLoading: Boolean                       = true,
    val personalRecords: List<PersonalRecord>    = emptyList(),
    val bodyweightEntries: List<BodyweightEntry> = emptyList(),
    val sleepEntries: List<SleepEntry>           = emptyList(),
    val activeInjuries: List<InjuryLog>          = emptyList(),
    val allInjuries: List<InjuryLog>             = emptyList(),
    val showInjurySheet: Boolean                 = false,
    val onboardingDone: Boolean                  = false,
    val userName: String                         = "",
    val currentWeek: Int                         = 1,
    val checkedStandards: Set<String>            = emptySet(),
    val journalEntries: List<JournalEntry>       = emptyList(),
    val goals: List<Goal>                        = emptyList(),
    val wins: List<Win>                          = emptyList(),
    val slipEntries: List<SlipEntry>             = emptyList(),
    val currentTier: KaizenTier                  = KaizenTier.FOUNDATION,
    val showAddJournal: Boolean                  = false,
    val showAddGoal: Boolean                     = false,
    val showAddWin: Boolean                      = false,
    val editingJournal: JournalEntry?            = null,
    val editingGoal: Goal?                       = null,
    val chatMessages: List<ChatMessage>          = emptyList(),
    val chatLoading: Boolean                     = false,
    val isSyncing: Boolean                       = false,
    val syncResult: Boolean?                     = null,
    val garminEntry: GarminEntry?                = null,
    val garminConnected: Boolean                 = false,
    val isLoadingGarmin: Boolean                 = false,
) {
    val whoopRecovery: Int?          get() = whoopRecoveryInput.toIntOrNull()?.coerceIn(0, 100)
    val whoopStrain: Float?          get() = whoopStrainInput.toFloatOrNull()?.coerceIn(0f, 21f)
    val whoopSuggestedStrain: Float? get() = whoopSuggestedStrainInput.toFloatOrNull()?.coerceIn(0f, 21f)
    val whoopZone: WhoopZone get() = when {
        (whoopRecovery ?: -1) >= 67 -> WhoopZone.GREEN
        (whoopRecovery ?: -1) >= 34 -> WhoopZone.YELLOW
        whoopRecovery != null        -> WhoopZone.RED
        else                         -> WhoopZone.GREEN
    }
    val hasWhoopData: Boolean get() = whoopRecovery != null
    val whoopScaledDifficulty: Difficulty get() = when (whoopZone) {
        WhoopZone.GREEN -> Difficulty.ADVANCED
        WhoopZone.YELLOW -> Difficulty.INTERMEDIATE
        WhoopZone.RED -> Difficulty.BEGINNER
    }
    val whoopScaledSets: Int get() = when (whoopZone) {
        WhoopZone.GREEN -> 5; WhoopZone.YELLOW -> 4; WhoopZone.RED -> 3
    }
    val whoopScaledRestSeconds: Int get() = when (whoopZone) {
        WhoopZone.GREEN -> 90; WhoopZone.YELLOW -> 120; WhoopZone.RED -> 150
    }
}

data class AddHabitForm(val name: String = "", val category: HabitCategory = HabitCategory.HEALTH, val slot: TimeSlot = TimeSlot.MORNING)

fun scheduledWorkoutForDate(date: String, tier: KaizenTier = KaizenTier.FOUNDATION): WorkoutType? {
    val target  = LocalDate.parse(date)
    val dow     = target.dayOfWeek  // MONDAY=1 … SUNDAY=7
    return when (tier) {
        KaizenTier.FOUNDATION -> when (dow) {
            DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY -> WorkoutType.FULL_BODY
            else -> null
        }
        KaizenTier.DEVELOPMENT -> when (dow) {
            DayOfWeek.MONDAY, DayOfWeek.THURSDAY -> WorkoutType.PUSH
            DayOfWeek.TUESDAY, DayOfWeek.FRIDAY  -> WorkoutType.LEGS
            else -> null
        }
        KaizenTier.STRENGTH -> {
            val epoch     = LocalDate.of(2024, 1, 1)
            val dayOffset = ChronoUnit.DAYS.between(epoch, target).toInt()
            val rotation  = listOf(WorkoutType.PUSH, WorkoutType.PULL, WorkoutType.LEGS, null, null)
            rotation[((dayOffset % rotation.size) + rotation.size) % rotation.size]
        }
        KaizenTier.MASTERY -> {
            val epoch     = LocalDate.of(2024, 1, 1)
            val dayOffset = ChronoUnit.DAYS.between(epoch, target).toInt()
            val rotation  = listOf(WorkoutType.PUSH, WorkoutType.PULL, WorkoutType.LEGS, WorkoutType.FULL_BODY, null, null)
            rotation[((dayOffset % rotation.size) + rotation.size) % rotation.size]
        }
    }
}

class KaizenViewModel(
    private val repo: KaizenRepository,
    private val prefs: UserPrefs,
    private val hcm: HealthConnectManager,
    private val owClient: OWHealthClient,
) : ViewModel() {

    private val _state       = MutableStateFlow(KaizenUiState())
    val state: StateFlow<KaizenUiState> = _state.asStateFlow()
    private val _form        = MutableStateFlow(AddHabitForm())
    val form: StateFlow<AddHabitForm> = _form.asStateFlow()
    private val _journalForm = MutableStateFlow(JournalForm())
    val journalForm: StateFlow<JournalForm> = _journalForm.asStateFlow()
    private val _goalForm    = MutableStateFlow(GoalForm())
    val goalForm: StateFlow<GoalForm> = _goalForm.asStateFlow()
    private val _winForm     = MutableStateFlow(WinForm())
    val winForm: StateFlow<WinForm> = _winForm.asStateFlow()
    private val _slipForm    = MutableStateFlow(SlipForm())
    val slipForm: StateFlow<SlipForm> = _slipForm.asStateFlow()
    private val _chatInput   = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repo.habits, repo.recentWorkouts) { habits, workouts -> habits to workouts }
                .collect { (habits, workouts) ->
                    val today = _state.value.today
                    _state.update { s ->
                        s.copy(habits = habits, recentWorkouts = workouts,
                            todayWorkoutLog = workouts.firstOrNull { it.date == today },
                            isLoading = false)
                    }
                }
        }
        viewModelScope.launch { repo.personalRecords.collect  { v -> _state.update { it.copy(personalRecords = v) } } }
        viewModelScope.launch { repo.bodyweightEntries.collect { v -> _state.update { it.copy(bodyweightEntries = v) } } }
        viewModelScope.launch { repo.sleepEntries.collect      { v -> _state.update { it.copy(sleepEntries = v) } } }
        viewModelScope.launch { repo.activeInjuries.collect    { v -> _state.update { it.copy(activeInjuries = v) } } }
        viewModelScope.launch { repo.allInjuries.collect       { v -> _state.update { it.copy(allInjuries = v) } } }
        viewModelScope.launch { repo.journalEntries.collect    { v -> _state.update { it.copy(journalEntries = v) } } }
        viewModelScope.launch { repo.goals.collect             { v -> _state.update { it.copy(goals = v) } } }
        viewModelScope.launch { repo.wins.collect              { v -> _state.update { it.copy(wins = v) } } }
        viewModelScope.launch { repo.slipEntries.collect       { v -> _state.update { it.copy(slipEntries = v) } } }
        viewModelScope.launch { prefs.currentTier.collect      { v -> _state.update { it.copy(currentTier = v) } } }
        viewModelScope.launch { prefs.onboardingDone.collect   { v -> _state.update { it.copy(onboardingDone = v) } } }
        viewModelScope.launch { prefs.userName.collect         { v -> _state.update { it.copy(userName = v) } } }
        viewModelScope.launch { prefs.currentWeek.collect      { v -> _state.update { it.copy(currentWeek = v) } } }
        viewModelScope.launch {
            _state.first { !it.isLoading }
            repo.pullFromCloud()
        }
        viewModelScope.launch { repo.garminEntryToday.collect { v -> _state.update { it.copy(garminEntry = v) } } }
        viewModelScope.launch {
            val connected = hcm.isAvailable() && hcm.hasPermissions()
            _state.update { it.copy(garminConnected = connected) }
            if (connected) refreshGarminData()
        }
        // Garmin Health API — uncomment after enabling WearableSync.kt + KaizenRepository stub:
        // viewModelScope.launch { repo.syncGarminFromCloud(_state.value.today) }
    }

    // ── Slot / Habit ──────────────────────────────────────────────────────

    fun selectSlot(slot: TimeSlot) = _state.update { it.copy(selectedSlot = slot) }
    fun openAddHabit()             = _state.update { it.copy(showAddHabitSheet = true) }
    fun dismissAddHabit() {
        _state.update { it.copy(showAddHabitSheet = false, editingHabit = null) }
        _form.value = AddHabitForm()
    }
    fun formName(v: String)            = _form.update { it.copy(name = v) }
    fun formCategory(v: HabitCategory) = _form.update { it.copy(category = v) }
    fun formSlot(v: TimeSlot)          = _form.update { it.copy(slot = v) }

    fun openEditHabit(hwc: HabitWithCompletions) {
        _form.value = AddHabitForm(name = hwc.habit.name, category = hwc.habit.category, slot = hwc.habit.timeSlot)
        _state.update { it.copy(editingHabit = hwc, showAddHabitSheet = true) }
    }
    fun submitHabit() {
        val f = _form.value; if (f.name.isBlank()) return
        viewModelScope.launch { repo.addHabit(f.name.trim(), f.category, f.slot); dismissAddHabit() }
    }
    fun submitEditHabit() {
        val f = _form.value; if (f.name.isBlank()) return
        val hwc = _state.value.editingHabit ?: return
        viewModelScope.launch {
            repo.updateHabit(hwc.habit.copy(name = f.name.trim(), category = f.category, timeSlot = f.slot))
            dismissAddHabit()
        }
    }
    fun deleteHabit(hwc: HabitWithCompletions) = viewModelScope.launch { repo.deleteHabit(hwc.habit) }
    fun toggleHabit(hwc: HabitWithCompletions) {
        val today = _state.value.today
        viewModelScope.launch {
            if (hwc.isCompletedOn(today)) repo.markHabitUndone(hwc.habit, today)
            else repo.markHabitDone(hwc.habit, today)
        }
    }

    // ── Whoop ─────────────────────────────────────────────────────────────

    fun setWhoopRecovery(input: String)        = _state.update { it.copy(whoopRecoveryInput = input.filter { c -> c.isDigit() }.take(3)) }
    fun setWhoopStrain(input: String)          = _state.update { it.copy(whoopStrainInput = input.filter { c -> c.isDigit() || c == '.' }.take(4)) }
    fun setWhoopSuggestedStrain(input: String) = _state.update { it.copy(whoopSuggestedStrainInput = input.filter { c -> c.isDigit() || c == '.' }.take(4)) }

    // ── Active workout ────────────────────────────────────────────────────

    fun openActiveWorkout(type: WorkoutType) = _state.update { it.copy(showActiveWorkout = true, activeWorkoutType = type) }
    fun closeActiveWorkout()                 = _state.update { it.copy(showActiveWorkout = false) }

    fun coreSetTapped(exerciseName: String, newCount: Int) {
        val log = _state.value.todayWorkoutLog ?: return
        viewModelScope.launch {
            val map  = mutableMapOf<String, Int>()
            val json = log.coreSetsJson
            if (json.length > 2) {
                json.substring(1, json.length - 1).split(",").filter { it.contains(":") }.forEach { entry ->
                    val parts = entry.split(":")
                    if (parts.size == 2) {
                        val key   = parts[0].trim().removeSurrounding("\"")
                        val value = parts[1].trim().toIntOrNull() ?: 0
                        map[key]  = value
                    }
                }
            }
            map[exerciseName] = newCount
            val pairs   = map.entries.joinToString(",") { e -> "\"${e.key}\":${e.value}" }
            val newJson = "{$pairs}"
            repo.updateCoreSets(log, newJson)
        }
    }

    fun logPR(exerciseName: String, workoutType: WorkoutType, difficulty: Difficulty, repsOrDuration: String, notes: String = "") {
        viewModelScope.launch { repo.logPR(exerciseName, workoutType, difficulty, repsOrDuration, notes) }
    }
    fun logBodyweight(weightKg: Float, notes: String = "") { viewModelScope.launch { repo.logBodyweight(weightKg, notes) } }
    fun logSleep(hours: Float, hrv: Int?, quality: Int, notes: String = "") { viewModelScope.launch { repo.logSleep(hours, hrv, quality, notes) } }

    // ── Injury ────────────────────────────────────────────────────────────

    fun openInjurySheet()  = _state.update { it.copy(showInjurySheet = true) }
    fun closeInjurySheet() = _state.update { it.copy(showInjurySheet = false) }
    fun logInjury(bodyPart: BodyPart, side: InjurySide, type: InjuryType, severity: Int, notes: String, date: String) {
        viewModelScope.launch { repo.logInjury(bodyPart, side, type, severity, notes, date) }
        closeInjurySheet()
    }
    fun resolveInjury(injury: InjuryLog) = viewModelScope.launch { repo.resolveInjury(injury) }

    // ── Onboarding / Week / Standards ─────────────────────────────────────

    fun completeOnboarding(name: String, week: Int) {
        viewModelScope.launch { prefs.completeOnboarding(name, week) }
    }
    fun setCurrentWeek(week: Int) {
        viewModelScope.launch { prefs.setCurrentWeek(week) }
    }
    fun toggleStandard(standard: String) {
        val set = _state.value.checkedStandards.toMutableSet()
        if (set.contains(standard)) set.remove(standard) else set.add(standard)
        _state.update { it.copy(checkedStandards = set.toSet()) }
    }

    // ── Tier ──────────────────────────────────────────────────────────────

    fun setTier(tier: KaizenTier) { viewModelScope.launch { prefs.setTier(tier) } }

    // ── Journal ───────────────────────────────────────────────────────────

    fun journalText(v: String) = _journalForm.update { it.copy(text = v) }
    fun journalMood(v: Int)    = _journalForm.update { it.copy(mood = v) }
    fun journalTags(v: String) = _journalForm.update { it.copy(tags = v) }
    fun openAddJournal()       = _state.update { it.copy(showAddJournal = true, editingJournal = null) }
    fun openEditJournal(entry: JournalEntry) {
        _journalForm.value = JournalForm(text = entry.text, mood = entry.mood, tags = entry.tags)
        _state.update { it.copy(showAddJournal = true, editingJournal = entry) }
    }
    fun dismissJournal() {
        _state.update { it.copy(showAddJournal = false, editingJournal = null) }
        _journalForm.value = JournalForm()
    }
    fun submitJournal() {
        val f = _journalForm.value; if (f.text.isBlank()) return
        val editing = _state.value.editingJournal
        viewModelScope.launch {
            if (editing != null) repo.updateJournal(editing.copy(text = f.text.trim(), mood = f.mood, tags = f.tags, updatedAt = System.currentTimeMillis()))
            else repo.addJournal(f.text.trim(), f.mood, f.tags)
            dismissJournal()
        }
    }
    fun deleteJournal(entry: JournalEntry) = viewModelScope.launch { repo.deleteJournal(entry) }

    // ── Goals ─────────────────────────────────────────────────────────────

    fun goalTitle(v: String)       = _goalForm.update { it.copy(title = v) }
    fun goalDescription(v: String) = _goalForm.update { it.copy(description = v) }
    fun goalTargetDate(v: String)  = _goalForm.update { it.copy(targetDate = v) }
    fun openAddGoal()              = _state.update { it.copy(showAddGoal = true, editingGoal = null) }
    fun openEditGoal(goal: Goal) {
        _goalForm.value = GoalForm(title = goal.title, description = goal.description, targetDate = goal.targetDate)
        _state.update { it.copy(showAddGoal = true, editingGoal = goal) }
    }
    fun dismissGoal() {
        _state.update { it.copy(showAddGoal = false, editingGoal = null) }
        _goalForm.value = GoalForm()
    }
    fun submitGoal() {
        val f = _goalForm.value; if (f.title.isBlank()) return
        val editing = _state.value.editingGoal
        viewModelScope.launch {
            if (editing != null) repo.updateGoal(editing.copy(title = f.title.trim(), description = f.description, targetDate = f.targetDate, updatedAt = System.currentTimeMillis()))
            else repo.addGoal(f.title.trim(), f.description, f.targetDate)
            dismissGoal()
        }
    }
    fun completeGoal(goal: Goal) = viewModelScope.launch { repo.completeGoal(goal) }
    fun deleteGoal(goal: Goal)   = viewModelScope.launch { repo.deleteGoal(goal) }

    // ── Wins ──────────────────────────────────────────────────────────────

    fun winTitle(v: String)       = _winForm.update { it.copy(title = v) }
    fun winDescription(v: String) = _winForm.update { it.copy(description = v) }
    fun winType(v: WinType)       = _winForm.update { it.copy(type = v) }
    fun openAddWin()              = _state.update { it.copy(showAddWin = true) }
    fun dismissWin() {
        _state.update { it.copy(showAddWin = false) }
        _winForm.value = WinForm()
    }
    fun submitWin() {
        val f = _winForm.value; if (f.title.isBlank()) return
        viewModelScope.launch {
            repo.addWin(f.title.trim(), f.description, f.type)
            dismissWin()
        }
    }
    fun deleteWin(win: Win) = viewModelScope.launch { repo.deleteWin(win) }

    // ── Ledger (slips) ────────────────────────────────────────────────────

    fun slipBody(v: String) = _slipForm.update { it.copy(body = v) }
    fun submitSlip() {
        val f = _slipForm.value; if (f.body.isBlank()) return
        viewModelScope.launch {
            repo.addSlip(f.body.trim())
            _slipForm.value = SlipForm()
        }
    }
    fun deleteSlip(slip: SlipEntry) = viewModelScope.launch { repo.deleteSlip(slip) }

    // ── Garmin / Health Connect ───────────────────────────────────────────

    val garminPermissions get() = hcm.permissions

    fun onGarminPermissionsGranted() {
        _state.update { it.copy(garminConnected = true) }
        refreshGarminData()
    }

    fun refreshGarminData() {
        viewModelScope.launch {
            if (!hcm.isAvailable() || !hcm.hasPermissions()) {
                _state.update { it.copy(garminConnected = false) }
                return@launch
            }
            _state.update { it.copy(isLoadingGarmin = true, garminConnected = true) }
            val data  = owClient.readTodayData()
            val today = LocalDate.now().toString()
            val cur   = repo.garminEntryOnce(today) ?: GarminEntry(date = today)
            repo.saveGarminEntry(cur.copy(
                steps     = data.steps     ?: cur.steps,
                restingHr = data.restingHr ?: cur.restingHr,
                hrv       = data.hrv       ?: cur.hrv,
            ))
            _state.update { it.copy(isLoadingGarmin = false) }
        }
    }

    fun saveBodyBattery(value: Int?) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val cur   = _state.value.garminEntry ?: GarminEntry(date = today)
            repo.saveGarminEntry(cur.copy(bodyBattery = value))
        }
    }

    fun saveStressScore(value: Int?) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val cur   = _state.value.garminEntry ?: GarminEntry(date = today)
            repo.saveGarminEntry(cur.copy(stressScore = value))
        }
    }

    // ── Cloud sync ────────────────────────────────────────────────────────

    fun syncToCloud() {
        if (_state.value.isSyncing) return
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, syncResult = null) }
            val s      = _state.value
            val pushOk = repo.syncToCloud(s.habits, s.journalEntries, s.goals, s.wins, s.slipEntries)
            val pullOk = repo.pullFromCloud()
            _state.update { it.copy(isSyncing = false, syncResult = pushOk && pullOk) }
            delay(2500)
            _state.update { it.copy(syncResult = null) }
        }
    }

    // ── Chat ──────────────────────────────────────────────────────────────

    fun setChatInput(v: String) { _chatInput.value = v }
    fun clearChat() { _state.update { it.copy(chatMessages = emptyList()) } }
    fun sendChat() {
        val input = _chatInput.value.trim()
        if (input.isBlank() || _state.value.chatLoading) return
        val userMsg  = ChatMessage("user", input)
        val messages = _state.value.chatMessages + userMsg
        _state.update { it.copy(chatMessages = messages, chatLoading = true) }
        _chatInput.value = ""
        val snapshot = _state.value
        viewModelScope.launch {
            val systemPrompt = buildCoachSystemPrompt(snapshot)
            val reply = callClaudeChat(systemPrompt, messages)
            _state.update { s -> s.copy(
                chatMessages = s.chatMessages + ChatMessage("assistant", reply),
                chatLoading  = false,
            )}
        }
    }

    // ── Computed helpers ──────────────────────────────────────────────────

    fun habitsForSlot(): List<HabitWithCompletions> {
        val slot = _state.value.selectedSlot
        return _state.value.habits.filter { it.habit.timeSlot == slot || it.habit.timeSlot == TimeSlot.ANYTIME }
    }
    fun scheduledWorkoutToday(): WorkoutType? = scheduledWorkoutForDate(_state.value.today, _state.value.currentTier)
    fun weeklyDays(): List<WeekDay> {
        val today  = LocalDate.now()
        val habits = _state.value.habits
        return (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val ds   = date.toString()
            WeekDay(
                date       = ds,
                label      = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                habitPct   = if (habits.isNotEmpty()) habits.count { it.isCompletedOn(ds) } / habits.size.toFloat() else 0f,
                hadWorkout = _state.value.recentWorkouts.any { it.date == ds },
                isToday    = offset == 0,
            )
        }
    }
}

class KaizenViewModelFactory(
    private val repo: KaizenRepository,
    private val prefs: UserPrefs,
    private val hcm: HealthConnectManager,
    private val owClient: OWHealthClient,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = KaizenViewModel(repo, prefs, hcm, owClient) as T
}
