package com.architectprep.app.ui.reference

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.architectprep.app.data.db.entity.GlossaryTermEntity
import com.architectprep.app.ui.theme.LocalAppColors
import com.architectprep.app.ui.theme.MonoFontFamily
import com.architectprep.app.ui.theme.SerifFontFamily

@Composable
fun GlossaryScreen(viewModel: GlossaryViewModel, onBack: () -> Unit) {
    val query by viewModel.query.collectAsState()
    val terms by viewModel.terms.collectAsState()
    val colors = LocalAppColors.current

    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        Column(modifier = Modifier.padding(20.dp, 16.dp, 20.dp, 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "‹",
                    color = colors.textPrimary,
                    fontSize = 22.sp,
                    modifier = Modifier.clickable(onClick = onBack).padding(end = 12.dp)
                )
                Text(
                    text = "Glossary",
                    color = colors.textPrimary,
                    fontFamily = SerifFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp
                )
            }
            TextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("Search terms…") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedIndicatorColor = colors.border,
                    unfocusedIndicatorColor = colors.border
                ),
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp, 6.dp, 20.dp, 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(terms, key = { it.id }) { term -> GlossaryCard(term) }
        }
    }
}

@Composable
private fun GlossaryCard(term: GlossaryTermEntity) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = term.term, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
            Text(text = term.category, color = colors.accent, fontFamily = MonoFontFamily, fontSize = 10.sp)
        }
        Text(
            text = term.definition,
            color = colors.textSecondary,
            fontSize = 12.5.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
