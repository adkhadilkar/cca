package com.architectprep.app.ui.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.architectprep.app.data.prefs.ThemePref
import com.architectprep.app.ui.theme.LocalAppColors
import com.architectprep.app.ui.theme.MonoFontFamily
import com.architectprep.app.ui.theme.SerifFontFamily
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit, onRedoOnboarding: () -> Unit) {
    val prefs by viewModel.userPrefs.collectAsState()
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showDatePicker by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val p = prefs
        if (p == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            return@Box
        }

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "‹", color = colors.textPrimary, fontSize = 22.sp, modifier = Modifier.clickable(onClick = onBack).padding(end = 12.dp))
                    Text(text = "Settings", color = colors.textPrimary, fontFamily = SerifFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
                }

                SectionLabel("Appearance", topPadding = 20.dp)
                SettingsCard {
                    Text(text = "Theme", color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp).background(colors.neutralLight, RoundedCornerShape(12.dp)).padding(4.dp)
                    ) {
                        ThemeOption("Light", ThemePref.LIGHT, p.theme, Modifier.weight(1f)) { viewModel.setTheme(it) }
                        ThemeOption("Dark", ThemePref.DARK, p.theme, Modifier.weight(1f)) { viewModel.setTheme(it) }
                        ThemeOption("System", ThemePref.SYSTEM, p.theme, Modifier.weight(1f)) { viewModel.setTheme(it) }
                    }
                }

                SectionLabel("Study")
                SettingsCard(padding = 0.dp) {
                    SettingsRow("Exam date", p.examDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                    } ?: "Not set", onClick = { showDatePicker = true })
                    SettingsRow("Daily goal", "${p.dailyGoalMinutes} min", onClick = { viewModel.setDailyGoalMinutes(cycleGoal(p.dailyGoalMinutes)) })
                    SettingsRow("New cards per day", "${p.dailyCardLimit}", onClick = { viewModel.setDailyCardLimit(cycleCardLimit(p.dailyCardLimit)) }, last = true)
                }

                SectionLabel("Data")
                SettingsCard(padding = 0.dp) {
                    SettingsRow("Export progress", "›", onClick = {
                        scope.launch {
                            val json = viewModel.exportProgressJson()
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, json)
                                putExtra(Intent.EXTRA_SUBJECT, "Architect Prep progress export")
                            }
                            context.startActivity(Intent.createChooser(intent, "Export progress"))
                        }
                    })
                    SettingsRow("Reset all progress", "›", danger = true, onClick = { showResetConfirm = true }, last = true)
                }

                SectionLabel("Onboarding")
                SettingsCard(padding = 0.dp) {
                    SettingsRow("Redo onboarding", "›", onClick = onRedoOnboarding, last = true)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                        .background(colors.neutralLight, RoundedCornerShape(16.dp))
                        .padding(14.dp, 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All content and progress are stored on this device. The app never connects to the internet.",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }

                Text(
                    text = "Architect Prep v0.1.0",
                    color = colors.textTertiary,
                    fontFamily = MonoFontFamily,
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        if (showDatePicker) {
            val state = rememberDatePickerState(initialSelectedDateMillis = p.examDateMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { viewModel.setExamDate(state.selectedDateMillis); showDatePicker = false }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
            ) {
                DatePicker(state = state)
            }
        }

        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = { Text("Reset all progress?") },
                text = { Text("This clears lesson completion, practice history, flashcard schedules, and mock exam results. Content stays. This can't be undone.") },
                confirmButton = {
                    TextButton(onClick = { showResetConfirm = false; viewModel.resetProgress {} }) { Text("Reset") }
                },
                dismissButton = { TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") } }
            )
        }
    }
}

private fun cycleGoal(current: Int): Int {
    val steps = listOf(10, 15, 20, 25, 30, 45, 60)
    val idx = steps.indexOf(current).takeIf { it >= 0 } ?: 2
    return steps[(idx + 1) % steps.size]
}

private fun cycleCardLimit(current: Int): Int {
    val steps = listOf(5, 10, 15, 20, 30, 50)
    val idx = steps.indexOf(current).takeIf { it >= 0 } ?: 3
    return steps[(idx + 1) % steps.size]
}

@Composable
private fun SectionLabel(text: String, topPadding: androidx.compose.ui.unit.Dp = 18.dp) {
    val colors = LocalAppColors.current
    Text(
        text = text.uppercase(),
        color = colors.textTertiary,
        fontFamily = MonoFontFamily,
        fontSize = 10.sp,
        modifier = Modifier.padding(top = topPadding, bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(padding: androidx.compose.ui.unit.Dp = 16.dp, content: @Composable ColumnScope.() -> Unit) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(20.dp))
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .padding(padding),
        content = content
    )
}

@Composable
private fun ThemeOption(label: String, value: ThemePref, current: ThemePref, modifier: Modifier, onClick: (ThemePref) -> Unit) {
    val colors = LocalAppColors.current
    val selected = value == current
    Box(
        modifier = modifier
            .background(if (selected) colors.surface else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(9.dp))
            .clickable { onClick(value) }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = if (selected) colors.textPrimary else colors.textTertiary, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, fontSize = 12.5.sp)
    }
}

@Composable
private fun SettingsRow(label: String, value: String, danger: Boolean = false, last: Boolean = false, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(18.dp, 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = if (danger) colors.accentMuted else colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = value, color = colors.accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
        if (!last) Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
            Box(modifier = Modifier.fillMaxWidth().background(colors.neutralLight).padding(top = 0.5.dp))
        }
    }
}
