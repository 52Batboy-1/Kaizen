package com.kaizen.app.ui.screens

import com.kaizen.app.data.*

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.kaizen.app.data.KaizenTier
import com.kaizen.app.ui.theme.*

@Composable
fun OnboardingScreen(
    onComplete: (name: String, week: Int) -> Unit,
) {
    var page    by remember { mutableIntStateOf(0) }
    var name    by remember { mutableStateOf("") }
    var week    by remember { mutableIntStateOf(1) }

    Box(modifier = Modifier.fillMaxSize().background(K.Bg), contentAlignment = Alignment.Center) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Page dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (0..2).forEach { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == page) 20.dp else 8.dp, 8.dp)
                            .background(if (i == page) K.Gold else K.Border, RoundedCornerShape(4.dp))
                    )
                }
            }

            AnimatedContent(targetState = page, transitionSpec = { slideInHorizontally { it } togetherWith slideOutHorizontally { -it } }) { p ->
                when (p) {
                    0 -> OnboardPage0()
                    1 -> OnboardPage1(name = name, onNameChange = { name = it })
                    2 -> OnboardPage2(week = week, onWeekChange = { week = it })
                    else -> {}
                }
            }

            // Navigation
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (page > 0) {
                    OutlinedButton(
                        onClick = { page-- },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        border   = BorderStroke(1.dp, K.Border),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = K.Muted),
                    ) { Text("Back") }
                }
                Button(
                    onClick = { if (page < 2) page++ else onComplete(name.trim().ifBlank { "Jordan" }, week) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = K.Gold, contentColor = Color(0xFF1A0E00)),
                ) {
                    Text(
                        if (page < 2) "Next →" else "Let's go →",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 16.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardPage0() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("K", fontSize = 72.sp, fontWeight = FontWeight.ExtraBold, color = K.Gold)
        Text("改善", fontSize = 24.sp, color = K.GoldDim)
        Text("Kaizen", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = K.Text)
        Text(
            "Continuous improvement through bodyweight training, habit tracking, and Whoop-powered recovery.",
            fontSize   = 14.sp,
            color      = K.Muted,
            textAlign  = TextAlign.Center,
            lineHeight = 21.sp,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            listOf("🏋️ Workouts", "📊 Habits", "💪 PRs", "🧘 Mobility").forEach { item ->
                Text(item, fontSize = 12.sp, color = K.Gold)
            }
        }
    }
}

@Composable
private fun OnboardPage1(name: String, onNameChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("👋", fontSize = 48.sp)
        Text("What's your name?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = K.Text)
        Text("Just for personalising your coaching.", fontSize = 13.sp, color = K.Muted)
        Surface(shape = RoundedCornerShape(14.dp), color = K.Card, border = BorderStroke(1.dp, K.Gold.copy(0.4f)), modifier = Modifier.fillMaxWidth()) {
            BasicTextField(
                value = name, onValueChange = onNameChange, singleLine = true,
                textStyle = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = K.Text),
                modifier  = Modifier.padding(16.dp),
                decorationBox = { inner ->
                    if (name.isEmpty()) Text("Your name...", fontSize = 22.sp, color = K.Muted.copy(0.5f))
                    else inner()
                }
            )
        }
    }
}

@Composable
private fun OnboardPage2(week: Int, onWeekChange: (Int) -> Unit) {
    val tier = tierForWeek(week)
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(tier.emoji, fontSize = 48.sp)
        Text("Where are you at?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = K.Text)
        Text("Set your current training week so we scale your workouts correctly.", fontSize = 13.sp, color = K.Muted, textAlign = TextAlign.Center)
        Text("Week $week · ${tier.label}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = K.Gold)
        Text(tier.split, fontSize = 12.sp, color = K.Muted)
        Slider(
            value = week.toFloat(), onValueChange = { onWeekChange(it.toInt()) },
            valueRange = 1f..24f, steps = 22,
            colors = SliderDefaults.colors(thumbColor = K.Gold, activeTrackColor = K.Gold),
        )
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("Week 1\nBeginner", fontSize = 10.sp, color = K.Muted, textAlign = TextAlign.Center)
            Text("Week 24\nMastery", fontSize = 10.sp, color = K.Muted, textAlign = TextAlign.Center)
        }
        Text("You can change this anytime in the Progress tab.", fontSize = 11.sp, color = K.Muted)
    }
}
