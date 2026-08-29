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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.local.entity.ProductEntity
import com.example.mawa.ui.components.MawaEmptyState
import com.example.mawa.ui.components.MawaTopBar
import com.example.mawa.ui.components.TransactionItemRow
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.MawaPrimary

@Composable
fun PurchaseScreen(
    viewModel: MawaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allPurchases by viewModel.allPurchases.collectAsStateWithLifecycle()
    val activeProducts by viewModel.activeProducts.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val qtyFocus = remember { FocusRequester() }
    val unitFocus = remember { FocusRequester() }
    val rateFocus = remember { FocusRequester() }
    val totalFocus = remember { FocusRequester() }
    val noteFocus = remember { FocusRequester() }

    var productName by remember { mutableStateOf("") }
    var selectedProductId by remember { mutableStateOf<Long?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("কেজি") }
    var rate by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var showSuccessMessage by remember { mutableStateOf(false) }

    val matchedProducts = remember(productName, activeProducts) {
        if (productName.isBlank()) emptyList()
        else activeProducts.filter {
            it.name.contains(productName, ignoreCase = true) || it.banglaName.contains(productName)
        }.take(3)
    }

    val totalPurchasedValue = remember(allPurchases) {
        allPurchases.sumOf { it.amount }
    }

    val qNum = quantity.toDoubleOrNull() ?: 0.0
    val rNum = rate.toDoubleOrNull() ?: 0.0
    val tNum = totalAmount.toDoubleOrNull() ?: (qNum * rNum)
    val canSave = productName.isNotBlank() && (tNum > 0 || (qNum > 0 && rNum > 0))

    val performSavePurchase = {
        if (canSave) {
            val finalTotal = if (tNum > 0) tNum else (qNum * rNum)
            val finalRate = if (rNum > 0) rNum else if (qNum > 0) finalTotal / qNum else 0.0

            viewModel.recordDirectPurchase(
                productName = productName.trim(),
                productId = selectedProductId,
                quantity = qNum,
                unit = unit.trim(),
                rate = finalRate,
                total = finalTotal,
                note = note.trim()
            )

            // Clear inputs
            productName = ""
            rate = ""
            totalAmount = ""
            note = ""
            showSuccessMessage = true
            focusManager.clearFocus()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        MawaTopBar(
            title = "সরাসরি মাল কেনা",
            subtitle = "মোট ক্রয় ব্যয়: ${BengaliUtils.formatTaka(totalPurchasedValue)}",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "পেছনে")
                }
            }
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Direct Purchase Entry Form
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
                        Text(
                            text = "ডিলার বা মহাজন থেকে মাল ক্রয়",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Product Name Field
                        OutlinedTextField(
                            value = productName,
                            onValueChange = {
                                productName = it
                                selectedProductId = null
                                showSuccessMessage = false
                            },
                            label = { Text("পণ্যের নাম *") },
                            placeholder = { Text("যেমন: চিনি, ডাল, তেল, বিস্কুট") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { qtyFocus.requestFocus() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("direct_purchase_name_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Suggestion Chips
                        if (matchedProducts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                matchedProducts.forEach { p ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable {
                                                productName = p.name
                                                selectedProductId = p.id
                                                unit = p.unit
                                                rate = if (p.defaultPurchasePrice > 0) p.defaultPurchasePrice.toInt().toString() else ""
                                                qtyFocus.requestFocus()
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = p.name, style = MaterialTheme.typography.bodySmall, color = MawaPrimary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quantity & Unit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = {
                                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                        quantity = it
                                        val q = it.toDoubleOrNull() ?: 0.0
                                        val r = rate.toDoubleOrNull() ?: 0.0
                                        if (q > 0 && r > 0) totalAmount = (q * r).toInt().toString()
                                    }
                                },
                                label = { Text("পরিমাণ *") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(onNext = { unitFocus.requestFocus() }),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(qtyFocus)
                            )

                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                label = { Text("একক") },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { rateFocus.requestFocus() }),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(unitFocus)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Rate & Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = rate,
                                onValueChange = {
                                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                        rate = it
                                        val r = it.toDoubleOrNull() ?: 0.0
                                        val q = quantity.toDoubleOrNull() ?: 0.0
                                        if (q > 0 && r > 0) totalAmount = (q * r).toInt().toString()
                                    }
                                },
                                label = { Text("দর (প্রতি একক)") },
                                placeholder = { Text("৳") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(onNext = { totalFocus.requestFocus() }),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(rateFocus)
                            )

                            OutlinedTextField(
                                value = totalAmount,
                                onValueChange = {
                                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) totalAmount = it
                                },
                                label = { Text("মোট টাকা *") },
                                placeholder = { Text("৳") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(onNext = { noteFocus.requestFocus() }),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(totalFocus)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Note / Dealer
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("ডিলার বা চালানের বিবরণ (ঐচ্ছিক)") },
                            placeholder = { Text("যেমন: ভাই ভাই ট্রেডার্স চালান নং ১২") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { performSavePurchase() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(noteFocus)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Save Button
                        Button(
                            onClick = { performSavePurchase() },
                            enabled = canSave,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_direct_purchase_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ক্রয় হিসাব সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Purchase History List
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সকল মাল কেনা ইতিহাস",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${BengaliUtils.toBanglaDigits(allPurchases.size.toLong())} টি ক্রয়",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (allPurchases.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        MawaEmptyState(
                            icon = Icons.Default.ShoppingCart,
                            title = "এখনও কোনো কেনাকাটা নেই",
                            subtitle = "উপরে ডিলার থেকে কেনা মাল এন্ট্রি দিন"
                        )
                    }
                }
            } else {
                items(allPurchases, key = { it.id }) { tx ->
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
}
