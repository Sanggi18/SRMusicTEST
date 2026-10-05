package com.example.ui.equalizer.core

sealed interface EqualizerAction {
    data class SetBitPerfect(val enabled: Boolean) : EqualizerAction
    data class SetEqualizerEnabled(val enabled: Boolean) : EqualizerAction
    data class SetAudioOutput(val output: String) : EqualizerAction
    data class SetPreampGain(val gain: Float) : EqualizerAction
    data class SelectPreset(val preset: String) : EqualizerAction
    data class SelectTemplate(val templateName: String) : EqualizerAction
    data class ApplyCustomPreset(val name: String, val values: List<Float>) : EqualizerAction
    data class UpdateBand(val index: Int, val gain: Float) : EqualizerAction
    data class SetBassBoost(val boost: Float) : EqualizerAction
    data class SetVocalBoost(val boost: Float) : EqualizerAction
    data class SetTrebleBoost(val boost: Float) : EqualizerAction
    data class SaveCustomPreset(val name: String, val bands: List<Float>) : EqualizerAction
    data class RenameCustomPreset(val index: Int, val oldName: String, val newName: String) : EqualizerAction
    data class DeleteCustomPreset(val index: Int, val name: String) : EqualizerAction
    data object ResetBands : EqualizerAction
    data object ResetAll : EqualizerAction
}

data class CustomPreset(
    val name: String,
    val bands: List<Float>
)

data class EqualizerUiState(
    val isBitPerfectEnabled: Boolean = false,
    val isEqualizerEnabled: Boolean = false,
    val selectedAudioOutput: String = "Default",
    val preampGain: Float = 0f,
    val selectedPreset: String = "Flat",
    val selectedTemplateName: String? = null,
    val selectedCustomName: String? = null,
    val bands: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
    val bassBoost: Float = 0f,
    val vocalBoost: Float = 0f,
    val trebleBoost: Float = 0f,
    val customPresets: List<CustomPreset> = listOf(
        CustomPreset("My Favorite", listOf(4f, 3f, 1f, 0f, 1f, 2f, 2f, 3f, 4f, 4f))
    )
) {
    val isEqActive: Boolean
        get() = isEqualizerEnabled && !isBitPerfectEnabled
}
