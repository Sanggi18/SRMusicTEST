package com.example.ui.equalizer.components

import com.example.ui.equalizer.core.EqualizerAction
import com.example.ui.equalizer.core.EqualizerUiState

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

val bandFrequencies = listOf(
    "32", "64", "125", "250", "500",
    "1K", "2K", "4K", "8K", "16K"
)

val frequencyGuides = listOf(
    "32 Hz" to "Sub Bass / deep low-frequency rumble",
    "64 Hz" to "Bass / low-end impact and kick weight",
    "125 Hz" to "Upper Bass / bass fullness and warmth",
    "250 Hz" to "Low Mid / body and muddiness",
    "500 Hz" to "Midrange / tonal body",
    "1K" to "Midrange / vocal and instrument fundamentals",
    "2K" to "Upper Mid / vocal presence and attack",
    "4K" to "Presence / clarity and articulation",
    "8K" to "Treble / detail and brightness",
    "16K" to "Air / high-frequency extension"
)

val basePresets = listOf(
    "Flat", "Bass boost", "Vocal boost", "Treble boost", "Template", "Custom"
)

val templatePresets = listOf(
    "Jazz", "Pop", "Rock", "Classical", "Acoustic", "Electronic", "Bass & Treble",
    "Hip Hop", "Dance", "R&B", "Metal", "Lounge", "Spoken Word", "Deep Bass", "Bright Vocal"
)

val presetBands = mapOf(
    "Flat" to listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
    "Bass boost" to listOf(6f, 5f, 4f, 2f, 0f, 0f, 0f, 0f, 0f, 0f),
    "Vocal boost" to listOf(-2f, -1f, 0f, 2f, 4f, 4f, 3f, 1f, 0f, -1f),
    "Treble boost" to listOf(0f, 0f, 0f, 0f, 0f, 1f, 2f, 4f, 6f, 6f),
    "Bass & Treble" to listOf(5f, 4f, 2f, 0f, -1f, 0f, 2f, 4f, 5f, 5f),
    "Rock" to listOf(5f, 3f, 1f, 0f, -1f, 1f, 3f, 4f, 5f, 4f),
    "Pop" to listOf(1f, 2f, 3f, 2f, 0f, -1f, 1f, 2f, 3f, 3f),
    "Jazz" to listOf(3f, 2f, 1f, 2f, -1f, -1f, 0f, 1f, 2f, 3f),
    "Classical" to listOf(4f, 3f, 2f, 1f, -1f, -1f, 0f, 2f, 3f, 4f),
    "Acoustic" to listOf(3f, 2f, 1f, 1f, 2f, 2f, 3f, 3f, 2f, 1f),
    "Electronic" to listOf(6f, 5f, 2f, 0f, -2f, 2f, 1f, 3f, 5f, 6f),
    "Hip Hop" to listOf(5f, 4f, 1f, 2f, -1f, -1f, 1f, 0f, 2f, 3f),
    "Dance" to listOf(6f, 5f, 2f, 0f, 0f, 2f, 3f, 4f, 4f, 3f),
    "R&B" to listOf(4f, 5f, 3f, 1f, -1f, 1f, 2f, 2f, 3f, 3f),
    "Metal" to listOf(5f, 4f, 2f, -1f, -3f, 0f, 3f, 5f, 6f, 5f),
    "Lounge" to listOf(-1f, 1f, 2f, 3f, 1f, 0f, -1f, 1f, 2f, 1f),
    "Spoken Word" to listOf(-4f, -2f, 0f, 2f, 5f, 5f, 4f, 2f, 0f, -2f),
    "Deep Bass" to listOf(7f, 6f, 4f, 1f, 0f, 0f, 0f, 0f, 0f, 0f),
    "Bright Vocal" to listOf(-3f, -2f, -1f, 1f, 4f, 5f, 4f, 3f, 2f, 1f)
)

val audioOutputOptions = listOf(
    "Default" to "Auto-detect optimal output path",
    "AAudio" to "Low-latency high performance API",
    "AudioTrack" to "Standard Android streaming pipeline",
    "OpenSL ES" to "Native legacy audio engine"
)

fun Float.formatDb(): String {
    val rounded = (this * 10).roundToInt() / 10f
    return if (rounded > 0) "+${rounded} dB" else if (rounded < 0) "${rounded} dB" else "0 dB"
}

@Composable
fun EqualizerPresetsCard(
    uiState: EqualizerUiState,
    onAction: (EqualizerAction) -> Unit,
    onOpenTemplateDialog: () -> Unit,
    onOpenCustomDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Presets",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                if (uiState.selectedPreset == "Template" && uiState.selectedTemplateName != null) {
                    Text(
                        text = "Template: ${uiState.selectedTemplateName}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                } else if (uiState.selectedPreset == "Custom" && uiState.selectedCustomName != null) {
                    Text(
                        text = "Custom: ${uiState.selectedCustomName}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: Bass Boost | Vocal Boost | Treble Boost
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val row1 = listOf(
                        "Bass boost" to "Bass Boost",
                        "Vocal boost" to "Vocal Boost",
                        "Treble boost" to "Treble Boost"
                    )
                    row1.forEach { (key, display) ->
                        val isSelected = uiState.selectedPreset == key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = uiState.isEqActive) {
                                    onAction(EqualizerAction.SelectPreset(key))
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = display,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else if (uiState.isEqActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Row 2: Flat | Template | Custom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val row2 = listOf(
                        Triple("Flat", "Flat") { onAction(EqualizerAction.SelectPreset("Flat")) },
                        Triple("Template", if (uiState.selectedPreset == "Template" && uiState.selectedTemplateName != null) uiState.selectedTemplateName else "Template") { onOpenTemplateDialog() },
                        Triple("Custom", if (uiState.selectedPreset == "Custom" && uiState.selectedCustomName != null) uiState.selectedCustomName else "Custom") { onOpenCustomDialog() }
                    )
                    row2.forEach { (key, label, onClickAction) ->
                        val isSelected = uiState.selectedPreset == key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = uiState.isEqActive) {
                                    onClickAction()
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else if (uiState.isEqActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
