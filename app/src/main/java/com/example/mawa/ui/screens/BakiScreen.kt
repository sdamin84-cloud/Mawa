package com.example.mawa.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.R
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.local.entity.CustomerEntity
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType
import com.example.mawa.data.model.CustomerWithBalance
import com.example.mawa.ui.components.BakiReminderDialog
import com.example.mawa.ui.components.BulkReminderDialog
import com.example.mawa.ui.components.CustomerStatementReportDialog
import com.example.mawa.ui.components.DigitalReceiptDialog
import com.example.mawa.ui.components.MawaAmountInput
import com.example.mawa.ui.components.MawaEmptyState
import com.example.mawa.ui.components.MawaTopBar
import com.example.mawa.ui.components.PaymentQrDialog
import com.example.mawa.ui.components.SetPromiseDateDialog
import com.example.mawa.ui.components.SettlementDiscountDialog
import com.example.mawa.ui.components.TransactionItemRow
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.DataBackupRestoreManager
import com.example.mawa.util.InvoiceItem
import com.example.mawa.util.ReceiptPrintManager
import com.example.mawa.util.ReportExportUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialNegativeContainer
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialPositiveContainer
import com.example.ui.theme.MawaPrimary
import com.example.mawa.ui.components.BakiAgingDonutChart
import com.example.mawa.ui.components.BakiCreditVsCollectionChart
import com.example.mawa.ui.components.BakiRiskDistributionCard
import com.example.mawa.ui.components.AllBakiSummaryReportDialog
import com.example.mawa.ui.components.SupplierBakiSection
import com.example.mawa.data.local.entity.SupplierCompanyEntity

enum class BakiSubTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CUSTOMER("গ্রাহক বাকি (পাওনা)", Icons.Default.People),
    SUPPLIER("সাপ্লায়ার বাকি (মহাজন দেনা)", Icons.Default.Handshake)
}

enum class BakiFilter(val banglaLabel: String) {
    ALL("সব"),
    DUE_ONLY("বাকি আছে"),
    VIP("VIP গ্রাহক"),
    REGULAR("নিয়মিত"),
    AGING_30_DAYS("৩০+ দিন পুরানো"),
    OVERDUE_DATE("তারিখ পার হয়েছে"),
    OVER_LIMIT("সীমা অতিক্রম"),
    SETTLED("পরিশোধ সম্পন্ন"),
    TODAY("আজকের লেনদেন")
}

enum class BakiSort(val banglaLabel: String) {
    HIGHEST_DUE("সর্বোচ্চ বাকি"),
    LOWEST_DUE("সর্বনিম্ন বাকি"),
    RECENT("সাম্প্রতিক লেনদেন"),
    NAME_AZ("নাম (A-Z)"),
    OLDEST_DUE("পুরানো বাকি")
}

/**
 * Polished visual badge for Customer Status (VIP, Regular, Overdue, Aging, etc.)
 */
