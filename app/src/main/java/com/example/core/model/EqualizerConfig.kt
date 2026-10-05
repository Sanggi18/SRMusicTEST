package com.example.core.model

data class EqualizerConfig(
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
    val customPresets: List<EqualizerCustomPreset> = listOf(
        EqualizerCustomPreset("My Favorite", listOf(4f, 3f, 1f, 0f, 1f, 2f, 2f, 3f, 4f, 4f))
    )
)

data class EqualizerCustomPreset(
    val name: String,
    val bandGains: List<Float>
)
