package com.bytecause.lenslex.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.theme.LensLexTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary
    ),
    navigationIcon: ImageVector? = null,
    actionIcons: List<@Composable () -> Unit> = emptyList(),
    onNavigationIconClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = titleRes), style = LensLexTypography.titleMedium) },
        colors = colors,
        modifier = modifier,
        navigationIcon = {
            if (navigationIcon != null) {
                IconButton(onClick = { onNavigationIconClick() }) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "Localized description",
                        tint = colors.titleContentColor
                    )
                }
            }
        },
        actions = {
            actionIcons.forEach {
                it()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun TopAppBarPreview() {
    TopAppBar(titleRes = R.string.preview, navigationIcon = Icons.AutoMirrored.Filled.ArrowBack) { }
}