package com.example.ui.player.components
import com.example.ui.player.core.*
import com.example.ui.common.components.*

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.PlaybackInfo
import com.example.ui.common.theme.ExpressiveMotion
import com.example.ui.common.theme.LocalSpacing
import com.example.ui.common.theme.springPress

@Composable
fun PlayerControlsRow(
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    playbackInfo: PlaybackInfo,
    onAction: (PlayerAction) -> Unit,
    modifier: Modifier = Modifier,
    playButtonColor: Color? = null,
    onPlayButtonColor: Color? = null,
    iconTintColor: Color? = null,
    activeAccentColor: Color? = null
) {
    val spacing = LocalSpacing.current
    val playBg = playButtonColor ?: MaterialTheme.colorScheme.primary
    val playOnBg = onPlayButtonColor ?: MaterialTheme.colorScheme.onPrimary
    val iconTint = iconTintColor ?: MaterialTheme.colorScheme.onSurface
    val activeTint = activeAccentColor ?: MaterialTheme.colorScheme.primary
    val inactiveTint = iconTintColor?.copy(alpha = 0.5f) ?: MaterialTheme.colorScheme.onSurfaceVariant

    // Expressive Play/Pause morph & spring scale
    val playInteractionSource = remember { MutableInteractionSource() }
    val isPlayPressed by playInteractionSource.collectIsPressedAsState()
    val playScale by animateFloatAsState(
        targetValue = if (isPlayPressed) 0.88f else if (playbackInfo.isPlaying) 1.02f else 1.0f,
        animationSpec = ExpressiveMotion.BouncySpring,
        label = "expressive_play_scale"
    )
    val playElevation by animateDpAsState(
        targetValue = if (playbackInfo.isPlaying) 8.dp else 4.dp,
        animationSpec = ExpressiveMotion.SnappySpringDp,
        label = "expressive_play_elevation"
    )
    val playCornerRadius by animateDpAsState(
        targetValue = if (playbackInfo.isPlaying) 24.dp else 36.dp,
        animationSpec = ExpressiveMotion.SpatialSpringDp,
        label = "expressive_play_shape"
    )

    // Secondary controls interaction sources
    val prevInteractionSource = remember { MutableInteractionSource() }
    val nextInteractionSource = remember { MutableInteractionSource() }
    val shuffleInteractionSource = remember { MutableInteractionSource() }
    val repeatInteractionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.small),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Shuffle Button with expressive active pill
        IconButton(
            onClick = { onAction(PlayerAction.ToggleShuffle) },
            interactionSource = shuffleInteractionSource,
            modifier = Modifier
                .size(spacing.minTouchTarget)
                .springPress(shuffleInteractionSource, pressedScale = 0.86f)
                .testTag("now_playing_shuffle_btn")
        ) {
            Icon(
                imageVector = Icons.Rounded.Shuffle,
                contentDescription = "Shuffle",
                tint = if (isShuffle) activeTint else inactiveTint,
                modifier = Modifier.size(24.dp)
            )
        }

        // 2. Previous Track Button
        IconButton(
            onClick = { onAction(PlayerAction.Previous) },
            interactionSource = prevInteractionSource,
            modifier = Modifier
                .size(52.dp)
                .springPress(prevInteractionSource, pressedScale = 0.86f)
                .testTag("now_playing_previous")
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous track",
                tint = iconTint,
                modifier = Modifier.size(34.dp)
            )
        }

        // 3. Central Expressive Play/Pause Action Button (72.dp M3 Expressive)
        Surface(
            shape = RoundedCornerShape(playCornerRadius),
            color = playBg,
            shadowElevation = playElevation,
            modifier = Modifier
                .size(72.dp)
                .graphicsLayer {
                    scaleX = playScale
                    scaleY = playScale
                }
                .clip(RoundedCornerShape(playCornerRadius))
        ) {
            IconButton(
                onClick = { onAction(PlayerAction.PlayPause) },
                interactionSource = playInteractionSource,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("now_playing_play_pause")
            ) {
                if (playbackInfo.isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
                        color = playOnBg
                    )
                } else {
                    Icon(
                        imageVector = if (playbackInfo.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (playbackInfo.isPlaying) "Pause" else "Play",
                        tint = playOnBg,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }

        // 4. Next Track Button
        IconButton(
            onClick = { onAction(PlayerAction.Next) },
            enabled = playbackInfo.hasNext,
            interactionSource = nextInteractionSource,
            modifier = Modifier
                .size(52.dp)
                .springPress(nextInteractionSource, pressedScale = 0.86f)
                .testTag("now_playing_next")
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Next track",
                tint = if (playbackInfo.hasNext) iconTint else iconTint.copy(alpha = 0.38f),
                modifier = Modifier.size(34.dp)
            )
        }

        // 5. Repeat Button with expressive active state
        IconButton(
            onClick = { onAction(PlayerAction.ToggleRepeat) },
            interactionSource = repeatInteractionSource,
            modifier = Modifier
                .size(spacing.minTouchTarget)
                .springPress(repeatInteractionSource, pressedScale = 0.86f)
                .testTag("now_playing_repeat_btn")
        ) {
            val repeatIcon = when (repeatMode) {
                RepeatMode.ONE -> Icons.Rounded.RepeatOne
                else -> Icons.Rounded.Repeat
            }
            val isRepeatActive = repeatMode != RepeatMode.OFF
            Icon(
                imageVector = repeatIcon,
                contentDescription = "Repeat",
                tint = if (isRepeatActive) activeTint else inactiveTint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun PlayerActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String,
    isActive: Boolean = false,
    iconTint: Color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            ),
            color = iconTint
        )
    }
}

