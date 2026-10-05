package com.example.ui.settings.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.settings.SettingsDestination
import com.example.ui.settings.components.SettingsCard
import com.example.ui.settings.components.SettingsDivider
import com.example.ui.settings.components.SettingsNavigationRow
import com.example.ui.settings.components.SettingsSectionHeader
import com.example.ui.settings.components.SettingsSwitchRow
import com.example.ui.settings.core.SettingsAction
import com.example.ui.settings.core.SettingsUiState

@Composable
fun LibrarySettingsScreen(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    onRefreshLibrary: () -> Unit,
    onNavigate: (SettingsDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterDurationDialog by remember { mutableStateOf(false) }
    var tempDurationInput by remember(uiState.scanOptions.minDurationSeconds) {
        mutableStateOf(uiState.scanOptions.minDurationSeconds.toString())
    }
    var tempFilterEnabled by remember(uiState.scanOptions.filterShortAudio) {
        mutableStateOf(uiState.scanOptions.filterShortAudio)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSectionHeader("Media Library Management")

        SettingsCard {
            Column {
                SettingsNavigationRow(
                    title = "Music Folders & Blacklist",
                    subtitle = "${uiState.scanOptions.includedFolders.size} included scan locations • ${uiState.scanOptions.blacklistedFolders.size} blacklisted",
                    icon = Icons.Rounded.Folder,
                    onClick = { onNavigate(SettingsDestination.MusicFolders) }
                )

                SettingsDivider()

                SettingsNavigationRow(
                    title = "Filter Short Audio",
                    subtitle = if (uiState.scanOptions.filterShortAudio)
                        "Active • Exclude clips under ${uiState.scanOptions.minDurationSeconds}s"
                    else
                        "Disabled • All audio clips included",
                    icon = Icons.Rounded.FolderSpecial,
                    onClick = {
                        tempDurationInput = uiState.scanOptions.minDurationSeconds.toString()
                        tempFilterEnabled = uiState.scanOptions.filterShortAudio
                        showFilterDurationDialog = true
                    }
                )

                SettingsDivider()

                SettingsSwitchRow(
                    title = "Auto-scan on Launch",
                    subtitle = "Scan storage automatically with folder & filter rules when app opens",
                    icon = Icons.Rounded.Refresh,
                    checked = uiState.autoScanEnabled,
                    onCheckedChange = { onAction(SettingsAction.SetAutoScanEnabled(it)) }
                )

                SettingsDivider()

                SettingsNavigationRow(
                    title = "Rescan Media Library",
                    subtitle = "Scan device storage for new or modified audio files",
                    icon = Icons.Rounded.Refresh,
                    onClick = onRefreshLibrary
                )
            }
        }
    }

    // Filter Short Audio Customization Dialog
    if (showFilterDurationDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDurationDialog = false },
            title = { Text("Filter Short Audio Duration") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Exclude audio files shorter than this threshold (in seconds) to remove ringtones, game sound effects, and voice notes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Short Audio Filter",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = tempFilterEnabled,
                            onCheckedChange = { tempFilterEnabled = it }
                        )
                    }

                    OutlinedTextField(
                        value = tempDurationInput,
                        onValueChange = { input ->
                            val digitsOnly = input.filter { it.isDigit() }
                            tempDurationInput = digitsOnly
                        },
                        label = { Text("Minimum duration (seconds)") },
                        suffix = { Text("sec") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = tempFilterEnabled
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedSeconds = tempDurationInput.toIntOrNull() ?: 30
                        val cleanSeconds = parsedSeconds.coerceIn(1, 3600)
                        onAction(SettingsAction.SetFilterShortAudio(tempFilterEnabled, cleanSeconds))
                        showFilterDurationDialog = false
                        onRefreshLibrary()
                    }
                ) {
                    Text("Save & Rescan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFilterDurationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
