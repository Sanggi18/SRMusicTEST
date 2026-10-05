package com.example.ui.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Song
import com.example.ui.common.components.ArtworkCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun HomeShuffleSection(
    shuffleTracks: List<Song>,
    allSongs: List<Song>,
    currentPlayingSongId: Long?,
    cardColor: Color,
    cardTextColor: Color,
    cardSubtextColor: Color,
    onRefreshShuffle: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onShuffleTrackClick: ((Song) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val activeShuffleTracks = if (shuffleTracks.isNotEmpty()) shuffleTracks else allSongs

    var activeSong by remember { mutableStateOf<Song?>(null) }
    var currentDisplayText by remember { mutableStateOf("") }

    // Keep in sync with current playing song if it belongs to shuffle
    LaunchedEffect(currentPlayingSongId, activeShuffleTracks) {
        val playingSong = activeShuffleTracks.find { it.id == currentPlayingSongId }
        if (playingSong != null && playingSong.id != activeSong?.id) {
            activeSong = playingSong
        }
    }

    // Tampilan teks: 3 detik Artist -> 6 detik Judul Lagu -> langsung hilang sampai lagu baru diputar
    LaunchedEffect(activeSong?.id) {
        val song = activeSong
        if (song != null) {
            currentDisplayText = song.artist
            delay(3000L) // 3 detik artist
            currentDisplayText = song.title
            delay(6000L) // 6 detik judul lagu
            currentDisplayText = "" // langsung hilang
        } else {
            currentDisplayText = ""
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("home_shuffle_mosaic_card"),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = "Shuffle",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = cardTextColor,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                AnimatedContent(
                    targetState = currentDisplayText,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500, easing = LinearOutSlowInEasing)) togetherWith
                        fadeOut(animationSpec = tween(400, easing = FastOutLinearInEasing))
                    },
                    label = "ShuffleTrackCycleFade",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 56.dp)
                ) { displayText ->
                    if (displayText.isNotBlank()) {
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            color = cardTextColor.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshShuffle,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh Shuffle",
                        tint = cardSubtextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            ShuffleMosaicCarousel(
                tracks = activeShuffleTracks,
                currentPlayingSongId = currentPlayingSongId,
                placeholderColor = Color.Black.copy(alpha = 0.30f),
                onTrackClick = { clickedSong ->
                    activeSong = clickedSong
                    if (onShuffleTrackClick != null) {
                        onShuffleTrackClick(clickedSong)
                    } else {
                        onSongClick(clickedSong, activeShuffleTracks)
                    }
                }
            )
        }
    }
}

