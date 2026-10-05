package com.example.ui.common.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Material Design 3 Expressive Motion Specifications.
 * Uses physics-based spring curves for organic, fluid, responsive animations.
 * Reference: https://m3.material.io/styles/motion/overview
 */
object ExpressiveMotion {

    /**
     * Expressive bouncy spring for energetic micro-interactions (presses, badges, toggles).
     */
    val BouncySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    /**
     * Snappy spring for quick, responsive UI elements (switches, tabs, small sheets).
     */
    val SnappySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /**
     * Gentle spring for large surface expansions, page transitions, and sheets.
     */
    val GentleSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    /**
     * Expressive spatial morph spec for smooth corner radius and dimension morphing.
     */
    val SpatialSpring = spring<Float>(
        dampingRatio = 0.7f,
        stiffness = 300f
    )

    val SpatialSpringDp = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = 0.7f,
        stiffness = 300f
    )

    val SnappySpringDp = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val SlideSpringIntOffset = spring<androidx.compose.ui.unit.IntOffset>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val StandardDecelTween = tween<Float>(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )
}

/**
 * Modifier that applies Material Expressive spring bounce when an element is pressed.
 * Replaces flat, static touch feedback with tactile depth.
 */
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.95f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = ExpressiveMotion.BouncySpring,
        label = "expressive_press_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null, // Custom visual feedback via scale; ripple can be chained or included
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Applies only the spring press scale without consuming clicks (useful when child handles click).
 */
fun Modifier.springPress(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.94f
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = ExpressiveMotion.BouncySpring,
        label = "expressive_spring_press"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
