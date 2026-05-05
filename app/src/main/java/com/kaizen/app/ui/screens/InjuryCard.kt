package com.kaizen.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import com.kaizen.app.data.*
import com.kaizen.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ── Severity color ─────────────────────────────────────────────────────────
fun severityColor(s: Int): Color = when (s) {
    1, 2 -> Color(0xFF4ADE80)
    3    -> Color(0xFFFACC15)
    4    -> Color(0xFFFB923C)
    else -> Color(0xFFF87171)
}

// ── Injury highlights for body map ─────────────────────────────────────────
private fun List<InjuryLog>.toInjuryHighlights(): List<MuscleHighlight> {
    val red    = Color(0xFFF87171)
    val result = mutableMapOf<String, MuscleHighlight>()
    forEach { inj ->
        val muscleNames = when (inj.bodyPart) {
            BodyPart.CALF, BodyPart.ANKLE, BodyPart.FOOT -> listOf("calves")
            BodyPart.IT_BAND, BodyPart.KNEE, BodyPart.QUAD -> listOf("quads")
            BodyPart.HIP -> listOf("glutes")
            BodyPart.HAMSTRING -> listOf("hamstrings")
            BodyPart.LOWER_BACK -> listOf("core")
            BodyPart.UPPER_BACK -> listOf("upper_back", "lats")
            BodyPart.SHOULDER -> listOf("shoulders", "rear_delts")
            BodyPart.ELBOW -> listOf("triceps", "biceps")
            BodyPart.WRIST -> listOf("triceps")
            BodyPart.NECK -> listOf()
            BodyPart.OTHER -> listOf("full_body")
        }
        val intensity = (inj.severity / 5f).coerceIn(0.3f, 1f)
        muscleNames.forEach { name ->
            val existing = result[name]
            if (existing == null || intensity > existing.intensity) {
                result[name] = MuscleHighlight(name, red, intensity)
            }
        }
    }
    return result.values.toList()
}

// ── Compact body map showing injury locations ──────────────────────────────
@Composable
private fun InjuryBodyMap(activeInjuries: List<InjuryLog>) {
    val highlights = remember(activeInjuries) { activeInjuries.toInjuryHighlights() }
    Row(
        modifier              = Modifier.fillMaxWidth().height(130.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            BodyCanvas(view = BodyView.FRONT, highlights = highlights, alpha = 1f)
            Text("front", modifier = Modifier.align(Alignment.BottomCenter),
                fontSize = 8.sp, color = K.Muted, letterSpacing = 1.sp)
        }
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            BodyCanvas(view = BodyView.BACK, highlights = highlights, alpha = 1f)
            Text("back", modifier = Modifier.align(Alignment.BottomCenter),
                fontSize = 8.sp, color = K.Muted, letterSpacing = 1.sp)
        }
    }
}

