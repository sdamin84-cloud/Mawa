package com.example.mawa.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.data.local.entity.CustomerEntity
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.MawaPrimary

enum class CalculatorPostingAction {
    NONE,
    CASH_SALE,
    BAKI_SALE,
    EXPENSE,
    FORDI_ITEM
}

/**
 * Smart Retail Calculator Bottom Sheet
 * Designed specifically for grocery / retail store owners with instant entry actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartRetailCalculatorBottomSheet(
    customers: List<CustomerEntity> = emptyList(),
    onDismiss: () -> Unit,
    onAddCashSale: (amount: Double, note: String) -> Unit,
    onAddBakiSale: (customerId: Long, amount: Double, note: String) -> Unit,
    onAddExpense: (amount: Double, isShopExpense: Boolean, note: String) -> Unit,
    onAddFordiItem: (productName: String, quantity: Double, unit: String, rate: Double) -> Unit,
    onApplyToCash: (amount: Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val calcPrefs = remember { context.getSharedPreferences("mawa_calculator_state_prefs", Context.MODE_PRIVATE) }

    var isExpandedMode by remember {
        mutableStateOf(calcPrefs.getBoolean("pref_calc_expanded", true))
    }
    var isAlwaysOn by remember {
        mutableStateOf(calcPrefs.getBoolean("pref_calc_always_on", false))
    }

    var currentTab by remember { 
        mutableIntStateOf(calcPrefs.getInt("pref_calc_tab", 0))
    }

    // Calculator Expression State (restored on reopen)
    var expression by remember { 
        mutableStateOf(calcPrefs.getString("pref_calc_expression", "") ?: "") 
    }
    var resultValue by remember { 
        mutableDoubleStateOf(calcPrefs.getFloat("pref_calc_result", 0.0f).toDouble()) 
    }
    var calculationNote by remember { 
        mutableStateOf(calcPrefs.getString("pref_calc_note", "") ?: "") 
    }

    // Unit rate state (restored on reopen)
    var unitRateInput by remember { 
        mutableStateOf(calcPrefs.getString("pref_calc_unit_rate", "") ?: "") 
    }
    var quantityInput by remember { 
        mutableStateOf(calcPrefs.getString("pref_calc_qty", "") ?: "") 
    }
    var selectedUnit by remember { 
        mutableStateOf(calcPrefs.getString("pref_calc_unit", "কেজি") ?: "কেজি") 
    }
    var unitItemName by remember { 
        mutableStateOf(calcPrefs.getString("pref_calc_item_name", "") ?: "") 
    }

    // Automatically persist all calculations and preferences in SharedPreferences
    LaunchedEffect(expression, resultValue, calculationNote, currentTab, unitRateInput, quantityInput, selectedUnit, unitItemName, isExpandedMode, isAlwaysOn) {
        calcPrefs.edit()
            .putString("pref_calc_expression", expression)
            .putFloat("pref_calc_result", resultValue.toFloat())
            .putString("pref_calc_note", calculationNote)
            .putInt("pref_calc_tab", currentTab)
            .putString("pref_calc_unit_rate", unitRateInput)
            .putString("pref_calc_qty", quantityInput)
            .putString("pref_calc_unit", selectedUnit)
            .putString("pref_calc_item_name", unitItemName)
            .putBoolean("pref_calc_expanded", isExpandedMode)
            .putBoolean("pref_calc_always_on", isAlwaysOn)
            .apply()
    }

    // Selected Post Action Form
    var activeAction by remember { mutableStateOf(CalculatorPostingAction.NONE) }
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(customers.firstOrNull()) }
    var isCustomerDropdownExpanded by remember { mutableStateOf(false) }
    var expenseTypeShop by remember { mutableStateOf(true) } // true: দোকান, false: বাড়ি

    // Evaluate live math expression safely
    fun evaluateMath(expr: String): Double {
        if (expr.isBlank()) return 0.0
        return try {
            val sanitized = expr.replace('×', '*').replace('÷', '/')
            val tokens = mutableListOf<String>()
            var currentNum = ""
            for (ch in sanitized) {
                if (ch.isDigit() || ch == '.') {
                    currentNum += ch
                } else if (ch in "+-*/") {
                    if (currentNum.isNotEmpty()) {
                        tokens.add(currentNum)
                        currentNum = ""
                    }
                    tokens.add(ch.toString())
                }
            }
            if (currentNum.isNotEmpty()) tokens.add(currentNum)

            if (tokens.isEmpty()) return 0.0

            // 1. Process * and /
            val intermediate = mutableListOf<String>()
            var i = 0
            while (i < tokens.size) {
                val token = tokens[i]
                if (token == "*" || token == "/") {
                    val prev = intermediate.removeAt(intermediate.size - 1).toDoubleOrNull() ?: 0.0
                    val next = if (i + 1 < tokens.size) tokens[i + 1].toDoubleOrNull() ?: 1.0 else 1.0
                    val res = if (token == "*") prev * next else (if (next != 0.0) prev / next else 0.0)
                    intermediate.add(res.toString())
                    i += 2
                } else {
                    intermediate.add(token)
                    i++
                }
            }

            // 2. Process + and -
            var total = intermediate.firstOrNull()?.toDoubleOrNull() ?: 0.0
            var j = 1
            while (j < intermediate.size) {
                val op = intermediate[j]
                val nextVal = if (j + 1 < intermediate.size) intermediate[j + 1].toDoubleOrNull() ?: 0.0 else 0.0
                if (op == "+") total += nextVal
                else if (op == "-") total -= nextVal
                j += 2
            }
            total
        } catch (_: Exception) {
            0.0
        }
    }

    // Update result when expression changes
    val activeCalculatedAmount = if (currentTab == 0) {
        val calculated = evaluateMath(expression)
        if (calculated != 0.0) calculated else resultValue
    } else {
        val rate = unitRateInput.toDoubleOrNull() ?: 0.0
        val qty = quantityInput.toDoubleOrNull() ?: 0.0
        if (selectedUnit == "গ্রাম") (rate * (qty / 1000.0)) else (rate * qty)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isExpandedMode) Modifier.fillMaxHeight(0.96f) else Modifier)
        ) {
            val containerHeight = maxHeight

            // Proportional key height calculation: scales buttons to cleanly fill vertical space in expanded mode
            val dynamicKeyHeight = if (isExpandedMode) {
                if (activeAction == CalculatorPostingAction.NONE) {
                    if (containerHeight != Dp.Infinity && containerHeight > 450.dp) {
                        // Subtract overhead of top header, tabs, OLED display, quick action bar, spacers and paddings
                        ((containerHeight - 325.dp - 36.dp - 20.dp) / 5f).coerceIn(72.dp, 94.dp)
                    } else {
                        78.dp
                    }
                } else {
                    62.dp
                }
            } else {
                54.dp
            }

            val dynamicKeyFontSize = if (isExpandedMode) {
                if (dynamicKeyHeight >= 82.dp) 28.sp else 24.sp
            } else {
                19.sp
            }

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isExpandedMode) Modifier.fillMaxHeight() else Modifier.wrapContentHeight())
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
            // Header with Always-On Mode, Fullscreen/Header Expand, and Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "স্মার্ট খুচরা ক্যালকুলেটর",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isExpandedMode) "হেড বার ভিউ · দ্রুত আঙুল হিসাব" else "হিসাব করে এক ক্লিকে খাতা ও খরচে এন্ট্রি",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 1. Always On / Pin Mode Toggle Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAlwaysOn) MawaPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (isAlwaysOn) MawaPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.clickable {
                            isAlwaysOn = !isAlwaysOn
                            val msg = if (isAlwaysOn) {
                                "সবসময় ক্যালকুলেটর চালু মোড সক্রিয়! অ্যাপ খুললে সবসময় দেখতে পাবেন।"
                            } else {
                                "সবসময় ক্যালকুলেটর মোড নিষ্ক্রিয় করা হয়েছে।"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "সবসময় স্ক্রিনে রাখুন",
                                tint = if (isAlwaysOn) MawaPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isAlwaysOn) "সবসময় চালু" else "পিন মোড",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAlwaysOn) MawaPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 2. Fullscreen / Expand to Header Bar Toggle
                    IconButton(
                        onClick = {
                            isExpandedMode = !isExpandedMode
                            val msg = if (isExpandedMode) "হেড বার পর্যন্ত বড় স্ক্রিন চালু হয়েছে" else "স্বাভাবিক সাইজ চালু হয়েছে"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpandedMode) Icons.Default.CloseFullscreen else Icons.Default.OpenInFull,
                            contentDescription = if (isExpandedMode) "স্বাভাবিক সাইজ" else "হেড বার পর্যন্ত বড় করুন",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // 3. Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("btn_close_calculator")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "বন্ধ করুন",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Selector: [ সাধারণ হিসাব ] | [ দর ও ওজন হিসাব ]
            TabRow(
                selectedTabIndex = currentTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    text = { Text("সাধারণ যোগ-গুণ", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    text = { Text("দর ও একক হিসাব", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Digital OLED Display Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = if (isExpandedMode) 16.dp else 12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    if (currentTab == 0) {
                        Text(
                            text = expression.ifBlank { "০" },
                            style = if (isExpandedMode) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF94A3B8),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        val rate = unitRateInput.ifBlank { "০" }
                        val qty = quantityInput.ifBlank { "০" }
                        Text(
                            text = "৳ $rate × $qty $selectedUnit",
                            style = if (isExpandedMode) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isExpandedMode) 6.dp else 4.dp))

                    Text(
                        text = BengaliUtils.formatTaka(activeCalculatedAmount),
                        style = if (isExpandedMode) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF38BDF8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Direct Action Bar (এক ক্লিকে খাতা/খরচ এন্ট্রি)
            Text(
                text = "হিসাবকৃত টাকা সরাসরি এন্ট্রি করুন:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Cash Sale Button
                CalculatorQuickActionButton(
                    label = "নগদ বিক্রি",
                    icon = Icons.Default.Payments,
                    color = FinancialPositive,
                    isSelected = activeAction == CalculatorPostingAction.CASH_SALE,
                    isExpandedMode = isExpandedMode,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        activeAction = if (activeAction == CalculatorPostingAction.CASH_SALE) CalculatorPostingAction.NONE else CalculatorPostingAction.CASH_SALE
                    }
                )

                // 2. Baki Sale Button
                CalculatorQuickActionButton(
                    label = "বাকি বিক্রি",
                    icon = Icons.Default.Person,
                    color = FinancialNegative,
                    isSelected = activeAction == CalculatorPostingAction.BAKI_SALE,
                    isExpandedMode = isExpandedMode,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        activeAction = if (activeAction == CalculatorPostingAction.BAKI_SALE) CalculatorPostingAction.NONE else CalculatorPostingAction.BAKI_SALE
                    }
                )

                // 3. Expense Button
                CalculatorQuickActionButton(
                    label = "খরচ",
                    icon = Icons.Default.Receipt,
                    color = FinancialWarning,
                    isSelected = activeAction == CalculatorPostingAction.EXPENSE,
                    isExpandedMode = isExpandedMode,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        activeAction = if (activeAction == CalculatorPostingAction.EXPENSE) CalculatorPostingAction.NONE else CalculatorPostingAction.EXPENSE
                    }
                )

                // 4. Fordi Button
                CalculatorQuickActionButton(
                    label = "ফর্দ/কেনা",
                    icon = Icons.Default.ShoppingCart,
                    color = MaterialTheme.colorScheme.primary,
                    isSelected = activeAction == CalculatorPostingAction.FORDI_ITEM,
                    isExpandedMode = isExpandedMode,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        activeAction = if (activeAction == CalculatorPostingAction.FORDI_ITEM) CalculatorPostingAction.NONE else CalculatorPostingAction.FORDI_ITEM
                    }
                )
            }

            // Expanded Post Confirmation Panels
            AnimatedVisibility(visible = activeAction != CalculatorPostingAction.NONE) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        when (activeAction) {
                            CalculatorPostingAction.CASH_SALE -> {
                                Text(
                                    text = "🟢 নগদ বিক্রি যোগ (${BengaliUtils.formatTaka(activeCalculatedAmount)})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = FinancialPositive
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = calculationNote,
                                    onValueChange = { calculationNote = it },
                                    placeholder = { Text("পণ্যের নাম বা বিবরণ (ঐচ্ছিক)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (activeCalculatedAmount > 0) {
                                            val note = calculationNote.ifBlank { "ক্যালকুলেটর নগদ বিক্রি" }
                                            onAddCashSale(activeCalculatedAmount, note)
                                            expression = ""
                                            resultValue = 0.0
                                            calculationNote = ""
                                            onDismiss()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("নগদ বিক্রি নিশ্চিত করুন", fontWeight = FontWeight.Bold)
                                }
                            }

                            CalculatorPostingAction.BAKI_SALE -> {
                                Text(
                                    text = "🔴 বাকি বিক্রি খাতা (${BengaliUtils.formatTaka(activeCalculatedAmount)})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = FinancialNegative
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                // Customer Dropdown
                                Box {
                                    OutlinedButton(
                                        onClick = { isCustomerDropdownExpanded = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = selectedCustomer?.name ?: "গ্রাহক নির্বাচন করুন",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = isCustomerDropdownExpanded,
                                        onDismissRequest = { isCustomerDropdownExpanded = false }
                                    ) {
                                        customers.forEach { customer ->
                                            DropdownMenuItem(
                                                text = { Text("${customer.name} (${customer.phone.ifBlank { "নম্বর নেই" }})") },
                                                onClick = {
                                                    selectedCustomer = customer
                                                    isCustomerDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = calculationNote,
                                    onValueChange = { calculationNote = it },
                                    placeholder = { Text("পণ্য বিবরণ (ঐচ্ছিক)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        selectedCustomer?.let { cust ->
                                            if (activeCalculatedAmount > 0) {
                                                val note = calculationNote.ifBlank { "ক্যালকুলেটর বাকি বিক্রি" }
                                                onAddBakiSale(cust.id, activeCalculatedAmount, note)
                                                expression = ""
                                                resultValue = 0.0
                                                calculationNote = ""
                                                onDismiss()
                                            }
                                        }
                                    },
                                    enabled = selectedCustomer != null && activeCalculatedAmount > 0,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = FinancialNegative),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("বাকি খাতায় যোগ করুন", fontWeight = FontWeight.Bold)
                                }
                            }

                            CalculatorPostingAction.EXPENSE -> {
                                Text(
                                    text = "🟠 খরচ এন্ট্রি (${BengaliUtils.formatTaka(activeCalculatedAmount)})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = FinancialWarning
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { expenseTypeShop = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = if (expenseTypeShop) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors()
                                    ) {
                                        Text("দোকানের খরচ", fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { expenseTypeShop = false },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = if (!expenseTypeShop) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer) else ButtonDefaults.outlinedButtonColors()
                                    ) {
                                        Text("বাড়ির খরচ/উত্তোলন", fontSize = 12.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = calculationNote,
                                    onValueChange = { calculationNote = it },
                                    placeholder = { Text("খরচের খাত (যেমন: চা, বিদ্যুৎ বিল, ভাড়া)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (activeCalculatedAmount > 0) {
                                            val note = calculationNote.ifBlank { if (expenseTypeShop) "দোকানের সাধারণ খরচ" else "বাড়ির খরচ" }
                                            onAddExpense(activeCalculatedAmount, expenseTypeShop, note)
                                            expression = ""
                                            resultValue = 0.0
                                            calculationNote = ""
                                            onDismiss()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = FinancialWarning),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("খরচ তালিকায় সেভ করুন", fontWeight = FontWeight.Bold)
                                }
                            }

                            CalculatorPostingAction.FORDI_ITEM -> {
                                Text(
                                    text = "🛒 বাজার ফর্দতে যোগ (${BengaliUtils.formatTaka(activeCalculatedAmount)})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = unitItemName,
                                    onValueChange = { unitItemName = it },
                                    placeholder = { Text("পণ্যের নাম (যেমন: চাল, তেল, ডাল)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val name = unitItemName.ifBlank { "বাজার আইটেম" }
                                        val qty = if (currentTab == 1) (quantityInput.toDoubleOrNull() ?: 1.0) else 1.0
                                        val rate = if (currentTab == 1) (unitRateInput.toDoubleOrNull() ?: activeCalculatedAmount) else activeCalculatedAmount
                                        val unit = if (currentTab == 1) selectedUnit else "টি"
                                        onAddFordiItem(name, qty, unit, rate)
                                        unitItemName = ""
                                        expression = ""
                                        resultValue = 0.0
                                        calculationNote = ""
                                        onDismiss()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("ফর্দ আইটেম হিসেবে যুক্ত করুন", fontWeight = FontWeight.Bold)
                                }
                            }

                            CalculatorPostingAction.NONE -> {}
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TAB 0: Numeric Keypad with dynamic enlarged key sizing
            if (currentTab == 0) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(if (isExpandedMode) 8.dp else 6.dp)
                ) {
                    val keyRows = listOf(
                        listOf("AC", "÷", "×", "DEL"),
                        listOf("7", "8", "9", "−"),
                        listOf("4", "5", "6", "+"),
                        listOf("1", "2", "3", "="),
                        listOf("0", "00", ".", "ক্যাশে বসান")
                    )

                    keyRows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(if (isExpandedMode) 8.dp else 6.dp)
                        ) {
                            row.forEach { key ->
                                val isOp = key in listOf("÷", "×", "−", "+", "=")
                                val isSpecial = key in listOf("AC", "C", "DEL", "ক্যাশে বসান")

                                Surface(
                                    modifier = Modifier
                                        .weight(if (key == "ক্যাশে বসান") 1.8f else 1f)
                                        .height(dynamicKeyHeight)
                                        .clip(RoundedCornerShape(if (isExpandedMode) 14.dp else 10.dp))
                                        .clickable {
                                            when (key) {
                                                "AC", "C" -> {
                                                    expression = ""
                                                    resultValue = 0.0
                                                }
                                                "DEL" -> {
                                                    if (expression.isNotEmpty()) {
                                                        expression = expression.dropLast(1)
                                                    }
                                                }
                                                "=" -> {
                                                    val res = evaluateMath(expression)
                                                    resultValue = res
                                                    expression = if (res % 1.0 == 0.0) res.toLong().toString() else String.format(java.util.Locale.US, "%.2f", res)
                                                }
                                                "÷" -> expression += "÷"
                                                "×" -> expression += "×"
                                                "−" -> expression += "-"
                                                "+" -> expression += "+"
                                                "ক্যাশে বসান" -> {
                                                    if (activeCalculatedAmount > 0) {
                                                        onApplyToCash(activeCalculatedAmount)
                                                        expression = ""
                                                        resultValue = 0.0
                                                        onDismiss()
                                                    }
                                                }
                                                else -> expression += key
                                            }
                                        },
                                    shape = RoundedCornerShape(if (isExpandedMode) 14.dp else 10.dp),
                                    color = when {
                                        key == "=" -> MawaPrimary
                                        key == "AC" || key == "C" -> FinancialNegative.copy(alpha = 0.14f)
                                        key == "ক্যাশে বসান" -> MaterialTheme.colorScheme.primaryContainer
                                        isOp -> MaterialTheme.colorScheme.secondaryContainer
                                        isSpecial -> MaterialTheme.colorScheme.surfaceVariant
                                        else -> MaterialTheme.colorScheme.surface
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (key == "=") MawaPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    ),
                                    tonalElevation = if (isExpandedMode) 2.dp else 1.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (key == "DEL") {
                                            Icon(
                                                imageVector = Icons.Default.Backspace,
                                                contentDescription = "ডিলিট",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(if (isExpandedMode) 28.dp else 20.dp)
                                            )
                                        } else if (key == "ক্যাশে বসান") {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Payments,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(if (isExpandedMode) 20.dp else 16.dp)
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "ক্যাশে বসান",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = if (isExpandedMode) 13.5.sp else 11.sp,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = key,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = if (isOp || key in listOf("AC", "=")) FontWeight.ExtraBold else FontWeight.Bold,
                                                fontSize = when {
                                                    key in listOf("÷", "×", "−", "+", "=") -> if (isExpandedMode) 30.sp else 22.sp
                                                    key == "AC" -> if (isExpandedMode) 24.sp else 18.sp
                                                    else -> dynamicKeyFontSize
                                                },
                                                color = when {
                                                    key == "=" -> Color.White
                                                    key == "AC" || key == "C" -> FinancialNegative
                                                    key == "ক্যাশে বসান" -> MaterialTheme.colorScheme.onPrimaryContainer
                                                    isOp -> MaterialTheme.colorScheme.onSecondaryContainer
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // TAB 1: Unit Rate Calculator (দর ও পরিমাণ)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = unitRateInput,
                            onValueChange = { unitRateInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("দর (প্রতি এককের দাম)") },
                            prefix = { Text("৳ ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = quantityInput,
                            onValueChange = { quantityInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("পরিমাণ ($selectedUnit)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Unit selector chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("কেজি", "গ্রাম", "লিটার", "পিস", "ডজন", "বস্তা").forEach { unit ->
                            val isSelected = selectedUnit == unit
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(if (isExpandedMode) 44.dp else 34.dp)
                                    .clickable { selectedUnit = unit }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = unit,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isExpandedMode) 13.sp else 11.sp,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Reset and Direct Copy to Cash button row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                unitRateInput = ""
                                quantityInput = ""
                                unitItemName = ""
                            },
                            modifier = Modifier
                                .weight(0.4f)
                                .height(if (isExpandedMode) 52.dp else 46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "রিসেট",
                                modifier = Modifier.size(if (isExpandedMode) 20.dp else 16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("মুছুন", fontSize = if (isExpandedMode) 14.sp else 12.sp)
                        }

                        Button(
                            onClick = {
                                if (activeCalculatedAmount > 0) {
                                    onApplyToCash(activeCalculatedAmount)
                                    unitRateInput = ""
                                    quantityInput = ""
                                    unitItemName = ""
                                    onDismiss()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(if (isExpandedMode) 52.dp else 46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(if (isExpandedMode) 20.dp else 16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ক্যাশবক্সে বসান (${BengaliUtils.formatTaka(activeCalculatedAmount)})",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isExpandedMode) 15.sp else 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun CalculatorQuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    isExpandedMode: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) color else color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, if (isSelected) color else color.copy(alpha = 0.3f)),
        modifier = modifier
            .height(if (isExpandedMode) 50.dp else 44.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else color,
                modifier = Modifier.size(if (isExpandedMode) 17.dp else 15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = if (isExpandedMode) 12.sp else 11.sp,
                color = if (isSelected) Color.White else color
            )
        }
    }
}
