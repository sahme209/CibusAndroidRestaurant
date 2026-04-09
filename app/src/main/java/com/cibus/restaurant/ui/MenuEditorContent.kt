package com.cibus.restaurant.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.cibus.restaurant.api.MenuCategoryDto
import com.cibus.restaurant.api.MenuImportRequest
import com.cibus.restaurant.api.MenuItemDto
import com.cibus.restaurant.api.MenuItemUpdateRequest
import com.cibus.restaurant.api.AddMenuItemRequest
import com.cibus.restaurant.api.ModifierGroupDto
import com.cibus.restaurant.api.ModifierOptionDto
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MenuEditorContent(restaurantId: String, isVenueUnderReview: Boolean = false) {
    val context = LocalContext.current
    var categories by remember { mutableStateOf<List<MenuCategoryDto>>(emptyList()) }
    var menuStatus by remember { mutableStateOf("pending_partner_onboarding") }
    var menuReviewStatus by remember { mutableStateOf<String?>(null) }
    var menuReviewNote by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var availabilityFeedback by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var addItemCategory by remember { mutableStateOf("") }
    var showEditItemDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Pair<String, MenuItemDto>?>(null) }
    var isSubmittingForReview by remember { mutableStateOf(false) }
    var reviewSubmitMessage by remember { mutableStateOf<String?>(null) }
    var isImportingPhoto by remember { mutableStateOf(false) }
    var photoImportMessage by remember { mutableStateOf<String?>(null) }
    var aiImportsRemaining by remember { mutableStateOf(AIImportTracker.remainingImports(context)) }
    val scope = rememberCoroutineScope()

    // Photo picker launcher
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isImportingPhoto = true
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                if (bytes != null) {
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    val r = RetrofitClient.restaurantApi.importMenu(
                        restaurantId,
                        MenuImportRequest(
                            source = "menu_photo_ai",
                            imageBase64 = base64,
                            contentType = "image/jpeg",
                            replaceExisting = true
                        )
                    )
                    if (r.isSuccessful) {
                        categories = r.body()?.categories ?: categories
                        AIImportTracker.recordImport(context)
                        aiImportsRemaining = AIImportTracker.remainingImports(context)
                        photoImportMessage = r.body()?.message ?: "AI imported ${r.body()?.totalItems ?: 0} items!"
                    }
                }
            } catch (e: Exception) {
                photoImportMessage = "Import failed: ${e.message}"
            }
            isImportingPhoto = false
        }
    }

    fun loadMenu() {
        scope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.restaurantApi.getMenuTyped(restaurantId)
                if (response.isSuccessful) {
                    val body = response.body()
                    categories = body?.categories ?: emptyList()
                    menuStatus = body?.menuStatus ?: "pending_partner_onboarding"
                    menuReviewStatus = body?.menuReviewStatus
                    menuReviewNote = body?.menuReviewNote
                    android.util.Log.d("MenuEditor", "Loaded ${categories.size} categories for $restaurantId, status=$menuStatus")
                } else {
                    val errorBody = response.errorBody()?.string()?.take(500) ?: ""
                    android.util.Log.e("MenuEditor", "getMenu HTTP ${response.code()} for $restaurantId: $errorBody")
                    categories = emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("MenuEditor", "loadMenu failed for $restaurantId", e)
                categories = emptyList()
            }
            isLoading = false
        }
    }

    LaunchedEffect(restaurantId) { if (restaurantId.isNotEmpty()) loadMenu() }

    val totalItems = categories.sumOf { it.items.size }

    Box(modifier = Modifier.fillMaxSize().background(CibusSurfaceSecondary)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Green gradient hero header ───────────────────────────────────
            MenuHeroHeader(
                categoryCount = categories.size,
                itemCount = totalItems,
                isLoading = isLoading,
                onImportClick = { showImportDialog = true },
                onPhotoScanClick = {
                    if (AIImportTracker.remainingImports(context) > 0) {
                        photoLauncher.launch("image/*")
                    } else {
                        photoImportMessage = "You've used all 3 AI scans this month. Resets next month."
                    }
                },
                aiImportsRemaining = aiImportsRemaining,
            )

            // ── Saving indicator strip ───────────────────────────────────────
            AnimatedVisibility(
                visible = isSaving,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CibusGreen.copy(alpha = 0.08f))
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = CibusGreen
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Saving changes\u2026",
                        fontSize = CibusDimens.captionSp,
                        fontWeight = FontWeight.Medium,
                        color = CibusGreen
                    )
                }
            }

            // ── Venue under review banner ─────────────────────────────────────
            if (isVenueUnderReview) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CibusOrange.copy(alpha = 0.08f))
                        .padding(horizontal = CibusDimens.screenHorizontal, vertical = CibusDimens.spacing8),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, null, Modifier.size(16.dp), tint = CibusOrange)
                    Text(
                        "Your venue is under review. You can edit your menu now \u2014 it will go live once approved.",
                        fontSize = CibusDimens.captionSp,
                        color = CibusOrange
                    )
                }
            }

            // ── Menu review status banner ─────────────────────────────────────
            MenuReviewBanner(status = menuReviewStatus, note = menuReviewNote)

            // ── Submit for review button ──────────────────────────────────────
            if (categories.isNotEmpty() && menuReviewStatus == "changes_requested" && !isLoading) {
                Button(
                    onClick = {
                        isSubmittingForReview = true
                        scope.launch {
                            try {
                                val r = RetrofitClient.restaurantApi.patchMenu(
                                    restaurantId,
                                    mapOf(
                                        "menuCategories" to categories,
                                        "menuStatus" to "active",
                                        "menuReviewStatus" to "pending_review"
                                    )
                                )
                                if (r.isSuccessful) {
                                    menuReviewStatus = "pending_review"
                                    reviewSubmitMessage = "Menu submitted for review!"
                                } else {
                                    val errBody = r.errorBody()?.string() ?: ""
                                    android.util.Log.e("MenuEditor", "submitForReview HTTP ${r.code()}: $errBody")
                                    reviewSubmitMessage = "Failed (HTTP ${r.code()})"
                                }
                            } catch (e: Exception) {
                                reviewSubmitMessage = "Failed: ${e.message}"
                            }
                            isSubmittingForReview = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CibusDimens.screenHorizontal, vertical = CibusDimens.spacing8)
                        .height(CibusDimens.btnHeight),
                    enabled = !isSubmittingForReview,
                    colors = ButtonDefaults.buttonColors(containerColor = CibusGreen),
                    shape = RoundedCornerShape(CibusDimens.btnRadius)
                ) {
                    if (isSubmittingForReview) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Send, null, Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Re-submit for Review",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            // ── Content ──────────────────────────────────────────────────────
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CibusGreen)
                }
            } else if (categories.isEmpty()) {
                MenuEmptyState(
                    onPhotoScan = {
                        if (AIImportTracker.remainingImports(context) > 0) {
                            photoLauncher.launch("image/*")
                        } else {
                            photoImportMessage = "You've used all 3 AI scans this month. Resets next month."
                        }
                    },
                    aiImportsRemaining = aiImportsRemaining,
                    onImportTemplate = { showImportDialog = true },
                    onAddCategoryManually = {
                        val name = "New Category ${categories.size + 1}"
                        scope.launch {
                            try {
                                val r = RetrofitClient.restaurantApi.addMenuItem(
                                    restaurantId,
                                    AddMenuItemRequest(
                                        categoryName = name,
                                        item = MenuItemDto(id = "", name = "Sample Item", price = 100.0)
                                    )
                                )
                                if (r.isSuccessful) {
                                    categories = r.body()?.categories ?: categories
                                }
                            } catch (_: Exception) {}
                        }
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(CibusDimens.screenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing12)
                ) {
                    categories.forEach { category ->
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                shadowElevation = 3.dp,
                                color = CibusCardBg
                            ) {
                                Column(modifier = Modifier.padding(CibusDimens.cardPadding)) {
                                    // Category header
                                    CategoryHeader(
                                        name = category.name,
                                        itemCount = category.items.size,
                                        onDelete = {
                                            // Delete all items in category one by one
                                            scope.launch {
                                                try {
                                                    for (item in category.items) {
                                                        val r = RetrofitClient.restaurantApi.deleteMenuItem(
                                                            restaurantId, item.id
                                                        )
                                                        if (r.isSuccessful) {
                                                            categories = r.body()?.categories ?: categories
                                                        }
                                                    }
                                                } catch (_: Exception) {}
                                            }
                                        }
                                    )

                                    if (category.items.isNotEmpty()) {
                                        Spacer(Modifier.height(CibusDimens.spacing8))
                                        HorizontalDivider(
                                            thickness = CibusDimens.dividerThickness,
                                            color = CibusSurfaceSecondary
                                        )
                                        Spacer(Modifier.height(CibusDimens.spacing4))
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
                                                            val r = RetrofitClient.restaurantApi.deleteMenuItem(
                                                                restaurantId, item.id
                                                            )
                                                            if (r.isSuccessful) {
                                                                categories = r.body()?.categories ?: categories
                                                            }
                                                        } catch (_: Exception) {}
                                                    }
                                                },
                                                onToggleAvailable = {
                                                    scope.launch {
                                                        try {
                                                            val newAvail = !item.available
                                                            val r = RetrofitClient.restaurantApi.updateMenuItem(
                                                                restaurantId, item.id,
                                                                MenuItemUpdateRequest(available = newAvail)
                                                            )
                                                            if (r.isSuccessful) {
                                                                categories = r.body()?.categories ?: categories
                                                                availabilityFeedback = if (newAvail) "${item.name} is now available" else "${item.name} marked as sold out"
                                                            }
                                                        } catch (_: Exception) {
                                                            availabilityFeedback = "Failed to update availability"
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(CibusDimens.spacing8))
                                    TextButton(
                                        onClick = {
                                            addItemCategory = category.name
                                            showAddItemDialog = true
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = CibusGreen
                                        )
                                        Spacer(Modifier.width(CibusDimens.spacing4))
                                        Text(
                                            "Add item to ${category.name}",
                                            color = CibusGreen,
                                            fontSize = CibusDimens.captionSp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Daypart menus section
                    item {
                        DaypartMenuSection(categoryNames = categories.map { it.name })
                    }

                    // Add new category button
                    item {
                        OutlinedButton(
                            onClick = {
                                val name = "New Category ${categories.size + 1}"
                                scope.launch {
                                    try {
                                        val r = RetrofitClient.restaurantApi.addMenuItem(
                                            restaurantId,
                                            AddMenuItemRequest(
                                                categoryName = name,
                                                item = MenuItemDto(
                                                    id = "",
                                                    name = "Sample Item",
                                                    price = 100.0
                                                )
                                            )
                                        )
                                        if (r.isSuccessful) {
                                            categories = r.body()?.categories ?: categories
                                        }
                                    } catch (_: Exception) {}
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(CibusDimens.radiusMd)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Add New Category")
                        }
                    }
                }
            }

            // Error snackbar
            errorMsg?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(CibusDimens.spacing8),
                    action = {
                        TextButton(onClick = { errorMsg = null }) { Text("Dismiss") }
                    }
                ) { Text(msg) }
            }

            // Availability toggle feedback snackbar
            availabilityFeedback?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2500)
                    availabilityFeedback = null
                }
                Snackbar(
                    modifier = Modifier
                        .padding(CibusDimens.spacing8),
                    containerColor = CibusGreenDark,
                    action = {
                        TextButton(onClick = { availabilityFeedback = null }) {
                            Text("OK", color = Color.White)
                        }
                    }
                ) { Text(msg, color = Color.White) }
            }

            // Review submit feedback
            reviewSubmitMessage?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(3000)
                    reviewSubmitMessage = null
                }
                Snackbar(
                    modifier = Modifier.padding(CibusDimens.spacing8),
                    containerColor = CibusGreenDark,
                    action = { TextButton(onClick = { reviewSubmitMessage = null }) { Text("OK", color = Color.White) } }
                ) { Text(msg, color = Color.White) }
            }

            // Photo import feedback
            photoImportMessage?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(3000)
                    photoImportMessage = null
                }
                Snackbar(
                    modifier = Modifier.padding(CibusDimens.spacing8),
                    containerColor = CibusGreenDark,
                    action = { TextButton(onClick = { photoImportMessage = null }) { Text("OK", color = Color.White) } }
                ) { Text(msg, color = Color.White) }
            }
        }

        // Photo import loading overlay
        if (isImportingPhoto) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(CibusDimens.radiusLg),
                    color = CibusCardBg,
                    shadowElevation = 8.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(CibusDimens.spacing32),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing16)
                    ) {
                        CircularProgressIndicator(color = CibusGreen)
                        Text(
                            "AI is reading your menu\u2026",
                            fontWeight = FontWeight.Medium,
                            fontSize = CibusDimens.bodySp,
                            color = CibusTextOnSurface
                        )
                    }
                }
            }
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
                                item = MenuItemDto(
                                    id = "",
                                    name = name,
                                    price = price,
                                    description = description
                                )
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
                                MenuItemUpdateRequest(
                                    name = name,
                                    price = price,
                                    description = description,
                                    available = available
                                )
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

