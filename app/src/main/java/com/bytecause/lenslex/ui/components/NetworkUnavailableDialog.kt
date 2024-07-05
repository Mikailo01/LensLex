package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R

@Composable
fun NetworkUnavailableDialog(
    modifier: Modifier = Modifier,
    text: String,
    onTryAgainClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        title = stringResource(id = R.string.network_unavailable),
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.network_error),
            contentDescription = stringResource(id = R.string.network_unavailable),
            modifier = Modifier.size(128.dp)
        )
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Button(onClick = onTryAgainClick, shape = RectangleShape) {
            Text(text = stringResource(id = R.string.try_again))
        }
    }
}