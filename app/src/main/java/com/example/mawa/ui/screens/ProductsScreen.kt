package com.example.mawa.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.local.entity.ProductEntity
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.model.ProductStats
import com.example.mawa.ui.components.MawaEmptyState
import com.example.mawa.ui.components.MawaSummaryRow
import com.example.mawa.ui.components.MawaTopBar
import com.example.mawa.ui.components.TransactionItemRow
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialNeutral
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialPositiveContainer
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.FinancialWarningContainer
import com.example.ui.theme.MawaPrimary

import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sell
import com.example.mawa.ui.components.BarcodeScannerDialog

@Composable
fun ProductsScreen(
    viewModel: MawaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedProductForDetail by remember { mutableStateOf<ProductEntity?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var initialBarcodeForNewProduct by remember { mutableStateOf("") }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var showDuplicateDetector by remember { mutableStateOf(false) }

    // If detail is active, show the Product Detail screen
    if (selectedProductForDetail != null) {
        val currentProd = allProducts.find { it.id == selectedProductForDetail?.id } ?: selectedProductForDetail!!
        val stats = viewModel.getProductStats(currentProd, allTransactions)

        ProductDetailScreen(
            product = currentProd,
            stats = stats,
            viewModel = viewModel,
            onBack = { selectedProductForDetail = null }
        )
        return
    }

    val filteredProducts = remember(allProducts, searchQuery) {
        if (searchQuery.isBlank()) allProducts
        else allProducts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.banglaName.contains(searchQuery) ||
                    it.category.contains(searchQuery, ignoreCase = true) ||
                    it.barcode.contains(searchQuery, ignoreCase = true) ||
                    it.id.toString() == searchQuery.trim()
        }
    }

    val duplicates = remember(allProducts) {
        viewModel.findPotentialDuplicates(allProducts)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        MawaTopBar(
            title = "পণ্য ও স্টক তালিকা",
            subtitle = "${BengaliUtils.toBanglaDigits(allProducts.size.toLong())} টি পণ্য সংরক্ষিত",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "পেছনে")
                }
            },
            actions = {
                IconButton(onClick = { showBarcodeScanner = true }) {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "বারকোড স্ক্যান", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Search & Add Product Row
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("নাম বা বারকোড দিয়ে খুঁজুন") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { showBarcodeScanner = true }) {
                                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "স্ক্যান", tint = MawaPrimary)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("product_search_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MawaPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { 
                                    initialBarcodeForNewProduct = ""
                                    showAddProductDialog = true 
                                },
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MawaPrimary)
                                    .testTag("add_product_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "নতুন পণ্য", tint = Color.White)
                            }
                        }

                        // Duplicate Product warning / merger banner
                        if (duplicates.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDuplicateDetector = true },
                                colors = CardDefaults.cardColors(containerColor = FinancialWarningContainer),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CallMerge,
                                            contentDescription = null,
                                            tint = FinancialWarning,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${BengaliUtils.toBanglaDigits(duplicates.size.toLong())} টি ডুপ্লিকেট পণ্য পাওয়া গেছে",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = FinancialWarning
                                        )
                                    }
                                    Text(
                                        text = "একত্রিত করুন →",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = FinancialWarning
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Table Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "পণ্য, বারকোড ও স্টক", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "ডিফল্ট দর (ক্রয়→বিক্রয়)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (filteredProducts.isEmpty()) {
                item {
                    MawaEmptyState(
                        icon = Icons.Default.Inventory,
                        title = "কোনো পণ্য নেই",
                        subtitle = "নতুন পণ্য যোগ করে বা বারকোড দিয়ে ক্রয়-বিক্রয় ও ফর্দ তৈরি সহজ করুন",
                        actionLabel = "পণ্য যোগ করুন",
                        onActionClick = { 
                            initialBarcodeForNewProduct = ""
                            showAddProductDialog = true 
                        }
                    )
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedProductForDetail = product },
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text = "${product.category} · প্রতি ${product.unit}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (product.barcode.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = MawaPrimary.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "🏷️ ${product.barcode}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MawaPrimary,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        if (product.stockQuantity > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = FinancialPositive.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "স্টক: ${BengaliUtils.formatQuantity(product.stockQuantity, product.unit)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = FinancialPositive,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${BengaliUtils.toBanglaDigits(product.defaultPurchasePrice.toInt().toString())}৳ → ${BengaliUtils.toBanglaDigits(product.defaultSellingPrice.toInt().toString())}৳",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MawaPrimary
                                    )
                                    Text(
                                        text = "বিস্তারিত হিসাব →",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Barcode Scanner Dialog for Products Screen
    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            allProducts = allProducts,
            onProductSelected = { matchedProd ->
                selectedProductForDetail = matchedProd
                showBarcodeScanner = false
            },
            onDismiss = { showBarcodeScanner = false },
            onAddNewProductWithBarcode = { barcode ->
                initialBarcodeForNewProduct = barcode
                showAddProductDialog = true
                showBarcodeScanner = false
            }
        )
    }

    // Add Product Dialog
    if (showAddProductDialog) {
        AddProductDialog(
            initialBarcode = initialBarcodeForNewProduct,
            onDismiss = { showAddProductDialog = false },
            onAdd = { name, bangla, unit, pPrice, sPrice, category, barcode, stock ->
                viewModel.addProduct(
                    name = name,
                    banglaName = bangla,
                    unit = unit,
                    purchasePrice = pPrice,
                    sellingPrice = sPrice,
                    category = category,
                    barcode = barcode,
                    stockQuantity = stock
                )
                showAddProductDialog = false
            }
        )
    }

    // Duplicate Merger Dialog
    if (showDuplicateDetector && duplicates.isNotEmpty()) {
        DuplicateMergerDialog(
            duplicates = duplicates,
            onDismiss = { showDuplicateDetector = false },
            onMerge = { canonical, duplicateId ->
                viewModel.mergeProducts(canonical, duplicateId)
                showDuplicateDetector = false
            }
        )
    }
}

