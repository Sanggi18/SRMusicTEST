package com.example.core.data.repository

import android.content.Context
import com.example.core.model.LibraryScanOptions
import com.example.core.data.preferences.LibraryPreferences
import com.example.core.data.database.AppDatabase
import com.example.core.data.database.SongDao
import com.example.core.data.scanner.MediaStoreScanner
import com.example.core.data.sync.LibrarySyncEngine
import com.example.core.data.sync.SyncResult
import com.example.core.model.Song

interface LibraryRepository {
    val libraryPreferences: LibraryPreferences
    fun getScanOptions(): LibraryScanOptions
    fun updateScanOptions(options: LibraryScanOptions)
    suspend fun syncLibrary(
        options: LibraryScanOptions = getScanOptions(),
        onProgress: ((count: Int) -> Unit)? = null
    ): SyncResult
    fun formatTotalLibrarySize(songs: List<Song>): String
}

class LibraryRepositoryImpl(
    private val context: Context,
    private val songDao: SongDao = AppDatabase.getInstance(context).songDao(),
    private val scanner: MediaStoreScanner = MediaStoreScanner(context),
    override val libraryPreferences: LibraryPreferences = LibraryPreferences(context)
) : LibraryRepository {

    private val syncEngine = LibrarySyncEngine(songDao, scanner)

    override fun getScanOptions(): LibraryScanOptions = libraryPreferences.getScanOptions()

    override fun updateScanOptions(options: LibraryScanOptions) {
        libraryPreferences.updateScanOptions(options)
    }

    override suspend fun syncLibrary(
        options: LibraryScanOptions,
        onProgress: ((count: Int) -> Unit)?
    ): SyncResult {
        return syncEngine.reconcile(options, onProgress)
    }

    override fun formatTotalLibrarySize(songs: List<Song>): String {
        val totalBytes = songs.sumOf { it.size }
        val mb = totalBytes / (1024.0 * 1024.0)
        return if (mb >= 1024.0) {
            val gb = mb / 1024.0
            String.format("%.2f GB", gb)
        } else {
            String.format("%.1f MB", mb)
        }
    }
}
