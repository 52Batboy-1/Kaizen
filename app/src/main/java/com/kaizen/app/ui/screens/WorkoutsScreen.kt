package com.kaizen.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.kaizen.app.data.*
import com.kaizen.app.ui.*
import com.kaizen.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.background

private enum class WTab { HISTORY, RECORDS }

@Composable
fun WorkoutsScreen(
    state: KaizenUiState,
    onStartWorkout: (WorkoutType) -> Unit,
    onSetTier: (KaizenTier) -> Unit,
    modifier: Modifier = Modifier,
) {
    var activeTab by remember { mutableStateOf(WTab.HISTORY) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WTab.values().forEach { tab ->
                val sel = tab == activeTab
                Surface(modifier = Modifier.weight(1f).clickable { activeTab = tab }, shape = RoundedCornerShape(12.dp),
                    color = if (sel) K.Gold.copy(0.15f) else K.Card, border = BorderStroke(1.dp, if (sel) K.Gold.copy(0.5f) else K.Border)) {
                    Text(tab.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(vertical = 10.dp), textAlign = TextAlign.Center,
                        fontSize = 13.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                        color = if (sel) K.Gold else K.Muted)
                }
            }
        }
        when (activeTab) {
            WTab.HISTORY -> HistoryTab(state, onStartWorkout, onSetTier)
            WTab.RECORDS -> RecordsTab(state)
        }
    }
}

