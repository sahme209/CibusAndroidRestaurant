package com.cibus.restaurant.ui

// 5-tab architecture matching iOS: Home | Orders | Menu | Store | More

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.ui.theme.*

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
    var hasChain by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(RestaurantTabItem.HOME) }
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(true) }

    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { isConnected = true }
            override fun onLost(network: Network) { isConnected = false }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, callback)
        onDispose { cm.unregisterNetworkCallback(callback) }
    }

    LaunchedEffect(Unit) {
        try {
            val resp = RetrofitClient.restaurantApi.getMe()
            if (resp.isSuccessful) {
                val me = resp.body()
                restaurantId = me?.restaurantId ?: ""
                hasChain = !me?.chainId.isNullOrEmpty()
            }
        } catch (_: Exception) {}
    }

    Scaffold(
        bottomBar = {
            Column {
                // Clean top border on navigation bar
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(CibusGreen.copy(alpha = 0.15f))
                )
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .shadow(
                            elevation = 6.dp,
                            ambientColor = Color.Black.copy(alpha = 0.04f),
                            spotColor = Color.Black.copy(alpha = 0.03f)
                        ),
                    containerColor = Color.White.copy(alpha = 0.95f),
                    tonalElevation = 0.dp,
                ) {
                    RestaurantTabItem.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        // Spring scale animation on tab switch
                        val tabScale by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0.92f,
                            animationSpec = CibusMotion.snapSpring,
                            label = "tab_scale_${tab.name}"
                        )
                        // Bounce-on-select — matches iOS .symbolEffect(.bounce)
                        var bounceScale by remember { mutableFloatStateOf(1f) }
                        val animatedBounce by animateFloatAsState(
                            targetValue = bounceScale,
                            animationSpec = spring(dampingRatio = 0.4f, stiffness = 500f),
                            label = "tab_bounce_${tab.name}"
                        )
                        LaunchedEffect(isSelected) {
                            if (isSelected) {
                                bounceScale = 1.15f
                                delay(100)
                                bounceScale = 1f
                            }
                        }
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        tab.icon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.scale(tabScale * animatedBounce)
                                    )
                                    // Active tab dot indicator
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .background(CibusGreen, CircleShape)
                                        )
                                    }
                                }
                            },
                            label = {
                                Text(
                                    tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CibusGreen,
                                selectedTextColor = CibusGreen,
                                unselectedIconColor = AppleLabelSecondary,
                                unselectedTextColor = AppleLabelSecondary,
                                indicatorColor = Color.Transparent,
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            AnimatedVisibility(visible = !isConnected) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE53935))
                        .padding(vertical = 10.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text("No internet — orders may not update", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
                    RestaurantTabItem.MORE -> RestaurantMoreScreen(hasChain = hasChain, onLogout = onLogout)
                }
            }
        }
    }
}
