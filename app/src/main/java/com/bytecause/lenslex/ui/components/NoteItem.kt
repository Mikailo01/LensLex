package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R
import com.bytecause.lenslex.util.swipeToDismiss

@Composable
fun NoteItem(
    modifier: Modifier = Modifier,
    originalText: String,
    translatedText: String,
    onRemove: () -> Unit,
    onClick: (String) -> Unit
) {
    Column(
        modifier
            .padding(8.dp)
            .swipeToDismiss { onRemove() }
    ) {
        Text(
            text = originalText,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.clickable {
                onClick(originalText)
            })
        Text(
            text = translatedText,
            fontStyle = FontStyle.Italic,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.clickable {
            onClick(translatedText)
        })
        HorizontalDivider(thickness = 2.dp, color = Color.Gray)
    }
}

@Composable
@Preview(showBackground = true)
fun NoteItemPreview() {
    NoteItem(
        originalText = stringResource(id = R.string.preview),
        translatedText = stringResource(id = R.string.preview),
        onRemove = {},
        onClick = {}
    )
}