package com.example.ui.sidebar.favorites
import com.example.ui.sidebar.core.*
import com.example.ui.common.components.*

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.PlaybackInfo
import com.example.core.model.Song
import com.example.ui.library.core.SongAction
import com.example.ui.library.song.SongItem
import com.example.ui.sidebar.core.CollectionAction
import com.example.ui.sidebar.core.CollectionUiState

@Composable
fun FavoritesScreen(
    uiState: CollectionUiState,
    playbackInfo: PlaybackInfo,
    onAction: (CollectionAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSongActionExternal: ((SongAction, Song) -> Unit)? = null
) {
    BackHandler(enabled = true) {
        onBackClick()
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("favorites_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("favorites_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Favorite Songs",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.size(48.dp))
            }

            if (uiState.favoriteSongs.isEmpty()) {
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
                            imageVector = Icons.Rounded.FavoriteBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No favorites added",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the heart icon on any track to save it to your favorites.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            onAction(CollectionAction.PlaySong(uiState.favoriteSongs.first(), uiState.favoriteSongs))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Play All (${uiState.favoriteSongs.size})")
                    }

                    OutlinedButton(
                        onClick = {
                            val shuffled = uiState.favoriteSongs.shuffled()
                            if (shuffled.isNotEmpty()) {
                                onAction(CollectionAction.PlaySong(shuffled.first(), shuffled))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.Shuffle, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Shuffle")
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(uiState.favoriteSongs, key = { it.id }) { song ->
                        val isPlaying = playbackInfo.currentSong?.id == song.id
                        SongItem(
                            song = song,
                            isPlaying = isPlaying,
                            isFavorite = true,
                            onSongClick = { onAction(CollectionAction.PlaySong(song, uiState.favoriteSongs)) },
                            onActionClick = { action, _ ->
                                when (action) {
                                    SongAction.PLAY -> onAction(CollectionAction.PlaySong(song, uiState.favoriteSongs))
                                    SongAction.PLAY_NEXT -> onAction(CollectionAction.PlayNext(song))
                                    SongAction.ADD_TO_QUEUE -> onAction(CollectionAction.AddToQueue(song))
                                    SongAction.TOGGLE_FAVORITE -> onAction(CollectionAction.ToggleFavorite(song.id))
                                    else -> onSongActionExternal?.invoke(action, song)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
