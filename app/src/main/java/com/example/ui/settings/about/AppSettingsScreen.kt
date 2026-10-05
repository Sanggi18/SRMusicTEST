package com.example.ui.settings.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.settings.components.SettingsCard
import com.example.ui.settings.SettingsDestination
import com.example.ui.settings.components.SettingsNavigationRow

@Composable
fun AppSettingsScreen(
    onNavigate: (SettingsDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsCard {
            SettingsNavigationRow(
                title = "About SRMusic",
                subtitle = "Version 1.0.0",
                icon = Icons.Rounded.Info,
                onClick = { onNavigate(SettingsDestination.About) }
            )
        }

        SettingsCard {
            SettingsNavigationRow(
                title = "Changelog",
                subtitle = "Release notes & updates",
                icon = Icons.Rounded.History,
                onClick = { onNavigate(SettingsDestination.Changelog) }
            )
        }

        SettingsCard {
            SettingsNavigationRow(
                title = "Credits & Support",
                subtitle = "Open source libraries & architecture",
                icon = Icons.Rounded.SupportAgent,
                onClick = { onNavigate(SettingsDestination.Credits) }
            )
        }
    }
}
