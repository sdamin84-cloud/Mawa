package com.example.mawa.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.local.entity.PersonalTransactionEntity
import com.example.mawa.data.local.entity.PersonalTransactionType
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.model.AppMode
import com.example.mawa.data.model.CategorySpending
import com.example.mawa.data.model.PersonalSummary
import com.example.mawa.data.model.TimeFilter
import com.example.mawa.ui.components.HomeWithdrawalBottomSheet
import com.example.mawa.ui.components.MawaEmptyState
import com.example.mawa.ui.components.MawaTopBar
import com.example.mawa.ui.components.PersonalBudgetSettingDialog
import com.example.mawa.ui.components.PersonalExpenseReportDialog
import com.example.mawa.ui.components.PersonalCashFlowComparisonChart
import com.example.mawa.ui.components.SavingsRateGaugeCard
import com.example.mawa.ui.components.InteractiveCategoryDonutChart
import com.example.mawa.ui.components.DailyExpenseTrendBarChart
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.ExpenseReportEntry
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.FinancialWarningContainer
import com.example.ui.theme.MawaPrimary
import androidx.compose.material.icons.filled.Handshake
import com.example.mawa.ui.components.BazaarChecklistSection
import com.example.mawa.ui.components.HouseholdBillsSection
import com.example.mawa.ui.components.PersonalLoansSection
import com.example.mawa.ui.components.SavingsGoalsSection
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class PersonalSubTab(val label: String, val icon: ImageVector) {
    PERSONAL("ব্যক্তিগত খাতা", Icons.Default.Person),
    HOUSEHOLD("সংসার ও বাড়ি", Icons.Default.Home),
    LOANS("ধার-দেনা", Icons.Default.Handshake),
    BUDGET_REPORTS("বাজেট ও রিপোর্ট", Icons.Default.Analytics)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalScreen(
    viewModel: MawaViewModel,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null,
    onSwitchToBusiness: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val summary by viewModel.personalSummary.collectAsStateWithLifecycle()
    val timeFilter by viewModel.personalTimeFilter.collectAsStateWithLifecycle()
    val appMode by viewModel.currentAppMode.collectAsStateWithLifecycle()

    // Household expenses from business transactions
    val homeExpenses by viewModel.filteredHomeExpenses.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    var selectedSubTab by remember { mutableStateOf(PersonalSubTab.PERSONAL) }

    // Budget SharedPreferences
    val personalBudgetPrefs = remember { context.getSharedPreferences("mawa_personal_budget_pref", Context.MODE_PRIVATE) }
    var personalMonthlyBudget by remember {
        mutableStateOf(personalBudgetPrefs.getFloat("personal_monthly_budget", 0f).toDouble())
    }

    val homeBudgetPrefs = remember { context.getSharedPreferences("mawa_home_budget_pref", Context.MODE_PRIVATE) }
    var homeMonthlyBudget by remember {
        mutableStateOf(homeBudgetPrefs.getFloat("home_monthly_budget", 0f).toDouble())
    }

    // Dialog and Sheet States
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTypeForAdd by remember { mutableStateOf(PersonalTransactionType.EXPENSE) }
    var transactionToEdit by remember { mutableStateOf<PersonalTransactionEntity?>(null) }
    var transactionToDelete by remember { mutableStateOf<PersonalTransactionEntity?>(null) }
    var showPersonalBudgetDialog by remember { mutableStateOf(false) }
    var showHomeBudgetDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showHouseholdAddExpenseDialog by remember { mutableStateOf(false) }
    var showGoodsWithdrawalSheet by remember { mutableStateOf(false) }

    // Search and Category filter for Personal Ledger
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("সকল") }
    var selectedTypeFilter by remember { mutableStateOf<PersonalTransactionType?>(null) }

    // Filtered Personal Transactions
    val filteredPersonalTransactions = remember(summary.periodTransactions, searchQuery, selectedCategoryFilter, selectedTypeFilter) {
        summary.periodTransactions.filter { tx ->
            val matchesSearch = searchQuery.isBlank() ||
                tx.title.contains(searchQuery, ignoreCase = true) ||
                tx.category.contains(searchQuery, ignoreCase = true) ||
                tx.note.contains(searchQuery, ignoreCase = true) ||
                tx.amount.toString().contains(searchQuery)

            val matchesCategory = selectedCategoryFilter == "সকল" || tx.category == selectedCategoryFilter
            val matchesType = selectedTypeFilter == null || tx.type == selectedTypeFilter

            matchesSearch && matchesCategory && matchesType
        }
    }

    // Days calculation for Daily Burn Rate
    val calendar = Calendar.getInstance()
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    val remainingDays = (daysInMonth - currentDay + 1).coerceAtLeast(1)

    val personalDailyBurn = if (personalMonthlyBudget > summary.totalExpense && remainingDays > 0) {
        (personalMonthlyBudget - summary.totalExpense) / remainingDays
    } else 0.0

    val totalHomeExpense = remember(homeExpenses) { homeExpenses.sumOf { it.amount } }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            MawaTopBar(
                title = "ব্যক্তিগত ও সংসার",
                subtitle = "দৈনন্দিন খরচ, আয়, সঞ্চয় ও গৃহস্থালি খাতা",
                onMenuClick = onOpenDrawer
            )

            // Unified 3-Tab Segment: [ 👤 ব্যক্তিগত ] [ 🏡 সংসার ও বাড়ি ] [ 📊 বাজেট ও রিপোর্ট ]
            TabRow(
                selectedTabIndex = selectedSubTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                PersonalSubTab.values().forEach { tab ->
                    val isSelected = selectedSubTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { selectedSubTab = tab },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    )
                }
            }

            // Body Content based on active tab
            when (selectedSubTab) {
                PersonalSubTab.PERSONAL -> {
                    PersonalLedgerView(
                        summary = summary,
                        timeFilter = timeFilter,
                        appMode = appMode,
                        personalMonthlyBudget = personalMonthlyBudget,
                        remainingDays = remainingDays,
                        personalDailyBurn = personalDailyBurn,
                        filteredTransactions = filteredPersonalTransactions,
                        searchQuery = searchQuery,
                        selectedCategoryFilter = selectedCategoryFilter,
                        selectedTypeFilter = selectedTypeFilter,
                        onSearchChange = { searchQuery = it },
                        onCategoryFilterChange = { selectedCategoryFilter = it },
                        onTypeFilterChange = { selectedTypeFilter = it },
                        onTimeFilterSelected = { viewModel.setPersonalTimeFilter(it) },
                        onOpenBudgetDialog = { showPersonalBudgetDialog = true },
                        onOpenReportDialog = { showReportDialog = true },
                        onQuickAdd = { type ->
                            selectedTypeForAdd = type
                            showAddDialog = true
                        },
                        onEditTransaction = { transactionToEdit = it },
                        onDeleteTransaction = { transactionToDelete = it },
                        onSwitchToBusiness = onSwitchToBusiness,
                        onRecordSavings = { amount, title ->
                            viewModel.recordPersonalTransaction(
                                type = PersonalTransactionType.SAVINGS,
                                amount = amount,
                                title = title,
                                category = "সঞ্চয়"
                            )
                        }
                    )
                }

                PersonalSubTab.HOUSEHOLD -> {
                    HouseholdView(
                        homeExpenses = homeExpenses,
                        totalHomeExpense = totalHomeExpense,
                        homeMonthlyBudget = homeMonthlyBudget,
                        remainingDays = remainingDays,
                        onOpenAddExpense = { showHouseholdAddExpenseDialog = true },
                        onOpenGoodsWithdrawal = { showGoodsWithdrawalSheet = true },
                        onOpenBudgetDialog = { showHomeBudgetDialog = true },
                        onOpenReportDialog = { showReportDialog = true },
                        onDeleteExpense = { viewModel.deleteTransaction(it.id) },
                        onAddHouseholdExpense = { amount, desc ->
                            viewModel.recordExpense(amount = amount, description = desc, isHome = true)
                        }
                    )
                }

                PersonalSubTab.LOANS -> {
                    PersonalLoansSection(
                        onRecordRepaymentAsTransaction = { isIncome, amount, title ->
                            viewModel.recordPersonalTransaction(
                                type = if (isIncome) PersonalTransactionType.INCOME else PersonalTransactionType.EXPENSE,
                                amount = amount,
                                title = title,
                                category = if (isIncome) "ধার ফেরত" else "ধার পরিশোধ"
                            )
                        }
                    )
                }

                PersonalSubTab.BUDGET_REPORTS -> {
                    BudgetAndAnalyticsView(
                        personalSummary = summary,
                        personalMonthlyBudget = personalMonthlyBudget,
                        homeMonthlyBudget = homeMonthlyBudget,
                        totalHomeExpense = totalHomeExpense,
                        remainingDays = remainingDays,
                        onOpenPersonalBudgetDialog = { showPersonalBudgetDialog = true },
                        onOpenHomeBudgetDialog = { showHomeBudgetDialog = true },
                        onOpenFullReport = { showReportDialog = true }
                    )
                }
            }
        }
    }

    // Dialog: Add / Create Personal Transaction
    if (showAddDialog) {
        PersonalTransactionEntryDialog(
            initialType = selectedTypeForAdd,
            onDismiss = { showAddDialog = false },
            onSave = { type, amount, title, category, note, timestamp ->
                viewModel.recordPersonalTransaction(
                    type = type,
                    amount = amount,
                    title = title,
                    category = category,
                    note = note,
                    timestamp = timestamp
                )
                showAddDialog = false
            }
        )
    }

    // Dialog: Edit Personal Transaction
    if (transactionToEdit != null) {
        val tx = transactionToEdit!!
        PersonalTransactionEntryDialog(
            initialType = tx.type,
            initialAmount = tx.amount.toString(),
            initialTitle = tx.title,
            initialCategory = tx.category,
            initialNote = tx.note,
            initialTimestamp = tx.timestamp,
            isEditing = true,
            onDismiss = { transactionToEdit = null },
            onSave = { type, amount, title, category, note, timestamp ->
                viewModel.updatePersonalTransaction(
                    tx.copy(
                        type = type,
                        amount = amount,
                        title = title,
                        category = category,
                        note = note,
                        timestamp = timestamp
                    )
                )
                transactionToEdit = null
            }
        )
    }

    // Dialog: Delete Confirmation
    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("হিসাব মুছবেন?", fontWeight = FontWeight.Bold) },
            text = {
                Text("${tx.title} (${BengaliUtils.formatTaka(tx.amount)})-এর হিসাবটি মুছে ফেলতে চান?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePersonalTransaction(tx.id)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Dialog: Personal Monthly Budget
    if (showPersonalBudgetDialog) {
        PersonalBudgetSettingDialog(
            currentBudget = personalMonthlyBudget,
            title = "ব্যক্তিগত মাসিক বাজেট নির্ধারণ",
            onDismiss = { showPersonalBudgetDialog = false },
            onSaveBudget = { newBudget ->
                personalMonthlyBudget = newBudget
                personalBudgetPrefs.edit().putFloat("personal_monthly_budget", newBudget.toFloat()).apply()
            }
        )
    }

    // Dialog: Household Monthly Budget
    if (showHomeBudgetDialog) {
        PersonalBudgetSettingDialog(
            currentBudget = homeMonthlyBudget,
            title = "সংসারের মাসিক বাজেট নির্ধারণ",
            onDismiss = { showHomeBudgetDialog = false },
            onSaveBudget = { newBudget ->
                homeMonthlyBudget = newBudget
                homeBudgetPrefs.edit().putFloat("home_monthly_budget", newBudget.toFloat()).apply()
            }
        )
    }

    // Dialog: Full Statement & PDF Print
    if (showReportDialog) {
        val isPersonal = selectedSubTab == PersonalSubTab.PERSONAL
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

        val reportTitle = when (selectedSubTab) {
            PersonalSubTab.PERSONAL -> "ব্যক্তিগত আর্থিক বিবরণী"
            PersonalSubTab.HOUSEHOLD -> "সংসার ও পারিবারিক খরচ বিবরণী"
            PersonalSubTab.LOANS -> "ব্যক্তিগত ধার-দেনা বিবরণী"
            PersonalSubTab.BUDGET_REPORTS -> "পূর্ণাঙ্গ আর্থিক ও বাজেট অ্যানালিটিক্স"
        }

        val entries = if (isPersonal) {
            summary.periodTransactions.map { tx ->
                val typeName = when (tx.type) {
                    PersonalTransactionType.EXPENSE -> "খরচ"
                    PersonalTransactionType.INCOME -> "আয়"
                    PersonalTransactionType.SAVINGS -> "সঞ্চয়"
                }
                ExpenseReportEntry(
                    title = tx.title,
                    category = tx.category,
                    typeName = typeName,
                    amount = tx.amount,
                    dateFormatted = sdf.format(Date(tx.timestamp)),
                    note = tx.note
                )
            }
        } else {
            homeExpenses.map { tx ->
                val titleStr = tx.note.ifBlank { tx.productName ?: "সংসার খরচ" }
                ExpenseReportEntry(
                    title = titleStr,
                    category = "সংসার / বাড়ি",
                    typeName = "সংসার খরচ",
                    amount = tx.amount,
                    dateFormatted = sdf.format(Date(tx.timestamp)),
                    note = ""
                )
            }
        }

        val categories = if (isPersonal) {
            summary.categoryBreakdown.map { Pair(it.category, it.totalAmount) }
        } else {
            listOf(Pair("সংসার ও বাজার খরচ", totalHomeExpense))
        }

        val inc = if (isPersonal) summary.totalIncome else 0.0
        val exp = if (isPersonal) summary.totalExpense else totalHomeExpense
        val sav = if (isPersonal) summary.totalSavings else 0.0
        val bal = if (isPersonal) summary.netBalance else -totalHomeExpense
        val bud = if (isPersonal) personalMonthlyBudget else homeMonthlyBudget

        PersonalExpenseReportDialog(
            title = reportTitle,
            periodLabel = timeFilter.banglaLabel,
            totalIncome = inc,
            totalExpense = exp,
            totalSavings = sav,
            netBalance = bal,
            budgetAmount = bud,
            categories = categories,
            transactions = entries,
            onDismiss = { showReportDialog = false }
        )
    }

    // BottomSheet: Take Goods from Shop for Home
    if (showGoodsWithdrawalSheet) {
        HomeWithdrawalBottomSheet(
            products = allProducts,
            todayTransactions = allTransactions,
            onDismiss = { showGoodsWithdrawalSheet = false },
            onRecordCashWithdrawal = { amount, desc ->
                viewModel.recordExpense(amount = amount, description = desc, isHome = true)
                showGoodsWithdrawalSheet = false
            },
            onRecordGoodsWithdrawal = { pName, pId, qty, unit, rate, isDeduct, note ->
                viewModel.recordHomeGoodsWithdrawal(pName, pId, qty, unit, rate, isDeduct, note)
                showGoodsWithdrawalSheet = false
            }
        )
    }

    // Quick Household Add Cash Expense Dialog
    if (showHouseholdAddExpenseDialog) {
        var expenseAmount by remember { mutableStateOf("") }
        var expenseDesc by remember { mutableStateOf("বাজার খরচ") }
        val quickReasons = listOf("🥬 বাজার খরচ", "🐟 মাছ-মাংস", "⚡ বিদ্যুৎ বিল", "🏠 বাসা ভাড়া", "🎒 বাচ্চার বেতন", "💊 ওষুধ", "📶 ওয়াইফাই")

        AlertDialog(
            onDismissRequest = { showHouseholdAddExpenseDialog = false },
            title = { Text("বাড়ির জন্য খরচ যোগ", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = expenseAmount,
                        onValueChange = {
                            if (it.isEmpty() || it.all { ch -> ch.isDigit() }) expenseAmount = it
                        },
                        label = { Text("টাকার পরিমাণ (৳) *") },
                        placeholder = { Text("0") },
                        prefix = { Text("৳ ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = expenseDesc,
                        onValueChange = { expenseDesc = it },
                        label = { Text("কী জন্য খরচ *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickReasons) { r ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { expenseDesc = r },
                                color = if (expenseDesc == r) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = r,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = expenseAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.recordExpense(amount = amt, description = expenseDesc, isHome = true)
                            showHouseholdAddExpenseDialog = false
                        }
                    },
                    enabled = (expenseAmount.toDoubleOrNull() ?: 0.0) > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialWarning)
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHouseholdAddExpenseDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// View 1: Personal Ledger View
// -------------------------------------------------------------

@Composable
private fun PersonalLedgerView(
    summary: PersonalSummary,
    timeFilter: TimeFilter,
    appMode: AppMode,
    personalMonthlyBudget: Double,
    remainingDays: Int,
    personalDailyBurn: Double,
    filteredTransactions: List<PersonalTransactionEntity>,
    searchQuery: String,
    selectedCategoryFilter: String,
    selectedTypeFilter: PersonalTransactionType?,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (String) -> Unit,
    onTypeFilterChange: (PersonalTransactionType?) -> Unit,
    onTimeFilterSelected: (TimeFilter) -> Unit,
    onOpenBudgetDialog: () -> Unit,
    onOpenReportDialog: () -> Unit,
    onQuickAdd: (PersonalTransactionType) -> Unit,
    onEditTransaction: (PersonalTransactionEntity) -> Unit,
    onDeleteTransaction: (PersonalTransactionEntity) -> Unit,
    onSwitchToBusiness: (() -> Unit)?,
    onRecordSavings: (Double, String) -> Unit = { _, _ -> }
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("personal_screen_list")
    ) {
        // Business Mode switch banner (if both active)
        if (appMode == AppMode.BOTH && onSwitchToBusiness != null) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ব্যক্তিগত মোডে আছেন",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = onSwitchToBusiness,
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = "দোকানের খাতা →",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Hero Card
        item {
            PersonalMonthlyHeroCard(
                summary = summary,
                timeFilter = timeFilter,
                onTimeFilterSelected = onTimeFilterSelected,
                onQuickAdd = onQuickAdd,
                onOpenReport = onOpenReportDialog
            )
        }

        // Fast Entry Action Bar
        item {
            PersonalFastEntryBanner(
                onAddExpense = { onQuickAdd(PersonalTransactionType.EXPENSE) },
                onAddIncome = { onQuickAdd(PersonalTransactionType.INCOME) },
                onAddSavings = { onQuickAdd(PersonalTransactionType.SAVINGS) },
                onOpenReport = onOpenReportDialog
            )
        }

        // Category-wise Spending
        if (summary.categoryBreakdown.isNotEmpty()) {
            item {
                PersonalCategorySpendingSection(
                    categoryList = summary.categoryBreakdown,
                    totalExpense = summary.totalExpense
                )
            }
        }

        // Search and Filter Bar
        item {
            PersonalSearchAndFilterBar(
                searchQuery = searchQuery,
                onSearchChange = onSearchChange,
                selectedCategory = selectedCategoryFilter,
                onCategoryChange = onCategoryFilterChange,
                selectedType = selectedTypeFilter,
                onTypeChange = onTypeFilterChange
            )
        }

        // Ledger List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "হিসাব খতিয়ান",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${BengaliUtils.toBanglaDigits(filteredTransactions.size.toLong())} টি এন্ট্রি",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Transactions List
        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    MawaEmptyState(
                        icon = Icons.Outlined.AccountBalanceWallet,
                        title = if (searchQuery.isNotEmpty()) "কোনো মিল পাওয়া যায়নি" else "কোনো হিসাব যোগ করা নেই",
                        subtitle = "নিচের বাটন চেপে খরচ, আয় বা সঞ্চয় যোগ করুন",
                        actionLabel = "＋ হিসাব যোগ করুন",
                        onActionClick = { onQuickAdd(PersonalTransactionType.EXPENSE) }
                    )
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        filteredTransactions.forEachIndexed { index, tx ->
                            PersonalTransactionLedgerItem(
                                transaction = tx,
                                onEdit = { onEditTransaction(tx) },
                                onDelete = { onDeleteTransaction(tx) }
                            )
                            if (index < filteredTransactions.size - 1) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

// -------------------------------------------------------------
// View 2: Embedded Household View
// -------------------------------------------------------------

@Composable
private fun HouseholdView(
    homeExpenses: List<TransactionEntity>,
    totalHomeExpense: Double,
    homeMonthlyBudget: Double,
    remainingDays: Int,
    onOpenAddExpense: () -> Unit,
    onOpenGoodsWithdrawal: () -> Unit,
    onOpenBudgetDialog: () -> Unit,
    onOpenReportDialog: () -> Unit,
    onDeleteExpense: (TransactionEntity) -> Unit,
    onAddHouseholdExpense: (Double, String) -> Unit = { _, _ -> }
) {
    var searchQuery by remember { mutableStateOf("") }
    val dailyBurn = if (homeMonthlyBudget > totalHomeExpense && remainingDays > 0) {
        (homeMonthlyBudget - totalHomeExpense) / remainingDays
    } else 0.0

    val filteredList = remember(homeExpenses, searchQuery) {
        if (searchQuery.isBlank()) homeExpenses else {
            homeExpenses.filter {
                val fullText = "${it.note} ${it.productName ?: ""}"
                fullText.contains(searchQuery, ignoreCase = true) || it.amount.toString().contains(searchQuery)
            }
        }
    }

    val householdCategories = remember(homeExpenses, totalHomeExpense) {
        homeExpenses.groupBy { tx ->
            val noteLower = tx.note.lowercase()
            val prodLower = (tx.productName ?: "").lowercase()
            when {
                noteLower.contains("বাজার") || prodLower.contains("বাজার") || noteLower.contains("মাছ") || noteLower.contains("মাংস") || noteLower.contains("সবজি") -> "কাঁচাবাজার ও খাদ্য"
                noteLower.contains("মুদি") || noteLower.contains("চাল") || noteLower.contains("ডাল") || noteLower.contains("তেল") -> "মুদি বাজার"
                noteLower.contains("বিল") || noteLower.contains("বিদ্যুৎ") || noteLower.contains("গ্যাস") || noteLower.contains("পানি") || noteLower.contains("ওয়াইফাই") -> "ইউটিলিটি ও বিল"
                noteLower.contains("ভাড়া") -> "বাড়ি ভাড়া"
                noteLower.contains("চিকিৎসা") || noteLower.contains("ওষুধ") || noteLower.contains("ডাক্তার") -> "চিকিৎসা ও ওষুধ"
                noteLower.contains("পড়ালেখা") || noteLower.contains("স্কুল") || noteLower.contains("ফি") -> "সন্তানের লেখাপড়া"
                else -> "অন্যান্য সংসার খরচ"
            }
        }.map { (cat, list) ->
            val sum = list.sumOf { it.amount }
            CategorySpending(
                category = cat,
                totalAmount = sum,
                percentage = if (totalHomeExpense > 0) ((sum / totalHomeExpense) * 100).toFloat() else 0f,
                transactionCount = list.size
            )
        }.sortedByDescending { it.totalAmount }
    }

    val householdDailyTrend = remember(homeExpenses) {
        val sdfDay = SimpleDateFormat("dd MMM", Locale.getDefault())
        homeExpenses
            .groupBy { sdfDay.format(Date(it.timestamp)) }
            .map { (day, list) -> Pair(day, list.sumOf { it.amount }) }
            .takeLast(7)
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Household Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "এই মাসের মোট সংসার খরচ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = BengaliUtils.formatTaka(totalHomeExpense),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = FinancialWarning
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(FinancialWarningContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = FinancialWarning, modifier = Modifier.size(24.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    if (homeMonthlyBudget > 0) {
                        val ratio = (totalHomeExpense / homeMonthlyBudget).toFloat().coerceIn(0f, 1.5f)
                        val isOver = totalHomeExpense > homeMonthlyBudget
                        val percent = (totalHomeExpense / homeMonthlyBudget * 100).toInt()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("সংসার বাজেট: ${BengaliUtils.formatTaka(homeMonthlyBudget)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "${BengaliUtils.toBanglaDigits(percent.toLong())}% ব্যবহৃত",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isOver) FinancialNegative else FinancialWarning
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { ratio.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isOver) FinancialNegative else FinancialWarning,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = if (isOver) "বাজেট অতিক্রম: ${BengaliUtils.formatTaka(totalHomeExpense - homeMonthlyBudget)}"
                                else "অবশিষ্ট: ${BengaliUtils.formatTaka(homeMonthlyBudget - totalHomeExpense)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isOver) FinancialNegative else FinancialPositive
                            )
                            if (!isOver && remainingDays > 0) {
                                Text(
                                    text = "দৈনিক সর্বোচ্চ: ৳${BengaliUtils.toBanglaDigits(dailyBurn.toInt().toLong())}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("কোনো বাজেট সেট নেই", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedButton(onClick = onOpenBudgetDialog, modifier = Modifier.height(30.dp)) {
                                Text("বাজেট সেট করুন", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Action Hub
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("⚡ দ্রুত সংসারের অ্যাকশন", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onOpenAddExpense,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FinancialWarning)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("টাকা নিলাম", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onOpenGoodsWithdrawal,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Store, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("পণ্য নিলাম", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onOpenReportDialog,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Print, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("রিপোর্ট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Bazaar Checklist Section
        item {
            BazaarChecklistSection(
                onConvertToExpense = onAddHouseholdExpense
            )
        }

        // Household Monthly Recurring Bills Tracker
        item {
            HouseholdBillsSection(
                onPayBill = { bill, amount ->
                    onAddHouseholdExpense(amount, "বিল: ${bill.title}")
                }
            )
        }

        // Visual Graphical Analysis of Household Expenses
        if (householdCategories.isNotEmpty()) {
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    InteractiveCategoryDonutChart(
                        categories = householdCategories,
                        totalExpense = totalHomeExpense,
                        title = "সংসার খরচের খাতভিত্তিক চিত্র"
                    )
                }
            }
        }

        if (householdDailyTrend.isNotEmpty()) {
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    DailyExpenseTrendBarChart(
                        dailyExpenses = householdDailyTrend,
                        title = "সংসার ব্যয়ের দৈনিক ওঠানামা"
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("সংসার খরচ খুঁজুন...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, null) }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // List
        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    MawaEmptyState(
                        icon = Icons.Default.Home,
                        title = "সংসারের কোনো খরচ নেই",
                        subtitle = "দোকান থেকে সংসারের বাজার বা খরচ নিলে যুক্ত করুন",
                        actionLabel = "＋ খরচ যোগ করুন",
                        onActionClick = onOpenAddExpense
                    )
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        filteredList.forEachIndexed { index, tx ->
                            val timeStr = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date(tx.timestamp))
                            val displayName = tx.note.ifBlank { tx.productName ?: "সংসার খরচ" }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(displayName, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(timeStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(BengaliUtils.formatTaka(tx.amount), fontWeight = FontWeight.Bold, color = FinancialWarning)
                                IconButton(onClick = { onDeleteExpense(tx) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(16.dp))
                                }
                            }
                            if (index < filteredList.size - 1) {
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 14.dp))
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(70.dp)) }
    }
}

// -------------------------------------------------------------
// View 3: Budget & Analytics View
// -------------------------------------------------------------

@Composable
private fun BudgetAndAnalyticsView(
    personalSummary: PersonalSummary,
    personalMonthlyBudget: Double,
    homeMonthlyBudget: Double,
    totalHomeExpense: Double,
    remainingDays: Int,
    onOpenPersonalBudgetDialog: () -> Unit,
    onOpenHomeBudgetDialog: () -> Unit,
    onOpenFullReport: () -> Unit
) {
    val dailyPersonalTrend = remember(personalSummary.periodTransactions) {
        val sdfDay = SimpleDateFormat("dd MMM", Locale.getDefault())
        personalSummary.periodTransactions
            .filter { it.type == PersonalTransactionType.EXPENSE }
            .groupBy { sdfDay.format(Date(it.timestamp)) }
            .map { (day, list) -> Pair(day, list.sumOf { it.amount }) }
            .takeLast(7)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "📊 বাজেট ও আর্থিক পরিকল্পনা",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "বাজেটের ভেতরে খরচ রেখে সঞ্চয় বৃদ্ধি করুন",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Personal Budget Planner Card
        item {
            BudgetPlannerCard(
                title = "👤 ব্যক্তিগত মাসিক বাজেট",
                currentBudget = personalMonthlyBudget,
                totalSpent = personalSummary.totalExpense,
                remainingDays = remainingDays,
                accentColor = MaterialTheme.colorScheme.primary,
                onEditBudget = onOpenPersonalBudgetDialog
            )
        }

        // Household Budget Planner Card
        item {
            BudgetPlannerCard(
                title = "🏡 সংসার ও পারিবারিক বাজেট",
                currentBudget = homeMonthlyBudget,
                totalSpent = totalHomeExpense,
                remainingDays = remainingDays,
                accentColor = FinancialWarning,
                onEditBudget = onOpenHomeBudgetDialog
            )
        }

        // Cash Flow Comparison Chart
        item {
            PersonalCashFlowComparisonChart(
                totalIncome = personalSummary.totalIncome,
                totalExpense = personalSummary.totalExpense,
                totalHousehold = totalHomeExpense,
                totalSavings = personalSummary.totalSavings
            )
        }

        // Savings Rate Gauge
        item {
            SavingsRateGaugeCard(
                totalIncome = personalSummary.totalIncome,
                totalSavings = personalSummary.totalSavings,
                totalExpense = personalSummary.totalExpense
            )
        }

        // Category-wise Breakdown Donut
        if (personalSummary.categoryBreakdown.isNotEmpty()) {
            item {
                InteractiveCategoryDonutChart(
                    categories = personalSummary.categoryBreakdown,
                    totalExpense = personalSummary.totalExpense,
                    title = "ব্যক্তিগত খরচের খাতভিত্তিক বিশ্লেষণ"
                )
            }
        }

        if (dailyPersonalTrend.isNotEmpty()) {
            item {
                DailyExpenseTrendBarChart(
                    dailyExpenses = dailyPersonalTrend,
                    title = "ব্যক্তিগত খরচের দৈনিক প্রবণতা"
                )
            }
        }

        // Net Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "আর্থিক সারসংক্ষেপ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("মোট ব্যক্তিগত আয়", style = MaterialTheme.typography.bodyMedium)
                        Text(BengaliUtils.formatTaka(personalSummary.totalIncome), fontWeight = FontWeight.Bold, color = FinancialPositive)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("মোট ব্যক্তিগত খরচ", style = MaterialTheme.typography.bodyMedium)
                        Text(BengaliUtils.formatTaka(personalSummary.totalExpense), fontWeight = FontWeight.Bold, color = FinancialNegative)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("মোট সংসার খরচ", style = MaterialTheme.typography.bodyMedium)
                        Text(BengaliUtils.formatTaka(totalHomeExpense), fontWeight = FontWeight.Bold, color = FinancialWarning)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("মোট সঞ্চয়", style = MaterialTheme.typography.bodyMedium)
                        Text(BengaliUtils.formatTaka(personalSummary.totalSavings), fontWeight = FontWeight.Bold, color = FinancialWarning)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onOpenFullReport,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
                    ) {
                        Icon(Icons.Default.Print, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("রিপোর্ট ও PDF প্রিন্ট", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(60.dp)) }
    }
}

@Composable
private fun BudgetPlannerCard(
    title: String,
    currentBudget: Double,
    totalSpent: Double,
    remainingDays: Int,
    accentColor: Color,
    onEditBudget: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onEditBudget, modifier = Modifier.height(30.dp)) {
                    Text(if (currentBudget > 0) "পরিবর্তন" else "সেট করুন", fontWeight = FontWeight.Bold, color = accentColor)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (currentBudget > 0) {
                val ratio = (totalSpent / currentBudget).toFloat().coerceIn(0f, 1.5f)
                val percent = (totalSpent / currentBudget * 100).toInt()
                val isOver = totalSpent > currentBudget
                val remaining = currentBudget - totalSpent
                val dailyAllowance = if (!isOver && remainingDays > 0) remaining / remainingDays else 0.0

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("বাজেট: ${BengaliUtils.formatTaka(currentBudget)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${BengaliUtils.toBanglaDigits(percent.toLong())}% ব্যবহৃত",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOver) FinancialNegative else accentColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { ratio.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isOver) FinancialNegative else accentColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = if (isOver) "বাজেট ছাড়িয়েছে ${BengaliUtils.formatTaka(totalSpent - currentBudget)}"
                        else "অবশিষ্ট: ${BengaliUtils.formatTaka(remaining)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOver) FinancialNegative else FinancialPositive
                    )
                    if (!isOver && remainingDays > 0) {
                        Text(
                            text = "দৈনিক সর্বোচ্চ: ৳${BengaliUtils.toBanglaDigits(dailyAllowance.toInt().toLong())}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    text = "কোনো বাজেট নির্ধারণ করা হয়নি। বাজেট নির্ধারণ করলে অতিরিক্ত খরচ নিয়ন্ত্রণ করা সহজ হয়।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Component: Personal Monthly Hero Card
// -------------------------------------------------------------

@Composable
private fun PersonalMonthlyHeroCard(
    summary: PersonalSummary,
    timeFilter: TimeFilter,
    onTimeFilterSelected: (TimeFilter) -> Unit,
    onQuickAdd: (PersonalTransactionType) -> Unit,
    onOpenReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Main Top Title: "এই মাসের খরচ" + Report Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "${timeFilter.banglaLabel} খরচ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = BengaliUtils.formatTaka(summary.totalExpense),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = if (summary.netBalance >= 0) FinancialPositive.copy(alpha = 0.12f) else FinancialNegative.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (summary.netBalance >= 0) Icons.Default.TrendingUp else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (summary.netBalance >= 0) FinancialPositive else FinancialNegative,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "অবশিষ্ট: ${BengaliUtils.formatTaka(summary.netBalance)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.netBalance >= 0) FinancialPositive else FinancialNegative
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Time Filter Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TimeFilter.values()) { filter ->
                    val isSelected = timeFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTimeFilterSelected(filter) },
                        label = {
                            Text(
                                text = filter.banglaLabel,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(14.dp))

            // 3-Metric Row: মোট আয় | মোট খরচ | সঞ্চয়
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn(
                    title = "মোট আয়",
                    amount = summary.totalIncome,
                    color = FinancialPositive,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                )

                MetricColumn(
                    title = "মোট খরচ",
                    amount = summary.totalExpense,
                    color = FinancialNegative,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                )

                MetricColumn(
                    title = "সঞ্চয়",
                    amount = summary.totalSavings,
                    color = FinancialWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Component: Personal Monthly Budget Health Card
// -------------------------------------------------------------

@Composable
private fun PersonalMonthlyBudgetCard(
    budget: Double,
    totalExpense: Double,
    remainingDays: Int,
    dailyBurnAllowance: Double,
    onOpenBudgetDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            if (budget > 0) {
                val ratio = (totalExpense / budget).toFloat().coerceIn(0f, 1.5f)
                val percent = (totalExpense / budget * 100).toInt()
                val isOver = totalExpense > budget
                val remaining = budget - totalExpense

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎯 মাসিক বাজেট লক্ষ্য",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${BengaliUtils.formatTaka(totalExpense)} / ${BengaliUtils.formatTaka(budget)} (${BengaliUtils.toBanglaDigits(percent.toLong())}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isOver) FinancialNegative else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "বদলান",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onOpenBudgetDialog() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { ratio.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isOver) FinancialNegative else if (ratio > 0.8f) FinancialWarning else FinancialPositive,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isOver) "⚠️ বাজেট অতিক্রম করেছে ${BengaliUtils.formatTaka(totalExpense - budget)}"
                        else "✅ অবশিষ্ট বাজেট: ${BengaliUtils.formatTaka(remaining)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOver) FinancialNegative else FinancialPositive,
                        fontWeight = FontWeight.Medium
                    )

                    if (!isOver && remainingDays > 0) {
                        Text(
                            text = "দৈনিক সর্বোচ্চ: ৳${BengaliUtils.toBanglaDigits(dailyBurnAllowance.toInt().toLong())}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎯 কোনো বাজেট সেট করা নেই",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = onOpenBudgetDialog,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("বাজেট সেট করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Component: Metric Column
// -------------------------------------------------------------

@Composable
private fun MetricColumn(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = BengaliUtils.formatTaka(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// -------------------------------------------------------------
// Component: Fast Entry Banner
// -------------------------------------------------------------

@Composable
private fun PersonalFastEntryBanner(
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onAddSavings: () -> Unit,
    onOpenReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "＋ দ্রুত হিসাব যোগ করুন",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = onOpenReport,
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("রিপোর্ট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickTypeButton(
                    label = "খরচ",
                    icon = Icons.Default.ArrowDownward,
                    color = FinancialNegative,
                    onClick = onAddExpense,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_add_expense"
                )

                QuickTypeButton(
                    label = "আয়",
                    icon = Icons.Default.ArrowUpward,
                    color = FinancialPositive,
                    onClick = onAddIncome,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_add_income"
                )

                QuickTypeButton(
                    label = "সঞ্চয়",
                    icon = Icons.Default.Savings,
                    color = FinancialWarning,
                    onClick = onAddSavings,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_add_savings"
                )
            }
        }
    }
}

@Composable
private fun QuickTypeButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// -------------------------------------------------------------
// Component: Category Spending Section
// -------------------------------------------------------------

@Composable
private fun PersonalCategorySpendingSection(
    categoryList: List<CategorySpending>,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                Text(
                    text = "ক্যাটাগরি ভিত্তিক খরচ",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "মোট: ${BengaliUtils.formatTaka(totalExpense)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            categoryList.take(6).forEach { cat ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = getCategoryIcon(cat.category),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cat.category,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${BengaliUtils.toBanglaDigits(cat.percentage.toInt().toLong())}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = BengaliUtils.formatTaka(cat.totalAmount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { (cat.percentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Component: Search and Filter Bar
// -------------------------------------------------------------

@Composable
private fun PersonalSearchAndFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    selectedType: PersonalTransactionType?,
    onTypeChange: (PersonalTransactionType?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("হিসাব খুঁজুন (যেমন: নাস্তা, রিকশা, বেতন)...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "মুছুন")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("personal_search_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Type Filter Chips: [ সকল ধরন ] [ খরচ ] [ আয় ] [ সঞ্চয় ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val typeItems = listOf(
                null to "সকল ধরন",
                PersonalTransactionType.EXPENSE to "খরচ",
                PersonalTransactionType.INCOME to "আয়",
                PersonalTransactionType.SAVINGS to "সঞ্চয়"
            )

            typeItems.forEach { (type, label) ->
                val isSelected = selectedType == type
                val color = when (type) {
                    PersonalTransactionType.EXPENSE -> FinancialNegative
                    PersonalTransactionType.INCOME -> FinancialPositive
                    PersonalTransactionType.SAVINGS -> FinancialWarning
                    null -> MaterialTheme.colorScheme.primary
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTypeChange(type) },
                    color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = if (isSelected) BorderStroke(1.dp, color) else null
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Category Filter Chips
        val categories = listOf("সকল", "খাবার", "যাতায়াত", "পড়াশোনা", "চিকিৎসা", "বিল", "কেনাকাটা", "মোবাইল", "বেতন", "সঞ্চয়", "অন্যান্য")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCategoryChange(cat) },
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Component: Personal Transaction Ledger Item
// -------------------------------------------------------------

@Composable
private fun PersonalTransactionLedgerItem(
    transaction: PersonalTransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typeColor = when (transaction.type) {
        PersonalTransactionType.EXPENSE -> FinancialNegative
        PersonalTransactionType.INCOME -> FinancialPositive
        PersonalTransactionType.SAVINGS -> FinancialWarning
    }

    val icon = when (transaction.type) {
        PersonalTransactionType.EXPENSE -> getCategoryIcon(transaction.category)
        PersonalTransactionType.INCOME -> Icons.Default.TrendingUp
        PersonalTransactionType.SAVINGS -> Icons.Default.Savings
    }

    val timeStr = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date(transaction.timestamp))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(typeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = " • $timeStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (transaction.note.isNotBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = when (transaction.type) {
                    PersonalTransactionType.EXPENSE -> "৳${BengaliUtils.toBanglaDigits(transaction.amount.toLong())}"
                    PersonalTransactionType.INCOME -> "+৳${BengaliUtils.toBanglaDigits(transaction.amount.toLong())}"
                    PersonalTransactionType.SAVINGS -> "৳${BengaliUtils.toBanglaDigits(transaction.amount.toLong())}"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = typeColor
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "মুছে ফেলুন",
                    tint = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Component: Personal Transaction Entry Dialog
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalTransactionEntryDialog(
    initialType: PersonalTransactionType = PersonalTransactionType.EXPENSE,
    initialAmount: String = "",
    initialTitle: String = "",
    initialCategory: String = "",
    initialNote: String = "",
    initialTimestamp: Long = System.currentTimeMillis(),
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (type: PersonalTransactionType, amount: Double, title: String, category: String, note: String, timestamp: Long) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var amount by remember { mutableStateOf(initialAmount) }
    var title by remember { mutableStateOf(initialTitle) }
    var category by remember { mutableStateOf(initialCategory) }
    var note by remember { mutableStateOf(initialNote) }
    var timestamp by remember { mutableStateOf(initialTimestamp) }

    val context = LocalContext.current

    // Category lists based on type
    val expenseCategories = listOf("খাবার", "যাতায়াত", "পড়াশোনা", "চিকিৎসা", "বিল", "কেনাকাটা", "মোবাইল", "বাজারসদাই", "অন্যান্য")
    val incomeCategories = listOf("বেতন", "ব্যবসা/উত্তোলন", "উপহার", "ফ্রিল্যান্সিং", "বাড়ি ভাড়া", "অন্যান্য")
    val savingsCategories = listOf("ব্যাংক", "ডিপিএস", "নগদ", "সোনা/জমি", "অন্যান্য")

    val currentCategories = when (selectedType) {
        PersonalTransactionType.EXPENSE -> expenseCategories
        PersonalTransactionType.INCOME -> incomeCategories
        PersonalTransactionType.SAVINGS -> savingsCategories
    }

    if (category.isBlank() && currentCategories.isNotEmpty()) {
        category = currentCategories.first()
    }

    val quickExpenseTitles = listOf("নাস্তা", "দুপুরের খাবার", "রিকশা ভাড়া", "বাস ভাড়া", "মোবাইল রিচার্জ", "বাজার", "ঔষধ")
    val quickIncomeTitles = listOf("মাসিক বেতন", "দোকান থেকে উত্তোলন", "বোনাস", "উপহার", "বকেয়া প্রাপ্তি")
    val quickSavingsTitles = listOf("ব্যাংকে জমা", "ডিপিএস কিস্তি", "সঞ্চয়পত্র", "জরুরি ফান্ড")

    val activeQuickTitles = when (selectedType) {
        PersonalTransactionType.EXPENSE -> quickExpenseTitles
        PersonalTransactionType.INCOME -> quickIncomeTitles
        PersonalTransactionType.SAVINGS -> quickSavingsTitles
    }

    val dateFormatted = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(Date(timestamp))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "হিসাব পরিবর্তন করুন" else "ব্যক্তিগত হিসাব যোগ",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Type Selector Tabs: [ খরচ ] [ আয় ] [ সঞ্চয় ]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PersonalTransactionType.values().forEach { type ->
                        val isSelected = selectedType == type
                        val color = when (type) {
                            PersonalTransactionType.EXPENSE -> FinancialNegative
                            PersonalTransactionType.INCOME -> FinancialPositive
                            PersonalTransactionType.SAVINGS -> FinancialWarning
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) color else Color.Transparent)
                                .clickable {
                                    selectedType = type
                                    category = when (type) {
                                        PersonalTransactionType.EXPENSE -> expenseCategories.first()
                                        PersonalTransactionType.INCOME -> incomeCategories.first()
                                        PersonalTransactionType.SAVINGS -> savingsCategories.first()
                                    }
                                }
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.banglaLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount Field
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            amount = it
                        }
                    },
                    label = { Text("টাকার পরিমাণ (৳) *") },
                    placeholder = { Text("0") },
                    prefix = { Text("৳ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_personal_amount")
                )

                // Quick Amount Suggestion Chips
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(listOf(50, 100, 200, 500, 1000)) { quickVal ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val current = amount.toDoubleOrNull() ?: 0.0
                                    amount = (current + quickVal).toInt().toString()
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "+${BengaliUtils.toBanglaDigits(quickVal.toLong())}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("কী জন্য *") },
                    placeholder = { Text("যেমন: নাস্তা, রিকশা ভাড়া, ইত্যাদি") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_personal_title")
                )

                // Quick Title Suggestions
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(activeQuickTitles) { suggestion ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { title = suggestion },
                            color = if (title == suggestion) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (title == suggestion) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Chips
                Text(
                    text = "ক্যাটাগরি:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(currentCategories) { cat ->
                        val isSelected = category == cat
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { category = cat },
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date Picker Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(year, month, dayOfMonth)
                                    }
                                    timestamp = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "তারিখ: $dateFormatted",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "পরিবর্তন",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Optional Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("নোট (ঐচ্ছিক)") },
                    placeholder = { Text("অতিরিক্ত কোনো বিবরণ...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull() ?: 0.0
                    val finalTitle = title.trim().ifBlank { category }
                    val finalCategory = category.trim().ifBlank { "অন্যান্য" }
                    if (amountVal > 0 && finalTitle.isNotBlank()) {
                        onSave(selectedType, amountVal, finalTitle, finalCategory, note, timestamp)
                    }
                },
                enabled = (amount.toDoubleOrNull() ?: 0.0) > 0 && title.isNotBlank(),
                modifier = Modifier.testTag("btn_save_personal_transaction")
            ) {
                Text(if (isEditing) "আপডেট করুন" else "হিসাব যুক্ত করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

// -------------------------------------------------------------
// Helper: Category Icon Mapper
// -------------------------------------------------------------

fun getCategoryIcon(category: String): ImageVector {
    return when (category.trim()) {
        "খাবার" -> Icons.Default.Restaurant
        "যাতায়াত" -> Icons.Default.DirectionsBus
        "পড়াশোনা" -> Icons.Default.School
        "চিকিৎসা" -> Icons.Default.LocalHospital
        "বিল" -> Icons.Default.ReceiptLong
        "কেনাকাটা" -> Icons.Default.ShoppingBag
        "মোবাইল" -> Icons.Default.PhoneAndroid
        "বেতন" -> Icons.Default.TrendingUp
        "ব্যবসা/উত্তোলন" -> Icons.Default.AccountBalance
        "ব্যাংক" -> Icons.Default.AccountBalance
        "ডিপিএস" -> Icons.Default.Savings
        "নগদ" -> Icons.Outlined.AccountBalanceWallet
        "বাজারسদাই", "বাজার" -> Icons.Default.ShoppingBag
        "বাসা ভাড়া", "ভাড়া" -> Icons.Default.Home
        "উপহার" -> Icons.Default.CardGiftcard
        "ফ্রিল্যান্সিং" -> Icons.Default.Laptop
        "দান/সাহায্য" -> Icons.Default.VolunteerActivism
        else -> Icons.Default.MoreHoriz
    }
}
