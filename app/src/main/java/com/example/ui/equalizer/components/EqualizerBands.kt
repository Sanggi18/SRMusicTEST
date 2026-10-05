package com.example.ui.equalizer.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.equalizer.core.EqualizerAction
import com.example.ui.equalizer.core.EqualizerUiState
import kotlin.math.roundToInt

@Composable
fun UnifiedGraphicEqualizerAndPresetsCard(
    uiState: EqualizerUiState,
    onAction: (EqualizerAction) -> Unit,
    frequencies: List<String>,
    onSaveCustomPreset: () -> Unit,
    onOpenFrequencyGuide: () -> Unit,
    onOpenTemplateDialog: () -> Unit,
    onOpenCustomDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = uiState.isEqActive
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("unified_equalizer_presets_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Master EQ Switch & Action Buttons Row (TOP)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Switch(
                        checked = uiState.isEqualizerEnabled,
                        onCheckedChange = { onAction(EqualizerAction.SetEqualizerEnabled(it)) },
                        enabled = !uiState.isBitPerfectEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.testTag("eq_master_switch")
                    )
                    Column {
                        Text(
                            text = if (enabled) "10-Band Equalizer" else if (uiState.isBitPerfectEnabled) "Bit Perfect Mode" else "Equalizer Disabled",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = onSurfaceColor
                        )
                        Text(
                            text = if (enabled) "±15 dB • High Precision" else if (uiState.isBitPerfectEnabled) "Bypassed by Bit Perfect" else "Toggle switch to activate",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Normal,
                                color = if (enabled) primaryColor else onSurfaceVariant
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onOpenFrequencyGuide,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("eq_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Frequency Guide",
                            tint = onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onSaveCustomPreset,
                        enabled = enabled,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("eq_save_preset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Save,
                            contentDescription = "Save Custom Preset",
                            tint = if (enabled) primaryColor else onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { onAction(EqualizerAction.ResetBands) },
                        enabled = enabled,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("eq_reset_bands_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = "Reset EQ Bands",
                            tint = if (enabled) onSurfaceVariant else onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 2. 10-Band Responsive Multi-band Canvas & Vertical Faders (MIDDLE)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(top = 2.dp, bottom = 2.dp)
            ) {
                val faderTrackHeight = maxHeight - 36.dp
                val faderTrackHeightPx = with(LocalDensity.current) {
                    faderTrackHeight.toPx()
                }

                // Grid Lines and Spline Curve
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(faderTrackHeight)
                ) {
                    val width = size.width
                    val height = size.height

                    // Grid Horizontal Guides: +15dB, +10dB, +5dB, 0dB, -5dB, -10dB, -15dB
                    val dbLevels = listOf(15f, 10f, 5f, 0f, -5f, -10f, -15f)
                    dbLevels.forEach { db ->
                        val normalized = (15f - db) / 30f
                        val lineY = normalized * height
                        val isZero = db == 0f

                        drawLine(
                            color = if (isZero) primaryColor.copy(alpha = 0.35f) else outlineColor.copy(alpha = 0.15f),
                            start = Offset(0f, lineY),
                            end = Offset(width, lineY),
                            strokeWidth = if (isZero) 1.5f else 0.75f
                        )
                    }

                    // Continuous Frequency Response Curve
                    if (uiState.bands.isNotEmpty()) {
                        val numBands = frequencies.size
                        val stepX = width / numBands
                        val points = mutableListOf<Offset>()

                        for (i in 0 until numBands) {
                            val gain = uiState.bands.getOrElse(i) { 0f }
                            val normY = (15f - gain) / 30f
                            val px = stepX * i + stepX / 2f
                            val py = normY * height
                            points.add(Offset(px, py))
                        }

                        if (points.size >= 2) {
                            val curvePath = Path()
                            val fillPath = Path()

                            curvePath.moveTo(points.first().x, points.first().y)
                            fillPath.moveTo(points.first().x, height / 2f)
                            fillPath.lineTo(points.first().x, points.first().y)

                            for (i in 0 until points.size - 1) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val midX = (p0.x + p1.x) / 2f
                                curvePath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                                fillPath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                            }

                            fillPath.lineTo(points.last().x, height / 2f)
                            fillPath.close()

                            // Translucent EQ curve fill
                            drawPath(
                                path = fillPath,
                                color = primaryColor.copy(alpha = if (enabled) 0.12f else 0.03f)
                            )

                            // EQ curve outline
                            drawPath(
                                path = curvePath,
                                color = primaryColor.copy(alpha = if (enabled) 0.55f else 0.18f),
                                style = Stroke(width = 2f, cap = StrokeCap.Round)
                            )
                        }
                    }
                }

                // Interactive Vertical Faders Row (10 bands side-by-side)
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    frequencies.forEachIndexed { index, freqLabel ->
                        val gain = uiState.bands.getOrElse(index) { 0f }
                        VerticalBandFader(
                            index = index,
                            freqLabel = freqLabel,
                            gain = gain,
                            enabled = enabled,
                            trackHeight = faderTrackHeight,
                            trackHeightPx = faderTrackHeightPx,
                            onGainChange = { newGain -> onAction(EqualizerAction.UpdateBand(index, newGain)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Divider Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(outlineColor.copy(alpha = 0.25f))
            )

            // 3. Presets Header & 2-Row Presets Grid (BOTTOM - UNDER 10 BAND)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Presets",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = onSurfaceColor
                )

                val row1 = listOf("Bass Boost", "Vocal Boost", "Treble Boost")
                val row2 = listOf("Flat", "Template", "Custom")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row1.forEach { presetName ->
                        val isSelected = when (presetName) {
                            "Bass Boost" -> uiState.selectedPreset == "Bass boost"
                            "Vocal Boost" -> uiState.selectedPreset == "Vocal boost"
                            "Treble Boost" -> uiState.selectedPreset == "Treble boost"
                            else -> false
                        }
                        PresetButton(
                            name = presetName,
                            isSelected = isSelected,
                            enabled = enabled,
                            onClick = {
                                val mapped = when (presetName) {
                                    "Bass Boost" -> "Bass boost"
                                    "Vocal Boost" -> "Vocal boost"
                                    "Treble Boost" -> "Treble boost"
                                    else -> presetName
                                }
                                onAction(EqualizerAction.SelectPreset(mapped))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row2.forEach { presetName ->
                        val isSelected = when (presetName) {
                            "Flat" -> uiState.selectedPreset == "Flat"
                            "Template" -> uiState.selectedPreset == "Template"
                            "Custom" -> uiState.selectedPreset == "Custom"
                            else -> false
                        }
                        val displayName = when (presetName) {
                            "Template" -> uiState.selectedTemplateName?.ifEmpty { "Template" } ?: "Template"
                            "Custom" -> uiState.selectedCustomName?.ifEmpty { "Custom" } ?: "Custom"
                            else -> presetName
                        }

                        PresetButton(
                            name = displayName,
                            isSelected = isSelected,
                            enabled = enabled,
                            onClick = {
                                when (presetName) {
                                    "Flat" -> onAction(EqualizerAction.SelectPreset("Flat"))
                                    "Template" -> onOpenTemplateDialog()
                                    "Custom" -> onOpenCustomDialog()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetButton(
    name: String,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val containerColor = if (isSelected) {
        primaryColor.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }
    val contentColor = if (isSelected) {
        primaryColor
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
    }
    val borderStroke = if (isSelected) {
        BorderStroke(1.5.dp, primaryColor)
    } else {
        BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = borderStroke,
        modifier = modifier
            .height(44.dp)
            .testTag("preset_button_${name.lowercase().replace(" ", "_")}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = if (enabled) contentColor else contentColor.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun VerticalBandFader(
    index: Int,
    freqLabel: String,
    gain: Float,
    enabled: Boolean,
    trackHeight: Dp,
    trackHeightPx: Float,
    onGainChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxHeight()
            .testTag("eq_band_fader_$index"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Draggable Fader Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .pointerInput(enabled, trackHeightPx) {
                    if (!enabled || trackHeightPx <= 0f) return@pointerInput
                    detectTapGestures { offset ->
                        val fraction = 1f - (offset.y / trackHeightPx).coerceIn(0f, 1f)
                        val rawGain = -15f + fraction * 30f
                        val stepped = rawGain.roundToInt().toFloat().coerceIn(-15f, 15f)
                        onGainChange(stepped)
                    }
                }
                .pointerInput(enabled, trackHeightPx) {
                    if (!enabled || trackHeightPx <= 0f) return@pointerInput
                    detectVerticalDragGestures { change, _ ->
                        change.consume()
                        val fraction = 1f - (change.position.y / trackHeightPx).coerceIn(0f, 1f)
                        val rawGain = -15f + fraction * 30f
                        val stepped = rawGain.roundToInt().toFloat().coerceIn(-15f, 15f)
                        onGainChange(stepped)
                    }
                },
            contentAlignment = Alignment.TopCenter
        ) {
            // Track Centerline
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            )

            // Fader Thumb / Knob
            val normalizedGain = ((15f - gain) / 30f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = 0,
                            y = (normalizedGain * (trackHeightPx - 20.dp.toPx())).roundToInt()
                        )
                    }
                    .size(width = 24.dp, height = 18.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (enabled) {
                            if (gain != 0f) primaryColor else MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (enabled && gain != 0f) primaryColor else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Center metallic grip indicator line on the knob
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            if (enabled && gain != 0f) MaterialTheme.colorScheme.onPrimary
                            else onSurfaceVariant.copy(alpha = 0.7f)
                        )
                )
            }
        }

        // Frequency Label and Gain at Bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = freqLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (gain != 0f && enabled) primaryColor else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = if (gain > 0) "+${gain.roundToInt()} dB" else if (gain < 0) "${gain.roundToInt()} dB" else "0 dB",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (gain != 0f && enabled) primaryColor.copy(alpha = 0.95f) else onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
