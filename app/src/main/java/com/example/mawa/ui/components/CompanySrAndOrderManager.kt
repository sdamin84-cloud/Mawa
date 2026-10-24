package com.example.mawa.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mawa.data.local.entity.FordiItemEntity
import com.example.mawa.data.local.entity.SupplierCompanyEntity
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.model.SimilarNameGroup
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialNegativeContainer
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialPositiveContainer
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.FinancialWarningContainer
import com.example.ui.theme.MawaPrimary
import com.example.ui.theme.MawaPrimaryContainer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 1. Similar Names Reminder Banner on Home / Company ledger
 * Notifies the shopkeeper when variations like "আবুল" / "আবুল খায়ের", "অলি" / "অলিম্পিক", "বন" / "বনরুটি" exist.
 */
@Composable
fun SimilarNamesReminderBanner(
    similarGroups: List<SimilarNameGroup>,
    onOpenMergeDialog: () -> Unit,
    onKeepSeparate: ((SimilarNameGroup) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (similarGroups.isEmpty()) return

    var isDismissed by remember { mutableStateOf(false) }
    if (isDismissed) return

    val firstGroup = similarGroups.first()
    val otherVariation = firstGroup.variations.firstOrNull { it != firstGroup.primaryName } ?: firstGroup.variations.lastOrNull() ?: ""
    val isDark = isSystemInDarkTheme()
    val brandPrimary = MaterialTheme.colorScheme.primary
    val containerBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
    val borderStrokeColor = if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .testTag("similar_names_reminder_banner"),
        shape = RoundedCornerShape(14.dp),
        color = containerBg,
        border = BorderStroke(1.dp, borderStrokeColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(brandPrimary.copy(alpha = if (isDark) 0.2f else 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "পরামর্শ",
                            tint = brandPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "✨ নাম যাচাইয়ের স্মার্ট পরামর্শ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "'${firstGroup.primaryName}' ও '$otherVariation' কি একই নাকি দুটি আলাদা?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = { isDismissed = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "বন্ধ করুন",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick 1-tap Keep Both Separate Button
                if (onKeepSeparate != null) {
                    Surface(
                        onClick = {
                            onKeepSeparate(firstGroup)
                            isDismissed = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "দুটিই আলাদা",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Open Dialog Button
                Surface(
                    onClick = { onOpenMergeDialog() },
                    shape = RoundedCornerShape(8.dp),
                    color = brandPrimary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "বিস্তারিত সাজান",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog allowing user to unify variations into a single clean official name or keep them separate with checkboxes
 */
@Composable
fun SimilarNamesMergeDialog(
    similarGroups: List<SimilarNameGroup>,
    onDismiss: () -> Unit,
    onMergeNames: (oldName: String, newName: String) -> Unit,
    onKeepSeparate: ((SimilarNameGroup) -> Unit)? = null
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    var selectedGroup by remember { mutableStateOf<SimilarNameGroup?>(similarGroups.firstOrNull()) }
    var selectedTargetName by remember(selectedGroup) {
        mutableStateOf(selectedGroup?.primaryName ?: "")
    }
    var customNameInput by remember { mutableStateOf("") }
    var isCustomTarget by remember { mutableStateOf(false) }

    // Map of variations checked for keeping both
    var checkedVariations by remember(selectedGroup) {
        mutableStateOf<Map<String, Boolean>>(selectedGroup?.variations?.associateWith { true } ?: emptyMap<String, Boolean>())
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("similar_names_merge_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    color = Color(0xFF4F46E5), // Royal Indigo
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "নামের অমিল ও আলাদা পণ্য যাচাই",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "আলাদা পণ্য হলে টিক দিন, অথবা এক নামে রূপান্তর করুন",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "বন্ধ করুন",
                                tint = Color.White
                            )
                        }
                    }
                }

                if (similarGroups.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কোনো নামের মিল বা অমিল পাওয়া যায়নি। আপনার সব তথ্য সুশৃঙ্খল ও নিখুঁত!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "সনাক্তকৃত নামের গ্রুপসমূহ:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Horizontal selector chips for clusters
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(similarGroups) { group ->
                                    val isSelected = selectedGroup == group
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                selectedGroup = group
                                                selectedTargetName = group.primaryName
                                                isCustomTarget = false
                                                checkedVariations = group.variations.associateWith { true }
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) Color(0xFF4F46E5) else MaterialTheme.colorScheme.surfaceVariant,
                                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Text(
                                            text = "${group.primaryName} (${BengaliUtils.toBanglaDigits(group.variations.size.toLong())})",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }

                        selectedGroup?.let { group ->
                            // SECTION 1: Explicit Tick/Checkbox Option to Keep Both as Separate
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDark) Color(0xFF064E3B).copy(alpha = 0.35f) else Color(0xFFECFDF5)
                                    ),
                                    border = BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.7f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF059669),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "✓ টিক অপশন: উভয়টিই সঠিক পণ্য ও আলাদা খাতা",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF047857)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "যদি দুটিই ভিন্ন ও সঠিক পণ্য হয় (যেমন: 'দেশি চাল' ও 'দেশি ডাল'), তবে নিচে টিক দিয়ে আলাদা রাখুন:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Checkboxes for each item
                                        group.variations.forEach { variation ->
                                            val isChecked = checkedVariations[variation] ?: true
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        checkedVariations = checkedVariations + (variation to !isChecked)
                                                    }
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = isChecked,
                                                    onCheckedChange = { newVal ->
                                                        checkedVariations = checkedVariations + (variation to newVal)
                                                    },
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = Color(0xFF059669),
                                                        checkmarkColor = Color.White
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "$variation (সঠিক পণ্য হিসেবে টিক দিন)",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Button(
                                            onClick = {
                                                onKeepSeparate?.invoke(group)
                                                Toast.makeText(
                                                    context,
                                                    "✓ উভয়টিই আলাদা ও সঠিক পণ্য হিসেবে নিশ্চিত করা হলো!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                onDismiss()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("btn_keep_both_separate"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DoneAll,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(19.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "✓ দুটিই টিক আছে — আলাদা রাখুন",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 13.5.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // SECTION 2: Merge into a Single Clean Name
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "অথবা বানান ভুল থাকলে এক নামে একীভূত করুন:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        group.variations.forEach { variation ->
                                            val count = group.occurrences[variation] ?: 0
                                            val isChosen = !isCustomTarget && selectedTargetName == variation

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        selectedTargetName = variation
                                                        isCustomTarget = false
                                                    }
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = isChosen,
                                                    onClick = {
                                                        selectedTargetName = variation
                                                        isCustomTarget = false
                                                    },
                                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4F46E5))
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = variation,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = "${BengaliUtils.toBanglaDigits(count.toLong())} বার ব্যবহৃত",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Custom Name Option
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { isCustomTarget = true }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isCustomTarget,
                                                onClick = { isCustomTarget = true },
                                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF4F46E5))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "নতুন সঠিক নাম লিখুন:",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        if (isCustomTarget) {
                                            OutlinedTextField(
                                                value = customNameInput,
                                                onValueChange = { customNameInput = it },
                                                placeholder = { Text("যেমন: আবুল খায়ের টোব্যাকো / অলিম্পিক") },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 32.dp, top = 4.dp),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        val finalTarget = if (isCustomTarget) customNameInput.trim() else selectedTargetName.trim()
                                        val variationsToMerge = group.variations.filter { !it.equals(finalTarget, ignoreCase = true) }

                                        Button(
                                            onClick = {
                                                if (finalTarget.isNotBlank()) {
                                                    variationsToMerge.forEach { old ->
                                                        onMergeNames(old, finalTarget)
                                                    }
                                                    Toast.makeText(
                                                        context,
                                                        "✓ সফলভাবে '$finalTarget' নামে একীভূত করা হলো",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    onDismiss()
                                                }
                                            },
                                            enabled = finalTarget.isNotBlank() && variationsToMerge.isNotEmpty(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("btn_merge_all_names"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "সবগুলো '$finalTarget' নামে একীভূত করুন",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
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
}

/**
 * 2. Pending Afternoon Supplier Payments Banner & Actions
 * Directly addresses: "মাল বোঝে পেলাম তবে টাকা পেমেন্ট করিনি বিকালে দেবার কথা এমন দেখাবে"
 */
@Composable
fun PendingAfternoonPaymentsBanner(
    pendingTransactions: List<TransactionEntity>,
    onMarkAsPaid: (transactionId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (pendingTransactions.isEmpty()) return

    val totalPendingAmount = pendingTransactions.sumOf { it.amount }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pending_supplier_payments_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FinancialWarningContainer.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, FinancialWarning.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(FinancialWarning.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = FinancialWarning,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "🕒 মাল পেয়েছি, টাকা বিকালে দেওয়ার কথা",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${BengaliUtils.toBanglaDigits(pendingTransactions.size.toLong())}টি কোম্পানির টাকা বিকালে পরিশোধ বাকি",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Text(
                    text = BengaliUtils.formatTaka(totalPendingAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = FinancialNegative
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                pendingTransactions.forEach { tx ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.productName ?: tx.note.ifBlank { "কোম্পানি মাল" },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "মূল্য: ${BengaliUtils.formatTaka(tx.amount)} • স্ট্যাটাস: পেন্ডিং (বিকালে পরিশোধ)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FinancialWarning,
                                    fontSize = 10.5.sp
                                )
                            }

                            Button(
                                onClick = { onMarkAsPaid(tx.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "নগদ পরিশোধ",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. Daily Scheduled Company Orders & Tomorrow Delivery Tracking
 * Directly addresses: "আজ রবিবার অর্ডারে আসলে sr সোমবারের অর্ডার কি দিছি কোন কম্পোনি থেকে কতটাকার পন্য সেটা সহজে জানা যাবে আর তখন সোমবার এর এনট্রি আরও সহজ হবে শুধু পন্য নিছি এমন নিশ্চিত দিলে খরচে চলে যাবে"
 */
@Composable
fun ScheduledDeliveryOrdersCard(
    fordiItems: List<FordiItemEntity>,
    onConfirmReceivedAndPaid: (item: FordiItemEntity) -> Unit,
    onConfirmReceivedDueAfternoon: (item: FordiItemEntity) -> Unit,
    onAddNewOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todayBengaliDay = remember { BengaliUtils.getBengaliDayOfWeek(System.currentTimeMillis()) }

    // Filter pending fordi items that have order day or delivery day set
    val scheduledItems = remember(fordiItems, todayBengaliDay) {
        fordiItems.filter { !it.isPurchased }
    }

    val todayDeliveries = remember(scheduledItems, todayBengaliDay) {
        scheduledItems.filter {
            it.deliveryDay.isNotBlank() && it.deliveryDay.contains(todayBengaliDay, ignoreCase = true)
        }
    }

    val displayList = if (todayDeliveries.isNotEmpty()) todayDeliveries else scheduledItems.filter { it.deliveryDay.isNotBlank() || it.orderDay.isNotBlank() }
    if (displayList.isEmpty()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("scheduled_orders_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MawaPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = MawaPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (todayDeliveries.isNotEmpty()) "📦 আজকের ডেলিভারি তালিকা ($todayBengaliDay)" else "📦 কোম্পানি অর্ডার ও ডেলিভারি তালিকা",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (todayDeliveries.isNotEmpty()) "${BengaliUtils.toBanglaDigits(todayDeliveries.size.toLong())}টি কোম্পানির মাল আজ দোকানে আসার কথা" else "এসআর-কে দেওয়া অর্ডারের তালিকা",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onAddNewOrder,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "অর্ডার যোগ",
                        tint = MawaPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                displayList.forEach { item ->
                    val estTotal = (if (item.actualQuantity > 0) item.actualQuantity else item.plannedQuantity) *
                            (if (item.actualRate > 0) item.actualRate else item.purchaseRate)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.productName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val companyStr = if (item.companyName.isNotBlank()) "${item.companyName} • " else ""
                                    val scheduleStr = if (item.deliveryDay.isNotBlank()) "ডেলিভারি: ${item.deliveryDay}" else "ফর্দ আইটেম"
                                    Text(
                                        text = "$companyStr$scheduleStr",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }

                                Text(
                                    text = BengaliUtils.formatTaka(estTotal),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MawaPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 1-Click Fast Arrival Confirmations
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. Mal peyechi & Nogod Porishodh
                                Button(
                                    onClick = { onConfirmReceivedAndPaid(item) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "নগদে বুঝে পেয়েছি",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                }

                                // 2. Mal peyechi & Taka Bikale debo
                                OutlinedButton(
                                    onClick = { onConfirmReceivedDueAfternoon(item) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FinancialWarning),
                                    border = BorderStroke(1.dp, FinancialWarning),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = FinancialWarning,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "পেয়েছি, টাকা বিকালে",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = FinancialWarning,
                                        fontSize = 11.sp
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

/**
 * 4. Company SR Profile & Direct Call Component
 * Directly addresses: "আর কোম্পানির সেলস ম্যান বা প্রতিনিধির নাম্বার সেভ থাকা জরুরি প্রয়োজনে সরাসরি কল অপশন সহ"
 */
@Composable
fun CompanySrProfileCard(
    company: SupplierCompanyEntity,
    onEditCompany: (SupplierCompanyEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .background(MawaPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = MawaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = company.companyName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val srInfo = if (company.srName.isNotBlank()) "প্রতিনিধি: ${company.srName}" else "প্রতিনিধির তথ্য যোগ করুন"
                        Text(
                            text = srInfo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(
                    onClick = { onEditCompany(company) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "সম্পাদনা",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Order & Delivery Schedule row
            if (company.orderDay.isNotBlank() || company.deliveryDay.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (company.orderDay.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MawaPrimaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "অর্ডার দিন: ${company.orderDay}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MawaPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    if (company.deliveryDay.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FinancialPositiveContainer.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "ডেলিভারি: ${company.deliveryDay}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = FinancialPositive,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Direct Call & SMS actions if phone exists
            if (company.srPhone.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Call Button
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${company.srPhone}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "কল করা সম্ভব হয়নি", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "কল দিন (${company.srPhone})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // SMS Button
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${company.srPhone}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "এসএমএস অপশন চালু হয়নি", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(50.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = "এসএমএস",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onEditCompany(company) }
                ) {
                    Text(
                        text = "+ সেলসম্যান বা প্রতিনিধির মোবাইল নম্বর সেভ করুন",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dialog for adding or editing Company SR Contact and Schedule
 */
@Composable
fun EditCompanySrDialog(
    initialCompany: SupplierCompanyEntity?,
    defaultName: String = "",
    onDismiss: () -> Unit,
    onSave: (SupplierCompanyEntity) -> Unit
) {
    var companyName by remember { mutableStateOf(initialCompany?.companyName ?: defaultName) }
    var srName by remember { mutableStateOf(initialCompany?.srName ?: "") }
    var srPhone by remember { mutableStateOf(initialCompany?.srPhone ?: "") }
    var orderDay by remember { mutableStateOf(initialCompany?.orderDay ?: "") }
    var deliveryDay by remember { mutableStateOf(initialCompany?.deliveryDay ?: "") }
    var note by remember { mutableStateOf(initialCompany?.note ?: "") }

    val daysOfWeek = listOf("রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .testTag("edit_company_sr_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    color = MawaPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (initialCompany != null) "কোম্পানি ও এসআর তথ্য পরিবর্তন" else "নতুন কোম্পানি ও এসআর যোগ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "বন্ধ",
                                tint = Color.White
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("কোম্পানির নাম (যেমন: আবুল খায়ের / অলিম্পিক)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = srName,
                            onValueChange = { srName = it },
                            label = { Text("সেলসম্যান বা প্রতিনিধির নাম (SR Name)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = srPhone,
                            onValueChange = { srPhone = it },
                            label = { Text("প্রতিনিধির মোবাইল নাম্বার (সরাসরি কল দিন)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text(
                            text = "অর্ডার নেওয়ার দিন:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(daysOfWeek) { day ->
                                val isSelected = orderDay == day
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MawaPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { orderDay = if (isSelected) "" else day }
                                ) {
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "মাল ডেলিভারির দিন:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(daysOfWeek) { day ->
                                val isSelected = deliveryDay == day
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) FinancialPositive else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { deliveryDay = if (isSelected) "" else day }
                                ) {
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("অতিরিক্ত নোট বা পণ্যের বিবরণ") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("বাতিল")
                        }

                        Button(
                            onClick = {
                                if (companyName.isNotBlank()) {
                                    onSave(
                                        SupplierCompanyEntity(
                                            id = initialCompany?.id ?: 0L,
                                            companyName = companyName.trim(),
                                            srName = srName.trim(),
                                            srPhone = srPhone.trim(),
                                            orderDay = orderDay,
                                            deliveryDay = deliveryDay,
                                            note = note.trim(),
                                            createdAt = initialCompany?.createdAt ?: System.currentTimeMillis()
                                        )
                                    )
                                    onDismiss()
                                }
                            },
                            enabled = companyName.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("সংরক্ষণ করুন")
                        }
                    }
                }
            }
        }
    }
}
