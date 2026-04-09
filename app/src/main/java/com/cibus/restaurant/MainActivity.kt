package com.cibus.restaurant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsControllerCompat
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.ui.RestaurantApp
import com.cibus.restaurant.ui.theme.CibusRestaurantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Green status bar to blend with hero; no enableEdgeToEdge() so layout is stable
        @Suppress("DEPRECATION")
        window.statusBarColor = 0xFF1B5E20.toInt()
        @Suppress("DEPRECATION")
        window.navigationBarColor = 0xFFFFFFFF.toInt()
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = true
        RetrofitClient.init(this)
        // Register FCM token with backend on each launch (idempotent on backend side)
        registerRestaurantFcmToken(applicationContext)
        setContent {
            CibusRestaurantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RestaurantApp()
                }
            }
        }
    }
}
