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

    // Phase 300: Premium Motion Curves
    val silkSpring = spring<Float>(dampingRatio = 0.92f, stiffness = 260f)
    val snapSpring = spring<Float>(dampingRatio = 0.72f, stiffness = 600f)
    val elasticPop = spring<Float>(dampingRatio = 0.45f, stiffness = 350f)
    val fluidSpring = spring<Float>(dampingRatio = 0.85f, stiffness = 280f)
    val microDelight = spring<Float>(dampingRatio = 0.50f, stiffness = 500f)
    val modalReveal = spring<Float>(dampingRatio = 0.86f, stiffness = 300f)
    const val CASCADE_STAGGER_MS = 30
    const val BREATHE_DURATION_MS = 2000

    // Premium Motion: liquid and crisp springs for refined interactions
    /** Liquid feel — smooth, slightly under-damped for natural deceleration */
    val liquidSpring = spring<Float>(dampingRatio = 0.88f, stiffness = 240f)
    /** Crisp snap — tight response for toggles, chips, quick state changes */
    val crispSnap = spring<Float>(dampingRatio = 0.72f, stiffness = 580f)
}
