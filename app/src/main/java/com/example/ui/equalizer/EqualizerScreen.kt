package com.example.ui.equalizer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.equalizer.components.AudioOutputCard
import com.example.ui.equalizer.components.AudioOutputDialog
import com.example.ui.equalizer.components.BitPerfectCard
import com.example.ui.equalizer.components.CustomPresetsDialog
import com.example.ui.equalizer.components.EditCustomPresetDialog
import com.example.ui.equalizer.components.FrequencyInfoDialog
import com.example.ui.equalizer.components.PreampCard
import com.example.ui.equalizer.components.SaveCustomPresetDialog
import com.example.ui.equalizer.components.TemplatePresetDialog
import com.example.ui.equalizer.components.ToneControlCard
import com.example.ui.equalizer.components.UnifiedGraphicEqualizerAndPresetsCard
import com.example.ui.equalizer.core.EqualizerAction
import com.example.ui.equalizer.core.EqualizerUiState

@Composable
fun EqualizerScreen(
    uiState: EqualizerUiState,
    onAction: (EqualizerAction) -> Unit,
    onMenuClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bandFrequencies = remember {
        listOf("31Hz", "62Hz", "125Hz", "250Hz", "500Hz", "1kHz", "2kHz", "4kHz", "8kHz", "16kHz")
    }

    BackHandler(enabled = onBackClick != null) {
        onBackClick?.invoke()
    }

    var showAudioOutputDialog by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var showSaveCustomDialog by remember { mutableStateOf(false) }
    var newCustomPresetName by remember { mutableStateOf("") }
    var showEditPresetDialog by remember { mutableStateOf(false) }
    var editingPresetIndex by remember { mutableIntStateOf(-1) }
    var editingPresetName by remember { mutableStateOf("") }
    var showFrequencyInfoDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 96.dp)
            .testTag("equalizer_screen")
    ) {
        // Top Header using official M3 TopAppBar
        @OptIn(ExperimentalMaterial3Api::class)
        TopAppBar(
            title = {
                Text(
                    text = "Equalizer",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            navigationIcon = {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("eq_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else if (onMenuClick != null) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.testTag("eq_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "Open Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            actions = {},
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Bit Perfect Mode Card
            BitPerfectCard(
                isBitPerfectEnabled = uiState.isBitPerfectEnabled,
                onBitPerfectToggle = { onAction(EqualizerAction.SetBitPerfect(it)) }
            )

            // 2. Audio Output
            AudioOutputCard(
                selectedAudioOutput = uiState.selectedAudioOutput,
                onClick = { showAudioOutputDialog = true }
            )

            // 3. Unified 10-Band Graphic EQ & Presets Card
            UnifiedGraphicEqualizerAndPresetsCard(
                uiState = uiState,
                onAction = onAction,
                frequencies = bandFrequencies,
                onSaveCustomPreset = {
                    newCustomPresetName = ""
                    showSaveCustomDialog = true
                },
                onOpenFrequencyGuide = { showFrequencyInfoDialog = true },
                onOpenTemplateDialog = { showTemplateDialog = true },
                onOpenCustomDialog = { showCustomDialog = true },
                modifier = Modifier.fillMaxWidth()
            )

            // 4. Preamp Section
            PreampCard(
                preampGain = uiState.preampGain,
                isEqActive = uiState.isEqActive,
                onPreampGainChange = { onAction(EqualizerAction.SetPreampGain(it)) }
            )

            // 5. Tone Controls (Bass, Vocal, Treble)
            ToneControlCard(
                bassBoost = uiState.bassBoost,
                vocalBoost = uiState.vocalBoost,
                trebleBoost = uiState.trebleBoost,
                isEqActive = uiState.isEqActive,
                onBassBoostChange = { onAction(EqualizerAction.SetBassBoost(it)) },
                onVocalBoostChange = { onAction(EqualizerAction.SetVocalBoost(it)) },
                onTrebleBoostChange = { onAction(EqualizerAction.SetTrebleBoost(it)) }
            )
        }
    }

    // Dialogs
    if (showAudioOutputDialog) {
        AudioOutputDialog(
            selectedAudioOutput = uiState.selectedAudioOutput,
            onSelectAudioOutput = { onAction(EqualizerAction.SetAudioOutput(it)) },
            onDismiss = { showAudioOutputDialog = false }
        )
    }

    if (showTemplateDialog) {
        TemplatePresetDialog(
            selectedPreset = uiState.selectedPreset,
            selectedTemplateName = uiState.selectedTemplateName,
            onSelectTemplate = { templateName ->
                onAction(EqualizerAction.SelectTemplate(templateName))
                showTemplateDialog = false
            },
            onDismiss = { showTemplateDialog = false }
        )
    }

    if (showCustomDialog) {
        CustomPresetsDialog(
            customPresets = uiState.customPresets,
            selectedPreset = uiState.selectedPreset,
            selectedCustomName = uiState.selectedCustomName,
            onApplyCustomPreset = { name, values ->
                onAction(EqualizerAction.ApplyCustomPreset(name, values))
                showCustomDialog = false
            },
            onOpenSaveCurrent = {
                newCustomPresetName = ""
                showSaveCustomDialog = true
            },
            onEditPreset = { index, oldName ->
                editingPresetIndex = index
                editingPresetName = oldName
                showEditPresetDialog = true
            },
            onDeletePreset = { index, name ->
                onAction(EqualizerAction.DeleteCustomPreset(index, name))
            },
            onDismiss = { showCustomDialog = false }
        )
    }

    if (showSaveCustomDialog) {
        SaveCustomPresetDialog(
            presetName = newCustomPresetName,
            onPresetNameChange = { newCustomPresetName = it },
            onSave = { name ->
                onAction(EqualizerAction.SaveCustomPreset(name, uiState.bands))
                showSaveCustomDialog = false
            },
            onDismiss = { showSaveCustomDialog = false }
        )
    }

    if (showEditPresetDialog) {
        EditCustomPresetDialog(
            presetName = editingPresetName,
            onPresetNameChange = { editingPresetName = it },
            onSave = { newName ->
                val oldName = uiState.customPresets.getOrNull(editingPresetIndex)?.name ?: ""
                onAction(EqualizerAction.RenameCustomPreset(editingPresetIndex, oldName, newName))
                showEditPresetDialog = false
            },
            onDismiss = { showEditPresetDialog = false }
        )
    }

    if (showFrequencyInfoDialog) {
        FrequencyInfoDialog(
            onDismiss = { showFrequencyInfoDialog = false }
        )
    }
}
