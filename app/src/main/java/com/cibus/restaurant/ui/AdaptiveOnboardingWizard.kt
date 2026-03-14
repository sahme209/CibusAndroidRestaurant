package com.cibus.restaurant.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.api.AdaptiveOnboardingRequest
import com.cibus.restaurant.api.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Constants ─────────────────────────────────────────────────────────────────

private val CUISINE_TYPES = listOf(
    "Pakistani", "Fast Food", "BBQ", "Chinese", "Italian", "Desi",
    "Biryani", "Burgers", "Pizza", "Wraps & Rolls", "Desserts",
    "Bakery", "Sushi", "Indian", "Continental", "Beverages", "Other"
)

private val ISLAMABAD_SECTORS = listOf(
    "F-6", "F-7", "F-8", "F-10", "F-11",
    "G-6", "G-7", "G-8", "G-9", "G-10", "G-11",
    "I-8", "I-9", "I-10",
    "DHA Phase 1", "DHA Phase 2",
    "Bahria Town Phase 4", "Bahria Town Phase 7",
    "Blue Area", "Gulberg", "Other"
)

private val POS_PROVIDERS = listOf("Square", "Toast", "Lightspeed", "Revel", "Foodics", "Other")
private val MENU_CATEGORIES = listOf("Starters", "Mains", "Burgers", "Pizza", "Desserts", "Drinks", "Sides", "Specials")

enum class IntegrationType(val label: String, val icon: String, val description: String) {
    APP("Cibus Restaurant App", "📱", "Receive and manage orders directly in the Cibus Restaurant app"),
    WEB("Web Dashboard", "🌐", "Manage orders from any browser at restaurant.cibus.app"),
    POS("Connect Existing POS", "🖥️", "Orders automatically pushed to your existing POS system")
}

data class OnboardingMenuItem(val name: String, val price: String, val category: String)

