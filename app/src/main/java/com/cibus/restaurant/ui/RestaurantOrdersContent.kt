package com.cibus.restaurant.ui
import com.cibus.restaurant.ui.theme.*

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import android.media.AudioManager
import android.media.ToneGenerator
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

private val REJECT_REASONS = listOf("Too busy", "Out of ingredients", "Closing", "Other")

@Composable
fun RestaurantOrdersContent() {
    var restaurantId by remember { mutableStateOf<String?>(null) }
    var orders by remember { mutableStateOf<List<RestaurantOrderDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var actionLoadingId by remember { mutableStateOf<String?>(null) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var pendingRejectOrderId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val prevNewOrderIds = remember { mutableStateOf<Set<String>>(emptySet()) }
    val view = androidx.compose.ui.platform.LocalView.current

    // New order alert: vibration + sound when a new order appears (Uber Eats-style)
    LaunchedEffect(orders) {
        val newIds = orders.filter { it.status == "order_placed" }.map { it.id }.toSet()
        if (newIds.isNotEmpty() && newIds != prevNewOrderIds.value) {
            val added = newIds - prevNewOrderIds.value
            if (added.isNotEmpty()) {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                val ctx = view.context
                val soundOn = ctx.getSharedPreferences("restaurant_prefs", android.content.Context.MODE_PRIVATE)
                    .getBoolean("soundOnNewOrder", true)
                if (soundOn) {
                    try {
                        val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
                        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 250)
                        kotlinx.coroutines.delay(260)
                        toneGen.release()
                    } catch (_: Exception) {}
                }
            }
            prevNewOrderIds.value = newIds
        } else if (newIds.isEmpty()) prevNewOrderIds.value = emptySet()
    }

    fun refresh(id: String) {
        scope.launch {
            try {
                val r = RetrofitClient.restaurantApi.getOrders(id)
                if (r.isSuccessful) orders = r.body() ?: emptyList()
            } catch (_: Exception) {}
        }
    }
    val onActionError: (String) -> Unit = { actionError = it; scope.launch { kotlinx.coroutines.delay(4000); actionError = null } }

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

    // Group orders by workflow stage — queue-first, operator-friendly
    val newOrders       = orders.filter { it.status == "order_placed" }
    val preparingOrds   = orders.filter { it.status == "accepted" || it.status == "preparing" }
    val readyForPickup  = orders.filter { it.status in listOf("ready_for_pickup", "dispatch_pending", "rider_assigned", "rider_en_route", "rider_arrived") }
    val outForDelivery  = orders.filter { it.status in listOf("picked_up", "on_the_way", "arriving_soon") }
    val completedOrds   = orders.filter { it.status == "delivered" }

    Box(modifier = Modifier.fillMaxSize().background(CibusSurfaceNeutral)) {
        when {
            loading && orders.isEmpty() -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RestaurantSkeletonCard(modifier = Modifier.weight(1f), height = 32.dp)
                            RestaurantSkeletonCard(modifier = Modifier.weight(1f), height = 32.dp)
                        }
                    }
                    items(3) {
                        RestaurantSkeletonCard(height = 140.dp)
                    }
                }
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
                            if (readyForPickup.isNotEmpty()) StatPill("${readyForPickup.size}", "Ready", CibusAmber)
                            if (outForDelivery.isNotEmpty()) StatPill("${outForDelivery.size}", "En Route", Color(0xFF2563EB))
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
                                isActionLoading = actionLoadingId == order.id,
                                onAccept = { scope.launch { actionLoadingId = order.id; try { runOrderAction(order.id, "accept", restaurantId, ::refresh, onError = onActionError) } finally { actionLoadingId = null } } },
                                onReject = { pendingRejectOrderId = order.id; showRejectDialog = true },
                                onDelay = { mins -> scope.launch { actionLoadingId = order.id; try { runDelayOrder(order.id, mins, restaurantId, ::refresh) } finally { actionLoadingId = null } } },
                                onRunningLate = {},
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
                                isActionLoading = actionLoadingId == order.id,
                                onAccept = {},
                                onReject = {},
                                onDelay = {},
                                onRunningLate = { scope.launch { actionLoadingId = order.id; try { runOrderAction(order.id, "running_late", restaurantId, ::refresh, onError = onActionError) } finally { actionLoadingId = null } } },
                                onStartPreparing = { scope.launch { actionLoadingId = order.id; try { runOrderAction(order.id, "preparing", restaurantId, ::refresh, onError = onActionError) } finally { actionLoadingId = null } } },
                                onMarkReady = { scope.launch { actionLoadingId = order.id; try { runOrderAction(order.id, "ready_for_pickup", restaurantId, ::refresh, onError = onActionError) } finally { actionLoadingId = null } } }
                            )
                        }
                    }

                    // Section divider between Preparing and Ready for Pickup
                    if (preparingOrds.isNotEmpty() && readyForPickup.isNotEmpty()) {
                        item { RestaurantSectionBreak() }
                    }

                    // ── READY FOR PICKUP ──────────────────────────────────
                    if (readyForPickup.isNotEmpty()) {
                        item { SectionHeader("Ready for Pickup", Icons.Default.CheckCircle, CibusAmber, readyForPickup.size) }
                        items(readyForPickup, key = { it.id }) { order ->
                            OrderCard(
                                order = order,
                                isActionLoading = false,
                                onAccept = {},
                                onReject = {},
                                onDelay = {},
                                onRunningLate = {},
                                onStartPreparing = {},
                                onMarkReady = {}
                            )
                        }
                    }

                    // ── OUT FOR DELIVERY ───────────────────────────────────
                    if (outForDelivery.isNotEmpty()) {
                        item { SectionHeader("Out for Delivery", Icons.Default.LocalShipping, Color(0xFF2563EB), outForDelivery.size) }
                        items(outForDelivery, key = { it.id }) { order ->
                            OrderCard(
                                order = order,
                                isActionLoading = false,
                                onAccept = {},
                                onReject = {},
                                onDelay = {},
                                onRunningLate = {},
                                onStartPreparing = {},
                                onMarkReady = {}
                            )
                        }
                    }

                    // ── COMPLETED ─────────────────────────────────────────
                    if (completedOrds.isNotEmpty()) {
                        item { SectionHeader("Completed", Icons.Default.Done, CibusTextSecondary, completedOrds.size) }
                        items(completedOrds, key = { it.id }) { order ->
                            OrderCard(order = order, isActionLoading = false, onAccept = {}, onReject = {}, onDelay = {}, onRunningLate = {}, onStartPreparing = {}, onMarkReady = {})
                        }
                    }

                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }

        // Action error banner
        actionError?.let { msg ->
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = CibusRed.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(msg, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { actionError = null }) { Text("Dismiss", color = Color.White) }
                }
            }
        }

        // Reject reason dialog (Uber Eats-style)
        if (showRejectDialog) {
            AlertDialog(
                onDismissRequest = { showRejectDialog = false; pendingRejectOrderId = null },
                title = { Text("Reject order?") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Select a reason for rejecting this order.", style = MaterialTheme.typography.bodyMedium)
                        REJECT_REASONS.forEach { reason ->
                            TextButton(
                                onClick = {
                                    val oid = pendingRejectOrderId
                                    showRejectDialog = false
                                    pendingRejectOrderId = null
                                    if (oid != null) {
                                        scope.launch { actionLoadingId = oid; try { runOrderAction(oid, "reject", restaurantId, ::refresh, rejectReason = reason, onError = onActionError) } finally { actionLoadingId = null } }
                                    }
                                }
                            ) { Text(reason) }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRejectDialog = false; pendingRejectOrderId = null }) { Text("Cancel") }
                }
            )
        }
    }
}

