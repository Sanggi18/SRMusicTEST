package com.example.ui.player.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Pure layout anchor component for Now Playing artwork.
 * Provides the rock-solid, static target geometry (position & size) for morphing transitions.
 * This anchor is completely free from graphicsLayer, scale, shadow, or visual transforms.
 */
@Composable
fun NowPlayingArtworkAnchor(
    onCoordinatesMeasured: ((LayoutCoordinates) -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val measuredModifier = if (onCoordinatesMeasured != null) {
        Modifier.onGloballyPositioned { coordinates ->
            onCoordinatesMeasured(coordinates)
        }
    } else Modifier

    Box(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .aspectRatio(1f)
            .then(measuredModifier),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
