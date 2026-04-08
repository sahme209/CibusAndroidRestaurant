package com.cibus.restaurant.ui.claim

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.ResL10n
import com.cibus.restaurant.claim.ClaimStatusSummary
import com.cibus.restaurant.claim.RestaurantListingState
import com.cibus.restaurant.ui.RestaurantPrimaryButton
import com.cibus.restaurant.ui.RestBackground
import com.cibus.restaurant.ui.RestCardBG
import com.cibus.restaurant.ui.RestTextPrimary
import com.cibus.restaurant.ui.RestTextSecondary
import com.cibus.restaurant.ui.RestTextTertiary
import com.cibus.restaurant.ui.RestGreen
import com.cibus.restaurant.ui.theme.CibusDimens
import com.cibus.restaurant.ui.theme.CibusGreen
import com.cibus.restaurant.ui.theme.CibusGreenDark
import com.cibus.restaurant.ui.theme.CibusGreenLight
import com.cibus.restaurant.ui.theme.CibusOrange
import com.cibus.restaurant.ui.theme.CibusSuccess

/**
 * PartnerOnboardingScreen — hub for unverified restaurant partners.
 * Matches iOS PartnerOnboardingView: green gradient hero with HUBB branding,
 * benefits card, and CTA buttons for unclaimed state. Status sections for other states.
 */
@Composable
fun PartnerOnboardingScreen(
    listingState: RestaurantListingState,
    claimStatus: ClaimStatusSummary?,
    onClaimNavigate: (restaurantId: String, restaurantName: String) -> Unit,
    onStatusNavigate: () -> Unit,
    onVerified: () -> Unit,
    onGetStarted: () -> Unit = {},
) {
    val ctx = LocalContext.current
    var showHomeKitchenOnboarding by remember { mutableStateOf(false) }

    // Home Kitchen onboarding full-screen dialog
    if (showHomeKitchenOnboarding) {
        com.cibus.restaurant.ui.HomeKitchenOnboardingWizard(
            onComplete = { _, _ ->
                showHomeKitchenOnboarding = false
            },
            onDismiss = { showHomeKitchenOnboarding = false }
        )
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val heroHeight = maxHeight * 0.36f

        // Background: green gradient hero top, light below
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
                    .background(RestBackground)
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Hero Section (on dark green) ────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(16.dp))

                // Storefront icon with concentric glow rings
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.10f))
                    )
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    )
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color.White,
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Title + Tagline
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Become a HUBB\nMerchant Partner",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp,
                    )

                    Text(
                        text = ResL10n.entryTagline(ctx),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = CibusDimens.spacing24),
                    )
                }

                // Status badge for non-unclaimed states
                if (listingState != RestaurantListingState.UNCLAIMED &&
                    listingState != RestaurantListingState.IMPORTED_PUBLIC
                ) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.2f),
                    ) {
                        Text(
                            text = listingState.displayLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        )
                    }
                }

                Spacer(Modifier.weight(1f))
            }

            // ── Content Section (below hero) ────────────────────────
            when (listingState) {
                RestaurantListingState.UNCLAIMED,
                RestaurantListingState.IMPORTED_PUBLIC -> {
                    FindAndClaimSection(
                        ctx = ctx,
                        onGetStarted = onGetStarted,
                        onHomeKitchen = { showHomeKitchenOnboarding = true },
                    )
                }

                RestaurantListingState.CLAIM_SUBMITTED,
                RestaurantListingState.UNDER_REVIEW -> {
                    WaitingSection(onRefresh = onStatusNavigate)
                }

                RestaurantListingState.NEEDS_MORE_INFO -> {
                    NeedsInfoSection(
                        reviewNote = claimStatus?.reviewNote,
                        onUpdateDocs = onStatusNavigate
                    )
                }

                RestaurantListingState.REJECTED -> {
                    RejectedSection(reviewNote = claimStatus?.reviewNote)
                }

                RestaurantListingState.SUSPENDED -> {
                    SuspendedSection()
                }

                RestaurantListingState.VERIFIED_PARTNER -> {
                    LaunchedEffect(Unit) { onVerified() }
                }
            }
        }
    }
}

// ── Find & Claim: Benefits Card + CTAs (matches iOS/EntryScreen) ────────────

