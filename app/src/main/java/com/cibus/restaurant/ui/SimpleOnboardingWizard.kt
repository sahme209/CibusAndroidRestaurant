/**
 * Pakistan Restaurant Onboarding — 3-step simplified flow.
 * Minimal steps, no technical language, visual-first.
 * Target: <10 seconds, low cognitive load.
 */

package com.cibus.restaurant.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.cibus.restaurant.api.AdaptiveOnboardingRequest
import com.cibus.restaurant.api.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleOnboardingWizard(
    onDismiss: () -> Unit,
    onCompleted: (accessToken: String, expiresIn: Long) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(0) }
    var menuImageUri by remember { mutableStateOf<Uri?>(null) }
    var processing by remember { mutableStateOf(false) }
    var showCompleteSetup by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }

    // Complete setup fields (when user taps "Looks good")
    var restaurantName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Islamabad") }
    var sector by remember { mutableStateOf("F-6") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val cameraPermission = Manifest.permission.CAMERA
    val hasCameraPermission = ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && menuImageUri != null) {
            step = 1
            processing = true
            scope.launch {
                delay(1200) // Brief transition — photo stored for future use
                processing = false
                step = 2
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "menu_photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            menuImageUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            menuImageUri = uri
            step = 1
            processing = true
            scope.launch {
                delay(1200) // Brief transition — photo stored for future use
                processing = false
                step = 2
            }
        }
    }

    fun launchCamera() {
        if (!hasCameraPermission) {
            permissionLauncher.launch(cameraPermission)
            return
        }
        val file = File(context.cacheDir, "menu_photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        menuImageUri = uri
        takePictureLauncher.launch(uri)
    }

    fun launchGallery() {
        pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RestBackground)
            )
        },
        containerColor = RestBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                showCompleteSetup -> {
                    SimpleCompleteSetupContent(
                        restaurantName = restaurantName,
                        onRestaurantNameChange = { restaurantName = it },
                        ownerName = ownerName,
                        onOwnerNameChange = { ownerName = it },
                        city = city,
                        onCityChange = { city = it },
                        sector = sector,
                        onSectorChange = { sector = it },
                        phone = phone,
                        onPhoneChange = { v -> phone = v.filter { it.isDigit() }.take(11) },
                        email = email,
                        onEmailChange = { email = it },
                        password = password,
                        onPasswordChange = { password = it },
                        error = submitError,
                        submitting = submitting,
                        onBack = { showCompleteSetup = false },
                        onSubmit = {
                            if (restaurantName.isBlank() || ownerName.isBlank() || city.isBlank() || sector.isBlank() || phone.length < 10 || email.isBlank() || password.length < 6) return@SimpleCompleteSetupContent
                            submitting = true
                            submitError = null
                            scope.launch {
                                try {
                                    val req = AdaptiveOnboardingRequest(
                                        partnerName = ownerName,
                                        email = email,
                                        password = password,
                                        restaurantName = restaurantName,
                                        address = "$sector, $city",
                                        city = city,
                                        sector = sector,
                                        phone = phone,
                                        cuisineType = "Pakistani",
                                        integrationType = "APP",
                                        menuItems = emptyList(),
                                    )
                                    val resp = RetrofitClient.restaurantApi.submitOnboarding(req)
                                    val data = resp.body()?.data
                                    if (resp.isSuccessful && data != null) {
                                        onCompleted(data.accessToken, data.expiresIn?.toLong() ?: 86400L)
                                    } else {
                                        submitError = resp.errorBody()?.string()?.take(120) ?: "Please try again."
                                    }
                                } catch (e: Exception) {
                                    submitError = e.message ?: "Network error. Please try again."
                                }
                                submitting = false
                            }
                        },
                    )
                }
                step == 0 -> Step1TakePhoto(onTakePhoto = ::launchCamera, onChooseGallery = ::launchGallery)
                step == 1 -> Step2Processing()
                step == 2 -> Step3Confirmation(
                    onLooksGood = { showCompleteSetup = true },
                    onEdit = { step = 0; menuImageUri = null },
                )
            }
        }
    }
}

@Composable
private fun Step1TakePhoto(onTakePhoto: () -> Unit, onChooseGallery: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.weight(0.3f))
        Text(
            "Take photo of your menu",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = RestTextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            "Apni menu ki photo lo",
            fontSize = 16.sp,
            color = RestTextSecondary,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(40.dp))
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(RestGreen.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = RestGreen,
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onTakePhoto,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RestGreen),
        ) {
            Text("Take Photo", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onChooseGallery) {
            Text("Choose from gallery", color = RestGreen, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.weight(0.5f))
    }
}

@Composable
private fun Step2Processing() {
    val infiniteTransition = rememberInfiniteTransition(label = "processing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(RestGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = RestGreen,
                strokeWidth = 3.dp,
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Setting up your restaurant…",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = RestTextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            "Thora intezar karo…",
            fontSize = 16.sp,
            color = RestTextSecondary,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Step3Confirmation(
    onLooksGood: () -> Unit,
    onEdit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(RestGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(40.dp), tint = RestGreen)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Almost there",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = RestTextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            "Ab details bharo aur start karo",
            fontSize = 16.sp,
            color = RestTextSecondary,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onLooksGood,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RestGreen),
        ) {
            Text("Looks good", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = RestGreen),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(RestGreen)),
        ) {
            Text("Edit", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "You can edit anytime",
            fontSize = 14.sp,
            color = RestTextSecondary,
            textAlign = TextAlign.Center,
        )
        Text(
            "Aap baad mein bhi change kar sakte ho",
            fontSize = 13.sp,
            color = RestTextTertiary,
            modifier = Modifier.padding(top = 4.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SimpleCompleteSetupContent(
    restaurantName: String,
    onRestaurantNameChange: (String) -> Unit,
    ownerName: String,
    onOwnerNameChange: (String) -> Unit,
    city: String,
    onCityChange: (String) -> Unit,
    sector: String,
    onSectorChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    error: String?,
    submitting: Boolean,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text("Almost done", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RestTextPrimary)
        Text("2 minutes", fontSize = 14.sp, color = RestTextSecondary, modifier = Modifier.padding(top = 4.dp))
        Spacer(modifier = Modifier.height(24.dp))
        if (error != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                Text(error, modifier = Modifier.padding(12.dp), fontSize = 14.sp, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
        SimpleField("Restaurant name", restaurantName, onRestaurantNameChange)
        SimpleField("Your name", ownerName, onOwnerNameChange)
        SimpleField("City", city, onCityChange)
        SimpleField("Area / Sector", sector, onSectorChange)
        SimpleField("Phone", phone, onPhoneChange, KeyboardType.Phone)
        SimpleField("Email", email, onEmailChange, KeyboardType.Email)
        SimpleField("Password", password, onPasswordChange, KeyboardType.Password, isPassword = true)
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back") }
            Button(
                onClick = onSubmit,
                modifier = Modifier.weight(1f),
                enabled = !submitting && restaurantName.isNotBlank() && ownerName.isNotBlank() && city.isNotBlank() && sector.isNotBlank() && phone.length >= 10 && email.isNotBlank() && password.length >= 6,
                colors = ButtonDefaults.buttonColors(containerColor = RestGreen),
            ) {
                if (submitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Start")
            }
        }
    }
}

@Composable
private fun SimpleField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, fontSize = 14.sp, color = RestTextSecondary)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            shape = RoundedCornerShape(10.dp),
        )
    }
}
