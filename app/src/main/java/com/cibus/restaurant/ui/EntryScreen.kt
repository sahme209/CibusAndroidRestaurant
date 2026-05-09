package com.cibus.restaurant.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
            onComplete = { _, _ -> showHomeKitchenOnboarding = false },
            onDismiss = { showHomeKitchenOnboarding = false }
        )
        return
    }

    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    val heroAlpha by animateFloatAsState(
        if (appeared) 1f else 0f, tween(500), label = "hero"
    )
    val cardAlpha by animateFloatAsState(
        if (appeared) 1f else 0f, tween(500, delayMillis = 150), label = "card"
    )
    val ctaAlpha by animateFloatAsState(
        if (appeared) 1f else 0f, tween(500, delayMillis = 300), label = "cta"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleGroupedBackground)
            .statusBarsPadding()
    ) {
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(64.dp))

            // Hero
            Column(
                modifier = Modifier.graphicsLayer { alpha = heroAlpha },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(CibusGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Storefront, null,
                        Modifier.size(40.dp), tint = CibusGreen,
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    if (ResL10n.isUrdu(ctx)) "HUBB Merchant\nPartner Banein" else "Become a HUBB\nMerchant Partner",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleLabelPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    ResL10n.entryTagline(ctx),
                    fontSize = 15.sp,
                    color = AppleLabelSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
            }

            Spacer(Modifier.height(40.dp))

            // Benefits card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CibusDimens.screenHorizontal)
                    .graphicsLayer { alpha = cardAlpha },
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                tonalElevation = 0.dp,
            ) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    EntryBenefitRow(
                        Icons.Default.Schedule,
                        if (ResL10n.isUrdu(ctx)) "Flexible Hours" else "Flexible Hours",
                        if (ResL10n.isUrdu(ctx)) "Jab chahein kholen, apni marzi" else "Open when you want, on your schedule",
                        AppleSystemBlue,
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 62.dp),
                        color = AppleSeparator.copy(alpha = 0.3f),
                        thickness = 0.5.dp,
                    )
                    EntryBenefitRow(
                        Icons.Default.Payments,
                        if (ResL10n.isUrdu(ctx)) "Secure Payouts" else "Secure Payouts",
                        if (ResL10n.isUrdu(ctx)) "Transparent fees, hafta-waar settlement" else "Transparent fees, weekly settlements",
                        AppleSystemGreen,
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 62.dp),
                        color = AppleSeparator.copy(alpha = 0.3f),
                        thickness = 0.5.dp,
                    )
                    EntryBenefitRow(
                        Icons.Default.TrendingUp,
                        if (ResL10n.isUrdu(ctx)) "Zyada Customers" else "Grow Your Reach",
                        if (ResL10n.isUrdu(ctx)) "Apne area ke customers se judo" else "Connect with more customers nearby",
                        AppleSystemOrange,
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 62.dp),
                        color = AppleSeparator.copy(alpha = 0.3f),
                        thickness = 0.5.dp,
                    )
                    EntryBenefitRow(
                        Icons.Default.FlashOn,
                        if (ResL10n.isUrdu(ctx)) "Real-Time Tools" else "Real-Time Tools",
                        if (ResL10n.isUrdu(ctx)) "Orders aate hi manage karein" else "Manage orders as they come in",
                        AppleSystemIndigo,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // Pinned bottom CTAs
        HorizontalDivider(color = AppleSeparator.copy(alpha = 0.3f), thickness = 0.5.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppleGroupedBackground)
                .padding(horizontal = CibusDimens.screenHorizontal)
                .padding(top = 16.dp, bottom = 34.dp)
                .navigationBarsPadding()
                .graphicsLayer { alpha = ctaAlpha },
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RestaurantPrimaryButton(
                text = ResL10n.entryGetStarted(ctx),
                onClick = onGetStarted,
                isLargeCTA = true,
            )

            Button(
                onClick = { showHomeKitchenOnboarding = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            TextButton(onClick = onSignIn) {
                Text(
                    ResL10n.entrySignInLink(ctx),
                    fontSize = 15.sp,
                    color = CibusGreen,
                )
            }
        }
    }
}

// ── Benefit Row ─────────────────────────────────────────────────────────────

@Composable
private fun EntryBenefitRow(icon: ImageVector, title: String, subtitle: String, accent: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, Modifier.size(16.dp), tint = accent)
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppleLabelPrimary)
            Text(subtitle, fontSize = 13.sp, color = AppleLabelSecondary)
        }
    }
}
