package com.cibus.restaurant.ui
import com.cibus.restaurant.ui.theme.*

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.api.RestaurantOrderDto
import com.cibus.restaurant.api.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ── Prep timer composable ──────────────────────────────────────────────────────

@Composable
private fun PrepTimerDisplay(preparingAtMs: Long, warnAfterMs: Long = 15 * 60 * 1000L) {
    var elapsedMs by remember { mutableStateOf(System.currentTimeMillis() - preparingAtMs) }
    LaunchedEffect(preparingAtMs) {
        while (true) {
            delay(1000L)
            elapsedMs = System.currentTimeMillis() - preparingAtMs
        }
    }
    val isOvertime = elapsedMs > warnAfterMs
    val minutes = (elapsedMs / 1000L / 60L).toInt()
    val seconds = (elapsedMs / 1000L % 60L).toInt()
    val fgColor = if (isOvertime) CibusRed else CibusAmber
    val bgColor = if (isOvertime) CibusRed.copy(alpha = 0.08f) else CibusAmber.copy(alpha = 0.12f)

    Surface(shape = RoundedCornerShape(6.dp), color = bgColor) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                if (isOvertime) Icons.Default.Warning else Icons.Default.Timer,
                contentDescription = null,
                tint = fgColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                "%d:%02d".format(minutes, seconds),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = fgColor
            )
            if (isOvertime) {
                Text("OVERTIME", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = fgColor)
            } else {
                Text("prep time", style = MaterialTheme.typography.labelSmall, color = CibusTextSecondary)
            }
        }
    }
}

// ── Main orders composable ─────────────────────────────────────────────────────

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
                val r = RetrofitClient.restaurantApi.getOrders(id)
                if (r.isSuccessful) orders = r.body() ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    // Auto-poll every 15 s
    LaunchedEffect(restaurantId) {
        val id = restaurantId ?: return@LaunchedEffect
        while (true) {
            delay(15_000L)
            try {
                val r = RetrofitClient.restaurantApi.getOrders(id)
                if (r.isSuccessful) orders = r.body() ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        try {
            val me = RetrofitClient.restaurantApi.getMe()
            if (!me.isSuccessful) { error = "Could not load profile"; loading = false; return@LaunchedEffect }
            val id = me.body()?.restaurantId
            if (id.isNullOrBlank()) { error = "No restaurant linked"; loading = false; return@LaunchedEffect }
            restaurantId = id
            val ord = RetrofitClient.restaurantApi.getOrders(id)
            if (ord.isSuccessful) orders = ord.body() ?: emptyList()
            else error = "Could not load orders"
        } catch (e: Exception) { error = e.message ?: "Error loading orders" }
        loading = false
    }

    // Group orders by workflow stage
    val newOrders     = orders.filter { it.status == "order_placed" }
    val preparingOrds = orders.filter { it.status == "accepted" || it.status == "preparing" }
    val readyOrds     = orders.filter { it.status in listOf("ready_for_pickup", "dispatch_pending", "rider_assigned", "rider_en_route") }
    val completedOrds = orders.filter { it.status in listOf("delivered", "picked_up", "on_the_way", "arriving_soon") }

    Box(modifier = Modifier.fillMaxSize().background(CibusSurfaceNeutral)) {
        when {
            loading && orders.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = CibusGreenDark)
            }
            error != null && orders.isEmpty() -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, null, tint = CibusRed, modifier = Modifier.size(40.dp))
                    Text(error ?: "Error", fontWeight = FontWeight.SemiBold, color = CibusHeaderCard)
                }
            }
            orders.isEmpty() -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Assignment, null, tint = Color(0xFF8A8A8A), modifier = Modifier.size(48.dp))
                    Text("No active orders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = CibusHeaderCard)
                    Text("Incoming orders will appear here when customers place them.", style = MaterialTheme.typography.bodySmall, color = CibusTextSecondary, textAlign = TextAlign.Center)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // ── Kitchen pressure bar ────────────────────────────────
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (newOrders.isNotEmpty()) StatPill("${newOrders.size}", "New", CibusRed)
                            if (preparingOrds.isNotEmpty()) StatPill("${preparingOrds.size}", "Preparing", CibusGreenDark)
                            if (readyOrds.isNotEmpty()) StatPill("${readyOrds.size}", "Ready", CibusAmber)
                            Spacer(Modifier.weight(1f))
                            if (loading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = CibusGreenDark)
                        }
                    }

                    // ── NEW ORDERS ─────────────────────────────────────────
                    if (newOrders.isNotEmpty()) {
                        item { SectionHeader("New Orders", Icons.Default.NotificationsActive, CibusRed, newOrders.size) }
                        items(newOrders, key = { it.id }) { order ->
                            OrderCard(
                                order = order,
                                onAccept = { scope.launch { runOrderAction(order.id, "accept", restaurantId, ::refresh) } },
                                onReject = { scope.launch { runOrderAction(order.id, "reject", restaurantId, ::refresh) } },
                                onStartPreparing = {},
                                onMarkReady = {}
                            )
                        }
                    }

                    // ── PREPARING ─────────────────────────────────────────
                    if (preparingOrds.isNotEmpty()) {
                        item { SectionHeader("Preparing", Icons.Default.Whatshot, CibusGreenDark, preparingOrds.size) }
                        items(preparingOrds, key = { it.id }) { order ->
                            OrderCard(
                                order = order,
                                onAccept = {},
                                onReject = {},
                                onStartPreparing = { scope.launch { runOrderAction(order.id, "preparing", restaurantId, ::refresh) } },
                                onMarkReady = { scope.launch { runOrderAction(order.id, "ready_for_pickup", restaurantId, ::refresh) } }
                            )
                        }
                    }

                    // ── READY FOR PICKUP ──────────────────────────────────
                    if (readyOrds.isNotEmpty()) {
                        item { SectionHeader("Ready for Pickup", Icons.Default.CheckCircle, CibusAmber, readyOrds.size) }
                        items(readyOrds, key = { it.id }) { order ->
                            OrderCard(
                                order = order,
                                onAccept = {},
                                onReject = {},
                                onStartPreparing = {},
                                onMarkReady = {}
                            )
                        }
                    }

                    // ── COMPLETED ─────────────────────────────────────────
                    if (completedOrds.isNotEmpty()) {
                        item { SectionHeader("Completed", Icons.Default.Done, CibusTextSecondary, completedOrds.size) }
                        items(completedOrds, key = { it.id }) { order ->
                            OrderCard(order = order, onAccept = {}, onReject = {}, onStartPreparing = {}, onMarkReady = {})
                        }
                    }

                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}

