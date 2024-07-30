package com.bytecause.lenslex.util

import androidx.compose.ui.graphics.Brush
import com.bytecause.lenslex.ui.theme.blue
import com.bytecause.lenslex.ui.theme.purple

val gradientBackground = Brush.verticalGradient(
    0.2f to purple,
    1.0f to blue,
    startY = 0.0f,
    endY = 1500f
)

const val introShowcaseBackgroundAlpha = 0.98f
