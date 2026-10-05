package com.example.core.util

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.example.core.model.Song
import com.example.ui.common.artwork.audiocover.AudioFileCover
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import java.io.File
import java.util.concurrent.ConcurrentHashMap

data class ArtworkColors(
    val primary: Color,
    val secondary: Color,
    val darkMuted: Color,
    val lightSurface: Color,
    val darkSurface: Color,
    val activeProgress: Color,
    val inactiveProgress: Color,
    val controlButton: Color,
    val onControlButton: Color,
    val controlIcon: Color
) {
    fun getSurface(isDark: Boolean): Color = if (isDark) darkSurface else lightSurface
}

object ArtworkPaletteExtractor {
    private data class CacheKey(val songId: Long, val isDark: Boolean)
    private val colorCache = ConcurrentHashMap<CacheKey, ArtworkColors>()

    fun defaultColors(
        primary: Color = Color(0xFF2196F3),
        darkBg: Color = Color(0xFF121214),
        lightBg: Color = Color(0xFFF8F9FC),
        isDark: Boolean = false
    ): ArtworkColors {
        val surface = if (isDark) darkBg else lightBg
        val onSurface = if (isDark) Color(0xFFF0F0F5) else Color(0xFF14161C)
        return ArtworkColors(
            primary = primary,
            secondary = primary,
            darkMuted = darkBg,
            lightSurface = lightBg,
            darkSurface = darkBg,
            activeProgress = primary,
            inactiveProgress = if (isDark) Color.White.copy(alpha = 0.24f) else Color.Black.copy(alpha = 0.16f),
            controlButton = primary,
            onControlButton = Color.White,
            controlIcon = onSurface
        )
    }

    fun normalizeSafeAccentColor(rgb: Int, isDarkTheme: Boolean): Color {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(rgb, hsl)
        val hue = hsl[0]
        var sat = hsl[1]
        var light = hsl[2]

        // Achromatic / nearly grayscale (e.g. pure black, dark grey, pure white album covers)
        if (sat < 0.15f || (isDarkTheme && light < 0.18f) || (!isDarkTheme && light > 0.85f)) {
            return if (isDarkTheme) {
                // Soft non-flashy pastel light blue with crisp contrast on dark canvas
                Color(0xFF64B5F6)
            } else {
                // Rich, deep readable blue with crisp contrast on light canvas
                Color(0xFF1E6EB8)
            }
        }

        if (isDarkTheme) {
            // Guarantee luminous readable primary against dark surfaces (never black or invisible)
            light = light.coerceIn(0.62f, 0.82f)
            sat = sat.coerceAtLeast(0.50f)
        } else {
            // Guarantee deep readable primary against light surfaces (never white or washed out)
            light = light.coerceIn(0.26f, 0.42f)
            sat = sat.coerceAtLeast(0.60f)
        }

        val safeRgb = ColorUtils.HSLToColor(floatArrayOf(hue, sat, light))
        return Color(safeRgb)
    }

    suspend fun extractColors(
        context: Context,
        song: Song?,
        isDarkTheme: Boolean = false
    ): ArtworkColors = withContext(Dispatchers.IO) {
        if (song == null) {
            return@withContext defaultColors(isDark = isDarkTheme)
        }

        val cacheKey = CacheKey(song.id, isDarkTheme)
        colorCache[cacheKey]?.let { return@withContext it }

        val bitmap = extractBitmap(context, song)

        if (bitmap == null) {
            return@withContext defaultColors(isDark = isDarkTheme)
        }

        val palette = try {
            Palette.from(bitmap).generate()
        } catch (_: Exception) {
            null
        }

        val dominant = palette?.dominantSwatch
        val vibrant = palette?.vibrantSwatch ?: palette?.lightVibrantSwatch ?: dominant
        val darkMuted = palette?.darkMutedSwatch ?: palette?.darkVibrantSwatch ?: dominant

        val colors = if (vibrant != null || dominant != null) {
            val rawPrimaryRgb = vibrant?.rgb ?: dominant!!.rgb
            val darkRgb = darkMuted?.rgb ?: 0xFF121212.toInt()

            // Safe normalized primary with guaranteed contrast in dark and light themes
            val safePrimaryColor = normalizeSafeAccentColor(rawPrimaryRgb, isDarkTheme)
            val primaryRgb = safePrimaryColor.toArgb()

            // Calculate AyraMusic clean pastel surface for light theme & rich surface for dark theme
            val lightSurface = calculateTintedSurface(primaryRgb, isDark = false)
            val darkSurface = calculateTintedSurface(primaryRgb, isDark = true)

            // Dynamic progress & control colors (Ayra style logic)
            val hsl = FloatArray(3)
            ColorUtils.colorToHSL(primaryRgb, hsl)
            val hue = hsl[0]

            // Active progress color with guaranteed saturation and readable lightness
            val activeProgressLightness = if (isDarkTheme) 0.68f else 0.38f
            val activeProgressRgb = ColorUtils.HSLToColor(floatArrayOf(hue, 0.85f, activeProgressLightness))
            val activeProgressColor = Color(activeProgressRgb)

            // Inactive progress track color with guaranteed visible contrast against light & dark canvases
            val inactiveProgressColor = if (isDarkTheme) {
                Color.White.copy(alpha = 0.24f)
            } else {
                Color.Black.copy(alpha = 0.16f)
            }

            // Control button color (Play / Pause pill/circle)
            val buttonLightness = if (isDarkTheme) 0.60f else 0.40f
            val buttonRgb = ColorUtils.HSLToColor(floatArrayOf(hue, 0.70f, buttonLightness))
            val controlButtonColor = Color(buttonRgb)

            val onControlButtonColor = if (buttonLightness > 0.50f && !isDarkTheme) Color.Black else Color.White

            // Control icons
            val iconLightness = if (isDarkTheme) 0.95f else 0.12f
            val iconRgb = ColorUtils.HSLToColor(floatArrayOf(hue, 0.20f, iconLightness))
            val controlIconColor = Color(iconRgb)

            ArtworkColors(
                primary = safePrimaryColor,
                secondary = safePrimaryColor,
                darkMuted = Color(darkRgb),
                lightSurface = lightSurface,
                darkSurface = darkSurface,
                activeProgress = activeProgressColor,
                inactiveProgress = inactiveProgressColor,
                controlButton = controlButtonColor,
                onControlButton = onControlButtonColor,
                controlIcon = controlIconColor
            )
        } else {
            defaultColors(isDark = isDarkTheme)
        }

        colorCache[cacheKey] = colors
        colors
    }

