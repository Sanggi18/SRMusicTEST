package com.example.ui.library.album
import com.example.ui.library.core.*
import com.example.ui.library.components.*
import com.example.ui.common.components.*

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.library.core.SongAction
import com.example.ui.library.core.LibraryUiState
import com.example.ui.library.core.toUi
import com.example.core.model.Song
import com.example.ui.library.components.FastScrollBar
import com.example.ui.library.components.FastScrollBarGrid
import com.example.ui.library.components.GridMode
import com.example.ui.common.components.SearchPill
import com.example.ui.library.components.SortBar
import com.example.ui.library.components.SortCriteria

@Composable
fun AlbumScreen(
    uiState: LibraryUiState,
    currentPlayingSongId: Long?,
    isPlaying: Boolean,
    onSongClick: (Song, List<Song>) -> Unit,
    onMenuClick: () -> Unit,
    searchQuery: String = uiState.searchQuery,
    onSearchQueryChange: (String) -> Unit = {},
    selectedAlbumName: String? = uiState.selectedAlbumName,
    onSelectedAlbumChange: (String?) -> Unit = {},
    sortCriteria: SortCriteria = uiState.albumSortCriteria.toUi(),
    onSortCriteriaChange: (SortCriteria) -> Unit = {},
    isAscending: Boolean = uiState.albumIsAscending,
    onAscendingChange: (Boolean) -> Unit = {},
    gridMode: GridMode = uiState.albumGridMode.toUi(),
    onGridModeChange: (GridMode) -> Unit = {},
    onSongAction: ((SongAction, Song) -> Unit)? = null,
    favoriteSongIds: Set<Long> = uiState.favoriteIds,
    onToggleFavorite: ((Song) -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
    gridState: LazyGridState = rememberLazyGridState(),
    modifier: Modifier = Modifier
) {
    if (selectedAlbumName != null) {
        val songs = uiState.songs
        AlbumDetailScreen(
            albumName = selectedAlbumName,
            songs = songs,
            currentPlayingSongId = currentPlayingSongId,
            isPlaying = isPlaying,
            onSongClick = onSongClick,
            onBackClick = { onSelectedAlbumChange(null) },
            onSongAction = onSongAction,
            favoriteSongIds = favoriteSongIds,
            onToggleFavorite = onToggleFavorite,
            modifier = modifier
        )
        return
    }

    var isFastScrolling by remember { mutableStateOf(false) }
    var isHighSpeedScrolling by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 4.dp)
            .testTag("album_screen")
    ) {
        SearchPill(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onMenuClick = onMenuClick,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            placeholderText = "Search albums..."
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val songs = uiState.songs
                val albumsList = remember(songs, searchQuery, sortCriteria, isAscending) {
                    AlbumFilter.buildAlbumsList(
                        songs = songs,
                        searchQuery = searchQuery,
                        sortCriteria = sortCriteria,
                        isAscending = isAscending
                    )
                }

                SortBar(
                    sortCriteria = sortCriteria,
                    isAscending = isAscending,
                    onSortCriteriaChange = onSortCriteriaChange,
                    onAscendingChange = onAscendingChange,
                    gridMode = gridMode,
                    onGridModeChange = onGridModeChange,
                    totalCount = albumsList.size,
                    countLabel = "albums",
                    availableCriteria = listOf(
                        SortCriteria.ALBUM,
                        SortCriteria.ARTIST,
                        SortCriteria.COUNT
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                if (albumsList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No albums match \"$searchQuery\"" else "No albums found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (gridMode == GridMode.LIST) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp)
                            ) {
                                items(albumsList, key = { it.name + it.id }) { album ->
                                    AlbumItem(
                                        album = album,
                                        gridMode = gridMode,
                                        onClick = { onSelectedAlbumChange(album.name) },
                                        isFastScrolling = isFastScrolling,
                                        isHighSpeedScrolling = isHighSpeedScrolling
                                    )
                                }
                            }

                            FastScrollBar(
                                listState = listState,
                                itemCount = albumsList.size,
                                previewTextProvider = { index ->
                                    val album = albumsList.getOrNull(index)
                                    val text = if (sortCriteria == SortCriteria.ARTIST) album?.artist else album?.name
                                    text?.trim()?.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
                                },
                                onDraggingChange = { isFastScrolling = it },
                                onHighSpeedChange = { isHighSpeedScrolling = it },
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(gridMode.columns),
                                state = gridState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 12.dp,
                                    end = 12.dp,
                                    top = 4.dp,
                                    bottom = 96.dp
                                )
                            ) {
                                items(albumsList, key = { it.name + it.id }) { album ->
                                    AlbumItem(
                                        album = album,
                                        gridMode = gridMode,
                                        onClick = { onSelectedAlbumChange(album.name) },
                                        isFastScrolling = isFastScrolling,
                                        isHighSpeedScrolling = isHighSpeedScrolling
                                    )
                                }
                            }

                            FastScrollBarGrid(
                                gridState = gridState,
                                itemCount = albumsList.size,
                                previewTextProvider = { index ->
                                    val album = albumsList.getOrNull(index)
                                    val text = if (sortCriteria == SortCriteria.ARTIST) album?.artist else album?.name
                                    text?.trim()?.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
                                },
                                onDraggingChange = { isFastScrolling = it },
                                onHighSpeedChange = { isHighSpeedScrolling = it },
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }
                }
            }
        }
    }
