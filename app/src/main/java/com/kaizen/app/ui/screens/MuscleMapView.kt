package com.kaizen.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.kaizen.app.data.*
import com.kaizen.app.ui.theme.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.ExperimentalMaterial3Api

// ── Muscle group definitions ───────────────────────────────────────────────

data class MuscleHighlight(
    val name: String,
    val color: Color,
    val intensity: Float,       // 0f = dim, 1f = fully lit
)

fun WorkoutType.muscleHighlights(): List<MuscleHighlight> = when (this) {
    WorkoutType.PUSH -> listOf(
        MuscleHighlight("chest",      Color(0xFFFF6B6B), 1.0f),
        MuscleHighlight("shoulders",  Color(0xFFFF6B6B), 0.8f),
        MuscleHighlight("triceps",    Color(0xFFFF9F43), 0.7f),
        MuscleHighlight("core",       Color(0xFFFFE66D), 0.3f),
    )
    WorkoutType.PULL -> listOf(
        MuscleHighlight("lats",       Color(0xFF4ECDC4), 1.0f),
        MuscleHighlight("upper_back", Color(0xFF4ECDC4), 0.9f),
        MuscleHighlight("biceps",     Color(0xFF4ECDC4), 0.8f),
        MuscleHighlight("rear_delts", Color(0xFF4ECDC4), 0.6f),
        MuscleHighlight("core",       Color(0xFFFFE66D), 0.4f),
    )
    WorkoutType.LEGS -> listOf(
        MuscleHighlight("glutes",     Color(0xFFFFE66D), 1.0f),
        MuscleHighlight("quads",      Color(0xFFFFE66D), 0.9f),
        MuscleHighlight("hamstrings", Color(0xFFFF9F43), 0.7f),
        MuscleHighlight("calves",     Color(0xFFFF9F43), 0.4f),
        MuscleHighlight("core",       Color(0xFFFFE66D), 0.3f),
    )
    WorkoutType.CARDIO -> listOf(
        MuscleHighlight("full_body",  Color(0xFFA8EDEA), 0.6f),
        MuscleHighlight("core",       Color(0xFFA8EDEA), 0.8f),
        MuscleHighlight("heart",      Color(0xFFF87171), 1.0f),
    )
}

// ── Composable ────────────────────────────────────────────────────────────

@Composable
fun MuscleMapCard(
    workoutType: WorkoutType?,
    modifier: Modifier = Modifier,
) {
    val highlights = workoutType?.muscleHighlights() ?: emptyList()
    val wColor     = workoutType?.color() ?: K.Muted

    // Animate alpha on workout type change
    val animAlpha by animateFloatAsState(
        targetValue   = if (workoutType != null) 1f else 0.3f,
        animationSpec = tween(500),
    )

    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(20.dp),
        color    = K.Card,
        border   = androidx.compose.foundation.BorderStroke(1.dp, K.Border),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    "Muscles Today",
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                    color = K.Text,
                )
                if (workoutType != null) {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = wColor.copy(alpha = 0.15f),
                    ) {
                        Text(
                            "${workoutType.emoji} ${workoutType.label}",
                            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color      = wColor,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Front + back body side by side
            Row(
                modifier              = Modifier.fillMaxWidth().height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Front view
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    BodyCanvas(
                        view       = BodyView.FRONT,
                        highlights = highlights,
                        alpha      = animAlpha,
                    )
                    Text(
                        "front",
                        modifier = Modifier.align(Alignment.BottomCenter),
                        fontSize = 9.sp,
                        color    = K.Muted,
                        letterSpacing = 1.sp,
                    )
                }
                // Back view
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    BodyCanvas(
                        view       = BodyView.BACK,
                        highlights = highlights,
                        alpha      = animAlpha,
                    )
                    Text(
                        "back",
                        modifier = Modifier.align(Alignment.BottomCenter),
                        fontSize = 9.sp,
                        color    = K.Muted,
                        letterSpacing = 1.sp,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Muscle legend chips
            if (highlights.isNotEmpty()) {
                @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement   = Arrangement.spacedBy(4.dp),
                ) {
                    highlights.sortedByDescending { it.intensity }.forEach { m ->
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = m.color.copy(alpha = 0.15f * m.intensity),
                        ) {
                            Text(
                                m.name.replace('_', ' '),
                                modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = m.color.copy(alpha = 0.4f + 0.6f * m.intensity),
                            )
                        }
                    }
                }
            } else {
                Text("Rest day — recovery mode 🛌", fontSize = 12.sp, color = K.Muted)
            }
        }
    }
}

