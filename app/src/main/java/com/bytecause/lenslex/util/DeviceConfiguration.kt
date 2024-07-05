package com.bytecause.lenslex.util

import android.content.res.Configuration

fun getOrientationMode(configuration: Configuration): OrientationMode =
    if (configuration.screenWidthDp < configuration.screenHeightDp) OrientationMode.Portrait
    else OrientationMode.Landscape

sealed interface OrientationMode {
    data object Portrait : OrientationMode
    data object Landscape : OrientationMode
}