// ── Main Wizard ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveOnboardingWizard(
    onDismiss: () -> Unit,
    onCompleted: (accessToken: String, expiresIn: Long) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(0) }
    val totalSteps = 6

    // Step 0 — Basic info
    var restaurantName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Islamabad") }
    var sector by remember { mutableStateOf("") }
    var cuisineType by remember { mutableStateOf("Pakistani") }
    var emailAvailable by remember { mutableStateOf(true) }
    var checkingEmail by remember { mutableStateOf(false) }

    // Step 1 — Integration
    var integrationType by remember { mutableStateOf(IntegrationType.APP) }

    // Step 2 — POS config
    var posProvider by remember { mutableStateOf("Other") }
    var posApiEndpoint by remember { mutableStateOf("") }
    var posApiKey by remember { mutableStateOf("") }
    var posWebhookUrl by remember { mutableStateOf("") }

    // Step 3 — Menu
    val menuItems = remember { mutableStateListOf<OnboardingMenuItem>() }
    var newItemName by remember { mutableStateOf("") }
    var newItemPrice by remember { mutableStateOf("") }
    var newItemCategory by remember { mutableStateOf("Mains") }

    // Step 4 — Store
    var openHoursOpen by remember { mutableStateOf("09:00") }
    var openHoursClose by remember { mutableStateOf("23:00") }
    var deliveryRadius by remember { mutableStateOf(5f) }
    var kitchenPrep by remember { mutableStateOf(15f) }
    var storeAvailability by remember { mutableStateOf("open") }

    // Step 5 — Submit
    var submitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var completedAccessToken by remember { mutableStateOf<String?>(null) }
    var completedExpiresIn by remember { mutableStateOf(86400L) }
    var completedRestaurantName by remember { mutableStateOf("") }
    var completedIntegrationType by remember { mutableStateOf("APP") }
    var completedMenuCount by remember { mutableStateOf(0) }
    var completedWebUrl by remember { mutableStateOf<String?>(null) }

    val canAdvance = when (step) {
        0 -> restaurantName.isNotBlank() && ownerName.isNotBlank() && email.isNotBlank() &&
             emailAvailable && password.isNotBlank() &&
             phone.filter { it.isDigit() }.length >= 10 && address.isNotBlank()
        2 -> if (integrationType == IntegrationType.POS) posApiEndpoint.isNotBlank() && posApiKey.isNotBlank() else true
        5 -> completedAccessToken != null
        else -> true
    }

    fun handleNext() {
        if (step == totalSteps - 2) {
            // Submit
            submitting = true
            submitError = null
            step++
            scope.launch {
                try {
                    val req = AdaptiveOnboardingRequest(
                        partnerName = ownerName,
                        email = email,
                        password = password,
                        restaurantName = restaurantName,
                        address = address,
                        city = city.ifBlank { "Islamabad" },
                        sector = sector,
                        phone = phone.filter { it.isDigit() },
                        cuisineType = cuisineType,
                        integrationType = integrationType.name,
                        posProvider = if (integrationType == IntegrationType.POS) posProvider else null,
                        posApiEndpoint = if (integrationType == IntegrationType.POS) posApiEndpoint else null,
                        posApiKey = if (integrationType == IntegrationType.POS) posApiKey else null,
                        posWebhookUrl = if (integrationType == IntegrationType.POS && posWebhookUrl.isNotBlank()) posWebhookUrl else null,
                        openHours = mapOf("open" to openHoursOpen, "close" to openHoursClose),
                        deliveryRadiusKm = deliveryRadius.toInt(),
                        kitchenPrepMinutes = kitchenPrep.toInt(),
                    )
                    val resp = RetrofitClient.restaurantApi.submitOnboarding(req)
                    val data = resp.body()?.data
                    if (resp.isSuccessful && data != null) {
                        completedAccessToken = data.accessToken
                        completedExpiresIn = data.expiresIn?.toLong() ?: 86400L
                        completedRestaurantName = data.restaurantName
                        completedIntegrationType = data.integrationType
                        completedMenuCount = menuItems.size
                        completedWebUrl = data.webDashboardUrl
                    } else {
                        submitError = resp.errorBody()?.string()?.take(120) ?: "Setup failed. Please try again."
                    }
                } catch (e: Exception) {
                    submitError = e.message ?: "Network error. Please try again."
                } finally {
                    submitting = false
                }
            }
        } else if (step == totalSteps - 1) {
            completedAccessToken?.let { token ->
                onCompleted(token, completedExpiresIn)
            }
        } else {
            step++
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stepTitle(step), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (step > 0) {
                        OutlinedButton(onClick = { step-- }, modifier = Modifier.width(90.dp)) {
                            Text("Back")
                        }
                    }
                    Button(
                        onClick = ::handleNext,
                        modifier = Modifier.weight(1f),
                        enabled = canAdvance && !submitting,
                        colors = ButtonDefaults.buttonColors(containerColor = RestGreen)
                    ) {
                        if (submitting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Creating…")
                        } else {
                            Text(if (step == totalSteps - 1) "Go Live!" else if (step == totalSteps - 2) "Review & Submit" else "Continue")
                        }
                    }
                }
            }
        },
        containerColor = RestBackground
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Progress bar
            WizardProgressBar(step = step, total = totalSteps)

            // Step content
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "step_transition"
            ) { currentStep ->
                val scrollState = rememberScrollState()
                Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    when (currentStep) {
                        0 -> Step0BasicInfoContent(
                            restaurantName = restaurantName, onRestaurantNameChange = { restaurantName = it },
                            ownerName = ownerName, onOwnerNameChange = { ownerName = it },
                            email = email, onEmailChange = { v ->
                                email = v
                                scope.launch {
                                    delay(600)
                                    if (v.contains("@") && v.isNotBlank()) {
                                        checkingEmail = true
                                        try {
                                            val r = RetrofitClient.restaurantApi.checkEmailAvailable(v)
                                            emailAvailable = r.body()?.get("available") ?: true
                                        } catch (_: Exception) { emailAvailable = true }
                                        checkingEmail = false
                                    }
                                }
                            },
                            emailAvailable = emailAvailable, checkingEmail = checkingEmail,
                            password = password, onPasswordChange = { password = it },
                            phone = phone, onPhoneChange = { v ->
                                val d = v.filter { it.isDigit() }
                                if (d.length <= 11) phone = d
                            },
                            address = address, onAddressChange = { address = it },
                            city = city, onCityChange = { city = it },
                            sector = sector, onSectorChange = { sector = it },
                            cuisineType = cuisineType, onCuisineChange = { cuisineType = it },
                        )
                        1 -> Step1IntegrationTypeContent(selected = integrationType, onSelect = { integrationType = it })
                        2 -> Step2AdaptiveConfigContent(
                            type = integrationType,
                            posProvider = posProvider, onPosProviderChange = { posProvider = it },
                            posApiEndpoint = posApiEndpoint, onPosEndpointChange = { posApiEndpoint = it },
                            posApiKey = posApiKey, onPosApiKeyChange = { posApiKey = it },
                            posWebhookUrl = posWebhookUrl, onPosWebhookChange = { posWebhookUrl = it },
                        )
                        3 -> Step3MenuSetupContent(
                            items = menuItems,
                            newItemName = newItemName, onNewNameChange = { newItemName = it },
                            newItemPrice = newItemPrice, onNewPriceChange = { newItemPrice = it },
                            newItemCategory = newItemCategory, onNewCategoryChange = { newItemCategory = it },
                            onAddItem = {
                                if (newItemName.isNotBlank() && newItemPrice.isNotBlank()) {
                                    menuItems.add(OnboardingMenuItem(newItemName, newItemPrice, newItemCategory))
                                    newItemName = ""; newItemPrice = ""
                                }
                            },
                        )
                        4 -> Step4StoreStatusContent(
                            openHoursOpen = openHoursOpen, onOpenChange = { openHoursOpen = it },
                            openHoursClose = openHoursClose, onCloseChange = { openHoursClose = it },
                            deliveryRadius = deliveryRadius, onRadiusChange = { deliveryRadius = it },
                            kitchenPrep = kitchenPrep, onPrepChange = { kitchenPrep = it },
                            availability = storeAvailability, onAvailabilityChange = { storeAvailability = it },
                        )
                        else -> Step5GoLiveContent(
                            submitting = submitting,
                            error = submitError,
                            restaurantName = completedRestaurantName,
                            email = email,
                            integrationType = completedIntegrationType,
                            webUrl = completedWebUrl,
                            menuCount = completedMenuCount,
                        )
                    }
                }
            }
        }
    }
}

