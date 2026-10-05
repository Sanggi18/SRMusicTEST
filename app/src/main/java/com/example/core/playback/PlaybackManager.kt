package com.example.core.playback

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.SRMusicApp
import com.example.core.data.repository.HistoryRepository
import com.example.core.data.repository.HistoryRepositoryImpl
import com.example.core.data.repository.SongRepository
import com.example.core.data.repository.SongRepositoryImpl
import com.example.core.model.PlaybackInfo
import com.example.core.model.PlaybackProgress
import com.example.core.model.Song
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _playbackInfo = MutableStateFlow(PlaybackInfo())
    val playbackInfo: StateFlow<PlaybackInfo> = _playbackInfo.asStateFlow()

    private val _playbackProgress = MutableStateFlow(PlaybackProgress())
    val playbackProgress: StateFlow<PlaybackProgress> = _playbackProgress.asStateFlow()

    private val _currentQueue = MutableStateFlow<List<Song>>(emptyList())
    val currentQueue: StateFlow<List<Song>> = _currentQueue.asStateFlow()

    private val _currentMediaItemIndex = MutableStateFlow(0)
    val currentMediaItemIndex: StateFlow<Int> = _currentMediaItemIndex.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabledFlow: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_ALL)
    val repeatModeFlow: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _onPlaybackEndedEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val onPlaybackEndedEvent: kotlinx.coroutines.flow.SharedFlow<Unit> = _onPlaybackEndedEvent.asSharedFlow()

    private val historyRepository: HistoryRepository by lazy {
        (context.applicationContext as? SRMusicApp)?.historyRepository ?: HistoryRepositoryImpl(context.applicationContext)
    }
    private val songRepository: SongRepository by lazy {
        (context.applicationContext as? SRMusicApp)?.songRepository ?: SongRepositoryImpl(context.applicationContext)
    }

    private var isShuffleSessionActive: Boolean = false
    private var lastRecordedSongId: Long? = null
    private var secondsTickCounterMs: Long = 0L
    private var lastEndedEmitTime: Long = 0L

    private var pendingSeekPosition: Long? = null
    private var pendingSeekTimestamp: Long = 0L

    private var progressJob: Job? = null

    companion object {
        @Volatile
        var instance: PlaybackManager? = null
            private set
    }

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            updatePlaybackInfo(player)
            if (events.contains(Player.EVENT_IS_PLAYING_CHANGED) ||
                events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) ||
                events.contains(Player.EVENT_POSITION_DISCONTINUITY)
            ) {
                if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)) {
                    pendingSeekPosition = null
                }
                updateProgress(player)
                if (player.isPlaying) {
                    startProgressPolling()
                } else {
                    stopProgressPolling()
                }
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaController?.let { updatePlaybackInfo(it) }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _isShuffleEnabled.value = shuffleModeEnabled
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = repeatMode
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                startProgressPolling()
            } else {
                stopProgressPolling()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                val now = System.currentTimeMillis()
                if (now - lastEndedEmitTime > 1000L) {
                    lastEndedEmitTime = now
                    _onPlaybackEndedEvent.tryEmit(Unit)
                }
            }
        }
    }

    init {
        instance = this
        initializeController()
    }

    private fun songToMediaItem(s: Song): MediaItem {
        return MediaItem.Builder()
            .setMediaId(s.id.toString())
            .setUri(s.contentUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(s.title)
                    .setArtist(s.artist)
                    .setAlbumTitle(s.album)
                    .setArtworkUri(s.artworkUri)
                    .build()
            )
            .build()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future

        future.addListener(
            {
                try {
                    val controller = future.get()
                    mediaController = controller
                    controller.repeatMode = Player.REPEAT_MODE_ALL
                    controller.addListener(playerListener)
                    updatePlaybackInfo(controller)
                    if (controller.isPlaying) {
                        startProgressPolling()
                    }
                } catch (e: Exception) {
                    Log.e("SRMusic:Playback", "PlaybackManager: Failed to connect to MediaController", e)
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    fun playSong(song: Song, playlist: List<Song>, isShuffleSession: Boolean = false) {
        val controller = mediaController ?: return
        isShuffleSessionActive = isShuffleSession
        val queue = if (playlist.isNotEmpty()) playlist else listOf(song)
        _currentQueue.value = queue
        val startIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        val mediaItems = queue.map { s -> songToMediaItem(s) }
        controller.repeatMode = if (isShuffleSession) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ALL
        controller.shuffleModeEnabled = false
        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.play()
    }

    fun playNext(song: Song) {
        val controller = mediaController ?: return
        val current = _currentQueue.value.toMutableList()
        if (current.isEmpty()) {
            _currentQueue.value = listOf(song)
            val mediaItem = songToMediaItem(song)
            controller.setMediaItems(listOf(mediaItem))
            controller.prepare()
            updatePlaybackInfo(controller)
            return
        }
        val currentIndex = controller.currentMediaItemIndex
        val insertIndex = if (currentIndex in current.indices) currentIndex + 1 else current.size
        current.add(insertIndex, song)
        _currentQueue.value = current
        val mediaItem = songToMediaItem(song)
        controller.addMediaItem(insertIndex, mediaItem)
        updatePlaybackInfo(controller)
    }

    fun playNextBatch(songs: List<Song>) {
        if (songs.isEmpty()) return
        val controller = mediaController ?: return
        val current = _currentQueue.value.toMutableList()
        if (current.isEmpty()) {
            _currentQueue.value = songs
            val mediaItems = songs.map { songToMediaItem(it) }
            controller.setMediaItems(mediaItems)
            controller.prepare()
            updatePlaybackInfo(controller)
            return
        }
        val currentIndex = controller.currentMediaItemIndex
        val insertIndex = if (currentIndex in current.indices) currentIndex + 1 else current.size
        current.addAll(insertIndex, songs)
        _currentQueue.value = current
        val mediaItems = songs.map { songToMediaItem(it) }
        controller.addMediaItems(insertIndex, mediaItems)
        updatePlaybackInfo(controller)
    }

    fun addToQueue(song: Song) {
        val controller = mediaController ?: return
        val current = _currentQueue.value.toMutableList()
        if (current.isEmpty()) {
            _currentQueue.value = listOf(song)
            val mediaItem = songToMediaItem(song)
            controller.setMediaItems(listOf(mediaItem))
            controller.prepare()
            updatePlaybackInfo(controller)
            return
        }
        current.add(song)
        _currentQueue.value = current
        val mediaItem = songToMediaItem(song)
        controller.addMediaItem(mediaItem)
        updatePlaybackInfo(controller)
    }

    fun addToQueueBatch(songs: List<Song>) {
        if (songs.isEmpty()) return
        val controller = mediaController ?: return
        val current = _currentQueue.value.toMutableList()
        if (current.isEmpty()) {
            _currentQueue.value = songs
            val mediaItems = songs.map { songToMediaItem(it) }
            controller.setMediaItems(mediaItems)
            controller.prepare()
            updatePlaybackInfo(controller)
            return
        }
        current.addAll(songs)
        _currentQueue.value = current
        val mediaItems = songs.map { songToMediaItem(it) }
        controller.addMediaItems(mediaItems)
        updatePlaybackInfo(controller)
    }

    fun playFromQueue(index: Int) {
        val controller = mediaController ?: return
        if (index in 0 until controller.mediaItemCount) {
            controller.seekTo(index, 0L)
            controller.play()
        }
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            if (controller.playbackState == Player.STATE_ENDED) {
                controller.seekTo(0, 0L)
            }
            controller.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val controller = mediaController ?: return
        val duration = controller.duration.coerceAtLeast(0L)
        val clamped = positionMs.coerceIn(0L, duration)
        pendingSeekPosition = clamped
        pendingSeekTimestamp = System.currentTimeMillis()
        _playbackProgress.value = PlaybackProgress(
            currentPosition = clamped,
            duration = duration
        )
        controller.seekTo(clamped)
    }

    fun skipToNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        } else {
            controller.pause()
        }
    }

    fun skipToPrevious() {
        val controller = mediaController ?: return
        if (controller.currentPosition > 3000L) {
            controller.seekTo(0L)
        } else if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        } else {
            controller.seekTo(0L)
        }
    }

    fun toggleShuffle(): Boolean {
        val newMode = !_isShuffleEnabled.value
        _isShuffleEnabled.value = newMode
        mediaController?.shuffleModeEnabled = newMode
        MusicService.activePlayer?.shuffleModeEnabled = newMode
        return newMode
    }

    fun isShuffleEnabled(): Boolean {
        val direct = mediaController?.shuffleModeEnabled ?: MusicService.activePlayer?.shuffleModeEnabled
        if (direct != null && direct != _isShuffleEnabled.value) {
            _isShuffleEnabled.value = direct
        }
        return _isShuffleEnabled.value
    }

    fun toggleRepeat(): Int {
        val current = _repeatMode.value
        val newMode = when (current) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
            else -> Player.REPEAT_MODE_ALL
        }
        _repeatMode.value = newMode
        mediaController?.repeatMode = newMode
        MusicService.activePlayer?.repeatMode = newMode
        return newMode
    }

    fun getRepeatMode(): Int {
        val direct = mediaController?.repeatMode ?: MusicService.activePlayer?.repeatMode
        if (direct != null && direct != _repeatMode.value) {
            _repeatMode.value = direct
        }
        return _repeatMode.value
    }

    private fun updatePlaybackInfo(player: Player) {
        val currentIndex = player.currentMediaItemIndex
        _currentMediaItemIndex.value = currentIndex
        val queue = _currentQueue.value
        val currentSong = if (currentIndex in queue.indices) {
            queue[currentIndex]
        } else {
            val currentMediaItem = player.currentMediaItem
            currentMediaItem?.let { item ->
                val id = item.mediaId.toLongOrNull() ?: -1L
                queue.find { it.id == id } ?: Song(
                    id = id,
                    title = item.mediaMetadata.title?.toString() ?: "Unknown Title",
                    artist = item.mediaMetadata.artist?.toString() ?: "Unknown Artist",
                    album = item.mediaMetadata.albumTitle?.toString() ?: "Unknown Album",
                    duration = player.duration.coerceAtLeast(0L),
                    albumId = -1L,
                    contentUri = item.requestMetadata.mediaUri ?: android.net.Uri.EMPTY,
                    artworkUri = item.mediaMetadata.artworkUri ?: android.net.Uri.EMPTY
                )
            }
        }

        val isBuffering = player.playbackState == Player.STATE_BUFFERING

        _playbackInfo.value = PlaybackInfo(
            currentSong = currentSong,
            isPlaying = player.isPlaying,
            isBuffering = isBuffering,
            hasNext = player.hasNextMediaItem()
        )

        if (currentSong != null && player.isPlaying) {
            if (lastRecordedSongId != currentSong.id) {
                lastRecordedSongId = currentSong.id
                scope.launch {
                    try {
                        historyRepository.recordSongPlay(currentSong.id)
                    } catch (e: Exception) {
                        Log.e("SRMusic:Playback", "Failed to record song play", e)
                    }
                }
            }
        }
    }

    fun getAccuratePosition(): Long {
        val direct = MusicService.activePlayer?.currentPosition
        if (direct != null && direct >= 0L) {
            return direct
        }
        return mediaController?.currentPosition?.coerceAtLeast(0L) ?: _playbackProgress.value.currentPosition
    }

    private fun updateProgress(player: Player) {
        val duration = player.duration.coerceAtLeast(0L)
        val directPosition = MusicService.activePlayer?.currentPosition
        val rawPosition = if (directPosition != null && directPosition >= 0L) directPosition else player.currentPosition
        val position = rawPosition.coerceAtLeast(0L).coerceAtMost(if (duration > 0L) duration else Long.MAX_VALUE)

        val pending = pendingSeekPosition
        if (pending != null) {
            val elapsed = System.currentTimeMillis() - pendingSeekTimestamp
            if (kotlin.math.abs(position - pending) <= 1500L || elapsed > 500L) {
                pendingSeekPosition = null
            } else {
                _playbackProgress.value = PlaybackProgress(
                    currentPosition = pending,
                    duration = duration
                )
                return
            }
        }

        _playbackProgress.value = PlaybackProgress(
            currentPosition = position,
            duration = duration
        )
    }

    private fun startProgressPolling() {
        if (progressJob?.isActive == true) return
        progressJob = scope.launch {
            while (isActive) {
                val controller = mediaController
                if (controller != null && controller.isPlaying) {
                    updateProgress(controller)
                    secondsTickCounterMs += 100L
                    if (secondsTickCounterMs >= 1000L) {
                        historyRepository.addListenedSeconds(secondsTickCounterMs / 1000L)
                        secondsTickCounterMs %= 1000L
                    }
                    delay(100L)
                } else if (controller != null) {
                    updateProgress(controller)
                    delay(250L)
                } else {
                    delay(500L)
                }
            }
        }
    }

    private fun stopProgressPolling() {
        progressJob?.cancel()
        progressJob = null
        mediaController?.let { updateProgress(it) }
    }

    fun release() {
        if (instance == this) instance = null
        stopProgressPolling()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        controllerFuture = null
    }
}
