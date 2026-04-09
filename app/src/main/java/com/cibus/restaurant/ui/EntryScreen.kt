package com.cibus.restaurant.ui

// Rewritten from scratch to match iOS EntryView.swift exactly.
// No BoxWithConstraints, no WindowInsets, no resource lookups — pure weight layout.

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.ResL10n
import com.cibus.restaurant.ui.theme.*

@Composable
fun EntryScreen(
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit,
) {
    val ctx = LocalContext.current
    var showHomeKitchenOnboarding by remember { mutableStateOf(false) }

    if (showHomeKitchenOnboarding) {
        HomeKitchenOnboardingWizard(
            onComplete = { token, _ ->
                showHomeKitchenOnboarding = false
            },
            onDismiss = { showHomeKitchenOnboarding = false }
        )
        return
    }

    // Single appearance toggle — matches iOS `@State private var appeared`
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    val heroAlpha by animateFloatAsState(
        if (appeared) 1f else 0f, tween(400, delayMillis = 150), label = "hero"
    )
    val cardAlpha by animateFloatAsState(
        if (appeared) 1f else 0f, tween(400, delayMillis = 300), label = "card"
    )
    val ctaAlpha by animateFloatAsState(
        if (appeared) 1f else 0f, tween(400, delayMillis = 450), label = "cta"
    )

    // Layout: Box with background layer + content layer.
    // Both use weight(0.42) for the hero split → always perfectly aligned.
    Box(modifier = Modifier.fillMaxSize()) {

        // ── Background ──────────────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(0.42f)
                    .background(Brush.linearGradient(listOf(CibusGreenDark, CibusGreen)))
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(0.58f)
                    .background(Color.White)
            )
        }

        // ── Content ─────────────────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxSize()) {

            // Hero section — weight(0.42) matches background green exactly
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.42f)
                    .graphicsLayer { alpha = heroAlpha },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(36.dp))

                // Storefront icon with concentric glow rings
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.size(80.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.10f))
                    )
                    Box(
                        Modifier.size(64.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    )
                    Icon(
                        Icons.Default.Storefront, null,
                        Modifier.size(32.dp), tint = Color.White,
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Title + tagline
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        "Become a HUBB\nMerchant Partner",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp,
                    )
                    Text(
                        ResL10n.entryTagline(ctx),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                }

                Spacer(Modifier.weight(1f))
            }

            // Bottom half — weight(0.58) matches background white
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.58f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Benefits card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = 4.dp)
                        .graphicsLayer { alpha = cardAlpha },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                ) {
                    Column(
                        Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            "WHY PARTNER WITH US",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CibusTextOnSurfaceSecondary,
                            letterSpacing = 0.8.sp,
                        )
                        EntryBenefitRow(Icons.Default.Schedule, "Flexible hours \u2013 open when you want", CibusGreen)
                        EntryBenefitRow(Icons.Default.Payments, "Secure payouts, transparent fees", CibusGreenLight)
                        EntryBenefitRow(Icons.Default.TrendingUp, "Reach more customers in your area", CibusSuccess)
                        EntryBenefitRow(Icons.Default.Shield, "Real-time order management tools", CibusOrange)
                    }
                }

                Spacer(Modifier.weight(1f))

                // CTAs
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 32.dp)
                        .graphicsLayer { alpha = ctaAlpha },
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Get Started
                    RestaurantPrimaryButton(
                        text = ResL10n.entryGetStarted(ctx),
                        onClick = onGetStarted,
                        isLargeCTA = true,
                    )

                    // Start Home Kitchen
                    Button(
                        onClick = { showHomeKitchenOnboarding = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CibusGreen.copy(alpha = 0.10f),
                            contentColor = CibusGreen,
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    ) {
                        Icon(Icons.Default.Home, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (ResL10n.isUrdu(ctx)) "Ghar se Khana Becho" else "Start Home Kitchen",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    // Already have account? Sign in
                    TextButton(onClick = onSignIn) {
                        Text(
                            ResL10n.entrySignInLink(ctx),
                            fontSize = 14.sp,
                            color = CibusTextOnSurfaceSecondary,
                        )
                    }
                }
            }
        }
    }
}

// ── Benefit Row ─────────────────────────────────────────────────────────────

@Composable
private fun EntryBenefitRow(icon: ImageVector, text: String, accent: Color) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, Modifier.size(16.dp), tint = accent)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, color = CibusTextOnSurface)
    }
}
