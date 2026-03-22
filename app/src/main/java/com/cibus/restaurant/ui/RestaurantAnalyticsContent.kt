package com.cibus.restaurant.ui

import com.cibus.restaurant.api.RetrofitClient
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.cibus.restaurant.ui.theme.CibusGreenDark
import com.cibus.restaurant.ui.theme.CibusGreenLight
import com.cibus.restaurant.ui.theme.CibusRed
import com.cibus.restaurant.ui.theme.CibusRedHot
import com.cibus.restaurant.ui.theme.CibusAmber
import com.cibus.restaurant.ui.theme.CibusOrange
import com.cibus.restaurant.ui.theme.CibusOrangeWarm
import com.cibus.restaurant.ui.theme.CibusCoral
import com.cibus.restaurant.ui.theme.CibusHeaderCard
import com.cibus.restaurant.ui.theme.CibusTextSecondary
import com.cibus.restaurant.ui.theme.CibusSurface
import com.cibus.restaurant.ui.theme.CibusSurfaceNeutral
import com.cibus.restaurant.ui.theme.CibusSurfaceGreen
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Restaurant analytics dashboard — live daily orders, revenue, kitchen pressure, rider timing, quality risk. */
/** Date filter for sales dashboard (Uber Eats-style). */
private enum class SalesDateFilter(val label: String, val daysBack: Int?) {
    TODAY("Today", 0),
    YESTERDAY("Yesterday", 1),
    LAST_7("Last 7 days", 7),
}

private fun parseOrderCreatedAt(ts: String?): Long? {
    if (ts.isNullOrBlank()) return null
    return listOf(
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") },
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") },
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.US),
    ).firstNotNullOfOrNull { fmt -> runCatching { fmt.parse(ts)?.time }.getOrNull() }
}

