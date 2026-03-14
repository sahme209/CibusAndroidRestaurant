package com.cibus.restaurant.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing

/**
 * Cibus Motion System — all animations under 300ms.
 * Use these tokens instead of hardcoding durations.
 */
object CibusMotion {
    // Speed constants (ms)
    const val FAST   = 150
    const val NORMAL = 220
    const val SLOW   = 300

    // Spring presets
    val buttonSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessHigh
    )
    val cardSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness    = Spring.StiffnessMedium
    )
    val iconSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessHigh
    )

    // Tween presets
    fun tweenFast()   = tween<Float>(durationMillis = FAST,   easing = FastOutSlowInEasing)
    fun tweenNormal() = tween<Float>(durationMillis = NORMAL, easing = FastOutSlowInEasing)
    fun tweenSlow()   = tween<Float>(durationMillis = SLOW,   easing = FastOutSlowInEasing)

    // Stagger delay per list item (ms)
    const val STAGGER_MS = 40
}
