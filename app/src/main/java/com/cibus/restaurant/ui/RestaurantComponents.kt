package com.cibus.restaurant.ui

// Phase 110: Light design alignment — shared reusable components for the Restaurant app,
// aligned with the Cibus customer app design language.

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
