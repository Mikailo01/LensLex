package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.interfaces.TranslationOption
import com.bytecause.lenslex.util.TestTags

@Composable
fun LanguagePreferences(
    modifier: Modifier = Modifier,
    originLangName: String,
    targetLangName: String,
    isLoading: Boolean = false,
    onClick: (TranslationOption) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundedText(
            modifier = modifier.padding(start = 5.dp, end = 5.dp),
            text = originLangName,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            isLoading = isLoading,
            onClick = { onClick(TranslationOption.Origin()) }
        )
        Text(text = "->")
        RoundedText(
            modifier = modifier
                .padding(start = 5.dp, end = 5.dp)
                .testTag(TestTags.SELECT_LANG_OPTION),
            text = targetLangName,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            isLoading = isLoading,
            onClick = { onClick(TranslationOption.Target()) }
        )
    }
}

@Composable
@Preview(showBackground = true)
fun LanguagePreferencesPreview() {
    LanguagePreferences(
        originLangName = stringResource(id = R.string.preview),
        targetLangName = stringResource(id = R.string.preview)
    ) { }
}