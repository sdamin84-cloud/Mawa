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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import com.example.mawa.data.local.entity.CashTallyEntity
import com.example.mawa.ui.components.DenominationCounterBottomSheet
import com.example.mawa.ui.components.shareTallyViaIntent
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

    val currentDateKey = remember(selectedDateMillis) {
        SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(selectedDateMillis))
    }
    val dayTallies by viewModel.getCashTalliesForDateFlow(currentDateKey).collectAsStateWithLifecycle(initialValue = emptyList<CashTallyEntity>())
    var editingTally by remember { mutableStateOf<CashTallyEntity?>(null) }

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

                // Saved Note Entries for this day
                if (dayTallies.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "সংরক্ষিত নোট এন্ট্রি (${BengaliUtils.toBengaliDigits(dayTallies.size.toString())} টি):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "নোটের বিবরণসহ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MawaPrimary
                            )
                        }
                    }

                    items(dayTallies, key = { it.id }) { tally ->
                        SavedNoteBreakdownCard(
                            tally = tally,
                            shopName = shopName,
                            onEdit = {
                                editingTally = tally
                                showCashReconciliationSheet = true
                            },
                            onDelete = {
                                viewModel.deleteCashTally(tally.id)
                            }
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

                // Also display saved note tallies in Tab 2
                if (dayTallies.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "সংরক্ষিত নোটের হিসাব (${BengaliUtils.toBengaliDigits(dayTallies.size.toString())} টি):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    items(dayTallies, key = { it.id }) { tally ->
                        SavedNoteBreakdownCard(
                            tally = tally,
                            shopName = shopName,
                            onEdit = {
                                editingTally = tally
                                showCashReconciliationSheet = true
                            },
                            onDelete = {
                                viewModel.deleteCashTally(tally.id)
                            }
                        )
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
                    onClick = {
                        editingTally = dayTallies.firstOrNull()
                        showCashReconciliationSheet = true
                    },
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
                        text = if (dayTallies.isNotEmpty()) "ক্যাশবক্স মিলাই (নোট আপডেট/গণনা)" else "ক্যাশবক্স মিলাই (নোট গণনা)",
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
        DenominationCounterBottomSheet(
            systemBalance = cashboxBalance,
            dateKey = currentDateKey,
            dateMillis = selectedDateMillis,
            initialTally = editingTally ?: dayTallies.firstOrNull(),
            shopName = shopName,
            onSaveTally = { tally ->
                viewModel.saveCashTally(tally, updateClosingCash = true)
            },
            onDismiss = {
                showCashReconciliationSheet = false
                editingTally = null
            },
            onApplyCountedTotal = { counted ->
                viewModel.saveDailyClosingCash(currentDateKey, selectedDateMillis, counted, isClosed = true)
            }
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

@Composable
private fun SavedNoteBreakdownCard(
    tally: CashTallyEntity,
    shopName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("saved_tally_card_${tally.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MawaPrimary.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Icon, Time, Total, Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MawaPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = MawaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        val timeStr = remember(tally.timestamp) {
                            SimpleDateFormat("hh:mm a", Locale.US).format(Date(tally.timestamp))
                        }
                        Text(
                            text = "সংরক্ষিত নোট এন্ট্রি ($timeStr)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "মোট নোট: ${BengaliUtils.toBengaliDigits(tally.totalNotesCount().toString())} টি",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { shareTallyViaIntent(context, tally, shopName) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "শেয়ার",
                            tint = MawaPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "এডিট",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "মুছুন",
                            tint = FinancialNegative,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Total Amount Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "গণনাকৃত মোট ক্যাশ:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = BengaliUtils.formatTaka(tally.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MawaPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Banknote breakdown: exact count of each note!
            Text(
                text = "কোন নোট কতটি ছিল:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            val breakdown = tally.getDenominationsBreakdown()
            if (breakdown.isEmpty()) {
                Text(
                    text = "কোনো নির্দিষ্ট নোটের সংখ্যা উল্লেখ নেই",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    breakdown.chunked(2).forEach { rowPair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPair.forEach { (denom, count) ->
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "৳ ${BengaliUtils.toBengaliDigits(denom.toString())} × ${BengaliUtils.toBengaliDigits(count.toString())}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "= ৳ ${BengaliUtils.formatTaka((denom * count).toDouble())}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MawaPrimary
                                        )
                                    }
                                }
                            }
                            if (rowPair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Difference status if systemBalance > 0
            if (tally.systemBalance > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "হিসাবের সাথে পার্থক্য:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val diff = tally.difference
                    val diffText = if (diff == 0.0) "✓ পুরোপুরি মিলেছে"
                    else if (diff > 0) "+${BengaliUtils.formatTaka(diff)} বেশি"
                    else "−${BengaliUtils.formatTaka(Math.abs(diff))} কম"
                    val diffColor = if (diff == 0.0) FinancialPositive else if (diff > 0) FinancialWarning else FinancialNegative
                    Text(
                        text = diffText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = diffColor
                    )
                }
            }
        }
    }
}
