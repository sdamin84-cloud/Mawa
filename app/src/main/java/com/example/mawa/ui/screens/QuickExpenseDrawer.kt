package com.example.mawa.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.ui.components.MawaAmountInput
import com.example.mawa.ui.components.VoiceInputButton
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.ExpenseClassifier
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialNegativeContainer
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialPositiveContainer
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.FinancialWarningContainer
import com.example.ui.theme.MawaPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class QuickExpenseTarget {
    SHOP,      // দোকান পরিচালনা খরচ
    HOME,      // সংসার / বাড়ি খরচ
    PURCHASE   // পণ্য ক্রয় / মাল কেনা (স্টক)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickExpenseDrawer(
    viewModel: MawaViewModel,
    onDismiss: () -> Unit,
    initialTarget: QuickExpenseTarget = QuickExpenseTarget.SHOP,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expenseTarget by remember(initialTarget) { mutableStateOf(initialTarget) }
    var successNotice by remember { mutableStateOf<String?>(null) }
    var isManualSelection by remember { mutableStateOf(false) }
    var selectedTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }

    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "খরচ ও ক্রয় যোগ করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    VoiceInputButton(
                        onVoiceResult = { parsed ->
                            if (parsed.amount != null && parsed.amount > 0) {
                                amount = if (parsed.amount % 1.0 == 0.0) parsed.amount.toLong().toString() else parsed.amount.toString()
                            }
                            if (parsed.note.isNotBlank()) {
                                description = parsed.note
                            }
                            when (parsed.detectedType) {
                                com.example.mawa.data.local.entity.TransactionType.PURCHASE_DIRECT,
                                com.example.mawa.data.local.entity.TransactionType.PURCHASE_FORDI -> {
                                    expenseTarget = QuickExpenseTarget.PURCHASE
                                    isManualSelection = true
                                }
                                com.example.mawa.data.local.entity.TransactionType.EXPENSE_HOME -> {
                                    expenseTarget = QuickExpenseTarget.HOME
                                    isManualSelection = true
                                }
                                else -> {
                                    expenseTarget = QuickExpenseTarget.SHOP
                                    isManualSelection = true
                                }
                            }
                        },
                        buttonText = "মুখে বলুন",
                        isPillStyle = true,
                        testTag = "voice_btn_expense_drawer"
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_expense_drawer")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "বন্ধ করুন",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Success feedback bar
            if (successNotice != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = FinancialPositiveContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = FinancialPositive,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = successNotice ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = FinancialPositive,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Amount input
            MawaAmountInput(
                amount = amount,
                onAmountChange = {
                    amount = it
                    successNotice = null
                },
                label = "কত টাকা?",
                quickAmounts = listOf(20, 50, 100, 200, 500, 1000),
                testTag = "expense_amount_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Location / Target Toggle: [ দোকান খরচ ] [ বাড়ির জন্য ] [ পণ্য ক্রয় ]
            Text(
                text = "খরচ বা ক্রয়ের খাত",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Shop Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (expenseTarget == QuickExpenseTarget.SHOP) FinancialNegativeContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable {
                            expenseTarget = QuickExpenseTarget.SHOP
                            isManualSelection = true
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = if (expenseTarget == QuickExpenseTarget.SHOP) FinancialNegative else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "দোকান খরচ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (expenseTarget == QuickExpenseTarget.SHOP) FontWeight.Bold else FontWeight.Normal,
                            color = if (expenseTarget == QuickExpenseTarget.SHOP) FinancialNegative else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                // Home Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (expenseTarget == QuickExpenseTarget.HOME) FinancialWarningContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable {
                            expenseTarget = QuickExpenseTarget.HOME
                            isManualSelection = true
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = if (expenseTarget == QuickExpenseTarget.HOME) FinancialWarning else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "সংসার / বাড়ি",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (expenseTarget == QuickExpenseTarget.HOME) FontWeight.Bold else FontWeight.Normal,
                            color = if (expenseTarget == QuickExpenseTarget.HOME) FinancialWarning else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                // Purchase / Stock Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (expenseTarget == QuickExpenseTarget.PURCHASE) FinancialPositiveContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable {
                            expenseTarget = QuickExpenseTarget.PURCHASE
                            isManualSelection = true
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = if (expenseTarget == QuickExpenseTarget.PURCHASE) FinancialPositive else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "পণ্য ক্রয়",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (expenseTarget == QuickExpenseTarget.PURCHASE) FontWeight.Bold else FontWeight.Normal,
                            color = if (expenseTarget == QuickExpenseTarget.PURCHASE) FinancialPositive else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Description Note with Smart Keyword Classifier
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    successNotice = null
                    // Smart Auto-Classification
                    if (!isManualSelection && it.isNotBlank()) {
                        when (ExpenseClassifier.autoClassifyType(it)) {
                            ExpenseClassifier.TYPE_PURCHASE -> expenseTarget = QuickExpenseTarget.PURCHASE
                            ExpenseClassifier.TYPE_HOME -> expenseTarget = QuickExpenseTarget.HOME
                            ExpenseClassifier.TYPE_OPERATING_EXPENSE -> expenseTarget = QuickExpenseTarget.SHOP
                            else -> {}
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_description_input"),
                label = { Text("কী জন্য? (বিবরণ / পণ্যের নাম)") },
                placeholder = {
                    Text(
                        when (expenseTarget) {
                            QuickExpenseTarget.HOME -> "যেমন: বাজার, বিদ্যুৎ বিল, পড়াশোনা"
                            QuickExpenseTarget.PURCHASE -> "যেমন: চাল, ডাল, চিনি, তেল (স্টক ক্রয়)"
                            QuickExpenseTarget.SHOP -> "যেমন: চা-নাস্তা, পরিবহন, দোকান ভাড়া, কারেন্ট বিল"
                        }
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MawaPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Date Picker Component (Backdating & Custom Date Selection)
            val cal = remember(selectedTimestamp) {
                Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
            }
            val isToday = remember(selectedTimestamp) {
                val now = Calendar.getInstance()
                cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
            }
            val isYesterday = remember(selectedTimestamp) {
                val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
            }

            val dateDisplayString = remember(selectedTimestamp) {
                val formatter = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
                val formatted = formatter.format(Date(selectedTimestamp))
                when {
                    isToday -> "আজ ($formatted)"
                    isYesterday -> "গতকাল ($formatted)"
                    else -> formatted
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        val currentCal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val newCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                }
                                selectedTimestamp = newCal.timeInMillis
                            },
                            currentCal.get(Calendar.YEAR),
                            currentCal.get(Calendar.MONTH),
                            currentCal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                    .testTag("btn_select_expense_date"),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(
                    1.dp,
                    if (!isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (!isToday) Icons.Default.EditCalendar else Icons.Outlined.CalendarToday,
                            contentDescription = "তারিখ নির্বাচন",
                            tint = if (!isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "খরচের তারিখ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dateDisplayString,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (!isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (!isToday) "পূর্বের তারিখ" else "তারিখ বদলান",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Quick Date Chips (Today, Yesterday, Calendar)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Today chip
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            selectedTimestamp = System.currentTimeMillis()
                        },
                    color = if (isToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "আজ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Yesterday chip
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                            selectedTimestamp = yesterdayCal.timeInMillis
                        },
                    color = if (isYesterday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "গতকাল",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isYesterday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isYesterday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Custom calendar picker chip
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val currentCal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, y)
                                        set(Calendar.MONTH, m)
                                        set(Calendar.DAY_OF_MONTH, d)
                                    }
                                    selectedTimestamp = newCal.timeInMillis
                                },
                                currentCal.get(Calendar.YEAR),
                                currentCal.get(Calendar.MONTH),
                                currentCal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    color = if (!isToday && !isYesterday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = if (!isToday && !isYesterday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "অন্য তারিখ...",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (!isToday && !isYesterday) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isToday && !isYesterday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Save Button
            val amountNum = amount.toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    if (amountNum > 0) {
                        val savedAmount = amountNum
                        val savedTargetName: String
                        
                        when (expenseTarget) {
                            QuickExpenseTarget.PURCHASE -> {
                                savedTargetName = "পণ্য ক্রয়"
                                val note = description.ifBlank { "পণ্য ক্রয় (স্টক)" }
                                viewModel.recordDirectPurchase(
                                    productName = note,
                                    quantity = 1.0,
                                    unit = "পিস",
                                    rate = savedAmount,
                                    total = savedAmount,
                                    note = note,
                                    timestamp = selectedTimestamp
                                )
                            }
                            QuickExpenseTarget.HOME -> {
                                savedTargetName = "সংসার / বাড়ি"
                                val note = description.ifBlank { "বাড়ির খরচ" }
                                viewModel.recordExpense(
                                    amount = savedAmount,
                                    description = note,
                                    isHome = true,
                                    timestamp = selectedTimestamp
                                )
                            }
                            QuickExpenseTarget.SHOP -> {
                                savedTargetName = "দোকান খরচ"
                                val note = description.ifBlank { "দোকান পরিচালনা খরচ" }
                                viewModel.recordExpense(
                                    amount = savedAmount,
                                    description = note,
                                    isHome = false,
                                    timestamp = selectedTimestamp
                                )
                            }
                        }

                        // Clear inputs and keep drawer open for consecutive entries
                        amount = ""
                        description = ""
                        isManualSelection = false
                        val dateSuffix = if (!isToday) " (${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(selectedTimestamp))})" else ""
                        successNotice = "$savedTargetName ৳${BengaliUtils.toBanglaDigits(savedAmount)}$dateSuffix সংরক্ষিত হয়েছে"
                    }
                },
                enabled = amountNum > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_expense_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (expenseTarget) {
                        QuickExpenseTarget.PURCHASE -> FinancialPositive
                        QuickExpenseTarget.HOME -> FinancialWarning
                        QuickExpenseTarget.SHOP -> FinancialNegative
                    }
                )
            ) {
                Text(
                    text = "সংরক্ষণ করুন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
