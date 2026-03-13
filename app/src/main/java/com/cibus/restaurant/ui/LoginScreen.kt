package com.cibus.restaurant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
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
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.api.RestaurantSignInRequest
import com.cibus.restaurant.getAppLang
import com.cibus.restaurant.setAppLang
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onApplyClick: () -> Unit = {},
    onLoginSuccess: () -> Unit
) {
    val ctx = LocalContext.current
    var lang by remember { mutableStateOf(ctx.getAppLang()) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun doLogin() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please enter email and password"
            return
        }
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val response = RetrofitClient.restaurantApi.signIn(
                    RestaurantSignInRequest(email.trim(), password)
                )
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    if (data != null) {
                        RetrofitClient.getTokenStore().saveToken(data.accessToken)
                    }
                    onLoginSuccess()
                } else {
                    errorMessage = response.message() ?: "Login failed"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Network error"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppLanguage.all.forEach { l ->
                    TextButton(onClick = { ctx.setAppLang(l); lang = l }) {
                        Text(l, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(ResL10n.loginTitle(ctx), style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(ResL10n.loginSubtitle(ctx), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text(ResL10n.email(ctx)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text(ResL10n.password(ctx)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let { msg ->
                Spacer(Modifier.height(8.dp))
                Text(msg, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = ::doLogin,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    Text(ResL10n.signIn(ctx))
                }
            }
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onApplyClick) {
                Text(ResL10n.applyLink(ctx), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }
        }
    }
}
