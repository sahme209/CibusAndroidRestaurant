package com.cibus.restaurant.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.cibus.restaurant.ResL10n
import androidx.compose.material3.HorizontalDivider
import com.cibus.restaurant.ui.theme.CibusGreenDark
import com.cibus.restaurant.ui.theme.CibusGreenLight
import com.cibus.restaurant.ui.theme.CibusSurfaceNeutral
import com.cibus.restaurant.ui.theme.CibusTextPrimary
import com.cibus.restaurant.ui.theme.CibusTextSecondary

private enum class MoreDestination {
    Hub,
    Chain,
    Payouts,
    Reviews,
    Issues,
    Inbox,
    Settings,
}

/**
 * Hub for secondary destinations: Wallet, Reviews, Issues, Inbox, Settings (and Chain when applicable).
 */
@Composable
fun RestaurantMoreScreen(
    hasChain: Boolean,
    onLogout: () -> Unit,
) {
    val ctx = LocalContext.current
    var destination by remember { mutableStateOf(MoreDestination.Hub) }

    when (destination) {
        MoreDestination.Hub -> MoreHubList(
            ctx = ctx,
            hasChain = hasChain,
            onNavigate = { destination = it },
        )
        MoreDestination.Chain -> MoreNestedScaffold(
            title = ResL10n.navChainOverview(ctx),
            onBack = { destination = MoreDestination.Hub },
        ) {
            ChainDashboardContent()
        }
        MoreDestination.Payouts -> MoreNestedScaffold(
            title = ResL10n.navWalletPayouts(ctx),
            onBack = { destination = MoreDestination.Hub },
        ) {
            RestaurantPayoutsContent()
        }
        MoreDestination.Reviews -> MoreNestedScaffold(
            title = ResL10n.navReviews(ctx),
            onBack = { destination = MoreDestination.Hub },
        ) {
            RestaurantReviewsContent()
        }
        MoreDestination.Issues -> MoreNestedScaffold(
            title = ResL10n.navOrderIssues(ctx),
            onBack = { destination = MoreDestination.Hub },
        ) {
            RestaurantOrderIssuesContent()
        }
        MoreDestination.Inbox -> MoreNestedScaffold(
            title = ResL10n.navInbox(ctx),
            onBack = { destination = MoreDestination.Hub },
        ) {
            RestaurantInboxContent()
        }
        MoreDestination.Settings -> MoreNestedScaffold(
            title = ResL10n.moreRowSettingsTitle(ctx),
            onBack = { destination = MoreDestination.Hub },
        ) {
            RestaurantMoreContent(onLogout = onLogout)
        }
    }
}

@Composable
private fun MoreHubList(
    ctx: Context,
    hasChain: Boolean,
    onNavigate: (MoreDestination) -> Unit,
) {
    Scaffold(
        containerColor = CibusSurfaceNeutral,
        topBar = {
            Surface(shadowElevation = 0.dp, color = CibusSurfaceNeutral) {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                    Text(
                        ResL10n.tabMore(ctx),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = CibusTextPrimary,
                    )
                    Text(
                        ResL10n.moreHubBrandSubtitle(ctx),
                        style = MaterialTheme.typography.bodySmall,
                        color = CibusTextSecondary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    ResL10n.moreSectionTools(ctx),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = CibusTextSecondary,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
                )
            }
            if (hasChain) {
                item {
                    MoreRow(
                        title = ResL10n.moreRowChainTitle(ctx),
                        subtitle = ResL10n.moreRowChainSubtitle(ctx),
                        icon = Icons.Default.Business,
                        onClick = { onNavigate(MoreDestination.Chain) },
                    )
                }
            }
            item {
                MoreRow(
                    title = ResL10n.moreRowWalletTitle(ctx),
                    subtitle = ResL10n.moreRowWalletSubtitle(ctx),
                    icon = Icons.Default.AccountBalanceWallet,
                    onClick = { onNavigate(MoreDestination.Payouts) },
                )
            }
            item {
                MoreRow(
                    title = ResL10n.moreRowReviewsTitle(ctx),
                    subtitle = ResL10n.moreRowReviewsSubtitle(ctx),
                    icon = Icons.Default.Star,
                    onClick = { onNavigate(MoreDestination.Reviews) },
                )
            }
            item {
                MoreRow(
                    title = ResL10n.moreRowIssuesTitle(ctx),
                    subtitle = ResL10n.moreRowIssuesSubtitle(ctx),
                    icon = Icons.Default.Warning,
                    onClick = { onNavigate(MoreDestination.Issues) },
                )
            }
            item {
                MoreRow(
                    title = ResL10n.moreRowInboxTitle(ctx),
                    subtitle = ResL10n.moreRowInboxSubtitle(ctx),
                    icon = Icons.Default.Mail,
                    onClick = { onNavigate(MoreDestination.Inbox) },
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color(0xFF3A3A3C),
                )
            }
            item {
                MoreRow(
                    title = ResL10n.moreRowSettingsTitle(ctx),
                    subtitle = ResL10n.moreRowSettingsSubtitle(ctx),
                    icon = Icons.Default.Settings,
                    onClick = { onNavigate(MoreDestination.Settings) },
                )
            }
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1B4332).copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, CibusGreenLight.copy(alpha = 0.28f)),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🇵🇰", style = MaterialTheme.typography.titleMedium)
                            Text(
                                ResL10n.moreHubPakistanTipTitle(ctx),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                        }
                        Text(
                            ResL10n.moreHubPakistanTipBody(ctx),
                            style = MaterialTheme.typography.bodySmall,
                            color = CibusTextSecondary,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(28.dp)) }
        }
    }
}

@Composable
private fun MoreRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, CibusGreenLight.copy(alpha = 0.28f)),
                RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF2C2C2E),
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CibusGreenDark.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color(0xFF6EE7B7),
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = Color.White, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = CibusTextSecondary, lineHeight = 18.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF636366))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreNestedScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CibusSurfaceNeutral,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                ),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            content()
        }
    }
}
