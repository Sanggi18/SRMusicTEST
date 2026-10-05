package com.example.ui.player.style.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.core.model.NowPlayingTheme
import com.example.core.model.Song
import com.example.core.util.ArtworkColors
import com.example.core.util.ArtworkPaletteExtractor
import com.example.ui.common.components.GlideArtworkImage

@Composable
fun NowPlayingThemeContainer(
    theme: NowPlayingTheme,
    song: Song?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    val bgColor = MaterialTheme.colorScheme.background
    val defaultPrimary = MaterialTheme.colorScheme.primary
    val isDark = bgColor.luminance() <= 0.5f

    // Extract artwork palette when COLOR, BLUR, or BLUR_2 is selected
    val artworkColors = produceState(
        initialValue = ArtworkPaletteExtractor.defaultColors(defaultPrimary, darkBg = bgColor, lightBg = bgColor, isDark = isDark),
        key1 = song?.id,
        key2 = theme,
        key3 = isDark
    ) {
        if (theme != NowPlayingTheme.DEFAULT) {
            value = ArtworkPaletteExtractor.extractColors(context, song, isDarkTheme = isDark)
        }
    }

    val currentContainerBg = when (theme) {
        NowPlayingTheme.DEFAULT -> bgColor
        NowPlayingTheme.COLOR -> artworkColors.value.getSurface(isDark)
        NowPlayingTheme.BLUR, NowPlayingTheme.BLUR_2 -> bgColor
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(currentContainerBg)
    ) {
        when (theme) {
            NowPlayingTheme.DEFAULT, NowPlayingTheme.COLOR -> {
                // Solid clean background (AyraMusic signature)
            }
            NowPlayingTheme.BLUR -> {
                // Full blurred artwork backdrop with contrast scrim
                if (song != null) {
                    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.fillMaxSize().blur(32.dp)
                    } else {
                        Modifier.fillMaxSize()
                    }

                    GlideArtworkImage(
                        filePath = song.dataPath,
                        albumId = song.albumId,
                        contentDescription = null,
                        modifier = blurModifier,
                        targetSizePx = 500,
                        dateModified = song.dateModified,
                        artworkUri = song.artworkUri
                    )

                    // Scrim overlay to ensure high contrast and readability
                    val scrimAlpha = if (isDark) 0.78f else 0.86f
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bgColor.copy(alpha = scrimAlpha))
                    )
                }
            }
            NowPlayingTheme.BLUR_2 -> {
                // RetroMusicPlayer reference (Centered Blur):
                // Centered blurred artwork backdrop covering the entire screen behind the player
                if (song != null) {
                    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.fillMaxSize().blur(45.dp)
                    } else {
                        Modifier.fillMaxSize()
                    }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        GlideArtworkImage(
                            filePath = song.dataPath,
                            albumId = song.albumId,
                            contentDescription = null,
                            modifier = blurModifier,
                            targetSizePx = 500,
                            dateModified = song.dateModified,
                            artworkUri = song.artworkUri
                        )
                    }

                    // Soft luminous top gradient + solid fading scrim at the bottom
                    val scrimColors = if (isDark) {
                        listOf(
                            bgColor.copy(alpha = 0.40f),
                            bgColor.copy(alpha = 0.72f),
                            bgColor.copy(alpha = 0.94f)
                        )
                    } else {
                        listOf(
                            bgColor.copy(alpha = 0.50f),
                            bgColor.copy(alpha = 0.82f),
                            bgColor.copy(alpha = 0.96f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(scrimColors)
                            )
                    )
                }
            }
        }

        content()
    }
}
