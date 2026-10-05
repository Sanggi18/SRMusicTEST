package com.example.ui.settings.appearance

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.FormatColorFill
import androidx.compose.material.icons.rounded.FormatColorText
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.theme.ThemeMode
import com.example.ui.settings.SettingsDestination

/**
 * App Theme Screen
 *
 * Modern Android Settings visual style:
 * - Large grouped rounded containers (26.dp)
 * - Clear title/subtitle hierarchy with descriptive text
 * - Left icons for rapid visual scanning
 * - Inset subtle dividers
 * - Generous, breathable spacing between groups
 * - Full existing logic and state integration preserved
 */
@Composable
fun AppThemeScreen(
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    isDynamicColor: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    isArtworkAccent: Boolean,
    onArtworkAccentChange: (Boolean) -> Unit,
    currentAccentColor: Color,
    onAccentColorChange: (Color) -> Unit,
    currentBackgroundColor: Color?,
    onBackgroundColorChange: (Color?) -> Unit,
    currentFontColor: Color?,
    onFontColorChange: (Color?) -> Unit,
    onNavigate: (SettingsDestination) -> Unit,
    modifier: Modifier = Modifier,
    currentCustomThemePresetId: String = "nordic_night"
) {
    val context = LocalContext.current

    var showAccentPopup by remember { mutableStateOf(false) }
    var showBackgroundPopup by remember { mutableStateOf(false) }
    var showFontColorPopup by remember { mutableStateOf(false) }

    val customVariant = remember(currentCustomThemePresetId) {
        com.example.ui.common.theme.getVariantById(currentCustomThemePresetId)
    }
    val customPreset = remember(currentCustomThemePresetId) {
        com.example.ui.common.theme.PRESET_CUSTOM_THEMES.find { preset ->
            preset.variants.any { it.id == currentCustomThemePresetId }
        }
    }
    val customLabel = if (customPreset != null) {
        "Custom : ${customPreset.name} ${customVariant.name}"
    } else {
        "Custom : ${customVariant.name}"
    }

    val activeBgColor = currentBackgroundColor ?: MaterialTheme.colorScheme.background
    val activeFontColor = currentFontColor ?: MaterialTheme.colorScheme.onSurface

    val isDarkCanvas = isSystemInDarkTheme() ||
        currentThemeMode == ThemeMode.AMOLED ||
        currentThemeMode == ThemeMode.DARK ||
        activeBgColor.red * 0.299f + activeBgColor.green * 0.587f + activeBgColor.blue * 0.114f < 0.5f

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .padding(bottom = 56.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // GROUP 1: Color Scheme (Unified Modern Grouped Card)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SectionHeaderLeft("Color Scheme")

            ThemeGroupCard {
                // Item 1: Material You
                ThemeOptionRow(
                    title = "Material You",
                    subtitle = "Dynamic system accent from wallpaper",
                    icon = Icons.Rounded.ColorLens,
                    onClick = {
                        if (currentThemeMode == ThemeMode.CUSTOM) {
                            Toast.makeText(
                                context,
                                "Pilih tema lain (System/Light/Dark/Amoled) terlebih dahulu untuk mengaktifkan Material You",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val next = !isDynamicColor
                            onDynamicColorChange(next)
                            if (next) {
                                onBackgroundColorChange(null)
                                onFontColorChange(null)
                            }
                        }
                    },
                    testTag = "theme_row_material_you"
                ) {
                    Switch(
                        checked = isDynamicColor,
                        onCheckedChange = { checked ->
                            if (currentThemeMode == ThemeMode.CUSTOM) {
                                Toast.makeText(
                                    context,
                                    "Pilih tema lain (System/Light/Dark/Amoled) terlebih dahulu untuk mengaktifkan Material You",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                onDynamicColorChange(checked)
                                if (checked) {
                                    onBackgroundColorChange(null)
                                    onFontColorChange(null)
                                }
                            }
                        },
                        colors = highContrastSwitchColors(isDarkCanvas)
                    )
                }

                SubtleCardDivider()

                // Item 2: Artwork Accent
                ThemeOptionRow(
                    title = "Artwork Accent",
                    subtitle = "Dynamic accent from playing song cover",
                    icon = Icons.Rounded.Brush,
                    onClick = { onArtworkAccentChange(!isArtworkAccent) },
                    testTag = "theme_row_artwork_accent"
                ) {
                    Switch(
                        checked = isArtworkAccent,
                        onCheckedChange = onArtworkAccentChange,
                        colors = highContrastSwitchColors(isDarkCanvas)
                    )
                }
            }
        }

        // GROUP 2: Theme Mode (Unified Grouped Card)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SectionHeaderLeft("Theme Mode")

            ThemeGroupCard {
                val customDisplayLabel = if (currentThemeMode == ThemeMode.CUSTOM) customLabel else "Custom"
                val themeModes = listOf(
                    ThemeModeOption(ThemeMode.SYSTEM, "Follow System", "Match system dark or light appearance", Icons.Rounded.BrightnessAuto),
                    ThemeModeOption(ThemeMode.LIGHT, "Light", "Clean bright look with light surfaces", Icons.Rounded.LightMode),
                    ThemeModeOption(ThemeMode.DARK, "Dark", "Comfortable dark theme for low light", Icons.Rounded.DarkMode),
                    ThemeModeOption(ThemeMode.AMOLED, "Amoled", "Pure pitch black background (#000000)", Icons.Rounded.Nightlight),
                    ThemeModeOption(ThemeMode.CUSTOM, customDisplayLabel, "Personalized custom theme preset", Icons.Rounded.Palette)
                )

                themeModes.forEachIndexed { index, item ->
                    val isSelected = currentThemeMode == item.mode

                    ThemeOptionRow(
                        title = item.title,
                        subtitle = item.subtitle,
                        icon = item.icon,
                        isSelected = isSelected,
                        onClick = {
                            if (item.mode == ThemeMode.CUSTOM && isDynamicColor) {
                                Toast.makeText(
                                    context,
                                    "Matikan Material You terlebih dahulu untuk memilih Custom Theme",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                onThemeModeChange(item.mode)
                                if (item.mode != ThemeMode.CUSTOM) {
                                    onBackgroundColorChange(null)
                                    onFontColorChange(null)
                                    onAccentColorChange(Color(0xFF2196F3L)) // Default Electric Blue
                                }
                            }
                        },
                        testTag = "theme_row_mode_${item.title.lowercase().replace(' ', '_')}"
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null // Handled by row click
                        )
                    }

                    if (index < themeModes.size - 1) {
                        SubtleCardDivider()
                    }
                }
            }
        }

        // GROUP 3: Custom Palette (Unified Grouped Card) & Theme Preset
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SectionHeaderLeft("Custom Palette")

            // Unified Custom Palette Card
            ThemeGroupCard {
                // Item 1: Custom Accent
                ThemeOptionRow(
                    title = "Custom Accent",
                    subtitle = "Set your own accent highlight color",
                    icon = Icons.Rounded.Colorize,
                    onClick = {
                        if (isDynamicColor) {
                            Toast.makeText(
                                context,
                                "Matikan Material You terlebih dahulu untuk mengubah Custom Accent",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (isArtworkAccent) {
                            Toast.makeText(
                                context,
                                "Matikan Artwork Accent terlebih dahulu untuk mengubah Custom Accent",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showAccentPopup = true
                        }
                    },
                    testTag = "theme_row_custom_accent"
                ) {
                    ColorPreviewCapsule(color = currentAccentColor)
                }

                SubtleCardDivider()

                // Item 2: Custom Background Color
                ThemeOptionRow(
                    title = "Custom Background Color",
                    subtitle = "Set custom window background canvas",
                    icon = Icons.Rounded.FormatColorFill,
                    onClick = {
                        if (isDynamicColor) {
                            Toast.makeText(
                                context,
                                "Matikan Material You terlebih dahulu untuk mengubah Custom Background Color",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showBackgroundPopup = true
                        }
                    },
                    testTag = "theme_row_custom_background"
                ) {
                    ColorPreviewCapsule(color = activeBgColor)
                }

                SubtleCardDivider()

                // Item 3: Custom Font Color
                ThemeOptionRow(
                    title = "Custom Font Color",
                    subtitle = "Set custom primary typography color",
                    icon = Icons.Rounded.FormatColorText,
                    onClick = {
                        if (isDynamicColor) {
                            Toast.makeText(
                                context,
                                "Matikan Material You terlebih dahulu untuk mengubah Custom Font Color",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showFontColorPopup = true
                        }
                    },
                    testTag = "theme_row_custom_font_color"
                ) {
                    ColorPreviewCapsule(color = activeFontColor)
                }
            }

            // Spacing separating Theme Preset from Custom Palette
            Spacer(modifier = Modifier.height(10.dp))

            // Standalone Card: Theme Preset
            ThemeGroupCard {
                ThemeOptionRow(
                    title = "Theme Preset",
                    subtitle = "Explore curated stylish color schemes",
                    icon = Icons.Rounded.DashboardCustomize,
                    onClick = {
                        if (isDynamicColor) {
                            Toast.makeText(
                                context,
                                "Matikan Material You terlebih dahulu untuk memilih Theme Preset",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            onNavigate(SettingsDestination.CustomThemes)
                        }
                    },
                    testTag = "theme_row_theme_preset"
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Open theme presets",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    // Color Pickers Dialogs
    if (showAccentPopup) {
        CustomColorPickerDialog(
            initialColor = currentAccentColor,
            onColorSelected = { color ->
                onAccentColorChange(color)
                onDynamicColorChange(false)
                com.example.core.util.ArtworkPaletteExtractor.clearCache()
                showAccentPopup = false
            },
            onDismiss = { showAccentPopup = false },
            title = "Choose Accent Color"
        )
    }

    if (showBackgroundPopup) {
        CustomColorPickerDialog(
            initialColor = activeBgColor,
            onColorSelected = { color ->
                onBackgroundColorChange(color)
                onThemeModeChange(ThemeMode.CUSTOM)
                onDynamicColorChange(false)
                showBackgroundPopup = false
            },
            onDismiss = { showBackgroundPopup = false },
            title = "Choose Background Color"
        )
    }

    if (showFontColorPopup) {
        CustomColorPickerDialog(
            initialColor = activeFontColor,
            onColorSelected = { color ->
                onFontColorChange(color)
                onThemeModeChange(ThemeMode.CUSTOM)
                onDynamicColorChange(false)
                showFontColorPopup = false
            },
            onDismiss = { showFontColorPopup = false },
            title = "Choose Font Color"
        )
    }
}

/**
 * Left-aligned Section Header matching modern Android Settings
 */
@Composable
private fun SectionHeaderLeft(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, top = 10.dp, bottom = 4.dp)
    )
}

/**
 * Grouped Rounded Container with generous 26.dp corner radius matching screenshot
 */
@Composable
private fun ThemeGroupCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

/**
 * Sleek Option Row inside a Grouped Card with icon, title, subtitle, and right control
 */
@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    testTag: String = "",
    control: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))
        control()
    }
}

/**
 * Inset Subtle Card Divider aligned with text content
 */
@Composable
private fun SubtleCardDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f),
        thickness = 0.6.dp,
        modifier = Modifier.padding(start = 58.dp, end = 18.dp)
    )
}

/**
 * High-contrast switch colors for AMOLED, Dark, and Light canvases
 */
@Composable
private fun highContrastSwitchColors(isDark: Boolean) = SwitchDefaults.colors(
    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
    checkedTrackColor = MaterialTheme.colorScheme.primary,
    checkedBorderColor = Color.Transparent,
    uncheckedThumbColor = if (isDark) Color(0xFFD4D4D8) else Color(0xFF71717A),
    uncheckedTrackColor = if (isDark) Color(0xFF27272A) else Color(0xFFE4E4E7),
    uncheckedBorderColor = if (isDark) Color(0xFF52525B) else Color(0xFFA1A1AA)
)

/**
 * Color Preview Capsule Pill
 */
@Composable
private fun ColorPreviewCapsule(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 44.dp, height = 24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
    )
}

private data class ThemeModeOption(
    val mode: ThemeMode,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

