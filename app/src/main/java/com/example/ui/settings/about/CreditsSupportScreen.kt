package com.example.ui.settings.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class CoreTechItem(
    val name: String,
    val url: String
)

private data class CreditCardItem(
    val title: String,
    val url: String,
    val license: String,
    val description: String? = null
)

@Composable
fun CreditsSupportScreen(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    val coreTechs = listOf(
        CoreTechItem("Android Jetpack Compose", "https://developer.android.com/jetpack/compose"),
        CoreTechItem("Material Design 3", "https://m3.material.io"),
        CoreTechItem("Kotlin Coroutines & StateFlow", "https://github.com/Kotlin/kotlinx.coroutines"),
        CoreTechItem("AndroidX Media3 & ExoPlayer", "https://github.com/androidx/media"),
        CoreTechItem("Glide Image Loading & Caching", "https://github.com/bumptech/glide"),
        CoreTechItem("Google Material Symbols", "https://fonts.google.com/icons")
    )

    val sourceLibraries = listOf(
        CreditCardItem(
            title = "Glide",
            url = "https://github.com/bumptech/glide",
            license = "BSD, part MIT and Apache 2.0."
        ),
        CreditCardItem(
            title = "AndroidX Media3 & ExoPlayer",
            url = "https://github.com/androidx/media",
            license = "Apache 2.0"
        ),
        CreditCardItem(
            title = "Kotlinx Coroutines",
            url = "https://github.com/Kotlin/kotlinx.coroutines",
            license = "Apache 2.0"
        ),
        CreditCardItem(
            title = "AndroidX Room Database",
            url = "https://developer.android.com/training/data-storage/room",
            license = "Apache 2.0"
        ),
        CreditCardItem(
            title = "AndroidX Navigation Compose",
            url = "https://developer.android.com/guide/navigation",
            license = "Apache 2.0"
        ),
        CreditCardItem(
            title = "Google Material Icons Extended",
            url = "https://fonts.google.com/icons",
            license = "Apache 2.0"
        )
    )

    val designReferences = listOf(
        CreditCardItem(
            title = "AyraMusic",
            url = "https://github.com/Brosssh/AyraMusic",
            license = "GPL-3.0",
            description = "Interactive Mini Player morphing gesture & Now Playing layout architecture"
        ),
        CreditCardItem(
            title = "Catppuccin",
            url = "https://github.com/catppuccin/catppuccin",
            license = "MIT license",
            description = "Soothing pastel theme color palette"
        ),
        CreditCardItem(
            title = "Dracula Theme",
            url = "https://github.com/dracula/dracula-theme",
            license = "MIT license",
            description = "Official dark theme color scheme"
        ),
        CreditCardItem(
            title = "Nord Theme",
            url = "https://github.com/nordtheme/nord",
            license = "MIT license",
            description = "Arctic north-bluish clean palette"
        ),
        CreditCardItem(
            title = "Gruvbox",
            url = "https://github.com/morhetz/gruvbox",
            license = "MIT license",
            description = "Retro groove terminal color scheme"
        ),
        CreditCardItem(
            title = "Tokyo Night",
            url = "https://github.com/enkia/tokyo-night-vscode-theme",
            license = "MIT license",
            description = "Tokyo nocturnal neon dark aesthetics"
        ),
        CreditCardItem(
            title = "Solarized",
            url = "https://github.com/altercation/solarized",
            license = "MIT license",
            description = "Precision color palette for visual readability"
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .padding(bottom = 56.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // TOP HERO CARD: SRMusic Project
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Text(
                    text = "SRMusic Project",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(18.dp))

                coreTechs.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "• ",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledTonalButton(
                            onClick = {
                                try {
                                    uriHandler.openUri(item.url)
                                } catch (_: Exception) {}
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 10.dp,
                                vertical = 4.dp
                            ),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = "Source",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                contentDescription = "Open source",
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // SECTION 1: Source (Open Source Libraries)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Source",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
            )

            sourceLibraries.forEach { item ->
                CreditItemCard(
                    title = item.title,
                    url = item.url,
                    license = item.license,
                    description = item.description,
                    onClick = {
                        try {
                            uriHandler.openUri(item.url)
                        } catch (_: Exception) {}
                    }
                )
            }
        }

        // SECTION 2: Themes & Design References
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Themes & References",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
            )

            designReferences.forEach { item ->
                CreditItemCard(
                    title = item.title,
                    url = item.url,
                    license = item.license,
                    description = item.description,
                    onClick = {
                        try {
                            uriHandler.openUri(item.url)
                        } catch (_: Exception) {}
                    }
                )
            }
        }
    }
}

/**
 * Individual Rounded Card for Library / Theme / Reference
 */
@Composable
private fun CreditItemCard(
    title: String,
    url: String,
    license: String,
    description: String? = null,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = license,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (!description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    lineHeight = 16.sp
                )
            }
        }
    }
}
