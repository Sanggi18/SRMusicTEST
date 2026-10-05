package com.example.ui.common.artwork

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.example.ui.common.artwork.audiocover.AudioFileCover
import com.example.ui.common.artwork.audiocover.AudioFileCoverLoader
import com.example.ui.common.artwork.palette.BitmapPaletteTranscoder
import com.example.ui.common.artwork.palette.BitmapPaletteWrapper
import java.io.InputStream

@GlideModule
class SRMusicGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(
            AudioFileCover::class.java,
            InputStream::class.java,
            AudioFileCoverLoader.Factory()
        )
        registry.register(
            Bitmap::class.java,
            BitmapPaletteWrapper::class.java,
            BitmapPaletteTranscoder()
        )
    }

    override fun isManifestParsingEnabled(): Boolean = false
}
