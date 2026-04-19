package com.cibus.restaurant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import com.cibus.restaurant.ResL10n
import com.cibus.restaurant.ui.theme.CibusDimens

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
 * Light theme to match the rest of the app.
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
        containerColor = RestBackground,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),
        ) {
            // Header
            item {
                Column(Modifier.padding(vertical = 18.dp)) {
                    Text(
                        ResL10n.tabMore(ctx),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = RestTextPrimary,
                    )
                    Text(
                        ResL10n.moreHubBrandSubtitle(ctx),
                        fontSize = CibusDimens.captionSp,
                        color = RestTextSecondary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            // Tools section label
            item {
                Text(
                    ResL10n.moreSectionTools(ctx),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RestTextTertiary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
                )
            }

            // Tools card
            item {
                Surface(
                    shape = RoundedCornerShape(CibusDimens.radiusLg),
                    color = RestCardBG,
                    border = BorderStroke(1.dp, RestDivider),
                ) {
                    Column {
                        if (hasChain) {
                            MoreRow(
                                title = ResL10n.moreRowChainTitle(ctx),
                                subtitle = ResL10n.moreRowChainSubtitle(ctx),
                                icon = Icons.Default.Business,
                                iconColor = RestGreen,
                                onClick = { onNavigate(MoreDestination.Chain) },
                            )
                            HorizontalDivider(color = RestDivider, modifier = Modifier.padding(start = 64.dp))
                        }
                        MoreRow(
                            title = ResL10n.moreRowWalletTitle(ctx),
                            subtitle = ResL10n.moreRowWalletSubtitle(ctx),
                            icon = Icons.Default.AccountBalanceWallet,
                            iconColor = RestGreen,
                            onClick = { onNavigate(MoreDestination.Payouts) },
                        )
                        HorizontalDivider(color = RestDivider, modifier = Modifier.padding(start = 64.dp))
                        MoreRow(
                            title = ResL10n.moreRowReviewsTitle(ctx),
                            subtitle = ResL10n.moreRowReviewsSubtitle(ctx),
                            icon = Icons.Default.Star,
                            iconColor = Color(0xFFF59E0B),
                            onClick = { onNavigate(MoreDestination.Reviews) },
                        )
                        HorizontalDivider(color = RestDivider, modifier = Modifier.padding(start = 64.dp))
                        MoreRow(
                            title = ResL10n.moreRowIssuesTitle(ctx),
                            subtitle = ResL10n.moreRowIssuesSubtitle(ctx),
                            icon = Icons.Default.Warning,
                            iconColor = Color(0xFFB45309),
                            onClick = { onNavigate(MoreDestination.Issues) },
                        )
                        HorizontalDivider(color = RestDivider, modifier = Modifier.padding(start = 64.dp))
                        MoreRow(
                            title = ResL10n.moreRowInboxTitle(ctx),
                            subtitle = ResL10n.moreRowInboxSubtitle(ctx),
                            icon = Icons.Default.Mail,
                            iconColor = Color(0xFF3B82F6),
                            onClick = { onNavigate(MoreDestination.Inbox) },
                        )
                    }
                }
            }

            // Settings section label
            item {
                Text(
                    ResL10n.moreRowSettingsTitle(ctx),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RestTextTertiary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 8.dp),
                )
            }

            // Settings card
            item {
                Surface(
                    shape = RoundedCornerShape(CibusDimens.radiusLg),
                    color = RestCardBG,
                    border = BorderStroke(1.dp, RestDivider),
                ) {
                    MoreRow(
                        title = ResL10n.moreRowSettingsTitle(ctx),
                        subtitle = ResL10n.moreRowSettingsSubtitle(ctx),
                        icon = Icons.Default.Settings,
                        iconColor = RestTextSecondary,
                        onClick = { onNavigate(MoreDestination.Settings) },
                    )
                }
            }

            // Pakistan partner tip card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(CibusDimens.radiusLg),
                    color = RestGreen.copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, RestGreen.copy(alpha = 0.15f)),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, contentDescription = "Business information", tint = RestGreen, modifier = Modifier.size(18.dp))
                            Text(
                                ResL10n.moreHubPakistanTipTitle(ctx),
                                fontSize = CibusDimens.bodySp,
                                fontWeight = FontWeight.SemiBold,
                                color = RestTextPrimary,
                            )
                        }
                        Text(
                            ResL10n.moreHubPakistanTipBody(ctx),
                            fontSize = CibusDimens.captionSp,
                            color = RestTextSecondary,
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
    iconColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = FontWeight.Medium,
                fontSize = CibusDimens.bodySp,
                color = RestTextPrimary,
            )
            Text(
                subtitle,
                fontSize = CibusDimens.captionSp,
                color = RestTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = RestTextTertiary,
            modifier = Modifier.size(18.dp),
        )
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
        containerColor = RestBackground,
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RestBackground,
                    titleContentColor = RestTextPrimary,
                    navigationIconContentColor = RestTextPrimary,
                ),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            content()
        }
    }
}
