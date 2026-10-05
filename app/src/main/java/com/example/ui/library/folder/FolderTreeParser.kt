package com.example.ui.library.folder

import com.example.core.model.Song

object FolderTreeParser {

    fun getSongRelativeDirectory(song: Song): String {
        val path = when {
            song.relativePath.isNotBlank() -> song.relativePath.trim('/')
            song.dataPath.isNotBlank() -> {
                val dir = song.dataPath.substringBeforeLast('/', "")
                when {
                    dir.contains("/Music/") -> dir.substringAfter("/Music/").trim('/')
                    dir.contains("/Music") -> dir.substringAfter("/Music").trim('/')
                    dir.contains("/sdcard/") -> dir.substringAfter("/sdcard/").trim('/')
                    dir.contains("/storage/emulated/0/") -> dir.substringAfter("/storage/emulated/0/").trim('/')
                    else -> dir.trim('/')
                }
            }
            else -> ""
        }
        return path.removePrefix("Music/").removePrefix("Music").trim('/')
    }

    fun parseFolderContent(
        allSongs: List<Song>,
        effectiveRelativePath: String,
        searchQuery: String
    ): Pair<List<FolderUiModel>, List<Song>> {
        val normalizedCurrentPath = effectiveRelativePath.trim('/')

        val songsUnderCurrent = allSongs.filter { song ->
            val songDir = getSongRelativeDirectory(song)
            if (normalizedCurrentPath.isEmpty()) {
                true
            } else {
                songDir == normalizedCurrentPath || songDir.startsWith("$normalizedCurrentPath/")
            }
        }

        // Direct songs in this folder
        val directSongs = songsUnderCurrent.filter { song ->
            val songDir = getSongRelativeDirectory(song)
            songDir == normalizedCurrentPath
        }.let { list ->
            if (searchQuery.isBlank()) list else list.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true) ||
                it.displayName.contains(searchQuery, ignoreCase = true)
            }
        }

        // Discover immediate child subfolders
        val subfolderMap = mutableMapOf<String, MutableList<Song>>()
        songsUnderCurrent.forEach { song ->
            val songDir = getSongRelativeDirectory(song)
            if (songDir != normalizedCurrentPath && songDir.isNotEmpty()) {
                val remainingPath = if (normalizedCurrentPath.isEmpty()) {
                    songDir
                } else {
                    songDir.removePrefix("$normalizedCurrentPath/").trim('/')
                }
                val immediateChild = remainingPath.substringBefore('/')
                if (immediateChild.isNotEmpty()) {
                    subfolderMap.getOrPut(immediateChild) { mutableListOf() }.add(song)
                }
            }
        }

        val subfolderModels = subfolderMap.map { (folderName, songsInSubfolder) ->
            val subfolderRelPath = if (normalizedCurrentPath.isEmpty()) folderName else "$normalizedCurrentPath/$folderName"

            // Count immediate nested sub-subfolders
            val nestedSubCount = songsInSubfolder.map { getSongRelativeDirectory(it) }
                .filter { it.startsWith("$subfolderRelPath/") }
                .map { it.removePrefix("$subfolderRelPath/").substringBefore('/') }
                .distinct()
                .size

            val repSong = songsInSubfolder.firstOrNull { it.artworkUri != null || it.dataPath.isNotBlank() } ?: songsInSubfolder.firstOrNull()

            FolderUiModel(
                path = subfolderRelPath,
                name = folderName,
                songCount = songsInSubfolder.size,
                subfolderCount = nestedSubCount,
                thumbnailArtworkUri = repSong?.artworkUri,
                thumbnailDataPath = repSong?.dataPath.orEmpty(),
                thumbnailAlbumId = repSong?.albumId ?: -1L,
                thumbnailDateModified = repSong?.dateModified ?: 0L
            )
        }.sortedBy { it.name.lowercase() }.let { list ->
            if (searchQuery.isBlank()) list else list.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }

        return Pair(subfolderModels, directSongs)
    }

    fun getSongsInFolder(allSongs: List<Song>, folderPath: String): List<Song> {
        val targetRelPath = folderPath.trim('/')
        return allSongs.filter { s ->
            val dir = getSongRelativeDirectory(s)
            dir == targetRelPath || dir.startsWith("$targetRelPath/")
        }
    }
}

typealias FolderParser = FolderTreeParser
