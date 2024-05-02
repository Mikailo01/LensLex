package com.bytecause.lenslex.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import com.bytecause.lenslex.ui.theme.blue
import com.bytecause.lenslex.ui.theme.purple


val gradientBackground = Brush.verticalGradient(
    0.2f to purple,
    1.0f to blue,
    startY = 0.0f,
    endY = 1500f
)

val LocalOrientationMode = @Composable {
    val configuration = LocalConfiguration.current

    if (configuration.screenWidthDp < configuration.screenHeightDp) OrientationMode.Portrait
    else OrientationMode.Landscape
}

sealed interface OrientationMode {
    data object Portrait : OrientationMode
    data object Landscape : OrientationMode
}
