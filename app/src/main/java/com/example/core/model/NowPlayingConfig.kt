package com.example.core.model

enum class NowPlayingProgressStyle(val label: String) {
    LINE_THUMB("Line & Thumb"),
    CAPSULE_PILL("Capsule Pill"),
    ROUNDED_BAR("Rounded Bar"),
    WAVE_MINIMAL("Wave Minimal"),
    EXPRESSIVE_WAVE("Expressive Wave"),
    MATERIAL_3("Material 3"),
    WAVEFORM("Waveform")
}

enum class NowPlayingTheme(val label: String) {
    DEFAULT("Default"),
    COLOR("Color"),
    BLUR("Blur"),
    BLUR_2("Blur 2")
}

enum class TrackInfoAlignment(val label: String) {
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right")
}
