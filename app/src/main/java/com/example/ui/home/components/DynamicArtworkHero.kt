package com.example.ui.home.components

import android.app.Activity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.core.model.Song
import com.example.ui.common.components.ArtworkCard
import com.example.ui.common.components.ArtworkPlaceholderType
import com.example.ui.common.components.ArtworkShape
import com.example.ui.common.theme.springPress
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun DynamicArtworkHero(
    songs: List<Song>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentPlayingSong: Song? = null,
    placeholderType: ArtworkPlaceholderType = ArtworkPlaceholderType.ALBUM,
    extraAction: (@Composable () -> Unit)? = null
) {
    val view = LocalView.current
    val isAppInLightMode = MaterialTheme.colorScheme.background.luminance() > 0.5f
    DisposableEffect(isAppInLightMode) {
        val window = (view.context as? Activity)?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        insetsController?.isAppearanceLightStatusBars = false
        onDispose {
            insetsController?.isAppearanceLightStatusBars = isAppInLightMode
        }
    }

    // Filter valid artwork candidates from current active songs list
    val artworkCandidates = remember(songs) {
        val withArt = songs.filter { it.artworkUri != null || it.dataPath.isNotBlank() }
        if (withArt.isNotEmpty()) withArt else songs
    }

    var currentIndex by remember(artworkCandidates, currentPlayingSong) {
        val playingIndex = if (currentPlayingSong != null) {
            artworkCandidates.indexOfFirst { it.id == currentPlayingSong.id }
        } else -1
        mutableIntStateOf(if (playingIndex >= 0) playingIndex else 0)
    }

    LaunchedEffect(artworkCandidates, currentPlayingSong) {
        if (currentPlayingSong != null) {
            val idx = artworkCandidates.indexOfFirst { it.id == currentPlayingSong.id }
            if (idx >= 0) {
                currentIndex = idx
                return@LaunchedEffect
            }
        }
        if (artworkCandidates.size > 1) {
            while (isActive) {
                delay(4000L)
                currentIndex = (currentIndex + 1) % artworkCandidates.size
            }
        }
    }

    val displaySong = currentPlayingSong?.takeIf { playing ->
        artworkCandidates.any { it.id == playing.id }
    } ?: artworkCandidates.getOrNull(currentIndex)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        // Hero artwork with subtle crossfade matching the active context
        Crossfade(
            targetState = displaySong,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            label = "hero_artwork_crossfade",
            modifier = Modifier.fillMaxSize()
        ) { song ->
            if (song != null) {
                ArtworkCard(
                    artworkUri = song.artworkUri,
                    dataPath = song.dataPath,
                    albumId = song.albumId,
                    dateModified = song.dateModified,
                    title = song.title,
                    size = 420.dp,
                    shape = ArtworkShape.ROUNDED_SQUARE,
                    cornerRadius = 0.dp,
                    placeholderType = placeholderType,
                    topCrop = true,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }

        // Top dark gradient scrim for status bar readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.70f),
                            Color.Black.copy(alpha = 0.30f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Bottom gradient fade blending into background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.35f to Color.Transparent,
                            0.60f to MaterialTheme.colorScheme.background.copy(alpha = 0.25f),
                            0.78f to MaterialTheme.colorScheme.background.copy(alpha = 0.65f),
                            0.92f to MaterialTheme.colorScheme.background.copy(alpha = 0.90f),
                            1.0f to MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        // Back button (Top Start)
        val backInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        IconButton(
            onClick = onBackClick,
            interactionSource = backInteractionSource,
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 8.dp, start = 12.dp)
                .springPress(backInteractionSource, pressedScale = 0.88f)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.40f))
                .testTag("hero_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        // Optional extra action button (e.g. Refresh on Top End)
        if (extraAction != null) {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 8.dp, end = 12.dp)
                    .align(Alignment.TopEnd)
            ) {
                extraAction()
            }
        }
    }
}
