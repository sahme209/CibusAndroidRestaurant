package com.cibus.restaurant.ui
import com.cibus.restaurant.ui.theme.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Uber Eats Manager: Order Issues — refunds, disputes, chargebacks. */
@Composable
fun RestaurantOrderIssuesContent() {
    val issues = remember {
        listOf(
            OrderIssueItem("1", "ord-1", "Refund", "pending", 100.0, "Customer requested partial refund — cold food"),
        )
    }
    val pendingCount = issues.count { it.status == "pending" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Order Issues", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
        }
        if (pendingCount > 0) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                        Text("$pendingCount pending", fontWeight = FontWeight.SemiBold, color = Color(0xFF92400E))
                    }
                }
            }
        }
        issues.forEach { i ->
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
                            Text("${i.type} — #${i.orderId.takeLast(6)}", fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (i.status == "pending") Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
                            ) {
                                Text(i.status, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (i.status == "pending") Color(0xFF92400E) else Color(0xFF065F46), modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                            }
                        }
                        Text(i.description, fontSize = 13.sp, color = Color(0xFF6B7280))
                        if (i.amount != null) {
                            Text("Rs ${i.amount.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A1A1A))
                        }
                        if (i.status == "pending") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) { Text("Acknowledge", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                                TextButton(onClick = { }) { Text("Dispute", color = CibusRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class OrderIssueItem(
    val id: String,
    val orderId: String,
    val type: String,
    val status: String,
    val amount: Double?,
    val description: String,
)
