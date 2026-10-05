package com.example.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.core.model.EqualizerConfig
import com.example.core.model.EqualizerCustomPreset
import org.json.JSONArray
import org.json.JSONObject

class EqualizerPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "srmusic_prefs"
        private const val KEY_BIT_PERFECT = "eq_bit_perfect"
        private const val KEY_EQ_ENABLED = "eq_enabled"
        private const val KEY_AUDIO_OUTPUT = "eq_audio_output"
        private const val KEY_PREAMP_GAIN = "eq_preamp_gain"
        private const val KEY_SELECTED_PRESET = "eq_selected_preset"
        private const val KEY_SELECTED_TEMPLATE = "eq_selected_template"
        private const val KEY_SELECTED_CUSTOM = "eq_selected_custom"
        private const val KEY_BANDS = "eq_bands"
        private const val KEY_BASS_BOOST = "eq_bass_boost"
        private const val KEY_VOCAL_BOOST = "eq_vocal_boost"
        private const val KEY_TREBLE_BOOST = "eq_treble_boost"
        private const val KEY_CUSTOM_PRESETS = "eq_custom_presets"

        val DEFAULT_BANDS = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        val DEFAULT_CUSTOM_PRESETS = listOf(
            EqualizerCustomPreset("My Favorite", listOf(4f, 3f, 1f, 0f, 1f, 2f, 2f, 3f, 4f, 4f))
        )
    }

    fun loadEqualizerConfig(): EqualizerConfig {
        val bitPerfect = prefs.getBoolean(KEY_BIT_PERFECT, false)
        val eqEnabled = prefs.getBoolean(KEY_EQ_ENABLED, false)
        val audioOutput = prefs.getString(KEY_AUDIO_OUTPUT, "Default") ?: "Default"
        val preamp = prefs.getFloat(KEY_PREAMP_GAIN, 0f)
        val selectedPreset = prefs.getString(KEY_SELECTED_PRESET, "Flat") ?: "Flat"
        val selectedTemplate = prefs.getString(KEY_SELECTED_TEMPLATE, null)
        val selectedCustom = prefs.getString(KEY_SELECTED_CUSTOM, null)
        val bassBoost = prefs.getFloat(KEY_BASS_BOOST, 0f)
        val vocalBoost = prefs.getFloat(KEY_VOCAL_BOOST, 0f)
        val trebleBoost = prefs.getFloat(KEY_TREBLE_BOOST, 0f)

        val bands = loadBands()
        val customPresets = loadCustomPresets()

        return EqualizerConfig(
            isBitPerfectEnabled = bitPerfect,
            isEqualizerEnabled = eqEnabled,
            selectedAudioOutput = audioOutput,
            preampGain = preamp,
            selectedPreset = selectedPreset,
            selectedTemplateName = selectedTemplate,
            selectedCustomName = selectedCustom,
            bands = bands,
            bassBoost = bassBoost,
            vocalBoost = vocalBoost,
            trebleBoost = trebleBoost,
            customPresets = customPresets
        )
    }

    fun saveEqualizerConfig(config: EqualizerConfig) {
        prefs.edit()
            .putBoolean(KEY_BIT_PERFECT, config.isBitPerfectEnabled)
            .putBoolean(KEY_EQ_ENABLED, config.isEqualizerEnabled)
            .putString(KEY_AUDIO_OUTPUT, config.selectedAudioOutput)
            .putFloat(KEY_PREAMP_GAIN, config.preampGain)
            .putString(KEY_SELECTED_PRESET, config.selectedPreset)
            .putString(KEY_SELECTED_TEMPLATE, config.selectedTemplateName)
            .putString(KEY_SELECTED_CUSTOM, config.selectedCustomName)
            .putFloat(KEY_BASS_BOOST, config.bassBoost)
            .putFloat(KEY_VOCAL_BOOST, config.vocalBoost)
            .putFloat(KEY_TREBLE_BOOST, config.trebleBoost)
            .putString(KEY_BANDS, serializeBands(config.bands))
            .putString(KEY_CUSTOM_PRESETS, serializeCustomPresets(config.customPresets))
            .apply()
    }

    private fun loadBands(): List<Float> {
        val saved = prefs.getString(KEY_BANDS, null) ?: return DEFAULT_BANDS
        return try {
            val jsonArray = JSONArray(saved)
            val list = mutableListOf<Float>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getDouble(i).toFloat())
            }
            if (list.size == 10) list else DEFAULT_BANDS
        } catch (_: Exception) {
            DEFAULT_BANDS
        }
    }

    private fun serializeBands(bands: List<Float>): String {
        val jsonArray = JSONArray()
        bands.forEach { jsonArray.put(it.toDouble()) }
        return jsonArray.toString()
    }

    private fun loadCustomPresets(): List<EqualizerCustomPreset> {
        val saved = prefs.getString(KEY_CUSTOM_PRESETS, null) ?: return DEFAULT_CUSTOM_PRESETS
        return try {
            val jsonArray = JSONArray(saved)
            val list = mutableListOf<EqualizerCustomPreset>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val name = obj.getString("name")
                val bandsArray = obj.getJSONArray("bands")
                val bandValues = mutableListOf<Float>()
                for (j in 0 until bandsArray.length()) {
                    bandValues.add(bandsArray.getDouble(j).toFloat())
                }
                list.add(EqualizerCustomPreset(name, bandValues))
            }
            if (list.isNotEmpty()) list else DEFAULT_CUSTOM_PRESETS
        } catch (_: Exception) {
            DEFAULT_CUSTOM_PRESETS
        }
    }

    private fun serializeCustomPresets(customPresets: List<EqualizerCustomPreset>): String {
        val jsonArray = JSONArray()
        customPresets.forEach { preset ->
            val obj = JSONObject()
            obj.put("name", preset.name)
            val bandsArray = JSONArray()
            preset.bandGains.forEach { bandsArray.put(it.toDouble()) }
            obj.put("bands", bandsArray)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }
}
