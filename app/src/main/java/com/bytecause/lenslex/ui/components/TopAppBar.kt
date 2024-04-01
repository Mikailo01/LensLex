package com.bytecause.lenslex.ui.components

import androidx.annotation.StringRes
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
    navigationIcon: ImageVector,
    actionIcon: @Composable () -> Unit = {},
    onNavigationIconClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = titleRes)) },
        colors = colors,
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = { onNavigationIconClick() }) {
                Icon(
                    imageVector = navigationIcon,
                    contentDescription = "Localized description"
                )
            }
        },
        actions = {
            actionIcon()
        }
    )
}