// ── Progress bar ──────────────────────────────────────────────────────────────

@Composable
private fun WizardProgressBar(step: Int, total: Int) {
    val progress by animateFloatAsState(
        targetValue = (step + 1f) / total,
        animationSpec = spring(),
        label = "progress"
    )
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Step ${step + 1} of $total", style = MaterialTheme.typography.labelSmall, color = RestTextSecondary)
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = RestGreen, fontWeight = FontWeight.SemiBold)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = RestGreen,
            trackColor = RestDivider,
        )
    }
}

private fun stepTitle(step: Int) = listOf("Restaurant Info", "Order System", "Configuration", "Menu Setup", "Store Hours", "Ready to Go!").getOrElse(step) { "" }

// ── Step 0: Basic Info ────────────────────────────────────────────────────────

@Composable
private fun Step0BasicInfoContent(
    restaurantName: String, onRestaurantNameChange: (String) -> Unit,
    ownerName: String, onOwnerNameChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    emailAvailable: Boolean, checkingEmail: Boolean,
    password: String, onPasswordChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    address: String, onAddressChange: (String) -> Unit,
    city: String, onCityChange: (String) -> Unit,
    sector: String, onSectorChange: (String) -> Unit,
    cuisineType: String, onCuisineChange: (String) -> Unit,
) {
    OnboardingCard(title = "Restaurant", icon = "🍽️") {
        OnboardingField("Restaurant name", restaurantName, onRestaurantNameChange)
        DropdownPickerField("Cuisine type", cuisineType, CUISINE_TYPES, onCuisineChange)
    }

    OnboardingCard(title = "Location", icon = "📍") {
        OnboardingField("Full address", address, onAddressChange)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                OnboardingField("City", city, onCityChange)
            }
            Column(modifier = Modifier.weight(1f)) {
                DropdownPickerField("Sector", sector.ifBlank { "Select" }, ISLAMABAD_SECTORS, onSectorChange)
            }
        }
    }

    OnboardingCard(title = "Contact & Account", icon = "👤") {
        OnboardingField("Your name (owner / manager)", ownerName, onOwnerNameChange)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Email", style = MaterialTheme.typography.labelSmall, color = RestTextSecondary)
            OutlinedTextField(
                value = email, onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("email@restaurant.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = email.isNotBlank() && !emailAvailable,
                trailingIcon = {
                    when {
                        checkingEmail -> CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        email.contains("@") && !checkingEmail ->
                            Icon(
                                if (emailAvailable) Icons.Default.CheckCircle else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (emailAvailable) Color(0xFF2D6A4F) else MaterialTheme.colorScheme.error
                            )
                        else -> {}
                    }
                },
                shape = RoundedCornerShape(10.dp),
            )
            if (!emailAvailable) {
                Text("Email already registered. Sign in instead.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
        }

        OnboardingField("Password", password, onPasswordChange, isPassword = true)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Phone number", style = MaterialTheme.typography.labelSmall, color = RestTextSecondary)
            OutlinedTextField(
                value = phone.chunked(4).joinToString("-").take(13),
                onValueChange = onPhoneChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("03xx-xxxxxxx") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(10.dp),
            )
        }
    }
}

