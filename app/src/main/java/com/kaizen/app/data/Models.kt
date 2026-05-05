package com.kaizen.app.data

import androidx.room.*

// ══ SHARED ENUMS ══════════════════════════════════════════════════════════

enum class HabitCategory(val label: String, val emoji: String, val hexColor: String) {
    HEALTH("Health", "💪", "#4ADE80"),
    MINDFULNESS("Mindfulness", "🧘", "#C084FC"),
    PRODUCTIVITY("Productivity", "⚡", "#60A5FA"),
    SOCIAL("Social", "👥", "#F472B6"),
    FITNESS("Fitness", "🔥", "#FB923C"),
    FLEXIBILITY("Flexibility", "🧘", "#67E8F9"),
    YOGA("Yoga", "🌿", "#86EFAC"),
}

enum class TimeSlot(val label: String, val emoji: String) {
    MORNING("Morning", "☀️"),
    EVENING("Evening", "🌙"),
    ANYTIME("Anytime", "⏰"),
}

enum class Difficulty { BEGINNER, INTERMEDIATE, ADVANCED }

enum class WorkoutType(val label: String, val emoji: String, val hexColor: String, val muscles: String) {
    PUSH("Push",    "🔺", "#FF6B6B", "Chest · Shoulders · Triceps"),
    PULL("Pull",    "🔻", "#4ECDC4", "Back · Biceps · Rear Delts"),
    LEGS("Legs",    "⬟",  "#FFE66D", "Quads · Hamstrings · Glutes"),
    CARDIO("Cardio","⚡", "#A8EDEA", "Heart · Lungs · Full Body");

    val coreCircuit: List<CoreExercise> get() = when (this) {
        PUSH   -> listOf(
            CoreExercise("Hollow Body Hold",   sets = 3, repsOrSeconds = "30 sec",   cue = "Press lower back into floor"),
            CoreExercise("Dead Bug",           sets = 3, repsOrSeconds = "10/side",  cue = "Opposite arm & leg, exhale fully"),
            CoreExercise("Pallof Press",       sets = 3, repsOrSeconds = "12/side",  cue = "Anti-rotation — resist twisting"),
        )
        PULL   -> listOf(
            CoreExercise("Ab Wheel Rollout",   sets = 3, repsOrSeconds = "8–10 reps",cue = "Hips stay level, no arching"),
            CoreExercise("Hanging Knee Raise", sets = 3, repsOrSeconds = "12 reps",  cue = "Control the descent"),
            CoreExercise("Bird Dog",           sets = 3, repsOrSeconds = "10/side",  cue = "Squeeze glute at top"),
        )
        LEGS   -> listOf(
            CoreExercise("Plank",              sets = 3, repsOrSeconds = "45 sec",   cue = "Squeeze everything"),
            CoreExercise("Suitcase Carry",     sets = 3, repsOrSeconds = "30m/side", cue = "Stand tall, resist lateral lean"),
            CoreExercise("Dragon Flag",        sets = 3, repsOrSeconds = "5–8 reps", cue = "Lower slowly — 3 sec descent"),
        )
        CARDIO -> listOf(
            CoreExercise("Mountain Climber",   sets = 3, repsOrSeconds = "20/side",  cue = "Keep hips low, drive knees"),
            CoreExercise("V-Up",               sets = 3, repsOrSeconds = "15 reps",  cue = "Meet in the middle"),
            CoreExercise("Russian Twist",      sets = 3, repsOrSeconds = "20 reps",  cue = "Rotate from thoracic"),
        )
    }
}

data class CoreExercise(val name: String, val sets: Int, val repsOrSeconds: String, val cue: String)

enum class WhoopZone(val label: String, val hexColor: String) {
    GREEN("Go Hard", "#4ADE80"),
    YELLOW("Moderate", "#FACC15"),
    RED("Rest Day", "#F87171"),
}

// ══ HABIT ENTITIES ════════════════════════════════════════════════════════

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: HabitCategory,
    val timeSlot: TimeSlot,
    val streak: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "habit_completions",
    primaryKeys = ["habitId", "date"],
    foreignKeys = [ForeignKey(Habit::class, ["id"], ["habitId"], onDelete = ForeignKey.CASCADE)],
)
data class HabitCompletion(val habitId: Long, val date: String)

data class HabitWithCompletions(
    @Embedded val habit: Habit,
    @Relation(parentColumn = "id", entityColumn = "habitId")
    val completions: List<HabitCompletion>,
) {
    fun isCompletedOn(date: String) = completions.any { it.date == date }
}

// ══ WORKOUT LOG ═══════════════════════════════════════════════════════════

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val workoutType: WorkoutType,
    val whoopRecovery: Int?,
    val strainScore: Float?,
    val suggestedStrain: Float? = null,
    val durationMinutes: Int?,
    val coreSetsJson: String = "{}",
    val completedAt: Long = System.currentTimeMillis(),
) {
    fun totalCoreSetsCompleted(): Int = parseCoreSets().values.sumOf { it }
    fun totalCoreSetsTarget(): Int    = workoutType.coreCircuit.sumOf { it.sets }
    fun coreFullyComplete(): Boolean  = totalCoreSetsCompleted() >= totalCoreSetsTarget()
    fun setsCompletedFor(name: String): Int = parseCoreSets()[name] ?: 0

    private fun parseCoreSets(): Map<String, Int> = try {
        coreSetsJson.trim('{', '}').split(",")
            .filter { it.contains(":") }
            .associate { e -> val (k, v) = e.split(":"); k.trim().trim('"') to v.trim().toInt() }
    } catch (_: Exception) { emptyMap() }
}

