package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

sealed interface ImageResource {
    data class Painter(val painter: androidx.compose.ui.graphics.painter.Painter) : ImageResource
    data class ImageVector(val imageVector: androidx.compose.ui.graphics.vector.ImageVector) :
        ImageResource
}

@Composable
fun LoginOptionRow(
    modifier: Modifier = Modifier,
    optionImage: ImageResource,
    text: String,
    contentDescription: String,
    onSignOptionClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable {
                onSignOptionClick()
            }
    ) {
        when (optionImage) {
            is ImageResource.Painter -> {
                Image(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterVertically),
                    painter = optionImage.painter,
                    contentDescription = contentDescription
                )
            }

            is ImageResource.ImageVector -> {
                Image(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterVertically),
                    imageVector = optionImage.imageVector,
                    contentDescription = contentDescription
                )
            }
        }
        Text(modifier = Modifier.align(Alignment.CenterVertically), text = text)
    }
}