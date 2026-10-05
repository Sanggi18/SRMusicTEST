package com.example.core.data.sync

import com.example.core.model.LibraryScanOptions
import com.example.core.data.database.SongDao
import com.example.core.data.database.SongEntity
import com.example.core.data.scanner.MediaStoreScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SyncResult(
    val newCount: Int,
    val updatedCount: Int,
    val deletedCount: Int,
    val totalCount: Int
)

class LibrarySyncEngine(
    private val songDao: SongDao,
    private val scanner: MediaStoreScanner
) {

    suspend fun reconcile(
        options: LibraryScanOptions,
        onProgress: ((count: Int) -> Unit)? = null
    ): SyncResult = withContext(Dispatchers.IO) {
        // 1. Snapshot existing entities in Room
        val existingEntities = songDao.getAllSongEntities()
        val roomMap = existingEntities.associateBy { it.id }

        // 2. Discover raw items from MediaStore
        val scannedItems = scanner.scanMediaStore(options, onProgress)

        val toInsertOrUpdate = mutableListOf<SongEntity>()
        val scannedIds = HashSet<Long>(scannedItems.size)
        var newCount = 0
        var updatedCount = 0

        for (item in scannedItems) {
            scannedIds.add(item.id)
            val existing = roomMap[item.id]
            if (existing == null) {
                newCount++
                toInsertOrUpdate.add(item.toSongEntity())
            } else if (existing.dateModified != item.dateModified ||
                existing.size != item.size ||
                existing.title != item.title ||
                existing.artist != item.artist ||
                existing.album != item.album ||
                existing.dataPath != item.dataPath ||
                existing.duration != item.duration
            ) {
                updatedCount++
                toInsertOrUpdate.add(item.toSongEntity())
            }
        }

        val toDeleteIds = existingEntities.mapNotNull {
            if (it.id !in scannedIds) it.id else null
        }

        // 3. Batch apply changes in Room
        if (toDeleteIds.isNotEmpty()) {
            // Delete in chunks of 500 to stay well below SQLite parameter limits
            toDeleteIds.chunked(500).forEach { chunk ->
                songDao.deleteSongsByIds(chunk)
            }
        }

        if (toInsertOrUpdate.isNotEmpty()) {
            toInsertOrUpdate.chunked(500).forEach { chunk ->
                songDao.upsertSongs(chunk)
            }
        }

        val finalTotal = scannedItems.size
        SyncResult(
            newCount = newCount,
            updatedCount = updatedCount,
            deletedCount = toDeleteIds.size,
            totalCount = finalTotal
        )
    }
}
