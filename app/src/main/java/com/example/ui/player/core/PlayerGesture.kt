package com.example.ui.player.core
import com.example.ui.player.components.*
import com.example.ui.common.components.*

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.ui.common.theme.ExpressiveMotion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

fun Modifier.miniPlayerHorizontalDrag(
    onNext: () -> Unit,
    onPrevious: () -> Unit
): Modifier = composed {
    val coroutineScope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }

    this
        .graphicsLayer {
            translationX = dragOffset.value
            alpha = (1f - (abs(dragOffset.value) / 800f)).coerceIn(0.55f, 1f)
        }
        .pointerInput(Unit) {
            val swipeThresholdPx = 72.dp.toPx()
            detectHorizontalDragGestures(
                onDragStart = { },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    coroutineScope.launch {
                        // Apply rubber-band damping
                        val current = dragOffset.value
                        val newOffset = current + (dragAmount * 0.75f)
                        dragOffset.snapTo(newOffset.coerceIn(-300f, 300f))
                    }
                },
                onDragEnd = {
                    coroutineScope.launch {
                        val current = dragOffset.value
                        if (current < -swipeThresholdPx) {
                            onNext()
                        } else if (current > swipeThresholdPx) {
                            onPrevious()
                        }
                        dragOffset.animateTo(
                            targetValue = 0f,
                            animationSpec = ExpressiveMotion.BouncySpring
                        )
                    }
                },
                onDragCancel = {
                    coroutineScope.launch {
                        dragOffset.animateTo(
                            targetValue = 0f,
                            animationSpec = ExpressiveMotion.BouncySpring
                        )
                    }
                }
            )
        }
}

fun Modifier.playerSurfaceVerticalDrag(
    coroutineScope: CoroutineScope,
    expansionAnimatable: Animatable<Float, AnimationVector1D>,
    dismissOffsetY: Animatable<Float, AnimationVector1D>,
    screenHeight: Float,
    density: Density,
    onExpandedChange: (Boolean) -> Unit,
    onDismissMiniPlayer: () -> Unit
): Modifier = this.pointerInput(screenHeight) {
    var dragStartProgress = 0f
    var isDismissDragActive = false

    detectVerticalDragGestures(
        onDragStart = {
            dragStartProgress = expansionAnimatable.value
            isDismissDragActive = false
        },
        onVerticalDrag = { change, dragAmount ->
            change.consume()
            coroutineScope.launch {
                if (expansionAnimatable.value == 0f && dismissOffsetY.value == 0f) {
                    if (dragAmount > 0f) {
                        isDismissDragActive = true
                    }
                }
                if (isDismissDragActive || dismissOffsetY.value > 0f) {
                    val newDismissOffset = (dismissOffsetY.value + dragAmount).coerceAtLeast(0f)
                    dismissOffsetY.snapTo(newDismissOffset)
                    if (dismissOffsetY.value == 0f) {
                        isDismissDragActive = false
                    }
                } else {
                    val deltaProgress = -dragAmount / (screenHeight * 0.85f)
                    val newProgress = (expansionAnimatable.value + deltaProgress).coerceIn(0f, 1f)
                    expansionAnimatable.snapTo(newProgress)
                }
            }
        },
        onDragEnd = {
            coroutineScope.launch {
                if (isDismissDragActive || dismissOffsetY.value > 0f) {
                    if (dismissOffsetY.value > 80f) {
                        val dismissTargetPx: Float = with(density) { 200.dp.toPx() }
                        dismissOffsetY.animateTo(
                            targetValue = dismissTargetPx,
                            animationSpec = tween(
                                durationMillis = 180,
                                easing = FastOutSlowInEasing
                            )
                        )
                        onDismissMiniPlayer()
                        dismissOffsetY.snapTo(0f)
                    } else {
                        dismissOffsetY.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(
                                durationMillis = 200,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                    isDismissDragActive = false
                } else {
                    val target = if (dragStartProgress < 0.5f) {
                        if (expansionAnimatable.value > 0.20f) 1f else 0f
                    } else {
                        if (expansionAnimatable.value < 0.80f) 0f else 1f
                    }
                    val isTargetExpanded = target == 1f
                    expansionAnimatable.animateTo(
                        targetValue = target,
                        animationSpec = spring(
                            dampingRatio = 0.90f,
                            stiffness = 520f
                        )
                    )
                    onExpandedChange(isTargetExpanded)
                }
            }
        },
        onDragCancel = {
            coroutineScope.launch {
                if (dismissOffsetY.value > 0f) {
                    dismissOffsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = FastOutSlowInEasing
                        )
                    )
                    isDismissDragActive = false
                } else {
                    val target = if (dragStartProgress < 0.5f) {
                        if (expansionAnimatable.value > 0.5f) 1f else 0f
                    } else {
                        if (expansionAnimatable.value < 0.5f) 0f else 1f
                    }
                    val isTargetExpanded = target == 1f
                    expansionAnimatable.animateTo(
                        targetValue = target,
                        animationSpec = spring(
                            dampingRatio = 0.90f,
                            stiffness = 520f
                        )
                    )
                    onExpandedChange(isTargetExpanded)
                }
            }
        }
    )
}
