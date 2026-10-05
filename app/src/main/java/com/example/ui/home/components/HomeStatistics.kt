package com.example.ui.home.components

import com.example.ui.home.core.HomeContentGenerator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Song
import com.example.ui.common.components.ArtworkCard
import com.example.ui.common.components.ArtworkPlaceholderType

@Composable
fun HomeMostPlayedCard(
    songs: List<Song>,
    currentPlayingSongId: Long?,
    cardColor: Color,
    cardTextColor: Color,
    cardSubtextColor: Color,
    onSongClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val displaySongs = songs.take(4)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .testTag("home_most_played_card"),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "MOST PLAYED",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                ),
                color = cardTextColor.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (displaySongs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tracks played yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = cardSubtextColor,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    displaySongs.forEach { song ->
                        val isCurrent = song.id == currentPlayingSongId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSongClick(song) }
                                .padding(horizontal = 2.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ArtworkCard(
                                artworkUri = song.artworkUri,
                                dataPath = song.dataPath,
                                albumId = song.albumId,
                                dateModified = song.dateModified,
                                title = song.title,
                                size = 36.dp,
                                cornerRadius = 6.dp,
                                placeholderType = ArtworkPlaceholderType.MUSIC
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else cardTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp
                                    ),
                                    color = cardSubtextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeStatisticCard(
    playSongsCount: Int,
    listenedSeconds: Long,
    albumsCount: Int,
    artistsCount: Int,
    cardColor: Color,
    cardTextColor: Color,
    cardSubtextColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .testTag("home_statistic_card"),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "STATISTIC",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                ),
                color = cardTextColor.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(4.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatisticItemRow(
                    icon = Icons.Rounded.Schedule,
                    label = "Listened",
                    value = HomeContentGenerator.formatListeningTime(listenedSeconds),
                    textColor = cardTextColor,
                    iconTint = cardTextColor
                )
                StatisticItemRow(
                    icon = Icons.Rounded.MusicNote,
                    label = "Play Songs",
                    value = playSongsCount.toString(),
                    textColor = cardTextColor,
                    iconTint = cardTextColor
                )
                StatisticItemRow(
                    icon = Icons.Rounded.Person,
                    label = "Artists",
                    value = artistsCount.toString(),
                    textColor = cardTextColor,
                    iconTint = cardTextColor
                )
                StatisticItemRow(
                    icon = Icons.Rounded.Album,
                    label = "Albums",
                    value = albumsCount.toString(),
                    textColor = cardTextColor,
                    iconTint = cardTextColor
                )
            }
        }
    }
}

@Composable
private fun StatisticItemRow(
    icon: ImageVector,
    label: String,
    value: String,
    textColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(textColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp
                ),
                color = textColor
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            color = textColor
        )
    }
}
