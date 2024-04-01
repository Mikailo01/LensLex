package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.models.SupportedLanguage

@Composable
fun LanguageDialog(
    modifier: Modifier = Modifier,
    lazyListContent: List<SupportedLanguage>,
    onDismiss: () -> Unit,
    onConfirm: (SupportedLanguage) -> Unit,
    onDownload: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Choose language")
            }
            Divider(thickness = 1, color = Color.Gray)
            LazyColumn {
                items(lazyListContent, key = { item -> item.langCode }) { item ->
                    LanguageItem(
                        modifier = modifier.padding(10.dp),
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
}