package com.example.ui.player
import com.example.ui.player.components.*
import com.example.ui.player.core.*
import com.example.ui.common.components.*

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PlayerHost(
    uiState: PlayerUiState,
    onAction: (PlayerAction) -> Unit,
    playbackProgressFlow: kotlinx.coroutines.flow.StateFlow<com.example.core.model.PlaybackProgress>? = null,
    isEqualizerOpen: Boolean = false,
    isLyricsOpen: Boolean = false,
    isSettingsOpen: Boolean = false,
    hasDrawerScreenOpen: Boolean = false,
    onNavigateToEqualizer: () -> Unit = {},
    onNavigateToLyrics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentSong = uiState.currentSong
    if (currentSong == null || uiState.isDismissed || isEqualizerOpen || isLyricsOpen || isSettingsOpen || hasDrawerScreenOpen) return

    val coroutineScope = rememberCoroutineScope()
    val expansionAnimatable = remember { Animatable(if (uiState.isExpanded) 1f else 0f) }
    val expansionProgress = expansionAnimatable.value

    BackHandler(enabled = uiState.isExpanded && !isLyricsOpen && !isEqualizerOpen && !isSettingsOpen) {
        onAction(PlayerAction.Collapse)
    }

    LaunchedEffect(uiState.isExpanded) {
        val target = if (uiState.isExpanded) 1f else 0f
        if (kotlin.math.abs(expansionAnimatable.value - target) > 0.02f && !expansionAnimatable.isRunning) {
            expansionAnimatable.animateTo(
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = 0.90f,
                    stiffness = 520f
                )
            )
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenHeight = constraints.maxHeight.toFloat()
        val screenHeightDp = maxHeight
        val screenWidthDp = maxWidth
        val density = LocalDensity.current
        val collapsedHeight = 55.dp
        val dismissOffsetY = remember { Animatable(0f) }
        val progress = expansionProgress.coerceIn(0f, 1f)

        val navBarBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val defaultBottomPadding = 80.dp + navBarBottomInset + 16.dp

        var targetArtworkCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
        var surfaceCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

        val currentHorizontalPadding = (12 * (1f - progress)).dp
        val currentCornerRadius = (28 * (1f - progress)).dp
        val currentBottomPadding = lerp(defaultBottomPadding, 0.dp, progress)
        val currentHeight = lerp(collapsedHeight, screenHeightDp, progress)
        val surfaceShape = RoundedCornerShape(currentCornerRadius)
        val containerColor = MaterialTheme.colorScheme.background
        val shadowElevation = (6 * (1f - progress)).dp
        val tonalElevation = 0.dp
        val borderWidth = (0.5f * (1f - progress)).dp
        val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f * (1f - progress))

        val dismissAlpha = if (progress == 0f && dismissOffsetY.value > 0f) {
            (1f - (dismissOffsetY.value / 250f)).coerceIn(0f, 1f)
        } else {
            1f
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset { IntOffset(0, dismissOffsetY.value.roundToInt()) }
                .padding(horizontal = currentHorizontalPadding)
                .padding(bottom = currentBottomPadding)
                .fillMaxWidth()
                .height(currentHeight)
                .clip(surfaceShape)
                .onGloballyPositioned { coords ->
                    surfaceCoordinates = coords
                }
                .graphicsLayer { alpha = dismissAlpha }
                .then(
                    if (borderWidth > 0.dp) {
                        Modifier.border(
                            width = borderWidth,
                            color = borderColor,
                            shape = surfaceShape
                        )
                    } else Modifier
                )
                .playerSurfaceVerticalDrag(
                    coroutineScope = coroutineScope,
                    expansionAnimatable = expansionAnimatable,
                    dismissOffsetY = dismissOffsetY,
                    screenHeight = screenHeight,
                    density = density,
                    onExpandedChange = { expanded ->
                        onAction(PlayerAction.SetExpanded(expanded))
                    },
                    onDismissMiniPlayer = {
                        onAction(PlayerAction.Dismiss)
                    }
                )
                .testTag("interactive_player_surface"),
            shape = surfaceShape,
            color = containerColor,
            shadowElevation = shadowElevation,
            tonalElevation = tonalElevation
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val nowPlayingAlpha = ((progress - 0.15f) / 0.45f).coerceIn(0f, 1f)
                NowPlayingScreen(
                    uiState = uiState,
                    onAction = { action ->
                        if (action is PlayerAction.OpenEqualizer) {
                            coroutineScope.launch {
                                onAction(PlayerAction.Collapse)
                                expansionAnimatable.snapTo(0f)
                                onNavigateToEqualizer()
                            }
                        } else {
                            onAction(action)
                        }
                    },
                    playbackProgressFlow = playbackProgressFlow,
                    containerColor = Color.Transparent,
                    hideArtwork = true,
                    onArtworkCoordinatesMeasured = { coords ->
                        targetArtworkCoordinates = coords
                    },
                    onOpenLyrics = onNavigateToLyrics,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = nowPlayingAlpha
                        }
                )

                if (progress < 0.70f) {
                    val miniPlayerAlpha = (1f - (progress / 0.35f)).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = miniPlayerAlpha
                            }
                            .clickable(
                                enabled = progress < 0.05f,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                coroutineScope.launch {
                                    onAction(PlayerAction.Expand)
                                    expansionAnimatable.animateTo(
                                        targetValue = 1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                            }
                            .testTag("mini_player")
                    ) {
                        MiniPlayerContent(
                            currentSong = currentSong,
                            playbackInfo = uiState.playbackInfo,
                            onAction = onAction,
                            showArtwork = false
                        )
                    }
                }

                MorphingPlayerArtwork(
                    song = currentSong,
                    progress = progress,
                    targetArtworkCoordinates = targetArtworkCoordinates,
                    surfaceCoordinates = surfaceCoordinates,
                    screenWidthDp = screenWidthDp,
                    currentHorizontalPadding = currentHorizontalPadding,
                    density = density
                )
            }
        }
    }
}
