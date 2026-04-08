package com.cibus.restaurant.ui
import com.cibus.restaurant.ui.theme.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog

data class RestaurantPromo(
    val id: String,
    val title: String,
    val code: String,
    val type: String,
    val value: String,
    val validUntil: String,
    val isActive: Boolean = true,
    val usageCount: Int = 0,
    val maxUses: Int = 0,
    val scheduleDays: List<String> = emptyList(),
    val scheduleStart: String = "",
    val scheduleEnd: String = ""
)

@Composable
fun RestaurantPromotionsContent() {
    var promotions by remember { mutableStateOf(listOf<RestaurantPromo>()) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val activeCount = promotions.count { it.isActive }
    val totalRedemptions = promotions.sumOf { it.usageCount }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Promotions", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RestTextPrimary)
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CibusGreenDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Create")
                }
            }
        }

        // Campaign performance summary
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CibusGreenDark.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$activeCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CibusGreenDark)
                        Text("Active", fontSize = 11.sp, color = RestTextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$totalRedemptions", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = RestTextPrimary)
                        Text("Redemptions", fontSize = 11.sp, color = RestTextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${promotions.size}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = RestTextPrimary)
                        Text("Total promos", fontSize = 11.sp, color = RestTextSecondary)
                    }
                }
            }
        }

        if (promotions.isNotEmpty()) {
            item {
                Text("Active promotions", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RestTextSecondary)
            }
        }

        promotions.forEachIndexed { index, promo ->
            item {
                PromotionCard(
                    promo = promo,
                    onToggle = {
                        promotions = promotions.toMutableList().also {
                            it[index] = promo.copy(isActive = !promo.isActive)
                        }
                    }
                )
            }
        }

        if (promotions.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocalOffer, null, tint = RestTextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No promotions yet", fontWeight = FontWeight.SemiBold, color = RestTextPrimary)
                        Text("Create your first promotion to attract more customers", fontSize = 13.sp, color = RestTextSecondary)
                    }
                }
            }
        }

        item {
            Text("Featured dishes", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = RestTextSecondary)
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Text(
                    "Featured dishes will be pulled from your menu once orders are live.",
                    fontSize = 14.sp,
                    color = RestTextSecondary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }

    if (showCreateDialog) {
        CreatePromotionDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { promo ->
                promotions = promotions + promo
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun PromotionCard(promo: RestaurantPromo, onToggle: () -> Unit) {
    val (icon, typeLabel) = when (promo.type) {
        "discount" -> Icons.Default.Percent to "% Off"
        "bogo" -> Icons.Default.CardGiftcard to "BOGO"
        "combo" -> Icons.Default.Restaurant to "Combo"
        "free_delivery" -> Icons.Default.DeliveryDining to "Free delivery"
        else -> Icons.Default.LocalOffer to promo.type
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = (if (promo.isActive) CibusGreenDark else RestTextSecondary).copy(alpha = 0.12f)
                ) {
                    Icon(icon, null, tint = if (promo.isActive) CibusGreenDark else RestTextSecondary, modifier = Modifier.padding(12.dp).size(24.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(promo.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = RestTextPrimary)
                    Text("Code: ${promo.code}", fontSize = 12.sp, color = RestTextSecondary)
                }
                Switch(
                    checked = promo.isActive,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CibusGreen)
                )
            }

            // Stats row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("${promo.usageCount} uses", fontSize = 11.sp, color = CibusGreenDark)
                if (promo.maxUses > 0) {
                    Text("Max: ${promo.maxUses}", fontSize = 11.sp, color = RestTextSecondary)
                }
                Text(promo.value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CibusGreenDark)
            }

            // Schedule info
            if (promo.scheduleDays.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Schedule, null, tint = RestTextSecondary, modifier = Modifier.size(12.dp))
                    Text(
                        "${promo.scheduleDays.joinToString(", ")} ${promo.scheduleStart}–${promo.scheduleEnd}",
                        fontSize = 11.sp,
                        color = RestTextSecondary
                    )
                }
            }

            Text("Valid: ${promo.validUntil}", fontSize = 11.sp, color = RestTextSecondary)
        }
    }
}