// ── Step 1: Integration Type ──────────────────────────────────────────────────

@Composable
private fun Step1IntegrationTypeContent(
    selected: IntegrationType,
    onSelect: (IntegrationType) -> Unit,
) {
    Text("How do you want to receive orders?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Text("Choose how Cibus delivers new orders to you. You can change this later in Settings.", style = MaterialTheme.typography.bodySmall, color = RestTextSecondary)

    IntegrationType.entries.forEach { type ->
        val isSelected = selected == type
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { onSelect(type) },
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) Color(0xFF2D6A4F).copy(alpha = 0.06f) else Color.White,
            border = androidx.compose.foundation.BorderStroke(
                if (isSelected) 1.5.dp else 0.5.dp,
                if (isSelected) Color(0xFF2D6A4F) else RestDivider
            )
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFF2D6A4F) else Color(0xFFF0F0F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(type.icon, fontSize = 20.sp)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(type.label, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(type.description, style = MaterialTheme.typography.bodySmall, color = RestTextSecondary)
                }
                if (isSelected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2D6A4F))
                } else {
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).border(1.dp, RestDivider, CircleShape))
                }
            }
        }
    }
}

// ── Step 2: Adaptive Config ───────────────────────────────────────────────────

@Composable
private fun Step2AdaptiveConfigContent(
    type: IntegrationType,
    posProvider: String, onPosProviderChange: (String) -> Unit,
    posApiEndpoint: String, onPosEndpointChange: (String) -> Unit,
    posApiKey: String, onPosApiKeyChange: (String) -> Unit,
    posWebhookUrl: String, onPosWebhookChange: (String) -> Unit,
) {
    when (type) {
        IntegrationType.APP -> {
            OnboardingHighlightCard(icon = "📱", title = "Cibus Restaurant App", message = "Orders arrive instantly in this app with sound and vibration alerts. Make sure notifications are enabled in your device settings.")
            OnboardingCard(title = "You're all set", icon = "✅") {
                Text("No additional configuration needed. Orders will appear in the Orders tab.", style = MaterialTheme.typography.bodySmall, color = RestTextSecondary)
            }
        }
        IntegrationType.WEB -> {
            OnboardingHighlightCard(icon = "🌐", title = "Web Dashboard", message = "Manage all orders from any browser using your email and password.")
            OnboardingCard(title = "Your dashboard URL", icon = "🔗") {
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF0F8F4)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🌐", fontSize = 18.sp)
                        Text("restaurant.cibus.app", color = Color(0xFF2D6A4F), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text("Use your email and password to log in. Orders appear here in real time.", style = MaterialTheme.typography.labelSmall, color = RestTextSecondary)
            }
        }
        IntegrationType.POS -> {
            OnboardingCard(title = "POS Provider", icon = "🖥️") {
                DropdownPickerField("Select your POS", posProvider, POS_PROVIDERS, onPosProviderChange)
            }
            OnboardingCard(title = "API Configuration", icon = "🔗") {
                OnboardingField("API Endpoint URL", posApiEndpoint, onPosEndpointChange, placeholder = "https://your-pos.com/api/orders")
                OnboardingField("API Key", posApiKey, onPosApiKeyChange, placeholder = "sk_live_...", isPassword = true)
                OnboardingField("Webhook URL (optional)", posWebhookUrl, onPosWebhookChange, placeholder = "https://your-pos.com/webhooks/cibus")
            }
            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF0F0F0)) {
                Text(
                    "Cibus will POST new orders to your API endpoint as JSON with your API key in the Authorization header.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.labelSmall, color = RestTextSecondary
                )
            }
        }
    }
}

