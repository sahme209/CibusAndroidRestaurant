package com.cibus.restaurant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cibus.restaurant.api.RetrofitClient

data class RestaurantTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun RestaurantMainScreen(onLogout: () -> Unit = {}) {
    var hasChain by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) {
        try {
            val resp = RetrofitClient.restaurantApi.getMe()
            if (resp.isSuccessful) hasChain = !resp.body()?.chainId.isNullOrBlank()
            else hasChain = false
        } catch (_: Exception) { hasChain = false }
    }
    val baseTabs = listOf(
        RestaurantTab("Dashboard", Icons.Default.Dashboard),
        RestaurantTab("Orders", Icons.Default.Assignment),
        RestaurantTab("Menu", Icons.Default.MenuBook),
        RestaurantTab("Promotions", Icons.Default.LocalOffer),
        RestaurantTab("Settings", Icons.Default.Settings)
    )
    val tabs = if (hasChain == true) {
        listOf(RestaurantTab("Chain", Icons.Default.Business)) + baseTabs
    } else baseTabs
    var selectedIndex by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                selectedIndex == 0 && hasChain == true -> ChainDashboardContent()
                selectedIndex == 0 && hasChain != true -> RestaurantAnalyticsContent()
                selectedIndex == 1 && hasChain == true -> RestaurantAnalyticsContent()
                selectedIndex == 1 && hasChain != true -> RestaurantOrdersContent()
                selectedIndex == 2 && hasChain == true -> RestaurantOrdersContent()
                selectedIndex == 2 && hasChain != true -> RestaurantMenuContent()
                selectedIndex == 3 && hasChain == true -> RestaurantMenuContent()
                selectedIndex == 3 && hasChain != true -> RestaurantPromotionsContent()
                selectedIndex == 4 && hasChain == true -> RestaurantPromotionsContent()
                selectedIndex == 4 && hasChain != true -> settingsColumn(onLogout)
                selectedIndex == 5 && hasChain == true -> settingsColumn(onLogout)
                else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("${tabs.getOrNull(selectedIndex)?.title ?: ""} — coming soon")
                }
            }
        }
    }
}

@Composable
private fun settingsColumn(onLogout: () -> Unit) = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    Text("Settings — coming soon")
    Button(onClick = onLogout) { Text("Sign Out") }
}
