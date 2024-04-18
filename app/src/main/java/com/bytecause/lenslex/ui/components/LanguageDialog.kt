package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R
import com.bytecause.lenslex.models.SupportedLanguage

@Composable
fun LanguageDialog(
    modifier: Modifier = Modifier,
    lazyListContent: List<SupportedLanguage>,
    onDismiss: () -> Unit,
    onConfirm: (SupportedLanguage) -> Unit,
    onDownload: () -> Unit
) {
    Dialog(
        title = stringResource(id = R.string.choose_language),
        onDismiss = { onDismiss() },
        modifier = modifier
    ) {
        LazyColumn {
            items(lazyListContent, key = { item -> item.langCode }) { item ->
                LanguageItem(
                    modifier = Modifier.padding(10.dp),
                    item = item,
                    onItemClick = {
                        onConfirm(it)
                    },
                    onDownloadClick = {
                        onDownload()
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
        onDismiss = {},
        onConfirm = {}) {}
}