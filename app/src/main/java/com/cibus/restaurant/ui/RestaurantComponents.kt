package com.cibus.restaurant.ui

// Phase 110: Light design alignment — shared reusable components for the Restaurant app,
// aligned with the Cibus customer app design language.

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

// ── Design Tokens ─────────────────────────────────────────────────────────────

val RestGreen        = Color(0xFF1F5C42)

// ── RestaurantPrimaryButton ───────────────────────────────────────────────────

@Composable
fun RestaurantPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isLargeCTA: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(if (isLargeCTA) CibusDimens.btnHeightLarge else CibusDimens.btnHeight)
            // Single clean shadow for Apple-refined depth
            .shadow(6.dp, RoundedCornerShape(CibusDimens.btnRadius), spotColor = RestGreen.copy(alpha = 0.2f)),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(CibusDimens.btnRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = RestGreen,
            disabledContainerColor = AppleLabelTertiary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(text, fontSize = CibusDimens.bodySp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

// ── RestaurantSurfaceCard ─────────────────────────────────────────────────────

@Composable
fun RestaurantSurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            // Lighter Apple-style shadow system
            .shadow(2.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.04f))
            .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(CibusDimens.cardPadding), content = content)
    }
}

/**
 * Subtle section break — hairline divider with consistent vertical spacing.
 * Use between major content sections (orders vs menu, dashboard vs analytics).
 */
@Composable
fun RestaurantSectionBreak(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier
            .padding(horizontal = CibusDimens.screenHorizontal)
            .padding(vertical = CibusDimens.dividerVerticalPadding),
        color = AppleSeparator.copy(alpha = 0.4f),
        thickness = CibusDimens.dividerThickness
    )
}

// ── Skeleton Card (WS7) ───────────────────────────────────────────────────────

@Composable
fun RestaurantSkeletonCard(modifier: Modifier = Modifier, height: Dp = 80.dp) {
    val transition = rememberInfiniteTransition(label = "restaurant_shimmer")
    val offset by transition.animateFloat(
        initialValue = -500f, targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "restaurant_shimmer_offset"
    )
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF2C2C2E), Color(0xFF3A3A3C), Color(0xFF2C2C2E)),
        start = Offset(offset, 0f), end = Offset(offset + 500f, 0f)
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(CibusDimens.cardRadius))
            .background(shimmerBrush)
    )
}

// ── Ambient Fog Background ──────────────────────────────────────────────────

