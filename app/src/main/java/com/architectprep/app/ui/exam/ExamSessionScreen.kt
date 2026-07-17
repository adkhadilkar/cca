package com.architectprep.app.ui.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.architectprep.app.data.content.ChoiceDto
import com.architectprep.app.ui.theme.LocalAppColors
import com.architectprep.app.ui.theme.MonoFontFamily
import com.architectprep.app.ui.theme.SerifFontFamily

@Composable
fun ExamSessionScreen(viewModel: ExamSessionViewModel, onSubmitted: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalAppColors.current
    var showSubmitConfirm by remember { mutableStateOf(false) }

    val s = state
    if (s?.submitted == true) {
        onSubmitted()
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        if (s == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            return@Box
        }
        val q = s.question ?: return@Box

        Column(modifier = Modifier.fillMaxSize()) {
            androidx.compose.runtime.key(s.index) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp, 16.dp, 20.dp, 16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Q ${s.index + 1} / ${s.total}", color = colors.textTertiary, fontFamily = MonoFontFamily, fontSize = 11.sp)
                        Text(
                            text = formatDuration(s.remainingMs),
                            color = colors.background,
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier.background(colors.textPrimary, RoundedCornerShape(999.dp)).padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                        Text(
                            text = if (s.flagged) "⚑ Flagged" else "⚑ Flag",
                            color = colors.accent,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { viewModel.toggleFlag() }
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        for (i in 0 until s.total) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .background(if (i == s.index) colors.accent else colors.neutralLight, RoundedCornerShape(2.dp))
                            )
                        }
                    }

                    Text(
                        text = q.stem,
                        color = colors.textPrimary,
                        fontFamily = SerifFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        lineHeight = 26.sp,
                        modifier = Modifier.padding(top = 22.dp)
                    )

                    Column(modifier = Modifier.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        q.choices.forEach { choice ->
                            ExamChoiceRow(choice, isSelected = choice.id == s.selectedChoiceId, onClick = { viewModel.selectChoice(choice.id) })
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .padding(20.dp, 10.dp, 20.dp, 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NavChip(text = "‹", enabled = s.index > 0) { viewModel.goTo(s.index - 1) }
                Text(
                    text = "${s.flaggedCount} flagged · ${s.answeredCount} answered",
                    color = colors.textTertiary,
                    fontFamily = MonoFontFamily,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                if (s.index + 1 == s.total) {
                    Box(
                        modifier = Modifier
                            .background(colors.accentMuted, RoundedCornerShape(14.dp))
                            .clickable { showSubmitConfirm = true }
                            .padding(horizontal = 20.dp, vertical = 13.dp)
                    ) {
                        Text(text = "Submit exam", color = colors.background, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(colors.textPrimary, RoundedCornerShape(14.dp))
                            .clickable { viewModel.goTo(s.index + 1) }
                            .padding(horizontal = 22.dp, vertical = 13.dp)
                    ) {
                        Text(text = "Next ›", color = colors.background, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }

        if (showSubmitConfirm) {
            AlertDialog(
                onDismissRequest = { showSubmitConfirm = false },
                title = { Text("Submit exam?") },
                text = { Text("${s.answeredCount} of ${s.total} answered. You can't change answers after submitting.") },
                confirmButton = {
                    TextButton(onClick = { showSubmitConfirm = false; viewModel.submit() }) { Text("Submit") }
                },
                dismissButton = {
                    TextButton(onClick = { showSubmitConfirm = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun ExamChoiceRow(choice: ChoiceDto, isSelected: Boolean, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) colors.accentLight.copy(alpha = 0.35f) else colors.surface, RoundedCornerShape(14.dp))
            .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) colors.accent else colors.border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Text(text = "${choice.id.uppercase()} · ${choice.text}", color = colors.textPrimary, fontSize = 13.5.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun NavChip(text: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .background(colors.surface, RoundedCornerShape(14.dp))
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp)
    ) {
        Text(text = text, color = if (enabled) colors.textPrimary else colors.textTertiary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
