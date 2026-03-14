package com.cibus.restaurant.ui.claim

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.api.acknowledgeDocuments
import com.cibus.restaurant.claim.*
import kotlinx.coroutines.launch

/**
 * DocumentUploadScreen.kt
 * Step 2: Upload or acknowledge documents required for claim verification.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentUploadScreen(
    claimId: String,
    onFinished: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var documents by remember {
        mutableStateOf(ClaimDocumentType.all.map { type ->
            ClaimDocument(id = java.util.UUID.randomUUID().toString(), type = type)
        })
    }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Upload Documents") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                "Upload the required documents to complete your claim. Clear photos or scans are accepted.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(20.dp))

            // Required docs
            Text(
                "REQUIRED DOCUMENTS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(8.dp))
            documents.filter { it.type.isRequired }.forEach { doc ->
                DocumentRow(doc) {
                    documents = documents.map { d ->
                        if (d.id == doc.id) d.copy(isPendingUpload = !d.isPendingUpload) else d
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "ADDITIONAL DOCUMENTS (RECOMMENDED)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(8.dp))
            documents.filter { !it.type.isRequired }.forEach { doc ->
                DocumentRow(doc) {
                    documents = documents.map { d ->
                        if (d.id == doc.id) d.copy(isPendingUpload = !d.isPendingUpload) else d
                    }
                }
            }

            // Info card
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "Secure file upload requires backend configuration. Your claim is submitted — our team will contact you to collect documents if needed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            RetrofitClient.restaurantApi.acknowledgeDocuments(claimId)
                        } catch (_: Exception) { }
                        onFinished()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Submit & Track Status")
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun DocumentRow(doc: ClaimDocument, onTap: () -> Unit) {
    ListItem(
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(doc.type.displayLabel, style = MaterialTheme.typography.bodyMedium)
                if (doc.type.isRequired) {
                    Badge { Text("Required") }
                }
            }
        },
        supportingContent = {
            Text(
                if (doc.isPendingUpload) "Marked for upload" else "Tap to attach",
                style = MaterialTheme.typography.bodySmall,
                color = if (doc.isPendingUpload)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                if (doc.isPendingUpload) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                null,
                tint = if (doc.isPendingUpload) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = { Icon(Icons.Default.ChevronRight, null) },
        modifier = Modifier.clickable(onClick = onTap)
    )
    HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
}
