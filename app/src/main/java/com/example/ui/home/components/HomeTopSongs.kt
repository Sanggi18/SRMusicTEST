package com.example.ui.home.components

import com.example.ui.home.core.HomeContentGenerator
import com.example.ui.home.core.BillboardCategory
import com.example.ui.home.core.BillboardFilter
import com.example.ui.home.core.BillboardItemData
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
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
import com.example.ui.common.components.ArtworkShape
import com.example.ui.common.artwork.ArtworkColorHelper

@Composable
fun TopSongsSlideCard(
    categoryLabel: String,
    title: String,
    subtitle: String,
    artworkSong: Song?,
    isArtist: Boolean,
    cardColor: Color,
    cardTextColor: Color,
    cardSubtextColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = cardColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = categoryLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = cardSubtextColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = cardTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = cardSubtextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            ArtworkCard(
                artworkUri = artworkSong?.artworkUri,
                dataPath = artworkSong?.dataPath ?: "",
                albumId = artworkSong?.albumId ?: -1L,
                dateModified = artworkSong?.dateModified ?: 0L,
                title = artworkSong?.title ?: title,
                size = 54.dp,
                shape = ArtworkShape.ROUNDED_SQUARE,
                cornerRadius = 12.dp,
                placeholderType = if (isArtist) ArtworkPlaceholderType.ARTIST else ArtworkPlaceholderType.ALBUM
            )
        }
    }
}

@Composable
fun BillboardRankScreen(
    category: BillboardCategory,
    allSongs: List<Song>,
    songPlayCounts: Map<Long, Int>,
    currentPlayingSongId: Long?,
    isPlaying: Boolean,
    onBackClick: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = true) {
        onBackClick()
    }

    val hasPlays = remember(songPlayCounts) { songPlayCounts.values.any { it > 0 } }
    var selectedFilter by remember { mutableStateOf(if (hasPlays) BillboardFilter.BY_PLAYING else BillboardFilter.BY_SONGS) }

    val categoryTitle = remember(category) {
        when (category) {
            BillboardCategory.TOP_ARTIST -> "Top Artists"
            BillboardCategory.TOP_ALBUM -> "Top Albums"
            BillboardCategory.TOP_SONGS -> "Top Songs"
        }
    }

    // Compute ranked list based on category & filter using generator
    val rankedItems: List<BillboardItemData> = remember(category, selectedFilter, allSongs, songPlayCounts) {
        HomeContentGenerator.computeRankedBillboardItems(category, selectedFilter, allSongs, songPlayCounts)
    }

    val topItem = rankedItems.firstOrNull()
    val accentColor = ArtworkColorHelper.rememberBillboardAccentColor(
        categoryTitle = categoryTitle,
        topItemName = topItem?.title ?: "Rank",
        artworkSong = topItem?.artworkSong
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("billboard_rank_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("billboard_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Billboard",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$categoryTitle • ${rankedItems.size} Total",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Filter Buttons: By Songs vs By Playing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { selectedFilter = BillboardFilter.BY_SONGS },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("filter_by_songs"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedFilter == BillboardFilter.BY_SONGS)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedFilter == BillboardFilter.BY_SONGS)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "By Songs",
                        fontWeight = if (selectedFilter == BillboardFilter.BY_SONGS) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = { selectedFilter = BillboardFilter.BY_PLAYING },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("filter_by_playing"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedFilter == BillboardFilter.BY_PLAYING)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedFilter == BillboardFilter.BY_PLAYING)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "By Playing",
                        fontWeight = if (selectedFilter == BillboardFilter.BY_PLAYING) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                // Top 1 Highlight Card
                if (topItem != null) {
                    item(key = "billboard_hero_card") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(accentColor)
                            .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(contentAlignment = Alignment.TopStart) {
                                    ArtworkCard(
                                        artworkUri = topItem.artworkSong?.artworkUri,
                                        dataPath = topItem.artworkSong?.dataPath,
                                        albumId = topItem.artworkSong?.albumId,
                                        dateModified = topItem.artworkSong?.dateModified,
                                        title = topItem.title,
                                        size = 80.dp,
                                        shape = ArtworkShape.ROUNDED_SQUARE,
                                        cornerRadius = 14.dp
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFD700)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "1",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
                                            color = Color(0xFF101018)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "NUMBER ONE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.sp,
                                            letterSpacing = 1.sp
                                        ),
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = topItem.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        ),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = topItem.subtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = topItem.detailMetric,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ),
                                        color = Color(0xFFFFD700)
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "rankings_header") {
                    Text(
                        text = "All Rankings",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                itemsIndexed(rankedItems, key = { index, item -> "${item.id}_$index" }) { index, item ->
                    val rankNumber = index + 1
                    val rankColor = when (rankNumber) {
                        1 -> Color(0xFFFFD700)
                        2 -> Color(0xFFC0C0C0)
                        3 -> Color(0xFFCD7F32)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                when (category) {
                                    BillboardCategory.TOP_ARTIST -> onArtistClick(item.title)
                                    BillboardCategory.TOP_ALBUM -> onAlbumClick(item.title)
                                    BillboardCategory.TOP_SONGS -> {
                                        val song = item.artworkSong
                                        if (song != null) onSongClick(song, allSongs)
                                    }
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.width(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#$rankNumber",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (rankNumber <= 3) FontWeight.Black else FontWeight.Bold,
                                    fontSize = if (rankNumber <= 3) 16.sp else 14.sp
                                ),
                                color = rankColor
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        ArtworkCard(
                            artworkUri = item.artworkSong?.artworkUri,
                            dataPath = item.artworkSong?.dataPath,
                            albumId = item.artworkSong?.albumId,
                            dateModified = item.artworkSong?.dateModified,
                            title = item.title,
                            size = 46.dp,
                            shape = ArtworkShape.ROUNDED_SQUARE,
                            cornerRadius = 10.dp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = item.detailMetric,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
