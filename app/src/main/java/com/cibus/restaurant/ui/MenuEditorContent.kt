package com.cibus.restaurant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.api.MenuCategoryDto
import com.cibus.restaurant.api.MenuImportRequest
import com.cibus.restaurant.api.MenuItemDto
import com.cibus.restaurant.api.MenuItemUpdateRequest
import com.cibus.restaurant.api.AddMenuItemRequest
import com.cibus.restaurant.api.MenuSuggestionResponse
import com.cibus.restaurant.api.RetrofitClient
import kotlinx.coroutines.launch

private val CibusGreen = Color(0xFF2D6A4F)

@Composable
fun MenuEditorContent(restaurantId: String) {
    var categories by remember { mutableStateOf<List<MenuCategoryDto>>(emptyList()) }
    var menuStatus by remember { mutableStateOf("pending_partner_onboarding") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var addItemCategory by remember { mutableStateOf("") }
    var showEditItemDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Pair<String, MenuItemDto>?>(null) }
    val scope = rememberCoroutineScope()

    fun loadMenu() {
        scope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.restaurantApi.getMenuTyped(restaurantId)
                if (response.isSuccessful) {
                    categories = response.body()?.categories ?: emptyList()
                    menuStatus = response.body()?.menuStatus ?: "pending_partner_onboarding"
                }
            } catch (e: Exception) {
                errorMsg = e.message
            }
            isLoading = false
        }
    }

    LaunchedEffect(restaurantId) { if (restaurantId.isNotEmpty()) loadMenu() }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Menu", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${categories.size} categories · ${categories.sumOf { it.items.size }} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showImportDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    border = ButtonDefaults.outlinedButtonBorder,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Import")
                }
            }
        }

        HorizontalDivider()

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CibusGreen)
            }
        } else if (categories.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("📋", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("No menu yet", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Import a template or add items manually.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { showImportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CibusGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("✨ Import Menu Template", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categories.forEach { category ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        category.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = CibusGreen
                                    )
                                    Text(
                                        "${category.items.size} items",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }

                                if (category.items.isNotEmpty()) {
                                    Spacer(Modifier.height(10.dp))
                                    HorizontalDivider()
                                    Spacer(Modifier.height(6.dp))
                                    category.items.forEach { item ->
                                        MenuItemRow(
                                            item = item,
                                            onEdit = {
                                                editingItem = category.name to item
                                                showEditItemDialog = true
                                            },
                                            onDelete = {
                                                scope.launch {
                                                    try {
                                                        val r = RetrofitClient.restaurantApi.deleteMenuItem(restaurantId, item.id)
                                                        if (r.isSuccessful) categories = r.body()?.categories ?: categories
                                                    } catch (_: Exception) {}
                                                }
                                            },
                                            onToggleAvailable = {
                                                scope.launch {
                                                    try {
                                                        val r = RetrofitClient.restaurantApi.updateMenuItem(
                                                            restaurantId, item.id,
                                                            MenuItemUpdateRequest(available = !item.available)
                                                        )
                                                        if (r.isSuccessful) categories = r.body()?.categories ?: categories
                                                    } catch (_: Exception) {}
                                                }
                                            }
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                TextButton(
                                    onClick = { addItemCategory = category.name; showAddItemDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = CibusGreen)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add item to ${category.name}", color = CibusGreen, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            val name = "New Category ${categories.size + 1}"
                            scope.launch {
                                try {
                                    val r = RetrofitClient.restaurantApi.addMenuItem(
                                        restaurantId,
                                        AddMenuItemRequest(categoryName = name, item = MenuItemDto(id = "", name = "Sample Item", price = 100.0))
                                    )
                                    if (r.isSuccessful) { categories = r.body()?.categories ?: categories }
                                } catch (_: Exception) {}
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add New Category")
                    }
                }
            }
        }

        errorMsg?.let { msg ->
            Snackbar(
                modifier = Modifier.padding(8.dp),
                action = { TextButton(onClick = { errorMsg = null }) { Text("Dismiss") } }
            ) { Text(msg) }
        }
    }

    // Import Dialog
    if (showImportDialog) {
        MenuImportDialog(
            restaurantId = restaurantId,
            onImported = { imported ->
                categories = imported
                showImportDialog = false
            },
            onDismiss = { showImportDialog = false }
        )
    }

    // Add Item Dialog
    if (showAddItemDialog) {
        AddMenuItemDialog(
            categoryName = addItemCategory,
            onAdd = { name, price, description ->
                scope.launch {
                    try {
                        val r = RetrofitClient.restaurantApi.addMenuItem(
                            restaurantId,
                            AddMenuItemRequest(
                                categoryName = addItemCategory,
                                item = MenuItemDto(id = "", name = name, price = price, description = description)
                            )
                        )
                        if (r.isSuccessful) categories = r.body()?.categories ?: categories
                    } catch (_: Exception) {}
                }
                showAddItemDialog = false
            },
            onDismiss = { showAddItemDialog = false }
        )
    }

    // Edit Item Dialog
    if (showEditItemDialog) {
        editingItem?.let { (catName, item) ->
            EditMenuItemDialog(
                item = item,
                onSave = { name, price, description, available ->
                    scope.launch {
                        try {
                            val r = RetrofitClient.restaurantApi.updateMenuItem(
                                restaurantId, item.id,
                                MenuItemUpdateRequest(name = name, price = price, description = description, available = available)
                            )
                            if (r.isSuccessful) categories = r.body()?.categories ?: categories
                        } catch (_: Exception) {}
                    }
                    showEditItemDialog = false
                },
                onDismiss = { showEditItemDialog = false }
            )
        }
    }
}

@Composable
private fun MenuItemRow(
    item: MenuItemDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAvailable: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = if (item.available) Color.Black else Color.Gray
                )
                if (item.isPopular) {
                    Text(
                        "Popular",
                        fontSize = 9.sp,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF59E0B))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
            if (item.description.isNotEmpty()) {
                Text(item.description, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
            }
            Text("Rs. ${item.price.toInt()}", fontSize = 12.sp, color = CibusGreen, fontWeight = FontWeight.SemiBold)
        }
        Switch(
            checked = item.available,
            onCheckedChange = { onToggleAvailable() },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CibusGreen),
            modifier = Modifier.size(40.dp, 24.dp)
        )
        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = Color.Gray)
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = Color.Red.copy(alpha = 0.7f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuImportDialog(
    restaurantId: String,
    onImported: (List<MenuCategoryDto>) -> Unit,
    onDismiss: () -> Unit,
) {
    val cuisines = listOf("Pakistani", "Fast Food", "BBQ", "Chinese", "Italian", "Desi",
        "Burgers", "Pizza", "Wraps & Rolls", "Desserts", "Bakery", "Beverages")
    var selectedCuisine by remember { mutableStateOf("Pakistani") }
    var preview by remember { mutableStateOf<List<MenuCategoryDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var replaceExisting by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedCuisine) {
        isLoading = true
        try {
            val r = RetrofitClient.restaurantApi.getMenuSuggestion(selectedCuisine)
            if (r.isSuccessful) preview = r.body()?.categories ?: emptyList()
        } catch (_: Exception) {}
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Menu Template", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Cuisine picker
                Text("Select cuisine:", style = MaterialTheme.typography.labelMedium)
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedCuisine,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        cuisines.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = { selectedCuisine = c; expanded = false }
                            )
                        }
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CibusGreen)
                } else if (preview.isNotEmpty()) {
                    val totalItems = preview.sumOf { cat -> cat.items.size }
                    Text(
                        "${preview.size} categories, $totalItems items",
                        style = MaterialTheme.typography.bodySmall,
                        color = CibusGreen
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (cat in preview.take(3)) {
                            val itemNames = cat.items.take(3).joinToString { item -> item.name }
                            Text(
                                "• ${cat.name}: $itemNames${if (cat.items.size > 3) "…" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = replaceExisting, onCheckedChange = { replaceExisting = it },
                        colors = CheckboxDefaults.colors(checkedColor = CibusGreen))
                    Text("Replace existing menu", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isImporting = true
                    scope.launch {
                        try {
                            val r = RetrofitClient.restaurantApi.importMenu(
                                restaurantId,
                                MenuImportRequest(source = "template", cuisineType = selectedCuisine, replaceExisting = replaceExisting)
                            )
                            if (r.isSuccessful) onImported(r.body()?.categories ?: preview)
                            else onImported(preview)
                        } catch (_: Exception) { onImported(preview) }
                        isImporting = false
                    }
                },
                enabled = !isImporting && preview.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = CibusGreen)
            ) {
                Text(if (isImporting) "Importing…" else "Import")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddMenuItemDialog(
    categoryName: String,
    onAdd: (String, Double, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to $categoryName", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item name") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (Rs.)") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, price.toDoubleOrNull() ?: 0.0, description) },
                enabled = name.isNotEmpty() && price.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = CibusGreen)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EditMenuItemDialog(
    item: MenuItemDto,
    onSave: (String, Double, String, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(item.name) }
    var price by remember { mutableStateOf(item.price.toInt().toString()) }
    var description by remember { mutableStateOf(item.description) }
    var available by remember { mutableStateOf(item.available) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Item", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (Rs.)") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Switch(checked = available, onCheckedChange = { available = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = CibusGreen))
                    Text("Available", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, price.toDoubleOrNull() ?: item.price, description, available) },
                colors = ButtonDefaults.buttonColors(containerColor = CibusGreen)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
