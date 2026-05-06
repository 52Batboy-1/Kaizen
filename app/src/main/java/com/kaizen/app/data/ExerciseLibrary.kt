package com.kaizen.app.data

/**
 * KaizenExerciseLibrary
 *
 * Synthesized from:
 *  - SuperMover (Bioneer) — Push/Pull/Legs/Full Body exercise selection & Liquid Motion
 *  - Stronger at Home (MoMoMuscle) — coaching cues, RPE, muscle activation focus
 *  - Road to Mastery (Calisthenics Nerd) — beginner→advanced progression & periodization
 *  - Bodyweight Fitness Progressions — full skill progression ladder
 *  - Core 6 (Soft Acro Aaron) — R.E.P.S. scaling methodology
 */

// ══ DIFFICULTY TIER ══════════════════════════════════════════════════════════

// ══ EXERCISE ═════════════════════════════════════════════════════════════════

data class Exercise(
    val name: String,
    val difficulty: Difficulty,
    val sets: Int,
    val repsOrDuration: String,    // e.g. "8-12 reps" or "45 sec"
    val rpe: Int,                  // 1-10 perceived exertion target
    val restSeconds: Int = 60,
    val cue: String,               // MoMo-style coaching cue
    val repsProgression: String,   // R.E.P.S. "Repetitions" — when to advance
    val endRangeProgression: String, // R.E.P.S. "End-Range" — how to deepen
    val peakStressProgression: String, // R.E.P.S. "Peak Stress" — how to load
    val musclesFocus: String,      // primary muscles
)

// ══ WARM-UP (from MoMo + SuperMover Liquid Motion) ══════════════════════════

object WarmUp {
    val standard = listOf(
        "Arm swings — 15 reps, open the chest, create space in shoulder joint",
        "Arm circles — 20 reps, squeeze shoulder blades back, avoid shrugging",
        "Cat/cow T-spine mobilization — 10 reps, squeeze hard each position",
        "Hip bridges — 20 reps, squeeze from butt first, tuck pelvis",
        "Bodyweight squats — 10 reps, slow tempo, push knees out, tall chest",
        "Standing glute squeeze — 15 sec, squeeze as hard as possible",
    )

    /** SuperMover Liquid Motion System — 30 sec each, 2-3 rounds */
    val liquidMotion = listOf(
        LiquidMotionDrill("Prayer Squat",          "30 sec", "Knees out, tall chest, deep hip opener"),
        LiquidMotionDrill("Shoulder Dislocates",   "30 sec", "Keep arms straight, control the range"),
        LiquidMotionDrill("Crab Reach",            "30 sec", "Push hips up, reach one arm overhead, alternate"),
        LiquidMotionDrill("Elephant Walks",        "30 sec", "Straight legs, walk hands forward, hamstring stretch"),
        LiquidMotionDrill("V-W Stretch",           "30 sec", "Floor-based shoulder + upper back opener"),
        LiquidMotionDrill("Roundhouse Kick Swing", "30 sec ea", "Open hips, balance on one foot, alternate legs"),
        LiquidMotionDrill("Hip Flexor Stretch",    "30 sec ea", "Squeeze butt hard, tall chest, anterior pelvic tilt"),
        LiquidMotionDrill("Lateral Gorilla Walk",  "30 sec", "Wide squat, hands touch floor, lateral shuffle"),
    )
}

data class LiquidMotionDrill(val name: String, val duration: String, val cue: String)

// ══ PUSH EXERCISES ═══════════════════════════════════════════════════════════
// Sources: SuperMover (Dips, Pike Push Ups, Push Ups, Lizard Crawl)
//          Road to Mastery (incline → standard → decline → pike → dips progression)
//          MoMo (coaching cues, RPE, superset format)

object PushExercises {

    // ── Tier 1: Beginner ─────────────────────────────────────────────────
    val inclinePushUp = Exercise(
        name = "Incline Push Up",
        difficulty = Difficulty.BEGINNER,
        sets = 4, repsOrDuration = "10-15 reps", rpe = 7, restSeconds = 0,
        cue = "Squeeze shoulder blades back and DOWN. Proud chest. Push up — not back toward your hips.",
        repsProgression = "Hit 15 clean reps → move to standard push ups",
        endRangeProgression = "Lower chest closer to the surface each set",
        peakStressProgression = "Slow the descent to 3 seconds",
        musclesFocus = "Chest, Shoulders, Triceps",
    )

