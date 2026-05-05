# Kaizen — Claude Code Project Brief

## What this is
A personal Android app (Kotlin + Jetpack Compose) built for one user (Jordan Robinson).
Not for distribution or the Play Store. Personal training + habit tracker + AI coach.
Package: `com.kaizen.app`
Project root: `~/AndroidStudioProjects/Kaizen/`
Device: OnePlus CPH2513 connected via USB

## Current state
**The app builds and runs cleanly.** No known compile errors.
DB is at version 4. All entities compile. App is installed on device.

---

## Architecture

**Build system:** Kotlin DSL (`build.gradle.kts`), KSP for Room, Glance for widget
**UI:** Jetpack Compose + Material3, single Activity
**State:** ViewModel + StateFlow + collectAsStateWithLifecycle
**Database:** Room with `fallbackToDestructiveMigration()` (personal app, data loss on schema change is acceptable)
**Preferences:** DataStore (`UserPrefs.kt`) for tier persistence
**Claude API:** `claude-sonnet-4-6` called directly via HttpsURLConnection
  - Key in `local.properties` as `ANTHROPIC_API_KEY`, injected via BuildConfig
  - Two call sites: `ClaudeCoachCard.kt` (single-shot advice) and `ClaudeContext.kt` (multi-turn chat)

---

## Package structure

```
com.kaizen.app/
├── MainActivity.kt              — Single activity, 6-tab nav, all sheets
├── KaizenApplication.kt         — Notification channels, WorkManager schedule
│
├── data/
│   ├── Models.kt                — All Room entities + enums
│   ├── KaizenDao.kt             — All DAO queries
│   ├── KaizenRepository.kt      — Repository + @Database declaration (version 4)
│   ├── UserPrefs.kt             — DataStore for KaizenTier persistence
│   ├── ExerciseLibrary.kt       — Exercise data from 5 training books
│   └── KaizenProgression.kt     — 24-week tier system + tierForWeek()
│
├── ui/
│   ├── KaizenViewModel.kt       — All state + business logic
│   ├── ClaudeContext.kt         — buildCoachSystemPrompt() + callClaudeChat()
│   └── screens/
│       ├── TodayScreen.kt       — Main daily view
│       ├── WorkoutsScreen.kt    — History heatmap + tier card + PR records
│       ├── HabitsScreen.kt      — Full habit list with swipe-to-delete
│       ├── StatsScreen.kt       — Bodyweight/sleep logging + charts + weekly view
│       ├── ProgressionScreen.kt — 24-week tier tracker with graduation standards
│       ├── CoachScreen.kt       — Chat/Journal/Goals/Wins (4 sub-tabs)
│       ├── OnboardingScreen.kt  — First-run 3-page onboarding
│       ├── ActiveWorkoutSheet.kt— Full workout session sheet (1hr volume)
│       ├── AddHabitSheet.kt     — Add/edit habit bottom sheet
│       ├── InjuryCard.kt        — InjuryStatusCard + AddInjurySheet
│       ├── MuscleMapView.kt     — Canvas body diagram with muscle highlights
│       ├── MobilitySection.kt   — Daily stretch/yoga flows with timers
│       ├── ClaudeCoachCard.kt   — AI coaching card on Today screen (claude-sonnet-4-6)
│       ├── CoreSection.kt       — Core circuit display
│       └── RestTimer.kt         — Adjustable countdown timer (min 90s)
│
├── notifications/
│   └── ReminderWorker.kt        — WorkManager 8am/8pm notifications
│
├── widget/
│   └── KaizenWidget.kt          — Glance home screen widget
│
└── ui/theme/
    └── KaizenTheme.kt           — Color palette, extension functions
```

---

## Data model (Room entities)

```kotlin
// 10 tables in KaizenDatabase (version 4)
Habit               — id, name, category, timeSlot, streak, createdAt
HabitCompletion     — habitId, date (composite PK)
WorkoutLog          — id, date, workoutType, whoopRecovery, strainScore,
                      suggestedStrain, durationMinutes, coreSetsJson
PersonalRecord      — id, exerciseName, workoutType, difficulty,
                      repsOrDuration, notes, date
BodyweightEntry     — id, date, weightKg, notes
SleepEntry          — id, date, hoursSlept, hrv, quality (1-5), notes
InjuryLog           — id, date, bodyPart, side, type, severity (1-5),
                      notes, resolved, resolvedDate
JournalEntry        — id, remoteId (UUID), date, text, mood (1-5),
                      tags (comma-separated), createdAt, updatedAt
Goal                — id, remoteId (UUID), title, description, targetDate,
                      status (ACTIVE/PAUSED/COMPLETED), linkedHabitIds, createdAt, updatedAt
Win                 — id, remoteId (UUID), title, description, date,
                      linkedWorkoutId?, linkedHabitId?, createdAt, updatedAt

// Key enums
HabitCategory: HEALTH, MINDFULNESS, PRODUCTIVITY, SOCIAL, FITNESS, FLEXIBILITY, YOGA
WorkoutType:   PUSH, PULL, LEGS, CARDIO  (each has coreCircuit property)
Difficulty:    BEGINNER, INTERMEDIATE, ADVANCED  (simple enum, no params)
WhoopZone:     GREEN, YELLOW, RED
InjurySide:    LEFT, RIGHT, BOTH
InjuryType:    TIGHTNESS, SORENESS, PAIN, STRAIN, TAPED
BodyPart:      CALF, IT_BAND, KNEE, HIP, HAMSTRING, QUAD, LOWER_BACK,
               UPPER_BACK, SHOULDER, ELBOW, WRIST, NECK, ANKLE, FOOT, OTHER
GoalStatus:    ACTIVE, PAUSED, COMPLETED
KaizenTier:    FOUNDATION, DEVELOPMENT, STRENGTH, MASTERY (in KaizenProgression.kt)
```

