package com.cibus.restaurant.ui

// Premium 5-step onboarding wizard for home-based food sellers.
// DoorDash Merchant-inspired: green gradient header, card-based steps, polished form fields.

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.ResL10n
import com.cibus.restaurant.ui.theme.*

data class SimpleMenuItem(
    val name: String,
    val price: Double,
    val category: String
)

@Composable
fun HomeKitchenOnboardingWizard(
    onComplete: (token: String, expiresIn: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    val isUrdu = ResL10n.isUrdu(ctx)
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 5

    // Form state
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var cnic by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var kitchenName by remember { mutableStateOf("") }
    var kitchenDescription by remember { mutableStateOf("") }
    var cuisineType by remember { mutableStateOf("Pakistani") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Islamabad") }
    var sector by remember { mutableStateOf("") }
    var menuItems by remember { mutableStateOf(listOf<SimpleMenuItem>()) }
    var newItemName by remember { mutableStateOf("") }
    var newItemPrice by remember { mutableStateOf("") }
    var newItemCategory by remember { mutableStateOf("Main Course") }
    var cleanKitchen by remember { mutableStateOf(false) }
    var separateStorage by remember { mutableStateOf(false) }
    var handWashing by remember { mutableStateOf(false) }
    var freshIngredients by remember { mutableStateOf(false) }
    var properCovering by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val hygieneComplete = cleanKitchen && separateStorage && handWashing && freshIngredients && properCovering

    val canProceed = when (currentStep) {
        0 -> true
        1 -> {
            val pd = phone.filter { it.isDigit() }
            ownerName.isNotBlank() && ((pd.length == 11 && pd.startsWith("03")) || (pd.length == 12 && pd.startsWith("92"))) && cnic.length == 13 && email.isNotBlank() && password.length >= 6
        }
        2 -> kitchenName.isNotBlank() && address.isNotBlank() && sector.isNotBlank()
        3 -> menuItems.isNotEmpty()
        4 -> hygieneComplete
        else -> true
    }

    val stepLabels = if (isUrdu)
        listOf("Khush Aamdeed", "Apni Maloomat", "Kitchen Info", "Menu", "Safai")
    else
        listOf("Welcome", "Your Details", "Kitchen Details", "Your Menu", "Hygiene")

    val cuisineTypes = listOf("Pakistani", "Desi", "Biryani", "BBQ", "Fast Food", "Chinese", "Desserts", "Bakery", "Beverages", "Other")
    val menuCategories = listOf("Main Course", "Starters", "BBQ", "Rice", "Bread", "Desserts", "Beverages", "Sides")
    val sectors = listOf("F-6", "F-7", "F-8", "F-10", "F-11", "G-6", "G-7", "G-8", "G-9", "G-10", "G-11", "I-8", "I-9", "I-10", "DHA Phase 1", "DHA Phase 2", "Bahria Town Phase 4", "Bahria Town Phase 7", "Blue Area", "Gulberg", "Other")
    val cities = listOf("Islamabad", "Rawalpindi", "Lahore", "Karachi")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleGroupedBackground)
    ) {
        // ── Green Gradient Header ──────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CibusGreen)
                .statusBarsPadding()
                .padding(horizontal = CibusDimens.spacing16)
                .padding(vertical = CibusDimens.spacing12)
        ) {
            // Nav row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (currentStep == 0) {
                    Text(
                        text = "Cancel",
                        fontSize = CibusDimens.bodySp,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.clickable { onDismiss() }
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { currentStep-- }
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft, null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text("Back", fontSize = CibusDimens.bodySp, color = Color.White)
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Step ${currentStep + 1} of $totalSteps",
                    fontSize = CibusDimens.captionSp,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }

            Spacer(Modifier.height(8.dp))

            // Step label
            Text(
                text = stepLabels[currentStep],
                fontSize = CibusDimens.bodySp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )

            Spacer(Modifier.height(10.dp))

            // Progress capsules
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 0 until totalSteps) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (i <= currentStep) Color.White
                                else Color.White.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }

        // ── Step Content ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (currentStep) {
                0 -> HKWelcomeStep(isUrdu)
                1 -> HKPersonalInfoStep(
                    isUrdu = isUrdu,
                    ownerName = ownerName, onOwnerNameChange = { ownerName = it },
                    email = email, onEmailChange = { email = it },
                    phone = phone, onPhoneChange = { phone = it },
                    password = password, onPasswordChange = { password = it },
                    cnic = cnic, onCnicChange = { cnic = it.take(13) },
                    error = error,
                )
                2 -> HKKitchenInfoStep(
                    isUrdu = isUrdu,
                    kitchenName = kitchenName, onKitchenNameChange = { kitchenName = it },
                    kitchenDescription = kitchenDescription, onDescChange = { kitchenDescription = it },
                    cuisineType = cuisineType, onCuisineChange = { cuisineType = it },
                    cuisineTypes = cuisineTypes,
                    address = address, onAddressChange = { address = it },
                    city = city, onCityChange = { city = it },
                    cities = cities,
                    sector = sector, onSectorChange = { sector = it },
                    sectors = sectors,
                )
                3 -> HKMenuStep(
                    isUrdu = isUrdu,
                    menuItems = menuItems,
                    newItemName = newItemName, onNewNameChange = { newItemName = it },
                    newItemPrice = newItemPrice, onNewPriceChange = { newItemPrice = it },
                    newItemCategory = newItemCategory, onNewCategoryChange = { newItemCategory = it },
                    menuCategories = menuCategories,
                    onAddItem = {
                        val price = newItemPrice.toDoubleOrNull() ?: return@HKMenuStep
                        menuItems = menuItems + SimpleMenuItem(newItemName, price, newItemCategory)
                        newItemName = ""; newItemPrice = ""
                    },
                    onRemoveItem = { item -> menuItems = menuItems.filter { it !== item } },
                )
                4 -> HKHygieneStep(
                    isUrdu = isUrdu,
                    cleanKitchen = cleanKitchen, onCleanChange = { cleanKitchen = it },
                    separateStorage = separateStorage, onStorageChange = { separateStorage = it },
                    handWashing = handWashing, onWashingChange = { handWashing = it },
                    freshIngredients = freshIngredients, onFreshChange = { freshIngredients = it },
                    properCovering = properCovering, onCoveringChange = { properCovering = it },
                    hygieneComplete = hygieneComplete,
                    error = error,
                )
            }

            Spacer(Modifier.height(100.dp)) // space for bottom bar
        }

        // ── Bottom Bar ─────────────────────────────────────────────────
        HorizontalDivider(color = AppleSeparator)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .padding(horizontal = CibusDimens.spacing16, vertical = CibusDimens.spacing12),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (currentStep > 0) {
                Button(
                    onClick = { currentStep-- },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(CibusDimens.btnRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CibusGreen.copy(alpha = 0.10f),
                        contentColor = CibusGreen,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                ) {
                    Text(
                        if (isUrdu) "Wapas" else "Back",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Button(
                onClick = {
                    if (currentStep < totalSteps - 1) {
                        currentStep++
                    } else {
                        // Submit
                        isSubmitting = true
                        error = null
                    }
                },
                modifier = Modifier
                    .weight(if (currentStep > 0) 1.5f else 1f)
                    .height(50.dp),
                shape = RoundedCornerShape(CibusDimens.btnRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CibusGreen,
                    disabledContainerColor = CibusGreen.copy(alpha = 0.4f),
                ),
                enabled = canProceed && !isSubmitting,
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = if (currentStep < totalSteps - 1)
                        (if (isUrdu) "Agay" else "Continue")
                    else
                        (if (isUrdu) "Shuru Karein!" else "Submit Application"),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

// ── Step 0: Welcome ────────────────────────────────────────────────────

@Composable
private fun HKWelcomeStep(isUrdu: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))

        // Icon with glow rings
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(CibusGreen.copy(alpha = 0.06f))
            )
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(CibusGreen.copy(alpha = 0.10f))
            )
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CibusGreen.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Home, null,
                    modifier = Modifier.size(28.dp),
                    tint = CibusGreen,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = if (isUrdu) "Ghar se khana becho!" else "Sell Food From Home!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AppleLabelPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isUrdu) "Bohat asaan hai — sirf 5 minute mein shuru karein" else "Super easy — get started in just 5 minutes",
            fontSize = CibusDimens.bodySp,
            color = AppleLabelSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = CibusDimens.spacing24),
        )

        Spacer(Modifier.height(28.dp))

        // Benefits card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CibusDimens.spacing16),
            shape = RoundedCornerShape(CibusDimens.cardRadius),
            color = Color.White,
            shadowElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(CibusDimens.spacing24),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "HOW IT WORKS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleLabelTertiary,
                    letterSpacing = 0.8.sp,
                )

                HKBenefitRow(Icons.Default.Phone, CibusGreen,
                    if (isUrdu) "Sirf phone aur CNIC chahiye" else "Just your phone & CNIC needed")
                HKBenefitRow(Icons.Default.RestaurantMenu, CibusOrange,
                    if (isUrdu) "Apna menu banayein" else "Create your own menu")
                HKBenefitRow(Icons.Default.ShoppingBag, CibusGreenLight,
                    if (isUrdu) "Orders milna shuru ho jayein ge" else "Start receiving orders instantly")
                HKBenefitRow(Icons.Default.AccountBalanceWallet, CibusSuccess,
                    if (isUrdu) "Roz kamayi karein" else "Earn daily with secure payouts")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Info tip
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CibusDimens.spacing16),
            shape = RoundedCornerShape(CibusDimens.radiusSm),
            color = CibusGreen.copy(alpha = 0.06f),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    Icons.Default.Info, null,
                    modifier = Modifier.size(16.dp),
                    tint = CibusGreen,
                )
                Text(
                    text = if (isUrdu)
                        "Koi commercial license ki zaroorat nahi — ghar se bina kisi mushkil ke shuru karein"
                    else
                        "No commercial license needed — start selling from your home kitchen hassle-free",
                    fontSize = CibusDimens.captionSp,
                    color = AppleLabelSecondary,
                )
            }
        }
    }
}

