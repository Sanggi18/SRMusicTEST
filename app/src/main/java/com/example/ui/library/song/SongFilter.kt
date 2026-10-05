package com.example.ui.library.song
import com.example.ui.library.core.*
import com.example.ui.library.components.*
import com.example.ui.common.components.*

import com.example.core.model.Song
import com.example.ui.library.components.SortCriteria
import com.example.core.util.SortHelper

object SongFilter {
    fun filterAndSort(
        songs: List<Song>,
        searchQuery: String,
        sortCriteria: SortCriteria,
        isAscending: Boolean,
        playCounts: Map<Long, Int> = emptyMap()
    ): List<Song> = filterAndSortSongs(songs, searchQuery, sortCriteria, isAscending, playCounts)

    fun filterAndSortSongs(
        songs: List<Song>,
        searchQuery: String,
        sortCriteria: SortCriteria,
        isAscending: Boolean,
        playCounts: Map<Long, Int> = emptyMap()
    ): List<Song> {
        val filtered = if (searchQuery.isBlank()) songs else {
            songs.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true) ||
                it.album.contains(searchQuery, ignoreCase = true)
            }
        }

        return when (sortCriteria) {
            SortCriteria.TITLE -> {
                val cmp = Comparator<Song> { a, b -> SortHelper.compareTitles(a.title, b.title) }
                if (isAscending) filtered.sortedWith(cmp) else filtered.sortedWith(cmp.reversed())
            }
            SortCriteria.ARTIST -> {
                if (isAscending) {
                    filtered.sortedWith { a, b ->
                        val artistCmp = SortHelper.compareArtists(a.artist, b.artist)
                        if (artistCmp != 0) artistCmp else SortHelper.compareTitles(a.title, b.title)
                    }
                } else {
                    filtered.sortedWith { a, b ->
                        val artistCmp = SortHelper.compareArtists(b.artist, a.artist)
                        if (artistCmp != 0) artistCmp else SortHelper.compareTitles(a.title, b.title)
                    }
                }
            }
            SortCriteria.ALBUM -> {
                if (isAscending) {
                    filtered.sortedWith { a, b ->
                        val albumCmp = SortHelper.compareAlbums(a.album, b.album)
                        if (albumCmp != 0) {
                            albumCmp
                        } else {
                            val trackCmp = a.trackNumber.compareTo(b.trackNumber)
                            if (trackCmp != 0 && a.trackNumber > 0 && b.trackNumber > 0) trackCmp
                            else SortHelper.compareTitles(a.title, b.title)
                        }
                    }
                } else {
                    filtered.sortedWith { a, b ->
                        val albumCmp = SortHelper.compareAlbums(b.album, a.album)
                        if (albumCmp != 0) {
                            albumCmp
                        } else {
                            val trackCmp = a.trackNumber.compareTo(b.trackNumber)
                            if (trackCmp != 0 && a.trackNumber > 0 && b.trackNumber > 0) trackCmp
                            else SortHelper.compareTitles(a.title, b.title)
                        }
                    }
                }
            }
            SortCriteria.DATE_ADDED -> {
                val cmp = compareBy<Song> { it.dateAdded.takeIf { d -> d > 0 } ?: it.year.toLong() }
                if (isAscending) filtered.sortedWith(cmp) else filtered.sortedWith(cmp.reversed())
            }
            SortCriteria.DATE_MODIFIED -> {
                val cmp = compareBy<Song> { it.dateModified.takeIf { d -> d > 0 } ?: it.dateAdded }
                if (isAscending) filtered.sortedWith(cmp) else filtered.sortedWith(cmp.reversed())
            }
            SortCriteria.DURATION -> {
                val cmp = compareBy<Song> { it.duration }
                if (isAscending) filtered.sortedWith(cmp) else filtered.sortedWith(cmp.reversed())
            }
            SortCriteria.COUNT -> {
                if (isAscending) {
                    filtered.sortedWith { a, b ->
                        val countA = playCounts[a.id] ?: 0
                        val countB = playCounts[b.id] ?: 0
                        val cmp = countA.compareTo(countB)
                        if (cmp != 0) cmp else SortHelper.compareTitles(a.title, b.title)
                    }
                } else {
                    filtered.sortedWith { a, b ->
                        val countA = playCounts[a.id] ?: 0
                        val countB = playCounts[b.id] ?: 0
                        val cmp = countB.compareTo(countA)
                        if (cmp != 0) cmp else SortHelper.compareTitles(a.title, b.title)
                    }
                }
            }
        }
    }
}
