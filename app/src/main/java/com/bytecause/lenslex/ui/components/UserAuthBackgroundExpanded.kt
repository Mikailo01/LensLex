package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.util.gradientBackground


@Composable
fun UserAuthBackgroundExpanded(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState? = null,
    backgroundContent: @Composable () -> Unit,
    foregroundContent: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBackground),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                backgroundContent()
            }
            if (snackBarHostState != null) {
                SnackbarHost(
                    hostState = snackBarHostState,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .height(IntrinsicSize.Max)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)
                )
                .padding(start = 30.dp, end = 30.dp, top = 30.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            foregroundContent()
        }
    }
}

@Composable
@Preview
fun UserAuthBackgroundExpandedPreview() {
    UserAuthBackgroundExpanded(backgroundContent = {}, foregroundContent = {})
}