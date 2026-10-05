package com.example.ui.library.album
import com.example.ui.library.core.*
import com.example.ui.library.components.*
import com.example.ui.common.components.*

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.ui.library.core.SongAction
import com.example.core.model.Song
import com.example.ui.common.components.ArtworkCard
import com.example.ui.common.components.ArtworkPlaceholderType
import com.example.ui.common.components.ArtworkShape
import com.example.core.util.SortHelper
import com.example.ui.library.song.SongItem

@Composable
fun AlbumDetailScreen(
    albumName: String,
    songs: List<Song>,
    currentPlayingSongId: Long?,
    isPlaying: Boolean,
    onSongClick: (Song, List<Song>) -> Unit,
    onBackClick: () -> Unit,
    onSongAction: ((SongAction, Song) -> Unit)? = null,
    favoriteSongIds: Set<Long> = emptySet(),
    onToggleFavorite: ((Song) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val albumTracks = remember(songs, albumName) {
        songs.filter { it.album.trim().equals(albumName.trim(), ignoreCase = true) }
            .sortedWith { a, b ->
                val trackCmp = a.trackNumber.compareTo(b.trackNumber)
                if (trackCmp != 0 && a.trackNumber > 0 && b.trackNumber > 0) {
                    trackCmp
                } else if (a.trackNumber > 0 && b.trackNumber <= 0) {
                    -1
                } else if (a.trackNumber <= 0 && b.trackNumber > 0) {
                    1
                } else {
                    SortHelper.compareTitles(a.title, b.title)
                }
            }
    }
    val albumArtist = remember(albumTracks) {
        albumTracks.firstOrNull()?.artist ?: "Unknown Artist"
    }

    val randomSong = remember(albumTracks) {
        albumTracks.filter { it.artworkUri != null || it.dataPath.isNotBlank() }.randomOrNull() ?: albumTracks.firstOrNull()
    }

    BackHandler(onBack = onBackClick)

    val view = LocalView.current
    val isAppInLightMode = MaterialTheme.colorScheme.background.luminance() > 0.5f
    DisposableEffect(isAppInLightMode) {
        val window = (view.context as? Activity)?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        insetsController?.isAppearanceLightStatusBars = false
        onDispose {
            insetsController?.isAppearanceLightStatusBars = isAppInLightMode
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("album_detail_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item(key = "album_hero_banner") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    ArtworkCard(
                        artworkUri = randomSong?.artworkUri,
                        dataPath = randomSong?.dataPath ?: "",
                        albumId = randomSong?.albumId ?: -1L,
                        dateModified = randomSong?.dateModified ?: 0L,
                        title = albumName,
                        size = 420.dp,
                        shape = ArtworkShape.ROUNDED_SQUARE,
                        cornerRadius = 0.dp,
                        placeholderType = ArtworkPlaceholderType.ALBUM,
                        topCrop = true,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.70f),
                                        Color.Black.copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.0f to Color.Transparent,
                                        0.50f to Color.Transparent,
                                        0.65f to MaterialTheme.colorScheme.background.copy(alpha = 0.20f),
                                        0.78f to MaterialTheme.colorScheme.background.copy(alpha = 0.55f),
                                        0.90f to MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                                        1.0f to MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    )

                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(top = 8.dp, start = 8.dp)
                            .align(Alignment.TopStart)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            item(key = "album_info") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = albumName,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .basicMarquee()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = albumArtist,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .basicMarquee()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${albumTracks.size} Tracks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (albumTracks.isNotEmpty()) {
                            Button(
                                onClick = { onSongClick(albumTracks.first(), albumTracks) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play All", fontWeight = FontWeight.SemiBold)
                            }

                            FilledTonalButton(
                                onClick = {
                                    val shuffled = albumTracks.shuffled()
                                    onSongClick(shuffled.first(), shuffled)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Shuffle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Shuffle", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Songs",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            items(albumTracks, key = { it.id }) { song ->
                val isFav = favoriteSongIds.contains(song.id)
                SongItem(
                    song = song,
                    isCurrentSong = song.id == currentPlayingSongId,
                    isPlaying = isPlaying && song.id == currentPlayingSongId,
                    onSongClick = { s -> onSongClick(s, albumTracks) },
                    onActionClick = onSongAction,
                    isFavorite = isFav,
                    onToggleFavorite = onToggleFavorite,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
