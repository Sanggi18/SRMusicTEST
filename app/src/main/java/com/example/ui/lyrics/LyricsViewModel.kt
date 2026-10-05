package com.example.ui.lyrics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.model.Song
import com.example.lyrics.LyricsDocument
import com.example.lyrics.LyricsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LyricsUiState {
    data object Idle : LyricsUiState

    data class Loading(
        val songId: Long
    ) : LyricsUiState

    data class Ready(
        val songId: Long,
        val document: LyricsDocument
    ) : LyricsUiState
}

class LyricsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LyricsRepository()

    private val _uiState = MutableStateFlow<LyricsUiState>(LyricsUiState.Idle)
    val uiState: StateFlow<LyricsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var requestedSongId: Long? = null

    fun load(song: Song) {
        if (requestedSongId == song.id && loadJob?.isActive == true) return

        requestedSongId = song.id
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _uiState.value = LyricsUiState.Loading(song.id)

            val result = repository.load(song)

            ensureActive()

            // Prevent an older slow read from publishing into a newer song.
            if (requestedSongId == song.id) {
                _uiState.value = LyricsUiState.Ready(
                    songId = song.id,
                    document = result
                )
            }
        }
    }

    override fun onCleared() {
        loadJob?.cancel()
        super.onCleared()
    }
}
