package com.example.ui.player.core
import com.example.ui.player.components.*
import com.example.ui.common.components.*

import com.example.core.data.metadata.AudioTechnicalInfo
import com.example.core.model.NowPlayingProgressStyle
import com.example.core.model.NowPlayingTheme
import com.example.core.model.PlaybackInfo
import com.example.core.model.PlaybackProgress
import com.example.core.model.Song
import com.example.core.model.TrackInfoAlignment

sealed interface PlayerAction {
    data object PlayPause : PlayerAction
    data object Next : PlayerAction
    data object Previous : PlayerAction
    data class SeekTo(val positionMs: Long) : PlayerAction
    data object ToggleShuffle : PlayerAction
    data object ToggleRepeat : PlayerAction
    data class ToggleFavorite(val songId: Long) : PlayerAction
    data class SelectQueueItem(val index: Int) : PlayerAction
    data class PlaySong(val song: Song, val playlist: List<Song> = emptyList()) : PlayerAction
    data class PlayNext(val song: Song) : PlayerAction
    data class PlayNextBatch(val songs: List<Song>) : PlayerAction
    data class AddToQueue(val song: Song) : PlayerAction
    data class AddToQueueBatch(val songs: List<Song>) : PlayerAction
    data class PlayShuffleSession(val startSong: Song, val sessionBatch: List<Song> = emptyList()) : PlayerAction
    data object Expand : PlayerAction
    data object Collapse : PlayerAction
    data object Dismiss : PlayerAction
    data class SetExpanded(val expanded: Boolean) : PlayerAction
    data class SetDismissed(val dismissed: Boolean) : PlayerAction
    data object OpenEqualizer : PlayerAction
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

data class PlayerUiState(
    val playbackInfo: PlaybackInfo = PlaybackInfo(),
    val playbackProgress: PlaybackProgress = PlaybackProgress(),
    val currentQueue: List<Song> = emptyList(),
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.ALL,
    val isFavorite: Boolean = false,
    val technicalInfo: AudioTechnicalInfo? = null,
    val isExpanded: Boolean = false,
    val isDismissed: Boolean = false,
    val progressStyle: NowPlayingProgressStyle = NowPlayingProgressStyle.ROUNDED_BAR,
    val nowPlayingTheme: NowPlayingTheme = NowPlayingTheme.DEFAULT,
    val trackInfoAlignment: TrackInfoAlignment = TrackInfoAlignment.LEFT
) {
    val currentSong: Song? get() = playbackInfo.currentSong
}
