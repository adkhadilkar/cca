package com.architectprep.app.ui.exam

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.architectprep.app.domain.PerDomainScore
import com.architectprep.app.ui.theme.LocalAppColors
import com.architectprep.app.ui.theme.MonoFontFamily
import com.architectprep.app.ui.theme.SerifFontFamily

@Composable
fun ResultsScreen(viewModel: ResultsViewModel, onDone: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalAppColors.current
    var reviewing by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val s = state
        if (s == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            return@Box
        }

        if (reviewing) {
            ReviewMissedList(s, onBack = { reviewing = false })
            return@Box
        }

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 20.dp, 20.dp, 28.dp)) {
            item {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "MOCK EXAM · CCAR-F", color = colors.textTertiary, fontFamily = MonoFontFamily, fontSize = 11.sp)
                    Text(
                        text = "${s.score}",
                        color = if (s.passed) colors.success else colors.accentMuted,
                        fontFamily = SerifFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 56.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .background(if (s.passed) colors.success.copy(alpha = 0.15f) else colors.accentMuted.copy(alpha = 0.15f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${if (s.passed) "PASS" else "FAIL"} · target ${s.passScore}",
                            color = if (s.passed) colors.success else colors.accentMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = "${s.correct} of ${s.total} correct",
                        color = colors.textSecondary,
                        fontSize = 12.5.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                Text(
                    text = "By domain",
                    color = colors.textPrimary,
                    fontFamily = SerifFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(top = 24.dp, bottom = 10.dp)
                )
            }
            items(s.perDomain) { d -> DomainScoreRow(d) }
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (s.missed.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(colors.surface, RoundedCornerShape(14.dp))
                                .border(1.5.dp, colors.textPrimary, RoundedCornerShape(14.dp))
                                .clickable { reviewing = true }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Review ${s.missed.size} missed", color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(colors.textPrimary, RoundedCornerShape(14.dp))
                            .clickable(onClick = onDone)
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Done", color = colors.background, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DomainScoreRow(d: PerDomainScore) {
    val colors = LocalAppColors.current
    val fraction = if (d.total == 0) 0f else d.correct.toFloat() / d.total
    val barColor = if (fraction >= 0.8f) colors.success else if (fraction >= 0.6f) colors.accent else colors.accentMuted
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .background(colors.surface, RoundedCornerShape(14.dp))
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "${d.domainCode} ${d.domainTitle}", color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(text = "${d.correct}/${d.total}", color = colors.textSecondary, fontFamily = MonoFontFamily, fontSize = 12.sp)
        }
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 7.dp).background(colors.neutralLight, RoundedCornerShape(2.dp))) {
            Box(modifier = Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(4.dp).background(barColor, RoundedCornerShape(2.dp)))
        }
    }
}

@Composable
private fun ReviewMissedList(s: ResultsUiState, onBack: () -> Unit) {
    val colors = LocalAppColors.current
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 16.dp, 20.dp, 24.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "‹", color = colors.textPrimary, fontSize = 22.sp, modifier = Modifier.clickable(onClick = onBack).padding(end = 12.dp))
                Text(text = "Missed questions", color = colors.textPrimary, fontFamily = SerifFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            }
        }
        items(s.missed) { m ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .background(colors.surface, RoundedCornerShape(16.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(text = m.stem, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp, lineHeight = 20.sp)
                Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    m.choices.forEach { c ->
                        val correct = c.id in m.correctChoiceIds
                        val chosen = c.id == m.chosenChoiceId
                        val color = when {
                            correct -> colors.success
                            chosen -> colors.accentMuted
                            else -> colors.textSecondary
                        }
                        Text(text = "${c.id.uppercase()} · ${c.text}${if (correct) "  ✓" else if (chosen) "  ✕" else ""}", color = color, fontSize = 12.5.sp)
                    }
                }
                Text(
                    text = m.explanation,
                    color = colors.textSecondary,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}