// ── Green Gradient Hero Header ───────────────────────────────────────────────

@Composable
private fun MenuHeroHeader(
    categoryCount: Int,
    itemCount: Int,
    isLoading: Boolean,
    onImportClick: () -> Unit,
    onPhotoScanClick: () -> Unit = {},
    aiImportsRemaining: Int = 3,
) {
    var showDropdown by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(CibusGreenDark, CibusGreen)
                )
            )
            .padding(
                start = CibusDimens.screenHorizontal,
                end = CibusDimens.screenHorizontal,
                top = CibusDimens.spacing32 + 24.dp, // account for status bar
                bottom = CibusDimens.spacing24
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    "Menu",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (!isLoading) {
                    Spacer(Modifier.height(CibusDimens.spacing4))
                    Text(
                        "$categoryCount categories \u00B7 $itemCount items",
                        fontSize = CibusDimens.captionSp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            Box {
                IconButton(onClick = { showDropdown = true }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add menu items",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Scan Menu Photo (AI) [$aiImportsRemaining/3]") },
                        leadingIcon = { Icon(Icons.Default.CameraAlt, null) },
                        onClick = { showDropdown = false; onPhotoScanClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("Import Template") },
                        leadingIcon = { Icon(Icons.Default.AutoAwesome, null) },
                        onClick = { showDropdown = false; onImportClick() }
                    )
                }
            }
        }
    }
}

