package com.bytecause.lenslex.util

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

fun String.capital(): String = this.replaceFirstChar { it.uppercase() }

@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

private fun Dp.px(density: Density): Float =
    with(density) { toPx() }

fun Modifier.shadowCustom(
    color: Color = Color.Black,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    blurRadius: Dp = 0.dp
) = composed {
    val paint: Paint = remember { Paint() }
    val blurRadiusPx = blurRadius.px(LocalDensity.current)
    val maskFilter = remember {
        BlurMaskFilter(blurRadiusPx, BlurMaskFilter.Blur.NORMAL)
    }
    drawBehind {
        drawIntoCanvas { canvas ->
            val frameworkPaint = paint.asFrameworkPaint()
            if (blurRadius != 0.dp) {
                frameworkPaint.maskFilter = maskFilter
            }
            frameworkPaint.color = color.toArgb()

            val leftPixel = offsetX.toPx() - 20
            val topPixel = offsetY.toPx()
            val rightPixel = size.width + leftPixel + 20
            val bottomPixel = size.height + topPixel - 80

            canvas.drawOval(
                left = leftPixel,
                top = topPixel,
                right = rightPixel,
                bottom = bottomPixel,
                paint = paint,
            )
        }
    }
}

fun <T> Flow<T>.mutableStateIn(
    scope: CoroutineScope,
    initialValue: T
): MutableStateFlow<T> {
    val flow = MutableStateFlow(initialValue)

    scope.launch {
        this@mutableStateIn.collect(flow)
    }

    return flow
}