package com.cibus.restaurant.ui.claim

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.claim.*
import com.cibus.restaurant.api.RestaurantClaimApiRequest
import com.cibus.restaurant.api.PayoutInfoDto
import kotlinx.coroutines.launch

/**
 * ClaimRestaurantScreen.kt
 * Step 1 of the verification flow: owner provides identity + business details.
 */

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ClaimRestaurantScreen(
    restaurantId: String,
    restaurantName: String,
    onClaimSubmitted: (claimId: String) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Form state
    var ownerName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(ClaimantRole.OWNER) }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var cnic by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var confirmedAddress by remember { mutableStateOf("") }
    var ntnNumber by remember { mutableStateOf("") }
    var pfaLicense by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var payoutAccountTitle by remember { mutableStateOf("") }
    var payoutBankName by remember { mutableStateOf("") }
    var payoutIBAN by remember { mutableStateOf("") }
    var jazzCash by remember { mutableStateOf("") }
    var easypaisa by remember { mutableStateOf("") }
    var showPayoutSection by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        if (ownerName.isBlank()) { errorMessage = "Full name is required."; return false }
        if (!email.contains("@")) { errorMessage = "A valid email is required."; return false }
        if (phone.length < 10) { errorMessage = "A valid phone number is required."; return false }
        val cnicDigits = cnic.replace("-", "").replace(" ", "")
        if (cnicDigits.length != 13 || !cnicDigits.all { it.isDigit() }) {
            errorMessage = "CNIC must be 13 digits."; return false
        }
        if (confirmedAddress.isBlank()) { errorMessage = "Confirmed address is required."; return false }
        return true
    }

    fun doSubmit() {
        if (!validate()) return
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val payout = if (payoutIBAN.isNotBlank()) RestaurantPayoutInfo(
                    accountTitle = payoutAccountTitle,
                    bankName = payoutBankName,
                    iban = payoutIBAN,
                    jazzCashWallet = jazzCash.ifBlank { null },
                    easypaisaWallet = easypaisa.ifBlank { null }
                ) else null

                val request = RestaurantClaimRequest(
                    restaurantId = restaurantId,
                    restaurantName = restaurantName,
                    ownerName = ownerName.trim(),
                    role = selectedRole.raw,
                    email = email.trim(),
                    phone = phone.trim(),
                    cnic = cnic.replace("-", "").replace(" ", ""),
                    businessName = businessName.ifBlank { null },
                    ntnNumber = ntnNumber.ifBlank { null },
                    pfaLicenseNumber = pfaLicense.ifBlank { null },
                    notes = notes.ifBlank { null },
                    confirmedAddress = confirmedAddress.trim(),
                    payoutInfo = payout
                )

                val claimId = RetrofitClient.restaurantApi.submitClaim(
                    RestaurantClaimApiRequest(
                        restaurantId = request.restaurantId,
                        restaurantName = request.restaurantName,
                        ownerName = request.ownerName,
                        role = request.role,
                        email = request.email,
                        phone = request.phone,
                        cnic = request.cnic,
                        businessName = request.businessName,
                        ntnNumber = request.ntnNumber,
                        pfaLicenseNumber = request.pfaLicenseNumber,
                        notes = request.notes,
                        confirmedAddress = request.confirmedAddress,
                        payoutInfo = request.payoutInfo?.let { p ->
                            PayoutInfoDto(
                                accountTitle = p.accountTitle,
                                bankName = p.bankName,
                                iban = p.iban,
                                jazzCashWallet = p.jazzCashWallet,
                                easypaisaWallet = p.easypaisaWallet
                            )
                        }
                    )
                ).body()?.string()?.let {
                    com.google.gson.Gson().fromJson(it, com.google.gson.JsonObject::class.java)
                        ?.get("claimId")?.asString
                } ?: java.util.UUID.randomUUID().toString()

                onClaimSubmitted(claimId)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Submission failed. Please try again."
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Claim Restaurant") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Cancel") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Text("Claim Ownership", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(6.dp))
            Text(
                "You are claiming $restaurantName. Provide your identity and business details.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))
            SectionHeader("Your Details")
            Spacer(Modifier.height(12.dp))

            // Role picker
            Text("Your Role", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ClaimantRole.all.forEachIndexed { index, role ->
                    SegmentedButton(
                        selected = selectedRole == role,
                        onClick = { selectedRole = role },
                        shape = SegmentedButtonDefaults.itemShape(index, ClaimantRole.all.size)
                    ) { Text(role.label, style = MaterialTheme.typography.labelSmall) }
                }
            }
            Spacer(Modifier.height(12.dp))

            FormField("Full Name", ownerName) { ownerName = it }
            FormField("Email", email, KeyboardType.Email) { email = it }
            FormField("Phone Number", phone, KeyboardType.Phone) { phone = it }
            FormField("CNIC (13 digits)", cnic, KeyboardType.Number) { cnic = it }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Business Details")
            Spacer(Modifier.height(12.dp))

            FormField("Business / Restaurant Name", businessName) { businessName = it }
            FormField("Confirmed Address", confirmedAddress) { confirmedAddress = it }
            FormField("NTN Number (optional)", ntnNumber, KeyboardType.Number) { ntnNumber = it }
            FormField("PFA License (optional)", pfaLicense) { pfaLicense = it }
            FormField("Notes (optional)", notes) { notes = it }

            Spacer(Modifier.height(16.dp))

            // Payout disclosure
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Payout / Banking Details", style = MaterialTheme.typography.titleSmall)
                        TextButton(onClick = { showPayoutSection = !showPayoutSection }) {
                            Text(if (showPayoutSection) "Hide" else "Add")
                        }
                    }
                    if (showPayoutSection) {
                        Spacer(Modifier.height(8.dp))
                        FormField("Account Title", payoutAccountTitle) { payoutAccountTitle = it }
                        FormField("Bank Name", payoutBankName) { payoutBankName = it }
                        FormField("IBAN (PK format)", payoutIBAN) { payoutIBAN = it }
                        FormField("JazzCash Wallet (optional)", jazzCash, KeyboardType.Phone) { jazzCash = it }
                        FormField("Easypaisa Wallet (optional)", easypaisa, KeyboardType.Phone) { easypaisa = it }
                    }
                }
            }

            errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = ::doSubmit,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Submit Claim & Continue to Documents")
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "By submitting you confirm these details are accurate.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun FormField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}
