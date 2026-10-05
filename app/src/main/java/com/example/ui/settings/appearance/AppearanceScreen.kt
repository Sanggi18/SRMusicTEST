package com.example.ui.settings.appearance
import com.example.ui.settings.core.*
import com.example.ui.settings.components.*
import com.example.ui.common.components.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.settings.BottomNavigationIcon
import com.example.ui.settings.components.SettingsCard
import com.example.ui.settings.SettingsDestination
import com.example.ui.settings.components.SettingsDivider
import com.example.ui.settings.components.SettingsNavigationRow
import com.example.ui.settings.components.SettingsSectionHeader
import com.example.ui.navigation.components.TabItem
import com.example.ui.common.theme.ThemeMode

@Composable
fun AppearanceScreen(
    currentThemeMode: ThemeMode,
    isDynamicColor: Boolean,
    enabledTabs: List<TabItem>,
    onNavigate: (SettingsDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Theme & Styling Section
        SettingsSectionHeader("Theme & Styling")
        SettingsCard {
            Column {
                SettingsNavigationRow(
                    title = "App Theme",
                    subtitle = if (isDynamicColor) "Material You Dynamic" else currentThemeMode.label,
                    icon = Icons.Rounded.Palette,
                    onClick = { onNavigate(SettingsDestination.AppTheme) }
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = "Now Playing Style",
                    subtitle = "Customize player theme, progress bar & track info",
                    icon = Icons.Rounded.PlayCircle,
                    onClick = { onNavigate(SettingsDestination.NowPlayingStyle) }
                )
            }
        }

        // Navigation & Layout Section
        SettingsSectionHeader("Navigation & Layout")
        SettingsCard {
            SettingsNavigationRow(
                title = "Bottom Navigation Tabs",
                subtitle = "${enabledTabs.size} active tabs configured",
                icon = BottomNavigationIcon,
                onClick = { onNavigate(SettingsDestination.BottomTabs) }
            )
        }
    }
}