// ── Menu Review Status Banner ────────────────────────────────────────────────

@Composable
private fun MenuReviewBanner(status: String?, note: String?) {
    if (status == null || status == "live") return
    val isPending = status == "pending_review"
    val color = if (isPending) CibusOrange else Color(0xFFEA580C)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = CibusDimens.screenHorizontal, vertical = CibusDimens.spacing12)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                if (isPending) Icons.Default.HourglassTop else Icons.Default.Warning,
                null, Modifier.size(16.dp), tint = color
            )
            Text(
                if (isPending) "Menu submitted for review" else "Changes requested",
                fontWeight = FontWeight.SemiBold,
                fontSize = CibusDimens.bodySp,
                color = color
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            if (isPending) "Your menu is being reviewed by our team. You can continue editing."
            else (note ?: "Our team has requested changes to your menu."),
            fontSize = CibusDimens.captionSp,
            color = CibusTextOnSurfaceSecondary
        )
    }
}

// ── AI Import Tracker (3/month limit) ────────────────────────────────────────

object AIImportTracker {
    private const val PREF_NAME = "restaurant_ai_import"
    private const val KEY_COUNT = "ai_import_count"
    private const val KEY_MONTH = "ai_import_month"
    private const val LIMIT = 3

    private fun currentMonth(): String =
        SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