@Composable
private fun ColumnScope.FindAndClaimSection(
    ctx: android.content.Context,
    onGetStarted: () -> Unit,
    onHomeKitchen: () -> Unit,
) {
    // Benefits card (overlapping hero)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CibusDimens.spacing16)
            .offset(y = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(CibusDimens.spacing24),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "WHY PARTNER WITH US",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = RestTextTertiary,
                letterSpacing = 0.8.sp,
            )

            BenefitRow(
                icon = Icons.Default.Schedule,
                text = "Flexible hours \u2013 open when you want",
                accent = CibusGreen,
            )
            BenefitRow(
                icon = Icons.Default.Payments,
                text = "Secure payouts, transparent fees",
                accent = CibusGreenLight,
            )
            BenefitRow(
                icon = Icons.Default.TrendingUp,
                text = "Reach more customers in your area",
                accent = CibusSuccess,
            )
            BenefitRow(
                icon = Icons.Default.Shield,
                text = "Real-time order management tools",
                accent = CibusOrange,
            )
        }
    }

    Spacer(Modifier.weight(1f))

    // CTAs
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CibusDimens.screenHorizontal)
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Primary CTA — "Get Started" / "Shuru karo"
        RestaurantPrimaryButton(
            text = ResL10n.entryGetStarted(ctx),
            onClick = onGetStarted,
            isLargeCTA = true,
        )

        // Secondary CTA — "Start Home Kitchen"
        Button(
            onClick = onHomeKitchen,
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

        // Sign in help text
        Text(
            text = ResL10n.entrySignInLink(ctx),
            fontSize = CibusDimens.bodySp,
            color = RestTextSecondary,
            modifier = Modifier.padding(vertical = 4.dp),
        )
    }
}

@Composable
private fun BenefitRow(icon: ImageVector, text: String, accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
            color = RestTextPrimary,
        )
    }
}

// ── Status sections (waiting, needs info, rejected, suspended) ──────────────

@Composable
private fun WaitingSection(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CibusDimens.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing16),
    ) {
        // Orange hourglass icon box
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF59E0B).copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.HourglassFull, null,
                modifier = Modifier.size(38.dp),
                tint = Color(0xFFF59E0B)
            )
        }

        Text(
            "Application Under Review",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = RestTextPrimary,
        )
        Text(
            "Our team is reviewing your details and documents.\nThis typically takes 1\u20133 business days.",
            fontSize = CibusDimens.bodySp,
            color = RestTextSecondary,
            textAlign = TextAlign.Center,
        )

        Button(
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CibusDimens.btnRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = CibusGreen.copy(alpha = 0.10f),
                contentColor = CibusGreen,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        ) {
            Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Refresh Status", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun NeedsInfoSection(reviewNote: String?, onUpdateDocs: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CibusDimens.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing16),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF59E0B).copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Warning, null,
                modifier = Modifier.size(38.dp),
                tint = Color(0xFFF59E0B)
            )
        }

        Text(
            "More Information Required",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = RestTextPrimary,
        )

        reviewNote?.let {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = RestBackground,
            ) {
                Text(
                    it,
                    fontSize = CibusDimens.bodySp,
                    color = RestTextSecondary,
                    modifier = Modifier.padding(14.dp),
                )
            }
        }

        RestaurantPrimaryButton(
            text = "View & Update Documents",
            onClick = onUpdateDocs,
        )
    }
}

@Composable
private fun RejectedSection(reviewNote: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CibusDimens.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing16),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFDC2626).copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Cancel, null,
                modifier = Modifier.size(38.dp),
                tint = Color(0xFFDC2626)
            )
        }

        Text(
            "Claim Not Approved",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = RestTextPrimary,
        )
        reviewNote?.let {
            Text(
                it,
                fontSize = CibusDimens.bodySp,
                color = RestTextSecondary,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            "If you believe this was an error, contact support@cibus.pk.",
            fontSize = CibusDimens.captionSp,
            color = RestTextTertiary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SuspendedSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CibusDimens.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing16),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF59E0B).copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.PauseCircle, null,
                modifier = Modifier.size(38.dp),
                tint = Color(0xFFF59E0B)
            )
        }

        Text(
            "Account Suspended",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = RestTextPrimary,
        )
        Text(
            "Your account has been temporarily suspended.\nContact support@cibus.pk.",
            fontSize = CibusDimens.bodySp,
            color = RestTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
