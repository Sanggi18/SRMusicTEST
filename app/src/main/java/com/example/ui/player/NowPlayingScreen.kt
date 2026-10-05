package com.example.ui.player
import com.example.ui.player.components.*
import com.example.ui.player.core.*
import com.example.ui.common.components.*
import com.example.core.model.PlaybackProgress
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.NowPlayingTheme
import com.example.ui.player.style.progress.PlayerProgressContainer
import com.example.ui.player.style.theme.NowPlayingThemeContainer
import com.example.ui.player.style.trackinfo.PlayerTrackInfo

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.ui.lyrics.LyricsScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Song
import com.example.core.util.ArtworkPaletteExtractor
import com.example.ui.common.components.GlideArtworkImage
import com.example.core.util.TimeUtils
import com.example.ui.common.theme.ExpressiveMotion
import com.example.ui.common.theme.LocalSpacing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun NowPlaying(
    uiState: PlayerUiState,
    onAction: (PlayerAction) -> Unit,
    playbackProgressFlow: StateFlow<PlaybackProgress>? = null,
    containerColor: Color = MaterialTheme.colorScheme.background,
    hideArtwork: Boolean = false,
    onArtworkCoordinatesMeasured: ((LayoutCoordinates) -> Unit)? = null,
    onOpenLyrics: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    NowPlayingScreen(
        uiState = uiState,
        onAction = onAction,
        playbackProgressFlow = playbackProgressFlow,
        containerColor = containerColor,
        hideArtwork = hideArtwork,
        onArtworkCoordinatesMeasured = onArtworkCoordinatesMeasured,
        onOpenLyrics = onOpenLyrics,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    uiState: PlayerUiState,
    onAction: (PlayerAction) -> Unit,
    playbackProgressFlow: StateFlow<PlaybackProgress>? = null,
    containerColor: Color = MaterialTheme.colorScheme.background,
    hideArtwork: Boolean = false,
    onArtworkCoordinatesMeasured: ((LayoutCoordinates) -> Unit)? = null,
    onOpenLyrics: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val song = uiState.currentSong ?: return

    val currentProgress = if (playbackProgressFlow != null) {
        playbackProgressFlow.collectAsStateWithLifecycle().value
    } else {
        uiState.playbackProgress
    }

    var showInfoDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() <= 0.5f

    // Dynamic color extraction for COLOR, BLUR, and BLUR_2 themes (Ayra & dynamic harmony)
    val artworkColorsState = produceState(
        initialValue = ArtworkPaletteExtractor.defaultColors(
            primary = MaterialTheme.colorScheme.primary,
            darkBg = MaterialTheme.colorScheme.background,
            lightBg = MaterialTheme.colorScheme.background,
            isDark = isDarkTheme
        ),
        key1 = song.id,
        key2 = uiState.nowPlayingTheme,
        key3 = isDarkTheme
    ) {
        if (uiState.nowPlayingTheme != NowPlayingTheme.DEFAULT) {
            value = ArtworkPaletteExtractor.extractColors(context, song, isDarkTheme)
        }
    }

    val isDynamicTheme = uiState.nowPlayingTheme != NowPlayingTheme.DEFAULT
    val dynamicActiveProgress = if (isDynamicTheme) artworkColorsState.value.activeProgress else null
    val dynamicInactiveProgress = if (isDynamicTheme) artworkColorsState.value.inactiveProgress else null
    val dynamicPlayButtonColor = if (isDynamicTheme) artworkColorsState.value.controlButton else null
    val dynamicOnPlayButtonColor = if (isDynamicTheme) artworkColorsState.value.onControlButton else null
    val dynamicIconTintColor = if (isDynamicTheme) artworkColorsState.value.controlIcon else null
    val dynamicActiveAccentColor = if (isDynamicTheme) artworkColorsState.value.primary else null

    val audioPillText = remember(song.id, uiState.technicalInfo) {
        val techInfo = uiState.technicalInfo
        if (techInfo != null && techInfo.bitrate.isNotBlank() && techInfo.sampleRate.isNotBlank()) {
            val format = if (techInfo.format.contains("aac", ignoreCase = true)) "M4A" else techInfo.format.uppercase()
            "$format • ${techInfo.bitrate.lowercase()} • ${techInfo.sampleRate.lowercase()}"
        } else {
            var format = if (song.displayName.contains('.')) {
                song.displayName.substringAfterLast('.').uppercase()
            } else if (song.dataPath.contains('.')) {
                song.dataPath.substringAfterLast('.').uppercase()
            } else {
                "MP3"
            }
            if (format.contains("AAC", ignoreCase = true) || format.contains("MP4", ignoreCase = true)) {
                format = "M4A"
            }
            val bitrate = if (song.duration > 0 && song.size > 0) {
                val calculatedKbps = ((song.size * 8) / song.duration).toInt()
                "${calculatedKbps}kb/s"
            } else {
                "320kb/s"
            }
            "$format • $bitrate • 44.1khz"
        }
    }

    val effectiveScaffoldColor = if (uiState.nowPlayingTheme == NowPlayingTheme.DEFAULT) containerColor else Color.Transparent

    // Material Expressive playback-reactive artwork spring physics
    val isPlaying = uiState.playbackInfo.isPlaying
    val artworkScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.89f,
        animationSpec = ExpressiveMotion.BouncySpring,
        label = "expressive_artwork_scale"
    )
    val artworkCornerRadius by animateDpAsState(
        targetValue = if (isPlaying) 28.dp else 22.dp,
        animationSpec = ExpressiveMotion.SpatialSpringDp,
        label = "expressive_artwork_corners"
    )
    val artworkElevation by animateDpAsState(
        targetValue = if (isPlaying) 16.dp else 6.dp,
        animationSpec = ExpressiveMotion.SnappySpringDp,
        label = "expressive_artwork_elevation"
    )

    NowPlayingThemeContainer(
        theme = uiState.nowPlayingTheme,
        song = song
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .testTag("now_playing_screen"),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = effectiveScaffoldColor,
            topBar = {
                TopAppBar(
                    title = {},
                    windowInsets = WindowInsets.statusBars,
                    navigationIcon = {
                        IconButton(
                            onClick = { onAction(PlayerAction.Collapse) },
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .testTag("now_playing_dismiss")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Collapse Now Playing",
                                tint = dynamicIconTintColor ?: MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    actions = {},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.height(2.dp))

                    NowPlayingArtworkAnchor(
                        onCoordinatesMeasured = onArtworkCoordinatesMeasured
                    ) {
                        if (!hideArtwork) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = artworkScale
                                        scaleY = artworkScale
                                    }
                                    .shadow(
                                        elevation = artworkElevation,
                                        shape = RoundedCornerShape(artworkCornerRadius),
                                        spotColor = dynamicActiveAccentColor?.copy(alpha = if (isPlaying) 0.42f else 0.2f)
                                            ?: MaterialTheme.colorScheme.primary.copy(alpha = if (isPlaying) 0.32f else 0.15f)
                                    )
                                    .clip(RoundedCornerShape(artworkCornerRadius)),
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
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PlayerTrackInfo(
                        song = song,
                        alignment = uiState.trackInfoAlignment
                    )

                    PlayerProgressContainer(
                        progress = currentProgress,
                        isPlaying = uiState.playbackInfo.isPlaying,
                        progressStyle = uiState.progressStyle,
                        onSeek = { targetMs -> onAction(PlayerAction.SeekTo(targetMs)) },
                        song = song,
                        activeColor = dynamicActiveProgress,
                        inactiveColor = dynamicInactiveProgress
                    )

                    PlayerControlsRow(
                        isShuffle = uiState.isShuffle,
                        repeatMode = uiState.repeatMode,
                        playbackInfo = uiState.playbackInfo,
                        onAction = onAction,
                        playButtonColor = dynamicPlayButtonColor,
                        onPlayButtonColor = dynamicOnPlayButtonColor,
                        iconTintColor = dynamicIconTintColor,
                        activeAccentColor = dynamicActiveAccentColor
                    )

                    PlayerAudioPill(
                        audioPillText = audioPillText,
                        pillBgColor = dynamicIconTintColor?.copy(alpha = 0.12f),
                        pillTextColor = dynamicIconTintColor
                    )

                    PlayerBottomActions(
                        onOpenLyrics = onOpenLyrics,
                        onOpenQueue = { showQueueSheet = true },
                        onNavigateToEqualizer = { onAction(PlayerAction.OpenEqualizer) },
                        onShowInfo = { showInfoDialog = true },
                        onShowDelete = { showDeleteDialog = true },
                        iconTintColor = dynamicIconTintColor,
                        activeAccentColor = dynamicActiveAccentColor
                    )
                }
            }
        }
    }

    if (showInfoDialog) {
        val techInfo = uiState.technicalInfo
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Song Info", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Title: ${song.title}", style = MaterialTheme.typography.bodyMedium)
                    Text("Artist: ${song.artist}", style = MaterialTheme.typography.bodyMedium)
                    Text("Album: ${song.album}", style = MaterialTheme.typography.bodyMedium)
                    Text("Duration: ${TimeUtils.formatDuration(song.duration)}", style = MaterialTheme.typography.bodyMedium)
                    if (techInfo != null) {
                        Text("Format: ${techInfo.format}", style = MaterialTheme.typography.bodyMedium)
                        Text("Bit Depth & Sample Rate: ${techInfo.bitDepth} / ${techInfo.sampleRate}", style = MaterialTheme.typography.bodyMedium)
                        Text("Bitrate: ${techInfo.bitrate}", style = MaterialTheme.typography.bodyMedium)
                        Text("Channels: ${techInfo.channels}", style = MaterialTheme.typography.bodyMedium)
                        if (techInfo.fileSizeFormatted.isNotBlank()) {
                            Text("File Size: ${techInfo.fileSizeFormatted}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (song.dataPath.isNotBlank()) {
                        Text("Path: ${song.dataPath}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showInfoDialog = false },
                    modifier = Modifier.testTag("song_info_dismiss")
                ) {
                    Text("Close")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Song", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete \"${song.title}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    },
                    modifier = Modifier.testTag("song_delete_confirm")
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showQueueSheet) {
        PlayerQueueSheet(
            queue = uiState.currentQueue,
            currentSongId = song.id,
            onDismissRequest = { showQueueSheet = false },
            onSelectQueueItem = { index ->
                onAction(PlayerAction.SelectQueueItem(index))
                showQueueSheet = false
            }
        )
    }
}
