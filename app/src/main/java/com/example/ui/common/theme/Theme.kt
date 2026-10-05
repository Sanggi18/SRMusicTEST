package com.example.ui.common.theme

import android.app.Activity
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.content.Context
import android.content.ContextWrapper

private tailrec fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

enum class ThemeMode(val label: String) {
    SYSTEM("Follow System"),
    LIGHT("Light"),
    DARK("Dark"),
    AMOLED("AMOLED"),
    CUSTOM("Custom Theme")
}

data class CustomThemeVariant(
    val id: String,
    val name: String,
    val primary: Color,
    val background: Color,
    val surface: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val onSurface: Color,
    val onPrimary: Color
)

data class CustomThemePreset(
    val id: String,
    val name: String,
    val description: String,
    val variants: List<CustomThemeVariant>
)

val PRESET_CUSTOM_THEMES = listOf(
    // 1. Catppuccin (Mocha, Macchiato, Frappé, Latte)
    CustomThemePreset(
        id = "catppuccin",
        name = "Catppuccin",
        description = "Soothing pastel theme designed for warm, cozy, and relaxing listening",
        variants = listOf(
            CustomThemeVariant(
                id = "catppuccin_mocha",
                name = "Mocha",
                primary = Color(0xFFCBA6F7),
                background = Color(0xFF1E1E2E),
                surface = Color(0xFF181825),
                surfaceContainer = Color(0xFF313244),
                surfaceContainerHigh = Color(0xFF45475A),
                onSurface = Color(0xFFCDD6F4),
                onPrimary = Color(0xFF11111B)
            ),
            CustomThemeVariant(
                id = "catppuccin_macchiato",
                name = "Macchiato",
                primary = Color(0xFFC6A0F6),
                background = Color(0xFF24273A),
                surface = Color(0xFF1E2030),
                surfaceContainer = Color(0xFF363A4F),
                surfaceContainerHigh = Color(0xFF494D64),
                onSurface = Color(0xFFCAD3F5),
                onPrimary = Color(0xFF181926)
            ),
            CustomThemeVariant(
                id = "catppuccin_frappe",
                name = "Frappé",
                primary = Color(0xFFCA9EE6),
                background = Color(0xFF303446),
                surface = Color(0xFF292C3C),
                surfaceContainer = Color(0xFF414559),
                surfaceContainerHigh = Color(0xFF51576D),
                onSurface = Color(0xFFC6D0F5),
                onPrimary = Color(0xFF232634)
            ),
            CustomThemeVariant(
                id = "catppuccin_latte",
                name = "Latte",
                primary = Color(0xFF8839EF),
                background = Color(0xFFEFF1F5),
                surface = Color(0xFFE6E9EF),
                surfaceContainer = Color(0xFFCCD0DA),
                surfaceContainerHigh = Color(0xFFBCC0CC),
                onSurface = Color(0xFF4C4F69),
                onPrimary = Color(0xFFFFFFFF)
            )
        )
    ),
    // 2. Tokyo Night (Night, Storm, Day)
    CustomThemePreset(
        id = "tokyo_night",
        name = "Tokyo Night",
        description = "Clean aesthetic celebrating the vibrant neon lights of downtown Tokyo",
        variants = listOf(
            CustomThemeVariant(
                id = "tokyo_night_dark",
                name = "Night",
                primary = Color(0xFF7AA2F7),
                background = Color(0xFF1A1B26),
                surface = Color(0xFF16161E),
                surfaceContainer = Color(0xFF24283B),
                surfaceContainerHigh = Color(0xFF414868),
                onSurface = Color(0xFFC0CAF5),
                onPrimary = Color(0xFF15161E)
            ),
            CustomThemeVariant(
                id = "tokyo_night_storm",
                name = "Storm",
                primary = Color(0xFFBB9AF7),
                background = Color(0xFF24283B),
                surface = Color(0xFF1F2335),
                surfaceContainer = Color(0xFF2F354A),
                surfaceContainerHigh = Color(0xFF565F89),
                onSurface = Color(0xFFC0CAF5),
                onPrimary = Color(0xFF1F2335)
            ),
            CustomThemeVariant(
                id = "tokyo_night_day",
                name = "Day",
                primary = Color(0xFF34548A),
                background = Color(0xFFE1E2E7),
                surface = Color(0xFFD5D6DB),
                surfaceContainer = Color(0xFFC4C8DA),
                surfaceContainerHigh = Color(0xFFA8AECB),
                onSurface = Color(0xFF2E334D),
                onPrimary = Color(0xFFFFFFFF)
            )
        )
    ),
    // 3. Rosé Pine (Main, Moon, Dawn)
    CustomThemePreset(
        id = "rose_pine",
        name = "Rosé Pine",
        description = "All natural pine, faux fur, and delicate gold/rose highlights",
        variants = listOf(
            CustomThemeVariant(
                id = "rose_pine_main",
                name = "Main",
                primary = Color(0xFFEBBCBA),
                background = Color(0xFF191724),
                surface = Color(0xFF1F1D2E),
                surfaceContainer = Color(0xFF26233A),
                surfaceContainerHigh = Color(0xFF403D52),
                onSurface = Color(0xFFE0DEF4),
                onPrimary = Color(0xFF191724)
            ),
            CustomThemeVariant(
                id = "rose_pine_moon",
                name = "Moon",
                primary = Color(0xFFEA9A97),
                background = Color(0xFF232136),
                surface = Color(0xFF2A273F),
                surfaceContainer = Color(0xFF393552),
                surfaceContainerHigh = Color(0xFF44415A),
                onSurface = Color(0xFFE0DEF4),
                onPrimary = Color(0xFF232136)
            ),
            CustomThemeVariant(
                id = "rose_pine_dawn",
                name = "Dawn",
                primary = Color(0xFFD7827E),
                background = Color(0xFFFAF4ED),
                surface = Color(0xFFFFFDF8),
                surfaceContainer = Color(0xFFF2E9DE),
                surfaceContainerHigh = Color(0xFFE4DFD7),
                onSurface = Color(0xFF575279),
                onPrimary = Color(0xFFFFFFFF)
            )
        )
    ),
    // 4. Everforest (Dark Hard, Dark Medium, Light Warm)
    CustomThemePreset(
        id = "everforest",
        name = "Everforest",
        description = "Comfortable natural green tones designed to be gentle on your eyes",
        variants = listOf(
            CustomThemeVariant(
                id = "everforest_dark_hard",
                name = "Dark Hard",
                primary = Color(0xFFA7C080),
                background = Color(0xFF272E33),
                surface = Color(0xFF232A2E),
                surfaceContainer = Color(0xFF2D353B),
                surfaceContainerHigh = Color(0xFF3D484D),
                onSurface = Color(0xFFD3C6AA),
                onPrimary = Color(0xFF232A2E)
            ),
            CustomThemeVariant(
                id = "everforest_dark_medium",
                name = "Dark Medium",
                primary = Color(0xFF83C092),
                background = Color(0xFF2D353B),
                surface = Color(0xFF343F44),
                surfaceContainer = Color(0xFF3D484D),
                surfaceContainerHigh = Color(0xFF475258),
                onSurface = Color(0xFFD3C6AA),
                onPrimary = Color(0xFF2D353B)
            ),
            CustomThemeVariant(
                id = "everforest_light",
                name = "Light Warm",
                primary = Color(0xFF8DA101),
                background = Color(0xFFFDF6E3),
                surface = Color(0xFFF4EAD4),
                surfaceContainer = Color(0xFFEFE1BD),
                surfaceContainerHigh = Color(0xFFE0CF9B),
                onSurface = Color(0xFF5C6A72),
                onPrimary = Color(0xFFFFFFFF)
            )
        )
    ),
    // 5. Gruvbox (Dark Hard, Light Soft)
    CustomThemePreset(
        id = "gruvbox",
        name = "Gruvbox",
        description = "Retro warm earthy tones inspired by iconic vintage terminal aesthetics",
        variants = listOf(
            CustomThemeVariant(
                id = "gruvbox_dark",
                name = "Dark Hard",
                primary = Color(0xFFFABD2F),
                background = Color(0xFF282828),
                surface = Color(0xFF1D2021),
                surfaceContainer = Color(0xFF3C3836),
                surfaceContainerHigh = Color(0xFF504945),
                onSurface = Color(0xFFEBDBB2),
                onPrimary = Color(0xFF282828)
            ),
            CustomThemeVariant(
                id = "gruvbox_light",
                name = "Light Soft",
                primary = Color(0xFFB57614),
                background = Color(0xFFFBF1C7),
                surface = Color(0xFFF9F5D7),
                surfaceContainer = Color(0xFFEBDBB2),
                surfaceContainerHigh = Color(0xFFD5C4A1),
                onSurface = Color(0xFF3C3836),
                onPrimary = Color(0xFFFFFFFF)
            )
        )
    ),
    // 6. Nord (Polar Night, Snow Storm)
    CustomThemePreset(
        id = "nord",
        name = "Nord",
        description = "Arctic, north-bluish clean and minimal Scandinavian palette",
        variants = listOf(
            CustomThemeVariant(
                id = "nord_polar",
                name = "Polar Night",
                primary = Color(0xFF88C0D0),
                background = Color(0xFF2E3440),
                surface = Color(0xFF242933),
                surfaceContainer = Color(0xFF3B4252),
                surfaceContainerHigh = Color(0xFF434C5E),
                onSurface = Color(0xFFECEFF4),
                onPrimary = Color(0xFF2E3440)
            ),
            CustomThemeVariant(
                id = "nord_snow_storm",
                name = "Snow Storm",
                primary = Color(0xFF5E81AC),
                background = Color(0xFFECEFF4),
                surface = Color(0xFFE5E9F0),
                surfaceContainer = Color(0xFFD8DEE9),
                surfaceContainerHigh = Color(0xFFC2D0E0),
                onSurface = Color(0xFF2E3440),
                onPrimary = Color(0xFFFFFFFF)
            )
        )
    ),
    // 7. Dracula (Classic, Vampire)
    CustomThemePreset(
        id = "dracula",
        name = "Dracula",
        description = "Famous vampire dark theme with vibrant purples, pinks, and cyans",
        variants = listOf(
            CustomThemeVariant(
                id = "dracula_classic",
                name = "Classic",
                primary = Color(0xFFFF79C6),
                background = Color(0xFF282A36),
                surface = Color(0xFF21222C),
                surfaceContainer = Color(0xFF44475A),
                surfaceContainerHigh = Color(0xFF6272A4),
                onSurface = Color(0xFFF8F8F2),
                onPrimary = Color(0xFF282A36)
            ),
            CustomThemeVariant(
                id = "dracula_vampire",
                name = "Vampire",
                primary = Color(0xFFBD93F9),
                background = Color(0xFF181A24),
                surface = Color(0xFF12131A),
                surfaceContainer = Color(0xFF252838),
                surfaceContainerHigh = Color(0xFF373B52),
                onSurface = Color(0xFFF8F8F2),
                onPrimary = Color(0xFF12131A)
            )
        )
    ),
    // 8. Monokai Pro (Pro Classic, Machine, Octagon)
    CustomThemePreset(
        id = "monokai",
        name = "Monokai Pro",
        description = "Legendary developer theme with high visual clarity and saturated accents",
        variants = listOf(
            CustomThemeVariant(
                id = "monokai_pro",
                name = "Pro Classic",
                primary = Color(0xFFA9DC76),
                background = Color(0xFF2D2A2E),
                surface = Color(0xFF221F22),
                surfaceContainer = Color(0xFF403E41),
                surfaceContainerHigh = Color(0xFF5B595C),
                onSurface = Color(0xFFFCFCFA),
                onPrimary = Color(0xFF2D2A2E)
            ),
            CustomThemeVariant(
                id = "monokai_machine",
                name = "Machine",
                primary = Color(0xFFFF6188),
                background = Color(0xFF273136),
                surface = Color(0xFF1D2529),
                surfaceContainer = Color(0xFF3A4449),
                surfaceContainerHigh = Color(0xFF525D62),
                onSurface = Color(0xFFF2F2F0),
                onPrimary = Color(0xFF1D2529)
            ),
            CustomThemeVariant(
                id = "monokai_octagon",
                name = "Octagon",
                primary = Color(0xFFFFD866),
                background = Color(0xFF282A3A),
                surface = Color(0xFF1E1F2B),
                surfaceContainer = Color(0xFF3A3D52),
                surfaceContainerHigh = Color(0xFF51556E),
                onSurface = Color(0xFFF5F5F7),
                onPrimary = Color(0xFF1E1F2B)
            )
        )
    ),
    // 9. Spotify Obsidian (Pitch Black, Emerald Deep)
    CustomThemePreset(
        id = "spotify",
        name = "Spotify Obsidian",
        description = "Sleek obsidian deep canvas with signature vibrant green branding",
        variants = listOf(
            CustomThemeVariant(
                id = "spotify_dark",
                name = "Pitch Black",
                primary = Color(0xFF1DB954),
                background = Color(0xFF121212),
                surface = Color(0xFF0A0A0A),
                surfaceContainer = Color(0xFF1E1E1E),
                surfaceContainerHigh = Color(0xFF2A2A2A),
                onSurface = Color(0xFFFFFFFF),
                onPrimary = Color(0xFF000000)
            ),
            CustomThemeVariant(
                id = "spotify_emerald",
                name = "Emerald Deep",
                primary = Color(0xFF1ED760),
                background = Color(0xFF05140C),
                surface = Color(0xFF030D08),
                surfaceContainer = Color(0xFF0C2417),
                surfaceContainerHigh = Color(0xFF163B26),
                onSurface = Color(0xFFE8F8EE),
                onPrimary = Color(0xFF05140C)
            )
        )
    ),
    // 10. One Dark Pro (Classic, Deep Amber)
    CustomThemePreset(
        id = "one_dark",
        name = "One Dark Pro",
        description = "Atom-inspired iconic syntax colors balanced with deep charcoal",
        variants = listOf(
            CustomThemeVariant(
                id = "one_dark_classic",
                name = "Classic",
                primary = Color(0xFF61AFEF),
                background = Color(0xFF282C34),
                surface = Color(0xFF21252B),
                surfaceContainer = Color(0xFF353B45),
                surfaceContainerHigh = Color(0xFF4B5263),
                onSurface = Color(0xFFABB2BF),
                onPrimary = Color(0xFF21252B)
            ),
            CustomThemeVariant(
                id = "one_dark_deep",
                name = "Deep Amber",
                primary = Color(0xFFE5C07B),
                background = Color(0xFF1E2227),
                surface = Color(0xFF181A1F),
                surfaceContainer = Color(0xFF282C34),
                surfaceContainerHigh = Color(0xFF3E4451),
                onSurface = Color(0xFFABB2BF),
                onPrimary = Color(0xFF181A1F)
            )
        )
    ),
    // 11. Cyberpunk 2077 (Neon Yellow, Night City Magenta)
    CustomThemePreset(
        id = "cyberpunk",
        name = "Cyberpunk 2077",
        description = "High-voltage futuristic neon aesthetics from Night City",
        variants = listOf(
            CustomThemeVariant(
                id = "cyberpunk_yellow",
                name = "Neon Yellow",
                primary = Color(0xFFFCEE0A),
                background = Color(0xFF121212),
                surface = Color(0xFF080808),
                surfaceContainer = Color(0xFF1F1F1F),
                surfaceContainerHigh = Color(0xFF2E2E2E),
                onSurface = Color(0xFFFDFDFD),
                onPrimary = Color(0xFF000000)
            ),
            CustomThemeVariant(
                id = "cyberpunk_magenta",
                name = "Night City Magenta",
                primary = Color(0xFFFF007F),
                background = Color(0xFF0D0221),
                surface = Color(0xFF080114),
                surfaceContainer = Color(0xFF1B0C3B),
                surfaceContainerHigh = Color(0xFF2D1460),
                onSurface = Color(0xFFE0F7FA),
                onPrimary = Color(0xFF0D0221)
            )
        )
    ),
    // 12. Matcha Latte & Sage (Matcha Latte, Sage Dark)
    CustomThemePreset(
        id = "matcha_sage",
        name = "Matcha & Sage",
        description = "Cozy earthy herbal green hues evoking tranquility and focus",
        variants = listOf(
            CustomThemeVariant(
                id = "matcha_latte",
                name = "Matcha Latte",
                primary = Color(0xFF5B7E54),
                background = Color(0xFFF4F6F0),
                surface = Color(0xFFE9EEE4),
                surfaceContainer = Color(0xFFDCE4D5),
                surfaceContainerHigh = Color(0xFFCCD6C3),
                onSurface = Color(0xFF283625),
                onPrimary = Color(0xFFFFFFFF)
            ),
            CustomThemeVariant(
                id = "sage_dark",
                name = "Sage Dark",
                primary = Color(0xFF8EBA94),
                background = Color(0xFF1C241E),
                surface = Color(0xFF151C17),
                surfaceContainer = Color(0xFF26332A),
                surfaceContainerHigh = Color(0xFF344539),
                onSurface = Color(0xFFE3EDE4),
                onPrimary = Color(0xFF151C17)
            )
        )
    ),
    // 13. Midnight Indigo
    CustomThemePreset(
        id = "midnight_indigo",
        name = "Midnight Indigo",
        description = "Deep dark navy blue and royal indigo for late night music sessions",
        variants = listOf(
            CustomThemeVariant(
                id = "midnight_navy",
                name = "Midnight Navy",
                primary = Color(0xFF818CF8),
                background = Color(0xFF0F172A),
                surface = Color(0xFF0A0F1D),
                surfaceContainer = Color(0xFF1E293B),
                surfaceContainerHigh = Color(0xFF334155),
                onSurface = Color(0xFFF1F5F9),
                onPrimary = Color(0xFF0F172A)
            )
        )
    )
)

