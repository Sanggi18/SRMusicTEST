package com.example.lyrics

import com.example.core.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class LyricsRepository {

    companion object {
        private const val MAX_CACHE_ENTRIES = 20
    }

    private data class CacheKey(
        val songId: Long,
        val audioSize: Long,
        val audioModified: Long,
        val sidecarModified: Long
    )

    private val embeddedReader = EmbeddedLyricsReader()
    private val sidecarReader = SidecarLyricsReader()
    private val mutex = Mutex()

    private val cache = object : LinkedHashMap<CacheKey, LyricsDocument>(
        MAX_CACHE_ENTRIES + 1,
        0.75f,
        true
    ) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<CacheKey, LyricsDocument>
        ): Boolean {
            return size > MAX_CACHE_ENTRIES
        }
    }

    suspend fun load(song: Song): LyricsDocument = withContext(Dispatchers.IO) {
        val key = buildCacheKey(song)

        mutex.withLock {
            cache[key]?.let { return@withLock it }
        }

        // Never hold the cache mutex while doing audio/tag/file I/O.
        val result = loadUncached(song)

        mutex.withLock {
            // Another caller may have populated the same key while we were reading.
            val existing = cache[key]
            if (existing != null) return@withLock existing
            cache[key] = result
            result
        }
    }

    private fun loadUncached(song: Song): LyricsDocument {
        val dataPath = song.dataPath
        if (dataPath.isBlank()) {
            return LyricsDocument(
                songId = song.id,
                source = null,
                content = LyricsContent.None
            )
        }

        val file = File(dataPath)
        if (!file.isFile || !file.canRead()) {
            return LyricsDocument(
                songId = song.id,
                source = null,
                content = LyricsContent.None
            )
        }

        // SRMusic product rule: embedded lyrics are authoritative.
        embeddedReader.read(song.id, dataPath)?.let { return it }

        sidecarReader.read(song.id, dataPath)?.let { return it }

        return LyricsDocument(
            songId = song.id,
            source = null,
            content = LyricsContent.None
        )
    }

    private fun buildCacheKey(song: Song): CacheKey {
        val audio = File(song.dataPath)
        val sidecar = if (audio.isFile) sidecarReader.findSidecar(audio) else null

        return CacheKey(
            songId = song.id,
            audioSize = if (audio.isFile) audio.length() else song.size,
            audioModified = if (audio.isFile) audio.lastModified() else song.dateModified,
            sidecarModified = sidecar?.lastModified() ?: 0L
        )
    }
}