@Composable
private fun ShuffleMosaicCarousel(
    tracks: List<Song>,
    currentPlayingSongId: Long?,
    placeholderColor: Color,
    onTrackClick: (Song) -> Unit
) {
    if (tracks.isEmpty()) return

    val totalPages = 2
    val pagerState = rememberPagerState(initialPage = 0) { totalPages }

    // Reset pager to page 0 whenever the batch changes
    LaunchedEffect(tracks) {
        if (pagerState.currentPage != 0) {
            pagerState.scrollToPage(0)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val availableWidth = maxWidth
        val spacing = 6.dp
        // Pas 5 kolom (1 kolom besar = 2 kolom kecil + spacing, total lebar = 5 unit kecil + 4 spacing)
        val smallSize = ((availableWidth - (spacing * 4)) / 5f).coerceAtLeast(36.dp)
        val bigSize = (smallSize * 2) + spacing

        HorizontalPager(
            state = pagerState,
            pageSpacing = spacing,
            modifier = Modifier
                .fillMaxWidth()
                .height(bigSize)
        ) { actualPageIndex ->
            val isBigLeft = actualPageIndex % 2 == 0
            val pageTracks = tracks.drop(actualPageIndex * 7).take(7)

            ShuffleMosaicPage(
                tracks = pageTracks,
                isBigLeft = isBigLeft,
                bigSize = bigSize,
                smallSize = smallSize,
                spacing = spacing,
                currentPlayingSongId = currentPlayingSongId,
                placeholderColor = placeholderColor,
                onTrackClick = onTrackClick
            )
        }
    }
}

@Composable
private fun ShuffleMosaicPage(
    tracks: List<Song>,
    isBigLeft: Boolean,
    bigSize: Dp,
    smallSize: Dp,
    spacing: Dp,
    currentPlayingSongId: Long?,
    placeholderColor: Color,
    onTrackClick: (Song) -> Unit
) {
    val bigSong = if (isBigLeft) tracks.firstOrNull() else tracks.lastOrNull()
    val smallSongs = if (isBigLeft) tracks.drop(1) else tracks.dropLast(1)

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isBigLeft) {
            val isBigPlaying = currentPlayingSongId != null && bigSong?.id == currentPlayingSongId
            val isBigDimmed = bigSong != null && !isBigPlaying

            MosaicTrackItem(
                song = bigSong,
                size = bigSize,
                cornerRadius = 12.dp,
                placeholderColor = placeholderColor,
                isDimmed = isBigDimmed,
                isPlaying = isBigPlaying,
                onClick = { if (bigSong != null) onTrackClick(bigSong) }
            )

            Column(
                modifier = Modifier
                    .size(width = (smallSize * 3) + (spacing * 2), height = bigSize),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(smallSize),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    for (i in 0..2) {
                        val song = smallSongs.getOrNull(i)
                        val isItemPlaying = currentPlayingSongId != null && song?.id == currentPlayingSongId
                        val isItemDimmed = song != null && !isItemPlaying
                        MosaicTrackItem(
                            song = song,
                            size = smallSize,
                            cornerRadius = 8.dp,
                            placeholderColor = placeholderColor,
                            isDimmed = isItemDimmed,
                            isPlaying = isItemPlaying,
                            onClick = { if (song != null) onTrackClick(song) }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(smallSize),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    for (i in 3..5) {
                        val song = smallSongs.getOrNull(i)
                        val isItemPlaying = currentPlayingSongId != null && song?.id == currentPlayingSongId
                        val isItemDimmed = song != null && !isItemPlaying
                        MosaicTrackItem(
                            song = song,
                            size = smallSize,
                            cornerRadius = 8.dp,
                            placeholderColor = placeholderColor,
                            isDimmed = isItemDimmed,
                            isPlaying = isItemPlaying,
                            onClick = { if (song != null) onTrackClick(song) }
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .size(width = (smallSize * 3) + (spacing * 2), height = bigSize),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(smallSize),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    for (i in 0..2) {
                        val song = smallSongs.getOrNull(i)
                        val isItemPlaying = currentPlayingSongId != null && song?.id == currentPlayingSongId
                        val isItemDimmed = song != null && !isItemPlaying
                        MosaicTrackItem(
                            song = song,
                            size = smallSize,
                            cornerRadius = 8.dp,
                            placeholderColor = placeholderColor,
                            isDimmed = isItemDimmed,
                            isPlaying = isItemPlaying,
                            onClick = { if (song != null) onTrackClick(song) }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(smallSize),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    for (i in 3..5) {
                        val song = smallSongs.getOrNull(i)
                        val isItemPlaying = currentPlayingSongId != null && song?.id == currentPlayingSongId
                        val isItemDimmed = song != null && !isItemPlaying
                        MosaicTrackItem(
                            song = song,
                            size = smallSize,
                            cornerRadius = 8.dp,
                            placeholderColor = placeholderColor,
                            isDimmed = isItemDimmed,
                            isPlaying = isItemPlaying,
                            onClick = { if (song != null) onTrackClick(song) }
                        )
                    }
                }
            }

            val isBigPlaying = currentPlayingSongId != null && bigSong?.id == currentPlayingSongId
            val isBigDimmed = bigSong != null && !isBigPlaying

            MosaicTrackItem(
                song = bigSong,
                size = bigSize,
                cornerRadius = 12.dp,
                placeholderColor = placeholderColor,
                isDimmed = isBigDimmed,
                isPlaying = isBigPlaying,
                onClick = { if (bigSong != null) onTrackClick(bigSong) }
            )
        }
    }
}

@Composable
private fun MosaicTrackItem(
    song: Song?,
    size: Dp,
    cornerRadius: Dp,
    placeholderColor: Color,
    isDimmed: Boolean = false,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(placeholderColor)
            .then(
                if (song != null) {
                    Modifier.clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { onClick() }
                } else Modifier
            )
    ) {
        if (song != null) {
            val contentAlpha = if (isPlaying) 1.0f else if (isDimmed) 0.70f else 1.0f
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = contentAlpha }
            ) {
                ArtworkCard(
                    artworkUri = song.artworkUri,
                    dataPath = song.dataPath,
                    albumId = song.albumId,
                    dateModified = song.dateModified,
                    title = song.title,
                    size = size,
                    cornerRadius = cornerRadius,
                    modifier = Modifier.fillMaxSize(),
                    grayscale = false
                )

                // 30% dark overlay for non-playing songs
                if (isDimmed && !isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.30f))
                    )
                }
            }
        }
    }
}