    fun remainingImports(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(KEY_MONTH, "") ?: ""
        if (stored != currentMonth()) return LIMIT
        return (LIMIT - prefs.getInt(KEY_COUNT, 0)).coerceAtLeast(0)
    }

    fun recordImport(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = currentMonth()
        val stored = prefs.getString(KEY_MONTH, "") ?: ""
        if (stored != current) {
            prefs.edit().putString(KEY_MONTH, current).putInt(KEY_COUNT, 1).apply()
        } else {
            prefs.edit().putInt(KEY_COUNT, prefs.getInt(KEY_COUNT, 0) + 1).apply()
        }
    }
}

// ── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun MenuEmptyState(
    onPhotoScan: () -> Unit = {},
    aiImportsRemaining: Int = 3,
    onImportTemplate: () -> Unit,
    onAddCategoryManually: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CibusDimens.spacing32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(1f))

        // Large circular icon backdrop
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(CibusGreen.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add, // Storefront stand-in
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = CibusGreen.copy(alpha = 0.6f)
            )
        }

        Spacer(Modifier.height(CibusDimens.spacing24))

        Text(
            "No menu yet",
            fontSize = CibusDimens.headingSp,
            fontWeight = FontWeight.Bold,
            color = CibusTextOnSurface
        )
        Spacer(Modifier.height(CibusDimens.spacing8))
        Text(
            "Start by importing a template or adding items manually.",
            fontSize = CibusDimens.bodySp,
            color = CibusTextOnSurfaceSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = CibusDimens.spacing16)
        )

        Spacer(Modifier.height(CibusDimens.spacing28))

        // Primary CTA: Scan Menu Photo (AI)
        Button(
            onClick = onPhotoScan,
            modifier = Modifier
                .fillMaxWidth()
                .height(CibusDimens.btnHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (aiImportsRemaining > 0) CibusGreen else CibusTextOnSurfaceSecondary
            ),
            shape = RoundedCornerShape(CibusDimens.btnRadius)
        ) {
            Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Scan Menu Photo (AI) [$aiImportsRemaining/3]",
                fontWeight = FontWeight.SemiBold,
                fontSize = CibusDimens.bodySp,
                color = Color.White
            )
        }

        Spacer(Modifier.height(CibusDimens.spacing12))

        // Secondary CTA: Import Menu Template
        Button(
            onClick = onImportTemplate,
            modifier = Modifier
                .fillMaxWidth()
                .height(CibusDimens.btnHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = CibusGreen.copy(alpha = 0.10f),
                contentColor = CibusGreen
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(CibusDimens.btnRadius)
        ) {
            Text(
                "Import Menu Template",
                fontWeight = FontWeight.SemiBold,
                fontSize = CibusDimens.bodySp
            )
        }

        Spacer(Modifier.height(CibusDimens.spacing12))

        // Secondary: Add category manually text link
        TextButton(onClick = onAddCategoryManually) {
            Text(
                "Add category manually",
                fontSize = CibusDimens.captionSp,
                color = CibusTextOnSurfaceSecondary
            )
        }

        Spacer(Modifier.weight(1f))
    }
}

