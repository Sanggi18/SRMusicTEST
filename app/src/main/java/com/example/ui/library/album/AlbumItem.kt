package com.example.ui.library.album
import com.example.ui.library.core.*
import com.example.ui.library.components.*
import com.example.ui.common.components.*

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.components.ArtworkCard
import com.example.ui.common.components.ArtworkPlaceholderType
import com.example.ui.common.components.ArtworkShape
import com.example.ui.library.components.GridMode

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.common.theme.ExpressiveMotion
import com.example.ui.common.theme.LocalSpacing

data class AlbumUiModel(
    val id: Long,
    val name: String,
    val artist: String,
    val songCount: Int,
    val artworkUri: Uri? = null,
    val dataPath: String? = null,
    val albumId: Long? = null,
    val dateModified: Long = 0L
)

@Composable
fun AlbumItem(
    album: AlbumUiModel,
    gridMode: GridMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFastScrolling: Boolean = false,
    isHighSpeedScrolling: Boolean = false
) {
    val spacing = LocalSpacing.current
    val itemInteractionSource = remember { MutableInteractionSource() }
    val isPressed by itemInteractionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = ExpressiveMotion.BouncySpring,
        label = "album_press_scale"
    )

    when (gridMode) {
        GridMode.LIST -> {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.small, vertical = spacing.extraSmall)
                    .graphicsLayer {
                        scaleX = pressScale
                        scaleY = pressScale
                    }
                    .clip(RoundedCornerShape(spacing.large))
                    .clickable(
                        interactionSource = itemInteractionSource,
                        indication = androidx.compose.material3.ripple()
                    ) { onClick() },
                shape = RoundedCornerShape(spacing.large),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.listTwoLineHeight)
                        .padding(horizontal = spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ArtworkCard(
                        artworkUri = album.artworkUri,
                        dataPath = album.dataPath ?: "",
                        albumId = album.albumId ?: album.id,
                        dateModified = album.dateModified,
                        title = album.name,
                        size = spacing.artworkThumbnailSize,
                        cornerRadius = spacing.artworkCornerRadius,
                        placeholderType = ArtworkPlaceholderType.ALBUM,
                        isLoading = isFastScrolling || isHighSpeedScrolling,
                        decodeSizePx = 512
                    )

                    Spacer(modifier = Modifier.width(spacing.large))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = album.name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(spacing.extraSmall))
                        Text(
                            text = "${album.artist} • ${album.songCount} ${if (album.songCount == 1) "track" else "tracks"}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        GridMode.GRID_2, GridMode.GRID_3, GridMode.GRID_4 -> {
            val isSmall = gridMode == GridMode.GRID_3 || gridMode == GridMode.GRID_4
            val cardPadding = if (isSmall) spacing.extraSmall else spacing.small
            val cornerRadius = if (isSmall) 10.dp else 16.dp

            Surface(
                modifier = modifier
                    .padding(cardPadding)
                    .graphicsLayer {
                        scaleX = pressScale
                        scaleY = pressScale
                    }
                    .clip(RoundedCornerShape(cornerRadius))
                    .clickable(
                        interactionSource = itemInteractionSource,
                        indication = androidx.compose.material3.ripple()
                    ) { onClick() },
                shape = RoundedCornerShape(cornerRadius),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (isSmall) 6.dp else 10.dp)
                ) {
                    ArtworkCard(
                        artworkUri = album.artworkUri,
                        dataPath = album.dataPath ?: "",
                        albumId = album.albumId ?: album.id,
                        dateModified = album.dateModified,
                        title = album.name,
                        shape = ArtworkShape.ROUNDED_SQUARE,
                        cornerRadius = cornerRadius,
                        placeholderType = ArtworkPlaceholderType.ALBUM,
                        isLoading = isFastScrolling || isHighSpeedScrolling,
                        decodeSizePx = if (isSmall) 384 else 512,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )

                    Spacer(modifier = Modifier.height(if (isSmall) 4.dp else 8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = if (isSmall) 4.dp else 8.dp)
                    ) {
                        Text(
                            text = album.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (isSmall) 11.sp else 13.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = album.artist,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = if (isSmall) 9.sp else 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
