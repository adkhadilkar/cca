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
import com.architectprep.app.ui.theme.LocalAppColors
import com.architectprep.app.ui.theme.MonoFontFamily
import com.architectprep.app.ui.theme.SerifFontFamily

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
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "‹",
                        color = colors.textPrimary,
                        fontSize = 22.sp,
                        modifier = Modifier.clickable(onClick = onBack).padding(end = 12.dp)
                    )
                    Text(
                        text = "Exam guide",
                        color = colors.textPrimary,
                        fontFamily = SerifFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp
                    )
                }
                Text(
                    text = g.title,
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            item {
                InfoCard(title = "Format") {
                    InfoRow("Questions", "${g.format.questions}")
                    InfoRow("Time limit", "${g.format.timeLimitMin} min")
                    InfoRow("Pass score", "${g.format.passScore} (${g.format.scoreScale})")
                    InfoRow("Fee", "\$${g.format.examFeeUSD}")
                    InfoRow("Delivery", g.format.delivery)
                    InfoRow("Open book", if (g.format.openBook) "Yes" else "No")
                    InfoRow("AI assistance", if (g.format.aiAssistanceAllowed) "Allowed" else "Not allowed")
                    InfoRow("Result reporting", g.format.resultReporting)
                    InfoRow("Validity", g.format.validityPeriod)
                }
            }

            item {
                InfoCard(title = "Domain weights") {
                    g.domainWeights.forEach { d -> DomainWeightRow(d) }
                }
            }

            item {
                InfoCard(title = "Proctoring") {
                    g.proctoringNotes.forEach { note ->
                        Text(
                            text = "•  $note",
                            color = colors.textPrimary,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            if (g.preparation.isNotEmpty()) {
                item {
                    InfoCard(title = "Preparation tips") {
                        g.preparation.forEach { tip ->
                            Text(
                                text = "•  $tip",
                                color = colors.textPrimary,
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = g.disclaimer,
                    color = colors.textTertiary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(18.dp))
            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title.uppercase(),
            color = colors.accent,
            fontFamily = MonoFontFamily,
            fontSize = 10.sp
        )
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = colors.textSecondary, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
        Text(
            text = value,
            color = colors.textPrimary,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1.3f)
        )
    }
}

@Composable
private fun DomainWeightRow(d: GuideDomainWeightDto) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "${d.code} · ${d.title}", color = colors.textPrimary, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
        Text(text = "${d.weightPct}%", color = colors.accent, fontFamily = MonoFontFamily, fontSize = 12.sp)
    }
}
