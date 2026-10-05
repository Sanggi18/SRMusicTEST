package com.example.ui.player
import com.example.ui.player.components.*
import com.example.ui.player.core.*
import com.example.ui.common.components.*

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.PlaybackInfo
import com.example.core.model.Song
import com.example.ui.common.components.ArtworkCard
import com.example.ui.common.components.ArtworkShape

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.common.theme.ExpressiveMotion
import com.example.ui.common.theme.LocalSpacing
import com.example.ui.common.theme.springPress

@Composable
fun MiniPlayer(
    uiState: PlayerUiState,
    onAction: (PlayerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong = uiState.currentSong ?: return
    val spacing = LocalSpacing.current
    val playerShape = RoundedCornerShape(spacing.pillCornerRadius)

    val pressInteractionSource = remember { MutableInteractionSource() }
    val isPressed by pressInteractionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = ExpressiveMotion.BouncySpring,
        label = "mini_player_press_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.medium)
            .height(spacing.miniPlayerHeight)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .shadow(
                elevation = 8.dp,
                shape = playerShape,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f),
                shape = playerShape
            )
            .miniPlayerHorizontalDrag(
                onNext = { onAction(PlayerAction.Next) },
                onPrevious = { onAction(PlayerAction.Previous) }
            )
            .clickable(
                interactionSource = pressInteractionSource,
                indication = null
            ) { onAction(PlayerAction.Expand) }
            .testTag("mini_player"),
        shape = playerShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 4.dp
    ) {
        MiniPlayerContent(
            currentSong = currentSong,
            playbackInfo = uiState.playbackInfo,
            onAction = onAction
        )
    }
}

@Composable
fun MiniPlayerContent(
    currentSong: Song,
    playbackInfo: PlaybackInfo,
    onAction: (PlayerAction) -> Unit,
    showArtwork: Boolean = true,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val playInteractionSource = remember { MutableInteractionSource() }
    val prevInteractionSource = remember { MutableInteractionSource() }
    val nextInteractionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(spacing.miniPlayerHeight)
            .padding(horizontal = spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showArtwork) {
            ArtworkCard(
                artworkUri = currentSong.artworkUri,
                dataPath = currentSong.dataPath,
                albumId = currentSong.albumId,
                dateModified = currentSong.dateModified,
                title = currentSong.title,
                size = 40.dp,
                shape = ArtworkShape.CIRCLE,
                cornerRadius = 20.dp,
                decodeSizePx = 180,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            )
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.width(spacing.medium))

        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = currentSong.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee()
            )
            Text(
                text = currentSong.artist,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee()
            )
        }

        IconButton(
            onClick = { onAction(PlayerAction.Previous) },
            interactionSource = prevInteractionSource,
            modifier = Modifier
                .size(40.dp)
                .springPress(prevInteractionSource, pressedScale = 0.85f)
                .align(Alignment.CenterVertically)
                .testTag("mini_player_prev")
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous song",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }

        if (playbackInfo.isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterVertically)
                    .padding(8.dp),
                strokeWidth = 2.5.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            IconButton(
                onClick = { onAction(PlayerAction.PlayPause) },
                interactionSource = playInteractionSource,
                modifier = Modifier
                    .size(40.dp)
                    .springPress(playInteractionSource, pressedScale = 0.85f)
                    .align(Alignment.CenterVertically)
                    .testTag("mini_player_play_pause")
            ) {
                Icon(
                    imageVector = if (playbackInfo.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (playbackInfo.isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        IconButton(
            onClick = { onAction(PlayerAction.Next) },
            enabled = playbackInfo.hasNext,
            interactionSource = nextInteractionSource,
            modifier = Modifier
                .size(40.dp)
                .springPress(nextInteractionSource, pressedScale = 0.85f)
                .align(Alignment.CenterVertically)
                .testTag("mini_player_next")
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Next song",
                tint = if (playbackInfo.hasNext) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