// ── Step 1: Personal Info ──────────────────────────────────────────────

@Composable
private fun HKPersonalInfoStep(
    isUrdu: Boolean,
    ownerName: String, onOwnerNameChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    cnic: String, onCnicChange: (String) -> Unit,
    error: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CibusDimens.spacing24),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                if (isUrdu) "Apni Maloomat" else "Your Details",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AppleLabelPrimary,
            )
            Text(
                if (isUrdu) "Hum aapki pehchaan verify karein ge" else "We'll verify your identity to keep things safe",
                fontSize = CibusDimens.bodySp,
                color = AppleLabelSecondary,
            )
        }

        // Account section
        HKSectionLabel(if (isUrdu) "ACCOUNT" else "ACCOUNT")

        HKFormField(
            label = if (isUrdu) "Apna Naam" else "Full Name",
            value = ownerName, onValueChange = onOwnerNameChange,
            icon = Icons.Default.Person,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HKFormField(
                label = "Email",
                value = email, onValueChange = onEmailChange,
                keyboardType = KeyboardType.Email,
                modifier = Modifier.weight(1f),
            )
            HKFormField(
                label = if (isUrdu) "Phone" else "Phone",
                value = phone, onValueChange = onPhoneChange,
                keyboardType = KeyboardType.Phone,
                modifier = Modifier.weight(1f),
            )
        }

        HKFormField(
            label = if (isUrdu) "Password banayein" else "Create Password",
            value = password, onValueChange = onPasswordChange,
            icon = Icons.Default.Lock,
            isPassword = true,
        )

        // Identity section
        HKSectionLabel(if (isUrdu) "PEHCHAAN" else "IDENTITY")

        HKFormField(
            label = "CNIC (13 digits)",
            value = cnic, onValueChange = onCnicChange,
            keyboardType = KeyboardType.Number,
        )

        // Info card
        Surface(
            shape = RoundedCornerShape(CibusDimens.radiusSm),
            color = CibusGreen.copy(alpha = 0.06f),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Default.Shield, null, Modifier.size(16.dp), tint = CibusGreen)
                Text(
                    text = if (isUrdu)
                        "CNIC aapki safety ke liye zaroori hai — hum isko safe rakhte hain"
                    else
                        "CNIC is required by Pakistani law for food business verification. Your data is kept secure.",
                    fontSize = CibusDimens.captionSp,
                    color = AppleLabelSecondary,
                )
            }
        }

        error?.let {
            Text(it, fontSize = CibusDimens.captionSp, color = Color(0xFFDC2626))
        }
    }
}

