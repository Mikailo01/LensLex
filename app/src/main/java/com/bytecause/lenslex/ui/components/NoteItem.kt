package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.util.swipeToDismiss

@Composable
fun NoteItem(
    modifier: Modifier = Modifier,
    originalText: String,
    translatedText: String,
    onRemove: () -> Unit
) {
    Column(
        modifier.padding(8.dp).swipeToDismiss { onRemove() }
    ) {
        Text(text = originalText, fontWeight = FontWeight.ExtraBold)
        Text(text = translatedText, fontStyle = FontStyle.Italic)
        Divider(thickness = 2, color = Color.Gray)
    }
}