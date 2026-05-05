package com.kaizen.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.kaizen.app.data.*
import com.kaizen.app.ui.*
import com.kaizen.app.ui.theme.*
import com.kaizen.app.ui.screens.MuscleMapCard

@Composable
fun TodayScreen(
    state: KaizenUiState,
    habitsForSlot: List<HabitWithCompletions>,
    scheduledWorkout: WorkoutType?,
    onToggleHabit: (HabitWithCompletions) -> Unit,
    onStartWorkout: (WorkoutType) -> Unit,
    onCoreSetTapped: (exerciseName: String, newCount: Int) -> Unit,
    onRecoveryChange: (String) -> Unit,
    onStrainChange: (String) -> Unit,
    onSuggestedChange: (String) -> Unit,
    onEditHabit: (HabitWithCompletions) -> Unit = {},
    onAddInjury: () -> Unit,
    onResolveInjury: (InjuryLog) -> Unit,
    onAddHabit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = state.today

    LazyColumn(
        modifier            = modifier.fillMaxSize().padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding      = PaddingValues(bottom = 32.dp, top = 2.dp),
    ) {
        // ── 1. Whoop + Workout card ────────────────────────────────────
        item {
            WorkoutCard(
                state             = state,
                scheduledWorkout  = scheduledWorkout,
                onStartWorkout    = onStartWorkout,
                onRecoveryChange  = onRecoveryChange,
                onStrainChange    = onStrainChange,
                onSuggestedChange = onSuggestedChange,
            )
        }


        // ── 1b. Injury status
        item {
            InjuryStatusCard(
                activeInjuries = state.activeInjuries,
                onAddInjury    = onAddInjury,
                onResolve      = onResolveInjury,
                modifier       = Modifier.fillMaxWidth(),
            )
        }

        // ── 2. Muscle map
        item {
            MuscleMapCard(
                workoutType = scheduledWorkout,
                modifier    = Modifier.fillMaxWidth(),
            )
        }

        // ── 3. Daily mobility
        item {
            MobilitySection()
        }

        // ── 4. Claude Coach card ──────────────────────────────────────
        item {
            ClaudeCoachCard(
                state           = state,
                workoutType     = scheduledWorkout,
                habitsDoneToday = state.habits.count { it.isCompletedOn(today) },
                modifier        = Modifier.fillMaxWidth(),
            )
        }

        // ── 5. Habit progress ring ────────────────────────────────────
        item {
            val done  = state.habits.count { it.isCompletedOn(today) }
            val total = state.habits.size
            val pct   = if (total > 0) done / total.toFloat() else 0f
            val accent = if (state.selectedSlot == TimeSlot.MORNING) K.Morning else K.Night

            Surface(
                shape  = RoundedCornerShape(16.dp),
                color  = K.Card,
                border = BorderStroke(1.dp, K.Border),
            ) {
                Row(
                    modifier              = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(Modifier.size(68.dp), contentAlignment = Alignment.Center) {
                        ProgressRing(pct, accent)
                        Text("${(pct * 100).toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = K.Text)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("$done / $total habits", style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp))
                        Text(
                            when { pct == 1f -> "🎉 All done!"; pct >= 0.6f -> "Keep pushing"; pct > 0f -> "Just getting started"; else -> "Ready to go?" },
                            color = K.Muted, fontSize = 12.sp,
                        )
                    }
                }
            }
        }

        // ── 5. Slot label ─────────────────────────────────────────────
        item {
            Text(
                "${state.selectedSlot.emoji} ${state.selectedSlot.label} Habits".uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 3.sp),
                color = K.GoldDim,
            )
        }

        // ── 6. Habit cards ────────────────────────────────────────────
        if (habitsForSlot.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 30.dp), contentAlignment = Alignment.Center) {
                    Text("No habits for this time slot.\nTap + to add one!", color = K.Muted, textAlign = TextAlign.Center)
                }
            }
        } else {
            items(habitsForSlot, key = { it.habit.id }) { hwc ->
                HabitCard(hwc = hwc, today = today, onToggle = { onToggleHabit(hwc) }, onLongPress = { onEditHabit(hwc) })
            }
        }

        // ── 7. Add habit button ───────────────────────────────────────
        item {
            OutlinedButton(
                onClick  = onAddHabit,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(14.dp),
                border   = BorderStroke(2.dp, K.Border),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = K.Muted),
            ) {
                Text("+ Add Habit", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

// ── Workout + Whoop card ──────────────────────────────────────────────────

@Composable
private fun WorkoutCard(
    state: KaizenUiState,
    scheduledWorkout: WorkoutType?,
    onStartWorkout: (WorkoutType) -> Unit,
    onRecoveryChange: (String) -> Unit,
    onStrainChange: (String) -> Unit,
    onSuggestedChange: (String) -> Unit,
) {
    val workout   = scheduledWorkout
    val wColor    = workout?.color() ?: K.Muted
    val isStarted = state.todayWorkoutLog != null
    val zColor    = state.whoopZone.color()

    Surface(shape = RoundedCornerShape(20.dp), color = K.Card, border = BorderStroke(1.dp, K.Border)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // ── Whoop top row: Recovery + Suggested ──────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                // Recovery %
                Surface(
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(14.dp),
                    color    = if (state.hasWhoopData) zColor.copy(0.12f) else K.Card2,
                    border   = BorderStroke(1.dp, if (state.hasWhoopData) zColor.copy(0.5f) else K.Border),
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("RECOVERY %", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.5.sp), color = K.Muted)
                        Spacer(Modifier.height(4.dp))
                        WhoopInput(
                            value       = state.whoopRecoveryInput,
                            onChange    = onRecoveryChange,
                            placeholder = "--",
                            color       = if (state.hasWhoopData) zColor else K.Muted.copy(0.4f),
                            keyboardType = KeyboardType.Number,
                        )
                        if (state.hasWhoopData) {
                            Text(state.whoopZone.label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = zColor)
                        } else {
                            Text("0–100", fontSize = 10.sp, color = K.Muted)
                        }
                    }
                }

                // Suggested strain
                Surface(
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(14.dp),
                    color    = K.Card2,
                    border   = BorderStroke(1.dp, K.Gold.copy(0.4f)),
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("SUGGESTED", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.5.sp), color = K.GoldDim)
                        Spacer(Modifier.height(4.dp))
                        WhoopInput(
                            value        = state.whoopSuggestedStrainInput,
                            onChange     = onSuggestedChange,
                            placeholder  = "--",
                            color        = K.Gold,
                            keyboardType = KeyboardType.Decimal,
                        )
                        Text("strain / 21", fontSize = 10.sp, color = K.Muted)
                    }
                }
            }

            // ── Actual strain + comparison badge ──────────────────────
            val strainNum    = state.whoopStrainInput.toFloatOrNull()
            val suggestedNum = state.whoopSuggestedStrainInput.toFloatOrNull()
            val overTarget   = strainNum != null && suggestedNum != null && strainNum > suggestedNum + 0.5f
            val underTarget  = strainNum != null && suggestedNum != null && strainNum < suggestedNum - 0.5f
            val borderColor  = when { overTarget -> Color(0xFFF87171).copy(0.5f); underTarget -> Color(0xFF60A5FA).copy(0.5f); else -> K.Border }

            Surface(shape = RoundedCornerShape(14.dp), color = K.Card2, border = BorderStroke(1.dp, borderColor)) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TODAY'S STRAIN", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.5.sp), color = K.Muted)
                        Spacer(Modifier.height(4.dp))
                        WhoopInput(
                            value        = state.whoopStrainInput,
                            onChange     = onStrainChange,
                            placeholder  = "log after workout",
                            color        = K.Streak,
                            keyboardType = KeyboardType.Decimal,
                            fontSize     = 22.sp,
                        )
                        if (strainNum != null) {
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(0.06f))) {
                                Box(Modifier.fillMaxWidth((strainNum / 21f).coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(K.Streak))
                            }
                        }
                    }
                    if (strainNum != null && suggestedNum != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when { overTarget -> Color(0xFFF87171).copy(0.15f); underTarget -> Color(0xFF60A5FA).copy(0.15f); else -> K.Health.copy(0.15f) },
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(when { overTarget -> "🔥"; underTarget -> "🎯"; else -> "✅" }, fontSize = 20.sp)
                                Text(when { overTarget -> "OVER"; underTarget -> "UNDER"; else -> "ON TARGET" }, fontSize = 8.sp, fontWeight = FontWeight.Bold,
                                    color = when { overTarget -> Color(0xFFF87171); underTarget -> Color(0xFF60A5FA); else -> K.Health })
                            }
                        }
                    }
                }
            }

            // ── Workout info + start button ───────────────────────────
            if (workout != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(shape = RoundedCornerShape(12.dp), color = wColor.copy(0.15f), modifier = Modifier.size(48.dp)) {
                        Box(contentAlignment = Alignment.Center) { Text(workout.emoji, fontSize = 24.sp) }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${workout.label} Day", style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp))
                        Text(workout.muscles, color = K.Muted, fontSize = 11.sp)
                    }
                }
                Button(
                    onClick  = { onStartWorkout(workout) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = if (isStarted) Color.Transparent else wColor,
                        contentColor   = if (isStarted) wColor else Color.Black,
                    ),
                    border = if (isStarted) BorderStroke(2.dp, wColor) else null,
                ) {
                    Text(
                        if (isStarted) "▶ IN PROGRESS — RESUME" else "START WORKOUT →",
                        fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp,
                    )
                }
            } else {
                Surface(shape = RoundedCornerShape(12.dp), color = K.Card2) {
                    Box(modifier = Modifier.fillMaxWidth().padding(14.dp), contentAlignment = Alignment.Center) {
                        Text("Rest & recover today 🛌", color = K.Muted, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ── Reusable Whoop number input ────────────────────────────────────────────

@Composable
private fun WhoopInput(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    color: Color,
    keyboardType: KeyboardType,
    fontSize: TextUnit = 28.sp,
) {
    BasicTextField(
        value           = value,
        onValueChange   = onChange,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine      = true,
        textStyle       = TextStyle(fontSize = fontSize, fontWeight = FontWeight.Bold, color = color, fontFamily = FontFamily.Monospace),
        decorationBox  = { innerTextField ->
            if (value.isEmpty()) {
                Text(placeholder, fontSize = fontSize, fontWeight = FontWeight.Bold, color = K.Muted.copy(0.35f), fontFamily = FontFamily.Monospace)
            } else {
                innerTextField()
            }
        },
    )
}

// ── Habit card ────────────────────────────────────────────────────────────

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun HabitCard(hwc: HabitWithCompletions, today: String, onToggle: () -> Unit, onLongPress: () -> Unit = {}) {
    val done     = hwc.isCompletedOn(today)
    val catColor = hwc.habit.category.color()
    val bgColor by animateColorAsState(targetValue = if (done) catColor.copy(0.12f) else K.Card, animationSpec = tween(300), label = "bgColor")
    val border by animateColorAsState(targetValue = if (done) catColor.copy(0.45f) else K.Border, animationSpec = tween(300), label = "border")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .combinedClickable(onClick = {}, onLongClick = { onLongPress() })
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (done) catColor else Color.Transparent)
                .border(2.dp, if (done) catColor else K.Border, CircleShape)
                .clickable { onToggle() },
            contentAlignment = Alignment.Center,
        ) {
            if (done) Text("✓", fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                hwc.habit.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    color          = if (done) K.Muted else K.Text,
                    textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None,
                    fontSize       = 14.sp,
                ),
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Surface(shape = RoundedCornerShape(100.dp), color = catColor.copy(0.15f), modifier = Modifier.padding(top = 4.dp)) {
                Text("${hwc.habit.category.emoji} ${hwc.habit.category.label}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = catColor)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${hwc.habit.streak}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = K.Streak)
            Text("STREAK", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp))
        }
    }
}

// ── Progress ring ─────────────────────────────────────────────────────────

@Composable
fun ProgressRing(progress: Float, color: Color, size: Int = 68, strokeDp: Float = 6f) {
    val animated by animateFloatAsState(progress, tween(600))
    Canvas(Modifier.size(size.dp)) {
        val stroke = Stroke(strokeDp.dp.toPx(), cap = StrokeCap.Round)
        drawArc(Color.White.copy(0.06f), -90f, 360f, false, style = stroke)
        drawArc(color, -90f, 360f * animated, false, style = stroke)
    }
}