    val pikePushUp = Exercise(
        name = "Pike Push Up",
        difficulty = Difficulty.BEGINNER,
        sets = 3, repsOrDuration = "6-10 reps", rpe = 7, restSeconds = 0,
        cue = "Hips high — inverted V shape. Elbows track back toward your hips, not out wide. Head in front of hands.",
        repsProgression = "Hit 10 reps → elevate feet for extra load",
        endRangeProgression = "Head touches floor at bottom",
        peakStressProgression = "Elevate feet on bench or couch",
        musclesFocus = "Shoulders (anterior deltoid), Triceps",
    )

    // ── Tier 2: Intermediate ──────────────────────────────────────────────
    val pushUp = Exercise(
        name = "Push Up",
        difficulty = Difficulty.INTERMEDIATE,
        sets = 3, repsOrDuration = "Failure (rapid cadence)", rpe = 8, restSeconds = 60,
        cue = "Almost bounce in and out — continuous tension. Keep a subtle hollow-body position throughout. Don't flare elbows.",
        repsProgression = "20 clean reps → switch to decline or archer variation",
        endRangeProgression = "Full chest-to-floor range",
        peakStressProgression = "Pause 2 sec at bottom each rep",
        musclesFocus = "Chest, Shoulders, Triceps, Core",
    )

    val dips = Exercise(
        name = "Dips",
        difficulty = Difficulty.INTERMEDIATE,
        sets = 3, repsOrDuration = "Failure (rapid cadence)", rpe = 8, restSeconds = 60,
        cue = "Chest leans forward over hands — that loads the pecs. Squeeze shoulder blades back. Lower until slight stretch in chest, then use the myotatic bounce to drive back up.",
        repsProgression = "20+ reps → add weight or switch to ring dips",
        endRangeProgression = "Lower elbows past 90° for deeper pec stretch",
        peakStressProgression = "Add a dip belt or weighted vest",
        musclesFocus = "Chest, Triceps, Anterior Deltoid",
    )

    val lizardCrawl = Exercise(
        name = "Lizard Crawl",
        difficulty = Difficulty.INTERMEDIATE,
        sets = 3, repsOrDuration = "1 minute", rpe = 8, restSeconds = 60,
        cue = "Elbow nearly touches the floor, chest close to ground, opposite arm + leg step together. Core braced — keep hips LOW.",
        repsProgression = "1 minute continuous → add a load (weight vest) or go slower",
        endRangeProgression = "Elbow brushes the floor each step",
        peakStressProgression = "Wear a light weight vest",
        musclesFocus = "Chest, Shoulders, Core, Hip Flexors",
    )

    // ── Tier 3: Advanced ──────────────────────────────────────────────────
    val declinePushUp = Exercise(
        name = "Decline Push Up",
        difficulty = Difficulty.ADVANCED,
        sets = 4, repsOrDuration = "10-12 reps", rpe = 8, restSeconds = 45,
        cue = "Feet elevated, hands wide. Upper chest emphasis. Squeeze armpits to activate lats — more shoulder stability.",
        repsProgression = "12 reps → pike push ups or wall handstand push up negatives",
        endRangeProgression = "Increase foot elevation height",
        peakStressProgression = "Weighted vest or ring push ups",
        musclesFocus = "Upper Chest, Shoulders, Triceps",
    )

    val pseudoPlanchePushUp = Exercise(
        name = "Pseudo-Planche Push Up",
        difficulty = Difficulty.ADVANCED,
        sets = 3, repsOrDuration = "8-10 reps", rpe = 9, restSeconds = 90,
        cue = "Hands pointed back toward hips. Lean forward as much as possible before lowering. This kills the front delts and straight-arm strength.",
        repsProgression = "10 reps → increase lean angle",
        endRangeProgression = "Deeper forward lean — more weight over hands",
        peakStressProgression = "Progress to tuck planche lean",
        musclesFocus = "Anterior Deltoid, Chest, Straight-Arm Strength",
    )

    /** Full push day — SuperMover sequence */
    val pushDay = listOf(dips, lizardCrawl, pikePushUp, pushUp)

    /** Beginner push day */
    val pushDayBeginner = listOf(inclinePushUp, pikePushUp)

    /** Advanced push day */
    val pushDayAdvanced = listOf(dips, declinePushUp, pseudoPlanchePushUp, lizardCrawl)
}

