package com.example.ui.equalizer.core

import com.example.ui.equalizer.components.presetBands
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.model.EqualizerConfig
import com.example.core.model.EqualizerCustomPreset
import com.example.core.data.preferences.EqualizerPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EqualizerViewModel(
    private val preferences: EqualizerPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(preferences.loadEqualizerConfig().toUiState())
    val uiState: StateFlow<EqualizerUiState> = _uiState.asStateFlow()

    private fun persistState() {
        preferences.saveEqualizerConfig(_uiState.value.toConfig())
    }

    fun onAction(action: EqualizerAction) {
        when (action) {
            is EqualizerAction.SetBitPerfect -> {
                _uiState.update { it.copy(isBitPerfectEnabled = action.enabled) }
                persistState()
            }
            is EqualizerAction.SetEqualizerEnabled -> {
                _uiState.update { it.copy(isEqualizerEnabled = action.enabled) }
                persistState()
            }
            is EqualizerAction.SetAudioOutput -> {
                _uiState.update { it.copy(selectedAudioOutput = action.output) }
                persistState()
            }
            is EqualizerAction.SetPreampGain -> {
                _uiState.update { it.copy(preampGain = action.gain) }
                persistState()
            }
            is EqualizerAction.SelectPreset -> {
                val newBands = presetBands[action.preset] ?: _uiState.value.bands
                _uiState.update {
                    it.copy(
                        selectedPreset = action.preset,
                        selectedTemplateName = if (action.preset == "Template") it.selectedTemplateName else null,
                        selectedCustomName = if (action.preset == "Custom") it.selectedCustomName else null,
                        bands = newBands
                    )
                }
                persistState()
            }
            is EqualizerAction.SelectTemplate -> {
                val newBands = presetBands[action.templateName] ?: _uiState.value.bands
                _uiState.update {
                    it.copy(
                        selectedPreset = "Template",
                        selectedTemplateName = action.templateName,
                        selectedCustomName = null,
                        bands = newBands
                    )
                }
                persistState()
            }
            is EqualizerAction.ApplyCustomPreset -> {
                _uiState.update {
                    it.copy(
                        selectedPreset = "Custom",
                        selectedCustomName = action.name,
                        selectedTemplateName = null,
                        bands = action.values
                    )
                }
                persistState()
            }
            is EqualizerAction.UpdateBand -> {
                if (_uiState.value.isEqActive && action.index in _uiState.value.bands.indices) {
                    val updatedBands = _uiState.value.bands.toMutableList().apply {
                        this[action.index] = action.gain
                    }
                    _uiState.update { it.copy(bands = updatedBands) }
                    persistState()
                }
            }
            is EqualizerAction.SetBassBoost -> {
                _uiState.update { it.copy(bassBoost = action.boost) }
                persistState()
            }
            is EqualizerAction.SetVocalBoost -> {
                _uiState.update { it.copy(vocalBoost = action.boost) }
                persistState()
            }
            is EqualizerAction.SetTrebleBoost -> {
                _uiState.update { it.copy(trebleBoost = action.boost) }
                persistState()
            }
            is EqualizerAction.SaveCustomPreset -> {
                val trimmedName = action.name.trim()
                if (trimmedName.isNotEmpty()) {
                    val currentList = _uiState.value.customPresets.toMutableList()
                    val existingIndex = currentList.indexOfFirst { it.name.equals(trimmedName, ignoreCase = true) }
                    val newPreset = CustomPreset(trimmedName, action.bands)
                    if (existingIndex >= 0) {
                        currentList[existingIndex] = newPreset
                    } else {
                        currentList.add(newPreset)
                    }
                    _uiState.update {
                        it.copy(
                            customPresets = currentList,
                            selectedPreset = "Custom",
                            selectedCustomName = trimmedName,
                            selectedTemplateName = null,
                            bands = action.bands
                        )
                    }
                    persistState()
                }
            }
            is EqualizerAction.RenameCustomPreset -> {
                val trimmedName = action.newName.trim()
                if (trimmedName.isNotEmpty() && action.index in _uiState.value.customPresets.indices) {
                    val currentList = _uiState.value.customPresets.toMutableList()
                    val oldPreset = currentList[action.index]
                    currentList[action.index] = oldPreset.copy(name = trimmedName)
                    val newSelectedCustom = if (_uiState.value.selectedCustomName == action.oldName) {
                        trimmedName
                    } else {
                        _uiState.value.selectedCustomName
                    }
                    _uiState.update {
                        it.copy(
                            customPresets = currentList,
                            selectedCustomName = newSelectedCustom
                        )
                    }
                    persistState()
                }
            }
            is EqualizerAction.DeleteCustomPreset -> {
                if (action.index in _uiState.value.customPresets.indices) {
                    val currentList = _uiState.value.customPresets.toMutableList()
                    currentList.removeAt(action.index)
                    val isCurrentSelected = _uiState.value.selectedCustomName == action.name
                    _uiState.update {
                        it.copy(
                            customPresets = currentList,
                            selectedPreset = if (isCurrentSelected) "Flat" else it.selectedPreset,
                            selectedCustomName = if (isCurrentSelected) null else it.selectedCustomName
                        )
                    }
                    persistState()
                }
            }
            is EqualizerAction.ResetBands -> {
                val flatBands = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
                _uiState.update {
                    it.copy(
                        bands = flatBands,
                        selectedPreset = "Flat",
                        selectedTemplateName = null,
                        selectedCustomName = null
                    )
                }
                persistState()
            }
            is EqualizerAction.ResetAll -> {
                val flatBands = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
                _uiState.update {
                    it.copy(
                        preampGain = 0f,
                        bands = flatBands,
                        selectedPreset = "Flat",
                        selectedTemplateName = null,
                        selectedCustomName = null,
                        bassBoost = 0f,
                        vocalBoost = 0f,
                        trebleBoost = 0f
                    )
                }
                persistState()
            }
        }
    }

    companion object {
        fun provideFactory(preferences: EqualizerPreferences): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EqualizerViewModel(preferences) as T
                }
            }
    }
}

private fun EqualizerConfig.toUiState(): EqualizerUiState = EqualizerUiState(
    isBitPerfectEnabled = isBitPerfectEnabled,
    isEqualizerEnabled = isEqualizerEnabled,
    selectedAudioOutput = selectedAudioOutput,
    preampGain = preampGain,
    selectedPreset = selectedPreset,
    selectedTemplateName = selectedTemplateName,
    selectedCustomName = selectedCustomName,
    bands = bands,
    bassBoost = bassBoost,
    vocalBoost = vocalBoost,
    trebleBoost = trebleBoost,
    customPresets = customPresets.map { CustomPreset(it.name, it.bandGains) }
)

private fun EqualizerUiState.toConfig(): EqualizerConfig = EqualizerConfig(
    isBitPerfectEnabled = isBitPerfectEnabled,
    isEqualizerEnabled = isEqualizerEnabled,
    selectedAudioOutput = selectedAudioOutput,
    preampGain = preampGain,
    selectedPreset = selectedPreset,
    selectedTemplateName = selectedTemplateName,
    selectedCustomName = selectedCustomName,
    bands = bands,
    bassBoost = bassBoost,
    vocalBoost = vocalBoost,
    trebleBoost = trebleBoost,
    customPresets = customPresets.map { EqualizerCustomPreset(it.name, it.bands) }
)
