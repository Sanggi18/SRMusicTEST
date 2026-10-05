package com.example.ui.settings.core
import com.example.ui.settings.components.*
import com.example.ui.common.components.*

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.core.model.AppThemeMode
import com.example.ui.navigation.components.TabItem
import com.example.ui.common.theme.ThemeMode

fun AppThemeMode.toUiThemeMode(): ThemeMode = when (this) {
    AppThemeMode.LIGHT -> ThemeMode.LIGHT
    AppThemeMode.DARK -> ThemeMode.DARK
    AppThemeMode.SYSTEM -> ThemeMode.SYSTEM
    AppThemeMode.AMOLED -> ThemeMode.AMOLED
    AppThemeMode.CUSTOM -> ThemeMode.CUSTOM
}

fun ThemeMode.toAppThemeMode(): AppThemeMode = when (this) {
    ThemeMode.LIGHT -> AppThemeMode.LIGHT
    ThemeMode.DARK -> AppThemeMode.DARK
    ThemeMode.SYSTEM -> AppThemeMode.SYSTEM
    ThemeMode.AMOLED -> AppThemeMode.AMOLED
    ThemeMode.CUSTOM -> AppThemeMode.CUSTOM
}

fun List<String>.toTabItems(): List<TabItem> = mapNotNull { id ->
    TabItem.entries.find { it.id == id || it.name.equals(id, ignoreCase = true) }
}.ifEmpty {
    listOf(TabItem.HOME, TabItem.SONG, TabItem.ARTIST, TabItem.ALBUM)
}

fun List<TabItem>.toTabIds(): List<String> = map { it.id }

fun Long.toComposeColor(): Color {
    val high32 = (this ushr 32) and 0xFFFFFFFFL
    return if (high32 != 0L) {
        Color(high32.toInt())
    } else {
        Color(this.toInt())
    }
}

fun Color.toArgbLong(): Long = (this.toArgb().toLong() and 0xFFFFFFFFL)
