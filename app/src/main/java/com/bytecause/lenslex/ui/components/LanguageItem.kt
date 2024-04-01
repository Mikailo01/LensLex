package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.bytecause.lenslex.R
import com.bytecause.lenslex.models.SupportedLanguage

@Composable
fun LanguageItem(
    modifier: Modifier = Modifier,
    item: SupportedLanguage,
    onItemClick: (SupportedLanguage) -> Unit,
    onDownloadClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) }
    ) {
        Text(text = item.langName, modifier = modifier)
        Image(
            painter = painterResource(id = R.drawable.baseline_download_24),
            contentDescription = "Download language",
            modifier = modifier.clip(CircleShape).clickable { onDownloadClick() }
        )
    }
}