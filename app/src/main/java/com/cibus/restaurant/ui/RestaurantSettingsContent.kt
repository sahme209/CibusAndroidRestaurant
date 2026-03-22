package com.cibus.restaurant.ui
import com.cibus.restaurant.BuildConfig
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
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.api.RetrofitClient
import kotlinx.coroutines.launch

private val AVAILABILITY_OPTIONS = listOf(
    "open"         to "Open",
    "busy"         to "Busy",
    "closing_soon" to "Closing Soon",
    "closed"       to "Closed"
)

private const val PREFS_NAME = "restaurant_prefs"
private const val PREF_SOUND_ON_NEW_ORDER = "soundOnNewOrder"

@Composable
fun RestaurantSettingsContent(onLogout: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var restaurantId by remember { mutableStateOf<String?>(null) }
    var availability by remember { mutableStateOf("open") }
    var loadingAvailability by remember { mutableStateOf(true) }
    var savingAvailability by remember { mutableStateOf(false) }
    var newOrderNotifications by remember {
        mutableStateOf(ctx.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE).getBoolean(PREF_SOUND_ON_NEW_ORDER, true))
    }
    var showCloseConfirm by remember { mutableStateOf(false) }
    var pendingAvailability by remember { mutableStateOf<String?>(null) }
    var pickupInstructions by remember { mutableStateOf("Enter through main door. Orders at counter.") }
    var darkModeKitchen by remember { mutableStateOf(false) }
    var defaultPrepMinutes by remember { mutableStateOf(20) }
    val scope = rememberCoroutineScope()

    val isOpen = availability == "open" || availability == "busy" || availability == "closing_soon"

    LaunchedEffect(Unit) {
        try {
            val r = RetrofitClient.restaurantApi.getMe()
            if (r.isSuccessful) {
                val body = r.body()
                body?.restaurantId?.let { restaurantId = it }
                body?.availability?.let { availability = it }
            }
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
            .padding(CibusDimens.spacing24),
        verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing16),
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
            Column(modifier = Modifier.padding(CibusDimens.spacing16), verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing12)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CibusDimens.spacing12)
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

        // ── Default prep time ─────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(CibusDimens.spacing16), verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing8)) {
                Text("Default prep time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Shown to customers (minutes)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("$defaultPrepMinutes min", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { if (defaultPrepMinutes > 5) defaultPrepMinutes -= 5 }) {
                            Icon(Icons.Default.Remove, null, tint = CibusGreenDark)
                        }
                        IconButton(onClick = { if (defaultPrepMinutes < 60) defaultPrepMinutes += 5 }) {
                            Icon(Icons.Default.Add, null, tint = CibusGreenDark)
                        }
                    }
                }
            }
        }

        // ── Kitchen Load Throttle ─────────────────────────────────────────
        KitchenThrottleCard(restaurantId = restaurantId, scope = scope)

        // ── Pickup instructions (Uber Eats) ─────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(CibusDimens.spacing16), verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing8)) {
                Text("Pickup instructions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Shown to riders when they arrive", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = pickupInstructions,
                    onValueChange = { pickupInstructions = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    placeholder = { Text("e.g. Enter through main door…") }
                )
            }
        }

        // ── Holiday hours (Uber Eats) ───────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(CibusDimens.spacing16), verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing8)) {
                Text("Holiday hours", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Set dates when closed or different hours", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("No holidays set", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { }) { Text("Add holiday", color = CibusGreenDark, fontWeight = FontWeight.SemiBold) }
            }
        }

        // ── Dark mode kitchen ───────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier.padding(CibusDimens.spacing16),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dark mode (kitchen)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Easier in low-light kitchens", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = darkModeKitchen,
                    onCheckedChange = { darkModeKitchen = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CibusGreenDark)
                )
            }
        }

        // ── Uber Eats: More notification toggles ───────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(CibusDimens.spacing16), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Warning, null, tint = CibusAmber, modifier = Modifier.size(20.dp))
                    Text("Store alerts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                listOf(
                    "Connectivity issues" to true,
                    "Missed orders" to true,
                    "Order inaccuracies" to true,
                ).forEach { (label, enabled) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = enabled,
                            onCheckedChange = { },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CibusGreenDark)
                        )
                    }
                }
            }
        }

        // ── Notifications ─────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(CibusDimens.spacing16), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        onCheckedChange = {
                            newOrderNotifications = it
                            ctx.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE).edit().putBoolean(PREF_SOUND_ON_NEW_ORDER, it).apply()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CibusGreenDark)
                    )
                }
            }
        }

        // ── Payout methods ───────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(CibusDimens.spacing16), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Payout methods", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("How you receive earnings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AccountBalanceWallet, null, tint = CibusGreenDark, modifier = Modifier.size(20.dp))
                        Text("Bank transfer", style = MaterialTheme.typography.bodyMedium)
                    }
                    Icon(Icons.Default.CheckCircle, null, tint = CibusGreenDark, modifier = Modifier.size(20.dp))
                }
                Text("Add JazzCash / Easypaisa in Ops Portal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // ── Help & Support ───────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = { /* Open help URL */ },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier.padding(CibusDimens.spacing16),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Info, null, tint = CibusGreenDark, modifier = Modifier.size(20.dp))
                    Text("Help & Support", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
                Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // ── App version ───────────────────────────────────────────────────
        Text(
            "App version ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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
        Column(modifier = Modifier.padding(CibusDimens.spacing16), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
