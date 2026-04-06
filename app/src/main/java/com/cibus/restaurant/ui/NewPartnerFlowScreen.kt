/**
 * Discovery-first onboarding: Search existing restaurants from DB -> Create account -> Submit.
 * Restaurant details (name, address, menu) are already in the database.
 * Only collect the claimer's identity info as required by Pakistani law.
 *
 * Android port of iOS NewPartnerFlowView.swift.
 */

package com.cibus.restaurant.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.api.AdaptiveOnboardingRequest
import com.cibus.restaurant.api.DiscoveredRestaurantDto
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.ui.theme.CibusDimens
import com.cibus.restaurant.ui.theme.CibusGreen
import com.cibus.restaurant.ui.theme.CibusGreenDark
import com.cibus.restaurant.ui.theme.CibusRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── NewPartnerFlowScreen ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPartnerFlowScreen(
    onCompleted: (token: String, expiresIn: Int) -> Unit,
    onDismiss: () -> Unit,
    onManualRegister: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var step by remember { mutableIntStateOf(0) }

    // Discovery bindings
    var restaurantName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var cuisineType by remember { mutableStateOf("") }
    var sector by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Islamabad") }
    var linkedRestaurantId by remember { mutableStateOf<String?>(null) }

    // Account fields
    var partnerName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Legal fields (Pakistani law requirements)
    var cnic by remember { mutableStateOf("") }
    var ntn by remember { mutableStateOf("") }
    var pfaLicense by remember { mutableStateOf("") }

    // State
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val stepLabels = listOf("Find Your Restaurant", "Your Details", "Review & Submit")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    if (step == 0) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    } else {
                        IconButton(onClick = { step -= 1; errorMessage = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RestBackground)
            )
        },
        containerColor = RestBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Progress header with green gradient ──────────────────────
            StepProgressHeader(
                currentStep = step,
                totalSteps = 3,
                label = stepLabels[step],
            )

            // ── Step content ────────────────────────────────────────────
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    (slideInHorizontally { w -> if (targetState > initialState) w else -w } + fadeIn())
                        .togetherWith(slideOutHorizontally { w -> if (targetState > initialState) -w else w } + fadeOut())
                },
                label = "step_transition"
            ) { currentStep ->
                when (currentStep) {
                    0 -> DiscoverStep(
                        onSelected = { dto ->
                            restaurantName = dto.name
                            address = dto.address
                            phone = dto.phone
                            cuisineType = dto.cuisine
                            sector = dto.sector
                            city = dto.city.ifBlank { "Islamabad" }
                            linkedRestaurantId = dto.id
                            step = 1
                        },
                        onManualRegister = onManualRegister,
                    )
                    1 -> DetailsStep(
                        restaurantName = restaurantName,
                        address = address,
                        cuisineType = cuisineType,
                        partnerName = partnerName,
                        onPartnerNameChange = { partnerName = it },
                        email = email,
                        onEmailChange = { email = it },
                        password = password,
                        onPasswordChange = { password = it },
                        phone = phone,
                        onPhoneChange = { phone = it.filter { c -> c.isDigit() }.take(11) },
                        cnic = cnic,
                        onCnicChange = { cnic = it.filter { c -> c.isDigit() }.take(13) },
                        ntn = ntn,
                        onNtnChange = { ntn = it },
                        pfaLicense = pfaLicense,
                        onPfaLicenseChange = { pfaLicense = it },
                        errorMessage = errorMessage,
                        onNext = {
                            errorMessage = null
                            // Validate
                            if (partnerName.isBlank()) { errorMessage = "Full name is required."; return@DetailsStep }
                            if (email.isBlank() || !email.contains("@")) { errorMessage = "A valid email is required."; return@DetailsStep }
                            if (password.length < 6) { errorMessage = "Password must be at least 6 characters."; return@DetailsStep }
                            val cnicDigits = cnic.replace("-", "")
                            if (cnicDigits.length != 13 || !cnicDigits.all { c -> c.isDigit() }) {
                                errorMessage = "CNIC must be 13 digits."; return@DetailsStep
                            }
                            step = 2
                        }
                    )
                    else -> ReviewStep(
                        restaurantName = restaurantName,
                        address = address,
                        cuisineType = cuisineType,
                        sector = sector,
                        city = city,
                        partnerName = partnerName,
                        email = email,
                        phone = phone,
                        cnic = cnic,
                        ntn = ntn,
                        pfaLicense = pfaLicense,
                        isSubmitting = isSubmitting,
                        errorMessage = errorMessage,
                        onSubmit = {
                            isSubmitting = true
                            errorMessage = null
                            scope.launch {
                                try {
                                    val req = AdaptiveOnboardingRequest(
                                        partnerName = partnerName,
                                        email = email,
                                        password = password,
                                        restaurantName = restaurantName,
                                        address = address,
                                        city = city,
                                        sector = sector,
                                        phone = phone.filter { c -> c.isDigit() },
                                        cuisineType = cuisineType,
                                        integrationType = "APP",
                                        linkedRestaurantId = linkedRestaurantId,
                                    )
                                    val resp = RetrofitClient.restaurantApi.submitOnboarding(req)
                                    val data = resp.body()?.data
                                    if (resp.isSuccessful && data != null) {
                                        onCompleted(data.accessToken, data.expiresIn ?: 86400)
                                    } else {
                                        errorMessage = resp.errorBody()?.string()?.take(120)
                                            ?: "Registration failed. Please try again."
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Network error. Please try again."
                                }
                                isSubmitting = false
                            }
                        }
                    )
                }
            }
        }
    }
}

