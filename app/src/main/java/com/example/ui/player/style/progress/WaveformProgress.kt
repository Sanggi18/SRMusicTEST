package com.example.ui.player.style.progress

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.model.PlaybackProgress
import com.example.core.model.Song
import com.example.core.util.FastAudioWaveformExtractor
import com.example.core.util.TimeUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

private const val WAVEFORM_BAR_COUNT = 100

/**
 * Waveform Progress Bar
 *
 * Parallel Execution Architecture:
 * - Next song triggered: Immediately begins extracting true acoustic waveform in background.
 * - Simultaneously: Begins closing previous waveform down to flat row of dots (up to 1000 ms).
 * - Instant Bloom Hand-off: As soon as audio data is ready, the closing animation is interrupted
 *   immediately (no waiting for full 1000 ms), and it gracefully blooms UP to peak acoustic heights (500 ms).
 */
@Composable
fun WaveformProgress(
    progress: PlaybackProgress,
    isPlaying: Boolean,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    song: Song? = null,
    activeColor: Color? = null,
    inactiveColor: Color? = null
) {
    val context = LocalContext.current
    val flatBaseline = remember { FloatArray(WAVEFORM_BAR_COUNT) { 0f } }

    var renderedBars by remember { mutableStateOf(flatBaseline) }

    // waveScale: 0f = flat line of circular dots, 1f = fully opened real waveform
    val waveScale = remember { Animatable(0f) }

    LaunchedEffect(song?.id) {
        val currentSong = song ?: run {
            if (waveScale.value > 0.01f) {
                waveScale.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                )
            }
            renderedBars = flatBaseline
            return@LaunchedEffect
        }

        // Parallel & Adaptive execution upon song change:
        // - Cached/Next-Previous: 500 ms smooth close down to flat + 500 ms bloom open.
        // - New Audio Data loading: 1000 ms calm close down to flat + 500 ms bloom open.
        coroutineScope {
            val waveformFlow = FastAudioWaveformExtractor.getWaveformFlow(context.applicationContext, currentSong)
            val isAlreadyCached = waveformFlow.value != null
            val closeDuration = if (isAlreadyCached) 500 else 1000

            // 1. Start fetching new song waveform in background
            var receivedData: FloatArray? = null
            val dataJob = launch {
                waveformFlow.first { it != null }.also { receivedData = it }
            }

            // 2. Smoothly close down to flat (500 ms if cached, 1000 ms if new)
            if (waveScale.value > 0.01f) {
                waveScale.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = closeDuration, easing = FastOutSlowInEasing)
                )
            } else {
                waveScale.snapTo(0f)
            }

            // 3. Ensure data is ready (instant if cached, waits if new)
            dataJob.join()

            // 4. Update to new audio data at flat position
            receivedData?.let {
                renderedBars = it
            }

            // 5. Blossom open to peak acoustic heights (500 ms)
            waveScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
    }

    // Scrubbing and Gesture States
    var isUserDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
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
            delay(650L)
            pendingSeekPositionMs = null
        }
    }

    val currentFraction = when {
        isUserDragging -> dragFraction.coerceIn(0f, 1f)
        pendingSeekPositionMs != null && currentDuration > 0L ->
            (pendingSeekPositionMs!!.toFloat() / currentDuration.toFloat()).coerceIn(0f, 1f)
        else -> progress.progressFraction.coerceIn(0f, 1f)
    }

    val displayedPositionMs = when {
        isUserDragging -> (dragFraction.coerceIn(0f, 1f) * currentDuration).toLong()
        pendingSeekPositionMs != null -> pendingSeekPositionMs!!
        else -> progress.currentPosition
    }

    val primaryActive = activeColor ?: MaterialTheme.colorScheme.primary
    val baseInactive = inactiveColor ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("now_playing_waveform")
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .pointerInput(currentDuration) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        val widthPx = size.width.toFloat()
                        if (widthPx > 0f) {
                            val initialFraction = (down.position.x / widthPx).coerceIn(0f, 1f)
                            isUserDragging = true
                            dragFraction = initialFraction
                        }

                        while (true) {
                            val event = awaitPointerEvent()
                            val dragChange = event.changes.firstOrNull()
                            if (dragChange == null || !dragChange.pressed) {
                                break
                            }
                            val currentX = dragChange.position.x
                            val width = size.width.toFloat()
                            if (width > 0f) {
                                dragFraction = (currentX / width).coerceIn(0f, 1f)
                            }
                            dragChange.consume()
                        }

                        val targetSeek = (dragFraction.coerceIn(0f, 1f) * currentDuration).toLong()
                        pendingSeekPositionMs = targetSeek
                        onSeek(targetSeek)
                        isUserDragging = false
                    }
                }
        ) {
            // Real Waveform Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .align(Alignment.Center)
            ) {
                val width = size.width
                val height = size.height
                if (width <= 0f || height <= 0f) return@Canvas

                val count = renderedBars.size
                val barSpacing = 1.5.dp.toPx()
                val totalSpacing = barSpacing * (count - 1)
                val barWidth = max(1.5.dp.toPx(), (width - totalSpacing) / count.toFloat())
                val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

                val playheadIndex = (currentFraction * count).toInt().coerceIn(0, count - 1)
                val centerY = height / 2f
                val animScale = waveScale.value

                for (i in 0 until count) {
                    val x = i * (barWidth + barSpacing)
                    val rawNormalized = renderedBars[i] // 0.0f .. 1.0f

                    // When animScale == 0f (loading / transition phase), effectiveAmp is 0f
                    // which cleanly draws barHeight = barWidth (minimal horizontal line of dots)
                    val effectiveAmp = rawNormalized * animScale
                    val barHeight = max(barWidth, height * effectiveAmp)
                    val top = centerY - (barHeight / 2f)

                    val isPlayed = i <= playheadIndex
                    val color = if (isPlayed) primaryActive else baseInactive

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = cornerRadius
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Time Indicators Row (TextElapsed left & TextDuration right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = TimeUtils.formatDuration(displayedPositionMs),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Text(
                text = TimeUtils.formatDuration(currentDuration),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
