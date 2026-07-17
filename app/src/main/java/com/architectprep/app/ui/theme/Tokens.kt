package com.architectprep.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Design tokens ported verbatim from design/README.md (branch base_design).
 * These are the source of truth for color — do not hand-tune values here
 * without updating the design doc too.
 */
data class AppColors(
    val background: Color,
    val surface: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentMuted: Color,
    val accentLight: Color,
    val success: Color,
    val neutralLight: Color
)

val LightAppColors = AppColors(
    background = Color(0xFFFAF7F0),
    surface = Color(0xFFFFFFFF),
    border = Color(0xFFE5DFCF),
    textPrimary = Color(0xFF29261F),
    textSecondary = Color(0xFF6B6555),
    textTertiary = Color(0xFF8A8371),
    accent = Color(0xFFC15F3C),
    accentMuted = Color(0xFFB0483A),
    accentLight = Color(0xFFE5B394),
    success = Color(0xFF5A8A5E),
    neutralLight = Color(0xFFF0EBDD)
)

val DarkAppColors = AppColors(
    background = Color(0xFF211E18),
    surface = Color(0xFF2A2620),
    border = Color(0xFF3A352B),
    textPrimary = Color(0xFFF0EBDD),
    textSecondary = Color(0xFFA8A190),
    textTertiary = Color(0xFF8A8371),
    accent = Color(0xFFE8956D),
    accentMuted = Color(0xFFB06246),
    accentLight = Color(0xFFE5B394),
    success = Color(0xFF7FB08A),
    neutralLight = Color(0xFF2E2A22)
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
