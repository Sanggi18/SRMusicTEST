package com.example.core.data.playlist

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.core.model.Song
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream

object PlaylistFileManager {

    enum class PlaylistFormat(val extension: String, val mimeType: String, val displayName: String) {
        M3U("m3u", "audio/x-mpegurl", "M3U (.m3u)"),
        M3U8("m3u8", "application/vnd.apple.mpegurl", "M3U8 (.m3u8)"),
        JSON("json", "application/json", "JSON (.json)")
    }

    data class ImportResult(
        val playlistName: String,
        val matchedSongs: List<Song>,
        val totalCount: Int
    )

    fun exportPlaylistContent(playlistName: String, songs: List<Song>, format: PlaylistFormat): ByteArray {
        return when (format) {
            PlaylistFormat.M3U, PlaylistFormat.M3U8 -> {
                val sb = StringBuilder()
                sb.append("#EXTM3U\n")
                for (song in songs) {
                    val durationSec = if (song.duration > 0) song.duration / 1000 else -1
                    sb.append("#EXTINF:$durationSec,${song.artist} - ${song.title}\n")
                    if (song.dataPath.isNotBlank()) {
                        sb.append("${song.dataPath}\n")
                    } else {
                        sb.append("${song.contentUri}\n")
                    }
                }
                sb.toString().toByteArray(Charsets.UTF_8)
            }
            PlaylistFormat.JSON -> {
                val root = JSONObject()
                root.put("name", playlistName)
                root.put("version", 1)
                root.put("exportedAt", System.currentTimeMillis())
                root.put("songCount", songs.size)

                val array = JSONArray()
                for (song in songs) {
                    val obj = JSONObject()
                    obj.put("title", song.title)
                    obj.put("artist", song.artist)
                    obj.put("album", song.album)
                    obj.put("duration", song.duration)
                    obj.put("dataPath", song.dataPath)
                    obj.put("contentUri", song.contentUri.toString())
                    array.put(obj)
                }
                root.put("songs", array)
                root.toString(2).toByteArray(Charsets.UTF_8)
            }
        }
    }

    fun parsePlaylistFile(
        context: Context,
        uri: Uri,
        allSongs: List<Song>
    ): ImportResult? {
        val fileName = getFileName(context, uri) ?: "Imported Playlist"
        val fallbackName = fileName.substringBeforeLast('.')
        val ext = fileName.substringAfterLast('.', "").lowercase()

        val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
        val rawContent = inputStream.use { it.bufferedReader(Charsets.UTF_8).readText() }

        if (rawContent.isBlank()) return null

        return if (ext == "json" || rawContent.trimStart().startsWith("{")) {
            parseJson(rawContent, fallbackName, allSongs)
        } else {
            parseM3u(rawContent, fallbackName, allSongs)
        }
    }

    private fun parseM3u(
        content: String,
        fallbackName: String,
        allSongs: List<Song>
    ): ImportResult {
        val lines = content.lines()
        val matchedSongs = mutableListOf<Song>()
        var pendingArtist: String? = null
        var pendingTitle: String? = null
        var totalEntries = 0

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            if (trimmed.startsWith("#EXTINF:", ignoreCase = true)) {
                // e.g. #EXTINF:180,Artist Name - Song Title
                val info = trimmed.substringAfter("#EXTINF:")
                val commaIndex = info.indexOf(',')
                val metadata = if (commaIndex != -1) info.substring(commaIndex + 1).trim() else info.trim()
                if (metadata.contains(" - ")) {
                    pendingArtist = metadata.substringBefore(" - ").trim()
                    pendingTitle = metadata.substringAfter(" - ").trim()
                } else {
                    pendingTitle = metadata
                    pendingArtist = null
                }
            } else if (!trimmed.startsWith("#")) {
                // This is a file path, filename, or URI
                totalEntries++
                val match = findMatchingSong(
                    pathOrUri = trimmed,
                    artist = pendingArtist,
                    title = pendingTitle,
                    allSongs = allSongs
                )
                if (match != null && !matchedSongs.any { it.id == match.id }) {
                    matchedSongs.add(match)
                }
                pendingArtist = null
                pendingTitle = null
            }
        }

        return ImportResult(
            playlistName = fallbackName,
            matchedSongs = matchedSongs,
            totalCount = totalEntries
        )
    }

    private fun parseJson(
        content: String,
        fallbackName: String,
        allSongs: List<Song>
    ): ImportResult? {
        return try {
            val root = JSONObject(content)
            val playlistName = root.optString("name").ifBlank { fallbackName }
            val songsArray = root.optJSONArray("songs") ?: JSONArray()
            val matchedSongs = mutableListOf<Song>()
            val totalCount = songsArray.length()

            for (i in 0 until songsArray.length()) {
                val item = songsArray.optJSONObject(i) ?: continue
                val title = item.optString("title").takeIf { it.isNotBlank() }
                val artist = item.optString("artist").takeIf { it.isNotBlank() }
                val path = item.optString("dataPath").takeIf { it.isNotBlank() }
                val uriStr = item.optString("contentUri").takeIf { it.isNotBlank() }

                val match = findMatchingSong(
                    pathOrUri = path ?: uriStr ?: "",
                    artist = artist,
                    title = title,
                    allSongs = allSongs
                )
                if (match != null && !matchedSongs.any { it.id == match.id }) {
                    matchedSongs.add(match)
                }
            }

            ImportResult(
                playlistName = playlistName,
                matchedSongs = matchedSongs,
                totalCount = totalCount
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun findMatchingSong(
        pathOrUri: String,
        artist: String?,
        title: String?,
        allSongs: List<Song>
    ): Song? {
        if (pathOrUri.isNotBlank()) {
            // 1. Match by exact dataPath
            val exactPath = allSongs.firstOrNull { it.dataPath.equals(pathOrUri, ignoreCase = true) }
            if (exactPath != null) return exactPath

            // 2. Match by exact contentUri
            val exactUri = allSongs.firstOrNull { it.contentUri.toString().equals(pathOrUri, ignoreCase = true) }
            if (exactUri != null) return exactUri

            // 3. Match by fileName
            val fileName = try {
                File(pathOrUri).name
            } catch (_: Exception) {
                ""
            }
            if (fileName.isNotBlank()) {
                val fileMatch = allSongs.firstOrNull {
                    try {
                        File(it.dataPath).name.equals(fileName, ignoreCase = true)
                    } catch (_: Exception) {
                        false
                    }
                }
                if (fileMatch != null) return fileMatch
            }
        }

        // 4. Match by title and artist
        if (!title.isNullOrBlank() && !artist.isNullOrBlank()) {
            val titleAndArtist = allSongs.firstOrNull {
                it.title.equals(title, ignoreCase = true) &&
                        it.artist.equals(artist, ignoreCase = true)
            }
            if (titleAndArtist != null) return titleAndArtist
        }

        // 5. Match by title alone
        if (!title.isNullOrBlank()) {
            val titleMatch = allSongs.firstOrNull {
                it.title.equals(title, ignoreCase = true)
            }
            if (titleMatch != null) return titleMatch
        }

        return null
    }

    fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            return cursor.getString(index)
                        }
                    }
                }
            } catch (_: Exception) {}
        }
        return uri.path?.let { File(it).name }
    }
}

typealias PlaylistExporterImporter = PlaylistFileManager
