package com.example.ui.common.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun InteractiveImageCropperDialog(
    uri: Uri,
    isAvatar: Boolean,
    targetFile: File,
    onDismiss: () -> Unit,
    onCropSuccess: (File) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }

    var zoom by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Load bitmap in background
    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            try {
                val bmp = decodeSampledBitmapFromUri(context, uri, 1800, 1800)
                loadedBitmap = bmp
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (!isProcessing) onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isProcessing,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAvatar) "Sesuaikan & Crop Avatar" else "Sesuaikan & Crop Banner",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = { if (!isProcessing) onDismiss() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Crop Viewport Box
                var savedVpW by remember { mutableFloatStateOf(0f) }
                var savedVpH by remember { mutableFloatStateOf(0f) }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF121212))
                        .clipToBounds(),
                    contentAlignment = Alignment.Center
                ) {
                    val boxWidthPx = constraints.maxWidth.toFloat()
                    val boxHeightPx = constraints.maxHeight.toFloat()

                    // Strict viewport dimension calculation
                    val viewportWidthPx: Float
                    val viewportHeightPx: Float
                    if (isAvatar) {
                        val diameter = min(boxWidthPx, boxHeightPx) * 0.78f
                        viewportWidthPx = diameter
                        viewportHeightPx = diameter
                    } else {
                        val targetW = boxWidthPx * 0.90f
                        viewportWidthPx = targetW
                        viewportHeightPx = targetW * 0.50f // 2:1 aspect ratio
                    }
                    savedVpW = viewportWidthPx
                    savedVpH = viewportHeightPx

                    if (isLoading || loadedBitmap == null) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    } else {
                        val bmp = loadedBitmap!!
                        val bmpW = bmp.width.toFloat()
                        val bmpH = bmp.height.toFloat()

                        // Strict base scale: image MUST completely cover the viewport
                        val baseScale = max(viewportWidthPx / bmpW, viewportHeightPx / bmpH)

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(bmp, baseScale) {
                                    detectTransformGestures { _, pan, zoomFactor, _ ->
                                        val newZoom = (zoom * zoomFactor).coerceIn(1.0f, 3.5f)
                                        zoom = newZoom

                                        val curScale = baseScale * newZoom
                                        val curRenderedW = bmpW * curScale
                                        val curRenderedH = bmpH * curScale

                                        val maxX = max(0f, (curRenderedW - viewportWidthPx) / 2f)
                                        val maxY = max(0f, (curRenderedH - viewportHeightPx) / 2f)

                                        val nextPan = panOffset + pan
                                        panOffset = Offset(
                                            x = nextPan.x.coerceIn(-maxX, maxX),
                                            y = nextPan.y.coerceIn(-maxY, maxY)
                                        )
                                    }
                                }
                        ) {
                            val canvasW = size.width
                            val canvasH = size.height
                            val cX = canvasW / 2f
                            val cY = canvasH / 2f

                            val curScale = baseScale * zoom
                            val curRenderedW = bmpW * curScale
                            val curRenderedH = bmpH * curScale

                            // Strict clamping ensures circle/rectangle NEVER goes outside image
                            val maxX = max(0f, (curRenderedW - viewportWidthPx) / 2f)
                            val maxY = max(0f, (curRenderedH - viewportHeightPx) / 2f)
                            val safePanX = panOffset.x.coerceIn(-maxX, maxX)
                            val safePanY = panOffset.y.coerceIn(-maxY, maxY)

                            // 1. Draw image bitmap
                            val dstLeft = (cX + safePanX - curRenderedW / 2f).roundToInt()
                            val dstTop = (cY + safePanY - curRenderedH / 2f).roundToInt()
                            val dstW = curRenderedW.roundToInt()
                            val dstH = curRenderedH.roundToInt()

                            drawImage(
                                image = bmp.asImageBitmap(),
                                dstOffset = IntOffset(dstLeft, dstTop),
                                dstSize = IntSize(dstW, dstH)
                            )

                            // 2. Draw overlay mask with cutout hole
                            val maskPath = Path().apply {
                                addRect(Rect(0f, 0f, canvasW, canvasH))
                                if (isAvatar) {
                                    val radius = viewportWidthPx / 2f
                                    addOval(Rect(cX - radius, cY - radius, cX + radius, cY + radius))
                                } else {
                                    val halfW = viewportWidthPx / 2f
                                    val halfH = viewportHeightPx / 2f
                                    addRoundRect(
                                        RoundRect(
                                            cX - halfW,
                                            cY - halfH,
                                            cX + halfW,
                                            cY + halfH,
                                            CornerRadius(14.dp.toPx(), 14.dp.toPx())
                                        )
                                    )
                                }
                                fillType = PathFillType.EvenOdd
                            }
                            drawPath(maskPath, color = Color.Black.copy(alpha = 0.70f))

                            // 3. Draw guideline border
                            val strokeW = 2.dp.toPx()
                            val guideColor = Color.White.copy(alpha = 0.90f)
                            if (isAvatar) {
                                drawCircle(
                                    color = guideColor,
                                    radius = viewportWidthPx / 2f,
                                    center = Offset(cX, cY),
                                    style = Stroke(width = strokeW)
                                )
                            } else {
                                val halfW = viewportWidthPx / 2f
                                val halfH = viewportHeightPx / 2f
                                drawRoundRect(
                                    color = guideColor,
                                    topLeft = Offset(cX - halfW, cY - halfH),
                                    size = Size(viewportWidthPx, viewportHeightPx),
                                    cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                                    style = Stroke(width = strokeW)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Gesture hint
                Text(
                    text = "Geser gambar untuk atur posisi • Gunakan slider atau cubit untuk zoom",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Zoom Slider
                val bmp = loadedBitmap
                val baseScale = if (bmp != null && savedVpW > 0f && savedVpH > 0f) {
                    max(savedVpW / bmp.width.toFloat(), savedVpH / bmp.height.toFloat())
                } else 1.0f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Zoom",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(42.dp)
                    )
                    Slider(
                        value = zoom,
                        onValueChange = { newZoom ->
                            zoom = newZoom
                            if (bmp != null && savedVpW > 0f) {
                                val curScale = baseScale * newZoom
                                val maxX = max(0f, (bmp.width.toFloat() * curScale - savedVpW) / 2f)
                                val maxY = max(0f, (bmp.height.toFloat() * curScale - savedVpH) / 2f)
                                panOffset = Offset(
                                    x = panOffset.x.coerceIn(-maxX, maxX),
                                    y = panOffset.y.coerceIn(-maxY, maxY)
                                )
                            }
                        },
                        valueRange = 1.0f..3.5f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${(zoom * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Control Toolbar: Rotate & Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                val current = loadedBitmap
                                if (current != null) {
                                    val matrix = Matrix().apply { postRotate(90f) }
                                    val rotated = Bitmap.createBitmap(
                                        current, 0, 0, current.width, current.height, matrix, true
                                    )
                                    loadedBitmap = rotated
                                    zoom = 1.0f
                                    panOffset = Offset.Zero
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Putar 90 Derajat",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Putar 90°",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                zoom = 1.0f
                                panOffset = Offset.Zero
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.RestartAlt,
                                contentDescription = "Reset Posisi",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Reset",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { if (!isProcessing) onDismiss() },
                        enabled = !isProcessing
                    ) {
                        Text("Batal")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isProcessing) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isProcessing && loadedBitmap != null) {
                                isProcessing = true
                                scope.launch(Dispatchers.IO) {
                                    val currentBmp = loadedBitmap
                                    if (currentBmp != null) {
                                        val success = executeInteractiveCrop(
                                            sourceBitmap = currentBmp,
                                            isAvatar = isAvatar,
                                            zoom = zoom,
                                            panOffset = panOffset,
                                            viewportWidthPx = savedVpW,
                                            viewportHeightPx = savedVpH,
                                            targetFile = targetFile
                                        )
                                        withContext(Dispatchers.Main) {
                                            isProcessing = false
                                            if (success) {
                                                onCropSuccess(targetFile)
                                            } else {
                                                Toast.makeText(context, "Gagal memproses crop gambar", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            isProcessing = false
                                        }
                                    }
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Terapkan",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun decodeSampledBitmapFromUri(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
    return try {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
        var inSampleSize = 1
        val height = options.outHeight
        val width = options.outWidth
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOptions)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun executeInteractiveCrop(
    sourceBitmap: Bitmap,
    isAvatar: Boolean,
    zoom: Float,
    panOffset: Offset,
    viewportWidthPx: Float,
    viewportHeightPx: Float,
    targetFile: File
): Boolean {
    return try {
        val bw = sourceBitmap.width.toFloat()
        val bh = sourceBitmap.height.toFloat()

        val vpW = if (viewportWidthPx > 0f) viewportWidthPx else (if (isAvatar) 220f else 260f)
        val vpH = if (viewportHeightPx > 0f) viewportHeightPx else (if (isAvatar) 220f else 130f)

        val baseScale = max(vpW / bw, vpH / bh)
        val curScale = baseScale * zoom.coerceIn(1.0f, 3.5f)

        val renderedW = bw * curScale
        val renderedH = bh * curScale

        val maxX = max(0f, (renderedW - vpW) / 2f)
        val maxY = max(0f, (renderedH - vpH) / 2f)
        val safePanX = panOffset.x.coerceIn(-maxX, maxX)
        val safePanY = panOffset.y.coerceIn(-maxY, maxY)

        // Strict mapping from viewport center to bitmap pixels
        val bmpCropCenterX = (renderedW / 2f - safePanX) / curScale
        val bmpCropCenterY = (renderedH / 2f - safePanY) / curScale

        val bmpCropW = vpW / curScale
        val bmpCropH = vpH / curScale

        val left = (bmpCropCenterX - bmpCropW / 2f).toInt().coerceIn(0, max(0, (bw - bmpCropW).toInt()))
        val top = (bmpCropCenterY - bmpCropH / 2f).toInt().coerceIn(0, max(0, (bh - bmpCropH).toInt()))
        val width = bmpCropW.toInt().coerceIn(1, (bw - left).toInt())
        val height = bmpCropH.toInt().coerceIn(1, (bh - top).toInt())

        val cropped = Bitmap.createBitmap(sourceBitmap, left, top, width, height)

        val outputW = if (isAvatar) 512 else 1024
        val outputH = if (isAvatar) 512 else 512
        val finalBmp = Bitmap.createScaledBitmap(cropped, outputW, outputH, true)

        targetFile.outputStream().use { out ->
            finalBmp.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }

        if (cropped != finalBmp && !cropped.isRecycled) cropped.recycle()
        if (!finalBmp.isRecycled) finalBmp.recycle()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
