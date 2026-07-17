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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.architectprep.app.data.db.entity.MockAttemptEntity
import com.architectprep.app.ui.theme.LocalAppColors
import com.architectprep.app.ui.theme.MonoFontFamily
import com.architectprep.app.ui.theme.SerifFontFamily
import kotlinx.coroutines.launch

@Composable
fun ExamHomeScreen(
    viewModel: ExamHomeViewModel,
    onStart: (String) -> Unit,
    onResume: (String) -> Unit,
    onViewResults: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalAppColors.current
    val scope = rememberCoroutineScope()

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
                    text = "Mock exam",
                    color = colors.textPrimary,
                    fontFamily = SerifFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 26.sp
                )
                Text(
                    text = "${s.questionCount} questions · ${s.timeLimitMin} min · pass ${s.passScore}/1000",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            item {
                if (s.inProgress != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.accent, RoundedCornerShape(16.dp))
                            .clickable { onResume(s.inProgress.id) }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Resume exam in progress ›", color = colors.background, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.textPrimary, RoundedCornerShape(16.dp))
                            .clickable {
                                scope.launch { onStart(viewModel.startNewAttempt()) }
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Start mock exam ›", color = colors.background, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
            if (s.history.isNotEmpty()) {
                item {
                    Text(
                        text = "SCORE HISTORY",
                        color = colors.textTertiary,
                        fontFamily = MonoFontFamily,
                        fontSize = 10.5.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(s.history) { attempt -> AttemptRow(attempt, s.passScore, onClick = { onViewResults(attempt.id) }) }
            }
        }
    }
}

@Composable
private fun AttemptRow(attempt: MockAttemptEntity, passScore: Int, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val passed = (attempt.score ?: 0) >= passScore
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(14.dp))
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "${attempt.score}", color = if (passed) colors.success else colors.accentMuted, fontFamily = SerifFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        Text(text = if (passed) "PASS" else "FAIL", color = if (passed) colors.success else colors.accentMuted, fontFamily = MonoFontFamily, fontSize = 11.sp)
    }
}
