package com.cibus.restaurant.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ══════════════════════════════════════════════════════════════════════════════
// RESTAURANT SHADOW SYSTEM
// ══════════════════════════════════════════════════════════════════════════════

object RestShadows {
    fun Modifier.restDepthCard(cornerRadius: Dp = 16.dp): Modifier = this
        .shadow(elevation = 6.dp, shape = RoundedCornerShape(cornerRadius), ambientColor = Color.Black.copy(alpha = 0.08f))
}