// ══ PULL EXERCISES ═══════════════════════════════════════════════════════════
// Sources: SuperMover (Bodyweight Rows, Goblet Curls, Ab Roll Outs, Tactical Pull Ups)
//          Road to Mastery (scapular shrugs → inverted rows → pull-up negatives → pull-ups)
//          MoMo (scapula depression focus, lat activation cues)

object PullExercises {

    // ── Tier 1: Beginner ─────────────────────────────────────────────────
    val scapularShrug = Exercise(
        name = "Scapular Shrug (Dead Hang)",
        difficulty = Difficulty.BEGINNER,
        sets = 5, repsOrDuration = "4-6 reps", rpe = 6, restSeconds = 60,
        cue = "Dead hang from bar. Without bending elbows, shrug your shoulder blades DOWN toward your hips — depress the scapula. Hold 2 sec at bottom. Arms stay straight.",
        repsProgression = "6 reps → inverted rows",
        endRangeProgression = "Increase hold duration to 3 sec",
        peakStressProgression = "Add slight hollow body tension",
        musclesFocus = "Serratus Anterior, Lower Trapezius, Lats",
    )

    val invertedRow = Exercise(
        name = "Inverted Row (Bodyweight Row)",
        difficulty = Difficulty.BEGINNER,
        sets = 4, repsOrDuration = "8-12 reps", rpe = 7, restSeconds = 30,
        cue = "Round shoulders and lower weight slow. Squeeze elbows back toward belly button. PROUD chest at the top — sternum up to ceiling. Create space between shoulder blades.",
        repsProgression = "15 reps at current angle → lower the angle (more horizontal)",
        endRangeProgression = "More horizontal body position — eventually floor level",
        peakStressProgression = "Elevate feet on bench for more bodyweight over hands",
        musclesFocus = "Upper Back, Rear Deltoid, Biceps, Core",
    )

    // ── Tier 2: Intermediate ──────────────────────────────────────────────
    val pullUpNegative = Exercise(
        name = "Pull-Up Negative (5 sec)",
        difficulty = Difficulty.INTERMEDIATE,
        sets = 3, repsOrDuration = "6-8 reps", rpe = 8, restSeconds = 60,
        cue = "Jump or step to top position. Lower yourself in 5 seconds — fight gravity. Shoulder blades retract and depress throughout.",
        repsProgression = "8 slow negatives → attempt full pull-ups",
        endRangeProgression = "Extend negative to 7-8 seconds",
        peakStressProgression = "Add light weight via belt",
        musclesFocus = "Lats, Biceps, Rear Deltoid",
    )

    val tacticalPullUp = Exercise(
        name = "Tactical Pull Up",
        difficulty = Difficulty.INTERMEDIATE,
        sets = 3, repsOrDuration = "Failure", rpe = 8, restSeconds = 60,
        cue = "Overhand grip, shoulder-width. Drive elbows DOWN toward hips — think 'put your shoulder blades in your back pockets.' Chin clears the bar. Control the descent.",
        repsProgression = "10 clean reps → add weight or widen grip",
        endRangeProgression = "Full dead hang at bottom each rep",
        peakStressProgression = "Weighted pull-ups or wide grip",
        musclesFocus = "Lats, Biceps, Rear Deltoid, Grip",
    )

    val gobletCurl = Exercise(
        name = "Bodyweight Bicep Curl (Goblet / Band)",
        difficulty = Difficulty.INTERMEDIATE,
        sets = 3, repsOrDuration = "Failure", rpe = 8, restSeconds = 60,
        cue = "Elbows forward — not behind your body. Squeeze FROM your biceps, not your shoulder. Slow on the way down. Extend wrist at bottom to deepen the stretch.",
        repsProgression = "20+ reps → increase resistance or switch to chin-up",
        endRangeProgression = "Full supination at top, full extension at bottom",
        peakStressProgression = "Increase band resistance or use a DB",
        musclesFocus = "Biceps Brachii, Brachialis",
    )

    val abRollOut = Exercise(
        name = "Ab Wheel Roll-Out",
        difficulty = Difficulty.INTERMEDIATE,
        sets = 3, repsOrDuration = "8-10 reps", rpe = 9, restSeconds = 60,
        cue = "Hips stay level — NO arching. Brace core like you're about to get punched. Pull back using lats + abs together. Start with a short range and build.",
        repsProgression = "10 reps with good form → increase range of motion",
        endRangeProgression = "Full extension where chest nearly touches floor",
        peakStressProgression = "Progress to standing ab wheel roll-outs",
        musclesFocus = "Core (Anti-Extension), Lats, Shoulders",
    )

