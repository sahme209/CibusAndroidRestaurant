package com.cibus.restaurant.ui
import com.cibus.restaurant.ui.theme.*

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cibus.restaurant.api.RetrofitClient
import kotlinx.coroutines.launch

private val AVAILABILITY_OPTIONS = listOf(
    "open"         to "Open",
    "busy"         to "Busy",
    "closing_soon" to "Closing Soon",
    "closed"       to "Closed"
)

@Composable
fun RestaurantSettingsContent(onLogout: () -> Unit) {
    var restaurantId by remember { mutableStateOf<String?>(null) }
    var availability by remember { mutableStateOf("open") }
    var loadingAvailability by remember { mutableStateOf(true) }
    var savingAvailability by remember { mutableStateOf(false) }
    var newOrderNotifications by remember { mutableStateOf(true) }
    var showCloseConfirm by remember { mutableStateOf(false) }
    var pendingAvailability by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val isOpen = availability == "open" || availability == "busy" || availability == "closing_soon"

    LaunchedEffect(Unit) {
        try {
            val r = RetrofitClient.restaurantApi.getMe()
            if (r.isSuccessful) restaurantId = r.body()?.restaurantId
        } catch (_: Exception) {}
        loadingAvailability = false
    }

    fun setAvailability(value: String) {
        val rid = restaurantId ?: return
        savingAvailability = true
        scope.launch {
            try {
                RetrofitClient.restaurantApi.patchAvailability(rid, mapOf("availability" to value))
                availability = value
            } catch (_: Exception) {}
            savingAvailability = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // ── Open / Closed hero toggle (Uber Eats–style status card) ───────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isOpen) Color(0xFFECF9EE) else MaterialTheme.colorScheme.surface
            ),
            border = if (isOpen) androidx.compose.foundation.BorderStroke(1.5.dp, CibusGreenDark.copy(alpha = 0.3f)) else null,
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Status icon
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOpen) CibusGreenDark.copy(alpha = 0.15f) else Color(0xFFDDDDDD)
                    ) {
                        if (savingAvailability) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp).padding(12.dp),
                                color = CibusGreenDark,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                            if (isOpen) Icons.Default.Store else Icons.Default.Store,
                                null,
                                modifier = Modifier.size(48.dp).padding(12.dp),
                                tint = if (isOpen) CibusGreenDark else Color(0xFF888888)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            if (isOpen) "Open for Orders" else "Temporarily Closed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isOpen) CibusGreenDark else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            if (savingAvailability) "Updating…"
                            else if (isOpen) "Customers can place orders"
                            else "Tap to reopen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Toggle button
                Button(
                    onClick = {
                        if (isOpen) {
                            pendingAvailability = "closed"
                            showCloseConfirm = true
                        } else {
                            setAvailability("open")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOpen) Color(0xFFDC2626) else CibusGreenDark
                    ),
                    enabled = !savingAvailability && !loadingAvailability && restaurantId != null
                ) {
                    Text(
                        if (isOpen) "Close Restaurant" else "Open Restaurant",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Fine-grained status chips (only when open)
                if (isOpen) {
                    Text(
                        "Status",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("open" to "Open", "busy" to "Busy", "closing_soon" to "Closing Soon").forEach { (v, l) ->
                            val isSelected = availability == v
                            val chipColor = when (v) {
                                "open"         -> CibusGreenDark
                                "busy"         -> CibusAmber
                                "closing_soon" -> CibusOrange
                                else -> CibusGreenDark
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { setAvailability(v) },
                                label = { Text(l, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = chipColor.copy(alpha = 0.15f),
                                    selectedLabelColor = chipColor,
                                )
                            )
                        }
                    }
                }
            }
        }

        // Confirmation dialog for closing
        if (showCloseConfirm) {
            AlertDialog(
                onDismissRequest = { showCloseConfirm = false },
                title = { Text("Close restaurant?", fontWeight = FontWeight.Bold) },
                text = { Text("Customers will not be able to place new orders while your restaurant is closed.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showCloseConfirm = false
                            pendingAvailability?.let { setAvailability(it) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) { Text("Close", fontWeight = FontWeight.SemiBold) }
                },
                dismissButton = {
                    TextButton(onClick = { showCloseConfirm = false }) { Text("Cancel") }
                }
            )
        }

        // ── Kitchen Load Throttle ─────────────────────────────────────────
        KitchenThrottleCard(restaurantId = restaurantId, scope = scope)

        // ── Notifications ─────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Notifications, null, tint = CibusGreenDark, modifier = Modifier.size(20.dp))
                    Text("Notifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("New order alerts", style = MaterialTheme.typography.bodyMedium)
                        Text("Sound & vibration when a new order arrives", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = newOrderNotifications,
                        onCheckedChange = { newOrderNotifications = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CibusGreenDark)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sign Out", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun KitchenThrottleCard(restaurantId: String?, scope: kotlinx.coroutines.CoroutineScope) {
    var orderingPaused by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (orderingPaused) Color(0xFFFFF3CD) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (orderingPaused) "⏸" else "🍳", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Kitchen Load Control",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (orderingPaused) Color(0xFF7D5800) else MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                if (orderingPaused)
                    "New order intake paused — kitchen catching up. Tap Resume when ready."
                else
                    "Temporarily pause new orders when your kitchen is overloaded.",
                style = MaterialTheme.typography.bodySmall,
                color = if (orderingPaused) Color(0xFF8A6400) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = {
                    val target = !orderingPaused
                    loading = true
                    scope.launch {
                        try {
                            val body = mapOf("paused" to target, "reason" to if (target) "Kitchen busy — partner self-throttle" else "")
                            RetrofitClient.restaurantApi.throttleOrdering(body)
                            orderingPaused = target
                        } catch (_: Exception) {}
                        loading = false
                    }
                },
                enabled = !loading && restaurantId != null,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (orderingPaused) CibusGreenDark else Color(0xFFDC2626)
                )
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(if (orderingPaused) "Resume Orders" else "Pause New Orders", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