@Composable
fun RestaurantFogBackground(
    modifier: Modifier = Modifier,
    primaryColor: Color = CibusGreen,
    secondaryColor: Color = CibusGreenDark,
    fogOpacity: Float = 0.15f,
) {
    val config = LocalConfiguration.current
    val screenW = config.screenWidthDp.toFloat()
    val screenH = config.screenHeightDp.toFloat()

    val inf = rememberInfiniteTransition(label = "fog")

    val phase1 by inf.animateFloat(
        initialValue = 0f, targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(tween(16000, easing = EaseInOut), RepeatMode.Reverse),
        label = "fog1"
    )
    val phase2 by inf.animateFloat(
        initialValue = 0f, targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(tween(20000, easing = EaseInOut), RepeatMode.Reverse),
        label = "fog2"
    )
    val phase3 by inf.animateFloat(
        initialValue = 0f, targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(tween(18000, easing = EaseInOut), RepeatMode.Reverse),
        label = "fog3"
    )
    val phase4 by inf.animateFloat(
        initialValue = 0f, targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(tween(24000, easing = EaseInOut), RepeatMode.Reverse),
        label = "fog4"
    )
    val phase5 by inf.animateFloat(
        initialValue = 0f, targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(tween(14000, easing = EaseInOut), RepeatMode.Reverse),
        label = "fog5"
    )

    Box(modifier = modifier.fillMaxSize().clearAndSetSemantics { }) {
        FogLayer(
            color = primaryColor, alpha = fogOpacity,
            width = (screenW * 1.4f).dp, height = (screenH * 0.5f).dp,
            blurRadius = 120.dp,
            x = (sin(phase1) * screenW * 0.2f - screenW * 0.1f).dp,
            y = (cos(phase1 * 0.7f) * screenH * 0.1f - screenH * 0.15f).dp,
        )

        FogLayer(
            color = secondaryColor, alpha = fogOpacity * 0.9f,
            width = (screenW * 1.2f).dp, height = (screenH * 0.45f).dp,
            blurRadius = 130.dp,
            x = (cos(phase2) * screenW * 0.25f - screenW * 0.05f).dp,
            y = (sin(phase2 * 0.6f) * screenH * 0.12f + screenH * 0.2f).dp,
        )

        FogLayer(
            color = primaryColor, alpha = fogOpacity * 0.7f,
            width = (screenW * 1.1f).dp, height = (screenH * 0.4f).dp,
            blurRadius = 100.dp,
            x = (sin(phase3 * 1.3f) * screenW * 0.2f).dp,
            y = (cos(phase3) * screenH * 0.15f - screenH * 0.05f).dp,
        )

        FogLayer(
            color = secondaryColor, alpha = fogOpacity * 0.6f,
            width = screenW.dp, height = (screenH * 0.35f).dp,
            blurRadius = 140.dp,
            x = (cos(phase4 * 0.8f) * screenW * 0.15f).dp,
            y = (sin(phase4 * 0.5f) * screenH * 0.1f + screenH * 0.35f).dp,
        )

        FogLayer(
            color = primaryColor, alpha = fogOpacity * 0.5f,
            width = (screenW * 0.8f).dp, height = (screenH * 0.3f).dp,
            blurRadius = 110.dp,
            x = (sin(phase5 * 1.1f + 1.5f) * screenW * 0.3f).dp,
            y = (cos(phase5 * 0.9f) * screenH * 0.2f + screenH * 0.1f).dp,
        )
    }
}

@Composable
private fun FogLayer(
    color: Color,
    alpha: Float,
    width: Dp,
    height: Dp,
    blurRadius: Dp,
    x: Dp,
    y: Dp,
) {
    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .offset(x = x, y = y)
            .blur(blurRadius)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to color.copy(alpha = alpha),
                        0.5f to color.copy(alpha = alpha * 0.3f),
                        1.0f to Color.Transparent,
                    ),
                ),
            )
    )
}

// ── AI Aurora Background ───────────────────────────────────────────────────