@Composable
fun RestaurantAnalyticsContent() {
    var hasBoost by remember { mutableStateOf(false) }
    var allOrders by remember { mutableStateOf<List<com.cibus.restaurant.api.RestaurantOrderDto>>(emptyList()) }
    var walletBalance by remember { mutableStateOf<Double?>(null) }
    var last30Revenue by remember { mutableStateOf<Double?>(null) }
    var pendingPayouts by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var completedPayouts by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var availability by remember { mutableStateOf("open") }
    var restaurantId by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var insights by remember { mutableStateOf<com.cibus.restaurant.api.RestaurantInsightsResponse?>(null) }
    var dateFilter by remember { mutableStateOf(SalesDateFilter.TODAY) }

    suspend fun loadData() {
        try {
            val me = RetrofitClient.restaurantApi.getMe().body()
            val rid = me?.restaurantId ?: return
            restaurantId = rid
            availability = me.availability ?: "open"
            val resp = RetrofitClient.restaurantApi.getMarketplaceSignals(rid).body()
            hasBoost = resp?.restaurantBoosts?.any { it.restaurantId == rid } == true
            allOrders = RetrofitClient.restaurantApi.getOrders(rid).body() ?: emptyList()
            try {
                insights = RetrofitClient.restaurantApi.getInsights(rid, 7).body()
            } catch (_: Exception) { }
            val wallet = RetrofitClient.restaurantApi.getRestaurantWallet().body()
            walletBalance = wallet?.walletBalance
            last30Revenue = wallet?.last30Revenue
            pendingPayouts = wallet?.pendingPayouts ?: emptyList()
            completedPayouts = wallet?.completedPayouts ?: emptyList()
        } catch (_: Exception) { }
        loading = false
    }

    LaunchedEffect(Unit) { loadData() }

    // Filter orders by date (Uber Eats-style date filter)
    val now = System.currentTimeMillis()
    val filteredOrders = remember(allOrders, dateFilter) {
        allOrders.filter { order ->
            val createdMs = parseOrderCreatedAt(order.createdAt) ?: return@filter true
            when (dateFilter) {
                SalesDateFilter.TODAY -> {
                    val dayStart = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    createdMs >= dayStart
                }
                SalesDateFilter.YESTERDAY -> {
                    val cal = java.util.Calendar.getInstance()
                    cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 0); cal.set(java.util.Calendar.MINUTE, 0)
                    cal.set(java.util.Calendar.SECOND, 0); cal.set(java.util.Calendar.MILLISECOND, 0)
                    val yesterdayStart = cal.timeInMillis
                    cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                    val yesterdayEnd = cal.timeInMillis
                    createdMs in yesterdayStart until yesterdayEnd
                }
                SalesDateFilter.LAST_7 -> {
                    val sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000L
                    createdMs >= sevenDaysAgo
                }
            }
        }
    }
    val preparingCount = filteredOrders.count { it.status == "preparing" || it.status == "accepted" }
    val readyCount = filteredOrders.count { it.status in listOf("ready_for_pickup", "dispatch_pending", "rider_assigned", "rider_en_route") }
    val completedCount = filteredOrders.count { it.status == "delivered" }
    val totalOrdersToday = filteredOrders.size
    val totalRevenue = filteredOrders.filter { it.status == "delivered" }.sumOf { it.total ?: 0.0 }
    val avgTicketSize = if (completedCount > 0) totalRevenue / completedCount else 0.0

    val qualityRisk = when {
        preparingCount > 8 || readyCount > 5 -> "high"
        preparingCount > 4 || readyCount > 2  -> "medium"
        else -> "low"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text("Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CibusHeaderCard)
            Spacer(Modifier.height(4.dp))
        }
        // Date filter (Uber Eats-style)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sales overview", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = CibusHeaderCard)
                Spacer(Modifier.weight(1f))
                SalesDateFilter.entries.forEach { filter ->
                    val selected = dateFilter == filter
                    Surface(
                        onClick = { dateFilter = filter },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) CibusGreenDark else CibusSurface,
                        modifier = Modifier
                    ) {
                        Text(
                            filter.label,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) Color.White else CibusTextSecondary
                        )
                    }
                }
            }
        }

        // ── Today's goal (Uber Eats) ─────────────────────────────────────
        if (!loading && totalRevenue > 0) {
            item {
                val goal = 15000.0
                val progress = (totalRevenue / goal).coerceIn(0.0, 1.0)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Today's goal", fontWeight = FontWeight.SemiBold, color = CibusHeaderCard)
                            Text("Rs ${totalRevenue.toInt()} / ${goal.toInt()}", fontSize = 12.sp, color = CibusTextSecondary)
                        }
                        LinearProgressIndicator(
                            progress = { progress.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = CibusGreenDark,
                            trackColor = CibusTextSecondary.copy(alpha = 0.2f),
                        )
                    }
                }
            }
        }

        // ── Store alerts (Uber Eats) ─────────────────────────────────────
        if (!loading) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CibusGreenDark.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = CibusGreenDark, modifier = Modifier.size(20.dp))
                        Column {
                            Text("All systems operational", fontWeight = FontWeight.SemiBold, color = CibusGreenDark)
                            Text("No store alerts. Orders flowing normally.", fontSize = 12.sp, color = CibusTextSecondary)
                        }
                    }
                }
            }
        }

        // ── Merchant Insights (popular items, peak hours) ───────────────
        insights?.let { ins ->
            if (ins.popularItems.isNotEmpty() || ins.peakHours.isNotEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = CibusSurface
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Insights", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = CibusHeaderCard)
                            if (ins.popularItems.isNotEmpty()) {
                                Text("Popular items (last ${ins.days} days)", fontSize = 12.sp, color = CibusTextSecondary)
                                ins.popularItems.take(5).forEach { item ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(item.name, fontSize = 13.sp, color = CibusHeaderCard, maxLines = 1)
                                        Text("${item.count} orders", fontSize = 12.sp, color = CibusTextSecondary)
                                    }
                                }
                            }
                            if (ins.peakHours.isNotEmpty()) {
                                Text("Peak hours", fontSize = 12.sp, color = CibusTextSecondary)
                                ins.peakHours.take(5).forEach { ph ->
                                    val hourLabel = when (ph.hour) { 0 -> "12 AM"; in 1..12 -> "$ph.hour AM"; 12 -> "12 PM"; else -> "${ph.hour - 12} PM" }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(hourLabel, fontSize = 13.sp, color = CibusHeaderCard)
                                        Text("${ph.count} orders", fontSize = 12.sp, color = CibusTextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Availability status badge ─────────────────────────────────
        if (!loading) {
            item {
                val (statusColor, statusLabel, statusIcon) = when (availability) {
                    "busy"         -> Triple(CibusAmber, "Busy", Icons.Default.Schedule)
                    "closing_soon" -> Triple(CibusOrange, "Closing Soon", Icons.Default.ExitToApp)
                    "closed"       -> Triple(CibusRed, "Closed", Icons.Default.StoreMallDirectory)
                    else           -> Triple(CibusGreenDark, "Open for Orders", Icons.Default.Store)
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = statusColor.copy(alpha = 0.10f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(16.dp))
                        Text(statusLabel, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = statusColor)
                        Spacer(Modifier.weight(1f))
                        Text("Restaurant status", style = MaterialTheme.typography.labelSmall, color = CibusTextSecondary)
                    }
                }
            }
        }

        // Boost banner
        if (hasBoost) {
            item {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = CibusGreenDark.copy(alpha = 0.15f)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.TrendingUp, null, tint = CibusGreenDark)
                        Text("Visibility boost active — your restaurant is featured", fontSize = 14.sp, color = CibusHeaderCard)
                    }
                }
            }
        }

        // ── Live metrics grid (Uber Eats-style) ───────────────────────────
        item {
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                MetricCard(
                    title = "Orders",
                    value = if (loading) "—" else "$totalOrdersToday",
                    subtitle = if (loading) "Loading…" else if (totalOrdersToday > 0) "In period" else "None yet",
                    icon = Icons.Default.Assignment,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Revenue",
                    value = if (loading) "—" else if (totalRevenue > 0) "Rs ${totalRevenue.toInt()}" else "Rs 0",
                    subtitle = if (loading) "Loading…" else "Completed orders",
                    icon = Icons.Default.AttachMoney,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Avg ticket",
                    value = if (loading) "—" else if (avgTicketSize > 0) "Rs ${avgTicketSize.toInt()}" else "—",
                    subtitle = if (loading) "…" else "Per order",
                    icon = Icons.Default.Receipt,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                MetricCard(
                    title = "Preparing Now",
                    value = if (loading) "—" else "$preparingCount",
                    subtitle = if (preparingCount > 6) "⚠ Kitchen busy" else "Active in kitchen",
                    icon = Icons.Default.Whatshot,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Awaiting Rider",
                    value = if (loading) "—" else "$readyCount",
                    subtitle = if (readyCount > 0) "Rider ~8–12 min" else "None ready",
                    icon = Icons.Default.DirectionsBike,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Wallet + Payout breakdown (Uber Eats-style) ───────────────────
        if (!loading && walletBalance != null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CibusGreenDark
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Wallet Balance", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                Text(
                                    "Rs ${walletBalance!!.toInt()}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text("Available for payout", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                            }
                            Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(32.dp))
                        }
                        if (last30Revenue != null && last30Revenue!! > 0 || pendingPayouts.isNotEmpty() || completedPayouts.isNotEmpty()) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                last30Revenue?.takeIf { it > 0 }?.let { rev ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Rs ${rev.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Last 30d", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                    }
                                }
                                if (pendingPayouts.isNotEmpty()) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${pendingPayouts.size}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Pending", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                    }
                                }
                                if (completedPayouts.isNotEmpty()) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${completedPayouts.size}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Paid out", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Quality risk ──────────────────────────────────────────────────
        if (!loading && (preparingCount > 0 || readyCount > 0)) {
            item { QualityRiskCard(preparingCount = preparingCount, readyCount = readyCount) }
        }

        // ── Rider pickup timing ───────────────────────────────────────────
        if (!loading && readyCount > 0) {
            item { RiderPickupTimingCard(readyCount = readyCount) }
        }

        // ── Kitchen load + capacity ───────────────────────────────────────
        if (!loading && preparingCount > 0) {
            item { KitchenCapacityManagementCard(preparingCount = preparingCount, readyCount = readyCount) }
        }

        item { LoyaltyInfoCard() }

        item { Text("Suggestions", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = CibusHeaderCard) }
        item { SuggestionCard(Icons.Default.Restaurant, "Add combo meals", "Combo deals drive 30% more orders. Try adding meal combinations to your menu.") }
        item { SuggestionCard(Icons.Default.Timer, "Optimize prep time", "Faster prep times lead to better ratings. Review your menu for quick-prep options.") }
        item { SuggestionCard(Icons.Default.Star, "Promote top dishes", "Once your orders are live, spotlight your most popular dishes to drive repeat orders.") }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(icon, null, tint = CibusGreenDark, modifier = Modifier.size(18.dp))
                Text(title, fontSize = 12.sp, color = CibusTextSecondary)
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CibusHeaderCard)
            Text(subtitle, fontSize = 11.sp, color = CibusTextSecondary)
        }
    }
}

