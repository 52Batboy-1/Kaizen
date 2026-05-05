package com.kaizen.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kaizen.app.ui.theme.*
import kotlinx.coroutines.delay

data class StretchExercise(
    val name: String,
    val duration: Int,          // seconds
    val cue: String,
    val emoji: String,
    val targetArea: String,
)

private val MOBILITY_SEQUENCES = mapOf(
    "Morning Flow" to listOf(
        StretchExercise("Cat / Cow", 30, "Breathe into each position. 5 slow reps.", "🐱", "Spine, Thoracic"),
        StretchExercise("Child's Pose", 45, "Arms extended, sink hips to heels. Breathe into your lower back.", "🧘", "Lower Back, Hips"),
        StretchExercise("Hip Flexor Stretch", 30, "Squeeze glute of the back leg hard. Tall chest.", "🦵", "Hip Flexors, Quads"),
        StretchExercise("Thoracic Rotation", 30, "Thread the needle — rotate from mid-back not lower back.", "🔄", "Thoracic Spine"),
        StretchExercise("Standing Forward Fold", 45, "Soft knees. Let gravity do the work. Breathe.", "🙇", "Hamstrings, Lower Back"),
        StretchExercise("Shoulder Cross-Body", 30, "Pull the arm across. Keep shoulder down.", "💪", "Rear Deltoid, Rotator Cuff"),
    ),
    "Hip & Leg Flow" to listOf(
        StretchExercise("Frog Stretch", 60, "Knees wide, feet flared. Breathe through stomach. Relax into the strength.", "🐸", "Hip Adductors, Groin"),
        StretchExercise("Pigeon Pose (L)", 45, "Square your hips. Fold forward to deepen.", "🕊️", "Glutes, Hip Rotators"),
        StretchExercise("Pigeon Pose (R)", 45, "Square your hips. Fold forward to deepen.", "🕊️", "Glutes, Hip Rotators"),
        StretchExercise("Couch Stretch (L)", 45, "Squeeze glute, do a side lean to hit the lower back.", "🛋️", "Hip Flexors, Quad"),
        StretchExercise("Couch Stretch (R)", 45, "Squeeze glute, do a side lean to hit the lower back.", "🛋️", "Hip Flexors, Quad"),
        StretchExercise("Standing Quad Pull", 30, "Balance on one foot. Pull heel to glute. Squeeze.", "🦵", "Quadriceps"),
        StretchExercise("Calf Stretch", 30, "Heel on floor, lean into wall. Feel the pull.", "🦶", "Calves, Achilles"),
    ),
    "Upper Body Flow" to listOf(
        StretchExercise("Doorway Chest Stretch", 45, "Show the armpits. Breathe into the stretch.", "🚪", "Chest, Anterior Deltoid"),
        StretchExercise("Shoulder Blade Squeeze", 20, "Squeeze as hard as you can. Hold 3 sec. Release.", "🔧", "Rhomboids, Mid Trap"),
        StretchExercise("Neck Side Tilt (L)", 30, "Ear to shoulder. Breathe. No forcing.", "🧠", "Neck, Upper Trap"),
        StretchExercise("Neck Side Tilt (R)", 30, "Ear to shoulder. Breathe. No forcing.", "🧠", "Neck, Upper Trap"),
        StretchExercise("Thread the Needle (L)", 40, "From table-top, sweep arm under body. Stack hips.", "🪡", "Thoracic, Rotator Cuff"),
        StretchExercise("Thread the Needle (R)", 40, "From table-top, sweep arm under body. Stack hips.", "🪡", "Thoracic, Rotator Cuff"),
        StretchExercise("Wrist Circles", 20, "Full range. Both directions. Essential for push work.", "🤲", "Wrists, Forearms"),
    ),
    "Full Body Wind-Down" to listOf(
        StretchExercise("Supine Twist (L)", 45, "Shoulders stay flat. Let the knee drop. Breathe.", "🌀", "Lower Back, Hip Rotators"),
        StretchExercise("Supine Twist (R)", 45, "Shoulders stay flat. Let the knee drop. Breathe.", "🌀", "Lower Back, Hip Rotators"),
        StretchExercise("Happy Baby", 45, "Pull feet down toward floor. Rock gently.", "👶", "Inner Groin, Lower Back"),
        StretchExercise("Legs Up Wall", 60, "Hips close to wall. Arms relaxed. Breathe deeply.", "🧘", "Hamstrings, Recovery"),
        StretchExercise("Savasana Breathing", 60, "10 deep belly breaths. Inhale 4 count, exhale 6.", "😮‍💨", "Nervous System, Recovery"),
    ),
)

