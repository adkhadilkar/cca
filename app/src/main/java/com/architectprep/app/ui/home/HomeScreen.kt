package com.architectprep.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.architectprep.app.ui.theme.LocalAppColors
import com.architectprep.app.ui.theme.MonoFontFamily
import com.architectprep.app.ui.theme.SerifFontFamily

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
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
                    text = s.trackTitle,
                    color = colors.textPrimary,
                    fontFamily = SerifFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp
                )
                Text(
                    text = "${s.questionCount} questions · ${s.timeLimitMin} min · pass ${s.passScore}/${s.scoreScale}",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            items(s.domains) { row -> DomainCard(row) }
        }
    }
}

@Composable
private fun DomainCard(row: DomainRow) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(18.dp))
            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = row.code,
                color = colors.accent,
                fontFamily = MonoFontFamily,
                fontSize = 11.sp
            )
            Text(
                text = "${row.weightPct}%",
                color = colors.textSecondary,
                fontFamily = MonoFontFamily,
                fontSize = 11.sp
            )
        }
        Text(
            text = row.title,
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = "${row.lessonsTotal} lessons",
            color = colors.textTertiary,
            fontSize = 11.5.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .padding(top = 8.dp)
                .background(colors.neutralLight, RoundedCornerShape(999.dp))
        )
    }
}
