package com.example.ui.library.core

import com.example.core.model.AppFolderStyle
import com.example.core.model.AppGridMode
import com.example.core.model.AppSortCriteria
import com.example.core.model.LibraryScanProgress
import com.example.core.model.Song

enum class SongAction {
    PLAY,
    PLAY_NEXT,
    ADD_TO_QUEUE,
    ADD_TO_PLAYLIST,
    TOGGLE_FAVORITE,
    REMOVE_FROM_FAVORITES,
    GO_TO_ARTIST,
    GO_TO_ALBUM,
    GO_TO_FOLDER,
    SONG_INFO,
    DELETE
}

sealed interface LibraryAction {
    data class Search(val query: String) : LibraryAction
    data class UpdateSongSort(val criteria: AppSortCriteria, val isAscending: Boolean) : LibraryAction
    data class UpdateArtistSort(val criteria: AppSortCriteria, val isAscending: Boolean) : LibraryAction
    data class UpdateArtistGridMode(val mode: AppGridMode) : LibraryAction
    data class UpdateAlbumSort(val criteria: AppSortCriteria, val isAscending: Boolean) : LibraryAction
    data class UpdateAlbumGridMode(val mode: AppGridMode) : LibraryAction
    data class SelectArtist(val artistName: String?) : LibraryAction
    data class SelectAlbum(val albumName: String?) : LibraryAction
    data object ClearDetail : LibraryAction
    data class SelectFolder(val path: String) : LibraryAction
    data class ChangeFolderStyle(val style: AppFolderStyle) : LibraryAction
    data class ChangeRootFolderDisplay(val display: String) : LibraryAction

    // Playback requests (dispatched to Player boundary)
    data class PlaySong(val song: Song, val playlist: List<Song> = emptyList()) : LibraryAction
    data class PlayArtistSongs(val songs: List<Song>, val shuffle: Boolean = false) : LibraryAction
    data class PlayAlbumSongs(val songs: List<Song>, val shuffle: Boolean = false) : LibraryAction
    data class PlayFolder(val folderPath: String, val shuffle: Boolean = false) : LibraryAction
    data class PlayNext(val song: Song) : LibraryAction
    data class AddToQueue(val song: Song) : LibraryAction
    data class PlayNextBatch(val songs: List<Song>) : LibraryAction
    data class AddToQueueBatch(val songs: List<Song>) : LibraryAction
    data class ShuffleAll(val playlist: List<Song>) : LibraryAction
    data class ToggleFavorite(val songId: Long) : LibraryAction
    data class HandleSongAction(val action: SongAction, val song: Song) : LibraryAction

    // Library sync & scan actions
    data class LoadSongs(
        val isRefreshing: Boolean = false,
        val showProgressDialog: Boolean = false,
        val progressTitle: String = "Scanning Media Library"
    ) : LibraryAction
    data object RefreshLibrary : LibraryAction
    data object DismissScanProgress : LibraryAction
    data class OnPermissionResult(val isGranted: Boolean) : LibraryAction
}

data class LibraryUiState(
    val songs: List<Song> = emptyList(),
    val favoriteIds: Set<Long> = emptySet(),
    val searchQuery: String = "",
    val songSortCriteria: AppSortCriteria = AppSortCriteria.TITLE,
    val songIsAscending: Boolean = true,
    val artistSortCriteria: AppSortCriteria = AppSortCriteria.ARTIST,
    val artistIsAscending: Boolean = true,
    val artistGridMode: AppGridMode = AppGridMode.GRID_2,
    val albumSortCriteria: AppSortCriteria = AppSortCriteria.ALBUM,
    val albumIsAscending: Boolean = true,
    val albumGridMode: AppGridMode = AppGridMode.GRID_2,
    val selectedArtistName: String? = null,
    val selectedAlbumName: String? = null,
    val currentFolderRelativePath: String = "",
    val folderStyle: AppFolderStyle = AppFolderStyle.ICON,
    val rootFolderDisplay: String = "Primary Storage",
    val totalSongCount: Int = 0,
    val totalLibrarySizeFormatted: String = "0 MB",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasAudioPermission: Boolean = true,
    val requiredPermission: String = "",
    val scanProgress: LibraryScanProgress = LibraryScanProgress()
)
