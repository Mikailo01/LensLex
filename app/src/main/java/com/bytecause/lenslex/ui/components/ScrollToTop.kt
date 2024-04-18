package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R

@Composable
fun ScrollToTop(
    modifier: Modifier = Modifier,
    onScrollUp: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        FloatingActionButton(
            modifier = Modifier
                .padding(16.dp)
                .size(50.dp)
                .align(Alignment.BottomStart),
            onClick = onScrollUp,
            containerColor = White,
            contentColor = Black
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_arrow_up_24),
                contentDescription = "go to top"
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ScrollToTopPreview() {
    ScrollToTop {}
}