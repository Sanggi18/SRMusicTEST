package com.example.core.model

enum class AppThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
    AMOLED,
    CUSTOM
}

data class ThemeConfig(
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val isDynamicColor: Boolean = true,
    val isArtworkAccent: Boolean = false,
    val accentColorArgb: Long = 0xFF2196F3L,
    val customBackgroundColorArgb: Long? = null,
    val customFontColorArgb: Long? = null,
    val customThemePresetId: String = "nordic_night"
)
