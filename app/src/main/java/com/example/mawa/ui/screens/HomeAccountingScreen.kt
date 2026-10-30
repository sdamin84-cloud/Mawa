package com.example.mawa.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.model.CategorySpending
import com.example.mawa.data.model.TimeFilter
import com.example.mawa.ui.components.BazaarChecklistSection
import com.example.mawa.ui.components.HouseholdBillsSection
import com.example.mawa.ui.components.HomeWithdrawalBottomSheet
import com.example.mawa.ui.components.InteractiveCategoryDonutChart
import com.example.mawa.ui.components.DailyExpenseTrendBarChart
import com.example.mawa.ui.components.MawaEmptyState
import com.example.mawa.ui.components.MawaTopBar
import com.example.mawa.ui.components.PersonalBudgetSettingDialog
import com.example.mawa.ui.components.PersonalExpenseReportDialog
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.ExpenseReportEntry
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.FinancialWarningContainer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAccountingScreen(
    viewModel: MawaViewModel,
    onOpenAddExpense: () -> Unit,
    onBack: (() -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentFilter by viewModel.homeExpenseTimeFilter.collectAsStateWithLifecycle()
    val homeExpenses by viewModel.filteredHomeExpenses.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    // SharedPreferences for Household Monthly Budget
    val budgetPrefs = remember { context.getSharedPreferences("mawa_home_budget_pref", Context.MODE_PRIVATE) }
    var homeBudget by remember {
        mutableStateOf(budgetPrefs.getFloat("home_monthly_budget", 0f).toDouble())
    }

    // Dialog states
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showGoodsWithdrawalSheet by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    // Search and category filter
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("সকল") }

    val totalHomeAmount = remember(homeExpenses) {
        homeExpenses.sumOf { it.amount }
    }

    // Category analysis from descriptions/notes
    val categorizedBreakdown = remember(homeExpenses) {
        val catMap = mutableMapOf<String, Double>()
        homeExpenses.forEach { tx ->
            val desc = (tx.note.ifBlank { tx.productName ?: "সংসার খরচ" }).lowercase()
            val cat = when {
                desc.contains("বাজার") || desc.contains("সবজি") || desc.contains("মাছ") || desc.contains("মাংস") || desc.contains("চাল") || desc.contains("ডাল") || desc.contains("তেল") -> "🥬 বাজারসদাই"
                desc.contains("বিল") || desc.contains("বিদ্যুৎ") || desc.contains("কারেন্ট") || desc.contains("গ্যাস") || desc.contains("পানি") -> "⚡ বিল ও পরিষেবা"
                desc.contains("ভাড়া") || desc.contains("বাড়ি ভাড়া") || desc.contains("বাসা") -> "🏠 বাসা ভাড়া"
                desc.contains("স্কুল") || desc.contains("বাচ্চা") || desc.contains("পড়া") || desc.contains("বেতন") || desc.contains("খাতা") -> "🎒 পড়াশোনা"
                desc.contains("ওষুধ") || desc.contains("ডাক্তার") || desc.contains("চিকিৎসা") || desc.contains("হাসপাতাল") -> "💊 চিকিৎসা"
                desc.contains("মোবাইল") || desc.contains("নেট") || desc.contains("ওয়াইফাই") || desc.contains("রিচার্জ") -> "📶 নেট ও রিচার্জ"
                desc.contains("পণ্য") || desc.contains("দোকান থেকে নেওয়া") || tx.productName != null -> "🏪 দোকানের পণ্য"
                else -> "📦 অন্যান্য সংসার খরচ"
            }
            catMap[cat] = (catMap[cat] ?: 0.0) + tx.amount
        }
        catMap.toList().sortedByDescending { it.second }
    }

    // Filtered transactions for list display
    val displayedExpenses = remember(homeExpenses, searchQuery, selectedCategoryFilter) {
        homeExpenses.filter { tx ->
            val fullText = "${tx.note} ${tx.productName ?: ""}"
            val matchesSearch = searchQuery.isBlank() ||
                fullText.contains(searchQuery, ignoreCase = true) ||
                tx.amount.toString().contains(searchQuery)

            val matchesCategory = if (selectedCategoryFilter == "সকল") true else {
                val desc = fullText.lowercase()
                when (selectedCategoryFilter) {
                    "বাজার" -> desc.contains("বাজার") || desc.contains("সবজি") || desc.contains("মাছ") || desc.contains("মাংস") || desc.contains("চাল") || desc.contains("ডাল") || desc.contains("তেল")
                    "বিল" -> desc.contains("বিল") || desc.contains("বিদ্যুৎ") || desc.contains("কারেন্ট") || desc.contains("গ্যাস") || desc.contains("পানি")
                    "ভাড়া" -> desc.contains("ভাড়া") || desc.contains("বাড়ি ভাড়া") || desc.contains("বাসা")
                    "পড়াশোনা" -> desc.contains("স্কুল") || desc.contains("বাচ্চা") || desc.contains("পড়া") || desc.contains("বেতন")
                    "চিকিৎসা" -> desc.contains("ওষুধ") || desc.contains("ডাক্তার") || desc.contains("চিকিৎসা")
                    "পণ্য" -> desc.contains("পণ্য") || desc.contains("দোকান থেকে নেওয়া") || tx.productName != null
                    else -> true
                }
            }
            matchesSearch && matchesCategory
        }
    }

    // Days calculation for budget burn-rate
    val calendar = Calendar.getInstance()
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    val remainingDays = (daysInMonth - currentDay + 1).coerceAtLeast(1)
    val dailyBurnAllowance = if (homeBudget > totalHomeAmount && remainingDays > 0) {
        (homeBudget - totalHomeAmount) / remainingDays
    } else 0.0

    val categorySpendings = remember(categorizedBreakdown, totalHomeAmount) {
        categorizedBreakdown.map { (cat, amt) ->
            CategorySpending(
                category = cat,
                totalAmount = amt,
                percentage = if (totalHomeAmount > 0) ((amt / totalHomeAmount) * 100).toFloat() else 0f,
                transactionCount = 1
            )
        }
    }

    val dailyExpenseTrend = remember(homeExpenses) {
        val sdfDay = SimpleDateFormat("dd MMM", Locale.getDefault())
        homeExpenses
            .groupBy { sdfDay.format(Date(it.timestamp)) }
            .map { (day, list) -> Pair(day, list.sumOf { it.amount }) }
            .takeLast(7)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Compact Header without redundant Bengali title/subtitle
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "পেছনে")
                        }
                    } else if (onOpenDrawer != null) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "মেনু")
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showBudgetDialog = true }) {
                        Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = "বাজেট", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showReportDialog = true }) {
                        Icon(imageVector = Icons.Default.Assessment, contentDescription = "রিপোর্ট", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_accounting_list")
        ) {
            // 1. Time Filter Tabs
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(TimeFilter.values()) { filter ->
                            val isSelected = currentFilter == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setHomeExpenseTimeFilter(filter) },
                                label = {
                                    Text(
                                        text = filter.banglaLabel,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FinancialWarning,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // 2. Hero Card with Total & Monthly Budget Health Bar
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
                                    text = "${currentFilter.banglaLabel} মোট সংসার খরচ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = BengaliUtils.formatTaka(totalHomeAmount),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = FinancialWarning
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(FinancialWarningContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = FinancialWarning,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Monthly Budget Bar (if set or prompt)
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))

                        if (homeBudget > 0) {
                            val ratio = (totalHomeAmount / homeBudget).toFloat().coerceIn(0f, 1.5f)
                            val percent = (totalHomeAmount / homeBudget * 100).toInt()
                            val isOver = totalHomeAmount > homeBudget
                            val remaining = homeBudget - totalHomeAmount

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "সংসারের মাসিক বাজেট",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${BengaliUtils.formatTaka(totalHomeAmount)} / ${BengaliUtils.formatTaka(homeBudget)} (${BengaliUtils.toBanglaDigits(percent.toLong())}%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOver) FinancialNegative else FinancialWarning
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "বদলান",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { showBudgetDialog = true }
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
                                    text = if (isOver) "⚠️ বাজেট অতিক্রম করেছে ${BengaliUtils.formatTaka(totalHomeAmount - homeBudget)}"
                                    else "✅ বাকি আছে: ${BengaliUtils.formatTaka(remaining)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isOver) FinancialNegative else FinancialPositive,
                                    fontWeight = FontWeight.Medium
                                )

                                if (!isOver && remainingDays > 0) {
                                    Text(
                                        text = "দৈনিক সীমা: ৳${BengaliUtils.toBanglaDigits(dailyBurnAllowance.toInt().toLong())}",
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
                                    text = "🎯 সংসারের জন্য কোনো বাজেট সেট নেই",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = { showBudgetDialog = true },
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("বাজেট সেট করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Quick Action Hub (৩টি প্রয়োজনীয় অ্যাকশন)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "⚡ দ্রুত সংসারের অ্যাকশন",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. টাকা খরচ (Cash)
                            HomeActionTile(
                                label = "টাকা খরচ",
                                subtitle = "বাজার ও বিল",
                                icon = Icons.Default.Add,
                                color = FinancialWarning,
                                onClick = onOpenAddExpense,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_home_cash_entry"
                            )

                            // 2. দোকানের পণ্য (Goods)
                            HomeActionTile(
                                label = "দোকানের পণ্য",
                                subtitle = "সংসারে নেওয়া",
                                icon = Icons.Default.Store,
                                color = MaterialTheme.colorScheme.primary,
                                onClick = { showGoodsWithdrawalSheet = true },
                                modifier = Modifier.weight(1f),
                                testTag = "btn_home_goods_entry"
                            )

                            // 3. রিপোর্ট ও প্রিন্ট
                            HomeActionTile(
                                label = "রিপোর্ট",
                                subtitle = "বিবরণী ও PDF",
                                icon = Icons.Default.Print,
                                color = FinancialPositive,
                                onClick = { showReportDialog = true },
                                modifier = Modifier.weight(1f),
                                testTag = "btn_home_report"
                            )
                        }
                    }
                }
            }

            // 4. Category Spending Breakdown (খাত অনুযায়ী সংসার খরচের চিত্র)
            if (categorizedBreakdown.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(18.dp),
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
                                    text = "সংসার খরচের খাত ভিত্তিক বিভাজন",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${BengaliUtils.toBanglaDigits(categorizedBreakdown.size.toLong())} টি খাত",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            categorizedBreakdown.take(5).forEach { (catName, catAmt) ->
                                val pct = if (totalHomeAmount > 0) (catAmt / totalHomeAmount * 100).toInt() else 0
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$catName (${BengaliUtils.toBanglaDigits(pct.toLong())}%)",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = BengaliUtils.formatTaka(catAmt),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = FinancialWarning
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    LinearProgressIndicator(
                                        progress = { (pct / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = FinancialWarning,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Bazaar Checklist Section (বাজারের ফর্দ ও কেনাকাটার চেকলিস্ট)
            item {
                BazaarChecklistSection(
                    onConvertToExpense = { amt, desc ->
                        viewModel.recordExpense(amount = amt, description = desc, isHome = true)
                    }
                )
            }

            // 5. Household Recurring Bills Section (মাসিক গৃহস্থালি বিল ও ইউটিলিটি ট্র্যাকার)
            item {
                HouseholdBillsSection(
                    onPayBill = { bill, amt ->
                        viewModel.recordExpense(amount = amt, description = "বিল: ${bill.title}", isHome = true)
                    }
                )
            }

            // 5.5 Visual Graphical Analysis Charts
            if (categorySpendings.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        InteractiveCategoryDonutChart(
                            categories = categorySpendings,
                            totalExpense = totalHomeAmount,
                            title = "সংসার খরচের খাতভিত্তিক চিত্র"
                        )
                    }
                }
            }

            if (dailyExpenseTrend.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        DailyExpenseTrendBarChart(
                            dailyExpenses = dailyExpenseTrend,
                            title = "সংসার ব্যয়ের দৈনিক ওঠানামা"
                        )
                    }
                }
            }

            // 6. Search Bar & Filter Chips
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("সংসারের খরচ খুঁজুন (যেমন: বাজার, বিদ্যুৎ, ওষুধ)") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "মুছুন")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_expense_search_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val catFilters = listOf("সকল", "বাজার", "বিল", "ভাড়া", "পড়াশোনা", "চিকিৎসা", "পণ্য")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(catFilters) { filterTag ->
                            val isSelected = selectedCategoryFilter == filterTag
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedCategoryFilter = filterTag },
                                color = if (isSelected) FinancialWarning.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isSelected) BorderStroke(1.dp, FinancialWarning) else null
                            ) {
                                Text(
                                    text = filterTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) FinancialWarning else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 6. Timeline List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "খরচের তালিকা",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${BengaliUtils.toBanglaDigits(displayedExpenses.size.toLong())} টি খরচ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 7. Transaction Cards
            if (displayedExpenses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        MawaEmptyState(
                            icon = Icons.Default.Home,
                            title = if (searchQuery.isNotEmpty() || selectedCategoryFilter != "সকল") "কোনো খরচ পাওয়া যায়নি" else "এই সময়ে কোনো খরচ নেই",
                            subtitle = "দোকান থেকে সংসারের বাজার বা খরচের জন্য টাকা নিলে এখানে যুক্ত করুন",
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
                            displayedExpenses.forEachIndexed { index, tx ->
                                HomeExpenseItemRow(
                                    transaction = tx,
                                    onDelete = { transactionToDelete = tx }
                                )
                                if (index < displayedExpenses.size - 1) {
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

    // Dialog: Monthly Budget Setting
    if (showBudgetDialog) {
        PersonalBudgetSettingDialog(
            currentBudget = homeBudget,
            title = "সংসারের মাসিক বাজেট নির্ধারণ",
            onDismiss = { showBudgetDialog = false },
            onSaveBudget = { newBudget ->
                homeBudget = newBudget
                budgetPrefs.edit().putFloat("home_monthly_budget", newBudget.toFloat()).apply()
            }
        )
    }

    // Dialog: Full Household Expense Report & PDF Print
    if (showReportDialog) {
        val reportEntries = remember(homeExpenses) {
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
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

        PersonalExpenseReportDialog(
            title = "সংসার ও পারিবারিক খরচ বিবরণী",
            periodLabel = currentFilter.banglaLabel,
            totalIncome = 0.0,
            totalExpense = totalHomeAmount,
            totalSavings = 0.0,
            netBalance = -totalHomeAmount,
            budgetAmount = homeBudget,
            categories = categorizedBreakdown,
            transactions = reportEntries,
            onDismiss = { showReportDialog = false }
        )
    }

    // Goods / Cash Withdrawal BottomSheet
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

    // Delete Confirmation Dialog
    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        val displayName = tx.note.ifBlank { tx.productName ?: "সংসার খরচ" }
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("খরচ মুছবেন?", fontWeight = FontWeight.Bold) },
            text = {
                Text("$displayName (${BengaliUtils.formatTaka(tx.amount)})-এর হিসাবটি মুছে ফেলতে চান?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(tx.id)
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
}

@Composable
private fun HomeActionTile(
    label: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun HomeExpenseItemRow(
    transaction: TransactionEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeStr = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date(transaction.timestamp))
    val isGoods = transaction.productName != null || transaction.note.contains("পণ্য") || transaction.note.contains("দোকান থেকে নেওয়া")
    val displayName = transaction.note.ifBlank { transaction.productName ?: "সংসারের খরচ" }

    Row(
        modifier = modifier
            .fillMaxWidth()
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
                    .background(FinancialWarning.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGoods) Icons.Default.ShoppingBag else Icons.Default.Home,
                    contentDescription = null,
                    tint = FinancialWarning,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isGoods) "দোকানের পণ্য" else "সংসারের খরচ",
                        style = MaterialTheme.typography.labelSmall,
                        color = FinancialWarning,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = " • $timeStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "৳${BengaliUtils.toBanglaDigits(transaction.amount.toLong())}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FinancialWarning
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
