package com.cibus.restaurant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.cibus.restaurant.api.MerchantDeliveryMode
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.ui.theme.*
import kotlinx.coroutines.launch

// Store operations screen — open/close, pause orders, hours, delivery, prep time.
// Matches iOS StoreView with green hero header, DoorDash Merchant-inspired.

@Composable
fun StoreContent() {
    val scope = rememberCoroutineScope()

    // ── State ────────────────────────────────────────────────────────────────
    var restaurantId by remember { mutableStateOf<String?>(null) }
    var restaurantName by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("open") }
    var loadingAvailability by remember { mutableStateOf(true) }
    var savingAvailability by remember { mutableStateOf(false) }
    var orderingPaused by remember { mutableStateOf(false) }
    var isPauseLoading by remember { mutableStateOf(false) }
    var showCloseConfirmation by remember { mutableStateOf(false) }
    var pendingAvailability by remember { mutableStateOf<String?>(null) }

    var defaultPrepMinutes by remember { mutableIntStateOf(20) }
    var openHoursOpen by remember { mutableStateOf("09:00") }
    var openHoursClose by remember { mutableStateOf("23:00") }
    var deliveryRadiusKm by remember { mutableIntStateOf(8) }
    var pickupInstructions by remember { mutableStateOf("") }
    var savingStore by remember { mutableStateOf(false) }
    var storeMessage by remember { mutableStateOf<String?>(null) }

    // Delivery mode state
    var deliveryMode by remember { mutableStateOf(MerchantDeliveryMode.PLATFORM_RIDER) }
    var selfDeliveryRadiusKm by remember { mutableStateOf(5f) }
    var selfDeliveryFee by remember { mutableStateOf("") }
    var estimatedSelfDeliveryMinutes by remember { mutableStateOf(30f) }

    val isOpen = availability == "open" || availability == "busy" || availability == "closing_soon"

    val statusLabel = when (availability) {
        "open" -> "Open"
        "busy" -> "Busy"
        "closing_soon" -> "Closing Soon"
        "closed" -> "Closed"
        else -> availability.replaceFirstChar { it.uppercase() }
    }

    val statusColor = when (availability) {
        "open" -> CibusSuccess
        "busy" -> CibusAmber
        "closing_soon" -> CibusOrange
        else -> CibusTextTertiary
    }

    // ── API helpers ──────────────────────────────────────────────────────────

    fun loadStoreData() {
        scope.launch {
            loadingAvailability = true
            try {
                val resp = RetrofitClient.restaurantApi.getMe()
                val me = resp.body()
                if (resp.isSuccessful && me != null) {
                    restaurantId = me.restaurantId
                    me.restaurantName?.let { restaurantName = it }
                    me.availability?.let { availability = it }
                    me.throttlePaused?.let { orderingPaused = it }
                    me.kitchenPrepMinutes?.let { defaultPrepMinutes = it }
                    me.deliveryRadiusKm?.let { deliveryRadiusKm = it.coerceIn(1, 50) }
                    me.pickupInstructions?.let { pickupInstructions = it }
                    me.openHours?.let { oh ->
                        oh.open?.takeIf { it.isNotEmpty() }?.let { openHoursOpen = it }
                        oh.close?.takeIf { it.isNotEmpty() }?.let { openHoursClose = it }
                    }
                    deliveryMode = MerchantDeliveryMode.from(me.deliveryMode)
                    me.selfDeliveryRadiusKm?.let { selfDeliveryRadiusKm = it.toFloat().coerceIn(1f, 20f) }
                    me.selfDeliveryFee?.let { selfDeliveryFee = if (it == 0.0) "0" else it.toInt().toString() }
                    me.estimatedSelfDeliveryMinutes?.let { estimatedSelfDeliveryMinutes = it.toFloat().coerceIn(10f, 90f) }
                }
            } catch (_: Exception) { }
            loadingAvailability = false
        }
    }

    fun setAvailability(value: String) {
        val rid = restaurantId ?: return
        scope.launch {
            savingAvailability = true
            try {
                val resp = RetrofitClient.restaurantApi.patchAvailability(
                    rid, mapOf("availability" to value)
                )
                if (resp.isSuccessful) availability = value
            } catch (_: Exception) { }
            savingAvailability = false
        }
    }

    fun toggleThrottle() {
        scope.launch {
            isPauseLoading = true
            val target = !orderingPaused
            try {
                val body = mapOf<String, Any>(
                    "paused" to target,
                    "reason" to if (target) "Kitchen busy" else ""
                )
                val resp = RetrofitClient.restaurantApi.throttleOrdering(body)
                if (resp.isSuccessful) orderingPaused = target
            } catch (_: Exception) { }
            isPauseLoading = false
        }
    }

    fun saveStore() {
        if (restaurantId == null) return
        scope.launch {
            savingStore = true
            storeMessage = null
            val body = mutableMapOf<String, Any>(
                "openHours" to mapOf("open" to openHoursOpen, "close" to openHoursClose),
                "deliveryRadiusKm" to deliveryRadiusKm,
                "kitchenPrepMinutes" to defaultPrepMinutes,
                "pickupInstructions" to pickupInstructions,
                "availability" to availability,
                "deliveryMode" to deliveryMode.apiValue,
                "selfDeliveryEnabled" to (deliveryMode == MerchantDeliveryMode.MERCHANT_SELF),
            )
            if (deliveryMode == MerchantDeliveryMode.MERCHANT_SELF) {
                body["selfDeliveryRadiusKm"] = selfDeliveryRadiusKm.toDouble()
                body["selfDeliveryFee"] = selfDeliveryFee.toDoubleOrNull() ?: 0.0
                body["estimatedSelfDeliveryMinutes"] = estimatedSelfDeliveryMinutes.toInt()
            }
            try {
                val resp = RetrofitClient.restaurantApi.patchRestaurantStore(body)
                storeMessage = if (resp.isSuccessful) "Store settings saved" else "Could not save. Try again."
            } catch (_: Exception) {
                storeMessage = "Could not save. Try again."
            }
            savingStore = false
        }
    }

    // ── Load on first composition ────────────────────────────────────────────
    LaunchedEffect(Unit) { loadStoreData() }

    // ── Close restaurant confirmation dialog ─────────────────────────────────
    if (showCloseConfirmation) {
        AlertDialog(
            onDismissRequest = { showCloseConfirmation = false },
            title = { Text("Close your restaurant?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Customers will not be able to place orders while your restaurant is closed.",
                    fontSize = CibusDimens.bodySp,
                    color = CibusTextOnSurfaceSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showCloseConfirmation = false
                    pendingAvailability?.let { setAvailability(it) }
                }) {
                    Text("Yes, close now", color = CibusRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseConfirmation = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(CibusDimens.radiusLg)
        )
    }

    // ── UI ───────────────────────────────────────────────────────────────────

    LazyColumn(modifier = Modifier.fillMaxSize().background(CibusSurfaceSecondary)) {

        // ── Green hero header ────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(CibusGreenDark, CibusGreen))
                    )
                    .padding(
                        start = CibusDimens.spacing16,
                        end = CibusDimens.spacing16,
                        top = 56.dp,
                        bottom = CibusDimens.spacing24
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Store",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (restaurantName.isNotEmpty()) {
                            Text(
                                restaurantName,
                                fontSize = CibusDimens.bodySp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    // Live status pill
                    Row(
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isOpen) CibusAccent else CibusTextTertiary)
                        )
                        Text(
                            statusLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // ── Store Status Card ────────────────────────────────────────────
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CibusDimens.spacing16)
                    .padding(top = CibusDimens.spacing16),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 3.dp,
                color = CibusCardBg
            ) {
                Column(modifier = Modifier.padding(CibusDimens.spacing16)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(CibusDimens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon box
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(CibusDimens.radiusMd))
                                .background(
                                    if (isOpen) CibusGreen.copy(alpha = 0.15f)
                                    else Color(0xFFE5E5EA)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (savingAvailability) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = CibusGreen,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    if (isOpen) Icons.Default.Storefront else Icons.Default.DarkMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (isOpen) CibusGreen else CibusTextTertiary
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing4)) {
                            Text(
                                if (isOpen) "Open for Orders" else "Temporarily Closed",
                                fontSize = CibusDimens.headingSp,
                                fontWeight = FontWeight.Bold,
                                color = CibusTextOnSurface
                            )
                            Text(
                                when {
                                    savingAvailability -> "Updating\u2026"
                                    isOpen -> "Customers can place orders"
                                    else -> "Tap below to reopen"
                                },
                                fontSize = CibusDimens.bodySp,
                                color = CibusTextOnSurfaceSecondary
                            )
                        }
                    }

                    Spacer(Modifier.height(CibusDimens.spacing16))

                    // Open/Close button
                    Button(
                        onClick = {
                            if (isOpen) {
                                pendingAvailability = "closed"
                                showCloseConfirmation = true
                            } else {
                                setAvailability("open")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !savingAvailability && !loadingAvailability && restaurantId != null,
                        shape = RoundedCornerShape(CibusDimens.radiusButton),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOpen) CibusRed else CibusGreen,
                            disabledContainerColor = CibusTextTertiary
                        )
                    ) {
                        Icon(
                            if (isOpen) Icons.Default.PowerSettingsNew else Icons.Default.FlashOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isOpen) "Close Restaurant" else "Open Restaurant",
                            fontSize = CibusDimens.sectionTitleSp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // ── Status Chips (when open) ─────────────────────────────────────
        if (isOpen) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CibusDimens.spacing16)
                        .padding(top = CibusDimens.spacing16),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 3.dp,
                    color = CibusCardBg
                ) {
                    Column(modifier = Modifier.padding(CibusDimens.spacing16)) {
                        Text(
                            "Current Status",
                            fontSize = CibusDimens.labelSp,
                            color = CibusTextTertiary
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(CibusDimens.spacing8)) {
                            AvailabilityChip("open", "Open", CibusGreen, availability, savingAvailability) { setAvailability(it) }
                            AvailabilityChip("busy", "Busy", CibusAmber, availability, savingAvailability) { setAvailability(it) }
                            AvailabilityChip("closing_soon", "Closing Soon", CibusOrange, availability, savingAvailability) { setAvailability(it) }
                        }
                    }
                }
            }
        }

        // ── Section break ────────────────────────────────────────────────
        item { Spacer(Modifier.height(CibusDimens.spacing8)) }

        // ── Kitchen Throttle Card ────────────────────────────────────────
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CibusDimens.spacing16)
                    .padding(top = CibusDimens.spacing8),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 3.dp,
                color = if (orderingPaused) CibusAmberLight.copy(alpha = 0.08f) else CibusCardBg
            ) {
                Column(modifier = Modifier.padding(CibusDimens.spacing16)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (orderingPaused) Icons.Default.PauseCircleFilled else Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = if (orderingPaused) CibusAmberLight else CibusSuccess
                        )
                        Text(
                            "Kitchen Load Control",
                            fontSize = CibusDimens.sectionTitleSp,
                            fontWeight = FontWeight.SemiBold,
                            color = CibusTextOnSurface
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        if (orderingPaused)
                            "New orders paused \u2014 kitchen catching up. Tap Resume when ready."
                        else
                            "Temporarily pause incoming orders when the kitchen is overloaded.",
                        fontSize = CibusDimens.captionSp,
                        color = CibusTextOnSurfaceSecondary
                    )

                    Spacer(Modifier.height(CibusDimens.spacing12))

                    Button(
                        onClick = { toggleThrottle() },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        enabled = !isPauseLoading && restaurantId != null,
                        shape = RoundedCornerShape(CibusDimens.radiusMd),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (orderingPaused) CibusGreen else CibusRed,
                            disabledContainerColor = CibusTextTertiary
                        )
                    ) {
                        if (isPauseLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                if (orderingPaused) "Resume Orders" else "Pause New Orders",
                                fontSize = CibusDimens.sectionTitleSp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // ── Delivery Mode Card ────────────────────────────────────────────
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CibusDimens.spacing16)
                    .padding(top = CibusDimens.spacing16),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 3.dp,
                color = CibusCardBg
            ) {
                Column(modifier = Modifier.padding(CibusDimens.spacing16)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = CibusGreen
                        )
                        Text(
                            "Delivery Mode",
                            fontSize = CibusDimens.sectionTitleSp,
                            fontWeight = FontWeight.SemiBold,
                            color = CibusTextOnSurface
                        )
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Choose who delivers orders to your customers.",
                        fontSize = CibusDimens.captionSp,
                        color = CibusTextOnSurfaceSecondary
                    )

                    Spacer(Modifier.height(CibusDimens.spacing12))

                    // Mode selector chips
                    Row(horizontalArrangement = Arrangement.spacedBy(CibusDimens.spacing8)) {
                        MerchantDeliveryMode.entries.forEach { mode ->
                            val isSelected = deliveryMode == mode
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { deliveryMode = mode },
                                shape = RoundedCornerShape(CibusDimens.radiusMd),
                                color = if (isSelected) CibusGreen.copy(alpha = 0.12f) else CibusSurfaceSecondary,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, CibusGreen) else null
                            ) {
                                Column(
                                    modifier = Modifier.padding(CibusDimens.spacing12),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        if (mode == MerchantDeliveryMode.PLATFORM_RIDER) Icons.Default.TwoWheeler else Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = if (isSelected) CibusGreen else CibusTextTertiary
                                    )
                                    Text(
                                        mode.displayName,
                                        fontSize = CibusDimens.captionSp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) CibusGreen else CibusTextOnSurface
                                    )
                                }
                            }
                        }
                    }

                    // Self delivery settings
                    if (deliveryMode == MerchantDeliveryMode.MERCHANT_SELF) {
                        Spacer(Modifier.height(CibusDimens.spacing16))

                        HorizontalDivider(color = CibusTextTertiary.copy(alpha = 0.3f))

                        Spacer(Modifier.height(CibusDimens.spacing12))

                        Text(
                            "Self Delivery Settings",
                            fontSize = CibusDimens.bodySp,
                            fontWeight = FontWeight.SemiBold,
                            color = CibusTextOnSurface
                        )

                        Spacer(Modifier.height(CibusDimens.spacing12))

                        // Delivery radius slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Delivery radius", fontSize = CibusDimens.bodySp, color = CibusTextOnSurface)
                            Text(
                                "${selfDeliveryRadiusKm.toInt()} km",
                                fontSize = CibusDimens.bodySp,
                                fontWeight = FontWeight.Bold,
                                color = CibusGreen
                            )
                        }
                        Slider(
                            value = selfDeliveryRadiusKm,
                            onValueChange = { selfDeliveryRadiusKm = it },
                            valueRange = 1f..20f,
                            steps = 18,
                            colors = SliderDefaults.colors(thumbColor = CibusGreen, activeTrackColor = CibusGreen)
                        )

                        Spacer(Modifier.height(CibusDimens.spacing8))

                        // Delivery fee
                        Text("Delivery fee (Rs)", fontSize = CibusDimens.labelSp, color = CibusTextTertiary)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = selfDeliveryFee,
                            onValueChange = { v ->
                                val filtered = v.filter { it.isDigit() || it == '.' }
                                val num = filtered.toDoubleOrNull()
                                if (num == null || num <= 500) selfDeliveryFee = filtered
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. 100", fontSize = CibusDimens.bodySp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(CibusDimens.radiusMd),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = CibusSurfaceSecondary,
                                focusedContainerColor = CibusSurfaceSecondary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = CibusGreen
                            )
                        )
                        Text("Set 0 for free delivery. Maximum Rs 500.", fontSize = CibusDimens.captionSp, color = CibusTextOnSurfaceSecondary)

                        Spacer(Modifier.height(CibusDimens.spacing12))

                        // Estimated delivery time slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Estimated delivery time", fontSize = CibusDimens.bodySp, color = CibusTextOnSurface)
                            Text(
                                "${estimatedSelfDeliveryMinutes.toInt()} min",
                                fontSize = CibusDimens.bodySp,
                                fontWeight = FontWeight.Bold,
                                color = CibusGreen
                            )
                        }
                        Slider(
                            value = estimatedSelfDeliveryMinutes,
                            onValueChange = { estimatedSelfDeliveryMinutes = it },
                            valueRange = 10f..90f,
                            steps = 15,
                            colors = SliderDefaults.colors(thumbColor = CibusGreen, activeTrackColor = CibusGreen)
                        )
                    }
                }
            }
        }

        // ── Prep Time Card ───────────────────────────────────────────────
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CibusDimens.spacing16)
                    .padding(top = CibusDimens.spacing16),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 3.dp,
                color = CibusCardBg
            ) {
                Column(modifier = Modifier.padding(CibusDimens.spacing16)) {
                    Text(
                        "Default Prep Time",
                        fontSize = CibusDimens.sectionTitleSp,
                        fontWeight = FontWeight.SemiBold,
                        color = CibusTextOnSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Estimated kitchen preparation time for new orders.",
                        fontSize = CibusDimens.captionSp,
                        color = CibusTextOnSurfaceSecondary
                    )
                    Spacer(Modifier.height(CibusDimens.spacing12))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "$defaultPrepMinutes min",
                            fontSize = CibusDimens.headingSp,
                            fontWeight = FontWeight.Bold,
                            color = CibusTextOnSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(CibusDimens.spacing4)) {
                            IconButton(
                                onClick = { if (defaultPrepMinutes > 5) defaultPrepMinutes -= 5 }
                            ) {
                                Icon(
                                    Icons.Default.RemoveCircle,
                                    contentDescription = "Decrease prep time",
                                    modifier = Modifier.size(28.dp),
                                    tint = CibusGreenLight
                                )
                            }
                            IconButton(
                                onClick = { if (defaultPrepMinutes < 60) defaultPrepMinutes += 5 }
                            ) {
                                Icon(
                                    Icons.Default.AddCircle,
                                    contentDescription = "Increase prep time",
                                    modifier = Modifier.size(28.dp),
                                    tint = CibusGreenLight
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Section break ────────────────────────────────────────────────
        item { Spacer(Modifier.height(CibusDimens.spacing8)) }

        // ── Hours & Delivery Card ────────────────────────────────────────
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CibusDimens.spacing16)
                    .padding(top = CibusDimens.spacing8),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 3.dp,
                color = CibusCardBg
            ) {
                Column(modifier = Modifier.padding(CibusDimens.spacing16)) {
                    Text(
                        "Hours & Delivery",
                        fontSize = CibusDimens.sectionTitleSp,
                        fontWeight = FontWeight.SemiBold,
                        color = CibusTextOnSurface
                    )

                    Spacer(Modifier.height(CibusDimens.cardPadding))

                    // Open / Close time fields
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StoreTimeField(
                            title = "Open",
                            value = openHoursOpen,
                            onValueChange = { openHoursOpen = it },
                            modifier = Modifier.weight(1f)
                        )
                        StoreTimeField(
                            title = "Close",
                            value = openHoursClose,
                            onValueChange = { openHoursClose = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(CibusDimens.cardPadding))

                    // Delivery radius stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "Delivery Radius",
                                fontSize = CibusDimens.bodySp,
                                fontWeight = FontWeight.Medium,
                                color = CibusTextOnSurface
                            )
                            Text(
                                "$deliveryRadiusKm km",
                                fontSize = CibusDimens.captionSp,
                                color = CibusTextOnSurfaceSecondary
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(CibusDimens.spacing4)) {
                            IconButton(
                                onClick = { if (deliveryRadiusKm > 1) deliveryRadiusKm -= 1 }
                            ) {
                                Icon(
                                    Icons.Default.RemoveCircle,
                                    contentDescription = "Decrease radius",
                                    modifier = Modifier.size(28.dp),
                                    tint = CibusGreenLight
                                )
                            }
                            IconButton(
                                onClick = { if (deliveryRadiusKm < 50) deliveryRadiusKm += 1 }
                            ) {
                                Icon(
                                    Icons.Default.AddCircle,
                                    contentDescription = "Increase radius",
                                    modifier = Modifier.size(28.dp),
                                    tint = CibusGreenLight
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(CibusDimens.cardPadding))

                    // Pickup instructions
                    Text(
                        "Pickup Instructions",
                        fontSize = CibusDimens.labelSp,
                        color = CibusTextTertiary
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = pickupInstructions,
                        onValueChange = { pickupInstructions = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Ring bell at back door", fontSize = CibusDimens.bodySp) },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(CibusDimens.radiusMd),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = CibusSurfaceSecondary,
                            focusedContainerColor = CibusSurfaceSecondary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = CibusGreen
                        )
                    )

                    Spacer(Modifier.height(CibusDimens.cardPadding))

                    // Save button
                    Button(
                        onClick = { saveStore() },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        enabled = !savingStore && restaurantId != null,
                        shape = RoundedCornerShape(CibusDimens.radiusMd),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CibusGreen,
                            disabledContainerColor = CibusTextTertiary
                        )
                    ) {
                        if (savingStore) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Save Changes",
                                fontSize = CibusDimens.sectionTitleSp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    // Feedback message
                    storeMessage?.let { msg ->
                        Spacer(Modifier.height(CibusDimens.spacing8))
                        Text(
                            msg,
                            fontSize = CibusDimens.captionSp,
                            color = CibusTextOnSurfaceSecondary
                        )
                    }
                }
            }
        }

        // ── Holiday Hours Card ───────────────────────────────────────────
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CibusDimens.spacing16)
                    .padding(top = CibusDimens.spacing16),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 3.dp,
                color = CibusCardBg
            ) {
                Column(modifier = Modifier.padding(CibusDimens.spacing16)) {
                    Text(
                        "Holiday Hours",
                        fontSize = CibusDimens.sectionTitleSp,
                        fontWeight = FontWeight.SemiBold,
                        color = CibusTextOnSurface
                    )
                    Spacer(Modifier.height(CibusDimens.spacing8))
                    Text(
                        "Special holiday schedules can be managed via the merchant portal or by contacting partner support.",
                        fontSize = CibusDimens.captionSp,
                        color = CibusTextOnSurfaceSecondary
                    )
                }
            }
        }

        // Bottom padding
        item { Spacer(Modifier.height(CibusDimens.spacing24)) }
    }
}

// ── AvailabilityChip ─────────────────────────────────────────────────────────

@Composable
private fun AvailabilityChip(
    value: String,
    label: String,
    chipColor: Color,
    currentAvailability: String,
    disabled: Boolean,
    onSelect: (String) -> Unit
) {
    val isSelected = currentAvailability == value
    Surface(
        modifier = Modifier.clickable(enabled = !disabled) { onSelect(value) },
        shape = RoundedCornerShape(CibusDimens.radiusSm),
        color = if (isSelected) chipColor else chipColor.copy(alpha = 0.15f)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = CibusDimens.spacing12, vertical = 7.dp),
            fontSize = CibusDimens.captionSp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color.White else chipColor
        )
    }
}

// ── StoreTimeField ───────────────────────────────────────────────────────────

@Composable
private fun StoreTimeField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            title,
            fontSize = CibusDimens.labelSp,
            color = CibusTextTertiary
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(CibusDimens.radiusMd),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = CibusSurfaceSecondary,
                focusedContainerColor = CibusSurfaceSecondary,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = CibusGreen
            )
        )
    }
}
