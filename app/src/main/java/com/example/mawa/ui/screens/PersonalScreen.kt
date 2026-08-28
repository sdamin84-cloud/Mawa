package com.example.mawa.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.local.entity.PersonalTransactionEntity
import com.example.mawa.data.local.entity.PersonalTransactionType
import com.example.mawa.data.model.AppMode
import com.example.mawa.data.model.CategorySpending
import com.example.mawa.data.model.PersonalSummary
import com.example.mawa.data.model.TimeFilter
import com.example.mawa.ui.components.MawaEmptyState
import com.example.mawa.ui.components.MawaTopBar
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialWarning
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PersonalScreen(
    viewModel: MawaViewModel,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null,
    onSwitchToBusiness: (() -> Unit)? = null
) {
    val summary by viewModel.personalSummary.collectAsStateWithLifecycle()
    val timeFilter by viewModel.personalTimeFilter.collectAsStateWithLifecycle()
    val appMode by viewModel.currentAppMode.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTypeForAdd by remember { mutableStateOf(PersonalTransactionType.EXPENSE) }
    var transactionToEdit by remember { mutableStateOf<PersonalTransactionEntity?>(null) }
    var transactionToDelete by remember { mutableStateOf<PersonalTransactionEntity?>(null) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            MawaTopBar(
                title = "ব্যক্তিগত",
                subtitle = "দৈনন্দিন খরচ, আয় ও সঞ্চয়ের খাতা",
                onMenuClick = onOpenDrawer
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("personal_screen_list")
            ) {
                // 1. If AppMode is BOTH, show quick mode toggle banner
                if (appMode == AppMode.BOTH && onSwitchToBusiness != null) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
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
                                    modifier = Modifier.height(32.dp)
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

                // 2. Main Hero Card: এই মাসের খরচ & Summary
                item {
                    PersonalMonthlyHeroCard(
                        summary = summary,
                        timeFilter = timeFilter,
                        onTimeFilterSelected = { viewModel.setPersonalTimeFilter(it) },
                        onQuickAdd = { type ->
                            selectedTypeForAdd = type
                            showAddDialog = true
                        }
                    )
                }

                // 3. Fast Action Banner: "＋ দ্রুত হিসাব যোগ করুন"
                item {
                    PersonalFastEntryBanner(
                        onAddExpense = {
                            selectedTypeForAdd = PersonalTransactionType.EXPENSE
                            showAddDialog = true
                        },
                        onAddIncome = {
                            selectedTypeForAdd = PersonalTransactionType.INCOME
                            showAddDialog = true
                        },
                        onAddSavings = {
                            selectedTypeForAdd = PersonalTransactionType.SAVINGS
                            showAddDialog = true
                        }
                    )
                }

                // 4. Category-wise Spending Breakdown
                if (summary.categoryBreakdown.isNotEmpty()) {
                    item {
                        PersonalCategorySpendingSection(
                            categoryList = summary.categoryBreakdown,
                            totalExpense = summary.totalExpense
                        )
                    }
                }

                // 5. Ledger List Section ("আজকের হিসাব" / "নির্বাচিত সময়ের হিসাব")
                item {
                    val sectionTitle = when (timeFilter) {
                        TimeFilter.TODAY -> "আজকের হিসাব"
                        TimeFilter.THIS_WEEK -> "এই সপ্তাহের হিসাব"
                        TimeFilter.THIS_MONTH -> "এই মাসের হিসাব"
                        TimeFilter.ALL_TIME -> "সর্বমোট হিসাব"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sectionTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${BengaliUtils.toBanglaDigits(summary.periodTransactions.size.toLong())} টি এন্ট্রি",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (summary.periodTransactions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            MawaEmptyState(
                                icon = Icons.Outlined.AccountBalanceWallet,
                                title = "কোনো হিসাব যোগ করা নেই",
                                subtitle = "নিচের বাটন চেপে খরচ, আয় বা সঞ্চয় যোগ করুন",
                                actionLabel = "＋ হিসাব যোগ করুন",
                                onActionClick = {
                                    selectedTypeForAdd = PersonalTransactionType.EXPENSE
                                    showAddDialog = true
                                }
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
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                summary.periodTransactions.forEachIndexed { index, tx ->
                                    PersonalTransactionLedgerItem(
                                        transaction = tx,
                                        onEdit = { transactionToEdit = tx },
                                        onDelete = { transactionToDelete = tx }
                                    )
                                    if (index < summary.periodTransactions.size - 1) {
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
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Add / Create Dialog
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

    // Edit Dialog
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

    // Delete Confirmation Dialog
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
            // Main Top Title: "এই মাসের খরচ"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "এই মাসের খরচ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = BengaliUtils.formatTaka(summary.thisMonthExpense),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Balance indicator chip
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

            Spacer(modifier = Modifier.height(16.dp))

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
// Component: Personal Fast Entry Banner
// -------------------------------------------------------------

@Composable
private fun PersonalFastEntryBanner(
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onAddSavings: () -> Unit,
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
            Text(
                text = "＋ দ্রুত হিসাব যোগ করুন",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

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
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
// Component: Personal Transaction Ledger Item (Clean Ledger Style)
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
// Component: Personal Transaction Entry Dialog (Create / Edit)
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
    var customCategoryText by remember { mutableStateOf("") }
    var showCustomCategoryInput by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Category lists based on type
    val expenseCategories = listOf("খাবার", "যাতায়াত", "পড়াশোনা", "চিকিৎসা", "বিল", "কেনাকাটা", "মোবাইল", "অন্যান্য")
    val incomeCategories = listOf("বেতন", "ব্যবসা/উত্তোলন", "উপহার", "ফ্রিল্যান্সিং", "বাড়ি ভাড়া", "অন্যান্য")
    val savingsCategories = listOf("ব্যাংক", "ডিপিএস", "নগদ", "সোনা/জমি", "অন্যান্য")

    val currentCategories = when (selectedType) {
        PersonalTransactionType.EXPENSE -> expenseCategories
        PersonalTransactionType.INCOME -> incomeCategories
        PersonalTransactionType.SAVINGS -> savingsCategories
    }

    // Set default category if blank
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

                // "কী জন্য" (Title) Field
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
                    text = "ক্যাটাগরি নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(currentCategories) { catItem ->
                        val isSelected = category == catItem && !showCustomCategoryInput
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                category = catItem
                                showCustomCategoryInput = false
                            },
                            label = { Text(catItem, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getCategoryIcon(catItem),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Date Picker Section
                val entryCal = remember(timestamp) {
                    Calendar.getInstance().apply { timeInMillis = timestamp }
                }
                val isToday = remember(timestamp) {
                    val now = Calendar.getInstance()
                    entryCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                            entryCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
                }
                val isYesterday = remember(timestamp) {
                    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                    entryCal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                            entryCal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, y)
                                        set(Calendar.MONTH, m)
                                        set(Calendar.DAY_OF_MONTH, d)
                                    }
                                    timestamp = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .testTag("btn_select_personal_date"),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(
                        1.dp,
                        if (!isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = "তারিখ",
                                tint = if (!isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "তারিখ: $dateFormatted",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (!isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
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
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Quick Chips for Date Selection
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { timestamp = System.currentTimeMillis() },
                        color = if (isToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "আজ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                                timestamp = yesterdayCal.timeInMillis
                            },
                        color = if (isYesterday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "গতকাল",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isYesterday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isYesterday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
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
        else -> Icons.Default.MoreHoriz
    }
}
