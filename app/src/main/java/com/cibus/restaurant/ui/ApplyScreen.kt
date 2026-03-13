package com.cibus.restaurant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.cibus.restaurant.AppLanguage
import com.cibus.restaurant.ResL10n
import com.cibus.restaurant.api.RestaurantApplyRequest
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.getAppLang
import com.cibus.restaurant.setAppLang
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

@Composable
fun ApplyScreen() {
    val ctx = LocalContext.current
    var lang by remember { mutableStateOf(ctx.getAppLang()) }
    var partnerName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var restaurantName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var cnic by remember { mutableStateOf("") }
    var ntnNumber by remember { mutableStateOf("") }
    var pfaLicenseNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun doApply() {
        errorMessage = null
        val cnicDigits = cnic.replace("-", "").replace(" ", "")
        when {
            partnerName.isBlank() || email.isBlank() || password.isBlank() -> errorMessage = ResL10n.errBasic(ctx)
            restaurantName.isBlank() || address.isBlank() || phone.isBlank() -> errorMessage = ResL10n.errBasic(ctx)
            phone.length < 10 -> errorMessage = ResL10n.errPhone(ctx)
            cnicDigits.length != 13 || !cnicDigits.all { it.isDigit() } -> errorMessage = ResL10n.errCnic(ctx)
            ntnNumber.isBlank() || pfaLicenseNumber.isBlank() -> errorMessage = ResL10n.errNtnPfa(ctx)
            else -> {
                isLoading = true
                scope.launch {
                    try {
                        val response = RetrofitClient.restaurantApi.apply(
                            RestaurantApplyRequest(
                                partnerName = partnerName.trim(),
                                email = email.trim(),
                                password = password,
                                restaurantName = restaurantName.trim(),
                                address = address.trim(),
                                city = city.trim(),
                                phone = phone.trim(),
                                cnic = cnicDigits,
                                ntnNumber = ntnNumber.trim(),
                                pfaLicenseNumber = pfaLicenseNumber.trim()
                            )
                        )
                        if (response.isSuccessful) {
                            successMessage = ResL10n.successMsg(ctx)
                        } else {
                            val body = response.errorBody()?.string()
                            errorMessage = body?.let { b ->
                                try {
                                    com.google.gson.Gson().fromJson(b, JsonObject::class.java)?.get("message")?.asString
                                } catch (_: Exception) { null }
                            } ?: response.message() ?: "Application failed"
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Network error"
                    } finally {
                        isLoading = false
                    }
                }
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppLanguage.all.forEach { l ->
                    TextButton(onClick = { ctx.setAppLang(l); lang = l }) {
                        Text(l, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(ResL10n.applyTitle(ctx), style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(ResL10n.applyHint(ctx), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(value = partnerName, onValueChange = { partnerName = it }, label = { Text(ResL10n.ownerName(ctx)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(ResL10n.email(ctx)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text(ResL10n.password(ctx)) }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), singleLine = true)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = restaurantName, onValueChange = { restaurantName = it }, label = { Text(ResL10n.restaurantName(ctx)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text(ResL10n.address(ctx)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text(ResL10n.city(ctx)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(ResL10n.phone(ctx)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = cnic, onValueChange = { cnic = it }, label = { Text(ResL10n.cnic(ctx)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = ntnNumber, onValueChange = { ntnNumber = it }, label = { Text(ResL10n.ntn(ctx)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = pfaLicenseNumber, onValueChange = { pfaLicenseNumber = it }, label = { Text(ResL10n.pfaLicense(ctx)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            errorMessage?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp)) }
            successMessage?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp)) }
            Spacer(Modifier.height(16.dp))
            Button(onClick = ::doApply, modifier = Modifier.fillMaxWidth(), enabled = !isLoading && successMessage == null) {
                if (isLoading) CircularProgressIndicator(Modifier.height(24.dp).padding(8.dp)) else Text(ResL10n.submit(ctx))
            }
        }
    }
}
