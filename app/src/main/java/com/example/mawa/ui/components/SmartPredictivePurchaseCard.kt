package com.example.mawa.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.data.local.entity.SupplierCompanyEntity
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.PredictionReasonType
import com.example.mawa.util.PredictivePurchaseItem
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialPositiveContainer
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.FinancialWarningContainer
import com.example.ui.theme.MawaPrimary
import com.example.ui.theme.MawaPrimaryContainer

/**
 * Smart Predictive Purchasing & Recurring Expense Assistant
 * Analyzes prior transactions, day-of-week recurrence and SR schedule
 * to suggest today's expected companies & items with prior rate & 1-click expense entry.
 */
@Composable
fun SmartPredictivePurchaseCard(
    predictions: List<PredictivePurchaseItem>,
    targetDateMillis: Long,
    onConfirmExpense: (name: String, amount: Double, isPurchase: Boolean, note: String) -> Unit,
    onOpenWeeklyCalendar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (predictions.isEmpty()) return

    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(true) }

    // Track dismissed/skipped item IDs for current session
    val dismissedItemIds = remember { mutableStateMapOf<String, Boolean>() }
    val visiblePredictions = remember(predictions, dismissedItemIds.size) {
        predictions.filter { dismissedItemIds[it.id] != true }
    }

    if (visiblePredictions.isEmpty()) return

    // Quick Edit & Confirm Dialog State
    var editingItem by remember { mutableStateOf<PredictivePurchaseItem?>(null) }
    var editAmountInput by remember { mutableStateOf("") }
    var editNoteInput by remember { mutableStateOf("") }
    var editIsPurchase by remember { mutableStateOf(true) }

    val dayName = BengaliUtils.getBengaliDayOfWeek(targetDateMillis)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("smart_predictive_purchase_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.5.dp, MawaPrimary.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .background(MawaPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MawaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "আজকের সম্ভাব্য কেনাকাটা ও কোম্পানি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MawaPrimary
                            ) {
                                Text(
                                    text = BengaliUtils.toBanglaDigits(visiblePredictions.size),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "পূর্বের ডাটা বিশ্লেষণ করে $dayName-এর নিয়মিত পণ্য ও কোম্পানি",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "ছোট করুন" else "বড় করুন",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal Quick-Pill Summary for fast glance
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        visiblePredictions.forEach { item ->
                            val reasonBadgeColor = when (item.reasonType) {
                                PredictionReasonType.SCHEDULED_COMPANY -> FinancialPositive
                                PredictionReasonType.DAILY_STREAK -> MawaPrimary
                                PredictionReasonType.DAY_OF_WEEK_PATTERN -> FinancialWarning
                                PredictionReasonType.RECENT_CYCLE -> MaterialTheme.colorScheme.secondary
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, reasonBadgeColor.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable {
                                    editingItem = item
                                    editAmountInput = if (item.predictedAmount > 0) item.predictedAmount.toInt().toString() else ""
                                    editNoteInput = item.name
                                    editIsPurchase = item.isPurchaseType
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (item.predictedAmount > 0) {
                                        Text(
                                            text = "৳${BengaliUtils.toBanglaDigits(item.predictedAmount.toInt())}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MawaPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // List of Item Cards
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        visiblePredictions.take(4).forEach { item ->
                            PredictiveItemRow(
                                item = item,
                                onQuickAdd = {
                                    val amount = if (item.predictedAmount > 0) item.predictedAmount else 0.0
                                    if (amount > 0) {
                                        onConfirmExpense(item.name, amount, item.isPurchaseType, "${item.name} (${item.reasonType.banglaLabel})")
                                        dismissedItemIds[item.id] = true
                                        Toast.makeText(context, "${item.name} ৳${BengaliUtils.toBanglaDigits(amount.toInt())} খরচে যোগ হয়েছে", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Open edit if amount is 0
                                        editingItem = item
                                        editAmountInput = ""
                                        editNoteInput = item.name
                                        editIsPurchase = item.isPurchaseType
                                    }
                                },
                                onEditAndAdd = {
                                    editingItem = item
                                    editAmountInput = if (item.predictedAmount > 0) item.predictedAmount.toInt().toString() else ""
                                    editNoteInput = item.name
                                    editIsPurchase = item.isPurchaseType
                                },
                                onDismiss = {
                                    dismissedItemIds[item.id] = true
                                    Toast.makeText(context, "${item.name} আজকের জন্য সরানো হয়েছে", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        if (visiblePredictions.size > 4) {
                            TextButton(
                                onClick = onOpenWeeklyCalendar,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "আরও ${BengaliUtils.toBanglaDigits(visiblePredictions.size - 4)}টি সম্ভাব্য কোম্পানি ও রুটিন দেখুন →",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MawaPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Quick Edit & Confirm Dialog
    if (editingItem != null) {
        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MawaPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "খরচ ও ক্রয়ে যোগ নিশ্চিত করুন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "পণ্য / কোম্পানি: ${editingItem?.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "💡 ${editingItem?.reasonText}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    // Purchase vs Shop Expense Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { editIsPurchase = true },
                            shape = RoundedCornerShape(8.dp),
                            color = if (editIsPurchase) FinancialPositiveContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, if (editIsPurchase) FinancialPositive else Color.Transparent)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = if (editIsPurchase) FinancialPositive else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "পণ্য ক্রয় (স্টক)",
                                    fontSize = 11.sp,
                                    fontWeight = if (editIsPurchase) FontWeight.Bold else FontWeight.Normal,
                                    color = if (editIsPurchase) FinancialPositive else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { editIsPurchase = false },
                            shape = RoundedCornerShape(8.dp),
                            color = if (!editIsPurchase) FinancialWarningContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, if (!editIsPurchase) FinancialWarning else Color.Transparent)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = if (!editIsPurchase) FinancialWarning else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "দোকান খরচ",
                                    fontSize = 11.sp,
                                    fontWeight = if (!editIsPurchase) FontWeight.Bold else FontWeight.Normal,
                                    color = if (!editIsPurchase) FinancialWarning else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editAmountInput,
                        onValueChange = { editAmountInput = it },
                        label = { Text("টাকার পরিমাণ (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MawaPrimary
                        )
                    )

                    OutlinedTextField(
                        value = editNoteInput,
                        onValueChange = { editNoteInput = it },
                        label = { Text("বিবরণ / নোট") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalAmount = editAmountInput.toDoubleOrNull() ?: 0.0
                        if (finalAmount > 0 && editingItem != null) {
                            val item = editingItem!!
                            onConfirmExpense(item.name, finalAmount, editIsPurchase, editNoteInput.ifBlank { item.name })
                            dismissedItemIds[item.id] = true
                            Toast.makeText(context, "${item.name} ৳${BengaliUtils.toBanglaDigits(finalAmount.toInt())} সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show()
                            editingItem = null
                        } else {
                            Toast.makeText(context, "সঠিক টাকার পরিমাণ দিন", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
                ) {
                    Text("খরচে যুক্ত করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun PredictiveItemRow(
    item: PredictivePurchaseItem,
    onQuickAdd: () -> Unit,
    onEditAndAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Reason Icon / Chip
                    val (badgeText, badgeBg, badgeTextColor) = when (item.reasonType) {
                        PredictionReasonType.SCHEDULED_COMPANY -> Triple("শিডিউল কোম্পানি", FinancialPositiveContainer, FinancialPositive)
                        PredictionReasonType.DAILY_STREAK -> Triple("প্রতিদিনের মাল", MawaPrimaryContainer, MawaPrimary)
                        PredictionReasonType.DAY_OF_WEEK_PATTERN -> Triple("সাপ্তাহিক রুটিন", FinancialWarningContainer, FinancialWarning)
                        PredictionReasonType.RECENT_CYCLE -> Triple("প্রয়োজনীয়", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.secondary)
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Previous Rate & Amount Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (item.predictedAmount > 0) {
                        Text(
                            text = "পূর্বের দাম: ৳${BengaliUtils.toBanglaDigits(item.predictedAmount.toInt())}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MawaPrimary
                        )
                    }

                    if (item.previousRate > 0) {
                        Text(
                            text = "(${BengaliUtils.toBanglaDigits(item.previousRate.toInt())}৳/${item.unit})",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (item.companyEntity?.srName?.isNotBlank() == true) {
                        Text(
                            text = "SR: ${item.companyEntity.srName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 1-Click Quick Add Button
                Button(
                    onClick = onQuickAdd,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "১-ক্লিকে যোগ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Edit Button
                IconButton(
                    onClick = onEditAndAdd,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "পরিবর্তন",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Dismiss / Not today Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "আজ লাগবে না",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
