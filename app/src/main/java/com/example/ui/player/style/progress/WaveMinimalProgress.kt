package com.example.ui.player.style.progress

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.PlaybackProgress
import com.example.core.util.TimeUtils
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun WaveMinimalProgress(
    progress: PlaybackProgress,
    isPlaying: Boolean,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color? = null,
    inactiveColor: Color? = null
) {
    var isUserDragging by remember { mutableStateOf(false) }
    var dragPositionFraction by remember { mutableFloatStateOf(0f) }
    var pendingSeekPositionMs by remember { mutableStateOf<Long?>(null) }

    val currentDuration = progress.duration

    LaunchedEffect(progress.currentPosition) {
        val pending = pendingSeekPositionMs
        if (pending != null && abs(progress.currentPosition - pending) <= 1500L) {
            pendingSeekPositionMs = null
        }
    }

    LaunchedEffect(pendingSeekPositionMs) {
        if (pendingSeekPositionMs != null) {
            delay(600L)
            pendingSeekPositionMs = null
        }
    }

    val currentProgressValue = when {
        isUserDragging -> dragPositionFraction.coerceIn(0f, 1f)
        pendingSeekPositionMs != null && currentDuration > 0L -> (pendingSeekPositionMs!!.toFloat() / currentDuration.toFloat()).coerceIn(0f, 1f)
        else -> progress.progressFraction.coerceIn(0f, 1f)
    }

    val displayedPositionMs = when {
        isUserDragging -> (dragPositionFraction.coerceIn(0f, 1f) * currentDuration).toLong()
        pendingSeekPositionMs != null -> pendingSeekPositionMs!!
        else -> progress.currentPosition
    }

    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val trackActiveColor = activeColor ?: MaterialTheme.colorScheme.primary
    val trackInactiveColor = inactiveColor ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("now_playing_wave_minimal_slider")
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .pointerInput(currentDuration) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        val widthPx = size.width.toFloat()
                        if (widthPx > 0f) {
                            val initialFraction = (down.position.x / widthPx).coerceIn(0f, 1f)
                            isUserDragging = true
                            dragPositionFraction = initialFraction
                        }

                        var lastFraction = currentProgressValue

                        try {
                            while (true) {
                                val event = awaitPointerEvent()
                                val pointerChange = event.changes.firstOrNull() ?: break

                                if (!pointerChange.pressed) {
                                    pointerChange.consume()
                                    val finalFraction = if (widthPx > 0f) {
                                        (pointerChange.position.x / widthPx).coerceIn(0f, 1f)
                                    } else {
                                        lastFraction
                                    }
                                    val targetMs = (finalFraction * currentDuration).toLong()
                                    pendingSeekPositionMs = targetMs
                                    onSeek(targetMs)
                                    break
                                } else {
                                    pointerChange.consume()
                                    if (widthPx > 0f) {
                                        val currentFraction = (pointerChange.position.x / widthPx).coerceIn(0f, 1f)
                                        lastFraction = currentFraction
                                        dragPositionFraction = currentFraction
                                    }
                                }
                            }
                        } finally {
                            isUserDragging = false
                        }
                    }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val centerY = canvasHeight / 2f
                val strokeWidthPx = 4.dp.toPx()
                val waveAmplitudePx = 4.5.dp.toPx()
                val wavelengthPx = 24.dp.toPx()
                val gapPx = 5.dp.toPx()

                val progressX = (canvasWidth * currentProgressValue).coerceIn(0f, canvasWidth)

                // 1. Draw Active Wave Track
                if (progressX > 0f) {
                    val activePath = Path()
                    activePath.moveTo(0f, centerY)

                    var x = 0f
                    val stepPx = 2f
                    while (x <= progressX) {
                        val phase = if (isPlaying) wavePhase else 0f
                        val edgeDamping = ((progressX - x) / 16.dp.toPx()).coerceIn(0f, 1f)
                        val y = centerY + (sin((x / wavelengthPx) * 2f * PI.toFloat() + phase) * waveAmplitudePx * edgeDamping)
                        activePath.lineTo(x, y)
                        x += stepPx
                    }

                    drawPath(
                        path = activePath,
                        color = trackActiveColor,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )
                }

                // 2. Draw Inactive Straight Track
                val inactiveStartX = if (progressX > 0f) {
                    (progressX + gapPx).coerceAtMost(canvasWidth)
                } else {
                    0f
                }

                if (inactiveStartX < canvasWidth) {
                    drawLine(
                        color = trackInactiveColor,
                        start = Offset(inactiveStartX, centerY),
                        end = Offset(canvasWidth, centerY),
                        strokeWidth = strokeWidthPx,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = TimeUtils.formatDuration(displayedPositionMs),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    color = activeColor?.copy(alpha = 0.90f) ?: MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = TimeUtils.formatDuration(currentDuration),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    color = activeColor?.copy(alpha = 0.90f) ?: MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
