package com.example.lyrics

import kotlin.math.max

enum class LyricsSource {
    EMBEDDED_SYLT,
    EMBEDDED_LYRICS,
    SIDECAR_LRC,
    SIDECAR_TEXT
}

data class LyricWord(
    val startMs: Long,
    val text: String
)

data class LyricLine(
    val startMs: Long,
    val text: String,
    val words: List<LyricWord> = emptyList()
)

sealed interface LyricsContent {
    data class Synced(
        val lines: List<LyricLine>
    ) : LyricsContent {
        fun indexAt(positionMs: Long): Int {
            if (lines.isEmpty()) return -1

            val position = max(0L, positionMs)
            var low = 0
            var high = lines.lastIndex
            var result = -1

            while (low <= high) {
                val mid = (low + high) ushr 1
                if (lines[mid].startMs <= position) {
                    result = mid
                    low = mid + 1
                } else {
                    high = mid - 1
                }
            }

            return result
        }
    }

    data class Plain(
        val text: String
    ) : LyricsContent

    data object None : LyricsContent
}

data class LyricsDocument(
    val songId: Long,
    val source: LyricsSource?,
    val content: LyricsContent
)
