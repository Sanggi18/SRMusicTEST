package com.example.ui.player.components
import com.example.ui.player.core.*
import com.example.ui.common.components.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.example.core.model.Song
import com.example.ui.common.components.GlideArtworkImage

@Composable
fun MorphingPlayerArtwork(
    song: Song,
    progress: Float,
    targetArtworkCoordinates: LayoutCoordinates?,
    surfaceCoordinates: LayoutCoordinates?,
    screenWidthDp: Dp,
    currentHorizontalPadding: Dp,
    modifier: Modifier = Modifier,
    density: Density = LocalDensity.current
) {
    // Aligned 1:1 with MiniPlayer dimensions (40dp circle, 8dp left margin, 7.5dp vertical margins on 55dp container)
    val miniCoverSizeDp = 40.dp
    val miniCoverLeftDp = 8.dp
    val miniCoverTopDp = 7.5.dp
    val miniCornerRadius = miniCoverSizeDp / 2

    val expandedWidthDp = (screenWidthDp * 0.92f).coerceAtMost(screenWidthDp - 48.dp)

    val targetCoords = targetArtworkCoordinates
    val surfCoords = surfaceCoordinates
    val (expandedTargetLeftDp, expandedTargetTopDp, expandedTargetSizeDp) = if (targetCoords != null && targetCoords.isAttached && surfCoords != null && surfCoords.isAttached) {
        val relativePos = surfCoords.localPositionOf(targetCoords, Offset.Zero)
        val sWidth = with(density) { targetCoords.size.width.toDp() }
        val targetDim = if (sWidth > 0.dp) sWidth else expandedWidthDp
        val sLeft = with(density) { relativePos.x.toDp() }
        val sTop = with(density) { relativePos.y.toDp() }
        Triple(
            sLeft.coerceAtLeast(0.dp),
            sTop.coerceAtLeast(0.dp),
            targetDim
        )
    } else if (targetCoords != null && targetCoords.isAttached) {
        val localPos = targetCoords.positionInRoot()
        val sWidth = with(density) { targetCoords.size.width.toDp() }
        val targetDim = if (sWidth > 0.dp) sWidth else expandedWidthDp
        val sLeft = with(density) { localPos.x.toDp() }
        val sTop = with(density) { localPos.y.toDp() }
        Triple(
            (sLeft - currentHorizontalPadding).coerceAtLeast(0.dp),
            sTop.coerceAtLeast(60.dp),
            targetDim
        )
    } else {
        val defLeft = (screenWidthDp - expandedWidthDp) / 2
        Triple(defLeft, 84.dp, expandedWidthDp)
    }

    val currentArtworkSize = lerp(miniCoverSizeDp, expandedTargetSizeDp, progress)
    val currentArtworkLeft = lerp(miniCoverLeftDp, expandedTargetLeftDp, progress)
    val currentArtworkTop = lerp(miniCoverTopDp, expandedTargetTopDp, progress)
    val expandedCornerRadius = 24.dp
    val currentArtworkCornerRadius = lerp(miniCornerRadius, expandedCornerRadius, progress)
    val artworkShape = RoundedCornerShape(currentArtworkCornerRadius)

    var isArtworkLoading by remember(song.id, song.dataPath, song.albumId) { mutableStateOf(true) }

    // When loading, elevation is 0 to prevent detached or layered shadows.
    // Once loaded, apply smooth elevation following morph progress.
    val shadowElevation = if (isArtworkLoading) 0.dp else (12 * progress).dp

    Box(
        modifier = modifier
            .offset(x = currentArtworkLeft, y = currentArtworkTop)
            .size(currentArtworkSize)
            .then(
                if (shadowElevation > 0.dp) {
                    Modifier.shadow(
                        elevation = shadowElevation,
                        shape = artworkShape,
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f * progress)
                    )
                } else Modifier
            )
            .clip(artworkShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (isArtworkLoading) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f),
                modifier = Modifier.size(currentArtworkSize * 0.42f)
            )
        }

        GlideArtworkImage(
            filePath = song.dataPath,
            albumId = song.albumId,
            contentDescription = "Album art for ${song.title}",
            modifier = Modifier.fillMaxSize(),
            targetSizePx = 800,
            dateModified = song.dateModified,
            artworkUri = song.artworkUri,
            onLoadingStateChange = { loading ->
                isArtworkLoading = loading
            }
        )
    }
}
