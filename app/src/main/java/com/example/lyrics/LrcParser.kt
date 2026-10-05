package com.example.lyrics

object LrcParser {

    private val lineTimeTagRegex = Regex(
        """\[(\d{1,3}):([0-5]\d)(?:[\.:](\d{1,3}))?\]"""
    )

    private val offsetRegex = Regex(
        """^\s*\[offset\s*:\s*([+-]?\d+)\]\s*$""",
        RegexOption.IGNORE_CASE
    )

    private val wordTimeTagRegex = Regex(
        """<(\d{1,3}):([0-5]\d)(?:[\.:](\d{1,3}))?>"""
    )

    fun parse(raw: String): LyricsContent {
        val normalized = raw
            .replace("\u0000", "")
            .replace("\r\n", "\n")
            .replace('\r', '\n')

        if (normalized.isBlank()) return LyricsContent.None

        var offsetMs = 0L
        val synced = mutableListOf<LyricLine>()
        val plainLines = mutableListOf<String>()

        for (rawLine in normalized.lineSequence()) {
            val line = rawLine.trimEnd()
            if (line.isBlank()) continue

            val offsetMatch = offsetRegex.matchEntire(line)
            if (offsetMatch != null) {
                offsetMs = offsetMatch.groupValues[1].toLongOrNull() ?: offsetMs
                continue
            }

            val matches = lineTimeTagRegex.findAll(line).toList()
            if (matches.isEmpty()) {
                if (!looksLikeMetadataLine(line)) {
                    plainLines += line
                }
                continue
            }

            val rawLyricText = lineTimeTagRegex.replace(line, "").trim()
            if (rawLyricText.isBlank()) continue

            val words = parseWordTiming(rawLyricText)
            val cleanLyricText = wordTimeTagRegex.replace(rawLyricText, "").trim()
            if (cleanLyricText.isBlank()) continue

            for (match in matches) {
                val timestampMs = parseTimestamp(match)
                val adjusted = (timestampMs + offsetMs).coerceAtLeast(0L)

                val adjustedWords = if (words.isEmpty()) {
                    emptyList()
                } else {
                    words.map { word ->
                        word.copy(
                            startMs = (word.startMs + offsetMs).coerceAtLeast(0L)
                        )
                    }
                }

                synced += LyricLine(
                    startMs = adjusted,
                    text = cleanLyricText,
                    words = adjustedWords
                )
            }
        }

        if (synced.isNotEmpty()) {
            val lines = synced
                .filter { it.text.isNotBlank() }
                .sortedWith(compareBy<LyricLine> { it.startMs }.thenBy { it.text })

            return LyricsContent.Synced(lines)
        }

        val plain = plainLines
            .joinToString("\n")
            .trim()

        return if (plain.isNotBlank()) {
            LyricsContent.Plain(plain)
        } else {
            LyricsContent.None
        }
    }

    private fun parseTimestamp(match: MatchResult): Long {
        val minutes = match.groupValues[1].toLong()
        val seconds = match.groupValues[2].toLong()
        val fraction = match.groupValues[3]

        val fractionMs = when (fraction.length) {
            0 -> 0L
            1 -> fraction.toLong() * 100L
            2 -> fraction.toLong() * 10L
            else -> fraction.take(3).toLong()
        }

        return (minutes * 60_000L) +
            (seconds * 1_000L) +
            fractionMs
    }

    private fun parseWordTiming(text: String): List<LyricWord> {
        val matches = wordTimeTagRegex.findAll(text).toList()
        if (matches.isEmpty()) return emptyList()

        val result = mutableListOf<LyricWord>()

        for (index in matches.indices) {
            val current = matches[index]
            val start = parseWordTimestamp(current)
            val contentStart = current.range.last + 1
            val contentEnd = if (index + 1 < matches.size) {
                matches[index + 1].range.first
            } else {
                text.length
            }

            if (contentStart >= contentEnd) continue

            val word = text.substring(contentStart, contentEnd)
            if (word.isNotBlank()) {
                result += LyricWord(
                    startMs = start.coerceAtLeast(0L),
                    text = word
                )
            }
        }

        return result
    }

    private fun parseWordTimestamp(match: MatchResult): Long {
        return parseTimestamp(match)
    }

    private fun looksLikeMetadataLine(line: String): Boolean {
        val content = line.removePrefix("[").substringBefore("]")
        if (!content.contains(':')) return false

        val key = content.substringBefore(':').trim()
        return key.matches(Regex("[A-Za-z][A-Za-z0-9_-]*"))
    }
}