@Composable
private fun HistoryTab(state: KaizenUiState, onStartWorkout: (WorkoutType) -> Unit, onSetTier: (KaizenTier) -> Unit) {
    val workouts    = state.recentWorkouts.sortedByDescending { it.date }
    val workoutDates = workouts.map { it.date }.toSet()
    val workoutMap   = workouts.associateBy { it.date }
    val today        = LocalDate.now()

    LazyColumn(contentPadding = PaddingValues(horizontal = 22.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

        // ── Tier card ─────────────────────────────────────────────────────
        item { TierCard(state.currentTier, onSetTier) }

        // ── Calendar heatmap ──────────────────────────────────────────────
        item {
            Surface(shape = RoundedCornerShape(20.dp), color = K.Card, border = BorderStroke(1.dp, K.Border)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Activity", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = K.Text)
                        Text("${workouts.size} sessions", fontSize = 10.sp, color = K.Muted)
                    }
                    Spacer(Modifier.height(10.dp))

                    // 10 weeks × 7 days grid
                    val weeks = (9 downTo 0).map { w -> (6 downTo 0).map { d -> today.minusDays((w * 7 + d).toLong()) } }
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        weeks.forEach { week ->
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                week.forEach { date ->
                                    val ds  = date.toString()
                                    val wt  = workoutMap[ds]?.workoutType
                                    val isT = date == today
                                    val isF = date.isAfter(today)
                                    Box(
                                        modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp))
                                            .background(when { isF -> Color.Transparent; wt != null -> wt.color(); isT -> K.Gold.copy(0.3f); else -> Color.White.copy(0.05f) })
                                            .then(if (isT) Modifier.border(1.dp, K.Gold, RoundedCornerShape(2.dp)) else Modifier)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        WorkoutType.values().forEach { type ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(type.color()))
                                Text(type.label, fontSize = 9.sp, color = K.Muted)
                            }
                        }
                    }
                }
            }
        }

        // ── Quick start ───────────────────────────────────────────────────
        item {
            Text("QUICK START", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = K.GoldDim)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkoutType.values().forEach { type ->
                    Surface(modifier = Modifier.weight(1f).clickable { onStartWorkout(type) }, shape = RoundedCornerShape(14.dp),
                        color = type.color().copy(0.12f), border = BorderStroke(1.dp, type.color().copy(0.35f))) {
                        Column(modifier = Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(type.emoji, fontSize = 20.sp)
                            Text(type.label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = type.color())
                        }
                    }
                }
            }
        }

        // ── Session list ──────────────────────────────────────────────────
        item { Text("RECENT SESSIONS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = K.GoldDim) }

        if (workouts.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Text("No workouts yet — start one on the Today tab!", color = K.Muted, textAlign = TextAlign.Center, fontSize = 13.sp)
                }
            }
        } else {
            items(workouts) { log ->
                val wColor = log.workoutType.color()
                val date   = runCatching { LocalDate.parse(log.date).format(DateTimeFormatter.ofPattern("EEE, MMM d")) }.getOrElse { log.date }
                Surface(shape = RoundedCornerShape(16.dp), color = K.Card, border = BorderStroke(1.dp, K.Border)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(shape = RoundedCornerShape(10.dp), color = wColor.copy(0.15f), modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text(log.workoutType.emoji, fontSize = 22.sp) }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${log.workoutType.label} Day", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = K.Text)
                            Text(date, fontSize = 11.sp, color = K.Muted)
                            if (log.whoopRecovery != null) Text("Recovery ${log.whoopRecovery}%", fontSize = 10.sp, color = K.Gold)
                        }
                        if (log.strainScore != null) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("%.1f".format(log.strainScore), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = K.Streak, fontFamily = FontFamily.Monospace)
                                Text("strain", fontSize = 9.sp, color = K.Muted)
                            }
                        } else {
                            Text("✓", fontSize = 22.sp, color = wColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ── Tier progression card ─────────────────────────────────────────────────

@Composable
private fun TierCard(currentTier: KaizenTier, onSetTier: (KaizenTier) -> Unit) {
    val tiers = KaizenTier.values()
    val idx   = tiers.indexOf(currentTier)
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape  = RoundedCornerShape(20.dp),
        color  = K.Card,
        border = BorderStroke(1.dp, K.Night.copy(0.4f)),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(10.dp), color = K.Night.copy(0.15f), modifier = Modifier.size(42.dp)) {
                        Box(contentAlignment = Alignment.Center) { Text(currentTier.emoji, fontSize = 22.sp) }
                    }
                    Column {
                        Text(currentTier.label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = K.Text)
                        Text(currentTier.weekRange + " · " + currentTier.split, fontSize = 10.sp, color = K.Muted)
                    }
                }
                Text(if (expanded) "▲" else "▼", fontSize = 12.sp, color = K.GoldDim)
            }

            if (expanded) {
                Text(currentTier.description, fontSize = 12.sp, color = K.Muted, lineHeight = 17.sp)

                Text(
                    "GRADUATION STANDARDS",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.sp),
                    color = K.GoldDim,
                )
                currentTier.graduationStandards.forEach { standard ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(K.Night.copy(0.6f)))
                        Text(standard, fontSize = 12.sp, color = K.Text)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (idx > 0) {
                        OutlinedButton(
                            onClick  = { onSetTier(tiers[idx - 1]) },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(10.dp),
                            border   = BorderStroke(1.dp, K.Border),
                        ) {
                            Text("← ${tiers[idx - 1].label}", fontSize = 12.sp, color = K.Muted)
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    if (idx < tiers.lastIndex) {
                        Button(
                            onClick = { onSetTier(tiers[idx + 1]) },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(10.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = K.Night.copy(0.25f), contentColor = K.Night),
                        ) {
                            Text("${tiers[idx + 1].label} →", fontSize = 12.sp)
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordsTab(state: KaizenUiState) {
    val prs = state.personalRecords
    LazyColumn(contentPadding = PaddingValues(horizontal = 22.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (prs.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🏆", fontSize = 40.sp)
                        Text("No PRs yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = K.Text)
                        Text("Log a PR from the active workout sheet.", fontSize = 12.sp, color = K.Muted, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            WorkoutType.values().forEach { type ->
                val typePRs = prs.filter { it.workoutType == type }
                if (typePRs.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 8.dp)) {
                            Text(type.emoji, fontSize = 14.sp)
                            Text(type.label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = type.color())
                        }
                    }
                    items(typePRs) { pr ->
                        val wColor = pr.workoutType.color()
                        val date   = runCatching { LocalDate.parse(pr.date).format(DateTimeFormatter.ofPattern("MMM d")) }.getOrElse { pr.date }
                        Surface(shape = RoundedCornerShape(14.dp), color = K.Card, border = BorderStroke(1.dp, wColor.copy(0.25f))) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Surface(shape = RoundedCornerShape(8.dp), color = wColor.copy(0.15f), modifier = Modifier.size(36.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Text("🏆", fontSize = 16.sp) }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pr.exerciseName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = K.Text)
                                    Text("${"${pr.difficulty.name.take(1)}"} ${pr.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }}", fontSize = 10.sp, color = K.Muted)
                                    if (pr.notes.isNotBlank()) Text(pr.notes, fontSize = 10.sp, color = K.Muted)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(pr.repsOrDuration, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = wColor, fontFamily = FontFamily.Monospace)
                                    Text(date, fontSize = 9.sp, color = K.Muted)
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}