// ── Step 3: Menu Setup ────────────────────────────────────────────────────────

@Composable
private fun Step3MenuSetupContent(
    items: List<OnboardingMenuItem>,
    newItemName: String, onNewNameChange: (String) -> Unit,
    newItemPrice: String, onNewPriceChange: (String) -> Unit,
    newItemCategory: String, onNewCategoryChange: (String) -> Unit,
    onAddItem: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF2D6A4F).copy(alpha = 0.07f)) {
        Text(
            "Add a few items to get started. You can manage your full menu from the Menu tab.",
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall, color = Color(0xFF1A4A2E)
        )
    }

    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🧾", fontSize = 36.sp)
                Text("No items yet", style = MaterialTheme.typography.bodyMedium, color = RestTextSecondary)
            }
        }
    } else {
        items.forEach { item ->
            Surface(shape = RoundedCornerShape(10.dp), color = Color.White, shadowElevation = 1.dp) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(item.name, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                        Text(item.category, style = MaterialTheme.typography.labelSmall, color = RestTextSecondary)
                    }
                    Text("Rs ${item.price}", color = Color(0xFF2D6A4F), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    OnboardingCard(title = "Add Item", icon = "➕") {
        OnboardingField("Item name", newItemName, onNewNameChange)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Price (Rs)", style = MaterialTheme.typography.labelSmall, color = RestTextSecondary)
                OutlinedTextField(
                    value = newItemPrice, onValueChange = onNewPriceChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("450") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                DropdownPickerField("Category", newItemCategory, MENU_CATEGORIES, onNewCategoryChange)
            }
        }
        Button(
            onClick = onAddItem,
            modifier = Modifier.fillMaxWidth(),
            enabled = newItemName.isNotBlank() && newItemPrice.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = RestGreen)
        ) {
            Text("Add to menu")
        }
    }
}

// ── Step 4: Store Status ──────────────────────────────────────────────────────

