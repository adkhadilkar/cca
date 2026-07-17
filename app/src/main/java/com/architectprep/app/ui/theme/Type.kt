package com.architectprep.app.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.architectprep.app.R

// design/README.md's typography spec, bundled as offline font assets (no
// runtime Google Fonts fetch — see docs/DEVELOPMENT_DESIGN.md §3.3). Source
// Serif 4 and Public Sans ship only as variable fonts upstream, so each
// weight is a distinct FontVariation.Settings instance over the same file
// rather than a separate static font file.

@OptIn(ExperimentalTextApi::class)
val SerifFontFamily = FontFamily(
    Font(R.font.source_serif_4, weight = FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.source_serif_4, weight = FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.source_serif_4, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.source_serif_4, weight = FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700)))
)

@OptIn(ExperimentalTextApi::class)
val SansFontFamily = FontFamily(
    Font(R.font.public_sans, weight = FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.public_sans, weight = FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.public_sans, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.public_sans, weight = FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700)))
)

val MonoFontFamily = FontFamily(
    Font(R.font.ibm_plex_mono_regular, weight = FontWeight.Normal),
    Font(R.font.ibm_plex_mono_medium, weight = FontWeight.Medium)
)
