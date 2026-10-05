package com.example.core.model

data class LibraryScanOptions(
    val includedFolders: Set<String> = setOf("/sdcard/Music/", "/storage/emulated/0/Music/"),
    val blacklistedFolders: Set<String> = setOf(
        "Android/data",
        "Android/obb",
        "Android/media",
        ".thumbnails",
        "Notifications",
        "Ringtones",
        "Alarms"
    ),
    val filterShortAudio: Boolean = true,
    val minDurationSeconds: Int = 30
)
