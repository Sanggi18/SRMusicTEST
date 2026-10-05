package com.example.core.util

object ArtistHelper {
    // Delimiters for multiple artists: feat., ft., &, ,, vs., x, /, ;
    private val ARTIST_DELIMITER_REGEX = Regex("(?i)\\s+(?:feat\\.?|ft\\.?|featuring|&|vs\\.?|x|/)\\s+|,\\s*|;\\s*")

    /**
     * Extracts the primary / main artist name from composite artist strings.
     * Examples:
     * - "21 Savage & Metro Boomin" -> "21 Savage"
     * - "21 Savage feat. J Cole" -> "21 Savage"
     * - "21 Savage, Kendrick Lamar" -> "21 Savage"
     */
    fun extractMainArtist(rawArtist: String?): String {
        if (rawArtist.isNullOrBlank()) return "Unknown Artist"
        val trimmed = rawArtist.trim()
        if (trimmed.equals("<unknown>", ignoreCase = true) || trimmed.equals("unknown artist", ignoreCase = true)) {
            return "Unknown Artist"
        }
        val parts = trimmed.split(ARTIST_DELIMITER_REGEX)
        val first = parts.firstOrNull { it.isNotBlank() }?.trim() ?: trimmed
        return if (first.isNotBlank()) first else "Unknown Artist"
    }
}
