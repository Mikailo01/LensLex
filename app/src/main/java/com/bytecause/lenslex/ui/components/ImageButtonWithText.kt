package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ImageButtonWithText(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    text: String,
    textColor: Color,
    contentDescription: String?,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        onClick = { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                imageVector = icon,
                contentDescription = contentDescription,
                colorFilter = ColorFilter.tint(iconColor)
            )
            Text(text = text, color = textColor)
        }
    }
}