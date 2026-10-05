package com.example.ui.settings.appearance

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.NowPlayingProgressStyle
import com.example.core.model.NowPlayingTheme
import com.example.core.model.PlaybackProgress
import com.example.core.model.Song
import com.example.core.model.TrackInfoAlignment
import com.example.ui.player.style.progress.PlayerProgressContainer
import com.example.ui.settings.components.SimpleOptionSelectDialog
import kotlinx.coroutines.delay

private const val PREVIEW_TOTAL_DURATION_MS = 273000L // 04:33
private const val PREVIEW_START_POSITION_MS = 60000L   // 01:00 (menit 1)

/**
 * Now Playing Style Screen
 *
 * - Live preview card matching the app design system and standard card colors.
 * - Dynamic preview progress ticker running from 1:00 to end, draggable without reset.
 * - Single set of timestamps (no duplicates).
 * - Minimalist capsule option buttons triggering clean, card-based modal pickers.
 */
@Composable
fun NowPlayingStyleScreen(
    currentProgressStyle: NowPlayingProgressStyle,
    onProgressStyleChange: (NowPlayingProgressStyle) -> Unit,
    currentTheme: NowPlayingTheme,
    onThemeChange: (NowPlayingTheme) -> Unit,
    currentAlignment: TrackInfoAlignment,
    onAlignmentChange: (TrackInfoAlignment) -> Unit,
    previewSong: Song? = null,
    modifier: Modifier = Modifier
) {
    val displaySong = previewSong ?: Song(
        id = 0L,
        title = "Track Title",
        artist = "Artist Name",
        album = "Album Title",
        duration = PREVIEW_TOTAL_DURATION_MS,
        albumId = -1L,
        contentUri = Uri.EMPTY,
        artworkUri = Uri.EMPTY,
        dataPath = "",
        displayName = "Track Title",
        dateModified = 0L,
        size = 0L
    )

    // Dynamic self-advancing preview position (starts at 1:00, loops at end, draggable without reset)
    var currentPositionMs by remember { mutableLongStateOf(PREVIEW_START_POSITION_MS) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(100L)
            currentPositionMs += 100L
            if (currentPositionMs >= PREVIEW_TOTAL_DURATION_MS) {
                currentPositionMs = PREVIEW_START_POSITION_MS
            }
        }
    }

    val previewProgress = PlaybackProgress(
        currentPosition = currentPositionMs,
        duration = PREVIEW_TOTAL_DURATION_MS
    )

    // Dialog Visibility States
    var showProgressDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAlignmentDialog by remember { mutableStateOf(false) }

    // Unified M3 theme card background matching other SettingsCards
    val previewBg = when (currentTheme) {
        NowPlayingTheme.DEFAULT -> MaterialTheme.colorScheme.surfaceContainer
        NowPlayingTheme.COLOR -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        NowPlayingTheme.BLUR -> MaterialTheme.colorScheme.surfaceContainerHigh
        NowPlayingTheme.BLUR_2 -> MaterialTheme.colorScheme.surfaceContainerHighest
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HERO PREVIEW CARD (Standard Card Theme & Single Timestamp)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = previewBg
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("now_playing_preview_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Centered artwork placeholder matching card theme
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Track Title, Artist, Album Title aligned per currentAlignment
                val textAlignAlignment = when (currentAlignment) {
                    TrackInfoAlignment.LEFT -> Alignment.Start
                    TrackInfoAlignment.CENTER -> Alignment.CenterHorizontally
                    TrackInfoAlignment.RIGHT -> Alignment.End
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = textAlignAlignment
                ) {
                    Text(
                        text = displaySong.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = displaySong.artist,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = displaySong.album,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live animated progress bar (Includes single built-in timestamp row, no duplicate)
                PlayerProgressContainer(
                    progress = previewProgress,
                    isPlaying = true,
                    progressStyle = currentProgressStyle,
                    onSeek = { newPositionMs ->
                        currentPositionMs = newPositionMs
                    },
                    song = displaySong,
                    activeColor = MaterialTheme.colorScheme.primary,
                    inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // SECTION 1: Progress Bar Style
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Progress Bar Style",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )

            PillOptionCard(
                text = currentProgressStyle.label,
                onClick = { showProgressDialog = true },
                testTag = "pill_progress_bar_style"
            )
        }

        // SECTION 2: Now Playing Theme
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Now Playing Theme",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )

            PillOptionCard(
                text = currentTheme.label,
                onClick = { showThemeDialog = true },
                testTag = "pill_now_playing_theme"
            )
        }

        // SECTION 3: Track Info Alignment
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Track Info Alignment",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )

            PillOptionCard(
                text = currentAlignment.label,
                onClick = { showAlignmentDialog = true },
                testTag = "pill_track_info_alignment"
            )
        }
    }

    // Direct Modal Selection Dialogs (Card-based, minimal, auto-select on tap)
    if (showProgressDialog) {
        SimpleOptionSelectDialog(
            title = "Progress Bar Style",
            options = NowPlayingProgressStyle.entries,
            selectedOption = currentProgressStyle,
            onOptionSelected = onProgressStyleChange,
            onDismissRequest = { showProgressDialog = false },
            optionLabel = { it.label },
            testTagPrefix = "dialog_progress_style"
        )
    }

    if (showThemeDialog) {
        SimpleOptionSelectDialog(
            title = "Now Playing Theme",
            options = NowPlayingTheme.entries,
            selectedOption = currentTheme,
            onOptionSelected = onThemeChange,
            onDismissRequest = { showThemeDialog = false },
            optionLabel = { it.label },
            testTagPrefix = "dialog_now_playing_theme"
        )
    }

    if (showAlignmentDialog) {
        SimpleOptionSelectDialog(
            title = "Track Info Alignment",
            options = TrackInfoAlignment.entries,
            selectedOption = currentAlignment,
            onOptionSelected = onAlignmentChange,
            onDismissRequest = { showAlignmentDialog = false },
            optionLabel = { it.label },
            testTagPrefix = "dialog_track_info_alignment"
        )
    }
}

/**
 * Capsule / Pill Shaped Option Card matching the app's standard card colors
 */
@Composable
private fun PillOptionCard(
    text: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "Open options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