// ── Step Progress Header ─────────────────────────────────────────────────────

@Composable
private fun StepProgressHeader(
    currentStep: Int,
    totalSteps: Int,
    label: String,
) {
    val gradient = Brush.horizontalGradient(listOf(CibusGreen, CibusGreenDark))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = CibusDimens.radiusMd, bottomEnd = CibusDimens.radiusMd),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .background(gradient)
                .padding(horizontal = CibusDimens.screenHorizontal, vertical = CibusDimens.spacing12)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    fontSize = CibusDimens.bodySp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                Text(
                    text = "Step ${currentStep + 1} of $totalSteps",
                    fontSize = CibusDimens.captionSp,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
            Spacer(Modifier.height(CibusDimens.spacing8))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(totalSteps) { i ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (i <= currentStep) Color.White else Color.White.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

// ── Step 0: Discover ─────────────────────────────────────────────────────────

@Composable
private fun DiscoverStep(
    onSelected: (DiscoveredRestaurantDto) -> Unit,
    onManualRegister: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<DiscoveredRestaurantDto>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf<String?>(null) }

    // Debounced search via LaunchedEffect
    LaunchedEffect(query) {
        if (query.isBlank()) {
            results = emptyList()
            hasSearched = false
            return@LaunchedEffect
        }
        delay(400L)
        isSearching = true
        try {
            val response = RetrofitClient.restaurantApi.discoverRestaurants(
                query = query, sector = "", city = "Islamabad"
            )
            if (response.isSuccessful) {
                results = response.body()?.results ?: emptyList()
            }
        } catch (_: Exception) { }
        isSearching = false
        hasSearched = true
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CibusDimens.screenHorizontal, vertical = CibusDimens.spacing16)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = RestGreen, modifier = Modifier.size(28.dp))
                Text("Find Your Restaurant", fontSize = CibusDimens.titleSp, fontWeight = FontWeight.Bold, color = RestTextPrimary)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Search our database to auto-fill your restaurant details.",
                fontSize = CibusDimens.bodySp,
                color = RestTextSecondary,
            )
        }

        // Search field
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Restaurant name or area...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        results = emptyList()
                        hasSearched = false
                        selectedId = null
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CibusDimens.screenHorizontal),
            shape = RoundedCornerShape(CibusDimens.radiusMd),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RestGreen,
                cursorColor = RestGreen,
            )
        )

        Spacer(Modifier.height(CibusDimens.spacing12))

        // Results
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = CibusDimens.screenHorizontal, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing8),
        ) {
            if (isSearching) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = RestGreen, modifier = Modifier.size(28.dp))
                    }
                }
            } else if (results.isEmpty() && hasSearched) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing8),
                    ) {
                        Text("No results found", fontWeight = FontWeight.SemiBold, color = RestTextPrimary)
                        Text("Try a different name, or register manually.", fontSize = CibusDimens.bodySp, color = RestTextSecondary)
                    }
                }
            } else if (results.isEmpty() && query.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing8),
                    ) {
                        Text("Auto-fill your profile", fontWeight = FontWeight.SemiBold, color = RestTextPrimary)
                        Text(
                            "Type your restaurant name above.\nWe'll search our Islamabad database.",
                            fontSize = CibusDimens.bodySp,
                            color = RestTextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                if (results.isNotEmpty()) {
                    item {
                        Text(
                            "${results.size} result${if (results.size == 1) "" else "s"} found",
                            fontSize = CibusDimens.captionSp,
                            color = RestTextTertiary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                        )
                    }
                }
                items(results, key = { it.id }) { restaurant ->
                    DiscoverResultCard(
                        restaurant = restaurant,
                        isSelected = selectedId == restaurant.id,
                        onSelect = {
                            selectedId = restaurant.id
                            onSelected(restaurant)
                        },
                    )
                }
            }
        }

        // Skip — register manually
        TextButton(
            onClick = onManualRegister,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CibusDimens.screenHorizontal, vertical = CibusDimens.spacing8),
        ) {
            Text(
                "Skip -- register manually",
                color = RestTextSecondary,
                fontSize = CibusDimens.bodySp,
            )
        }
    }
}

