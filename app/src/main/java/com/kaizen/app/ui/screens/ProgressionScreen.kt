package com.kaizen.app.ui.screens

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
import com.kaizen.app.data.*
import com.kaizen.app.ui.KaizenUiState
import com.kaizen.app.ui.theme.*

@Composable
fun ProgressionScreen(
    state: KaizenUiState,
    currentWeek: Int,
    onWeekChange: (Int) -> Unit,
    checkedStandards: Set<String>,
    onToggleStandard: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tier = tierForWeek(currentWeek)

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(2.dp))

        // ── Tier badge ────────────────────────────────────────────────────
        Surface(
            shape  = RoundedCornerShape(20.dp),
            color  = K.Gold.copy(0.10f),
            border = BorderStroke(1.dp, K.Gold.copy(0.35f)),
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(tier.emoji, fontSize = 40.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(tier.label, style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp), color = K.Gold)
                    Text(tier.weekRange, fontSize = 11.sp, color = K.GoldDim)
                    Text(tier.split, fontSize = 11.sp, color = K.Muted)
                }
            }
        }

        Text(tier.description, fontSize = 13.sp, color = K.Muted, lineHeight = 19.sp)

        // ── Week selector ─────────────────────────────────────────────────
        Surface(shape = RoundedCornerShape(16.dp), color = K.Card, border = BorderStroke(1.dp, K.Border)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Week $currentWeek", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = K.Text)
                    Text("of 24", fontSize = 12.sp, color = K.Muted)
                }
                Slider(
                    value         = currentWeek.toFloat(),
                    onValueChange = { onWeekChange(it.toInt()) },
                    valueRange    = 1f..24f,
                    steps         = 22,
                    colors        = SliderDefaults.colors(thumbColor = K.Gold, activeTrackColor = K.Gold, inactiveTrackColor = K.Border),
                )
                // Tier markers
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    KaizenTier.values().forEach { t ->
                        Text(
                            "${t.emoji} ${t.weekRange.take(7)}",
                            fontSize = 9.sp,
                            color    = if (tier == t) K.Gold else K.Muted,
                            fontWeight = if (tier == t) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }

        // ── Graduation standards ──────────────────────────────────────────
        Text("GRADUATION STANDARDS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = K.GoldDim)
        Text("Tick each standard when you can hit it consistently:", fontSize = 11.sp, color = K.Muted)

        val allChecked = tier.graduationStandards.all { it in checkedStandards }
        tier.graduationStandards.forEach { standard ->
            val checked = standard in checkedStandards
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onToggleStandard(standard) },
                shape    = RoundedCornerShape(12.dp),
                color    = if (checked) K.Health.copy(0.10f) else K.Card,
                border   = BorderStroke(1.dp, if (checked) K.Health.copy(0.4f) else K.Border),
            ) {
                Row(
                    modifier              = Modifier.padding(12.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier         = Modifier.size(24.dp).clip(RoundedCornerShape(6.dp))
                            .background(if (checked) K.Health else K.Card2)
                            .border(1.dp, if (checked) K.Health else K.Border, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (checked) Text("✓", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Text(standard, fontSize = 13.sp, color = if (checked) K.Muted else K.Text, lineHeight = 17.sp, modifier = Modifier.weight(1f))
                }
            }
        }

        // ── Graduate button ───────────────────────────────────────────────
        if (allChecked && tier != KaizenTier.MASTERY) {
            Surface(shape = RoundedCornerShape(16.dp), color = K.Health.copy(0.12f), border = BorderStroke(1.dp, K.Health.copy(0.4f))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉 All standards met!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = K.Health)
                    Text("You're ready to progress to the next tier. Move your week slider forward.", fontSize = 12.sp, color = K.Muted)
                }
            }
        }

        if (tier == KaizenTier.MASTERY && allChecked) {
            Surface(shape = RoundedCornerShape(16.dp), color = K.Gold.copy(0.12f), border = BorderStroke(1.dp, K.Gold.copy(0.4f))) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏆 Mastery achieved!", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = K.Gold)
                    Text("You've completed the full 24-week Kaizen program.", fontSize = 12.sp, color = K.Muted)
                }
            }
        }

        // ── Next tier preview ─────────────────────────────────────────────
        val nextTier = KaizenTier.values().getOrNull(KaizenTier.values().indexOf(tier) + 1)
        if (nextTier != null) {
            Text("NEXT TIER", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = K.GoldDim)
            Surface(shape = RoundedCornerShape(14.dp), color = K.Card, border = BorderStroke(1.dp, K.Border)) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(nextTier.emoji, fontSize = 28.sp)
                    Column {
                        Text(nextTier.label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = K.Muted)
                        Text(nextTier.split, fontSize = 11.sp, color = K.Muted.copy(0.6f))
                    }
                }
            }
        }

        // ── Stats summary ─────────────────────────────────────────────────
        Text("YOUR NUMBERS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = K.GoldDim)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(Modifier.weight(1f), RoundedCornerShape(14.dp), K.Card, border = BorderStroke(1.dp, K.Border)) {
                Column(Modifier.padding(12.dp)) {
                    Text("${state.recentWorkouts.size}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = K.Gold, fontFamily = FontFamily.Monospace)
                    Text("Sessions", fontSize = 10.sp, color = K.Muted)
                }
            }
            Surface(Modifier.weight(1f), RoundedCornerShape(14.dp), K.Card, border = BorderStroke(1.dp, K.Border)) {
                Column(Modifier.padding(12.dp)) {
                    Text("${state.personalRecords.size}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = K.Streak, fontFamily = FontFamily.Monospace)
                    Text("PRs logged", fontSize = 10.sp, color = K.Muted)
                }
            }
            Surface(Modifier.weight(1f), RoundedCornerShape(14.dp), K.Card, border = BorderStroke(1.dp, K.Border)) {
                Column(Modifier.padding(12.dp)) {
                    Text("${state.habits.maxOfOrNull { it.habit.streak } ?: 0}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = K.Health, fontFamily = FontFamily.Monospace)
                    Text("Best streak", fontSize = 10.sp, color = K.Muted)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// Helper to get tier from week number
fun KaizenTier.forWeek(week: Int): KaizenTier = when {
    week <= 6  -> KaizenTier.FOUNDATION
    week <= 12 -> KaizenTier.DEVELOPMENT
    week <= 18 -> KaizenTier.STRENGTH
    else       -> KaizenTier.MASTERY
}
