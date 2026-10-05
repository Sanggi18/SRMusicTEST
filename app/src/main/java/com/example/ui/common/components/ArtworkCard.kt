package com.example.ui.common.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ArtworkShape {
    ROUNDED_SQUARE,
    CIRCLE
}

enum class ArtworkPlaceholderType {
    MUSIC,
    ARTIST,
    ALBUM,
    FOLDER,
    AUDIO_FILE
}

@Composable
fun ArtworkCard(
    artworkUri: Any? = null,
    modifier: Modifier = Modifier,
    title: String = "",
    size: Dp = 48.dp,
    shape: ArtworkShape = ArtworkShape.ROUNDED_SQUARE,
    cornerRadius: Dp = 12.dp,
    placeholderType: ArtworkPlaceholderType = ArtworkPlaceholderType.MUSIC,
    isLoading: Boolean = false,
    decodeSizePx: Int? = null,
    dataPath: String? = null,
    albumId: Long? = null,
    dateModified: Long? = null,
    topCrop: Boolean = false,
    grayscale: Boolean = false,
    tintColor: Int? = null
) {
    val clipShape = remember(shape, cornerRadius) {
        if (shape == ArtworkShape.CIRCLE) CircleShape else RoundedCornerShape(cornerRadius)
    }

    val isArtworkEmpty = dataPath.isNullOrBlank() && (albumId == null || albumId == -1L) && (artworkUri == null || (artworkUri is String && artworkUri.isBlank()))

    val gradientBrush = remember(title, isArtworkEmpty) {
        if (!isArtworkEmpty) null
        else {
            val hash = title.hashCode()
            val hue1 = kotlin.math.abs(hash % 360).toFloat()
            val hue2 = (hue1 + 45f) % 360f
            val color1 = Color.hsl(hue1, 0.55f, 0.45f)
            val color2 = Color.hsl(hue2, 0.65f, 0.35f)
            Brush.linearGradient(listOf(color1, color2))
        }
    }

    val placeholderIcon: ImageVector = remember(placeholderType) {
        when (placeholderType) {
            ArtworkPlaceholderType.MUSIC -> Icons.Rounded.MusicNote
            ArtworkPlaceholderType.ARTIST -> Icons.Rounded.Person
            ArtworkPlaceholderType.ALBUM -> Icons.Rounded.Album
            ArtworkPlaceholderType.FOLDER -> Icons.Rounded.Folder
            ArtworkPlaceholderType.AUDIO_FILE -> Icons.Rounded.AudioFile
        }
    }

    val density = LocalDensity.current
    val sizePx = remember(density, size, decodeSizePx) {
        decodeSizePx ?: with(density) { (size.roundToPx() * 2).coerceIn(120, 1000) }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(clipShape)
            .then(
                if (isLoading) {
                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                } else if (gradientBrush != null) {
                    Modifier.background(gradientBrush)
                } else {
                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            // Muted placeholder during high-speed scrolling (matches artwork loading placeholder)
            Icon(
                imageVector = placeholderIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                modifier = Modifier.size(size * 0.45f)
            )
        } else if (!isArtworkEmpty) {
            GlideArtworkImage(
                filePath = dataPath ?: "",
                albumId = albumId ?: -1L,
                artworkUri = artworkUri as? Uri,
                contentDescription = if (title.isNotBlank()) "$title artwork" else null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(clipShape),
                targetSizePx = sizePx,
                dateModified = dateModified ?: 0L,
                topCrop = topCrop,
                grayscale = grayscale,
                tintColor = tintColor
            )
        } else {
            Icon(
                imageVector = placeholderIcon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(size * 0.48f)
            )
        }
    }
}
