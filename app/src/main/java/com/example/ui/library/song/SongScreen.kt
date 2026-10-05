package com.example.ui.library.song
import com.example.ui.library.core.*
import com.example.ui.library.components.*
import com.example.ui.common.components.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.library.core.LibraryUiState
import com.example.ui.library.core.SongAction
import com.example.ui.library.core.toUi
import com.example.core.model.Song
import com.example.ui.library.components.FastScrollBar
import com.example.ui.common.components.SearchPill
import com.example.ui.library.components.SortBar
import com.example.ui.library.components.SortCriteria

@Composable
fun SongScreen(
    uiState: LibraryUiState,
    currentPlayingSongId: Long?,
    isPlaying: Boolean,
    onSongClick: (Song, List<Song>) -> Unit,
    onMenuClick: () -> Unit,
    searchQuery: String = uiState.searchQuery,
    onSearchQueryChange: (String) -> Unit = {},
    sortCriteria: SortCriteria = uiState.songSortCriteria.toUi(),
    onSortCriteriaChange: (SortCriteria) -> Unit = {},
    isAscending: Boolean = uiState.songIsAscending,
    onAscendingChange: (Boolean) -> Unit = {},
    onSongAction: ((SongAction, Song) -> Unit)? = null,
    favoriteSongIds: Set<Long> = uiState.favoriteIds,
    onToggleFavorite: ((Song) -> Unit)? = null,
    onPermissionGranted: ((Boolean) -> Unit)? = null,
    onRequestPermission: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    var isFastScrolling by remember { mutableStateOf(false) }
    var isHighSpeedScrolling by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 4.dp)
            .testTag("song_screen")
    ) {
        SearchPill(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onMenuClick = onMenuClick,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            placeholderText = "Search library..."
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

                val displaySongs = remember(songs, searchQuery, sortCriteria, isAscending) {
                    SongFilter.filterAndSortSongs(
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
                    onShuffleClick = {
                        if (displaySongs.isNotEmpty()) {
                            val shuffled = displaySongs.shuffled()
                            onSongClick(shuffled.first(), shuffled)
                        }
                    },
                    totalCount = displaySongs.size,
                    countLabel = "songs",
                    availableCriteria = listOf(
                        SortCriteria.TITLE,
                        SortCriteria.ARTIST,
                        SortCriteria.ALBUM,
                        SortCriteria.DATE_ADDED,
                        SortCriteria.DATE_MODIFIED,
                        SortCriteria.DURATION,
                        SortCriteria.COUNT
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                if (displaySongs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No songs match \"$searchQuery\"" else "No songs found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 4.dp,
                                bottom = 96.dp
                            )
                        ) {
                            items(
                                items = displaySongs,
                                key = { it.id },
                                contentType = { "song" }
                            ) { song ->
                                SongItem(
                                    song = song,
                                    isCurrentSong = song.id == currentPlayingSongId,
                                    isPlaying = isPlaying && song.id == currentPlayingSongId,
                                    onSongClick = { onSongClick(song, displaySongs) },
                                    onActionClick = onSongAction,
                                    isFastScrolling = isFastScrolling,
                                    isHighSpeedScrolling = isHighSpeedScrolling,
                                    isFavorite = favoriteSongIds.contains(song.id),
                                    onToggleFavorite = onToggleFavorite
                                )
                            }
                        }

                        FastScrollBar(
                            listState = listState,
                            itemCount = displaySongs.size,
                            previewTextProvider = { index ->
                                val song = displaySongs.getOrNull(index)
                                when (sortCriteria) {
                                    SortCriteria.ARTIST -> song?.artist?.trim()?.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
                                    SortCriteria.ALBUM -> song?.album?.trim()?.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
                                    else -> song?.title?.trim()?.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
                                }
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
