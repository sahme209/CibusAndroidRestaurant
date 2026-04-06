package com.cibus.restaurant.ui

// "More" tab — slimmed settings. Store controls moved to Store tab.

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.ui.theme.CibusDimens
import com.cibus.restaurant.ui.theme.CibusGreen
import com.cibus.restaurant.ui.theme.CibusGreenDark
import com.cibus.restaurant.ui.theme.CibusRed
import com.cibus.restaurant.ui.theme.CibusTextOnSurface
import com.cibus.restaurant.ui.theme.CibusTextOnSurfaceSecondary
import com.cibus.restaurant.ui.theme.CibusTextTertiary

/** "More" tab — navigation to sub-screens + sign out. */
@Composable
fun RestaurantMoreContent(onLogout: () -> Unit = {}) {
    var restaurantName by remember { mutableStateOf("") }
    var partnerName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Show existing tabs that were previously in the bottom bar
    var showPromotions by remember { mutableStateOf(false) }
    var showPayouts by remember { mutableStateOf(false) }
    var showReviews by remember { mutableStateOf(false) }
    var showIssues by remember { mutableStateOf(false) }
    var showInbox by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val resp = RetrofitClient.restaurantApi.getMe()
            if (resp.isSuccessful) {
                restaurantName = resp.body()?.restaurantName ?: ""
                partnerName = resp.body()?.partnerName ?: ""
                email = resp.body()?.email ?: ""
            }
        } catch (_: Exception) {}
    }

    // Full-screen content for sub-tabs
    if (showPromotions) { RestaurantPromotionsContent(); return }
    if (showPayouts) { RestaurantPayoutsContent(); return }
    if (showReviews) { RestaurantReviewsContent(); return }
    if (showIssues) { RestaurantOrderIssuesContent(); return }
    if (showInbox) { RestaurantInboxContent(); return }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = CibusDimens.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing8)
    ) {
        // Header
        item {
            Spacer(Modifier.height(CibusDimens.spacing24))
            Text("More", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CibusTextOnSurface)
            if (partnerName.isNotEmpty()) {
                Spacer(Modifier.height(CibusDimens.spacing4))
                Text(partnerName, fontSize = CibusDimens.bodySp, color = CibusTextOnSurfaceSecondary)
            }
            if (email.isNotEmpty()) {
                Text(email, fontSize = CibusDimens.captionSp, color = CibusTextTertiary)
            }
            Spacer(Modifier.height(CibusDimens.spacing16))
        }

        // Management section
        item {
            Text("MANAGEMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CibusTextTertiary, letterSpacing = 1.sp)
            Spacer(Modifier.height(CibusDimens.spacing8))
        }

        item {
            Surface(
                shape = RoundedCornerShape(CibusDimens.radiusLg),
                shadowElevation = CibusDimens.cardElevation,
                color = Color.White
            ) {
                Column {
                    MoreMenuItem(Icons.Default.LocalOffer, "Promotions", "Manage deals & campaigns") {
                        showPromotions = true
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    MoreMenuItem(Icons.Default.AccountBalanceWallet, "Wallet & Payouts", "Balance, payout history") {
                        showPayouts = true
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    MoreMenuItem(Icons.Default.Star, "Reviews", "Customer feedback") {
                        showReviews = true
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    MoreMenuItem(Icons.Default.Warning, "Order Issues", "Disputes & refunds") {
                        showIssues = true
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    MoreMenuItem(Icons.Default.Mail, "Inbox", "Messages") {
                        showInbox = true
                    }
                }
            }
        }

        // Settings section
        item {
            Spacer(Modifier.height(CibusDimens.spacing24))
            Text("SETTINGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CibusTextTertiary, letterSpacing = 1.sp)
            Spacer(Modifier.height(CibusDimens.spacing8))
        }

        item {
            Surface(
                shape = RoundedCornerShape(CibusDimens.radiusLg),
                shadowElevation = CibusDimens.cardElevation,
                color = Color.White
            ) {
                Column {
                    MoreMenuItem(Icons.Default.Language, "Language", "English / اردو") {}
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    MoreMenuItem(Icons.Default.Notifications, "Notifications", "Push, sound, alerts") {}
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    MoreMenuItem(Icons.Default.Business, "Business Profile", restaurantName.ifEmpty { "View details" }) {}
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    MoreMenuItem(Icons.Default.Info, "Help & Support", "Contact partner support") {}
                }
            }
        }

        // Sign out
        item {
            Spacer(Modifier.height(CibusDimens.spacing24))
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(CibusDimens.btnRadius),
                colors = ButtonDefaults.buttonColors(containerColor = CibusRed),
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Log out", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(CibusDimens.spacing16))
            Text(
                "HUBB Merchant v1.0",
                fontSize = CibusDimens.captionSp,
                color = CibusTextTertiary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(CibusDimens.spacing32))
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = CibusDimens.cardPadding, vertical = CibusDimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CibusDimens.spacing12)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = CibusGreen
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = CibusDimens.bodySp, fontWeight = FontWeight.Medium, color = CibusTextOnSurface)
            Text(subtitle, fontSize = CibusDimens.captionSp, color = CibusTextOnSurfaceSecondary)
        }
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = CibusTextTertiary
        )
    }
}