@Composable
fun MobilitySection() {
    var expanded        by remember { mutableStateOf(false) }
    var selectedFlow    by remember { mutableStateOf("Morning Flow") }
    var activeIndex     by remember { mutableStateOf<Int?>(null) }

    val accent = Color(0xFF67E8F9)  // flexibility cyan

    Surface(
        shape  = RoundedCornerShape(20.dp),
        color  = K.Card,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header ────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🧘", fontSize = 20.sp)
                    Column {
                        Text("Daily Mobility", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = K.Text)
                        Text("Stretch · Yoga · Recovery", fontSize = 10.sp, color = K.Muted)
                    }
                }
                Text(if (expanded) "▲" else "▼", fontSize = 12.sp, color = K.Muted)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // ── Flow selector ─────────────────────────────────
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                    ) {
                        MOBILITY_SEQUENCES.keys.forEach { flow ->
                            val selected = selectedFlow == flow
                            Surface(
                                modifier = Modifier.clickable {
                                    selectedFlow = flow
                                    activeIndex  = null
                                },
                                shape  = RoundedCornerShape(100.dp),
                                color  = if (selected) accent.copy(0.18f) else K.Card2,
                                border = BorderStroke(1.dp, if (selected) accent else K.Border),
                            ) {
                                Text(
                                    flow,
                                    modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize   = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color      = if (selected) accent else K.Muted,
                                )
                            }
                        }
                    }

                    // ── Stretch list ──────────────────────────────────
                    val exercises = MOBILITY_SEQUENCES[selectedFlow] ?: emptyList()
                    val totalTime = exercises.sumOf { it.duration }

                    Text(
                        "${exercises.size} exercises · ${totalTime / 60} min ${totalTime % 60}s",
                        fontSize = 10.sp,
                        color    = K.Muted,
                    )

                    exercises.forEachIndexed { index, ex ->
                        val isActive = activeIndex == index
                        StretchCard(
                            exercise = ex,
                            index    = index,
                            isActive = isActive,
                            accent   = accent,
                            onStart  = { activeIndex = if (isActive) null else index },
                            onDone   = {
                                activeIndex = if (index + 1 < exercises.size) index + 1 else null
                            },
                        )
                    }

                    // Start all button
                    Button(
                        onClick  = { activeIndex = 0 },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor   = Color(0xFF001A1F),
                        ),
                    ) {
                        Text("▶ Start Flow", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

// ── Individual stretch card with countdown timer ───────────────────────────

@Composable
private fun StretchCard(
    exercise: StretchExercise,
    index: Int,
    isActive: Boolean,
    accent: Color,
    onStart: () -> Unit,
    onDone: () -> Unit,
) {
    var remaining by remember(isActive, exercise.duration) {
        mutableIntStateOf(exercise.duration)
    }
    var running by remember(isActive) { mutableStateOf(isActive) }

    LaunchedEffect(isActive) {
        if (!isActive) { remaining = exercise.duration; running = false }
        else running = true
    }

    LaunchedEffect(running, remaining) {
        if (running && remaining > 0) {
            delay(1000)
            remaining--
            if (remaining == 0) { delay(300); onDone() }
        }
    }

    val pct by animateFloatAsState(
        targetValue   = if (isActive) remaining / exercise.duration.toFloat() else 1f,
        animationSpec = tween(900, easing = LinearEasing),
        label         = "stretch_pct",
    )

    Surface(
        shape  = RoundedCornerShape(14.dp),
        color  = if (isActive) accent.copy(0.10f) else K.Card2,
        border = BorderStroke(1.dp, if (isActive) accent.copy(0.45f) else K.Border),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(exercise.emoji, fontSize = 22.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(exercise.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isActive) K.Text else K.Text.copy(alpha = 0.8f))
                    Text(exercise.targetArea, fontSize = 10.sp, color = accent.copy(if (isActive) 0.9f else 0.55f))
                }
                // Timer display
                if (isActive) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "$remaining",
                            fontSize   = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color      = accent,
                            fontFamily = FontFamily.Monospace,
                        )
                        Text("sec", fontSize = 9.sp, color = K.Muted)
                    }
                } else {
                    Text(
                        "${exercise.duration}s",
                        fontSize   = 13.sp,
                        color      = K.Muted,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }

            // Progress bar (only when active)
            if (isActive) {
                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier.fillMaxWidth().height(3.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(0.06f))
                ) {
                    Box(
                        Modifier.fillMaxWidth(pct).fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(accent)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(exercise.cue, fontSize = 11.sp, color = accent.copy(0.85f), lineHeight = 15.sp)

                // Pause / skip controls
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedButton(
                        onClick = { running = !running },
                        shape   = RoundedCornerShape(100.dp),
                        border  = BorderStroke(1.dp, accent.copy(0.4f)),
                        colors  = ButtonDefaults.outlinedButtonColors(contentColor = accent),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    ) {
                        Text(if (running) "⏸" else "▶", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onDone,
                        shape   = RoundedCornerShape(100.dp),
                        border  = BorderStroke(1.dp, accent.copy(0.4f)),
                        colors  = ButtonDefaults.outlinedButtonColors(contentColor = accent),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    ) {
                        Text("Skip →", fontSize = 12.sp)
                    }
                }
            } else {
                // Tap to start hint
                TextButton(
                    onClick        = onStart,
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text("Tap to start", fontSize = 10.sp, color = K.Muted)
                }
            }
        }
    }
}
