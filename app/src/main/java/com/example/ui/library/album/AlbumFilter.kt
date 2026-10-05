package com.example.ui.library.album
import com.example.ui.library.core.*
import com.example.ui.library.components.*
import com.example.ui.common.components.*

import com.example.core.model.Song
import com.example.ui.library.components.SortCriteria
import com.example.core.util.SortHelper

object AlbumFilter {
    fun buildAlbumsList(
        songs: List<Song>,
        searchQuery: String,
        sortCriteria: SortCriteria,
        isAscending: Boolean
    ): List<AlbumUiModel> {
        val grouped = songs.groupBy { it.album.trim() }
            .filterKeys { it.isNotBlank() && it != "<unknown>" && !it.equals("unknown album", ignoreCase = true) }
            .map { (albumName, albumSongs) ->
                val repSong = albumSongs.firstOrNull { it.dataPath.isNotBlank() } ?: albumSongs.first()
                val validArtists = albumSongs.map { it.artist.trim() }
                    .filter { it.isNotBlank() && it != "<unknown>" && !it.equals("unknown artist", ignoreCase = true) }
                val resolvedArtist = if (validArtists.isNotEmpty()) {
                    validArtists.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: validArtists.first()
                } else {
                    repSong.artist.trim().takeIf { it.isNotBlank() && it != "<unknown>" } ?: "Unknown Artist"
                }

                AlbumUiModel(
                    id = repSong.albumId.takeIf { it != -1L } ?: repSong.id,
                    name = albumName,
                    artist = resolvedArtist,
                    songCount = albumSongs.size,
                    artworkUri = repSong.artworkUri,
                    dataPath = repSong.dataPath,
                    albumId = repSong.albumId,
                    dateModified = repSong.dateModified
                )
            }

        val filtered = if (searchQuery.isBlank()) grouped else {
            grouped.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true)
            }
        }

        return when (sortCriteria) {
            SortCriteria.ALBUM, SortCriteria.TITLE -> {
                if (isAscending) {
                    filtered.sortedWith { a, b ->
                        val nameCmp = SortHelper.compareAlbums(a.name, b.name)
                        if (nameCmp != 0) nameCmp else SortHelper.compareArtists(a.artist, b.artist)
                    }
                } else {
                    filtered.sortedWith { a, b ->
                        val nameCmp = SortHelper.compareAlbums(b.name, a.name)
                        if (nameCmp != 0) nameCmp else SortHelper.compareArtists(a.artist, b.artist)
                    }
                }
            }
            SortCriteria.ARTIST -> {
                if (isAscending) {
                    filtered.sortedWith { a, b ->
                        val artistCmp = SortHelper.compareArtists(a.artist, b.artist)
                        if (artistCmp != 0) artistCmp else SortHelper.compareAlbums(a.name, b.name)
                    }
                } else {
                    filtered.sortedWith { a, b ->
                        val artistCmp = SortHelper.compareArtists(b.artist, a.artist)
                        if (artistCmp != 0) artistCmp else SortHelper.compareAlbums(a.name, b.name)
                    }
                }
            }
            SortCriteria.COUNT -> {
                if (isAscending) {
                    filtered.sortedWith { a, b ->
                        val countCmp = a.songCount.compareTo(b.songCount)
                        if (countCmp != 0) countCmp else SortHelper.compareAlbums(a.name, b.name)
                    }
                } else {
                    filtered.sortedWith { a, b ->
                        val countCmp = b.songCount.compareTo(a.songCount)
                        if (countCmp != 0) countCmp else SortHelper.compareAlbums(a.name, b.name)
                    }
                }
            }
            else -> {
                if (isAscending) {
                    filtered.sortedWith { a, b ->
                        val nameCmp = SortHelper.compareAlbums(a.name, b.name)
                        if (nameCmp != 0) nameCmp else SortHelper.compareArtists(a.artist, b.artist)
                    }
                } else {
                    filtered.sortedWith { a, b ->
                        val nameCmp = SortHelper.compareAlbums(b.name, a.name)
                        if (nameCmp != 0) nameCmp else SortHelper.compareArtists(a.artist, b.artist)
                    }
                }
            }
        }
    }
}
