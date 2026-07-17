package com.architectprep.app.ui.study

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
fun LessonListScreen(
    viewModel: LessonListViewModel,
    onBack: () -> Unit,
    onLessonClick: (String) -> Unit
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "‹",
                        color = colors.textPrimary,
                        fontSize = 22.sp,
                        modifier = Modifier
                            .clickable(onClick = onBack)
                            .padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            text = s.domainCode,
                            color = colors.accent,
                            fontFamily = MonoFontFamily,
                            fontSize = 11.sp
                        )
                        Text(
                            text = s.domainTitle,
                            color = colors.textPrimary,
                            fontFamily = SerifFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp
                        )
                    }
                }
            }
            items(s.lessons) { lesson -> LessonListItem(lesson, onClick = { onLessonClick(lesson.id) }) }
        }
    }
}

@Composable
private fun LessonListItem(lesson: LessonRow, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp, 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Lesson ${lesson.orderIndex}",
                color = colors.textTertiary,
                fontFamily = MonoFontFamily,
                fontSize = 10.5.sp
            )
            Text(
                text = lesson.title,
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = "~${lesson.estMinutes} min read",
                color = colors.textSecondary,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Text(
            text = if (lesson.done) "✓" else "",
            color = colors.success,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
