package com.kaizen.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.kaizen.app.data.*
import com.kaizen.app.ui.theme.*

// ── Workout exercise data ─────────────────────────────────────────────────

data class WExercise(
    val name: String,
    val repsOrDuration: String,
    val cue: String,
    val progression: String,
    val muscles: String,
    val isCore: Boolean = false,
)

// ── Full ~1 hour exercise lists (scaled by difficulty) ────────────────────

private fun workoutExercises(type: WorkoutType, difficulty: Difficulty, sets: Int): List<WExercise> {
    val list = when (type) {
        WorkoutType.PUSH -> when (difficulty) {
            Difficulty.BEGINNER -> listOf(
                WExercise("Incline Push Up",    "10-15 reps",    "Shoulder blades back and DOWN. Proud chest. Push up, not back.", "Hit 15 → standard push ups", "Chest, Shoulders, Triceps"),
                WExercise("Pike Push Up",       "6-10 reps",     "Hips high — inverted V. Elbows track back toward hips.", "Hit 10 → elevate feet", "Shoulders, Triceps"),
                WExercise("Knee Push Up",       "12-20 reps",    "Full chest-to-floor range. Squeeze glutes throughout.", "→ Full push ups", "Chest, Triceps"),
                WExercise("Wall Shoulder Press","10-15 reps",    "Hands on wall, hips high. Walk feet closer for more load.", "→ Pike push up", "Shoulders"),
                WExercise("Diamond Push Up (Incline)","8-12 reps","Hands form diamond. Drive elbows back toward hips.", "→ Floor diamond push up", "Triceps, Inner Chest"),
                WExercise("Plank Hold",         "30-45 sec",     "Squeeze everything — glutes, abs, quads. Rigid plank.", "→ 60 sec hold", "Core, Shoulders", true),
            )
            Difficulty.INTERMEDIATE -> listOf(
                WExercise("Dips",               "8-15 reps",     "Chest leans forward. Use the myotatic bounce at the bottom. Full lock-out.", "→ Weighted dips", "Chest, Triceps, Delts"),
                WExercise("Push Up",            "15-25 reps",    "Continuous tension — almost bounce. Subtle hollow body. No elbow flare.", "→ Decline or archer", "Chest, Shoulders, Triceps"),
                WExercise("Pike Push Up",       "10-15 reps",    "Hips high. Head in front of hands. Elbows back toward hips.", "Feet elevated → shoulder press", "Shoulders"),
                WExercise("Diamond Push Up",    "10-15 reps",    "Lean chest forward. Drive elbows back to load triceps.", "→ One-arm assisted", "Triceps, Inner Chest"),
                WExercise("Lizard Crawl",       "60 sec",        "Elbow nearly touches floor. Opposite arm+leg. Hips LOW.", "→ Weighted vest", "Chest, Shoulders, Core, Hips"),
                WExercise("Lateral Plank Walk", "10 steps L+R",  "Squeeze butt, crunch abs, stay level throughout.", "→ Add shoulder tap", "Core, Shoulders", true),
            )
            Difficulty.ADVANCED -> listOf(
                WExercise("Dips to Failure",           "Failure",     "Deep range. Chest forward. Stretch reflex at bottom. Full lock-out at top.", "→ Ring dips or weighted", "Chest, Triceps"),
                WExercise("Decline Push Up",           "12-20 reps",  "Feet elevated. Upper chest emphasis. Squeeze armpits.", "→ Handstand push up negatives", "Upper Chest, Shoulders"),
                WExercise("Pseudo-Planche Push Up",    "8-12 reps",   "Hands toward hips. Lean forward as far as possible. Kills front delts.", "→ Increase lean angle", "Anterior Deltoid, Straight-Arm"),
                WExercise("Diamond Push Up",           "Failure",     "Elbows drive back toward hips. Full depth.", "→ One-arm assisted push up", "Triceps, Inner Chest"),
                WExercise("Archer Push Up",            "6-10 each",   "Wide hand position. Shift weight to one arm. Other arm guides only.", "→ One arm push up", "Chest, Triceps, Stability"),
                WExercise("Hollow Body Hold",          "60 sec",      "Lower back pressed INTO floor. Arms by ears. Point toes. No gap.", "→ Add weight overhead", "Core Anti-Extension", true),
            )
        }

        WorkoutType.PULL -> when (difficulty) {
            Difficulty.BEGINNER -> listOf(
                WExercise("Dead Hang",          "20-30 sec",     "Full grip. Shoulders packed down. Breathe steadily.", "→ Scapular shrugs", "Grip, Lats"),
                WExercise("Scapular Shrug",     "6-8 reps",      "Dead hang. Shrug shoulder blades DOWN without bending elbows. Hold 2 sec.", "→ Inverted rows", "Lower Traps, Serratus"),
                WExercise("Incline Row",        "10-15 reps",    "Round shoulders down slow. Squeeze elbows to belly button. Proud chest.", "→ Lower the angle", "Upper Back, Biceps"),
                WExercise("Reverse Snow Angel","12-15 reps",     "Face down. Arms sweep from hips to overhead. Squeeze back hard.", "→ Add light weight", "Rear Delts, Rhomboids"),
                WExercise("Superman Hold",      "15 reps 3s hold","Squeeze from butt. Hands 45° from head. Retract spine.", "→ T-Superman", "Lower Back, Glutes"),
                WExercise("Dead Bug",           "10 reps/side",  "Lower back pressed flat. Opposite arm + leg. Exhale fully.", "→ Add weight", "Core Anti-Extension", true),
            )
            Difficulty.INTERMEDIATE -> listOf(
                WExercise("Inverted Row",       "12-20 reps",    "Shoulders down slow. Elbows to belly button. PROUD chest. Pause at top.", "→ More horizontal angle", "Upper Back, Rear Delts, Biceps"),
                WExercise("Pull-Up Negative",   "6-8 reps 5sec", "Jump to top. Fight gravity for a full 5 seconds on the way down.", "→ 8 sec negatives, then pull-ups", "Lats, Biceps"),
                WExercise("Bodyweight Bicep Curl","15-25 reps",  "Elbows forward. Squeeze from bicep not shoulder. Slow eccentric.", "→ Chin-ups", "Biceps, Brachialis"),
                WExercise("Ab Wheel Roll-Out",  "8-12 reps",     "Hips level — NO arching. Pull back with lats + abs together.", "→ Increase range", "Core, Lats"),
                WExercise("Reverse Plank",      "30-45 sec",     "Hips up, squeeze glutes hard. Shoulders retracted.", "→ 60 sec hold", "Rear Delts, Glutes, Core"),
                WExercise("Hollow Body Hold",   "45-60 sec",     "Lower back INTO floor. Legs low, arms overhead. Point toes.", "→ Hollow rocks", "Core Anti-Extension", true),
            )
            Difficulty.ADVANCED -> listOf(
                WExercise("Pull-Up",            "Failure",       "Full dead hang start. Drive elbows DOWN — bar to chest, not chin to bar.", "→ Wide grip or weighted", "Lats, Biceps"),
                WExercise("Wide Grip Pull-Up",  "6-10 reps",     "Wider than shoulder. Focus on lat spread — armpit toward the floor.", "→ Archer pull-ups", "Lats outer, Rear Delts"),
                WExercise("Commando Pull-Up",   "6-8 reps",      "Neutral grip, hands staggered. Alternate which side your head goes to.", "→ L-sit pull-up", "Lats, Biceps, Core"),
                WExercise("Ab Wheel Roll-Out",  "10-15 reps",    "Full extension, chest nearly touches floor. Explode back through lats+abs.", "→ Standing ab wheel", "Core, Lats"),
                WExercise("Bodyweight Bicep Curl","Failure",     "Maximum supination at top. Fully extend at bottom. Slow 3-sec eccentric.", "→ Chin-ups", "Biceps"),
                WExercise("Dragon Flag",        "5-8 reps",      "Lower body slowly — 3 sec descent. Hips stay in line. Pull through abs.", "→ Ankle weights", "Core Full Flexion", true),
            )
        }

        WorkoutType.LEGS -> when (difficulty) {
            Difficulty.BEGINNER -> listOf(
                WExercise("Bodyweight Squat",   "20-30 reps",    "Push knees OUT. Curl big toe. Tall chest. Squeeze butt hard at top.", "→ Jump squats", "Quads, Glutes, Hams"),
                WExercise("Step Back Lunge",    "12-15 each leg","Hips BACK, chest over knee. 3-sec descent. Push from FRONT leg.", "→ Bulgarian split squat", "Glutes, Quads"),
                WExercise("Glute Bridge",       "15 reps + 30s hold","Load hip from heel. Knees out. Squeeze butt as hard as possible.", "→ Single leg glute bridge", "Glutes, Hamstrings"),
                WExercise("Wall Sit",           "45-60 sec",     "Hips at knee height. Round lower back into wall. Tall chest.", "→ Single leg wall sit", "Quads, Glutes"),
                WExercise("Step Up",            "12 each leg",   "Sit hip back, chest over knee. Drive from heel. Lower slow.", "→ Deep step up", "Glutes, Quads"),
                WExercise("Lying Leg Lift",     "12-15 reps",    "Belly button into spine. Lower legs slowly. Raise by crunching from abs.", "→ Add ankle weight", "Lower Abs, Core", true),
            )
            Difficulty.INTERMEDIATE -> listOf(
                WExercise("Multi-Direction Lunge","10 each way", "Forward, lateral, reverse, curtsy — all four. Hips BACK every time.", "→ Jump lunges", "Glutes all heads, Quads, Adductors"),
                WExercise("Hindu Squat",        "40-60 reps",    "Fluid motion — arms swing on descent, heels rise slightly. Rhythmic.", "→ Add goblet weight", "Quads, Glutes, Calves"),
                WExercise("Squat Walk",         "60-90 sec",     "Stay in squat the whole time. Push knees out. Weight on heels.", "→ Add resistance band", "Quads, Glutes, Hip Abductors"),
                WExercise("Precision Broad Jump","10 reps",      "Max distance. Land softly — absorb fully. Stick for 2 sec.", "→ Increase distance", "Glutes, Quads, Hams — Power"),
                WExercise("Single Leg Glute Bridge","15 each",   "Drive from heel. Tuck ribs. Squeeze butt at top. Don't rotate hips.", "→ Elevated single leg", "Glutes, Hamstrings"),
                WExercise("Hanging Knee Raise", "10-15 reps",    "Control the descent. Crunch from abs not hip flexors. Hollow body.", "→ Hanging leg raise", "Core, Hip Flexors", true),
            )
            Difficulty.ADVANCED -> listOf(
                WExercise("Pistol Squat",       "6-10 each",     "Extended leg parallel to floor. 3-sec descent. Drive through heel.", "→ Deficit pistol squat", "Quads, Glutes, Ankle Stability"),
                WExercise("Single Leg Hop",     "30 sec each",   "Tall posture. Quick reaction. Curl big toe. Small controlled hops.", "→ Lateral hops", "Ankle Stability, Calves"),
                WExercise("Precision Broad Jump","12 reps",      "Max distance. Explode completely. Absorb completely. Zero wasted energy.", "→ Box jump", "Full Chain Power"),
                WExercise("Bulgarian Split Squat","10-12 each",  "Rear foot elevated. Drive hips back. Front leg does the work.", "→ Add weight", "Glutes, Quads, Hip Stability"),
                WExercise("Nordic Curl Negative","5-8 reps",     "Anchor feet. Slow controlled descent to floor — 4 sec. Catch yourself.", "→ Full nordic curl", "Hamstrings — Injury Prevention"),
                WExercise("Dragon Flag",        "6-8 reps",      "Body rigid from shoulders to heels. Lower slow. Pull back through abs.", "→ Add ankle weights", "Core Full Range", true),
            )
        }

        WorkoutType.CARDIO -> when (difficulty) {
            Difficulty.BEGINNER -> listOf(
                WExercise("Jumping Jacks",       "60 reps",      "Tall posture, brace core. Land softly.", "→ High knees", "Cardio, Calves"),
                WExercise("Mountain Climber",    "20 reps/side", "Hips LOW. Drive knee to belly button from the core. Breathe.", "→ Cross-body mountain climber", "Core, Hip Flexors, Cardio"),
                WExercise("Bodyweight Squat",    "25 reps",      "Slow and controlled. Squeeze butt at top.", "→ Jump squats", "Quads, Glutes"),
                WExercise("Push Up",             "10-15 reps",   "Controlled tempo. Full range.", "→ Decline push up", "Chest, Shoulders"),
                WExercise("Shadow Boxing",       "3 min",        "Hands in guard. Throw from the hip. Light and fast.", "→ Add kicks", "Full Body, Coordination"),
                WExercise("Hollow Body Hold",    "30-45 sec",    "Press lower back INTO floor. Arms by ears. Point toes.", "→ 60 sec hold", "Core", true),
            )
            Difficulty.INTERMEDIATE -> listOf(
                WExercise("Burpee",              "10-15 reps",   "Full speed. Jump in, jump up, clap overhead. Max power.", "→ Add push up at bottom", "Full Body, Cardio, Power"),
                WExercise("Mountain Climber",    "25 reps/side", "Hips same height as shoulders. Drive from core. Breathe.", "→ Sliders", "Core, Hip Flexors, Cardio"),
                WExercise("Jump Squat",          "15-20 reps",   "Explosive. Go for max height. Good form. Land soft.", "→ Add weight vest", "Quads, Glutes, Power"),
                WExercise("Shadow Boxing + Kicks","5 min",       "Combinations — jab, cross, hook, roundhouse. Move your feet.", "→ Heavy bag", "Full Body, Coordination"),
                WExercise("Hollow Body Rocks",   "20 reps",      "Perfect hollow position maintained throughout. Arms + legs fixed.", "→ Add weight overhead", "Core", true),
                WExercise("Sprint in Place",     "30 sec on/off x5","Maximum knee drive. Stay on balls of feet.", "→ Increase duration", "Cardio, Coordination"),
            )
            Difficulty.ADVANCED -> listOf(
                WExercise("Burpee + Tuck Jump",  "12-15 reps",   "Full burpee then explode — knees to chest at top. Land soft.", "→ Add weight vest", "Full Body, Power"),
                WExercise("Clapping Push Up",    "8-12 reps",    "Explosive push. Get full air time. Land soft with bent elbows.", "→ Triple clap", "Chest, Power, Plyometrics"),
                WExercise("Jump Lunge",          "12 each leg",  "Explosive switch in the air. Stay tall. Land on the heel.", "→ Add weight vest", "Glutes, Quads, Power"),
                WExercise("Shadow Boxing + Kicks","8 min",       "Full combinations with power kicks. Move like you mean it.", "→ Heavy bag rounds", "Full Body, Coordination"),
                WExercise("Hollow Body Rocks",   "25 reps",      "Position maintained the entire set. No sagging at bottom.", "→ Add ankle weight", "Core", true),
                WExercise("Explosive Broad Jump x5","5 reps",   "Chain 5 jumps continuously. Maximum distance each time.", "→ Uphill jumps", "Full Chain Power, Cardio"),
            )
        }
        WorkoutType.FULL_BODY -> when (difficulty) {
            Difficulty.BEGINNER -> listOf(
                WExercise("Squat",               "12-15 reps",   "Hips below parallel. Chest up. Squeeze glutes at top.", "→ Jump squat", "Quads, Glutes"),
                WExercise("Push Up",             "8-12 reps",    "Full range. Proud chest. Elbows 45° — not flared.", "→ Decline push up", "Chest, Shoulders, Triceps"),
                WExercise("Inverted Row",        "8-12 reps",    "Pull chest to bar. Squeeze shoulder blades together.", "→ Feet elevated", "Back, Biceps"),
                WExercise("Reverse Lunge",       "10 reps/side", "Back knee hovers 1 inch from floor. Front shin stays vertical.", "→ Walking lunge", "Quads, Glutes, Hamstrings"),
                WExercise("Dead Bug",            "8 reps/side",  "Exhale fully. Lower back pressed into floor throughout.", "→ Add resistance band", "Core", true),
                WExercise("Glute Bridge",        "15-20 reps",   "Drive hips up, squeeze glutes hard at top. Hold 1 sec.", "→ Single leg bridge", "Glutes, Hamstrings"),
            )
            Difficulty.INTERMEDIATE -> listOf(
                WExercise("Goblet Squat",        "15 reps",      "Elbows track inside knees. Full depth. Drive knees out.", "→ Pause squat", "Quads, Glutes, Core"),
                WExercise("Push Up",             "15-25 reps",   "Continuous tension. Subtle hollow body. No elbow flare.", "→ Archer push up", "Chest, Shoulders, Triceps"),
                WExercise("Inverted Row",        "12-15 reps",   "Feet elevated version. Pause at top — shoulder blades in.", "→ One-arm assisted", "Back, Biceps, Rear Delts"),
                WExercise("Bulgarian Split Squat","10 reps/side","Back foot elevated. Lean torso forward ~15°. Full depth.", "→ Add weight", "Quads, Glutes, Hip Flexors"),
                WExercise("Pike Push Up",        "10-12 reps",   "Hips high — inverted V. Elbows track back.", "→ Feet elevated", "Shoulders, Triceps"),
                WExercise("Hollow Body Hold",    "30-45 sec",    "Lower back into floor. Arms overhead. Point toes.", "→ Add rocks", "Core", true),
            )
            Difficulty.ADVANCED -> listOf(
                WExercise("Pistol Squat",        "5-8 each",     "Heel flat. Use arms for balance. Full depth. Control descent.", "→ Weighted pistol", "Quads, Glutes, Balance"),
                WExercise("Dips",                "Failure",      "Deep range. Chest forward. Stretch reflex. Full lock-out.", "→ Weighted", "Chest, Triceps, Shoulders"),
                WExercise("One-Arm Row",         "10 each",      "Pull elbow to ceiling. Full shoulder blade retraction.", "→ One-arm chin-up negatives", "Back, Biceps"),
                WExercise("Jump Lunge",          "12 each leg",  "Explosive switch in the air. Stay tall. Land on heel.", "→ Add weight vest", "Quads, Glutes, Power"),
                WExercise("Pike Push Up (Feet Elevated)","10 reps","Maximum forward lean. Head through arms at bottom.", "→ Handstand negatives", "Shoulders"),
                WExercise("Dragon Flag",         "5-8 reps",     "Lower slowly — 3 sec descent. Full body rigid throughout.", "→ Weighted", "Core, Full Chain", true),
            )
        }
    }

    // Apply set count from Whoop scaling
    return list
}

