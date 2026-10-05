package com.example.ui.player.style.progress

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.core.model.NowPlayingProgressStyle
import com.example.core.model.PlaybackProgress
import com.example.core.model.Song

@Composable
fun PlayerProgressContainer(
    progress: PlaybackProgress,
    isPlaying: Boolean,
    progressStyle: NowPlayingProgressStyle,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    song: Song? = null,
    activeColor: Color? = null,
    inactiveColor: Color? = null
) {
    when (progressStyle) {
        NowPlayingProgressStyle.LINE_THUMB -> {
            LineThumbProgress(
                progress = progress,
                onSeek = onSeek,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                modifier = modifier
            )
        }
        NowPlayingProgressStyle.CAPSULE_PILL -> {
            CapsulePillProgress(
                progress = progress,
                onSeek = onSeek,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                modifier = modifier
            )
        }
        NowPlayingProgressStyle.ROUNDED_BAR -> {
            RoundedBarProgress(
                progress = progress,
                onSeek = onSeek,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                modifier = modifier
            )
        }
        NowPlayingProgressStyle.WAVE_MINIMAL -> {
            WaveMinimalProgress(
                progress = progress,
                isPlaying = isPlaying,
                onSeek = onSeek,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                modifier = modifier
            )
        }
        NowPlayingProgressStyle.EXPRESSIVE_WAVE -> {
            ExpressiveWaveProgress(
                progress = progress,
                isPlaying = isPlaying,
                onSeek = onSeek,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                modifier = modifier
            )
        }
        NowPlayingProgressStyle.MATERIAL_3 -> {
            Material3Progress(
                progress = progress,
                onSeek = onSeek,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                modifier = modifier
            )
        }
        NowPlayingProgressStyle.WAVEFORM -> {
            WaveformProgress(
                progress = progress,
                isPlaying = isPlaying,
                onSeek = onSeek,
                song = song,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                modifier = modifier
            )
        }
    }
}
