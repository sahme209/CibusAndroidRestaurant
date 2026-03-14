package com.cibus.restaurant.ui
import com.cibus.restaurant.ui.theme.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.api.ChainAnalyticsResponse
import com.cibus.restaurant.api.ChainBranch
import com.cibus.restaurant.api.ChainMeResponse
import com.cibus.restaurant.api.RetrofitClient

/** Phase 93: Chain Dashboard — branch selector, chain metrics, branch performance. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChainDashboardContent() {
    val api = RetrofitClient.restaurantApi
    var chain by remember { mutableStateOf<ChainMeResponse?>(null) }
    var analytics by remember { mutableStateOf<ChainAnalyticsResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedBranchId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val chainResp = api.getChainsMe()
            if (chainResp.isSuccessful) {
                chain = chainResp.body()
                chain?.id?.let { id ->
                    val aResp = api.getChainAnalytics(id)
                    if (aResp.isSuccessful) analytics = aResp.body()
                    selectedBranchId = chain?.branches?.firstOrNull()?.id
                }
            } else error = "Could not load chain"
        } catch (e: Exception) {
            error = e.message ?: "Failed to load"
        }
    }

    when {
        error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(error!!, color = Color(0xFF6B6B6B))
            }
        }
        chain == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CibusGreenDark)
        }
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    chain!!.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
            item {
                if (chain!!.branches.size > 1) {
                    var expanded by remember { mutableStateOf(false) }
                    val selected = chain!!.branches.find { it.id == selectedBranchId }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selected?.name ?: "Select branch",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            chain!!.branches.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text(b.name ?: b.id) },
                                    onClick = {
                                        selectedBranchId = b.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ChainMetricCard(
                        title = "Orders today",
                        value = "${analytics?.todayOrders ?: 0}",
                        icon = Icons.Default.ShoppingCart
                    )
                    ChainMetricCard(
                        title = "Revenue",
                        value = "Rs ${(analytics?.todayRevenue ?: 0.0).toInt()}",
                        icon = Icons.Default.AttachMoney
                    )
                }
            }
            item {
                Text(
                    "Branch performance",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )
            }
            chain!!.branches.forEach { branch ->
                item(key = branch.id) {
                    val metrics = analytics?.branches?.get(branch.id)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF5F5F5)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    branch.name ?: branch.id,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1A1A1A)
                                )
                                branch.address?.let { Text(it, fontSize = 12.sp, color = Color(0xFF6B6B6B)) }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${metrics?.orders ?: 0} orders",
                                    fontSize = 13.sp,
                                    color = Color(0xFF6B6B6B)
                                )
                                Text(
                                    "Rs ${(metrics?.revenue ?: 0.0).toInt()}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CibusGreenDark
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun RowScope.ChainMetricCard(
    title: String,
    value: String,
    icon: ImageVector
) {
    Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(icon, null, tint = CibusGreenDark, modifier = Modifier.size(18.dp))
                Text(title, fontSize = 12.sp, color = Color(0xFF6B6B6B))
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
        }
    }
}