    private fun calculateTintedSurface(rgb: Int, isDark: Boolean): Color {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(rgb, hsl)
        val hue = hsl[0]
        val saturation = hsl[1].coerceIn(0.12f, 0.38f)
        val lightness = if (isDark) 0.11f else 0.94f
        val resultInt = ColorUtils.HSLToColor(floatArrayOf(hue, saturation, lightness))
        return Color(resultInt)
    }

    private fun extractBitmap(context: Context, song: Song): Bitmap? {
        // 1. Try MediaMetadataRetriever directly from filePath
        if (song.dataPath.isNotBlank()) {
            val file = File(song.dataPath)
            if (file.exists()) {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(song.dataPath)
                    val picture = retriever.embeddedPicture
                    if (picture != null && picture.isNotEmpty()) {
                        val opts = BitmapFactory.Options().apply {
                            inSampleSize = 2
                        }
                        val bmp = BitmapFactory.decodeByteArray(picture, 0, picture.size, opts)
                        if (bmp != null) return bmp
                    }
                } catch (_: Exception) {
                } finally {
                    try { retriever.release() } catch (_: Exception) {}
                }

                // 2. Try Jaudiotagger
                try {
                    val audioFile = AudioFileIO.read(file)
                    val artworkData = audioFile.tag?.firstArtwork?.binaryData
                    if (artworkData != null && artworkData.isNotEmpty()) {
                        val opts = BitmapFactory.Options().apply {
                            inSampleSize = 2
                        }
                        val bmp = BitmapFactory.decodeByteArray(artworkData, 0, artworkData.size, opts)
                        if (bmp != null) return bmp
                    }
                } catch (_: Exception) {}
            }
        }

        // 3. Try ContentResolver via MediaStore Album Art URI
        if (song.albumId != -1L) {
            try {
                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    song.albumId
                )
                context.contentResolver.openInputStream(albumArtUri)?.use { stream ->
                    val opts = BitmapFactory.Options().apply {
                        inSampleSize = 2
                    }
                    val bmp = BitmapFactory.decodeStream(stream, null, opts)
                    if (bmp != null) return bmp
                }
            } catch (_: Exception) {}
        }

        // 4. Try Glide with AudioFileCover
        if (song.dataPath.isNotBlank()) {
            try {
                val bmp = Glide.with(context.applicationContext)
                    .asBitmap()
                    .load(AudioFileCover(song.dataPath))
                    .submit(120, 120)
                    .get()
                if (bmp != null) return bmp
            } catch (_: Exception) {}
        }

        // 5. Try song.contentUri with MediaMetadataRetriever
        if (song.contentUri != Uri.EMPTY) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, song.contentUri)
                val picture = retriever.embeddedPicture
                if (picture != null && picture.isNotEmpty()) {
                    val opts = BitmapFactory.Options().apply {
                        inSampleSize = 2
                    }
                    val bmp = BitmapFactory.decodeByteArray(picture, 0, picture.size, opts)
                    if (bmp != null) return bmp
                }
            } catch (_: Exception) {
            } finally {
                try { retriever.release() } catch (_: Exception) {}
            }
        }

        return null
    }

    fun clearCache() {
        colorCache.clear()
    }
}