    // ── Tier 3: Advanced ──────────────────────────────────────────────────
    val pullUp = Exercise(
        name = "Pull Up",
        difficulty = Difficulty.ADVANCED,
        sets = 3, repsOrDuration = "6-10 reps", rpe = 8, restSeconds = 90,
        cue = "Full dead hang to start. Drive elbows DOWN — pull the bar to your chest, not your chin to the bar. Scapula retracted and depressed throughout.",
        repsProgression = "10 reps → wide grip or commando pull-ups",
        endRangeProgression = "Chest-to-bar pull-ups",
        peakStressProgression = "Weighted pull-ups",
        musclesFocus = "Lats, Biceps, Rear Deltoid",
    )

    /** Full pull day — SuperMover sequence */
    val pullDay = listOf(invertedRow, gobletCurl, abRollOut, tacticalPullUp)

    /** Beginner pull day */
    val pullDayBeginner = listOf(scapularShrug, invertedRow, gobletCurl)

    /** Advanced pull day */
    val pullDayAdvanced = listOf(pullUp, pullUpNegative, abRollOut, gobletCurl)
}

// ══ LEG EXERCISES ════════════════════════════════════════════════════════════
// Sources: SuperMover (Multi-Direction Lunges, Squat Walk, Precision Broad Jumps, Hindu Squats)
//          Road to Mastery (pistol squat progression)
//          MoMo (glute activation, single-leg work, hip stability cues)

object LegExercises {

    // ── Tier 1: Beginner ─────────────────────────────────────────────────
    val bodyweightSquat = Exercise(
        name = "Bodyweight Squat",
        difficulty = Difficulty.BEGINNER,
        sets = 3, repsOrDuration = "15-20 reps", rpe = 6, restSeconds = 0,
        cue = "Slow tempo. Push knees OUT — curl big toe into the ground for ankle stability. Tall chest. Squeeze butt hard at the top.",
        repsProgression = "20 reps → add jumping squats or sumo stance",
        endRangeProgression = "Deeper squat, pause 2 sec at bottom",
        peakStressProgression = "Jump squats or goblet squat with weight",
        musclesFocus = "Quads, Glutes, Hamstrings",
    )

    val stepBackLunge = Exercise(
        name = "Step Back Lunge",
        difficulty = Difficulty.BEGINNER,
        sets = 4, repsOrDuration = "15-20 each leg", rpe = 7, restSeconds = 0,
        cue = "Sit hips BACK with chest over the front knee. 3-second descent, 1-second pause. Push from the FRONT leg to stand — that's the glute doing the work.",
        repsProgression = "20 reps each leg → Bulgarian split squat",
        endRangeProgression = "Deeper descent — back knee brushes floor",
        peakStressProgression = "Add dumbbells or weighted vest",
        musclesFocus = "Glutes, Quads, Hamstrings",
    )

    // ── Tier 2: Intermediate ──────────────────────────────────────────────
    val multiDirectionLunge = Exercise(
        name = "Multi-Direction Lunge",
        difficulty = Difficulty.INTERMEDIATE,
        sets = 2, repsOrDuration = "10 each direction", rpe = 7, restSeconds = 60,
        cue = "Forward, lateral, reverse, curtsy — all four directions. Hips BACK in every variation. Squeeze butt at top of each rep. Weight stays on heel.",
        repsProgression = "10 each direction → add jump lunges",
        endRangeProgression = "Deeper range in each direction",
        peakStressProgression = "Add dumbbells or jump between directions",
        musclesFocus = "Glutes (all heads), Quads, Hip Abductors, Adductors",
    )

    val hinduSquat = Exercise(
        name = "Hindu Squat",
        difficulty = Difficulty.INTERMEDIATE,
        sets = 3, repsOrDuration = "Failure", rpe = 8, restSeconds = 60,
        cue = "Continuous fluid motion — arms swing forward on descent, heels rise slightly. High rep, rhythmic. Build the flow. Great for leg endurance and knee health.",
        repsProgression = "50 continuous reps → add weight to goblet position",
        endRangeProgression = "Full depth, butt to ankles",
        peakStressProgression = "Add a light plate held at chest",
        musclesFocus = "Quads, Glutes, Calves, Hip Flexors",
    )

