package com.example.ui.home.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Song
import com.example.ui.common.components.ArtworkCard
import com.example.ui.common.components.ArtworkPlaceholderType
import com.example.ui.home.core.DailyGenreItem
import com.example.ui.library.core.SongAction
import com.example.ui.library.song.SongItem

@Composable
fun GenreTodayCard(
    dailyGenres: List<DailyGenreItem>,
    cardColor: Color,
    cardTextColor: Color,
    cardSubtextColor: Color,
    onCardClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerCount = dailyGenres.size.coerceAtLeast(1)
    val pagerState = rememberPagerState { pagerCount }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCardClick(pagerState.currentPage) }
            .testTag("home_genre_today_card"),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Genre Today",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = cardTextColor
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val currentGenre = dailyGenres.getOrNull(page)
                val genreName = currentGenre?.name ?: "Music"
                val genreSongsCount = currentGenre?.songs?.size ?: 0
                val sampleSong = currentGenre?.sampleSong

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ArtworkCard(
                        artworkUri = sampleSong?.artworkUri,
                        dataPath = sampleSong?.dataPath ?: "",
                        albumId = sampleSong?.albumId ?: -1L,
                        dateModified = sampleSong?.dateModified ?: 0L,
                        title = sampleSong?.title ?: genreName,
                        size = 42.dp,
                        cornerRadius = 8.dp,
                        placeholderType = ArtworkPlaceholderType.ALBUM
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = genreName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = cardTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$genreSongsCount Songs",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp
                            ),
                            color = cardSubtextColor,
                            maxLines = 1
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = cardSubtextColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Dots indicator - all dots uniform size (5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dotsCount = if (pagerCount in 2..3) pagerCount else 3
                repeat(dotsCount) { idx ->
                    val isSelected = (pagerState.currentPage % dotsCount) == idx
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.5.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) cardTextColor
                                else cardTextColor.copy(alpha = 0.25f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun GenreTodayScreen(
    genreName: String,
    genreSongs: List<Song>,
    artists: List<String>,
    currentPlayingSongId: Long?,
    isPlaying: Boolean,
    onBackClick: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onSongAction: (SongAction, Song) -> Unit,
    onRefreshGenre: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = true) {
        onBackClick()
    }

    val currentPlayingSong = remember(genreSongs, currentPlayingSongId) {
        genreSongs.firstOrNull { it.id == currentPlayingSongId }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("genre_today_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Immersive Hero Header with Dynamic Rotating Artwork & Switch Button
            item(key = "genre_hero_banner") {
                DynamicArtworkHero(
                    songs = genreSongs,
                    onBackClick = onBackClick,
                    currentPlayingSong = currentPlayingSong,
                    placeholderType = ArtworkPlaceholderType.ALBUM,
                    extraAction = {
                        IconButton(
                            onClick = onRefreshGenre,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.40f))
                                .testTag("genre_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Switch Genre",
                                tint = Color.White
                            )
                        }
                    }
                )
            }

            // Info and Action Buttons Section
            item(key = "genre_info") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = genreName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val artistSubtitle = if (artists.isNotEmpty()) {
                        "Featuring ${artists.take(3).joinToString(", ")} • ${genreSongs.size} Songs"
                    } else {
                        "Genre Today • ${genreSongs.size} Songs"
                    }
                    Text(
                        text = artistSubtitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (genreSongs.isNotEmpty()) {
                            Button(
                                onClick = {
                                    val first = genreSongs.firstOrNull()
                                    if (first != null) onSongClick(first, genreSongs)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("genre_play_all_btn")
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
                                    val shuffled = genreSongs.shuffled()
                                    val first = shuffled.firstOrNull()
                                    if (first != null) onSongClick(first, shuffled)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("genre_shuffle_btn")
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

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Songs in $genreName (${genreSongs.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Songs List with 16.dp horizontal padding
            itemsIndexed(genreSongs, key = { index, song -> "${song.id}_$index" }) { index, song ->
                SongItem(
                    song = song,
                    isCurrentSong = song.id == currentPlayingSongId,
                    isPlaying = isPlaying && song.id == currentPlayingSongId,
                    onSongClick = { onSongClick(song, genreSongs) },
                    onActionClick = onSongAction,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .testTag("genre_song_item_$index")
                )
            }
        }
    }
}
