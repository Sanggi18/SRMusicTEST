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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.PlaybackProgress
import com.example.core.util.TimeUtils
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * Expressive Wave Progress Bar
 *
 * Smooth continuous sinusoidal wave scrubber:
 * - Active played portion: Traveling sinusoidal wave
 * - Inactive unplayed portion: Clean horizontal baseline
 * - Interactive circular thumb indicator traveling along the wave
 * - Left time indicator: Elapsed duration (m:ss)
 * - Right time indicator: Negative remaining duration (-m:ss)
 */
@Composable
fun ExpressiveWaveProgress(
    progress: PlaybackProgress,
    isPlaying: Boolean,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color? = null,
    inactiveColor: Color? = null
) {
    val finalActiveColor = activeColor ?: MaterialTheme.colorScheme.primary
    val finalInactiveColor = inactiveColor ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)

    var isDragging by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableLongStateOf(0L) }
    var dragProgressFraction by remember { mutableFloatStateOf(0f) }
    var pendingSeekPositionMs by remember { mutableStateOf<Long?>(null) }

    val safeDuration = progress.duration.coerceAtLeast(1L)

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

    val displayPosition = when {
        isDragging -> dragPositionMs
        pendingSeekPositionMs != null -> pendingSeekPositionMs!!
        else -> progress.currentPosition.coerceIn(0L, safeDuration)
    }
    val currentFraction = (displayPosition.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)

    // Continuous wave animation when music is playing
    val infiniteTransition = rememberInfiniteTransition(label = "expressive_wave_phase_transition")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying && !isDragging) (2f * PI.toFloat()) else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "expressive_wave_phase"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("now_playing_expressive_wave_slider"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .pointerInput(safeDuration) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        val widthPx = size.width.toFloat()
                        val initialFrac = if (widthPx > 0f) {
                            (down.position.x / widthPx).coerceIn(0f, 1f)
                        } else 0f

                        isDragging = true
                        dragProgressFraction = initialFrac
                        dragPositionMs = (initialFrac * safeDuration).toLong().coerceIn(0L, safeDuration)
                        var lastFraction = initialFrac

                        try {
                            while (true) {
                                val event = awaitPointerEvent()
                                val pointerChange = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!pointerChange.pressed) {
                                    pointerChange.consume()
                                    val finalFraction = if (widthPx > 0f) {
                                        (pointerChange.position.x / widthPx).coerceIn(0f, 1f)
                                    } else {
                                        lastFraction
                                    }
                                    val targetMs = (finalFraction * safeDuration).toLong().coerceIn(0L, safeDuration)
                                    pendingSeekPositionMs = targetMs
                                    onSeek(targetMs)
                                    break
                                } else {
                                    pointerChange.consume()
                                    if (widthPx > 0f) {
                                        val frac = (pointerChange.position.x / widthPx).coerceIn(0f, 1f)
                                        lastFraction = frac
                                        dragProgressFraction = frac
                                        dragPositionMs = (frac * safeDuration).toLong().coerceIn(0L, safeDuration)
                                    }
                                }
                            }
                        } finally {
                            isDragging = false
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(36.dp)) {
                val width = size.width
                val height = size.height
                val centerY = height / 2f
                val progressX = (width * currentFraction).coerceIn(0f, width)

                val trackStrokeWidth = 4.dp.toPx()
                val waveAmplitude = 4.5.dp.toPx()
                val waveLength = 48.dp.toPx()
                val k = (2f * PI.toFloat()) / waveLength

                // 1. Draw Inactive Track
                if (progressX < width) {
                    drawLine(
                        color = finalInactiveColor,
                        start = Offset(progressX, centerY),
                        end = Offset(width, centerY),
                        strokeWidth = trackStrokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                // 2. Draw Active Track (Sinusoidal Wave from 0 to progressX)
                if (progressX > 0f) {
                    val wavePath = Path()
                    val stepPx = 2f
                    var x = 0f

                    val startY = centerY + waveAmplitude * sin(x * k + wavePhase)
                    wavePath.moveTo(0f, startY)

                    while (x < progressX) {
                        x = (x + stepPx).coerceAtMost(progressX)
                        val y = centerY + waveAmplitude * sin(x * k + wavePhase)
                        wavePath.lineTo(x, y)
                    }

                    drawPath(
                        path = wavePath,
                        color = finalActiveColor,
                        style = Stroke(
                            width = trackStrokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // 3. Draw Thumb Scrubber Dot
                val thumbRadius = 6.5.dp.toPx()
                val thumbY = centerY + waveAmplitude * sin(progressX * k + wavePhase)
                val thumbCenter = Offset(progressX.coerceIn(0f, width), thumbY)

                drawCircle(
                    color = finalActiveColor,
                    radius = thumbRadius,
                    center = thumbCenter
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = thumbCenter
                )
            }
        }

        // Time Indicators (Elapsed on left, Negative Remaining on right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val elapsedMs = displayPosition
            val remainingMs = (safeDuration - elapsedMs).coerceAtLeast(0L)

            Text(
                text = TimeUtils.formatDuration(elapsedMs),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "-${TimeUtils.formatDuration(remainingMs)}",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
