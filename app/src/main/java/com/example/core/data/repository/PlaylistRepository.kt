package com.example.core.data.repository

import android.content.Context
import android.net.Uri
import com.example.core.data.database.AppDatabase
import com.example.core.data.database.PlaylistDao
import com.example.core.data.database.PlaylistEntity
import com.example.core.data.database.PlaylistSongCrossRef
import com.example.core.model.Song
import com.example.core.data.playlist.PlaylistFileManager
import com.example.core.data.playlist.PlaylistFileManager.PlaylistFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class PlaylistImportData(
    val playlistName: String,
    val matchedSongs: List<Song>,
    val totalCount: Int
)

interface PlaylistRepository {
    val playlistsFlow: Flow<List<PlaylistEntity>>
    val allCrossRefsFlow: Flow<List<PlaylistSongCrossRef>>
    suspend fun createPlaylist(name: String): Long
    suspend fun renamePlaylist(playlistId: Long, newName: String)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun deleteMultiplePlaylists(playlistIds: List<Long>)
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)
    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>>
    suspend fun exportPlaylist(uri: Uri, playlistName: String, songs: List<Song>, formatName: String): Result<String>
    suspend fun importPlaylist(uri: Uri, allSongs: List<Song>): Result<PlaylistImportData>
}

class PlaylistRepositoryImpl(
    private val context: Context,
    private val playlistDao: PlaylistDao
) : PlaylistRepository {

    constructor(context: Context) : this(context.applicationContext, AppDatabase.getInstance(context).playlistDao())

    override val playlistsFlow: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
        .flowOn(Dispatchers.IO)

    override val allCrossRefsFlow: Flow<List<PlaylistSongCrossRef>> = playlistDao.getAllCrossRefs()
        .flowOn(Dispatchers.IO)

    override suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    override suspend fun renamePlaylist(playlistId: Long, newName: String) {
        withContext(Dispatchers.IO) {
            playlistDao.renamePlaylist(playlistId, newName)
        }
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        withContext(Dispatchers.IO) {
            playlistDao.deletePlaylistById(playlistId)
        }
    }

    override suspend fun deleteMultiplePlaylists(playlistIds: List<Long>) {
        withContext(Dispatchers.IO) {
            playlistDao.deletePlaylistsByIds(playlistIds)
        }
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        withContext(Dispatchers.IO) {
            playlistDao.insertCrossRef(PlaylistSongCrossRef(playlistId = playlistId, songId = songId))
        }
    }

    override suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        withContext(Dispatchers.IO) {
            val refs = songIds.mapIndexed { index, songId ->
                PlaylistSongCrossRef(playlistId = playlistId, songId = songId, orderIndex = index)
            }
            playlistDao.insertCrossRefs(refs)
        }
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        withContext(Dispatchers.IO) {
            playlistDao.removeSongFromPlaylist(playlistId, songId)
        }
    }

    override fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> =
        playlistDao.getSongsForPlaylist(playlistId)
            .map { list -> list.map { it.toSong() } }
            .flowOn(Dispatchers.IO)

    override suspend fun exportPlaylist(
        uri: Uri,
        playlistName: String,
        songs: List<Song>,
        formatName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val format = try {
                PlaylistFileManager.PlaylistFormat.valueOf(formatName)
            } catch (_: Exception) {
                PlaylistFileManager.PlaylistFormat.M3U
            }
            val bytes = PlaylistFileManager.exportPlaylistContent(
                playlistName = playlistName,
                songs = songs,
                format = format
            )
            context.contentResolver.openOutputStream(uri)?.use { os ->
                os.write(bytes)
            } ?: return@withContext Result.failure(Exception("Cannot open destination file"))
            Result.success("Playlist '$playlistName' exported successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun importPlaylist(
        uri: Uri,
        allSongs: List<Song>
    ): Result<PlaylistImportData> = withContext(Dispatchers.IO) {
        try {
            val parsed = PlaylistFileManager.parsePlaylistFile(context, uri, allSongs)
                ?: return@withContext Result.failure(Exception("Could not parse playlist file"))
            Result.success(
                PlaylistImportData(
                    playlistName = parsed.playlistName,
                    matchedSongs = parsed.matchedSongs,
                    totalCount = parsed.totalCount
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
