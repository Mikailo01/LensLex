package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R
import com.bytecause.lenslex.util.gradientBackground


@Composable
fun UserAuthBackgroundExpanded(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
    showNavigateBackButton: Boolean = false,
    backgroundContent: @Composable () -> Unit,
    foregroundContent: @Composable () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    backgroundContent()
                }
                SnackbarHost(
                    hostState = snackBarHostState,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .height(IntrinsicSize.Max)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)
                    )
                    .padding(start = 30.dp, end = 30.dp, top = 30.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                foregroundContent()
            }
        }
        if (showNavigateBackButton) {
            IconButton(onClick = onNavigateBack, modifier = Modifier.align(Alignment.TopStart)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.navigate_back),
                    tint = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

@Composable
@Preview
fun UserAuthBackgroundExpandedPreview() {
    UserAuthBackgroundExpanded(
        snackBarHostState = remember {
            SnackbarHostState()
        },
        backgroundContent = {},
        foregroundContent = {},
        onNavigateBack = {}
    )
}