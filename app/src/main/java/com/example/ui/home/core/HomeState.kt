package com.example.ui.home.core

import com.example.core.model.Song
import com.example.ui.library.core.SongAction

sealed interface HomeAction {
    data class PlaySong(val song: Song, val playlist: List<Song> = emptyList()) : HomeAction
    data class PlayShuffleSession(val startSong: Song, val tracks: List<Song> = emptyList()) : HomeAction
    data object RefreshShuffleTracks : HomeAction
    data object RefreshRemixQueue : HomeAction
    data object NextDailyGenre : HomeAction
    data class SelectDailyGenre(val index: Int) : HomeAction
    data class ToggleFavorite(val songId: Long) : HomeAction
    data class PlayNext(val song: Song) : HomeAction
    data class AddToQueue(val song: Song) : HomeAction
    data class HandleSongAction(val action: SongAction, val song: Song) : HomeAction
}

data class HomeUiState(
    val songs: List<Song> = emptyList(),
    val historySongs: List<Song> = emptyList(),
    val totalPlayCount: Int = 0,
    val totalListenedSeconds: Long = 0L,
    val songPlayCounts: Map<Long, Int> = emptyMap(),
    val shuffleTracks: List<Song> = emptyList(),
    val remixQueue: List<Song> = emptyList(),
    val dailyGenres: List<DailyGenreItem> = emptyList(),
    val selectedDailyGenreIndex: Int = 0,
    val recentSongs: List<Song> = emptyList(),
    val top1Artist: Map.Entry<String, List<Song>>? = null,
    val top1Album: Map.Entry<String, List<Song>>? = null,
    val top1Song: Song? = null,
    val topMostPlayed: List<Song> = emptyList(),
    val totalAlbumsCount: Int = 0,
    val totalArtistsCount: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
)
