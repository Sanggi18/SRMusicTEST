package com.example.ui.sidebar.core
import com.example.ui.common.components.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.data.repository.FavoritesRepository
import com.example.core.data.repository.HistoryRepository
import com.example.core.data.repository.PlaylistRepository
import com.example.core.data.repository.SongRepository
import com.example.core.model.Song
import com.example.ui.library.core.SongAction
import com.example.ui.player.core.PlayerAction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val historyRepository: HistoryRepository,
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository
) : ViewModel() {

    private val _playerEvents = MutableSharedFlow<PlayerAction>(extraBufferCapacity = 64)
    val playerEvents: SharedFlow<PlayerAction> = _playerEvents.asSharedFlow()

    private val _expandedPlaylistIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _playlistSortCriteria = MutableStateFlow(PlaylistSortCriteria.NAME)
    private val _playlistIsAscending = MutableStateFlow(true)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<CollectionUiState> = combine(
        favoritesRepository.favoriteSongsFlow,
        historyRepository.historySongIdsFlow,
        playlistRepository.playlistsFlow,
        playlistRepository.allCrossRefsFlow,
        songRepository.allSongsFlow,
        _expandedPlaylistIds,
        _playlistSortCriteria,
        _playlistIsAscending
    ) { args: Array<Any?> ->
        val favs = args[0] as List<Song>
        val histIds = args[1] as List<Long>
        val playlists = args[2] as List<com.example.core.data.database.PlaylistEntity>
        val crossRefs = args[3] as List<com.example.core.data.database.PlaylistSongCrossRef>
        val songs = args[4] as List<Song>
        val expandedIds = args[5] as Set<Long>
        val sortCriteria = args[6] as PlaylistSortCriteria
        val isAscending = args[7] as Boolean

        val songMap = songs.associateBy { it.id }
        val playlistSongsMap = crossRefs.groupBy { it.playlistId }
            .mapValues { entry -> entry.value.mapNotNull { ref -> songMap[ref.songId] } }
        val historySongs = histIds.mapNotNull { id -> songMap[id] }

        CollectionUiState(
            favoriteSongs = favs,
            historySongs = historySongs,
            historySongIds = histIds,
            playlists = playlists,
            playlistSongs = playlistSongsMap,
            expandedPlaylistIds = expandedIds,
            playlistSortCriteria = sortCriteria,
            playlistIsAscending = isAscending,
            allSongs = songs,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CollectionUiState()
    )

    fun onAction(action: CollectionAction) {
        when (action) {
            is CollectionAction.ToggleFavorite -> {
                viewModelScope.launch {
                    favoritesRepository.toggleFavorite(action.songId)
                }
            }
            is CollectionAction.ClearHistory -> {
                viewModelScope.launch {
                    historyRepository.clearHistory()
                }
            }
            is CollectionAction.CreatePlaylist -> {
                viewModelScope.launch {
                    val id = playlistRepository.createPlaylist(action.name)
                    action.onCreated?.invoke(id)
                }
            }
            is CollectionAction.RenamePlaylist -> {
                viewModelScope.launch {
                    playlistRepository.renamePlaylist(action.playlistId, action.newName)
                }
            }
            is CollectionAction.DeletePlaylist -> {
                viewModelScope.launch {
                    playlistRepository.deletePlaylist(action.playlistId)
                }
            }
            is CollectionAction.DeleteMultiplePlaylists -> {
                viewModelScope.launch {
                    playlistRepository.deleteMultiplePlaylists(action.playlistIds)
                }
            }
            is CollectionAction.AddSongToPlaylist -> {
                viewModelScope.launch {
                    playlistRepository.addSongToPlaylist(action.playlistId, action.songId)
                }
            }
            is CollectionAction.AddSongsToPlaylist -> {
                viewModelScope.launch {
                    playlistRepository.addSongsToPlaylist(action.playlistId, action.songIds)
                    action.onComplete?.invoke()
                }
            }
            is CollectionAction.RemoveSongFromPlaylist -> {
                viewModelScope.launch {
                    playlistRepository.removeSongFromPlaylist(action.playlistId, action.songId)
                }
            }
            is CollectionAction.ToggleExpandPlaylist -> {
                val current = _expandedPlaylistIds.value.toMutableSet()
                if (current.contains(action.playlistId)) {
                    current.remove(action.playlistId)
                } else {
                    current.add(action.playlistId)
                }
                _expandedPlaylistIds.value = current
            }
            is CollectionAction.SetPlaylistSort -> {
                _playlistSortCriteria.value = action.criteria
                _playlistIsAscending.value = action.isAscending
            }
            is CollectionAction.ExportPlaylist -> {
                viewModelScope.launch {
                    val songs = uiState.value.playlistSongs[action.playlist.id] ?: emptyList()
                    val res = playlistRepository.exportPlaylist(
                        uri = action.uri,
                        playlistName = action.playlist.name,
                        songs = songs,
                        formatName = action.format.name
                    )
                    action.onResult?.invoke(res.isSuccess, res.getOrNull() ?: res.exceptionOrNull()?.message ?: "Export failed")
                }
            }
            is CollectionAction.ImportPlaylist -> {
                viewModelScope.launch {
                    val allSongs = uiState.value.allSongs
                    val res = playlistRepository.importPlaylist(action.uri, allSongs)
                    if (res.isSuccess) {
                        val data = res.getOrNull()
                        if (data != null && data.matchedSongs.isNotEmpty()) {
                            val playlistId = playlistRepository.createPlaylist(data.playlistName)
                            playlistRepository.addSongsToPlaylist(playlistId, data.matchedSongs.map { it.id })
                            action.onResult?.invoke(true, "Imported playlist '${data.playlistName}' (${data.matchedSongs.size} tracks)")
                        } else {
                            action.onResult?.invoke(false, "No matching audio tracks found in library for import.")
                        }
                    } else {
                        action.onResult?.invoke(false, res.exceptionOrNull()?.message ?: "Import failed")
                    }
                }
            }
            is CollectionAction.PlaySong -> {
                _playerEvents.tryEmit(PlayerAction.PlaySong(action.song, action.playlist))
            }
            is CollectionAction.PlayNext -> {
                _playerEvents.tryEmit(PlayerAction.PlayNext(action.song))
            }
            is CollectionAction.AddToQueue -> {
                _playerEvents.tryEmit(PlayerAction.AddToQueue(action.song))
            }
            is CollectionAction.HandleSongAction -> {
                when (action.action) {
                    SongAction.PLAY -> {
                        _playerEvents.tryEmit(PlayerAction.PlaySong(action.song, action.playlistContext ?: listOf(action.song)))
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
            favoritesRepository: FavoritesRepository,
            historyRepository: HistoryRepository,
            playlistRepository: PlaylistRepository,
            songRepository: SongRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CollectionViewModel(
                    favoritesRepository = favoritesRepository,
                    historyRepository = historyRepository,
                    playlistRepository = playlistRepository,
                    songRepository = songRepository
                ) as T
            }
        }
    }
}
