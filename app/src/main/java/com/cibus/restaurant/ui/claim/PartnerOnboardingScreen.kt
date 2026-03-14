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
import com.cibus.restaurant.claim.ClaimStatusSummary
import com.cibus.restaurant.claim.RestaurantListingState

/**
 * PartnerOnboardingScreen.kt
 * Hub screen for unverified restaurant partners.
 * Routes between find/claim, waiting, needs-info, rejected, and suspended states.
 */
@Composable
fun PartnerOnboardingScreen(
    listingState: RestaurantListingState,
    claimStatus: ClaimStatusSummary?,
    onClaimNavigate: (restaurantId: String, restaurantName: String) -> Unit,
    onStatusNavigate: () -> Unit,
    onVerified: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Spacer(Modifier.height(32.dp))

        Icon(
            Icons.Default.Storefront,
            null,
            modifier = Modifier.size(72.dp),
            tint = Color(0xFF1F5C42)
        )

        Spacer(Modifier.height(16.dp))
        Text("Become a Cibus Partner", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Claim your restaurant listing, verify ownership, and start receiving orders.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Status badge
        if (listingState != RestaurantListingState.UNCLAIMED && listingState != RestaurantListingState.IMPORTED_PUBLIC) {
            Spacer(Modifier.height(12.dp))
            AssistChip(
                onClick = {},
                label = { Text(listingState.displayLabel) },
                leadingIcon = { Icon(Icons.Default.Info, null, Modifier.size(16.dp)) }
            )
        }

        Spacer(Modifier.height(32.dp))

        // ── Route by state ─────────────────────────────────────────────────
        when (listingState) {
            RestaurantListingState.UNCLAIMED,
            RestaurantListingState.IMPORTED_PUBLIC -> {
                FindAndClaimSection(
                    searchText = searchText,
                    onSearchChange = { searchText = it },
                    onClaim = {
                        val name = searchText.trim()
                        val id = "search_${name.lowercase().replace(" ", "_")}"
                        onClaimNavigate(id, name)
                    }
                )
            }

            RestaurantListingState.CLAIM_SUBMITTED,
            RestaurantListingState.UNDER_REVIEW -> {
                WaitingSection(onRefresh = onStatusNavigate)
            }

            RestaurantListingState.NEEDS_MORE_INFO -> {
                NeedsInfoSection(
                    reviewNote = claimStatus?.reviewNote,
                    onUpdateDocs = onStatusNavigate
                )
            }

            RestaurantListingState.REJECTED -> {
                RejectedSection(reviewNote = claimStatus?.reviewNote)
            }

            RestaurantListingState.SUSPENDED -> {
                SuspendedSection()
            }

            RestaurantListingState.VERIFIED_PARTNER -> {
                LaunchedEffect(Unit) { onVerified() }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

// ── Sub-sections ─────────────────────────────────────────────────────────────

@Composable
private fun FindAndClaimSection(
    searchText: String,
    onSearchChange: (String) -> Unit,
    onClaim: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(
            Triple("1", "Find Your Restaurant", "Search for your restaurant among public listings."),
            Triple("2", "Claim Ownership", "Provide your identity and business details."),
            Triple("3", "Upload Documents", "Submit CNIC and tenancy proof for verification."),
            Triple("4", "Activation", "Verified by our team in 1–3 business days.")
        ).forEach { (step, title, body) ->
            StepCard(step, title, body)
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchChange,
            label = { Text("Your Restaurant Name") },
            placeholder = { Text("e.g. Butt Karahi F-8") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, null) }
        )

        Button(
            onClick = onClaim,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = searchText.isNotBlank()
        ) {
            Icon(Icons.Default.VerifiedUser, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Claim This Restaurant")
        }

        Text(
            "Can't find your restaurant? Contact partner-support@cibus.pk.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StepCard(step: String, title: String, body: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Badge { Text(step) }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun WaitingSection(onRefresh: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(Icons.Default.HourglassFull, null, modifier = Modifier.size(56.dp), tint = Color(0xFFE88C2B))
        Text("Application Under Review", style = MaterialTheme.typography.titleLarge)
        Text(
            "Our team is reviewing your details (1–3 business days). You'll receive an email update.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Check Status")
        }
    }
}

@Composable
private fun NeedsInfoSection(reviewNote: String?, onUpdateDocs: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(Icons.Default.Warning, null, modifier = Modifier.size(56.dp), tint = Color(0xFFE88C2B))
        Text("More Information Required", style = MaterialTheme.typography.titleLarge)
        reviewNote?.let {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(14.dp))
            }
        }
        Button(onClick = onUpdateDocs, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("View & Update Documents")
        }
    }
}

@Composable
private fun RejectedSection(reviewNote: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.error)
        Text("Claim Not Approved", style = MaterialTheme.typography.titleLarge)
        reviewNote?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("If you believe this was an error, contact support@cibus.pk.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SuspendedSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(Icons.Default.PauseCircle, null, modifier = Modifier.size(56.dp), tint = Color(0xFFE88C2B))
        Text("Account Suspended", style = MaterialTheme.typography.titleLarge)
        Text("Your account has been temporarily suspended. Contact support@cibus.pk.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
