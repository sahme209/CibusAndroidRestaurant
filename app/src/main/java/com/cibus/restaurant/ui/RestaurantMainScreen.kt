package com.cibus.restaurant.ui

// 5-tab architecture matching iOS: Home | Orders | Menu | Store | More

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.ui.theme.CibusDimens
import com.cibus.restaurant.ui.theme.CibusGreen
import com.cibus.restaurant.ui.theme.CibusTextOnSurfaceSecondary

/** Five-tab enum matching the iOS RestaurantTab. */
enum class RestaurantTabItem(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Dashboard),
    ORDERS("Orders", Icons.Default.Assignment),
    MENU("Menu", Icons.Default.MenuBook),
    STORE("Store", Icons.Default.Storefront),
    MORE("More", Icons.Default.MoreHoriz),
}

@Composable
fun RestaurantMainScreen(onLogout: () -> Unit = {}) {
    var restaurantId by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(RestaurantTabItem.HOME) }

    LaunchedEffect(Unit) {
        try {
            val resp = RetrofitClient.restaurantApi.getMe()
            if (resp.isSuccessful) {
                restaurantId = resp.body()?.restaurantId ?: ""
            }
        } catch (_: Exception) {}
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = Color.White,
                tonalElevation = 0.dp,
            ) {
                RestaurantTabItem.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = {
                            Text(
                                tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CibusGreen,
                            selectedTextColor = CibusGreen,
                            unselectedIconColor = CibusTextOnSurfaceSecondary,
                            unselectedTextColor = CibusTextOnSurfaceSecondary,
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                RestaurantTabItem.HOME -> RestaurantAnalyticsContent()
                RestaurantTabItem.ORDERS -> RestaurantOrdersContent()
                RestaurantTabItem.MENU -> {
                    if (restaurantId.isNotEmpty()) {
                        MenuEditorContent(restaurantId = restaurantId)
                    } else {
                        RestaurantMenuContent()
                    }
                }
                RestaurantTabItem.STORE -> StoreContent()
                RestaurantTabItem.MORE -> RestaurantMoreContent(onLogout = onLogout)
            }
        }
    }
}
