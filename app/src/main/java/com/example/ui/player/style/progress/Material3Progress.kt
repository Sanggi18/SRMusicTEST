package com.example.ui.player.style.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.PlaybackProgress
import com.example.core.util.TimeUtils

/**
 * Standard Material 3 Default Slider Progress Bar
 */
@Composable
fun Material3Progress(
    progress: PlaybackProgress,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color? = null,
    inactiveColor: Color? = null
) {
    val finalActiveColor = activeColor ?: MaterialTheme.colorScheme.primary
    val finalInactiveColor = inactiveColor ?: MaterialTheme.colorScheme.surfaceVariant

    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    var pendingSeekPositionMs by remember { mutableStateOf<Long?>(null) }

    val safeDuration = progress.duration.coerceAtLeast(1L)

    androidx.compose.runtime.LaunchedEffect(progress.currentPosition) {
        val pending = pendingSeekPositionMs
        if (pending != null && kotlin.math.abs(progress.currentPosition - pending) <= 1500L) {
            pendingSeekPositionMs = null
        }
    }

    androidx.compose.runtime.LaunchedEffect(pendingSeekPositionMs) {
        if (pendingSeekPositionMs != null) {
            kotlinx.coroutines.delay(600L)
            pendingSeekPositionMs = null
        }
    }

    val actualProgress = when {
        isDragging -> dragProgress
        pendingSeekPositionMs != null -> (pendingSeekPositionMs!!.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
        else -> (progress.currentPosition.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
    }
    val sliderValue = actualProgress

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("material3_progress_bar"),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Slider(
            value = sliderValue,
            onValueChange = { frac ->
                isDragging = true
                dragProgress = frac
            },
            onValueChangeFinished = {
                val targetMs = (dragProgress * safeDuration).toLong().coerceIn(0L, safeDuration)
                pendingSeekPositionMs = targetMs
                onSeek(targetMs)
                isDragging = false
            },
            colors = SliderDefaults.colors(
                thumbColor = finalActiveColor,
                activeTrackColor = finalActiveColor,
                inactiveTrackColor = finalInactiveColor
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val displayMs = if (isDragging) (dragProgress * safeDuration).toLong() else progress.currentPosition
            val remainingMs = (safeDuration - displayMs).coerceAtLeast(0L)

            Text(
                text = TimeUtils.formatDuration(displayMs),
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
