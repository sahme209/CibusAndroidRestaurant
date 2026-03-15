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

/** Phase 83 + 99 + Phase 116D: Restaurant analytics dashboard — daily orders, top dishes, prep time, delivery, kitchen pressure, boost banner. */
@Composable
fun RestaurantAnalyticsContent() {
    var hasBoost by remember { mutableStateOf(false) }
    var preparingCount by remember { mutableStateOf(0) }
    var readyCount by remember { mutableStateOf(0) }
    var totalOrdersToday by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        try {
            val me = RetrofitClient.restaurantApi.getMe().body()
            val rid = me?.restaurantId ?: return@LaunchedEffect
            val resp = RetrofitClient.restaurantApi.getMarketplaceSignals(rid).body()
            hasBoost = resp?.restaurantBoosts?.any { it.restaurantId == rid } == true
            val orders = RetrofitClient.restaurantApi.getOrders(rid).body() ?: emptyList()
            preparingCount = orders.count { it.status == "preparing" }
            readyCount = orders.count { it.status == "ready_for_pickup" }
            totalOrdersToday = orders.size
        } catch (_: Exception) { }
    }

    val kitchenPressureLabel = when {
        preparingCount > 6 -> "High"
        preparingCount > 2 -> "Medium"
        else -> "Normal"
    }
    val kitchenPressureColor = when (kitchenPressureLabel) {
        "High" -> CibusRed
        "Medium" -> CibusAmber
        else -> CibusGreenDark
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Analytics",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = CibusHeaderCard
            )
        }
        if (hasBoost) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CibusGreenDark.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = CibusGreenDark)
                        Text("Visibility boost active — your restaurant is featured", fontSize = 14.sp, color = CibusHeaderCard)
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Daily Orders",
                    value = if (totalOrdersToday > 0) "$totalOrdersToday" else "—",
                    subtitle = if (totalOrdersToday > 0) "Today" else "Not yet available",
                    icon = Icons.Default.ShoppingCart,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Avg Prep",
                    value = "—",
                    subtitle = "Not yet available",
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Delivery Time",
                    value = "—",
                    subtitle = "Not yet available",
                    icon = Icons.Default.DeliveryDining,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Revenue",
                    value = "—",
                    subtitle = "Not yet available",
                    icon = Icons.Default.AttachMoney,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Phase 116D: Live kitchen queue pressure card
        if (preparingCount > 0 || readyCount > 0) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = kitchenPressureColor.copy(alpha = 0.08f)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Kitchen Queue",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CibusHeaderCard
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = kitchenPressureColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    kitchenPressureLabel,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = kitchenPressureColor
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (preparingCount > 0) {
                                Column {
                                    Text("$preparingCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CibusGreenDark)
                                    Text("preparing", fontSize = 11.sp, color = CibusTextSecondary)
                                }
                            }
                            if (readyCount > 0) {
                                Column {
                                    Text("$readyCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CibusAmber)
                                    Text("awaiting rider", fontSize = 11.sp, color = CibusTextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Top Selling Dishes",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = CibusHeaderCard
            )
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CibusSurfaceNeutral
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Top dish data will be available once order analytics are configured.",
                        fontSize = 14.sp,
                        color = CibusTextSecondary
                    )
                }
            }
        }

        item {
            Text(
                "Dish Popularity Signals",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = CibusHeaderCard
            )
        }
        item {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = CibusSurfaceNeutral) {
                Text(
                    "Popularity signals will be available once order analytics are configured.",
                    fontSize = 14.sp,
                    color = CibusTextSecondary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        item { LoyaltyInfoCard() }
        item { QualityRiskCard(preparingCount = preparingCount, readyCount = readyCount) }
        item { RiderPickupTimingCard(readyCount = readyCount) }
        item { KitchenCapacityManagementCard(preparingCount = preparingCount, readyCount = readyCount) }

        item {
            Text("Suggestions", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = CibusHeaderCard)
        }
        item {
            SuggestionCard(icon = Icons.Default.Restaurant, title = "Add combo meals", body = "Combo deals drive 30% more orders. Try adding meal combinations to your menu.")
        }
        item {
            SuggestionCard(icon = Icons.Default.Timer, title = "Optimize prep time", body = "Faster prep times lead to better ratings. Review your menu for quick-prep options.")
        }
        item {
            SuggestionCard(icon = Icons.Default.Star, title = "Promote top dishes", body = "Once your orders are live, spotlight your most popular dishes to drive repeat orders.")
        }

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
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
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
