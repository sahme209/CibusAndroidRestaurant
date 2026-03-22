package com.cibus.restaurant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun RestaurantPayoutsContent() {
    var loading by remember { mutableStateOf(true) }
    var walletBalance by remember { mutableStateOf(0.0) }
    var last30Revenue by remember { mutableStateOf(0.0) }
    var pendingCount by remember { mutableStateOf(0) }
    var completedCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val resp = RetrofitClient.restaurantApi.getRestaurantWallet()
                if (resp.isSuccessful) {
                    val body = resp.body()
                    walletBalance = body?.walletBalance ?: 0.0
                    last30Revenue = body?.last30Revenue ?: 0.0
                    pendingCount = body?.pendingPayoutsCount ?: 0
                    completedCount = body?.completedPayoutsCount ?: 0
                }
            } catch (_: Exception) {}
        }
        loading = false
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Payouts", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
        }

        if (loading) {
            item {
                Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2D6A4F))
                }
            }
        } else {
            item {
                // Wallet balance card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF1B4D2E), Color(0xFF2D6A4F))))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Payout Wallet", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            Text("Rs ${walletBalance.toInt()}", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text("Available for payout", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                        Text("🏦", fontSize = 36.sp)
                    }
                }
            }

            item {
                // KPI row
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PayoutKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "30-Day Net Earnings",
                        value = "Rs ${last30Revenue.toInt()}",
                        color = Color(0xFF2D6A4F)
                    )
                    PayoutKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Pending",
                        value = "$pendingCount payouts",
                        color = Color(0xFFD97706)
                    )
                    PayoutKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Completed",
                        value = "$completedCount",
                        color = Color(0xFF2563EB)
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("How Payouts Work", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A1A))
                        listOf(
                            "Net earnings (after platform commission) credited after each delivery",
                            "Ops team approves payout requests weekly",
                            "Funds transferred via bank transfer or mobile wallet (JazzCash/Easypaisa)",
                            "Transaction history and payout receipts available in Ops Portal"
                        ).forEachIndexed { i, text ->
                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("${i + 1}.", fontSize = 12.sp, color = Color(0xFF2D6A4F), fontWeight = FontWeight.Bold)
                                Text(text, fontSize = 12.sp, color = Color(0xFF4B5563))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PayoutKpiCard(modifier: Modifier = Modifier, title: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            Text(title, fontSize = 10.sp, color = Color(0xFF6B7280))
        }
    }
}
