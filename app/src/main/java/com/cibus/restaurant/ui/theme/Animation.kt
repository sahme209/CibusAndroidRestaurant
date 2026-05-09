package com.cibus.restaurant.ui.theme

import androidx.compose.animation.core.spring

/**
 * Cibus Motion System — all animations under 300ms.
 * Use these tokens instead of hardcoding durations.
 */
object CibusMotion {
    // Phase 300: Premium Motion Curves
    val snapSpring = spring<Float>(dampingRatio = 0.72f, stiffness = 600f)
}