// ── Category Header ──────────────────────────────────────────────────────────

@Composable
private fun CategoryHeader(
    name: String,
    itemCount: Int,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(CibusDimens.spacing8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                name.uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = CibusDimens.labelSp,
                color = CibusGreen
            )
            Text(
                "($itemCount)",
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = CibusTextOnSurfaceSecondary
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete category",
                modifier = Modifier.size(14.dp),
                tint = CibusRed.copy(alpha = 0.6f)
            )
        }
    }
}

// ── Menu Item Row ────────────────────────────────────────────────────────────

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
            .clickable { onEdit() }
            .padding(vertical = CibusDimens.spacing8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CibusDimens.spacing12)
    ) {
        // Availability indicator bar (3dp wide)
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    if (item.available) CibusGreen
                    else CibusRed.copy(alpha = 0.6f)
                )
        )

        // Item details
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = CibusDimens.bodySp,
                    color = if (item.available) CibusTextOnSurface else CibusTextOnSurfaceSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.isPopular) {
                    Text(
                        "POPULAR",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(CibusDimens.radiusXs))
                            .background(CibusOrange)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                if (!item.available) {
                    Text(
                        "SOLD OUT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(CibusDimens.radiusXs))
                            .background(CibusRed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                if (item.modifiers.isNotEmpty()) {
                    Text(
                        "${item.modifiers.size} add-on${if (item.modifiers.size > 1) "s" else ""}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CibusGreen,
                        modifier = Modifier
                            .clip(RoundedCornerShape(CibusDimens.radiusXs))
                            .background(CibusGreen.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            if (item.description.isNotEmpty()) {
                Text(
                    item.description,
                    fontSize = CibusDimens.captionSp,
                    color = CibusTextOnSurfaceSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                "Rs. ${item.price.toInt()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = CibusGreen
            )
        }

        // Toggle switch
        Switch(
            checked = item.available,
            onCheckedChange = { onToggleAvailable() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CibusGreen
            ),
            modifier = Modifier.size(40.dp, 24.dp)
        )

        // Chevron right (edit)
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Edit",
            modifier = Modifier.size(20.dp),
            tint = CibusTextOnSurfaceSecondary
        )
    }
}

// ── Import Dialog ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuImportDialog(
    restaurantId: String,
    onImported: (List<MenuCategoryDto>) -> Unit,
    onDismiss: () -> Unit,
) {
    val cuisines = listOf(
        "Pakistani", "Fast Food", "BBQ", "Chinese", "Italian", "Desi",
        "Burgers", "Pizza", "Wraps & Rolls", "Desserts", "Bakery", "Beverages"
    )
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
        title = {
            Text("Import Menu Template", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing12)) {
                // Cuisine picker
                Text("Select cuisine:", style = MaterialTheme.typography.labelMedium)
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCuisine,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(CibusDimens.radiusSm),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CibusGreen,
                            cursorColor = CibusGreen
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        cuisines.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = { selectedCuisine = c; expanded = false }
                            )
                        }
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = CibusGreen
                    )
                } else if (preview.isNotEmpty()) {
                    val totalItems = preview.sumOf { cat -> cat.items.size }
                    Text(
                        "${preview.size} categories, $totalItems items",
                        style = MaterialTheme.typography.bodySmall,
                        color = CibusGreen
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (cat in preview.take(3)) {
                            val itemNames = cat.items.take(3).joinToString { it.name }
                            Text(
                                "\u2022 ${cat.name}: $itemNames${if (cat.items.size > 3) "\u2026" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CibusTextOnSurfaceSecondary
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = replaceExisting,
                        onCheckedChange = { replaceExisting = it },
                        colors = CheckboxDefaults.colors(checkedColor = CibusGreen)
                    )
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
                                MenuImportRequest(
                                    source = "template",
                                    cuisineType = selectedCuisine,
                                    replaceExisting = replaceExisting
                                )
                            )
                            if (r.isSuccessful) onImported(r.body()?.categories ?: preview)
                            else onImported(preview)
                        } catch (_: Exception) {
                            onImported(preview)
                        }
                        isImporting = false
                    }
                },
                enabled = !isImporting && preview.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = CibusGreen),
                shape = RoundedCornerShape(CibusDimens.btnRadius)
            ) {
                Text(if (isImporting) "Importing\u2026" else "Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CibusGreen)
            }
        }
    )
}

