package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun IndeterminateCircularIndicator(modifier: Modifier = Modifier, isShowed: Boolean) {
    if (!isShowed) return

    Box(modifier = modifier) {
        Column(modifier = modifier) {
            CircularProgressIndicator(
                modifier = Modifier.size(65.dp).align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Text(text = "Processing")
        }
    }
}