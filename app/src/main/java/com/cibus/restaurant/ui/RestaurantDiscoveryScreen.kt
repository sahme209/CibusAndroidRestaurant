package com.cibus.restaurant.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.api.DiscoveredRestaurantDto
import com.cibus.restaurant.api.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CibusGreen = Color(0xFF2D6A4F)

@Composable
fun RestaurantDiscoveryScreen(
    city: String = "Islamabad",
    onSkip: () -> Unit,
    onSelected: (DiscoveredRestaurantDto) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<DiscoveredRestaurantDto>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Search, contentDescription = null, tint = CibusGreen, modifier = Modifier.size(28.dp))
                Text("Find Your Restaurant", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Search our database to auto-fill your restaurant details.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        // Search field
        OutlinedTextField(
            value = query,
            onValueChange = { newVal ->
                query = newVal
                searchJob?.cancel()
                if (newVal.isBlank()) {
                    results = emptyList()
                    hasSearched = false
                    return@OutlinedTextField
                }
                searchJob = scope.launch {
                    delay(400)
                    isSearching = true
                    try {
                        val response = RetrofitClient.restaurantApi.discoverRestaurants(query = newVal, city = city)
                        if (response.isSuccessful) {
                            results = response.body()?.results ?: emptyList()
                        }
                    } catch (_: Exception) {}
                    isSearching = false
                    hasSearched = true
                }
            },
            placeholder = { Text("Restaurant name or area…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = ""; results = emptyList(); hasSearched = false }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CibusGreen,
                cursorColor = CibusGreen,
            )
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSearching) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CibusGreen, modifier = Modifier.size(28.dp))
                    }
                }
            } else if (results.isEmpty() && hasSearched) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🍽️", fontSize = 36.sp)
                        Text("No results found", fontWeight = FontWeight.SemiBold)
                        Text("Enter your details manually.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            } else if (results.isEmpty() && query.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("✨", fontSize = 32.sp)
                        Text("Auto-fill your profile", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Type your restaurant name above.\nWe'll search our Islamabad database.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                if (results.isNotEmpty()) {
                    item {
                        Text(
                            "${results.size} result${if (results.size == 1) "" else "s"} found",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                        )
                    }
                }
                items(results) { restaurant ->
                    DiscoveryResultCard(
                        restaurant = restaurant,
                        isSelected = selectedId == restaurant.id,
                        onClick = { selectedId = restaurant.id }
                    )
                }
            }

            // Confirm selected
            if (selectedId != null) {
                item {
                    val selected = results.find { it.id == selectedId }
                    if (selected != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider()
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text("✅", fontSize = 16.sp)
                                Text(
                                    "${selected.name} selected — details pre-filled",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CibusGreen
                                )
                            }
                            Button(
                                onClick = { onSelected(selected) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CibusGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Use This Restaurant", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }

        // Skip button
        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Enter details manually →", color = Color.Gray)
        }
    }
}

@Composable
private fun DiscoveryResultCard(
    restaurant: DiscoveredRestaurantDto,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) CibusGreen else Color(0xFFE0E0E0)
    val bgColor = if (isSelected) CibusGreen.copy(alpha = 0.06f) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(14.dp)
            .then(
                Modifier.then(
                    other = Modifier
                )
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CibusGreen.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(cuisineEmoji(restaurant.cuisine), fontSize = 22.sp)
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    restaurant.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                restaurant.rating?.let { r ->
                    if (r > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                            Text(String.format("%.1f", r), fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (restaurant.cuisine.isNotEmpty()) {
                    Text(restaurant.cuisine, fontSize = 11.sp, color = Color.Gray)
                }
                if (restaurant.sector.isNotEmpty()) {
                    Text("·", fontSize = 11.sp, color = Color.Gray)
                    Text(restaurant.sector, fontSize = 11.sp, color = Color.Gray)
                }
            }
            if (restaurant.address.isNotEmpty()) {
                Text(restaurant.address, fontSize = 10.sp, color = Color(0xFFAAAAAA), maxLines = 1)
            }
            if (restaurant.source == "existing_listing") {
                Text("Already on Cibus", fontSize = 10.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Medium)
            }
        }

        if (isSelected) {
            Text("✓", fontSize = 18.sp, color = CibusGreen, fontWeight = FontWeight.Bold)
        }
    }
}

private fun cuisineEmoji(cuisine: String): String {
    return when (cuisine.lowercase()) {
        "pizza" -> "🍕"
        "burgers" -> "🍔"
        "bbq" -> "🥩"
        "chinese" -> "🍜"
        "desserts", "bakery" -> "🎂"
        "fast food" -> "🍟"
        "desi" -> "🍛"
        "pakistani" -> "🍛"
        "biryani" -> "🍚"
        else -> "🍽️"
    }
}
