package com.cibus.restaurant.ui

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.ui.theme.*

@Composable
fun RestaurantReviewsContent() {
    var avgRating by remember { mutableStateOf(0.0) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val me = RetrofitClient.restaurantApi.getMe()
            if (me.isSuccessful) {
                avgRating = me.body()?.rating ?: 0.0
            }
        } catch (_: Exception) {}
        loading = false
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CibusGreen)
        }
        return
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Reviews & Ratings", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = RestTextPrimary)

        // Rating summary card — shows only the real API average rating
        if (avgRating > 0) {
            Surface(shape = RoundedCornerShape(16.dp), color = CibusCardBg, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "%.1f".format(avgRating),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = RestTextPrimary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(5) { i ->
                            Icon(
                                Icons.Default.Star,
                                null,
                                tint = if (i < avgRating.toInt()) CibusAmber else Color(0xFFE0E0E0),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text("Average customer rating", fontSize = 13.sp, color = RestTextSecondary)
                }
            }
        }

        // Empty state for individual reviews
        Box(Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.RateReview, null, tint = RestTextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                Text(
                    if (avgRating > 0) "Individual reviews coming soon" else "No reviews yet",
                    fontWeight = FontWeight.SemiBold,
                    color = RestTextPrimary
                )
                Text(
                    if (avgRating > 0)
                        "Detailed customer reviews and reply functionality will be available here once the reviews feature goes live."
                    else
                        "Customer reviews will appear here after your first orders are delivered.",
                    fontSize = 13.sp,
                    color = RestTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
