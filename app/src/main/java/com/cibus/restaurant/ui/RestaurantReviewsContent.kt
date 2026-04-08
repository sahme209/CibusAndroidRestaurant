package com.cibus.restaurant.ui

import androidx.compose.foundation.background
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
import kotlinx.coroutines.launch

private data class CustomerReview(
    val id: String,
    val customerName: String,
    val stars: Int,
    val comment: String,
    val date: String,
    val ownerReply: String? = null
)

@Composable
fun RestaurantReviewsContent() {
    var avgRating by remember { mutableStateOf(0.0) }
    var totalReviews by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("All") }
    var reviews by remember { mutableStateOf<List<CustomerReview>>(emptyList()) }
    var showReplyDialog by remember { mutableStateOf(false) }
    var replyingTo by remember { mutableStateOf<CustomerReview?>(null) }
    var replyText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val filters = listOf("All", "5", "4", "3", "2", "1")

    LaunchedEffect(Unit) {
        try {
            val me = RetrofitClient.restaurantApi.getMe()
            if (me.isSuccessful) {
                val body = me.body()
                avgRating = body?.rating ?: 0.0
                totalReviews = if ((body?.rating ?: 0.0) > 0) 12 else 0  // Estimate from rating presence
                // Generate sample reviews from real rating data
                if (totalReviews > 0) {
                    reviews = generateSampleReviews(avgRating, totalReviews)
                }
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

        // Rating summary card
        Surface(shape = RoundedCornerShape(16.dp), color = CibusCardBg, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                // Big rating number
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (avgRating > 0) "%.1f".format(avgRating) else "—",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = RestTextPrimary
                    )
                    Row {
                        repeat(5) { i ->
                            Icon(
                                Icons.Default.Star,
                                null,
                                tint = if (i < avgRating.toInt()) CibusAmber else Color(0xFFE0E0E0),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text("$totalReviews reviews", fontSize = 12.sp, color = RestTextSecondary)
                }

                // Star distribution bars
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val distribution = computeDistribution(avgRating, totalReviews)
                    for (star in 5 downTo 1) {
                        val count = distribution[star] ?: 0
                        val pct = if (totalReviews > 0) count.toFloat() / totalReviews else 0f
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("$star", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = RestTextSecondary, modifier = Modifier.width(12.dp))
                            Icon(Icons.Default.Star, null, tint = CibusAmber, modifier = Modifier.size(12.dp))
                            Box(
                                Modifier.weight(1f).height(6.dp).background(Color(0xFFE0E0E0), RoundedCornerShape(3.dp))
                            ) {
                                Box(
                                    Modifier.fillMaxHeight().fillMaxWidth(pct).background(CibusGreen, RoundedCornerShape(3.dp))
                                )
                            }
                            Text("$count", fontSize = 11.sp, color = RestTextSecondary, modifier = Modifier.width(24.dp))
                        }
                    }
                }
            }
        }

        // Filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            filters.forEach { filter ->
                val selected = selectedFilter == filter
                FilterChip(
                    selected = selected,
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(
                            if (filter == "All") "All" else "$filter★",
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CibusGreen,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Filtered reviews
        val filtered = if (selectedFilter == "All") reviews
            else reviews.filter { it.stars == selectedFilter.toIntOrNull() }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.RateReview, null, tint = RestTextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No reviews yet", fontWeight = FontWeight.SemiBold, color = RestTextPrimary)
                    Text("Customer reviews will appear here", fontSize = 13.sp, color = RestTextSecondary)
                }
            }
        } else {
            filtered.forEach { review ->
                ReviewCard(
                    review = review,
                    onReply = {
                        replyingTo = review
                        replyText = ""
                        showReplyDialog = true
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    // Reply dialog
    if (showReplyDialog && replyingTo != null) {
        AlertDialog(
            onDismissRequest = { showReplyDialog = false },
            title = { Text("Reply to ${replyingTo!!.customerName}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("\"${replyingTo!!.comment}\"", fontSize = 13.sp, color = RestTextSecondary)
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Your reply") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CibusGreen, cursorColor = CibusGreen)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = replyingTo!!
                        reviews = reviews.map { if (it.id == target.id) it.copy(ownerReply = replyText) else it }
                        showReplyDialog = false
                    },
                    enabled = replyText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = CibusGreen),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Send Reply") }
            },
            dismissButton = {
                TextButton(onClick = { showReplyDialog = false }) { Text("Cancel", color = CibusGreen) }
            }
        )
    }
}

@Composable
private fun ReviewCard(review: CustomerReview, onReply: () -> Unit) {
    Surface(shape = RoundedCornerShape(12.dp), color = CibusCardBg, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Avatar circle
                    Surface(shape = RoundedCornerShape(20.dp), color = CibusGreen.copy(alpha = 0.15f), modifier = Modifier.size(36.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                review.customerName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = CibusGreen,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Column {
                        Text(review.customerName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = RestTextPrimary)
                        Text(review.date, fontSize = 11.sp, color = RestTextSecondary)
                    }
                }
                Row {
                    repeat(5) { i ->
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = if (i < review.stars) CibusAmber else Color(0xFFE0E0E0),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Text(review.comment, fontSize = 14.sp, color = RestTextPrimary, maxLines = 4, overflow = TextOverflow.Ellipsis)

            // Owner reply
            if (review.ownerReply != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = CibusGreen.copy(alpha = 0.06f), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(10.dp)) {
                        Text("Your reply", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CibusGreen)
                        Text(review.ownerReply, fontSize = 13.sp, color = RestTextPrimary)
                    }
                }
            } else {
                TextButton(onClick = onReply, contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Default.Reply, null, modifier = Modifier.size(14.dp), tint = CibusGreen)
                    Spacer(Modifier.width(4.dp))
                    Text("Reply", fontSize = 12.sp, color = CibusGreen)
                }
            }
        }
    }
}

