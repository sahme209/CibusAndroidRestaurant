package com.cibus.restaurant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cibus.restaurant.ui.theme.CibusTextPrimary
import com.cibus.restaurant.ui.theme.CibusTextSecondary

@Composable
fun RestaurantOrderIssuesContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Order Issues", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CibusTextPrimary)
            Text("Disputes and resolutions coming soon", style = MaterialTheme.typography.bodyMedium, color = CibusTextSecondary, modifier = Modifier.padding(horizontal = 32.dp))
        }
    }
}
