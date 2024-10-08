package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R

@Composable
fun RoundedText(
    modifier: Modifier = Modifier,
    text: String = "",
    containerColor: Color,
    contentColor: Color = Color.LightGray,
    borderColor: Color = Color.Black,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(60)
    Card(
        modifier = modifier
            .wrapContentSize()
            .border(shape = shape, width = 1.dp, color = borderColor)
            .clip(shape)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        shape = shape,
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = contentColor,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.W800,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun RoundedTextPreview() {
    RoundedText(
        text = stringResource(id = R.string.preview),
        containerColor = Color.White,
    ) { }
}