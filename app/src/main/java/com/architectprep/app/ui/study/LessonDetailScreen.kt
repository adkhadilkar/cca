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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.architectprep.app.data.content.LessonBlockDto
import com.architectprep.app.ui.theme.LocalAppColors
import com.architectprep.app.ui.theme.MonoFontFamily
import com.architectprep.app.ui.theme.SerifFontFamily

@Composable
fun LessonDetailScreen(
    viewModel: LessonDetailViewModel,
    onBack: () -> Unit,
    onNavigateToLesson: (String) -> Unit
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
            contentPadding = PaddingValues(20.dp, 16.dp, 20.dp, 24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "‹",
                        color = colors.textPrimary,
                        fontSize = 22.sp,
                        modifier = Modifier.clickable(onClick = onBack)
                    )
                    Text(
                        text = "${s.domainCode} · Lesson ${s.indexInDomain} of ${s.totalInDomain}",
                        color = colors.textTertiary,
                        fontFamily = MonoFontFamily,
                        fontSize = 11.sp
                    )
                    Box(modifier = Modifier.height(22.dp))
                }

                val fraction = if (s.totalInDomain == 0) 0f else s.indexInDomain.toFloat() / s.totalInDomain
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .padding(top = 16.dp, bottom = 20.dp)
                        .background(colors.neutralLight, RoundedCornerShape(2.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction.coerceIn(0f, 1f))
                            .height(4.dp)
                            .background(colors.accent, RoundedCornerShape(2.dp))
                    )
                }

                Text(
                    text = s.domainTitle.uppercase(),
                    color = colors.accent,
                    fontFamily = MonoFontFamily,
                    fontSize = 11.sp
                )
                Text(
                    text = s.title,
                    color = colors.textPrimary,
                    fontFamily = SerifFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    text = "~${s.estMinutes} min read",
                    color = colors.textTertiary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                )
            }

            items(s.body) { block -> LessonBlockView(block) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (s.prevLessonId != null) {
                        SecondaryButton(text = "‹ Previous", modifier = Modifier.weight(1f)) {
                            onNavigateToLesson(s.prevLessonId)
                        }
                    }
                    PrimaryButton(
                        text = if (s.done) "Marked done ✓" else "Mark done ›",
                        modifier = Modifier.weight(1f)
                    ) { viewModel.toggleDone() }
                }
                if (s.nextLessonId != null) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                        SecondaryButton(text = "Next ›", modifier = Modifier.weight(1f)) {
                            onNavigateToLesson(s.nextLessonId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonBlockView(block: LessonBlockDto) {
    val colors = LocalAppColors.current
    when (block.type) {
        "text" -> Text(
            text = block.value,
            color = colors.textPrimary,
            fontSize = 14.5.sp,
            lineHeight = 22.sp,
            modifier = Modifier.padding(top = 12.dp)
        )

        "code" -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .background(colors.textPrimary, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Text(
                text = block.value,
                color = colors.accentLight,
                fontFamily = MonoFontFamily,
                fontSize = 12.sp,
                lineHeight = 19.sp
            )
        }

        "callout" -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .background(colors.accentLight.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Text(
                text = (block.variant ?: "callout").replace("-", " ").uppercase(),
                color = colors.accentMuted,
                fontFamily = MonoFontFamily,
                fontSize = 10.sp
            )
            Text(
                text = block.value,
                color = colors.textPrimary,
                fontSize = 13.5.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        "image" -> Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            AsyncImage(
                model = "file:///android_asset/content/${block.path}",
                contentDescription = block.value,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(14.dp))
                    .padding(8.dp)
            )
            Text(
                text = block.value,
                color = colors.textTertiary,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        else -> Unit
    }
}

@Composable
private fun PrimaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Box(
        modifier = modifier
            .background(colors.textPrimary, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = colors.background, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun SecondaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Box(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(14.dp))
            .border(1.5.dp, colors.textPrimary, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}
