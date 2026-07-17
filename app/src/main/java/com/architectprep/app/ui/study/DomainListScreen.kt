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
fun DomainListScreen(
    viewModel: DomainListViewModel,
    onDomainClick: (String) -> Unit,
    onGuideClick: () -> Unit,
    onGlossaryClick: () -> Unit
) {
    val domains by viewModel.domains.collectAsState()
    val colors = LocalAppColors.current

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val rows = domains
        if (rows == null) {
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
                    text = "Study",
                    color = colors.textPrimary,
                    fontFamily = SerifFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 26.sp
                )
            }
            items(rows) { row -> DomainProgressCard(row, onClick = { onDomainClick(row.id) }) }
            item { NavCard(icon = "📋", title = "Exam guide", subtitle = "Format, scoring & what to expect on the day", onClick = onGuideClick) }
            item { NavCard(icon = "🔎", title = "Glossary", subtitle = "Search key terms", onClick = onGlossaryClick) }
        }
    }
}

@Composable
private fun DomainProgressCard(row: DomainProgressRow, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val fraction = if (row.lessonsTotal == 0) 0f else row.lessonsDone.toFloat() / row.lessonsTotal
    val barColor = if (fraction >= 1f) colors.success else colors.accent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(20.dp))
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp, 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${row.code} · ${row.weightPct}% of exam",
                color = colors.accent,
                fontFamily = MonoFontFamily,
                fontSize = 11.sp
            )
            Text(
                text = "${row.lessonsDone}/${row.lessonsTotal}",
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
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = row.summary,
            color = colors.textSecondary,
            fontSize = 12.5.sp,
            modifier = Modifier.padding(top = 3.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .padding(top = 12.dp)
                .background(colors.neutralLight, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(5.dp)
                    .background(barColor, RoundedCornerShape(3.dp))
            )
        }
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
            .padding(14.dp, 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = icon, fontSize = 18.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = subtitle, color = colors.textSecondary, fontSize = 12.sp)
        }
        Text(text = "›", color = colors.accent, fontSize = 18.sp)
    }
}
