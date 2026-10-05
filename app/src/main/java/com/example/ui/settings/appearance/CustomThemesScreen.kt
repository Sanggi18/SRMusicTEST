package com.example.ui.settings.appearance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.theme.CUSTOM_THEME_PRESETS
import com.example.ui.common.theme.CustomThemePreset
import com.example.ui.common.theme.ThemeMode
import com.example.ui.settings.components.SettingsSectionHeader

@Composable
fun CustomThemesScreen(
    currentThemeMode: ThemeMode,
    currentCustomThemePresetId: String,
    onCustomThemePresetChange: (String) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBackgroundColorChange: (Color?) -> Unit = {},
    onFontColorChange: (Color?) -> Unit = {},
    onAccentColorChange: (Color) -> Unit = {},
    onDynamicColorChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedPresetForPopup by remember { mutableStateOf<CustomThemePreset?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 48.dp)
    ) {
        SettingsSectionHeader("Curated Theme Presets (13 Palettes)")
        Text(
            text = "Click any theme preset below to open style variants and activate custom color harmonies.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CUSTOM_THEME_PRESETS.forEach { preset ->
                val isSelected = currentThemeMode == ThemeMode.CUSTOM && preset.variants.any { it.id == currentCustomThemePresetId }
                CustomThemePresetCard(
                    preset = preset,
                    isSelected = isSelected,
                    onClick = {
                        selectedPresetForPopup = preset
                    }
                )
            }
        }
    }

    selectedPresetForPopup?.let { preset ->
        CustomThemeVariantSelectionDialog(
            preset = preset,
            currentVariantId = currentCustomThemePresetId,
            onVariantSelected = { variantId ->
                onCustomThemePresetChange(variantId)
                val variant = com.example.ui.common.theme.getVariantById(variantId)
                onThemeModeChange(ThemeMode.CUSTOM)
                onBackgroundColorChange(null)
                onFontColorChange(null)
                onAccentColorChange(variant.primary)
                onDynamicColorChange(false)
                selectedPresetForPopup = null
            },
            onDismiss = { selectedPresetForPopup = null }
        )
    }
}

@Composable
fun CustomThemeVariantSelectionDialog(
    preset: CustomThemePreset,
    currentVariantId: String,
    onVariantSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "${preset.name} Variants")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = preset.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                preset.variants.forEach { variant ->
                    val isSelected = currentVariantId == variant.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVariantSelected(variant.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = variant.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(variant.primary))
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(variant.surfaceContainerHigh))
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(variant.background))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun CustomThemePresetCard(
    preset: CustomThemePreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
        ),
        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "ACTIVE",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = preset.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sampleVariant = preset.variants.first()
                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(sampleVariant.primary))
                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(sampleVariant.surfaceContainerHigh))
                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(sampleVariant.background))
            }
        }
    }
}