val CUSTOM_THEME_PRESETS = PRESET_CUSTOM_THEMES

fun getVariantById(variantId: String): CustomThemeVariant {
    for (preset in PRESET_CUSTOM_THEMES) {
        preset.variants.find { it.id == variantId }?.let { return it }
    }
    // Fallback: search by prefix or return default
    for (preset in PRESET_CUSTOM_THEMES) {
        preset.variants.find { variantId.startsWith(it.id.substringBefore("_")) }?.let { return it }
    }
    return PRESET_CUSTOM_THEMES.first().variants.first()
}

private fun blendColor(base: Color, overlay: Color, amount: Float): Color {
    val r = base.red + (overlay.red - base.red) * amount
    val g = base.green + (overlay.green - base.green) * amount
    val b = base.blue + (overlay.blue - base.blue) * amount
    return Color(r.coerceIn(0f, 1f), g.coerceIn(0f, 1f), b.coerceIn(0f, 1f))
}

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = PixelPlayWhite,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = PixelPlayPink,
    onSecondary = PixelPlayWhite,
    secondaryContainer = Color(0xFFFFD8E4),
    onSecondaryContainer = Color(0xFF3B071E),
    tertiary = PixelPlayOrange,
    onTertiary = PixelPlayBlack,
    tertiaryContainer = Color(0xFFFFDBCF),
    onTertiaryContainer = Color(0xFF341000),
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    surfaceContainerLow = LightSurfaceContainerLow,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = PixelPlayPurplePrimary,
    onPrimary = PixelPlayWhite,
    primaryContainer = Color(0xFF371B58),
    onPrimaryContainer = Color(0xFFF3E5F5),
    secondary = PixelPlayPink,
    onSecondary = PixelPlayWhite,
    secondaryContainer = Color(0xFF4A192C),
    onSecondaryContainer = Color(0xFFFFD8E4),
    tertiary = PixelPlayOrange,
    onTertiary = Color(0xFF441400),
    tertiaryContainer = Color(0xFF5A240F),
    onTertiaryContainer = Color(0xFFFFDBCF),
    background = PixelPlayPurpleDark,
    onBackground = PixelPlayLightPurple,
    surface = PixelPlaySurface,
    onSurface = PixelPlayLightPurple,
    surfaceVariant = PixelPlaySurfaceContainer,
    onSurfaceVariant = PixelPlayMuted,
    surfaceContainer = PixelPlaySurfaceContainer,
    surfaceContainerHigh = PixelPlaySurfaceContainerHigh,
    surfaceContainerHighest = PixelPlaySurfaceContainerHighest,
    surfaceContainerLow = PixelPlaySurfaceContainerLow,
    outline = PixelPlayOutline,
    outlineVariant = PixelPlayOutlineVariant
)

