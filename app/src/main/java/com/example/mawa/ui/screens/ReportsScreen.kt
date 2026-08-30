package com.example.mawa.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
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
    val transactions by viewModel.filteredTransactionsForReport.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val shopSettings by viewModel.shopSettings.collectAsStateWithLifecycle()

    var showMemoDialog by remember { mutableStateOf(false) }
    var showDailyClosureDialog by remember { mutableStateOf(false) }
    var expandedDayMillis by remember { mutableStateOf<Long?>(null) }

    // Group historical transactions by day
    val historicalDailyLedgers = remember(allTransactions) {
        val cal = Calendar.getInstance()
        val groups = allTransactions.groupBy { tx ->
            cal.timeInMillis = tx.timestamp
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }

        groups.entries.map { (dayMillis, txList) ->
            val sales = txList.filter { it.type == TransactionType.SALE_CASH || it.type == TransactionType.SALE_BAKI }.sumOf { it.amount }
            val purchases = txList.filter { it.type == TransactionType.PURCHASE_FORDI || it.type == TransactionType.PURCHASE_DIRECT }.sumOf { it.amount }
            val shopExp = txList.filter { it.type == TransactionType.EXPENSE_SHOP }.sumOf { it.amount }
            val homeExp = txList.filter { it.type == TransactionType.EXPENSE_HOME }.sumOf { it.amount }
            val profit = sales - purchases - shopExp

            cal.timeInMillis = dayMillis
            val dateLabel = BengaliUtils.getFormattedTodayDate(dayMillis)
            val weekday = SimpleDateFormat("EEEE", Locale("bn", "BD")).format(Date(dayMillis))

            DailyLedgerGroup(
                dayStartMillis = dayMillis,
                dateLabel = dateLabel,
                weekday = weekday,
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

    // Real-time calculated metrics for the selected time filter
    val periodCashSales = remember(transactions) {
        transactions.filter { it.type == TransactionType.SALE_CASH }.sumOf { it.amount }
    }
    val periodBakiSales = remember(transactions) {
        transactions.filter { it.type == TransactionType.SALE_BAKI }.sumOf { it.amount }
    }
    val periodTotalSales = periodCashSales + periodBakiSales

    val periodBakiCollection = remember(transactions) {
        transactions.filter { it.type == TransactionType.BAKI_COLLECTION }.sumOf { it.amount }
    }

    val purchaseTransactions = remember(transactions) {
        transactions.filter { it.type == TransactionType.PURCHASE_FORDI || it.type == TransactionType.PURCHASE_DIRECT }
    }

    val periodPurchases = remember(purchaseTransactions) {
        purchaseTransactions.sumOf { it.amount }
    }

    val periodShopExpenses = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE_SHOP }.sumOf { it.amount }
    }

    val periodHomeWithdrawals = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE_HOME }.sumOf { it.amount }
    }

    var profitMarginPercent by remember {
        mutableStateOf(com.example.mawa.util.ProfitMarginManager.getProfitMargin(context))
    }

    // Accounting Formulas (টাকার উপর শতকরা লাভ)
    val estimatedBusinessProfit = com.example.mawa.util.ProfitMarginManager.calculateProfit(periodTotalSales, profitMarginPercent)
    val profitRemaining = estimatedBusinessProfit - periodHomeWithdrawals

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        MawaTopBar(
            title = "রিপোর্ট ও লাভ-ক্ষতি",
            subtitle = "${currentFilter.banglaLabel}-এর পূর্ণাঙ্গ বিবরণী",
            onMenuClick = onOpenDrawer
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // 1. Time Filter Selector (Daily, Weekly, Monthly, All-time)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TimeFilter.values().forEach { filter ->
                            val isSelected = currentFilter == filter
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
                    }
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

            // 3. Export & Share Action Bar (CSV, মেমো ছবি PNG, মেমো ভিউ, ব্যাকআপ)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 1.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // CSV Download / Export Button
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
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
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = MawaPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "CSV ফাইল",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Memo Image (PNG) Export Button
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
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
                                color = FinancialPositive.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, FinancialPositive.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = FinancialPositive,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ছবি (PNG)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = FinancialPositive
                                    )
                                }
                            }

                            // Digital Memo Text View Button
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showMemoDialog = true }
                                    .testTag("btn_view_memo"),
                                color = MawaPrimary.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, MawaPrimary.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = MawaPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "মেমো রসিদ",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MawaPrimary
                                    )
                                }
                            }

                            // PDF / HTML Print Button
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
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
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Assessment,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "PDF প্রিন্ট",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Daily Closure / Lock Button
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showDailyClosureDialog = true }
                                    .testTag("btn_reports_daily_closure"),
                                color = FinancialWarningContainer.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, FinancialWarning.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "দিন সমাপ্তি",
                                        tint = FinancialWarning,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Backup Dialog Trigger
                            if (onOpenBackupRestore != null) {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onOpenBackupRestore() }
                                        .testTag("btn_reports_backup"),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Backup,
                                            contentDescription = "ব্যাকআপ",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
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

            // 4. Interactive Trend Line Chart (আপ-ডাউন রেখা ও প্রতিটি পয়েন্টে ক্লিক করে তথ্য দেখা)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                InteractiveTrendChart(transactions = transactions)
            }

            // 5. Interactive Company / Supplier / Product Purchase & Sales Donut Chart (সকল পণ্য ও কোম্পানি ক্লিক করে ইতিহাস দেখা)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                InteractiveCompanyPurchaseDonutChart(
                    purchases = purchaseTransactions,
                    allTransactions = transactions
                )
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
            val closureTransactions by viewModel.selectedDateTransactions.collectAsStateWithLifecycle()
            val closureCashSales = closureTransactions.filter { it.type == TransactionType.SALE_CASH }.sumOf { it.amount }
            val closureBakiSales = closureTransactions.filter { it.type == TransactionType.SALE_BAKI }.sumOf { it.amount }
            val closureBakiCollection = closureTransactions.filter { it.type == TransactionType.BAKI_COLLECTION }.sumOf { it.amount }
            val closurePurchases = closureTransactions.filter { it.type == TransactionType.PURCHASE_FORDI || it.type == TransactionType.PURCHASE_DIRECT }.sumOf { it.amount }
            val closureExpenses = closureTransactions.filter { it.type == TransactionType.EXPENSE_SHOP }.sumOf { it.amount }
            val closureHome = closureTransactions.filter { it.type == TransactionType.EXPENSE_HOME }.sumOf { it.amount }

            DailyClosureDialog(
                dateMillis = System.currentTimeMillis(),
                openingBalance = shopSettings?.openingBalance ?: 0.0,
                cashSales = closureCashSales,
                bakiSales = closureBakiSales,
                bakiCollection = closureBakiCollection,
                purchases = closurePurchases,
                shopExpenses = closureExpenses,
                homeWithdrawals = closureHome,
                onDismiss = { showDailyClosureDialog = false }
            )
        }
    }
}
