package com.example.ui.library.core
import com.example.ui.library.components.*
import com.example.ui.common.components.*

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.data.preferences.AppPreferences
import com.example.core.data.repository.FavoritesRepository
import com.example.core.data.repository.LibraryRepository
import com.example.core.data.repository.SongRepository
import com.example.ui.library.folder.FolderTreeParser
import com.example.ui.player.core.PlayerAction
import com.example.core.model.Song
import com.example.core.model.AppFolderStyle
import com.example.core.model.AppGridMode
import com.example.core.model.AppSortCriteria
import com.example.core.model.LibraryScanProgress
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class SortAndFilterParams(
    val searchQuery: String,
    val songSortCriteria: AppSortCriteria,
    val songIsAscending: Boolean,
    val artistSortCriteria: AppSortCriteria,
    val artistIsAscending: Boolean,
    val artistGridMode: AppGridMode,
    val albumSortCriteria: AppSortCriteria,
    val albumIsAscending: Boolean,
    val albumGridMode: AppGridMode
)

private data class NavigationParams(
    val selectedArtistName: String?,
    val selectedAlbumName: String?,
    val currentFolderRelativePath: String,
    val folderStyle: AppFolderStyle,
    val rootFolderDisplay: String
)

private data class LibraryStatus(
    val isRefreshing: Boolean,
    val scanProgress: LibraryScanProgress,
    val hasPermission: Boolean
)

