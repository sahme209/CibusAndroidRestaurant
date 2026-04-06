package com.cibus.restaurant.ui

// Premium entry screen — dramatic DoorDash-Merchant-inspired with bold dark-green hero,
// floating benefits card, and staggered entrance animations. Matches iOS EntryView.

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.cibus.restaurant.ResL10n
import com.cibus.restaurant.ui.theme.*

/**
 * Premium entry screen shown on app launch when user is not logged in.
 * DoorDash Merchant-inspired: bold dark-green hero filling the top third,
 * storefront icon with concentric glow rings, floating white benefits card
 * that overlaps the hero via negative offset, and staggered fade-slide-in.
 */
@Composable
fun EntryScreen(
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit,
) {
    val ctx = LocalContext.current
    var showHomeKitchenOnboarding by remember { mutableStateOf(false) }

    // ── Home Kitchen onboarding full-screen dialog ──────────────────────────
    if (showHomeKitchenOnboarding) {
        HomeKitchenOnboardingWizard(
            onComplete = { token, expiresIn ->
                showHomeKitchenOnboarding = false
                // Token received — navigate to main screen
            },
            onDismiss = { showHomeKitchenOnboarding = false }
        )
        return
    }

    // ── Entrance animations ─────────────────────────────────────────────────
    val heroAlpha = remember { Animatable(0f) }
    val heroOffsetY = remember { Animatable(24f) }
    val cardAlpha = remember { Animatable(0f) }
    val cardOffsetY = remember { Animatable(16f) }
    val ctaAlpha = remember { Animatable(0f) }
    val ctaOffsetY = remember { Animatable(12f) }

    LaunchedEffect(Unit) {
        // Hero fade-slide
        launch {
            heroAlpha.animateTo(1f, tween(durationMillis = 400, delayMillis = 150))
        }
        launch {
            heroOffsetY.animateTo(0f, tween(durationMillis = 400, delayMillis = 150))
        }
        // Card fade-slide (staggered)
        launch {
            cardAlpha.animateTo(1f, tween(durationMillis = 400, delayMillis = 300))
        }
        launch {
            cardOffsetY.animateTo(0f, tween(durationMillis = 400, delayMillis = 300))
        }
        // CTA fade-slide (staggered)
        launch {
            ctaAlpha.animateTo(1f, tween(durationMillis = 400, delayMillis = 450))
        }
        launch {
            ctaOffsetY.animateTo(0f, tween(durationMillis = 400, delayMillis = 450))
        }
    }

    // ── Layout ──────────────────────────────────────────────────────────────
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val heroHeight = maxHeight * 0.44f

        // Background: dark-green hero fills top ~44%, white below
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CibusGreenDark, CibusGreen)
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
            )
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // ── Hero Section (on dark green) ────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = heroAlpha.value
                        translationY = heroOffsetY.value * density
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(20.dp))

                // Storefront icon with concentric glow rings
                Box(contentAlignment = Alignment.Center) {
                    // Outer glow ring
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.10f))
                    )
                    // Inner glow ring
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    )
                    // Icon
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Color.White,
                    )
                }

                Spacer(Modifier.height(CibusDimens.spacing16))

                // Title + Tagline grouped together
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Become a HUBB\nMerchant Partner",
                        fontSize = CibusDimens.displaySp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp,
                    )

                    Text(
                        text = ResL10n.entryTagline(ctx),
                        fontSize = CibusDimens.bodySp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = CibusDimens.spacing24),
                    )
                }
            }

            // ── Benefits Card (overlapping hero via negative offset) ────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CibusDimens.spacing16)
                    .offset(y = (-20).dp)
                    .graphicsLayer {
                        alpha = cardAlpha.value
                        translationY = cardOffsetY.value * density
                    },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier.padding(CibusDimens.spacing24),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Section label
                    Text(
                        text = "WHY PARTNER WITH US",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CibusTextOnSurfaceSecondary,
                        letterSpacing = 0.8.sp,
                    )

                    // Benefit rows
                    EntryBenefitRow(
                        icon = Icons.Default.Schedule,
                        text = "Flexible hours \u2013 open when you want",
                        accent = CibusGreen,
                    )
                    EntryBenefitRow(
                        icon = Icons.Default.Payments,
                        text = "Secure payouts, transparent fees",
                        accent = CibusGreenLight,
                    )
                    EntryBenefitRow(
                        icon = Icons.Default.TrendingUp,
                        text = "Reach more customers in your area",
                        accent = CibusSuccess,
                    )
                    EntryBenefitRow(
                        icon = Icons.Default.Shield,
                        text = "Real-time order management tools",
                        accent = CibusOrange,
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // ── CTAs ────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CibusDimens.screenHorizontal)
                    .padding(bottom = 32.dp)
                    .graphicsLayer {
                        alpha = ctaAlpha.value
                        translationY = ctaOffsetY.value * density
                    },
                verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing16),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Primary CTA — "Get Started"
                RestaurantPrimaryButton(
                    text = ResL10n.entryGetStarted(ctx),
                    onClick = onGetStarted,
                    isLargeCTA = true,
                )

                // Secondary CTA — "Start Home Kitchen"
                Button(
                    onClick = { showHomeKitchenOnboarding = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CibusDimens.btnHeight),
                    shape = RoundedCornerShape(CibusDimens.btnRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CibusGreen.copy(alpha = 0.10f),
                        contentColor = CibusGreen,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(CibusDimens.spacing8))
                    Text(
                        text = if (ResL10n.isUrdu(ctx)) "Ghar se Khana Becho" else "Start Home Kitchen",
                        fontSize = CibusDimens.bodySp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                // Sign in text link
                Text(
                    text = ResL10n.entrySignInLink(ctx),
                    fontSize = CibusDimens.bodySp,
                    color = CibusTextOnSurfaceSecondary,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onSignIn,
                        )
                        .padding(vertical = CibusDimens.spacing8),
                )
            }

        }
    }
}

// ── Benefit Row ─────────────────────────────────────────────────────────────

@Composable
private fun EntryBenefitRow(
    icon: ImageVector,
    text: String,
    accent: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        // Rounded square icon background
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(CibusDimens.radiusSm))
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = accent,
            )
        }

        Spacer(Modifier.width(CibusDimens.spacing12))

        Text(
            text = text,
            fontSize = CibusDimens.bodySp,
            color = CibusTextOnSurface,
        )
    }
}
