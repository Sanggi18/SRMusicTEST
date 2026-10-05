package com.example.ui.library.song
import com.example.ui.library.core.*
import com.example.ui.library.components.*
import com.example.ui.common.components.*

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Queue
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Song
import com.example.ui.common.components.ArtworkCard
import com.example.ui.library.core.SongAction
import com.example.core.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongItem(
    song: Song,
    isCurrentSong: Boolean = false,
    isPlaying: Boolean = false,
    onSongClick: (Song) -> Unit,
    onActionClick: ((SongAction, Song) -> Unit)? = null,
    modifier: Modifier = Modifier,
    isFastScrolling: Boolean = false,
    isHighSpeedScrolling: Boolean = false,
    showFavoriteHeart: Boolean = false,
    isFavorite: Boolean = false,
    onToggleFavorite: ((Song) -> Unit)? = null
) {
    var showActionMenu by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.5.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSongClick(song) }
            .testTag("song_item_${song.id}"),
        shape = RoundedCornerShape(14.dp),
        color = if (isCurrentSong) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        tonalElevation = if (isCurrentSong) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 2.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                ArtworkCard(
                    artworkUri = song.artworkUri,
                    dataPath = song.dataPath,
                    albumId = song.albumId,
                    dateModified = song.dateModified,
                    title = song.title,
                    size = 48.dp,
                    cornerRadius = 10.dp,
                    isLoading = isFastScrolling || isHighSpeedScrolling
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isCurrentSong) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = if (isCurrentSong) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (song.album.isNotBlank() && song.album != "Unknown Album") "${song.artist} • ${song.album}" else song.artist,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = if (isCurrentSong) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isFavorite || showFavoriteHeart) {
                IconButton(
                    onClick = { onToggleFavorite?.invoke(song) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = TimeUtils.formatDuration(song.duration),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                ),
                color = if (isCurrentSong) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Trailing 3-dots action menu
            Box {
                IconButton(
                    onClick = { showActionMenu = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Song options",
                        tint = if (isCurrentSong) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (showActionMenu) {
                    DropdownMenu(
                        expanded = true,
                        onDismissRequest = { showActionMenu = false }
                    ) {
                        // Playback & Queue
                        SongActionMenuItem("Play", Icons.Rounded.PlayArrow) {
                            showActionMenu = false
                            if (onActionClick != null) {
                                onActionClick.invoke(SongAction.PLAY, song)
                            } else {
                                onSongClick(song)
                            }
                        }
                        SongActionMenuItem("Play Next", Icons.Rounded.QueueMusic) {
                            showActionMenu = false
                            onActionClick?.invoke(SongAction.PLAY_NEXT, song)
                        }
                        SongActionMenuItem("Add to Queue", Icons.Rounded.Queue) {
                            showActionMenu = false
                            onActionClick?.invoke(SongAction.ADD_TO_QUEUE, song)
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Collection
                        SongActionMenuItem("Add to Playlist", Icons.Rounded.PlaylistAdd) {
                            showActionMenu = false
                            onActionClick?.invoke(SongAction.ADD_TO_PLAYLIST, song)
                        }
                        if (isFavorite) {
                            SongActionMenuItem("Remove from Favorites", Icons.Rounded.Favorite) {
                                showActionMenu = false
                                onActionClick?.invoke(SongAction.REMOVE_FROM_FAVORITES, song)
                            }
                        } else {
                            SongActionMenuItem("Add to Favorites", Icons.Rounded.FavoriteBorder) {
                                showActionMenu = false
                                onActionClick?.invoke(SongAction.TOGGLE_FAVORITE, song)
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Navigation
                        SongActionMenuItem("Go to Artist", Icons.Rounded.Person) {
                            showActionMenu = false
                            onActionClick?.invoke(SongAction.GO_TO_ARTIST, song)
                        }
                        SongActionMenuItem("Go to Album", Icons.Rounded.Album) {
                            showActionMenu = false
                            onActionClick?.invoke(SongAction.GO_TO_ALBUM, song)
                        }
                        SongActionMenuItem("Go to Folder", Icons.Rounded.Folder) {
                            showActionMenu = false
                            onActionClick?.invoke(SongAction.GO_TO_FOLDER, song)
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Details & Management
                        SongActionMenuItem("Song Info", Icons.Rounded.Info) {
                            showActionMenu = false
                            showInfoDialog = true
                            onActionClick?.invoke(SongAction.SONG_INFO, song)
                        }
                        SongActionMenuItem("Delete", Icons.Rounded.Delete) {
                            showActionMenu = false
                            onActionClick?.invoke(SongAction.DELETE, song)
                        }
                    }
                }
            }
        }
    }

    if (showInfoDialog) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showInfoDialog = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Song Info",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                InfoRow("Title", song.title)
                InfoRow("Artist", song.artist)
                InfoRow("Album", song.album)
                InfoRow("Duration", TimeUtils.formatDuration(song.duration))
                InfoRow("File Name", song.displayName.ifBlank { song.title })
                if (song.dataPath.isNotBlank()) {
                    InfoRow("Path", song.dataPath)
                } else {
                    InfoRow("Content URI", song.contentUri.toString())
                }
                InfoRow("Size", formatFileSize(song.size))
            }
        }
    }
}

@Composable
private fun SongActionMenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text, style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        onClick = onClick
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value.ifBlank { "Unknown" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format("%.2f GB", gb)
        mb >= 1.0 -> String.format("%.2f MB", mb)
        kb >= 1.0 -> String.format("%.1f KB", kb)
        else -> "$bytes B"
    }
}
