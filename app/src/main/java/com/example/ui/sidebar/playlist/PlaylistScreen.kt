package com.example.ui.sidebar.playlist
import com.example.ui.sidebar.core.*
import com.example.ui.common.components.*

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.data.database.PlaylistEntity
import com.example.core.data.playlist.PlaylistFileManager.PlaylistFormat
import com.example.ui.sidebar.core.CollectionAction
import com.example.ui.sidebar.core.CollectionUiState
import com.example.ui.library.core.SongAction
import com.example.core.model.PlaybackInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.library.song.SongItem
import com.example.core.model.Song

@Composable
fun PlaylistList(
    uiState: CollectionUiState,
    playbackInfo: PlaybackInfo,
    onAction: (CollectionAction) -> Unit,
    onRenameClick: (PlaylistEntity) -> Unit,
    onDeleteClick: (PlaylistEntity) -> Unit,
    onExportClick: (PlaylistEntity) -> Unit,
    onAddSongsClick: (PlaylistEntity) -> Unit,
    modifier: Modifier = Modifier,
    onSongActionExternal: ((SongAction, Song) -> Unit)? = null
) {
    val playlists = uiState.sortedPlaylists

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        items(playlists, key = { it.id }) { playlist ->
            val songs = uiState.playlistSongs[playlist.id] ?: emptyList()
            val isExpanded = uiState.expandedPlaylistIds.contains(playlist.id)

            PlaylistItemCard(
                playlist = playlist,
                songs = songs,
                isExpanded = isExpanded,
                playbackInfo = playbackInfo,
                favoriteSongs = uiState.favoriteSongs,
                onToggleExpand = { onAction(CollectionAction.ToggleExpandPlaylist(playlist.id)) },
                onPlayPlaylist = {
                    if (songs.isNotEmpty()) {
                        onAction(CollectionAction.PlaySong(songs.first(), songs))
                    }
                },
                onRenameClick = { onRenameClick(playlist) },
                onDeleteClick = { onDeleteClick(playlist) },
                onExportClick = { onExportClick(playlist) },
                onAddSongsClick = { onAddSongsClick(playlist) },
                onSongClick = { song -> onAction(CollectionAction.PlaySong(song, songs)) },
                onRemoveSong = { songId -> onAction(CollectionAction.RemoveSongFromPlaylist(playlist.id, songId)) },
                onToggleFavorite = { songId -> onAction(CollectionAction.ToggleFavorite(songId)) },
                onSongActionExternal = onSongActionExternal
            )
        }
    }
}

