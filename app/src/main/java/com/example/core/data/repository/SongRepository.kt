package com.example.core.data.repository

import android.content.Context
import com.example.core.data.database.AppDatabase
import com.example.core.data.database.SongDao
import com.example.core.data.database.SongEntity
import com.example.core.model.Song
import com.example.core.data.metadata.AudioTechnicalInfo
import com.example.core.data.metadata.MetadataExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface SongRepository {
    val allSongsFlow: Flow<List<Song>>
    suspend fun getSongCount(): Int
    suspend fun getAllSongEntities(): List<SongEntity>
    suspend fun getAllSongs(): List<Song>
    suspend fun getSongTechnicalInfo(song: Song): AudioTechnicalInfo
}

class SongRepositoryImpl(
    private val context: Context,
    private val songDao: SongDao = AppDatabase.getInstance(context).songDao()
) : SongRepository {

    override val allSongsFlow: Flow<List<Song>> = songDao.getAllSongsWithFavorites()
        .map { list -> list.map { it.toSong() } }
        .flowOn(Dispatchers.IO)

    override suspend fun getSongCount(): Int = withContext(Dispatchers.IO) {
        songDao.getSongCount()
    }

    override suspend fun getAllSongEntities(): List<SongEntity> = withContext(Dispatchers.IO) {
        songDao.getAllSongEntities()
    }

    override suspend fun getAllSongs(): List<Song> = withContext(Dispatchers.IO) {
        songDao.getAllSongsWithFavoritesDirect().map { it.toSong() }
    }

    override suspend fun getSongTechnicalInfo(song: Song): AudioTechnicalInfo {
        return MetadataExtractor.extractTechnicalInfo(context, song)
    }
}
