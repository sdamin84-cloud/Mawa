package com.example.mawa.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.data.local.entity.ProductEntity
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialNegativeContainer
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialPositiveContainer
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.FinancialWarningContainer
import com.example.ui.theme.MawaPrimary
import com.example.ui.theme.MawaPrimaryContainer

/**
 * 5. ঘাটতি রোধ: দোকান থেকে সংসারের খরচের নিখুঁত বিভাজন ("বাসার জন্য নিলাম")
 * 
 * সমস্যা ও সমাধান:
 * - দোকানদার ক্যাশ ড্রয়ার থেকে বাজার খরচ বা বাচ্চার জন্য ৫০০ টাকা নেন, বা শেলফ থেকে ১ বোতল তেল নেন।
 * - দিন শেষে ক্যাশ মেলালে মনে হয় "আমার ক্যাশে ৫০০ টাকা কম কেন? চুরি বা হিসাবের ভুল হলো নাকি?"
 * - এইBottomSheet এ ১-ট্যাপে এন্ট্রি দিলে ক্যাশ ড্রয়ার ও ব্যক্তিগত খরচের হিসাব দুধের মতো পরিষ্কার থাকে।
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeWithdrawalBottomSheet(
    products: List<ProductEntity>,
    todayTransactions: List<TransactionEntity>,
    onDismiss: () -> Unit,
    onRecordCashWithdrawal: (amount: Double, description: String) -> Unit,
    onRecordGoodsWithdrawal: (
        productName: String,
        productId: Long?,
        quantity: Double,
        unit: String,
        rate: Double,
        isDeductFromCash: Boolean,
        note: String
    ) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = ক্যাশ টাকা নিলাম, 1 = দোকানের পণ্য নিলাম, 2 = আজকের খতিয়ান

    // Cash Tab State
    var cashAmount by remember { mutableStateOf("") }
    var selectedReason by remember { mutableStateOf("বাজার খরচ") }
    var customCashNote by remember { mutableStateOf("") }

    // Goods Tab State
    var productSearchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var customProductName by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var unitText by remember { mutableStateOf("পিস") }
    var rateText by remember { mutableStateOf("") }
    var isDeductFromCashDrawer by remember { mutableStateOf(false) } // Default: shelf item taken without touching cash drawer
    var goodsNote by remember { mutableStateOf("") }

    // Filter today's home withdrawals
    val todayHomeWithdrawals = remember(todayTransactions) {
        todayTransactions.filter { it.type == TransactionType.EXPENSE_HOME }
    }
    val totalHomeAmountToday = remember(todayHomeWithdrawals) {
        todayHomeWithdrawals.sumOf { it.amount }
    }

    val quickAmounts = listOf(50, 100, 200, 500, 1000, 2000)
    val quickReasons = listOf("🥬 বাজার খরচ", "🎒 বাচ্চার খরচ", "🏠 বাসা ভাড়া", "💊 ওষুধ/চিকিৎসা", "☕ ব্যক্তিগত হাতখরচ", "⚡ পারিবারিক বিল")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("home_withdrawal_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(FinancialWarningContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = FinancialWarning,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "বাসার জন্য নিলাম (ঘাটতি রোধ)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "সংসার ও ব্যক্তিগত খরচ আলাদা রাখুন",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "বন্ধ",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Shortage Prevention Educational Notice
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "দোকানের ক্যাশ বা শেলফ থেকে সংসারে যাই নেন, এখানে লিখলে দিন শেষে ক্যাশে কোনো অজ্ঞাত 'ঘাটতি' হবে না।",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs: 1. ক্যাশ টাকা, 2. দোকানের পণ্য, 3. আজকের খতিয়ান
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .height(44.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "💵 ক্যাশ টাকা",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "🛒 দোকানের পণ্য",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📜 খতিয়ান",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                            if (todayHomeWithdrawals.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = FinancialWarning
                                ) {
                                    Text(
                                        text = BengaliUtils.toBanglaDigits(todayHomeWithdrawals.size),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // TAB 1: 💵 ক্যাশ ড্রয়ার থেকে টাকা নিলাম
                    Column {
                        Text(
                            text = "ক্যাশ ড্রয়ার থেকে কত টাকা নিয়েছেন?",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        MawaAmountInput(
                            amount = cashAmount,
                            onAmountChange = { cashAmount = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_cash_amount_input"),
                            label = "টাকার পরিমাণ (৳)",
                            quickAmounts = quickAmounts
                        )

                        // Quick Amount Chips
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickAmounts.forEach { amt ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { cashAmount = amt.toString() },
                                    color = if (cashAmount == amt.toString()) FinancialWarningContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (cashAmount == amt.toString()) FinancialWarning else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Text(
                                        text = "+৳${BengaliUtils.toBanglaDigits(amt)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (cashAmount == amt.toString()) FinancialWarning else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Reason Chips
                        Text(
                            text = "খরচের খাত / কারণ বেছে নিন:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickReasons.forEach { r ->
                                val cleanName = r.substringAfter(" ")
                                val isSelected = selectedReason == cleanName
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedReason = cleanName },
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Text(
                                        text = r,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = customCashNote,
                            onValueChange = { customCashNote = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("অন্য কোনো মন্তব্য থাকলে লিখুন (ঐচ্ছিক)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                val amtNum = cashAmount.toDoubleOrNull() ?: 0.0
                                if (amtNum > 0) {
                                    val finalDesc = if (customCashNote.isNotBlank()) "$selectedReason: $customCashNote" else selectedReason
                                    onRecordCashWithdrawal(amtNum, finalDesc)
                                    onDismiss()
                                }
                            },
                            enabled = (cashAmount.toDoubleOrNull() ?: 0.0) > 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_home_cash_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = FinancialWarning),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ক্যাশ থেকে ৳${BengaliUtils.toBanglaDigits((cashAmount.toDoubleOrNull() ?: 0.0).toInt())} সংসারের খরচ সেভ করুন",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                1 -> {
                    // TAB 2: 🛒 দোকান থেকে সংসারের পণ্য নিলাম
                    Column {
                        Text(
                            text = "দোকানের শেলফ বা স্টক থেকে কি পণ্য নিয়েছেন?",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Product Search & Quick Suggestions
                        OutlinedTextField(
                            value = productSearchQuery,
                            onValueChange = {
                                productSearchQuery = it
                                val match = products.find { p -> p.name.equals(it.trim(), ignoreCase = true) || p.banglaName.equals(it.trim(), ignoreCase = true) }
                                if (match != null) {
                                    selectedProduct = match
                                    unitText = match.unit
                                    rateText = if (match.defaultSellingPrice > 0) match.defaultSellingPrice.toString() else match.defaultPurchasePrice.toString()
                                } else {
                                    customProductName = it
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("পণ্যের নাম লিখুন (যেমন: তীর তেল, চিনি, ডিম)") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        // Filtered Active Products Chips
                        val filteredProducts = remember(products, productSearchQuery) {
                            if (productSearchQuery.isBlank()) products.take(6)
                            else products.filter {
                                it.name.contains(productSearchQuery, ignoreCase = true) ||
                                it.banglaName.contains(productSearchQuery, ignoreCase = true)
                            }.take(8)
                        }

                        if (filteredProducts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                filteredProducts.forEach { prod ->
                                    val isSelected = selectedProduct?.id == prod.id
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                selectedProduct = prod
                                                productSearchQuery = prod.name
                                                unitText = prod.unit
                                                rateText = if (prod.defaultSellingPrice > 0) prod.defaultSellingPrice.toString() else prod.defaultPurchasePrice.toString()
                                            },
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = prod.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quantity & Rate
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Quantity
                            OutlinedTextField(
                                value = quantityText,
                                onValueChange = { quantityText = it },
                                label = { Text("পরিমাণ") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            // Unit
                            OutlinedTextField(
                                value = unitText,
                                onValueChange = { unitText = it },
                                label = { Text("একক") },
                                modifier = Modifier.weight(0.8f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            // Rate
                            OutlinedTextField(
                                value = rateText,
                                onValueChange = { rateText = it },
                                label = { Text("দর (৳)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }

                        // Calculated Total
                        val qtyNum = quantityText.toDoubleOrNull() ?: 1.0
                        val rateNum = rateText.toDoubleOrNull() ?: 0.0
                        val totalGoodsValue = qtyNum * rateNum

                        if (totalGoodsValue > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("পণ্যের মোট মূল্য:", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        "৳${BengaliUtils.toBanglaDigits(totalGoodsValue.toInt())}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = FinancialWarning
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Cash Drawer Choice (Deduct or Untouched)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isDeductFromCashDrawer = !isDeductFromCashDrawer }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isDeductFromCashDrawer,
                                onClick = { isDeductFromCashDrawer = !isDeductFromCashDrawer },
                                colors = RadioButtonDefaults.colors(selectedColor = FinancialWarning)
                            )
                            Column {
                                Text(
                                    text = if (isDeductFromCashDrawer) "ক্যাশ ড্রয়ারের টাকা সমন্বয় করুন" else "ক্যাশ ড্রয়ার স্পর্শ করা হয়নি (শুধু স্টক কমবে)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isDeductFromCashDrawer) "ক্যাশ ড্রয়ার থেকে এই টাকা কর্তন হবে" else "ক্যাশ ব্যালেন্সে কোনো পরিবর্তন হবে না, শুধু পণ্যটি হিসাবভুক্ত থাকবে",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val pName = selectedProduct?.name ?: productSearchQuery.ifBlank { "গৃহস্থালি পণ্য" }
                                onRecordGoodsWithdrawal(
                                    pName,
                                    selectedProduct?.id,
                                    qtyNum,
                                    unitText.ifBlank { "পিস" },
                                    rateNum,
                                    isDeductFromCashDrawer,
                                    goodsNote
                                )
                                onDismiss()
                            },
                            enabled = (productSearchQuery.isNotBlank() || selectedProduct != null) && qtyNum > 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_home_goods_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "পণ্য উত্তোলন সংরক্ষণ করুন",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                2 -> {
                    // TAB 3: 📜 আজকের সংসারের খতিয়ান
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "আজকের নেওয়া মোট ব্যক্তিগত/সংসার খরচ:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "৳${BengaliUtils.toBanglaDigits(totalHomeAmountToday.toInt())}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = FinancialWarning
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (todayHomeWithdrawals.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "আজ এখনও ব্যক্তিগত বা সংসারের জন্য কোনো টাকা নেওয়া হয়নি",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(todayHomeWithdrawals) { tx ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(FinancialWarningContainer),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (tx.quantity > 0) Icons.Default.ShoppingBag else Icons.Default.Payments,
                                                        contentDescription = null,
                                                        tint = FinancialWarning,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = tx.note.ifBlank { "সংসার খরচ" },
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = BengaliUtils.formatTransactionTime(tx.timestamp),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Text(
                                                text = if (tx.amount > 0) "৳${BengaliUtils.toBanglaDigits(tx.amount.toInt())}" else "পণ্য উত্তোলন",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = FinancialWarning
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
    }
}

/**
 * Clean banner shown on HomeScreen when home withdrawals exist today,
 * explaining to the shopkeeper that their cash in drawer is reduced due to home withdrawal,
 * NOT due to theft or loss.
 */
@Composable
fun HomeWithdrawalShortageBanner(
    todayHomeWithdrawals: Double,
    onOpenHomeWithdrawalSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (todayHomeWithdrawals <= 0) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onOpenHomeWithdrawalSheet() }
            .testTag("home_withdrawal_banner"),
        shape = RoundedCornerShape(12.dp),
        color = FinancialWarningContainer.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, FinancialWarning.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(FinancialWarning.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = FinancialWarning,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "আজ ক্যাশ থেকে সংসারে নেওয়া: ৳${BengaliUtils.toBanglaDigits(todayHomeWithdrawals.toInt())}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ঘাটতি নয় (ব্যক্তিগত উত্তোলন হিসেবে সমন্বিত) · বিস্তারিত দেখতে ট্যাপ করুন",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "+ আরও নিন",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = FinancialWarning,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.7f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
    }
}
