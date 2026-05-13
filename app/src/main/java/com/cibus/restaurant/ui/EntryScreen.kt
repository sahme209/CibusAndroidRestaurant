package com.cibus.restaurant.ui

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CibusSurfaceNeutral)
    ) {
        AIAuroraBackground(intensity = 0.2f)
        AIParticleField(particleCount = 18, intensity = 0.35f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(64.dp))

                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(CibusGreen.copy(alpha = 0.12f)),
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

                Spacer(Modifier.height(40.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    Column(Modifier.padding(vertical = 4.dp)) {
                        EntryBenefitRow(
                            Icons.Default.Schedule,
                            if (ResL10n.isUrdu(ctx)) "Flexible Hours" else "Flexible Hours",
                            if (ResL10n.isUrdu(ctx)) "Jab chahein kholen, apni marzi" else "Open when you want, on your schedule",
                            CibusGreenLight,
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
                            CibusSuccess,
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
                            CibusAmberLight,
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
                            CibusGreenLight,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            HorizontalDivider(color = AppleSeparator.copy(alpha = 0.3f), thickness = 0.5.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CibusSurfaceNeutral)
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 34.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RestGreen,
                    ),
                ) {
                    Text(
                        ResL10n.entryGetStarted(ctx),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }

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
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                TextButton(
                    onClick = onSignIn,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text(
                        ResL10n.entrySignInLink(ctx),
                        fontSize = 15.sp,
                        color = CibusGreen,
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryBenefitRow(icon: ImageVector, title: String, subtitle: String, accent: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 13.sp, color = AppleLabelSecondary)
        }
    }
}
