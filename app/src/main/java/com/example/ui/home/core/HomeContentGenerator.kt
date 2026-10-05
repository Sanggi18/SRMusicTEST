package com.example.ui.home.core

import com.example.core.model.Song
import com.example.core.util.ArtistHelper
import com.example.core.data.metadata.MetadataExtractor
import com.example.core.util.TimeUtils
import java.util.Calendar
import java.util.Random

enum class HomeChildScreen {
    NONE,
    FOR_YOU_REMIX,
    GENRE_TODAY,
    NEW_RELEASE,
    TOP_ARTIST,
    TOP_ALBUM,
    TOP_SONGS
}

data class DailyGenreItem(
    val name: String,
    val songs: List<Song>,
    val sampleSong: Song?,
    val artists: List<String>
)

enum class BillboardCategory {
    TOP_ARTIST,
    TOP_ALBUM,
    TOP_SONGS
}

enum class BillboardFilter {
    BY_PLAYING,
    BY_SONGS
}

data class BillboardItemData(
    val id: String,
    val title: String,
    val subtitle: String,
    val detailMetric: String,
    val artworkSong: Song?,
    val isArtist: Boolean = false,
    val songs: List<Song> = emptyList()
)

object HomeContentGenerator {

    fun generateRemixQueue(
        allSongs: List<Song>,
        installSeed: Long,
        manualOffset: Int
    ): List<Song> {
        if (allSongs.isEmpty()) return emptyList()
        val calendar = Calendar.getInstance()
        val seed = installSeed + calendar.get(Calendar.DAY_OF_YEAR) * 10000L + calendar.get(Calendar.YEAR) + manualOffset
        val rnd = Random(seed)
        val shuffled = allSongs.shuffled(rnd)
        return if (shuffled.size <= 100) shuffled else shuffled.take(100)
    }

    fun generateDailyGenres(
        allSongs: List<Song>,
        installSeed: Long,
        manualOffset: Int
    ): List<DailyGenreItem> {
        if (allSongs.isEmpty()) {
            return emptyList()
        }

        val genreMap = mutableMapOf<String, MutableList<Song>>()
        for (song in allSongs) {
            if (MetadataExtractor.isValidGenre(song.genre)) {
                val cleaned = MetadataExtractor.cleanGenre(song.genre)
                genreMap.getOrPut(cleaned) { mutableListOf() }.add(song)
            }
        }

        val strictGenres = genreMap.filter { it.value.size >= 10 }.keys.toList().sorted()
        val availableGenres = if (strictGenres.isNotEmpty()) strictGenres else genreMap.keys.toList().sorted()
        val calendar = Calendar.getInstance()
        val daySeed = installSeed + calendar.get(Calendar.DAY_OF_YEAR) + (calendar.get(Calendar.YEAR) * 366)

        return if (availableGenres.isNotEmpty()) {
            val baseIndex = (Math.abs(daySeed) % availableGenres.size).toInt()
            val count = minOf(3, availableGenres.size)
            (0 until count).map { i ->
                val gIndex = (baseIndex + manualOffset + i).mod(availableGenres.size)
                val gName = availableGenres[gIndex]
                val gSongs = genreMap[gName] ?: emptyList()
                val gArtists = gSongs.map { ArtistHelper.extractMainArtist(it.artist) }
                    .filter { it.isNotBlank() && it != "<unknown>" && !it.equals("unknown artist", ignoreCase = true) }
                    .distinct()
                DailyGenreItem(
                    name = gName,
                    songs = gSongs,
                    sampleSong = gSongs.firstOrNull(),
                    artists = gArtists
                )
            }
        } else {
            val defaultArtists = allSongs.map { ArtistHelper.extractMainArtist(it.artist) }
                .filter { it.isNotBlank() && it != "<unknown>" && !it.equals("unknown artist", ignoreCase = true) }
                .distinct()
                .take(5)
            listOf(
                DailyGenreItem(
                    name = "Music",
                    songs = allSongs,
                    sampleSong = allSongs.firstOrNull(),
                    artists = defaultArtists
                )
            )
        }
    }

    fun computeTopArtist(
        songs: List<Song>,
        songPlayCounts: Map<Long, Int>
    ): Map.Entry<String, List<Song>>? {
        return songs.groupBy { ArtistHelper.extractMainArtist(it.artist) }
            .filter { it.key.isNotBlank() && it.key != "<unknown>" && !it.key.equals("unknown artist", ignoreCase = true) }
            .maxWithOrNull(
                compareBy<Map.Entry<String, List<Song>>> { it.value.size }
                    .thenBy { entry -> entry.value.sumOf { songPlayCounts[it.id] ?: 0 } }
            )
    }

