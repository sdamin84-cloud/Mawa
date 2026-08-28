package com.example.mawa.ui.screens

import android.content.Context
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType
import com.example.mawa.data.repository.MawaRepository
import com.example.mawa.ui.components.MawaAmountInput
import com.example.mawa.ui.components.MawaTopBar
import com.example.mawa.ui.components.TransactionItemRow
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.DataBackupRestoreManager
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

enum class CashReportTab(val banglaLabel: String) {
    CASHBOX_REPORT("ক্যাশবক্স রিপোর্ট"),
    CASH_REPORT("ক্যাশ রিপোর্ট")
}

@Composable
fun CashTallyScreen(
    viewModel: MawaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(CashReportTab.CASHBOX_REPORT) }
    var showDetailedBreakdown by remember { mutableStateOf(false) }
    var showCashReconciliationSheet by remember { mutableStateOf(false) }

    // Date navigation state
    val selectedDateMillis by viewModel.selectedHomeDateMillis.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val shopSettings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()

    val startOfDay = remember(selectedDateMillis) { MawaRepository.getStartOfDayMillis(selectedDateMillis) }
    val endOfDay = remember(selectedDateMillis) { MawaRepository.getEndOfDayMillis(selectedDateMillis) }

    // Transactions for the selected date
    val dayTransactions = remember(allTransactions, startOfDay, endOfDay) {
        allTransactions.filter { it.timestamp in startOfDay..endOfDay }.sortedByDescending { it.timestamp }
    }

    // Cashflow components for Cashbox
    val openingCash = shopSettings?.openingBalance ?: 0.0

    val cashSales = remember(dayTransactions) {
        dayTransactions.filter { it.type == TransactionType.SALE_CASH }.sumOf { it.amount }
    }
    val bakiCollection = remember(dayTransactions) {
        dayTransactions.filter { it.type == TransactionType.BAKI_COLLECTION }.sumOf { it.amount }
    }
    val ownerDeposit = remember(dayTransactions) {
        dayTransactions.filter { it.type == TransactionType.CASH_ADJUSTMENT && it.amount > 0 }.sumOf { it.amount }
    }

    val cashPurchases = remember(dayTransactions) {
        dayTransactions.filter { it.type == TransactionType.PURCHASE_FORDI || it.type == TransactionType.PURCHASE_DIRECT }.sumOf { it.amount }
    }
    val shopExpenses = remember(dayTransactions) {
        dayTransactions.filter { it.type == TransactionType.EXPENSE_SHOP }.sumOf { it.amount }
    }
    val ownerWithdrawal = remember(dayTransactions) {
        dayTransactions.filter { it.type == TransactionType.EXPENSE_HOME }.sumOf { it.amount }
    }

    val totalInflow = openingCash + cashSales + bakiCollection + ownerDeposit
    val totalOutflow = cashPurchases + shopExpenses + ownerWithdrawal
    val cashboxBalance = totalInflow - totalOutflow

    val dateFormatted = remember(selectedDateMillis) {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val monthStr = SimpleDateFormat("MMM", Locale("bn", "BD")).format(cal.time)
        val isToday = MawaRepository.getStartOfDayMillis(System.currentTimeMillis()) == startOfDay
        if (isToday) "আজ, $day $monthStr" else "$day $monthStr"
    }

    val shopName = shopSettings?.shopName?.ifBlank { "মাওয়া স্মার্ট খাতা" } ?: "মাওয়া স্মার্ট খাতা"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        MawaTopBar(
            title = "ক্যাশ হিসাব",
            subtitle = "ক্যাশবক্স ব্যালেন্স: ${BengaliUtils.formatTaka(cashboxBalance)}",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "পেছনে")
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        val bitmap = DataBackupRestoreManager.createDailyCashboxMemoBitmap(
                            shopName = shopName,
                            dateLabel = dateFormatted,
                            openingCash = openingCash,
                            cashSales = cashSales,
                            bakiCollection = bakiCollection,
                            ownerDeposit = ownerDeposit,
                            purchases = cashPurchases,
                            expenses = shopExpenses,
                            ownerWithdrawal = ownerWithdrawal,
                            closingBalance = cashboxBalance
                        )
                        DataBackupRestoreManager.shareBitmapAsImage(
                            context = context,
                            bitmap = bitmap,
                            fileNamePrefix = "cashbox_report",
                            title = "$dateFormatted - ক্যাশবক্স হিসাব রসিদ"
                        )
                    }
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "রসিদ শেয়ার", tint = Color.White)
                }
            }
        )

        // Tally-Style Top Tab Row: [ ক্যাশবক্স রিপোর্ট ] | [ ক্যাশ রিপোর্ট ]
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MawaPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = MawaPrimary,
                    height = 3.dp
                )
            }
        ) {
            CashReportTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = tab.banglaLabel,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                )
            }
        }

        // Sub Header with Date Selector & "বিস্তারিত" Checkbox (Exact match to Tally screenshot)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date Selector with < >
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.goToPreviousDay() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "আগের দিন", modifier = Modifier.size(20.dp))
                    }

                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    IconButton(
                        onClick = { viewModel.goToNextDay() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "পরের দিন", modifier = Modifier.size(20.dp))
                    }
                }

                // Checkbox: বিস্তারিত
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showDetailedBreakdown = !showDetailedBreakdown }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Checkbox(
                        checked = showDetailedBreakdown,
                        onCheckedChange = { showDetailedBreakdown = it },
                        colors = CheckboxDefaults.colors(checkedColor = MawaPrimary),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "বিস্তারিত",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Table Header: বিবরণ (with download icon) | পেলাম (Green) | দিলাম (Red)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.3f)) {
                    Text(
                        text = "বিবরণ",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "ডাউনলোড",
                        tint = MawaPrimary,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                val bitmap = DataBackupRestoreManager.createDailyCashboxMemoBitmap(
                                    shopName = shopName,
                                    dateLabel = dateFormatted,
                                    openingCash = openingCash,
                                    cashSales = cashSales,
                                    bakiCollection = bakiCollection,
                                    ownerDeposit = ownerDeposit,
                                    purchases = cashPurchases,
                                    expenses = shopExpenses,
                                    ownerWithdrawal = ownerWithdrawal,
                                    closingBalance = cashboxBalance
                                )
                                DataBackupRestoreManager.shareBitmapAsImage(
                                    context = context,
                                    bitmap = bitmap,
                                    fileNamePrefix = "tally_report",
                                    title = "$dateFormatted - ক্যাশবক্স রিপোর্ট"
                                )
                            }
                    )
                }

                Text(
                    text = "পেলাম",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = FinancialPositive,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "দিলাম",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = FinancialNegative,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Main Content depending on Tab
        if (selectedTab == CashReportTab.CASHBOX_REPORT) {
            // --- TAB 1: ক্যাশবক্স রিপোর্ট (Categorized Tally In/Out) ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        TallyRowItem(
                            label = "দিনের শুরুতে ক্যাশ",
                            subtitle = if (showDetailedBreakdown) "সাবেক প্রারম্ভিক ক্যাশ ইন হ্যান্ড" else null,
                            pelamAmount = openingCash,
                            dilamAmount = null
                        )

                        TallyRowItem(
                            label = "ক্যাশ বেচা",
                            subtitle = if (showDetailedBreakdown) "${dayTransactions.count { it.type == TransactionType.SALE_CASH }} টি নগদ বিক্রয় লেনদেন" else null,
                            pelamAmount = cashSales,
                            dilamAmount = null
                        )

                        TallyRowItem(
                            label = "বাকি আদায়",
                            subtitle = if (showDetailedBreakdown) "${dayTransactions.count { it.type == TransactionType.BAKI_COLLECTION }} টি গ্রাহক খতিয়ান জমা" else null,
                            pelamAmount = bakiCollection,
                            dilamAmount = null
                        )

                        if (ownerDeposit > 0 || showDetailedBreakdown) {
                            TallyRowItem(
                                label = "মালিক দিল (ক্যাশ বৃদ্ধি)",
                                subtitle = if (showDetailedBreakdown) "মালিক বা ব্যবসার অতিরিক্ত ক্যাশ জমা" else null,
                                pelamAmount = ownerDeposit,
                                dilamAmount = null
                            )
                        }

                        TallyRowItem(
                            label = "ক্যাশ কেনা (মাল খরিদ)",
                            subtitle = if (showDetailedBreakdown) "${dayTransactions.count { it.type == TransactionType.PURCHASE_FORDI || it.type == TransactionType.PURCHASE_DIRECT }} টি ফর্দ ও পাইকারি ক্রয়" else null,
                            pelamAmount = null,
                            dilamAmount = cashPurchases
                        )

                        TallyRowItem(
                            label = "দোকানের খরচ",
                            subtitle = if (showDetailedBreakdown) "চা, নাস্তা, ভাড়া, পরিবহন ও অন্যান্য ব্যয়" else null,
                            pelamAmount = null,
                            dilamAmount = shopExpenses
                        )

                        TallyRowItem(
                            label = "মালিক নিল (সংসার)",
                            subtitle = if (showDetailedBreakdown) "দোকানের ক্যাশ থেকে সংসার বা ব্যক্তিগত উত্তোলন" else null,
                            pelamAmount = null,
                            dilamAmount = ownerWithdrawal
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        } else {
            // --- TAB 2: ক্যাশ রিপোর্ট (Individual Cash Transactions for Date) ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (dayTransactions.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "এই তারিখে কোনো ক্যাশ লেনদেন নেই",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(dayTransactions, key = { it.id }) { tx ->
                        val isPelam = tx.type == TransactionType.SALE_CASH || tx.type == TransactionType.BAKI_COLLECTION || (tx.type == TransactionType.CASH_ADJUSTMENT && tx.amount > 0)
                        val isDilam = tx.type == TransactionType.PURCHASE_FORDI || tx.type == TransactionType.PURCHASE_DIRECT || tx.type == TransactionType.EXPENSE_SHOP || tx.type == TransactionType.EXPENSE_HOME

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.3f)) {
                                        Text(
                                            text = tx.note.ifBlank { tx.type.banglaLabel },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${BengaliUtils.formatTransactionTime(tx.timestamp)} · ${tx.type.banglaLabel}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Text(
                                        text = if (isPelam) BengaliUtils.formatTaka(tx.amount) else "-",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPelam) FinancialPositive else MaterialTheme.colorScheme.outline,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Text(
                                        text = if (isDilam) BengaliUtils.formatTaka(tx.amount) else "-",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDilam) FinancialNegative else MaterialTheme.colorScheme.outline,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // Bottom Fixed Total Summary Bar (মোট: পেলাম ... দিলাম ... | ব্যালেন্স: ...)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "মোট হিসাব",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.3f)
                    )

                    Text(
                        text = BengaliUtils.formatTaka(totalInflow),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FinancialPositive,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = BengaliUtils.formatTaka(totalOutflow),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FinancialNegative,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                // Closing Balance Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ক্যাশবক্স ব্যালেন্স:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = BengaliUtils.formatTaka(cashboxBalance),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (cashboxBalance >= 0) MawaPrimary else FinancialNegative
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Big Button: [ ক্যাশবক্স মিলাই ]
                Button(
                    onClick = { showCashReconciliationSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_reconcile_cashbox"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ক্যাশবক্স মিলাই (নোট গণনা)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Modal Sheet for Cash Denomination Reconciliation
    if (showCashReconciliationSheet) {
        CashReconciliationSheet(
            systemBalance = cashboxBalance,
            onDismiss = { showCashReconciliationSheet = false }
        )
    }
}

@Composable
private fun TallyRowItem(
    label: String,
    subtitle: String? = null,
    pelamAmount: Double?,
    dilamAmount: Double?
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.3f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = if (pelamAmount != null && pelamAmount > 0) BengaliUtils.formatTaka(pelamAmount) else "-",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (pelamAmount != null && pelamAmount > 0) FinancialPositive else MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = if (dilamAmount != null && dilamAmount > 0) BengaliUtils.formatTaka(dilamAmount) else "-",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (dilamAmount != null && dilamAmount > 0) FinancialNegative else MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CashReconciliationSheet(
    systemBalance: Double,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val denominations = listOf(1000, 500, 200, 100, 50, 20, 10, 5, 2, 1)
    val noteCounts = remember { mutableStateMapOf<Int, String>() }

    val physicalCashCalculated = denominations.sumOf { denom ->
        val count = noteCounts[denom]?.toIntOrNull() ?: 0
        denom * count.toDouble()
    }

    val difference = physicalCashCalculated - systemBalance

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "ক্যাশবক্স মিলানো (ফিজিক্যাল ক্যাশ গণনা)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "ক্যাশবক্সের টাকা গুনে নোটের সংখ্যা লিখুন",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Comparison Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "হিসাব অনুযায়ী ক্যাশ:", style = MaterialTheme.typography.bodyMedium)
                        Text(text = BengaliUtils.formatTaka(systemBalance), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "গণনাকৃত মোট ক্যাশ:", style = MaterialTheme.typography.bodyMedium)
                        Text(text = BengaliUtils.formatTaka(physicalCashCalculated), fontWeight = FontWeight.Bold, color = MawaPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "পার্থক্য (ক্যাশ মিল):", fontWeight = FontWeight.Bold)
                        Text(
                            text = if (difference == 0.0) "✓ পুরোপুরি মিলেছে"
                            else if (difference > 0) "+${BengaliUtils.formatTaka(difference)} বেশি"
                            else "−${BengaliUtils.formatTaka(Math.abs(difference))} কম",
                            fontWeight = FontWeight.Bold,
                            color = if (difference == 0.0) FinancialPositive else if (difference > 0) FinancialWarning else FinancialNegative
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Note denomination rows
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                items(denominations) { denom ->
                    val countStr = noteCounts[denom] ?: ""
                    val count = countStr.toIntOrNull() ?: 0
                    val rowTotal = denom * count

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "৳ $denom ×",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(80.dp)
                        )

                        OutlinedTextField(
                            value = countStr,
                            onValueChange = { noteCounts[denom] = it.filter { ch -> ch.isDigit() } },
                            placeholder = { Text("০") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .width(100.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Text(
                            text = "= ${BengaliUtils.formatTaka(rowTotal.toDouble())}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
            ) {
                Text("সম্পন্ন হয়েছে", fontWeight = FontWeight.Bold)
            }
        }
    }
}
