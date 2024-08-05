package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.ui.interfaces.TranslationOption

@Composable
fun LanguageDialog(
    modifier: Modifier = Modifier,
    filterText: String,
    translationOption: TranslationOption,
    lazyListContent: List<SupportedLanguage>,
    onFilterTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (TranslationOption) -> Unit,
    onDownload: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    Dialog(
        title = stringResource(id = R.string.choose_language),
        onDismiss = { onDismiss() },
        modifier = modifier
    ) {
        TextField(
            value = filterText,
            onValueChange = onFilterTextChange,
            label = { Text(text = stringResource(id = R.string.filter_language)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_filter_list_alt_24),
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (filterText.isNotBlank()) {
                    IconButton(
                        onClick = { onFilterTextChange("") },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = stringResource(id = R.string.clear_text)
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors()
                .copy(unfocusedContainerColor = MaterialTheme.colorScheme.inversePrimary),
            modifier = Modifier.fillMaxWidth()
        )
        LazyColumn {
            items(lazyListContent, key = { item -> item.langCode }) { item ->
                LanguageItem(
                    modifier = Modifier.padding(10.dp),
                    translationOption = translationOption,
                    item = item,
                    onItemClick = {
                        onConfirm(it)
                    },
                    onDownloadClick = { langCode ->
                        onDownload(langCode)
                    },
                    onRemoveClick = { langCode ->
                        onRemove(langCode)
                    }
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun LanguageDialogPreview() {
    LanguageDialog(
        lazyListContent = listOf(
            SupportedLanguage(langCode = "cs", langName = "Czech"),
            SupportedLanguage(langCode = "en", langName = "English")
        ),
        translationOption = TranslationOption.Origin(),
        filterText = "",
        onFilterTextChange = {},
        onDismiss = {},
        onConfirm = {},
        onDownload = {},
        onRemove = {}
    )
}