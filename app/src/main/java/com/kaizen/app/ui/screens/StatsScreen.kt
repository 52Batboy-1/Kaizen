package com.kaizen.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.kaizen.app.data.*
import com.kaizen.app.ui.KaizenUiState
import com.kaizen.app.ui.WeekDay
import com.kaizen.app.ui.theme.*

@Composable
fun StatsScreen(
    state: KaizenUiState,
    weekDays: List<WeekDay>,
    onLogBodyweight: (Float, String) -> Unit = { _, _ -> },
    onLogSleep: (Float, Int?, Int) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier,
) {
    val today  = state.today
    val habits = state.habits
    val top3   = remember(habits) { habits.sortedByDescending { it.habit.streak }.take(3) }

    var weightInput  by remember { mutableStateOf("") }
    var sleepInput   by remember { mutableStateOf("") }
    var hrvInput     by remember { mutableStateOf("") }
    var sleepQuality by remember { mutableIntStateOf(3) }

    val latestBW    = state.bodyweightEntries.firstOrNull()
    val latestSleep = state.sleepEntries.firstOrNull()

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(2.dp))

        // ── Log today card ────────────────────────────────────────────────
        Surface(shape = RoundedCornerShape(20.dp), color = K.Card, border = BorderStroke(1.dp, K.Gold.copy(0.25f))) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Log Today", style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp), color = K.Gold)

                // Bodyweight row
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), color = K.Card2, border = BorderStroke(1.dp, Color(0xFF60A5FA).copy(0.4f))) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("BODYWEIGHT", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.5.sp), color = Color(0xFF60A5FA).copy(0.7f))
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                BasicTextField(
                                    value = weightInput, onValueChange = { weightInput = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA), fontFamily = FontFamily.Monospace),
                                    decorationBox = { inner ->
                                        if (weightInput.isEmpty()) Text(latestBW?.let { "%.1f".format(it.weightKg) } ?: "--", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = K.Muted.copy(0.4f), fontFamily = FontFamily.Monospace)
                                        else inner()
                                    }
                                )
                                Text("kg", fontSize = 12.sp, color = K.Muted, modifier = Modifier.padding(bottom = 4.dp))
                            }
                            if (latestBW != null) Text("Last: %.1f kg".format(latestBW.weightKg), fontSize = 9.sp, color = K.Muted)
                        }
                    }
                    Button(
                        onClick = { weightInput.toFloatOrNull()?.let { onLogBodyweight(it, ""); weightInput = "" } },
                        enabled = weightInput.toFloatOrNull() != null,
                        shape   = RoundedCornerShape(12.dp),
                        colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF60A5FA), contentColor = Color.Black),
                        modifier = Modifier.height(54.dp),
                    ) { Text("Log", fontWeight = FontWeight.Bold) }
                }

                // Sleep + HRV row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), color = K.Card2, border = BorderStroke(1.dp, K.Night.copy(0.4f))) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("SLEEP HRS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.5.sp), color = K.Night.copy(0.7f))
                            Spacer(Modifier.height(4.dp))
                            BasicTextField(
                                value = sleepInput, onValueChange = { sleepInput = it.filter { c -> c.isDigit() || c == '.' }.take(4) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = K.Night, fontFamily = FontFamily.Monospace),
                                decorationBox = { inner ->
                                    if (sleepInput.isEmpty()) Text(latestSleep?.let { "%.1f".format(it.hoursSlept) } ?: "--", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = K.Muted.copy(0.4f), fontFamily = FontFamily.Monospace)
                                    else inner()
                                }
                            )
                            if (latestSleep != null) Text("Last: %.1f hrs".format(latestSleep.hoursSlept), fontSize = 9.sp, color = K.Muted)
                        }
                    }
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), color = K.Card2, border = BorderStroke(1.dp, Color(0xFFC084FC).copy(0.4f))) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("HRV (ms)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.5.sp), color = Color(0xFFC084FC).copy(0.7f))
                            Spacer(Modifier.height(4.dp))
                            BasicTextField(
                                value = hrvInput, onValueChange = { hrvInput = it.filter { c -> c.isDigit() }.take(3) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC084FC), fontFamily = FontFamily.Monospace),
                                decorationBox = { inner ->
                                    if (hrvInput.isEmpty()) Text(latestSleep?.hrv?.toString() ?: "--", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = K.Muted.copy(0.4f), fontFamily = FontFamily.Monospace)
                                    else inner()
                                }
                            )
                            if (latestSleep?.hrv != null) Text("Last: ${latestSleep.hrv} ms", fontSize = 9.sp, color = K.Muted)
                        }
                    }
                }

                // Sleep quality + log button
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Quality:", fontSize = 11.sp, color = K.Muted)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { star ->
                            Text(
                                if (star <= sleepQuality) "★" else "☆",
                                fontSize = 22.sp,
                                color    = if (star <= sleepQuality) K.Gold else K.Muted,
                                modifier = Modifier.clickable { sleepQuality = star },
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = {
                            sleepInput.toFloatOrNull()?.let {
                                onLogSleep(it, hrvInput.toIntOrNull(), sleepQuality)
                                sleepInput = ""; hrvInput = ""
                            }
                        },
                        enabled = sleepInput.toFloatOrNull() != null,
                        shape   = RoundedCornerShape(12.dp),
                        colors  = ButtonDefaults.buttonColors(containerColor = K.Night, contentColor = Color.White),
                    ) { Text("Log Sleep", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                }
            }
        }

        // ── Bodyweight graph ──────────────────────────────────────────────
        if (state.bodyweightEntries.size >= 2) {
            SectionLabel("BODYWEIGHT TREND")
            BodyweightGraph(entries = state.bodyweightEntries.take(30).reversed())
        }

        // ── Sleep graph ───────────────────────────────────────────────────
        if (state.sleepEntries.size >= 2) {
            SectionLabel("SLEEP & HRV")
            SleepGraph(entries = state.sleepEntries.take(14).reversed())
        }

        // ── Whoop summary ─────────────────────────────────────────────────
        if (state.hasWhoopData) {
            val zColor = state.whoopZone.color()
            Surface(shape = RoundedCornerShape(16.dp), color = zColor.copy(0.10f), border = BorderStroke(1.dp, zColor.copy(0.35f))) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(36.dp).clip(RoundedCornerShape(100.dp)).background(zColor.copy(0.2f)), Alignment.Center) {
                        Box(Modifier.size(14.dp).clip(RoundedCornerShape(100.dp)).background(zColor))
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Whoop · ${state.whoopZone.label}", style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp))
                        Text(buildString {
                            append("Recovery ${state.whoopRecovery}%")
                            state.whoopSuggestedStrain?.let { append(" · Target ${"%.1f".format(it)}") }
                            state.whoopStrain?.let { append(" · Strain ${"%.1f".format(it)}") }
                        }, color = K.Muted, fontSize = 11.sp)
                    }
                }
            }
        }

        SectionLabel("7-DAY OVERVIEW")
        WeeklyChart(weekDays)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(Modifier.weight(1f), "Habits Today", "${habits.count { it.isCompletedOn(today) }}/${habits.size}", K.Morning)
            StatTile(Modifier.weight(1f), "Workouts", "${state.recentWorkouts.size}", K.Gold)
            StatTile(Modifier.weight(1f), "Best Streak", "${habits.maxOfOrNull { it.habit.streak } ?: 0}d", K.Streak)
        }

        SectionLabel("🔥 STREAK LEADERS")
        if (top3.isEmpty()) {
            Text("Complete habits to build streaks!", color = K.Muted, fontSize = 13.sp)
        } else {
            top3.forEachIndexed { i, hwc ->
                Surface(shape = RoundedCornerShape(14.dp), color = K.Card, border = BorderStroke(1.dp, K.Border)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(listOf("🥇","🥈","🥉").getOrElse(i){"🏅"}, fontSize = 22.sp)
                        Column(Modifier.weight(1f)) {
                            Text(hwc.habit.name, style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp))
                            Text("${hwc.habit.category.emoji} ${hwc.habit.category.label}", fontSize = 10.sp, color = hwc.habit.category.color())
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${hwc.habit.streak}", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = K.Streak, fontFamily = FontFamily.Monospace)
                            Text("DAYS", fontSize = 8.sp, color = K.Muted)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }

        SectionLabel("BY CATEGORY")
        HabitCategory.values().forEach { cat ->
            val hs = habits.filter { it.habit.category == cat }
            if (hs.isEmpty()) return@forEach
            val pct by animateFloatAsState(if (hs.isNotEmpty()) hs.count { it.isCompletedOn(today) } / hs.size.toFloat() else 0f, tween(500))
            val color = cat.color()
            Surface(shape = RoundedCornerShape(12.dp), color = K.Card, border = BorderStroke(1.dp, K.Border)) {
                Column(Modifier.padding(horizontal = 13.dp, vertical = 10.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("${cat.emoji} ${cat.label} (${hs.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = K.Text)
                        Text("${(pct * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(0.06f))) {
                        Box(Modifier.fillMaxWidth(pct).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(color))
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun BodyweightGraph(entries: List<BodyweightEntry>) {
    if (entries.isEmpty()) return
    val minW  = entries.minOf { it.weightKg }
    val maxW  = entries.maxOf { it.weightKg }
    val range = (maxW - minW).coerceAtLeast(2f)

    Surface(shape = RoundedCornerShape(16.dp), color = K.Card, border = BorderStroke(1.dp, K.Border)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("%.1f kg".format(entries.last().weightKg), fontSize = 11.sp, color = K.Muted)
                Text("%.1f kg now".format(entries.first().weightKg), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
            }
            Spacer(Modifier.height(8.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                val step = size.width / (entries.size - 1).coerceAtLeast(1)
                val pts  = entries.mapIndexed { i, e ->
                    Offset(i * step, size.height - ((e.weightKg - minW) / range) * size.height * 0.85f)
                }
                for (i in 0 until pts.size - 1) {
                    drawLine(Color(0xFF60A5FA), pts[i], pts[i + 1], strokeWidth = 2.dp.toPx())
                }
                pts.forEach { pt -> drawCircle(Color(0xFF60A5FA), 4.dp.toPx(), pt) }
            }
        }
    }
}

@Composable
private fun SleepGraph(entries: List<SleepEntry>) {
    if (entries.isEmpty()) return
    Surface(shape = RoundedCornerShape(16.dp), color = K.Card, border = BorderStroke(1.dp, K.Border)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Avg: ${"%.1f".format(entries.map { it.hoursSlept }.average())} hrs", fontSize = 11.sp, color = K.Muted)
                entries.mapNotNull { it.hrv }.average().let { if (!it.isNaN()) Text("Avg HRV: ${it.toInt()} ms", fontSize = 11.sp, color = Color(0xFFC084FC)) }
            }
            Spacer(Modifier.height(8.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(70.dp)) {
                val step  = size.width / entries.size
                val maxH  = 10f
                entries.forEachIndexed { i, e ->
                    val barH = (e.hoursSlept / maxH) * size.height * 0.9f
                    val x    = i * step + step * 0.1f
                    drawRoundRect(
                        color        = K.Night.copy(if (e.hoursSlept >= 7f) 0.9f else 0.5f),
                        topLeft      = Offset(x, size.height - barH),
                        size         = Size(step * 0.7f, barH),
                        cornerRadius = CornerRadius(3f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) = Text(text, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.5.sp), color = K.GoldDim)

@Composable
private fun StatTile(modifier: Modifier, label: String, value: String, color: Color) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = K.Card, border = BorderStroke(1.dp, K.Border)) {
        Column(Modifier.padding(12.dp)) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color, fontFamily = FontFamily.Monospace)
            Text(label, fontSize = 10.sp, color = K.Muted, lineHeight = 13.sp)
        }
    }
}

@Composable
private fun WeeklyChart(weekDays: List<WeekDay>) {
    Surface(shape = RoundedCornerShape(20.dp), color = K.Card, border = BorderStroke(1.dp, K.Border)) {
        Column(Modifier.padding(16.dp)) {
            Canvas(Modifier.fillMaxWidth().height(110.dp)) {
                val groupW = size.width / weekDays.size; val barW = groupW / 2.5f
                weekDays.forEachIndexed { i, day ->
                    val gx = i * groupW; val hH = (day.habitPct * size.height).coerceAtLeast(4f)
                    drawRoundRect(if (day.isToday) K.Morning else K.Morning.copy(0.35f), Offset(gx + groupW * 0.05f, size.height - hH), Size(barW, hH), CornerRadius(4f))
                    val wH = if (day.hadWorkout) size.height * 0.9f else 4f
                    drawRoundRect(if (day.isToday) K.Gold else K.Gold.copy(0.35f), Offset(gx + groupW * 0.05f + barW + groupW * 0.05f, size.height - wH), Size(barW, wH), CornerRadius(4f))
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                weekDays.forEach { day ->
                    Text(day.label, fontSize = 10.sp, color = if (day.isToday) K.Gold else K.Muted, fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}