@Composable
private fun PlaylistItemCard(
    playlist: PlaylistEntity,
    songs: List<Song>,
    isExpanded: Boolean,
    playbackInfo: PlaybackInfo,
    favoriteSongs: List<Song>,
    onToggleExpand: () -> Unit,
    onPlayPlaylist: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onExportClick: () -> Unit,
    onAddSongsClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onRemoveSong: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onSongActionExternal: ((SongAction, Song) -> Unit)?
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.QueueMusic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${songs.size} tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (songs.isNotEmpty()) {
                IconButton(onClick = onPlayPlaylist) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Play Playlist",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = { onAddSongsClick() }) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Songs",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Playlist Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onRenameClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Export...") },
                        leadingIcon = { Icon(Icons.Rounded.FileDownload, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onExportClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }

            IconButton(onClick = onToggleExpand) {
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            if (songs.isEmpty()) {
                Text(
                    text = "No songs in this playlist. Tap + to add songs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )
            } else {
                songs.forEach { song ->
                    val isPlaying = playbackInfo.currentSong?.id == song.id
                    val isFav = favoriteSongs.any { it.id == song.id }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            SongItem(
                                song = song,
                                isPlaying = isPlaying,
                                isFavorite = isFav,
                                onSongClick = { onSongClick(song) },
                                onActionClick = { action, _ ->
                                    when (action) {
                                        SongAction.PLAY -> onSongClick(song)
                                        SongAction.TOGGLE_FAVORITE -> onToggleFavorite(song.id)
                                        else -> onSongActionExternal?.invoke(action, song)
                                    }
                                }
                            )
                        }
                        IconButton(onClick = { onRemoveSong(song.id) }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Remove from playlist",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistScreen(
    uiState: CollectionUiState,
    playbackInfo: PlaybackInfo,
    onAction: (CollectionAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSongActionExternal: ((SongAction, Song) -> Unit)? = null
) {
    val context = LocalContext.current

    var showNewDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showDeleteMultipleDialog by remember { mutableStateOf(false) }
    var playlistToRename by remember { mutableStateOf<PlaylistEntity?>(null) }
    var playlistToDelete by remember { mutableStateOf<PlaylistEntity?>(null) }
    var playlistToExport by remember { mutableStateOf<PlaylistEntity?>(null) }
    var playlistToAddSongs by remember { mutableStateOf<PlaylistEntity?>(null) }

    var showMoreMenu by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf(PlaylistFormat.M3U) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(exportFormat.mimeType)
    ) { uri ->
        if (uri != null && playlistToExport != null) {
            val target = playlistToExport!!
            onAction(
                CollectionAction.ExportPlaylist(
                    uri = uri,
                    playlist = target,
                    format = exportFormat,
                    onResult = { _, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            )
            playlistToExport = null
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onAction(
                CollectionAction.ImportPlaylist(
                    uri = uri,
                    onResult = { _, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            )
        }
    }

    BackHandler(enabled = true) {
        onBackClick()
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("playlist_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("playlist_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Playlists",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = { showSortDialog = true },
                    modifier = Modifier.testTag("playlist_sort_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Sort,
                        contentDescription = "Sort Playlists",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier.testTag("playlist_more_menu")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("New Playlist") },
                            leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                showNewDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Import Playlist") },
                            leadingIcon = { Icon(Icons.Rounded.FileUpload, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                importLauncher.launch(arrayOf("*/*", "audio/x-mpegurl", "application/json"))
                            }
                        )
                        if (uiState.playlists.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Delete Playlists...", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    showDeleteMultipleDialog = true
                                }
                            )
                        }
                    }
                }
            }

            if (uiState.playlists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.QueueMusic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No playlists yet",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + to create a new playlist or import one from storage.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showNewDialog = true }) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Create Playlist")
                        }
                    }
                }
            } else {
                PlaylistList(
                    uiState = uiState,
                    playbackInfo = playbackInfo,
                    onAction = onAction,
                    onRenameClick = { playlistToRename = it },
                    onDeleteClick = { playlistToDelete = it },
                    onExportClick = { playlistToExport = it },
                    onAddSongsClick = { playlistToAddSongs = it },
                    onSongActionExternal = onSongActionExternal
                )
            }
        }
    }

    // Dialogs
    if (showNewDialog) {
        NewPlaylistDialog(
            onDismiss = { showNewDialog = false },
            onCreate = { name ->
                onAction(CollectionAction.CreatePlaylist(name))
                showNewDialog = false
            }
        )
    }

    if (showSortDialog) {
        PlaylistSortDialog(
            currentSort = uiState.playlistSortCriteria,
            isAscending = uiState.playlistIsAscending,
            onDismiss = { showSortDialog = false },
            onApplySort = { criteria, asc ->
                onAction(CollectionAction.SetPlaylistSort(criteria, asc))
                showSortDialog = false
            }
        )
    }

    if (showDeleteMultipleDialog) {
        DeleteMultiplePlaylistsDialog(
            playlists = uiState.playlists,
            onDismiss = { showDeleteMultipleDialog = false },
            onDeletePlaylists = { ids ->
                onAction(CollectionAction.DeleteMultiplePlaylists(ids))
                showDeleteMultipleDialog = false
            }
        )
    }

    playlistToRename?.let { target ->
        RenamePlaylistDialog(
            playlist = target,
            onDismiss = { playlistToRename = null },
            onRename = { id, name ->
                onAction(CollectionAction.RenamePlaylist(id, name))
                playlistToRename = null
            }
        )
    }

    playlistToDelete?.let { target ->
        DeletePlaylistDialog(
            playlist = target,
            onDismiss = { playlistToDelete = null },
            onDelete = { id ->
                onAction(CollectionAction.DeletePlaylist(id))
                playlistToDelete = null
            }
        )
    }

    playlistToExport?.let { target ->
        ExportFormatDialog(
            playlist = target,
            currentFormat = exportFormat,
            onDismiss = { playlistToExport = null },
            onFormatSelected = { format ->
                exportFormat = format
                exportLauncher.launch("${target.name}.${format.extension}")
            }
        )
    }

    playlistToAddSongs?.let { target ->
        val existingSongs = uiState.playlistSongs[target.id] ?: emptyList()
        AddSongsToPlaylistDialog(
            playlist = target,
            allSongs = uiState.allSongs,
            existingSongs = existingSongs,
            onDismiss = { playlistToAddSongs = null },
            onAddSongs = { ids ->
                onAction(CollectionAction.AddSongsToPlaylist(target.id, ids))
                playlistToAddSongs = null
            }
        )
    }
}
