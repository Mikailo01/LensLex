package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R

@Composable
fun LanguagePreferences(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundedText(
            modifier = modifier.padding(start = 5.dp, end = 5.dp),
            text = "English",
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            onClick = { }
        )
        Text(text = "->")
        RoundedText(
            modifier = modifier.padding(start = 5.dp, end = 5.dp),
            text = text,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = { onClick() }
        )
    }
}

@Composable
@Preview(showBackground = true)
fun LanguagePreferencesPreview() {
    LanguagePreferences(text = stringResource(id = R.string.preview)) { }
}