// ── Step 2: Kitchen Info ───────────────────────────────────────────────

@Composable
private fun HKKitchenInfoStep(
    isUrdu: Boolean,
    kitchenName: String, onKitchenNameChange: (String) -> Unit,
    kitchenDescription: String, onDescChange: (String) -> Unit,
    cuisineType: String, onCuisineChange: (String) -> Unit,
    cuisineTypes: List<String>,
    address: String, onAddressChange: (String) -> Unit,
    city: String, onCityChange: (String) -> Unit,
    cities: List<String>,
    sector: String, onSectorChange: (String) -> Unit,
    sectors: List<String>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CibusDimens.spacing24),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                if (isUrdu) "Kitchen ki Maloomat" else "Kitchen Details",
                fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppleLabelPrimary,
            )
            Text(
                if (isUrdu) "Apne kitchen ke baare mein bataayein" else "Tell us about your home kitchen",
                fontSize = CibusDimens.bodySp, color = AppleLabelSecondary,
            )
        }

        HKSectionLabel(if (isUrdu) "KITCHEN" else "KITCHEN")

        HKFormField(
            label = if (isUrdu) "Kitchen ka Naam" else "Kitchen Name",
            value = kitchenName, onValueChange = onKitchenNameChange,
            icon = Icons.Default.Storefront,
        )
        HKFormField(
            label = if (isUrdu) "Mukhtar Bayaan" else "Short Description",
            value = kitchenDescription, onValueChange = onDescChange,
        )

        // Cuisine dropdown
        HKDropdown(
            label = if (isUrdu) "Khane ki Qisam" else "Cuisine Type",
            selected = cuisineType,
            options = cuisineTypes,
            onSelect = onCuisineChange,
        )

        HKSectionLabel(if (isUrdu) "PATA" else "LOCATION")

        HKFormField(
            label = if (isUrdu) "Ghar ka Pata" else "Home Address",
            value = address, onValueChange = onAddressChange,
            icon = Icons.Default.LocationOn,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HKDropdown(
                label = if (isUrdu) "Sheher" else "City",
                selected = city,
                options = cities,
                onSelect = onCityChange,
                modifier = Modifier.weight(1f),
            )
            HKDropdown(
                label = if (isUrdu) "Sector / Mohalla" else "Sector / Area",
                selected = sector.ifEmpty { "Select" },
                options = sectors,
                onSelect = onSectorChange,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ── Step 3: Menu ───────────────────────────────────────────────────────

@Composable
private fun HKMenuStep(
    isUrdu: Boolean,
    menuItems: List<SimpleMenuItem>,
    newItemName: String, onNewNameChange: (String) -> Unit,
    newItemPrice: String, onNewPriceChange: (String) -> Unit,
    newItemCategory: String, onNewCategoryChange: (String) -> Unit,
    menuCategories: List<String>,
    onAddItem: () -> Unit,
    onRemoveItem: (SimpleMenuItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CibusDimens.spacing24),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                if (isUrdu) "Apna Menu Banayein" else "Create Your Menu",
                fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppleLabelPrimary,
            )
            Text(
                if (isUrdu) "Kam az kam 1 item daalein" else "Add at least 1 item to get started",
                fontSize = CibusDimens.bodySp, color = AppleLabelSecondary,
            )
        }

        // Add item card
        Surface(
            shape = RoundedCornerShape(CibusDimens.cardRadius),
            color = Color.White,
            shadowElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier.padding(CibusDimens.spacing24),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                HKSectionLabel(if (isUrdu) "NAYA ITEM" else "NEW ITEM")

                HKFormField(
                    label = if (isUrdu) "Khane ka Naam" else "Item Name",
                    value = newItemName, onValueChange = onNewNameChange,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HKFormField(
                        label = if (isUrdu) "Qeemat (Rs)" else "Price (Rs)",
                        value = newItemPrice, onValueChange = onNewPriceChange,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                    )
                    HKDropdown(
                        label = "Category",
                        selected = newItemCategory,
                        options = menuCategories,
                        onSelect = onNewCategoryChange,
                        modifier = Modifier.weight(1f),
                    )
                }

                // Add button
                val addEnabled = newItemName.isNotBlank() && newItemPrice.isNotBlank()
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable(enabled = addEnabled) { onAddItem() },
                    shape = RoundedCornerShape(CibusDimens.radiusSm),
                    color = if (addEnabled) CibusGreen.copy(alpha = 0.10f) else CibusGreen.copy(alpha = 0.04f),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.AddCircle, null,
                            modifier = Modifier.size(16.dp),
                            tint = if (addEnabled) CibusGreen else CibusGreen.copy(alpha = 0.4f),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isUrdu) "Item Daalein" else "Add Item",
                            fontSize = CibusDimens.bodySp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (addEnabled) CibusGreen else CibusGreen.copy(alpha = 0.4f),
                        )
                    }
                }
            }
        }

        // Menu items list
        if (menuItems.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (isUrdu) "Aapke Items" else "Your Menu",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppleLabelPrimary,
                )
                Text(
                    "${menuItems.size} ${if (menuItems.size == 1) "item" else "items"}",
                    fontSize = CibusDimens.captionSp, color = AppleLabelTertiary,
                )
            }

            Surface(
                shape = RoundedCornerShape(CibusDimens.radiusSm),
                color = Color.White,
                shadowElevation = 2.dp,
            ) {
                Column {
                    menuItems.forEachIndexed { idx, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Icon
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CibusGreen.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.Restaurant, null,
                                    modifier = Modifier.size(16.dp),
                                    tint = CibusGreen,
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.name,
                                    fontSize = CibusDimens.bodySp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppleLabelPrimary,
                                )
                                Text(
                                    item.category,
                                    fontSize = CibusDimens.captionSp,
                                    color = AppleLabelTertiary,
                                )
                            }

                            Text(
                                "Rs ${item.price.toInt()}",
                                fontSize = CibusDimens.bodySp,
                                fontWeight = FontWeight.Bold,
                                color = CibusGreen,
                            )

                            Spacer(Modifier.width(8.dp))

                            IconButton(
                                onClick = { onRemoveItem(item) },
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    Icons.Default.Cancel, null,
                                    modifier = Modifier.size(18.dp),
                                    tint = AppleLabelTertiary.copy(alpha = 0.6f),
                                )
                            }
                        }
                        if (idx < menuItems.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 68.dp),
                                color = AppleSeparator,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Step 4: Hygiene ────────────────────────────────────────────────────

