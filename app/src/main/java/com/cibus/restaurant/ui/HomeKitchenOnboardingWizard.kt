package com.cibus.restaurant.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cibus.restaurant.ResL10n
import com.cibus.restaurant.ui.theme.CibusGreen

data class SimpleMenuItem(
    val name: String,
    val price: Double,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
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
        1 -> ownerName.isNotBlank() && phone.length >= 10 && cnic.length == 13 && email.isNotBlank() && password.length >= 6
        2 -> kitchenName.isNotBlank() && address.isNotBlank() && sector.isNotBlank()
        3 -> menuItems.isNotEmpty()
        4 -> hygieneComplete
        else -> true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ResL10n.hkTitle(ctx)) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 0) {
                    OutlinedButton(onClick = { currentStep-- }) {
                        Text(ResL10n.back(ctx))
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                if (currentStep < totalSteps - 1) {
                    Button(
                        onClick = { currentStep++ },
                        enabled = canProceed,
                        colors = ButtonDefaults.buttonColors(containerColor = CibusGreen)
                    ) {
                        Text(if (isUrdu) "Agay" else "Next")
                    }
                } else {
                    Button(
                        onClick = {
                            // Submit onboarding
                            isSubmitting = true
                            error = null
                            // Call API...
                        },
                        enabled = canProceed && !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = CibusGreen)
                    ) {
                        if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isUrdu) "Shuru Karein!" else "Go Live!")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = { (currentStep + 1).toFloat() / totalSteps },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = CibusGreen
            )

            Text(
                text = "${if (isUrdu) "Qadam" else "Step"} ${currentStep + 1}/$totalSteps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (currentStep) {
                    0 -> {
                        // Welcome
                        Spacer(Modifier.height(24.dp))
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .align(Alignment.CenterHorizontally),
                            tint = CibusGreen
                        )
                        Text(
                            text = ResL10n.hkWelcome(ctx),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = ResL10n.hkSubtitle(ctx),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Benefit rows
                        listOf(
                            Icons.Default.Phone to (if (isUrdu) "Sirf phone aur CNIC chahiye" else "Just your phone and CNIC needed"),
                            Icons.Default.RestaurantMenu to (if (isUrdu) "Apna menu banayein" else "Create your own menu"),
                            Icons.Default.ShoppingBag to (if (isUrdu) "Orders milna shuru ho jayein ge" else "Start receiving orders"),
                            Icons.Default.AccountBalanceWallet to (if (isUrdu) "Roz kamayi karein" else "Earn daily")
                        ).forEach { (icon, text) ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(icon, contentDescription = null, tint = CibusGreen, modifier = Modifier.size(24.dp))
                                Text(text, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    1 -> {
                        // Personal Info
                        Text(
                            if (isUrdu) "Apni Maloomat" else "Your Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text(if (isUrdu) "Apna Naam" else "Your Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = cnic,
                            onValueChange = { cnic = it.take(13) },
                            label = { Text("CNIC") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = {
                                Text(if (isUrdu) "13 number ka CNIC \u2014 aapki safety ke liye" else "13-digit CNIC \u2014 for your safety")
                            }
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(if (isUrdu) "Password banayein" else "Create Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    2 -> {
                        // Kitchen Info
                        Text(
                            if (isUrdu) "Kitchen ki Maloomat" else "Kitchen Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = kitchenName,
                            onValueChange = { kitchenName = it },
                            label = { Text(if (isUrdu) "Kitchen ka Naam" else "Kitchen Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = kitchenDescription,
                            onValueChange = { kitchenDescription = it },
                            label = { Text(if (isUrdu) "Mukhtar Bayaan" else "Short Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text(ResL10n.address(ctx)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Sector field
                        OutlinedTextField(
                            value = sector,
                            onValueChange = { sector = it },
                            label = { Text(if (isUrdu) "Sector / Mohalla" else "Sector / Area") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    3 -> {
                        // Menu
                        Text(
                            if (isUrdu) "Apna Menu Banayein" else "Create Your Menu",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = newItemName,
                            onValueChange = { newItemName = it },
                            label = { Text(if (isUrdu) "Khane ka Naam" else "Item Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newItemPrice,
                            onValueChange = { newItemPrice = it },
                            label = { Text(if (isUrdu) "Qeemat (Rs)" else "Price (Rs)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val price = newItemPrice.toDoubleOrNull() ?: return@Button
                                menuItems = menuItems + SimpleMenuItem(newItemName, price, newItemCategory)
                                newItemName = ""; newItemPrice = ""
                            },
                            enabled = newItemName.isNotBlank() && newItemPrice.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (isUrdu) "Item Daalein" else "Add Item") }

                        menuItems.forEach { item ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontWeight = FontWeight.Bold)
                                        Text(item.category, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(
                                        "Rs ${item.price.toInt()}",
                                        fontWeight = FontWeight.Bold,
                                        color = CibusGreen
                                    )
                                    IconButton(onClick = { menuItems = menuItems - item }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                    4 -> {
                        // Hygiene
                        Text(
                            if (isUrdu) "Safai ka Checklist" else "Hygiene Checklist",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isUrdu) "Yeh sab zaroori hain" else "All required \u2014 builds customer trust",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        listOf(
                            Triple(cleanKitchen, { v: Boolean -> cleanKitchen = v }, if (isUrdu) "Kitchen saaf aur suthri hai" else "Kitchen is clean and tidy"),
                            Triple(separateStorage, { v: Boolean -> separateStorage = v }, if (isUrdu) "Khana alag rakhne ka intezam hai" else "Separate food storage available"),
                            Triple(handWashing, { v: Boolean -> handWashing = v }, if (isUrdu) "Haath dhone ka intezam hai" else "Handwashing facility available"),
                            Triple(freshIngredients, { v: Boolean -> freshIngredients = v }, if (isUrdu) "Taaza saman istamaal hota hai" else "Fresh ingredients used"),
                            Triple(properCovering, { v: Boolean -> properCovering = v }, if (isUrdu) "Khana dhak kar rakhte hain" else "Food is properly covered")
                        ).forEach { (checked, onChange, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = onChange,
                                    colors = CheckboxDefaults.colors(checkedColor = CibusGreen)
                                )
                                Text(label, modifier = Modifier.padding(start = 8.dp))
                            }
                        }

                        if (hygieneComplete) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = CibusGreen.copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CibusGreen)
                                    Text(
                                        if (isUrdu) "Sab tayaar hai!" else "All set! Ready to go live",
                                        fontWeight = FontWeight.Bold,
                                        color = CibusGreen
                                    )
                                }
                            }
                        }
                    }
                }

                error?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