---

## KaizenUiState (all fields)

```kotlin
data class KaizenUiState(
    // Core
    val habits: List<HabitWithCompletions>,
    val recentWorkouts: List<WorkoutLog>,
    val todayWorkoutLog: WorkoutLog?,
    val selectedSlot: TimeSlot,
    val today: String,
    // Whoop inputs
    val whoopRecoveryInput: String,
    val whoopStrainInput: String,
    val whoopSuggestedStrainInput: String,
    // Sheet visibility
    val showAddHabitSheet: Boolean,
    val showActiveWorkout: Boolean,
    val showInjurySheet: Boolean,
    val showAddJournal: Boolean,
    val showAddGoal: Boolean,
    val showAddWin: Boolean,
    // Active workout
    val activeWorkoutType: WorkoutType?,
    // Editing
    val editingHabit: HabitWithCompletions?,
    val editingJournal: JournalEntry?,
    val editingGoal: Goal?,
    // Loading
    val isLoading: Boolean,
    // Data lists
    val personalRecords: List<PersonalRecord>,
    val bodyweightEntries: List<BodyweightEntry>,
    val sleepEntries: List<SleepEntry>,
    val activeInjuries: List<InjuryLog>,
    val journalEntries: List<JournalEntry>,
    val goals: List<Goal>,
    val wins: List<Win>,
    // Progression
    val currentWeek: Int,
    val checkedStandards: Set<String>,
    val currentTier: KaizenTier,
    // Onboarding
    val onboardingDone: Boolean,
    // Chat
    val chatMessages: List<ChatMessage>,
    val chatLoading: Boolean,
    // Computed properties:
    // whoopRecovery, whoopStrain, whoopSuggestedStrain (parsed from inputs)
    // whoopZone, hasWhoopData
    // whoopScaledDifficulty, whoopScaledSets, whoopScaledRestSeconds
)
```

---

## ViewModel form state flows (separate from UiState)

```kotlin
vm.form        : StateFlow<AddHabitForm>   — name, category, slot
vm.journalForm : StateFlow<JournalForm>    — text, mood, tags
vm.goalForm    : StateFlow<GoalForm>       — title, description, targetDate
vm.winForm     : StateFlow<WinForm>        — title, description
vm.chatInput   : StateFlow<String>         — current chat input field value
```

---

## Navigation tabs (6)

| Tab | Icon | Content |
|-----|------|---------|
| Today | ⬡ | Whoop inputs, injury card, muscle map, mobility, Claude Coach card, habit ring, habit list |
| Workouts | ◈ | Tier card, calendar heatmap, quick start, session history / PR records |
| Habits | ≡ | Full habit list, swipe-to-delete, long-press to edit |
| Stats | ◉ | Bodyweight + sleep logging, graphs, 7-day chart, streak leaders |
| Progress | 🏆 | 24-week tier system, graduation standards checklist, week slider |
| Coach | ✦ | Chat (multi-turn Claude), Journal, Goals, Wins sub-tabs |

---

## Key features

### Whoop integration
- Three input fields: Recovery %, Suggested Strain, Actual Strain
- Zone auto-derives: Green 67-100% = Advanced/5sets/90s, Yellow 34-66% = Intermediate/4sets/120s, Red 0-33% = Beginner/3sets/150s
- Feeds into ActiveWorkoutSheet scaling and Claude prompts

### Active workout sheet
- Slides up as ModalBottomSheet
- ~1 hour volume: 6 exercises × 4-5 sets (scaled by Whoop zone)
- BEG/INT/ADV tier chips for manual override
- Set dot buttons — tap to complete, auto-starts rest timer
- Rest timer: adjustable +/-15/30s, minimum 90s, pause/resume/skip
- PR logging row appears when all sets done for an exercise
- Core circuit finisher at the bottom

### Injury tracking
- InjuryStatusCard on Today screen — shows active injuries, tap to resolve
- AddInjurySheet: body part, side (L/R/Both), type, severity 1-5, notes
- Active injuries feed into Claude context

### Claude Coach card (Today screen)
- `ClaudeCoachCard.kt` — single-shot daily advice
- Calls `claude-sonnet-4-6` with: Whoop data, last 12 sessions, top PRs,
  bodyweight, 7-day avg sleep/HRV, active injuries, habit completion
- Auto-fetches when Whoop recovery is entered
- Refresh button, animated loading dots