@Composable
private fun CreatePromotionDialog(
    onDismiss: () -> Unit,
    onCreate: (RestaurantPromo) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("discount") }
    var value by remember { mutableStateOf("") }
    var validUntil by remember { mutableStateOf("This week") }
    var maxUses by remember { mutableStateOf("") }
    var scheduleEnabled by remember { mutableStateOf(false) }
    var scheduleStart by remember { mutableStateOf("11:00") }
    var scheduleEnd by remember { mutableStateOf("15:00") }
    val allDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    var selectedDays by remember { mutableStateOf(setOf("Mon", "Tue", "Wed", "Thu", "Fri")) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp).heightIn(max = 560.dp)
                    .then(Modifier.imePadding()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Create promotion", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RestTextPrimary)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CibusGreen, cursorColor = CibusGreen)
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Promo code") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CibusGreen, cursorColor = CibusGreen)
                )

                // Type chips (discount, BOGO, combo, free delivery)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("discount" to "Discount", "bogo" to "BOGO", "combo" to "Combo", "free_delivery" to "Free Delivery").forEach { (t, label) ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CibusGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = {
                        Text(
                            when (type) {
                                "discount" -> "Value (e.g. 20%)"
                                "bogo" -> "Buy X Get Y (e.g. Buy 1 Get 1)"
                                "free_delivery" -> "Min order (e.g. Rs 500)"
                                else -> "Value (e.g. Rs 99)"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CibusGreen, cursorColor = CibusGreen)
                )

                OutlinedTextField(
                    value = maxUses,
                    onValueChange = { maxUses = it },
                    label = { Text("Max uses (0 = unlimited)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CibusGreen, cursorColor = CibusGreen)
                )

                // Schedule toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = scheduleEnabled,
                        onCheckedChange = { scheduleEnabled = it },
                        colors = CheckboxDefaults.colors(checkedColor = CibusGreen)
                    )
                    Text("Schedule specific days/times", fontSize = 13.sp)
                }

                if (scheduleEnabled) {
                    // Day chips
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        allDays.forEach { day ->
                            val selected = day in selectedDays
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedDays = if (selected) selectedDays - day else selectedDays + day
                                },
                                label = { Text(day.take(2), fontSize = 10.sp) },
                                modifier = Modifier.height(28.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CibusGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = scheduleStart,
                            onValueChange = { scheduleStart = it },
                            label = { Text("Start") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CibusGreen, cursorColor = CibusGreen)
                        )
                        OutlinedTextField(
                            value = scheduleEnd,
                            onValueChange = { scheduleEnd = it },
                            label = { Text("End") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CibusGreen, cursorColor = CibusGreen)
                        )
                    }
                }

                OutlinedTextField(
                    value = validUntil,
                    onValueChange = { validUntil = it },
                    label = { Text("Valid until") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CibusGreen, cursorColor = CibusGreen)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = CibusGreen) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && code.isNotBlank()) {
                                onCreate(
                                    RestaurantPromo(
                                        id = "p${System.currentTimeMillis()}",
                                        title = title.ifBlank { "Promotion" },
                                        code = code.ifBlank { "PROMO" },
                                        type = type,
                                        value = value.ifBlank { "—" },
                                        validUntil = validUntil.ifBlank { "Limited" },
                                        maxUses = maxUses.toIntOrNull() ?: 0,
                                        scheduleDays = if (scheduleEnabled) selectedDays.toList() else emptyList(),
                                        scheduleStart = if (scheduleEnabled) scheduleStart else "",
                                        scheduleEnd = if (scheduleEnabled) scheduleEnd else ""
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CibusGreenDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}