    fun computeTopAlbum(
        songs: List<Song>,
        songPlayCounts: Map<Long, Int>
    ): Map.Entry<String, List<Song>>? {
        return songs.groupBy { it.album }
            .filter { it.key.isNotBlank() && it.key != "<unknown>" && !it.key.equals("unknown album", ignoreCase = true) }
            .maxWithOrNull(
                compareBy<Map.Entry<String, List<Song>>> { it.value.size }
                    .thenBy { entry -> entry.value.sumOf { songPlayCounts[it.id] ?: 0 } }
            )
    }

    fun computeTopSong(
        songs: List<Song>,
        songPlayCounts: Map<Long, Int>
    ): Song? {
        return songs.maxWithOrNull(
            compareByDescending<Song> { it.dateModified }
                .thenBy { songPlayCounts[it.id] ?: 0 }
        ) ?: songs.firstOrNull()
    }

    fun computeRankedBillboardItems(
        category: BillboardCategory,
        selectedFilter: BillboardFilter,
        allSongs: List<Song>,
        songPlayCounts: Map<Long, Int>
    ): List<BillboardItemData> {
        return when (category) {
            BillboardCategory.TOP_ARTIST -> {
                val artistGroups = allSongs.groupBy { it.artist.ifBlank { "Unknown Artist" } }
                val mapped = artistGroups.map { (artist, songs) ->
                    val totalPlays = songs.sumOf { songPlayCounts[it.id] ?: 0 }
                    val songCount = songs.size
                    BillboardItemData(
                        id = artist,
                        title = artist,
                        subtitle = "$songCount ${if (songCount == 1) "Song" else "Songs"}",
                        detailMetric = if (selectedFilter == BillboardFilter.BY_PLAYING) "$totalPlays Plays" else "$songCount Songs",
                        artworkSong = songs.firstOrNull(),
                        isArtist = true,
                        songs = songs
                    )
                }
                if (selectedFilter == BillboardFilter.BY_PLAYING) {
                    mapped.sortedWith(
                        compareByDescending<BillboardItemData> { it.songs.sumOf { s -> songPlayCounts[s.id] ?: 0 } }
                            .thenByDescending { it.songs.size }
                            .thenBy { it.title.lowercase() }
                    )
                } else {
                    mapped.sortedWith(
                        compareByDescending<BillboardItemData> { it.songs.size }
                            .thenByDescending { it.songs.sumOf { s -> songPlayCounts[s.id] ?: 0 } }
                            .thenBy { it.title.lowercase() }
                    )
                }
            }

            BillboardCategory.TOP_ALBUM -> {
                val albumGroups = allSongs.groupBy { it.album.ifBlank { "Unknown Album" } }
                val mapped = albumGroups.map { (album, songs) ->
                    val totalPlays = songs.sumOf { songPlayCounts[it.id] ?: 0 }
                    val songCount = songs.size
                    val artistName = songs.firstOrNull()?.artist ?: "Various Artists"
                    BillboardItemData(
                        id = album,
                        title = album,
                        subtitle = artistName,
                        detailMetric = if (selectedFilter == BillboardFilter.BY_PLAYING) "$totalPlays Plays" else "$songCount Songs",
                        artworkSong = songs.firstOrNull(),
                        isArtist = false,
                        songs = songs
                    )
                }
                if (selectedFilter == BillboardFilter.BY_PLAYING) {
                    mapped.sortedWith(
                        compareByDescending<BillboardItemData> { it.songs.sumOf { s -> songPlayCounts[s.id] ?: 0 } }
                            .thenByDescending { it.songs.size }
                            .thenBy { it.title.lowercase() }
                    )
                } else {
                    mapped.sortedWith(
                        compareByDescending<BillboardItemData> { it.songs.size }
                            .thenByDescending { it.songs.sumOf { s -> songPlayCounts[s.id] ?: 0 } }
                            .thenBy { it.title.lowercase() }
                    )
                }
            }

            BillboardCategory.TOP_SONGS -> {
                if (selectedFilter == BillboardFilter.BY_PLAYING) {
                    allSongs.sortedWith(
                        compareByDescending<Song> { songPlayCounts[it.id] ?: 0 }
                            .thenByDescending { it.dateModified }
                            .thenBy { it.title.lowercase() }
                    ).map { song ->
                        val plays = songPlayCounts[song.id] ?: 0
                        BillboardItemData(
                            id = song.id.toString(),
                            title = song.title,
                            subtitle = song.artist,
                            detailMetric = "$plays Plays",
                            artworkSong = song,
                            isArtist = false,
                            songs = listOf(song)
                        )
                    }
                } else {
                    allSongs.sortedWith(compareBy<Song> { it.title.lowercase() }).map { song ->
                        BillboardItemData(
                            id = song.id.toString(),
                            title = song.title,
                            subtitle = song.artist,
                            detailMetric = TimeUtils.formatDuration(song.duration),
                            artworkSong = song,
                            isArtist = false,
                            songs = listOf(song)
                        )
                    }
                }
            }
        }
    }

    fun formatListeningTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${seconds}s"
        }
    }
}
