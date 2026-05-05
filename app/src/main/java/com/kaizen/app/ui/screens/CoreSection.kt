package com.kaizen.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kaizen.app.data.*
import com.kaizen.app.ui.theme.*

/**
 * CoreSection — shown as part of every workout day.
 *
 * Displays the 3 core exercises specific to [workoutType] and lets the user
 * tick off individual sets. Progress is persisted via [onSetTapped].
 *
 * @param workoutType   The day's workout — determines which exercises appear.
 * @param coreSetsMap   Current state: exercise name → sets completed so far.
 * @param onSetTapped   Called with (exerciseName, newSetCount) when a set dot is tapped.
 */
@Composable
fun CoreSection(
    workoutType: WorkoutType,
    coreSetsMap: Map<String, Int>,
    onSetTapped: (exerciseName: String, newCount: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val circuit      = workoutType.coreCircuit
    val totalDone    = coreSetsMap.values.sumOf { it }
    val totalTarget  = circuit.sumOf { it.sets }
    val allComplete  = totalDone >= totalTarget
    val pct          = if (totalTarget > 0) totalDone / totalTarget.toFloat() else 0f
    val wColor       = workoutType.color()

    Column(modifier = modifier) {

        // ── Section header ────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🔩", fontSize = 18.sp)
                Text(
                    "Core Circuit",
                    style      = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                    color      = K.Text,
                )
            }
            // Mini progress pill
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = if (allComplete) K.Health.copy(alpha = 0.2f) else K.Card2,
                border = BorderStroke(1.dp, if (allComplete) K.Health.copy(alpha = 0.5f) else K.Border),
            ) {
                Text(
                    text     = if (allComplete) "✓ Done" else "$totalDone / $totalTarget sets",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color    = if (allComplete) K.Health else K.Muted,
                )
            }
        }

        // ── Mini progress bar ─────────────────────────────────────────
        val animPct by animateFloatAsState(pct, tween(400))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .padding(bottom = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animPct)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (allComplete) K.Health else wColor),
            )
        }

        Spacer(Modifier.height(10.dp))

        // ── Exercise rows ─────────────────────────────────────────────
        circuit.forEach { exercise ->
            CoreExerciseRow(
                exercise    = exercise,
                completed   = coreSetsMap[exercise.name] ?: 0,
                accentColor = wColor,
                onSetTapped = { newCount -> onSetTapped(exercise.name, newCount) },
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Individual exercise row ────────────────────────────────────────────────

@Composable
private fun CoreExerciseRow(
    exercise: CoreExercise,
    completed: Int,
    accentColor: Color,
    onSetTapped: (Int) -> Unit,
) {
    val allDone = completed >= exercise.sets
    val bgColor by animateColorAsState(
        if (allDone) accentColor.copy(alpha = 0.10f) else K.Card,
        tween(250),
    )
    val borderColor by animateColorAsState(
        if (allDone) accentColor.copy(alpha = 0.40f) else K.Border,
        tween(250),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        // Name + volume
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = exercise.name,
                    style      = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                    color      = if (allDone) K.Muted else K.Text,
                )
                Text(
                    text     = "${exercise.sets} sets · ${exercise.repsOrSeconds}",
                    fontSize = 11.sp,
                    color    = K.Muted,
                )
            }
            // Coaching cue chip
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.10f),
            ) {
                Text(
                    text     = "💡",
                    modifier = Modifier.padding(6.dp),
                    fontSize = 14.sp,
                )
            }
        }

        // Coaching cue (expandable on 💡 tap — simplified: always visible)
        Text(
            text     = exercise.cue,
            fontSize = 10.sp,
            color    = accentColor.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 5.dp),
        )

        Spacer(Modifier.height(10.dp))

        // Set dots
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 1..exercise.sets) {
                val done  = i <= completed
                val scale by animateFloatAsState(
                    if (done) 1f else 0.88f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                )
                Box(
                    modifier = Modifier
                        .scale(scale)
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (done) accentColor else accentColor.copy(alpha = 0.10f))
                        .border(
                            1.dp,
                            if (done) accentColor else accentColor.copy(alpha = 0.30f),
                            RoundedCornerShape(10.dp),
                        )
                        .clickable {
                            // Tap toggles: if this set is already the last done, undo it
                            val newCount = if (done && i == completed) i - 1 else i
                            onSetTapped(newCount.coerceIn(0, exercise.sets))
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (done) {
                        Text("✓", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    } else {
                        Text("$i", fontSize = 12.sp, color = accentColor.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Rest timer hint
            if (completed > 0 && !allDone) {
                Spacer(Modifier.width(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = K.Card2,
                ) {
                    Text(
                        text     = "⏱ Rest 60s",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        fontSize = 11.sp,
                        color    = K.Muted,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
