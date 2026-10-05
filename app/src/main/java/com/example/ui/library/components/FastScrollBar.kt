package com.example.ui.library.components
import com.example.ui.library.core.*
import com.example.ui.common.components.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.snapshotFlow


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridLayoutInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt


/**
 * Estimasi posisi list dalam pixel-space (seperti RecyclerView.computeVerticalScrollOffset).
 */
private class ScrollMetrics(val avgItemPx: Float, val currentPx: Float, val maxScrollPx: Float)


private fun LazyListLayoutInfo.estimate(itemCount: Int): ScrollMetrics? {
    val visible = visibleItemsInfo
    if (visible.isEmpty() || itemCount <= 0) return null
    // Rata-rata tinggi item visible + spacing -> stabil walau ada header/item beda tinggi
    val avg = visible.sumOf { it.size }.toFloat() / visible.size + mainAxisItemSpacing
    if (avg <= 0f) return null
    val viewport = (viewportEndOffset - viewportStartOffset).toFloat()
    val total = avg * itemCount + beforeContentPadding + afterContentPadding
    val maxScroll = (total - viewport).coerceAtLeast(1f)
    val first = visible.first()
    val current = (first.index * avg - first.offset).coerceIn(0f, maxScroll)
    return ScrollMetrics(avg, current, maxScroll)
}


private fun LazyGridLayoutInfo.estimate(itemCount: Int): ScrollMetrics? {
    val visible = visibleItemsInfo
    if (visible.isEmpty() || itemCount <= 0) return null
    var maxCol = 0
    for (i in visible.indices) {
        if (visible[i].column > maxCol) maxCol = visible[i].column
    }
    val columns = (maxCol + 1).coerceAtLeast(1)
    val totalRows = ceil(itemCount / columns.toFloat()).toInt().coerceAtLeast(1)


    var sumHeight = 0
    for (i in visible.indices) {
        sumHeight += visible[i].size.height
    }
    val avg = (sumHeight.toFloat() / visible.size) + mainAxisItemSpacing
    if (avg <= 0f) return null


    val viewport = (viewportEndOffset - viewportStartOffset).toFloat()
    val total = avg * totalRows + beforeContentPadding + afterContentPadding
    val maxScroll = (total - viewport).coerceAtLeast(1f)
    val first = visible.first()
    val current = (first.row * avg - first.offset.y).coerceIn(0f, maxScroll)
    return ScrollMetrics(avg, current, maxScroll)
}


val LocalMiniPlayerVisible = compositionLocalOf { false }


