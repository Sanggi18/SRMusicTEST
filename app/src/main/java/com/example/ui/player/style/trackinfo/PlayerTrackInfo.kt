package com.example.ui.player.style.trackinfo

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Song
import com.example.core.model.TrackInfoAlignment

@Composable
fun PlayerTrackInfo(
    song: Song,
    alignment: TrackInfoAlignment,
    modifier: Modifier = Modifier
) {
    val (horizontalAlign, textAlign) = when (alignment) {
        TrackInfoAlignment.LEFT -> Alignment.Start to TextAlign.Start
        TrackInfoAlignment.CENTER -> Alignment.CenterHorizontally to TextAlign.Center
        TrackInfoAlignment.RIGHT -> Alignment.End to TextAlign.End
    }

    Column(
        horizontalAlignment = horizontalAlign,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = song.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            textAlign = textAlign,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee()
                .testTag("now_playing_title")
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = song.artist,
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            ),
            textAlign = textAlign,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee()
                .testTag("now_playing_artist")
        )

        if (song.album.isNotBlank() && song.album != "<unknown>") {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = song.album,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 13.sp
                ),
                textAlign = textAlign,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee()
            )
        }
    }
}
