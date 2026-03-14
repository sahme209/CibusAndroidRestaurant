package com.cibus.restaurant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import com.cibus.restaurant.api.RestaurantOrderDto
import com.cibus.restaurant.api.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun RestaurantMenuContent() {
    var restaurantId by remember { mutableStateOf<String?>(null) }
    var categories by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var kitchenOrders by remember { mutableStateOf<List<RestaurantOrderDto>>(emptyList()) }
    var menuStatus by remember { mutableStateOf("pending_partner_onboarding") }
    var availability by remember { mutableStateOf("closed") }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

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
            val menu = RetrofitClient.restaurantApi.getMenu(id)
            if (menu.isSuccessful) {
                val body = menu.body()
                categories = body?.categories ?: emptyList()
                menuStatus = body?.menuStatus ?: "pending_partner_onboarding"
            }
            val ord = RetrofitClient.restaurantApi.getOrders(id)
            if (ord.isSuccessful) {
                val all = ord.body() ?: emptyList()
                kitchenOrders = all.filter { it.status in listOf("preparing", "ready_for_pickup") }
            }
        } catch (e: Exception) {
            error = e.message ?: "Error"
        }
        loading = false
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            loading -> CircularProgressIndicator()
            error != null -> Text(error ?: "")
            else -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Menu", style = MaterialTheme.typography.titleLarge)
                Text("Status: $menuStatus", style = MaterialTheme.typography.bodyMedium)
                Text("Categories: ${categories.size}", style = MaterialTheme.typography.bodyMedium)
                if (categories.isNotEmpty()) {
                    categories.forEachIndexed { i, cat ->
                        val name = (cat["name"] as? String) ?: "Category ${i + 1}"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Text(
                                text = name,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                Text("Kitchen orders: ${kitchenOrders.size}", style = MaterialTheme.typography.titleMedium)
                kitchenOrders.take(5).forEach { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Order #${order.id.take(8)}")
                            Text(order.status ?: "—")
                        }
                    }
                }
                val rid = restaurantId
                if (rid != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Kitchen open", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = availability == "open",
                            onCheckedChange = { open ->
                                val av = if (open) "open" else "closed"
                                scope.launch {
                                    try {
                                        RetrofitClient.restaurantApi.patchAvailability(rid, mapOf("availability" to av))
                                        availability = av
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
