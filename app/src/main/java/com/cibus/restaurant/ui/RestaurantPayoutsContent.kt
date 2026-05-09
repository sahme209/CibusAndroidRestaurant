package com.cibus.restaurant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.api.RestaurantWalletResponse
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RestaurantPayoutsContent() {
    var wallet by remember { mutableStateOf<RestaurantWalletResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val tabs = listOf("Overview", "Transactions")

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val resp = RetrofitClient.restaurantApi.getRestaurantWallet()
                if (resp.isSuccessful) wallet = resp.body()
            } catch (_: Exception) {}
            loading = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab, containerColor = Color.White) {
            tabs.forEachIndexed { idx, title ->
                Tab(selected = selectedTab == idx, onClick = { selectedTab = idx },
                    text = { Text(title, fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal) })
            }
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CibusGreen)
            }
        } else if (wallet == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Could not load wallet data", color = AppleLabelSecondary)
            }
        } else {
            when (selectedTab) {
                0 -> PayoutOverview(wallet!!)
                1 -> PayoutTransactions(wallet!!)
            }
        }
    }
}

@Composable
private fun PayoutOverview(wallet: RestaurantWalletResponse) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(CibusDimens.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance card
        Surface(shape = RoundedCornerShape(CibusDimens.cardRadius), modifier = Modifier.fillMaxWidth()) {
            Box(
                Modifier.background(CibusGreen)
                    .padding(20.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Payout Wallet", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                        Spacer(Modifier.height(4.dp))
                        Text("Rs ${wallet.walletBalance?.toInt() ?: 0}", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Spacer(Modifier.height(2.dp))
                        Text("Available for payout", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                    Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                }
            }
        }

        // KPI row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PayoutKpi(Modifier.weight(1f), "30-Day Revenue", "Rs ${wallet.last30Revenue?.toInt() ?: 0}", Icons.Default.BarChart, CibusGreen)
            PayoutKpi(Modifier.weight(1f), "Pending", "${wallet.pendingPayoutsCount}", Icons.Default.Schedule, CibusAmber)
            PayoutKpi(Modifier.weight(1f), "Completed", "${wallet.completedPayoutsCount}", Icons.Default.CheckCircle, Color(0xFF2196F3))
        }

        // Commission breakdown — only shown when commission rate is available from API
        val revenue = wallet.last30Revenue ?: 0.0
        val rate = wallet.commissionRate
        if (rate != null && revenue > 0) {
            val commission = revenue * rate
            val net = revenue - commission
            Surface(shape = RoundedCornerShape(CibusDimens.cardRadius), color = CibusCardBg, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Earnings Breakdown (30 days)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppleLabelPrimary)
                    BreakdownRow("Gross Revenue", revenue, AppleLabelPrimary)
                    BreakdownRow("Platform Fee (${(rate * 100).toInt()}%)", -commission, CibusRed)
                    HorizontalDivider(color = AppleSeparator.copy(alpha = 0.4f))
                    BreakdownRow("Net Earnings", net, CibusGreenDark, bold = true)
                }
            }
        } else if (revenue > 0) {
            Surface(shape = RoundedCornerShape(CibusDimens.cardRadius), color = CibusCardBg, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Earnings (30 days)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppleLabelPrimary)
                    BreakdownRow("Gross Revenue", revenue, AppleLabelPrimary)
                    Text("Commission breakdown will appear once your rate is configured.", fontSize = 12.sp, color = AppleLabelSecondary)
                }
            }
        }

        // ── Monthly Summary card ──────────────────────────────────────────
        run {
            val monthRevenue = wallet.last30Revenue ?: 0.0
            val commRate = wallet.commissionRate
            val estimatedCommission = if (commRate != null && monthRevenue > 0) monthRevenue * commRate else null
            val netEstimated = if (estimatedCommission != null) monthRevenue - estimatedCommission else null

            if (monthRevenue > 0 || wallet.pendingPayoutsCount > 0 || wallet.completedPayoutsCount > 0) {
                Surface(
                    shape = RoundedCornerShape(CibusDimens.cardRadius),
                    color = CibusCardBg,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, tint = CibusGreenDark, modifier = Modifier.size(20.dp))
                            Text("Monthly Summary", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppleLabelPrimary)
                        }

                        if (monthRevenue > 0) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("30-day revenue", fontSize = 14.sp, color = AppleLabelSecondary)
                                Text("Rs ${monthRevenue.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppleLabelPrimary)
                            }
                        }

                        if (estimatedCommission != null) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Estimated commission (${((commRate ?: 0.0) * 100).toInt()}%)", fontSize = 14.sp, color = AppleLabelSecondary)
                                Text("-Rs ${estimatedCommission.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = CibusRed)
                            }
                        }

                        if (netEstimated != null) {
                            HorizontalDivider(color = AppleSeparator.copy(alpha = 0.4f))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Net estimated earnings", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppleLabelPrimary)
                                Text("Rs ${netEstimated.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CibusGreenDark)
                            }
                        }

                        if (wallet.pendingPayoutsCount > 0 || wallet.completedPayoutsCount > 0) {
                            HorizontalDivider(color = AppleSeparator.copy(alpha = 0.4f))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${wallet.pendingPayoutsCount}", fontWeight = FontWeight.Bold, color = CibusAmber)
                                    Text("Pending payouts", fontSize = 11.sp, color = AppleLabelSecondary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${wallet.completedPayoutsCount}", fontWeight = FontWeight.Bold, color = CibusGreen)
                                    Text("Completed payouts", fontSize = 11.sp, color = AppleLabelSecondary)
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CibusGreenDark.copy(alpha = 0.06f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, null, tint = CibusGreenDark, modifier = Modifier.size(16.dp))
                                Text(
                                    "Full monthly report available at end of billing cycle",
                                    fontSize = 12.sp,
                                    color = AppleLabelSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        if (wallet.pendingPayoutsCount == 0 && wallet.completedPayoutsCount == 0) {
            Box(Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AccountBalance, null, tint = AppleLabelSecondary.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No payout history yet", fontWeight = FontWeight.SemiBold, color = AppleLabelPrimary)
                    Text("Revenue from delivered orders will appear here", fontSize = 13.sp, color = AppleLabelSecondary, textAlign = TextAlign.Center)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PayoutTransactions(wallet: RestaurantWalletResponse) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(CibusDimens.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Recent Transactions", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppleLabelPrimary)

        if (wallet.pendingPayoutsCount == 0 && wallet.completedPayoutsCount == 0) {
            Box(Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Receipt, null, tint = AppleLabelSecondary.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No transactions yet", fontWeight = FontWeight.SemiBold, color = AppleLabelPrimary)
                    Text("Completed payouts will appear here", fontSize = 13.sp, color = AppleLabelSecondary)
                }
            }
        } else {
            if (wallet.pendingPayoutsCount > 0) {
                Text("PENDING (${wallet.pendingPayoutsCount})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CibusAmber)
                wallet.pendingPayouts?.forEachIndexed { index, payout ->
                    val amount = (payout["amount"] as? Number)?.toInt()
                    val date = payout["createdAt"] as? String
                    TransactionRow(
                        title = if (amount != null) "Rs $amount" else "Payout pending",
                        subtitle = date ?: "Processing",
                        icon = Icons.Default.Schedule,
                        color = CibusAmber,
                        status = "Pending"
                    )
                }
            }
            if (wallet.completedPayoutsCount > 0) {
                Text("COMPLETED (${wallet.completedPayoutsCount})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CibusGreen)
                wallet.completedPayouts?.take(10)?.forEachIndexed { index, payout ->
                    val amount = (payout["amount"] as? Number)?.toInt()
                    val date = payout["createdAt"] as? String
                    TransactionRow(
                        title = if (amount != null) "Rs $amount" else "Payout completed",
                        subtitle = date ?: "Transferred to bank",
                        icon = Icons.Default.CheckCircle,
                        color = CibusGreen,
                        status = "Complete"
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PayoutKpi(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Surface(modifier = modifier, shape = RoundedCornerShape(CibusDimens.cardRadius), color = CibusCardBg) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Text(value, fontWeight = FontWeight.Bold, color = AppleLabelPrimary)
            Text(title, fontSize = 11.sp, color = AppleLabelSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun BreakdownRow(label: String, amount: Double, color: Color, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = if (bold) 15.sp else 14.sp, fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal, color = AppleLabelSecondary)
        val sign = if (amount < 0) "-" else ""
        Text("${sign}Rs ${kotlin.math.abs(amount).toInt()}", fontSize = if (bold) 16.sp else 14.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium, color = color)
    }
}

@Composable
private fun TransactionRow(title: String, subtitle: String, icon: ImageVector, color: Color, status: String) {
    Surface(shape = RoundedCornerShape(CibusDimens.cardRadius), color = CibusCardBg, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.12f)) {
                Icon(icon, null, tint = color, modifier = Modifier.padding(10.dp).size(18.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, color = AppleLabelPrimary)
                Text(subtitle, fontSize = 12.sp, color = AppleLabelSecondary)
            }
            Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.1f)) {
                Text(status, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
            }
        }
    }
}
