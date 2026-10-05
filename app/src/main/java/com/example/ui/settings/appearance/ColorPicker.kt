package com.example.ui.settings.appearance
import com.example.ui.settings.core.*
import com.example.ui.settings.components.*
import com.example.ui.common.components.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.theme.AccentAmber
import com.example.ui.common.theme.AccentBlue
import com.example.ui.common.theme.AccentCoral
import com.example.ui.common.theme.AccentEmerald
import com.example.ui.common.theme.AccentIndigo
import com.example.ui.common.theme.AccentPink
import com.example.ui.common.theme.AccentPurple
import com.example.ui.common.theme.AccentTeal
import kotlin.math.ceil
import kotlin.math.roundToInt

val PRESET_ACCENTS = listOf(
    AccentPurple, AccentIndigo, AccentBlue, AccentTeal,
    AccentEmerald, AccentAmber, AccentCoral, AccentPink
)

@Composable
fun CheckerboardBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val squareSize = 6.dp.toPx()
        val numCols = ceil(size.width / squareSize).toInt()
        val numRows = ceil(size.height / squareSize).toInt()
        for (col in 0 until numCols) {
            for (row in 0 until numRows) {
                val color = if ((col + row) % 2 == 0) Color(0xFFD6D6DE) else Color(0xFFFFFFFF)
                drawRect(
                    color = color,
                    topLeft = Offset(col * squareSize, row * squareSize),
                    size = Size(squareSize, squareSize)
                )
            }
        }
    }
}

@Composable
fun ColorSpectrumSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    trackBrush: Brush,
    thumbColor: Color,
    modifier: Modifier = Modifier,
    isCheckerboard: Boolean = false
) {
    var widthPx by remember { mutableFloatStateOf(1f) }
    val barHeight = 24.dp
    val thumbRadius = 14.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(barHeight / 2))
                .onGloballyPositioned { widthPx = it.size.width.toFloat().coerceAtLeast(1f) }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val thumbRadiusPx = thumbRadius.toPx()
                        val effectiveWidth = (widthPx - thumbRadiusPx * 2).coerceAtLeast(1f)
                        val newValue = ((offset.x - thumbRadiusPx) / effectiveWidth).coerceIn(0f, 1f)
                        onValueChange(newValue)
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        val thumbRadiusPx = thumbRadius.toPx()
                        val effectiveWidth = (widthPx - thumbRadiusPx * 2).coerceAtLeast(1f)
                        val newValue = ((change.position.x - thumbRadiusPx) / effectiveWidth).coerceIn(0f, 1f)
                        onValueChange(newValue)
                    }
                }
        ) {
            if (isCheckerboard) {
                CheckerboardBackground(modifier = Modifier.fillMaxSize())
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(trackBrush)
            )
        }

        // Circular Thumb with white ring outline matching user reference
        val density = LocalDensity.current
        val thumbRadiusPx = with(density) { thumbRadius.toPx() }
        val maxOffsetPx = (widthPx - thumbRadiusPx * 2).coerceAtLeast(0f)
        val thumbOffsetDp = with(density) { (value.coerceIn(0f, 1f) * maxOffsetPx).toDp() }

        Box(
            modifier = Modifier
                .offset(x = thumbOffsetDp)
                .size(thumbRadius * 2)
                .shadow(elevation = 4.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(thumbColor)
                .border(2.5.dp, Color.White, CircleShape)
        )
    }
}