// ── Add Item Dialog ──────────────────────────────────────────────────────────

@Composable
private fun AddMenuItemDialog(
    categoryName: String,
    onAdd: (String, Double, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CibusGreen,
        cursorColor = CibusGreen,
        focusedLabelColor = CibusGreen
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to $categoryName", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing12)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(CibusDimens.radiusSm),
                    colors = textFieldColors
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (Rs.)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(CibusDimens.radiusSm),
                    colors = textFieldColors,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(CibusDimens.radiusSm),
                    colors = textFieldColors
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, price.toDoubleOrNull() ?: 0.0, description) },
                enabled = name.isNotEmpty() && price.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = CibusGreen),
                shape = RoundedCornerShape(CibusDimens.btnRadius)
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CibusGreen)
            }
        }
    )
}

// ── Edit Item Dialog ─────────────────────────────────────────────────────────

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
    var modifiers by remember { mutableStateOf(item.modifiers.toMutableList()) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CibusGreen,
        cursorColor = CibusGreen,
        focusedLabelColor = CibusGreen
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Item", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing12),
                modifier = Modifier.heightIn(max = 400.dp).then(Modifier.imePadding())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(CibusDimens.radiusSm),
                    colors = textFieldColors
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (Rs.)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(CibusDimens.radiusSm),
                    colors = textFieldColors,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(CibusDimens.radiusSm),
                    colors = textFieldColors
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CibusDimens.spacing8)
                ) {
                    Switch(
                        checked = available,
                        onCheckedChange = { available = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CibusGreen
                        )
                    )
                    Text("Available", style = MaterialTheme.typography.bodyMedium)
                }

                // Modifier groups section
                HorizontalDivider(color = CibusTextTertiary.copy(alpha = 0.3f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Modifier Groups", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = RestTextPrimary)
                    TextButton(onClick = {
                        modifiers = (modifiers + ModifierGroupDto(
                            id = "mg${System.currentTimeMillis()}",
                            name = "New Group",
                            required = false,
                            maxSelections = 1,
                            options = emptyList()
                        )).toMutableList()
                    }) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp), tint = CibusGreen)
                        Spacer(Modifier.width(4.dp))
                        Text("Add Group", fontSize = 12.sp, color = CibusGreen)
                    }
                }
                modifiers.forEachIndexed { gIdx, group ->
                    ModifierGroupEditor(
                        group = group,
                        onUpdate = { updated ->
                            modifiers = modifiers.toMutableList().also { it[gIdx] = updated }
                        },
                        onDelete = {
                            modifiers = modifiers.toMutableList().also { it.removeAt(gIdx) }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, price.toDoubleOrNull() ?: item.price, description, available)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CibusGreen),
                shape = RoundedCornerShape(CibusDimens.btnRadius)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CibusGreen)
            }
        }
    )
}

