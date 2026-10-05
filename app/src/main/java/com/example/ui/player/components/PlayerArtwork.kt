package com.example.ui.player.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.model.Song
import com.example.ui.common.components.GlideArtworkImage

@Composable
fun PlayerArtwork(
    song: Song,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 8.dp
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .shadow(elevation, shape)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        GlideArtworkImage(
            filePath = song.dataPath,
            albumId = song.albumId,
            contentDescription = "Album art for ${song.title}",
            modifier = Modifier.fillMaxSize(),
            targetSizePx = 1000,
            dateModified = song.dateModified,
            artworkUri = song.artworkUri
        )
    }
}
