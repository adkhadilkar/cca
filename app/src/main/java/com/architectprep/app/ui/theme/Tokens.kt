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
    val neutralLight: Color,
    // The Home screen's "exam day" hero card is always a dark card, in both
    // light and dark theme (design/Home Variations.dc.html #1a and #1c) —
    // these don't flip like the rest of the palette.
    val heroBackground: Color,
    val heroBorder: Color,
    val heroTrack: Color,
    // 5-step tint ramp for the Home screen's segmented per-domain progress bar
    // (design/Home Variations.dc.html #1a / #1c), darkest/most-saturated first.
    val domainRamp: List<Color>
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
    neutralLight = Color(0xFFF0EBDD),
    heroBackground = Color(0xFF29261F),
    heroBorder = Color.Transparent,
    heroTrack = Color(0xFF453F33),
    domainRamp = listOf(Color(0xFFC15F3C), Color(0xFFD98E6B), Color(0xFFE5B394), Color(0xFFEFD2BD), Color(0xFFF3E3D3))
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
    neutralLight = Color(0xFF2E2A22),
    heroBackground = Color(0xFF2E2A22),
    heroBorder = Color(0xFF453F33),
    heroTrack = Color(0xFF453F33),
    domainRamp = listOf(Color(0xFFD97757), Color(0xFFB06246), Color(0xFF8A4E38), Color(0xFF663B2B), Color(0xFF4A2E23))
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
