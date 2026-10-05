package com.example.ui.home.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Song
import com.example.core.util.TimeUtils
import com.example.ui.common.components.ArtworkCard
import com.example.ui.common.components.ArtworkPlaceholderType
import com.example.ui.library.components.FastScrollBar
import com.example.ui.library.core.SongAction
import com.example.ui.library.song.SongItem

enum class YearSortOrder {
    DESCENDING, // Newest first (e.g. 2025 -> 1990)
    ASCENDING   // Oldest first (e.g. 1990 -> 2025)
}

@Composable
fun NewReleaseCard(
    songs: List<Song>,
    cardColor: Color,
    cardTextColor: Color,
    cardSubtextColor: Color,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCardClick() }
            .testTag("home_new_release_card"),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "New Release",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = cardTextColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${songs.size} Songs • By Year",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp
                    ),
                    color = cardSubtextColor
                )
            }

            // Stacked 3 cover artworks overlapping
            val stackSongs = remember(songs) {
                songs.sortedByDescending { it.year }.take(3)
            }
            Box(
                modifier = Modifier
                    .width(76.dp)
                    .height(54.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (stackSongs.size >= 3) {
                    val song3 = stackSongs[2]
                    ArtworkCard(
                        artworkUri = song3.artworkUri,
                        dataPath = song3.dataPath,
                        albumId = song3.albumId,
                        dateModified = song3.dateModified,
                        title = song3.title,
                        size = 42.dp,
                        cornerRadius = 8.dp,
                        modifier = Modifier
                            .offset(x = (-26).dp)
                            .alpha(0.6f)
                    )
                }
                if (stackSongs.size >= 2) {
                    val song2 = stackSongs[1]
                    ArtworkCard(
                        artworkUri = song2.artworkUri,
                        dataPath = song2.dataPath,
                        albumId = song2.albumId,
                        dateModified = song2.dateModified,
                        title = song2.title,
                        size = 46.dp,
                        cornerRadius = 8.dp,
                        modifier = Modifier
                            .offset(x = (-13).dp)
                            .alpha(0.85f)
                    )
                }
                if (stackSongs.isNotEmpty()) {
                    val song1 = stackSongs[0]
                    ArtworkCard(
                        artworkUri = song1.artworkUri,
                        dataPath = song1.dataPath,
                        albumId = song1.albumId,
                        dateModified = song1.dateModified,
                        title = song1.title,
                        size = 52.dp,
                        cornerRadius = 8.dp,
                        modifier = Modifier.offset(x = 0.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NewReleaseScreen(
    allSongs: List<Song>,
    currentPlayingSongId: Long?,
    isPlaying: Boolean,
    onBackClick: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onSongAction: (SongAction, Song) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = true) {
        onBackClick()
    }

    var sortOrder by remember { mutableStateOf(YearSortOrder.DESCENDING) }
    // Default: Semua tab tertutup (emptySet)
    var expandedYears by remember { mutableStateOf(emptySet<Int>()) }

    var isFastScrolling by remember { mutableStateOf(false) }
    var isHighSpeedScrolling by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Group all songs by year
    val yearGroups = remember(allSongs, sortOrder) {
        val grouped = allSongs.groupBy { if (it.year > 0) it.year else 0 }
        val sortedYears = grouped.keys.filter { it > 0 }.let { validYears ->
            if (sortOrder == YearSortOrder.DESCENDING) {
                validYears.sortedDescending()
            } else {
                validYears.sorted()
            }
        }

        // Put year 0 (Unknown Year) at the end
        val result = mutableListOf<Pair<Int, List<Song>>>()
        for (year in sortedYears) {
            val songsInYear = grouped[year]?.sortedBy { it.title.lowercase() } ?: emptyList()
            result.add(year to songsInYear)
        }
        val unknownYearSongs = grouped[0]
        if (!unknownYearSongs.isNullOrEmpty()) {
            result.add(0 to unknownYearSongs.sortedBy { it.title.lowercase() })
        }
        result
    }

    val flattenedSortedSongs = remember(yearGroups) {
        yearGroups.flatMap { it.second }
    }

    val currentPlayingSong = remember(allSongs, currentPlayingSongId) {
        allSongs.firstOrNull { it.id == currentPlayingSongId }
    }
    val activeHeroSongs = remember(allSongs, currentPlayingSong, expandedYears, yearGroups) {
        if (currentPlayingSong != null) {
            val matchingYearGroup = yearGroups.firstOrNull { (_, songs) ->
                songs.any { it.id == currentPlayingSong.id }
            }
            matchingYearGroup?.second ?: allSongs
        } else if (expandedYears.isNotEmpty()) {
            val firstExpandedYear = expandedYears.first()
            val group = yearGroups.firstOrNull { it.first == firstExpandedYear }
            group?.second ?: allSongs
        } else {
            allSongs
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("new_release_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Immersive Hero Header with Artwork Crossfade synced to playing song / active songs
            item(key = "new_release_hero_banner") {
                DynamicArtworkHero(
                    songs = activeHeroSongs,
                    onBackClick = onBackClick,
                    currentPlayingSong = currentPlayingSong,
                    placeholderType = ArtworkPlaceholderType.ALBUM
                )
            }

            // Info and Action Buttons Section
            item(key = "new_release_info") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "New Release",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${allSongs.size} Songs • ${yearGroups.size} Release Years",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (flattenedSortedSongs.isNotEmpty()) {
                            Button(
                                onClick = {
                                    val first = flattenedSortedSongs.firstOrNull()
                                    if (first != null) onSongClick(first, flattenedSortedSongs)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("new_release_play_all_btn")
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
                                    sortOrder = if (sortOrder == YearSortOrder.DESCENDING) {
                                        YearSortOrder.ASCENDING
                                    } else {
                                        YearSortOrder.DESCENDING
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("new_release_sort_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Sort,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (sortOrder == YearSortOrder.DESCENDING) "Newest First" else "Oldest First",
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Grouped By Year Accordion Cards (Default Closed)
            itemsIndexed(yearGroups, key = { _, pair -> "year_group_${pair.first}" }) { _, (year, songsInYear) ->
                val yearLabel = if (year > 0) "$year" else "Unknown Year"
                val isExpanded = expandedYears.contains(year)
                val arrowRotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    label = "arrow_rotation_$year"
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .testTag("year_card_$year"),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        // Year Card Clickable Header (Toggles Accordion Open/Close, No Click/Hold ripple highlight)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) {
                                    expandedYears = if (isExpanded) {
                                        expandedYears - year
                                    } else {
                                        expandedYears + year
                                    }
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = yearLabel,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        color = if (isExpanded) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Text(
                                    text = "${songsInYear.size} ${if (songsInYear.size == 1) "Song" else "Songs"}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Accordion Arrow Indicator (Clean, without per-tab play button)
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(arrowRotation)
                            )
                        }

                        // Collapsible Songs list in this year card (Animated Open / Close)
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                songsInYear.forEachIndexed { sIndex, song ->
                                    val isCurrent = song.id == currentPlayingSongId
                                    NewReleaseSongRow(
                                        song = song,
                                        isCurrentSong = isCurrent,
                                        isPlaying = isPlaying && isCurrent,
                                        onSongClick = {
                                            // Queue strictly based on this specific year
                                            onSongClick(song, songsInYear)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("new_release_song_${year}_$sIndex")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fast Scroll Bar on right side
        FastScrollBar(
            listState = listState,
            itemCount = yearGroups.size + 2,
            previewTextProvider = { index ->
                val yearIdx = index - 2
                if (yearIdx in yearGroups.indices) {
                    val y = yearGroups[yearIdx].first
                    if (y > 0) "$y" else "?"
                } else {
                    "NR"
                }
            },
            onDraggingChange = { isFastScrolling = it },
            onHighSpeedChange = { isHighSpeedScrolling = it },
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

/**
 * Dedicated song row for New Release screen:
 * - Displays Artwork, Title, Artist, and 3-letter Release Date on the right (e.g. "12 Feb", "22 Agu", "1 Mar").
 * - No minutes/duration displayed.
 * - No 3-dots button (hapus titik 3).
 * - Plays with year-based queue.
 */
@Composable
fun NewReleaseSongRow(
    song: Song,
    isCurrentSong: Boolean,
    isPlaying: Boolean,
    onSongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val releaseDateText = remember(song) {
        TimeUtils.formatSongReleaseDate(song, shortMonth = true)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSongClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ArtworkCard(
            artworkUri = song.artworkUri,
            dataPath = song.dataPath,
            albumId = song.albumId,
            dateModified = song.dateModified,
            title = song.title,
            size = 42.dp,
            cornerRadius = 8.dp,
            placeholderType = ArtworkPlaceholderType.MUSIC
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 13.5.sp
                ),
                color = if (isCurrentSong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Release date on the right side instead of song duration / minutes
        if (releaseDateText.isNotBlank()) {
            Text(
                text = releaseDateText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.5.sp
                ),
                color = if (isCurrentSong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HomeRecentlyAddedCard(
    songs: List<Song>,
    currentPlayingSongId: Long?,
    cardColor: Color,
    cardTextColor: Color,
    cardSubtextColor: Color,
    onSongClick: (Song, List<Song>) -> Unit,
    onSongAction: ((SongAction, Song) -> Unit)?,
    modifier: Modifier = Modifier
) {
    var songLimit by remember { mutableIntStateOf(20) }
    val displaySongs = songs.take(songLimit)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("home_recently_added_card"),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENTLY ADDED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    ),
                    color = cardTextColor.copy(alpha = 0.7f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${displaySongs.size} songs",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = cardSubtextColor
                    )

                    if (songLimit > 20) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = cardTextColor.copy(alpha = 0.08f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { songLimit = 20 }
                                .testTag("recent_songs_reset_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RestartAlt,
                                    contentDescription = "Reset to 20 songs",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Reset",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (displaySongs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No songs added yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = cardSubtextColor,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    displaySongs.forEach { song ->
                        val isCurrent = song.id == currentPlayingSongId
                        HomeRecentCardSongRow(
                            song = song,
                            isCurrentSong = isCurrent,
                            onSongClick = { onSongClick(song, displaySongs) },
                            onSongAction = onSongAction
                        )
                    }
                }

                if (songs.size > songLimit && songLimit < 100) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                songLimit = minOf(songLimit + 10, 100)
                            }
                            .testTag("recent_songs_show_more_btn")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Show More",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Show More",
                                tint = MaterialTheme.colorScheme.primary,
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
fun HomeRecentCardSongRow(
    song: Song,
    isCurrentSong: Boolean,
    onSongClick: (Song) -> Unit,
    onSongAction: ((SongAction, Song) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSongClick(song) }
            .padding(start = 4.dp, end = 0.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ArtworkCard(
            artworkUri = song.artworkUri,
            dataPath = song.dataPath,
            albumId = song.albumId,
            dateModified = song.dateModified,
            title = song.title,
            size = 38.dp,
            cornerRadius = 6.dp,
            placeholderType = ArtworkPlaceholderType.MUSIC
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = if (isCurrentSong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = TimeUtils.formatDuration(song.duration),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            ),
            color = if (isCurrentSong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (onSongAction != null) {
            IconButton(
                onClick = { onSongAction(SongAction.ADD_TO_PLAYLIST, song) },
                modifier = Modifier
                    .size(28.dp)
                    .testTag("home_card_song_more_${song.id}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
