package com.architectprep.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.architectprep.app.domain.ExamDate
import com.architectprep.app.ui.theme.LocalAppColors
import com.architectprep.app.ui.theme.MonoFontFamily
import com.architectprep.app.ui.theme.SerifFontFamily
import java.time.format.DateTimeFormatter

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun OnboardingScreen(viewModel: OnboardingViewModel, onDone: () -> Unit) {
    val colors = LocalAppColors.current
    var examDateMillis by remember { mutableStateOf<Long?>(null) }
    var dailyGoal by remember { mutableStateOf(20) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(colors.background).padding(24.dp, 36.dp, 24.dp, 28.dp)) {
        Box(
            modifier = Modifier.size(52.dp).background(colors.accent, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "A", color = colors.background, fontFamily = SerifFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
        Text(
            text = "Prepare for the Claude Certified Architect exams.",
            color = colors.textPrimary,
            fontFamily = SerifFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            modifier = Modifier.padding(top = 20.dp)
        )
        Text(
            text = "Lessons, practice questions, flashcards and full mock exams — everything stored on your device, no connection needed.",
            color = colors.textSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = "CHOOSE YOUR TRACK",
            color = colors.textTertiary,
            fontFamily = MonoFontFamily,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 28.dp, bottom = 10.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(20.dp))
                .border(2.dp, colors.accent, RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "CCAR-F", color = colors.accent, fontFamily = MonoFontFamily, fontSize = 11.sp)
                Text(text = "Architect — Foundations", color = colors.textPrimary, fontFamily = SerifFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.padding(top = 2.dp))
                Text(text = "60 questions · 120 min · 5 domains", color = colors.textSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }
            Box(modifier = Modifier.size(22.dp).background(colors.accent, CircleShape), contentAlignment = Alignment.Center) {
                Text(text = "✓", color = colors.background, fontSize = 13.sp)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .background(colors.surface, RoundedCornerShape(20.dp))
                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "CCAR-P", color = colors.textTertiary, fontFamily = MonoFontFamily, fontSize = 11.sp)
                Text(text = "Architect — Professional", color = colors.textTertiary, fontFamily = SerifFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.padding(top = 2.dp))
                Text(text = "Coming soon — no content pack yet", color = colors.textTertiary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(colors.surface, RoundedCornerShape(16.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .clickable { showDatePicker = true }
                    .padding(14.dp)
            ) {
                Text(text = "EXAM DATE", color = colors.textTertiary, fontFamily = MonoFontFamily, fontSize = 10.sp)
                Text(
                    text = examDateMillis?.let {
                        ExamDate.toLocalDate(it).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                    } ?: "Not set",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(colors.surface, RoundedCornerShape(16.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text(text = "DAILY GOAL", color = colors.textTertiary, fontFamily = MonoFontFamily, fontSize = 10.sp)
                Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "−", color = colors.accent, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.clickable { if (dailyGoal > 5) dailyGoal -= 5 })
                    Text(text = "$dailyGoal min", color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
                    Text(text = "+", color = colors.accent, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.clickable { if (dailyGoal < 120) dailyGoal += 5 })
                }
            }
        }

        Box(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.textPrimary, RoundedCornerShape(16.dp))
                .clickable { viewModel.complete(examDateMillis, dailyGoal, onDone) }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Get started ›", color = colors.background, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = examDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    examDateMillis = state.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = state)
        }
    }
}