private suspend fun runOrderAction(
    orderId: String,
    action: String,
    restaurantId: String?,
    refresh: (String) -> Unit
) {
    try {
        val api = RetrofitClient.restaurantApi
        when (action) {
            "accept"           -> api.acceptOrder(orderId)
            "reject"           -> api.rejectOrder(orderId)
            "preparing"        -> api.patchOrderStatus(orderId, mapOf("status" to "preparing"))
            "ready_for_pickup" -> api.patchOrderStatus(orderId, mapOf("status" to "ready_for_pickup") as Map<String, Any>)
        }
        restaurantId?.let { refresh(it) }
    } catch (_: Exception) {}
}

@Composable
private fun StatPill(value: String, label: String, color: Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
            Text(label, style = MaterialTheme.typography.labelSmall, color = CibusTextSecondary)
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(15.dp))
        Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = CibusTextSecondary)
        Text("($count)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun OrderCard(
    order: RestaurantOrderDto,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onStartPreparing: () -> Unit,
    onMarkReady: () -> Unit,
) {
    val status = order.status ?: ""
    val isNew        = status == "order_placed"
    val isAccepted   = status == "accepted"
    val isPreparing  = status == "preparing"
    val isReady      = status in listOf("ready_for_pickup", "dispatch_pending")
    val isRiderAssigned = status == "rider_assigned"
    val isRiderEnRoute  = status == "rider_en_route"
    val isRiderArrived  = status == "rider_arrived" || order.riderArrivedAt != null

    var actionLoading by remember(order.id) { mutableStateOf(false) }

    // Parse preparingAt to epoch ms for timer
    val preparingAtMs: Long? = remember(order.preparingAt) {
        order.preparingAt?.let { ts ->
            listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
            ).firstNotNullOfOrNull { fmt -> runCatching { fmt.parse(ts)?.time }.getOrNull() }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNew) CibusRed.copy(alpha = 0.03f) else Color.White
        ),
        border = if (isNew) androidx.compose.foundation.BorderStroke(1.5.dp, CibusRed.copy(alpha = 0.25f))
                 else if (isReady || isRiderAssigned || isRiderEnRoute || isRiderArrived) androidx.compose.foundation.BorderStroke(1.5.dp, CibusAmber.copy(alpha = 0.4f))
                 else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNew) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // ── Header ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Order #${order.id.takeLast(6)}",
                            fontWeight = FontWeight.SemiBold,
                            color = CibusHeaderCard
                        )
                        if (order.itemCount > 0) {
                            Text(
                                "${order.itemCount} item${if (order.itemCount != 1) "s" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CibusTextSecondary
                            )
                        }
                        if (isNew) {
                            Surface(shape = RoundedCornerShape(4.dp), color = CibusRed) {
                                Text("NEW", modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        if (order.paymentMethod?.lowercase() in listOf("cod", "cash")) {
                            Surface(shape = RoundedCornerShape(4.dp), color = CibusAmber.copy(alpha = 0.15f)) {
                                Text("COD", modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            }
                        }
                    }
                    // Area
                    val area = (order.address?.get("area") as? String) ?: (order.address?.get("city") as? String) ?: ""
                    if (area.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = CibusTextSecondary, modifier = Modifier.size(11.dp))
                            Text(area, style = MaterialTheme.typography.bodySmall, color = CibusTextSecondary)
                        }
                    }
                    // Time
                    order.createdAt?.take(16)?.replace("T", " ")?.let { t ->
                        Text(t, style = MaterialTheme.typography.labelSmall, color = CibusTextSecondary.copy(alpha = 0.6f))
                    }
                }
                Text(
                    "Rs ${order.total?.toInt() ?: 0}",
                    fontWeight = FontWeight.Bold,
                    color = CibusGreenDark,
                    fontSize = 15.sp
                )
            }

            // ── Items summary ──────────────────────────────────────────────
            val itemsSummary = order.items?.take(4)?.joinToString(" · ") { item ->
                val qty = (item["quantity"] as? Double)?.toInt() ?: (item["quantity"] as? Int) ?: 1
                val name = (item["name"] as? String) ?: "Item"
                "${qty}× $name"
            }
            if (!itemsSummary.isNullOrEmpty()) {
                Text(itemsSummary, style = MaterialTheme.typography.bodySmall, color = CibusTextSecondary, maxLines = 2)
            }

            // ── Special instructions ───────────────────────────────────────
            if (!order.specialInstructions.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(7.dp),
                    color = CibusAmber.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Default.Info, null, tint = CibusAmber, modifier = Modifier.size(12.dp).padding(top = 1.dp))
                        Text(
                            order.specialInstructions,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF92400E),
                            maxLines = 2
                        )
                    }
                }
            }

            // ── Prep timer ─────────────────────────────────────────────────
            if (isPreparing && preparingAtMs != null) {
                val warnMs = if (order.prepTimeMinutes != null) order.prepTimeMinutes * 60_000L else 15 * 60_000L
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PrepTimerDisplay(preparingAtMs = preparingAtMs, warnAfterMs = warnMs)
                    if (order.prepTimeMinutes != null) {
                        Text("/ ${order.prepTimeMinutes} min expected", style = MaterialTheme.typography.labelSmall, color = CibusTextSecondary)
                    }
                }
            }

            // ── Rider status ──────────────────────────────────────────────
            if (isReady || isRiderAssigned || isRiderEnRoute || isRiderArrived) {
                val riderBg = if (isRiderArrived) CibusGreenDark.copy(alpha = 0.15f) else CibusGreenDark.copy(alpha = 0.07f)
                Surface(
                    shape = RoundedCornerShape(7.dp),
                    color = riderBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.DirectionsBike,
                            null,
                            tint = CibusGreenDark,
                            modifier = Modifier.size(14.dp)
                        )
                        val riderName = order.riderName
                        if (!riderName.isNullOrEmpty()) {
                            val riderStateText = when {
                                isRiderArrived -> " • Arrived at restaurant"
                                isRiderEnRoute -> " • En route to you"
                                isRiderAssigned -> " • Assigned"
                                else -> ""
                            }
                            Text(
                                "Rider: $riderName$riderStateText",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = CibusGreenDark
                            )
                        } else {
                            Text("Awaiting rider assignment…", style = MaterialTheme.typography.labelSmall, color = CibusTextSecondary)
                        }
                        if (isRiderArrived) {
                            Spacer(Modifier.weight(1f))
                            Surface(shape = RoundedCornerShape(4.dp), color = CibusGreenDark) {
                                Text("ARRIVED", modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // ── Action buttons ─────────────────────────────────────────────
            if (actionLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                    color = CibusGreenDark,
                    strokeWidth = 2.dp
                )
            } else when {
                isNew -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { actionLoading = true; onReject() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CibusRed),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, CibusRed.copy(alpha = 0.4f))
                        ) { Text("Reject", fontWeight = FontWeight.SemiBold) }
                        Button(
                            onClick = { actionLoading = true; onAccept() },
                            modifier = Modifier.weight(1.6f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CibusGreenDark)
                        ) { Text("Accept Order", fontWeight = FontWeight.SemiBold) }
                    }
                }
                isAccepted -> {
                    Button(
                        onClick = { actionLoading = true; onStartPreparing() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CibusGreenDark)
                    ) {
                        Icon(Icons.Default.Whatshot, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Start Preparing", fontWeight = FontWeight.SemiBold)
                    }
                }
                isPreparing -> {
                    Button(
                        onClick = { actionLoading = true; onMarkReady() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF40916C))
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Mark Ready for Pickup", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
