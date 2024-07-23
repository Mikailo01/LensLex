package com.bytecause.lenslex.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R

@Composable
fun RowItem(
    modifier: Modifier = Modifier,
    @DrawableRes leadingIconId: Int,
    @StringRes contentDescription: Int,
    @StringRes text: Int,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    spacing: Dp = 10.dp,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clickable {
                onClick()
            },
        horizontalArrangement = horizontalArrangement,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = leadingIconId),
                contentDescription = stringResource(id = contentDescription),
                modifier = Modifier.padding(start = 10.dp, top = 10.dp, bottom = 10.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
            )
            Text(
                text = stringResource(id = text),
                modifier = Modifier.padding(end = 10.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun RowItemPreview() {
    RowItem(leadingIconId = R.drawable.id, contentDescription = 0, text = R.string.preview) { }
}