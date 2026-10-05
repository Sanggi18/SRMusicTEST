package com.example.ui.sidebar.core
import com.example.ui.common.components.*

import android.net.Uri
import com.example.core.data.database.PlaylistEntity
import com.example.core.data.playlist.PlaylistFileManager.PlaylistFormat
import com.example.core.model.Song
import com.example.ui.library.core.SongAction

enum class PlaylistSortCriteria(val displayName: String) {
    NAME("Name"),
    RECENT_ADD("Recent Add"),
    ITEM_COUNT("Item Playlist")
}

sealed interface CollectionAction {
    // Favorites
    data class ToggleFavorite(val songId: Long) : CollectionAction

    // History
    data object ClearHistory : CollectionAction

    // Playlist CRUD & mutations
    data class CreatePlaylist(val name: String, val onCreated: ((Long) -> Unit)? = null) : CollectionAction
    data class RenamePlaylist(val playlistId: Long, val newName: String) : CollectionAction
    data class DeletePlaylist(val playlistId: Long) : CollectionAction
    data class DeleteMultiplePlaylists(val playlistIds: List<Long>) : CollectionAction
    data class AddSongToPlaylist(val playlistId: Long, val songId: Long) : CollectionAction
    data class AddSongsToPlaylist(val playlistId: Long, val songIds: List<Long>, val onComplete: (() -> Unit)? = null) : CollectionAction
    data class RemoveSongFromPlaylist(val playlistId: Long, val songId: Long) : CollectionAction

    // Playlist UI state
    data class ToggleExpandPlaylist(val playlistId: Long) : CollectionAction
    data class SetPlaylistSort(val criteria: PlaylistSortCriteria, val isAscending: Boolean) : CollectionAction

    // Playlist Import & Export
    data class ExportPlaylist(
        val uri: Uri,
        val playlist: PlaylistEntity,
        val format: PlaylistFormat,
        val onResult: ((Boolean, String) -> Unit)? = null
    ) : CollectionAction

    data class ImportPlaylist(
        val uri: Uri,
        val onResult: ((Boolean, String) -> Unit)? = null
    ) : CollectionAction

    // Playback and song action routing
    data class PlaySong(val song: Song, val playlist: List<Song> = emptyList()) : CollectionAction
    data class PlayNext(val song: Song) : CollectionAction
    data class AddToQueue(val song: Song) : CollectionAction
    data class HandleSongAction(val action: SongAction, val song: Song, val playlistContext: List<Song>? = null) : CollectionAction
}

data class CollectionUiState(
    val favoriteSongs: List<Song> = emptyList(),
    val historySongs: List<Song> = emptyList(),
    val historySongIds: List<Long> = emptyList(),
    val playlists: List<PlaylistEntity> = emptyList(),
    val playlistSongs: Map<Long, List<Song>> = emptyMap(),
    val expandedPlaylistIds: Set<Long> = emptySet(),
    val playlistSortCriteria: PlaylistSortCriteria = PlaylistSortCriteria.NAME,
    val playlistIsAscending: Boolean = true,
    val allSongs: List<Song> = emptyList(),
    val isLoading: Boolean = false
) {
    val sortedPlaylists: List<PlaylistEntity>
        get() {
            val list = when (playlistSortCriteria) {
                PlaylistSortCriteria.NAME -> playlists.sortedBy { it.name.lowercase() }
                PlaylistSortCriteria.RECENT_ADD -> playlists.sortedByDescending { it.createdAt }
                PlaylistSortCriteria.ITEM_COUNT -> playlists.sortedByDescending { playlistSongs[it.id]?.size ?: 0 }
            }
            return if (playlistIsAscending) list else list.reversed()
        }
}
