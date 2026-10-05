package com.example.core.data.repository

import android.content.Context
import com.example.core.model.PlaybackInfo
import com.example.core.model.PlaybackProgress
import com.example.core.model.Song
import com.example.core.playback.PlaybackManager
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface PlaybackRepository {
    val playbackInfo: StateFlow<PlaybackInfo>
    val playbackProgress: StateFlow<PlaybackProgress>
    val currentQueue: StateFlow<List<Song>>
    val currentMediaItemIndex: StateFlow<Int>
    val isShuffleEnabledFlow: StateFlow<Boolean>
    val repeatModeFlow: StateFlow<Int>
    val onPlaybackEndedEvent: SharedFlow<Unit>

    fun playSong(song: Song, playlist: List<Song> = emptyList(), isShuffleSession: Boolean = false)
    fun playNext(song: Song)
    fun playNextBatch(songs: List<Song>)
    fun addToQueue(song: Song)
    fun addToQueueBatch(songs: List<Song>)
    fun playFromQueue(index: Int)
    fun togglePlayPause()
    fun seekTo(positionMs: Long)
    fun skipToNext()
    fun skipToPrevious()
    fun toggleShuffle(): Boolean
    fun isShuffleEnabled(): Boolean
    fun toggleRepeat(): Int
    fun getRepeatMode(): Int
}

class PlaybackRepositoryImpl(
    private val context: Context,
    private val playbackManager: PlaybackManager = PlaybackManager.instance ?: PlaybackManager(context.applicationContext)
) : PlaybackRepository {

    override val playbackInfo: StateFlow<PlaybackInfo> = playbackManager.playbackInfo
    override val playbackProgress: StateFlow<PlaybackProgress> = playbackManager.playbackProgress
    override val currentQueue: StateFlow<List<Song>> = playbackManager.currentQueue
    override val currentMediaItemIndex: StateFlow<Int> = playbackManager.currentMediaItemIndex
    override val isShuffleEnabledFlow: StateFlow<Boolean> = playbackManager.isShuffleEnabledFlow
    override val repeatModeFlow: StateFlow<Int> = playbackManager.repeatModeFlow
    override val onPlaybackEndedEvent: SharedFlow<Unit> = playbackManager.onPlaybackEndedEvent

    override fun playSong(song: Song, playlist: List<Song>, isShuffleSession: Boolean) {
        playbackManager.playSong(song, playlist, isShuffleSession)
    }

    override fun playNext(song: Song) {
        playbackManager.playNext(song)
    }

    override fun playNextBatch(songs: List<Song>) {
        playbackManager.playNextBatch(songs)
    }

    override fun addToQueue(song: Song) {
        playbackManager.addToQueue(song)
    }

    override fun addToQueueBatch(songs: List<Song>) {
        playbackManager.addToQueueBatch(songs)
    }

    override fun playFromQueue(index: Int) {
        playbackManager.playFromQueue(index)
    }

    override fun togglePlayPause() {
        playbackManager.togglePlayPause()
    }

    override fun seekTo(positionMs: Long) {
        playbackManager.seekTo(positionMs)
    }

    override fun skipToNext() {
        playbackManager.skipToNext()
    }

    override fun skipToPrevious() {
        playbackManager.skipToPrevious()
    }

    override fun toggleShuffle(): Boolean {
        return playbackManager.toggleShuffle()
    }

    override fun isShuffleEnabled(): Boolean {
        return playbackManager.isShuffleEnabled()
    }

    override fun toggleRepeat(): Int {
        return playbackManager.toggleRepeat()
    }

    override fun getRepeatMode(): Int {
        return playbackManager.getRepeatMode()
    }
}
