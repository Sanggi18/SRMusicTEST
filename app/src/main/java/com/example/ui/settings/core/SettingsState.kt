package com.example.ui.settings.core
import com.example.ui.settings.components.*
import com.example.ui.common.components.*

import com.example.core.model.AppThemeMode
import com.example.core.model.LibraryScanOptions
import com.example.core.model.NowPlayingProgressStyle
import com.example.core.model.NowPlayingTheme
import com.example.core.model.TrackInfoAlignment

sealed interface SettingsAction {
    data class SetThemeMode(val mode: AppThemeMode) : SettingsAction
    data class SetDynamicColor(val enabled: Boolean) : SettingsAction
    data class SetArtworkAccent(val enabled: Boolean) : SettingsAction
    data class SetAccentColor(val colorArgb: Long) : SettingsAction
    data class SetCustomBackgroundColor(val colorArgb: Long?) : SettingsAction
    data class SetCustomFontColor(val colorArgb: Long?) : SettingsAction
    data class SetCustomThemePresetId(val presetId: String) : SettingsAction
    data class SetEnabledTabIds(val tabIds: List<String>) : SettingsAction
    data class SetRootFolderDisplay(val path: String) : SettingsAction
    data class SetScanOptions(val options: LibraryScanOptions) : SettingsAction
    data class SetAutoScanEnabled(val enabled: Boolean) : SettingsAction
    data class AddIncludedFolder(val path: String) : SettingsAction
    data class RemoveIncludedFolder(val path: String) : SettingsAction
    data class AddBlacklistedFolder(val path: String) : SettingsAction
    data class RemoveBlacklistedFolder(val path: String) : SettingsAction
    data class SetFilterShortAudio(val enabled: Boolean, val minDurationSeconds: Int) : SettingsAction
    data class SetGaplessPlayback(val enabled: Boolean) : SettingsAction
    data class SetHighPrecisionResampling(val enabled: Boolean) : SettingsAction
    data class SetAudioFocusDucking(val enabled: Boolean) : SettingsAction
    data class SetReplayGain(val enabled: Boolean) : SettingsAction
    data class SetNowPlayingProgressStyle(val style: NowPlayingProgressStyle) : SettingsAction
    data class SetNowPlayingTheme(val theme: NowPlayingTheme) : SettingsAction
    data class SetTrackInfoAlignment(val alignment: TrackInfoAlignment) : SettingsAction
}

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val isDynamicColor: Boolean = true,
    val isArtworkAccent: Boolean = false,
    val accentColorArgb: Long = 0xFF64B5F6L, // Soft Light Blue
    val customBackgroundColorArgb: Long? = null,
    val customFontColorArgb: Long? = null,
    val customThemePresetId: String = "nordic_night",
    val enabledTabIds: List<String> = listOf("home", "song", "artist", "album", "folder"),
    val rootFolderDisplay: String = "/sdcard/Music/",
    val scanOptions: LibraryScanOptions = LibraryScanOptions(),
    val autoScanEnabled: Boolean = true,
    val gaplessPlayback: Boolean = false,
    val highPrecisionResampling: Boolean = false,
    val audioFocusDucking: Boolean = false,
    val replayGain: Boolean = false,
    val nowPlayingProgressStyle: NowPlayingProgressStyle = NowPlayingProgressStyle.ROUNDED_BAR,
    val nowPlayingTheme: NowPlayingTheme = NowPlayingTheme.DEFAULT,
    val trackInfoAlignment: TrackInfoAlignment = TrackInfoAlignment.LEFT
)