// ── Modifier Group Editor ─────────────────────────────────────────────────

@Composable
private fun ModifierGroupEditor(
    group: ModifierGroupDto,
    onUpdate: (ModifierGroupDto) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = CibusSurfaceSecondary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier.weight(1f).clickable { expanded = !expanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null, tint = CibusTextOnSurfaceSecondary, modifier = Modifier.size(16.dp)
                    )
                    Text(group.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = CibusTextOnSurface)
                    Text("(${group.options.size} options)", fontSize = 11.sp, color = CibusTextOnSurfaceSecondary)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, null, tint = CibusRed.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                }
            }

            if (expanded) {
                OutlinedTextField(
                    value = group.name,
                    onValueChange = { onUpdate(group.copy(name = it)) },
                    label = { Text("Group name") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CibusGreen, cursorColor = CibusGreen)
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Checkbox(
                        checked = group.required,
                        onCheckedChange = { onUpdate(group.copy(required = it)) },
                        colors = CheckboxDefaults.colors(checkedColor = CibusGreen),
                        modifier = Modifier.size(20.dp)
                    )
                    Text("Required", fontSize = 12.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("Max:", fontSize = 12.sp, color = CibusTextOnSurfaceSecondary)
                    Surface(shape = RoundedCornerShape(6.dp), color = Color.White) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (group.maxSelections > 1) onUpdate(group.copy(maxSelections = group.maxSelections - 1)) },
                                modifier = Modifier.size(24.dp)
                            ) { Text("-", fontWeight = FontWeight.Bold, color = CibusGreen) }
                            Text("${group.maxSelections}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            IconButton(
                                onClick = { onUpdate(group.copy(maxSelections = group.maxSelections + 1)) },
                                modifier = Modifier.size(24.dp)
                            ) { Text("+", fontWeight = FontWeight.Bold, color = CibusGreen) }
                        }
                    }
                }

                group.options.forEachIndexed { oIdx, option ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = option.name,
                            onValueChange = { newName ->
                                val newOpts = group.options.toMutableList()
                                newOpts[oIdx] = option.copy(name = newName)
                                onUpdate(group.copy(options = newOpts))
                            },
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodySmall,
                            shape = RoundedCornerShape(6.dp),
                            placeholder = { Text("Option name", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CibusGreen, cursorColor = CibusGreen)
                        )
                        OutlinedTextField(
                            value = if (option.price > 0) option.price.toInt().toString() else "",
                            onValueChange = { newPrice ->
                                val newOpts = group.options.toMutableList()
                                newOpts[oIdx] = option.copy(price = newPrice.toDoubleOrNull() ?: 0.0)
                                onUpdate(group.copy(options = newOpts))
                            },
                            modifier = Modifier.width(60.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            shape = RoundedCornerShape(6.dp),
                            placeholder = { Text("Rs", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CibusGreen, cursorColor = CibusGreen)
                        )
                        IconButton(
                            onClick = {
                                val newOpts = group.options.toMutableList()
                                newOpts.removeAt(oIdx)
                                onUpdate(group.copy(options = newOpts))
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = CibusRed.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
                        }
                    }
                }

                TextButton(onClick = {
                    val newOpts = group.options + ModifierOptionDto(id = "mo${System.currentTimeMillis()}", name = "", price = 0.0)
                    onUpdate(group.copy(options = newOpts))
                }) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(12.dp), tint = CibusGreen)
                    Spacer(Modifier.width(4.dp))
                    Text("Add Option", fontSize = 11.sp, color = CibusGreen)
                }
            }
        }
    }
}

