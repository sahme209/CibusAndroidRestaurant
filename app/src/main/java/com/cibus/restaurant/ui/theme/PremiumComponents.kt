package com.cibus.restaurant.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

// ══════════════════════════════════════════════════════════════════════════════
// RESTAURANT SHADOW SYSTEM
// ══════════════════════════════════════════════════════════════════════════════

object RestShadows {
    fun Modifier.restDepthCard(cornerRadius: Dp = 16.dp): Modifier = this
        .shadow(elevation = 6.dp, shape = RoundedCornerShape(cornerRadius), ambientColor = Color.Black.copy(alpha = 0.08f))

    fun Modifier.restLuxeCard(cornerRadius: Dp = 16.dp): Modifier = this
        .shadow(elevation = 8.dp, shape = RoundedCornerShape(cornerRadius), ambientColor = Color.Black.copy(alpha = 0.08f))
}

// ══════════════════════════════════════════════════════════════════════════════
// RESTAURANT LUXE CARD — single shadow + clip (light-theme optimised)
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Single-shadow card with white background and corner clip.
 */
fun Modifier.restaurantLuxeCard(
    cornerRadius: Dp = 20.dp
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .shadow(8.dp, shape, ambientColor = Color.Black.copy(alpha = 0.08f))
        .clip(shape)
        .background(Color.White)
}

// ══════════════════════════════════════════════════════════════════════════════
// SHIMMER
// ══════════════════════════════════════════════════════════════════════════════

fun Modifier.restPremiumShimmer(durationMs: Int = 1200): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(durationMs, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerTranslate"
    )
    this.drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.4f), Color.Transparent),
                start = Offset(size.width * translateAnim, 0f),
                end = Offset(size.width * (translateAnim + 0.6f), size.height)
            )
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// BREATHING MODIFIER
// ══════════════════════════════════════════════════════════════════════════════

fun Modifier.restBreathing(minScale: Float = 0.97f, maxScale: Float = 1.03f): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "breathing")
    val scale by transition.animateFloat(
        initialValue = minScale, targetValue = maxScale,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathScale"
    )
    this.graphicsLayer { scaleX = scale; scaleY = scale }
}

// ══════════════════════════════════════════════════════════════════════════════
// LIVE PULSE — for new order indicators
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun RestLivePulse(
    modifier: Modifier = Modifier,
    color: Color = RestLiveBadge,
    size: Dp = 8.dp
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(0.8f, 1.3f, infiniteRepeatable(tween(1200), RepeatMode.Restart), label = "ps")
    val alpha by transition.animateFloat(0.6f, 0f, infiniteRepeatable(tween(1200), RepeatMode.Restart), label = "pa")

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(Modifier.size(size * 2.5f).graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }.background(color.copy(alpha = 0.3f), CircleShape))
        Box(Modifier.size(size).shadow(4.dp, CircleShape, spotColor = color.copy(alpha = 0.4f)).background(color, CircleShape))
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PROGRESS RING — for order prep progress
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun RestProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    lineWidth: Dp = 6.dp,
    trackColor: Color = CibusTextTertiary.copy(alpha = 0.2f),
    progressColor: Color = CibusGreen,
    showPercentage: Boolean = false
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), spring(0.8f, 200f), label = "rp")
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = lineWidth.toPx()
            drawArc(trackColor, 0f, 360f, false, style = Stroke(sw, cap = StrokeCap.Round))
            drawArc(progressColor, -90f, 360f * animated, false, style = Stroke(sw, cap = StrokeCap.Round))
            if (animated > 0.05f) {
                val a = Math.toRadians((-90.0 + 360.0 * animated))
                val r = (this.size.minDimension - sw) / 2
                val dx = center.x + r * cos(a).toFloat()
                val dy = center.y + r * sin(a).toFloat()
                drawCircle(progressColor.copy(alpha = 0.5f), sw * 0.8f, Offset(dx, dy))
                drawCircle(progressColor, sw * 0.55f, Offset(dx, dy))
            }
        }
        if (showPercentage) Text("${(animated * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CibusTextPrimary)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// GRADIENT DIVIDER
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun RestGradientDivider(modifier: Modifier = Modifier, height: Dp = 0.5.dp) {
    Box(modifier.fillMaxWidth().height(height).background(Brush.horizontalGradient(listOf(Color.Transparent, CibusTextTertiary.copy(alpha = 0.3f), CibusTextTertiary.copy(alpha = 0.3f), Color.Transparent))))
}

// ══════════════════════════════════════════════════════════════════════════════
// ANIMATED CHECKMARK
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun RestAnimatedCheckmark(modifier: Modifier = Modifier, size: Dp = 72.dp, color: Color = CibusGreen, trigger: Boolean = true) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(trigger) { if (trigger) { progress.snapTo(0f); progress.animateTo(1f, spring(0.6f, 200f)) } }
    Canvas(modifier.size(size)) {
        val sw = 4.dp.toPx(); val p = progress.value
        drawCircle(color.copy(alpha = 0.12f), this.size.minDimension / 2)
        drawArc(color, -90f, 360f * (p.coerceAtMost(0.6f) / 0.6f), false, style = Stroke(sw, cap = StrokeCap.Round))
        if (p > 0.6f) {
            val cp = ((p - 0.6f) / 0.4f).coerceIn(0f, 1f)
            val cx = this.size.width / 2; val cy = this.size.height / 2; val r = this.size.minDimension / 2 * 0.45f
            val path = Path().apply {
                val sx = cx - r * 0.5f; val sy = cy; val mx = cx - r * 0.1f; val my = cy + r * 0.4f; val ex = cx + r * 0.55f; val ey = cy - r * 0.35f
                moveTo(sx, sy)
                if (cp < 0.5f) lineTo(sx + (mx - sx) * cp / 0.5f, sy + (my - sy) * cp / 0.5f)
                else { lineTo(mx, my); val t = (cp - 0.5f) / 0.5f; lineTo(mx + (ex - mx) * t, my + (ey - my) * t) }
            }
            drawPath(path, color, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// AMBIENT GLOW — subtle backdrop glow behind a composable
// ══════════════════════════════════════════════════════════════════════════════

fun Modifier.restAmbientGlow(color: Color = CibusGreen, radius: Dp = 40.dp, alpha: Float = 0.15f): Modifier = this.drawBehind {
    drawCircle(color.copy(alpha = alpha), radius.toPx() * 1.5f, center)
}

// ══════════════════════════════════════════════════════════════════════════════
// SKELETON CARD — loading placeholder
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun RestSkeletonCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)
            .then(with(RestShadows) { Modifier.restDepthCard() })
    ) {
        Box(Modifier.fillMaxWidth().height(120.dp).background(Color(0xFFF2F3F5)).restPremiumShimmer())
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(180.dp, 14.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFF2F3F5)).restPremiumShimmer(1400))
            Box(Modifier.size(120.dp, 10.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF2F3F5)).restPremiumShimmer(1600))
        }
    }
}