// ── Body canvas drawing ────────────────────────────────────────────────────

enum class BodyView { FRONT, BACK }

@Composable
internal fun BodyCanvas(
    view: BodyView,
    highlights: List<MuscleHighlight>,
    alpha: Float,
) {
    val outlineColor = Color.White.copy(alpha = 0.10f)
    val isFront      = view == BodyView.FRONT

    Canvas(modifier = Modifier.fillMaxSize()) {
        val density = this.density
        val w = size.width
        val h = size.height

        // Scale factor — body fits in the canvas
        val scale = minOf(w / 80f, h / 170f)
        val cx    = w / 2f
        val top   = h * 0.02f

        fun r(x: Float, y: Float, rw: Float, rh: Float, corner: Float = 4f) =
            RoundRect(Rect(cx + x * scale, top + y * scale, cx + (x + rw) * scale, top + (y + rh) * scale), CornerRadius(corner.dp.toPx()))

        // Helper to draw a filled rounded rect
        fun DrawScope.fillRect(x: Float, y: Float, rw: Float, rh: Float, color: Color, cornerDp: Float = 4f) {
            val path = Path().apply { addRoundRect(r(x, y, rw, rh, cornerDp)) }
            drawPath(path, color)
        }

        // Helper to draw outline
        fun DrawScope.strokeRect(x: Float, y: Float, rw: Float, rh: Float, cornerDp: Float = 4f) {
            val path = Path().apply { addRoundRect(r(x, y, rw, rh, cornerDp)) }
            drawPath(path, outlineColor, style = Stroke(width = 0.8.dp.toPx()))
        }

        // Muscle color lookups
        fun muscleColor(name: String): Color? =
            highlights.firstOrNull { it.name == name }
                ?.let { it.color.copy(alpha = it.intensity * alpha * 0.75f) }

        val dimBody = Color.White.copy(alpha = 0.04f)

        // ── HEAD ──────────────────────────────────────────────────────
        val headPath = Path().apply {
            addOval(Rect(cx - 10 * scale, top, cx + 10 * scale, top + 20 * scale))
        }
        drawPath(headPath, outlineColor)

        // ── NECK ──────────────────────────────────────────────────────
        fillRect(-4f, 20f, 8f, 8f, outlineColor)

        // ── TORSO ─────────────────────────────────────────────────────
        if (isFront) {
            // Chest
            val chestCol = muscleColor("chest") ?: dimBody
            fillRect(-22f, 28f, 44f, 26f, chestCol, 5f)
            strokeRect(-22f, 28f, 44f, 26f, 5f)

            // Abs / core
            val coreCol = muscleColor("core") ?: dimBody
            fillRect(-14f, 54f, 28f, 30f, coreCol, 4f)
            strokeRect(-14f, 54f, 28f, 30f, 4f)
        } else {
            // Upper back / lats
            val backCol  = muscleColor("upper_back") ?: dimBody
            val latCol   = muscleColor("lats") ?: dimBody
            fillRect(-22f, 28f, 16f, 40f, latCol, 4f)
            fillRect(6f, 28f, 16f, 40f, latCol, 4f)
            fillRect(-6f, 28f, 12f, 28f, backCol, 4f)
            strokeRect(-22f, 28f, 44f, 42f, 5f)

            // Lower back
            val coreCol = muscleColor("core") ?: dimBody
            fillRect(-12f, 70f, 24f, 18f, coreCol, 4f)
        }

        // ── HIPS ──────────────────────────────────────────────────────
        val gluteCol = muscleColor("glutes") ?: dimBody
        if (isFront) {
            fillRect(-20f, 84f, 40f, 22f, dimBody, 4f)
            strokeRect(-20f, 84f, 40f, 22f, 4f)
        } else {
            fillRect(-20f, 84f, 40f, 22f, gluteCol, 4f)
            strokeRect(-20f, 84f, 40f, 22f, 4f)
        }

        // ── ARMS ──────────────────────────────────────────────────────
        val shoulderCol = muscleColor("shoulders") ?: dimBody
        val bicepCol    = muscleColor("biceps") ?: dimBody
        val tricepCol   = muscleColor("triceps") ?: dimBody
        val rearDeltCol = muscleColor("rear_delts") ?: dimBody

        // Left arm (viewer's right)
        fillRect(-36f, 28f, 13f, 16f, if (isFront) shoulderCol else rearDeltCol, 5f)
        fillRect(-36f, 44f, 13f, 22f, if (isFront) tricepCol else bicepCol, 5f)
        strokeRect(-36f, 28f, 13f, 46f, 5f)

        // Right arm (viewer's left)
        fillRect(23f, 28f, 13f, 16f, if (isFront) shoulderCol else rearDeltCol, 5f)
        fillRect(23f, 44f, 13f, 22f, if (isFront) tricepCol else bicepCol, 5f)
        strokeRect(23f, 28f, 13f, 46f, 5f)

        // Forearms (dimmed)
        fillRect(-35f, 70f, 12f, 22f, dimBody, 4f)
        fillRect(23f, 70f, 12f, 22f, dimBody, 4f)
        strokeRect(-35f, 70f, 12f, 22f, 4f)
        strokeRect(23f, 70f, 12f, 22f, 4f)

        // ── LEGS ──────────────────────────────────────────────────────
        val quadCol  = muscleColor("quads") ?: dimBody
        val hamCol   = muscleColor("hamstrings") ?: dimBody
        val calfCol  = muscleColor("calves") ?: dimBody

        val leftLegColor  = if (isFront) quadCol else hamCol
        val rightLegColor = if (isFront) quadCol else hamCol

        // Left leg
        fillRect(-20f, 106f, 17f, 36f, leftLegColor, 5f)
        fillRect(-20f, 142f, 17f, 22f, calfCol, 4f)
        strokeRect(-20f, 106f, 17f, 58f, 5f)

        // Right leg
        fillRect(3f, 106f, 17f, 36f, rightLegColor, 5f)
        fillRect(3f, 142f, 17f, 22f, calfCol, 4f)
        strokeRect(3f, 106f, 17f, 58f, 5f)

        // ── HEART glow for cardio ──────────────────────────────────────
        val heartCol = muscleColor("heart")
        if (heartCol != null && isFront) {
            val heartPath = Path().apply {
                val hx = cx - 4 * scale
                val hy = top + 36 * scale
                val hr = 5 * scale
                moveTo(hx, hy + hr * 0.7f)
                cubicTo(hx - hr * 1.5f, hy - hr * 0.5f, hx - hr * 2f, hy + hr * 1.5f, hx, hy + hr * 2.5f)
                cubicTo(hx + hr * 2f, hy + hr * 1.5f, hx + hr * 1.5f, hy - hr * 0.5f, hx, hy + hr * 0.7f)
            }
            drawPath(heartPath, heartCol.copy(alpha = alpha * 0.9f))
        }

        // ── FULL BODY overlay for cardio ──────────────────────────────
        val fullBodyCol = muscleColor("full_body")
        if (fullBodyCol != null) {
            val path = Path().apply {
                addRoundRect(r(-36f, 28f, 72f, 136f, 8f))
            }
            drawPath(path, fullBodyCol.copy(alpha = alpha * 0.25f))
        }
    }
}
