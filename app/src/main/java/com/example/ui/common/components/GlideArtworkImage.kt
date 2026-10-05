package com.example.ui.common.components

import android.content.ContentUris
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.MediaStoreSignature
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.example.ui.common.artwork.audiocover.AudioFileCover

@Composable
fun GlideArtworkImage(
    filePath: String,
    albumId: Long,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    targetSizePx: Int = 400,
    dateModified: Long = 0L,
    artworkUri: Uri? = null,
    topCrop: Boolean = false,
    grayscale: Boolean = false,
    tintColor: Int? = null,
    onLoadingStateChange: ((Boolean) -> Unit)? = null
) {
    val albumArtUri = if (albumId != -1L) {
        ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
    } else artworkUri

    val safeTargetSize = targetSizePx.coerceIn(64, 1000)

    AndroidView<ImageView>(
        modifier = modifier,
        factory = { ctx ->
            if (topCrop) {
                object : ImageView(ctx) {
                    init {
                        scaleType = ScaleType.MATRIX
                    }

                    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
                        val changed = super.setFrame(l, t, r, b)
                        recomputeTopCropMatrix()
                        return changed
                    }

                    override fun setImageDrawable(drawable: Drawable?) {
                        super.setImageDrawable(drawable)
                        recomputeTopCropMatrix()
                    }

                    private fun recomputeTopCropMatrix() {
                        val d = drawable ?: return
                        val dWidth = d.intrinsicWidth
                        val dHeight = d.intrinsicHeight
                        val vWidth = width - paddingLeft - paddingRight
                        val vHeight = height - paddingTop - paddingBottom
                        if (dWidth <= 0 || dHeight <= 0 || vWidth <= 0 || vHeight <= 0) return

                        val scale = maxOf(vWidth.toFloat() / dWidth, vHeight.toFloat() / dHeight)
                        val matrix = Matrix()
                        matrix.setScale(scale, scale)
                        // Center horizontally
                        val dx = (vWidth - dWidth * scale) * 0.5f
                        // Align to top vertically so top of artwork is completely visible
                        val dy = 0f
                        matrix.postTranslate(dx, dy)
                        imageMatrix = matrix
                    }
                }
            } else {
                ImageView(ctx).apply {
                    scaleType = ScaleType.CENTER_CROP
                }
            }
        },
        update = { imageView ->
            if (grayscale) {
                val matrix = ColorMatrix()
                matrix.setSaturation(0f)
                imageView.colorFilter = ColorMatrixColorFilter(matrix)
            } else if (tintColor != null) {
                imageView.colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP)
            } else {
                imageView.colorFilter = null
            }

            val hasFilePath = filePath.isNotBlank()
            val primaryModel = if (hasFilePath) AudioFileCover(filePath) else albumArtUri
            val requestManager = Glide.with(imageView)

            val currentTag = "$filePath:$albumId:$dateModified:$safeTargetSize"
            val lastTag = imageView.tag as? String
            if (lastTag == currentTag) {
                // Artwork is already requested/bound for this exact image and size
                return@AndroidView
            }
            if (lastTag != null) {
                requestManager.clear(imageView)
            }
            imageView.tag = currentTag
            onLoadingStateChange?.invoke(true)

            val fallbackRequest = if (albumArtUri != null && hasFilePath) {
                requestManager
                    .load(albumArtUri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(safeTargetSize, safeTargetSize)
            } else null

            val builder = requestManager
                .load(primaryModel)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(safeTargetSize, safeTargetSize)
                .signature(MediaStoreSignature("image/jpeg", dateModified, 0))

            if (onLoadingStateChange != null) {
                builder.listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        onLoadingStateChange(false)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        onLoadingStateChange(false)
                        return false
                    }
                })
            }

            if (fallbackRequest != null) {
                builder.error(fallbackRequest).into(imageView)
            } else if (albumArtUri != null && primaryModel != albumArtUri) {
                builder.error(albumArtUri).into(imageView)
            } else {
                builder.into(imageView)
            }
        }
    )
}