@Composable
fun ProductDetailScreen(
    product: ProductEntity,
    stats: ProductStats,
    viewModel: MawaViewModel,
    onBack: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        MawaTopBar(
            title = product.name,
            subtitle = "প্রতি ${product.unit} · ${product.category}",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "পেছনে")
                }
            }
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Barcode / SKU Info Card
            if (product.barcode.isNotBlank()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MawaPrimary.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MawaPrimary.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null,
                                    tint = MawaPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "বারকোড / প্রোডাক্ট কোড",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = product.barcode,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MawaPrimary
                                    )
                                }
                            }

                            if (product.stockQuantity > 0) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "বর্তমান স্টক",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = BengaliUtils.formatQuantity(product.stockQuantity, product.unit),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = FinancialPositive
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Main Metrics Grid / Cards
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "গড় ক্রয় দর", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = BengaliUtils.formatTaka(stats.avgPurchasePrice),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "সম্পাদনা")
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Rate Breakdown (Latest, Highest, Lowest, Margin)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "সর্বশেষ ক্রয় দর", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = BengaliUtils.formatTaka(stats.latestPurchasePrice), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(text = "সর্বোচ্চ ক্রয় দর", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = BengaliUtils.formatTaka(stats.highestPurchasePrice), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FinancialNegative)
                            }
                            Column {
                                Text(text = "সর্বনিম্ন ক্রয় দর", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = BengaliUtils.formatTaka(stats.lowestPurchasePrice), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FinancialPositive)
                            }
                            Column {
                                Text(text = "আনুমানিক লাভ/মার্জিন", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = BengaliUtils.formatTaka(stats.estimatedMargin), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MawaPrimary)
                            }
                        }
                    }
                }
            }

            // Summary metrics
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "ক্রয় পরিসংখ্যান",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        MawaSummaryRow(
                            label = "মোট ক্রয় পরিমাণ",
                            amount = stats.totalPurchasedQty,
                            suffixText = product.unit
                        )
                        MawaSummaryRow(
                            label = "মোট ক্রয় ব্যয়",
                            amount = stats.totalPurchasedAmount
                        )
                        MawaSummaryRow(
                            label = "বর্তমান বিক্রয় দর",
                            amount = stats.sellingPrice,
                            amountColor = FinancialPositive,
                            showDivider = false
                        )
                    }
                }
            }

            // Purchase History Timeline for this product
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ক্রয় ইতিহাস",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${BengaliUtils.toBanglaDigits(stats.purchaseHistory.size.toLong())} বার কেনা হয়েছে",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (stats.purchaseHistory.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        MawaEmptyState(
                            icon = Icons.Default.Inventory,
                            title = "এখনও কেনা হয়নি",
                            subtitle = "ফর্দ থেকে কিনলে বা সরাসরি ক্রয় এন্ট্রি দিলে এখানে তালিকা দেখাবে"
                        )
                    }
                }
            } else {
                items(stats.purchaseHistory, key = { it.id }) { tx ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column {
                            TransactionItemRow(
                                transaction = tx,
                                onDelete = { viewModel.deleteTransaction(tx.id) }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    if (showEditDialog) {
        EditProductDialog(
            product = product,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                viewModel.updateProduct(updated)
                showEditDialog = false
            },
            onDelete = {
                viewModel.deleteProduct(product.id)
                showEditDialog = false
                onBack()
            }
        )
    }
}

@Composable
fun AddProductDialog(
    initialBarcode: String = "",
    onDismiss: () -> Unit,
    onAdd: (name: String, banglaName: String, unit: String, purchasePrice: Double, sellingPrice: Double, category: String, barcode: String, stockQuantity: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf(initialBarcode) }
    var unit by remember { mutableStateOf("কেজি") }
    var purchasePrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var stockQuantity by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("মুদি") }

    val categories = listOf("মুদি", "কনফেকশনারি", "পান/সিগারেট", "কসমেটিক্স", "ফল/সবজি", "স্টেশনারি", "সাধারণ")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "নতুন পণ্য যোগ", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("পণ্যের নাম *") },
                    placeholder = { Text("যেমন: চিনি, সয়াবিন তেল, মিনিকেট চাল") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Barcode / QR code field with auto-generate button
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("বারকোড / কিউআর কোড (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: 890123456789") },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                // Auto generate an 8-digit unique product barcode
                                val randomDigits = (10000000..99999999).random().toString()
                                barcode = randomDigits
                            }
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "অটো কোড তৈরি", tint = MawaPrimary)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Category Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(4).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("একক") },
                        placeholder = { Text("কেজি/পিস/লিটার") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stockQuantity,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) stockQuantity = it },
                        label = { Text("বর্তমান স্টক") },
                        placeholder = { Text("০") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchasePrice,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) purchasePrice = it },
                        label = { Text("ডিফল্ট ক্রয় দর (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sellingPrice,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) sellingPrice = it },
                        label = { Text("ডিফল্ট বিক্রয় দর (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val pPrice = purchasePrice.toDoubleOrNull() ?: 0.0
                        val sPrice = sellingPrice.toDoubleOrNull() ?: 0.0
                        val stock = stockQuantity.toDoubleOrNull() ?: 0.0
                        onAdd(name.trim(), name.trim(), unit.trim(), pPrice, sPrice, category.trim(), barcode.trim(), stock)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
fun EditProductDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var barcode by remember { mutableStateOf(product.barcode) }
    var unit by remember { mutableStateOf(product.unit) }
    var purchasePrice by remember { mutableStateOf(if (product.defaultPurchasePrice > 0) product.defaultPurchasePrice.toString() else "") }
    var sellingPrice by remember { mutableStateOf(if (product.defaultSellingPrice > 0) product.defaultSellingPrice.toString() else "") }
    var stockQuantity by remember { mutableStateOf(if (product.stockQuantity > 0) product.stockQuantity.toString() else "") }
    var category by remember { mutableStateOf(product.category) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "পণ্য তথ্য সম্পাদনা", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("পণ্যের নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("বারকোড / কিউআর কোড") },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val randomDigits = (10000000..99999999).random().toString()
                                barcode = randomDigits
                            }
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "অটো কোড", tint = MawaPrimary)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("একক") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stockQuantity,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) stockQuantity = it },
                        label = { Text("স্টক") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchasePrice,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) purchasePrice = it },
                        label = { Text("ক্রয় দর") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sellingPrice,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) sellingPrice = it },
                        label = { Text("বিক্রয় দর") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FinancialNegative),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("পণ্য মুছে ফেলুন")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val p = purchasePrice.toDoubleOrNull() ?: 0.0
                        val s = sellingPrice.toDoubleOrNull() ?: 0.0
                        val stock = stockQuantity.toDoubleOrNull() ?: 0.0
                        onSave(
                            product.copy(
                                name = name.trim(),
                                banglaName = name.trim(),
                                barcode = barcode.trim(),
                                unit = unit.trim(),
                                defaultPurchasePrice = p,
                                defaultSellingPrice = s,
                                stockQuantity = stock,
                                category = category.trim()
                            )
                        )
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
            ) {
                Text("আপডেট")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
fun DuplicateMergerDialog(
    duplicates: List<Pair<ProductEntity, ProductEntity>>,
    onDismiss: () -> Unit,
    onMerge: (canonical: ProductEntity, duplicateId: Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "ডুপ্লিকেট পণ্য একত্রিত করুন", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Text(
                        text = "নিচের পণ্যগুলো একই রকম মনে হচ্ছে। কোনটিকে মূল পণ্য হিসেবে রাখবেন তা বেছে নিন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(duplicates) { (p1, p2) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "মিল পাওয়া গেছে:", style = MaterialTheme.typography.labelSmall, color = MawaPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "১. ${p1.name} (${p1.unit})", fontWeight = FontWeight.Bold)
                                    Text(text = "২. ${p2.name} (${p2.unit})", fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onMerge(p1, p2.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
                                ) {
                                    Text("১-এ মার্জ করুন", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { onMerge(p2, p1.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
                                ) {
                                    Text("২-এ মার্জ করুন", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("বন্ধ") }
        }
    )
}
