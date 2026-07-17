package com.architectprep.app.ui.flashcards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.architectprep.app.domain.Grade
import com.architectprep.app.ui.theme.LocalAppColors
import com.architectprep.app.ui.theme.MonoFontFamily
import com.architectprep.app.ui.theme.SerifFontFamily

@Composable
fun FlashcardsScreen(viewModel: FlashcardsViewModel, onExit: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalAppColors.current

    Column(modifier = Modifier.fillMaxSize().background(colors.background).padding(20.dp, 16.dp)) {
        val s = state
        if (s == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "✕", color = colors.textPrimary, fontSize = 18.sp, modifier = Modifier.clickable(onClick = onExit))
            Text(
                text = "${s.dueCount} due · ${s.newCount} new",
                color = colors.textTertiary,
                fontFamily = MonoFontFamily,
                fontSize = 11.sp
            )
            Box(modifier = Modifier.height(18.dp))
        }

        if (s.totalInSession == 0) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("All caught up — nothing due right now.", color = colors.textSecondary, fontSize = 14.sp)
            }
            return@Column
        }

        val fraction = s.index.toFloat() / s.totalInSession
        Box(
            modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 14.dp)
                .background(colors.neutralLight, RoundedCornerShape(2.dp))
        ) {
            Box(modifier = Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(4.dp).background(colors.accent, RoundedCornerShape(2.dp)))
        }

        if (s.sessionComplete) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Session complete", color = colors.textPrimary, fontFamily = SerifFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
                    Box(
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .background(colors.textPrimary, RoundedCornerShape(14.dp))
                            .clickable(onClick = onExit)
                            .padding(horizontal = 28.dp, vertical = 13.dp)
                    ) {
                        Text(text = "Done", color = colors.background, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
            return@Column
        }

        val card = s.card ?: return@Column

        Column(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.Center) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(24.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(24.dp))
                    .clickable { viewModel.flip() }
                    .padding(24.dp),
            ) {
                Text(text = card.domainCode, color = colors.textTertiary, fontFamily = MonoFontFamily, fontSize = 10.sp)
                Text(
                    text = card.front,
                    color = colors.textPrimary,
                    fontFamily = SerifFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 19.sp,
                    lineHeight = 26.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
                if (s.flipped) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(1.dp).background(colors.border))
                    Text(text = card.back, color = colors.textPrimary, fontSize = 14.sp, lineHeight = 21.sp)
                }
            }
            Text(
                text = if (s.flipped) "How well did you know this?" else "Tap to reveal",
                color = colors.textTertiary,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        if (s.flipped) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GradeButton("Again", "1 min", colors.accentMuted, Modifier.weight(1f)) { viewModel.grade(Grade.AGAIN) }
                GradeButton("Hard", "2 d", colors.accent, Modifier.weight(1f)) { viewModel.grade(Grade.HARD) }
                GradeButton("Good", "4 d", colors.success, Modifier.weight(1f)) { viewModel.grade(Grade.GOOD) }
                GradeButton("Easy", "8 d", colors.textSecondary, Modifier.weight(1f)) { viewModel.grade(Grade.EASY) }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(58.dp))
        }
    }
}

@Composable
private fun GradeButton(label: String, sub: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(14.dp))
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = sub, color = colors.textTertiary, fontSize = 10.5.sp)
    }
}
