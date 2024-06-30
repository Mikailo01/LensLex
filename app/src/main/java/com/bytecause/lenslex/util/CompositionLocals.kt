package com.bytecause.lenslex.util

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

fun getOrientationMode(configuration: Configuration): OrientationMode =
    if (configuration.screenWidthDp < configuration.screenHeightDp) OrientationMode.Portrait
    else OrientationMode.Landscape

/*val LocalOrientationMode = @Composable {
    val configuration = LocalConfiguration.current

    if (configuration.screenWidthDp < configuration.screenHeightDp) OrientationMode.Portrait
    else OrientationMode.Landscape
}*/

sealed interface OrientationMode {
    data object Portrait : OrientationMode
    data object Landscape : OrientationMode
}