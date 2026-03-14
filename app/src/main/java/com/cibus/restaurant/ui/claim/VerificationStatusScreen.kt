package com.cibus.restaurant.ui.claim

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.api.fetchClaimStatus
import com.cibus.restaurant.claim.ClaimStatusSummary
import com.cibus.restaurant.claim.RestaurantListingState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * VerificationStatusScreen.kt
 * Step 3: Show current claim/verification status to the partner.
 * Refreshes on pull and polls for state changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationStatusScreen(
    onVerified: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<ClaimStatusSummary?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    fun refresh() {
        scope.launch {
            isLoading = true
            try {
                status = RetrofitClient.restaurantApi.fetchClaimStatus()
                if (status?.canOperate == true) onVerified()
            } catch (_: Exception) { }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refresh()
        // Poll every 60 seconds while waiting
        while (true) {
            delay(60_000)
            if (status?.isWaiting == true) refresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verification Status") },
                actions = {
                    IconButton(onClick = ::refresh) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                Spacer(Modifier.height(80.dp))
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Loading status…", style = MaterialTheme.typography.bodyMedium)
            } else {
                val s = status
                if (s == null) {
                    Spacer(Modifier.height(80.dp))
                    Icon(Icons.Default.HourglassEmpty, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text("Your claim has been submitted.", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("We are reviewing your application (1–3 business days).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Spacer(Modifier.height(40.dp))
                    StatusIcon(s.state)
                    Spacer(Modifier.height(20.dp))
                    Text(s.state.displayLabel, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(statusMessage(s.state), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    s.reviewNote?.takeIf { it.isNotBlank() }?.let { note ->
                        Spacer(Modifier.height(20.dp))
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Text("From Cibus Team", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(6.dp))
                                Text(note, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    VerificationSteps(s.state)

                    if (s.state == RestaurantListingState.VERIFIED_PARTNER) {
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onVerified,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text("Open Restaurant Dashboard")
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun StatusIcon(state: RestaurantListingState) {
    val (icon, tint) = when (state) {
        RestaurantListingState.VERIFIED_PARTNER -> Icons.Default.VerifiedUser to Color(0xFF1F5C42)
        RestaurantListingState.REJECTED         -> Icons.Default.Cancel to MaterialTheme.colorScheme.error
        RestaurantListingState.SUSPENDED        -> Icons.Default.PauseCircle to Color(0xFFE88C2B)
        RestaurantListingState.NEEDS_MORE_INFO  -> Icons.Default.Warning to Color(0xFFE88C2B)
        else                                    -> Icons.Default.HourglassFull to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Icon(icon, null, modifier = Modifier.size(72.dp), tint = tint)
}

@Composable
private fun VerificationSteps(state: RestaurantListingState) {
    val steps = listOf(
        "Claim Submitted" to listOf(RestaurantListingState.CLAIM_SUBMITTED, RestaurantListingState.UNDER_REVIEW, RestaurantListingState.NEEDS_MORE_INFO, RestaurantListingState.VERIFIED_PARTNER, RestaurantListingState.REJECTED).contains(state),
        "Documents Received" to listOf(RestaurantListingState.UNDER_REVIEW, RestaurantListingState.NEEDS_MORE_INFO, RestaurantListingState.VERIFIED_PARTNER).contains(state),
        "Under Review" to listOf(RestaurantListingState.UNDER_REVIEW, RestaurantListingState.NEEDS_MORE_INFO, RestaurantListingState.VERIFIED_PARTNER).contains(state),
        "Verified Partner" to (state == RestaurantListingState.VERIFIED_PARTNER)
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            steps.forEachIndexed { i, (label, done) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        if (done) Icons.Default.CheckCircle else Icons.Default.Circle,
                        null,
                        modifier = Modifier.size(20.dp),
                        tint = if (done) Color(0xFF1F5C42) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        label,
                        style = if (done) MaterialTheme.typography.bodyMedium
                                else MaterialTheme.typography.bodySmall,
                        color = if (done) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun statusMessage(state: RestaurantListingState): String = when (state) {
    RestaurantListingState.CLAIM_SUBMITTED  -> "Your claim is in our queue. We'll review it shortly."
    RestaurantListingState.UNDER_REVIEW     -> "Our team is reviewing your documents and details."
    RestaurantListingState.NEEDS_MORE_INFO  -> "We need additional information to complete verification."
    RestaurantListingState.VERIFIED_PARTNER -> "You're verified! Your restaurant is now live on Cibus."
    RestaurantListingState.REJECTED         -> "Your claim was not approved. See the message from our team."
    RestaurantListingState.SUSPENDED        -> "Your account has been temporarily suspended."
    else                                    -> "Status unavailable. Please check again later."
}