    val sqautWalk = Exercise(
        name = "Squat Walk",
        difficulty = Difficulty.INTERMEDIATE,
        sets = 3, repsOrDuration = "1 minute", rpe = 8, restSeconds = 60,
        cue = "Stay in squat position throughout — don't rise up between steps. Push knees out, weight on heels, tall chest. This builds incredible quad endurance.",
        repsProgression = "1 minute without losing position → add lateral variation",
        endRangeProgression = "Lower squat height throughout",
        peakStressProgression = "Add resistance band around knees",
        musclesFocus = "Quads, Glutes, Hip Abductors",
    )

    val precisionBroadJump = Exercise(
        name = "Precision Broad Jump",
        difficulty = Difficulty.INTERMEDIATE,
        sets = 3, repsOrDuration = "10 reps", rpe = 8, restSeconds = 60,
        cue = "Load hips back, swing arms, jump as far as possible — land SOFTLY with knees bent and hips back. Absorb the landing. This is power training.",
        repsProgression = "10 reps with controlled landing → increase distance target",
        endRangeProgression = "Stick the landing for 2 sec each rep",
        peakStressProgression = "Jump to a target, or add a slight incline",
        musclesFocus = "Glutes, Quads, Hamstrings, Calves — Full Chain Power",
    )

    // ── Tier 3: Advanced ──────────────────────────────────────────────────
    val assistedPistolSquat = Exercise(
        name = "Assisted Pistol Squat",
        difficulty = Difficulty.ADVANCED,
        sets = 3, repsOrDuration = "6-8 each leg", rpe = 8, restSeconds = 60,
        cue = "Slow and controlled — curl big toe, reach hip back, maintain flat back. Hold TRX or doorframe for assistance. This trains the single-leg balance you need for everything.",
        repsProgression = "8 reps with light support → unassisted pistol",
        endRangeProgression = "Reduce assistance until freestanding",
        peakStressProgression = "Hold a light weight at chest",
        musclesFocus = "Quads, Glutes, Hip Stability, Ankle Stability",
    )

    val pistolSquat = Exercise(
        name = "Pistol Squat",
        difficulty = Difficulty.ADVANCED,
        sets = 3, repsOrDuration = "5-8 each leg", rpe = 9, restSeconds = 90,
        cue = "Extended leg stays parallel to floor. Descend slow — 3 sec down. Drive through the heel. Full extension at top — squeeze glute hard.",
        repsProgression = "10 reps → add weight or deficit pistol",
        endRangeProgression = "Heel elevated for deeper range",
        peakStressProgression = "Goblet pistol squat with DB",
        musclesFocus = "Quads, Glutes, Core, Ankle Stability",
    )

    /** Full leg day — SuperMover sequence */
    val legDay = listOf(multiDirectionLunge, sqautWalk, precisionBroadJump, hinduSquat)

    /** Beginner leg day */
    val legDayBeginner = listOf(bodyweightSquat, stepBackLunge)

    /** Advanced leg day */
    val legDayAdvanced = listOf(pistolSquat, precisionBroadJump, sqautWalk, multiDirectionLunge)
}

// ══ CARDIO / FULL BODY ═══════════════════════════════════════════════════════
// Sources: SuperMover (Full Body workout), MoMo (HIIT finishers)

object CardioExercises {

    val jumpRope = Exercise(
        name = "Jump Rope",
        difficulty = Difficulty.BEGINNER,
        sets = 1, repsOrDuration = "10 minutes", rpe = 7, restSeconds = 0,
        cue = "Arms close to sides — rotate the rope from the wrists, not the shoulders. Small hops — just enough to clear the rope. Slight hollow-body position.",
        repsProgression = "10 minutes continuous → learn double-unders",
        endRangeProgression = "Crossovers, double unders",
        peakStressProgression = "Heavier rope or weighted vest",
        musclesFocus = "Cardio, Calves, Coordination",
    )

    val shadowBoxing = Exercise(
        name = "Shadow Boxing",
        difficulty = Difficulty.BEGINNER,
        sets = 1, repsOrDuration = "10 minutes", rpe = 7, restSeconds = 0,
        cue = "Hands in guard position. Throw punches from the hip — turn the fist over at full extension. Light, fast, expressive. Throw roundhouse kicks too — opens hips and builds balance.",
        repsProgression = "10 minutes → add combinations and kicks",
        endRangeProgression = "More complex combinations",
        peakStressProgression = "Heavy bag or resistance bands",
        musclesFocus = "Full Body, Coordination, Hip Mobility",
    )

