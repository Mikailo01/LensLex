package com.bytecause.lenslex.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

fun Modifier.swipeToDismiss(
    onDismissed: () -> Unit
): Modifier = composed {
    // Create an Animatable instance for the offset of the swiped element.
    val offsetX = remember { Animatable(0f) }
    // used to receive user touch events
    pointerInput(Unit) {
        // Used to calculate a settling position of a fling animation.
        val decay = splineBasedDecay<Float>(this)
        // Wrap in a coroutine scope to use suspend functions for touch events and animation.
        coroutineScope {
            while (true) {
                // Wait for a touch down event.
                val pointerId = awaitPointerEventScope { awaitFirstDown().id }
                // Cancel any-ongoing animations.
                offsetX.stop()

                // Prepare for drag events and record velocity of a fling.
                val velocityTracker = VelocityTracker()

                // Wait for drag events.
                awaitPointerEventScope {
                    horizontalDrag(pointerId) { change ->
                        // Get the drag amount change to offset the item with
                        val horizontalDragOffset = offsetX.value + change.positionChange().x

                        // Need to call this in a launch block in order to run it separately outside of the awaitPointerEventScope
                        launch {
                            // Instantly set the Animable to the dragOffset to ensure its moving
                            // as the user's finger moves
                            offsetX.snapTo(horizontalDragOffset)
                        }

                        // Record the velocity of the drag.
                        velocityTracker.addPosition(change.uptimeMillis, change.position)

                        // Consume the gesture event, not passed to external
                        if (change.positionChange() != Offset.Zero) change.consume()
                    }
                }
                // Dragging finished. Calculate the velocity of the fling.
                val velocity = velocityTracker.calculateVelocity().x

                // Add this line to calculate where it would end up with
                // the current velocity and position
                val targetOffsetX = decay.calculateTargetValue(offsetX.value, velocity)

                // We want to set upper and lower value bounds to the Animatable so that it stops
                // as soon as it reaches the bounds (-size.width and size.width since we don't want
                // the offsetX to be able to extend past these two values).
                offsetX.updateBounds(
                    lowerBound = -size.width.toFloat(),
                    upperBound = size.width.toFloat()
                )

                launch {
                    if (targetOffsetX.absoluteValue <= size.width) {
                        // Not enough velocity; Slide back.
                        offsetX.animateTo(targetValue = 0f, initialVelocity = velocity)
                    } else {
                        // Enough velocity to slide away the element to the edge.
                        offsetX.animateDecay(velocity, decay)
                        // The element was swiped away.
                        onDismissed()
                    }
                }
            }
        }
    }
        .offset {
            // Apply the horizontal offset to the element.
            IntOffset(offsetX.value.roundToInt(), 0)
        }
}
