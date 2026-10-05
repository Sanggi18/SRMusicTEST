package com.example.core.data.metadata

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import com.example.core.model.Song
import com.example.core.util.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.util.concurrent.ConcurrentHashMap

data class AudioTechnicalInfo(
    val format: String,
    val bitDepth: String,
    val sampleRate: String,
    val bitrate: String,
    val channels: String,
    val fileSizeFormatted: String = ""
)

object MetadataExtractor {

    private val cache = ConcurrentHashMap<Long, AudioTechnicalInfo>()
    private val genreCache = ConcurrentHashMap<Long, String>()
    private val releaseDateCache = ConcurrentHashMap<Long, String>()

    fun extractReleaseDateTag(context: Context, song: Song): String {
        releaseDateCache[song.id]?.let { return it }

        // 1. Try Jaudiotagger on physical file for exact MediaInfo RECORD_DATE / ORIGINAL_RELEASE_DATE
        if (song.dataPath.isNotBlank()) {
            val file = File(song.dataPath)
            if (file.exists() && file.canRead()) {
                try {
                    val audioFile = AudioFileIO.read(file)
                    val tag = audioFile.tag
                    if (tag != null) {
                        // Check detailed full release dates first (e.g. 2024-05-18, 18/05/2024)
                        for (rawKey in arrayOf("TDRC", "TDRL", "TDOR", "TDAT", "DATE", "ORIGINALDATE", "ORIGINALYEAR", "TYER", "\u00a9day")) {
                            val rawVal = try { tag.getFirst(rawKey) } catch (_: Exception) { "" }
                            if (!rawVal.isNullOrBlank()) {
                                val formatted = TimeUtils.formatReleaseDateTag(rawVal, shortMonth = true)
                                if (!formatted.isNullOrBlank()) {
                                    releaseDateCache[song.id] = formatted
                                    return formatted
                                }
                            }
                        }

                        val yearTag = tag.getFirst(FieldKey.YEAR)
                        if (!yearTag.isNullOrBlank()) {
                            val formatted = TimeUtils.formatReleaseDateTag(yearTag, shortMonth = true)
                            if (!formatted.isNullOrBlank()) {
                                releaseDateCache[song.id] = formatted
                                return formatted
                            }
                        }
                    }
                } catch (_: Exception) {}
            }
        }

        // 2. Fallback to MediaMetadataRetriever
        val retriever = MediaMetadataRetriever()
        try {
            if (song.dataPath.isNotBlank() && File(song.dataPath).exists()) {
                retriever.setDataSource(song.dataPath)
            } else {
                retriever.setDataSource(context, song.contentUri)
            }
            val date = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
            if (!date.isNullOrBlank()) {
                val formatted = TimeUtils.formatReleaseDateTag(date, shortMonth = true)
                if (!formatted.isNullOrBlank()) {
                    releaseDateCache[song.id] = formatted
                    return formatted
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val yearMeta = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                if (!yearMeta.isNullOrBlank()) {
                    val formatted = TimeUtils.formatReleaseDateTag(yearMeta, shortMonth = true)
                    if (!formatted.isNullOrBlank()) {
                        releaseDateCache[song.id] = formatted
                        return formatted
                    }
                }
            }
        } catch (_: Exception) {
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }

        // 3. Fallback to MediaStore song.year
        if (song.year in 1900..2100) {
            val yStr = song.year.toString()
            releaseDateCache[song.id] = yStr
            return yStr
        }

        // 4. Fallback to timestamp if available
        val fallback = TimeUtils.formatTimestampFallback(song)
        if (fallback.isNotBlank()) {
            releaseDateCache[song.id] = fallback
            return fallback
        }

        return ""
    }

    private val id3Genres = arrayOf(
        "Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge",
        "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B",
        "Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska",
        "Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient",
        "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical",
        "Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel",
        "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative",
        "Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic", "Darkwave",
        "Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance", "Dream",
        "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40", "Christian Rap",
        "Pop/Funk", "Jungle", "Native American", "Cabaret", "New Wave",
        "Psychadelic", "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal",
        "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll",
        "Hard Rock", "Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion",
        "Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde",
        "Gothic Rock", "Progressive Rock", "Psychedelic Rock", "Symphonic Rock",
        "Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour",
        "Speech", "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony",
        "Booty Bass", "Primus", "Porn Groove", "Satire", "Slow Jam", "Club",
        "Tango", "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul",
        "Freestyle", "Duet", "Punk Rock", "Drum Solo", "A capella", "Euro-House",
        "Dance Hall", "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror",
        "Indie", "BritPop", "Negerpunk", "Polsk Punk", "Beat", "Christian Gangsta",
        "Heavy Metal", "Black Metal", "Crossover", "Contemporary Christian",
        "Christian Rock", "Merengue", "Salsa", "Thrash Metal", "Anime", "JPop", "Synthpop"
    )

    fun isValidGenre(genre: String?): Boolean {
        if (genre.isNullOrBlank()) return false
        val g = genre.trim()
        val lower = g.lowercase()
        if (lower == "<unknown>" || lower == "unknown" || lower == "unknown genre" ||
            lower == "null" || lower == "other" || lower == "undefined" || lower == "genre" ||
            lower == "none" || lower == "track" || lower == "audio"
        ) {
            return false
        }
        return g.length in 2..50
    }

    fun cleanGenre(genre: String): String {
        var g = genre.trim()

        // Handle numeric ID3 tags, e.g. "(17)" or "(17)Rock" or "17"
        if (g.startsWith("(") && g.contains(")")) {
            val inside = g.substringAfter("(").substringBefore(")")
            val after = g.substringAfter(")").trim()
            if (after.isNotEmpty() && isValidGenre(after)) {
                g = after
            } else {
                val num = inside.toIntOrNull()
                if (num != null && num in id3Genres.indices) {
                    g = id3Genres[num]
                }
            }
        } else if (g.toIntOrNull() != null) {
            val num = g.toInt()
            if (num in id3Genres.indices) {
                g = id3Genres[num]
            }
        }

        // Clean trailing null bytes, semicolons, quotes, slashes
        g = g.trim().trim('\"', '\'', ';', '/', '\\', '\u0000').trim()

        // Normalize capitalization for ALL-CAPS or all-lowercase genres (e.g. "POP" -> "Pop", "J-POP" -> "J-Pop")
        if (g.isNotEmpty()) {
            if (g.equals("jpop", ignoreCase = true) || g.equals("j-pop", ignoreCase = true)) {
                g = "J-Pop"
            } else if (g.equals("kpop", ignoreCase = true) || g.equals("k-pop", ignoreCase = true)) {
                g = "K-Pop"
            } else if (g.equals("r&b", ignoreCase = true) || g.equals("rnb", ignoreCase = true)) {
                g = "R&B"
            } else if (g.equals("ost", ignoreCase = true)) {
                g = "OST"
            } else if (g.equals("edm", ignoreCase = true)) {
                g = "EDM"
            } else if (g.all { it.isUpperCase() || !it.isLetter() } && g.length > 3) {
                g = g.lowercase().split(" ", "-").joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            } else if (g.all { it.isLowerCase() || !it.isLetter() }) {
                g = g.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }

        return g
    }

    /**
     * Extracts genre tag using MediaMetadataRetriever with fallback to Jaudiotagger.
     */
    fun extractGenre(
        context: Context,
        contentUri: Uri?,
        dataPath: String?,
        preloadedGenre: String? = null
    ): String {
        if (isValidGenre(preloadedGenre)) {
            return cleanGenre(preloadedGenre!!)
        }

        // 1. Try MediaMetadataRetriever
        val retriever = MediaMetadataRetriever()
        try {
            if (!dataPath.isNullOrBlank() && File(dataPath).exists()) {
                retriever.setDataSource(dataPath)
                val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                if (isValidGenre(genre)) {
                    return cleanGenre(genre!!)
                }
            } else if (contentUri != null) {
                retriever.setDataSource(context, contentUri)
                val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                if (isValidGenre(genre)) {
                    return cleanGenre(genre!!)
                }
            }
        } catch (_: Exception) {
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }

        // 2. Fallback to Jaudiotagger
        if (!dataPath.isNullOrBlank()) {
            val file = File(dataPath)
            if (file.exists() && file.canRead()) {
                try {
                    val audioFile = AudioFileIO.read(file)
                    val tag = audioFile.tag
                    val jGenre = tag?.getFirst(FieldKey.GENRE)
                    if (isValidGenre(jGenre)) {
                        return cleanGenre(jGenre!!)
                    }
                } catch (_: Exception) {}
            }
        }

        return ""
    }

    suspend fun extractTechnicalInfo(context: Context, song: Song): AudioTechnicalInfo = withContext(Dispatchers.IO) {
        cache[song.id]?.let { return@withContext it }

        val extension = if (song.displayName.contains('.')) {
            song.displayName.substringAfterLast('.').uppercase()
        } else if (song.dataPath.contains('.')) {
            song.dataPath.substringAfterLast('.').uppercase()
        } else {
            "MP3"
        }

        val formattedSize = if (song.size > 0) {
            val mb = song.size / (1024.0 * 1024.0)
            String.format("%.1f MB", mb)
        } else ""

        var format = extension
        var bitDepth = ""
        var sampleRate = ""
        var bitrate = ""
        var channels = ""

        try {
            val file = if (song.dataPath.isNotBlank()) File(song.dataPath) else null
            if (file != null && file.exists() && file.canRead()) {
                val audioFile = AudioFileIO.read(file)
                val header = audioFile.audioHeader
                if (header != null) {
                    val rawFormat = header.format
                    if (!rawFormat.isNullOrBlank()) {
                        format = rawFormat.uppercase()
                    }

                    val bits = try { header.bitsPerSample } catch (_: Exception) { -1 }
                    if (bits > 0) {
                        bitDepth = "${bits}-bit"
                    }

                    val sr = try { header.sampleRateAsNumber } catch (_: Exception) { -1 }
                    if (sr > 0) {
                        sampleRate = if (sr % 1000 == 0) {
                            "${sr / 1000} kHz"
                        } else {
                            String.format("%.1f kHz", sr / 1000.0)
                        }
                    }

                    val br = try { header.bitRateAsNumber } catch (_: Exception) { -1L }
                    if (br > 0) {
                        bitrate = "$br kbps"
                    }

                    val ch = try { header.channels } catch (_: Exception) { "" }
                    if (!ch.isNullOrBlank()) {
                        channels = if (ch.contains("2") || ch.equals("stereo", ignoreCase = true)) "Stereo"
                        else if (ch.contains("1") || ch.equals("mono", ignoreCase = true)) "Mono"
                        else ch
                    }
                }
            }
        } catch (_: Exception) {
            // Safe fallback if jaudiotagger encounters format quirks
        }

        if (bitDepth.isBlank()) {
            bitDepth = if (format == "FLAC" || format == "WAV" || format == "AIFF") "24-bit" else "16-bit"
        }
        if (sampleRate.isBlank()) {
            sampleRate = "44.1 kHz"
        }
        if (bitrate.isBlank()) {
            if (song.duration > 0 && song.size > 0) {
                val calculatedKbps = ((song.size * 8) / (song.duration)).toInt()
                bitrate = "$calculatedKbps kbps"
            } else {
                bitrate = "320 kbps"
            }
        }
        if (channels.isBlank()) {
            channels = "Stereo"
        }

        if (format.contains("AAC", ignoreCase = true) || format.contains("MP4", ignoreCase = true) || format.contains("M4A", ignoreCase = true)) {
            format = "M4A"
        }

        val result = AudioTechnicalInfo(
            format = format,
            bitDepth = bitDepth,
            sampleRate = sampleRate,
            bitrate = bitrate,
            channels = channels,
            fileSizeFormatted = formattedSize
        )

        cache[song.id] = result
        result
    }
}
