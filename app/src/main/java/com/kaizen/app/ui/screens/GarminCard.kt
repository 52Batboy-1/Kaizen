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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.kaizen.app.data.GarminEntry
import com.kaizen.app.ui.theme.*

@Composable
fun GarminCard(
    entry: GarminEntry?,
    isConnected: Boolean,
    isLoading: Boolean,
    onBodyBattery: (Int?) -> Unit,
    onStressScore: (Int?) -> Unit,
    onRefresh: () -> Unit,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "garmin")
    val spin by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label         = "spin",
    )

    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(16.dp),
        color    = K.Card,
        border   = BorderStroke(1.dp, K.Border),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header ────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⌚", fontSize = 16.sp)
                    Text(
                        "GARMIN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize      = 9.sp,
                            letterSpacing = 3.sp,
                            fontWeight    = FontWeight.Bold,
                        ),
                        color = K.GoldDim,
                    )
                    Box(
                        Modifier
                            .size(6.dp)
                            .background(
                                if (isConnected) Color(0xFF4ADE80) else K.Border,
                                RoundedCornerShape(50),
                            )
                    )
                }
                Surface(
                    modifier = Modifier
                        .size(30.dp)
                        .clickable { if (isConnected) onRefresh() else onConnect() },
                    shape  = RoundedCornerShape(8.dp),
                    color  = K.Card2,
                    border = BorderStroke(1.dp, K.Border),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            if (isConnected) "↻" else "⊕",
                            fontSize = 14.sp,
                            color    = K.Muted,
                            modifier = Modifier.graphicsLayer {
                                rotationZ = if (isLoading) spin else 0f
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            if (!isConnected) {
                // ── Not connected ─────────────────────────────────────────
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Connect Health Connect to auto-sync your Garmin data", fontSize = 12.sp, color = K.Muted, textAlign = TextAlign.Center)
                    Surface(
                        modifier = Modifier.clickable { onConnect() },
                        shape    = RoundedCornerShape(10.dp),
                        color    = K.Gold.copy(alpha = 0.12f),
                        border   = BorderStroke(1.dp, K.Gold.copy(alpha = 0.4f)),
                    ) {
                        Text(
                            "Connect Health Connect",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = K.Gold,
                        )
                    }
                }
            } else {
                // ── Auto-fetched metrics row ───────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GarminMetric("👟 Steps",      entry?.steps?.toString() ?: "—", Modifier.weight(1f))
                    GarminMetric("❤️ Resting HR", entry?.restingHr?.let { "$it bpm" } ?: "—", Modifier.weight(1f))
                    GarminMetric("📡 HRV",        entry?.hrv?.let { "${it.toInt()} ms" } ?: "—", Modifier.weight(1f))
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = K.Border, thickness = 0.5.dp)
                Spacer(Modifier.height(14.dp))

                // ── Manual inputs row ─────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    GarminManualInput(
                        label   = "Body Battery",
                        value   = entry?.bodyBattery?.toString() ?: "",
                        hint    = "0–100",
                        accent  = batteryColor(entry?.bodyBattery),
                        onSave  = { onBodyBattery(it.toIntOrNull()?.coerceIn(0, 100)) },
                        modifier = Modifier.weight(1f),
                    )
                    GarminManualInput(
                        label   = "Stress",
                        value   = entry?.stressScore?.toString() ?: "",
                        hint    = "0–100",
                        accent  = stressColor(entry?.stressScore),
                        onSave  = { onStressScore(it.toIntOrNull()?.coerceIn(0, 100)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun GarminMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(10.dp),
        color    = K.Card2,
        border   = BorderStroke(1.dp, K.Border),
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, fontSize = 9.sp, color = K.Muted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = K.Text, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun GarminManualInput(
    label: String,
    value: String,
    hint: String,
    accent: Color,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember(value) { mutableStateOf(value) }

    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(10.dp),
        color    = K.Card2,
        border   = BorderStroke(1.dp, accent.copy(alpha = if (draft.isNotBlank()) 0.4f else 0.15f)),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                label,
                fontSize      = 9.sp,
                letterSpacing = 1.5.sp,
                color         = accent.copy(alpha = 0.8f),
                fontWeight    = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value         = draft,
                    onValueChange = {
                        val filtered = it.filter { c -> c.isDigit() }.take(3)
                        draft = filtered
                        onSave(filtered)
                    },
                    textStyle     = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (draft.isBlank()) K.Muted else accent),
                    cursorBrush   = SolidColor(accent),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine    = true,
                    decorationBox = { inner ->
                        if (draft.isEmpty()) Text(hint, fontSize = 22.sp, color = K.Muted.copy(0.4f), fontWeight = FontWeight.Bold)
                        inner()
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private fun batteryColor(v: Int?): Color = when {
    v == null   -> Color(0xFF60A5FA)
    v >= 76     -> Color(0xFF4ADE80)
    v >= 51     -> Color(0xFFFACC15)
    v >= 26     -> Color(0xFFFB923C)
    else        -> Color(0xFFF87171)
}

private fun stressColor(v: Int?): Color = when {
    v == null   -> Color(0xFF60A5FA)
    v <= 25     -> Color(0xFF4ADE80)
    v <= 50     -> Color(0xFFFACC15)
    v <= 75     -> Color(0xFFFB923C)
    else        -> Color(0xFFF87171)
}
