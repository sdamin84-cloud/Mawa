package com.example.mawa.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.local.entity.FordiItemEntity
import com.example.mawa.data.local.entity.ProductEntity
import com.example.mawa.data.model.AppThemeMode
import com.example.mawa.ui.components.BarcodeScannerDialog
import com.example.mawa.ui.components.MawaEmptyState
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliFordiParser
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.DataBackupRestoreManager
import com.example.mawa.util.FordiQuickProduct
import com.example.mawa.util.FordiSuggestionManager
import com.example.mawa.util.FordiTemplate
import com.example.mawa.util.FordiTemplateManager
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialPositiveContainer
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.MawaPrimary

enum class FordiTab {
    CURRENT, // বর্তমান ফর্দ
    HISTORY  // আগে করা সব ফর্দ
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FordiScreen(
    viewModel: MawaViewModel,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null,
    onOpenProducts: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val allFordiItems by viewModel.allFordiItems.collectAsStateWithLifecycle()
    val activeProducts by viewModel.activeProducts.collectAsStateWithLifecycle()
    val shopSettings by viewModel.shopSettings.collectAsStateWithLifecycle()

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    val isDarkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    // Professional Designer Color Tokens for Fordi (Market/Grocery)
    // Cohesive White & Mint Theme (সাদা-মিন্ট) for Light, Deep Midnight Forest for Dark
    val fCanvasBg = if (isDarkTheme) Color(0xFF0F1713) else Color(0xFFF6FAF7)
    val fSurface = if (isDarkTheme) Color(0xFF16241D) else Color(0xFFFFFFFF)
    val fSurfaceSubtle = if (isDarkTheme) Color(0xFF1D3027) else Color(0xFFF1F8F4)
    val fMintContainer = if (isDarkTheme) Color(0xFF1A3828) else Color(0xFFE5F5EC)
    val fMintBorder = if (isDarkTheme) Color(0xFF2B4435) else Color(0xFFCCE8D7)
    val fEmerald = if (isDarkTheme) Color(0xFF38D385) else Color(0xFF0F8A4B)
    val fDeepEmerald = if (isDarkTheme) Color(0xFF90F0BC) else Color(0xFF095E32)
    val fTextPrimary = if (isDarkTheme) Color(0xFFEDF7F1) else Color(0xFF142B1F)
    val fTextSecondary = if (isDarkTheme) Color(0xFFA2C2B0) else Color(0xFF4A6B58)
    val fTextPlaceholder = if (isDarkTheme) Color(0xFF6F917E) else Color(0xFF7D9E8D)
    val fChipBg = if (isDarkTheme) Color(0xFF18281F) else Color(0xFFFFFFFF)
    val fChipBorder = if (isDarkTheme) Color(0xFF2A4535) else Color(0xFFCCE4D4)
    val fProgressTrack = if (isDarkTheme) Color(0xFF1D3027) else Color(0xFFE2EFE6)
    val fCheckedBg = if (isDarkTheme) Color(0xFF121E18) else Color(0xFFF3FAF6)
    val fCheckedBorder = if (isDarkTheme) Color(0xFF1F3327) else Color(0xFFD6EBDF)

    var activeTab by remember { mutableStateOf(FordiTab.CURRENT) }
    var rawInputText by remember { mutableStateOf("") }
    var shoppingMode by remember { mutableStateOf(false) }

    // Dialog & Sheet States
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showSaveTemplateDialog by remember { mutableStateOf(false) }
    var showTemplatesSheet by remember { mutableStateOf(false) }
    var showCatalogSheet by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var showVoiceHelperDialog by remember { mutableStateOf(false) }
    var showFinishShoppingDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<FordiItemEntity?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    // Budget Persistence in SharedPreferences
    val prefs = remember { context.getSharedPreferences("mawa_fordi_budget_pref", Context.MODE_PRIVATE) }
    var currentBudget by remember { mutableStateOf(prefs.getFloat("current_budget", 0f).toDouble()) }

    val pendingItems = remember(allFordiItems) { allFordiItems.filter { !it.isPurchased } }
    val purchasedItems = remember(allFordiItems) { allFordiItems.filter { it.isPurchased } }

    // Checked IDs (Selection)
    var checkedItemIds by remember { mutableStateOf(setOf<Long>()) }
    val validCheckedIds = remember(pendingItems, checkedItemIds) {
        val pendingIds = pendingItems.map { it.id }.toSet()
        checkedItemIds.filter { pendingIds.contains(it) }.toSet()
    }
    val checkedItems = remember(pendingItems, validCheckedIds) {
        pendingItems.filter { validCheckedIds.contains(it.id) }
    }
    val uncheckedItems = remember(pendingItems, validCheckedIds) {
        pendingItems.filter { !validCheckedIds.contains(it.id) }
    }

    // Totals
    val totalPendingAmount = remember(pendingItems) {
        pendingItems.sumOf { it.plannedQuantity * it.purchaseRate }
    }
    val checkedTotalAmount = remember(checkedItems) {
        checkedItems.sumOf { it.plannedQuantity * it.purchaseRate }
    }
    val uncheckedTotalAmount = remember(uncheckedItems) {
        uncheckedItems.sumOf { it.plannedQuantity * it.purchaseRate }
    }

    // Progress percentage
    val progressPercent = if (pendingItems.isNotEmpty()) {
        ((checkedItems.size.toFloat() / pendingItems.size.toFloat()) * 100).toInt()
    } else 0
    val progressFraction = if (pendingItems.isNotEmpty()) {
        (checkedItems.size.toFloat() / pendingItems.size.toFloat()).coerceIn(0f, 1f)
    } else 0f

    // Suggestions based on history & typing
    var quickAddList by remember { mutableStateOf(FordiSuggestionManager.getQuickAddSuggestions(context)) }
    val dynamicSuggestions = remember(rawInputText, activeProducts) {
        if (rawInputText.isBlank()) emptyList()
        else FordiSuggestionManager.filterSuggestions(context, rawInputText, activeProducts)
    }

    // Helper: Add item to Fordi and record suggestion
    val handleAddItem: (name: String, qty: Double, unit: String, rate: Double, productId: Long?) -> Unit = { name, qty, unit, rate, prodId ->
        viewModel.addFordiItem(
            productName = name,
            productId = prodId,
            plannedQty = qty,
            unit = unit,
            purchaseRate = rate,
            sellingRate = 0.0
        )
        FordiSuggestionManager.recordItemAdded(
            context = context,
            productName = name,
            unit = unit,
            rate = rate,
            productId = prodId
        )
        quickAddList = FordiSuggestionManager.getQuickAddSuggestions(context)
        rawInputText = ""
    }

    // Helper: Parse text and add
    val handleParseAndAdd: () -> Unit = {
        if (rawInputText.isNotBlank()) {
            val parsed = BengaliFordiParser.parse(rawInputText)
            if (parsed.productName.isNotBlank()) {
                handleAddItem(
                    parsed.productName,
                    parsed.quantity,
                    parsed.unit,
                    parsed.estimatedRate,
                    null
                )
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            // Modern Top Bar with White & Mint harmonious styling
            Surface(
                color = fSurface,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, fMintBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onOpenDrawer?.invoke() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "মেনু বা ফিরে যান",
                                tint = fTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "আজকের বাজার",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = fTextPrimary
                            )
                            if (shoppingMode) {
                                Text(
                                    text = "🛒 শピング মোড সক্রিয়",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = fEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 1-Tap Quick Theme Toggle: Switch between White-Mint and Dark Mode
                        Surface(
                            shape = CircleShape,
                            color = if (isDarkTheme) fMintContainer else Color(0xFFEAF5EE),
                            border = BorderStroke(1.dp, fMintBorder),
                            modifier = Modifier.clickable {
                                viewModel.setThemeMode(if (isDarkTheme) AppThemeMode.LIGHT else AppThemeMode.DARK)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = if (isDarkTheme) "সাদা-মিন্ট মোড" else "ডার্ক মোড",
                                    tint = fEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isDarkTheme) "সাদা-মিন্ট" else "ডার্ক",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = fEmerald
                                )
                            }
                        }

                        // Shopping Mode / Cart Toggle
                        IconButton(onClick = { shoppingMode = !shoppingMode }) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "শপিং মোড",
                                tint = if (shoppingMode) fEmerald else fTextSecondary
                            )
                        }

                        // Share Action
                        IconButton(onClick = {
                            val shopName = shopSettings?.shopName?.ifBlank { "মাওয়া স্মার্ট খাতা" } ?: "মাওয়া স্মার্ট খাতা"
                            val bitmap = DataBackupRestoreManager.createFordiMemoBitmap(shopName = shopName, items = pendingItems)
                            DataBackupRestoreManager.shareBitmapAsImage(
                                context = context,
                                bitmap = bitmap,
                                fileNamePrefix = "fordi_${System.currentTimeMillis()}",
                                title = "ক্রয় ফর্দ মেমো"
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "শেয়ার করুন",
                                tint = fTextSecondary
                            )
                        }

                        // 3-Dots Menu
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "মেনু",
                                    tint = fTextSecondary
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = null,
                                            tint = fEmerald,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = {
                                        Text(
                                            if (isDarkTheme) "সাদা-মিন্ট মোড চালু করুন" else "ডার্ক মোড চালু করুন",
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    onClick = {
                                        viewModel.setThemeMode(if (isDarkTheme) AppThemeMode.LIGHT else AppThemeMode.DARK)
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingCart,
                                            contentDescription = null,
                                            tint = fTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = { Text(if (shoppingMode) "সাধারণ তালিকা মোড" else "শপিং কার্ট মোড") },
                                    onClick = {
                                        shoppingMode = !shoppingMode
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Paid,
                                            contentDescription = null,
                                            tint = fTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = { Text("বাজেট পরিবর্তন করুন") },
                                    onClick = {
                                        showBudgetDialog = true
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Inventory2,
                                            contentDescription = null,
                                            tint = fEmerald,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = { Text("পণ্য ও স্টক খাতা খুলুন") },
                                    onClick = {
                                        showMenu = false
                                        onOpenProducts?.invoke()
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.BookmarkAdd,
                                            contentDescription = null,
                                            tint = fTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = { Text("টেমপ্লেট হিসেবে সংরক্ষণ") },
                                    onClick = {
                                        if (pendingItems.isEmpty()) {
                                            Toast.makeText(context, "সংরক্ষণের জন্য কোনো পণ্য নেই", Toast.LENGTH_SHORT).show()
                                        } else {
                                            showSaveTemplateDialog = true
                                        }
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Bookmarks,
                                            contentDescription = null,
                                            tint = fTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = { Text("সংরক্ষিত ফর্দ টেমপ্লেটসমূহ") },
                                    onClick = {
                                        showTemplatesSheet = true
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = null,
                                            tint = fTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = { Text("কেনা আইটেমগুলো ক্লিয়ার করুন") },
                                    onClick = {
                                        viewModel.clearCompletedFordi()
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DeleteSweep,
                                            contentDescription = null,
                                            tint = FinancialNegative,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = { Text("সম্পূর্ণ ফর্দ খালি করুন", color = FinancialNegative) },
                                    onClick = {
                                        showClearConfirmDialog = true
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (pendingItems.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
                ) {
                    // Lock / Bag Quick Button
                    Surface(
                        shape = CircleShape,
                        color = fSurface,
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, fMintBorder),
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { shoppingMode = !shoppingMode }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (shoppingMode) Icons.Default.Lock else Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = if (shoppingMode) fEmerald else fTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Green Pill Finish Shopping Button (Screenshot 1 & 3: "✓ বাজার সম্পন্ন করুন")
                    Button(
                        onClick = { showFinishShoppingDialog = true },
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = fEmerald),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                        modifier = Modifier.testTag("finish_shopping_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "বাজার সম্পন্ন করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(fCanvasBg) // Designer cohesive Mint/White or Midnight Forest canvas
        ) {
            // TAB SELECTOR: [ বর্তমান ফর্দ ] [ পূর্বের ইতিহাস ]
            item {
                TabRow(
                    selectedTabIndex = activeTab.ordinal,
                    containerColor = fSurface,
                    contentColor = fEmerald,
                    divider = { HorizontalDivider(thickness = 1.dp, color = fMintBorder) }
                ) {
                    Tab(
                        selected = activeTab == FordiTab.CURRENT,
                        onClick = { activeTab = FordiTab.CURRENT },
                        text = {
                            Text(
                                text = "বর্তমান ফর্দ (${BengaliUtils.toBanglaDigits(pendingItems.size.toLong())})",
                                fontWeight = if (activeTab == FordiTab.CURRENT) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeTab == FordiTab.CURRENT) fEmerald else fTextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = activeTab == FordiTab.HISTORY,
                        onClick = { activeTab = FordiTab.HISTORY },
                        text = {
                            Text(
                                text = "আগে করা সব ফর্দ (${BengaliUtils.toBanglaDigits(purchasedItems.size.toLong())})",
                                fontWeight = if (activeTab == FordiTab.HISTORY) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeTab == FordiTab.HISTORY) fEmerald else fTextSecondary
                            )
                        }
                    )
                }
            }

            if (activeTab == FordiTab.CURRENT) {
                // ==========================================
                // 1. TOP PROGRESS & BUDGET CARD (Screenshots 1 & 3)
                // ==========================================
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = fSurface),
                        border = BorderStroke(1.dp, fMintBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Top Row: Check circle + progress text & + বাজেট দিন button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = fEmerald,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${BengaliUtils.toBanglaDigits(checkedItems.size.toLong())}/${BengaliUtils.toBanglaDigits(pendingItems.size.toLong())} টি টিক করা (${BengaliUtils.toBanglaDigits(progressPercent.toLong())}%)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = fEmerald
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.Transparent,
                                    border = BorderStroke(1.dp, fEmerald.copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable { showBudgetDialog = true }
                                ) {
                                    Text(
                                        text = if (currentBudget > 0) "বাজেট: ${BengaliUtils.formatTaka(currentBudget)}" else "+ বাজেট দিন",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = fEmerald,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Smooth Rounded Progress Indicator
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = fEmerald,
                                trackColor = fProgressTrack
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Row of 2 Cards: [ টিক মার্ক করা মোট ] [ বাকি কেনার মোট ]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Card 1: টিক মার্ক করা মোট
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = fMintContainer)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "✓ টিক মার্ক করা মোট",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = fDeepEmerald
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = BengaliUtils.formatTaka(checkedTotalAmount),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = fDeepEmerald
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${BengaliUtils.toBanglaDigits(checkedItems.size.toLong())} টি পণ্য",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = fDeepEmerald.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                // Card 2: বাকি কেনার মোট
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = fSurfaceSubtle)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "বাকি কেনার মোট",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = fTextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = BengaliUtils.formatTaka(uncheckedTotalAmount),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = fTextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${BengaliUtils.toBanglaDigits(uncheckedItems.size.toLong())} টি পণ্য",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = fTextSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Bottom Summary Line: ফর্দের মোট: ৳... | বাজেট অবস্থা
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ফর্দের মোট: ${BengaliUtils.formatTaka(totalPendingAmount)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = fTextPrimary
                                )

                                if (currentBudget > 0) {
                                    val remainingBudget = currentBudget - totalPendingAmount
                                    val isOverBudget = remainingBudget < 0
                                    Text(
                                        text = if (isOverBudget) "বাজেটের অতিরিক্ত: ${BengaliUtils.formatTaka(-remainingBudget)}"
                                               else "বাজেট অবশিষ্ট: ${BengaliUtils.formatTaka(remainingBudget)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOverBudget) FinancialNegative else fEmerald
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 2. QUICK SMART INPUT FIELD (Screenshots 1, 2, 3)
                // ==========================================
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = fSurface),
                        border = BorderStroke(1.dp, fMintBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Grid Icon (Opens Catalog Picker)
                            IconButton(onClick = { showCatalogSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = "ক্যাটালগ থেকে পছন্দ",
                                    tint = fEmerald,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Center: TextField with Bengali / English Natural Input
                            OutlinedTextField(
                                value = rawInputText,
                                onValueChange = { rawInputText = it },
                                placeholder = {
                                    Text(
                                        text = "যেমন: ৫ কেজি চাল, ১২টি ডিম, মাছ ২০০...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = fTextPlaceholder
                                    )
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    focusedTextColor = fTextPrimary,
                                    unfocusedTextColor = fTextPrimary
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { handleParseAndAdd() }),
                                trailingIcon = {
                                    if (rawInputText.isNotEmpty()) {
                                        IconButton(onClick = { rawInputText = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "মুছুন",
                                                tint = fTextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("fordi_smart_input_box")
                            )

                            // Mic Icon (Voice Input Helper)
                            IconButton(onClick = { showVoiceHelperDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "ভয়েস ইনপুট",
                                    tint = fEmerald,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Right: Prominent Deep Emerald Plus Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = fEmerald,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { handleParseAndAdd() }
                                    .testTag("smart_add_fordi_btn")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "যোগ করুন",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 3. DYNAMIC SUGGESTIONS AS USER TYPES (Screenshot 1)
                // ==========================================
                if (rawInputText.isNotBlank()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = fEmerald,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "পণ্য তালিকা বা সরাসরি নির্বাচন করুন:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = fEmerald
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Direct option with parsed preview
                                item {
                                    val parsed = BengaliFordiParser.parse(rawInputText)
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = fMintContainer,
                                        border = BorderStroke(1.dp, fEmerald),
                                        modifier = Modifier.clickable { handleParseAndAdd() }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "+ সরাসরি: '${parsed.productName}' (${BengaliUtils.formatQuantity(parsed.quantity, parsed.unit)})",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = fDeepEmerald
                                            )
                                        }
                                    }
                                }

                                // Matched Suggestions
                                items(dynamicSuggestions) { item ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = fSurface,
                                        border = BorderStroke(1.dp, fChipBorder),
                                        modifier = Modifier.clickable {
                                            handleAddItem(
                                                item.productName,
                                                1.0,
                                                item.unit,
                                                item.defaultRate,
                                                item.productId
                                            )
                                        }
                                    ) {
                                        Text(
                                            text = "${item.productName} • ${if (item.defaultRate > 0) "৳" + BengaliUtils.toBanglaDigits(item.defaultRate.toLong()) + "/" + item.unit else item.unit}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = fTextPrimary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 4. QUICK ADD HORIZONTAL CAROUSEL ("দ্রুত যোগ করুন" & "সব ক্যাটালগ →") (Screenshot 1 & 3)
                // ==========================================
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "দ্রুত যোগ করুন",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = fTextPrimary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Direct Stock & Products Sidebar Connection
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = fSurfaceSubtle,
                                    border = BorderStroke(1.dp, fMintBorder),
                                    modifier = Modifier.clickable { onOpenProducts?.invoke() }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Inventory2,
                                            contentDescription = null,
                                            tint = fEmerald,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "স্টক খাতা",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = fEmerald
                                        )
                                    }
                                }

                                // Direct Template Access Pill
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = fMintContainer,
                                    border = BorderStroke(1.dp, fEmerald.copy(alpha = 0.3f)),
                                    modifier = Modifier.clickable { showTemplatesSheet = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bookmarks,
                                            contentDescription = null,
                                            tint = fEmerald,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "টেমপ্লেট",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = fDeepEmerald
                                        )
                                    }
                                }

                                // Save current Fordi as template if items exist
                                if (pendingItems.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = fSurfaceSubtle,
                                        border = BorderStroke(1.dp, fMintBorder),
                                        modifier = Modifier.clickable { showSaveTemplateDialog = true }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.BookmarkAdd,
                                                contentDescription = null,
                                                tint = fTextSecondary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "সেভ",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = fTextSecondary
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "ক্যাটালগ →",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = fEmerald,
                                    modifier = Modifier
                                        .clickable { showCatalogSheet = true }
                                        .padding(4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quickAddList, key = { it.productName }) { item ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = fSurface,
                                    border = BorderStroke(1.dp, fChipBorder),
                                    modifier = Modifier.clickable {
                                        handleAddItem(
                                            item.productName,
                                            1.0,
                                            item.unit,
                                            item.defaultRate,
                                            item.productId
                                        )
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "+ ${item.productName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = fTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 5. UNCHECKED ITEMS ("কেনা বাকি") (Screenshot 1 & 3)
                // ==========================================
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "কেনা বাকি (${BengaliUtils.toBanglaDigits(uncheckedItems.size.toLong())} টি পণ্য)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = fTextPrimary
                        )

                        Text(
                            text = BengaliUtils.formatTaka(uncheckedTotalAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = fTextPrimary
                        )
                    }
                }

                if (pendingItems.isEmpty()) {
                    item {
                        MawaEmptyState(
                            icon = Icons.Default.Assignment,
                            title = "বর্তমান ফর্দে কোনো পণ্য নেই",
                            subtitle = "উপরের ইনপুট বক্সে লিখুন (যেমন: ৫ কেজি চাল, মাছ ২০০) অথবা দ্রুত যোগ করুন বা টেমপ্লেট লোড করুন",
                            actionLabel = "টেমপ্লেট থেকে লোড করুন",
                            onActionClick = { showTemplatesSheet = true }
                        )
                    }
                } else if (uncheckedItems.isEmpty() && checkedItems.isNotEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = fMintContainer,
                            border = BorderStroke(1.dp, fEmerald.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = fEmerald,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "সব পণ্য কেনা হয়ে গেছে!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = fDeepEmerald
                                    )
                                    Text(
                                        text = "নিচের 'বাজার সম্পন্ন করুন' বাটনে ক্লিক করে খরচে স্থানান্তর করুন",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = fTextSecondary
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(uncheckedItems, key = { it.id }) { item ->
                        val lineTotal = item.plannedQuantity * item.purchaseRate

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = fSurface),
                            border = BorderStroke(1.dp, fMintBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Large Checkbox on the Left
                                Checkbox(
                                    checked = false,
                                    onCheckedChange = { checked ->
                                        if (checked) checkedItemIds = checkedItemIds + item.id
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = fEmerald,
                                        uncheckedColor = fTextSecondary.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier.testTag("check_fordi_${item.id}")
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                // Product Info
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.productName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = fTextPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Quantity Pill
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = fSurfaceSubtle
                                        ) {
                                            Text(
                                                text = BengaliUtils.formatQuantity(item.plannedQuantity, item.unit),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = fTextSecondary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }

                                        // Rate / Total
                                        if (item.purchaseRate > 0) {
                                            Text(
                                                text = BengaliUtils.formatTaka(lineTotal),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = fTextPrimary
                                            )
                                        }
                                    }
                                }

                                // Actions: Edit & Delete
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { itemToEdit = item },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "এডিট",
                                            tint = fTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteFordiItem(item.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "মুছুন",
                                            tint = FinancialNegative.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 6. CHECKED ITEMS ("কেনা সম্পন্ন")
                // ==========================================
                if (checkedItems.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = fEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "কেনা হয়েছে (${BengaliUtils.toBanglaDigits(checkedItems.size.toLong())} টি)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = fEmerald
                                )
                            }

                            Text(
                                text = BengaliUtils.formatTaka(checkedTotalAmount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = fEmerald
                            )
                        }
                    }

                    items(checkedItems, key = { it.id }) { item ->
                        val lineTotal = item.plannedQuantity * item.purchaseRate

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = fCheckedBg),
                            border = BorderStroke(1.dp, fCheckedBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Green Checkbox Checked
                                Checkbox(
                                    checked = true,
                                    onCheckedChange = { checked ->
                                        if (!checked) checkedItemIds = checkedItemIds - item.id
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = fEmerald,
                                        uncheckedColor = fTextSecondary.copy(alpha = 0.5f)
                                    )
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.productName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = TextDecoration.LineThrough,
                                        color = fTextSecondary.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = BengaliUtils.formatQuantity(item.plannedQuantity, item.unit),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = fTextSecondary
                                        )

                                        if (item.purchaseRate > 0) {
                                            Text(
                                                text = BengaliUtils.formatTaka(lineTotal),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = fEmerald
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.deleteFordiItem(item.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "মুছুন",
                                        tint = fTextSecondary.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Spacing for Floating Action Button
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            } else {
                // ==========================================
                // HISTORY TAB (আগে করা সব ফর্দ)
                // ==========================================
                if (purchasedItems.isEmpty()) {
                    item {
                        MawaEmptyState(
                            icon = Icons.Default.History,
                            title = "পূর্বে সম্পন্ন কোনো ফর্দ নেই",
                            subtitle = "বর্তমান ফর্দের বাজার সম্পন্ন করলে তা এখানে সংরক্ষিত থাকবে",
                            actionLabel = "বর্তমান ফর্দে যান",
                            onActionClick = { activeTab = FordiTab.CURRENT }
                        )
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "মোট কেনা হয়েছে (${BengaliUtils.toBanglaDigits(purchasedItems.size.toLong())} টি)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = fTextPrimary
                            )

                            TextButton(onClick = { viewModel.clearCompletedFordi() }) {
                                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = FinancialNegative, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ইতিহাস পরিষ্কার", color = FinancialNegative)
                            }
                        }
                    }

                    items(purchasedItems, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = fSurface),
                            border = BorderStroke(1.dp, fMintBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.productName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = fTextPrimary
                                    )
                                    Text(
                                        text = "${BengaliUtils.formatQuantity(item.actualQuantity, item.unit)} • ${BengaliUtils.formatTaka(item.actualQuantity * item.actualRate)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = fTextSecondary
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.reAddPurchasedItemsToFordi(listOf(item))
                                        Toast.makeText(context, "${item.productName} নতুন ফর্দে যোগ করা হয়েছে", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = fEmerald),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("+ পুনরায় ফর্দে নিন", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOGS & BOTTOM SHEETS
    // ==========================================

    // 1. Set / Edit Budget Dialog
    if (showBudgetDialog) {
        var budgetInput by remember {
            mutableStateOf(if (currentBudget > 0) currentBudget.toInt().toString() else "")
        }

        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text(text = "বাজারের বাজেট নির্ধারণ", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "আপনার আজকের বাজারের লক্ষ্যমাত্রা বা সর্বোচ্চ খরচের বাজেট নির্ধারণ করুন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it.filter { char -> char.isDigit() } },
                        label = { Text("বাজেট (৳)") },
                        placeholder = { Text("যেমন: ১০০০ বা ১৫০০") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = budgetInput.toDoubleOrNull() ?: 0.0
                        currentBudget = num
                        prefs.edit().putFloat("current_budget", num.toFloat()).apply()
                        showBudgetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive)
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // 2. Save as Template Dialog
    if (showSaveTemplateDialog) {
        var templateName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSaveTemplateDialog = false },
            title = { Text(text = "ফর্দ টেমপ্লেট হিসেবে সংরক্ষণ", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "বর্তমান ফর্দের ${BengaliUtils.toBanglaDigits(pendingItems.size.toLong())}টি পণ্য টেমপ্লেট আকারে সংরক্ষণ করলে পরে এক ক্লিকে লোড করতে পারবেন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = templateName,
                        onValueChange = { templateName = it },
                        label = { Text("টেমপ্লেটের নাম *") },
                        placeholder = { Text("যেমন: সাপ্তাহিক মুদি বাজার, কাঁচাবাজার") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (templateName.isNotBlank()) {
                            FordiTemplateManager.saveCurrentFordiAsTemplate(
                                context = context,
                                templateName = templateName,
                                items = pendingItems
                            )
                            Toast.makeText(context, "টেমপ্লেট সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                            showSaveTemplateDialog = false
                        }
                    },
                    enabled = templateName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive)
                ) {
                    Text("সংরক্ষণ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveTemplateDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // 3. Templates Bottom Sheet
    if (showTemplatesSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val allTemplates = remember { FordiTemplateManager.getAllTemplates(context) }

        ModalBottomSheet(
            onDismissRequest = { showTemplatesSheet = false },
            sheetState = sheetState,
            containerColor = fSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = fMintContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bookmarks,
                                    contentDescription = null,
                                    tint = fEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "সংরক্ষিত ফর্দ টেমপ্লেটসমূহ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = fTextPrimary
                            )
                            Text(
                                text = "এক ক্লিকে প্রয়োজনীয় বাজার ফর্দে লোড করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = fTextSecondary
                            )
                        }
                    }
                    IconButton(onClick = { showTemplatesSheet = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = fTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allTemplates, key = { it.id }) { t ->
                        val templateIcon = when {
                            t.name.contains("মুদি") -> Icons.Default.Storefront
                            t.name.contains("শাক") || t.name.contains("সবজি") || t.name.contains("কাঁচাবাজার") -> Icons.Default.Eco
                            t.name.contains("মাছ") || t.name.contains("মাংস") -> Icons.Default.Restaurant
                            else -> Icons.Default.LocalGroceryStore
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = if (t.isDefault) fSurfaceSubtle else fSurface,
                            border = BorderStroke(1.dp, fMintBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = fMintContainer,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = templateIcon,
                                                    contentDescription = null,
                                                    tint = fDeepEmerald,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = t.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = fTextPrimary
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (t.isDefault) fMintContainer else fSurface,
                                                    border = BorderStroke(0.5.dp, fMintBorder)
                                                ) {
                                                    Text(
                                                        text = if (t.isDefault) "রেডিমেড টেমপ্লেট" else "আমার নিজস্ব টেমপ্লেট",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (t.isDefault) fDeepEmerald else fTextSecondary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "${BengaliUtils.toBanglaDigits(t.items.size.toLong())} টি পণ্য",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = fTextSecondary
                                                )
                                            }
                                        }
                                    }

                                    if (!t.isDefault) {
                                        IconButton(
                                            onClick = {
                                                FordiTemplateManager.deleteTemplate(context, t.id)
                                                showTemplatesSheet = false
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "মুছুন",
                                                tint = FinancialNegative,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = t.items.joinToString(" • ") { "${it.productName} (${BengaliUtils.formatQuantity(it.plannedQuantity, it.unit)})" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = fTextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.addMultipleFordiTemplateItems(t.items) {
                                                Toast.makeText(context, "${t.name} থেকে পণ্য ফর্দে লোড হয়েছে", Toast.LENGTH_SHORT).show()
                                            }
                                            showTemplatesSheet = false
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = fEmerald),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.PlaylistAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ফর্দে লোড করুন", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }

    // 4. Product Catalog Picker Bottom Sheet ("সব ক্যাটালগ →" & Grid View)
    if (showCatalogSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var catalogSearch by remember { mutableStateOf("") }
        var onlyLowStock by remember { mutableStateOf(false) }

        val filteredCatalog = remember(catalogSearch, activeProducts, onlyLowStock) {
            var list = if (catalogSearch.isBlank()) activeProducts
            else activeProducts.filter {
                it.name.contains(catalogSearch, ignoreCase = true) ||
                it.banglaName.contains(catalogSearch, ignoreCase = true) ||
                it.category.contains(catalogSearch, ignoreCase = true)
            }
            if (onlyLowStock) {
                list = list.filter { it.stockQuantity <= 5.0 }
            }
            list
        }

        ModalBottomSheet(
            onDismissRequest = { showCatalogSheet = false },
            sheetState = sheetState,
            containerColor = fSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = fMintContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = fEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "পণ্য ও স্টক ক্যাটালগ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = fTextPrimary
                            )
                            Text(
                                text = "স্টক স্থিতি দেখে এক ক্লিকে ফর্দে যুক্ত করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = fTextSecondary
                            )
                        }
                    }
                    IconButton(onClick = { showCatalogSheet = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ", tint = fTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = catalogSearch,
                    onValueChange = { catalogSearch = it },
                    placeholder = { Text("পণ্যের নাম বা ক্যাটাগরি দিয়ে খুঁজুন...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = fTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = fEmerald,
                        unfocusedBorderColor = fMintBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter & Navigation Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (!onlyLowStock) fMintContainer else fSurface,
                            border = BorderStroke(1.dp, if (!onlyLowStock) fEmerald else fMintBorder),
                            modifier = Modifier.clickable { onlyLowStock = false }
                        ) {
                            Text(
                                text = "সকল (${BengaliUtils.toBanglaDigits(activeProducts.size.toLong())})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (!onlyLowStock) FontWeight.Bold else FontWeight.Normal,
                                color = if (!onlyLowStock) fDeepEmerald else fTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (onlyLowStock) fMintContainer else fSurface,
                            border = BorderStroke(1.dp, if (onlyLowStock) fEmerald else fMintBorder),
                            modifier = Modifier.clickable { onlyLowStock = true }
                        ) {
                            Text(
                                text = "কম স্টক / ঘাটতি",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (onlyLowStock) FontWeight.Bold else FontWeight.Normal,
                                color = if (onlyLowStock) fDeepEmerald else fTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            showCatalogSheet = false
                            onOpenProducts?.invoke()
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "স্টক খাতা পরিচালনা →",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = fEmerald
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredCatalog.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (activeProducts.isEmpty()) "কোনো সংরক্ষিত পণ্য পাওয়া যায়নি" else "খুঁজে পাওয়া যায়নি",
                            color = fTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (activeProducts.isEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.seedStandardProductsIfEmpty() },
                                colors = ButtonDefaults.buttonColors(containerColor = fEmerald),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("প্রয়োজনীয় রেডিমেড পণ্য লোড করুন")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCatalog, key = { it.id }) { prod ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = fSurface,
                                border = BorderStroke(1.dp, fMintBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = prod.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = fTextPrimary
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text(
                                                text = "${prod.category} • দর: ৳${prod.defaultPurchasePrice.toInt()}/${prod.unit}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = fTextSecondary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            // Stock Badge
                                            if (prod.stockQuantity <= 0) {
                                                Surface(
                                                    color = FinancialNegative.copy(alpha = 0.12f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "ঘাটতি (০ ${prod.unit})",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = FinancialNegative,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                    )
                                                }
                                            } else if (prod.stockQuantity <= 5.0) {
                                                Surface(
                                                    color = FinancialWarning.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "কম স্টক (${BengaliUtils.formatQuantity(prod.stockQuantity, prod.unit)})",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = FinancialWarning,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                    )
                                                }
                                            } else {
                                                Surface(
                                                    color = fMintContainer,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "স্টক: ${BengaliUtils.formatQuantity(prod.stockQuantity, prod.unit)}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = fDeepEmerald,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            handleAddItem(
                                                prod.name,
                                                1.0,
                                                prod.unit.ifBlank { "পিস" },
                                                prod.defaultPurchasePrice,
                                                prod.id
                                            )
                                            Toast.makeText(context, "${prod.name} ফর্দে যুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = fEmerald),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("যোগ")
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
            }
        }
    }

    // 5. Voice Input Helper Dialog
    if (showVoiceHelperDialog) {
        var voiceText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showVoiceHelperDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = FinancialPositive)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ভয়েস / প্রাকৃতিক ভাষায় ফর্দ ইনপুট", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "মুখে বলুন বা সহজ ভাষায় লিখুন, সিস্টেম স্বয়ংক্রিয়ভাবে পণ্য, পরিমাণ এবং দর শনাক্ত করবে:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = voiceText,
                        onValueChange = { voiceText = it },
                        placeholder = { Text("যেমন: মাছ ২০০ বা পিয়াজ দুই কেজি") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("দ্রুত নমুনা নির্বাচন করুন:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "মাছ ২০০",
                            "পিয়াজ দুই কেজি",
                            "৫ কেজি চাল",
                            "১২টি ডিম",
                            "সয়াবিন তেল ২ লিটার",
                            "আলু ৫০ টাকার"
                        ).forEach { sample ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEFF7F1),
                                border = BorderStroke(1.dp, FinancialPositive.copy(alpha = 0.5f)),
                                modifier = Modifier.clickable { voiceText = sample }
                            ) {
                                Text(
                                    text = sample,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FinancialPositive,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (voiceText.isNotBlank()) {
                        val parsed = BengaliFordiParser.parse(voiceText)
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8F8F0),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("সনাক্তকৃত ফলাফল:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = FinancialPositive)
                                Text("পণ্য: ${parsed.productName}", style = MaterialTheme.typography.bodySmall)
                                Text("পরিমাণ: ${BengaliUtils.formatQuantity(parsed.quantity, parsed.unit)}", style = MaterialTheme.typography.bodySmall)
                                if (parsed.estimatedRate > 0) {
                                    Text("দর/মূল্য: ${BengaliUtils.formatTaka(parsed.estimatedRate)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (voiceText.isNotBlank()) {
                            val parsed = BengaliFordiParser.parse(voiceText)
                            handleAddItem(
                                parsed.productName,
                                parsed.quantity,
                                parsed.unit,
                                parsed.estimatedRate,
                                null
                            )
                            showVoiceHelperDialog = false
                        }
                    },
                    enabled = voiceText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive)
                ) {
                    Text("ফর্দে যোগ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVoiceHelperDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // 6. Finish Shopping Confirmation Dialog ("✓ বাজার সম্পন্ন করুন")
    if (showFinishShoppingDialog) {
        val targetItems = if (checkedItems.isNotEmpty()) checkedItems else pendingItems

        AlertDialog(
            onDismissRequest = { showFinishShoppingDialog = false },
            title = { Text(text = "বাজার সম্পন্ন নিশ্চিতকরণ", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "আপনি কি নিশ্চিত যে ${BengaliUtils.toBanglaDigits(targetItems.size.toLong())}টি পণ্য ক্রয় সম্পন্ন হয়েছে এবং খরচের তালিকায় যোগ করতে চান?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "মোট খরচে যাবে: ${BengaliUtils.formatTaka(targetItems.sumOf { it.plannedQuantity * it.purchaseRate })}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FinancialPositive
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.convertMultipleFordiToPurchases(targetItems) {
                            checkedItemIds = emptySet()
                        }
                        showFinishShoppingDialog = false
                        Toast.makeText(context, "বাজার সম্পন্ন ও খরচে অন্তর্ভুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive)
                ) {
                    Text("হ্যাঁ, সম্পন্ন করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishShoppingDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // 7. Clear Confirm Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text(text = "সম্পূর্ণ ফর্দ খালি করবেন?", fontWeight = FontWeight.Bold, color = FinancialNegative) },
            text = {
                Text("এটি বর্তমান ফর্দের সকল আইটেম মুছে ফেলবে। আপনি কি নিশ্চিত?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllFordiItems()
                        checkedItemIds = emptySet()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialNegative)
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // 8. Edit Item Dialog
    itemToEdit?.let { item ->
        EditFordiItemDialog(
            fordiItem = item,
            onDismiss = { itemToEdit = null },
            onSave = { updated ->
                viewModel.updateFordiItem(updated)
                itemToEdit = null
            },
            onDelete = {
                viewModel.deleteFordiItem(item.id)
                itemToEdit = null
            }
        )
    }
}

@Composable
fun EditFordiItemDialog(
    fordiItem: FordiItemEntity,
    onDismiss: () -> Unit,
    onSave: (FordiItemEntity) -> Unit,
    onDelete: () -> Unit
) {
    var productName by remember { mutableStateOf(fordiItem.productName) }
    var quantity by remember { mutableStateOf(fordiItem.plannedQuantity.toString().removeSuffix(".0")) }
    var unit by remember { mutableStateOf(fordiItem.unit) }
    var purchaseRate by remember { mutableStateOf(if (fordiItem.purchaseRate > 0) fordiItem.purchaseRate.toString().removeSuffix(".0") else "") }

    val qtyNum = quantity.toDoubleOrNull() ?: 0.0
    val pRateNum = purchaseRate.toDoubleOrNull() ?: 0.0
    val calculatedTotal = qtyNum * pRateNum

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "পণ্য ও দর এডিট করুন", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("পণ্যের নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) quantity = it },
                        label = { Text("পরিমাণ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("একক") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = purchaseRate,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) purchaseRate = it },
                    label = { Text("ক্রয় দর (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF3F7F4),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "মোট লাইন মূল্য:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = BengaliUtils.formatTaka(calculatedTotal),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FinancialPositive
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = FinancialNegative, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ফর্দ থেকে মুছুন", color = FinancialNegative)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (productName.isNotBlank() && qtyNum > 0) {
                        onSave(
                            fordiItem.copy(
                                productName = productName.trim(),
                                plannedQuantity = qtyNum,
                                unit = unit.trim(),
                                purchaseRate = pRateNum
                            )
                        )
                    }
                },
                enabled = productName.isNotBlank() && qtyNum > 0,
                colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive)
            ) {
                Text("আপডেট করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
