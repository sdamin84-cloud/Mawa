package com.example.mawa.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import com.example.R
import com.example.mawa.ui.components.BluetoothThermalPrinterDialog
import com.example.mawa.util.InvoiceItem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType
import com.example.mawa.data.model.TimeFilter
import com.example.mawa.data.repository.MawaRepository
import com.example.mawa.ui.components.DailyClosureDialog
import com.example.mawa.ui.components.InteractiveCompanyPurchaseDonutChart
import com.example.mawa.ui.components.InteractiveTrendChart
import com.example.mawa.ui.components.MawaSummaryRow
import com.example.mawa.ui.components.MawaTopBar
import com.example.mawa.ui.components.TransactionItemRow
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.DataBackupRestoreManager
import com.example.mawa.util.ReceiptPrintManager
import com.example.mawa.util.ReportExportUtils
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

private data class DailyLedgerGroup(
    val dayStartMillis: Long,
    val dateLabel: String,
    val weekday: String,
    val cashSales: Double,
    val bakiSales: Double,
    val bakiCollection: Double,
    val totalSales: Double,
    val purchases: Double,
    val shopExpenses: Double,
    val homeWithdrawals: Double,
    val estimatedProfit: Double,
    val txList: List<TransactionEntity>
)

@Composable
fun ReportsScreen(
    viewModel: MawaViewModel,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null,
    onNavigateToDayOnHome: ((Long) -> Unit)? = null,
    onNavigateCashTally: (() -> Unit)? = null,
    onOpenBackupRestore: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val currentFilter by viewModel.reportTimeFilter.collectAsStateWithLifecycle()
    val customDateMillis by viewModel.reportCustomDateMillis.collectAsStateWithLifecycle()
    val transactions by viewModel.filteredTransactionsForReport.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val shopSettings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val allDailyCash by viewModel.allDailyCash.collectAsStateWithLifecycle()
    val customersWithBalance by viewModel.customersWithBalance.collectAsStateWithLifecycle()
    val allSupplierCompanies by viewModel.allSupplierCompanies.collectAsStateWithLifecycle()

    var showMemoDialog by remember { mutableStateOf(false) }
    var showDailyClosureDialog by remember { mutableStateOf(false) }
    var showBluetoothPrinterDialog by remember { mutableStateOf(false) }
    var expandedDayMillis by remember { mutableStateOf<Long?>(null) }

    var profitMarginPercent by remember {
        mutableStateOf(com.example.mawa.util.ProfitMarginManager.getProfitMargin(context))
    }

    val (reportStartMillis, reportEndMillis) = remember(currentFilter, customDateMillis) {
        if (customDateMillis != null) {
            Pair(
                MawaRepository.getStartOfDayMillis(customDateMillis!!),
                MawaRepository.getEndOfDayMillis(customDateMillis!!)
            )
        } else {
            val start = when (currentFilter) {
                TimeFilter.TODAY -> MawaRepository.getStartOfDayMillis(System.currentTimeMillis())
                TimeFilter.THIS_WEEK -> {
                    val cal = Calendar.getInstance().apply {
                        firstDayOfWeek = Calendar.SATURDAY
                        set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    cal.timeInMillis
                }
                TimeFilter.THIS_MONTH -> {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    cal.timeInMillis
                }
                TimeFilter.ALL_TIME -> 0L
            }
            Pair(start, Long.MAX_VALUE)
        }
    }

    // Group historical transactions & daily cash by day to compute true daily sales
    val historicalDailyLedgers = remember(allTransactions, allDailyCash, shopSettings, profitMarginPercent) {
        val cal = Calendar.getInstance()
        val allDayStartMillis = buildSet {
            allTransactions.forEach { tx ->
                cal.timeInMillis = tx.timestamp
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                add(cal.timeInMillis)
            }
            allDailyCash.forEach { dc ->
                cal.timeInMillis = dc.dateMillis
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                add(cal.timeInMillis)
            }
        }

        allDayStartMillis.map { dayMillis ->
            val dayStart = dayMillis
            val dayEnd = dayMillis + 86400000L - 1L
            val txList = allTransactions.filter { it.timestamp in dayStart..dayEnd }

            val explicitCashSales = txList.filter { it.type == TransactionType.SALE_CASH }.sumOf { it.amount }
            val bakiSales = txList.filter { it.type == TransactionType.SALE_BAKI }.sumOf { it.amount }
            val bakiCol = txList.filter { it.type == TransactionType.BAKI_COLLECTION }.sumOf { it.amount }
            val purchases = txList.filter { it.type == TransactionType.PURCHASE_FORDI || it.type == TransactionType.PURCHASE_DIRECT }.sumOf { it.amount }
            val shopExp = txList.filter { it.type == TransactionType.EXPENSE_SHOP }.sumOf { it.amount }
            val homeExp = txList.filter { it.type == TransactionType.EXPENSE_HOME }.sumOf { it.amount }
            val totalExpenses = purchases + shopExp + homeExp

            val targetDateKey = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(dayMillis))
            val dayRecord = allDailyCash.find { it.dateKey == targetDateKey }
            val sabekCash = if (dayRecord != null && dayRecord.sabekCash > 0) {
                dayRecord.sabekCash
            } else {
                val prevRecord = allDailyCash.filter { it.dateMillis < dayMillis && it.closingCash > 0 }.maxByOrNull { it.dateMillis }
                prevRecord?.closingCash ?: (shopSettings?.openingBalance ?: 0.0)
            }

            // Reconciled cash sale from closing cash only if day was closed with closing cash,
            // or if total expenses exceed available opening balance & collection
            val reconciledCashSales = if (dayRecord != null && dayRecord.closingCash > 0) {
                (dayRecord.closingCash + totalExpenses - sabekCash - bakiCol).coerceAtLeast(0.0)
            } else if (totalExpenses > (sabekCash + bakiCol)) {
                (totalExpenses - (sabekCash + bakiCol)).coerceAtLeast(0.0)
            } else {
                0.0
            }

            val effectiveCashSales = maxOf(explicitCashSales, reconciledCashSales)
            val sales = effectiveCashSales + bakiSales
            val profit = com.example.mawa.util.ProfitMarginManager.calculateProfit(sales, profitMarginPercent)

            val dateLabel = BengaliUtils.getFormattedTodayDate(dayMillis)
            val weekday = SimpleDateFormat("EEEE", Locale("bn", "BD")).format(Date(dayMillis))

            DailyLedgerGroup(
                dayStartMillis = dayMillis,
                dateLabel = dateLabel,
                weekday = weekday,
                cashSales = effectiveCashSales,
                bakiSales = bakiSales,
                bakiCollection = bakiCol,
                totalSales = sales,
                purchases = purchases,
                shopExpenses = shopExp,
                homeWithdrawals = homeExp,
                estimatedProfit = profit,
                txList = txList.sortedByDescending { it.timestamp }
            )
        }.sortedByDescending { it.dayStartMillis }
    }

    val shopName = shopSettings?.shopName?.ifBlank { "মাওয়া স্মার্ট খাতা" } ?: "মাওয়া স্মার্ট খাতা"

    // Real-time calculated metrics for the selected time filter across all days
    val periodDailyLedgers = remember(historicalDailyLedgers, reportStartMillis, reportEndMillis) {
        historicalDailyLedgers.filter { it.dayStartMillis in reportStartMillis..reportEndMillis }
    }

    val periodCashSales = remember(periodDailyLedgers) {
        periodDailyLedgers.sumOf { it.cashSales }
    }
    val periodBakiSales = remember(periodDailyLedgers) {
        periodDailyLedgers.sumOf { it.bakiSales }
    }
    val periodTotalSales = periodCashSales + periodBakiSales

    val periodBakiCollection = remember(periodDailyLedgers) {
        periodDailyLedgers.sumOf { it.bakiCollection }
    }

    val purchaseTransactions = remember(transactions) {
        transactions.filter { it.type == TransactionType.PURCHASE_FORDI || it.type == TransactionType.PURCHASE_DIRECT }
    }

    val periodPurchases = remember(periodDailyLedgers) {
        periodDailyLedgers.sumOf { it.purchases }
    }

    val periodShopExpenses = remember(periodDailyLedgers) {
        periodDailyLedgers.sumOf { it.shopExpenses }
    }

    val periodHomeWithdrawals = remember(periodDailyLedgers) {
        periodDailyLedgers.sumOf { it.homeWithdrawals }
    }

    // Accounting Formulas (টাকার উপর শতকরা লাভ)
    val estimatedBusinessProfit = com.example.mawa.util.ProfitMarginManager.calculateProfit(periodTotalSales, profitMarginPercent)
    val profitRemaining = estimatedBusinessProfit - periodHomeWithdrawals

    val periodCashInflow = periodCashSales + periodBakiCollection
    val periodTotalOutflow = periodPurchases + periodShopExpenses

    val topDueCustomers = remember(customersWithBalance) {
        customersWithBalance
            .filter { it.currentBalance > 0 }
            .sortedByDescending { it.currentBalance }
            .take(5)
    }

    val topSuppliers = remember(purchaseTransactions) {
        purchaseTransactions
            .groupBy { it.category.ifBlank { it.note.ifBlank { it.productName ?: "সাধারণ সরবরাহকারী" } } }
            .map { (name, txs) -> Pair(name, txs.sumOf { it.amount }) }
            .sortedByDescending { it.second }
            .take(5)
    }

    val activeDateMillis = customDateMillis ?: System.currentTimeMillis()
    val isToday = remember(activeDateMillis) {
        val cal1 = Calendar.getInstance().apply { timeInMillis = activeDateMillis }
        val cal2 = Calendar.getInstance()
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    val activeDateLabel = remember(activeDateMillis) {
        val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale("bn", "BD")).format(Date(activeDateMillis))
        val dayOfWeek = SimpleDateFormat("EEEE", Locale("bn", "BD")).format(Date(activeDateMillis))
        "$dateStr, $dayOfWeek"
    }

    val displayPeriodLabel = remember(currentFilter, customDateMillis, activeDateLabel, isToday) {
        if (customDateMillis != null) {
            if (isToday) "আজ ($activeDateLabel)" else activeDateLabel
        } else {
            currentFilter.banglaLabel
        }
    }

    val currentCal = Calendar.getInstance()
    currentCal.timeInMillis = activeDateMillis

    val datePickerDialog = remember(context, activeDateMillis) {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                viewModel.setReportCustomDate(selCal.timeInMillis)
            },
            currentCal.get(Calendar.YEAR),
            currentCal.get(Calendar.MONTH),
            currentCal.get(Calendar.DAY_OF_MONTH)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        MawaTopBar(
            title = "রিপোর্ট ও লাভ-ক্ষতি",
            subtitle = "$displayPeriodLabel-এর পূর্ণাঙ্গ বিবরণী",
            onMenuClick = onOpenDrawer
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // 0. Modern 2D Analytics Hero Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("reports_hero_banner_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(125.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_analytics_banner),
                            contentDescription = "রিপোর্ট অ্যানালিটিক্স ব্যানার",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.25f),
                                            Color.Black.copy(alpha = 0.82f)
                                        )
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "স্মার্ট বিজনেস অ্যানালিটিক্স",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "$displayPeriodLabel · মোট লেনদেন: ${BengaliUtils.toBengaliDigits(transactions.size.toString())}টি",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.5.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.9f)
                            ) {
                                Text(
                                    text = "লাইভ রিপোর্ট",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 1. Time Filter Selector (Daily, Weekly, Monthly, All-time, Custom Date)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TimeFilter.values().forEach { filter ->
                            val isSelected = currentFilter == filter && customDateMillis == null
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MawaPrimary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    )
                                    .clickable { viewModel.setReportTimeFilter(filter) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                    .testTag("report_filter_${filter.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = filter.banglaLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Custom Single Day Filter Chip
                        val isCustomSelected = customDateMillis != null
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isCustomSelected) MawaPrimary
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                )
                                .clickable {
                                    if (customDateMillis == null) {
                                        viewModel.setReportCustomDate(System.currentTimeMillis())
                                    }
                                    datePickerDialog.show()
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("report_filter_custom_day"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = if (isCustomSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "যেকোনো দিন",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCustomSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 1.1 Single Day Navigator Bar (আজকের দিন সহ যেকোনো পেছনের দিন দেখার কন্ট্রোল)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Previous Day
                            OutlinedButton(
                                onClick = {
                                    val prevCal = Calendar.getInstance().apply {
                                        timeInMillis = activeDateMillis
                                        add(Calendar.DAY_OF_MONTH, -1)
                                    }
                                    viewModel.setReportCustomDate(prevCal.timeInMillis)
                                },
                                modifier = Modifier.height(34.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("আগের দিন", fontSize = 11.5.sp)
                            }

                            // Selected Date Picker Button
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { datePickerDialog.show() }
                                    .border(
                                        width = 1.dp,
                                        color = if (customDateMillis != null) MawaPrimary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                color = if (customDateMillis != null) MawaPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "তারিখ বাছুন",
                                        tint = if (customDateMillis != null) MawaPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (customDateMillis == null && isToday) "আজকের দিন (ক্যালেন্ডার 📅)" else activeDateLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (customDateMillis != null) MawaPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Next Day
                            OutlinedButton(
                                onClick = {
                                    val nextCal = Calendar.getInstance().apply {
                                        timeInMillis = activeDateMillis
                                        add(Calendar.DAY_OF_MONTH, 1)
                                    }
                                    viewModel.setReportCustomDate(nextCal.timeInMillis)
                                },
                                modifier = Modifier.height(34.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !isToday || customDateMillis != null
                            ) {
                                Text("পরের দিন", fontSize = 11.5.sp)
                                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }

                        if (customDateMillis != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "নির্দিষ্ট দিনের রিপোর্ট দেখছেন",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "আজকের হিসেবে ফিরুন ↺",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MawaPrimary,
                                    modifier = Modifier
                                        .clickable { viewModel.setReportTimeFilter(TimeFilter.TODAY) }
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 1.5 Executive KPI Metric Tiles (৪টি শক্তিশালী মেট্রিক কার্ড)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // KPI 1: Gross Sales
                    ReportKpiCard(
                        title = "মোট বিক্রি",
                        value = BengaliUtils.formatTaka(periodTotalSales),
                        subtitle = "নগদ: ${BengaliUtils.formatTaka(periodCashSales)}",
                        icon = Icons.Default.ShoppingCart,
                        accentColor = Color(0xFF059669),
                        containerColor = Color(0xFFECFDF5),
                        modifier = Modifier.weight(1f)
                    )
                    // KPI 2: Net Profit
                    ReportKpiCard(
                        title = "নিট লাভ",
                        value = BengaliUtils.formatTaka(estimatedBusinessProfit),
                        subtitle = "মার্জিন: ${BengaliUtils.toBengaliDigits(if (profitMarginPercent % 1.0 == 0.0) profitMarginPercent.toInt().toString() else profitMarginPercent.toString())}%",
                        icon = Icons.Default.TrendingUp,
                        accentColor = FinancialPositive,
                        containerColor = FinancialPositiveContainer.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // KPI 3: Cash Inflow
                    ReportKpiCard(
                        title = "ক্যাশ আগমন",
                        value = BengaliUtils.formatTaka(periodCashInflow),
                        subtitle = "নগদ + বাকি আদায়",
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = Color(0xFF0284C7),
                        containerColor = Color(0xFFE0F2FE),
                        modifier = Modifier.weight(1f)
                    )
                    // KPI 4: Total Outflow
                    ReportKpiCard(
                        title = "মোট খরচ ও ক্রয়",
                        value = BengaliUtils.formatTaka(periodTotalOutflow),
                        subtitle = "মাল ক্রয় + দোকান খরচ",
                        icon = Icons.Default.TrendingDown,
                        accentColor = Color(0xFFDC2626),
                        containerColor = Color(0xFFFEE2E2),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Tally Accounting & Note Counter Quick Banner
            if (onNavigateCashTally != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onNavigateCashTally() }
                            .testTag("btn_reports_open_cash_tally"),
                        colors = CardDefaults.cardColors(containerColor = MawaPrimary.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, MawaPrimary.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MawaPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PointOfSale,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "ক্যাশ টালি ও ক্যাশবক্স মিলানো",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MawaPrimary
                                    )
                                    Text(
                                        text = "টালি স্টাইলে জমা-খরচ হিসাব ও নোট গণনা দেখুন",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = onNavigateCashTally,
                                colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("টালি দেখুন", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 3. Export & Share Action Bar (Organized into clean, non-cramped rows)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "রিপোর্ট এক্সপোর্ট ও শেয়ার",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Row 1: CSV and PNG Export
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // CSV Button
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val csvContent = ReportExportUtils.generateTransactionsCsv(
                                            transactions = transactions,
                                            filterLabel = currentFilter.banglaLabel
                                        )
                                        ReportExportUtils.shareText(
                                            context = context,
                                            text = csvContent,
                                            title = "${currentFilter.banglaLabel} - লেনদেন CSV রিপোর্ট"
                                        )
                                        Toast.makeText(context, "CSV তৈরি হয়েছে", Toast.LENGTH_SHORT).show()
                                    }
                                    .testTag("btn_export_csv"),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = MawaPrimary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CSV ফাইল",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Memo Image (PNG) Export Button
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val bitmap = DataBackupRestoreManager.createMonthlyReportMemoBitmap(
                                            shopName = shopName,
                                            periodLabel = currentFilter.banglaLabel,
                                            totalSales = periodTotalSales,
                                            purchases = periodPurchases,
                                            shopExpenses = periodShopExpenses,
                                            netProfit = estimatedBusinessProfit,
                                            homeWithdrawals = periodHomeWithdrawals,
                                            profitRemaining = profitRemaining
                                        )
                                        DataBackupRestoreManager.shareBitmapAsImage(
                                            context = context,
                                            bitmap = bitmap,
                                            fileNamePrefix = "report_memo_${currentFilter.name.lowercase()}",
                                            title = "$shopName - ${currentFilter.banglaLabel} মেমো"
                                        )
                                    }
                                    .testTag("btn_export_memo_png"),
                                color = FinancialPositive.copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, FinancialPositive.copy(alpha = 0.35f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = FinancialPositive,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "মেমো ছবি (PNG)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        color = FinancialPositive
                                    )
                                }
                            }
                        }

                        // Row 2: Memo Dialog and PDF Print
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Digital Memo Text View Button
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showMemoDialog = true }
                                    .testTag("btn_view_memo"),
                                color = MawaPrimary.copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, MawaPrimary.copy(alpha = 0.35f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = MawaPrimary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "মেমো রসিদ",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        color = MawaPrimary
                                    )
                                }
                            }

                            // PDF / HTML Print Button
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val html = ReceiptPrintManager.generateFinancialReportHtml(
                                            shopName = shopName,
                                            shopOwner = shopSettings?.ownerName ?: "মালিক",
                                            shopPhone = shopSettings?.phone ?: "",
                                            periodLabel = currentFilter.banglaLabel,
                                            dateGenerated = BengaliUtils.getFormattedTodayDate(),
                                            totalSales = periodTotalSales,
                                            cashSales = periodCashSales,
                                            bakiSales = periodBakiSales,
                                            bakiCollection = periodBakiCollection,
                                            totalPurchases = periodPurchases,
                                            shopExpenses = periodShopExpenses,
                                            homeWithdrawals = periodHomeWithdrawals,
                                            netProfit = estimatedBusinessProfit,
                                            profitRemaining = profitRemaining,
                                            transactions = transactions
                                        )
                                        ReceiptPrintManager.printHtml(
                                            context = context,
                                            htmlContent = html,
                                            jobName = "Mawa_Report_${currentFilter.name}"
                                        )
                                    }
                                    .testTag("btn_reports_print_pdf"),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Assessment,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PDF প্রিন্ট",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Row 3: Daily Closure & Cloud/Local Backup Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Daily Closure Button
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showDailyClosureDialog = true }
                                    .testTag("btn_reports_daily_closure"),
                                color = FinancialWarningContainer.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, FinancialWarning.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = FinancialWarning,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "দিন সমাপ্তি ক্লোজার",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Backup Dialog Trigger
                            if (onOpenBackupRestore != null) {
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onOpenBackupRestore() }
                                        .testTag("btn_reports_backup"),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Backup,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ডাটা ব্যাকআপ",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Row 4: Bluetooth POS Thermal Print Button
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showBluetoothPrinterDialog = true }
                                .testTag("btn_reports_bluetooth_print"),
                            color = Color(0xFF0D9488).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFF0D9488).copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = null,
                                    tint = Color(0xFF0D9488),
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ব্লুটুথ POS প্রিন্টার (রসিদ/মেমো প্রিন্ট)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0D9488)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Main Business Profit & Remaining Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
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
                                Text(
                                    text = "টাকার উপর শতকরা লাভ (${BengaliUtils.toBengaliDigits(if (profitMarginPercent % 1.0 == 0.0) profitMarginPercent.toInt().toString() else profitMarginPercent.toString())}%)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = BengaliUtils.formatTaka(estimatedBusinessProfit),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FinancialPositive
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(FinancialPositiveContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = FinancialPositive,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Preset Margin Chips in Report
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                                    color = if (isSelected) FinancialPositive else Color.Transparent,
                                    border = BorderStroke(1.dp, if (isSelected) FinancialPositive else MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Profit Remaining After Home Withdrawals
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "বাড়ির খরচ বাদ দিয়ে অবশিষ্ট",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "ব্যবসায়িক লাভ − বাড়ির জন্য নেওয়া",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            Text(
                                text = BengaliUtils.formatTaka(profitRemaining),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (profitRemaining >= 0) MawaPrimary else FinancialNegative
                            )
                        }
                    }
                }
            }

            // Formal Audited Profit & Loss Statement (P&L নিরীক্ষিত খতিয়ান)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                text = "লাভ-ক্ষতির নিরীক্ষিত স্টেটমেন্ট (P&L)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MawaPrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = currentFilter.banglaLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MawaPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(8.dp))

                        PnLStatementRow(label = "(+) মোট বিক্রয় টার্নওভার", amount = periodTotalSales, isPositive = true)
                        PnLStatementRow(label = "(−) পাইকারি পণ্য ক্রয় (ফর্দ ও চালান)", amount = periodPurchases, isPositive = false)
                        PnLStatementRow(label = "(=) মোট বিক্রয় মুনাফা", amount = estimatedBusinessProfit, isPositive = true, isBold = true)
                        PnLStatementRow(label = "(−) দোকান পরিচালন খরচ", amount = periodShopExpenses, isPositive = false)
                        PnLStatementRow(label = "(−) ব্যক্তিগত / সংসার খরচ", amount = periodHomeWithdrawals, isPositive = false)

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "(=) ব্যবসায়ের রক্ষিত উদ্বৃত্ত মূলধন",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = BengaliUtils.formatTaka(profitRemaining),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (profitRemaining >= 0) FinancialPositive else FinancialNegative
                            )
                        }
                    }
                }
            }

            // Top Due Customers Card with WhatsApp & Call Actions (শীর্ষ দেনাদার ও তাগাদা)
            if (topDueCustomers.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.35f))
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFEF3C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "শীর্ষ বাকিদার (তাগাদা ও কালেকশন)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "${BengaliUtils.toBengaliDigits(topDueCustomers.size.toString())} জন",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(6.dp))

                            topDueCustomers.forEach { cust ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cust.customer.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        if (cust.customer.phone.isNotBlank()) {
                                            Text(
                                                text = cust.customer.phone,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = BengaliUtils.formatTaka(cust.currentBalance),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = FinancialNegative
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))

                                        // WhatsApp Reminder Button
                                        if (cust.customer.phone.isNotBlank()) {
                                            val phone = cust.customer.phone.trim()
                                            IconButton(
                                                onClick = {
                                                    try {
                                                        val cleanPhone = if (phone.startsWith("+88")) phone else if (phone.startsWith("88")) "+$phone" else "+88$phone"
                                                        val msg = "আসসালামু আলাইকুম ${cust.customer.name} ভাই, $shopName-এ আপনার পূর্ববর্তী বাকি হিসাব ${BengaliUtils.formatTaka(cust.currentBalance)} টাকা বাকি রয়েছে। সুবিধাজনক সময়ে পরিশোধ করার অনুরোধ রইল। ধন্যবাদ।"
                                                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(msg)}")
                                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "হোয়াটসঅ্যাপ অ্যাপ পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Chat,
                                                    contentDescription = "WhatsApp",
                                                    tint = Color(0xFF25D366),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            // Phone Call Button
                                            IconButton(
                                                onClick = {
                                                    try {
                                                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                                        context.startActivity(callIntent)
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "কল শুরু করা যায়নি", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Phone,
                                                    contentDescription = "কল",
                                                    tint = MawaPrimary,
                                                    modifier = Modifier.size(18.dp)
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

            // 4. Interactive Trend Line Chart (আপ-ডাউন রেখা ও প্রতিটি পয়েন্টে ক্লিক করে তথ্য দেখা)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                InteractiveTrendChart(
                    transactions = transactions,
                    dailyCashList = allDailyCash,
                    shopSettings = shopSettings
                )
            }

            // 5. Interactive Company / Supplier / Product Purchase & Sales Donut Chart (সকল পণ্য ও কোম্পানি ক্লিক করে ইতিহাস দেখা)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                InteractiveCompanyPurchaseDonutChart(
                    purchases = purchaseTransactions,
                    allTransactions = transactions
                )
            }

            // 5.5 Top Suppliers & Brands (শীর্ষ পাইকারি মালামাল সরবরাহকারী)
            if (topSuppliers.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEDE9FE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Store,
                                            contentDescription = null,
                                            tint = Color(0xFF7C3AED),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "শীর্ষ মালামাল সরবরাহকারী / ব্র্যান্ড",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "ক্রয় ভলিউম",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(6.dp))

                            val maxSpend = topSuppliers.maxOfOrNull { it.second } ?: 1.0
                            topSuppliers.forEach { (name, amount) ->
                                val ratio = (amount / maxSpend).toFloat().coerceIn(0.05f, 1f)
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
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = BengaliUtils.formatTaka(amount),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(ratio)
                                                .height(5.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Color(0xFF7C3AED))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Complete Financial Summary Breakdown (Detailed Clean Rows)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${currentFilter.banglaLabel}-এর পূর্ণাঙ্গ হিসাব",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )

                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        MawaSummaryRow(
                            label = "মোট বিক্রি (নগদ + বাকি)",
                            amount = periodTotalSales,
                            amountColor = MaterialTheme.colorScheme.onSurface
                        )
                        MawaSummaryRow(
                            label = "নগদ বিক্রি",
                            amount = periodCashSales,
                            amountColor = FinancialPositive,
                            isSubRow = true
                        )
                        MawaSummaryRow(
                            label = "বাকি বিক্রি",
                            amount = periodBakiSales,
                            amountColor = FinancialNegative,
                            isSubRow = true
                        )
                        MawaSummaryRow(
                            label = "বাকি আদায় (জমা)",
                            amount = periodBakiCollection,
                            amountColor = FinancialPositive
                        )
                        MawaSummaryRow(
                            label = "মাল কেনা (ফর্দ + সরাসরি)",
                            amount = periodPurchases,
                            amountColor = MaterialTheme.colorScheme.onSurface,
                            prefix = "−"
                        )
                        MawaSummaryRow(
                            label = "দোকানের পরিচালনা খরচ",
                            amount = periodShopExpenses,
                            amountColor = FinancialNegative,
                            prefix = "−"
                        )
                        MawaSummaryRow(
                            label = "বাড়ির জন্য নেওয়া",
                            amount = periodHomeWithdrawals,
                            amountColor = FinancialWarning,
                            prefix = "−",
                            showDivider = false
                        )
                    }
                }
            }

            // 7. Accounting Formula Reference & Notes (লাভ-ক্ষতি ও খতিয়ান নীতিমালা)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = FinancialPositive,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "লাভ-ক্ষতি যেভাবে হিসাব করা হয়",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "১. মোট বিক্রি = নগদ বিক্রি + বাকি বিক্রি (বাকি আদায় মূল বিক্রির হিসাব নয়, এটি পূর্বের দেনা পরিশোধ)।\n\n" +
                                    "২. শতকরা লাভ (%) = মোট বিক্রি × মুনাফার হার (যেমন: ১৫% ধরে ১,০০,০০০ টাকার বিক্রিতে ১৫,০০০ টাকা ব্যবসায়িক লাভ)।\n\n" +
                                    "৩. প্রকৃত নিট লাভ = মোট বিক্রি − মাল কেনা (ফর্দ+সরাসরি) − দোকানের পরিচালনা খরচ।\n\n" +
                                    "৪. বাড়ির খরচ = এটি ব্যবসার ক্ষতি নয়, ব্যবসায়ের অর্জিত লাভ থেকে মালিকের ব্যক্তিগত উত্তোলন।\n\n" +
                                    "৫. ক্যাশবক্সের ক্যাশ = সাবেক ক্যাশ + আজকের নগদ বিক্রি + বাকি আদায় − মাল ক্রয় − সকল খরচ।",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 8. Historical Daily Ledgers Section (পূর্ববর্তী দিনের খতিয়ান ও ইতিহাস)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MawaPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "পূর্ববর্তী দিনের খতিয়ান ও ইতিহাস",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${BengaliUtils.toBanglaDigits(historicalDailyLedgers.size.toLong())} দিন",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Text(
                            text = "যেকোনো দিনের কার্ডে ক্লিক করে ঐ দিনের পূর্ণাঙ্গ খতিয়ানে ফিরে যেতে পারেন:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        if (historicalDailyLedgers.isEmpty()) {
                            Text(
                                text = "কোনো পূর্ববর্তী দিনের লেনদেন পাওয়া যায়নি",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                historicalDailyLedgers.forEach { dayGroup ->
                                    val isExpanded = expandedDayMillis == dayGroup.dayStartMillis

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                if (onNavigateToDayOnHome != null) {
                                                    onNavigateToDayOnHome(dayGroup.dayStartMillis)
                                                } else {
                                                    expandedDayMillis = if (isExpanded) null else dayGroup.dayStartMillis
                                                }
                                            }
                                            .testTag("historical_day_${dayGroup.dayStartMillis}"),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = MawaPrimary.copy(alpha = 0.15f),
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                imageVector = Icons.Default.CalendarMonth,
                                                                contentDescription = null,
                                                                tint = MawaPrimary,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = dayGroup.dateLabel,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = "${dayGroup.weekday} · ${BengaliUtils.toBanglaDigits(dayGroup.txList.size.toLong())}টি হিসাব",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.outline
                                                        )
                                                    }
                                                }

                                                // Navigate Button
                                                Button(
                                                    onClick = {
                                                        onNavigateToDayOnHome?.invoke(dayGroup.dayStartMillis)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(30.dp).testTag("open_day_${dayGroup.dayStartMillis}_btn")
                                                ) {
                                                    Text("খতিয়ান দেখুন", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Metrics Row (বিক্রি, মাল ক্রয়, খরচ, লাভ)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text("বিক্রি", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                                    Text(BengaliUtils.formatTaka(dayGroup.totalSales), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = FinancialPositive)
                                                }
                                                Column {
                                                    Text("মাল ক্রয়", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                                    Text(BengaliUtils.formatTaka(dayGroup.purchases), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                                                }
                                                Column {
                                                    Text("খরচ", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                                    Text(BengaliUtils.formatTaka(dayGroup.shopExpenses + dayGroup.homeWithdrawals), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = FinancialNegative)
                                                }
                                                Column {
                                                    Text("লাভ", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                                    Text(BengaliUtils.formatTaka(dayGroup.estimatedProfit), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (dayGroup.estimatedProfit >= 0) MawaPrimary else FinancialNegative)
                                                }
                                            }

                                            // Toggle transaction list view
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { expandedDayMillis = if (isExpanded) null else dayGroup.dayStartMillis }
                                                    .padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = if (isExpanded) "লেনদেন তালিকা লুকান" else "লেনদেন তালিকা দেখুন (${BengaliUtils.toBanglaDigits(dayGroup.txList.size.toLong())}টি)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Icon(
                                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            if (isExpanded) {
                                                HorizontalDivider(
                                                    thickness = 0.5.dp,
                                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                                    modifier = Modifier.padding(vertical = 6.dp)
                                                )
                                                dayGroup.txList.forEach { tx ->
                                                    TransactionItemRow(
                                                        transaction = tx,
                                                        onDelete = { viewModel.deleteTransaction(tx.id) }
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

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Digital Memo / Slip Preview Dialog
    if (showMemoDialog) {
        val memoText = remember(transactions, currentFilter) {
            ReportExportUtils.generateMemoText(
                shopName = shopName,
                filterLabel = currentFilter.banglaLabel,
                totalSales = periodTotalSales,
                cashSales = periodCashSales,
                bakiSales = periodBakiSales,
                bakiCollection = periodBakiCollection,
                purchases = periodPurchases,
                shopExpenses = periodShopExpenses,
                homeWithdrawals = periodHomeWithdrawals,
                profit = estimatedBusinessProfit,
                profitRemaining = profitRemaining
            )
        }

        Dialog(onDismissRequest = { showMemoDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MawaPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "হিসাবের মেমো / রসিদ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = { showMemoDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Formatted Paper-style Slip Box
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = memoText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Copy & Share Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(memoText))
                                Toast.makeText(context, "মেমো কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("কপি")
                        }

                        Button(
                            onClick = {
                                ReportExportUtils.shareText(
                                    context = context,
                                    text = memoText,
                                    title = "$shopName - ${currentFilter.banglaLabel} মেমো"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("শেয়ার")
                        }
                    }
                }
            }
        }

        // Daily Closure Dialog
        if (showDailyClosureDialog) {
            val closureSummary by viewModel.selectedDateAccountingSummary.collectAsStateWithLifecycle()

            DailyClosureDialog(
                dateMillis = System.currentTimeMillis(),
                openingBalance = closureSummary.openingBalance,
                cashSales = closureSummary.todayCashSales,
                bakiSales = closureSummary.todayBakiSales,
                bakiCollection = closureSummary.todayBakiCollection,
                purchases = closureSummary.todayPurchases,
                shopExpenses = closureSummary.todayShopExpenses,
                homeWithdrawals = closureSummary.todayHomeWithdrawals,
                onDismiss = { showDailyClosureDialog = false }
            )
        }

        // Bluetooth Thermal POS Printer Dialog
        if (showBluetoothPrinterDialog) {
            BluetoothThermalPrinterDialog(
                shopName = shopName,
                shopPhone = shopSettings?.phone ?: "",
                memoNo = "RPT-${currentFilter.name}",
                dateFormatted = BengaliUtils.getFormattedTodayDate(),
                customerName = "হিসাব রিপোর্ট (${currentFilter.banglaLabel})",
                items = listOf(
                    InvoiceItem(name = "মোট বিক্রি", amount = periodTotalSales),
                    InvoiceItem(name = "নগদ বিক্রি", amount = periodCashSales),
                    InvoiceItem(name = "বাকি বিক্রি", amount = periodBakiSales),
                    InvoiceItem(name = "বাকি আদায়", amount = periodBakiCollection),
                    InvoiceItem(name = "পণ্য ক্রয়", amount = periodPurchases),
                    InvoiceItem(name = "দোকান খরচ", amount = periodShopExpenses),
                    InvoiceItem(name = "ব্যবসায়িক লাভ", amount = estimatedBusinessProfit),
                    InvoiceItem(name = "বাড়ির খরচ", amount = periodHomeWithdrawals),
                    InvoiceItem(name = "অবশিষ্ট উদ্বৃত্ত", amount = profitRemaining)
                ),
                totalAmount = periodTotalSales,
                paidAmount = periodCashInflow,
                dueAmount = periodTotalOutflow,
                onDismiss = { showBluetoothPrinterDialog = false }
            )
        }
    }
}

@Composable
private fun ReportKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PnLStatementRow(
    label: String,
    amount: Double,
    isPositive: Boolean,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = BengaliUtils.formatTaka(amount),
            style = if (isBold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isPositive) FinancialPositive else FinancialNegative
        )
    }
}
