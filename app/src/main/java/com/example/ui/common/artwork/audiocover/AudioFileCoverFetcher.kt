package com.example.ui.common.artwork.audiocover

import android.media.MediaMetadataRetriever
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import org.jaudiotagger.audio.AudioFileIO
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

class AudioFileCoverFetcher(private val model: AudioFileCover) : DataFetcher<InputStream> {
    private var stream: InputStream? = null

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        if (model.filePath.isBlank()) {
            callback.onLoadFailed(FileNotFoundException("Empty file path"))
            return
        }

        val file = File(model.filePath)
        if (!file.exists()) {
            callback.onLoadFailed(FileNotFoundException("File does not exist: ${model.filePath}"))
            return
        }

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(model.filePath)
            val picture = retriever.embeddedPicture
            if (picture != null && picture.isNotEmpty()) {
                stream = ByteArrayInputStream(picture)
                callback.onDataReady(stream)
                return
            }
        } catch (_: Exception) {
            // Fallback ke jaudiotagger jika format native gagal
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }

        try {
            val audioFile = AudioFileIO.read(file)
            val artwork = audioFile.tag?.firstArtwork?.binaryData
            if (artwork != null && artwork.isNotEmpty()) {
                stream = ByteArrayInputStream(artwork)
                callback.onDataReady(stream)
                return
            }
        } catch (_: Exception) {}

        callback.onLoadFailed(FileNotFoundException("No embedded artwork found for ${model.filePath}"))
    }

    override fun cleanup() {
        try { stream?.close() } catch (_: Exception) {}
    }

    override fun cancel() {}
    override fun getDataClass(): Class<InputStream> = InputStream::class.java
    override fun getDataSource(): DataSource = DataSource.LOCAL
}
