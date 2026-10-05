package com.example.ui.settings
import com.example.ui.settings.components.*
import com.example.ui.settings.core.*
import com.example.ui.common.components.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.ui.graphics.vector.ImageVector

val BottomNavigationIcon: ImageVector = Icons.Rounded.Apps

sealed class SettingsDestination(val title: String) {
    object Main : SettingsDestination("Settings")
    object Appearance : SettingsDestination("Appearance")
    object AppTheme : SettingsDestination("App Theme")
    object CustomThemes : SettingsDestination("Custom Theme Presets")
    object NowPlayingStyle : SettingsDestination("Now Playing Style")
    object BottomTabs : SettingsDestination("Bottom Navigation Tabs")
    object AudioPlayback : SettingsDestination("Audio & Playback")
    object Library : SettingsDestination("Library")
    object MusicFolders : SettingsDestination("Music Library Folders")
    object App : SettingsDestination("App")
    object About : SettingsDestination("About SRMusic")
    object Changelog : SettingsDestination("Changelog")
    object Credits : SettingsDestination("Credits & Support")
}
