package com.example.mawa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.local.entity.CustomerEntity
import com.example.mawa.ui.components.BarcodeScannerDialog
import com.example.mawa.ui.components.DigitalReceiptDialog
import com.example.mawa.ui.components.MawaAmountInput
import com.example.mawa.ui.components.MawaTopBar
import com.example.mawa.ui.components.VoiceInputButton
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.InvoiceItem
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialNegativeContainer
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialPositiveContainer
import com.example.ui.theme.MawaPrimary

@Composable
fun SalesScreen(
    viewModel: MawaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customers by viewModel.customersWithBalance.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val shopSettings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val shopName = shopSettings?.shopName?.ifBlank { "মাওয়া স্মার্ট খাতা" } ?: "মাওয়া স্মার্ট খাতা"
    val shopPhone = shopSettings?.phone ?: ""

    var isCashSale by remember { mutableStateOf(true) } // true = Cash Sale, false = Baki Sale
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerSearchQuery by remember { mutableStateOf("") }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showBarcodeDialog by remember { mutableStateOf(false) }
    var showReceiptDialog by remember { mutableStateOf(false) }

    var lastSaleAmount by remember { mutableStateOf(0.0) }
    var lastSaleNote by remember { mutableStateOf("") }
    var lastSaleCustomerName by remember { mutableStateOf("") }
    var lastSaleCustomerPhone by remember { mutableStateOf("") }
    var lastSaleIsCash by remember { mutableStateOf(true) }

    var showSuccessBanner by remember { mutableStateOf(false) }
    var lastSavedMessage by remember { mutableStateOf("") }

    val filteredCustomers = remember(customers, customerSearchQuery) {
        if (customerSearchQuery.isBlank()) {
            customers.map { it.customer }
        } else {
            customers
                .filter {
                    it.customer.name.contains(customerSearchQuery, ignoreCase = true) ||
                            it.customer.phone.contains(customerSearchQuery)
                }
                .map { it.customer }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        MawaTopBar(
            title = "বিক্রি হিসাব",
            subtitle = if (isCashSale) "নগদ বিক্রি যোগ করুন" else "বাকি বিক্রি যোগ করুন",
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("sales_back_button")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "পেছনে")
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(14.dp))

                // Success Message Banner with Print Receipt Action
                if (showSuccessBanner) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = FinancialPositiveContainer),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = FinancialPositive,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = lastSavedMessage,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = FinancialPositive
                                    )
                                }

                                Button(
                                    onClick = { showReceiptDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("btn_print_sale_receipt")
                                ) {
                                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("রসিদ প্রিন্ট", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                // 1. Sale Type Toggle: [ নগদ বিক্রি ] [ বাকি বিক্রি ] & Voice Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "বিক্রির ধরন",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Voice Input Button
                    VoiceInputButton(
                        onVoiceResult = { parsed ->
                            val amt = parsed.amount
                            if (amt != null && amt > 0) {
                                amount = if (amt % 1.0 == 0.0) amt.toLong().toString() else amt.toString()
                            }
                            if (parsed.note.isNotBlank()) {
                                note = parsed.note
                            }
                            isCashSale = parsed.detectedType == com.example.mawa.data.local.entity.TransactionType.SALE_CASH
                            if (!parsed.customerOrItemName.isNullOrBlank()) {
                                val match = customers.find { it.customer.name.contains(parsed.customerOrItemName, ignoreCase = true) }
                                if (match != null) {
                                    selectedCustomer = match.customer
                                }
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cash Sale Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isCashSale) FinancialPositiveContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                isCashSale = true
                                showSuccessBanner = false
                            }
                            .padding(vertical = 12.dp)
                            .testTag("tab_cash_sale"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (isCashSale) FinancialPositive else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "নগদ বিক্রি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isCashSale) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCashSale) FinancialPositive else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Baki Sale Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (!isCashSale) FinancialNegativeContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                isCashSale = false
                                showSuccessBanner = false
                            }
                            .padding(vertical = 12.dp)
                            .testTag("tab_baki_sale"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (!isCashSale) FinancialNegative else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "বাকি বিক্রি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (!isCashSale) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isCashSale) FinancialNegative else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Amount Field
                MawaAmountInput(
                    amount = amount,
                    onAmountChange = {
                        amount = it
                        showSuccessBanner = false
                    },
                    label = if (isCashSale) "নগদ বিক্রির পরিমাণ" else "বাকি বিক্রির পরিমাণ",
                    quickAmounts = listOf(100, 200, 500, 1000, 2000, 5000),
                    testTag = "sale_amount_input"
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            // 3. Customer Selection for Baki Sale
            if (!isCashSale) {
                item {
                    Text(
                        text = "কাস্টমার নির্বাচন করুন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Customer search & Add Customer Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customerSearchQuery,
                            onValueChange = { customerSearchQuery = it },
                            placeholder = { Text("নাম বা মোবাইল নম্বর খুঁজুন") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("customer_search_field"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { showAddCustomerDialog = true },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MawaPrimary)
                                .testTag("add_customer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "নতুন কাস্টমার",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (selectedCustomer != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(FinancialNegativeContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = FinancialNegative,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = selectedCustomer?.name ?: "",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (!selectedCustomer?.phone.isNullOrBlank()) {
                                            Text(
                                                text = selectedCustomer?.phone ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "নির্বাচিত",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FinancialNegative,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // Filtered Customers List
                items(filteredCustomers.take(5), key = { it.id }) { customer ->
                    val isSelected = selectedCustomer?.id == customer.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) FinancialNegativeContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface)
                            .clickable { selectedCustomer = customer }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = customer.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            if (customer.phone.isNotBlank()) {
                                Text(
                                    text = customer.phone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = FinancialNegative
                            )
                        }
                    }
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))

                // 4. Note input with Barcode & Product Quick Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("পণ্য বিবরণ / নোট") },
                        placeholder = { Text("যেমন: মিনিকেট চাল, মসুর ডাল ইত্যাদি") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sale_note_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MawaPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showBarcodeDialog = true },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MawaPrimary.copy(alpha = 0.12f))
                            .testTag("btn_barcode_scanner")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "বারকোড ও পণ্য খুঁজুন",
                            tint = MawaPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5. Submit Button
                val amountVal = amount.toDoubleOrNull() ?: 0.0
                val canSubmit = amountVal > 0 && (isCashSale || selectedCustomer != null)

                Button(
                    onClick = {
                        lastSaleAmount = amountVal
                        lastSaleNote = note
                        lastSaleIsCash = isCashSale

                        if (isCashSale) {
                            viewModel.recordSale(
                                isCash = true,
                                amount = amountVal,
                                note = note
                            )
                            lastSaleCustomerName = "নগদ ক্রেতা"
                            lastSaleCustomerPhone = ""
                            lastSavedMessage = "নগদ বিক্রি ৳${BengaliUtils.toBanglaDigits(amountVal)} সংরক্ষিত হয়েছে"
                        } else {
                            val cust = selectedCustomer ?: return@Button
                            viewModel.recordSale(
                                isCash = false,
                                amount = amountVal,
                                customerId = cust.id,
                                customerName = cust.name,
                                note = note
                            )
                            lastSaleCustomerName = cust.name
                            lastSaleCustomerPhone = cust.phone
                            lastSavedMessage = "${cust.name}-কে বাকি ৳${BengaliUtils.toBanglaDigits(amountVal)} সংরক্ষিত হয়েছে"
                        }

                        // Reset fields
                        amount = ""
                        note = ""
                        showSuccessBanner = true
                    },
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_sale_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCashSale) FinancialPositive else FinancialNegative
                    )
                ) {
                    Text(
                        text = if (isCashSale) "নগদ বিক্রি সংরক্ষণ করুন" else "বাকি বিক্রি সংরক্ষণ করুন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Barcode Scanner & Product Search Dialog
    if (showBarcodeDialog) {
        BarcodeScannerDialog(
            allProducts = products,
            onProductSelected = { selectedProd ->
                note = selectedProd.banglaName.ifBlank { selectedProd.name }
                if (selectedProd.defaultSellingPrice > 0) {
                    amount = if (selectedProd.defaultSellingPrice % 1.0 == 0.0) {
                        selectedProd.defaultSellingPrice.toLong().toString()
                    } else {
                        selectedProd.defaultSellingPrice.toString()
                    }
                }
            },
            onDismiss = { showBarcodeDialog = false }
        )
    }

    // Digital Receipt Dialog for Completed Sale
    if (showReceiptDialog && lastSaleAmount > 0) {
        val invoiceItems = listOf(
            InvoiceItem(
                name = lastSaleNote.ifBlank { if (lastSaleIsCash) "নগদ পণ্য বিক্রি" else "বাকি পণ্য বিক্রি" },
                quantity = 1.0,
                unit = "টি",
                rate = lastSaleAmount,
                amount = lastSaleAmount
            )
        )
        DigitalReceiptDialog(
            shopName = shopName,
            shopPhone = shopPhone,
            customerName = lastSaleCustomerName.ifBlank { "সম্মানিত ক্রেতা" },
            customerPhone = lastSaleCustomerPhone,
            items = invoiceItems,
            subtotal = lastSaleAmount,
            discount = 0.0,
            paidAmount = if (lastSaleIsCash) lastSaleAmount else 0.0,
            previousDue = 0.0,
            currentDue = if (lastSaleIsCash) 0.0 else lastSaleAmount,
            note = if (lastSaleIsCash) "নগদ বিক্রি রসিদ" else "বাকি বিক্রি চালান",
            onDismiss = { showReceiptDialog = false }
        )
    }

    // Quick Add Customer Dialog
    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onAdd = { name, phone, address, openingBalance ->
                viewModel.addCustomer(
                    name = name,
                    phone = phone,
                    address = address,
                    openingBalance = openingBalance
                )
                showAddCustomerDialog = false
            }
        )
    }
}
