package com.architectprep.app.ui.theme

import androidx.compose.ui.text.font.FontFamily

/**
 * TODO(M0 follow-up): bundle the actual offline font files and swap these
 * system fallbacks for real FontFamily(Font(R.font....)) declarations —
 * design/README.md specifies Source Serif 4 (headlines), Public Sans (body/
 * UI), and IBM Plex Mono (codes/timestamps/domain tags). Using system
 * fallbacks for now keeps M0 buildable without shipping font assets yet;
 * fetching Google Fonts at runtime would violate the offline requirement.
 */
val SerifFontFamily = FontFamily.Serif
val SansFontFamily = FontFamily.SansSerif
val MonoFontFamily = FontFamily.Monospace
