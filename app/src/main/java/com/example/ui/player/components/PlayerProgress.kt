package com.example.ui.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.model.PlaybackProgress
import com.example.core.util.TimeUtils

@Composable
fun PlayerProgressBar(
    progress: PlaybackProgress,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isUserDragging by remember { mutableStateOf(false) }
    var dragPositionFraction by remember { mutableFloatStateOf(0f) }
    var pendingSeekPositionMs by remember { mutableStateOf<Long?>(null) }

    val currentDuration = progress.duration

    // Reset pendingSeekPositionMs once player's reported position catches up close to target
    androidx.compose.runtime.LaunchedEffect(progress.currentPosition) {
        val pending = pendingSeekPositionMs
        if (pending != null && kotlin.math.abs(progress.currentPosition - pending) <= 1500L) {
            pendingSeekPositionMs = null
        }
    }

    // Safety timeout: ensure pendingSeekPositionMs doesn't stick indefinitely if paused
    androidx.compose.runtime.LaunchedEffect(pendingSeekPositionMs) {
        if (pendingSeekPositionMs != null) {
            kotlinx.coroutines.delay(600L)
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("now_playing_slider")
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
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
                                val pointerChange = event.changes.firstOrNull { it.id == down.id } ?: break

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
                },
            contentAlignment = Alignment.Center
        ) {
            val barWidth = maxWidth
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                if (currentProgressValue > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(barWidth * currentProgressValue)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = TimeUtils.formatDuration(displayedPositionMs),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = TimeUtils.formatDuration(currentDuration),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

