package com.kaizen.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay

@Composable
fun RestTimer(
    initialSeconds: Int,
    accentColor: Color,
    onFinished: () -> Unit,
    onSkip: () -> Unit,
) {
    var totalSeconds by remember(initialSeconds) { mutableIntStateOf(initialSeconds.coerceAtLeast(90)) }
    var remaining    by remember(totalSeconds)    { mutableIntStateOf(totalSeconds) }
    var running      by remember { mutableStateOf(true) }

    // Reset remaining when totalSeconds changes (user adjusts)
    LaunchedEffect(totalSeconds) { remaining = totalSeconds }

    LaunchedEffect(running, remaining) {
        if (running && remaining > 0) {
            delay(1000)
            remaining--
            if (remaining == 0) onFinished()
        }
    }

    val pct by animateFloatAsState(
        targetValue   = if (totalSeconds > 0) remaining / totalSeconds.toFloat() else 0f,
        animationSpec = tween(900, easing = LinearEasing),
    )

    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            "⏱ REST",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 3.sp),
            color = accentColor,
        )

        // Ring + countdown
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
            Canvas(Modifier.size(110.dp)) {
                val stroke = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                drawArc(Color.White.copy(alpha = 0.06f), -90f, 360f, false, style = stroke)
                drawArc(accentColor, -90f, 360f * pct, false, style = stroke)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$remaining",
                    fontSize   = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color      = accentColor,
                    fontFamily = FontFamily.Monospace,
                )
                Text("sec", fontSize = 11.sp, color = accentColor.copy(alpha = 0.6f))
            }
        }

        // Adjust duration row (+/- 15s, min 90s)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                "Adjust:",
                fontSize = 11.sp,
                color    = accentColor.copy(0.7f),
                fontWeight = FontWeight.SemiBold,
            )
            listOf(-15, -30, +15, +30).forEach { delta ->
                val label = if (delta > 0) "+${delta}s" else "${delta}s"
                val canApply = totalSeconds + delta >= 90
                OutlinedButton(
                    onClick  = {
                        if (canApply) {
                            totalSeconds = (totalSeconds + delta).coerceAtLeast(90)
                        }
                    },
                    enabled  = canApply,
                    shape    = RoundedCornerShape(8.dp),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, if (canApply) accentColor.copy(0.4f) else accentColor.copy(0.1f)),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp),
                ) {
                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Min 90s note
        Text("Min 90s · Tap + to extend", fontSize = 9.sp, color = accentColor.copy(0.4f))

        // Controls
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { running = !running },
                shape   = RoundedCornerShape(100.dp),
                border  = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(0.4f)),
                colors  = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
            ) {
                Text(if (running) "⏸ Pause" else "▶ Resume", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onSkip,
                shape   = RoundedCornerShape(100.dp),
                colors  = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
            ) {
                Text("Skip →", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