@Composable
fun AIAuroraBackground(
    modifier: Modifier = Modifier,
    intensity: Float = 0.15f,
) {
    val config = LocalConfiguration.current
    val screenW = config.screenWidthDp.toFloat()
    val screenH = config.screenHeightDp.toFloat()

    val inf = rememberInfiniteTransition(label = "aurora")

    val p1 by inf.animateFloat(0f, (Math.PI * 2).toFloat(),
        infiniteRepeatable(tween(18000, easing = EaseInOut), RepeatMode.Reverse), label = "a1")
    val p2 by inf.animateFloat(0f, (Math.PI * 2).toFloat(),
        infiniteRepeatable(tween(22000, easing = EaseInOut), RepeatMode.Reverse), label = "a2")
    val p3 by inf.animateFloat(0f, (Math.PI * 2).toFloat(),
        infiniteRepeatable(tween(16000, easing = EaseInOut), RepeatMode.Reverse), label = "a3")
    val p4 by inf.animateFloat(0f, (Math.PI * 2).toFloat(),
        infiniteRepeatable(tween(26000, easing = EaseInOut), RepeatMode.Reverse), label = "a4")

    Box(modifier = modifier.fillMaxSize().clearAndSetSemantics { }) {
        AuroraLayer(
            colors = listOf(Color(0xFF00704A), Color(0xFF2DD4BF)),
            width = (screenW * 1.3f).dp, height = (screenH * 0.4f).dp,
            blurRadius = 100.dp, alpha = intensity,
            x = (sin(p1) * screenW * 0.2f).dp,
            y = (cos(p1 * 0.6f) * screenH * 0.15f - screenH * 0.15f).dp,
        )
        AuroraLayer(
            colors = listOf(Color(0xFF14B8A6), Color(0xFF06B6D4)),
            width = (screenW * 1.1f).dp, height = (screenH * 0.35f).dp,
            blurRadius = 110.dp, alpha = intensity * 0.9f,
            x = (cos(p2) * screenW * 0.25f).dp,
            y = (sin(p2 * 0.5f) * screenH * 0.12f + screenH * 0.1f).dp,
        )
        AuroraLayer(
            colors = listOf(Color(0xFF7C3AED), Color(0xFFA78BFA)),
            width = (screenW * 0.8f).dp, height = (screenH * 0.3f).dp,
            blurRadius = 120.dp, alpha = intensity * 0.7f,
            x = (sin(p3 * 1.2f) * screenW * 0.3f).dp,
            y = (cos(p3 * 0.8f) * screenH * 0.2f + screenH * 0.25f).dp,
        )
        AuroraLayer(
            colors = listOf(Color(0xFF2D6A4F), Color(0xFF40916C)),
            width = (screenW * 1.2f).dp, height = (screenH * 0.45f).dp,
            blurRadius = 130.dp, alpha = intensity * 0.8f,
            x = (cos(p4 * 0.7f) * screenW * 0.15f).dp,
            y = (sin(p4 * 0.4f) * screenH * 0.1f + screenH * 0.35f).dp,
        )
    }
}

@Composable
private fun AuroraLayer(
    colors: List<Color>,
    width: Dp,
    height: Dp,
    blurRadius: Dp,
    alpha: Float,
    x: Dp,
    y: Dp,
) {
    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .offset(x = x, y = y)
            .blur(blurRadius)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = colors.map { it.copy(alpha = alpha) },
                ),
            )
    )
}

// ── AI Particle Field ──────────────────────────────────────────────────────

private data class ParticleData(
    val baseX: Float,
    val baseY: Float,
    val size: Float,
    val speed: Float,
    val brightness: Float,
    val phase: Float,
)

@Composable
fun AIParticleField(
    modifier: Modifier = Modifier,
    particleCount: Int = 15,
    color: Color = Color.White,
    intensity: Float = 0.4f,
) {
    val particles = remember {
        (0 until particleCount).map {
            ParticleData(
                baseX = (Math.random().toFloat()),
                baseY = (Math.random().toFloat()),
                size = (2f + Math.random().toFloat() * 3f),
                speed = (0.3f + Math.random().toFloat() * 0.7f),
                brightness = (0.3f + Math.random().toFloat() * 0.7f),
                phase = (Math.random().toFloat() * Math.PI.toFloat() * 2f),
            )
        }
    }

    val inf = rememberInfiniteTransition(label = "particles")
    val time by inf.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2 * 10).toFloat(),
        animationSpec = infiniteRepeatable(tween(60000, easing = LinearEasing), RepeatMode.Restart),
        label = "particle_time"
    )

    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxSize()
            .clearAndSetSemantics { }
    ) {
        for (p in particles) {
            val t = time * p.speed
            val px = p.baseX * size.width + sin(t * 0.4f + p.phase) * 30f
            val py = p.baseY * size.height + cos(t * 0.3f + p.phase * 1.3f) * 25f
            val alpha = (sin(t * 0.6f + p.phase * 2f) * 0.3f + 0.5f) * p.brightness * intensity

            if (px in -10f..size.width + 10f && py in -10f..size.height + 10f) {
                drawCircle(
                    color = color,
                    radius = p.size,
                    center = Offset(px, py),
                    alpha = alpha.coerceIn(0f, 1f),
                )
            }
        }
    }
}