@Composable
fun FastScrollBar(
    listState: LazyListState,
    itemCount: Int,
    modifier: Modifier = Modifier,
    previewTextProvider: ((Int) -> String)? = null,
    topPadding: Dp = 8.dp,
    bottomPadding: Dp? = null,
    hasMiniPlayer: Boolean = LocalMiniPlayerVisible.current,
    directScroll: Boolean = false,
    onDraggingChange: ((Boolean) -> Unit)? = null,
    onHighSpeedChange: ((Boolean) -> Unit)? = null
) {
    val canScroll by remember {
        derivedStateOf { listState.canScrollForward || listState.canScrollBackward }
    }
    if (!canScroll) return


    val density = LocalDensity.current
    val count by rememberUpdatedState(itemCount)
    val provider by rememberUpdatedState(previewTextProvider)
    val currentDirectScroll by rememberUpdatedState(directScroll)

    val navBarBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val effectiveBottomPadding = bottomPadding ?: if (hasMiniPlayer) {
        142.dp + navBarBottomInset
    } else {
        80.dp + navBarBottomInset
    }

    val thumbHeight = 28.dp
    val thumbPx = with(density) { thumbHeight.toPx() }
    val topPx = with(density) { topPadding.toPx() }
    val bottomPx = with(density) { effectiveBottomPadding.toPx() }

    val currentTopPx by rememberUpdatedState(topPx)
    val currentBottomPx by rememberUpdatedState(bottomPx)


    var isDragging by remember { mutableStateOf(false) }
    var isFastScrollingActive by remember { mutableStateOf(false) }
    var trackHeightPx by remember { mutableFloatStateOf(1f) }
    var previewHeightPx by remember { mutableFloatStateOf(1f) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    var previewText by remember { mutableStateOf<String?>(null) }


    // Event-driven velocity detection: calculate velocity only when scroll position actually changes
    LaunchedEffect(listState) {
        var lastOffset = 0f
        var lastTime = System.currentTimeMillis()
        snapshotFlow {
            if (listState.isScrollInProgress) {
                listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
            } else {
                null
            }
        }.collect { scrollPos ->
            val now = System.currentTimeMillis()
            val currentOffset = listState.layoutInfo.estimate(count)?.currentPx ?: 0f
            if (scrollPos != null) {
                val dt = (now - lastTime).coerceAtLeast(1)
                val distance = abs(currentOffset - lastOffset)
                val velocity = (distance / dt) * 1000f // px per second

                if (velocity > 2500f) {
                    isFastScrollingActive = true
                }
            }
            lastOffset = currentOffset
            lastTime = now
        }
    }


    // Auto-hide fast scrollbar after fast scrolling finishes
    LaunchedEffect(isFastScrollingActive, listState.isScrollInProgress, isDragging) {
        if (isFastScrollingActive && !listState.isScrollInProgress && !isDragging) {
            delay(1200)
            isFastScrollingActive = false
        }
    }


    val isVisible = isDragging || isFastScrollingActive
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "fast_scroll_alpha"
    )


    // Hanya dibaca di graphicsLayer -> tidak memicu recomposition
    val scrollFraction by remember {
        derivedStateOf {
            val m = listState.layoutInfo.estimate(count) ?: return@derivedStateOf 0f
            (m.currentPx / m.maxScrollPx).coerceIn(0f, 1f)
        }
    }


    // Scroll handling during dragging
    LaunchedEffect(isDragging, directScroll) {
        onDraggingChange?.invoke(isDragging)
        onHighSpeedChange?.invoke(isDragging)
        if (!isDragging) return@LaunchedEffect


        if (directScroll) {
            // Cancel any active fling or gesture scroll immediately so direct positioning has exclusive control
            listState.scroll(MutatePriority.UserInput) { }
            return@LaunchedEffect
        }


        var lastFraction = -1f
        while (isDragging) {
            withFrameNanos { }
            val frac = dragFraction
            if (frac == lastFraction) continue
            lastFraction = frac


            val info = listState.layoutInfo
            val m = info.estimate(count) ?: continue
            val visible = info.visibleItemsInfo
            if (visible.isEmpty()) continue


            // fraction -> pixel -> (index, offset)
            val targetPx = frac * m.maxScrollPx
            val targetIndex = (targetPx / m.avgItemPx).toInt().coerceIn(0, count - 1)
            val targetOffsetPx = (targetPx - targetIndex * m.avgItemPx).roundToInt().coerceAtLeast(0)


            val targetItem = visible.firstOrNull { it.index == targetIndex }
            val firstVisible = visible.first()
            val lastVisible = visible.last()
            val nearRange = (firstVisible.index - visible.size)..(lastVisible.index + visible.size)


            when {
                // Item target sudah ter-layout: pakai posisi NYATA. Positif = maju (sama dengan scrollBy)
                targetItem != null -> {
                    val delta = (targetItem.offset + targetOffsetPx - info.viewportStartOffset).toFloat()
                    if (abs(delta) >= 0.5f) listState.dispatchRawDelta(delta)
                }
                // Dekat (±1 layar): tetap pakai delta pixel supaya konten "mengalir", bukan snap
                targetIndex in nearRange -> {
                    val delta = targetPx - m.currentPx
                    if (abs(delta) >= 0.5f) listState.dispatchRawDelta(delta)
                }
                // Lompatan jauh: satu-satunya kasus yang butuh jump
                else -> listState.requestScrollToItem(targetIndex, targetOffsetPx)
            }
        }
    }


    Box(
        modifier = modifier
            .fillMaxHeight()
            .testTag("fast_scroll_bar")
    ) {
        if (isDragging && !previewText.isNullOrBlank()) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 68.dp)
                    .graphicsLayer {
                        val maxH = (trackHeightPx - topPx - bottomPx - thumbPx).coerceAtLeast(0f)
                        val thumbCenter = topPx + maxH * dragFraction + thumbPx / 2f
                        translationY = (thumbCenter - previewHeightPx / 2f).coerceIn(
                            topPx, (trackHeightPx - bottomPx - previewHeightPx).coerceAtLeast(topPx)
                        )
                    }
                    .onSizeChanged { previewHeightPx = it.height.toFloat() }
                    .testTag("fast_scroll_preview")
            ) {
                Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = previewText!!,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 20.sp
                        )
                    )
                }
            }
        }


        if (alpha > 0.01f) {
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(44.dp)
                    .graphicsLayer { this.alpha = alpha }
                    .onSizeChanged { trackHeightPx = it.height.toFloat().coerceAtLeast(1f) }
                    .pointerInput(isVisible) {
                        if (!isVisible) return@pointerInput
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val scrollable = (trackHeightPx - currentTopPx - currentBottomPx - thumbPx).coerceAtLeast(1f)
                            val currentFraction = if (isDragging) dragFraction else scrollFraction
                            val currentThumbTop = currentTopPx + scrollable * currentFraction


                            // Only trigger dragging if touch started within or near the thumb (+/- 24dp)
                            val touchTolerance = 24.dp.toPx()
                            if (down.position.y < (currentThumbTop - touchTolerance) ||
                                down.position.y > (currentThumbTop + thumbPx + touchTolerance)
                            ) {
                                return@awaitEachGesture
                            }


                            down.consume()
                            var lastIndex = -1


                            fun update(y: Float) {
                                val f = ((y - currentTopPx - thumbPx / 2f) / scrollable).coerceIn(0f, 1f)
                                dragFraction = f


                                if (currentDirectScroll) {
                                    if (count > 0) {
                                        val targetIndex = (f * (count - 1)).roundToInt().coerceIn(0, count - 1)


                                        if (targetIndex != lastIndex) {
                                            lastIndex = targetIndex
                                            listState.requestScrollToItem(targetIndex, 0)
                                            val letter = provider?.invoke(targetIndex)?.trim()
                                                ?.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
                                            previewText = if (letter.isNotEmpty()) letter else null
                                        }
                                    }
                                } else {
                                    val m = listState.layoutInfo.estimate(count) ?: return
                                    val idx = ((f * m.maxScrollPx) / m.avgItemPx).toInt().coerceIn(0, count - 1)
                                    if (idx != lastIndex) {
                                        lastIndex = idx
                                        val letter = provider?.invoke(idx)?.trim()
                                            ?.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
                                        previewText = letter
                                    }
                                }
                            }


                            isDragging = true
                            update(down.position.y)
                            try {
                                do {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull() ?: break
                                    change.consume()
                                    update(change.position.y)
                                } while (change.pressed)
                            } finally {
                                isDragging = false
                                previewText = null
                            }
                        }
                    }
                    .testTag("fast_scroll_scrollbar_container")
            ) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 6.dp)
                        .graphicsLayer {
                            val f = if (isDragging) dragFraction else scrollFraction
                            val maxH = (trackHeightPx - topPx - bottomPx - thumbPx).coerceAtLeast(0f)
                            translationY = topPx + maxH * f
                        }
                        .width(if (isDragging) 8.dp else 6.dp)
                        .height(thumbHeight)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isDragging) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        .testTag("fast_scroll_thumb")
                )
            }
        }
    }
}


