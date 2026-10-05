package com.example.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.core.model.LibraryScanOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LibraryPreferences(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _scanOptionsFlow = MutableStateFlow(loadScanOptions())
    val scanOptionsFlow: StateFlow<LibraryScanOptions> = _scanOptionsFlow.asStateFlow()

    private val _autoScanOnLaunchFlow = MutableStateFlow(prefs.getBoolean(KEY_AUTO_SCAN_ON_LAUNCH, true))
    val autoScanOnLaunchFlow: StateFlow<Boolean> = _autoScanOnLaunchFlow.asStateFlow()

    companion object {
        private const val PREFS_NAME = "srmusic_prefs"
        const val KEY_INCLUDED_FOLDERS = "music_included_folders"
        const val KEY_BLACKLISTED_FOLDERS = "music_blacklisted_folders"
        const val KEY_FILTER_SHORT_AUDIO = "filter_short_audio_enabled"
        const val KEY_FILTER_SHORT_SECONDS = "filter_short_audio_seconds"
        const val KEY_AUTO_SCAN_ON_LAUNCH = "auto_scan_on_launch"

        val DEFAULT_INCLUDED = setOf("/sdcard/Music/", "/storage/emulated/0/Music/")
        val DEFAULT_BLACKLIST = setOf(
            "Android/data",
            "Android/obb",
            "Android/media",
            ".thumbnails",
            "Notifications",
            "Ringtones",
            "Alarms"
        )
    }

    private fun loadScanOptions(): LibraryScanOptions {
        val included = prefs.getStringSet(KEY_INCLUDED_FOLDERS, null) ?: DEFAULT_INCLUDED
        val blacklisted = prefs.getStringSet(KEY_BLACKLISTED_FOLDERS, null) ?: DEFAULT_BLACKLIST
        val filterShort = prefs.getBoolean(KEY_FILTER_SHORT_AUDIO, true)
        val filterSec = prefs.getInt(KEY_FILTER_SHORT_SECONDS, 30)
        return LibraryScanOptions(
            includedFolders = included,
            blacklistedFolders = blacklisted,
            filterShortAudio = filterShort,
            minDurationSeconds = filterSec
        )
    }

    fun getScanOptions(): LibraryScanOptions = _scanOptionsFlow.value

    fun updateScanOptions(options: LibraryScanOptions) {
        prefs.edit()
            .putStringSet(KEY_INCLUDED_FOLDERS, options.includedFolders)
            .putStringSet(KEY_BLACKLISTED_FOLDERS, options.blacklistedFolders)
            .putBoolean(KEY_FILTER_SHORT_AUDIO, options.filterShortAudio)
            .putInt(KEY_FILTER_SHORT_SECONDS, options.minDurationSeconds)
            .apply()
        _scanOptionsFlow.value = options
    }

    fun addIncludedFolder(path: String) {
        val current = _scanOptionsFlow.value.includedFolders.toMutableSet()
        current.add(path)
        updateScanOptions(_scanOptionsFlow.value.copy(includedFolders = current))
    }

    fun removeIncludedFolder(path: String) {
        val current = _scanOptionsFlow.value.includedFolders.toMutableSet()
        current.remove(path)
        updateScanOptions(_scanOptionsFlow.value.copy(includedFolders = current))
    }

    fun addBlacklistedFolder(path: String) {
        val current = _scanOptionsFlow.value.blacklistedFolders.toMutableSet()
        current.add(path)
        updateScanOptions(_scanOptionsFlow.value.copy(blacklistedFolders = current))
    }

    fun removeBlacklistedFolder(path: String) {
        val current = _scanOptionsFlow.value.blacklistedFolders.toMutableSet()
        current.remove(path)
        updateScanOptions(_scanOptionsFlow.value.copy(blacklistedFolders = current))
    }

    fun setFilterShortAudio(enabled: Boolean, seconds: Int = _scanOptionsFlow.value.minDurationSeconds) {
        updateScanOptions(_scanOptionsFlow.value.copy(filterShortAudio = enabled, minDurationSeconds = seconds))
    }

    fun setAutoScanOnLaunch(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SCAN_ON_LAUNCH, enabled).apply()
        _autoScanOnLaunchFlow.value = enabled
    }

    fun isAutoScanOnLaunch(): Boolean = _autoScanOnLaunchFlow.value
}
