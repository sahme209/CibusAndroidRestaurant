package com.cibus.restaurant.ui
import com.cibus.restaurant.ui.theme.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Uber Eats Manager: Customer Reviews — read and respond to feedback. */
@Composable
fun RestaurantReviewsContent() {
    val reviews = remember {
        listOf(
            ReviewItem("1", "Ahmed K.", 5, "Amazing biryani! Will order again.", "Yesterday", "Thank you! Glad you enjoyed it."),
            ReviewItem("2", "Sara M.", 4, "Good food but delivery was a bit late.", "2 days ago", null),
        )
    }
    val avgRating = if (reviews.isNotEmpty()) {
        String.format("%.1f", reviews.map { it.rating }.average())
    } else "—"

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Reviews", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(avgRating, fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Average rating", fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                        Text("${reviews.size} reviews", fontSize = 12.sp, color = Color(0xFF6B7280))
                    }
                }
            }
        }
        reviews.forEach { r ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(r.customerName, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(5) { i ->
                                    Icon(
                                        Icons.Default.Star,
                                        null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (i < r.rating) Color(0xFFF59E0B) else Color(0xFFD1D5DB)
                                    )
                                }
                            }
                        }
                        Text(r.comment, fontSize = 14.sp, color = Color(0xFF6B7280))
                        r.reply?.let { reply ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CibusGreenDark.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    "Your reply: $reply",
                                    fontSize = 12.sp,
                                    color = CibusGreenDark,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                        Text(r.date, fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    }
                }
            }
        }
    }
}

private data class ReviewItem(
    val id: String,
    val customerName: String,
    val rating: Int,
    val comment: String,
    val date: String,
    val reply: String?,
)