// ── Active Workout Sheet ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutSheet(
    workoutType: WorkoutType,
    userDifficulty: Difficulty = Difficulty.INTERMEDIATE,
    defaultSets: Int           = 4,
    defaultRestSecs: Int       = 90,
    onDismiss: () -> Unit,
    onLogPR: (exerciseName: String, difficulty: Difficulty, repsOrDuration: String) -> Unit = { _, _, _ -> },
) {
    var currentDifficulty by remember { mutableStateOf(userDifficulty) }
    var setsPerExercise   by remember { mutableIntStateOf(defaultSets) }

    val exercises = remember(workoutType, currentDifficulty) {
        workoutExercises(workoutType, currentDifficulty, setsPerExercise)
    }
    val mainExercises = exercises.filter { !it.isCore }
    val coreExercises = workoutType.coreCircuit   // from Models.kt

    val setsCompleted = remember { mutableStateMapOf<Int, Int>() }
    var restingAfter  by remember { mutableStateOf<Int?>(null) }
    var restSeconds   by remember { mutableIntStateOf(defaultRestSecs.coerceAtLeast(90)) }
    var expandedIndex by remember { mutableIntStateOf(0) }

    val wColor    = workoutType.color()
    val totalSets = mainExercises.size * setsPerExercise + coreExercises.sumOf { it.sets }
    val doneSets  = setsCompleted.values.sum()
    val animPct   by animateFloatAsState((doneSets / totalSets.toFloat()).coerceIn(0f, 1f), tween(400))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = K.Card2,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle       = {
            Box(Modifier.padding(top = 14.dp, bottom = 6.dp).size(width = 36.dp, height = 4.dp).clip(RoundedCornerShape(4.dp)).background(K.Border))
        },
    ) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.95f)) {

            // ── Header ────────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(10.dp), color = wColor.copy(0.2f), modifier = Modifier.size(38.dp)) {
                        Box(
                            contentAlignment = Alignment.Center) { Text(workoutType.emoji, fontSize = 20.sp) }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${workoutType.label} Day", style = MaterialTheme.typography.titleMedium)
                        Text("${currentDifficulty.name.lowercase().replaceFirstChar{it.uppercase()}} · ${mainExercises.size} exercises · ~${(mainExercises.size * setsPerExercise * 2.5).toInt()} min",
                            fontSize = 11.sp, color = K.Muted)
                    }
                }
                Spacer(Modifier.height(10.dp))

                // Whoop-derived scaling info
                Surface(shape = RoundedCornerShape(10.dp), color = wColor.copy(0.08f), border = BorderStroke(1.dp, wColor.copy(0.2f))) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("💪 ${currentDifficulty.name} · ${setsPerExercise} sets · ${restSeconds}s rest",
                            fontSize = 11.sp, color = wColor, fontWeight = FontWeight.SemiBold)
                        // Manual override
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Difficulty.values().forEach { d ->
                                val sel = d == currentDifficulty
                                Surface(modifier = Modifier.clickable { currentDifficulty = d }, shape = RoundedCornerShape(6.dp),
                                    color = if (sel) wColor.copy(0.25f) else K.Card, border = BorderStroke(1.dp, if (sel) wColor else K.Border)) {
                                    Text(d.name.take(3), modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        fontSize = 9.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (sel) wColor else K.Muted)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Overall progress
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("$doneSets / $totalSets sets", fontSize = 11.sp, color = K.Muted)
                    if (doneSets >= totalSets) Text("🎉 Complete!", fontSize = 11.sp, color = K.Health, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(0.06f))) {
                    Box(Modifier.fillMaxWidth(animPct).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(wColor))
                }
            }

            // ── Rest timer ────────────────────────────────────────────
            AnimatedVisibility(visible = restingAfter != null, enter = fadeIn() + slideInVertically(), exit = fadeOut() + slideOutVertically()) {
                Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp), color = wColor.copy(0.10f), border = BorderStroke(1.dp, wColor.copy(0.3f))) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        if (restingAfter != null) {
                            RestTimer(
                                initialSeconds = restSeconds,
                                accentColor    = wColor,
                                onFinished     = { restingAfter = null },
                                onSkip         = { restingAfter = null },
                            )
                        }
                    }
                }
            }

            // ── Exercise list ─────────────────────────────────────────
            LazyColumn(
                modifier        = Modifier.weight(1f),
                contentPadding  = PaddingValues(horizontal = 22.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {

                // Main exercises
                item { SectionLabel("MAIN EXERCISES", wColor) }

                itemsIndexed(mainExercises) { index, ex ->
                    WExerciseCard(
                        exercise      = ex,
                        index         = index,
                        sets          = setsPerExercise,
                        setsCompleted = setsCompleted[index] ?: 0,
                        accentColor   = wColor,
                        expanded      = expandedIndex == index,
                        onExpand      = { expandedIndex = index },


                    onSetTapped   = { newDone ->
                            val prev = setsCompleted[index] ?: 0
                            setsCompleted[index] = newDone
                            if (newDone > prev && newDone < setsPerExercise) {
                                restSeconds  = defaultRestSecs.coerceAtLeast(90)
                                restingAfter = index
                            }
                            if (newDone >= setsPerExercise) {
                                restingAfter = null
                                if (index + 1 < mainExercises.size) expandedIndex = index + 1
                                else expandedIndex = mainExercises.size  // jump to core
                            }
                        },
                    )
                }

                // Core finisher
                item { SectionLabel("🔩 CORE FINISHER", wColor) }

                itemsIndexed(coreExercises) { index, cex ->
                    val coreIndex = mainExercises.size + index
                    CoreExerciseCard(
                        exercise      = cex,
                        accentColor   = wColor,
                        setsCompleted = setsCompleted[coreIndex] ?: 0,

                    onSetTapped   = { newDone ->
                            val prev = setsCompleted[coreIndex] ?: 0
                            setsCompleted[coreIndex] = newDone
                            if (newDone > prev && newDone < cex.sets) {
                                restSeconds  = 90  // core rest always 90s
                                restingAfter = coreIndex
                            }
                            if (newDone >= cex.sets) restingAfter = null
                        },
                    )
                }

                // Finish button
                item {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick  = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = if (doneSets >= totalSets) K.Health else wColor,
                            contentColor   = Color.Black,
                        ),
                    ) {
                        Text(if (doneSets >= totalSets) "✅ Finish Workout" else "Save & Exit",
                            fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, color: Color) {
    Text(text, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.sp),
        color = color, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun WExerciseCard(
    exercise: WExercise,
    index: Int,
    sets: Int,
    setsCompleted: Int,
    accentColor: Color,
    expanded: Boolean,
    onExpand: () -> Unit,
    onSetTapped: (Int) -> Unit,
    onLogPR: (repsOrDuration: String) -> Unit = {},
) {
    val allDone = setsCompleted >= sets
    val bgColor by animateColorAsState(targetValue = if (allDone) accentColor.copy(0.12f) else K.Card, animationSpec = tween(300), label = "bg")
    val border by animateColorAsState(targetValue = if (allDone) accentColor.copy(0.4f) else K.Border, animationSpec = tween(300), label = "border")

    Surface(modifier = Modifier.fillMaxWidth().clickable { onExpand() },
        shape = RoundedCornerShape(16.dp), color = bgColor, border = BorderStroke(1.dp, border)) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = RoundedCornerShape(8.dp), color = if (allDone) accentColor.copy(0.3f) else accentColor.copy(0.12f), modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (allDone) "✓" else "${setsCompleted}/${sets}",
                            fontSize = if (allDone) 16.sp else 11.sp, fontWeight = FontWeight.Bold,
                            color = if (allDone) Color.Black else accentColor)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(exercise.name, style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                        maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (allDone) K.Muted else K.Text)
                    Text("${sets} sets · ${exercise.repsOrDuration}", fontSize = 11.sp, color = K.Muted)
                }
                Text(if (expanded) "▲" else "▼", fontSize = 12.sp, color = K.Muted)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(10.dp), color = accentColor.copy(0.08f)) {
                        Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("💡", fontSize = 14.sp)
                            Text(exercise.cue, fontSize = 12.sp, color = accentColor.copy(0.9f), lineHeight = 16.sp)
                        }
                    }
                    Text("→ ${exercise.progression}", fontSize = 10.sp, color = K.Muted, lineHeight = 14.sp)
                    Text("🎯 ${exercise.muscles}", fontSize = 10.sp, color = K.Muted)

                    // Set dots
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        for (i in 1..sets) {
                            val setDone = i <= setsCompleted
                            Surface(
                                modifier = Modifier.size(44.dp).clickable {
                                    val newCount = if (setDone && i == setsCompleted) i - 1 else i
                                    onSetTapped(newCount.coerceIn(0, sets))
                                },
                                shape  = RoundedCornerShape(12.dp),
                                color  = if (setDone) accentColor else accentColor.copy(0.10f),
                                border = BorderStroke(1.dp, accentColor.copy(if (setDone) 1f else 0.3f)),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(if (setDone) "✓" else "$i",
                                        fontSize = if (setDone) 16.sp else 13.sp, fontWeight = FontWeight.Bold,
                                        color = if (setDone) Color.Black else accentColor.copy(0.6f))
                                }
                            }
                        }
                        if (setsCompleted in 1 until sets) {
                            Text("⏱ rest", fontSize = 11.sp, color = K.Muted, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoreExerciseCard(
    exercise: CoreExercise,
    accentColor: Color,
    setsCompleted: Int,
    onSetTapped: (Int) -> Unit,
) {
    val allDone = setsCompleted >= exercise.sets
    val bgColor by animateColorAsState(targetValue = if (allDone) accentColor.copy(0.10f) else K.Card, animationSpec = tween(250), label = "bg2")

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = bgColor, border = BorderStroke(1.dp, if (allDone) accentColor.copy(0.35f) else K.Border)) {
        Column(modifier = Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(exercise.name, style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp), color = if (allDone) K.Muted else K.Text)
                    Text("${exercise.sets} sets · ${exercise.repsOrSeconds}", fontSize = 11.sp, color = K.Muted)
                }
                if (allDone) Text("✓", fontSize = 18.sp, color = accentColor, fontWeight = FontWeight.Bold)
            }
            Text("💡 ${exercise.cue}", fontSize = 10.sp, color = accentColor.copy(0.8f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 1..exercise.sets) {
                    val setDone = i <= setsCompleted
                    Surface(
                        modifier = Modifier.size(40.dp).clickable {
                            val newCount = if (setDone && i == setsCompleted) i - 1 else i
                            onSetTapped(newCount.coerceIn(0, exercise.sets))
                        },
                        shape  = RoundedCornerShape(10.dp),
                        color  = if (setDone) accentColor else accentColor.copy(0.10f),
                        border = BorderStroke(1.dp, accentColor.copy(if (setDone) 1f else 0.3f)),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(if (setDone) "✓" else "$i",
                                fontSize = if (setDone) 14.sp else 12.sp, fontWeight = FontWeight.Bold,
                                color = if (setDone) Color.Black else accentColor.copy(0.6f))
                        }
                    }
                }
            }
        }
    }
}

