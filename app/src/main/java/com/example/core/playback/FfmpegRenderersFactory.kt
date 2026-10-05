package com.example.core.playback

import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.decoder.ffmpeg.FfmpegAudioRenderer
import androidx.media3.decoder.ffmpeg.FfmpegLibrary
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector

@UnstableApi
class FfmpegRenderersFactory(private val context: Context) : DefaultRenderersFactory(context) {

    init {
        setExtensionRendererMode(EXTENSION_RENDERER_MODE_ON)
        setEnableAudioTrackPlaybackParams(true)
    }

    override fun buildAudioRenderers(
        context: Context,
        extensionRendererMode: Int,
        mediaCodecSelector: MediaCodecSelector,
        enableDecoderFallback: Boolean,
        audioSink: AudioSink,
        eventHandler: Handler,
        eventListener: AudioRendererEventListener,
        out: ArrayList<Renderer>
    ) {
        val ffmpegAvailable = FfmpegLibrary.isAvailable()

        if (ffmpegAvailable && (extensionRendererMode == EXTENSION_RENDERER_MODE_PREFER || extensionRendererMode == EXTENSION_RENDERER_MODE_ON)) {
            val extensionRendererIndex = out.size
            try {
                val ffmpegRenderer = FfmpegAudioRenderer(eventHandler, eventListener, audioSink)
                out.add(if (extensionRendererMode == EXTENSION_RENDERER_MODE_PREFER) 0 else extensionRendererIndex, ffmpegRenderer)
            } catch (e: Exception) {
                Log.e("SRMusic:Playback", "FfmpegRenderersFactory: Failed to instantiate FfmpegAudioRenderer", e)
            }
        }

        // Add standard MediaCodecAudioRenderer with decoder fallback enabled
        out.add(
            MediaCodecAudioRenderer(
                context,
                mediaCodecSelector,
                eventHandler,
                eventListener,
                audioSink
            )
        )
    }

    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean
    ): AudioSink {
        return DefaultAudioSink.Builder(context)
            .setEnableFloatOutput(enableFloatOutput)
            .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
            .build()
    }
}
