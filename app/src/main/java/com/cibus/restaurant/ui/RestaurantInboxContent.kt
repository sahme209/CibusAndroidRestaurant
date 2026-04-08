package com.cibus.restaurant.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.ui.theme.*

private data class SupportTicket(
    val id: String,
    val subject: String,
    val message: String,
    val status: String = "open",
    val createdAt: String = "Just now"
)

private data class FaqItem(
    val question: String,
    val answer: String
)

@Composable
fun RestaurantInboxContent() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("FAQ", "Support", "My Tickets")
    var tickets by remember { mutableStateOf<List<SupportTicket>>(emptyList()) }
    var showCreateTicket by remember { mutableStateOf(false) }
    var newSubject by remember { mutableStateOf("") }
    var newMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    val faqs = remember {
        listOf(
            FaqItem("How do I update my menu?", "Go to the Menu tab and tap any item to edit. You can change names, prices, descriptions, and availability. Use the import feature to load a template."),
            FaqItem("When do I get paid?", "Payouts are processed weekly. Revenue from delivered orders minus the 15% platform fee is credited to your wallet. You can view your balance in the Payouts section."),
            FaqItem("How do I pause orders?", "Go to the Store tab and toggle 'Pause New Orders'. This temporarily stops incoming orders while you catch up with your kitchen queue."),
            FaqItem("What if I need to reject an order?", "Tap the X button on any new order and select a reason. Please try to accept orders whenever possible to maintain your rating."),
            FaqItem("How do promotions work?", "Create promotions from the More tab > Promotions. You can offer discounts, BOGO deals, or free delivery to attract more customers."),
            FaqItem("How is commission calculated?", "HUBB charges a 15% platform fee on completed orders. This is automatically deducted from your revenue before payout."),
            FaqItem("How do I contact support?", "Use the Support tab here to create a ticket, or reach us via WhatsApp for urgent issues.")
        )
    }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab, containerColor = Color.White) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(title, fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        when (selectedTab) {
            0 -> FaqTab(faqs)
            1 -> SupportTab(
                onCreateTicket = { showCreateTicket = true },
                onWhatsApp = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/923001234567?text=Hi%20HUBB%20Support"))
                    context.startActivity(intent)
                }
            )
            2 -> TicketsTab(tickets)
        }
    }

    // Create ticket dialog
    if (showCreateTicket) {
        AlertDialog(
            onDismissRequest = { showCreateTicket = false },
            title = { Text("New Support Ticket", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newSubject,
                        onValueChange = { newSubject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CibusGreen, cursorColor = CibusGreen)
                    )
                    OutlinedTextField(
                        value = newMessage,
                        onValueChange = { newMessage = it },
                        label = { Text("Describe your issue") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CibusGreen, cursorColor = CibusGreen)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSubject.isNotBlank() && newMessage.isNotBlank()) {
                            tickets = tickets + SupportTicket(
                                id = "T${System.currentTimeMillis()}",
                                subject = newSubject,
                                message = newMessage
                            )
                            newSubject = ""
                            newMessage = ""
                            showCreateTicket = false
                            selectedTab = 2 // Switch to My Tickets
                        }
                    },
                    enabled = newSubject.isNotBlank() && newMessage.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = CibusGreen),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Submit") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTicket = false }) { Text("Cancel", color = CibusGreen) }
            }
        )
    }
}

@Composable
private fun FaqTab(faqs: List<FaqItem>) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Frequently Asked Questions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RestTextPrimary)
        Text("Find answers to common questions about running your restaurant on HUBB", fontSize = 13.sp, color = RestTextSecondary)
        Spacer(Modifier.height(8.dp))

        faqs.forEach { faq ->
            var expanded by remember { mutableStateOf(false) }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CibusCardBg,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.clickable { expanded = !expanded }.padding(16.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(faq.question, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = RestTextPrimary, modifier = Modifier.weight(1f))
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            null,
                            tint = RestTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    AnimatedVisibility(visible = expanded) {
                        Text(faq.answer, fontSize = 13.sp, color = RestTextSecondary, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SupportTab(onCreateTicket: () -> Unit, onWhatsApp: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Get Help", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RestTextPrimary)

        // Create ticket card
        SupportOptionCard(
            icon = Icons.Default.ConfirmationNumber,
            title = "Create Support Ticket",
            subtitle = "Describe your issue and our team will respond within 24 hours",
            color = CibusGreen,
            onClick = onCreateTicket
        )

        // WhatsApp card
        SupportOptionCard(
            icon = Icons.Default.Chat,
            title = "WhatsApp Support",
            subtitle = "Chat with us directly for urgent issues",
            color = Color(0xFF25D366),
            onClick = onWhatsApp
        )

        // Email card
        SupportOptionCard(
            icon = Icons.Default.Email,
            title = "Email Support",
            subtitle = "support@hubbfood.com — for detailed inquiries",
            color = CibusAmber,
            onClick = {}
        )

        // Emergency
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = CibusRed.copy(alpha = 0.06f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Warning, null, tint = CibusRed, modifier = Modifier.size(24.dp))
                Column {
                    Text("Emergency?", fontWeight = FontWeight.SemiBold, color = CibusRed)
                    Text("For safety or security emergencies, call 1122", fontSize = 12.sp, color = RestTextSecondary)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SupportOptionCard(icon: ImageVector, title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CibusCardBg,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.12f)) {
                Icon(icon, null, tint = color, modifier = Modifier.padding(10.dp).size(22.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = RestTextPrimary)
                Text(subtitle, fontSize = 12.sp, color = RestTextSecondary)
            }
            Icon(Icons.Default.ChevronRight, null, tint = RestTextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun TicketsTab(tickets: List<SupportTicket>) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("My Tickets", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RestTextPrimary)

        if (tickets.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inbox, null, tint = RestTextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No tickets", fontWeight = FontWeight.SemiBold, color = RestTextPrimary)
                    Text("Support tickets you create will appear here", fontSize = 13.sp, color = RestTextSecondary, textAlign = TextAlign.Center)
                }
            }
        } else {
            tickets.reversed().forEach { ticket ->
                Surface(shape = RoundedCornerShape(12.dp), color = CibusCardBg, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(shape = RoundedCornerShape(10.dp), color = CibusAmber.copy(alpha = 0.12f)) {
                            Icon(Icons.Default.ConfirmationNumber, null, tint = CibusAmber, modifier = Modifier.padding(10.dp).size(18.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(ticket.subject, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = RestTextPrimary)
                            Text(ticket.message, fontSize = 12.sp, color = RestTextSecondary, maxLines = 2)
                            Text(ticket.createdAt, fontSize = 11.sp, color = RestTextSecondary)
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = CibusAmber.copy(alpha = 0.1f)) {
                            Text(
                                ticket.status.replaceFirstChar { it.uppercase() },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CibusAmber,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
