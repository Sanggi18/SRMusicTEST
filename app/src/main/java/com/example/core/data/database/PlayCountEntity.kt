package com.example.core.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "play_counts")
data class PlayCountEntity(
    @PrimaryKey
    val songId: Long,
    val count: Int = 0,
    val lastPlayedTimestamp: Long = System.currentTimeMillis()
)