private val AmoledColorScheme = darkColorScheme(
    primary = PixelPlayPurplePrimary,
    onPrimary = PixelPlayWhite,
    primaryContainer = Color(0xFF371B58),
    onPrimaryContainer = Color(0xFFF3E5F5),
    secondary = PixelPlayPink,
    onSecondary = PixelPlayWhite,
    secondaryContainer = Color(0xFF4A192C),
    onSecondaryContainer = Color(0xFFFFD8E4),
    tertiary = PixelPlayOrange,
    onTertiary = PixelPlayBlack,
    background = AmoledBg,
    onBackground = AmoledOnSurface,
    surface = AmoledSurface,
    onSurface = AmoledOnSurface,
    surfaceVariant = AmoledSurfaceContainer,
    onSurfaceVariant = AmoledOnSurface.copy(alpha = 0.75f),
    surfaceContainer = AmoledSurfaceContainer,
    surfaceContainerHigh = AmoledSurfaceContainerHigh,
    surfaceContainerHighest = AmoledSurfaceContainerHighest,
    surfaceContainerLow = AmoledBg,
    outline = AmoledBorder,
    outlineVariant = AmoledBorder
)

fun createAccentColorScheme(
    accent: Color,
    isDark: Boolean,
    customBg: Color? = null,
    customFontColor: Color? = null
): ColorScheme {
    val bg = customBg ?: if (isDark) PixelPlayPurpleDark else LightBackground
    val surface = customBg ?: if (isDark) PixelPlaySurface else LightSurface
    // Determine on-surface text contrast based on background luminance
    val bgLuminance = bg.red * 0.299f + bg.green * 0.587f + bg.blue * 0.114f
    val isBgLight = bgLuminance > 0.5f

    val onSurface = customFontColor ?: if (customBg != null) {
        if (isBgLight) LightOnSurface else PixelPlayLightPurple
    } else {
        if (isDark) PixelPlayLightPurple else LightOnSurface
    }

    val onSurfaceVariant = if (customFontColor != null) {
        customFontColor.copy(alpha = 0.72f)
    } else {
        if (isDark) PixelPlayMuted else LightOnSurfaceVariant
    }

    val surfaceContainer = if (isDark) {
        blendColor(PixelPlaySurfaceContainer, accent, 0.08f)
    } else {
        blendColor(Color(0xFFEFF2F8), accent, 0.05f)
    }
    val surfaceContainerHigh = if (isDark) {
        blendColor(PixelPlaySurfaceContainerHigh, accent, 0.14f)
    } else {
        blendColor(Color(0xFFE5E8F0), accent, 0.08f)
    }
    val surfaceContainerHighest = if (isDark) {
        blendColor(PixelPlaySurfaceContainerHighest, accent, 0.20f)
    } else {
        blendColor(Color(0xFFDBDFEA), accent, 0.12f)
    }
    val surfaceContainerLow = if (isDark) {
        blendColor(PixelPlaySurfaceContainerLow, bg, 0.50f)
    } else {
        Color(0xFFF5F6FA)
    }

    val outline = if (isDark) {
        blendColor(PixelPlayOutline, accent, 0.15f)
    } else {
        Color(0xFFCBD5E1)
    }

    return if (isDark) {
        darkColorScheme(
            primary = accent,
            onPrimary = if (accent.red * 0.299f + accent.green * 0.587f + accent.blue * 0.114f > 0.6f) Color.Black else Color.White,
            primaryContainer = blendColor(surface, accent, 0.35f),
            onPrimaryContainer = Color.White,
            secondary = accent,
            onSecondary = if (accent.red * 0.299f + accent.green * 0.587f + accent.blue * 0.114f > 0.6f) Color.Black else Color.White,
            background = bg,
            onBackground = onSurface,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceContainer,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            surfaceContainerLow = surfaceContainerLow,
            outline = outline,
            outlineVariant = outline
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = if (accent.red * 0.299f + accent.green * 0.587f + accent.blue * 0.114f > 0.6f) Color.Black else Color.White,
            primaryContainer = blendColor(Color.White, accent, 0.18f),
            onPrimaryContainer = if (accent.red * 0.299f + accent.green * 0.587f + accent.blue * 0.114f > 0.6f) Color(0xFF1E1237) else accent,
            secondary = accent,
            onSecondary = if (accent.red * 0.299f + accent.green * 0.587f + accent.blue * 0.114f > 0.6f) Color.Black else Color.White,
            background = bg,
            onBackground = onSurface,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceContainer,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            surfaceContainerLow = surfaceContainerLow,
            outline = outline,
            outlineVariant = outline
        )
    }
}

