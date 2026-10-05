package com.example.ui.home.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.data.preferences.AppPreferences
import com.example.core.data.repository.FavoritesRepository
import com.example.core.data.repository.HistoryRepository
import com.example.core.data.repository.PlaybackRepository
import com.example.core.data.repository.SongRepository
import com.example.core.model.Song
import com.example.ui.library.core.SongAction
import com.example.ui.player.core.PlayerAction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class HomeStats(
    val totalPlayCount: Int,
    val totalListenedSeconds: Long,
    val songPlayCounts: Map<Long, Int>
)

private data class HomeOffsets(
    val remixOffset: Int,
    val genreOffset: Int,
    val selectedDailyGenreIndex: Int,
    val shuffleOffset: Int
)

class HomeViewModel(
    private val songRepository: SongRepository,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val appPreferences: AppPreferences,
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    private val _playerEvents = MutableSharedFlow<PlayerAction>(extraBufferCapacity = 64)
    val playerEvents: SharedFlow<PlayerAction> = _playerEvents.asSharedFlow()

    private val _shuffleTracks = MutableStateFlow<List<Song>>(emptyList())
    private var isShuffleSessionActive = false

    private val _remixOffset = MutableStateFlow(0)
    private val _genreOffset = MutableStateFlow(0)
    private val _selectedDailyGenreIndex = MutableStateFlow(0)
    private val _shuffleOffset = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            songRepository.allSongsFlow.collect { songs ->
                if (songs.isNotEmpty() && _shuffleTracks.value.isEmpty()) {
                    val savedIds = appPreferences.getShuffleTrackIds()
                    val savedSongs = savedIds.mapNotNull { id -> songs.find { it.id == id } }
                    if (savedSongs.isNotEmpty() && (savedSongs.size == savedIds.size || savedSongs.size == 14 || savedSongs.size == songs.size)) {
                        _shuffleTracks.value = savedSongs
                    } else {
                        val newBatch = if (songs.size <= 14) songs.shuffled() else songs.shuffled().take(14)
                        _shuffleTracks.value = newBatch
                        appPreferences.setShuffleTrackIds(newBatch.map { it.id })
                    }
                }
            }
        }

        viewModelScope.launch {
            playbackRepository.onPlaybackEndedEvent.collect {
                if (isShuffleSessionActive) {
                    val allSongs = songRepository.getAllSongs()
                    if (allSongs.isNotEmpty()) {
                        val prevBatchIds = _shuffleTracks.value.map { it.id }.toSet()
                        val candidates = allSongs.filter { it.id !in prevBatchIds }
                        val nextBatch = if (candidates.size >= 14) {
                            candidates.shuffled().take(14)
                        } else if (allSongs.size <= 14) {
                            allSongs.shuffled()
                        } else {
                            (candidates.shuffled() + allSongs.filter { it.id in prevBatchIds }.shuffled()).take(14)
                        }
                        _shuffleTracks.value = nextBatch
                        appPreferences.setShuffleTrackIds(nextBatch.map { it.id })
                        playbackRepository.playSong(nextBatch[0], nextBatch, isShuffleSession = true)
                    }
                }
            }
        }
    }

    private val statsFlow = combine(
        historyRepository.totalPlayCountFlow,
        historyRepository.totalListenedSecondsFlow,
        historyRepository.allPlayCountsFlow
    ) { count, seconds, countsMap ->
        HomeStats(count, seconds, countsMap)
    }

    private val offsetsFlow = combine(
        _remixOffset,
        _genreOffset,
        _selectedDailyGenreIndex,
        _shuffleOffset
    ) { r, g, s, manualSh ->
        HomeOffsets(r, g, s, manualSh)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        songRepository.allSongsFlow,
        historyRepository.historySongIdsFlow,
        statsFlow,
        offsetsFlow,
        _shuffleTracks
    ) { allSongs, historySongIds, stats, offsets, currentShuffleTracks ->
        val installSeed = 42L // stable seed
        val historySongs = historySongIds.mapNotNull { id -> allSongs.find { it.id == id } }
        val recentSongs = allSongs.sortedByDescending { it.dateModified }.take(50)
        val shuffleTracks = currentShuffleTracks
        val remixQueue = HomeContentGenerator.generateRemixQueue(allSongs, installSeed, offsets.remixOffset)
        val dailyGenres = HomeContentGenerator.generateDailyGenres(allSongs, installSeed, offsets.genreOffset)

        val top1Artist = HomeContentGenerator.computeTopArtist(allSongs, stats.songPlayCounts)
        val top1Album = HomeContentGenerator.computeTopAlbum(allSongs, stats.songPlayCounts)
        val top1Song = HomeContentGenerator.computeTopSong(allSongs, stats.songPlayCounts)
        val topMostPlayed = allSongs.sortedByDescending { stats.songPlayCounts[it.id] ?: 0 }.take(20)

        val albumsCount = allSongs.map { it.album }.distinct().size
        val artistsCount = allSongs.map { it.artist }.distinct().size

        HomeUiState(
            songs = allSongs,
            historySongs = historySongs,
            totalPlayCount = stats.totalPlayCount,
            totalListenedSeconds = stats.totalListenedSeconds,
            songPlayCounts = stats.songPlayCounts,
            shuffleTracks = shuffleTracks,
            remixQueue = remixQueue,
            dailyGenres = dailyGenres,
            selectedDailyGenreIndex = offsets.selectedDailyGenreIndex,
            recentSongs = recentSongs,
            top1Artist = top1Artist,
            top1Album = top1Album,
            top1Song = top1Song,
            topMostPlayed = topMostPlayed,
            totalAlbumsCount = albumsCount,
            totalArtistsCount = artistsCount,
            isLoading = allSongs.isEmpty(),
            isRefreshing = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.PlaySong -> {
                isShuffleSessionActive = false
                _playerEvents.tryEmit(PlayerAction.PlaySong(action.song, action.playlist))
            }
            is HomeAction.PlayShuffleSession -> {
                isShuffleSessionActive = true
                val batch = if (_shuffleTracks.value.isNotEmpty()) _shuffleTracks.value else action.tracks
                _shuffleTracks.value = batch
                _playerEvents.tryEmit(PlayerAction.PlayShuffleSession(action.startSong, sessionBatch = batch))
            }
            is HomeAction.RefreshShuffleTracks -> {
                viewModelScope.launch {
                    val allSongs = songRepository.getAllSongs()
                    if (allSongs.isNotEmpty()) {
                        val currentIds = _shuffleTracks.value.map { it.id }.toSet()
                        val candidates = allSongs.filter { it.id !in currentIds }
                        val newBatch = if (candidates.size >= 14) {
                            candidates.shuffled().take(14)
                        } else if (allSongs.size <= 14) {
                            allSongs.shuffled()
                        } else {
                            (candidates.shuffled() + allSongs.filter { it.id in currentIds }.shuffled()).take(14)
                        }
                        _shuffleTracks.value = newBatch
                        appPreferences.setShuffleTrackIds(newBatch.map { it.id })
                    }
                }
            }
            is HomeAction.RefreshRemixQueue -> {
                _remixOffset.value += 1
            }
            is HomeAction.NextDailyGenre -> {
                _genreOffset.value += 1
            }
            is HomeAction.SelectDailyGenre -> {
                _selectedDailyGenreIndex.value = action.index
            }
            is HomeAction.ToggleFavorite -> {
                viewModelScope.launch {
                    favoritesRepository.toggleFavorite(action.songId)
                }
            }
            is HomeAction.PlayNext -> {
                _playerEvents.tryEmit(PlayerAction.PlayNext(action.song))
            }
            is HomeAction.AddToQueue -> {
                _playerEvents.tryEmit(PlayerAction.AddToQueue(action.song))
            }
            is HomeAction.HandleSongAction -> {
                when (action.action) {
                    SongAction.PLAY -> {
                        _playerEvents.tryEmit(PlayerAction.PlaySong(action.song, listOf(action.song)))
                    }
                    SongAction.PLAY_NEXT -> {
                        _playerEvents.tryEmit(PlayerAction.PlayNext(action.song))
                    }
                    SongAction.ADD_TO_QUEUE -> {
                        _playerEvents.tryEmit(PlayerAction.AddToQueue(action.song))
                    }
                    SongAction.TOGGLE_FAVORITE -> {
                        viewModelScope.launch {
                            favoritesRepository.toggleFavorite(action.song.id)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            songRepository: SongRepository,
            historyRepository: HistoryRepository,
            favoritesRepository: FavoritesRepository,
            appPreferences: AppPreferences,
            playbackRepository: PlaybackRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(
                        songRepository = songRepository,
                        historyRepository = historyRepository,
                        favoritesRepository = favoritesRepository,
                        appPreferences = appPreferences,
                        playbackRepository = playbackRepository
                    ) as T
                }
            }
    }
}
