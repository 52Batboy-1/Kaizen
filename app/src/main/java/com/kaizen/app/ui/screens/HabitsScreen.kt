package com.kaizen.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.kaizen.app.data.*
import com.kaizen.app.ui.*
import com.kaizen.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    state: KaizenUiState,
    onDelete: (HabitWithCompletions) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = state.today

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 4.dp),
    ) {
        // Header row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${state.habits.size} habit${if (state.habits.size != 1) "s" else ""} tracked",
                    color = K.Muted,
                    fontSize = 13.sp,
                )
                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = K.Gold,
                        contentColor   = Color(0xFF1A0E00),
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                ) {
                    Text("+ New", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        // Category breakdown
        item {
            HabitCategory.values().forEach { cat ->
                val count = state.habits.count { it.habit.category == cat }
                if (count > 0) {
                    val done = state.habits.count {
                        it.habit.category == cat && it.isCompletedOn(today)
                    }
                    CategorySummaryChip(cat, done, count)
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        // Habit cards — swipe to delete
        items(state.habits, key = { it.habit.id }) { hwc ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value == SwipeToDismissBoxValue.EndToStart) {
                        onDelete(hwc)
                        true
                    } else false
                }
            )
            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = false,
                backgroundContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 12.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.2f),
                        ) {
                            Text(
                                "🗑 Delete",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444),
                            )
                        }
                    }
                },
            ) {
                AllHabitRow(hwc = hwc, today = today)
            }
        }

        // Empty state
        if (state.habits.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌱", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No habits yet — tap + New to start",
                            color = K.Muted,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}

// ── Habit row card ────────────────────────────────────────────────────────

@Composable
private fun AllHabitRow(hwc: HabitWithCompletions, today: String) {
    val cat      = hwc.habit.category
    val catColor = cat.color()
    val slotIcon = hwc.habit.timeSlot.emoji
    val doneToday = hwc.isCompletedOn(today)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = K.Card,
        border = BorderStroke(1.dp, if (doneToday) catColor.copy(alpha = 0.35f) else K.Border),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Category icon
            Surface(
                shape = CircleShape,
                color = catColor.copy(alpha = 0.15f),
                modifier = Modifier.size(42.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(cat.emoji, fontSize = 20.sp)
                }
            }

            // Name + meta
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = hwc.habit.name,
                    style    = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = catColor.copy(alpha = 0.12f),
                    ) {
                        Text(
                            cat.label,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = catColor,
                        )
                    }
                    Text(
                        "$slotIcon ${hwc.habit.timeSlot.label}",
                        fontSize = 10.sp,
                        color = K.Muted,
                    )
                }
            }

            // Streak + today status
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${hwc.habit.streak}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = K.Streak,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                )
                Text(
                    if (doneToday) "✓ done" else "STREAK",
                    fontSize = 8.sp,
                    color = if (doneToday) K.Health else K.Muted,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }
}

// ── Category summary chip ─────────────────────────────────────────────────

@Composable
private fun CategorySummaryChip(cat: HabitCategory, done: Int, total: Int) {
    val color = cat.color()
    val pct   = if (total > 0) done / total.toFloat() else 0f
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(cat.emoji, fontSize = 16.sp)
            Text(cat.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = K.Text, modifier = Modifier.weight(1f))
            Text("$done/$total", fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}