@Composable
private fun Step4StoreStatusContent(
    openHoursOpen: String, onOpenChange: (String) -> Unit,
    openHoursClose: String, onCloseChange: (String) -> Unit,
    deliveryRadius: Float, onRadiusChange: (Float) -> Unit,
    kitchenPrep: Float, onPrepChange: (Float) -> Unit,
    availability: String, onAvailabilityChange: (String) -> Unit,
) {
    OnboardingCard(title = "Opening hours", icon = "🕐") {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Opens at", style = MaterialTheme.typography.labelSmall, color = RestTextSecondary)
                OutlinedTextField(value = openHoursOpen, onValueChange = onOpenChange, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Closes at", style = MaterialTheme.typography.labelSmall, color = RestTextSecondary)
                OutlinedTextField(value = openHoursClose, onValueChange = onCloseChange, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp))
            }
        }
    }

    OnboardingCard(title = "Delivery settings", icon = "🚴") {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Delivery radius", style = MaterialTheme.typography.bodySmall)
                    Text("${deliveryRadius.toInt()} km", color = Color(0xFF2D6A4F), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }
                Slider(value = deliveryRadius, onValueChange = onRadiusChange, valueRange = 1f..20f, steps = 18, colors = SliderDefaults.colors(thumbColor = RestGreen, activeTrackColor = RestGreen))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Kitchen prep time", style = MaterialTheme.typography.bodySmall)
                    Text("${kitchenPrep.toInt()} min", color = Color(0xFF2D6A4F), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }
                Slider(value = kitchenPrep, onValueChange = onPrepChange, valueRange = 5f..60f, steps = 10, colors = SliderDefaults.colors(thumbColor = RestGreen, activeTrackColor = RestGreen))
            }
        }
    }

    OnboardingCard(title = "Initial availability", icon = "🟢") {
        listOf("open", "closed").forEach { status ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onAvailabilityChange(status) }.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(if (status == "open") Color(0xFF2D6A4F) else RestTextSecondary))
                Text(status.replaceFirstChar { it.uppercase() }, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                if (availability == status) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2D6A4F))
            }
        }
    }
}

// ── Step 5: Go Live ───────────────────────────────────────────────────────────

@Composable
private fun Step5GoLiveContent(
    submitting: Boolean,
    error: String?,
    restaurantName: String,
    email: String,
    integrationType: String,
    webUrl: String?,
    menuCount: Int,
) {
    when {
        submitting -> {
            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularProgressIndicator(color = RestGreen)
                    Text("Setting up your restaurant…", style = MaterialTheme.typography.bodyMedium, color = RestTextSecondary)
                }
            }
        }
        error != null -> {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.errorContainer) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("⚠️", fontSize = 18.sp)
                        Text("Setup failed", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                    Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
        restaurantName.isNotBlank() -> {
            // Success
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF2D6A4F).copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2D6A4F), modifier = Modifier.size(36.dp))
                }
                Text("Your restaurant is ready!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Tap Go Live! to open your dashboard.", style = MaterialTheme.typography.bodyMedium, color = RestTextSecondary)
            }

            // Summary
            Surface(shape = RoundedCornerShape(14.dp), color = Color.White, shadowElevation = 2.dp) {
                Column {
                    SummaryRow("Restaurant", restaurantName)
                    Divider()
                    SummaryRow("Email", email)
                    Divider()
                    SummaryRow("Orders via", IntegrationType.entries.firstOrNull { it.name == integrationType }?.label ?: integrationType)
                    if (!webUrl.isNullOrBlank()) { Divider(); SummaryRow("Dashboard", webUrl) }
                    Divider()
                    SummaryRow("Menu items", "$menuCount")
                }
            }
        }
        else -> {
            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RestGreen)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = RestTextSecondary)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

// ── Shared UI components ──────────────────────────────────────────────────────

@Composable
private fun OnboardingCard(title: String, icon: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(icon, fontSize = 14.sp)
                Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, color = RestTextSecondary, letterSpacing = 0.5.sp)
            }
            content()
        }
    }
}

@Composable
private fun OnboardingHighlightCard(icon: String, title: String, message: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF2D6A4F).copy(alpha = 0.07f)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(icon, fontSize = 22.sp, modifier = Modifier.width(32.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(message, style = MaterialTheme.typography.bodySmall, color = RestTextSecondary)
            }
        }
    }
}

@Composable
private fun OnboardingField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isPassword: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = RestTextSecondary)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder.ifBlank { label }) },
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            shape = RoundedCornerShape(10.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownPickerField(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = RestTextSecondary)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(10.dp),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { opt ->
                    DropdownMenuItem(text = { Text(opt) }, onClick = { onSelect(opt); expanded = false })
                }
            }
        }
    }
}
