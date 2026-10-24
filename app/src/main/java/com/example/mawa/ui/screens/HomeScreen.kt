package com.example.mawa.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.mawa.ui.components.AppSecuritySettingsDialog
import com.example.mawa.ui.components.DailyBackupReminderCard
import com.example.mawa.ui.components.FinancialHealthScoreCard
import com.example.mawa.ui.components.PaymentQrDialog
import com.example.mawa.ui.components.ShopProfileDialog
import com.example.mawa.util.AppLockManager
import com.example.mawa.util.DailyClosureManager
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.local.entity.CashTallyEntity
import com.example.mawa.data.local.entity.ShopSettingsEntity
import com.example.mawa.data.local.entity.SupplierCompanyEntity
import com.example.mawa.data.model.AccountingSummary
import com.example.mawa.ui.components.Animated2DShopHeroBanner
import com.example.mawa.ui.components.CompanySrProfileCard
import com.example.mawa.ui.components.DailyClosureDialog
import com.example.mawa.ui.components.DenominationCounterBottomSheet
import com.example.mawa.ui.components.EditCompanySrDialog
import com.example.mawa.ui.components.HomeWithdrawalBottomSheet
import com.example.mawa.ui.components.HomeWithdrawalShortageBanner
import com.example.mawa.ui.components.MawaEmptyState
import com.example.mawa.ui.components.MawaQuickActionButton
import com.example.mawa.ui.components.MawaSummaryRow
import com.example.mawa.ui.components.PendingAfternoonPaymentsBanner
import com.example.mawa.ui.components.ScheduledDeliveryOrdersCard
import com.example.mawa.ui.components.SimilarNamesMergeDialog
import com.example.mawa.ui.components.SimilarNamesReminderBanner
import com.example.mawa.ui.components.SmartCompanyQuickEntryBar
import com.example.mawa.ui.components.SmartPredictivePurchaseCard
import com.example.mawa.ui.components.SmartRetailCalculatorBottomSheet
import com.example.mawa.ui.components.TransactionItemRow
import com.example.mawa.ui.components.WeeklyCompanyScheduleCalendarDialog
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.ExpenseClassifier
import com.example.mawa.util.SmartPredictivePurchaseEngine
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MawaViewModel,
    onOpenSales: () -> Unit,
    onOpenBaki: () -> Unit,
    onOpenJoma: () -> Unit,
    onOpenExpenseDrawer: () -> Unit,
    onOpenOrderKhata: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenProducts: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenSupabaseCloud: () -> Unit = {},
    onOpenCalculator: () -> Unit = {},
    onLockApp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedHomeDateMillis by viewModel.selectedHomeDateMillis.collectAsStateWithLifecycle()
    val summary by viewModel.selectedDateAccountingSummary.collectAsStateWithLifecycle()
    val settings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val displayTransactions by viewModel.selectedDateTransactions.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allFordiItems by viewModel.allFordiItems.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val allSupplierCompanies by viewModel.allSupplierCompanies.collectAsStateWithLifecycle()
    val pendingSupplierPayments by viewModel.pendingSupplierPayments.collectAsStateWithLifecycle()
    val detectedSimilarGroups by viewModel.detectedSimilarNameGroups.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()

    val homeDateKey = remember(selectedHomeDateMillis) {
        SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(selectedHomeDateMillis))
    }
    val homeCashTallies by viewModel.getCashTalliesForDateFlow(homeDateKey).collectAsStateWithLifecycle(initialValue = emptyList<CashTallyEntity>())

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showShopProfileDialog by remember { mutableStateOf(false) }
    var showPaymentQrDialog by remember { mutableStateOf(false) }
    var showSecuritySettingsDialog by remember { mutableStateOf(false) }
    var showDenominationSheet by remember { mutableStateOf(false) }
    var showDailyClosureDialog by remember { mutableStateOf(false) }
    var showSimilarNamesMergeDialog by remember { mutableStateOf(false) }
    var showEditCompanySrDialog by remember { mutableStateOf(false) }
    var companyToEditSr by remember { mutableStateOf<SupplierCompanyEntity?>(null) }
    var showWeeklyScheduleDialog by remember { mutableStateOf(false) }
    var showHomeWithdrawalSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val closingCashFocusRequester = remember { FocusRequester() }
    var profitMarginPercent by remember {
        mutableStateOf(com.example.mawa.util.ProfitMarginManager.getProfitMargin(context))
    }
    var isDetailedSummary by remember { mutableStateOf(false) }
    var showCustomMarginDialog by remember { mutableStateOf(false) }
    var customMarginInput by remember { mutableStateOf("") }

    val isToday = remember(selectedHomeDateMillis) {
        val calSelected = Calendar.getInstance().apply { timeInMillis = selectedHomeDateMillis }
        val calToday = Calendar.getInstance()
        calSelected.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
        calSelected.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR)
    }

    var sabekInput by remember(selectedHomeDateMillis, summary.openingBalance) {
        val initialSabek = summary.openingBalance
        mutableStateOf(
            if (initialSabek > 0) {
                if (initialSabek % 1.0 == 0.0) initialSabek.toLong().toString() else initialSabek.toString()
            } else ""
        )
    }

    var closingCashInput by remember(selectedHomeDateMillis, summary.totalCashInHand) {
        val initialClosing = if (summary.totalCashInHand > 0) summary.totalCashInHand else 0.0
        mutableStateOf(
            if (initialClosing > 0) {
                if (initialClosing % 1.0 == 0.0) initialClosing.toLong().toString() else initialClosing.toString()
            } else ""
        )
    }

    val todayExpensesTotal = summary.todayPurchases + summary.todayShopExpenses + summary.todayHomeWithdrawals
    val sabekValue = sabekInput.toDoubleOrNull() ?: 0.0
    val closingCashValue = closingCashInput.toDoubleOrNull() ?: 0.0

    // Automatic Daily Sales & Cash In Hand Calculation (দোকানের ঐতিহ্যবাহী নির্ভুল খাতা নিয়ম):
    // ১. যদি সমাপনী ক্যাশ লিখে দেওয়া থাকে: মোট বেচা = সমাপনী ক্যাশ + মোট খরচ - সাবেক - বাকি আদায়
    // ২. যদি সমাপনী ক্যাশ না লেখা থাকে: খরচ সাবেক অপেক্ষা বেশি হলে সেই বাড়তি খরচই আজকের নগদ বিক্রি
    val autoComputedDailySale = if (closingCashInput.isNotBlank()) {
        (closingCashValue + todayExpensesTotal - sabekValue - summary.todayBakiCollection).coerceAtLeast(0.0)
    } else {
        if (todayExpensesTotal > (sabekValue + summary.todayBakiCollection)) {
            (todayExpensesTotal - (sabekValue + summary.todayBakiCollection)).coerceAtLeast(0.0)
        } else 0.0
    }

    // সর্বমোট বিক্রি (Explicit Sales থাকলে অথবা Reconciled Auto Sales + বাকি বিক্রি)
    val effectiveDailySales = maxOf(
        summary.todayTotalSales,
        autoComputedDailySale + summary.todayBakiSales
    )

    val effectiveCashSales = maxOf(
        summary.todayCashSales,
        autoComputedDailySale
    )

    // প্রদর্শনের জন্য বর্তমান হাতে ক্যাশ:
    // কখনো ঋণাত্মক (-৪৯০০) দেখাবে না; ঘাটতি পূরণ হয়ে ০ দেখাবে বা প্রকৃত সমাপনী ক্যাশ দেখাবে
    val displayCashInHand = if (closingCashInput.isNotBlank()) {
        closingCashValue
    } else {
        (sabekValue + effectiveCashSales + summary.todayBakiCollection - todayExpensesTotal).coerceAtLeast(0.0)
    }

    // টাকার উপর শতকরা লাভ (দোকানের মালামাল কেনা ও বিক্রির ঘূর্ণায়মান মূলধন হিসেবে নিট মার্জিন)
    val estimatedProfit = com.example.mawa.util.ProfitMarginManager.calculateProfit(effectiveDailySales, profitMarginPercent)

    // স্মার্ট পূর্বাভাস: পূর্বের ডাটা, দিন ও শিডিউল দেখে আজকের সম্ভাব্য কোম্পানি ও নিয়মিত কেনাকাটা
    val predictivePurchases = remember(allTransactions, allSupplierCompanies, selectedHomeDateMillis) {
        SmartPredictivePurchaseEngine.generatePredictions(
            transactions = allTransactions,
            supplierCompanies = allSupplierCompanies,
            targetDateMillis = selectedHomeDateMillis
        )
    }

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
            dateKey = homeDateKey,
            dateMillis = selectedHomeDateMillis,
            initialTally = homeCashTallies.firstOrNull(),
            shopName = settings?.shopName ?: "মাওয়া স্টোর",
            onSaveTally = { tally ->
                viewModel.saveCashTally(tally, updateClosingCash = true)
            },
            onDismiss = { showDenominationSheet = false },
            onApplyCountedTotal = { countedAmount ->
                val formatted = if (countedAmount % 1.0 == 0.0) countedAmount.toLong().toString() else countedAmount.toString()
                closingCashInput = formatted
            }
        )
    }

    // Custom Profit Margin Dialog
    if (showCustomMarginDialog) {
        AlertDialog(
            onDismissRequest = { showCustomMarginDialog = false },
            title = {
                Text("টাকার উপর শতকরা লাভ (%) সেট করুন", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "দোকানের বিক্রি ও খরচের টাকার ওপর আপনার গড় শতকরা কত লাভ হয় (% মার্জিন) তা লিখুন:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customMarginInput,
                        onValueChange = { customMarginInput = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("লাভের শতকরা হার (%)") },
                        suffix = { Text("%") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = customMarginInput.toDoubleOrNull()
                        if (p != null && p > 0) {
                            profitMarginPercent = p
                            com.example.mawa.util.ProfitMarginManager.setProfitMargin(context, p)
                        }
                        showCustomMarginDialog = false
                    }
                ) {
                    Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomMarginDialog = false }) {
                    Text("বাতিল")
                }
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
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showShopProfileDialog = true }
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                .testTag("header_shop_profile_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Shop Avatar / Storefront Badge
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.secondaryContainer
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = "দোকান প্রোফাইল",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                val shopDisplayName = settings?.shopName?.takeIf { it.isNotBlank() && it != "মাওয়া ডিজিটাল খাতা" } ?: "আমার দোকান"
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = shopDisplayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "দোকান প্রোফাইল পরিবর্তন",
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Prominent MAWA App Brand Pill
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = "MAWA",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 0.5.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    Text(
                                        text = "$greeting · দোকান খতিয়ান",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Quick Lock Button (if PIN lock is configured)
                            if (AppLockManager.isLockEnabled(context)) {
                                IconButton(
                                    onClick = {
                                        AppLockManager.lockNow()
                                        onLockApp()
                                    },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981).copy(alpha = 0.12f))
                                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.35f), CircleShape)
                                        .testTag("header_lock_app_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "এখনই লক করুন",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Supabase Cloud Sync Quick Button
                            IconButton(
                                onClick = {
                                    if (currentUser != null) {
                                        viewModel.syncAndRestoreFromCloud()
                                    } else {
                                        onOpenSupabaseCloud()
                                    }
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (currentUser != null) Color(0xFF10B981).copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .testTag("header_cloud_sync_btn")
                            ) {
                                if (isCloudSyncing) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF10B981),
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (currentUser != null) Icons.Default.CloudDone else Icons.Default.Cloud,
                                        contentDescription = "সুপাবেজ ক্লাউড সিঙ্ক",
                                        tint = if (currentUser != null) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // Calculator Quick Button
                            IconButton(
                                onClick = onOpenCalculator,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape)
                                    .testTag("header_calculator_btn")
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

                    // Sleek Modern Date Navigator (matching image (1).jpg teal bar)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F766E),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
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
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Middle Date Label (Clickable Date Picker)
                            Surface(
                                onClick = { showDatePickerDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.16f),
                                modifier = Modifier.testTag("header_date_picker_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isToday) "আজ: $activeDateString" else activeDateString,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
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
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Search Bar (প্রোফাইল সেট করুন এর জায়গায় সার্বজনীন সার্চ বার)
                    var homeSearchQuery by remember { mutableStateOf("") }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "সার্চ",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                androidx.compose.foundation.text.BasicTextField(
                                    value = homeSearchQuery,
                                    onValueChange = { homeSearchQuery = it },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("home_search_bar_input"),
                                    decorationBox = { innerTextField ->
                                        if (homeSearchQuery.isEmpty()) {
                                            Text(
                                                text = "কাস্টমার, বিক্রি, বাকি, খরচ সার্চ করুন...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                if (homeSearchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { homeSearchQuery = "" },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "মুছুন",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Dynamic Live Search Results
                            if (homeSearchQuery.isNotBlank()) {
                                val q = homeSearchQuery.trim().lowercase(Locale.ROOT)
                                val matchingCust = remember(allCustomers, q) {
                                    allCustomers.filter { it.name.lowercase(Locale.ROOT).contains(q) || it.phone.contains(q) }.take(3)
                                }
                                val matchingTx = remember(allTransactions, q) {
                                    allTransactions.filter { 
                                        it.note.lowercase(Locale.ROOT).contains(q) || 
                                        (it.customerName?.lowercase(Locale.ROOT)?.contains(q) == true) ||
                                        (it.productName?.lowercase(Locale.ROOT)?.contains(q) == true)
                                    }.take(3)
                                }
                                val matchingProd = remember(allProducts, q) {
                                    allProducts.filter { it.name.lowercase(Locale.ROOT).contains(q) || it.banglaName.contains(q) }.take(3)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(6.dp))

                                if (matchingCust.isEmpty() && matchingTx.isEmpty() && matchingProd.isEmpty()) {
                                    Text(
                                        text = "কোনো মিল পাওয়া যায়নি",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                } else {
                                    matchingCust.forEach { cust ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onOpenBaki() }
                                                .padding(vertical = 5.dp, horizontal = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(cust.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                if (cust.phone.isNotBlank()) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("(${cust.phone})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                            Text("বাকি খাতা ➔", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    matchingProd.forEach { prod ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onOpenProducts() }
                                                .padding(vertical = 5.dp, horizontal = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(if (prod.banglaName.isNotBlank()) prod.banglaName else prod.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("স্টক: ${BengaliUtils.toBanglaDigits(prod.stockQuantity.toLong())}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Text("৳ ${BengaliUtils.formatTaka(prod.defaultSellingPrice)}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    matchingTx.forEach { tx ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onOpenSales() }
                                                .padding(vertical = 5.dp, horizontal = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Receipt, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(tx.note.ifBlank { tx.type.name }, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            Text("৳ ${BengaliUtils.formatTaka(tx.amount)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 6 CORE ACTION TILES GRID (সারাংশের উপরে - কোনো ব্যানার বা রিপিট বাটন ছাড়া)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Row 1: নতুন বিক্রি, বাকি খাতা, অর্ডার খাতা
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BigHomeActionTile(
                                icon = Icons.Default.ShoppingCart,
                                label = "নতুন বিক্রি",
                                iconTint = Color(0xFF059669),
                                containerTint = Color(0xFFECFDF5),
                                modifier = Modifier.weight(1f),
                                onClick = onOpenSales
                            )
                            BigHomeActionTile(
                                icon = Icons.Default.Receipt,
                                label = "বাকি খাতা",
                                iconTint = Color(0xFFD97706),
                                containerTint = Color(0xFFFEF3C7),
                                modifier = Modifier.weight(1f),
                                onClick = onOpenBaki
                            )
                            BigHomeActionTile(
                                icon = Icons.Default.Assignment,
                                label = "অর্ডার খাতা",
                                iconTint = Color(0xFF0F766E),
                                containerTint = Color(0xFFCCFBF1),
                                modifier = Modifier.weight(1f),
                                onClick = onOpenOrderKhata
                            )
                        }

                        // Row 2: খরচ ও ক্রয়, পণ্য ও স্টক, হিসাব রিপোর্ট
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BigHomeActionTile(
                                icon = Icons.Default.Paid,
                                label = "খরচ ও ক্রয়",
                                iconTint = Color(0xFFE11D48),
                                containerTint = Color(0xFFFFF1F2),
                                modifier = Modifier.weight(1f),
                                onClick = onOpenExpenseDrawer
                            )
                            BigHomeActionTile(
                                icon = Icons.Default.Storefront,
                                label = "পণ্য ও স্টক",
                                iconTint = Color(0xFF0284C7),
                                containerTint = Color(0xFFE0F2FE),
                                modifier = Modifier.weight(1f),
                                onClick = onOpenProducts
                            )
                            BigHomeActionTile(
                                icon = Icons.Default.Assessment,
                                label = "হিসাব রিপোর্ট",
                                iconTint = Color(0xFF4F46E5),
                                containerTint = Color(0xFFEEF2FF),
                                modifier = Modifier.weight(1f),
                                onClick = onOpenReports
                            )
                        }
                    }

                    // Historical Notice Banner if viewing past date
                    if (!isToday) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = FinancialWarningContainer.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚠️ আপনি পূর্ববর্তী দিনের হিসাব দেখছেন",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. MASTER ACCOUNTING SUMMARY (আজকের মূল হিসাব ও ক্যাশ সারাংশ - প্রথম পলকেই দৃশ্যমান)
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Header with Icon Badge & Compact/Detailed Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Assessment,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "আজকের হিসাব ও ক্যাশ",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = FinancialNegativeContainer.copy(alpha = 0.85f)
                                ) {
                                    Text(
                                        text = "খরচ: ${BengaliUtils.formatTaka(todayExpensesTotal)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = FinancialNegative,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { isDetailedSummary = !isDetailedSummary }
                                        .testTag("toggle_detailed_summary_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isDetailedSummary) "সংক্ষিপ্ত ▴" else "বিস্তারিত ▾",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        if (summary.openingBalance > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                shadowElevation = 0.5.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981).copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = null,
                                                tint = Color(0xFF059669),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "গতকালের সমাপনী ক্যাশ: ${BengaliUtils.formatTaka(summary.openingBalance)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            val formatted = if (summary.openingBalance % 1.0 == 0.0) summary.openingBalance.toLong().toString() else summary.openingBalance.toString()
                                            sabekInput = formatted
                                            viewModel.updateOpeningBalance(summary.openingBalance)
                                        },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(
                                            text = "সাবেক বসান",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

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
                                        val dateKey = BengaliUtils.formatDateKey(selectedHomeDateMillis)
                                        viewModel.saveDailySabekCash(dateKey, selectedHomeDateMillis, d)
                                        if (isToday) {
                                            viewModel.updateOpeningBalance(d)
                                        }
                                    }
                                },
                                label = { Text("সাবেক (শুরুর ক্যাশ)") },
                                prefix = { Text("৳ ") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_sabek_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { closingCashFocusRequester.requestFocus() }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                )
                            )

                            OutlinedTextField(
                                value = closingCashInput,
                                onValueChange = { input ->
                                    val digitsOnly = input.filter { it.isDigit() || it == '.' }
                                    closingCashInput = digitsOnly
                                    val d = digitsOnly.toDoubleOrNull()
                                    if (d != null && d >= 0) {
                                        val dateKey = BengaliUtils.formatDateKey(selectedHomeDateMillis)
                                        viewModel.saveDailyClosingCash(dateKey, selectedHomeDateMillis, d, isClosed = true)
                                    }
                                },
                                label = { Text("হাতে থাকা নগদ") },
                                prefix = { Text("৳ ") },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(closingCashFocusRequester)
                                    .testTag("home_closing_cash_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
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
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Payments,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (homeCashTallies.isNotEmpty()) "নোট এন্ট্রি সেভ করা আছে" else "ক্যাশবক্স মেলান (নোট গুনে ক্যাশ বসান)",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (homeCashTallies.isNotEmpty()) {
                                            Text(
                                                text = "${BengaliUtils.toBengaliDigits(homeCashTallies.first().totalNotesCount().toString())}টি নোট • ৳ ${BengaliUtils.formatTaka(homeCashTallies.first().totalAmount)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = if (homeCashTallies.isNotEmpty()) "নোট দেখুন ➔" else "নোট টালি ➔",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 4-Tile High-Impact Grid (কুল ও মিনিমাল ডিজাইন - কোনো কালার ব্লিডিং বা জ্বলে যাওয়া নেই)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Tile 1: মোট বিক্রি (বেচা)
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    shadowElevation = 0.5.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "মোট বিক্রি (বেচা)",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(RoundedCornerShape(7.dp))
                                                    .background(Color(0xFF10B981).copy(alpha = 0.12f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.TrendingUp,
                                                    contentDescription = null,
                                                    tint = Color(0xFF059669),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = BengaliUtils.formatTaka(effectiveDailySales),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF047857)
                                        )
                                    }
                                }

                                // Tile 2: মোট খরচ
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    shadowElevation = 0.5.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "মোট খরচ",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(RoundedCornerShape(7.dp))
                                                    .background(Color(0xFFF43F5E).copy(alpha = 0.12f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.TrendingDown,
                                                    contentDescription = null,
                                                    tint = Color(0xFFE11D48),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = BengaliUtils.formatTaka(todayExpensesTotal),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFBE123C)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Tile 3: সাবেক (শুরুর ক্যাশ)
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    shadowElevation = 0.5.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "সাবেক (শুরু)",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(RoundedCornerShape(7.dp))
                                                    .background(Color(0xFF6366F1).copy(alpha = 0.12f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.History,
                                                    contentDescription = null,
                                                    tint = Color(0xFF4F46E5),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = BengaliUtils.formatTaka(sabekValue),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4338CA)
                                        )
                                    }
                                }

                                // Tile 4: হাতে ক্যাশ (সমাপনী) - কখনো ঋণাত্মক দেখাবে না
                                val safeDisplayCash = displayCashInHand.coerceAtLeast(0.0)
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    shadowElevation = 0.5.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "হাতে ক্যাশ",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(RoundedCornerShape(7.dp))
                                                    .background(Color(0xFF0D9488).copy(alpha = 0.12f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AccountBalanceWallet,
                                                    contentDescription = null,
                                                    tint = Color(0xFF0D9488),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = BengaliUtils.formatTaka(safeDisplayCash),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F766E)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Profit Margin Card (টাকার উপর শতকরা লাভ) - Cool Minimal
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            shadowElevation = 0.5.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
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
                                            shape = CircleShape,
                                            color = FinancialPositive.copy(alpha = 0.2f),
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoGraph,
                                                    contentDescription = null,
                                                    tint = FinancialPositive,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val marginDisplay = if (profitMarginPercent % 1.0 == 0.0) "${profitMarginPercent.toInt()}%" else "$profitMarginPercent%"
                                        Text(
                                            text = "শতকরা লাভ ($marginDisplay)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Text(
                                        text = BengaliUtils.formatTaka(estimatedProfit),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = FinancialPositive
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Quick Percentage Selection Chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    com.example.mawa.util.ProfitMarginManager.PRESET_MARGINS.forEach { preset ->
                                        val isSelected = Math.abs(profitMarginPercent - preset) < 0.1
                                        val label = "${preset.toInt()}%"
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    profitMarginPercent = preset
                                                    com.example.mawa.util.ProfitMarginManager.setProfitMargin(context, preset)
                                                },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) FinancialPositive else MaterialTheme.colorScheme.surface,
                                            border = if (isSelected) null else BorderStroke(1.dp, FinancialPositive.copy(alpha = 0.35f))
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else FinancialPositive,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    // Custom % Button
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                customMarginInput = if (profitMarginPercent % 1.0 == 0.0) profitMarginPercent.toInt().toString() else profitMarginPercent.toString()
                                                showCustomMarginDialog = true
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = "কাস্টম %",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "অন্য %",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.5.sp
                                            )
                                        }
                                    }
                                }

                                if (isDetailedSummary) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                    ) {
                                        Text(
                                            text = "💡 মোট বিক্রির (${BengaliUtils.toBengaliDigits(if (profitMarginPercent % 1.0 == 0.0) profitMarginPercent.toInt().toString() else profitMarginPercent.toString())}%) আপনার প্রকৃত ব্যবসায়িক লাভ।",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 11.sp,
                                            color = FinancialPositive,
                                            lineHeight = 15.sp,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Detailed step-by-step formula & breakdown rows
                        if (isDetailedSummary) {
                            Spacer(modifier = Modifier.height(10.dp))

                            val explanationText = if (sabekInput.isBlank() && closingCashInput.isBlank()) {
                                if (todayExpensesTotal > 0) {
                                    "সকল খরচ (${BengaliUtils.formatTaka(todayExpensesTotal)}) আজকের বিক্রির অন্তর্ভুক্ত"
                                } else {
                                    "খরচ এন্ট্রি বা দিন শেষের ক্যাশ লিখলে স্বয়ংক্রিয়ভাবে মোট বেচা হিসাব হবে"
                                }
                            } else {
                                "নগদ (${BengaliUtils.formatTaka(closingCashValue)}) + খরচ (${BengaliUtils.formatTaka(todayExpensesTotal)}) − সাবেক (${BengaliUtils.formatTaka(sabekValue)}) = বেচা ${BengaliUtils.formatTaka(autoComputedDailySale)}"
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = explanationText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            MawaSummaryRow(
                                label = "মোট বিক্রি (দোকানের বেচা)",
                                amount = effectiveDailySales,
                                amountColor = MaterialTheme.colorScheme.onSurface,
                                icon = Icons.Default.ShoppingCart,
                                iconTint = MaterialTheme.colorScheme.primary,
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
                                iconTint = FinancialPositive,
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
                                iconTint = MaterialTheme.colorScheme.primary,
                                isSubRow = true,
                                onClick = onOpenOrderKhata
                            )
                            MawaSummaryRow(
                                label = "দোকান পরিচালনা খরচ",
                                amount = summary.todayShopExpenses,
                                amountColor = FinancialNegative,
                                prefix = "−",
                                icon = Icons.Default.Store,
                                iconTint = FinancialNegative,
                                isSubRow = true,
                                onClick = onOpenExpenseDrawer
                            )
                            MawaSummaryRow(
                                label = "বাড়ির জন্য খরচ / উত্তোলন",
                                amount = summary.todayHomeWithdrawals,
                                amountColor = FinancialWarning,
                                prefix = "−",
                                icon = Icons.Default.Home,
                                iconTint = FinancialWarning,
                                isSubRow = true,
                                onClick = { showHomeWithdrawalSheet = true }
                            )
                        }
                    }

                    // Baki Outstanding Summary Card - Cool Minimal
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onOpenBaki() },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        shadowElevation = 0.5.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "সর্বমোট বকেয়া বাকি",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "আজকের নতুন বাকি: ${BengaliUtils.formatTaka(summary.todayNewBaki)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = BengaliUtils.formatTaka(summary.totalOutstandingBaki),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = FinancialNegative
                                )
                                Text(
                                    text = "খাতা দেখুন →",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }


                }
            }

            // 5. Recent Activity Header (Cardless)
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

            // 6. Recent Activity List (Cardless Flat List)
            if (displayTransactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        displayTransactions.forEachIndexed { index, tx ->
                            TransactionItemRow(
                                transaction = tx,
                                onDelete = { viewModel.deleteTransaction(tx.id) }
                            )
                            if (index < displayTransactions.size - 1) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 7. SECONDARY MANAGEMENT MODULES (খরচ ও হিসাব তালিকার নিচে সাজানো)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    HorizontalDivider(
                        thickness = 0.8.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ব্যবসায়িক ব্যবস্থাপনা ও শিডিউল",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "অর্ডার, স্কোর ও ব্যাকআপ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. আজকের কোম্পানি শিডিউল ও অর্ডার খাতা (আজকের নির্দিষ্ট কোম্পানির ডাটা একটি একটি করে প্রদর্শন)
                    val todayBengaliDay = remember { BengaliUtils.getBengaliDayOfWeek(System.currentTimeMillis()) }
                    val todayScheduledCompanies = remember(allSupplierCompanies, todayBengaliDay) {
                        allSupplierCompanies.filter { 
                            it.orderDay.contains(todayBengaliDay, ignoreCase = true) || 
                            it.deliveryDay.contains(todayBengaliDay, ignoreCase = true) 
                        }
                    }
                    var activeCompanyScheduleIndex by remember { mutableStateOf(0) }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .testTag("today_company_schedule_card"),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Store,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "আজকের কোম্পানি শিডিউল ($todayBengaliDay)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (todayScheduledCompanies.isNotEmpty()) {
                                                "আজকের নির্ধারিত কোম্পানি (${BengaliUtils.toBanglaDigits(todayScheduledCompanies.size.toLong())}টির মধ্যে ${BengaliUtils.toBanglaDigits((activeCompanyScheduleIndex.coerceIn(0, todayScheduledCompanies.size - 1) + 1).toLong())}নং)"
                                            } else {
                                                "আজ $todayBengaliDay কোনো কোম্পানির নির্ধারিত শিডিউল নেই"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showWeeklyScheduleDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = "পূর্ণ ক্যালেন্ডার",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (todayScheduledCompanies.isEmpty()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "আজ নিয়মিত কোনো কোম্পানির আসার দিন নেই।",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Button(
                                            onClick = onOpenOrderKhata,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("অর্ডার খাতা", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            } else {
                                val currentComp = todayScheduledCompanies[activeCompanyScheduleIndex.coerceIn(0, todayScheduledCompanies.size - 1)]
                                val isDeliveryDay = currentComp.deliveryDay.contains(todayBengaliDay, ignoreCase = true)
                                val isOrderDay = currentComp.orderDay.contains(todayBengaliDay, ignoreCase = true)

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = currentComp.companyName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Black,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                val statusText = if (isDeliveryDay && isOrderDay) "আজ মাল ডেলিভারি ও নতুন অর্ডার নেওয়ার দিন"
                                                    else if (isDeliveryDay) "আজ কোম্পানির মাল পৌঁছানোর (ডেলিভারি) দিন"
                                                    else "আজ অর্ডার নেওয়ার দিন"
                                                Text(
                                                    text = "🚚 $statusText",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = FinancialPositive
                                                )
                                                if (!currentComp.srName.isNullOrBlank()) {
                                                    Text(
                                                        text = "এসআর: ${currentComp.srName}${if (!currentComp.srPhone.isNullOrBlank()) " (${currentComp.srPhone})" else ""}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Button(
                                                onClick = onOpenOrderKhata,
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ShoppingCart,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("অর্ডার খাতা", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }

                                        // Pagination if more than 1 company for today
                                        if (todayScheduledCompanies.size > 1) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TextButton(
                                                    onClick = {
                                                        activeCompanyScheduleIndex = (activeCompanyScheduleIndex - 1 + todayScheduledCompanies.size) % todayScheduledCompanies.size
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text("← পূর্ববর্তী", style = MaterialTheme.typography.labelSmall)
                                                }

                                                Text(
                                                    text = "${BengaliUtils.toBanglaDigits((activeCompanyScheduleIndex + 1).toLong())} / ${BengaliUtils.toBanglaDigits(todayScheduledCompanies.size.toLong())}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                TextButton(
                                                    onClick = {
                                                        activeCompanyScheduleIndex = (activeCompanyScheduleIndex + 1) % todayScheduledCompanies.size
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text("পরবর্তী কোম্পানি →", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Financial Health Score & Day Closure Audit Card (অর্ডার ও স্বাস্থ্য স্কোর)
                    Spacer(modifier = Modifier.height(10.dp))
                    val isDayLocked = remember(selectedHomeDateMillis) {
                        DailyClosureManager.isDayLocked(context, selectedHomeDateMillis)
                    }
                    FinancialHealthScoreCard(
                        dailyCashInHand = displayCashInHand,
                        todaySales = effectiveDailySales,
                        todayBakiCollection = summary.todayBakiCollection,
                        todayTotalExpenses = todayExpensesTotal,
                        totalOutstandingBaki = summary.totalOutstandingBaki,
                        isDayLocked = isDayLocked,
                        onOpenDailyClosure = { showDailyClosureDialog = true }
                    )

                    // 5. Scheduled Delivery & Orders Card
                    val hasScheduledOrders = remember(allFordiItems) {
                        allFordiItems.any { !it.isPurchased && (it.orderDay.isNotBlank() || it.deliveryDay.isNotBlank()) }
                    }
                    if (hasScheduledOrders) {
                        Spacer(modifier = Modifier.height(10.dp))
                        ScheduledDeliveryOrdersCard(
                            fordiItems = allFordiItems,
                            onConfirmReceivedAndPaid = { item ->
                                val qty = if (item.actualQuantity > 0) item.actualQuantity else item.plannedQuantity
                                val rate = if (item.actualRate > 0) item.actualRate else item.purchaseRate
                                viewModel.convertFordiToPurchase(item, qty, rate, paymentStatus = "PAID")
                            },
                            onConfirmReceivedDueAfternoon = { item ->
                                val qty = if (item.actualQuantity > 0) item.actualQuantity else item.plannedQuantity
                                val rate = if (item.actualRate > 0) item.actualRate else item.purchaseRate
                                viewModel.convertFordiToPurchase(item, qty, rate, paymentStatus = "DUE_PAYMENT")
                            },
                            onAddNewOrder = { onOpenOrderKhata() }
                        )
                    }

                    // 7. Pending Afternoon / Promised Payments
                    if (pendingSupplierPayments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        PendingAfternoonPaymentsBanner(
                            pendingTransactions = pendingSupplierPayments,
                            onMarkAsPaid = { txId ->
                                viewModel.updateTransactionPaymentStatus(txId, "PAID")
                            }
                        )
                    }

                    // 8. Home Withdrawal Shortage Banner
                    if (summary.todayHomeWithdrawals > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HomeWithdrawalShortageBanner(
                            todayHomeWithdrawals = summary.todayHomeWithdrawals,
                            onOpenHomeWithdrawalSheet = { showHomeWithdrawalSheet = true }
                        )
                    }

                    // Ample clearance for the bottom navigation cradle
                    Spacer(modifier = Modifier.height(84.dp))
                }
            }
        }

        // Shop Profile Dialog (দোকানের নামে নিজস্ব প্রোফাইল তৈরি ও আপডেট)
        if (showShopProfileDialog) {
            ShopProfileDialog(
                currentSettings = settings,
                onDismiss = { showShopProfileDialog = false },
                onSave = { updated ->
                    viewModel.updateSettings(updated)
                    showShopProfileDialog = false
                }
            )
        }

        // Digital Payment QR Hub Dialog (বিকাশ, নগদ, কিউআর পেমেন্ট কালেকশন)
        if (showPaymentQrDialog) {
            PaymentQrDialog(
                settings = settings,
                onDismiss = { showPaymentQrDialog = false },
                onEditShopProfile = {
                    showPaymentQrDialog = false
                    showShopProfileDialog = true
                }
            )
        }

        // App Security & 4-Digit PIN Lock Dialog
        if (showSecuritySettingsDialog) {
            AppSecuritySettingsDialog(
                shopPhone = settings?.phone ?: "",
                onDismiss = { showSecuritySettingsDialog = false },
                onLockNow = {
                    showSecuritySettingsDialog = false
                    AppLockManager.lockNow()
                    onLockApp()
                }
            )
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

        // Similar Company & Product Names Merge Dialog (১-ট্যাপ সংশোধন বা আলাদা নিশ্চিতকরণ)
        if (showSimilarNamesMergeDialog) {
            SimilarNamesMergeDialog(
                similarGroups = detectedSimilarGroups,
                onDismiss = { showSimilarNamesMergeDialog = false },
                onMergeNames = { oldName, newName ->
                    viewModel.mergeEntityNames(oldName, newName)
                },
                onKeepSeparate = { group ->
                    viewModel.markSimilarGroupAsSeparate(group)
                }
            )
        }

        // Edit Supplier Company & SR Representative Profile Dialog
        if (showEditCompanySrDialog) {
            EditCompanySrDialog(
                initialCompany = companyToEditSr,
                onDismiss = {
                    showEditCompanySrDialog = false
                    companyToEditSr = null
                },
                onSave = { updated ->
                    viewModel.saveSupplierCompany(updated)
                    showEditCompanySrDialog = false
                    companyToEditSr = null
                }
            )
        }

        // Weekly 7-Day Company Schedule Calendar Dialog
        if (showWeeklyScheduleDialog) {
            WeeklyCompanyScheduleCalendarDialog(
                supplierCompanies = allSupplierCompanies,
                fordiItems = allFordiItems,
                onDismiss = { showWeeklyScheduleDialog = false },
                onSaveCompany = { comp -> viewModel.saveSupplierCompany(comp) },
                onOpenFordi = onOpenOrderKhata,
                onConfirmDeliveryReceived = { item, isPaid ->
                    val qty = if (item.actualQuantity > 0) item.actualQuantity else item.plannedQuantity
                    val rate = if (item.actualRate > 0) item.actualRate else item.purchaseRate
                    viewModel.convertFordiToPurchase(item, qty, rate, paymentStatus = if (isPaid) "PAID" else "DUE_PAYMENT")
                }
            )
        }

        // Home Withdrawal & Shortage Prevention Sheet ("বাসার জন্য নিলাম")
        if (showHomeWithdrawalSheet) {
            HomeWithdrawalBottomSheet(
                products = allProducts,
                todayTransactions = allTransactions,
                onDismiss = { showHomeWithdrawalSheet = false },
                onRecordCashWithdrawal = { amount, desc ->
                    viewModel.recordExpense(amount = amount, description = desc, isHome = true)
                },
                onRecordGoodsWithdrawal = { pName, pId, qty, unit, rate, isDeduct, note ->
                    viewModel.recordHomeGoodsWithdrawal(pName, pId, qty, unit, rate, isDeduct, note)
                }
            )
        }
    }
}

@Composable
private fun BigHomeActionTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconTint: Color,
    containerTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tile_scale"
    )
    val isDark = isSystemInDarkTheme()

    Surface(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 1.dp else 2.5.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("action_tile_${label}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(containerTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BigHomeWideActionTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconTint: Color,
    containerTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        label = "wide_tile_scale"
    )
    val isDark = isSystemInDarkTheme()

    Surface(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 1.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("action_wide_tile_${label}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(containerTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
