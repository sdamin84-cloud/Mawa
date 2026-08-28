package com.example.mawa.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.local.entity.ShopSettingsEntity
import com.example.mawa.data.model.AccountingSummary
import com.example.mawa.ui.components.DailyClosureDialog
import com.example.mawa.ui.components.DenominationCounterBottomSheet
import com.example.mawa.ui.components.MawaEmptyState
import com.example.mawa.ui.components.MawaQuickActionButton
import com.example.mawa.ui.components.MawaSummaryRow
import com.example.mawa.ui.components.SmartRetailCalculatorBottomSheet
import com.example.mawa.ui.components.TransactionItemRow
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.ExpenseClassifier
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialNegativeContainer
import com.example.ui.theme.FinancialNeutral
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialPositiveContainer
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.FinancialWarningContainer
import com.example.ui.theme.MawaOutlineVariant
import com.example.ui.theme.MawaPillDark
import com.example.ui.theme.MawaPrimary
import com.example.ui.theme.MawaPrimaryContainer
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MawaViewModel,
    onOpenSales: () -> Unit,
    onOpenBaki: () -> Unit,
    onOpenJoma: () -> Unit,
    onOpenExpenseDrawer: () -> Unit,
    onOpenFordi: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedHomeDateMillis by viewModel.selectedHomeDateMillis.collectAsStateWithLifecycle()
    val summary by viewModel.selectedDateAccountingSummary.collectAsStateWithLifecycle()
    val settings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val displayTransactions by viewModel.selectedDateTransactions.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showDenominationSheet by remember { mutableStateOf(false) }
    var showCalculatorSheet by remember { mutableStateOf(false) }
    var showDailyClosureDialog by remember { mutableStateOf(false) }

    val isToday = remember(selectedHomeDateMillis) {
        val calSelected = Calendar.getInstance().apply { timeInMillis = selectedHomeDateMillis }
        val calToday = Calendar.getInstance()
        calSelected.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
        calSelected.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR)
    }

    var sabekInput by remember(settings?.openingBalance) {
        val initialSabek = settings?.openingBalance ?: summary.openingBalance
        mutableStateOf(
            if (initialSabek > 0) {
                if (initialSabek % 1.0 == 0.0) initialSabek.toLong().toString() else initialSabek.toString()
            } else ""
        )
    }

    var closingCashInput by remember {
        mutableStateOf("")
    }

    val todayExpensesTotal = summary.todayPurchases + summary.todayShopExpenses + summary.todayHomeWithdrawals
    val sabekValue = sabekInput.toDoubleOrNull() ?: 0.0
    val closingCashValue = closingCashInput.toDoubleOrNull() ?: 0.0

    // Automatic Daily Sales & Cash In Hand Calculation (দোকানের ঐতিহ্যবাহী নির্ভুল খাতা নিয়ম):
    // ১. যদি সমাপনী ক্যাশ লিখে দেওয়া থাকে: মোট বেচা = সমাপনী ক্যাশ + মোট খরচ - সাবেক
    // ২. যদি সমাপনী ক্যাশ না লেখা থাকে: হাতে থাকা ক্যাশ = সাবেক + নগদ বিক্রি + বাকি আদায় - মোট খরচ (খরচ বেশি হলে স্বাভাবিকভাবে মাইনাস আসবে)
    val autoComputedDailySale = if (closingCashInput.isNotBlank()) {
        (closingCashValue + todayExpensesTotal - sabekValue - summary.todayBakiCollection)
    } else {
        if (todayExpensesTotal > sabekValue) (todayExpensesTotal - sabekValue) else 0.0
    }

    // সর্বমোট বিক্রি (Explicit Sales থাকলে অথবা Reconciled Auto Sales + বাকি বিক্রি)
    val effectiveDailySales = if (summary.todayTotalSales > 0) {
        summary.todayTotalSales
    } else if (closingCashInput.isNotBlank()) {
        autoComputedDailySale + summary.todayBakiSales
    } else {
        summary.todayBakiSales
    }

    val effectiveCashSales = if (summary.todayCashSales > 0) {
        summary.todayCashSales
    } else {
        if (closingCashInput.isNotBlank()) autoComputedDailySale else 0.0
    }

    // প্রদর্শনের জন্য বর্তমান হাতে ক্যাশ (সাবেক ২৫০ এবং খরচ ২০০ হলে +৫০, খরচ ৩০০ হলে -৫০)
    val displayCashInHand = if (closingCashInput.isNotBlank()) {
        closingCashValue
    } else {
        sabekValue + summary.todayCashSales + summary.todayBakiCollection - todayExpensesTotal
    }

    // আনুমানিক মুনাফা / নিট লাভ (২০% গড় গ্রস মার্জিন)
    val estimatedProfit = effectiveDailySales * 0.20

    val greeting = BengaliUtils.getGreeting()
    val activeDateString = BengaliUtils.getFormattedTodayDate(selectedHomeDateMillis)

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedHomeDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.setSelectedHomeDate(it)
                        }
                        showDatePickerDialog = false
                    },
                    modifier = Modifier.testTag("confirm_date_btn")
                ) {
                    Text("ঠিক আছে", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("বাতিল")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Denomination Modal
    if (showDenominationSheet) {
        DenominationCounterBottomSheet(
            systemBalance = summary.totalCashInHand,
            onDismiss = { showDenominationSheet = false },
            onApplyCountedTotal = { countedAmount ->
                val formatted = if (countedAmount % 1.0 == 0.0) countedAmount.toLong().toString() else countedAmount.toString()
                closingCashInput = formatted
            }
        )
    }

    // Smart Retail Calculator Modal
    if (showCalculatorSheet) {
        SmartRetailCalculatorBottomSheet(
            customers = allCustomers,
            onDismiss = { showCalculatorSheet = false },
            onAddCashSale = { amount, note ->
                viewModel.recordSale(isCash = true, amount = amount, note = note)
            },
            onAddBakiSale = { custId, amount, note ->
                val custName = allCustomers.find { it.id == custId }?.name ?: "কাস্টমার"
                viewModel.recordSale(isCash = false, amount = amount, customerId = custId, customerName = custName, note = note)
            },
            onAddExpense = { amount, isShop, note ->
                viewModel.recordExpense(amount = amount, description = note, isHome = !isShop)
            },
            onAddFordiItem = { name, qty, unit, rate ->
                viewModel.addFordiItem(
                    productName = name,
                    plannedQty = qty,
                    unit = unit,
                    purchaseRate = rate,
                    sellingRate = rate * 1.15
                )
            },
            onApplyToCash = { amount ->
                val formatted = if (amount % 1.0 == 0.0) amount.toLong().toString() else amount.toString()
                closingCashInput = formatted
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. Header Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MAWA",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = " · ${settings?.shopName ?: "ডিজিটাল খাতা"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$greeting · দোকান খতিয়ান",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Quick Calculator Button in Top Header
                            IconButton(
                                onClick = { showCalculatorSheet = true },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .testTag("header_calc_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = "ক্যালকুলেটর",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            IconButton(
                                onClick = onOpenDrawer,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("menu_drawer_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "মেনু ড্রয়ার",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Interactive Date Navigator in Header
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous Day Arrow <
                            IconButton(
                                onClick = { viewModel.goToPreviousDay() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("header_prev_day_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "পূর্ববর্তী দিন",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Middle Date Label (Clickable Date Picker)
                            Surface(
                                onClick = { showDatePickerDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.testTag("header_date_picker_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = if (isToday) "আজ: $activeDateString" else activeDateString,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }

                            // Next Day Arrow >
                            IconButton(
                                onClick = { viewModel.goToNextDay() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("header_next_day_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "পরবর্তী দিন",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Historical Notice Banner if viewing past date
                    if (!isToday) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = FinancialWarningContainer,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, FinancialWarning.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
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
                                    Text(
                                        text = "⚠️ আপনি পূর্ববর্তী দিনের হিসাব দেখছেন",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Button(
                                    onClick = { viewModel.resetToToday() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("reset_to_today_banner_btn")
                                ) {
                                    Text("আজকে ফিরুন", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Unified Master Card: ক্যাশ মেলানো ও আজকের হিসাব সারাংশ
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Header with Premium Icon Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Assessment,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "আজকের ক্যাশ ও হিসাব সারাংশ",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = "মোট খরচ: ${BengaliUtils.formatTaka(todayExpensesTotal)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FinancialNegative
                                )
                            }

                            if (summary.openingBalance > 0) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFE8F5E9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "গতকালের সমাপনী ক্যাশ: ${BengaliUtils.formatTaka(summary.openingBalance)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                        Button(
                                            onClick = {
                                                val formatted = if (summary.openingBalance % 1.0 == 0.0) summary.openingBalance.toLong().toString() else summary.openingBalance.toString()
                                                sabekInput = formatted
                                                viewModel.updateOpeningBalance(summary.openingBalance)
                                            },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text(
                                                text = "সাবেক বসান",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Two side-by-side inputs: [ সাবেক (শুরুর ক্যাশ) ] and [ হাতে থাকা নগদ ]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = sabekInput,
                                    onValueChange = { input ->
                                        val digitsOnly = input.filter { it.isDigit() || it == '.' }
                                        sabekInput = digitsOnly
                                        val d = digitsOnly.toDoubleOrNull()
                                        if (d != null && d >= 0) {
                                            viewModel.updateOpeningBalance(d)
                                        }
                                    },
                                    label = { Text("সাবেক (শুরুর ক্যাশ)") },
                                    prefix = { Text("৳ ") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("home_sabek_input"),
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )

                                OutlinedTextField(
                                    value = closingCashInput,
                                    onValueChange = { input ->
                                        val digitsOnly = input.filter { it.isDigit() || it == '.' }
                                        closingCashInput = digitsOnly
                                    },
                                    label = { Text("হাতে থাকা নগদ") },
                                    prefix = { Text("৳ ") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("home_closing_cash_input"),
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Denomination Counter Hint Bar (ক্যাশবক্স মেলান - নোট গণনা টালি)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showDenominationSheet = true }
                                    .testTag("btn_open_denomination_hint"),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
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
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Payments,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "ক্যাশবক্স মেলান (নোট গণনা টালি)",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "১০০০, ৫০০, ১০০ টাকার নোট গুনে এক ক্লিকে ক্যাশ বসান",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "নোট গুনুন ➔",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 4-Tile Grid with Premium Visual Badges & Icons
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Tile 1: মোট বিক্রি
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFFF0F7FF),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "মোট বিক্রি (বেচা)",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Color(0xFF0369A1),
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color(0xFF0284C7).copy(alpha = 0.15f),
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.TrendingUp,
                                                            contentDescription = null,
                                                            tint = Color(0xFF0284C7),
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = BengaliUtils.formatTaka(effectiveDailySales),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF0284C7)
                                            )
                                            Text(
                                                text = "দোকানের বিক্রি",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF0284C7).copy(alpha = 0.85f)
                                            )
                                        }
                                    }

                                    // Tile 2: মোট খরচ
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        color = FinancialNegativeContainer.copy(alpha = 0.5f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "মোট খরচ",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = FinancialNegative,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Surface(
                                                    shape = CircleShape,
                                                    color = FinancialNegative.copy(alpha = 0.15f),
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.TrendingDown,
                                                            contentDescription = null,
                                                            tint = FinancialNegative,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = BengaliUtils.formatTaka(todayExpensesTotal),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Black,
                                                color = FinancialNegative
                                            )
                                            Text(
                                                text = "আজকের খরচ",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = FinancialNegative.copy(alpha = 0.85f)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Tile 3: সাবেক ক্যাশ
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "সাবেক ক্যাশ",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Surface(
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.History,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = BengaliUtils.formatTaka(sabekValue),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "শুরুর ব্যালেন্স",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Tile 4: হাতে ক্যাশ
                                    val isCashPositive = displayCashInHand >= 0
                                    val tile4Color = if (isCashPositive) FinancialPositive else FinancialNegative
                                    val tile4Container = if (isCashPositive) FinancialPositiveContainer.copy(alpha = 0.5f) else FinancialNegativeContainer.copy(alpha = 0.5f)
                                    val tile4Border = if (isCashPositive) Color(0xFFC8E6C9) else Color(0xFFFFCDD2)

                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        color = tile4Container,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, tile4Border)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "হাতে ক্যাশ",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = tile4Color,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Surface(
                                                    shape = CircleShape,
                                                    color = tile4Color.copy(alpha = 0.15f),
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Payments,
                                                            contentDescription = null,
                                                            tint = tile4Color,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = BengaliUtils.formatTaka(displayCashInHand),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Black,
                                                color = tile4Color
                                            )
                                            Text(
                                                text = if (isCashPositive) "নগদ স্থিতি" else "ঘাটতি / ঋণাত্মক",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = tile4Color.copy(alpha = 0.85f)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Step by step calculation note
                            val explanationText = if (sabekInput.isBlank() && closingCashInput.isBlank()) {
                                if (todayExpensesTotal > 0) {
                                    "কোনো সাবেক বা ক্যাশ ছাড়া সকল খরচ (${BengaliUtils.formatTaka(todayExpensesTotal)}) আজকের বিক্রি হিসেবে গণ্য"
                                } else {
                                    "খরচ এন্ট্রি বা দিন শেষের ক্যাশ লিখলে স্বয়ংক্রিয়ভাবে মোট বেচা হিসাব হবে"
                                }
                            } else {
                                "হিসাবের নিয়ম: (হাতে নগদ ${BengaliUtils.formatTaka(closingCashValue)} + মোট খরচ ${BengaliUtils.formatTaka(todayExpensesTotal)}) − সাবেক ${BengaliUtils.formatTaka(sabekValue)} = মোট বেচা ${BengaliUtils.formatTaka(autoComputedDailySale)}"
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = explanationText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Profit / Loss Banner with Icon
                            val isNegativeProfit = estimatedProfit < 0
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = if (isNegativeProfit) FinancialNegativeContainer.copy(alpha = 0.6f) else FinancialPositiveContainer.copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isNegativeProfit) Color(0xFFFFCDD2) else Color(0xFFA5D6A7)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isNegativeProfit) FinancialNegative.copy(alpha = 0.2f) else FinancialPositive.copy(alpha = 0.2f),
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (isNegativeProfit) Icons.Default.TrendingDown else Icons.Default.AutoGraph,
                                                    contentDescription = null,
                                                    tint = if (isNegativeProfit) FinancialNegative else FinancialPositive,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isNegativeProfit) "ঘাটতি / লোকসান (আনুমানিক)" else "লাভ হয়েছে (আনুমানিক নিট লাভ)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isNegativeProfit) FinancialNegative else FinancialPositive
                                        )
                                    }
                                    Text(
                                        text = BengaliUtils.formatTaka(estimatedProfit),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isNegativeProfit) FinancialNegative else FinancialPositive
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            // Compact Detail Rows with Premium Custom Icons
                            MawaSummaryRow(
                                label = "মোট বিক্রি (দোকানের বেচা)",
                                amount = effectiveDailySales,
                                amountColor = MaterialTheme.colorScheme.onSurface,
                                icon = Icons.Default.ShoppingCart,
                                iconTint = Color(0xFF0284C7),
                                onClick = onOpenSales
                            )
                            MawaSummaryRow(
                                label = "নগদ বিক্রি",
                                amount = effectiveCashSales,
                                amountColor = FinancialPositive,
                                icon = Icons.Default.Payments,
                                iconTint = FinancialPositive,
                                isSubRow = true
                            )
                            MawaSummaryRow(
                                label = "বাকি বিক্রি",
                                amount = summary.todayBakiSales,
                                amountColor = FinancialNegative,
                                icon = Icons.Default.Receipt,
                                iconTint = FinancialNegative,
                                isSubRow = true
                            )
                            MawaSummaryRow(
                                label = "বাকি আদায় (জমা)",
                                amount = summary.todayBakiCollection,
                                amountColor = FinancialPositive,
                                icon = Icons.Default.MoveToInbox,
                                iconTint = Color(0xFF00897B),
                                onClick = onOpenJoma
                            )
                            MawaSummaryRow(
                                label = "মোট খরচ (মাল কেনা + দোকান + বাড়ি)",
                                amount = todayExpensesTotal,
                                amountColor = FinancialNegative,
                                prefix = "−",
                                icon = Icons.Default.TrendingDown,
                                iconTint = FinancialNegative,
                                onClick = onOpenExpenseDrawer
                            )
                            MawaSummaryRow(
                                label = "মাল কেনা / পণ্য ক্রয়",
                                amount = summary.todayPurchases,
                                amountColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                prefix = "−",
                                icon = Icons.Default.ShoppingBag,
                                iconTint = Color(0xFF5C6BC0),
                                isSubRow = true,
                                onClick = onOpenFordi
                            )
                            MawaSummaryRow(
                                label = "দোকান পরিচালনা খরচ",
                                amount = summary.todayShopExpenses,
                                amountColor = FinancialNegative,
                                prefix = "−",
                                icon = Icons.Default.Store,
                                iconTint = Color(0xFFE53935),
                                isSubRow = true,
                                onClick = onOpenExpenseDrawer
                            )
                            MawaSummaryRow(
                                label = "বাড়ির জন্য খরচ / উত্তোলন",
                                amount = summary.todayHomeWithdrawals,
                                amountColor = FinancialWarning,
                                prefix = "−",
                                icon = Icons.Default.Home,
                                iconTint = Color(0xFFF57C00),
                                isSubRow = true,
                                onClick = onOpenExpenseDrawer
                            )
                        }
                    }
                }
            }

            // 3. Fast Quick Actions
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)) {
                        Text(
                            text = "দ্রুত হিসাব যোগ",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MawaQuickActionButton(
                                label = "বিক্রি",
                                icon = Icons.Default.ShoppingCart,
                                containerColor = FinancialPositiveContainer,
                                contentColor = FinancialPositive,
                                onClick = onOpenSales,
                                modifier = Modifier.weight(1f),
                                testTag = "quick_action_sale"
                            )
                            MawaQuickActionButton(
                                label = "বাকি দিন",
                                icon = Icons.Default.People,
                                containerColor = FinancialNegativeContainer,
                                contentColor = FinancialNegative,
                                onClick = onOpenBaki,
                                modifier = Modifier.weight(1f),
                                testTag = "quick_action_baki"
                            )
                            MawaQuickActionButton(
                                label = "জমা নিন",
                                icon = Icons.Default.ArrowDownward,
                                containerColor = FinancialPositiveContainer,
                                contentColor = FinancialPositive,
                                onClick = onOpenJoma,
                                modifier = Modifier.weight(1f),
                                testTag = "quick_action_joma"
                            )
                            MawaQuickActionButton(
                                label = "খরচ",
                                icon = Icons.Default.Store,
                                containerColor = FinancialWarningContainer,
                                contentColor = FinancialWarning,
                                onClick = onOpenExpenseDrawer,
                                modifier = Modifier.weight(1f),
                                testTag = "quick_action_expense"
                            )
                            MawaQuickActionButton(
                                label = "নোট টালি",
                                icon = Icons.Default.Payments,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary,
                                onClick = { showDenominationSheet = true },
                                modifier = Modifier.weight(1f),
                                testTag = "quick_action_denomination"
                            )
                            MawaQuickActionButton(
                                label = "ফর্দ",
                                icon = Icons.Default.Assignment,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.primary,
                                onClick = onOpenFordi,
                                modifier = Modifier.weight(1f),
                                testTag = "quick_action_fordi"
                            )
                            MawaQuickActionButton(
                                label = "রিপোর্ট",
                                icon = Icons.Default.Assessment,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                onClick = onOpenReports,
                                modifier = Modifier.weight(1f),
                                testTag = "quick_action_report"
                            )
                        }
                    }
                }
            }

            // 4. Baki Outstanding Summary
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onOpenBaki() },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "সর্বমোট বকেয়া বাকি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "আজকের নতুন বাকি: ${BengaliUtils.formatTaka(summary.todayNewBaki)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = BengaliUtils.formatTaka(summary.totalOutstandingBaki),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = FinancialNegative
                            )
                            Text(
                                text = "খাতা দেখুন →",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 6. Recent Activity (নির্বাচিত দিনের লেনদেন)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isToday) "আজকের হিসাব খতিয়ান" else "ঐ দিনের হিসাব খতিয়ান",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${BengaliUtils.toBanglaDigits(displayTransactions.size.toLong())} টি হিসাব",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (displayTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        MawaEmptyState(
                            icon = Icons.Default.Receipt,
                            title = if (isToday) "আজ এখনও কোনো হিসাব নেই" else "এই তারিখে কোনো হিসাব পাওয়া যায়নি",
                            subtitle = if (isToday) "উপরে বিক্রি, জমা বা খরচ যোগ করুন" else "তারিখ পরিবর্তন করে অন্যান্য দিনের হিসাব দেখতে পারেন",
                            actionLabel = if (isToday) "বিক্রি যোগ করুন" else null,
                            onActionClick = if (isToday) onOpenSales else null
                        )
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            displayTransactions.forEachIndexed { index, tx ->
                                TransactionItemRow(
                                    transaction = tx,
                                    onDelete = { viewModel.deleteTransaction(tx.id) }
                                )
                                if (index < displayTransactions.size - 1) {
                                    HorizontalDivider(
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Daily Closure Dialog
        if (showDailyClosureDialog) {
            DailyClosureDialog(
                dateMillis = selectedHomeDateMillis,
                openingBalance = sabekValue,
                cashSales = summary.todayCashSales,
                bakiSales = summary.todayBakiSales,
                bakiCollection = summary.todayBakiCollection,
                purchases = summary.todayPurchases,
                shopExpenses = summary.todayShopExpenses,
                homeWithdrawals = summary.todayHomeWithdrawals,
                onDismiss = { showDailyClosureDialog = false }
            )
        }
    }
}