@Composable
fun CustomColorPickerDialog(
    title: String = "Custom Accent Color",
    confirmText: String = "Apply",
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit,
    onReset: (() -> Unit)? = null
) {
    val initialHsv = remember(initialColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(
            android.graphics.Color.rgb(
                (initialColor.red * 255).roundToInt(),
                (initialColor.green * 255).roundToInt(),
                (initialColor.blue * 255).roundToInt()
            ),
            hsv
        )
        hsv
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) } // 0f..360f
    var brightness by remember { mutableFloatStateOf(if (initialHsv[2] == 0f) 1f else initialHsv[2]) } // 0f..1f (Value)
    var saturation by remember { mutableFloatStateOf(if (initialHsv[1] == 0f) 1f else initialHsv[1]) } // 0f..1f (Saturation/Opacity)

    val currentColor = Color.hsv(
        hue = hue.coerceIn(0f, 360f),
        saturation = saturation.coerceIn(0f, 1f),
        value = brightness.coerceIn(0f, 1f)
    )

    var hexInput by remember {
        mutableStateOf(
            String.format("#%02X%02X%02X", (currentColor.red * 255).roundToInt(), (currentColor.green * 255).roundToInt(), (currentColor.blue * 255).roundToInt())
        )
    }

    fun updateFromHex(hex: String) {
        hexInput = hex
        val cleanHex = hex.removePrefix("#").trim()
        if (cleanHex.length == 6) {
            try {
                val r = cleanHex.substring(0, 2).toInt(16)
                val g = cleanHex.substring(2, 4).toInt(16)
                val b = cleanHex.substring(4, 6).toInt(16)
                val hsv = FloatArray(3)
                android.graphics.Color.colorToHSV(android.graphics.Color.rgb(r, g, b), hsv)
                hue = hsv[0]
                saturation = hsv[1]
                brightness = hsv[2]
            } catch (_: Exception) {
                // Ignore parse errors safely
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val hexDisplay = String.format("#%02X%02X%02X", (currentColor.red * 255).roundToInt(), (currentColor.green * 255).roundToInt(), (currentColor.blue * 255).roundToInt())
                val isLight = (currentColor.red * 0.299 + currentColor.green * 0.587 + currentColor.blue * 0.114) > 0.55

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(currentColor)
                        .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = hexDisplay,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = if (isLight) Color.Black else Color.White
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ColorSpectrumSlider(
                        value = (hue / 360f).coerceIn(0f, 1f),
                        onValueChange = { norm ->
                            hue = (norm * 360f).coerceIn(0f, 360f)
                            val newColor = Color.hsv(hue, saturation, brightness)
                            hexInput = String.format("#%02X%02X%02X", (newColor.red * 255).roundToInt(), (newColor.green * 255).roundToInt(), (newColor.blue * 255).roundToInt())
                        },
                        trackBrush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFF0000),
                                Color(0xFFFFFF00),
                                Color(0xFF00FF00),
                                Color(0xFF00FFFF),
                                Color(0xFF0000FF),
                                Color(0xFFFF00FF),
                                Color(0xFFFF0000)
                            )
                        ),
                        thumbColor = Color.hsv(hue, 1f, 1f)
                    )

                    ColorSpectrumSlider(
                        value = brightness.coerceIn(0f, 1f),
                        onValueChange = { norm ->
                            brightness = norm.coerceIn(0f, 1f)
                            val newColor = Color.hsv(hue, saturation, brightness)
                            hexInput = String.format("#%02X%02X%02X", (newColor.red * 255).roundToInt(), (newColor.green * 255).roundToInt(), (newColor.blue * 255).roundToInt())
                        },
                        trackBrush = Brush.horizontalGradient(
                            listOf(
                                Color.Black,
                                Color.hsv(hue, saturation.coerceAtLeast(0.1f), 1f)
                            )
                        ),
                        thumbColor = Color.hsv(hue, saturation, brightness)
                    )

                    ColorSpectrumSlider(
                        value = saturation.coerceIn(0f, 1f),
                        onValueChange = { norm ->
                            saturation = norm.coerceIn(0f, 1f)
                            val newColor = Color.hsv(hue, saturation, brightness)
                            hexInput = String.format("#%02X%02X%02X", (newColor.red * 255).roundToInt(), (newColor.green * 255).roundToInt(), (newColor.blue * 255).roundToInt())
                        },
                        trackBrush = Brush.horizontalGradient(
                            listOf(
                                Color.hsv(hue, 0f, brightness),
                                Color.hsv(hue, 1f, brightness)
                            )
                        ),
                        thumbColor = currentColor,
                        isCheckerboard = true
                    )
                }

                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { updateFromHex(it) },
                    label = { Text("Hex Code") },
                    placeholder = { Text("#7C4DFF") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.85f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onReset != null) {
                    TextButton(
                        onClick = {
                            onReset()
                            onDismiss()
                        }
                    ) {
                        Text("Reset Default")
                    }
                }
                Button(
                    onClick = {
                        onColorSelected(currentColor)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = confirmText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
            ) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

@Composable
fun AccentColorDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    CustomColorPickerDialog(
        title = "Custom Accent Color",
        confirmText = "Apply Accent",
        initialColor = initialColor,
        onColorSelected = onColorSelected,
        onDismiss = onDismiss
    )
}
