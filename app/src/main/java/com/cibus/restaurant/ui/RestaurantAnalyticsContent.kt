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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Phase 83 + 99: Restaurant analytics dashboard — daily orders, top dishes, prep time, delivery, boost banner. */
@Composable
fun RestaurantAnalyticsContent() {
    var hasBoost by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        try {
            val me = RetrofitClient.restaurantApi.getMe().body()
            val rid = me?.restaurantId ?: return@LaunchedEffect
            val resp = RetrofitClient.restaurantApi.getMarketplaceSignals(rid).body()
            hasBoost = resp?.restaurantBoosts?.any { it.restaurantId == rid } == true
        } catch (_: Exception) { }
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
                color = Color(0xFF1A1A1A)
            )
        }
        if (hasBoost) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF2D6A4F).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF2D6A4F))
                        Text("Visibility boost active — your restaurant is featured", fontSize = 14.sp, color = Color(0xFF1A1A1A))
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
                    value = "24",
                    subtitle = "vs 18 yesterday",
                    icon = Icons.Default.ShoppingCart,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Avg Prep",
                    value = "12 min",
                    subtitle = "Target: 10 min",
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
                    value = "28 min",
                    subtitle = "Average ETA",
                    icon = Icons.Default.DeliveryDining,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Revenue",
                    value = "Rs 18,500",
                    subtitle = "Today",
                    icon = Icons.Default.AttachMoney,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                "Top Selling Dishes",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TopDishRow(rank = 1, name = "Beef Cheese Burger", orders = 12)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    TopDishRow(rank = 2, name = "Chicken Biryani", orders = 9)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    TopDishRow(rank = 3, name = "Chocolate Ice Cream", orders = 7)
                }
            }
        }

        item {
            Text(
                "Dish Popularity Signals",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PopularityChip(label = "Best sellers", count = 3, color = Color(0xFF2D6A4F))
                PopularityChip(label = "Trending", count = 2, color = Color(0xFF40916C))
                PopularityChip(label = "Underperforming", count = 1, color = Color(0xFFE07A5F))
            }
        }

        // Phase 87: Restaurant loyalty visibility (Android)
        item {
            LoyaltyInfoCard()
        }

        item {
            Text(
                "Suggestions",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )
        }

        item {
            SuggestionCard(
                icon = Icons.Default.Star,
                title = "Promote this dish",
                body = "Beef Cheese Burger is trending. Consider a spotlight deal."
            )
        }
        item {
            SuggestionCard(
                icon = Icons.Default.Timer,
                title = "Improve prep time",
                body = "Pizza category avg 22 min. Batch prep dough to save time."
            )
        }
        item {
            SuggestionCard(
                icon = Icons.Default.Restaurant,
                title = "Add combo meals",
                body = "Combo deals drive 30% more orders. Try burger + drink."
            )
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
                Icon(icon, null, tint = Color(0xFF2D6A4F), modifier = Modifier.size(18.dp))
                Text(title, fontSize = 12.sp, color = Color(0xFF6B6B6B))
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            Text(subtitle, fontSize = 11.sp, color = Color(0xFF8E8E8E))
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
                color = Color(0xFF2D6A4F).copy(alpha = 0.15f)
            ) {
                Text(
                    "#$rank",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2D6A4F)
                )
            }
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A1A1A))
        }
        Text("$orders orders", fontSize = 13.sp, color = Color(0xFF6B6B6B))
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
            Text(label, fontSize = 12.sp, color = Color(0xFF6B6B6B))
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
        color = Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Default.CardGiftcard, null, tint = Color(0xFF2D6A4F), modifier = Modifier.size(24.dp))
            Column {
                Text("Cibus Loyalty", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                Text(
                    "Your restaurant participates in Cibus Loyalty. Repeat customers earn points per order and get rewards like free delivery and discounts. Favourite restaurants earn 25% bonus points.",
                    fontSize = 12.sp, color = Color(0xFF6B6B6B)
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
        color = Color(0xFFF0F7F4)
    ) {
    Row(
        modifier = Modifier.padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, null, tint = Color(0xFF2D6A4F), modifier = Modifier.size(24.dp))
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
            Text(body, fontSize = 12.sp, color = Color(0xFF6B6B6B))
        }
    }
    }
}