@Composable
fun CustomerBadge(
    label: String,
    containerColor: Color,
    contentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(5.dp),
        color = containerColor,
        border = BorderStroke(0.8.dp, contentColor.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
fun BakiScreen(
    viewModel: MawaViewModel,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val customersWithBalance by viewModel.customersWithBalance.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val shopSettings by viewModel.shopSettings.collectAsStateWithLifecycle()

    val shopName = shopSettings?.shopName?.ifBlank { "মাওয়া স্মার্ট খাতা" } ?: "মাওয়া স্মার্ট খাতা"
    val shopOwner = shopSettings?.ownerName ?: "প্রোপাইটার"
    val shopPhone = shopSettings?.phone ?: ""

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(BakiFilter.ALL) }
    var selectedSort by remember { mutableStateOf(BakiSort.HIGHEST_DUE) }
    var showSortMenu by remember { mutableStateOf(false) }

    var selectedCustomerForLedger by remember { mutableStateOf<CustomerWithBalance?>(null) }
    val allSupplierCompanies by viewModel.allSupplierCompanies.collectAsStateWithLifecycle()
    var selectedBakiSubTab by remember { mutableStateOf(BakiSubTab.CUSTOMER) }
    var showAllBakiReportDialog by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showBulkReminderDialog by remember { mutableStateOf(false) }
    var reminderCustomerTarget by remember { mutableStateOf<CustomerWithBalance?>(null) }
    var reportCustomerTarget by remember { mutableStateOf<CustomerWithBalance?>(null) }
    var quickBakiCustomerTarget by remember { mutableStateOf<CustomerWithBalance?>(null) }
    var quickJomaCustomerTarget by remember { mutableStateOf<CustomerWithBalance?>(null) }
    var promiseDateCustomerTarget by remember { mutableStateOf<CustomerEntity?>(null) }
    var settlementCustomerTarget by remember { mutableStateOf<CustomerWithBalance?>(null) }
    var editCustomerTarget by remember { mutableStateOf<CustomerEntity?>(null) }

    // If a customer is opened, show their detailed Customer Ledger screen
    if (selectedCustomerForLedger != null) {
        val currentCust = customersWithBalance.find { it.customer.id == selectedCustomerForLedger?.customer?.id }
            ?: selectedCustomerForLedger!!
        val customerTransactions = allTransactions.filter { it.customerId == currentCust.customer.id }

        CustomerLedgerScreen(
            customerWithBalance = currentCust,
            transactions = customerTransactions,
            viewModel = viewModel,
            onBack = { selectedCustomerForLedger = null }
        )
        return
    }

    val now = System.currentTimeMillis()
    val startOfToday = remember(now) {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = now
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    // Calculations for Intelligence Dashboard
    val totalOutstanding = remember(customersWithBalance) {
        customersWithBalance.sumOf { it.currentBalance }
    }
    val totalDueCustomersCount = remember(customersWithBalance) {
        customersWithBalance.count { it.currentBalance > 0 }
    }

    val todayTransactions = remember(allTransactions, startOfToday) {
        allTransactions.filter { it.timestamp >= startOfToday }
    }
    val todayBakiGiven = remember(todayTransactions) {
        todayTransactions.filter { it.type == TransactionType.SALE_BAKI }.sumOf { it.amount }
    }
    val todayJomaCollected = remember(todayTransactions) {
        todayTransactions.filter { it.type == TransactionType.BAKI_COLLECTION }.sumOf { it.amount }
    }

    // Aging Buckets: 0-15 days, 16-30 days, 30+ days
    val fifteenDaysMillis = 15L * 24 * 60 * 60 * 1000
    val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000

    val due0to15 = remember(customersWithBalance, now) {
        customersWithBalance.filter { it.currentBalance > 0 }.filter { item ->
            val lastTs = item.lastTransaction?.timestamp ?: item.customer.createdAt
            (now - lastTs) < fifteenDaysMillis
        }.sumOf { it.currentBalance }
    }
    val due16to30 = remember(customersWithBalance, now) {
        customersWithBalance.filter { it.currentBalance > 0 }.filter { item ->
            val lastTs = item.lastTransaction?.timestamp ?: item.customer.createdAt
            val age = now - lastTs
            age in fifteenDaysMillis until thirtyDaysMillis
        }.sumOf { it.currentBalance }
    }
    val due30plus = remember(customersWithBalance, now) {
        customersWithBalance.filter { it.currentBalance > 0 }.filter { item ->
            val lastTs = item.lastTransaction?.timestamp ?: item.customer.createdAt
            (now - lastTs) >= thirtyDaysMillis
        }.sumOf { it.currentBalance }
    }

    // Overdue promise count & limit breaches
    val overdueCount = remember(customersWithBalance, now) {
        customersWithBalance.count { it.currentBalance > 0 && it.customer.promisedPaymentDate in 1 until now }
    }
    val overLimitCount = remember(customersWithBalance) {
        customersWithBalance.count { it.customer.creditLimit > 0 && it.currentBalance > it.customer.creditLimit }
    }

    // Filter & Sort Customers List
    val filteredList = remember(customersWithBalance, searchQuery, selectedFilter, selectedSort) {
        val list = customersWithBalance.filter { item ->
            val cust = item.customer
            val matchesSearch = searchQuery.isBlank() ||
                    cust.name.contains(searchQuery, ignoreCase = true) ||
                    cust.phone.contains(searchQuery) ||
                    cust.address.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                BakiFilter.ALL -> true
                BakiFilter.DUE_ONLY -> item.currentBalance > 0
                BakiFilter.VIP -> cust.categoryTag == "VIP"
                BakiFilter.REGULAR -> cust.categoryTag == "REGULAR"
                BakiFilter.TODAY -> item.hasTransactionToday
                BakiFilter.AGING_30_DAYS -> {
                    val lastTs = item.lastTransaction?.timestamp ?: cust.createdAt
                    item.currentBalance > 0 && (now - lastTs) >= thirtyDaysMillis
                }
                BakiFilter.OVERDUE_DATE -> item.currentBalance > 0 && cust.promisedPaymentDate in 1 until now
                BakiFilter.OVER_LIMIT -> cust.creditLimit > 0 && item.currentBalance > cust.creditLimit
                BakiFilter.SETTLED -> item.currentBalance <= 0.0
            }

            matchesSearch && matchesFilter
        }

        when (selectedSort) {
            BakiSort.HIGHEST_DUE -> list.sortedByDescending { it.currentBalance }
            BakiSort.LOWEST_DUE -> list.sortedBy { it.currentBalance }
            BakiSort.RECENT -> list.sortedByDescending { it.lastTransaction?.timestamp ?: it.customer.createdAt }
            BakiSort.NAME_AZ -> list.sortedBy { it.customer.name.lowercase() }
            BakiSort.OLDEST_DUE -> list.sortedBy { it.lastTransaction?.timestamp ?: it.customer.createdAt }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val totalPendingSupplier = remember(allTransactions) {
            allTransactions.filter {
                (it.type == TransactionType.EXPENSE_SHOP || it.type == TransactionType.PURCHASE_DIRECT || it.type == TransactionType.PURCHASE_FORDI) &&
                it.paymentStatus == "DUE_PAYMENT"
            }.sumOf { it.amount }
        }

        // Top Header
        MawaTopBar(
            title = "বাকির খাতা",
            subtitle = if (selectedBakiSubTab == BakiSubTab.CUSTOMER) {
                "মোট বকেয়া: ${BengaliUtils.formatTaka(totalOutstanding)} (${BengaliUtils.toBanglaDigits(totalDueCustomersCount.toLong())} জন)"
            } else {
                "মোট মহাজন দেনা: ${BengaliUtils.formatTaka(totalPendingSupplier)}"
            },
            onMenuClick = onOpenDrawer
        )

        // Sub-Tab Switcher: গ্রাহক বাকি (পাওনা) | সাপ্লায়ার বাকি (মহাজন দেনা)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BakiSubTab.values().forEach { tab ->
                    val isSelected = selectedBakiSubTab == tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedBakiSubTab = tab },
                        color = if (isSelected) {
                            if (tab == BakiSubTab.CUSTOMER) MawaPrimary else Color(0xFFD97706)
                        } else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (selectedBakiSubTab == BakiSubTab.SUPPLIER) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // SUPPLIER HERO BANNER
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 4.dp)
                            .testTag("supplier_hero_banner_card"),
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
                                painter = painterResource(id = R.drawable.img_retail_banner),
                                contentDescription = "মহাজন দেনা ব্যানার",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.35f),
                                                Color.Black.copy(alpha = 0.85f)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFD97706).copy(alpha = 0.95f)
                                ) {
                                    Text(
                                        text = "🤝 মহাজন দেনা ও সাপ্লায়ার খাতা",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontSize = 11.sp
                                    )
                                }

                                Column {
                                    Text(
                                        text = "ডিলার ও মহাজন চালান হিসাব",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "মোট দেনা: ${BengaliUtils.formatTaka(totalPendingSupplier)} · চালান ও পরিশোধ ট্র্যাকিং",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.92f),
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    SupplierBakiSection(
                        supplierInvoices = allTransactions.filter {
                            (it.type == TransactionType.EXPENSE_SHOP || it.type == TransactionType.PURCHASE_DIRECT || it.type == TransactionType.PURCHASE_FORDI)
                        },
                        supplierCompanies = allSupplierCompanies,
                        onAddSupplierDue = { compName, srPhone, amount, note, date ->
                            viewModel.recordDirectPurchase(
                                productName = compName,
                                productId = null,
                                quantity = 1.0,
                                unit = "চালান",
                                rate = amount,
                                total = amount,
                                note = note.ifBlank { "মহাজন বাকি চালান" },
                                paymentStatus = "DUE_PAYMENT",
                                promisedPaymentDate = date
                            )
                            if (srPhone.isNotBlank()) {
                                viewModel.saveSupplierCompany(
                                    SupplierCompanyEntity(
                                        companyName = compName,
                                        srPhone = srPhone
                                    )
                                )
                            }
                        },
                        onPaySupplierDue = { txId ->
                            viewModel.updateTransactionPaymentStatus(txId, "PAID")
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
            // 0. BAKI HERO BANNER
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 4.dp)
                        .testTag("baki_hero_banner_card"),
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
                            painter = painterResource(id = R.drawable.img_baki_banner),
                            contentDescription = "বাকির খাতা ব্যানার",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.30f),
                                            Color.Black.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF59E0B).copy(alpha = 0.95f)
                                ) {
                                    Text(
                                        text = "📒 ডিজিটাল বাকি খাতা",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontSize = 11.sp
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Black.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = "${BengaliUtils.toBanglaDigits(totalDueCustomersCount.toLong())} জন বাকিদার",
                                        color = Color(0xFFFDE68A),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "কাস্টমার হিসাব ও বাকি ব্যবস্থাপনা",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "মোট বকেয়া: ${BengaliUtils.formatTaka(totalOutstanding)} · সময়মতো তাগাদা বার্তা পাঠান",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.92f),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
            // 1. INTELLIGENCE DASHBOARD & SUMMARY CARDS
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Main Balance Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = FinancialNegative.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, FinancialNegative.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "দোকানের সর্বমোট বাকি",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = BengaliUtils.formatTaka(totalOutstanding),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = FinancialNegative
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = FinancialNegative.copy(alpha = 0.15f),
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "বাকিদার",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = FinancialNegative
                                            )
                                            Text(
                                                text = "${BengaliUtils.toBanglaDigits(totalDueCustomersCount.toLong())} জন",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = FinancialNegative
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = FinancialNegative.copy(alpha = 0.15f))
                                Spacer(modifier = Modifier.height(8.dp))

                                // Today's Activity Bar (Today Given vs Collected)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = FinancialNegative, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "আজকের বাকি: ${BengaliUtils.formatTaka(todayBakiGiven)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = FinancialPositive, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "আজকের আদায়: ${BengaliUtils.formatTaka(todayJomaCollected)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = FinancialPositive,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. AGING ANALYSIS MINI CARDS (০-১৫ দিন, ১৬-৩০ দিন, ৩০+ দিন)
                        Text(
                            text = "বাকি বয়স বিশ্লেষণ (Due Aging):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // 0-15 Days
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedFilter = BakiFilter.DUE_ONLY },
                                colors = CardDefaults.cardColors(containerColor = FinancialPositive.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, FinancialPositive.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("০–১৫ দিন (নতুন)", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = FinancialPositive)
                                    Text(BengaliUtils.formatTaka(due0to15), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = FinancialPositive)
                                }
                            }

                            // 16-30 Days
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedFilter = BakiFilter.DUE_ONLY },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF59E0B).copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("১৬–৩০ দিন", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Color(0xFFD97706))
                                    Text(BengaliUtils.formatTaka(due16to30), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                }
                            }

                            // 30+ Days
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedFilter = BakiFilter.AGING_30_DAYS },
                                colors = CardDefaults.cardColors(containerColor = FinancialNegative.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, FinancialNegative.copy(alpha = 0.25f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("৩০+ দিন (পুরানো)", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = FinancialNegative)
                                    Text(BengaliUtils.formatTaka(due30plus), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = FinancialNegative)
                                }
                            }
                        }

                        // Overdue / Credit Limit Breaches Notice
                        if (overdueCount > 0 || overLimitCount > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEF2F2))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = FinancialNegative, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${if (overdueCount > 0) "${BengaliUtils.toBanglaDigits(overdueCount.toLong())} জনের তারিখ পার " else ""}${if (overLimitCount > 0) "· ${BengaliUtils.toBanglaDigits(overLimitCount.toLong())} জনের সীমা অতিক্রম" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = FinancialNegative,
                                        fontSize = 11.sp
                                    )
                                }

                                Text(
                                    text = "দেখুন",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MawaPrimary,
                                    modifier = Modifier.clickable {
                                        selectedFilter = if (overdueCount > 0) BakiFilter.OVERDUE_DATE else BakiFilter.OVER_LIMIT
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3. MASTER ACTION BUTTONS (Clean, proportionate 2-row layout with fixed heights)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Primary Action Row: + নতুন কাস্টমার & তাগাদা বার্তা হাব
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Add New Customer
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { showAddCustomerDialog = true }
                                        .testTag("baki_add_customer_btn"),
                                    color = MawaPrimary,
                                    shape = RoundedCornerShape(10.dp),
                                    shadowElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("+ নতুন কাস্টমার", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp, maxLines = 1)
                                    }
                                }

                                // Bulk Reminder Hub
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { showBulkReminderDialog = true }
                                        .testTag("btn_bulk_reminder_hub"),
                                    color = FinancialPositive.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, FinancialPositive.copy(alpha = 0.35f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = FinancialPositive, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("তাগাদা বার্তা হাব", fontWeight = FontWeight.Bold, color = FinancialPositive, fontSize = 12.sp, maxLines = 1)
                                    }
                                }
                            }

                            // Utility Row: প্রিন্ট / PDF | বাকি রিপোর্ট | CSV ফাইল
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Print Master Baki PDF
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            val html = ReceiptPrintManager.generateAllCustomersBakiSummaryHtml(
                                                shopName = shopName,
                                                shopOwner = shopOwner,
                                                shopPhone = shopPhone,
                                                customers = customersWithBalance,
                                                totalOutstanding = totalOutstanding,
                                                dateGenerated = BengaliUtils.formatTransactionTime(System.currentTimeMillis())
                                            )
                                            ReceiptPrintManager.printHtmlDocument(context, html, "Baki_Khata_Register")
                                        }
                                        .testTag("btn_print_baki_master"),
                                    color = MawaPrimary.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, MawaPrimary.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = null, tint = MawaPrimary, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("প্রিন্ট / PDF", fontWeight = FontWeight.Bold, color = MawaPrimary, fontSize = 11.sp, maxLines = 1)
                                    }
                                }

                                // Full Statement Report Dialog
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showAllBakiReportDialog = true }
                                        .testTag("btn_baki_full_report"),
                                    color = FinancialNegative.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, FinancialNegative.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Assessment, contentDescription = null, tint = FinancialNegative, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("বাকি রিপোর্ট", fontWeight = FontWeight.Bold, color = FinancialNegative, fontSize = 11.sp, maxLines = 1)
                                    }
                                }

                                // Export CSV
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            val csv = ReportExportUtils.generateCustomerBakiSummaryCsv(customersWithBalance)
                                            ReportExportUtils.shareText(context, csv, "বাকির খাতা তালিকা - $shopName")
                                        }
                                        .testTag("btn_export_baki_csv"),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = "CSV", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("CSV ফাইল", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. VISUAL GRAPHICAL ANALYSIS CHARTS (AGING DONUT, CREDIT VS COLLECTION, RISK DISTRIBUTION)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BakiAgingDonutChart(
                        due0to15 = due0to15,
                        due16to30 = due16to30,
                        due30plus = due30plus,
                        totalDue = totalOutstanding
                    )

                    BakiCreditVsCollectionChart(
                        todayBakiGiven = todayBakiGiven,
                        todayJomaCollected = todayJomaCollected,
                        totalOutstanding = totalOutstanding
                    )

                    BakiRiskDistributionCard(
                        customers = customersWithBalance
                    )
                }
            }

            // 4. SEARCH BAR & SORTING / FILTERS ROW
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("নাম, ফোন বা ঠিকানা দিয়ে খুঁজুন...") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("baki_search_field"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MawaPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Sort dropdown trigger
                            Box {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { showSortMenu = true }
                                        .testTag("btn_baki_sort_trigger"),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Sort, contentDescription = "বাছাই", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(selectedSort.banglaLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }

                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    BakiSort.values().forEach { sort ->
                                        DropdownMenuItem(
                                            text = { Text(sort.banglaLabel, fontWeight = if (selectedSort == sort) FontWeight.Bold else FontWeight.Normal) },
                                            onClick = {
                                                selectedSort = sort
                                                showSortMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Scrollable Filter Chips
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(BakiFilter.values()) { filter ->
                                val isSelected = selectedFilter == filter
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedFilter = filter }
                                        .testTag("filter_baki_${filter.name.lowercase()}"),
                                    color = if (isSelected) MawaPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    border = BorderStroke(1.dp, if (isSelected) MawaPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = filter.banglaLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Customer List Header Count
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "কাস্টমার তালিকা (${BengaliUtils.toBanglaDigits(filteredList.size.toLong())} জন)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "বর্তমান বাকি",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 5. CUSTOMER LIST ITEMS
            if (filteredList.isEmpty()) {
                item {
                    MawaEmptyState(
                        icon = Icons.Default.People,
                        title = "কোনো কাস্টমার পাওয়া যায়নি",
                        subtitle = "নতুন কাস্টমার যোগ করতে নিচের বাটনে চাপুন",
                        actionLabel = "নতুন কাস্টমার যোগ করুন",
                        onActionClick = { showAddCustomerDialog = true }
                    )
                }
            } else {
                items(filteredList, key = { it.customer.id }) { item ->
                    val cust = item.customer
                    val isOverLimit = cust.creditLimit > 0 && item.currentBalance > cust.creditLimit
                    val isOverdueDate = cust.promisedPaymentDate in 1 until now && item.currentBalance > 0
                    val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000
                    val lastTxTs = item.lastTransaction?.timestamp ?: cust.createdAt
                    val isAging30Days = (now - lastTxTs) >= thirtyDaysMillis && item.currentBalance > 0
                    val isNewCustomer = (now - cust.createdAt) < (7L * 24 * 60 * 60 * 1000)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                            .testTag("customer_card_${cust.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            width = 1.dp,
                            color = when {
                                isOverdueDate -> FinancialNegative.copy(alpha = 0.4f)
                                isOverLimit -> Color(0xFFF97316).copy(alpha = 0.4f)
                                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Top Row: Avatar, Info, and Balances
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCustomerForLedger = item },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                // Left: Avatar + Details
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (item.currentBalance > 0) FinancialNegative.copy(alpha = 0.12f)
                                                else FinancialPositive.copy(alpha = 0.12f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cust.name.take(1).uppercase(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.currentBalance > 0) FinancialNegative else FinancialPositive
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        // Customer Name
                                        Text(
                                            text = cust.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(3.dp))

                                        // Badges Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (cust.categoryTag == "VIP") {
                                                CustomerBadge(
                                                    label = "VIP",
                                                    icon = Icons.Default.Star,
                                                    containerColor = Color(0xFFFEF3C7),
                                                    contentColor = Color(0xFFB45309)
                                                )
                                            }
                                            if (cust.categoryTag == "REGULAR") {
                                                CustomerBadge(
                                                    label = "নিয়মিত",
                                                    icon = Icons.Default.CheckCircle,
                                                    containerColor = Color(0xFFECFDF5),
                                                    contentColor = Color(0xFF047857)
                                                )
                                            }
                                            if (cust.categoryTag == "WHOLESALE") {
                                                CustomerBadge(
                                                    label = "পাইকারি",
                                                    containerColor = Color(0xFFEEF2FF),
                                                    contentColor = Color(0xFF4338CA)
                                                )
                                            }
                                            if (isOverdueDate) {
                                                CustomerBadge(
                                                    label = "ওয়াদা পার",
                                                    icon = Icons.Default.Schedule,
                                                    containerColor = Color(0xFFFEE2E2),
                                                    contentColor = Color(0xFF991B1B)
                                                )
                                            }
                                            if (isOverLimit) {
                                                CustomerBadge(
                                                    label = "সীমা পার",
                                                    icon = Icons.Default.Warning,
                                                    containerColor = Color(0xFFFFEDD5),
                                                    contentColor = Color(0xFFC2410C)
                                                )
                                            }
                                            if (!isOverdueDate && item.currentBalance > 0 && isAging30Days) {
                                                CustomerBadge(
                                                    label = "৩০+ দিন",
                                                    icon = Icons.Default.Warning,
                                                    containerColor = Color(0xFFFFF1F2),
                                                    contentColor = Color(0xFFE11D48)
                                                )
                                            }
                                            if (item.currentBalance <= 0) {
                                                CustomerBadge(
                                                    label = "পরিশোধিত",
                                                    icon = Icons.Default.CheckCircle,
                                                    containerColor = Color(0xFFF0FDF4),
                                                    contentColor = Color(0xFF15803D)
                                                )
                                            }
                                            if (isNewCustomer && cust.categoryTag != "VIP") {
                                                CustomerBadge(
                                                    label = "নতুন",
                                                    containerColor = Color(0xFFE0F2FE),
                                                    contentColor = Color(0xFF0369A1)
                                                )
                                            }
                                        }

                                        // Phone & Address
                                        if (cust.phone.isNotBlank() || cust.address.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${if (cust.phone.isNotBlank()) cust.phone else ""}${if (cust.phone.isNotBlank() && cust.address.isNotBlank()) " • " else ""}${cust.address}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }

                                        // Promised Date
                                        if (cust.promisedPaymentDate > 0 && item.currentBalance > 0) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (isOverdueDate) "⚠️ দেওয়ার তারিখ পার: ${BengaliUtils.formatTransactionDateOnly(cust.promisedPaymentDate)}"
                                                else "🗓️ দেওয়ার তারিখ: ${BengaliUtils.formatTransactionDateOnly(cust.promisedPaymentDate)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isOverdueDate) FinancialNegative else MawaPrimary
                                            )
                                        }

                                        // Credit Limit Progress
                                        if (cust.creditLimit > 0) {
                                            Spacer(modifier = Modifier.height(3.dp))
                                            val progress = (item.currentBalance / cust.creditLimit).toFloat().coerceIn(0f, 1f)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                LinearProgressIndicator(
                                                    progress = { progress },
                                                    modifier = Modifier
                                                        .width(64.dp)
                                                        .height(4.dp)
                                                        .clip(RoundedCornerShape(2.dp)),
                                                    color = if (isOverLimit) FinancialNegative else MawaPrimary,
                                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    text = if (isOverLimit) "সীমা পার!" else "সীমা: ${BengaliUtils.formatTaka(cust.creditLimit)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 9.sp,
                                                    color = if (isOverLimit) FinancialNegative else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                // Right: Balance & Time
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = BengaliUtils.formatTaka(item.currentBalance),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.currentBalance > 0) FinancialNegative else FinancialPositive
                                    )
                                    if (item.lastTransaction != null) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = BengaliUtils.formatTransactionTime(item.lastTransaction.timestamp),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                            Spacer(modifier = Modifier.height(6.dp))

                            // Bottom Quick Action Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Call
                                    if (cust.phone.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier
                                                .height(30.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cust.phone.trim()}"))
                                                        context.startActivity(intent)
                                                    } catch (_: Exception) {}
                                                }
                                                .testTag("btn_call_${cust.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = "কল", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("কল", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                            }
                                        }
                                    }

                                    // Reminder (if has due)
                                    if (item.currentBalance > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = FinancialPositive.copy(alpha = 0.12f),
                                            modifier = Modifier
                                                .height(30.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { reminderCustomerTarget = item }
                                                .testTag("btn_quick_reminder_${cust.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Send, contentDescription = "তাগাদা", tint = FinancialPositive, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("তাগাদা", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FinancialPositive, maxLines = 1)
                                            }
                                        }
                                    }

                                    // Customer Report
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MawaPrimary.copy(alpha = 0.12f),
                                        modifier = Modifier
                                            .height(30.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { reportCustomerTarget = item }
                                            .testTag("btn_report_${cust.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Assessment, contentDescription = "রিপোর্ট", tint = MawaPrimary, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("রিপোর্ট", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MawaPrimary, maxLines = 1)
                                        }
                                    }

                                    // Ledger
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .height(30.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { selectedCustomerForLedger = item }
                                            .testTag("btn_ledger_${cust.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Receipt, contentDescription = "খতিয়ান", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("খতিয়ান", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                        }
                                    }
                                }

                                // More Options Menu (3 dots)
                                Box {
                                    var menuExpanded by remember { mutableStateOf(false) }
                                    IconButton(
                                        onClick = { menuExpanded = true },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "আরও অপশন", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp))
                                    }

                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("বাকি দিন (+)") },
                                            leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = FinancialNegative) },
                                            onClick = {
                                                menuExpanded = false
                                                quickBakiCustomerTarget = item
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("জমা নিন (-)") },
                                            leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = FinancialPositive) },
                                            onClick = {
                                                menuExpanded = false
                                                quickJomaCustomerTarget = item
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("ওয়াদা তারিখ দিন") },
                                            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, tint = MawaPrimary) },
                                            onClick = {
                                                menuExpanded = false
                                                promiseDateCustomerTarget = cust
                                            }
                                        )
                                        if (item.currentBalance > 0) {
                                            DropdownMenuItem(
                                                text = { Text("হিসাব রফা/ছাড়") },
                                                leadingIcon = { Icon(Icons.Default.Handshake, contentDescription = null, tint = Color(0xFFD97706)) },
                                                onClick = {
                                                    menuExpanded = false
                                                    settlementCustomerTarget = item
                                                }
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = { Text("মেমো ছবি শেয়ার") },
                                            leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
                                            onClick = {
                                                menuExpanded = false
                                                val bitmap = DataBackupRestoreManager.createCustomerBakiMemoBitmap(
                                                    shopName = shopName,
                                                    shopPhone = shopPhone,
                                                    customerWithBalance = item,
                                                    recentTransactions = allTransactions.filter { it.customerId == cust.id }
                                                )
                                                DataBackupRestoreManager.shareBitmapAsImage(
                                                    context = context,
                                                    bitmap = bitmap,
                                                    fileNamePrefix = "memo_${cust.name.replace(" ", "_")}",
                                                    title = "${cust.name} - বাকি মেমো"
                                                )
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("তথ্য সম্পাদনা") },
                                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                            onClick = {
                                                menuExpanded = false
                                                editCustomerTarget = cust
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("কাস্টমার ডিলিট", color = FinancialNegative) },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = FinancialNegative) },
                                            onClick = {
                                                menuExpanded = false
                                                viewModel.deleteCustomer(cust.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

    // Customer Statement Report Dialog
    if (reportCustomerTarget != null) {
        val custTransactions = allTransactions.filter { it.customerId == reportCustomerTarget!!.customer.id }
        CustomerStatementReportDialog(
            customerWithBalance = reportCustomerTarget!!,
            transactions = custTransactions,
            shopName = shopName,
            shopOwner = shopOwner,
            shopPhone = shopPhone,
            onDismiss = { reportCustomerTarget = null }
        )
    }

    // Quick Baki Entry Sheet from Main List
    if (quickBakiCustomerTarget != null) {
        val cust = quickBakiCustomerTarget!!.customer
        QuickLedgerEntrySheet(
            title = "${cust.name}-কে বাকি দিন",
            isBaki = true,
            onDismiss = { quickBakiCustomerTarget = null },
            onSave = { amount, note, promiseDate ->
                viewModel.recordBaki(
                    customerId = cust.id,
                    customerName = cust.name,
                    amount = amount,
                    note = note
                )
                if (promiseDate > 0) {
                    viewModel.updateCustomerPromiseDate(cust.id, promiseDate)
                }
                quickBakiCustomerTarget = null
            }
        )
    }

    // Quick Joma Entry Sheet from Main List
    if (quickJomaCustomerTarget != null) {
        val cust = quickJomaCustomerTarget!!.customer
        QuickLedgerEntrySheet(
            title = "${cust.name}-এর জমা নিন",
            isBaki = false,
            onDismiss = { quickJomaCustomerTarget = null },
            onSave = { amount, note, _ ->
                viewModel.recordJoma(
                    customerId = cust.id,
                    customerName = cust.name,
                    amount = amount,
                    note = note
                )
                quickJomaCustomerTarget = null
            }
        )
    }

    // Set Promise Date Dialog from Main List
    if (promiseDateCustomerTarget != null) {
        val cust = promiseDateCustomerTarget!!
        SetPromiseDateDialog(
            customer = cust,
            onDismiss = { promiseDateCustomerTarget = null },
            onDateSet = { timestamp ->
                viewModel.updateCustomerPromiseDate(cust.id, timestamp)
                promiseDateCustomerTarget = null
            }
        )
    }

    // Settlement Discount Dialog from Main List
    if (settlementCustomerTarget != null) {
        val target = settlementCustomerTarget!!
        val cust = target.customer
        SettlementDiscountDialog(
            customerWithBalance = target,
            onDismiss = { settlementCustomerTarget = null },
            onSettle = { cashPaid, discount, note ->
                viewModel.settleCustomerAccountWithDiscount(
                    customerId = cust.id,
                    customerName = cust.name,
                    cashPaid = cashPaid,
                    discountGiven = discount,
                    note = note
                )
                settlementCustomerTarget = null
            }
        )
    }

    // Edit Customer Dialog from Main List
    if (editCustomerTarget != null) {
        val cust = editCustomerTarget!!
        EditCustomerDialog(
            customer = cust,
            onDismiss = { editCustomerTarget = null },
            onSave = { updated ->
                viewModel.updateCustomer(updated)
                editCustomerTarget = null
            },
            onDelete = {
                viewModel.deleteCustomer(cust.id)
                editCustomerTarget = null
            }
        )
    }

    // Individual Quick Reminder Dialog
    if (reminderCustomerTarget != null) {
        BakiReminderDialog(
            customerWithBalance = reminderCustomerTarget!!,
            shopName = shopName,
            shopPhone = shopPhone,
            bkashNumber = shopSettings?.bkashNumber ?: "",
            nagadNumber = shopSettings?.nagadNumber ?: "",
            bankDetails = shopSettings?.bankDetails ?: "",
            onDismiss = { reminderCustomerTarget = null }
        )
    }

    // Bulk Reminder Hub Dialog
    if (showBulkReminderDialog) {
        BulkReminderDialog(
            customersWithBalance = customersWithBalance,
            shopName = shopName,
            shopPhone = shopPhone,
            bkashNumber = shopSettings?.bkashNumber ?: "",
            nagadNumber = shopSettings?.nagadNumber ?: "",
            bankDetails = shopSettings?.bankDetails ?: "",
            onDismiss = { showBulkReminderDialog = false }
        )
    }

    // Add Customer Dialog
    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onAdd = { name, phone, address, openingBalance, creditLimit, promiseDate, category, nidOrGuarantor ->
                viewModel.addCustomer(
                    name = name,
                    phone = phone,
                    address = address,
                    openingBalance = openingBalance,
                    creditLimit = creditLimit,
                    promisedPaymentDate = promiseDate,
                    categoryTag = category,
                    nidOrGuarantor = nidOrGuarantor
                )
                showAddCustomerDialog = false
            }
        )
    }

    // All Baki Summary Report Dialog
    if (showAllBakiReportDialog) {
        AllBakiSummaryReportDialog(
            shopName = shopName,
            totalOutstanding = totalOutstanding,
            totalCustomersCount = totalDueCustomersCount,
            due0to15 = due0to15,
            due16to30 = due16to30,
            due30plus = due30plus,
            customersList = customersWithBalance,
            onDismiss = { showAllBakiReportDialog = false }
        )
    }
}

