package com.architectprep.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppTypography = Typography(
    headlineMedium = TextStyle(fontFamily = SerifFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
    titleMedium = TextStyle(fontFamily = SerifFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    bodyMedium = TextStyle(fontFamily = SansFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = SansFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
    labelSmall = TextStyle(fontFamily = MonoFontFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp)
)

@Composable
fun ArchitectPrepTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val materialScheme = if (darkTheme) {
        darkColorScheme(
            background = appColors.background,
            surface = appColors.surface,
            primary = appColors.accent,
            onBackground = appColors.textPrimary,
            onSurface = appColors.textPrimary,
            outline = appColors.border
        )
    } else {
        lightColorScheme(
            background = appColors.background,
            surface = appColors.surface,
            primary = appColors.accent,
            onBackground = appColors.textPrimary,
            onSurface = appColors.textPrimary,
            outline = appColors.border
        )
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = AppTypography,
            content = content
        )
    }
}
