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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

// ══════════════════════════════════════════════════════════════════════════════
// RESTAURANT PREMIUM GRADIENTS
// ══════════════════════════════════════════════════════════════════════════════

object RestGradients {
    val emeraldPremium = Brush.verticalGradient(
        colors = listOf(RestEmeraldStart, RestEmeraldMid, Color(0xFF005A3C), RestEmeraldEnd)
    )
    val primaryCTA = Brush.verticalGradient(
        colors = listOf(Color(0xFF008B5A), CibusAccentGreen, Color(0xFF005A3C))
    )
    /** Refined 3-stop emerald CTA gradient */
    val richCTAGradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0f to Color(0xFF009963),
            0.5f to Color(0xFF00704A),
            1.0f to Color(0xFF005A3C)
        )
    )
    /** Specular highlight overlay — draw on top of richCTAGradient for glass-like sheen */
    val specularOverlay = Brush.verticalGradient(
        colorStops = arrayOf(
            0f to Color.White.copy(alpha = 0.28f),
            0.35f to Color.White.copy(alpha = 0.08f),
            0.5f to Color.Transparent,
            1.0f to Color.Transparent
        )
    )
    val darkSurface = Brush.verticalGradient(
        colors = listOf(CibusGreenDark, Color(0xFF0E2A1B))
    )
    val premiumDark = Brush.linearGradient(
        colors = listOf(RestPremiumDarkStart, Color(0xFF1D1D1F), RestPremiumDarkEnd)
    )
    val heroOverlay = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.55f))
    )
    /** 5-stop cinematic hero overlay — richer depth for hero images */
    val cinematicHero = Brush.verticalGradient(
        colorStops = arrayOf(
            0f to Color.Transparent,
            0.2f to Color.Black.copy(alpha = 0.02f),
            0.45f to Color.Black.copy(alpha = 0.12f),
            0.72f to Color.Black.copy(alpha = 0.38f),
            1.0f to Color.Black.copy(alpha = 0.72f)
        )
    )
    val successCelebration = Brush.linearGradient(
        colors = listOf(RestEmeraldMid, RestSuccessTeal)
    )
    val goldShimmer = Brush.linearGradient(
        colors = listOf(RestGoldLight, RestGold, RestGoldDark, RestGold)
    )
    val ambientSurface = Brush.verticalGradient(
        colors = listOf(Color.White, Color(0xFFF8F9FB), Color(0xFFF0F2F5))
    )
    val cardShine = Brush.verticalGradient(
        colors = listOf(Color.White.copy(alpha = 0.06f), Color.Transparent)
    )
    val glassBorder = Brush.linearGradient(
        colors = listOf(Color.White.copy(alpha = 0.8f), Color.White.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.03f))
    )
    /** Deep green-to-black gradient for login hero backgrounds */
    val loginHero = Brush.linearGradient(
        colorStops = arrayOf(
            0.0f to LoginGradientDeep,
            0.25f to LoginGradientMid,
            0.55f to LoginGradientBrand,
            0.75f to LoginGradientLight,
            1.0f to LoginGradientMid
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    /** Frosted glass effect gradient */
    val appleGlass = Brush.verticalGradient(
        colorStops = arrayOf(
            0f to Color.White.copy(alpha = 0.45f),
            0.4f to Color.White.copy(alpha = 0.15f),
            1f to Color.White.copy(alpha = 0.05f)
        )
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// RESTAURANT SHADOW SYSTEM
// ══════════════════════════════════════════════════════════════════════════════

object RestShadows {
    fun Modifier.restDepthCard(cornerRadius: Dp = 16.dp): Modifier = this
        .shadow(elevation = 6.dp, shape = RoundedCornerShape(cornerRadius), ambientColor = Color.Black.copy(alpha = 0.08f))

    fun Modifier.restLuxeCard(cornerRadius: Dp = 16.dp): Modifier = this
        .shadow(elevation = 8.dp, shape = RoundedCornerShape(cornerRadius), ambientColor = Color.Black.copy(alpha = 0.08f))

    fun Modifier.restFloatingPill(): Modifier = this
        .shadow(elevation = 4.dp, shape = RoundedCornerShape(999.dp), ambientColor = Color.Black.copy(alpha = 0.08f))
        .shadow(elevation = 12.dp, shape = RoundedCornerShape(999.dp), ambientColor = Color.Black.copy(alpha = 0.04f))

    fun Modifier.restAccentGlow(color: Color = CibusGreen): Modifier = this
        .shadow(elevation = 8.dp, shape = RoundedCornerShape(14.dp), spotColor = color.copy(alpha = 0.28f))
}

// ══════════════════════════════════════════════════════════════════════════════
// RESTAURANT LUXE CARD — 4-layer shadow + gradient stroke (light-theme optimised)
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Premium 4-layer shadow stack designed for bright kitchen environments.
 * Gradient stroke transitions white 0.95 -> 0.25 -> black 0.05 for Apple-depth
 * glass edge. White background + clip for crisp card rendering.
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
// RESTAURANT RICH CTA GRADIENT — 4-stop emerald + specular + dual glow
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Premium CTA gradient matching iOS CibusRichCTAGradient.
 * 4-stop emerald (#009963 -> #00704A -> #005A3C -> #004A32) with
 * white 0.18 specular overlay and dual green glow shadows (24dp + 6dp).
 * Gradient border drawn via drawWithContent + drawRoundRect + Stroke.
 */
fun Modifier.restaurantRichCTAGradient(
    cornerRadius: Dp = 14.dp
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .clip(shape)
        .background(CibusGreen)
}

// ══════════════════════════════════════════════════════════════════════════════
// RESTAURANT ANIMATED BORDER — sweep gradient rotating around border
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Sweep gradient rotating around the border for incoming order cards.
 * 3000ms cycle, emerald palette. Draws via drawWithContent + rotate.
 */
@Composable
fun Modifier.restaurantAnimatedBorder(
    cornerRadius: Dp = 16.dp,
    lineWidth: Dp = 1.5.dp,
    colors: List<Color> = listOf(
        CibusGreen,
        RestAccentTeal,
        CibusGreen.copy(alpha = 0.3f)
    )
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "restBorderRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "restBorderAngle"
    )
    return this.drawWithContent {
        drawContent()
        rotate(rotation) {
            val brush = Brush.sweepGradient(
                colors = colors + colors,
                center = center
            )
            drawRoundRect(
                brush = brush,
                cornerRadius = CornerRadius(cornerRadius.toPx()),
                style = Stroke(width = lineWidth.toPx())
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// RESTAURANT RADIAL VIGNETTE — hero image overlay
// ══════════════════════════════════════════════════════════════════════════════

/**
 * 5-stop radial gradient for hero images: clear center -> 0.32 black edges.
 * Optimised for food photography — keeps centre bright, adds cinematic depth.
 */
fun Modifier.restaurantRadialVignette(): Modifier = this.drawWithContent {
    drawContent()
    val maxDim = maxOf(size.width, size.height)
    drawRect(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.00f to Color.Transparent,
                0.35f to Color.Transparent,
                0.60f to Color.Black.copy(alpha = 0.06f),
                0.82f to Color.Black.copy(alpha = 0.18f),
                1.00f to Color.Black.copy(alpha = 0.32f)
            ),
            center = center,
            radius = maxDim * 0.7f
        )
    )
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
// GLASS CARD MODIFIER
// ══════════════════════════════════════════════════════════════════════════════

fun Modifier.restGlassCard(cornerRadius: Dp = 16.dp): Modifier = this
    .shadow(0.5.dp, RoundedCornerShape(cornerRadius), ambientColor = Color.Black.copy(alpha = 0.015f))
    .shadow(4.dp, RoundedCornerShape(cornerRadius), ambientColor = Color.Black.copy(alpha = 0.04f))
    .shadow(12.dp, RoundedCornerShape(cornerRadius), ambientColor = Color.Black.copy(alpha = 0.06f))
    .clip(RoundedCornerShape(cornerRadius))
    .background(Color.White.copy(alpha = 0.92f))
    .drawWithContent {
        drawContent()
        // Specular top highlight
        drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.45f), Color.Transparent), endY = size.height * 0.25f))
        // Subtle glass border (inner stroke effect)
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = 0.7f), Color.White.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.04f))
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx()),
            style = Stroke(0.5.dp.toPx())
        )
    }

// Ambient glow
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

// Status glow ring for order indicators
@Composable
fun RestStatusGlowRing(color: Color = CibusGreen, modifier: Modifier = Modifier, size: Dp = 56.dp) {
    val transition = rememberInfiniteTransition(label = "glow")
    val gs by transition.animateFloat(0.95f, 1.15f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "gs")
    val ga by transition.animateFloat(0.8f, 0.4f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "ga")
    Box(modifier.size(size * 1.4f), contentAlignment = Alignment.Center) {
        Box(Modifier.size(size * 1.4f).graphicsLayer { scaleX = gs; scaleY = gs; alpha = ga }.background(color.copy(alpha = 0.12f), CircleShape))
        Canvas(Modifier.size(size)) { drawCircle(color.copy(alpha = 0.4f), style = Stroke(2.dp.toPx())) }
        Canvas(Modifier.size(size)) { drawCircle(color, style = Stroke(2.5.dp.toPx())) }
    }
}
