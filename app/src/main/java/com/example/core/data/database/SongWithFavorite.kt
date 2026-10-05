package com.example.core.data.database

import android.net.Uri
import androidx.room.Embedded
import androidx.room.Relation
import com.example.core.model.Song

data class SongWithFavorite(
    @Embedded
    val song: SongEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "songId"
    )
    val favorite: FavoriteEntity?
) {
    val isFavorite: Boolean
        get() = favorite != null

    fun toSong(): Song {
        return Song(
            id = song.id,
            title = song.title,
            artist = song.artist,
            album = song.album,
            duration = song.duration,
            albumId = song.albumId,
            contentUri = Uri.parse(song.contentUriString),
            artworkUri = Uri.parse(song.artworkUriString),
            trackNumber = song.trackNumber,
            year = song.year,
            size = song.size,
            displayName = song.displayName,
            relativePath = song.relativePath,
            dataPath = song.dataPath,
            dateAdded = song.dateAdded,
            dateModified = song.dateModified,
            genre = song.genre
        )
    }
}
