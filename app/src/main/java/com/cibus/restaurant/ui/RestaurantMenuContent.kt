package com.cibus.restaurant.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cibus.restaurant.api.RetrofitClient

// Phase 140: RestaurantMenuContent now delegates to the full MenuEditorContent
@Composable
fun RestaurantMenuContent() {
    var restaurantId by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val me = RetrofitClient.restaurantApi.getMe()
            if (me.isSuccessful) {
                restaurantId = me.body()?.restaurantId
            } else {
                error = "Could not load profile"
            }
        } catch (e: Exception) {
            error = e.message ?: "Error"
        }
        loading = false
    }

    when {
        loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(error ?: "")
        }
        restaurantId.isNullOrBlank() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No restaurant linked. Complete onboarding first.")
        }
        else -> MenuEditorContent(restaurantId = restaurantId!!)
    }
}
