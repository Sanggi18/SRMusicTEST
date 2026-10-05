package com.example.ui.player.style.progress

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.model.PlaybackProgress
import com.example.core.util.TimeUtils
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun CapsulePillProgress(
    progress: PlaybackProgress,
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

    val activeTrackColor = activeColor ?: MaterialTheme.colorScheme.primary
    val inactiveTrackColor = inactiveColor ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("now_playing_capsule_pill_slider")
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
                        val initialFraction = if (widthPx > 0f) {
                            (down.position.x / widthPx).coerceIn(0f, 1f)
                        } else 0f

                        isUserDragging = true
                        dragPositionFraction = initialFraction
                        var lastFraction = initialFraction

                        try {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                if (!change.pressed) {
                                    change.consume()
                                    val finalFraction = if (widthPx > 0f) {
                                        (change.position.x / widthPx).coerceIn(0f, 1f)
                                    } else {
                                        lastFraction
                                    }
                                    val targetMs = (finalFraction * currentDuration).toLong()
                                    pendingSeekPositionMs = targetMs
                                    onSeek(targetMs)
                                    break
                                } else {
                                    change.consume()
                                    if (widthPx > 0f) {
                                        val newFraction = (change.position.x / widthPx).coerceIn(0f, 1f)
                                        lastFraction = newFraction
                                        dragPositionFraction = newFraction
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
                    .height(32.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val centerY = canvasHeight / 2f
                val trackHeightPx = 4.dp.toPx()
                val pillWidthPx = 6.dp.toPx()
                val pillHeightPx = 18.dp.toPx()
                val pillCornerPx = 3.dp.toPx()

                val progressX = (canvasWidth * currentProgressValue).coerceIn(0f, canvasWidth)

                // Inactive Background Track
                drawLine(
                    color = inactiveTrackColor,
                    start = Offset(0f, centerY),
                    end = Offset(canvasWidth, centerY),
                    strokeWidth = trackHeightPx,
                    cap = StrokeCap.Round
                )

                // Active Foreground Track
                if (progressX > 0f) {
                    drawLine(
                        color = activeTrackColor,
                        start = Offset(0f, centerY),
                        end = Offset(progressX, centerY),
                        strokeWidth = trackHeightPx,
                        cap = StrokeCap.Round
                    )
                }

                // Vertical Capsule Pill thumb
                val pillLeft = (progressX - pillWidthPx / 2f).coerceIn(0f, canvasWidth - pillWidthPx)
                val pillTop = centerY - pillHeightPx / 2f

                val pillPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = androidx.compose.ui.geometry.Rect(
                                offset = Offset(pillLeft, pillTop),
                                size = Size(pillWidthPx, pillHeightPx)
                            ),
                            cornerRadius = CornerRadius(pillCornerPx, pillCornerPx)
                        )
                    )
                }
                drawPath(path = pillPath, color = activeTrackColor)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = TimeUtils.formatDuration(displayedPositionMs),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = activeColor?.copy(alpha = 0.85f) ?: MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = TimeUtils.formatDuration(currentDuration),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = activeColor?.copy(alpha = 0.85f) ?: MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