// ══ PERSONAL RECORDS ══════════════════════════════════════════════════════

@Entity(tableName = "personal_records")
data class PersonalRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseName: String,
    val workoutType: WorkoutType,
    val difficulty: Difficulty,
    val repsOrDuration: String,
    val notes: String = "",
    val date: String,
    val setAt: Long = System.currentTimeMillis(),
)

// ══ BODYWEIGHT ════════════════════════════════════════════════════════════

@Entity(tableName = "bodyweight_entries")
data class BodyweightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val weightKg: Float,
    val notes: String = "",
    val loggedAt: Long = System.currentTimeMillis(),
)

// ══ SLEEP + HRV ═══════════════════════════════════════════════════════════

@Entity(tableName = "sleep_entries")
data class SleepEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val hoursSlept: Float,
    val hrv: Int? = null,
    val quality: Int = 3,
    val notes: String = "",
    val loggedAt: Long = System.currentTimeMillis(),
)

// ══ JOURNAL ════════════════════════════════════════════════════════════════

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = java.util.UUID.randomUUID().toString(),
    val date: String,
    val text: String,
    val mood: Int = 3,     // 1–5: 😔 😐 🙂 😄 🔥
    val tags: String = "", // comma-separated
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

// ══ GOALS ══════════════════════════════════════════════════════════════════

enum class GoalStatus { ACTIVE, PAUSED, COMPLETED }

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val targetDate: String = "",       // LocalDate.toString() or ""
    val status: GoalStatus = GoalStatus.ACTIVE,
    val linkedHabitIds: String = "",   // comma-separated Long IDs
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

// ══ WINS ═══════════════════════════════════════════════════════════════════

@Entity(tableName = "wins")
data class Win(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val date: String,
    val linkedWorkoutId: Long? = null,
    val linkedHabitId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)


// ══ INJURY LOG ════════════════════════════════════════════════════════════

enum class InjurySide(val label: String) { LEFT("Left"), RIGHT("Right"), BOTH("Both") }

enum class InjuryType(val label: String, val emoji: String) {
    TIGHTNESS("Tightness", "😬"),
    SORENESS("Soreness",   "😣"),
    PAIN("Pain",           "🔴"),
    STRAIN("Strain",       "⚠️"),
    TAPED("Taped/Braced",  "🩹"),
}

enum class BodyPart(val label: String, val emoji: String, val affectedWorkouts: List<String>) {
    CALF("Calf",          "🦵", listOf("LEGS", "CARDIO")),
    IT_BAND("IT Band",    "🦵", listOf("LEGS", "CARDIO")),
    KNEE("Knee",          "🦵", listOf("LEGS", "CARDIO")),
    HIP("Hip",            "🍑", listOf("LEGS", "CARDIO")),
    HAMSTRING("Hamstring","🦵", listOf("LEGS", "CARDIO")),
    QUAD("Quad",          "🦵", listOf("LEGS")),
    LOWER_BACK("Lower Back","🔙", listOf("LEGS", "PULL", "CARDIO")),
    UPPER_BACK("Upper Back","🔙", listOf("PULL", "PUSH")),
    SHOULDER("Shoulder",  "💪", listOf("PUSH", "PULL")),
    ELBOW("Elbow",        "💪", listOf("PUSH", "PULL")),
    WRIST("Wrist",        "🤲", listOf("PUSH", "PULL")),
    NECK("Neck",          "😤", listOf("PUSH", "PULL")),
    ANKLE("Ankle",        "🦶", listOf("LEGS", "CARDIO")),
    FOOT("Foot",          "🦶", listOf("LEGS", "CARDIO")),
    OTHER("Other",        "🩹", listOf()),
}

@Entity(tableName = "injury_logs")
data class InjuryLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val bodyPart: BodyPart,
    val side: InjurySide,
    val type: InjuryType,
    val severity: Int = 3,        // 1 = mild, 5 = severe
    val notes: String = "",
    val resolved: Boolean = false,
    val resolvedDate: String? = null,
    val loggedAt: Long = System.currentTimeMillis(),
)

// ══ TYPE CONVERTERS ═══════════════════════════════════════════════════════

class KaizenConverters {
    @TypeConverter fun fromHabitCat(v: String)      = HabitCategory.valueOf(v)
    @TypeConverter fun toHabitCat(v: HabitCategory) = v.name
    @TypeConverter fun fromSlot(v: String)           = TimeSlot.valueOf(v)
    @TypeConverter fun toSlot(v: TimeSlot)           = v.name
    @TypeConverter fun fromWorkoutType(v: String)    = WorkoutType.valueOf(v)
    @TypeConverter fun toWorkoutType(v: WorkoutType) = v.name
    @TypeConverter fun fromDifficulty(v: String)     = Difficulty.valueOf(v)
    @TypeConverter fun toDifficulty(v: Difficulty)   = v.name
    @TypeConverter fun fromGoalStatus(v: String)     = GoalStatus.valueOf(v)
    @TypeConverter fun toGoalStatus(v: GoalStatus)   = v.name
    @TypeConverter fun fromBodyPart(v: String)       = BodyPart.valueOf(v)
    @TypeConverter fun toBodyPart(v: BodyPart)       = v.name
    @TypeConverter fun fromInjurySide(v: String)     = InjurySide.valueOf(v)
    @TypeConverter fun toInjurySide(v: InjurySide)   = v.name
    @TypeConverter fun fromInjuryType(v: String)     = InjuryType.valueOf(v)
    @TypeConverter fun toInjuryType(v: InjuryType)   = v.name
}
