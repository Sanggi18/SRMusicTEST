package com.example.ui.library.artist
import com.example.ui.library.core.*
import com.example.ui.library.components.*
import com.example.ui.common.components.*

import com.example.core.model.Song
import com.example.ui.library.components.SortCriteria
import com.example.core.util.ArtistHelper
import com.example.core.util.SortHelper

object ArtistFilter {
    fun buildArtistsList(
        songs: List<Song>,
        searchQuery: String,
        sortCriteria: SortCriteria,
        isAscending: Boolean
    ): List<ArtistUiModel> {
        val grouped = songs.groupBy { ArtistHelper.extractMainArtist(it.artist) }
            .filterKeys { it.isNotBlank() && it != "<unknown>" && !it.equals("unknown artist", ignoreCase = true) }
            .map { (artistName, artistSongs) ->
                val representativeSong = artistSongs.firstOrNull { it.artworkUri != null || it.dataPath.isNotBlank() } ?: artistSongs.first()
                ArtistUiModel(
                    id = representativeSong.id,
                    name = artistName,
                    songCount = artistSongs.size,
                    albumCount = artistSongs.map { it.album }.distinct().size,
                    artworkUri = representativeSong.artworkUri,
                    dataPath = representativeSong.dataPath,
                    albumId = representativeSong.albumId,
                    dateModified = representativeSong.dateModified
                )
            }

        val filtered = if (searchQuery.isBlank()) grouped else {
            grouped.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

        val sorted = when (sortCriteria) {
            SortCriteria.ARTIST, SortCriteria.TITLE -> filtered.sortedWith { a, b -> SortHelper.compareTitles(a.name, b.name) }
            SortCriteria.COUNT -> filtered.sortedWith { a, b ->
                val countCmp = a.songCount.compareTo(b.songCount)
                if (countCmp != 0) countCmp else SortHelper.compareTitles(a.name, b.name)
            }
            else -> filtered.sortedWith { a, b -> SortHelper.compareTitles(a.name, b.name) }
        }

        return if (isAscending) sorted else sorted.reversed()
    }
}
