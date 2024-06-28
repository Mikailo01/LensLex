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
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.ui.interfaces.TranslationOption

@Composable
fun LanguageDialog(
    modifier: Modifier = Modifier,
    translationOption: TranslationOption,
    lazyListContent: List<SupportedLanguage>,
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
        onDismiss = {},
        onConfirm = {},
        onDownload = {},
        onRemove = {}
    )
}