@Composable
private fun DiscoverResultCard(
    restaurant: DiscoveredRestaurantDto,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    val bgColor = if (isSelected) RestGreen.copy(alpha = 0.06f) else RestCardBG
    val borderColor = if (isSelected) RestGreen else RestDivider

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CibusDimens.cardRadius))
            .border(1.dp, borderColor, RoundedCornerShape(CibusDimens.cardRadius)),
        color = bgColor,
        shape = RoundedCornerShape(CibusDimens.cardRadius),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CibusDimens.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CibusDimens.spacing12),
        ) {
            // Info
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        restaurant.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = CibusDimens.bodySp,
                        color = RestTextPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    restaurant.rating?.let { r ->
                        if (r > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                                Text(String.format("%.1f", r), fontSize = 11.sp, color = RestTextTertiary)
                            }
                        }
                    }
                }
                if (restaurant.address.isNotEmpty()) {
                    Text(restaurant.address, fontSize = CibusDimens.captionSp, color = RestTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (restaurant.cuisine.isNotEmpty()) {
                        Text(restaurant.cuisine, fontSize = 11.sp, color = RestTextTertiary)
                    }
                    if (restaurant.sector.isNotEmpty()) {
                        Text("·", fontSize = 11.sp, color = RestTextTertiary)
                        Text(restaurant.sector, fontSize = 11.sp, color = RestTextTertiary)
                    }
                }
                if (restaurant.alreadyClaimed == true) {
                    Text("Already claimed", fontSize = 10.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Medium)
                }
            }

            // Select button
            Button(
                onClick = onSelect,
                shape = RoundedCornerShape(CibusDimens.radiusSm),
                colors = ButtonDefaults.buttonColors(containerColor = RestGreen),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp),
            ) {
                Text("Select", fontSize = CibusDimens.captionSp, color = Color.White)
            }
        }
    }
}

// ── Step 1: Account & Legal Details ──────────────────────────────────────────

@Composable
private fun DetailsStep(
    restaurantName: String,
    address: String,
    cuisineType: String,
    partnerName: String,
    onPartnerNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    cnic: String,
    onCnicChange: (String) -> Unit,
    ntn: String,
    onNtnChange: (String) -> Unit,
    pfaLicense: String,
    onPfaLicenseChange: (String) -> Unit,
    errorMessage: String?,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(CibusDimens.screenHorizontal)
            .padding(top = CibusDimens.spacing16, bottom = CibusDimens.spacing24),
        verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing20),
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Your Details", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RestTextPrimary)
            Text("Create your account and provide identity info", fontSize = CibusDimens.bodySp, color = RestTextSecondary)
        }

        // Selected restaurant preview
        if (restaurantName.isNotEmpty()) {
            SelectedRestaurantPreview(restaurantName = restaurantName, address = address, cuisineType = cuisineType)
        }

        // Account section
        RestaurantSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing12)) {
                Text("ACCOUNT", fontSize = CibusDimens.labelSp, fontWeight = FontWeight.SemiBold, color = RestTextTertiary, letterSpacing = 0.5.sp)
                PartnerFormField(label = "Partner Name", value = partnerName, onValueChange = onPartnerNameChange)
                PartnerFormField(label = "Email", value = email, onValueChange = onEmailChange, keyboardType = KeyboardType.Email)
                PartnerFormField(label = "Password", value = password, onValueChange = onPasswordChange, isPassword = true)
                PartnerFormField(label = "Phone Number", value = phone, onValueChange = onPhoneChange, keyboardType = KeyboardType.Phone)
            }
        }

        // Legal section
        RestaurantSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing12)) {
                Text("LEGAL INFORMATION", fontSize = CibusDimens.labelSp, fontWeight = FontWeight.SemiBold, color = RestTextTertiary, letterSpacing = 0.5.sp)
                PartnerFormField(label = "CNIC (National ID)", value = cnic, onValueChange = onCnicChange, keyboardType = KeyboardType.Number)
                PartnerFormField(label = "NTN (Tax Number)", value = ntn, onValueChange = onNtnChange)
                PartnerFormField(label = "PFA License Number", value = pfaLicense, onValueChange = onPfaLicenseChange)
            }
        }

        // Info card
        Surface(
            shape = RoundedCornerShape(CibusDimens.radiusMd),
            color = RestGreen.copy(alpha = 0.06f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(CibusDimens.cardPadding),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = RestGreen, modifier = Modifier.size(16.dp))
                Text(
                    "Your CNIC is required for food business verification under Pakistani law. NTN and PFA license help speed up approval.",
                    fontSize = CibusDimens.captionSp,
                    color = RestTextSecondary,
                    lineHeight = 18.sp,
                )
            }
        }

        // Error
        if (errorMessage != null) {
            Text(errorMessage, fontSize = CibusDimens.captionSp, color = CibusRed)
        }

        // Next button
        RestaurantPrimaryButton(
            text = "Review & Submit",
            onClick = onNext,
            enabled = partnerName.isNotBlank() && email.contains("@") && password.length >= 6 && cnic.replace("-", "").length == 13,
        )

        Spacer(Modifier.height(CibusDimens.spacing20))
    }
}

