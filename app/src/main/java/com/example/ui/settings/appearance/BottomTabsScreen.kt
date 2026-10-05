package com.example.ui.settings.appearance

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.navigation.components.TabItem
import com.example.ui.settings.components.SettingsCard
import com.example.ui.settings.components.SettingsDivider
import com.example.ui.settings.components.SettingsSectionHeader
import kotlinx.coroutines.launch

fun getTabIcon(tab: TabItem): ImageVector {
    return when (tab) {
        TabItem.HOME -> Icons.Rounded.Home
        TabItem.SONG -> Icons.Rounded.MusicNote
        TabItem.FOLDER -> Icons.Rounded.Folder
        TabItem.ARTIST -> Icons.Rounded.Person
        TabItem.ALBUM -> Icons.Rounded.Album
    }
}

@Composable
fun BottomTabsScreen(
    enabledTabs: List<TabItem>,
    onEnabledTabsChange: (List<TabItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    val availableTabs = TabItem.entries.filter { !enabledTabs.contains(it) }
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var targetIndex by remember { mutableIntStateOf(-1) }
    val dragAnimatable = remember { Animatable(0f) }
    var isSettling by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    var itemHeightPx by remember { mutableFloatStateOf(with(density) { 56.dp.toPx() }) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 48.dp)
    ) {
        // Active / Enabled Tabs Section
        SettingsSectionHeader("Enabled Navigation Tabs (${enabledTabs.size})")
        Text(
            text = "Hold and drag the handle to reorder tabs. Remove with the icon on the right (min 2 tabs required).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )

        SettingsCard {
            enabledTabs.forEachIndexed { index, tab ->
                key(tab.id) {
                    val isBeingDragged = draggingIndex == index
                    val isAnyItemDragging = draggingIndex != -1

                    // Target shift for non-dragged items
                    val targetShift = if (isAnyItemDragging && !isBeingDragged) {
                        when {
                            draggingIndex < targetIndex && index in (draggingIndex + 1)..targetIndex -> -itemHeightPx
                            draggingIndex > targetIndex && index in targetIndex until draggingIndex -> itemHeightPx
                            else -> 0f
                        }
                    } else 0f

                    val animatedShift by animateFloatAsState(
                        targetValue = targetShift,
                        animationSpec = if (isAnyItemDragging) {
                            spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        } else {
                            snap()
                        },
                        label = "tab_shift_${tab.id}"
                    )

                    val itemOffset = if (isBeingDragged) {
                        dragAnimatable.value
                    } else {
                        animatedShift
                    }

                    val currentDisplayPosition = when {
                        isBeingDragged -> (targetIndex + 1).coerceIn(1, enabledTabs.size)
                        draggingIndex != -1 -> {
                            when {
                                draggingIndex < targetIndex && index in (draggingIndex + 1)..targetIndex -> index
                                draggingIndex > targetIndex && index in targetIndex until draggingIndex -> index + 2
                                else -> index + 1
                            }
                        }
                        else -> index + 1
                    }

                    val elevation by animateFloatAsState(
                        targetValue = if (isBeingDragged) 8f else 0f,
                        label = "elevation_$index"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isBeingDragged) 10f else 1f)
                            .graphicsLayer {
                                shadowElevation = elevation
                                translationY = itemOffset
                            }
                            .background(
                                if (isBeingDragged) MaterialTheme.colorScheme.surfaceVariant
                                else Color.Transparent
                            )
                            .onGloballyPositioned { coordinates ->
                                if (coordinates.size.height > 0 && !isAnyItemDragging) {
                                    itemHeightPx = coordinates.size.height.toFloat()
                                }
                            }
                            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tab Icon badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = getTabIcon(tab),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Tab Title & position
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Position $currentDisplayPosition",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Drag Handle directly beside the remove button on the right side
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .pointerInput(enabledTabs, index) {
                                    detectVerticalDragGestures(
                                        onDragStart = {
                                            if (!isSettling) {
                                                draggingIndex = index
                                                targetIndex = index
                                                coroutineScope.launch {
                                                    dragAnimatable.snapTo(0f)
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            if (draggingIndex != -1) {
                                                val fromIdx = draggingIndex
                                                val toIdx = targetIndex
                                                if (fromIdx != toIdx && fromIdx in 0..enabledTabs.lastIndex && toIdx in 0..enabledTabs.lastIndex) {
                                                    val mutableList = enabledTabs.toMutableList()
                                                    val item = mutableList.removeAt(fromIdx)
                                                    mutableList.add(toIdx, item)
                                                    onEnabledTabsChange(mutableList)
                                                    draggingIndex = -1
                                                    targetIndex = -1
                                                    isSettling = false
                                                    coroutineScope.launch {
                                                        dragAnimatable.snapTo(0f)
                                                    }
                                                } else {
                                                    coroutineScope.launch {
                                                        isSettling = true
                                                        dragAnimatable.animateTo(
                                                            0f,
                                                            spring(
                                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                                stiffness = Spring.StiffnessMediumLow
                                                            )
                                                        )
                                                        draggingIndex = -1
                                                        targetIndex = -1
                                                        isSettling = false
                                                    }
                                                }
                                            }
                                        },
                                        onDragCancel = {
                                            if (draggingIndex != -1) {
                                                draggingIndex = -1
                                                targetIndex = -1
                                                isSettling = false
                                                coroutineScope.launch {
                                                    dragAnimatable.snapTo(0f)
                                                }
                                            }
                                        },
                                        onVerticalDrag = { change, dragAmount ->
                                            change.consume()
                                            if (draggingIndex != -1 && !isSettling) {
                                                coroutineScope.launch {
                                                    val newOffset = dragAnimatable.value + dragAmount
                                                    dragAnimatable.snapTo(newOffset)
                                                    val initialCenterY = (draggingIndex + 0.5f) * itemHeightPx
                                                    val currentCenterY = initialCenterY + newOffset
                                                    val computedTarget = (currentCenterY / itemHeightPx).toInt()
                                                        .coerceIn(0, enabledTabs.lastIndex)
                                                    if (computedTarget != targetIndex) {
                                                        targetIndex = computedTarget
                                                    }
                                                }
                                            }
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DragHandle,
                                contentDescription = "Drag to reorder ${tab.title}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Remove action on the far right
                        IconButton(
                            onClick = {
                                if (enabledTabs.size > 2) {
                                    onEnabledTabsChange(enabledTabs.filter { it != tab })
                                }
                            },
                            enabled = enabledTabs.size > 2
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Remove ${tab.title} from bottom bar",
                                tint = if (enabledTabs.size > 2) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }
                    }

                    if (index < enabledTabs.size - 1) {
                        SettingsDivider()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Available Tabs to Add back
        SettingsSectionHeader("Available Tabs (${availableTabs.size})")
        if (availableTabs.isEmpty()) {
            Text(
                text = "All available tabs are currently active in the bottom navigation bar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    availableTabs.forEachIndexed { index, tab ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = getTabIcon(tab),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    onEnabledTabsChange(enabledTabs + tab)
                                }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = "Add ${tab.title} to bottom bar",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (index < availableTabs.size - 1) {
                            SettingsDivider()
                        }
                    }
                }
            }
        }
    }
}
