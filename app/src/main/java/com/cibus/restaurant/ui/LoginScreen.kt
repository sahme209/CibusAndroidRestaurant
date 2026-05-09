package com.cibus.restaurant.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.AppLanguage
import com.cibus.restaurant.ResL10n
import com.cibus.restaurant.api.RestaurantSignInRequest
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.getAppLang
import com.cibus.restaurant.setAppLang
import com.cibus.restaurant.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onBackToEntry: (() -> Unit)? = null,
    onApplyClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onLoginSuccess: suspend () -> String?
) {
    val ctx = LocalContext.current
    var lang by remember { mutableStateOf(ctx.getAppLang()) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var emailFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var headerVisible by remember { mutableStateOf(false) }
    var cardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        headerVisible = true
        kotlinx.coroutines.delay(200)
        cardVisible = true
    }

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
                        val sessionError = onLoginSuccess()
                        if (sessionError != null) {
                            RetrofitClient.getTokenStore().clear()
                            errorMessage = sessionError
                        }
                    } else {
                        errorMessage = "Login failed"
                    }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleGroupedBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Language pills
            AnimatedVisibility(
                visible = headerVisible,
                enter = fadeIn(tween(400))
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    AppLanguage.all.forEach { l ->
                        val isSelected = lang == l
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    if (isSelected) AppleSeparator.copy(alpha = 0.3f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    ctx.setAppLang(l)
                                    lang = l
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = l,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) AppleLabelPrimary else AppleLabelSecondary,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // Brand identity
            AnimatedVisibility(
                visible = headerVisible,
                enter = fadeIn(tween(500))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(CibusGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            modifier = Modifier.size(34.dp),
                            tint = CibusGreen
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "HUBB",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleLabelPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Merchant",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppleLabelSecondary,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Login card
            AnimatedVisibility(
                visible = cardVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 40 }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    tonalElevation = 0.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "Sign In",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleLabelPrimary
                        )

                        // Email field
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = ResL10n.email(ctx).uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppleLabelSecondary,
                                letterSpacing = 0.5.sp
                            )
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it; errorMessage = null },
                                placeholder = {
                                    Text("name@example.com", color = AppleLabelTertiary)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CibusGreen,
                                    unfocusedBorderColor = Color.Transparent,
                                    cursorColor = CibusGreen,
                                    focusedLabelColor = CibusGreen,
                                    unfocusedContainerColor = AppleGroupedBackground,
                                    focusedContainerColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { emailFocused = it.isFocused }
                            )
                        }

                        // Password field
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = ResL10n.password(ctx).uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppleLabelSecondary,
                                letterSpacing = 0.5.sp
                            )
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it; errorMessage = null },
                                placeholder = {
                                    Text(
                                        if (ResL10n.isUrdu(ctx)) "Zaruri" else "Required",
                                        color = AppleLabelTertiary
                                    )
                                },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CibusGreen,
                                    unfocusedBorderColor = Color.Transparent,
                                    cursorColor = CibusGreen,
                                    focusedLabelColor = CibusGreen,
                                    unfocusedContainerColor = AppleGroupedBackground,
                                    focusedContainerColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { passwordFocused = it.isFocused }
                            )
                        }

                        // Error
                        if (errorMessage != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = AppleSystemRed
                                )
                                Text(
                                    text = errorMessage ?: "",
                                    fontSize = 14.sp,
                                    color = AppleSystemRed
                                )
                            }
                        }

                        // Continue button
                        Button(
                            onClick = ::doLogin,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CibusGreen,
                                disabledContainerColor = CibusGreen.copy(alpha = 0.5f),
                            ),
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    if (ResL10n.isUrdu(ctx)) "Jari Rakhein" else "Continue",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        // Divider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            HorizontalDivider(
                                Modifier.weight(1f),
                                color = AppleSeparator.copy(alpha = 0.5f),
                                thickness = 0.5.dp
                            )
                            Text(
                                if (ResL10n.isUrdu(ctx)) "ya" else "or",
                                fontSize = 13.sp,
                                color = AppleLabelTertiary
                            )
                            HorizontalDivider(
                                Modifier.weight(1f),
                                color = AppleSeparator.copy(alpha = 0.5f),
                                thickness = 0.5.dp
                            )
                        }

                        // Register button
                        Button(
                            onClick = onRegisterClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppleGroupedBackground,
                                contentColor = AppleLabelPrimary,
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = CibusGreen
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                ResL10n.registerNewRestaurant(ctx),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Terms
            AnimatedVisibility(
                visible = cardVisible,
                enter = fadeIn(tween(500, delayMillis = 200))
            ) {
                Text(
                    text = if (ResL10n.isUrdu(ctx))
                        "Sign in karke aap HUBB ki Terms of Service se mutafiq hain."
                    else
                        "By signing in, you agree to HUBB's Terms of Service.",
                    fontSize = 12.sp,
                    color = AppleLabelTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 40.dp)
                        .padding(top = 20.dp)
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