// ── Injury status card for Today screen ───────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InjuryStatusCard(
    activeInjuries: List<InjuryLog>,
    resolvedInjuries: List<InjuryLog>,
    onAddInjury: () -> Unit,
    onResolve: (InjuryLog) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasInjuries   = activeInjuries.isNotEmpty()
    var showHistory   by remember { mutableStateOf(false) }
    val injuryRed     = Color(0xFFF87171)

    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(16.dp),
        color    = if (hasInjuries) injuryRed.copy(0.08f) else K.Card,
        border   = BorderStroke(1.dp, if (hasInjuries) injuryRed.copy(0.35f) else K.Border),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // ── Header row ──────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (hasInjuries) "🩹" else "✅", fontSize = 18.sp)
                    Column {
                        Text(
                            if (hasInjuries) "${activeInjuries.size} Active ${if (activeInjuries.size == 1) "Injury" else "Injuries"}"
                            else "All Clear",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color      = if (hasInjuries) injuryRed else K.Health,
                        )
                        Text(
                            if (hasInjuries) "Tap any to mark resolved" else "No active injuries logged",
                            fontSize = 10.sp,
                            color    = K.Muted,
                        )
                    }
                }
                TextButton(onClick = onAddInjury) {
                    Text("+ Log", color = injuryRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // ── Body map (only when injured) ─────────────────────────────
            if (hasInjuries) {
                InjuryBodyMap(activeInjuries)
            }

            // ── Active injury rows ───────────────────────────────────────
            activeInjuries.forEach { injury ->
                val sColor = severityColor(injury.severity)
                val dateLabel = runCatching {
                    LocalDate.parse(injury.date).format(DateTimeFormatter.ofPattern("MMM d"))
                }.getOrElse { injury.date }

                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { onResolve(injury) },
                    shape    = RoundedCornerShape(10.dp),
                    color    = sColor.copy(0.10f),
                    border   = BorderStroke(1.dp, sColor.copy(0.3f)),
                ) {
                    Row(
                        modifier              = Modifier.padding(10.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(injury.bodyPart.emoji, fontSize = 18.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${injury.side.label} ${injury.bodyPart.label}",
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color      = K.Text,
                            )
                            Text(
                                "${injury.type.emoji} ${injury.type.label} · Severity ${injury.severity}/5 · $dateLabel",
                                fontSize = 10.sp,
                                color    = K.Muted,
                            )
                            if (injury.notes.isNotBlank()) Text(injury.notes, fontSize = 9.sp, color = K.Muted)
                        }
                        Text("✓ Resolve", fontSize = 9.sp, color = K.Muted)
                    }
                }
            }

            // ── Injury history (collapsible) ─────────────────────────────
            if (resolvedInjuries.isNotEmpty()) {
                TextButton(
                    onClick            = { showHistory = !showHistory },
                    contentPadding     = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                ) {
                    Text(
                        "${if (showHistory) "▲" else "▼"} History (${resolvedInjuries.size})",
                        color    = K.Muted,
                        fontSize = 11.sp,
                    )
                }

                AnimatedVisibility(visible = showHistory) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        resolvedInjuries.take(10).forEach { injury ->
                            val dateLabel = runCatching {
                                LocalDate.parse(injury.date).format(DateTimeFormatter.ofPattern("MMM d"))
                            }.getOrElse { injury.date }
                            Row(
                                modifier              = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                            ) {
                                Text(injury.bodyPart.emoji, fontSize = 14.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${injury.side.label} ${injury.bodyPart.label}",
                                        fontSize            = 11.sp,
                                        fontWeight          = FontWeight.SemiBold,
                                        color               = K.Muted,
                                        textDecoration      = TextDecoration.LineThrough,
                                    )
                                    Text(
                                        "${injury.type.label} · $dateLabel",
                                        fontSize = 9.sp,
                                        color    = K.Muted.copy(0.6f),
                                    )
                                }
                                Surface(shape = RoundedCornerShape(100.dp), color = K.Health.copy(0.12f)) {
                                    Text(
                                        "Resolved",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontSize = 9.sp,
                                        color    = K.Health,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Add injury bottom sheet ────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInjurySheet(
    onSubmit: (BodyPart, InjurySide, InjuryType, Int, String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var bodyPart   by remember { mutableStateOf(BodyPart.CALF) }
    var side       by remember { mutableStateOf(InjurySide.LEFT) }
    var type       by remember { mutableStateOf(InjuryType.TIGHTNESS) }
    var severity   by remember { mutableIntStateOf(3) }
    var notes      by remember { mutableStateOf("") }
    var injuryDate by remember { mutableStateOf(LocalDate.now().toString()) }
    val injuryRed  = Color(0xFFF87171)

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = K.Card2, contentColor = K.Text) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("🩹 Log Injury / Issue", style = MaterialTheme.typography.headlineSmall)

            // Body part
            SLabel("BODY PART")
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(BodyPart.values().size) { i ->
                    val bp  = BodyPart.values()[i]
                    val sel = bp == bodyPart
                    Surface(
                        modifier = Modifier.clickable { bodyPart = bp },
                        shape    = RoundedCornerShape(100.dp),
                        color    = if (sel) injuryRed.copy(0.20f) else K.Card,
                        border   = BorderStroke(1.dp, if (sel) injuryRed else K.Border),
                    ) {
                        Text(
                            "${bp.emoji} ${bp.label}",
                            modifier   = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            fontSize   = 12.sp,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                            color      = if (sel) injuryRed else K.Muted,
                        )
                    }
                }
            }

            // Side
            SLabel("SIDE")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InjurySide.values().forEach { s ->
                    val sel = s == side
                    Surface(
                        modifier = Modifier.weight(1f).clickable { side = s },
                        shape    = RoundedCornerShape(10.dp),
                        color    = if (sel) injuryRed.copy(0.15f) else K.Card,
                        border   = BorderStroke(1.dp, if (sel) injuryRed else K.Border),
                    ) {
                        Text(
                            s.label,
                            modifier   = Modifier.padding(vertical = 10.dp),
                            textAlign  = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize   = 13.sp,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                            color      = if (sel) injuryRed else K.Muted,
                        )
                    }
                }
            }

            // Type
            SLabel("TYPE")
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier              = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                InjuryType.values().forEach { t ->
                    val sel = t == type
                    Surface(
                        modifier = Modifier.clickable { type = t },
                        shape    = RoundedCornerShape(10.dp),
                        color    = if (sel) injuryRed.copy(0.15f) else K.Card,
                        border   = BorderStroke(1.dp, if (sel) injuryRed else K.Border),
                    ) {
                        Text(
                            "${t.emoji} ${t.label}",
                            modifier   = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            fontSize   = 12.sp,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                            color      = if (sel) injuryRed else K.Muted,
                        )
                    }
                }
            }

            // Severity
            SLabel("SEVERITY  $severity/5")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { s ->
                    val col = severityColor(s)
                    Surface(
                        modifier = Modifier.weight(1f).clickable { severity = s },
                        shape    = RoundedCornerShape(10.dp),
                        color    = if (s <= severity) col.copy(0.20f) else K.Card,
                        border   = BorderStroke(1.dp, if (s <= severity) col else K.Border),
                    ) {
                        Text(
                            "$s",
                            modifier   = Modifier.padding(vertical = 10.dp),
                            textAlign  = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color      = if (s <= severity) col else K.Muted,
                        )
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mild", fontSize = 9.sp, color = K.Muted)
                Text("Severe", fontSize = 9.sp, color = K.Muted)
            }

            // Date occurred
            SLabel("DATE OCCURRED")
            OutlinedTextField(
                value         = injuryDate,
                onValueChange = { injuryDate = it },
                placeholder   = { Text(LocalDate.now().toString(), color = K.Muted) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = injuryRed,
                    unfocusedBorderColor = K.Border,
                    focusedTextColor     = K.Text,
                    unfocusedTextColor   = K.Text,
                    cursorColor          = injuryRed,
                ),
            )

            // Notes
            SLabel("NOTES (optional)")
            OutlinedTextField(
                value         = notes,
                onValueChange = { notes = it },
                placeholder   = { Text("e.g. KT taped, aggravated during run", color = K.Muted) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = injuryRed,
                    unfocusedBorderColor = K.Border,
                    focusedTextColor     = K.Text,
                    unfocusedTextColor   = K.Text,
                    cursorColor          = injuryRed,
                ),
            )

            Button(
                onClick  = { onSubmit(bodyPart, side, type, severity, notes, injuryDate.ifBlank { LocalDate.now().toString() }); onDismiss() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = injuryRed, contentColor = Color.White),
            ) {
                Text("Log Injury", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun SLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.5.sp), color = K.Muted)
}