// ── Daypart Menu Section ──────────────────────────────────────────────────

private data class DaypartConfig(
    val name: String,
    val startHour: Int,
    val endHour: Int,
    val enabled: Boolean = false,
    val categories: Set<String> = emptySet()
)

@Composable
fun DaypartMenuSection(categoryNames: List<String>) {
    var dayparts by remember {
        mutableStateOf(
            listOf(
                DaypartConfig("Breakfast", 7, 11),
                DaypartConfig("Lunch", 11, 15),
                DaypartConfig("Dinner", 18, 23)
            )
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 3.dp,
        color = CibusCardBg
    ) {
        Column(modifier = Modifier.padding(CibusDimens.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("DAYPART MENUS", fontWeight = FontWeight.Bold, fontSize = CibusDimens.labelSp, color = CibusGreen)
                    Text("Show different items at different times", fontSize = 11.sp, color = CibusTextOnSurfaceSecondary)
                }
                Icon(Icons.Default.Schedule, null, tint = CibusGreen, modifier = Modifier.size(20.dp))
            }

            dayparts.forEachIndexed { idx, daypart ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (daypart.enabled) CibusGreen.copy(alpha = 0.06f) else CibusSurfaceSecondary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(daypart.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CibusTextOnSurface)
                                Text("${daypart.startHour}:00 – ${daypart.endHour}:00", fontSize = 12.sp, color = CibusTextOnSurfaceSecondary)
                            }
                            Switch(
                                checked = daypart.enabled,
                                onCheckedChange = {
                                    dayparts = dayparts.toMutableList().also { list -> list[idx] = daypart.copy(enabled = it) }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CibusGreen)
                            )
                        }

                        if (daypart.enabled && categoryNames.isNotEmpty()) {
                            Text("Categories:", fontSize = 11.sp, color = CibusTextOnSurfaceSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                categoryNames.take(5).forEach { cat ->
                                    val selected = cat in daypart.categories
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            val newCats = if (selected) daypart.categories - cat else daypart.categories + cat
                                            dayparts = dayparts.toMutableList().also { list -> list[idx] = daypart.copy(categories = newCats) }
                                        },
                                        label = { Text(cat, fontSize = 10.sp) },
                                        modifier = Modifier.height(26.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CibusGreen,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
