package com.example.ui.library.artist
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
import androidx.compose.ui.text.style.TextAlign
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

data class ArtistUiModel(
    val id: Long,
    val name: String,
    val songCount: Int,
    val albumCount: Int = 1,
    val artworkUri: Uri? = null,
    val dataPath: String? = null,
    val albumId: Long? = null,
    val dateModified: Long = 0L
)

@Composable
fun ArtistItem(
    artist: ArtistUiModel,
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
        label = "artist_press_scale"
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
                        artworkUri = artist.artworkUri,
                        dataPath = artist.dataPath,
                        albumId = artist.albumId,
                        dateModified = artist.dateModified,
                        title = artist.name,
                        size = spacing.artworkThumbnailSize,
                        shape = ArtworkShape.CIRCLE,
                        placeholderType = ArtworkPlaceholderType.ARTIST,
                        isLoading = isFastScrolling || isHighSpeedScrolling,
                        decodeSizePx = 512
                    )

                    Spacer(modifier = Modifier.width(spacing.large))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = artist.name,
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
                            text = "${artist.songCount} ${if (artist.songCount == 1) "track" else "tracks"}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                        .padding(vertical = if (isSmall) 6.dp else 12.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ArtworkCard(
                        artworkUri = artist.artworkUri,
                        dataPath = artist.dataPath,
                        albumId = artist.albumId,
                        dateModified = artist.dateModified,
                        title = artist.name,
                        shape = ArtworkShape.CIRCLE,
                        placeholderType = ArtworkPlaceholderType.ARTIST,
                        isLoading = isFastScrolling || isHighSpeedScrolling,
                        decodeSizePx = 512,
                        modifier = Modifier
                            .fillMaxWidth(if (isSmall) 0.85f else 0.75f)
                            .aspectRatio(1f)
                    )

                    Spacer(modifier = Modifier.height(if (isSmall) 6.dp else 10.dp))

                    Text(
                        text = artist.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (isSmall) 11.sp else 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${artist.songCount} tracks",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = if (isSmall) 9.sp else 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
