package com.example.ui.common.artwork
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import com.example.core.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

data class ExtractedArtworkPalette(
    val vibrant: Color?,
    val darkVibrant: Color?,
    val lightVibrant: Color?,
    val muted: Color?,
    val darkMuted: Color?,
    val lightMuted: Color?,
    val dominant: Color?
)

object ArtworkColorHelper {

    private val paletteCache = ConcurrentHashMap<String, ExtractedArtworkPalette>()

    /**
     * Extracts Palette swatches from a song's audio file cover on Dispatchers.IO.
     */
    suspend fun extractPalette(song: Song): ExtractedArtworkPalette? = withContext(Dispatchers.IO) {
        val cacheKey = "${song.dataPath}_${song.dateModified}_${song.albumId}"
        paletteCache[cacheKey]?.let { return@withContext it }

        if (song.dataPath.isBlank()) return@withContext null
        val file = File(song.dataPath)
        if (!file.exists()) return@withContext null

        var bitmap: Bitmap? = null
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(song.dataPath)
            val picture = retriever.embeddedPicture
            if (picture != null && picture.isNotEmpty()) {
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 4 // Downsample for fast Palette generation
                }
                bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.size, options)
            }
        } catch (_: Exception) {
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }

        if (bitmap == null) {
            try {
                val audioFile = AudioFileIO.read(file)
                val artwork = audioFile.tag?.firstArtwork?.binaryData
                if (artwork != null && artwork.isNotEmpty()) {
                    val options = BitmapFactory.Options().apply { inSampleSize = 4 }
                    bitmap = BitmapFactory.decodeByteArray(artwork, 0, artwork.size, options)
                }
            } catch (_: Exception) {}
        }

        val bmp = bitmap ?: return@withContext null
        try {
            val palette = Palette.from(bmp).maximumColorCount(16).generate()
            val extracted = ExtractedArtworkPalette(
                vibrant = palette.vibrantSwatch?.rgb?.let { Color(it) },
                darkVibrant = palette.darkVibrantSwatch?.rgb?.let { Color(it) },
                lightVibrant = palette.lightVibrantSwatch?.rgb?.let { Color(it) },
                muted = palette.mutedSwatch?.rgb?.let { Color(it) },
                darkMuted = palette.darkMutedSwatch?.rgb?.let { Color(it) },
                lightMuted = palette.lightMutedSwatch?.rgb?.let { Color(it) },
                dominant = palette.dominantSwatch?.rgb?.let { Color(it) }
            )
            paletteCache[cacheKey] = extracted
            extracted
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 1. For You: single dynamic accent color
     */
    fun getForYouAccentColor(queue: List<Song>): Color {
        val s = queue.firstOrNull()
        if (s != null) {
            val cacheKey = "${s.dataPath}_${s.dateModified}_${s.albumId}"
            paletteCache[cacheKey]?.let { p ->
                val c = p.vibrant ?: p.dominant ?: p.muted
                if (c != null) return c
            }
        }
        val sKey = "${s?.albumId}_${s?.title}"
        val hash = abs(sKey.hashCode())
        val hue = (hash % 360).toFloat()
        return Color.hsl(hue, 0.65f, 0.45f)
    }

    @Composable
    fun rememberForYouAccentColor(queue: List<Song>): Color {
        var accent by remember(queue) { mutableStateOf(getForYouAccentColor(queue)) }
        LaunchedEffect(queue) {
            val s1 = queue.firstOrNull()
            if (s1 != null) {
                extractPalette(s1)?.let {
                    accent = getForYouAccentColor(queue)
                }
            }
        }
        return accent
    }

    /**
     * 2. Genre Today: single dynamic accent color (light/pastel accent)
     */
    fun getGenreTodayAccentColor(song: Song?, genreName: String): Color {
        if (song != null) {
            val cacheKey = "${song.dataPath}_${song.dateModified}_${song.albumId}"
            paletteCache[cacheKey]?.let { p ->
                val c = p.lightVibrant ?: p.lightMuted ?: p.vibrant?.let { lightenColor(it, 0.82f) } ?: p.dominant?.let { lightenColor(it, 0.80f) }
                if (c != null) return c
            }
        }
        val key = "${song?.albumId}_${genreName}_${song?.artist}"
        val hash = abs(key.hashCode())
        val hue = (hash % 360).toFloat()
        return Color.hsl(hue, 0.65f, 0.80f)
    }

    @Composable
    fun rememberGenreTodayAccentColor(song: Song?, genreName: String): Color {
        var accent by remember(song, genreName) { mutableStateOf(getGenreTodayAccentColor(song, genreName)) }
        LaunchedEffect(song, genreName) {
            if (song != null) {
                extractPalette(song)?.let {
                    accent = getGenreTodayAccentColor(song, genreName)
                }
            }
        }
        return accent
    }

    private fun isMonochrome(color: Color): Boolean {
        val max = maxOf(color.red, color.green, color.blue)
        val min = minOf(color.red, color.green, color.blue)
        return (max - min) < 0.10f
    }

    /**
     * 3. Terbaru: single dynamic accent color from newest songs' artwork
     */
    fun getTerbaruAccentColor(songs: List<Song>): Color {
        val firstSong = songs.firstOrNull()
        if (firstSong != null) {
            val cacheKey = "${firstSong.dataPath}_${firstSong.dateModified}_${firstSong.albumId}"
            paletteCache[cacheKey]?.let { p ->
                val c = p.vibrant ?: p.darkVibrant ?: p.lightVibrant ?: p.dominant ?: p.muted
                if (c != null) return c
            }
        }
        for (song in songs.take(4)) {
            val cacheKey = "${song.dataPath}_${song.dateModified}_${song.albumId}"
            paletteCache[cacheKey]?.let { p ->
                val c = p.vibrant ?: p.darkVibrant ?: p.lightVibrant ?: p.dominant ?: p.muted
                if (c != null && !isMonochrome(c)) return c
            }
        }
        val newestSong = songs.firstOrNull() ?: return Color(0xFF38B2A6)
        val key = "Terbaru_${newestSong.id}_${newestSong.title}_${newestSong.dateModified}"
        val hash = abs(key.hashCode())
        val hue = (hash % 360).toFloat()
        return Color.hsl(hue, 0.65f, 0.42f)
    }

    fun getTerbaruAccentColor(newestSong: Song?): Color {
        return if (newestSong != null) getTerbaruAccentColor(listOf(newestSong)) else Color(0xFF38B2A6)
    }

    @Composable
    fun rememberTerbaruAccentColor(songs: List<Song>): Color {
        val topSongs = remember(songs) { songs.take(4) }
        val songKeys = topSongs.map { "${it.id}_${it.dateModified}" }
        var accent by remember(songKeys) {
            mutableStateOf(getTerbaruAccentColor(topSongs))
        }
        LaunchedEffect(songKeys) {
            for (s in topSongs) {
                extractPalette(s)
            }
            accent = getTerbaruAccentColor(topSongs)
        }
        return accent
    }

    /**
     * 4. Billboard: single dynamic accent color from top ranked item
     */
    fun getBillboardAccentColor(categoryTitle: String, topItemName: String, artworkSong: Song? = null): Color {
        if (artworkSong != null) {
            val cacheKey = "${artworkSong.dataPath}_${artworkSong.dateModified}_${artworkSong.albumId}"
            paletteCache[cacheKey]?.let { p ->
                val c = p.vibrant ?: p.dominant ?: p.muted
                if (c != null) return c
            }
        }
        val key = "Billboard_${categoryTitle}_$topItemName"
        val hash = abs(key.hashCode())
        val hue = (hash % 360).toFloat()
        return Color.hsl(hue, 0.68f, 0.42f)
    }

    @Composable
    fun rememberBillboardAccentColor(categoryTitle: String, topItemName: String, artworkSong: Song? = null): Color {
        var accent by remember(categoryTitle, topItemName, artworkSong?.id, artworkSong?.dateModified) {
            mutableStateOf(getBillboardAccentColor(categoryTitle, topItemName, artworkSong))
        }
        LaunchedEffect(categoryTitle, topItemName, artworkSong?.id, artworkSong?.dateModified) {
            if (artworkSong != null) {
                extractPalette(artworkSong)?.let {
                    accent = getBillboardAccentColor(categoryTitle, topItemName, artworkSong)
                }
            }
        }
        return accent
    }

    private fun lightenColor(color: Color, targetLightness: Float = 0.82f): Color {
        val r = color.red
        val g = color.green
        val b = color.blue
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min

        var h = 0f
        if (delta != 0f) {
            h = when (max) {
                r -> ((g - b) / delta) % 6f
                g -> ((b - r) / delta) + 2f
                else -> ((r - g) / delta) + 4f
            } * 60f
            if (h < 0) h += 360f
        }

        val sat = if (max == 0f || min == 1f) 0.5f else (delta / (1f - abs(max + min - 1f))).coerceIn(0.4f, 0.75f)
        return Color.hsl(h, sat, targetLightness.coerceIn(0.75f, 0.88f))
    }
}
