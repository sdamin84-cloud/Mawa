package com.example.mawa.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.util.BazaarItem
import com.example.mawa.util.BazaarListManager
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.HouseholdBillItem
import com.example.mawa.util.HouseholdBillsManager
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialWarning

// =========================================================================
// 1. Bazaar Checklist Section (বাজারের ফর্দ ও কেনাকাটার চেকলিস্ট)
// =========================================================================

@Composable
fun BazaarChecklistSection(
    onConvertToExpense: (amount: Double, description: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val manager = remember { BazaarListManager.getInstance(context) }
    val items by manager.items.collectAsStateWithLifecycle()

    var isExpanded by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var newItemQty by remember { mutableStateOf("") }
    var newItemPrice by remember { mutableStateOf("") }

    var showConvertToExpenseDialog by remember { mutableStateOf(false) }

    val totalItems = items.size
    val purchasedCount = items.count { it.isPurchased }
    val totalEstimated = items.sumOf { it.estimatedPrice }

    val quickPills = listOf(
        "চাল", "ডাল", "তেল", "পেঁয়াজ", "আলু", "রসুন",
        "মাছ", "মুরগি", "গরুর মাংস", "ডিম", "দুধ", "সবজি", "মরিচ", "মসলা", "লবণ", "সাবান"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FinancialPositive.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = FinancialPositive,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "🛒 বাজারের ফর্দ ও সদাই তালিকা",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (totalItems > 0)
                                "${BengaliUtils.toBanglaDigits(purchasedCount.toLong())}/${BengaliUtils.toBanglaDigits(totalItems.toLong())} টি কেনা হয়েছে" +
                                    (if (totalEstimated > 0) " • আনুমানিক ৳${BengaliUtils.toBanglaDigits(totalEstimated.toLong())}" else "")
                            else "কাঁচাবাজার ও সংসারের ফর্দ তৈরি করুন",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = if (isExpanded) "সংক্ষেপ" else "খুলুন",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick item chips
                    Text(
                        text = "ঝটপট আইটেম যোগ করুন:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickPills) { pill ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable {
                                    newItemName = pill
                                }
                            ) {
                                Text(
                                    text = "+ $pill",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Add item input row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newItemName,
                            onValueChange = { newItemName = it },
                            placeholder = { Text("আইটেমের নাম", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        OutlinedTextField(
                            value = newItemQty,
                            onValueChange = { newItemQty = it },
                            placeholder = { Text("পরিমাণ (২ কেজি)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1.1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        OutlinedTextField(
                            value = newItemPrice,
                            onValueChange = { newItemPrice = it },
                            placeholder = { Text("দাম", fontSize = 11.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.9f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        Button(
                            onClick = {
                                if (newItemName.isNotBlank()) {
                                    val est = newItemPrice.toDoubleOrNull() ?: 0.0
                                    manager.addItem(newItemName, newItemQty, est)
                                    newItemName = ""
                                    newItemQty = ""
                                    newItemPrice = ""
                                }
                            },
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "যুক্ত করুন")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Items List
                    if (items.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "বাজারের কোনো আইটেম যোগ করা হয়নি",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items.forEach { item ->
                                val bg by animateColorAsState(
                                    if (item.isPurchased) FinancialPositive.copy(alpha = 0.08f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                    label = "item_bg"
                                )

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = bg,
                                    border = BorderStroke(
                                        0.5.dp,
                                        if (item.isPurchased) FinancialPositive.copy(alpha = 0.3f)
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Checkbox(
                                                checked = item.isPurchased,
                                                onCheckedChange = { manager.togglePurchased(item.id) },
                                                colors = CheckboxDefaults.colors(checkedColor = FinancialPositive)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Column {
                                                Text(
                                                    text = item.name + (if (item.quantity.isNotBlank()) " (${item.quantity})" else ""),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (item.isPurchased) FontWeight.Normal else FontWeight.SemiBold,
                                                    textDecoration = if (item.isPurchased) TextDecoration.LineThrough else TextDecoration.None,
                                                    color = if (item.isPurchased) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (item.estimatedPrice > 0) {
                                                    Text(
                                                        text = "আনুমানিক: ৳${BengaliUtils.toBanglaDigits(item.estimatedPrice.toLong())}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }

                                        IconButton(
                                            onClick = { manager.deleteItem(item.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = "মুছুন",
                                                tint = MaterialTheme.colorScheme.outlineVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions Row: Convert to Expense & Clean
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showConvertToExpenseDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FinancialWarning)
                            ) {
                                Icon(Icons.Default.Paid, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "সংসার খরচে যোগ করুন",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (purchasedCount > 0) {
                                OutlinedButton(
                                    onClick = { manager.clearCompleted() },
                                    modifier = Modifier.height(40.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("কেনাগুলো মুছুন", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Convert Bazaar to Household Expense
    if (showConvertToExpenseDialog) {
        var expenseAmountStr by remember {
            mutableStateOf(if (totalEstimated > 0) totalEstimated.toInt().toString() else "")
        }
        var expenseNote by remember {
            val purchasedNames = items.filter { it.isPurchased }.joinToString(", ") { it.name }
            val defaultNote = if (purchasedNames.isNotBlank()) "বাজার: $purchasedNames" else "কাঁচাবাজারের সদাই"
            mutableStateOf(defaultNote)
        }

        AlertDialog(
            onDismissRequest = { showConvertToExpenseDialog = false },
            title = { Text("বাজারের খরচ হিসেবে যোগ করবেন?", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "মোট কেনা আইটেম: ${BengaliUtils.toBanglaDigits(purchasedCount.toLong())} টি\nবাজারের সঠিক মোট খরচ লিখে নিশ্চিত করুন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = expenseAmountStr,
                        onValueChange = { expenseAmountStr = it },
                        label = { Text("বাজারের মোট টাকা (৳)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = expenseNote,
                        onValueChange = { expenseNote = it },
                        label = { Text("বিবরণ / নোট") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = expenseAmountStr.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onConvertToExpense(amt, expenseNote)
                            manager.clearCompleted()
                            showConvertToExpenseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialWarning)
                ) {
                    Text("নিশ্চিত করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConvertToExpenseDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

// =========================================================================
// 2. Household Recurring Bills Section (মাসিক ইউটিলিটি ও বিল ট্র্যাকার)
// =========================================================================

@Composable
fun HouseholdBillsSection(
    onPayBill: (bill: HouseholdBillItem, amount: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val manager = remember { HouseholdBillsManager.getInstance(context) }
    val bills by manager.bills.collectAsStateWithLifecycle()
    val currentMonthKey = remember { HouseholdBillsManager.getCurrentMonthYearKey() }

    var isExpanded by remember { mutableStateOf(false) }
    var showAddBillDialog by remember { mutableStateOf(false) }
    var billToPay by remember { mutableStateOf<HouseholdBillItem?>(null) }
    var billToDelete by remember { mutableStateOf<HouseholdBillItem?>(null) }

    val totalBillsCount = bills.size
    val paidBills = bills.filter { it.isPaidThisMonth(currentMonthKey) }
    val unpaidBills = bills.filter { !it.isPaidThisMonth(currentMonthKey) }

    val paidAmount = paidBills.sumOf { it.defaultAmount }
    val unpaidAmount = unpaidBills.sumOf { it.defaultAmount }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FinancialWarning.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = FinancialWarning,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "🏠 মাসিক বিল ও ভাড়া ট্র্যাকার",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (unpaidBills.isNotEmpty())
                                "বাকি আছে ${BengaliUtils.toBanglaDigits(unpaidBills.size.toLong())} টি বিল (৳${BengaliUtils.toBanglaDigits(unpaidAmount.toLong())})"
                            else "চলতি মাসের সব বিল পরিশোধিত ✅",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (unpaidBills.isNotEmpty()) FinancialNegative else FinancialPositive,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = if (isExpanded) "সংক্ষেপ" else "খুলুন",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Summary Banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = FinancialPositive.copy(alpha = 0.08f),
                            border = BorderStroke(0.5.dp, FinancialPositive.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("পরিশোধিত", style = MaterialTheme.typography.labelSmall, color = FinancialPositive)
                                Text(
                                    text = BengaliUtils.formatTaka(paidAmount),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = FinancialPositive
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = FinancialNegative.copy(alpha = 0.08f),
                            border = BorderStroke(0.5.dp, FinancialNegative.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("বকেয়া বিল", style = MaterialTheme.typography.labelSmall, color = FinancialNegative)
                                Text(
                                    text = BengaliUtils.formatTaka(unpaidAmount),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = FinancialNegative
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // List of Bills
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        bills.forEach { bill ->
                            val isPaid = bill.isPaidThisMonth(currentMonthKey)
                            val icon = getBillIcon(bill.title)

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isPaid) FinancialPositive.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = BorderStroke(
                                    0.5.dp,
                                    if (isPaid) FinancialPositive.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isPaid) FinancialPositive.copy(alpha = 0.15f)
                                                    else MaterialTheme.colorScheme.surfaceVariant
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = if (isPaid) FinancialPositive else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = bill.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "প্রতি মাসের ${BengaliUtils.toBanglaDigits(bill.dueDay.toLong())} তারিখ • ৳${BengaliUtils.toBanglaDigits(bill.defaultAmount.toLong())}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isPaid) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = FinancialPositive.copy(alpha = 0.15f),
                                                modifier = Modifier.clickable {
                                                    manager.markUnpaid(bill.id)
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.Check, null, tint = FinancialPositive, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("পরিশোধিত", fontSize = 11.sp, color = FinancialPositive, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        } else {
                                            Button(
                                                onClick = { billToPay = bill },
                                                modifier = Modifier.height(32.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = FinancialWarning),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                                            ) {
                                                Text("বিল দিন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        IconButton(
                                            onClick = { billToDelete = bill },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "মুছুন",
                                                tint = MaterialTheme.colorScheme.outlineVariant,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showAddBillDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("＋ নতুন বিল যোগ করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Dialog: Confirm Paying Bill & Adding to Home Expense
    if (billToPay != null) {
        val bill = billToPay!!
        var payAmountStr by remember { mutableStateOf(bill.defaultAmount.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { billToPay = null },
            title = { Text("${bill.title} পরিশোধ", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "বিলটি পরিশোধ করে সংসারের খরচে যুক্ত করতে চান?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = payAmountStr,
                        onValueChange = { payAmountStr = it },
                        label = { Text("পরিশোধিত টাকা (৳)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = payAmountStr.toDoubleOrNull() ?: bill.defaultAmount
                        onPayBill(bill, amt)
                        manager.markPaid(bill.id, currentMonthKey)
                        billToPay = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialWarning)
                ) {
                    Text("পরিশোধ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { billToPay = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Dialog: Add New Bill
    if (showAddBillDialog) {
        var billTitle by remember { mutableStateOf("") }
        var billAmt by remember { mutableStateOf("") }
        var billDay by remember { mutableStateOf("10") }

        AlertDialog(
            onDismissRequest = { showAddBillDialog = false },
            title = { Text("নতুন মাসিক বিল যোগ", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = billTitle,
                        onValueChange = { billTitle = it },
                        label = { Text("বিলের নাম (যেমন: বাচ্চার স্কুল বেতন)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = billAmt,
                        onValueChange = { billAmt = it },
                        label = { Text("বিলের আনুমানিক টাকা (৳)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = billDay,
                        onValueChange = { billDay = it },
                        label = { Text("মাসের কত তারিখের মধ্যে দিতে হয়") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = billAmt.toDoubleOrNull() ?: 0.0
                        val day = billDay.toIntOrNull() ?: 10
                        if (billTitle.isNotBlank() && amt > 0) {
                            manager.addBill(billTitle, amt, day)
                            showAddBillDialog = false
                        }
                    }
                ) {
                    Text("যোগ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBillDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Dialog: Delete Bill
    if (billToDelete != null) {
        AlertDialog(
            onDismissRequest = { billToDelete = null },
            title = { Text("বিলটি মুছবেন?", fontWeight = FontWeight.Bold) },
            text = { Text("${billToDelete!!.title} তালিকা থেকে মুছে ফেলতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        manager.deleteBill(billToDelete!!.id)
                        billToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { billToDelete = null }) { Text("বাতিল") }
            }
        )
    }
}

private fun getBillIcon(title: String): ImageVector {
    val t = title.lowercase()
    return when {
        t.contains("ভাড়া") || t.contains("বাসা") -> Icons.Default.Home
        t.contains("বিদ্যুৎ") || t.contains("কারেন্ট") || t.contains("ইলেকট্রিক") -> Icons.Default.Bolt
        t.contains("গ্যাস") -> Icons.Default.LocalGasStation
        t.contains("পানি") -> Icons.Default.WaterDrop
        t.contains("ওয়াইফাই") || t.contains("নেট") || t.contains("ইন্টারনেট") -> Icons.Default.Wifi
        t.contains("স্কুল") || t.contains("কলেজ") || t.contains("পড়া") || t.contains("মাদ্রাসা") -> Icons.Default.School
        else -> Icons.Default.Paid
    }
}