fun createCustomThemeColorScheme(
    variant: CustomThemeVariant,
    customAccent: Color? = null,
    customBg: Color? = null,
    customFontColor: Color? = null
): ColorScheme {
    // If user explicitly defined customBg (e.g. Pure Black 100%), use that; otherwise use curated preset variant background
    val bg = customBg ?: variant.background
    val isTrueBlack = customBg != null && bg == Color(0xFF000000)
    val isLight = (bg.red * 0.299f + bg.green * 0.587f + bg.blue * 0.114f) > 0.5f

    // If customBg is pure black and accent not specified, use SoftLightBlue; otherwise use variant.primary (or customAccent if overridden)
    val primary = if (isTrueBlack) {
        customAccent ?: SoftLightBlue
    } else {
        variant.primary
    }
    val onPrimary = variant.onPrimary

    val surface = when {
        isTrueBlack -> Color(0xFF000000)
        customBg != null -> blendColor(bg, if (isLight) Color.Black else Color.White, 0.05f)
        else -> variant.surface
    }

    val onSurface = customFontColor ?: (if (isTrueBlack) Color(0xFFFFFFFF) else variant.onSurface)
    val onSurfaceVariant = customFontColor?.copy(alpha = 0.72f) ?: (if (isTrueBlack) Color(0xFFE2E2E6) else (if (isLight) Color(0xFF49454F) else Color(0xFFCAC4D0)))

    val surfaceContainer = if (isTrueBlack) Color(0xFF151518) else (variant.surfaceContainer ?: blendColor(surface, primary, if (isLight) 0.08f else 0.10f))
    val surfaceContainerHigh = if (isTrueBlack) Color(0xFF202025) else (variant.surfaceContainerHigh ?: blendColor(surface, primary, if (isLight) 0.14f else 0.16f))
    val surfaceContainerHighest = if (isTrueBlack) Color(0xFF2C2C33) else blendColor(surface, primary, if (isLight) 0.20f else 0.22f)
    val surfaceContainerLow = if (isTrueBlack) Color(0xFF0D0D10) else blendColor(surface, bg, 0.50f)

    val outlineColor = if (isTrueBlack) Color(0xFF383842) else blendColor(surface, primary, 0.25f)
    val outlineVariantColor = if (isTrueBlack) Color(0xFF24242C) else surfaceContainer

    return if (isLight) {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = blendColor(Color.White, primary, 0.18f),
            onPrimaryContainer = primary,
            secondary = primary,
            onSecondary = onPrimary,
            background = bg,
            onBackground = onSurface,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceContainer,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            surfaceContainerLow = surfaceContainerLow,
            outline = outlineColor,
            outlineVariant = outlineVariantColor
        )
    } else {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = blendColor(surface, primary, 0.35f),
            onPrimaryContainer = Color.White,
            secondary = primary,
            onSecondary = onPrimary,
            background = bg,
            onBackground = onSurface,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceContainer,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            surfaceContainerLow = surfaceContainerLow,
            outline = outlineColor,
            outlineVariant = outlineVariantColor
        )
    }
}

