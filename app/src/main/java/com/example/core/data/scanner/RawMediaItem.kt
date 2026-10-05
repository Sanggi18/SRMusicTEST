package com.example.core.data.scanner

import com.example.core.data.database.SongEntity

data class RawMediaItem(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val albumId: Long,
    val contentUriString: String,
    val artworkUriString: String,
    val trackNumber: Int = 0,
    val year: Int = 0,
    val size: Long = 0L,
    val displayName: String = "",
    val relativePath: String = "",
    val dataPath: String = "",
    val dateAdded: Long = 0L,
    val dateModified: Long = 0L,
    val genre: String = ""
) {
    fun toSongEntity(): SongEntity {
        return SongEntity(
            id = id,
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            albumId = albumId,
            contentUriString = contentUriString,
            artworkUriString = artworkUriString,
            trackNumber = trackNumber,
            year = year,
            size = size,
            displayName = displayName,
            relativePath = relativePath,
            dataPath = dataPath,
            dateAdded = dateAdded,
            dateModified = dateModified,
            genre = genre
        )
    }
}
