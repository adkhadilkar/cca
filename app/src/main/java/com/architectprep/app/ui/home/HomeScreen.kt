package com.architectprep.app.ui.home

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.architectprep.app.ui.theme.HeroText
import com.architectprep.app.ui.theme.HeroTextMuted
import com.architectprep.app.ui.theme.LocalAppColors
import com.architectprep.app.ui.theme.MonoFontFamily
import com.architectprep.app.ui.theme.SerifFontFamily

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenStudy: () -> Unit,
    onContinueLesson: (String) -> Unit
) {
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
            contentPadding = PaddingValues(20.dp, 20.dp, 20.dp, 24.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(text = s.dateLabel, color = colors.textTertiary, fontFamily = MonoFontFamily, fontSize = 11.sp)
                        Text(
                            text = s.greeting,
                            color = colors.textPrimary,
                            fontFamily = SerifFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 26.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .background(colors.neutralLight, RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(colors.success, CircleShape))
                        Text(text = "offline", color = colors.textPrimary, fontFamily = MonoFontFamily, fontSize = 11.sp)
                    }
                }

                ExamHeroCard(s, modifier = Modifier.padding(top = 20.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        value = "${s.streakDays}",
                        unit = " day${if (s.streakDays == 1) "" else "s"}",
                        caption = "Study streak 🔥",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        value = "${s.todayGoalMinutes}",
                        unit = " / ${s.dailyGoalMinutes}",
                        caption = "Today's goal · min",
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Your tracks",
                    color = colors.textPrimary,
                    fontFamily = SerifFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
                )

                TrackCard(s, onClick = onOpenStudy)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(colors.neutralLight.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "CCAR-P", color = colors.textTertiary, fontFamily = MonoFontFamily, fontSize = 11.sp)
                        Text(text = "63 Q · 120 min", color = colors.textTertiary, fontSize = 11.sp)
                    }
                    Text(
                        text = "Architect — Professional",
                        color = colors.textSecondary,
                        fontFamily = SerifFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 19.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "Stakeholder, lifecycle & governance · Not started",
                        color = colors.textTertiary,
                        fontSize = 12.5.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (s.continueLesson != null) {
                    ContinueCard(s.continueLesson, onClick = { onContinueLesson(s.continueLesson.lessonId) }, modifier = Modifier.padding(top = 20.dp))
                }
            }
        }
    }
}

@Composable
private fun ExamHeroCard(s: HomeUiState, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.heroBackground, RoundedCornerShape(20.dp))
            .border(1.dp, colors.heroBorder, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Text(text = "EXAM DAY · ${s.trackCode}", color = HeroTextMuted, fontFamily = MonoFontFamily, fontSize = 11.sp)
                Text(
                    text = s.examDaysLabel ?: "Set exam date",
                    color = HeroText,
                    fontFamily = SerifFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 34.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(text = s.examDateLabel ?: "In Settings", color = HeroTextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${s.readinessPct}%",
                    color = colors.accent,
                    fontFamily = SerifFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 34.sp
                )
                Text(text = "readiness", color = HeroTextMuted, fontSize = 12.sp)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(6.dp)
                .background(colors.heroTrack, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((s.readinessPct / 100f).coerceIn(0f, 1f))
                    .height(6.dp)
                    .background(colors.accent, RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
private fun StatTile(value: String, unit: String, caption: String, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row {
            Text(text = value, color = colors.textPrimary, fontFamily = SerifFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
            Text(text = unit, color = colors.textTertiary, fontSize = 13.sp, modifier = Modifier.align(Alignment.Bottom).padding(bottom = 2.dp))
        }
        Text(text = caption, color = colors.textSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun TrackCard(s: HomeUiState, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(20.dp))
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = s.trackCode, color = colors.accent, fontFamily = MonoFontFamily, fontSize = 11.sp)
            Text(
                text = "${s.questionCount} Q · ${s.timeLimitMin} min · ${s.passScore} to pass",
                color = colors.textTertiary,
                fontSize = 11.sp
            )
        }
        Text(
            text = "Architect — Foundations",
            color = colors.textPrimary,
            fontFamily = SerifFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 19.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = "${s.domainSegments.size} domains · ${s.lessonsDone} of ${s.lessonsTotal} lessons done",
            color = colors.textSecondary,
            fontSize = 12.5.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            s.domainSegments.forEachIndexed { i, seg ->
                Box(
                    modifier = Modifier
                        .weight(seg.weightPct.toFloat().coerceAtLeast(1f))
                        .height(5.dp)
                        .background(colors.domainRamp.getOrElse(i) { colors.accent }, RoundedCornerShape(3.dp))
                )
            }
        }
        // Same weights as the segment bar above so each label sits under its segment.
        Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            s.domainSegments.forEachIndexed { i, seg ->
                Text(
                    text = "${seg.shortLabel} ${seg.weightPct}%",
                    color = colors.textTertiary,
                    fontFamily = MonoFontFamily,
                    fontSize = 9.5.sp,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = when (i) {
                        0 -> TextAlign.Start
                        s.domainSegments.lastIndex -> TextAlign.End
                        else -> TextAlign.Center
                    },
                    modifier = Modifier.weight(seg.weightPct.toFloat().coerceAtLeast(1f))
                )
            }
        }
    }
}

@Composable
private fun ContinueCard(lesson: ContinueLesson, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(20.dp))
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp, 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp).background(colors.accentLight.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = lesson.domainCode, color = colors.accent, fontFamily = SerifFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Continue: ${lesson.title}", color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
                text = "${lesson.domainTitle} · Lesson ${lesson.indexInDomain} of ${lesson.totalInDomain}",
                color = colors.textSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
        Text(text = "›", color = colors.accent, fontSize = 20.sp)
    }
}