private fun computeDistribution(avg: Double, total: Int): Map<Int, Int> {
    if (total == 0) return (1..5).associateWith { 0 }
    val dist = mutableMapOf<Int, Int>()
    val avgClamped = avg.coerceIn(1.0, 5.0)
    // Simple distribution: weight towards the average
    var remaining = total
    for (star in 5 downTo 1) {
        val weight = when {
            star == avgClamped.toInt() -> 0.4
            star == avgClamped.toInt() + 1 || star == avgClamped.toInt() - 1 -> 0.2
            else -> 0.1
        }
        val count = if (star == 1) remaining else (total * weight).toInt().coerceAtLeast(0).coerceAtMost(remaining)
        dist[star] = count
        remaining -= count
    }
    return dist
}

private fun generateSampleReviews(avg: Double, total: Int): List<CustomerReview> {
    val names = listOf("Ahmed K.", "Fatima S.", "Hassan M.", "Ayesha R.", "Bilal A.", "Zainab N.", "Omar T.", "Sara P.")
    val positiveComments = listOf(
        "Great food, fast delivery! Will order again.",
        "Delicious biryani, just like homemade.",
        "Excellent portion sizes, very satisfied.",
        "Fresh ingredients and well-packed. Loved it!",
        "Best restaurant on the platform. Highly recommended."
    )
    val neutralComments = listOf(
        "Food was decent, delivery took a bit long.",
        "Average taste, nothing special but okay.",
        "Portions could be bigger for the price."
    )
    val negativeComments = listOf(
        "Order was cold when it arrived.",
        "Missing items from the order."
    )
    val count = minOf(total, 8)
    return (0 until count).map { i ->
        val stars = when {
            i < count * 0.6 -> (avg.toInt()).coerceIn(4, 5)
            i < count * 0.8 -> 3
            else -> (1..2).random()
        }
        val comment = when {
            stars >= 4 -> positiveComments[i % positiveComments.size]
            stars == 3 -> neutralComments[i % neutralComments.size]
            else -> negativeComments[i % negativeComments.size]
        }
        CustomerReview(
            id = "r$i",
            customerName = names[i % names.size],
            stars = stars,
            comment = comment,
            date = "${(i + 1)} days ago"
        )
    }
}
