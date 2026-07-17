package com.architectprep.app.ui.practice

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
import com.architectprep.app.data.content.ChoiceDto
import com.architectprep.app.ui.theme.LocalAppColors
import com.architectprep.app.ui.theme.MonoFontFamily
import com.architectprep.app.ui.theme.SerifFontFamily

@Composable
fun PracticeSessionScreen(
    viewModel: PracticeSessionViewModel,
    onExit: () -> Unit,
    onReviewDomain: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalAppColors.current

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val s = state
        if (s == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            return@Box
        }
        if (s.total == 0) {
            Column(modifier = Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No questions in this domain yet.", color = colors.textSecondary, fontSize = 14.sp)
            }
            return@Box
        }
        if (s.sessionComplete) {
            SessionSummary(s, onExit)
            return@Box
        }
        val q = s.question ?: return@Box

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 16.dp, 20.dp, 24.dp)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "✕", color = colors.textPrimary, fontSize = 18.sp, modifier = Modifier.clickable(onClick = onExit))
                    Text(
                        text = "Question ${s.index + 1} of ${s.total} · ${s.domainCode}",
                        color = colors.textTertiary,
                        fontFamily = MonoFontFamily,
                        fontSize = 11.sp
                    )
                    Box(modifier = Modifier.height(18.dp))
                }

                Text(
                    text = q.stem,
                    color = colors.textPrimary,
                    fontFamily = SerifFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    q.choices.forEach { choice ->
                        ChoiceRow(
                            choice = choice,
                            isCorrect = choice.id in q.correctChoiceIds,
                            isSelected = choice.id == s.selectedChoiceId,
                            revealed = s.revealed,
                            onClick = { viewModel.selectChoice(choice.id) }
                        )
                    }
                }

                if (s.revealed) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .background(colors.surface, RoundedCornerShape(16.dp))
                            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(text = "EXPLANATION", color = colors.success, fontFamily = MonoFontFamily, fontSize = 10.sp)
                        Text(
                            text = q.explanation,
                            color = colors.textPrimary,
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        Text(
                            text = q.sourceRef,
                            color = colors.textTertiary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                        Text(
                            text = "→ Review: ${s.domainCode} · ${s.domainTitle}",
                            color = colors.accent,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .clickable { onReviewDomain(s.domainId) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(colors.surface, RoundedCornerShape(14.dp))
                                .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                                .clickable(enabled = !s.flashcardAdded) { viewModel.addToFlashcards() }
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (s.flashcardAdded) "✓ Added" else "+ Flashcard",
                                color = if (s.flashcardAdded) colors.success else colors.textSecondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(colors.textPrimary, RoundedCornerShape(14.dp))
                                .clickable { viewModel.nextQuestion() }
                                .padding(vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val label = if (s.index + 1 == s.total) "See results ›" else "Next question ›"
                            Text(text = label, color = colors.background, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoiceRow(choice: ChoiceDto, isCorrect: Boolean, isSelected: Boolean, revealed: Boolean, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val (bg, border) = when {
        !revealed -> colors.surface to colors.border
        isCorrect -> colors.success.copy(alpha = 0.12f) to colors.success
        isSelected -> colors.accentMuted.copy(alpha = 0.12f) to colors.accentMuted
        else -> colors.surface to colors.border
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(14.dp))
            .border(if (revealed && (isCorrect || isSelected)) 1.5.dp else 1.dp, border, RoundedCornerShape(14.dp))
            .clickable(enabled = !revealed, onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${choice.id.uppercase()} · ${choice.text}",
            color = colors.textPrimary,
            fontSize = 13.5.sp,
            modifier = Modifier.weight(1f)
        )
        if (revealed && isCorrect) Text(text = "✓", color = colors.success, fontWeight = FontWeight.Bold)
        if (revealed && isSelected && !isCorrect) Text(text = "✕", color = colors.accentMuted, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SessionSummary(s: PracticeSessionUiState, onExit: () -> Unit) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${s.sessionCorrect}/${s.total}",
            color = colors.textPrimary,
            fontFamily = SerifFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 40.sp
        )
        Text(
            text = "correct in ${s.domainTitle}",
            color = colors.textSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
                .background(colors.textPrimary, RoundedCornerShape(14.dp))
                .clickable(onClick = onExit)
                .padding(vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Done", color = colors.background, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}
