package com.example.core.model

data class LibraryScanProgress(
    val isScanning: Boolean = false,
    val currentCount: Int = 0,
    val isFinished: Boolean = false,
    val title: String = "Scanning Media Library",
    val description: String = "Detecting audio tracks..."
)
