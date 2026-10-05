package com.example.ui.navigation.components

/**
 * Canonical Navigation Tab item enum representing available and enabled
 * bottom navigation destinations in the application.
 */
enum class TabItem(val id: String, val label: String) {
    HOME("home", "Home"),
    SONG("song", "Song"),
    FOLDER("folder", "Folder"),
    ARTIST("artist", "Artist"),
    ALBUM("album", "Album");

    val title: String get() = label
}
