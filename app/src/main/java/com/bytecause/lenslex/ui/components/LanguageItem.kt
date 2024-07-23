package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.ui.interfaces.TranslationOption

@Composable
fun LanguageItem(
    modifier: Modifier = Modifier,
    translationOption: TranslationOption,
    item: SupportedLanguage,
    onItemClick: (TranslationOption) -> Unit,
    onDownloadClick: (String) -> Unit,
    onRemoveClick: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onItemClick(
                    when (translationOption) {
                        is TranslationOption.Origin -> {
                            TranslationOption.Origin(item)
                        }

                        is TranslationOption.Target -> {
                            TranslationOption.Target(item)
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(text = item.langName, modifier = modifier)
            HorizontalDivider(color = MaterialTheme.colorScheme.secondary)
        }

        when {
            item.isDownloaded -> {
                Image(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(id = R.string.delete_language),
                    modifier = modifier
                        .padding(end = 10.dp)
                        .clip(CircleShape)
                        .clickable { onRemoveClick(item.langCode) }
                )
            }

            item.isDownloading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 22.dp)
                        .size(20.dp)
                )
            }

            else -> {
                Image(
                    painter = painterResource(id = R.drawable.baseline_download_24),
                    contentDescription = stringResource(id = R.string.download_language),
                    modifier = modifier
                        .padding(end = 10.dp)
                        .clip(CircleShape)
                        .clickable { onDownloadClick(item.langCode) }
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun LanguageItemPreview() {
    LanguageItem(
        translationOption = TranslationOption.Origin(),
        item = SupportedLanguage(langName = stringResource(id = R.string.preview)),
        onItemClick = {},
        onDownloadClick = {},
        onRemoveClick = {}
    )
}