/**
 * Detailed Individual Customer Ledger Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLedgerScreen(
    customerWithBalance: CustomerWithBalance,
    transactions: List<TransactionEntity>,
    viewModel: MawaViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val customer = customerWithBalance.customer
    val shopSettings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val shopName = shopSettings?.shopName?.ifBlank { "মাওয়া স্মার্ট খাতা" } ?: "মাওয়া স্মার্ট খাতা"
    val shopOwner = shopSettings?.ownerName ?: "প্রোপাইটার"
    val shopPhone = shopSettings?.phone ?: ""

    var showBakiSheet by remember { mutableStateOf(false) }
    var showJomaSheet by remember { mutableStateOf(false) }
    var showEditCustomerDialog by remember { mutableStateOf(false) }
    var showDigitalReceiptDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showSettlementDialog by remember { mutableStateOf(false) }
    var showPromiseDateDialog by remember { mutableStateOf(false) }
    var showCustomerReportDialog by remember { mutableStateOf(false) }
    var showPaymentQrDialog by remember { mutableStateOf(false) }
    var selectedTxForReceipt by remember { mutableStateOf<TransactionEntity?>(null) }

    var ledgerTxFilter by remember { mutableStateOf("ALL") } // "ALL", "BAKI", "JOMA"
    var ledgerSearchQuery by remember { mutableStateOf("") }

    val filteredTransactions = remember(transactions, ledgerTxFilter, ledgerSearchQuery) {
        transactions.filter { tx ->
            val matchesType = when (ledgerTxFilter) {
                "BAKI" -> tx.type == TransactionType.SALE_BAKI
                "JOMA" -> tx.type == TransactionType.BAKI_COLLECTION
                else -> true
            }
            val matchesSearch = ledgerSearchQuery.isBlank() || tx.note.contains(ledgerSearchQuery, ignoreCase = true)
            matchesType && matchesSearch
        }.sortedByDescending { it.timestamp }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        MawaTopBar(
            title = customer.name,
            subtitle = if (customer.phone.isNotBlank()) customer.phone else "ব্যক্তিগত বাকি খতিয়ান",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "পেছনে")
                }
            }
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Customer Banner Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Top profile row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "বর্তমান বাকি",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (customer.categoryTag == "VIP") {
                                        CustomerBadge(
                                            label = "VIP",
                                            icon = Icons.Default.Star,
                                            containerColor = Color(0xFFFEF3C7),
                                            contentColor = Color(0xFFB45309)
                                        )
                                    }
                                    if (customer.categoryTag == "REGULAR") {
                                        CustomerBadge(
                                            label = "নিয়মিত",
                                            icon = Icons.Default.CheckCircle,
                                            containerColor = Color(0xFFECFDF5),
                                            contentColor = Color(0xFF047857)
                                        )
                                    }
                                    if (customer.categoryTag == "WHOLESALE") {
                                        CustomerBadge(
                                            label = "পাইকারি",
                                            containerColor = Color(0xFFEEF2FF),
                                            contentColor = Color(0xFF4338CA)
                                        )
                                    }
                                    if (customerWithBalance.currentBalance <= 0) {
                                        CustomerBadge(
                                            label = "পরিশোধিত",
                                            icon = Icons.Default.CheckCircle,
                                            containerColor = Color(0xFFF0FDF4),
                                            contentColor = Color(0xFF15803D)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = BengaliUtils.formatTaka(customerWithBalance.currentBalance),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (customerWithBalance.currentBalance > 0) FinancialNegative else FinancialPositive
                                )
                            }

                            Row {
                                IconButton(onClick = { showEditCustomerDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "সম্পাদনা",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Meta details (Phone, Address, Limit, Promise Date)
                        if (customer.address.isNotBlank() || customer.creditLimit > 0 || customer.promisedPaymentDate > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (customer.address.isNotBlank()) {
                                    Text(
                                        text = "ঠিকানা: ${customer.address}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (customer.creditLimit > 0) {
                                    Text(
                                        text = "বাকির সীমা: ${BengaliUtils.formatTaka(customer.creditLimit)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (customerWithBalance.currentBalance > customer.creditLimit) FinancialNegative else MawaPrimary
                                    )
                                }
                            }
                        }

                        // Promise Date Chip
                        if (customer.promisedPaymentDate > 0 && customerWithBalance.currentBalance > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { showPromiseDateDialog = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Event, contentDescription = null, tint = MawaPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "টাকা দেওয়ার প্রতিশ্রুত তারিখ: ${BengaliUtils.formatTransactionDateOnly(customer.promisedPaymentDate)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MawaPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // SUMMARY GRID: সাবেক | মোট বাকি প্রদান | মোট জমা আদায়
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Opening
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("সাবেক বাকি", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(BengaliUtils.formatTaka(customer.openingBalance), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Total Given
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = FinancialNegative.copy(alpha = 0.08f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("মোট বাকি দেওয়া", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = FinancialNegative)
                                    Text(BengaliUtils.formatTaka(customerWithBalance.totalBakiGiven), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = FinancialNegative)
                                }
                            }

                            // Total Collected
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = FinancialPositive.copy(alpha = 0.08f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("মোট জমা আদায়", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = FinancialPositive)
                                    Text(BengaliUtils.formatTaka(customerWithBalance.totalJomaReceived), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = FinancialPositive)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // PRIMARY ACTIONS: [ বাকি দিন ] [ জমা নিন ]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showBakiSheet = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("ledger_baki_din_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = FinancialNegative),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "বাকি দিন", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { showJomaSheet = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("ledger_joma_nin_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "জমা নিন", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // SECONDARY ACTIONS: [ রিপোর্ট ] [ মেমো ] [ তাগাদা ] [ ছবি ] [ খতিয়ান PDF ] [ নিষ্পত্তি ও ছাড় ] [ কল ]
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Full Customer Statement Report & Health Score
                            item {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showCustomerReportDialog = true }
                                        .testTag("btn_customer_report_ledger"),
                                    color = MawaPrimary.copy(alpha = 0.14f),
                                    border = BorderStroke(1.dp, MawaPrimary.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Assessment, contentDescription = null, tint = MawaPrimary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "কাস্টমার রিপোর্ট", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MawaPrimary, fontSize = 11.sp)
                                    }
                                }
                            }

                            // Digital Memo
                            item {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showDigitalReceiptDialog = true }
                                        .testTag("btn_customer_digital_memo"),
                                    color = MawaPrimary.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, MawaPrimary.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = MawaPrimary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "ডিজিটাল মেমো", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MawaPrimary, fontSize = 11.sp)
                                    }
                                }
                            }

                            // Reminder Message
                            item {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showReminderDialog = true }
                                        .testTag("btn_customer_reminder"),
                                    color = FinancialPositive.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, FinancialPositive.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = FinancialPositive, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "তাগাদা বার্তা", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = FinancialPositive, fontSize = 11.sp)
                                    }
                                }
                            }

                            // Digital Payment QR Code Scan-to-Pay for Customer Due
                            if (customerWithBalance.currentBalance > 0) {
                                item {
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showPaymentQrDialog = true }
                                            .testTag("btn_customer_payment_qr"),
                                        color = Color(0xFFE2136E).copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, Color(0xFFE2136E).copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 7.dp, horizontal = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.QrCode2, contentDescription = null, tint = Color(0xFFE2136E), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "পেমেন্ট QR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFE2136E), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            // Full Settlement & Discount (হিসাব নিষ্পত্তি ও ছাড়)
                            if (customerWithBalance.currentBalance > 0) {
                                item {
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showSettlementDialog = true }
                                            .testTag("btn_customer_settlement"),
                                        color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 7.dp, horizontal = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.Handshake, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "হিসাব রফা/ছাড়", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFD97706), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            // Full Ledger Statement PDF Print
                            item {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            val html = ReceiptPrintManager.generateCustomerFullLedgerHtml(
                                                shopName = shopName,
                                                shopOwner = shopOwner,
                                                shopPhone = shopPhone,
                                                customerWithBalance = customerWithBalance,
                                                transactions = transactions,
                                                dateGenerated = BengaliUtils.formatTransactionTime(System.currentTimeMillis())
                                            )
                                            ReceiptPrintManager.printHtmlDocument(context, html, "Ledger_${customer.name}")
                                        }
                                        .testTag("btn_customer_ledger_pdf"),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Print, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "খতিয়ান PDF", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                    }
                                }
                            }

                            // Promise Date button
                            item {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showPromiseDateDialog = true }
                                        .testTag("btn_customer_set_promise_date"),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "টাকা দেওয়ার তারিখ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                    }
                                }
                            }

                            // Share PNG Memo Picture
                            item {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            val bitmap = DataBackupRestoreManager.createCustomerBakiMemoBitmap(
                                                shopName = shopName,
                                                shopPhone = shopPhone,
                                                customerWithBalance = customerWithBalance,
                                                recentTransactions = transactions
                                            )
                                            DataBackupRestoreManager.shareBitmapAsImage(
                                                context = context,
                                                bitmap = bitmap,
                                                fileNamePrefix = "memo_${customer.name.replace(" ", "_")}",
                                                title = "${customer.name} - বাকি মেমো"
                                            )
                                        }
                                        .testTag("btn_customer_memo_png"),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "ছবি", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                    }
                                }
                            }

                            // Call
                            if (customer.phone.isNotBlank()) {
                                item {
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                try {
                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone.trim()}"))
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {}
                                            }
                                            .testTag("btn_customer_call"),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                            Icon(imageVector = Icons.Default.Call, contentDescription = "কল", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Filter & Search Row for Ledger Transactions
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("ALL" to "সব", "BAKI" to "শুধু বাকি", "JOMA" to "শুধু জমা").forEach { (key, label) ->
                                val isSelected = ledgerTxFilter == key
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { ledgerTxFilter = key },
                                    color = if (isSelected) MawaPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${BengaliUtils.toBanglaDigits(filteredTransactions.size.toLong())} টি এন্ট্রি",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Timeline Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "লেনদেনের খতিয়ান বিবরণী",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (filteredTransactions.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        MawaEmptyState(
                            icon = Icons.Default.Receipt,
                            title = "এখনও কোনো লেনদেন নেই",
                            subtitle = "উপরে 'বাকি দিন' বা 'জমা নিন' বোতাম চাপুন",
                            actionLabel = "বাকি দিন",
                            onActionClick = { showBakiSheet = true }
                        )
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTxForReceipt = tx },
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column {
                            TransactionItemRow(
                                transaction = tx,
                                onDelete = { viewModel.deleteTransaction(tx.id) }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Modal Sheet for Quick Baki Entry
    if (showBakiSheet) {
        QuickLedgerEntrySheet(
            title = "${customer.name}-কে বাকি দিন",
            isBaki = true,
            onDismiss = { showBakiSheet = false },
            onSave = { amount, note, promiseDate ->
                viewModel.recordBaki(
                    customerId = customer.id,
                    customerName = customer.name,
                    amount = amount,
                    note = note
                )
                if (promiseDate > 0) {
                    viewModel.updateCustomerPromiseDate(customer.id, promiseDate)
                }
                showBakiSheet = false
            }
        )
    }

    // Modal Sheet for Quick Joma Entry
    if (showJomaSheet) {
        QuickLedgerEntrySheet(
            title = "${customer.name}-এর জমা নিন",
            isBaki = false,
            onDismiss = { showJomaSheet = false },
            onSave = { amount, note, _ ->
                viewModel.recordJoma(
                    customerId = customer.id,
                    customerName = customer.name,
                    amount = amount,
                    note = note
                )
                showJomaSheet = false
            }
        )
    }

    // Full Settlement & Discount Dialog
    if (showSettlementDialog) {
        SettlementDiscountDialog(
            customerWithBalance = customerWithBalance,
            onDismiss = { showSettlementDialog = false },
            onSettle = { cashPaid, discount, note ->
                viewModel.settleCustomerAccountWithDiscount(
                    customerId = customer.id,
                    customerName = customer.name,
                    cashPaid = cashPaid,
                    discountGiven = discount,
                    note = note
                )
                showSettlementDialog = false
            }
        )
    }

    // Set Promise Date Dialog
    if (showPromiseDateDialog) {
        SetPromiseDateDialog(
            customer = customer,
            onDismiss = { showPromiseDateDialog = false },
            onDateSet = { timestamp ->
                viewModel.updateCustomerPromiseDate(customer.id, timestamp)
                showPromiseDateDialog = false
            }
        )
    }

    // Edit Customer Dialog
    if (showEditCustomerDialog) {
        EditCustomerDialog(
            customer = customer,
            onDismiss = { showEditCustomerDialog = false },
            onSave = { updated ->
                viewModel.updateCustomer(updated)
                showEditCustomerDialog = false
            },
            onDelete = {
                viewModel.deleteCustomer(customer.id)
                showEditCustomerDialog = false
                onBack()
            }
        )
    }

    // Digital Receipt Dialog
    if (showDigitalReceiptDialog) {
        val invoiceItems = transactions.take(5).map { tx ->
            InvoiceItem(
                name = tx.note.ifBlank { if (tx.type == TransactionType.SALE_BAKI) "বাকি প্রদান" else "জমা গ্রহণ" },
                quantity = 1.0,
                unit = "বার",
                rate = tx.amount,
                amount = tx.amount
            )
        }
        DigitalReceiptDialog(
            shopName = shopName,
            shopPhone = shopPhone,
            customerName = customer.name,
            customerPhone = customer.phone,
            items = invoiceItems,
            subtotal = customerWithBalance.currentBalance,
            discount = 0.0,
            paidAmount = customerWithBalance.totalJomaReceived,
            previousDue = customer.openingBalance,
            currentDue = customerWithBalance.currentBalance,
            note = "বাকি খাতা বিবরণী",
            onDismiss = { showDigitalReceiptDialog = false }
        )
    }

    // Reminder Dialog
    if (showReminderDialog) {
        BakiReminderDialog(
            customerWithBalance = customerWithBalance,
            shopName = shopName,
            shopPhone = shopPhone,
            bkashNumber = shopSettings?.bkashNumber ?: "",
            nagadNumber = shopSettings?.nagadNumber ?: "",
            bankDetails = shopSettings?.bankDetails ?: "",
            onDismiss = { showReminderDialog = false }
        )
    }

    // Individual Transaction Receipt
    if (selectedTxForReceipt != null) {
        val tx = selectedTxForReceipt!!
        val isBaki = tx.type == TransactionType.SALE_BAKI
        DigitalReceiptDialog(
            shopName = shopName,
            shopPhone = shopPhone,
            customerName = customer.name,
            customerPhone = customer.phone,
            items = listOf(
                InvoiceItem(
                    name = tx.note.ifBlank { if (isBaki) "বাকি হিসাব" else "জমা পরিশোধ" },
                    quantity = 1.0,
                    unit = "টি",
                    rate = tx.amount,
                    amount = tx.amount
                )
            ),
            subtotal = tx.amount,
            discount = 0.0,
            paidAmount = if (isBaki) 0.0 else tx.amount,
            previousDue = 0.0,
            currentDue = if (isBaki) tx.amount else 0.0,
            note = if (isBaki) "বাকি প্রদান রসিদ" else "জমা গ্রহণ রসিদ",
            onDismiss = { selectedTxForReceipt = null }
        )
    }

    // Full Customer Statement Report & Health Score Dialog
    if (showCustomerReportDialog) {
        CustomerStatementReportDialog(
            customerWithBalance = customerWithBalance,
            transactions = transactions,
            shopName = shopName,
            shopOwner = shopOwner,
            shopPhone = shopPhone,
            onDismiss = { showCustomerReportDialog = false }
        )
    }

    // Payment QR Code for Customer Due Collection
    if (showPaymentQrDialog) {
        PaymentQrDialog(
            settings = shopSettings,
            initialAmount = customerWithBalance.currentBalance,
            customerName = customer.name,
            onDismiss = { showPaymentQrDialog = false },
            onEditShopProfile = {}
        )
    }
}

/**
 * Modal Bottom Sheet for Quick Baki / Joma Entry with optional Promised Payment Date
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLedgerEntrySheet(
    title: String,
    isBaki: Boolean,
    onDismiss: () -> Unit,
    onSave: (amount: Double, note: String, promiseDate: Long) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var promiseDateDays by remember { mutableStateOf(0) } // 0 = none, 3 = 3 days, 7 = 7 days, 15 = 15 days, 30 = 30 days

    val now = System.currentTimeMillis()
    val calculatedPromiseDate = if (promiseDateDays > 0) now + (promiseDateDays.toLong() * 24 * 60 * 60 * 1000) else 0L

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isBaki) FinancialNegative else FinancialPositive
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            MawaAmountInput(
                amount = amount,
                onAmountChange = { amount = it },
                label = if (isBaki) "বাকি টাকার পরিমাণ" else "জমা টাকার পরিমাণ",
                quickAmounts = listOf(100, 200, 500, 1000, 2000, 5000)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("বিবরণ / পণ্যের তালিকা (ঐচ্ছিক)") },
                placeholder = { Text(if (isBaki) "যেমন: ২ কেজি চাল, ১ লিটার তেল" else "যেমন: নগদ পরিশোধ / বিকাশ") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            if (isBaki) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "টাকা দেওয়ার প্রতিশ্রুত সময় নির্ধারণ করুন (ঐচ্ছিক):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0 to "প্রয়োজন নেই", 3 to "৩ দিন", 7 to "৭ দিন", 15 to "১৫ দিন", 30 to "১ মাস").forEach { (days, label) ->
                        val isSelected = promiseDateDays == days
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { promiseDateDays = days },
                            color = if (isSelected) MawaPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isSelected) MawaPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                fontSize = 10.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val amtNum = amount.toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    if (amtNum > 0) {
                        onSave(amtNum, note, calculatedPromiseDate)
                    }
                },
                enabled = amtNum > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_save_ledger_entry"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBaki) FinancialNegative else FinancialPositive
                )
            ) {
                Text(
                    text = if (isBaki) "বাকি নিশ্চিত করুন" else "জমা নিশ্চিত করুন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String, address: String, openingBalance: Double) -> Unit
) {
    AddCustomerDialog(
        onDismiss = onDismiss,
        onAdd = { name, phone, address, openingBalance, _, _, _, _ ->
            onAdd(name, phone, address, openingBalance)
        }
    )
}

/**
 * Enhanced Add Customer Dialog with Credit Limit, Promise Date, and Category
 */
