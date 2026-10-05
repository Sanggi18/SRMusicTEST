package com.example.core.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["artist"]),
        Index(value = ["album"]),
        Index(value = ["title"]),
        Index(value = ["dateAdded"]),
        Index(value = ["genre"])
    ]
)
data class SongEntity(
    @PrimaryKey
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
)