@Composable
private fun TopDishRow(rank: Int, name: String, orders: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CibusGreenDark.copy(alpha = 0.15f)
            ) {
                Text(
                    "#$rank",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CibusGreenDark
                )
            }
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = CibusHeaderCard)
        }
        Text("$orders orders", fontSize = 13.sp, color = CibusTextSecondary)
    }
}

@Composable
private fun RowScope.PopularityChip(label: String, count: Int, color: Color) {
    Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 12.sp, color = CibusTextSecondary)
            Text("$count items", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

// Phase 87: Restaurant loyalty visibility (Android)
@Composable
private fun LoyaltyInfoCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CibusSurfaceNeutral
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Default.CardGiftcard, null, tint = CibusGreenDark, modifier = Modifier.size(24.dp))
            Column {
                Text("Cibus Loyalty", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CibusHeaderCard)
                Text(
                    "Your restaurant participates in Cibus Loyalty. Repeat customers earn points per order and get rewards like free delivery and discounts. Favourite restaurants earn 25% bonus points.",
                    fontSize = 12.sp, color = CibusTextSecondary
                )
            }
        }
    }
}

// Phase 119 — Restaurant quality risk signal card
@Composable
private fun QualityRiskCard(preparingCount: Int, readyCount: Int) {
    val qualityRisk = when {
        preparingCount > 8 || readyCount > 5 -> "high"
        preparingCount > 4 || readyCount > 2 -> "medium"
        else -> "low"
    }
    val (riskColor, riskLabel, riskIcon) = when (qualityRisk) {
        "high"   -> Triple(CibusRedHot, "High customer wait risk — consider pausing new orders", Icons.Default.Warning)
        "medium" -> Triple(CibusOrangeWarm, "Moderate queue pressure — monitor pickup readiness", Icons.Default.Info)
        else     -> Triple(CibusGreenDark, "Queue is healthy", Icons.Default.CheckCircle)
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = riskColor.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(riskIcon, null, tint = riskColor, modifier = Modifier.size(22.dp))
            Column {
                Text("Quality risk level: ${qualityRisk.replaceFirstChar { it.uppercase() }}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CibusHeaderCard)
                Text(riskLabel, fontSize = 12.sp, color = CibusTextSecondary)
            }
        }
    }
}

