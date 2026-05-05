package com.kaizen.app.data

/**
 * KaizenProgression
 *
 * Maps Road to Mastery's 24-week program into 4 Kaizen tiers.
 * Each tier unlocks new exercises and uses the Core 6 R.E.P.S. method
 * (Repetitions → End-Range → Peak Stress) to guide progression.
 *
 * Tier 1: Foundation    (Road to Mastery Weeks 1-6   — Full Body x3/week)
 * Tier 2: Development   (Road to Mastery Weeks 7-12  — Upper/Lower split)
 * Tier 3: Strength      (Road to Mastery Weeks 13-18 — Push/Pull/Legs)
 * Tier 4: Mastery       (Road to Mastery Weeks 19-24 — PPL + Full Body)
 */

enum class KaizenTier(
    val label: String,
    val emoji: String,
    val description: String,
    val weekRange: String,
    val split: String,
    val graduationStandards: List<String>,
) {
    FOUNDATION(
        label = "Foundation",
        emoji = "🌱",
        description = "Master the basics. 3x full-body workouts per week. Focus on form, muscle activation, and building consistent habits.",
        weekRange = "Weeks 1–6",
        split = "Full Body × 3/week",
        graduationStandards = listOf(
            "10 clean push ups",
            "8 inverted rows (horizontal)",
            "15 bodyweight squats with full depth",
            "30 sec plank with hollow body",
            "Complete 3 workouts/week for 4 consecutive weeks",
        )
    ),
    DEVELOPMENT(
        label = "Development",
        emoji = "⚡",
        description = "Upper/lower split. Introduce pull-up negatives, pike push-ups, and single-leg work. Build week-to-week volume.",
        weekRange = "Weeks 7–12",
        split = "Upper + Lower × 4/week",
        graduationStandards = listOf(
            "5 pull-up negatives (5 sec each)",
            "8 decline push ups",
            "8 dips with full range",
            "6 assisted pistol squats each leg",
            "1-min hollow body hold",
        )
    ),
    STRENGTH(
        label = "Strength",
        emoji = "🔥",
        description = "Push/Pull/Legs split. Real pull-ups, dips to failure, pistol squats. Introduce Liquid Motion mobility daily.",
        weekRange = "Weeks 13–18",
        split = "Push / Pull / Legs × 5/week",
        graduationStandards = listOf(
            "5 strict pull-ups",
            "15 dips to failure",
            "5 pistol squats each leg",
            "10 ab wheel roll-outs",
            "30-sec wall handstand",
        )
    ),
    MASTERY(
        label = "Mastery",
        emoji = "🏆",
        description = "PPL + Full Body. Advanced progressions: pseudo-planche push-ups, commando pull-ups, plyometrics. Skills training encouraged.",
        weekRange = "Weeks 19–24+",
        split = "Push / Pull / Legs / Full Body",
        graduationStandards = listOf(
            "10 strict pull-ups",
            "Pseudo-planche push-up 8 reps",
            "10 pistol squats each leg",
            "20 ab wheel roll-outs",
            "Free-standing handstand hold 10 sec",
        )
    ),
}

// ══ R.E.P.S. PROGRESS TRACKER ════════════════════════════════════════════════
// Core 6 methodology: track progression in 3 dimensions per exercise

data class RepsProgress(
    val exerciseName: String,
    val currentReps: Int,          // Repetitions
    val currentEndRange: String,   // End-Range description (e.g. "head touches floor")
    val currentPeakStress: String, // Peak Stress description (e.g. "feet elevated")
    val lastSessionDate: String,
    val notes: String = "",
) {
    /** R.E.P.S. readiness score 1-10 for next session */
    fun readinessScore(): Int = when {
        currentReps >= 9 -> 8  // time to extend End-Range
        currentReps >= 6 -> 7  // build reps
        else             -> 6  // still in foundation
    }
}

// ══ WEEKLY SCHEDULE TEMPLATES ════════════════════════════════════════════════

data class WeekSchedule(
    val tier: KaizenTier,
    val monday: WorkoutType?,
    val tuesday: WorkoutType?,
    val wednesday: WorkoutType?,
    val thursday: WorkoutType?,
    val friday: WorkoutType?,
    val saturday: WorkoutType?,
    val sunday: WorkoutType?,
) {
    fun workoutForDayOffset(offset: Int): WorkoutType? = when (offset % 7) {
        0 -> monday
        1 -> tuesday
        2 -> wednesday
        3 -> thursday
        4 -> friday
        5 -> saturday
        6 -> sunday
        else -> null
    }
}

/** Pre-built weekly schedules per tier — based on Road to Mastery */
object WeekSchedules {

    val foundation = WeekSchedule(
        tier      = KaizenTier.FOUNDATION,
        monday    = WorkoutType.PUSH,    // Full body A (push-dominant)
        tuesday   = null,                // Rest
        wednesday = WorkoutType.PULL,    // Full body B (pull-dominant)
        thursday  = null,                // Rest
        friday    = WorkoutType.LEGS,    // Full body C (legs-dominant)
        saturday  = null,                // Rest or Liquid Motion
        sunday    = null,                // Rest
    )

    val development = WeekSchedule(
        tier      = KaizenTier.DEVELOPMENT,
        monday    = WorkoutType.PUSH,    // Upper A
        tuesday   = WorkoutType.LEGS,    // Lower
        wednesday = null,                // Rest
        thursday  = WorkoutType.PULL,    // Upper B
        friday    = WorkoutType.LEGS,    // Lower
        saturday  = null,                // Rest
        sunday    = null,                // Liquid Motion
    )

    val strength = WeekSchedule(
        tier      = KaizenTier.STRENGTH,
        monday    = WorkoutType.PULL,
        tuesday   = WorkoutType.PUSH,
        wednesday = WorkoutType.LEGS,
        thursday  = null,
        friday    = WorkoutType.PULL,
        saturday  = WorkoutType.PUSH,
        sunday    = null,
    )

    /** SuperMover "Workout-Only" version: PPLFB */
    val mastery = WeekSchedule(
        tier      = KaizenTier.MASTERY,
        monday    = WorkoutType.PUSH,
        tuesday   = WorkoutType.PULL,
        wednesday = null,
        thursday  = null,
        friday    = WorkoutType.LEGS,
        saturday  = WorkoutType.CARDIO,  // Full Body
        sunday    = null,
    )

    fun forTier(tier: KaizenTier): WeekSchedule = when (tier) {
        KaizenTier.FOUNDATION  -> foundation
        KaizenTier.DEVELOPMENT -> development
        KaizenTier.STRENGTH    -> strength
        KaizenTier.MASTERY     -> mastery
    }
}


fun tierForWeek(week: Int): KaizenTier = when {
    week <= 6  -> KaizenTier.FOUNDATION
    week <= 12 -> KaizenTier.DEVELOPMENT
    week <= 18 -> KaizenTier.STRENGTH
    else       -> KaizenTier.MASTERY
}
