package com.example.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Transaction
    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE ASC")
    fun getAllSongsWithFavorites(): Flow<List<SongWithFavorite>>

    @Transaction
    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE ASC")
    suspend fun getAllSongsWithFavoritesDirect(): List<SongWithFavorite>

    @Query("SELECT * FROM songs")
    suspend fun getAllSongEntities(): List<SongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSongs(songs: List<SongEntity>): List<Long>

    @Query("DELETE FROM songs WHERE id IN (:ids)")
    suspend fun deleteSongsByIds(ids: List<Long>): Int

    // --- Favorites ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity): Long

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun deleteFavorite(songId: Long): Int

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    suspend fun isFavorite(songId: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    fun observeIsFavorite(songId: Long): Flow<Boolean>

    @Transaction
    @Query("SELECT songs.* FROM songs INNER JOIN favorites ON songs.id = favorites.songId ORDER BY favorites.addedAt DESC")
    fun getFavoriteSongs(): Flow<List<SongWithFavorite>>

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int

    // --- Most Played ---

    @Query("SELECT COALESCE(SUM(count), 0) FROM play_counts")
    fun getTotalPlayCount(): Flow<Int>

    @Query("SELECT * FROM play_counts")
    fun getAllPlayCounts(): Flow<List<PlayCountEntity>>

    @Query("INSERT INTO play_counts (songId, count, lastPlayedTimestamp) VALUES (:songId, 1, :timestamp) ON CONFLICT(songId) DO UPDATE SET count = count + 1, lastPlayedTimestamp = :timestamp")
    suspend fun incrementPlayCount(songId: Long, timestamp: Long): Long

    @Transaction
    @Query("""
        SELECT songs.* FROM songs
        INNER JOIN play_counts ON songs.id = play_counts.songId
        WHERE play_counts.count > 0
        ORDER BY play_counts.count DESC, play_counts.lastPlayedTimestamp DESC
        LIMIT :limit
    """)
    fun getMostPlayedSongs(limit: Int): Flow<List<SongWithFavorite>>
}