/** Phase 122/G: Rider pickup timing card — helps restaurants coordinate when to mark orders ready. */
@Composable
private fun RiderPickupTimingCard(readyCount: Int) {
    if (readyCount == 0) return
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CibusGreenDark.copy(alpha = 0.07f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DirectionsBike, null, tint = CibusGreenDark, modifier = Modifier.size(22.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Rider Pickup Window",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CibusHeaderCard
                )
                Text(
                    "$readyCount order${if (readyCount > 1) "s" else ""} awaiting pickup · Rider expected in 8–12 min",
                    fontSize = 12.sp,
                    color = CibusTextSecondary
                )
                Text(
                    "Keep food at temperature · Seal packaging",
                    fontSize = 11.sp,
                    color = CibusGreenDark
                )
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    icon: ImageVector,
    title: String,
    body: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CibusSurfaceGreen
    ) {
    Row(
        modifier = Modifier.padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, null, tint = CibusGreenDark, modifier = Modifier.size(24.dp))
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CibusHeaderCard)
            Text(body, fontSize = 12.sp, color = CibusTextSecondary)
        }
    }
    }
}

/** Phase 125/126: Kitchen capacity management + prep prediction card. */
@Composable
private fun KitchenCapacityManagementCard(preparingCount: Int, readyCount: Int) {
    if (preparingCount == 0) return
    val capacityLabel = when {
        preparingCount > 8 -> "Overloaded"
        preparingCount > 5 -> "Strained"
        preparingCount > 2 -> "Busy"
        else -> "Optimal"
    }
    val capacityColor = when (capacityLabel) {
        "Overloaded" -> CibusRed
        "Strained"   -> CibusOrange
        "Busy"       -> CibusAmber
        else         -> CibusGreenDark
    }
    // Phase 126: Predict prep time for next order based on kitchen load
    val prepPrediction = when {
        preparingCount > 6 -> (12 * 1.5).toInt()
        preparingCount > 3 -> (12 * 1.25).toInt()
        else -> 12
    }
    val shouldThrottle = preparingCount > 7

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = capacityColor.copy(alpha = 0.06f)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Whatshot, null, tint = capacityColor, modifier = Modifier.size(18.dp))
                Text(
                    "Kitchen: $capacityLabel · $preparingCount cooking",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CibusHeaderCard
                )
            }
            // Capacity bar
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(CibusSurfaceNeutral, RoundedCornerShape(2.dp))
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (preparingCount.toFloat() / 10f).coerceIn(0f, 1f))
                        .height(4.dp)
                        .background(capacityColor, RoundedCornerShape(2.dp))
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Next order: ~${prepPrediction} min prep",
                    fontSize = 11.sp,
                    color = CibusTextSecondary
                )
                if (shouldThrottle) {
                    Text(
                        "⏸ Consider pausing new orders",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = CibusRed
                    )
                }
            }
        }
    }
}
