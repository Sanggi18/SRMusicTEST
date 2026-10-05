package com.example.ui.home

import com.example.ui.home.components.*
import com.example.ui.home.core.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    currentPlayingSongId: Long?,
    isPlaying: Boolean,
    onArtistClick: ((String) -> Unit)? = null,
    onAlbumClick: ((String) -> Unit)? = null,
    onMenuClick: () -> Unit,
    onAction: (HomeAction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentChildScreen by remember { mutableStateOf(HomeChildScreen.NONE) }

    val allSongsList = uiState.songs
    val remixQueue = uiState.remixQueue
    val dailyGenres = uiState.dailyGenres
    val recentSongs = uiState.recentSongs

    BackHandler(enabled = currentChildScreen != HomeChildScreen.NONE) {
        currentChildScreen = HomeChildScreen.NONE
    }

    when (currentChildScreen) {
        HomeChildScreen.FOR_YOU_REMIX -> {
            ForYouRemixScreen(
                remixSongs = remixQueue,
                currentPlayingSongId = currentPlayingSongId,
                isPlaying = isPlaying,
                onBackClick = { currentChildScreen = HomeChildScreen.NONE },
                onSongClick = { song, list -> onAction(HomeAction.PlaySong(song, list)) },
                onSongAction = { action, song -> onAction(HomeAction.HandleSongAction(action, song)) },
                modifier = modifier
            )
            return
        }
        HomeChildScreen.GENRE_TODAY -> {
            val activeGenre = dailyGenres.getOrNull(uiState.selectedDailyGenreIndex)
                ?: dailyGenres.firstOrNull()
                ?: DailyGenreItem("Music", allSongsList, allSongsList.firstOrNull(), emptyList())
            GenreTodayScreen(
                genreName = activeGenre.name,
                genreSongs = activeGenre.songs,
                artists = activeGenre.artists,
                currentPlayingSongId = currentPlayingSongId,
                isPlaying = isPlaying,
                onBackClick = { currentChildScreen = HomeChildScreen.NONE },
                onSongClick = { song, list -> onAction(HomeAction.PlaySong(song, list)) },
                onSongAction = { action, song -> onAction(HomeAction.HandleSongAction(action, song)) },
                onRefreshGenre = { onAction(HomeAction.NextDailyGenre) },
                modifier = modifier
            )
            return
        }
        HomeChildScreen.NEW_RELEASE -> {
            NewReleaseScreen(
                allSongs = allSongsList,
                currentPlayingSongId = currentPlayingSongId,
                isPlaying = isPlaying,
                onBackClick = { currentChildScreen = HomeChildScreen.NONE },
                onSongClick = { song, list -> onAction(HomeAction.PlaySong(song, list)) },
                onSongAction = { action, song -> onAction(HomeAction.HandleSongAction(action, song)) },
                modifier = modifier
            )
            return
        }
        HomeChildScreen.TOP_ARTIST -> {
            BillboardRankScreen(
                category = BillboardCategory.TOP_ARTIST,
                allSongs = allSongsList,
                songPlayCounts = uiState.songPlayCounts,
                currentPlayingSongId = currentPlayingSongId,
                isPlaying = isPlaying,
                onBackClick = { currentChildScreen = HomeChildScreen.NONE },
                onSongClick = { song, list -> onAction(HomeAction.PlaySong(song, list)) },
                onArtistClick = onArtistClick ?: {},
                onAlbumClick = onAlbumClick ?: {},
                modifier = modifier
            )
            return
        }
        HomeChildScreen.TOP_ALBUM -> {
            BillboardRankScreen(
                category = BillboardCategory.TOP_ALBUM,
                allSongs = allSongsList,
                songPlayCounts = uiState.songPlayCounts,
                currentPlayingSongId = currentPlayingSongId,
                isPlaying = isPlaying,
                onBackClick = { currentChildScreen = HomeChildScreen.NONE },
                onSongClick = { song, list -> onAction(HomeAction.PlaySong(song, list)) },
                onArtistClick = onArtistClick ?: {},
                onAlbumClick = onAlbumClick ?: {},
                modifier = modifier
            )
            return
        }
        HomeChildScreen.TOP_SONGS -> {
            BillboardRankScreen(
                category = BillboardCategory.TOP_SONGS,
                allSongs = allSongsList,
                songPlayCounts = uiState.songPlayCounts,
                currentPlayingSongId = currentPlayingSongId,
                isPlaying = isPlaying,
                onBackClick = { currentChildScreen = HomeChildScreen.NONE },
                onSongClick = { song, list -> onAction(HomeAction.PlaySong(song, list)) },
                onArtistClick = onArtistClick ?: {},
                onAlbumClick = onAlbumClick ?: {},
                modifier = modifier
            )
            return
        }
        HomeChildScreen.NONE -> {}
    }

    val cardColor = MaterialTheme.colorScheme.surfaceContainer
    val cardTextColor = MaterialTheme.colorScheme.onSurface
    val cardSubtextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("home_screen")
    ) {
        HomeHeader(onMenuClick = onMenuClick)

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (allSongsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No songs found in your library.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val fourArtworks = remember(remixQueue) { remixQueue.take(4) }

            val billboardPageCount = 3
            val totalVirtualPages = 1000 * billboardPageCount
            val initialVirtualPage = (totalVirtualPages / 2) - ((totalVirtualPages / 2) % billboardPageCount)
            val billboardPagerState = rememberPagerState(initialPage = initialVirtualPage) { totalVirtualPages }

            LaunchedEffect(billboardPagerState) {
                while (isActive) {
                    delay(4000L)
                    if (!billboardPagerState.isScrollInProgress) {
                        val nextPage = billboardPagerState.currentPage + 1
                        billboardPagerState.animateScrollToPage(nextPage)
                    }
                }
            }

            val topMostPlayed = uiState.topMostPlayed
            val totalAlbumsCount = uiState.totalAlbumsCount
            val totalArtistsCount = uiState.totalArtistsCount

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_content_list"),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                // SECTION 1: Top Grid (For You on left, Genre Today & Terbaru on right with dynamic proportional height)
                item(key = "section_top_grid") {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        val availableWidth = maxWidth - 12.dp
                        val leftWeight = 1f
                        val rightWeight = 1.25f
                        val totalWeight = leftWeight + rightWeight
                        val leftWidth = availableWidth * (leftWeight / totalWeight)
                        val dynamicRowHeight = leftWidth + 32.dp

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dynamicRowHeight),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Left column: For You Card
                            ForYouCard(
                                fourArtworks = fourArtworks,
                                cardColor = cardColor,
                                cardTextColor = cardTextColor,
                                onCardClick = { currentChildScreen = HomeChildScreen.FOR_YOU_REMIX },
                                modifier = Modifier
                                    .weight(leftWeight)
                                    .fillMaxHeight()
                            )

                            // Right column: Genre Today Card (Top) & Terbaru Card (Bottom)
                            Column(
                                modifier = Modifier
                                    .weight(rightWeight)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                GenreTodayCard(
                                    dailyGenres = dailyGenres,
                                    cardColor = cardColor,
                                    cardTextColor = cardTextColor,
                                    cardSubtextColor = cardSubtextColor,
                                    onCardClick = { clickedIndex ->
                                        onAction(HomeAction.SelectDailyGenre(clickedIndex))
                                        currentChildScreen = HomeChildScreen.GENRE_TODAY
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )

                                NewReleaseCard(
                                    songs = allSongsList,
                                    cardColor = cardColor,
                                    cardTextColor = cardTextColor,
                                    cardSubtextColor = cardSubtextColor,
                                    onCardClick = { currentChildScreen = HomeChildScreen.NEW_RELEASE },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }
                        }
                    }
                }

                // SECTION 2: Carousel Billboard / Top Songs (Artist, Album, Songs)
                item(key = "section_billboard_carousel") {
                    Spacer(modifier = Modifier.height(12.dp))

                    val top1Artist = uiState.top1Artist
                    val top1Album = uiState.top1Album
                    val top1Song = uiState.top1Song

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("home_billboard_carousel"),
                        shape = RoundedCornerShape(16.dp),
                        color = cardColor,
                        tonalElevation = 1.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(84.dp)
                        ) {
                            HorizontalPager(
                                state = billboardPagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                val actualPage = page % billboardPageCount
                                when (actualPage) {
                                    0 -> {
                                        val artistName = top1Artist?.key ?: "No Artist"
                                        val artistSongs = top1Artist?.value ?: emptyList()
                                        val artistArtwork = artistSongs.firstOrNull()
                                        val artistPlays = artistSongs.sumOf { uiState.songPlayCounts[it.id] ?: 0 }
                                        val artistSubtitle = if (artistPlays > 0) "${artistSongs.size} Songs • $artistPlays Plays" else "${artistSongs.size} Songs"

                                        TopSongsSlideCard(
                                            categoryLabel = "TOP ARTIST",
                                            title = artistName,
                                            subtitle = artistSubtitle,
                                            artworkSong = artistArtwork,
                                            isArtist = true,
                                            cardColor = Color.Transparent,
                                            cardTextColor = cardTextColor,
                                            cardSubtextColor = cardSubtextColor,
                                            onClick = { currentChildScreen = HomeChildScreen.TOP_ARTIST }
                                        )
                                    }
                                    1 -> {
                                        val albumName = top1Album?.key ?: "No Album"
                                        val albumSongs = top1Album?.value ?: emptyList()
                                        val albumArtwork = albumSongs.firstOrNull()
                                        val albumArtist = albumArtwork?.artist ?: ""
                                        val albumPlays = albumSongs.sumOf { uiState.songPlayCounts[it.id] ?: 0 }
                                        val albumSubtitle = if (albumPlays > 0) "$albumArtist • $albumPlays Plays" else if (albumArtist.isNotBlank()) "$albumArtist • ${albumSongs.size} Songs" else "${albumSongs.size} Songs"

                                        TopSongsSlideCard(
                                            categoryLabel = "TOP ALBUM",
                                            title = albumName,
                                            subtitle = albumSubtitle,
                                            artworkSong = albumArtwork,
                                            isArtist = false,
                                            cardColor = Color.Transparent,
                                            cardTextColor = cardTextColor,
                                            cardSubtextColor = cardSubtextColor,
                                            onClick = { currentChildScreen = HomeChildScreen.TOP_ALBUM }
                                        )
                                    }
                                    2 -> {
                                        val sTitle = top1Song?.title ?: "No Songs"
                                        val sArtist = top1Song?.artist ?: ""
                                        val plays = top1Song?.let { uiState.songPlayCounts[it.id] ?: 0 } ?: 0
                                        val subtitle = if (plays > 0) "$sArtist • $plays Plays" else sArtist

                                        TopSongsSlideCard(
                                            categoryLabel = "TOP SONGS",
                                            title = sTitle,
                                            subtitle = subtitle,
                                            artworkSong = top1Song,
                                            isArtist = false,
                                            cardColor = Color.Transparent,
                                            cardTextColor = cardTextColor,
                                            cardSubtextColor = cardSubtextColor,
                                            onClick = { currentChildScreen = HomeChildScreen.TOP_SONGS }
                                        )
                                    }
                                }
                            }

                            // Dots indicator - uniform 5.dp size for all dots, only color changes
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(billboardPageCount) { index ->
                                    val isSelected = (billboardPagerState.currentPage % billboardPageCount) == index
                                    Box(
                                        modifier = Modifier
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

                // SECTION 3: Shuffle Mosaic Carousel
                item(key = "section_shuffle_mosaic") {
                    Spacer(modifier = Modifier.height(12.dp))
                    HomeShuffleSection(
                        shuffleTracks = uiState.shuffleTracks,
                        allSongs = allSongsList,
                        currentPlayingSongId = currentPlayingSongId,
                        cardColor = cardColor,
                        cardTextColor = cardTextColor,
                        cardSubtextColor = cardSubtextColor,
                        onRefreshShuffle = { onAction(HomeAction.RefreshShuffleTracks) },
                        onSongClick = { song, queue ->
                            onAction(HomeAction.PlaySong(song, queue))
                        },
                        onShuffleTrackClick = { clickedSong ->
                            onAction(HomeAction.PlayShuffleSession(clickedSong, uiState.shuffleTracks))
                        }
                    )
                }

                // SECTION 4: Side-by-side Most Played & Statistic
                item(key = "section_most_played_and_statistic") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HomeMostPlayedCard(
                            songs = topMostPlayed,
                            currentPlayingSongId = currentPlayingSongId,
                            cardColor = cardColor,
                            cardTextColor = cardTextColor,
                            cardSubtextColor = cardSubtextColor,
                            onSongClick = { song ->
                                onAction(HomeAction.PlaySong(song, topMostPlayed))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        HomeStatisticCard(
                            playSongsCount = uiState.totalPlayCount,
                            listenedSeconds = uiState.totalListenedSeconds,
                            albumsCount = totalAlbumsCount,
                            artistsCount = totalArtistsCount,
                            cardColor = cardColor,
                            cardTextColor = cardTextColor,
                            cardSubtextColor = cardSubtextColor,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }

                // SECTION 5: Recently Added Songs Card
                item(key = "recently_added_card") {
                    Spacer(modifier = Modifier.height(12.dp))
                    HomeRecentlyAddedCard(
                        songs = recentSongs,
                        currentPlayingSongId = currentPlayingSongId,
                        cardColor = cardColor,
                        cardTextColor = cardTextColor,
                        cardSubtextColor = cardSubtextColor,
                        onSongClick = { s, list ->
                            onAction(HomeAction.PlaySong(s, list))
                        },
                        onSongAction = { action, s ->
                            onAction(HomeAction.HandleSongAction(action, s))
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
