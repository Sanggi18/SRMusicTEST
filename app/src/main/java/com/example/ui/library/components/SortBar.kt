package com.example.ui.library.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class SortCriteria(val label: String) {
    TITLE("Title"),
    ARTIST("Artist"),
    ALBUM("Album"),
    DATE_ADDED("Date Added"),
    DATE_MODIFIED("Date Modified"),
    DURATION("Duration"),
    COUNT("Count Song")
}

enum class GridMode(val columns: Int, val label: String) {
    LIST(1, "List"),
    GRID_2(2, "Grid 2"),
    GRID_3(3, "Grid 3"),
    GRID_4(4, "Grid 4")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBar(
    sortCriteria: SortCriteria,
    isAscending: Boolean,
    onSortCriteriaChange: (SortCriteria) -> Unit,
    modifier: Modifier = Modifier,
    onAscendingChange: ((Boolean) -> Unit)? = null,
    onToggleSortOrder: (() -> Unit)? = null,
    onShuffleClick: (() -> Unit)? = null,
    totalCount: Int = 0,
    countLabel: String = "items",
    gridMode: GridMode? = null,
    onGridModeChange: ((GridMode) -> Unit)? = null,
    availableCriteria: List<SortCriteria> = listOf(
        SortCriteria.TITLE,
        SortCriteria.ARTIST,
        SortCriteria.ALBUM,
        SortCriteria.DATE_ADDED,
        SortCriteria.DATE_MODIFIED
    ),
    customLabelProvider: ((SortCriteria) -> String)? = null
) {
    var showSortSheet by remember { mutableStateOf(false) }

    val isAmoled = MaterialTheme.colorScheme.background == com.example.ui.common.theme.AmoledBg
    val chipContainerColor = if (isAmoled) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Count & Criteria Chip
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = chipContainerColor,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .testTag("sort_bar_chip")
        ) {
            Row(
                modifier = Modifier
                    .clickable { showSortSheet = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Sort,
                    contentDescription = "Sort options",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val displayCriteriaLabel = customLabelProvider?.invoke(sortCriteria) ?: sortCriteria.label
                Text(
                    text = "$displayCriteriaLabel ($totalCount $countLabel)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Shuffle Button with text and matching container background
            if (onShuffleClick != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = chipContainerColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onShuffleClick() }
                        .testTag("sort_bar_shuffle_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Shuffle",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Optional Grid Mode switcher
            if (gridMode != null && onGridModeChange != null) {
                Spacer(modifier = Modifier.width(4.dp))
                val nextMode = when (gridMode) {
                    GridMode.LIST -> GridMode.GRID_2
                    GridMode.GRID_2 -> GridMode.GRID_3
                    GridMode.GRID_3 -> GridMode.GRID_4
                    GridMode.GRID_4 -> GridMode.LIST
                }
                val modeIcon = when (gridMode) {
                    GridMode.LIST -> Icons.Rounded.ViewList
                    GridMode.GRID_2 -> Icons.Rounded.GridView
                    GridMode.GRID_3 -> Icons.Rounded.ViewModule
                    GridMode.GRID_4 -> Icons.Rounded.Widgets
                }
                IconButton(
                    onClick = { onGridModeChange(nextMode) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("sort_bar_grid_mode_button")
                ) {
                    Icon(
                        imageVector = modeIcon,
                        contentDescription = "View mode: ${gridMode.label}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showSortSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Sort by",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                // ASC / DESC Selector at the very top
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isAscending) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onAscendingChange?.invoke(true) ?: onToggleSortOrder?.invoke()
                            }
                            .testTag("sort_order_asc")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowUpward,
                                contentDescription = "Ascending",
                                tint = if (isAscending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ascending",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isAscending) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isAscending) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (!isAscending) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onAscendingChange?.invoke(false) ?: onToggleSortOrder?.invoke()
                            }
                            .testTag("sort_order_desc")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowDownward,
                                contentDescription = "Descending",
                                tint = if (!isAscending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Descending",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (!isAscending) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (!isAscending) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Criteria list
                availableCriteria.forEach { criteria ->
                    val isSelected = criteria == sortCriteria
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSortCriteriaChange(criteria)
                                showSortSheet = false
                            }
                            .testTag("sort_criteria_${criteria.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val itemCriteriaLabel = customLabelProvider?.invoke(criteria) ?: criteria.label
                            Text(
                                text = itemCriteriaLabel,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

