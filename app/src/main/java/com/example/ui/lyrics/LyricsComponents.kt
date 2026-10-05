package com.example.ui.lyrics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.PlaybackProgress
import com.example.core.model.Song
import com.example.lyrics.LyricLine
import com.example.lyrics.LyricsContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun InstrumentalDotsIndicator(
    introProgress: Float,
    isIntroActive: Boolean,
    isFirstLineActive: Boolean,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
) {
    // When first line is active, dots are dimmed into completed state (past line position)
    val dotTargetActiveColor = if (isFirstLineActive) inactiveColor.copy(alpha = 0.35f) else activeColor

    val isDot1Active = introProgress >= 0.05f || isFirstLineActive
    val isDot2Active = introProgress >= 0.40f || isFirstLineActive
    val isDot3Active = introProgress >= 0.75f || isFirstLineActive

    val dot1Color by animateColorAsState(
        targetValue = if (isDot1Active) dotTargetActiveColor else inactiveColor,
        animationSpec = tween(250),
        label = "dot1_color"
    )
    val dot2Color by animateColorAsState(
        targetValue = if (isDot2Active) dotTargetActiveColor else inactiveColor,
        animationSpec = tween(250),
        label = "dot2_color"
    )
    val dot3Color by animateColorAsState(
        targetValue = if (isDot3Active) dotTargetActiveColor else inactiveColor,
        animationSpec = tween(250),
        label = "dot3_color"
    )

    val dot1Size by animateDpAsState(
        targetValue = if (isDot1Active && !isFirstLineActive) 11.dp else 9.dp,
        animationSpec = tween(250),
        label = "dot1_size"
    )
    val dot2Size by animateDpAsState(
        targetValue = if (isDot2Active && !isFirstLineActive) 11.dp else 9.dp,
        animationSpec = tween(250),
        label = "dot2_size"
    )
    val dot3Size by animateDpAsState(
        targetValue = if (isDot3Active && !isFirstLineActive) 11.dp else 9.dp,
        animationSpec = tween(250),
        label = "dot3_size"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(dot1Size)
                .clip(CircleShape)
                .background(dot1Color)
        )
        Box(
            modifier = Modifier
                .size(dot2Size)
                .clip(CircleShape)
                .background(dot2Color)
        )
        Box(
            modifier = Modifier
                .size(dot3Size)
                .clip(CircleShape)
                .background(dot3Color)
        )
    }
}

