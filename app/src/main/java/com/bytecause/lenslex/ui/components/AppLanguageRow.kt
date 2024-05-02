package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.util.capital
import java.util.Locale


@Composable
fun AppLanguageRow(
    modifier: Modifier = Modifier,
    langCode: String,
    isChecked: Boolean,
    onLanguageClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth().clickable { if (!isChecked) onLanguageClick() }
    ) {
        Text(text = Locale(langCode).displayLanguage.capital(), modifier = Modifier.weight(1f).padding(10.dp))
        if (isChecked) Image(imageVector = Icons.Filled.Check, contentDescription = "", modifier = Modifier.padding(10.dp))
    }
}