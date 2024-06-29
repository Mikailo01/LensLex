package com.bytecause.lenslex.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

val LocalOrientationMode = @Composable {
    val configuration = LocalConfiguration.current

    if (configuration.screenWidthDp < configuration.screenHeightDp) OrientationMode.Portrait
    else OrientationMode.Landscape
}

sealed interface OrientationMode {
    data object Portrait : OrientationMode
    data object Landscape : OrientationMode
}