@Composable
private fun HKHygieneStep(
    isUrdu: Boolean,
    cleanKitchen: Boolean, onCleanChange: (Boolean) -> Unit,
    separateStorage: Boolean, onStorageChange: (Boolean) -> Unit,
    handWashing: Boolean, onWashingChange: (Boolean) -> Unit,
    freshIngredients: Boolean, onFreshChange: (Boolean) -> Unit,
    properCovering: Boolean, onCoveringChange: (Boolean) -> Unit,
    hygieneComplete: Boolean,
    error: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CibusDimens.spacing24),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                if (isUrdu) "Safai ka Checklist" else "Hygiene Checklist",
                fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppleLabelPrimary,
            )
            Text(
                if (isUrdu) "Yeh sab zaroori hain — customers ka bharosa" else "All required — builds trust with customers",
                fontSize = CibusDimens.bodySp, color = AppleLabelSecondary,
            )
        }

        Surface(
            shape = RoundedCornerShape(CibusDimens.cardRadius),
            color = Color.White,
            shadowElevation = 4.dp,
        ) {
            Column {
                HKChecklistItem(
                    icon = Icons.Default.AutoAwesome, color = Color(0xFF3B82F6),
                    title = if (isUrdu) "Kitchen saaf aur suthri hai" else "Kitchen is clean and tidy",
                    isChecked = cleanKitchen, onCheckedChange = onCleanChange,
                )
                HorizontalDivider(modifier = Modifier.padding(start = 60.dp), color = AppleSeparator)
                HKChecklistItem(
                    icon = Icons.Default.Inventory2, color = Color(0xFF8B5CF6),
                    title = if (isUrdu) "Khana alag rakhne ka intezam hai" else "Separate food storage available",
                    isChecked = separateStorage, onCheckedChange = onStorageChange,
                )
                HorizontalDivider(modifier = Modifier.padding(start = 60.dp), color = AppleSeparator)
                HKChecklistItem(
                    icon = Icons.Default.CleanHands, color = Color(0xFF06B6D4),
                    title = if (isUrdu) "Haath dhone ka intezam hai" else "Handwashing facility available",
                    isChecked = handWashing, onCheckedChange = onWashingChange,
                )
                HorizontalDivider(modifier = Modifier.padding(start = 60.dp), color = AppleSeparator)
                HKChecklistItem(
                    icon = Icons.Default.Eco, color = Color(0xFF22C55E),
                    title = if (isUrdu) "Taaza saman istamaal hota hai" else "Fresh ingredients used",
                    isChecked = freshIngredients, onCheckedChange = onFreshChange,
                )
                HorizontalDivider(modifier = Modifier.padding(start = 60.dp), color = AppleSeparator)
                HKChecklistItem(
                    icon = Icons.Default.Shield, color = CibusOrange,
                    title = if (isUrdu) "Khana dhak kar rakhte hain" else "Food is properly covered",
                    isChecked = properCovering, onCheckedChange = onCoveringChange,
                )
            }
        }

        if (hygieneComplete) {
            Surface(
                shape = RoundedCornerShape(CibusDimens.radiusSm),
                color = CibusGreen.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, CibusGreen.copy(alpha = 0.2f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        Icons.Default.VerifiedUser, null,
                        modifier = Modifier.size(22.dp),
                        tint = CibusGreen,
                    )
                    Column {
                        Text(
                            if (isUrdu) "Sab tayaar hai!" else "All Set!",
                            fontSize = CibusDimens.bodySp,
                            fontWeight = FontWeight.Bold,
                            color = CibusGreen,
                        )
                        Text(
                            if (isUrdu) "Ab aap shuru kar sakte hain" else "Your kitchen meets our hygiene standards",
                            fontSize = CibusDimens.captionSp,
                            color = AppleLabelSecondary,
                        )
                    }
                }
            }
        }

        error?.let {
            Text(it, fontSize = CibusDimens.captionSp, color = Color(0xFFDC2626))
        }
    }
}

