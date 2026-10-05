package com.example.ui.common.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.common.theme.ExpressiveMotion
import com.example.ui.common.theme.springPress

/**
 * Material Design 3 Expressive SearchBar component.
 * Follows M3 Search specs: https://m3.material.io/components/search/overview
 * Morphs seamlessly between Resting (pill container) and Active (docked surface) states
 * with leading icon transformation (Menu <-> Back Arrow) and fluid spring physics.
 */
@Composable
fun SearchPill(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "Search library..."
) {
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    val isActive = isFocused || searchQuery.isNotEmpty()

    val isAmoled = MaterialTheme.colorScheme.background == com.example.ui.common.theme.AmoledBg
    val containerColor = if (isAmoled) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val pillShape = RoundedCornerShape(26.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(pillShape)
            .testTag("unified_search_pill"),
        shape = pillShape,
        color = containerColor,
        tonalElevation = if (isAmoled) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // [ Leading Action: Menu <-> Back Arrow ]
            val leadingInteractionSource = remember { MutableInteractionSource() }
            IconButton(
                onClick = {
                    if (isActive) {
                        onSearchQueryChange("")
                        focusManager.clearFocus()
                    } else {
                        onMenuClick()
                    }
                },
                interactionSource = leadingInteractionSource,
                modifier = Modifier
                    .size(44.dp)
                    .springPress(leadingInteractionSource, pressedScale = 0.90f)
                    .testTag(if (isActive) "search_back_button" else "menu_drawer_button")
            ) {
                AnimatedContent(
                    targetState = isActive,
                    transitionSpec = {
                        fadeIn(animationSpec = ExpressiveMotion.SnappySpring) togetherWith
                                fadeOut(animationSpec = ExpressiveMotion.SnappySpring)
                    },
                    label = "search_leading_icon"
                ) { active ->
                    if (active) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                            contentDescription = "Exit search",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "Open navigation drawer",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // [ Search Input ]
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = placeholderText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                        }
                        .testTag("search_input_field")
                )
            }

            // [ Trailing Icon: Clear / Search ]
            if (searchQuery.isNotEmpty()) {
                val clearInteractionSource = remember { MutableInteractionSource() }
                IconButton(
                    onClick = {
                        onSearchQueryChange("")
                    },
                    interactionSource = clearInteractionSource,
                    modifier = Modifier
                        .size(44.dp)
                        .springPress(clearInteractionSource, pressedScale = 0.90f)
                        .testTag("search_clear_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
