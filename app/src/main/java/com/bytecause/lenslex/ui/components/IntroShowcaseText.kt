package com.bytecause.lenslex.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle

@Composable
fun IntroShowcaseText(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        fontStyle = FontStyle.Italic,
        modifier = modifier
    )
}