// ── PR log row ─────────────────────────────────────────────────────────────

@Composable
private fun PRLogRow(exerciseName: String, onLog: (String) -> Unit) {
    var repsInput by remember { mutableStateOf("") }
    var logged    by remember { mutableStateOf(false) }

    if (logged) {
        Text("🏆 PR logged!", fontSize = 11.sp, color = Color(0xFFFFE66D), fontWeight = FontWeight.Bold)
        return
    }

    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("🏆", fontSize = 14.sp)
        androidx.compose.foundation.text.BasicTextField(
            value           = repsInput,
            onValueChange   = { repsInput = it },
            singleLine      = true,
            textStyle       = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = Color(0xFFFFE66D), fontWeight = FontWeight.Bold),
            decorationBox   = { inner ->
                if (repsInput.isEmpty()) androidx.compose.material3.Text("Log PR (e.g. 12 reps)...", fontSize = 12.sp, color = K.Muted.copy(0.5f))
                else inner()
            },
            modifier = Modifier.weight(1f),
        )
        Surface(
            modifier  = Modifier.clickable {
                if (repsInput.isNotBlank()) { onLog(repsInput); logged = true }
            },
            shape     = RoundedCornerShape(8.dp),
            color     = Color(0xFFFFE66D).copy(0.15f),
            border    = BorderStroke(1.dp, Color(0xFFFFE66D).copy(0.4f)),
        ) {
            Text("Save", modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), fontSize = 11.sp, color = Color(0xFFFFE66D), fontWeight = FontWeight.Bold)
        }
    }
}
