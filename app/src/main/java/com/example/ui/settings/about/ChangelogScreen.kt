package com.example.ui.settings.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.settings.components.SettingsCard

@Composable
fun ChangelogScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 48.dp)
    ) {
        SettingsCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Version 1.0.0 Release Notes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "• Categorized Settings Architecture:\n" +
                            "  Separated Settings into 4 dedicated child categories (Appearance, Audio & Playback, Library, App) with full backstack support.\n\n" +
                            "• Material 3 App Theme & Dynamic Color Engine:\n" +
                            "  Independent Dynamic Color toggle, direct list selection of base modes (Follow System, Light, Dark, AMOLED), 5 curated custom presets, and rich RGB / Hex accent dialog.\n\n" +
                            "• Customizable Bottom Navigation Tabs:\n" +
                            "  Drag-handle reordering, tab removal with minimum 2 tabs requirement, and active tab restoration.\n\n" +
                            "• Hierarchical Folder Navigation:\n" +
                            "  Folder traversal with locked filesystem root support.\n\n" +
                            "• High-Precision Equalizer:\n" +
                            "  10-band graphic EQ, true vertical faders, Bit Perfect mode, and audio routing.\n\n" +
                            "• Full-Screen Now Playing Overlay:\n" +
                            "  Smooth vertical slide transitions covering bottom navigation bars.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