@Composable
fun FastScrollBarGrid(
    gridState: LazyGridState,
    itemCount: Int,
    modifier: Modifier = Modifier,
    previewTextProvider: ((Int) -> String)? = null,
    topPadding: Dp = 8.dp,
    bottomPadding: Dp? = null,
    hasMiniPlayer: Boolean = LocalMiniPlayerVisible.current,
    onDraggingChange: ((Boolean) -> Unit)? = null,
    onHighSpeedChange: ((Boolean) -> Unit)? = null
) {
    val canScroll by remember {
        derivedStateOf { gridState.canScrollForward || gridState.canScrollBackward }
    }
    if (!canScroll) return


    val density = LocalDensity.current
    val count by rememberUpdatedState(itemCount)
    val provider by rememberUpdatedState(previewTextProvider)

    val navBarBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val effectiveBottomPadding = bottomPadding ?: if (hasMiniPlayer) {
        142.dp + navBarBottomInset
    } else {
        80.dp + navBarBottomInset
    }

    val thumbHeight = 28.dp
    val thumbPx = with(density) { thumbHeight.toPx() }
    val topPx = with(density) { topPadding.toPx() }
    val bottomPx = with(density) { effectiveBottomPadding.toPx() }


    var isDragging by remember { mutableStateOf(false) }
    var isFastScrollingActive by remember { mutableStateOf(false) }
    var trackHeightPx by remember { mutableFloatStateOf(1f) }
    var previewHeightPx by remember { mutableFloatStateOf(1f) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    var previewText by remember { mutableStateOf<String?>(null) }


    // Event-driven velocity detection: calculate velocity only when scroll position actually changes
    LaunchedEffect(gridState) {
        var lastOffset = 0f
        var lastTime = System.currentTimeMillis()
        snapshotFlow {
            if (gridState.isScrollInProgress) {
                gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset
            } else {
                null
            }
        }.collect { scrollPos ->
            val now = System.currentTimeMillis()
            val currentOffset = gridState.layoutInfo.estimate(count)?.currentPx ?: 0f
            if (scrollPos != null) {
                val dt = (now - lastTime).coerceAtLeast(1)
                val distance = abs(currentOffset - lastOffset)
                val velocity = (distance / dt) * 1000f // px per second

                if (velocity > 2500f) {
                    isFastScrollingActive = true
                }
            }
            lastOffset = currentOffset
            lastTime = now
        }
    }


    // Auto-hide fast scrollbar after fast scrolling finishes
    LaunchedEffect(isFastScrollingActive, gridState.isScrollInProgress, isDragging) {
        if (isFastScrollingActive && !gridState.isScrollInProgress && !isDragging) {
            delay(1200)
            isFastScrollingActive = false
        }
    }


    val isVisible = isDragging || isFastScrollingActive
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "fast_scroll_grid_alpha"
    )


    val scrollFraction by remember {
        derivedStateOf {
            val m = gridState.layoutInfo.estimate(count) ?: return@derivedStateOf 0f
            (m.currentPx / m.maxScrollPx).coerceIn(0f, 1f)
        }
    }


    LaunchedEffect(isDragging) {
        onDraggingChange?.invoke(isDragging)
        onHighSpeedChange?.invoke(isDragging)
        if (!isDragging) return@LaunchedEffect


        var lastFraction = -1f
        while (isDragging) {
            withFrameNanos { }
            val frac = dragFraction
            if (frac == lastFraction) continue
            lastFraction = frac


            val info = gridState.layoutInfo
            val m = info.estimate(count) ?: continue
            val visible = info.visibleItemsInfo
            if (visible.isEmpty()) continue


            var maxCol = 0
            for (i in visible.indices) {
                if (visible[i].column > maxCol) maxCol = visible[i].column
            }
            val columns = (maxCol + 1).coerceAtLeast(1)
            val totalRows = ceil(count / columns.toFloat()).toInt().coerceAtLeast(1)


            val targetPx = frac * m.maxScrollPx
            val targetRow = (targetPx / m.avgItemPx).toInt().coerceIn(0, totalRows - 1)
            val targetOffsetPx = (targetPx - targetRow * m.avgItemPx).roundToInt().coerceAtLeast(0)
            val targetIndex = (targetRow * columns).coerceIn(0, count - 1)


            val targetItem = visible.firstOrNull { it.index == targetIndex }
            val firstVisible = visible.first()
            val lastVisible = visible.last()
            val nearRange = (firstVisible.index - visible.size)..(lastVisible.index + visible.size)


            when {
                targetItem != null -> {
                    val delta = (targetItem.offset.y + targetOffsetPx - info.viewportStartOffset).toFloat()
                    if (abs(delta) >= 0.5f) gridState.dispatchRawDelta(delta)
                }
                targetIndex in nearRange -> {
                    val delta = targetPx - m.currentPx
                    if (abs(delta) >= 0.5f) gridState.dispatchRawDelta(delta)
                }
                else -> gridState.requestScrollToItem(targetIndex, targetOffsetPx)
            }
        }
    }


    Box(
        modifier = modifier
            .fillMaxHeight()
            .testTag("fast_scroll_bar_grid")
    ) {
        if (isDragging && !previewText.isNullOrBlank()) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 68.dp)
                    .graphicsLayer {
                        val maxH = (trackHeightPx - topPx - bottomPx - thumbPx).coerceAtLeast(0f)
                        val thumbCenter = topPx + maxH * dragFraction + thumbPx / 2f
                        translationY = (thumbCenter - previewHeightPx / 2f).coerceIn(
                            topPx, (trackHeightPx - bottomPx - previewHeightPx).coerceAtLeast(topPx)
                        )
                    }
                    .onSizeChanged { previewHeightPx = it.height.toFloat() }
                    .testTag("fast_scroll_preview")
            ) {
                Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = previewText!!,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 20.sp
                        )
                    )
                }
            }
        }


        if (alpha > 0.01f) {
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(44.dp)
                    .graphicsLayer { this.alpha = alpha }
                    .onSizeChanged { trackHeightPx = it.height.toFloat().coerceAtLeast(1f) }
                    .pointerInput(isVisible) {
                        if (!isVisible) return@pointerInput
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val scrollable = (trackHeightPx - topPx - bottomPx - thumbPx).coerceAtLeast(1f)
                            val currentFraction = if (isDragging) dragFraction else scrollFraction
                            val currentThumbTop = topPx + scrollable * currentFraction


                            // Only trigger dragging if touch started within or near the thumb (+/- 24dp)
                            val touchTolerance = 24.dp.toPx()
                            if (down.position.y < (currentThumbTop - touchTolerance) ||
                                down.position.y > (currentThumbTop + thumbPx + touchTolerance)
                            ) {
                                return@awaitEachGesture
                            }


                            down.consume()
                            var lastIndex = -1


                            fun update(y: Float) {
                                val f = ((y - topPx - thumbPx / 2f) / scrollable).coerceIn(0f, 1f)
                                dragFraction = f
                                val m = gridState.layoutInfo.estimate(count) ?: return
                                val visible = gridState.layoutInfo.visibleItemsInfo
                                var maxCol = 0
                                for (i in visible.indices) {
                                    if (visible[i].column > maxCol) maxCol = visible[i].column
                                }
                                val columns = (maxCol + 1).coerceAtLeast(1)
                                val totalRows = ceil(count / columns.toFloat()).toInt().coerceAtLeast(1)
                                val targetRow = ((f * m.maxScrollPx) / m.avgItemPx).toInt().coerceIn(0, totalRows - 1)
                                val idx = (targetRow * columns).coerceIn(0, count - 1)


                                if (idx != lastIndex) {
                                    lastIndex = idx
                                    val letter = provider?.invoke(idx)?.trim()
                                        ?.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
                                    previewText = letter
                                }
                            }


                            isDragging = true
                            update(down.position.y)
                            try {
                                do {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull() ?: break
                                    change.consume()
                                    update(change.position.y)
                                } while (change.pressed)
                            } finally {
                                isDragging = false
                                previewText = null
                            }
                        }
                    }
                    .testTag("fast_scroll_scrollbar_container")
            ) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 6.dp)
                        .graphicsLayer {
                            val f = if (isDragging) dragFraction else scrollFraction
                            val maxH = (trackHeightPx - topPx - bottomPx - thumbPx).coerceAtLeast(0f)
                            translationY = topPx + maxH * f
                        }
                        .width(if (isDragging) 8.dp else 6.dp)
                        .height(thumbHeight)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isDragging) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        .testTag("fast_scroll_thumb")
                )
            }
        }
    }
}