private suspend fun runOrderAction(
    orderId: String,
    action: String,
    restaurantId: String?,
    refresh: (String) -> Unit,
    rejectReason: String? = null,
    onError: ((String) -> Unit)? = null
) {
    try {
        val api = RetrofitClient.restaurantApi
        when (action) {
            "accept"           -> api.acceptOrder(orderId)
            "reject"           -> api.rejectOrder(orderId, if (rejectReason != null) mapOf("reason" to rejectReason) else emptyMap())
            "preparing"        -> api.patchOrderStatus(orderId, mapOf("status" to "preparing"))
            "ready_for_pickup" -> api.patchOrderStatus(orderId, mapOf("status" to "ready_for_pickup") as Map<String, Any>)
            "running_late"     -> runDelayOrder(orderId, 15, restaurantId, refresh)
        }
        restaurantId?.let { refresh(it) }
    } catch (e: Exception) {
        onError?.invoke(e.message ?: "Action failed")
    }
}

private suspend fun runDelayOrder(orderId: String, delayMinutes: Int, restaurantId: String?, refresh: (String) -> Unit) {
    try {
        val estimatedReadyAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(java.util.Date(System.currentTimeMillis() + delayMinutes * 60_000L))
        RetrofitClient.restaurantApi.delayOrder(orderId, mapOf(
            "delayMinutes" to delayMinutes,
            "estimatedReadyAt" to estimatedReadyAt
        ))
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
    isActionLoading: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onDelay: (Int) -> Unit,
    onRunningLate: () -> Unit,
    onStartPreparing: () -> Unit,
    onMarkReady: () -> Unit,
) {
    val status = order.status ?: ""
    val fm = (order.fulfillmentMode ?: "delivery").lowercase()
    val isNew        = status == "order_placed"
    val isAccepted   = status == "accepted"
    val isPreparing  = status == "preparing"
    val isReady      = status in listOf("ready_for_pickup", "dispatch_pending", "rider_assigned", "rider_en_route", "rider_arrived")
    val isRiderAssigned = status == "rider_assigned"
    val isRiderEnRoute  = status == "rider_en_route"
    val isRiderArrived  = status == "rider_arrived" || order.riderArrivedAt != null

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
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNew) CibusRed.copy(alpha = 0.03f) else Color.White
        ),
        border = if (isNew) androidx.compose.foundation.BorderStroke(1.5.dp, CibusRed.copy(alpha = 0.25f))
                 else if (isReady || isRiderAssigned || isRiderEnRoute || isRiderArrived) androidx.compose.foundation.BorderStroke(1.5.dp, CibusAmber.copy(alpha = 0.4f))
                 else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNew) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // ── Customer notes (Uber Eats-style: prominent when present) ────
            if (!order.specialInstructions.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CibusAmber.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CibusAmber.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Notes, null, tint = CibusAmber, modifier = Modifier.size(18.dp).padding(top = 1.dp))
                        Column {
                            Text("Customer note", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                            Text(
                                order.specialInstructions,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF78350F),
                                maxLines = 3
                            )
                        }
                    }
                }
            }
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
                        val fmLabel = when (fm) {
                            "pickup" -> "Pickup"
                            "dine_in", "dine-in" -> "Dine-in"
                            else -> "Delivery"
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = CibusGreenDark.copy(alpha = 0.12f)) {
                            Text(fmLabel, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = CibusGreenDark)
                        }
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
                    // Call customer (Uber Eats-style)
                    val customerPhone = order.address?.get("phone") as? String
                    if (!customerPhone.isNullOrBlank()) {
                        val ctx = androidx.compose.ui.platform.LocalContext.current
                        TextButton(
                            onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                    data = android.net.Uri.parse("tel:${customerPhone.replace(" ", "").replace("-", "")}")
                                }
                                ctx.startActivity(intent)
                            },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Call, null, tint = CibusGreenDark, modifier = Modifier.size(12.dp))
                                Text("Call customer", style = MaterialTheme.typography.labelSmall, color = CibusGreenDark)
                            }
                        }
                    }
                    // Order age (Uber-style prominence)
                    order.createdAt?.let { ts ->
                        val createdMs = listOf(
                            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") },
                            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                        ).firstNotNullOfOrNull { fmt -> runCatching { fmt.parse(ts)?.time }.getOrNull() }
                        val ageMins = createdMs?.let { (System.currentTimeMillis() - it) / 60_000 }
                        if (ageMins != null) {
                            val ageColor = when {
                                ageMins >= 10 -> CibusRed
                                ageMins >= 5 -> CibusAmber
                                else -> CibusTextSecondary
                            }
                            Text(
                                "${ageMins} min ago",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = ageColor
                            )
                        } else {
                            Text(ts.take(16).replace("T", " "), style = MaterialTheme.typography.labelSmall, color = CibusTextSecondary.copy(alpha = 0.6f))
                        }
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

            // ── Rider status (Uber Eats-style: ETA + tap-to-call) ───────────
            val isDelivery = (order.fulfillmentMode ?: "delivery").lowercase() == "delivery"
            if (isDelivery && (isReady || isRiderAssigned || isRiderEnRoute || isRiderArrived)) {
                val riderBg = if (isRiderArrived) CibusGreenDark.copy(alpha = 0.15f) else CibusGreenDark.copy(alpha = 0.07f)
                val ctx = androidx.compose.ui.platform.LocalContext.current
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = riderBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CibusGreenDark.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(CibusGreenDark.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DirectionsBike, null, tint = CibusGreenDark, modifier = Modifier.size(18.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            val riderName = order.riderName
                            val etaMins = when {
                                isRiderArrived -> 0
                                isRiderEnRoute -> 8
                                isRiderAssigned -> 12
                                else -> null
                            }
                            if (!riderName.isNullOrEmpty()) {
                                Text(
                                    riderName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CibusGreenDark
                                )
                                Text(
                                    when {
                                        isRiderArrived -> "Arrived at restaurant"
                                        isRiderEnRoute -> "En route — Arriving in ~${etaMins ?: 8} min"
                                        isRiderAssigned -> "Assigned — ETA ~${etaMins ?: 12} min"
                                        else -> "Awaiting rider"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CibusTextSecondary
                                )
                            } else {
                                Text("Awaiting rider assignment…", style = MaterialTheme.typography.labelSmall, color = CibusTextSecondary)
                            }
                        }
                        if (isRiderArrived) {
                            val waitingMins = order.riderArrivedAt?.let { ts ->
                                listOf(
                                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
                                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
                                ).firstNotNullOfOrNull { fmt -> runCatching { fmt.parse(ts)?.time }.getOrNull() }
                                    ?.let { maxOf(0, (System.currentTimeMillis() - it) / 60_000).toInt() }
                            }
                            if (waitingMins != null) {
                                Surface(shape = RoundedCornerShape(6.dp), color = CibusAmber.copy(alpha = 0.3f)) {
                                    Text("Waiting $waitingMins min", modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                }
                            } else {
                                Surface(shape = RoundedCornerShape(6.dp), color = CibusGreenDark) {
                                    Text("ARRIVED", modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                        order.riderPhone?.takeIf { it.isNotBlank() }?.let { phone ->
                            IconButton(
                                onClick = {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                        data = android.net.Uri.parse("tel:${phone.replace(" ", "").replace("-", "")}")
                                    }
                                    ctx.startActivity(intent)
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.Call, null, tint = CibusGreenDark, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            } else if (!isDelivery && (isReady || isRiderAssigned || isRiderEnRoute || isRiderArrived)) {
                // Pickup / Dine-in: show ready-for-customer message (no rider needed)
                Surface(shape = RoundedCornerShape(7.dp), color = CibusAmber.copy(alpha = 0.12f)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Person, null, tint = CibusAmber, modifier = Modifier.size(14.dp))
                        Text(
                            if (fm == "dine_in") "Ready for dine-in — customer will collect at table" else "Ready for pickup — customer will collect",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = CibusAmber
                        )
                    }
                }
            }

            // ── Action buttons ─────────────────────────────────────────────
            if (isActionLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                    color = CibusGreenDark,
                    strokeWidth = 2.dp
                )
            } else when {
                isNew -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(10, 15, 30).forEach { mins ->
                                OutlinedButton(
                                    onClick = { onDelay(mins) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CibusAmber),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                ) { Text("+${mins}m", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onReject() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CibusRed),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, CibusRed.copy(alpha = 0.4f))
                            ) { Text("Reject", fontWeight = FontWeight.SemiBold) }
                            Button(
                                onClick = { onAccept() },
                                modifier = Modifier.weight(1.6f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CibusGreenDark)
                            ) { Text("Accept Order", fontWeight = FontWeight.SemiBold) }
                        }
                    }
                }
                isAccepted -> {
                    Button(
                        onClick = { onStartPreparing() },
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onRunningLate() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CibusAmber),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, CibusAmber.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Running late", fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = { onMarkReady() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF40916C))
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Mark Ready", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
