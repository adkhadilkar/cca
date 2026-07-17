package com.architectprep.app.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.architectprep.app.ui.theme.LocalAppColors
import com.architectprep.app.ui.theme.MonoFontFamily
import com.architectprep.app.ui.theme.SerifFontFamily

@Composable
fun ProgressScreen(viewModel: ProgressViewModel) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalAppColors.current

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val s = state
        if (s == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            return@Box
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(text = "Progress", color = colors.textPrimary, fontFamily = SerifFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 26.sp)
            }
            item { ReadinessCard(s.readinessPct) }
            if (s.mockScores.isNotEmpty()) {
                item { MockScoreChart(s.mockScores, s.passScore) }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("Questions done", "${s.questionsDone}", Modifier.weight(1f))
                    StatTile("Accuracy", "${s.accuracyPct}%", Modifier.weight(1f))
                    StatTile("Study time", "${"%.1f".format(s.studyTimeHours)}h", Modifier.weight(1f))
                }
            }
            if (s.weakDomains.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surface, RoundedCornerShape(20.dp))
                            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                            .padding(16.dp, 18.dp)
                    ) {
                        Text(text = "Weak areas", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        s.weakDomains.forEach { w ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "${w.code} ${w.title}", color = colors.accentMuted, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
                                Text(text = "${w.accuracyPct}%", color = colors.accentMuted, fontFamily = MonoFontFamily, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            item { StreakCard(s.streakDays, s.last28Days) }
        }
    }
}

@Composable
private fun ReadinessCard(readinessPct: Int) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(20.dp))
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = Stroke(width = 8.dp.toPx())
                drawArc(
                    color = colors.neutralLight,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke,
                    size = Size(size.width - stroke.width, size.height - stroke.width),
                    topLeft = androidx.compose.ui.geometry.Offset(stroke.width / 2, stroke.width / 2)
                )
                drawArc(
                    color = colors.accent,
                    startAngle = -90f,
                    sweepAngle = 360f * (readinessPct / 100f),
                    useCenter = false,
                    style = stroke,
                    size = Size(size.width - stroke.width, size.height - stroke.width),
                    topLeft = androidx.compose.ui.geometry.Offset(stroke.width / 2, stroke.width / 2)
                )
            }
            Text(text = "$readinessPct%", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Column {
            Text(text = "Exam readiness", color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(
                text = "Blended from mock scores, lesson completion, and flashcard mastery.",
                color = colors.textSecondary,
                fontSize = 11.5.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun MockScoreChart(scores: List<MockScorePoint>, passScore: Int) {
    val colors = LocalAppColors.current
    val maxScore = 1000
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(20.dp))
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .padding(16.dp, 18.dp)
    ) {
        Text(text = "Mock exam scores", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Row(
            modifier = Modifier.fillMaxWidth().height(120.dp).padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            scores.forEach { point ->
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${point.score}", color = colors.textTertiary, fontFamily = MonoFontFamily, fontSize = 10.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((point.score.coerceIn(0, maxScore) / maxScore.toFloat() * 90).dp)
                            .padding(top = 4.dp)
                            .background(if (point.passed) colors.success else colors.accentLight, RoundedCornerShape(6.dp, 6.dp, 0.dp, 0.dp))
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            scores.forEach { point ->
                Text(text = point.label, color = colors.textTertiary, fontFamily = MonoFontFamily, fontSize = 9.5.sp, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(text = label.uppercase(), color = colors.textTertiary, fontFamily = MonoFontFamily, fontSize = 9.5.sp)
        Text(text = value, color = colors.textPrimary, fontFamily = SerifFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun StreakCard(streakDays: Int, days: List<StreakCell>) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(20.dp))
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .padding(16.dp, 18.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Streak — last 4 weeks", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = "$streakDays days" + if (streakDays > 0) " 🔥" else "", color = colors.accent, fontFamily = MonoFontFamily, fontSize = 11.sp)
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth().height(90.dp).padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(days) { cell ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(if (cell.goalMet) colors.accent else colors.neutralLight, RoundedCornerShape(4.dp))
                )
            }
        }
    }
}