### Claude Chat (Coach tab)
- `ClaudeContext.kt` — `buildCoachSystemPrompt(state)` assembles full user context:
  week/tier, WHOOP, workouts, PRs, body metrics, active goals, last 3 journal entries,
  wins, injuries, habits
- `callClaudeChat(systemPrompt, messages)` — multi-turn, passes full conversation history
- Chat UI: user bubbles right-aligned (gold), Claude bubbles left with ✦ icon
- Empty state shows context-aware quick-prompt chips
- Clear button resets conversation thread

### Journal / Goals / Wins (Coach tab sub-tabs)
- Journal: mood picker (1-5 emoji), free text, comma-separated tags. Tap to edit, long-press to delete.
- Goals: title, description, target date. Mark done ✓. Active/Completed sections. Tap to edit, long-press to delete.
- Wins: title, description. Long-press to delete.
- All three have `remoteId` (UUID) fields ready for Supabase sync.

### Progression system
- `tierForWeek(week)` top-level function in `KaizenProgression.kt`
- Week slider 1-24, tier changes automatically
- Graduation standards as checkable boxes
- Celebrates when all standards met
- Current tier persisted in DataStore via `UserPrefs`

---

## Phase 3: Sync (in progress)

**Approach:** One-way push sync (phone → Supabase) via PostgREST REST API.
No SDK needed — same plain HTTPS pattern as Claude API calls.

**Models with remoteId ready for sync:** `JournalEntry`, `Goal`, `Win`

**Supabase schema (run in SQL editor):**
```sql
create table journal_entries (
  remote_id text primary key,
  date text not null,
  text text not null,
  mood int not null default 3,
  tags text not null default '',
  updated_at bigint not null
);

create table goals (
  remote_id text primary key,
  title text not null,
  description text not null default '',
  target_date text not null default '',
  status text not null default 'ACTIVE',
  updated_at bigint not null
);

create table wins (
  remote_id text primary key,
  title text not null,
  description text not null default '',
  date text not null,
  updated_at bigint not null
);
```

**local.properties keys needed:**
```
ANTHROPIC_API_KEY=sk-ant-...
SUPABASE_URL=https://xxxx.supabase.co
SUPABASE_ANON_KEY=eyJ...
```

**Sync is wired.** `SupabaseSync.kt` exists in `data/` — upserts journal/goal/win after every write, fires delete on remove.

**Web companion:** `docs/index.html` — dark-themed, reads live from Supabase. Host via GitHub Pages.
- GitHub username: `52batboy-1`
- Live URL: `https://52batboy-1.github.io/Kaizen/`
- App ↗ button in Coach tab already points to this URL.
- Setup: repo Settings → Pages → main branch, `/docs` folder.

---

## Common pitfalls

- `Difficulty` enum is SIMPLE — `enum class Difficulty { BEGINNER, INTERMEDIATE, ADVANCED }` — no label or emoji params
- `Box(contentAlignment = Alignment.Center)` not `Box(Alignment.Center)` — positional arg is Modifier not Alignment
- `Row()` and `Column()` need NAMED params: `horizontalArrangement =`, `verticalAlignment =`
- `animateColorAsState` needs `label = "..."` param in newer Compose
- `FlowRow` needs `@OptIn(ExperimentalLayoutApi::class)`
- `tierForWeek()` is a top-level function in `com.kaizen.app.data`, NOT a companion method
- `KaizenViewModelFactory` takes TWO params: `(repo: KaizenRepository, prefs: UserPrefs)`
- `TodayScreen` has `onAddInjury` and `onResolveInjury` params — don't remove them
- `CoachScreen` has chat params: `chatInput`, `chatMessages`, `chatLoading`, `onChatInput`, `onSendChat`, `onClearChat`
- Chat messages in state: `List<ChatMessage>` where `ChatMessage(role: String, content: String)`

---

## Theme colors (K object in KaizenTheme.kt)

```kotlin
K.Bg, K.Card, K.Card2, K.Border, K.Gold, K.GoldDim,
K.Text, K.Muted, K.Morning, K.Night, K.Streak, K.Health, K.WhoopRed
```

---

## Build commands

```bash
cd ~/AndroidStudioProjects/Kaizen

./gradlew compileDebugKotlin     # fastest — just checks for errors
./gradlew assembleDebug          # builds APK
./gradlew installDebug           # installs on connected device
./gradlew clean                  # clean before major fixes
```

---

## What works well (don't break these)
- `KaizenTheme.kt` color extensions for HabitCategory, WorkoutType, WhoopZone
- `ClaudeContext.kt` — `buildCoachSystemPrompt` + `callClaudeChat`
- `KaizenRepository.kt` data access patterns (all 10 entities)
- `MobilitySection.kt` stretch flows with countdown timers
- `MuscleMapView.kt` Canvas body drawing
- `RestTimer.kt` adjustable countdown
- `WorkoutsScreen.kt` calendar heatmap + tier card
- `StatsScreen.kt` bodyweight/sleep logging UI
- `AddHabitSheet.kt` with FLEXIBILITY/YOGA presets
- `HabitsScreen.kt` swipe-to-delete
- `CoachScreen.kt` chat thread + journal/goals/wins sheets