@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String, address: String, openingBalance: Double, creditLimit: Double, promiseDate: Long, category: String, nidOrGuarantor: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var openingBalance by remember { mutableStateOf("") }
    var creditLimit by remember { mutableStateOf("") }
    var categoryTag by remember { mutableStateOf("REGULAR") }
    var nidOrGuarantor by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "নতুন কাস্টমার নিবন্ধন", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("কাস্টমার নাম *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_customer_name_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর") },
                    placeholder = { Text("017XXXXXXXX") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা / গ্রাম / এলাকা") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = openingBalance,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) openingBalance = it },
                        label = { Text("পূর্বের বাকি (সাবেক)") },
                        placeholder = { Text("0") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = creditLimit,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) creditLimit = it },
                        label = { Text("বাকির সর্বোচ্চ সীমা") },
                        placeholder = { Text("যেমন 5000") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "কাস্টমার ধরন:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("REGULAR" to "নিয়মিত", "VIP" to "VIP", "WHOLESALE" to "পাইকারি").forEach { (tag, label) ->
                        val isSelected = categoryTag == tag
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { categoryTag = tag },
                            color = if (isSelected) MawaPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nidOrGuarantor,
                    onValueChange = { nidOrGuarantor = it },
                    label = { Text("জাতীয় পরিচয়পত্র / জামিনদার (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val opening = openingBalance.toDoubleOrNull() ?: 0.0
                        val limit = creditLimit.toDoubleOrNull() ?: 0.0
                        onAdd(name, phone, address, opening, limit, 0L, categoryTag, nidOrGuarantor)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary),
                modifier = Modifier.testTag("dialog_save_customer_btn")
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

/**
 * Enhanced Edit Customer Dialog
 */
@Composable
fun EditCustomerDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onSave: (CustomerEntity) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(customer.name) }
    var phone by remember { mutableStateOf(customer.phone) }
    var address by remember { mutableStateOf(customer.address) }
    var openingBalance by remember { mutableStateOf(customer.openingBalance.toInt().toString()) }
    var creditLimit by remember { mutableStateOf(if (customer.creditLimit > 0) customer.creditLimit.toInt().toString() else "") }
    var categoryTag by remember { mutableStateOf(customer.categoryTag) }
    var nidOrGuarantor by remember { mutableStateOf(customer.nidOrGuarantor) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("কাস্টমার মুছবেন?") },
            text = { Text("${customer.name}-এর সকল তথ্য মুছে যাবে। আপনি কি নিশ্চিত?") },
            confirmButton = {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialNegative)
                ) {
                    Text("হ্যাঁ, মুছুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("না")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "কাস্টমার তথ্য সম্পাদনা", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("কাস্টমার নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = openingBalance,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) openingBalance = it },
                        label = { Text("সাবেক বাকি") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = creditLimit,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) creditLimit = it },
                        label = { Text("বাকির সীমা") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("কাস্টমার ধরন:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("REGULAR" to "নিয়মিত", "VIP" to "VIP", "WHOLESALE" to "পাইকারি").forEach { (tag, label) ->
                        val isSelected = categoryTag == tag
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { categoryTag = tag },
                            color = if (isSelected) MawaPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nidOrGuarantor,
                    onValueChange = { nidOrGuarantor = it },
                    label = { Text("NID / জামিনদার তথ্য") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FinancialNegative),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("কাস্টমার মুছে ফেলুন")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val opening = openingBalance.toDoubleOrNull() ?: 0.0
                        val limit = creditLimit.toDoubleOrNull() ?: 0.0
                        onSave(
                            customer.copy(
                                name = name.trim(),
                                phone = phone.trim(),
                                address = address.trim(),
                                openingBalance = opening,
                                creditLimit = limit,
                                categoryTag = categoryTag,
                                nidOrGuarantor = nidOrGuarantor.trim()
                            )
                        )
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
            ) {
                Text("আপডেট করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
