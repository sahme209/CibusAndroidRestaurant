package com.cibus.restaurant.ui
import com.cibus.restaurant.ui.theme.*

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cibus.restaurant.api.RestaurantOrderDto
import com.cibus.restaurant.api.RetrofitClient
import kotlinx.coroutines.launch

/**
 * Phase 105 — Premium empty state for restaurant screens.
 */
@Composable
private fun RestaurantEmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF8A8A8A)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B6B6B),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RestaurantOrdersContent() {
    var restaurantId by remember { mutableStateOf<String?>(null) }
    var orders by remember { mutableStateOf<List<RestaurantOrderDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun refresh(id: String) {
        scope.launch {
            try {
                val ord = RetrofitClient.restaurantApi.getOrders(id)
                if (ord.isSuccessful) orders = ord.body() ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        try {
            val me = RetrofitClient.restaurantApi.getMe()
            if (!me.isSuccessful) {
                error = "Could not load profile"
                loading = false
                return@LaunchedEffect
            }
            val id = me.body()?.restaurantId
            if (id.isNullOrBlank()) {
                error = "No restaurant linked"
                loading = false
                return@LaunchedEffect
            }
            restaurantId = id
            val ord = RetrofitClient.restaurantApi.getOrders(id)
            if (ord.isSuccessful) {
                orders = ord.body() ?: emptyList()
            } else {
                error = "Could not load orders"
            }
        } catch (e: Exception) {
            error = e.message ?: "Error loading orders"
        }
        loading = false
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            loading && orders.isEmpty() -> CircularProgressIndicator(
                modifier = Modifier.padding(24.dp),
                color = Color(0xFF2E7D32)
            )
            error != null -> RestaurantEmptyState(
                icon = Icons.Default.Assignment,
                title = "Something went wrong",
                message = error ?: "Could not load orders."
            )
            orders.isEmpty() -> RestaurantEmptyState(
                icon = Icons.Default.Assignment,
                title = "No orders yet",
                message = "Incoming orders will appear here when customers place them."
            )
            else -> {
                // Phase 116D: Kitchen queue analysis
                val preparing = orders.count { it.status == "preparing" }
                val readyForPickup = orders.count { it.status == "ready_for_pickup" }
                val newOrders = orders.count { it.status == "order_placed" }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Phase 116D: Kitchen pressure header
                    if (preparing > 0 || readyForPickup > 0) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = if (readyForPickup > 0) Color(0xFFF59E0B).copy(alpha = 0.1f) else CibusGreenDark.copy(alpha = 0.08f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (newOrders > 0) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("$newOrders", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text("new", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B6B6B))
                                    }
                                    if (preparing > 0) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("$preparing", fontWeight = FontWeight.Bold, color = CibusGreenDark)
                                        Text("preparing", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B6B6B))
                                    }
                                    if (readyForPickup > 0) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("$readyForPickup", fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                        Text("ready / waiting rider", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B6B6B))
                                    }
                                }
                            }
                        }
                    }
                    items(orders) { order ->
                        val rid = restaurantId
                        OrderCard(
                            order = order,
                            onAccept = {
                                scope.launch {
                                    try {
                                        RetrofitClient.restaurantApi.acceptOrder(order.id)
                                        if (rid != null) refresh(rid)
                                    } catch (_: Exception) {}
                                }
                            },
                            onReject = {
                                scope.launch {
                                    try {
                                        RetrofitClient.restaurantApi.rejectOrder(order.id)
                                        if (rid != null) refresh(rid)
                                    } catch (_: Exception) {}
                                }
                            },
                            onStartPreparing = {
                                scope.launch {
                                    try {
                                        RetrofitClient.restaurantApi.patchOrderStatus(order.id, mapOf("status" to "preparing") as Map<String, Any>)
                                        if (rid != null) refresh(rid)
                                    } catch (_: Exception) {}
                                }
                            },
                            onMarkReady = {
                                scope.launch {
                                    try {
                                        RetrofitClient.restaurantApi.patchOrderStatus(order.id, mapOf("status" to "ready_for_pickup") as Map<String, Any>)
                                        if (rid != null) refresh(rid)
                                    } catch (_: Exception) {}
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: RestaurantOrderDto,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onStartPreparing: () -> Unit = {},
    onMarkReady: () -> Unit = {},
) {
    val status = order.status ?: ""
    val canAct = status == "order_placed"
    val isAccepted = status == "accepted"
    val isPreparing = status == "preparing"
    val isUrgent = status in listOf("ready_for_pickup", "dispatch_pending")
    val isActive = status in listOf("order_placed", "accepted", "preparing")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isUrgent) Color(0xFFFFF3E0) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUrgent) 4.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Order #${order.id.take(8)}",
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (isUrgent) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.15f)
                        ) {
                            Text(
                                "PICKUP READY",
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB45309),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        "Rs ${order.total?.toInt() ?: 0}",
                        fontWeight = FontWeight.SemiBold,
                        color = CibusGreenDark
                    )
                }
            }

            Text(
                "Status: ${status.replace("_", " ").replaceFirstChar { it.uppercase() }}",
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = if (isUrgent) Color(0xFFB45309) else if (isActive) MaterialTheme.colorScheme.primary else Color(0xFF6B6B6B)
            )

            val addr = order.address
            if (addr != null) {
                val area = (addr["area"] as? String) ?: (addr["city"] as? String) ?: ""
                if (area.isNotEmpty()) Text(
                    area,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B6B6B),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // ── Status-driven action buttons ───────────────────────────────
            when {
                canAct -> {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onAccept) { Text("Accept") }
                        Button(
                            onClick = onReject,
                            colors = ButtonDefaults.buttonColors(containerColor = CibusRed)
                        ) { Text("Reject") }
                    }
                }
                isAccepted -> {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onStartPreparing,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CibusGreenDark)
                    ) { Text("Start preparing") }
                }
                isPreparing -> {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onMarkReady,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF40916C))
                    ) { Text("Mark ready for pickup") }
                }
            }
        }
    }
}
