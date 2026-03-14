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

/** Phase 84: Restaurant promotions dashboard — create discounts, combos, free delivery. */
@Composable
fun RestaurantPromotionsContent() {
    var promotions by remember {
        mutableStateOf(
            listOf(
                RestaurantPromo("p1", "20% Off Biryani", "BIRYANI20", "discount", "20%", "Mar 20"),
                RestaurantPromo("p2", "Burger + Drink Combo", "COMBO1", "combo", "Rs 99", "Weekend")
            )
        )
    }
    var showCreateDialog by remember { mutableStateOf(false) }

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
                Text(
                    "Promotions",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
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

        item {
            Text(
                "Active promotions",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B6B6B)
            )
        }

        promotions.forEach { promo ->
            item {
                PromotionCard(promo = promo)
            }
        }

        item {
            Text(
                "Featured dishes",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B6B6B)
            )
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FeaturedDishRow(name = "Beef Cheese Burger", label = "Chef special")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    FeaturedDishRow(name = "Chicken Biryani", label = "Recommended dish")
                }
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

data class RestaurantPromo(
    val id: String,
    val title: String,
    val code: String,
    val type: String,
    val value: String,
    val validUntil: String
)

@Composable
private fun PromotionCard(promo: RestaurantPromo) {
    val (icon, typeLabel) = when (promo.type) {
        "discount" -> Icons.Default.Percent to "% Off"
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
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CibusGreenDark.copy(alpha = 0.12f)
            ) {
                Icon(icon, null, tint = CibusGreenDark, modifier = Modifier.padding(12.dp).size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(promo.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                Text("Code: ${promo.code}", fontSize = 12.sp, color = Color(0xFF6B6B6B))
                Text("Valid: ${promo.validUntil}", fontSize = 11.sp, color = Color(0xFF8E8E8E))
            }
            Text(promo.value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CibusGreenDark)
        }
    }
}

@Composable
private fun FeaturedDishRow(name: String, label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A1A1A))
            Text(label, fontSize = 11.sp, color = CibusGreenDark)
        }
        Icon(Icons.Default.Star, null, tint = CibusGreenDark, modifier = Modifier.size(20.dp))
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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text("Create promotion", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Promo code") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == "discount",
                        onClick = { type = "discount" },
                        label = { Text("Discount") }
                    )
                    FilterChip(
                        selected = type == "combo",
                        onClick = { type = "combo" },
                        label = { Text("Combo") }
                    )
                    FilterChip(
                        selected = type == "free_delivery",
                        onClick = { type = "free_delivery" },
                        label = { Text("Free delivery") }
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(if (type == "discount") "Value (e.g. 20%)" else "Value (e.g. Rs 99)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = validUntil,
                    onValueChange = { validUntil = it },
                    label = { Text("Valid until / Schedule (e.g. Mar 20, This week)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Limited time offers — set when the promotion ends") }
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
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
                                        validUntil = validUntil.ifBlank { "Limited" }
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CibusGreenDark)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}
