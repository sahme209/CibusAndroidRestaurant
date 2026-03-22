package com.cibus.restaurant.ui.claim

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.claim.ClaimStatusSummary
import com.cibus.restaurant.claim.RestaurantListingState
import com.cibus.restaurant.ui.theme.CibusDimens
import com.cibus.restaurant.ui.theme.CibusGreen

/**
 * PartnerOnboardingScreen.kt
 * Hub screen for unverified restaurant partners.
 * Premium design with consistent spacing and clear hierarchy.
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF8FAF9), Color(0xFFF0F7F4), Color.White),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY,
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(CibusDimens.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(CibusDimens.spacing32))

        Icon(
            Icons.Default.Storefront,
            null,
            modifier = Modifier.size(72.dp),
            tint = CibusGreen,
        )

        Spacer(Modifier.height(CibusDimens.spacing16))
        Text(
            "Become a Cibus Partner",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(CibusDimens.spacing8))
        Text(
            "Claim your restaurant listing, verify ownership, and start receiving orders.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        // Status badge
        if (listingState != RestaurantListingState.UNCLAIMED && listingState != RestaurantListingState.IMPORTED_PUBLIC) {
            Spacer(Modifier.height(CibusDimens.spacing12))
            AssistChip(
                onClick = {},
                label = { Text(listingState.displayLabel) },
                leadingIcon = { Icon(Icons.Default.Info, null, Modifier.size(16.dp)) }
            )
        }

        Spacer(Modifier.height(CibusDimens.spacing32))

        // ── Route by state ─────────────────────────────────────────────────
        when (listingState) {
            RestaurantListingState.UNCLAIMED,
            RestaurantListingState.IMPORTED_PUBLIC -> {
                FindAndClaimSection(
                    searchText = searchText,
                    onSearchChange = { searchText = it },
                    onClaim = {
                        val name = searchText.trim()
                        // Uses typed name as discovery key. Backend will attempt canonical match.
                        // Unmatched claims are linkable via the portal enrichment review screen.
                        val id = "discover_${name.lowercase().replace(" ", "_")}"
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

        Spacer(Modifier.height(CibusDimens.spacing32))
    }
}

// ── Sub-sections ─────────────────────────────────────────────────────────────

@Composable
private fun FindAndClaimSection(
    searchText: String,
    onSearchChange: (String) -> Unit,
    onClaim: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing12),
    ) {
        listOf(
            Triple("1", "Find Your Restaurant", "Search for your restaurant among public listings."),
            Triple("2", "Claim Ownership", "Provide your identity and business details."),
            Triple("3", "Upload Documents", "Submit CNIC and tenancy proof for verification."),
            Triple("4", "Activation", "Verified by our team in 1–3 business days.")
        ).forEach { (step, title, body) ->
            StepCard(step, title, body)
        }

        Spacer(Modifier.height(CibusDimens.spacing16))

        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchChange,
            label = { Text("Your Restaurant Name") },
            placeholder = { Text("e.g. Butt Karahi F-8") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, null) },
        )

        Spacer(Modifier.height(CibusDimens.spacing8))

        Button(
            onClick = onClaim,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = searchText.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = CibusGreen),
        ) {
            Icon(Icons.Default.VerifiedUser, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Claim This Restaurant", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(CibusDimens.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(CibusDimens.spacing12),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = CibusGreen.copy(alpha = 0.12f),
            ) {
                Text(
                    step,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = CibusGreen,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun WaitingSection(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing16),
    ) {
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing16),
    ) {
        Icon(Icons.Default.Warning, null, modifier = Modifier.size(56.dp), tint = Color(0xFFE88C2B))
        Text("More Information Required", style = MaterialTheme.typography.titleLarge)
        reviewNote?.let {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(14.dp))
            }
        }
        Button(
            onClick = onUpdateDocs,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CibusGreen),
        ) {
            Text("View & Update Documents", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}

@Composable
private fun RejectedSection(reviewNote: String?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing16),
    ) {
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing16),
    ) {
        Icon(Icons.Default.PauseCircle, null, modifier = Modifier.size(56.dp), tint = Color(0xFFE88C2B))
        Text("Account Suspended", style = MaterialTheme.typography.titleLarge)
        Text("Your account has been temporarily suspended. Contact support@cibus.pk.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