@Composable
fun PlayerBottomActions(
    onOpenQueue: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onShowInfo: () -> Unit,
    onShowDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenLyrics: (() -> Unit)? = null,
    iconTintColor: Color? = null,
    activeAccentColor: Color? = null
) {
    var showOverflowMenu by remember { mutableStateOf(false) }
    val defaultIconTint = iconTintColor ?: MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onOpenLyrics != null) {
            PlayerActionButton(
                icon = Icons.Rounded.Lyrics,
                label = "Lyrics",
                onClick = onOpenLyrics,
                testTag = "now_playing_action_lyrics",
                iconTint = defaultIconTint
            )
        }
        PlayerActionButton(
            icon = Icons.Rounded.QueueMusic,
            label = "Queue",
            onClick = onOpenQueue,
            testTag = "now_playing_action_queue",
            iconTint = defaultIconTint
        )
        PlayerActionButton(
            icon = Icons.Rounded.Equalizer,
            label = "Equalizer",
            onClick = onNavigateToEqualizer,
            testTag = "now_playing_action_equalizer",
            iconTint = defaultIconTint
        )
        Box {
            PlayerActionButton(
                icon = Icons.Rounded.MoreVert,
                label = "More",
                onClick = { showOverflowMenu = true },
                testTag = "now_playing_action_overflow",
                iconTint = defaultIconTint
            )
            DropdownMenu(
                expanded = showOverflowMenu,
                onDismissRequest = { showOverflowMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Song Info") },
                    leadingIcon = { Icon(Icons.Rounded.Info, contentDescription = null) },
                    onClick = {
                        showOverflowMenu = false
                        onShowInfo()
                    },
                    modifier = Modifier.testTag("now_playing_menu_info")
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        showOverflowMenu = false
                        onShowDelete()
                    },
                    modifier = Modifier.testTag("now_playing_menu_delete")
                )
            }
        }
    }
}

@Composable
fun PlayerAudioPill(
    audioPillText: String,
    modifier: Modifier = Modifier,
    pillBgColor: Color? = null,
    pillTextColor: Color? = null
) {
    val bg = pillBgColor ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val textCol = pillTextColor ?: MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = bg,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                pillTextColor?.copy(alpha = 0.2f) ?: MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = audioPillText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = textCol
                ),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
            )
        }
    }
}
