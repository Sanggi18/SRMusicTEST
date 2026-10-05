package com.example.ui.lyrics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.model.PlaybackProgress
import com.example.core.model.Song
import com.example.lyrics.LyricsContent
import kotlinx.coroutines.flow.StateFlow

@Composable
fun LyricsScreen(
    song: Song,
    playbackProgressFlow: StateFlow<PlaybackProgress>,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    viewModel: LyricsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(song.id) {
        viewModel.load(song)
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val current = state) {
            LyricsUiState.Idle -> Unit

            is LyricsUiState.Loading -> {
                LyricsLoading()
            }

            is LyricsUiState.Ready -> {
                if (current.songId != song.id) {
                    LyricsLoading()
                    return@Box
                }

                when (val content = current.document.content) {
                    is LyricsContent.Synced -> {
                        SyncedLyricsPanel(
                            song = song,
                            lyrics = content.lines,
                            playbackProgressFlow = playbackProgressFlow,
                            isPlaying = isPlaying
                        )
                    }

                    is LyricsContent.Plain -> {
                        PlainLyricsPanel(content.text)
                    }

                    LyricsContent.None -> {
                        LyricsEmpty()
                    }
                }
            }
        }
    }
}
