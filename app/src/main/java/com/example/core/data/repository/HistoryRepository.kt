package com.example.core.data.repository

import android.content.Context
import com.example.core.data.database.AppDatabase
import com.example.core.data.database.SongDao
import com.example.core.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface HistoryRepository {
    val historySongIdsFlow: StateFlow<List<Long>>
    val mostPlayedSongsFlow: Flow<List<Song>>
    val totalPlayCountFlow: Flow<Int>
    val totalListenedSecondsFlow: StateFlow<Long>
    val allPlayCountsFlow: Flow<Map<Long, Int>>
    fun addListenedSeconds(seconds: Long)
    suspend fun recordSongPlay(songId: Long)
    suspend fun clearHistory()
}

class HistoryRepositoryImpl(
    private val context: Context,
    private val songDao: SongDao = AppDatabase.getInstance(context).songDao()
) : HistoryRepository {

    private val historyPrefs = context.getSharedPreferences("srmusic_history_prefs", Context.MODE_PRIVATE)
    private val _historySongIdsFlow = MutableStateFlow(loadValidHistoryIds())
    override val historySongIdsFlow: StateFlow<List<Long>> = _historySongIdsFlow.asStateFlow()

    private val statsPrefs = context.getSharedPreferences("srmusic_stats_prefs", Context.MODE_PRIVATE)
    private val _totalListenedSecondsFlow = MutableStateFlow(statsPrefs.getLong("total_listened_seconds", 0L))
    override val totalListenedSecondsFlow: StateFlow<Long> = _totalListenedSecondsFlow.asStateFlow()

    override val mostPlayedSongsFlow: Flow<List<Song>> = songDao.getMostPlayedSongs(20)
        .map { list -> list.map { it.toSong() } }
        .flowOn(Dispatchers.IO)

    override val totalPlayCountFlow: Flow<Int> = songDao.getTotalPlayCount()
        .flowOn(Dispatchers.IO)

    override val allPlayCountsFlow: Flow<Map<Long, Int>> = songDao.getAllPlayCounts()
        .map { list -> list.associate { it.songId to it.count } }
        .flowOn(Dispatchers.IO)

    override fun addListenedSeconds(seconds: Long) {
        if (seconds <= 0) return
        val current = _totalListenedSecondsFlow.value
        val updated = current + seconds
        statsPrefs.edit().putLong("total_listened_seconds", updated).apply()
        _totalListenedSecondsFlow.value = updated
    }

    private fun loadValidHistoryIds(): List<Long> {
        val raw = historyPrefs.getString("playback_history_v1", "") ?: ""
        if (raw.isBlank()) return emptyList()
        val now = System.currentTimeMillis()
        val cutoff = now - 86400000L // 24 hours
        val validPairs = raw.split(",").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val id = parts[0].toLongOrNull()
                val timestamp = parts[1].toLongOrNull()
                if (id != null && timestamp != null && timestamp >= cutoff) {
                    Pair(id, timestamp)
                } else null
            } else null
        }
        return validPairs.map { it.first }
    }

    override suspend fun recordSongPlay(songId: Long) = withContext(Dispatchers.IO) {
        // 1. Increment play count in Room for Most Played
        songDao.incrementPlayCount(songId, System.currentTimeMillis())

        // 2. Record in SharedPreferences for History (24h expiry, max 50 items)
        val now = System.currentTimeMillis()
        val cutoff = now - 86400000L
        val raw = historyPrefs.getString("playback_history_v1", "") ?: ""
        val existing = raw.split(",").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val id = parts[0].toLongOrNull()
                val timestamp = parts[1].toLongOrNull()
                if (id != null && timestamp != null && timestamp >= cutoff && id != songId) {
                    Pair(id, timestamp)
                } else null
            } else null
        }.toMutableList()

        existing.add(0, Pair(songId, now))
        val trimmed = existing.take(50)
        val encoded = trimmed.joinToString(",") { "${it.first}:${it.second}" }
        historyPrefs.edit().putString("playback_history_v1", encoded).apply()
        _historySongIdsFlow.value = trimmed.map { it.first }
    }

    override suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyPrefs.edit().remove("playback_history_v1").apply()
        _historySongIdsFlow.value = emptyList()
    }
}