    val kettlebellSwing = Exercise(
        name = "Kettlebell Swing",
        difficulty = Difficulty.INTERMEDIATE,
        sets = 3, repsOrDuration = "50 reps", rpe = 8, restSeconds = 60,
        cue = "This is a HIP HINGE — not a squat. Snap the hips forward explosively. Bell floats to shoulder height from the momentum of the hips, not the arms. Squeeze glutes hard at the top.",
        repsProgression = "50 reps continuous → heavier KB",
        endRangeProgression = "American swing (overhead)",
        peakStressProgression = "Increase KB weight",
        musclesFocus = "Glutes, Hamstrings, Core, Cardio",
    )

    val hollowBodyHold = Exercise(
        name = "Hollow Body Hold",
        difficulty = Difficulty.INTERMEDIATE,
        sets = 3, repsOrDuration = "1 minute", rpe = 8, restSeconds = 60,
        cue = "Press lower back INTO the floor — no gap. Arms by ears, legs extended and low. Point toes. Tuck chin. This is the foundation of every gymnastics skill.",
        repsProgression = "1 minute → hollow body rocks",
        endRangeProgression = "Legs lower toward floor (harder)",
        peakStressProgression = "Add weight overhead or progress to hollow rocks",
        musclesFocus = "Core (Anti-Extension), Hip Flexors, Lats",
    )

    val burpee = Exercise(
        name = "Burpee",
        difficulty = Difficulty.INTERMEDIATE,
        sets = 3, repsOrDuration = "7-10 reps", rpe = 9, restSeconds = 30,
        cue = "Jump from floor → step or jump in → jump up and clap overhead. As fast as possible. This is a fat burner — maximize speed and power on each rep.",
        repsProgression = "10 reps → jump out (not step) version",
        endRangeProgression = "Add a push-up at the bottom",
        peakStressProgression = "Burpee + tuck jump or box jump",
        musclesFocus = "Full Body, Cardio, Power",
    )

    val mountainClimber = Exercise(
        name = "Mountain Climber",
        difficulty = Difficulty.INTERMEDIATE,
        sets = 3, repsOrDuration = "20 reps/side", rpe = 8, restSeconds = 30,
        cue = "Hips LOW — same height as shoulders. Drive knee to belly button — crunch from the core, not from momentum. Breathe rhythmically. Keep shoulders over wrists.",
        repsProgression = "20 reps each side → cross-body mountain climber",
        endRangeProgression = "Knee touches elbow at full crunch",
        peakStressProgression = "Sliders for more core load",
        musclesFocus = "Core, Hip Flexors, Shoulders, Cardio",
    )

    /** Full Body day — SuperMover sequence */
    val fullBodyDay = listOf(hollowBodyHold, kettlebellSwing, burpee, mountainClimber)
}

// ══ WORKOUT-TYPE → EXERCISE MAP ══════════════════════════════════════════════

fun WorkoutType.defaultExercises(): List<Exercise> = when (this) {
    WorkoutType.PUSH      -> PushExercises.pushDay
    WorkoutType.PULL      -> PullExercises.pullDay
    WorkoutType.LEGS      -> LegExercises.legDay
    WorkoutType.CARDIO    -> CardioExercises.fullBodyDay
    WorkoutType.FULL_BODY -> CardioExercises.fullBodyDay
}

fun WorkoutType.beginnerExercises(): List<Exercise> = when (this) {
    WorkoutType.PUSH      -> PushExercises.pushDayBeginner
    WorkoutType.PULL      -> PullExercises.pullDayBeginner
    WorkoutType.LEGS      -> LegExercises.legDayBeginner
    WorkoutType.CARDIO    -> listOf(CardioExercises.shadowBoxing, CardioExercises.hollowBodyHold, CardioExercises.burpee)
    WorkoutType.FULL_BODY -> listOf(CardioExercises.hollowBodyHold, CardioExercises.burpee, CardioExercises.mountainClimber)
}

fun WorkoutType.advancedExercises(): List<Exercise> = when (this) {
    WorkoutType.PUSH      -> PushExercises.pushDayAdvanced
    WorkoutType.PULL      -> PullExercises.pullDayAdvanced
    WorkoutType.LEGS      -> LegExercises.legDayAdvanced
    WorkoutType.CARDIO    -> listOf(CardioExercises.kettlebellSwing, CardioExercises.hollowBodyHold, CardioExercises.burpee, CardioExercises.mountainClimber)
    WorkoutType.FULL_BODY -> listOf(CardioExercises.kettlebellSwing, CardioExercises.hollowBodyHold, CardioExercises.burpee, CardioExercises.mountainClimber)
}