class LibraryViewModel(
    application: Application,
    private val songRepository: SongRepository,
    private val libraryRepository: LibraryRepository,
    private val favoritesRepository: FavoritesRepository,
    private val appPreferences: AppPreferences
) : AndroidViewModel(application) {

    private val _playerEvents = MutableSharedFlow<PlayerAction>(extraBufferCapacity = 64)
    val playerEvents: SharedFlow<PlayerAction> = _playerEvents.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _songSortCriteria = MutableStateFlow(appPreferences.getSongSortCriteria())
    val songSortCriteria: StateFlow<AppSortCriteria> = _songSortCriteria.asStateFlow()

    private val _songIsAscending = MutableStateFlow(appPreferences.getSongIsAscending())
    val songIsAscending: StateFlow<Boolean> = _songIsAscending.asStateFlow()

    private val _artistSortCriteria = MutableStateFlow(appPreferences.getArtistSortCriteria())
    val artistSortCriteria: StateFlow<AppSortCriteria> = _artistSortCriteria.asStateFlow()

    private val _artistIsAscending = MutableStateFlow(appPreferences.getArtistIsAscending())
    val artistIsAscending: StateFlow<Boolean> = _artistIsAscending.asStateFlow()

    private val _artistGridMode = MutableStateFlow(appPreferences.getArtistGridMode())
    val artistGridMode: StateFlow<AppGridMode> = _artistGridMode.asStateFlow()

    private val _albumSortCriteria = MutableStateFlow(appPreferences.getAlbumSortCriteria())
    val albumSortCriteria: StateFlow<AppSortCriteria> = _albumSortCriteria.asStateFlow()

    private val _albumIsAscending = MutableStateFlow(appPreferences.getAlbumIsAscending())
    val albumIsAscending: StateFlow<Boolean> = _albumIsAscending.asStateFlow()

    private val _albumGridMode = MutableStateFlow(appPreferences.getAlbumGridMode())
    val albumGridMode: StateFlow<AppGridMode> = _albumGridMode.asStateFlow()

    private val _selectedArtistName = MutableStateFlow<String?>(null)
    val selectedArtistName: StateFlow<String?> = _selectedArtistName.asStateFlow()

    private val _selectedAlbumName = MutableStateFlow<String?>(null)
    val selectedAlbumName: StateFlow<String?> = _selectedAlbumName.asStateFlow()

    private val _currentFolderRelativePath = MutableStateFlow("")
    val currentFolderRelativePath: StateFlow<String> = _currentFolderRelativePath.asStateFlow()

    private val _folderStyle = MutableStateFlow(appPreferences.getFolderStyle())
    val folderStyle: StateFlow<AppFolderStyle> = _folderStyle.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    private val _scanProgress = MutableStateFlow(LibraryScanProgress())
    val scanProgress: StateFlow<LibraryScanProgress> = _scanProgress.asStateFlow()

    private val _hasAudioPermission = MutableStateFlow(hasAudioPermission())

    init {
        if (hasAudioPermission()) {
            loadSongs()
        }
    }

    fun getRequiredAudioPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    fun hasAudioPermission(): Boolean {
        val permission = getRequiredAudioPermission()
        return ContextCompat.checkSelfPermission(
            getApplication(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun onPermissionResult(isGranted: Boolean) {
        _hasAudioPermission.value = isGranted
        if (isGranted) {
            loadSongs(showProgressDialog = true, progressTitle = "Initial Setup")
        }
    }

    fun refreshLibrary() {
        if (!hasAudioPermission()) {
            _hasAudioPermission.value = false
            return
        }
        loadSongs(showProgressDialog = true, progressTitle = "Scanning Media...")
    }

    private fun loadSongs(showProgressDialog: Boolean = false, progressTitle: String = "Scanning Media...") {
        viewModelScope.launch {
            _isRefreshing.value = true
            if (showProgressDialog) {
                _scanProgress.value = LibraryScanProgress(
                    isScanning = true,
                    title = progressTitle,
                    currentCount = 0,
                    isFinished = false
                )
            }
            try {
                libraryRepository.syncLibrary(
                    onProgress = { count ->
                        if (showProgressDialog) {
                            _scanProgress.value = _scanProgress.value.copy(currentCount = count)
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
                if (showProgressDialog) {
                    _scanProgress.value = _scanProgress.value.copy(
                        isScanning = false,
                        isFinished = true
                    )
                } else {
                    _scanProgress.value = LibraryScanProgress(isScanning = false)
                }
            }
        }
    }

    private val sortAndFilterFlow = combine(
        combine(_searchQuery, _songSortCriteria, _songIsAscending) { q, sc, sa -> Triple(q, sc, sa) },
        combine(_artistSortCriteria, _artistIsAscending, _artistGridMode) { ac, aa, ag -> Triple(ac, aa, ag) },
        combine(_albumSortCriteria, _albumIsAscending, _albumGridMode) { lc, la, lg -> Triple(lc, la, lg) }
    ) { (query, songSort, songAsc), (artistSort, artistAsc, artistGrid), (albumSort, albumAsc, albumGrid) ->
        SortAndFilterParams(
            searchQuery = query,
            songSortCriteria = songSort,
            songIsAscending = songAsc,
            artistSortCriteria = artistSort,
            artistIsAscending = artistAsc,
            artistGridMode = artistGrid,
            albumSortCriteria = albumSort,
            albumIsAscending = albumAsc,
            albumGridMode = albumGrid
        )
    }

    private val navigationFlow = combine(
        _selectedArtistName,
        _selectedAlbumName,
        _currentFolderRelativePath,
        _folderStyle,
        appPreferences.rootFolderDisplayFlow
    ) { artist, album, folder, folderStyle, rootDisplay ->
        NavigationParams(
            selectedArtistName = artist,
            selectedAlbumName = album,
            currentFolderRelativePath = folder,
            folderStyle = folderStyle,
            rootFolderDisplay = rootDisplay
        )
    }

    private val statusFlow = combine(
        _isRefreshing,
        _scanProgress,
        _hasAudioPermission
    ) { r, s, p ->
        LibraryStatus(r, s, p)
    }

    val uiState: StateFlow<LibraryUiState> = combine(
        songRepository.allSongsFlow,
        favoritesRepository.favoriteSongsFlow,
        sortAndFilterFlow,
        navigationFlow,
        statusFlow
    ) { songs, favs, sf, nav, status ->
        val favIds = favs.map { it.id }.toSet()
        val totalCount = songs.size
        val totalSizeFormatted = libraryRepository.formatTotalLibrarySize(songs)

        LibraryUiState(
            songs = songs,
            favoriteIds = favIds,
            searchQuery = sf.searchQuery,
            songSortCriteria = sf.songSortCriteria,
            songIsAscending = sf.songIsAscending,
            artistSortCriteria = sf.artistSortCriteria,
            artistIsAscending = sf.artistIsAscending,
            artistGridMode = sf.artistGridMode,
            albumSortCriteria = sf.albumSortCriteria,
            albumIsAscending = sf.albumIsAscending,
            albumGridMode = sf.albumGridMode,
            selectedArtistName = nav.selectedArtistName,
            selectedAlbumName = nav.selectedAlbumName,
            currentFolderRelativePath = nav.currentFolderRelativePath,
            folderStyle = nav.folderStyle,
            rootFolderDisplay = nav.rootFolderDisplay,
            totalSongCount = totalCount,
            totalLibrarySizeFormatted = totalSizeFormatted,
            isLoading = songs.isEmpty() && (status.isRefreshing || status.scanProgress.isScanning),
            isRefreshing = status.isRefreshing,
            hasAudioPermission = status.hasPermission,
            requiredPermission = getRequiredAudioPermission(),
            scanProgress = status.scanProgress
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryUiState())

    fun onAction(action: LibraryAction) {
        when (action) {
            is LibraryAction.Search -> _searchQuery.value = action.query
            is LibraryAction.UpdateSongSort -> {
                _songSortCriteria.value = action.criteria
                _songIsAscending.value = action.isAscending
                appPreferences.setSongSortCriteria(action.criteria)
                appPreferences.setSongIsAscending(action.isAscending)
            }
            is LibraryAction.UpdateArtistSort -> {
                _artistSortCriteria.value = action.criteria
                _artistIsAscending.value = action.isAscending
                appPreferences.setArtistSortCriteria(action.criteria)
                appPreferences.setArtistIsAscending(action.isAscending)
            }
            is LibraryAction.UpdateArtistGridMode -> {
                _artistGridMode.value = action.mode
                appPreferences.setArtistGridMode(action.mode)
            }
            is LibraryAction.UpdateAlbumSort -> {
                _albumSortCriteria.value = action.criteria
                _albumIsAscending.value = action.isAscending
                appPreferences.setAlbumSortCriteria(action.criteria)
                appPreferences.setAlbumIsAscending(action.isAscending)
            }
            is LibraryAction.UpdateAlbumGridMode -> {
                _albumGridMode.value = action.mode
                appPreferences.setAlbumGridMode(action.mode)
            }
            is LibraryAction.SelectArtist -> _selectedArtistName.value = action.artistName
            is LibraryAction.SelectAlbum -> _selectedAlbumName.value = action.albumName
            is LibraryAction.ClearDetail -> {
                _selectedArtistName.value = null
                _selectedAlbumName.value = null
            }
            is LibraryAction.SelectFolder -> _currentFolderRelativePath.value = action.path
            is LibraryAction.ChangeFolderStyle -> {
                _folderStyle.value = action.style
                appPreferences.setFolderStyle(action.style)
            }
            is LibraryAction.ChangeRootFolderDisplay -> {
                appPreferences.setRootFolderDisplay(action.display)
            }
            is LibraryAction.ToggleFavorite -> {
                viewModelScope.launch {
                    favoritesRepository.toggleFavorite(action.songId)
                }
            }
            is LibraryAction.RefreshLibrary -> refreshLibrary()
            is LibraryAction.LoadSongs -> {
                loadSongs(action.showProgressDialog, action.progressTitle)
            }
            is LibraryAction.DismissScanProgress -> {
                _scanProgress.value = LibraryScanProgress(isScanning = false)
            }
            is LibraryAction.PlaySong -> {
                _playerEvents.tryEmit(PlayerAction.PlaySong(action.song, action.playlist))
            }
            is LibraryAction.PlayNext -> {
                _playerEvents.tryEmit(PlayerAction.PlayNext(action.song))
            }
            is LibraryAction.AddToQueue -> {
                _playerEvents.tryEmit(PlayerAction.AddToQueue(action.song))
            }
            is LibraryAction.PlayNextBatch -> {
                action.songs.reversed().forEach { _playerEvents.tryEmit(PlayerAction.PlayNext(it)) }
            }
            is LibraryAction.AddToQueueBatch -> {
                action.songs.forEach { _playerEvents.tryEmit(PlayerAction.AddToQueue(it)) }
            }
            is LibraryAction.ShuffleAll -> {
                if (action.playlist.isNotEmpty()) {
                    val shuffled = action.playlist.shuffled()
                    _playerEvents.tryEmit(PlayerAction.PlaySong(shuffled.first(), shuffled))
                }
            }
            is LibraryAction.HandleSongAction -> {
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
            is LibraryAction.PlayArtistSongs -> {
                val songs = if (action.shuffle) action.songs.shuffled() else action.songs
                if (songs.isNotEmpty()) {
                    _playerEvents.tryEmit(PlayerAction.PlaySong(songs.first(), songs))
                }
            }
            is LibraryAction.PlayAlbumSongs -> {
                val songs = if (action.shuffle) action.songs.shuffled() else action.songs
                if (songs.isNotEmpty()) {
                    _playerEvents.tryEmit(PlayerAction.PlaySong(songs.first(), songs))
                }
            }
            is LibraryAction.PlayFolder -> {
                val folderSongs = FolderTreeParser.getSongsInFolder(uiState.value.songs, action.folderPath)
                val songs = if (action.shuffle) folderSongs.shuffled() else folderSongs
                if (songs.isNotEmpty()) {
                    _playerEvents.tryEmit(PlayerAction.PlaySong(songs.first(), songs))
                }
            }
            is LibraryAction.OnPermissionResult -> {
                onPermissionResult(action.isGranted)
            }
        }
    }

    companion object {
        fun provideFactory(
            application: Application,
            songRepository: SongRepository,
            libraryRepository: LibraryRepository,
            favoritesRepository: FavoritesRepository,
            appPreferences: AppPreferences
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LibraryViewModel(
                        application = application,
                        songRepository = songRepository,
                        libraryRepository = libraryRepository,
                        favoritesRepository = favoritesRepository,
                        appPreferences = appPreferences
                    ) as T
                }
            }
    }
}
