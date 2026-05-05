package com.kaizen.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.kaizen.app.data.*

// ── Kaizen color palette ──────────────────────────────────────────────────

object K {
    val Bg      = Color(0xFF04040C)
    val Card    = Color(0xFF0A0A18)
    val Card2   = Color(0xFF0F0F22)
    val Border  = Color(0x1EB4964F)
    val Gold    = Color(0xFFC9A84C)
    val GoldDim = Color(0xFF8A6E2F)
    val Text    = Color(0xFFF0ECDF)
    val Muted   = Color(0xFF5A5470)
    val Morning = Color(0xFF00DBA8)
    val Night   = Color(0xFF8B6DFF)
    val Streak  = Color(0xFFFF9F43)
    val Health  = Color(0xFF4ADE80)
    val WhoopRed= Color(0xFFF87171)
}

// ── Extension color functions ─────────────────────────────────────────────

fun HabitCategory.color(): Color = when (this) {
    HabitCategory.HEALTH       -> Color(0xFF4ADE80)
    HabitCategory.MINDFULNESS  -> Color(0xFFC084FC)
    HabitCategory.PRODUCTIVITY -> Color(0xFF60A5FA)
    HabitCategory.SOCIAL       -> Color(0xFFF472B6)
    HabitCategory.FITNESS      -> Color(0xFFFB923C)
    HabitCategory.FLEXIBILITY  -> Color(0xFF67E8F9)
    HabitCategory.YOGA         -> Color(0xFF86EFAC)
}

fun WorkoutType.color(): Color = Color(android.graphics.Color.parseColor(this.hexColor))

fun WhoopZone.color(): Color = when (this) {
    WhoopZone.GREEN  -> Color(0xFF4ADE80)
    WhoopZone.YELLOW -> Color(0xFFFACC15)
    WhoopZone.RED    -> Color(0xFFF87171)
}

fun Difficulty.color(): Color = when (this) {
    Difficulty.BEGINNER    -> Color(0xFF4ADE80)
    Difficulty.INTERMEDIATE-> Color(0xFFFACC15)
    Difficulty.ADVANCED    -> Color(0xFFF87171)
}

// ── Material3 theme ───────────────────────────────────────────────────────

private val darkColorScheme = darkColorScheme(
    primary         = Color(0xFFC9A84C),
    onPrimary       = Color(0xFF1A0E00),
    surface         = Color(0xFF0A0A18),
    onSurface       = Color(0xFFF0ECDF),
    background      = Color(0xFF04040C),
    onBackground    = Color(0xFFF0ECDF),
)

@Composable
fun KaizenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme,
        content     = content,
    )
}
