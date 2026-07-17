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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.architectprep.app.ui.theme.LocalAppColors
import com.architectprep.app.ui.theme.MonoFontFamily
import com.architectprep.app.ui.theme.SerifFontFamily

@Composable
fun PracticeHomeScreen(
    viewModel: PracticeHomeViewModel,
    onDomainClick: (String) -> Unit,
    onFlashcardsClick: () -> Unit
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
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Practice",
                    color = colors.textPrimary,
                    fontFamily = SerifFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 26.sp
                )
            }
            item {
                NavCard(
                    icon = "◫",
                    title = "Flashcards",
                    subtitle = if (s.flashcardsDue > 0) "${s.flashcardsDue} due for review" else "All caught up",
                    onClick = onFlashcardsClick
                )
            }
            item {
                Text(
                    text = "PRACTICE BY DOMAIN",
                    color = colors.textTertiary,
                    fontFamily = MonoFontFamily,
                    fontSize = 10.5.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            items(s.domains) { row -> PracticeDomainCard(row, onClick = { onDomainClick(row.id) }) }
        }
    }
}

@Composable
private fun PracticeDomainCard(row: PracticeDomainRow, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val accuracy = if (row.attempted == 0) null else (row.correct * 100 / row.attempted)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(18.dp))
            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
            .clickable(enabled = row.questionCount > 0, onClick = onClick)
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "${row.code} · ${row.weightPct}% of exam", color = colors.accent, fontFamily = MonoFontFamily, fontSize = 11.sp)
            Text(
                text = accuracy?.let { "$it% accuracy" } ?: "not started",
                color = colors.textSecondary,
                fontFamily = MonoFontFamily,
                fontSize = 11.sp
            )
        }
        Text(
            text = row.title,
            color = colors.textPrimary,
            fontFamily = SerifFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = if (row.questionCount > 0) "${row.questionCount} questions" else "No questions yet",
            color = colors.textTertiary,
            fontSize = 11.5.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun NavCard(icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.neutralLight, RoundedCornerShape(20.dp))
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = icon, fontSize = 20.sp, color = colors.accent)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = subtitle, color = colors.textSecondary, fontSize = 12.sp)
        }
        Text(text = "›", color = colors.accent, fontSize = 18.sp)
    }
}