// ── Reusable Components ────────────────────────────────────────────────

@Composable
private fun HKBenefitRow(icon: ImageVector, color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = color)
        }
        Text(text, fontSize = CibusDimens.bodySp, color = AppleLabelPrimary)
    }
}

@Composable
private fun HKSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = AppleLabelTertiary,
        letterSpacing = 0.8.sp,
    )
}

@Composable
private fun HKFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            label,
            fontSize = CibusDimens.captionSp,
            color = AppleLabelSecondary,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(CibusDimens.radiusSm),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            leadingIcon = icon?.let {
                { Icon(it, null, Modifier.size(18.dp), tint = AppleLabelTertiary) }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CibusGreen,
                unfocusedBorderColor = AppleSeparator,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = CibusDimens.bodySp,
                color = AppleLabelPrimary,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HKDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, fontSize = CibusDimens.captionSp, color = AppleLabelSecondary)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(CibusDimens.radiusSm),
                color = Color.White,
                border = BorderStroke(1.dp, AppleSeparator),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        selected,
                        fontSize = CibusDimens.bodySp,
                        color = if (selected == "Select") AppleLabelTertiary else AppleLabelPrimary,
                    )
                    Icon(
                        Icons.Default.ExpandMore, null,
                        modifier = Modifier.size(18.dp),
                        tint = AppleLabelTertiary,
                    )
                }
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(option, color = AppleLabelPrimary)
                                if (option == selected) {
                                    Icon(
                                        Icons.Default.Check, null,
                                        modifier = Modifier.size(16.dp),
                                        tint = CibusGreen,
                                    )
                                }
                            }
                        },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HKChecklistItem(
    icon: ImageVector,
    color: Color,
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val checkBg by animateColorAsState(
        if (isChecked) CibusGreen else Color.Transparent,
        label = "checkBg"
    )
    val checkBorder by animateColorAsState(
        if (isChecked) CibusGreen else AppleSeparator,
        label = "checkBorder"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = color)
        }

        Spacer(Modifier.width(14.dp))

        Text(
            title,
            fontSize = CibusDimens.bodySp,
            color = AppleLabelPrimary,
            modifier = Modifier.weight(1f),
        )

        Spacer(Modifier.width(8.dp))

        // Custom checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(checkBg)
                .then(
                    if (!isChecked) Modifier.background(Color.Transparent)
                    else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (!isChecked) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .then(
                            Modifier.background(Color.Transparent)
                        )
                ) {
                    // Border drawn via Surface
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Transparent,
                        border = BorderStroke(2.dp, checkBorder
                        ),
                    ) {}
                }
            } else {
                Icon(
                    Icons.Default.Check, null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White,
                )
            }
        }
    }
}
