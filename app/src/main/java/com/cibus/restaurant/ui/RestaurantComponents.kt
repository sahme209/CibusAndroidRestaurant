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
import androidx.compose.ui.draw.scale
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
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(48.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(CibusDimens.cardRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = RestGreen,
            disabledContainerColor = RestTextTertiary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
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
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CibusDimens.cardRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = RestTextPrimary)
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 12.sp, color = RestTextTertiary)
            }
        }
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 4.dp)) {
                Text(text = actionLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RestGreen)
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
            .clip(RoundedCornerShape(12.dp))
            .background(shimmerBrush)
    )
}