@Composable
private fun SelectedRestaurantPreview(
    restaurantName: String,
    address: String,
    cuisineType: String,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, RestGreen.copy(alpha = 0.2f), RoundedCornerShape(CibusDimens.radiusMd)),
        shape = RoundedCornerShape(CibusDimens.radiusMd),
        color = RestGreen.copy(alpha = 0.06f),
    ) {
        Row(
            modifier = Modifier.padding(CibusDimens.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CibusDimens.spacing12),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(RestGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("🏪", fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(restaurantName, fontSize = CibusDimens.bodySp, fontWeight = FontWeight.SemiBold, color = RestTextPrimary)
                if (address.isNotEmpty()) {
                    Text(address, fontSize = CibusDimens.captionSp, color = RestTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (cuisineType.isNotEmpty()) {
                    Text(cuisineType, fontSize = CibusDimens.labelSp, color = RestTextTertiary)
                }
            }
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RestGreen, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun PartnerFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = RestGreen,
            focusedLabelColor = RestGreen,
            cursorColor = RestGreen,
        ),
    )
}

// ── Step 2: Review & Submit ──────────────────────────────────────────────────

@Composable
private fun ReviewStep(
    restaurantName: String,
    address: String,
    cuisineType: String,
    sector: String,
    city: String,
    partnerName: String,
    email: String,
    phone: String,
    cnic: String,
    ntn: String,
    pfaLicense: String,
    isSubmitting: Boolean,
    errorMessage: String?,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(CibusDimens.screenHorizontal)
            .padding(top = CibusDimens.spacing16, bottom = CibusDimens.spacing24),
        verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing20),
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Review & Submit", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = RestTextPrimary)
            Text("Confirm your details before submitting", fontSize = CibusDimens.bodySp, color = RestTextSecondary)
        }

        // Restaurant section
        ReviewSection(
            title = "Restaurant",
            items = listOf(
                "Name" to restaurantName,
                "Address" to address,
                "Cuisine" to cuisineType,
                "Area" to if (sector.isEmpty()) city else "$sector, $city",
            )
        )

        // Account section
        ReviewSection(
            title = "Account",
            items = listOf(
                "Name" to partnerName,
                "Email" to email,
                "Phone" to phone,
            )
        )

        // Legal section
        ReviewSection(
            title = "Legal",
            items = listOf(
                "CNIC" to formatCNIC(cnic),
                "NTN" to ntn.ifBlank { "Not provided" },
                "PFA" to pfaLicense.ifBlank { "Not provided" },
            )
        )

        // Error
        if (errorMessage != null) {
            Text(errorMessage, fontSize = CibusDimens.captionSp, color = CibusRed)
        }

        // Submit button
        RestaurantPrimaryButton(
            text = "Submit Application",
            onClick = onSubmit,
            isLoading = isSubmitting,
            enabled = !isSubmitting,
        )

        // Disclaimer
        Text(
            "By submitting, you confirm these details are accurate.\nVerification takes 1-3 business days.",
            fontSize = CibusDimens.captionSp,
            color = RestTextTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(CibusDimens.spacing20))
    }
}

@Composable
private fun ReviewSection(
    title: String,
    items: List<Pair<String, String>>,
) {
    RestaurantSurfaceCard {
        Column {
            Text(
                title,
                fontSize = CibusDimens.sectionTitleSp,
                fontWeight = FontWeight.SemiBold,
                color = RestTextPrimary,
                modifier = Modifier.padding(bottom = CibusDimens.spacing8),
            )
            items.forEachIndexed { idx, (label, value) ->
                if (idx > 0) {
                    HorizontalDivider(color = RestDivider, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 2.dp))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = CibusDimens.spacing8),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(label, fontSize = CibusDimens.captionSp, color = RestTextSecondary, modifier = Modifier.width(80.dp))
                    Text(
                        value,
                        fontSize = CibusDimens.bodySp,
                        color = RestTextPrimary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun formatCNIC(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    if (digits.length != 13) return raw
    return "${digits.substring(0, 5)}-${digits.substring(5, 12)}-${digits.substring(12)}"
}
