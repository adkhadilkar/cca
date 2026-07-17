package com.architectprep.app.ui.reference

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.architectprep.app.data.content.GuideDomainWeightDto
import com.architectprep.app.ui.theme.HeroText
import com.architectprep.app.ui.theme.HeroTextMuted
import com.architectprep.app.ui.theme.LocalAppColors
import com.architectprep.app.ui.theme.MonoFontFamily
import com.architectprep.app.ui.theme.SerifFontFamily
import java.text.NumberFormat
import java.util.Locale

private data class HeroStat(val value: String, val label: String)
private val ON_DAY_ICONS = listOf("🖥", "🚫", "⚑", "📄", "🛈")

@Composable
fun ExamGuideScreen(viewModel: ExamGuideViewModel, onBack: () -> Unit) {
    val guide by viewModel.guide.collectAsState()
    val colors = LocalAppColors.current

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val g = guide
        if (g == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            return@Box
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp, 16.dp, 20.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "‹",
                        color = colors.textPrimary,
                        fontSize = 20.sp,
                        modifier = Modifier.clickable(onClick = onBack).padding(end = 12.dp)
                    )
                    Text(
                        text = "Exam guide",
                        color = colors.textPrimary,
                        fontFamily = SerifFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.heroBackground, RoundedCornerShape(20.dp))
                        .border(1.dp, colors.heroBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        text = "${g.examCode} · Foundations",
                        color = HeroTextMuted,
                        fontFamily = MonoFontFamily,
                        fontSize = 11.sp
                    )
                    val stats = listOf(
                        HeroStat(NumberFormat.getIntegerInstance(Locale.US).format(g.format.questions), "questions"),
                        HeroStat("${g.format.timeLimitMin} min", "time limit"),
                        HeroStat("${g.format.passScore}", "passing (scaled ${g.format.scoreScale.substringBefore(" (")})"),
                        HeroStat("MCQ", "single-answer, 4 choices")
                    )
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) {
                        stats.chunked(2).forEach { rowStats ->
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                rowStats.forEach { stat ->
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = stat.value, color = HeroText, fontFamily = SerifFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
                                        Text(text = stat.label, color = HeroTextMuted, fontSize = 11.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Domain weighting",
                    color = colors.textPrimary,
                    fontFamily = SerifFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface, RoundedCornerShape(20.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                        .padding(18.dp, 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    g.domainWeights.forEach { d -> DomainWeightRow(d) }
                }
            }

            item {
                Text(
                    text = "On the day",
                    color = colors.textPrimary,
                    fontFamily = SerifFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
            }
            itemsIndexed(g.proctoringNotes) { i, note ->
                IconTextCard(ON_DAY_ICONS.getOrElse(i) { "🛈" }, note)
            }

            if (g.preparation.isNotEmpty()) {
                item {
                    Text(
                        text = "Preparation",
                        color = colors.textPrimary,
                        fontFamily = SerifFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    )
                }
                itemsIndexed(g.preparation) { i, tip ->
                    IconTextCard("💡", tip)
                }
            }

            item {
                Text(
                    text = g.disclaimer,
                    color = colors.textTertiary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun IconTextCard(icon: String, text: String) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(14.dp))
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = icon, fontSize = 15.sp)
        Text(text = text, color = colors.textPrimary, fontSize = 13.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun DomainWeightRow(d: GuideDomainWeightDto) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = d.code, color = colors.accent, fontFamily = MonoFontFamily, fontSize = 11.sp, modifier = Modifier.padding(end = 0.dp))
        Text(text = d.title, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(text = "${d.weightPct}%", color = colors.textSecondary, fontFamily = MonoFontFamily, fontSize = 12.sp)
    }
}
