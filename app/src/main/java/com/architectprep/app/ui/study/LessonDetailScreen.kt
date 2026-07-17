package com.architectprep.app.ui.study

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
                        .padding(top = 16.dp, bottom = 20.dp)
                        .height(4.dp)
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
                // Design screen 03: exactly two buttons side by side. "Mark done"
                // also advances (arrow affordance); once done it turns into a
                // plain forward control.
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SecondaryButton(
                        text = "‹ Previous",
                        enabled = s.prevLessonId != null,
                        modifier = Modifier.weight(1f)
                    ) { s.prevLessonId?.let(onNavigateToLesson) }
                    PrimaryButton(
                        text = when {
                            !s.done -> "Mark done ›"
                            s.nextLessonId != null -> "Next ›"
                            else -> "Finish ›"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        if (!s.done) viewModel.markDone()
                        if (s.nextLessonId != null) onNavigateToLesson(s.nextLessonId) else if (s.done) onBack()
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
                .background(colors.codeBackground, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Text(
                text = block.value,
                color = colors.codeText,
                fontFamily = MonoFontFamily,
                fontSize = 12.sp,
                lineHeight = 19.sp
            )
        }

        "callout" -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .background(colors.calloutBackground, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Text(
                text = (block.variant ?: "callout").replace("-", " ").uppercase(),
                color = colors.calloutLabel,
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

        "image" -> {
            var showFullscreen by remember(block.path) { mutableStateOf(false) }
            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                AsyncImage(
                    model = "file:///android_asset/content/${block.path}",
                    contentDescription = block.value,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                        .clickable { showFullscreen = true }
                        .padding(8.dp)
                )
                Text(
                    text = "${block.value}  ·  Tap to zoom 🔍",
                    color = colors.textTertiary,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            if (showFullscreen) {
                ZoomableImageDialog(
                    path = block.path.orEmpty(),
                    caption = block.value,
                    onDismiss = { showFullscreen = false }
                )
            }
        }

        else -> Unit
    }
}

/** Full-screen image viewer with pinch-zoom and pan. White canvas — the bundled diagrams are dark-on-transparent. */
@Composable
private fun ZoomableImageDialog(path: String, caption: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 6f)
                        offset = if (scale > 1f) offset + pan else Offset.Zero
                    }
                }
        ) {
            AsyncImage(
                model = "file:///android_asset/content/$path",
                contentDescription = caption,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
            )
            Text(
                text = caption,
                color = Color(0xFF6B6555),
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .background(Color(0xFFF0EBDD), CircleShape)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✕", color = Color(0xFF29261F), fontSize = 16.sp)
            }
        }
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
private fun SecondaryButton(text: String, enabled: Boolean = true, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val contentColor = if (enabled) colors.textPrimary else colors.textTertiary
    Box(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(14.dp))
            .border(1.5.dp, if (enabled) colors.textPrimary else colors.border, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = contentColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}
