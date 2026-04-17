package com.cibus.restaurant.ui

//
//  LoginScreen.kt — DoorDash Merchant-inspired login
//
//  Dark green gradient header + white elevated form card that overlaps.
//  Matches iOS LoginView layout: branded header, language pills, form card with offset.
//

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.AppLanguage
import com.cibus.restaurant.ResL10n
import com.cibus.restaurant.api.RestaurantSignInRequest
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.getAppLang
import com.cibus.restaurant.setAppLang
import com.cibus.restaurant.ui.theme.CibusDimens
import com.cibus.restaurant.ui.theme.CibusGreen
import com.cibus.restaurant.ui.theme.CibusGreenDark
import com.cibus.restaurant.ui.theme.CibusRed
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

    // Gradient for the dark green header
    val headerGradient = Brush.verticalGradient(
        colors = listOf(CibusGreenDark, CibusGreen)
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background: dark green top ~300dp, white bottom
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(headerGradient)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFF7F7F7))
            )
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Dark green header section ──────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CibusDimens.screenHorizontal),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(CibusDimens.spacing20))

                // Language pills
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppLanguage.all.forEach { l ->
                        val isSelected = lang == l
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(CibusDimens.radiusSm))
                                .background(
                                    if (isSelected) Color.White.copy(alpha = 0.2f)
                                    else Color.White.copy(alpha = 0.08f)
                                )
                                .clickable {
                                    ctx.setAppLang(l)
                                    lang = l
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = l,
                                fontSize = CibusDimens.labelSp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(CibusDimens.spacing24))

                // Branding icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = Color.White
                    )
                }

                Spacer(Modifier.height(CibusDimens.spacing16))

                // "HUBB Merchant" branding
                Text(
                    text = "HUBB Merchant",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(Modifier.height(6.dp))

                // Subtitle
                Text(
                    text = ResL10n.loginSubtitle(ctx),
                    fontSize = CibusDimens.bodySp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(44.dp))
            }

            // ── White form card (overlaps the green header) ────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CibusDimens.spacing16)
                    .offset(y = (-28).dp),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(CibusDimens.spacing24)
                        .padding(top = CibusDimens.spacing24),
                    verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing20)
                ) {
                    // Card heading
                    Text(
                        text = ResL10n.loginTitle(ctx),
                        fontSize = CibusDimens.headingSp,
                        fontWeight = FontWeight.SemiBold,
                        color = RestTextPrimary
                    )

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text(ResL10n.email(ctx)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(CibusDimens.radiusMd),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CibusGreen,
                            cursorColor = CibusGreen,
                            focusedLabelColor = CibusGreen
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text(ResL10n.password(ctx)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(CibusDimens.radiusMd),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CibusGreen,
                            cursorColor = CibusGreen,
                            focusedLabelColor = CibusGreen
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Error display
                    errorMessage?.let { msg ->
                        Text(
                            text = msg,
                            fontSize = CibusDimens.captionSp,
                            color = CibusRed
                        )
                    }

                    // Sign In button (RestaurantPrimaryButton)
                    RestaurantPrimaryButton(
                        text = ResL10n.signIn(ctx),
                        onClick = ::doLogin,
                        enabled = !isLoading,
                        isLoading = isLoading
                    )

                    // Register new restaurant — secondary green background button
                    Button(
                        onClick = onRegisterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(CibusDimens.btnHeight),
                        shape = RoundedCornerShape(CibusDimens.btnRadius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CibusGreen.copy(alpha = 0.08f),
                            contentColor = CibusGreen
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = ResL10n.registerNewRestaurant(ctx),
                            fontSize = CibusDimens.bodySp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Apply to partner text link
                    TextButton(
                        onClick = onApplyClick,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = ResL10n.applyLink(ctx),
                            fontSize = CibusDimens.captionSp,
                            color = RestTextTertiary
                        )
                    }
                }
            }

            // Bottom spacing
            Spacer(Modifier.height(CibusDimens.spacing32))
        }
    }
}