@Composable
fun SRMusicTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    isDynamicColor: Boolean = true,
    customAccent: Color = AccentBlue,
    customBackgroundColor: Color? = null,
    customFontColor: Color? = null,
    customThemePresetId: String = "catppucin_mocha",
    systemInDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val baseScheme = when {
        // Dynamic Color: Material You when enabled (and not Custom theme)
        isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && themeMode != ThemeMode.CUSTOM -> {
            when (themeMode) {
                ThemeMode.AMOLED -> {
                    val dynamic = dynamicDarkColorScheme(context)
                    dynamic.copy(
                        background = AmoledBg,
                        surface = AmoledSurface,
                        surfaceContainer = blendColor(AmoledSurfaceContainer, dynamic.primary, 0.02f),
                        surfaceContainerHigh = blendColor(AmoledSurfaceContainerHigh, dynamic.primary, 0.04f),
                        surfaceContainerHighest = blendColor(AmoledSurfaceContainerHighest, dynamic.primary, 0.06f),
                        surfaceContainerLow = AmoledBg,
                        outline = AmoledBorder,
                        outlineVariant = Color(0xFF1C1C22)
                    )
                }
                ThemeMode.LIGHT -> dynamicLightColorScheme(context)
                ThemeMode.DARK -> {
                    val dynamic = dynamicDarkColorScheme(context)
                    dynamic.copy(
                        background = Color(0xFF111215),
                        surface = Color(0xFF131418),
                        surfaceContainer = blendColor(Color(0xFF1C1D22), dynamic.primary, 0.05f),
                        surfaceContainerHigh = blendColor(Color(0xFF24252C), dynamic.primary, 0.08f),
                        surfaceContainerHighest = blendColor(Color(0xFF2E2F38), dynamic.primary, 0.12f),
                        surfaceContainerLow = Color(0xFF16171B),
                        outline = Color(0xFF383944),
                        outlineVariant = Color(0xFF262730)
                    )
                }
                ThemeMode.SYSTEM -> {
                    if (systemInDarkTheme) {
                        val dynamic = dynamicDarkColorScheme(context)
                        dynamic.copy(
                            background = Color(0xFF111215),
                            surface = Color(0xFF131418),
                            surfaceContainer = blendColor(Color(0xFF1C1D22), dynamic.primary, 0.05f),
                            surfaceContainerHigh = blendColor(Color(0xFF24252C), dynamic.primary, 0.08f),
                            surfaceContainerHighest = blendColor(Color(0xFF2E2F38), dynamic.primary, 0.12f),
                            surfaceContainerLow = Color(0xFF16171B),
                            outline = Color(0xFF383944),
                            outlineVariant = Color(0xFF262730)
                        )
                    } else {
                        dynamicLightColorScheme(context)
                    }
                }
                else -> dynamicDarkColorScheme(context)
            }
        }
        // Custom Theme Mode: uses custom preset palette or custom background/accent/font
        themeMode == ThemeMode.CUSTOM -> {
            val variant = getVariantById(customThemePresetId)
            createCustomThemeColorScheme(
                variant = variant,
                customAccent = customAccent,
                customBg = customBackgroundColor,
                customFontColor = customFontColor
            )
        }
        // AMOLED Mode (without dynamic color)
        themeMode == ThemeMode.AMOLED -> {
            val amoledPrimary = customAccent
            AmoledColorScheme.copy(
                primary = amoledPrimary,
                secondary = amoledPrimary,
                primaryContainer = blendColor(AmoledSurfaceContainerHigh, amoledPrimary, 0.35f),
                onPrimaryContainer = Color.White,
                surfaceContainer = blendColor(AmoledSurfaceContainer, amoledPrimary, 0.02f),
                surfaceContainerHigh = blendColor(AmoledSurfaceContainerHigh, amoledPrimary, 0.04f),
                surfaceContainerHighest = blendColor(AmoledSurfaceContainerHighest, amoledPrimary, 0.06f),
                surfaceContainerLow = AmoledBg,
                outline = AmoledBorder,
                outlineVariant = Color(0xFF1C1C22),
                onSurface = customFontColor ?: AmoledOnSurface,
                onBackground = customFontColor ?: AmoledOnSurface,
                onSurfaceVariant = customFontColor?.copy(alpha = 0.75f) ?: AmoledOnSurface.copy(alpha = 0.75f)
            )
        }
        // Light Mode
        themeMode == ThemeMode.LIGHT -> {
            createAccentColorScheme(customAccent, isDark = false, customBg = customBackgroundColor, customFontColor = customFontColor)
        }
        // Dark Mode
        themeMode == ThemeMode.DARK -> {
            createAccentColorScheme(customAccent, isDark = true, customBg = customBackgroundColor, customFontColor = customFontColor)
        }
        // Follow System
        themeMode == ThemeMode.SYSTEM -> {
            createAccentColorScheme(customAccent, isDark = systemInDarkTheme, customBg = customBackgroundColor, customFontColor = customFontColor)
        }
        else -> LightColorScheme
    }

    val colorScheme = baseScheme

    val typography = createAppTypography()
    val view = LocalView.current

    if (!view.isInEditMode) {
        val isDark = colorScheme.background.luminance() <= 0.5f || colorScheme.surface.luminance() <= 0.5f
        DisposableEffect(isDark, colorScheme) {
            val activity = view.context.findActivity()
            val window = activity?.window ?: (view.context as? Activity)?.window
            if (activity != null) {
                activity.enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
            }
            if (window != null) {
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                val decorView = window.decorView
                val insetsController = WindowCompat.getInsetsController(window, decorView)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
            onDispose { }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalSpacing provides Spacing(),
                content = content
            )
        }
    )
}

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    isDynamicColor: Boolean = true,
    customAccent: Color = AccentPurple,
    customBackgroundColor: Color? = null,
    customFontColor: Color? = null,
    customThemePresetId: String = "catppucin_mocha",
    systemInDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    SRMusicTheme(
        themeMode = themeMode,
        isDynamicColor = isDynamicColor,
        customAccent = customAccent,
        customBackgroundColor = customBackgroundColor,
        customFontColor = customFontColor,
        customThemePresetId = customThemePresetId,
        systemInDarkTheme = systemInDarkTheme,
        content = content
    )
}