@Composable
fun SyncedLyricsPanel(
    song: Song,
    lyrics: List<LyricLine>,
    playbackProgressFlow: StateFlow<PlaybackProgress>,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    activeColor: Color = MaterialTheme.colorScheme.onBackground,
    inactiveColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val listState = rememberLazyListState()
    val isUserDragging by listState.interactionSource.collectIsDraggedAsState()
    var autoFollow by remember { mutableStateOf(true) }
    var activeIndex by remember(song.id, lyrics) { mutableIntStateOf(-1) }
    var currentVisualPositionMs by remember(song.id) { mutableLongStateOf(0L) }

    val firstLineStartMs = remember(lyrics) { lyrics.firstOrNull()?.startMs ?: 0L }
    val hasIntro = firstLineStartMs >= 1500L
    var isIntroActive by remember(song.id, lyrics) { mutableStateOf(hasIntro) }
    var introProgress by remember(song.id, lyrics) { mutableStateOf(0f) }

    val syncedContent = remember(lyrics) { LyricsContent.Synced(lyrics) }

    // Monotonic Frame Clock Interpolation + Single Progress Collector
    LaunchedEffect(song.id, lyrics, isPlaying) {
        var lastAnchorPositionMs = 0L
        var lastAnchorFrameNanos = 0L
        var lastActiveIndex = Int.MIN_VALUE

        // Single collector to anchor playback progress
        launch {
            playbackProgressFlow.collect { progress ->
                val currentPos = progress.currentPosition
                val nowNanos = System.nanoTime()

                // Check for discontinuity (> 400ms from predicted or backwards jump)
                val expectedPos = if (lastAnchorFrameNanos > 0L && isPlaying) {
                    lastAnchorPositionMs + (nowNanos - lastAnchorFrameNanos) / 1_000_000L
                } else {
                    currentPos
                }

                val isDiscontinuity = kotlin.math.abs(currentPos - expectedPos) > 400L || currentPos < lastAnchorPositionMs

                lastAnchorPositionMs = currentPos
                lastAnchorFrameNanos = nowNanos
                currentVisualPositionMs = currentPos

                // Calculate progressive intro countdown
                if (hasIntro) {
                    val threshold = firstLineStartMs - 150L
                    isIntroActive = currentPos < threshold
                    introProgress = if (threshold > 0) (currentPos.toFloat() / threshold).coerceIn(0f, 1f) else 1f
                }

                // When paused or discontinuous, immediately update activeIndex
                if (!isPlaying || isDiscontinuity) {
                    val targetPos = currentPos + 150L // 150ms visual anticipation
                    val idx = syncedContent.indexAt(targetPos)
                    if (idx != lastActiveIndex) {
                        lastActiveIndex = idx
                        activeIndex = idx
                    }
                }
            }
        }

        // Frame loop runs ONLY while isPlaying == true
        if (isPlaying) {
            while (isActive) {
                withFrameNanos { frameTimeNanos ->
                    if (lastAnchorFrameNanos > 0L) {
                        val elapsedMs = (frameTimeNanos - lastAnchorFrameNanos) / 1_000_000L
                        val estimatedPositionMs = lastAnchorPositionMs + elapsedMs
                        currentVisualPositionMs = estimatedPositionMs

                        if (hasIntro) {
                            val threshold = firstLineStartMs - 150L
                            isIntroActive = estimatedPositionMs < threshold
                            introProgress = if (threshold > 0) (estimatedPositionMs.toFloat() / threshold).coerceIn(0f, 1f) else 1f
                        }

                        val targetPos = estimatedPositionMs + 150L // 150ms visual anticipation
                        val idx = syncedContent.indexAt(targetPos)
                        if (idx != lastActiveIndex) {
                            lastActiveIndex = idx
                            activeIndex = idx
                        }
                    }
                }
            }
        }
    }

    // Auto-follow: User manual drag pauses auto-follow; resumes 2.5s after user stops dragging
    LaunchedEffect(isUserDragging) {
        if (isUserDragging) {
            autoFollow = false
        } else {
            delay(2_500L)
            autoFollow = true
        }
    }

    // The 3 dots remain visible during intro AND during the 1st line (red box position),
    // and only fade out when the 1st line finishes and the 2nd line begins (activeIndex >= 1)
    val shouldShowIntroDots = hasIntro && activeIndex <= 0

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        LaunchedEffect(activeIndex, autoFollow, shouldShowIntroDots) {
            if (!autoFollow) return@LaunchedEffect

            if (shouldShowIntroDots) {
                // While intro or 1st line is active: keep 3 dots at top (red box position) and 1st line right below it
                listState.animateScrollToItem(index = 0, scrollOffset = 0)
            } else if (activeIndex in lyrics.indices) {
                // Once 1st line finishes and 2nd line starts, scroll naturally
                val targetIndex = if (hasIntro) activeIndex + 1 else activeIndex
                listState.animateScrollToItem(
                    index = targetIndex,
                    scrollOffset = 0
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 360.dp,
                start = 24.dp,
                end = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            if (hasIntro) {
                item(key = "intro_prelude_dots") {
                    AnimatedVisibility(
                        visible = shouldShowIntroDots,
                        enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                        exit = fadeOut(tween(400, easing = FastOutSlowInEasing)) + shrinkVertically(tween(400, easing = FastOutSlowInEasing))
                    ) {
                        InstrumentalDotsIndicator(
                            introProgress = introProgress,
                            isIntroActive = isIntroActive,
                            isFirstLineActive = activeIndex == 0,
                            activeColor = accentColor,
                            inactiveColor = inactiveColor.copy(alpha = 0.20f)
                        )
                    }
                }
            }

            itemsIndexed(
                items = lyrics,
                key = { index, line -> "${line.startMs}_${index}" }
            ) { index, line ->
                val isActive = index == activeIndex

                // Pre-illumination transition: smooth warming-up when within 280ms of starting
                val isNextLine = index == activeIndex + 1
                val timeUntilStart = line.startMs - currentVisualPositionMs
                val isApproaching = isNextLine && timeUntilStart in 0L..280L
                val preIlluminationAlpha = if (isApproaching) {
                    0.35f + 0.35f * (1f - (timeUntilStart / 280f).coerceIn(0f, 1f))
                } else if (isActive) {
                    1.0f
                } else {
                    0.35f
                }

                val animatedWeight by animateFloatAsState(
                    targetValue = if (isActive) 1.0f else if (isApproaching) preIlluminationAlpha else 0.0f,
                    animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                    label = "lyric_weight"
                )

                val currentColor = lerp(inactiveColor, activeColor, animatedWeight)
                val currentScale = 1.0f + 0.025f * animatedWeight

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = currentScale
                            scaleY = currentScale
                        }
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = line.text,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = if (isActive || animatedWeight > 0.6f) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = if (isActive) 29.sp else 24.sp,
                            lineHeight = if (isActive) 40.sp else 34.sp
                        ),
                        color = currentColor
                    )
                }
            }
        }
    }
}

@Composable
fun PlainLyricsPanel(
    text: String,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp, start = 24.dp, end = 24.dp)
    ) {
        item {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 20.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.90f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun LyricsLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Loading lyrics...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LyricsEmpty(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Lyrics,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "No lyrics found",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "No embedded lyrics or local .lrc file found for this track.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
