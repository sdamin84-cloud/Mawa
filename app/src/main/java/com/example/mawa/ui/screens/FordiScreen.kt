package com.example.mawa.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.local.entity.FordiItemEntity
import com.example.mawa.data.local.entity.ProductEntity
import com.example.mawa.ui.components.MawaEmptyState
import com.example.mawa.ui.components.MawaTopBar
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.DataBackupRestoreManager
import com.example.mawa.util.ReportExportUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialPositiveContainer
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.FinancialWarningContainer
import com.example.ui.theme.MawaPrimary

enum class FordiTab {
    CURRENT, // বর্তমান ফর্দ
    HISTORY  // আগে করা সব ফর্দ
}

@Composable
fun FordiScreen(
    viewModel: MawaViewModel,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null
) {
    val allFordiItems by viewModel.allFordiItems.collectAsStateWithLifecycle()
    val activeProducts by viewModel.activeProducts.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(FordiTab.CURRENT) }
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<FordiItemEntity?>(null) }
    var showNewFordiConfirmDialog by remember { mutableStateOf(false) }

    val pendingItems = remember(allFordiItems) { allFordiItems.filter { !it.isPurchased } }
    val purchasedItems = remember(allFordiItems) { allFordiItems.filter { it.isPurchased } }

    // Checked Items State (Selection by ID)
    var checkedItemIds by remember { mutableStateOf(setOf<Long>()) }

    // Auto-update checked set when pending list changes
    val validCheckedIds = remember(pendingItems, checkedItemIds) {
        val pendingIds = pendingItems.map { it.id }.toSet()
        checkedItemIds.filter { pendingIds.contains(it) }.toSet()
    }

    val checkedItems = remember(pendingItems, validCheckedIds) {
        pendingItems.filter { validCheckedIds.contains(it.id) }
    }

    // Total of all pending items
    val grandPlannedTotal = remember(pendingItems) {
        pendingItems.sumOf { it.plannedQuantity * it.purchaseRate }
    }

    // Total of only checked items
    val checkedTotal = remember(checkedItems) {
        checkedItems.sumOf { it.plannedQuantity * it.purchaseRate }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        MawaTopBar(
            title = "বাজারের ফর্দ",
            subtitle = if (activeTab == FordiTab.CURRENT) {
                "মোট ফর্দ: ${BengaliUtils.formatTaka(grandPlannedTotal)} | টিক করা: ${BengaliUtils.formatTaka(checkedTotal)}"
            } else {
                "পূর্বে সম্পন্ন ফর্দ: ${BengaliUtils.toBanglaDigits(purchasedItems.size.toLong())} টি"
            },
            onMenuClick = onOpenDrawer
        )

        // Tab Switcher: [ বর্তমান ফর্দ ] [ আগে করা সব ফর্দ ]
        TabRow(
            selectedTabIndex = activeTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MawaPrimary,
            divider = {
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
        ) {
            Tab(
                selected = activeTab == FordiTab.CURRENT,
                onClick = { activeTab = FordiTab.CURRENT },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "বর্তমান ফর্দ (${BengaliUtils.toBanglaDigits(pendingItems.size.toLong())})",
                            fontWeight = if (activeTab == FordiTab.CURRENT) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                },
                modifier = Modifier.testTag("tab_current_fordi")
            )
            Tab(
                selected = activeTab == FordiTab.HISTORY,
                onClick = { activeTab = FordiTab.HISTORY },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "আগে করা সব ফর্দ (${BengaliUtils.toBanglaDigits(purchasedItems.size.toLong())})",
                            fontWeight = if (activeTab == FordiTab.HISTORY) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                },
                modifier = Modifier.testTag("tab_history_fordi")
            )
        }

        when (activeTab) {
            FordiTab.CURRENT -> {
                // CURRENT FORDI TAB
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Action Header: [ নতুন ফর্দ ] [ + পণ্য যোগ ]
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // New Fordi Button
                                OutlinedTextField(
                                    value = "",
                                    onValueChange = {},
                                    enabled = false,
                                    modifier = Modifier.size(0.dp) // dummy to avoid layout glitch
                                )
                                TextButton(
                                    onClick = {
                                        if (pendingItems.isNotEmpty()) {
                                            showNewFordiConfirmDialog = true
                                        } else {
                                            showAddDialog = true
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("new_fordi_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.PlaylistAdd, contentDescription = null, modifier = Modifier.size(18.dp), tint = MawaPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("নতুন ফর্দ", fontWeight = FontWeight.Bold, color = MawaPrimary)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Select All Checkbox Button
                                    if (pendingItems.isNotEmpty()) {
                                        val allChecked = pendingItems.isNotEmpty() && validCheckedIds.size == pendingItems.size
                                        TextButton(
                                            onClick = {
                                                checkedItemIds = if (allChecked) {
                                                    emptySet()
                                                } else {
                                                    pendingItems.map { it.id }.toSet()
                                                }
                                            }
                                        ) {
                                            Text(if (allChecked) "টিক সরান" else "সব টিক দিন", fontSize = 12.sp)
                                        }
                                    }

                                    // Add Item Button
                                    Button(
                                        onClick = { showAddDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary),
                                        modifier = Modifier.testTag("add_fordi_item_btn")
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("পণ্য যোগ")
                                    }
                                }
                            }
                        }
                    }

                    // Quick Actions: [ ফর্দ ছবি (PNG) ] [ শেয়ার (WhatsApp) ] [ CSV এক্সপোর্ট ]
                    if (pendingItems.isNotEmpty()) {
                        item {
                            val context = LocalContext.current
                            val shopSettings by viewModel.shopSettings.collectAsStateWithLifecycle()
                            val shopName = shopSettings?.shopName?.ifBlank { "মাওয়া স্মার্ট খাতা" } ?: "মাওয়া স্মার্ট খাতা"

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // PNG Memo Image
                                    Button(
                                        onClick = {
                                            val bitmap = DataBackupRestoreManager.createFordiMemoBitmap(
                                                shopName = shopName,
                                                items = pendingItems
                                            )
                                            DataBackupRestoreManager.shareBitmapAsImage(
                                                context = context,
                                                bitmap = bitmap,
                                                fileNamePrefix = "fordi_${System.currentTimeMillis()}",
                                                title = "ক্রয় ফর্দ মেমো"
                                            )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag("btn_fordi_png_share"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "ফর্দ ছবি",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }

                                    // WhatsApp Text Share
                                    Button(
                                        onClick = {
                                            val text = buildString {
                                                appendLine("🛒 *কেনাকাটার ফর্দ - $shopName*")
                                                appendLine("তারিখ: ${BengaliUtils.formatDateForExport(System.currentTimeMillis())}")
                                                appendLine("--------------------------------")
                                                pendingItems.forEachIndexed { index, item ->
                                                    val unitText = item.unit.ifBlank { "টি" }
                                                    val priceText = if (item.purchaseRate > 0) " (দর: ৳${item.purchaseRate})" else ""
                                                    appendLine("${BengaliUtils.toBanglaDigits((index + 1).toLong())}. ${item.productName} - ${BengaliUtils.toBanglaDigits(item.plannedQuantity.toLong())} $unitText$priceText")
                                                }
                                                appendLine("--------------------------------")
                                                appendLine("মোট আইটেম: ${BengaliUtils.toBanglaDigits(pendingItems.size.toLong())} টি")
                                            }
                                            ReportExportUtils.shareText(
                                                context = context,
                                                text = text,
                                                title = "ফর্দ পাঠান"
                                            )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag("btn_fordi_whatsapp_share"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive.copy(alpha = 0.15f)),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = null,
                                            tint = FinancialPositive,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "শেয়ার",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = FinancialPositive
                                        )
                                    }

                                    // CSV Export
                                    Button(
                                        onClick = {
                                            val csv = ReportExportUtils.generateFordiCsv(pendingItems)
                                            ReportExportUtils.shareText(
                                                context = context,
                                                text = csv,
                                                title = "ফর্দ CSV ফাইল"
                                            )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag("btn_fordi_csv_export"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "CSV",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // CHECKED ITEMS EXPENSE CARD (Prominent Banner to transfer ticked items to Expense List)
                    if (checkedItems.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .clickable {
                                        viewModel.convertMultipleFordiToPurchases(checkedItems) {
                                            checkedItemIds = emptySet()
                                        }
                                    }
                                    .testTag("checked_items_expense_banner"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = FinancialPositiveContainer),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, FinancialPositive)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
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
                                                .background(FinancialPositive),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = "টিক কৃত পণ্য (${BengaliUtils.toBanglaDigits(checkedItems.size.toLong())}টি)",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = FinancialPositive
                                            )
                                            Text(
                                                text = "ক্লিক করে খরচ লিস্টে পাঠান",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = BengaliUtils.formatTaka(checkedTotal),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = FinancialPositive
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = FinancialPositive
                                        ) {
                                            Text(
                                                text = "খরচে নিন ➔",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Table Header
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "টিক",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(36.dp)
                                )
                                Text(
                                    text = "পণ্য",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1.8f)
                                )
                                Text(
                                    text = "পরিমাণ",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1.1f)
                                )
                                Text(
                                    text = "ক্রয়→বেচা",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1.5f)
                                )
                                Text(
                                    text = "মোট",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1.2f)
                                )
                                Spacer(modifier = Modifier.width(36.dp))
                            }
                        }
                    }

                    // Empty State
                    if (pendingItems.isEmpty()) {
                        item {
                            MawaEmptyState(
                                icon = Icons.Default.Assignment,
                                title = "বর্তমান ফর্দে কোনো পণ্য নেই",
                                subtitle = "উপরে 'পণ্য যোগ' বাটনে ক্লিক করে ফর্দ বানান অথবা পূর্বে করা ফর্দ থেকে পণ্য নিন",
                                actionLabel = "পণ্য যোগ করুন",
                                onActionClick = { showAddDialog = true }
                            )
                        }
                    } else {
                        // Pending Items with Checkbox on Left & Edit Capability
                        items(pendingItems, key = { it.id }) { item ->
                            val isChecked = validCheckedIds.contains(item.id)
                            val lineTotal = item.plannedQuantity * item.purchaseRate

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { itemToEdit = item },
                                color = if (isChecked) FinancialPositiveContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 1. Checkbox on the Left (টিক অপশন)
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                checkedItemIds = if (checked) {
                                                    checkedItemIds + item.id
                                                } else {
                                                    checkedItemIds - item.id
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = FinancialPositive,
                                                uncheckedColor = MaterialTheme.colorScheme.outline
                                            ),
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("checkbox_fordi_item_${item.id}")
                                        )

                                        // 2. Product Name
                                        Column(modifier = Modifier.weight(1.8f)) {
                                            Text(
                                                text = item.productName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // 3. Planned Quantity
                                        Text(
                                            text = BengaliUtils.formatQuantity(item.plannedQuantity, item.unit),
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1.1f)
                                        )

                                        // 4. Purchase -> Selling Rate (ক্রয় ও বিক্রয় দর)
                                        Text(
                                            text = "${BengaliUtils.toBanglaDigits(item.purchaseRate.toInt().toString())}→${BengaliUtils.toBanglaDigits(item.sellingRate.toInt().toString())}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1.5f)
                                        )

                                        // 5. Line Total
                                        Text(
                                            text = BengaliUtils.formatTaka(lineTotal),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isChecked) FinancialPositive else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1.2f)
                                        )

                                        // 6. Edit Button
                                        IconButton(
                                            onClick = { itemToEdit = item },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("edit_fordi_item_${item.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "এডিট করুন",
                                                tint = MawaPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Grand Totals Summary Card at bottom
                    if (pendingItems.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "ফর্দের সর্বমোট ব্যয়:",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = BengaliUtils.formatTaka(grandPlannedTotal),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    if (checkedItems.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "টিক কৃত পণ্যের মোট (${BengaliUtils.toBanglaDigits(checkedItems.size.toLong())}টি):",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = FinancialPositive
                                            )
                                            Text(
                                                text = BengaliUtils.formatTaka(checkedTotal),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Black,
                                                color = FinancialPositive
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                viewModel.convertMultipleFordiToPurchases(checkedItems) {
                                                    checkedItemIds = emptySet()
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("convert_checked_to_expense_btn"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive)
                                        ) {
                                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "টিক করা পণ্য খরচ লিস্টে পাঠান (${BengaliUtils.formatTaka(checkedTotal)})",
                                                fontWeight = FontWeight.Bold
                                            )
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

            FordiTab.HISTORY -> {
                // PREVIOUS / PURCHASED FORDIS (আগে করা সব ফর্দ)
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "পূর্বে কেনা ফর্দ রেকর্ড",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "মোট সম্পন্ন পণ্য: ${BengaliUtils.toBanglaDigits(purchasedItems.size.toLong())} টি",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row {
                                    if (purchasedItems.isNotEmpty()) {
                                        // Re-add all to new fordi
                                        TextButton(
                                            onClick = {
                                                viewModel.reAddPurchasedItemsToFordi(purchasedItems) {
                                                    activeTab = FordiTab.CURRENT
                                                }
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = MawaPrimary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("সব ফর্দে নিন", color = MawaPrimary, fontWeight = FontWeight.Bold)
                                        }

                                        TextButton(
                                            onClick = { viewModel.clearCompletedFordi() }
                                        ) {
                                            Text("মুছুন", color = FinancialNegative)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (purchasedItems.isEmpty()) {
                        item {
                            MawaEmptyState(
                                icon = Icons.Default.History,
                                title = "পূর্বে সম্পন্ন কোনো ফর্দ নেই",
                                subtitle = "বর্তমান ফর্দ থেকে পণ্য কিনলে এখানে সংরক্ষণ থাকবে"
                            )
                        }
                    } else {
                        items(purchasedItems, key = { it.id }) { item ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = FinancialPositive,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = item.productName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${BengaliUtils.formatQuantity(if (item.actualQuantity > 0) item.actualQuantity else item.plannedQuantity, item.unit)} @ ${BengaliUtils.formatTaka(if (item.actualRate > 0) item.actualRate else item.purchaseRate)} • ${if (item.purchaseDate != null) BengaliUtils.formatTransactionTime(item.purchaseDate) else "পূর্বে কেনা"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = BengaliUtils.formatTaka(if (item.actualTotal > 0) item.actualTotal else (item.plannedQuantity * item.purchaseRate)),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.width(6.dp))

                                        // Button to re-add single item to current fordi
                                        IconButton(
                                            onClick = {
                                                viewModel.reAddPurchasedItemsToFordi(listOf(item)) {
                                                    activeTab = FordiTab.CURRENT
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AddShoppingCart,
                                                contentDescription = "ফর্দে যোগ করুন",
                                                tint = MawaPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
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
        }
    }

    // ADD FORDI ITEM DIALOG
    if (showAddDialog) {
        AddFordiItemDialog(
            products = activeProducts,
            onDismiss = { showAddDialog = false },
            onAdd = { name, prodId, qty, unit, purchaseRate, sellingRate ->
                viewModel.addFordiItem(
                    productName = name,
                    productId = prodId,
                    plannedQty = qty,
                    unit = unit,
                    purchaseRate = purchaseRate,
                    sellingRate = sellingRate
                )
                showAddDialog = false
            }
        )
    }

    // EDIT FORDI ITEM DIALOG (ক্রয় ও বিক্রয় দর এডিট)
    if (itemToEdit != null) {
        val item = itemToEdit!!
        EditFordiItemDialog(
            fordiItem = item,
            onDismiss = { itemToEdit = null },
            onSave = { updatedItem ->
                viewModel.updateFordiItem(updatedItem)
                itemToEdit = null
            },
            onDelete = {
                viewModel.deleteFordiItem(item.id)
                checkedItemIds = checkedItemIds - item.id
                itemToEdit = null
            }
        )
    }

    // NEW FORDI CONFIRMATION DIALOG
    if (showNewFordiConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showNewFordiConfirmDialog = false },
            title = {
                Text(text = "নতুন ফর্দ শুরু করবেন?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("বর্তমান ফর্দের অপূর্ণ পণ্যগুলো মুছে দিয়ে একটি সম্পূর্ণ নতুন ফর্দ তালিকা শুরু করবেন?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearPendingFordi {
                            checkedItemIds = emptySet()
                            showNewFordiConfirmDialog = false
                            showAddDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
                ) {
                    Text("হ্যাঁ, নতুন ফর্দ বানান")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFordiConfirmDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun AddFordiItemDialog(
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onAdd: (name: String, productId: Long?, qty: Double, unit: String, purchaseRate: Double, sellingRate: Double) -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var selectedProductId by remember { mutableStateOf<Long?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("কেজি") }
    var purchaseRate by remember { mutableStateOf("") }
    var sellingRate by remember { mutableStateOf("") }

    val matchedProducts = remember(productName, products) {
        if (productName.isBlank()) emptyList()
        else products.filter {
            it.name.contains(productName, ignoreCase = true) || it.banglaName.contains(productName)
        }.take(4)
    }

    val qtyNum = quantity.toDoubleOrNull() ?: 0.0
    val rateNum = purchaseRate.toDoubleOrNull() ?: 0.0
    val calculatedTotal = qtyNum * rateNum

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "ফর্দে পণ্য যোগ করুন", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Product Name input with autocomplete suggestions
                OutlinedTextField(
                    value = productName,
                    onValueChange = {
                        productName = it
                        selectedProductId = null
                    },
                    label = { Text("পণ্যের নাম *") },
                    placeholder = { Text("যেমন: চিনি, সয়াবিন তেল, মসুর ডাল") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fordi_product_name_input")
                )

                // Autocomplete suggestion chips
                if (matchedProducts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        matchedProducts.forEach { p ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        productName = p.name
                                        selectedProductId = p.id
                                        unit = p.unit
                                        purchaseRate = if (p.defaultPurchasePrice > 0) p.defaultPurchasePrice.toInt().toString() else ""
                                        sellingRate = if (p.defaultSellingPrice > 0) p.defaultSellingPrice.toInt().toString() else ""
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = p.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MawaPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quantity & Unit Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) quantity = it },
                        label = { Text("পরিমাণ *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("একক") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Purchase Rate & Selling Rate Row (ক্রয় ও বিক্রয় দর)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = purchaseRate,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) purchaseRate = it },
                        label = { Text("ক্রয় দর (৳)") },
                        placeholder = { Text("৳") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = sellingRate,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) sellingRate = it },
                        label = { Text("বিক্রয় দর (৳)") },
                        placeholder = { Text("৳") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Live Calculated Total Display
                if (calculatedTotal > 0) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "লাইন মোট:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = BengaliUtils.formatTaka(calculatedTotal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MawaPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (productName.isNotBlank() && qtyNum > 0) {
                        val pRate = purchaseRate.toDoubleOrNull() ?: 0.0
                        val sRate = sellingRate.toDoubleOrNull() ?: 0.0
                        onAdd(productName.trim(), selectedProductId, qtyNum, unit.trim(), pRate, sRate)
                    }
                },
                enabled = productName.isNotBlank() && qtyNum > 0,
                colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
            ) {
                Text("ফর্দে যোগ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

@Composable
fun EditFordiItemDialog(
    fordiItem: FordiItemEntity,
    onDismiss: () -> Unit,
    onSave: (FordiItemEntity) -> Unit,
    onDelete: () -> Unit
) {
    var productName by remember { mutableStateOf(fordiItem.productName) }
    var quantity by remember { mutableStateOf(fordiItem.plannedQuantity.toString().removeSuffix(".0")) }
    var unit by remember { mutableStateOf(fordiItem.unit) }
    var purchaseRate by remember { mutableStateOf(if (fordiItem.purchaseRate > 0) fordiItem.purchaseRate.toString().removeSuffix(".0") else "") }
    var sellingRate by remember { mutableStateOf(if (fordiItem.sellingRate > 0) fordiItem.sellingRate.toString().removeSuffix(".0") else "") }

    val qtyNum = quantity.toDoubleOrNull() ?: 0.0
    val pRateNum = purchaseRate.toDoubleOrNull() ?: 0.0
    val sRateNum = sellingRate.toDoubleOrNull() ?: 0.0
    val calculatedTotal = qtyNum * pRateNum

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "পণ্য ও দর এডিট করুন", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("পণ্যের নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) quantity = it },
                        label = { Text("পরিমাণ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("একক") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Purchase Rate & Selling Rate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = purchaseRate,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) purchaseRate = it },
                        label = { Text("ক্রয় দর (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = sellingRate,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) sellingRate = it },
                        label = { Text("বিক্রয় দর (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "লাইন মোট:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = BengaliUtils.formatTaka(calculatedTotal),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MawaPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = FinancialNegative, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ফর্দ থেকে মুছুন", color = FinancialNegative)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (productName.isNotBlank() && qtyNum > 0) {
                        onSave(
                            fordiItem.copy(
                                productName = productName.trim(),
                                plannedQuantity = qtyNum,
                                unit = unit.trim(),
                                purchaseRate = pRateNum,
                                sellingRate = sRateNum
                            )
                        )
                    }
                },
                enabled = productName.isNotBlank() && qtyNum > 0,
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
