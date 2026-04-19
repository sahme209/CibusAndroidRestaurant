package com.cibus.restaurant.ui

// Phase 110: Light design alignment — shared reusable components for the Restaurant app,
// aligned with the Cibus customer app design language.

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.ui.theme.CibusDimens

// ── Design Tokens ─────────────────────────────────────────────────────────────

val RestGreen        = Color(0xFF1F5C42)
val RestGreenMid     = Color(0xFF2D7A5A)
val RestHeaderDark   = Color(0xFF0E0E0E)
val RestBackground   = Color(0xFFF7F7F7)
val RestCardBG       = Color.White
val RestTextPrimary  = Color(0xFF0A0A0A)
val RestTextSecondary = Color(0xFF4A4A4A)
val RestTextTertiary = Color(0xFF919191)
val RestOrange       = Color(0xFFE8714A)
val RestGreenBright  = Color(0xFF3EA876)
val RestDivider      = Color(0xFFE8E8E8)

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
            // Dual-layer shadow for floating depth
            .shadow(3.dp, RoundedCornerShape(CibusDimens.btnRadius), spotColor = RestGreen.copy(alpha = 0.25f))
            .shadow(8.dp, RoundedCornerShape(CibusDimens.btnRadius), ambientColor = RestGreen.copy(alpha = 0.12f)),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(CibusDimens.btnRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = RestGreen,
            disabledContainerColor = RestTextTertiary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            // Specular highlight text for premium feel
            Text(text, fontSize = CibusDimens.bodySp, fontWeight = FontWeight.Bold, color = Color.White)
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
            // Multi-layer shadow: close (crisp edge), mid (form), far (ambient)
            .shadow(1.dp, RoundedCornerShape(CibusDimens.cardRadius), ambientColor = Color.Black.copy(alpha = 0.02f))
            .shadow(4.dp, RoundedCornerShape(CibusDimens.cardRadius), ambientColor = Color.Black.copy(alpha = 0.05f))
            .shadow(12.dp, RoundedCornerShape(CibusDimens.cardRadius), ambientColor = Color.Black.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(CibusDimens.cardRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = RestCardBG)
    ) {
        Column(modifier = Modifier.padding(CibusDimens.cardPadding), content = content)
    }
}

// ── RestaurantSectionHeader ───────────────────────────────────────────────────

@Composable
fun RestaurantSectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CibusDimens.screenHorizontal),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, fontSize = CibusDimens.titleSp, fontWeight = FontWeight.SemiBold, color = RestTextPrimary)
            if (subtitle != null) {
                Text(text = subtitle, fontSize = CibusDimens.captionSp, color = RestTextTertiary)
            }
        }
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 4.dp)) {
                Text(text = actionLabel, fontSize = CibusDimens.captionSp, fontWeight = FontWeight.SemiBold, color = RestGreen)
            }
        }
    }
}

// ── RestaurantStatusBadge ─────────────────────────────────────────────────────

@Composable
fun RestaurantStatusBadge(
    label: String,
    color: Color = RestGreenBright,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

// ── RestaurantDivider ─────────────────────────────────────────────────────────

@Composable
fun RestaurantDivider(indent: androidx.compose.ui.unit.Dp = 0.dp, modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier.padding(start = indent), color = RestDivider, thickness = 0.8.dp)
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
        color = RestDivider.copy(alpha = 0.4f),
        thickness = CibusDimens.dividerThickness
    )
}

// ── Restaurant Empty State ────────────────────────────────────────────────────

/**
 * Reusable empty state — icon, title, message, optional CTA.
 * Use for: empty orders, empty menu, no earnings, etc.
 */
@Composable
fun RestaurantEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(CibusDimens.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing16)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(RestBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = RestTextTertiary
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing4)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = RestTextPrimary
            )
            Text(
                text = message,
                fontSize = 14.sp,
                color = RestTextTertiary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = CibusDimens.spacing24)
            )
        }
        if (actionText != null && onAction != null) {
            RestaurantPrimaryButton(
                text = actionText,
                onClick = onAction,
                modifier = Modifier.width(200.dp)
            )
        }
    }
}

// ── Premium Button Scale (WS1) ────────────────────────────────────────────────

@Composable
fun Modifier.restaurantButtonScale(
    enabled: Boolean = true
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = com.cibus.restaurant.ui.theme.CibusMotion.buttonSpring,
        label = "restaurant_btn_scale"
    )
    return this.scale(scale)
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
        colors = listOf(Color(0xFFE8E8E8), Color(0xFFF5F5F5), Color(0xFFE8E8E8)),
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
