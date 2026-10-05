package com.example.core.util

import com.example.core.model.Song
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeUtils {
    private val MONTH_NAMES_FULL = arrayOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    private val MONTH_NAMES_SHORT = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
        "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
    )

    fun formatDuration(millis: Long): String {
        if (millis <= 0L) return "00:00"
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        return if (unitIndex == 0) {
            "${bytes} B"
        } else {
            String.format(Locale.getDefault(), "%.1f %s", size, units[unitIndex])
        }
    }

    /**
     * Formats date string (e.g. "2022-08-22", "2022-08-16 08:33:12 UTC", "2026/02/12", "2024", "2023-05")
     * into readable Indonesian date format: "22 Agu", "12 Feb", "1 Mar", "Mei 2023", or "2024".
     */
    fun formatReleaseDateTag(rawTag: String?, shortMonth: Boolean = true): String? {
        if (rawTag.isNullOrBlank()) return null
        val trimmed = rawTag.trim()

        // 1. Match YYYY-MM-DD or YYYY-MM-DDTHH:mm:ss... or YYYY/MM/DD
        val isoRegex = Regex("""^(\d{4})[-/.](0?[1-9]|1[0-2])[-/.](0?[1-9]|[12]\d|3[01])""")
        val isoMatch = isoRegex.find(trimmed)
        if (isoMatch != null) {
            val month = isoMatch.groupValues[2].toIntOrNull() ?: return null
            val day = isoMatch.groupValues[3].toIntOrNull() ?: return null
            val monthName = getIndonesianMonth(month, shortMonth)
            return "$day $monthName"
        }

        // 2. Match DD-MM-YYYY or DD/MM/YYYY
        val dmyRegex = Regex("""^(0?[1-9]|[12]\d|3[01])[-/.](0?[1-9]|1[0-2])[-/.](\d{4})""")
        val dmyMatch = dmyRegex.find(trimmed)
        if (dmyMatch != null) {
            val day = dmyMatch.groupValues[1].toIntOrNull() ?: return null
            val month = dmyMatch.groupValues[2].toIntOrNull() ?: return null
            val monthName = getIndonesianMonth(month, shortMonth)
            return "$day $monthName"
        }

        // 3. Match YYYY-MM or YYYY/MM
        val ymRegex = Regex("""^(\d{4})[-/.](0?[1-9]|1[0-2])$""")
        val ymMatch = ymRegex.find(trimmed)
        if (ymMatch != null) {
            val year = ymMatch.groupValues[1]
            val month = ymMatch.groupValues[2].toIntOrNull() ?: return null
            val monthName = getIndonesianMonth(month, shortMonth)
            return "$monthName $year"
        }

        // 4. Match 4-digit Year (e.g. "2024", "2019", "1998")
        val yearRegex = Regex("""\b(19\d{2}|20\d{2})\b""")
        val yearMatch = yearRegex.find(trimmed)
        if (yearMatch != null) {
            return yearMatch.value
        }

        return null
    }

    fun getIndonesianMonth(month1to12: Int, shortMonth: Boolean = true): String {
        val index = (month1to12 - 1).coerceIn(0, 11)
        return if (shortMonth) MONTH_NAMES_SHORT[index] else MONTH_NAMES_FULL[index]
    }

    /**
     * Fallback to timestamp if raw tag is missing.
     */
    fun formatTimestampFallback(song: Song, shortMonth: Boolean = true): String {
        val timestamp = when {
            song.dateAdded > 0L -> song.dateAdded
            song.dateModified > 0L -> song.dateModified
            else -> 0L
        }

        if (timestamp > 0L) {
            val millis = if (timestamp < 100_000_000_000L) timestamp * 1000L else timestamp
            val cal = Calendar.getInstance().apply { timeInMillis = millis }
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH) + 1
            val monthName = getIndonesianMonth(month, shortMonth)
            return "$day $monthName"
        }

        return ""
    }

    /**
     * Formats song release date into 3-letter month format without year: "12 Feb", "22 Agu", "1 Mar", or year "2024".
     */
    fun formatSongReleaseDate(song: Song, shortMonth: Boolean = true): String {
        // 1. Try file metadata tag first
        val tagDate = com.example.core.data.metadata.MetadataExtractor.extractReleaseDateTag(
            com.example.SRMusicApp.instance,
            song
        )
        if (tagDate.isNotBlank()) {
            return tagDate
        }

        // 2. Try MediaStore year if valid
        if (song.year in 1900..2100) {
            return song.year.toString()
        }

        // 3. Fallback to timestamp if available
        val fallback = formatTimestampFallback(song, shortMonth)
        if (fallback.isNotBlank()) {
            return fallback
        }

        return ""
    }
}
