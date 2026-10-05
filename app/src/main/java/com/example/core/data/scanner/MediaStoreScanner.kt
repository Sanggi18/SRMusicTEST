package com.example.core.data.scanner

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.core.model.LibraryScanOptions
import com.example.core.data.metadata.MetadataExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreScanner(private val context: Context) {

    private fun queryMediaStoreGenres(): Map<Long, String> {
        val genreMap = mutableMapOf<Long, String>()
        try {
            val genresUri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
            val genresProjection = arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME)
            context.contentResolver.query(genresUri, genresProjection, null, null, null)?.use { genreCursor ->
                val gIdCol = genreCursor.getColumnIndex(MediaStore.Audio.Genres._ID)
                val gNameCol = genreCursor.getColumnIndex(MediaStore.Audio.Genres.NAME)
                if (gIdCol != -1 && gNameCol != -1) {
                    while (genreCursor.moveToNext()) {
                        val genreId = genreCursor.getLong(gIdCol)
                        val genreName = genreCursor.getString(gNameCol)
                        if (MetadataExtractor.isValidGenre(genreName)) {
                            val cleaned = MetadataExtractor.cleanGenre(genreName)
                            try {
                                val membersUri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId)
                                val membersProjection = arrayOf(MediaStore.Audio.Genres.Members._ID)
                                context.contentResolver.query(membersUri, membersProjection, null, null, null)?.use { mCursor ->
                                    val mIdCol = mCursor.getColumnIndex(MediaStore.Audio.Genres.Members._ID)
                                    if (mIdCol != -1) {
                                        while (mCursor.moveToNext()) {
                                            val songId = mCursor.getLong(mIdCol)
                                            if (!genreMap.containsKey(songId)) {
                                                genreMap[songId] = cleaned
                                            }
                                        }
                                    }
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return genreMap
    }

    fun isPathBlacklisted(dataPath: String, relativePath: String, blacklist: Set<String>): Boolean {
        val normData = dataPath.replace('\\', '/').lowercase()
        val normRel = relativePath.replace('\\', '/').lowercase()

        // Explicitly reject system & app private folders across all devices (Android 10 - 17)
        if (normData.contains("/android/data/") || normData.contains("/android/data") ||
            normRel.contains("android/data/") || normRel.contains("android/data") ||
            normData.contains("/android/obb/") || normData.contains("/android/obb") ||
            normRel.contains("android/obb/") || normRel.contains("android/obb") ||
            normData.contains("/.thumbnails/") || normRel.contains(".thumbnails/")) {
            return true
        }

        for (entry in blacklist) {
            val clean = entry.trim().replace('\\', '/').trim('/').lowercase()
            if (clean.isEmpty()) continue
            if (normData.contains("/$clean/") || normData.endsWith("/$clean") ||
                normData.contains("/$clean") || normRel.contains("$clean/") ||
                normRel.startsWith("$clean/") || normRel.startsWith(clean) ||
                normRel.contains("/$clean")) {
                return true
            }
        }
        return false
    }

    fun isPathIncluded(dataPath: String, relativePath: String, includedFolders: Set<String>): Boolean {
        if (includedFolders.isEmpty()) return true

        val normData = dataPath.replace('\\', '/').lowercase()
        val normRel = relativePath.replace('\\', '/').trim('/').lowercase()

        for (folder in includedFolders) {
            val raw = folder.trim().replace('\\', '/')
            if (raw.isBlank()) continue

            val normFolder = raw.lowercase().trimEnd('/')
            if (normFolder.isEmpty()) continue

            // Direct path match
            if (normData.startsWith("$normFolder/") || normData == normFolder) {
                return true
            }

            // Handle alias /sdcard/ <=> /storage/emulated/0/
            if (normFolder.contains("/sdcard/")) {
                val emu = normFolder.replace("/sdcard/", "/storage/emulated/0/")
                if (normData.startsWith("$emu/") || normData == emu) return true
            }
            if (normFolder.contains("/storage/emulated/0/")) {
                val sdc = normFolder.replace("/storage/emulated/0/", "/sdcard/")
                if (normData.startsWith("$sdc/") || normData == sdc) return true
            }

            // Relative path match (e.g., "Music", "Download", "Audio")
            val cleanRel = raw
                .replace("/storage/emulated/0/", "")
                .replace("/sdcard/", "")
                .trim('/')
                .lowercase()
            if (cleanRel.isNotEmpty()) {
                if (normRel.startsWith(cleanRel) || normRel == cleanRel ||
                    normRel.contains("$cleanRel/") || normData.contains("/$cleanRel/")) {
                    return true
                }
            }
        }
        return false
    }

    suspend fun scanMediaStore(
        options: LibraryScanOptions,
        onProgress: ((count: Int) -> Unit)? = null
    ): List<RawMediaItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<RawMediaItem>()
        val mediaStoreGenres = queryMediaStoreGenres()
        val collectionUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projectionList = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projectionList.add(MediaStore.Audio.Media.GENRE)
        }
        val projection = projectionList.toTypedArray()

        val minDurationMs = if (options.filterShortAudio) (options.minDurationSeconds * 1000L).coerceAtLeast(1000L) else 1000L
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(minDurationMs.toString())
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        val albumArtBaseUri = Uri.parse("content://media/external/audio/albumart")

        try {
            context.contentResolver.query(
                collectionUri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val dateAddedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                val dateModifiedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
                val genreColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) cursor.getColumnIndex(MediaStore.Audio.Media.GENRE) else -1

                while (cursor.moveToNext()) {
                    val duration = cursor.getLong(durationColumn)
                    if (options.filterShortAudio && duration < minDurationMs) {
                        continue
                    }

                    val displayName = cursor.getString(displayNameColumn) ?: ""
                    if (displayName.startsWith(".")) {
                        continue
                    }

                    val dataPath = if (dataColumn != -1 && !cursor.isNull(dataColumn)) cursor.getString(dataColumn) ?: "" else ""

                    val relativePath = if (dataPath.isNotBlank()) {
                        val directoryPath = dataPath.substringBeforeLast('/', "")
                        if (directoryPath.contains("/Music/")) {
                            "Music/" + directoryPath.substringAfter("/Music/")
                        } else if (directoryPath.contains("/sdcard/")) {
                            directoryPath.substringAfter("/sdcard/")
                        } else if (directoryPath.contains("/storage/emulated/0/")) {
                            directoryPath.substringAfter("/storage/emulated/0/")
                        } else {
                            directoryPath.trimStart('/')
                        }
                    } else ""

                    if (isPathBlacklisted(dataPath, relativePath, options.blacklistedFolders)) {
                        continue
                    }

                    if (!isPathIncluded(dataPath, relativePath, options.includedFolders)) {
                        continue
                    }

                    val id = cursor.getLong(idColumn)
                    val rawTitle = cursor.getString(titleColumn)
                    val rawArtist = cursor.getString(artistColumn)
                    val rawAlbum = cursor.getString(albumColumn)
                    val albumId = cursor.getLong(albumIdColumn)
                    val track = if (!cursor.isNull(trackColumn)) cursor.getInt(trackColumn) else 0
                    val year = if (!cursor.isNull(yearColumn)) cursor.getInt(yearColumn) else 0
                    val size = if (!cursor.isNull(sizeColumn)) cursor.getLong(sizeColumn) else 0L
                    val dateAdded = if (dateAddedColumn != -1 && !cursor.isNull(dateAddedColumn)) cursor.getLong(dateAddedColumn) else 0L
                    val dateModified = if (dateModifiedColumn != -1 && !cursor.isNull(dateModifiedColumn)) cursor.getLong(dateModifiedColumn) else 0L

                    val rawGenre = if (genreColumn != -1 && !cursor.isNull(genreColumn)) {
                        cursor.getString(genreColumn)
                    } else {
                        mediaStoreGenres[id]
                    }

                    val title = if (!rawTitle.isNullOrBlank()) rawTitle else displayName.substringBeforeLast(".")
                    val artist = if (!rawArtist.isNullOrBlank() && rawArtist != "<unknown>") rawArtist else "Unknown Artist"
                    val album = if (!rawAlbum.isNullOrBlank() && rawAlbum != "<unknown>") rawAlbum else "Unknown Album"

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val artworkUri = ContentUris.withAppendedId(albumArtBaseUri, albumId)

                    val genre = if (MetadataExtractor.isValidGenre(rawGenre)) {
                        MetadataExtractor.cleanGenre(rawGenre!!)
                    } else {
                        MetadataExtractor.extractGenre(context, contentUri, dataPath, rawGenre)
                    }

                    items.add(
                        RawMediaItem(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            albumId = albumId,
                            contentUriString = contentUri.toString(),
                            artworkUriString = artworkUri.toString(),
                            trackNumber = track,
                            year = year,
                            size = size,
                            displayName = displayName,
                            relativePath = relativePath,
                            dataPath = dataPath,
                            dateAdded = dateAdded,
                            dateModified = dateModified,
                            genre = genre
                        )
                    )

                    if (onProgress != null && (items.size % 20 == 0 || items.size <= 20)) {
                        onProgress(items.size)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        items
    }
}
