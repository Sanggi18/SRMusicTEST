package com.example.ui.library.folder
import com.example.ui.library.core.*
import com.example.ui.library.components.*
import com.example.ui.common.components.*

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.library.core.LibraryUiState
import com.example.ui.library.core.SongAction
import com.example.ui.library.core.toUi
import com.example.ui.library.song.SongItem
import com.example.core.model.Song
import com.example.ui.library.components.FastScrollBar
import com.example.ui.common.components.SearchPill

@Composable
fun FolderScreen(
    uiState: LibraryUiState,
    currentPlayingSongId: Long?,
    isPlaying: Boolean,
    onSongClick: (Song, List<Song>) -> Unit,
    onMenuClick: () -> Unit,
    searchQuery: String = uiState.searchQuery,
    onSearchQueryChange: (String) -> Unit = {},
    currentRelativePath: String = uiState.currentFolderRelativePath,
    onRelativePathChange: (String) -> Unit = {},
    onCurrentRelativePathChange: (String) -> Unit = onRelativePathChange,
    rootFolderDisplay: String = uiState.rootFolderDisplay,
    onRootFolderDisplayChange: (String) -> Unit = {},
    onPlayNextBatch: ((List<Song>) -> Unit)? = null,
    onAddToQueueBatch: ((List<Song>) -> Unit)? = null,
    onSongAction: ((SongAction, Song) -> Unit)? = null,
    folderStyle: FolderStyle = uiState.folderStyle.toUi(),
    onFolderStyleChange: ((FolderStyle) -> Unit)? = null,
    favoriteSongIds: Set<Long> = uiState.favoriteIds,
    onToggleFavorite: ((Song) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val effectiveRelativePath = currentRelativePath.trim('/')

    val updateRelativePath: (String) -> Unit = { newPath ->
        onRelativePathChange(newPath)
        onCurrentRelativePathChange(newPath)
    }

    val canGoBack = effectiveRelativePath.isNotEmpty()
    val handleBack: () -> Unit = {
        if (canGoBack) {
            val parentPath = if (effectiveRelativePath.contains('/')) {
                effectiveRelativePath.substringBeforeLast('/')
            } else {
                ""
            }
            updateRelativePath(parentPath)
        }
    }

    if (canGoBack) {
        BackHandler(onBack = handleBack)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 4.dp)
            .testTag("folder_screen")
    ) {
        SearchPill(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onMenuClick = onMenuClick,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            placeholderText = "Search folders..."
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val allSongs = uiState.songs

                val (subfolders, directSongs) = remember(allSongs, effectiveRelativePath, searchQuery) {
                    FolderTreeParser.parseFolderContent(allSongs, effectiveRelativePath, searchQuery)
                }

                // Breadcrumb & Controls Row
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (canGoBack) {
                            IconButton(
                                onClick = handleBack,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                                    contentDescription = "Up one directory",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        val breadcrumbSegments = remember(effectiveRelativePath) {
                            if (effectiveRelativePath.isEmpty()) emptyList()
                            else effectiveRelativePath.split('/')
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { updateRelativePath("") }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Home,
                                    contentDescription = "Root",
                                    tint = if (effectiveRelativePath.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = rootFolderDisplay,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (effectiveRelativePath.isEmpty()) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = if (effectiveRelativePath.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            breadcrumbSegments.forEachIndexed { index, segment ->
                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.size(14.dp)
                                )

                                val segmentPath = breadcrumbSegments.take(index + 1).joinToString("/")
                                val isLast = index == breadcrumbSegments.lastIndex

                                Text(
                                    text = segment,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { updateRelativePath(segmentPath) }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Folder View Options Menu (Icon vs Thumbnail, Root Folder display)
                        var showOptionsMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = { showOptionsMenu = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "Folder display options",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false }
                            ) {
                                Text(
                                    text = "Folder Style",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                                DropdownMenuItem(
                                    text = { Text("Icon", style = MaterialTheme.typography.bodyMedium) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Folder,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (folderStyle == FolderStyle.ICON) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        onFolderStyleChange?.invoke(FolderStyle.ICON)
                                        showOptionsMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Thumbnail", style = MaterialTheme.typography.bodyMedium) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.FolderOpen,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (folderStyle == FolderStyle.THUMBNAIL) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        onFolderStyleChange?.invoke(FolderStyle.THUMBNAIL)
                                        showOptionsMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (subfolders.isEmpty() && directSongs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No items match \"$searchQuery\"" else "No songs or subfolders in this directory.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val listState = remember(effectiveRelativePath) { LazyListState() }
                    var isFastScrolling by remember { mutableStateOf(false) }
                    var isHighSpeedScrolling by remember { mutableStateOf(false) }

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
                            items(subfolders, key = { "folder_${it.path}" }, contentType = { "folder" }) { folder ->
                                FolderItem(
                                    folder = folder,
                                    folderStyle = folderStyle,
                                    onClick = { updateRelativePath(folder.path) },
                                    onActionClick = { action, targetFolder ->
                                        val folderSongs = FolderTreeParser.getSongsInFolder(allSongs, targetFolder.path)
                                        if (folderSongs.isNotEmpty()) {
                                            when (action) {
                                                FolderAction.PLAY -> onSongClick(folderSongs.first(), folderSongs)
                                                FolderAction.SHUFFLE -> {
                                                    val shuffled = folderSongs.shuffled()
                                                    onSongClick(shuffled.first(), shuffled)
                                                }
                                                FolderAction.PLAY_NEXT -> {
                                                    onPlayNextBatch?.invoke(folderSongs)
                                                }
                                                FolderAction.ADD_TO_QUEUE -> {
                                                    onAddToQueueBatch?.invoke(folderSongs)
                                                }
                                            }
                                        }
                                    }
                                )
                            }

                            items(directSongs, key = { "song_${it.id}" }, contentType = { "song" }) { song ->
                                SongItem(
                                    song = song,
                                    isCurrentSong = song.id == currentPlayingSongId,
                                    isPlaying = isPlaying && song.id == currentPlayingSongId,
                                    onSongClick = { s -> onSongClick(s, directSongs) },
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
                            itemCount = subfolders.size + directSongs.size,
                            previewTextProvider = { index ->
                                if (index < subfolders.size) {
                                    subfolders.getOrNull(index)?.name?.trim()?.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
                                } else {
                                    val songIndex = index - subfolders.size
                                    directSongs.getOrNull(songIndex)?.title?.trim()?.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
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
