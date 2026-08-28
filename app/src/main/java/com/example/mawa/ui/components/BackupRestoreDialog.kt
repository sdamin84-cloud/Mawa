package com.example.mawa.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.local.entity.TransactionType
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.DataBackupRestoreManager
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialNegativeContainer
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialPositiveContainer
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.MawaPrimary
import com.example.ui.theme.MawaPrimaryContainer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class BackupTab(val title: String) {
    BACKUP("ডাটা ব্যাকআপ"),
    RESTORE("ডাটা রিস্টোর"),
    MEMO_EXPORT("মেমো ও ছবি (PNG)")
}

@Composable
fun BackupRestoreDialog(
    viewModel: MawaViewModel,
    onDismiss: () -> Unit,
    onOpenSupabaseCloud: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(BackupTab.BACKUP) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }

    val customersWithBalance by viewModel.customersWithBalance.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val fordiItems by viewModel.allFordiItems.collectAsStateWithLifecycle()
    val shopSettings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val shopName = shopSettings?.shopName?.ifBlank { "মাওয়া স্মার্ট খাতা" } ?: "মাওয়া স্মার্ট খাতা"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MawaPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = null,
                                tint = MawaPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ডাটা ব্যাকআপ ও রিস্টোর",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "JSON, CSV ও PNG ছবি আকারে সংরক্ষণ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: [ ডাটা ব্যাকআপ ] | [ ডাটা রিস্টোর ] | [ মেমো ও ছবি ]
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MawaPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = MawaPrimary,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    BackupTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier.testTag("backup_tab_${tab.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Contents
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        BackupTab.BACKUP -> {
                            // --- TAB 1: BACKUP OPTIONS ---
                            // Supabase Cloud Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDismiss()
                                        onOpenSupabaseCloud()
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF3ECF8E).copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudUpload,
                                                contentDescription = null,
                                                tint = Color(0xFF047857),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "সুপাবেজ ক্লাউড ব্যাকআপ ☁️",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF047857)
                                            )
                                            Text(
                                                text = if (currentUser != null) "লগইন আছে: ${currentUser?.email}" else "অনলাইনে ক্লাউডে ব্যাকআপ রাখতে লগইন করুন",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            onDismiss()
                                            onOpenSupabaseCloud()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("ক্লাউড খুলুন ➔", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            BackupOptionCard(
                                icon = Icons.Default.CloudDownload,
                                iconTint = MawaPrimary,
                                iconBg = MawaPrimaryContainer,
                                title = "সম্পূর্ণ ডাটা JSON ব্যাকআপ",
                                subtitle = "দোকানের সকল খতিয়ান, হিসাব, ফর্দ ও সেটিংস একটি ফাইলে সংরক্ষণ করুন",
                                actionLabel = "JSON ডাউনলোড ও শেয়ার",
                                onAction = {
                                    scope.launch {
                                        isProcessing = true
                                        val json = viewModel.exportFullBackupJson()
                                        val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                                        DataBackupRestoreManager.shareTextFile(
                                            context = context,
                                            content = json,
                                            fileName = "mawa_backup_$dateStr.json",
                                            mimeType = "application/json",
                                            title = "MAWA ডিজিটাল খাতা সম্পূর্ণ ব্যাকআপ ফাইল"
                                        )
                                        isProcessing = false
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            BackupOptionCard(
                                icon = Icons.Default.TableChart,
                                iconTint = FinancialPositive,
                                iconBg = FinancialPositiveContainer,
                                title = "কাস্টমার তালিকা CSV / Excel এক্সপোর্ট",
                                subtitle = "সকল গ্রাহকের নাম, মোবাইল, পূর্বের বাকি ও বর্তমান বাকি এক্সেল ফাইলে নিন",
                                actionLabel = "CSV ডাউনলোড ও শেয়ার",
                                onAction = {
                                    val csv = DataBackupRestoreManager.generateCustomersCsv(customersWithBalance)
                                    val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                                    DataBackupRestoreManager.shareTextFile(
                                        context = context,
                                        content = csv,
                                        fileName = "customers_$dateStr.csv",
                                        mimeType = "text/csv",
                                        title = "কাস্টমার বাকি খতিয়ান তালিকা CSV"
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            BackupOptionCard(
                                icon = Icons.Default.Receipt,
                                iconTint = FinancialWarning,
                                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                                title = "বাজারের ফর্দ তালিকা CSV এক্সপোর্ট",
                                subtitle = "দোকানের বর্তমান কেনাকাটার ফর্দ ও দরদাম এক্সেল ফাইলে সংরক্ষণ করুন",
                                actionLabel = "ফর্দ CSV শেয়ার",
                                onAction = {
                                    val csv = DataBackupRestoreManager.generateFordiCsv(fordiItems)
                                    val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                                    DataBackupRestoreManager.shareTextFile(
                                        context = context,
                                        content = csv,
                                        fileName = "fordi_list_$dateStr.csv",
                                        mimeType = "text/csv",
                                        title = "দোকানের বাজার ফর্দ তালিকা CSV"
                                    )
                                }
                            )
                        }

                        BackupTab.RESTORE -> {
                            // --- TAB 2: RESTORE OPTIONS ---
                            // Supabase Cloud Restore Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDismiss()
                                        onOpenSupabaseCloud()
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF3ECF8E).copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudDownload,
                                                contentDescription = null,
                                                tint = Color(0xFF047857),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "সুপাবেজ ক্লাউড থেকে রিস্টোর ☁️",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF047857)
                                            )
                                            Text(
                                                text = "অনলাইন ডাটাবেজে সংরক্ষিত ব্যাকআপ তালিকা থেকে ১-ক্লিকে রিস্টোর করুন",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            onDismiss()
                                            onOpenSupabaseCloud()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("ক্লাউড দেখুন ➔", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = FinancialWarning.copy(alpha = 0.1f)),
                                border = BorderStroke(1.dp, FinancialWarning.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = FinancialWarning,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "সতর্কতা: রিস্টোর করলে ব্যাকআপ ফাইলের ডাটা দিয়ে বর্তমান হিসাব প্রতিস্থাপিত হবে।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "JSON ব্যাকআপ টেক্সট বা ফাইল পেস্ট করুন:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = restoreJsonInput,
                                onValueChange = { restoreJsonInput = it },
                                placeholder = { Text("এখানে ব্যাকআপের JSON টেক্সট পেস্ট করুন...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .testTag("input_restore_json"),
                                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MawaPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val clipText = clipboardManager.getText()?.text ?: ""
                                        if (clipText.isNotBlank()) {
                                            restoreJsonInput = clipText
                                            Toast.makeText(context, "ক্লিপবোর্ড থেকে পেস্ট হয়েছে", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "ক্লিপবোর্ডে কোনো ডাটা নেই", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("পেস্ট করুন")
                                }

                                Button(
                                    onClick = {
                                        if (restoreJsonInput.isBlank()) {
                                            Toast.makeText(context, "অনুগ্রহ করে JSON ব্যাকআপ টেক্সট দিন", Toast.LENGTH_SHORT).show()
                                        } else {
                                            showRestoreConfirmDialog = true
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_trigger_restore"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = FinancialNegative)
                                ) {
                                    Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("রিস্টোর করুন", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        BackupTab.MEMO_EXPORT -> {
                            // --- TAB 3: MODERN MEMO & BRANDED IMAGE GENERATION ---
                            MemoExportOptionCard(
                                title = "আজকের ক্যাশবক্স হিসাব রসিদ (PNG)",
                                subtitle = "দৈনিক নগদ বেচা, বাকি আদায় ও খরচের সুন্দর ভাউচার ছবি",
                                onGenerateAndShare = {
                                    val cal = java.util.Calendar.getInstance().apply {
                                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                                        set(java.util.Calendar.MINUTE, 0)
                                        set(java.util.Calendar.SECOND, 0)
                                        set(java.util.Calendar.MILLISECOND, 0)
                                    }
                                    val startOfDay = cal.timeInMillis
                                    cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                                    cal.set(java.util.Calendar.MINUTE, 59)
                                    cal.set(java.util.Calendar.SECOND, 59)
                                    cal.set(java.util.Calendar.MILLISECOND, 999)
                                    val endOfDay = cal.timeInMillis
                                    val todayTx = allTransactions.filter { it.timestamp in startOfDay..endOfDay }

                                    val openingCash = shopSettings?.openingBalance ?: 0.0
                                    val cashSales = todayTx.filter { it.type == TransactionType.SALE_CASH }.sumOf { it.amount }
                                    val bakiColl = todayTx.filter { it.type == TransactionType.BAKI_COLLECTION }.sumOf { it.amount }
                                    val ownerDep = todayTx.filter { it.type == TransactionType.CASH_ADJUSTMENT && it.amount > 0 }.sumOf { it.amount }
                                    val purchases = todayTx.filter { it.type == TransactionType.PURCHASE_FORDI || it.type == TransactionType.PURCHASE_DIRECT }.sumOf { it.amount }
                                    val expenses = todayTx.filter { it.type == TransactionType.EXPENSE_SHOP }.sumOf { it.amount }
                                    val ownerWith = todayTx.filter { it.type == TransactionType.EXPENSE_HOME }.sumOf { it.amount }
                                    val closing = (openingCash + cashSales + bakiColl + ownerDep) - (purchases + expenses + ownerWith)

                                    val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale("bn", "BD")).format(Date())

                                    val bitmap = DataBackupRestoreManager.createDailyCashboxMemoBitmap(
                                        shopName = shopName,
                                        dateLabel = dateStr,
                                        openingCash = openingCash,
                                        cashSales = cashSales,
                                        bakiCollection = bakiColl,
                                        ownerDeposit = ownerDep,
                                        purchases = purchases,
                                        expenses = expenses,
                                        ownerWithdrawal = ownerWith,
                                        closingBalance = closing
                                    )

                                    DataBackupRestoreManager.shareBitmapAsImage(
                                        context = context,
                                        bitmap = bitmap,
                                        fileNamePrefix = "daily_cashbox_memo",
                                        title = "$dateStr - ক্যাশবক্স রসিদ"
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            MemoExportOptionCard(
                                title = "দোকানের বাজার ফর্দ রসিদ (PNG)",
                                subtitle = "দোকানের সকল কেনাকাটার পণ্যের আইটেমাইজড সুন্দর ফর্দ কার্ড",
                                onGenerateAndShare = {
                                    val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale("bn", "BD")).format(Date())
                                    val bitmap = DataBackupRestoreManager.createFordiMemoBitmap(
                                        shopName = shopName,
                                        dateLabel = dateStr,
                                        fordiItems = fordiItems
                                    )
                                    DataBackupRestoreManager.shareBitmapAsImage(
                                        context = context,
                                        bitmap = bitmap,
                                        fileNamePrefix = "fordi_memo",
                                        title = "$dateStr - বাজারের ফর্দ তালিকা"
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            MemoExportOptionCard(
                                title = "লাভ-ক্ষতি ও ব্যবসা স্টেটমেন্ট (PNG)",
                                subtitle = "মোট বিক্রি, মাল ক্রয়, খরচ ও নিট লাভের ব্র্যান্ডেড মেমো ছবি",
                                onGenerateAndShare = {
                                    val sales = allTransactions.filter { it.type == TransactionType.SALE_CASH || it.type == TransactionType.SALE_BAKI }.sumOf { it.amount }
                                    val purchases = allTransactions.filter { it.type == TransactionType.PURCHASE_FORDI || it.type == TransactionType.PURCHASE_DIRECT }.sumOf { it.amount }
                                    val expenses = allTransactions.filter { it.type == TransactionType.EXPENSE_SHOP }.sumOf { it.amount }
                                    val home = allTransactions.filter { it.type == TransactionType.EXPENSE_HOME }.sumOf { it.amount }
                                    val netProfit = sales - purchases - expenses
                                    val rem = netProfit - home

                                    val dateStr = SimpleDateFormat("MMMM yyyy", Locale("bn", "BD")).format(Date())

                                    val bitmap = DataBackupRestoreManager.createMonthlyReportMemoBitmap(
                                        shopName = shopName,
                                        periodLabel = dateStr,
                                        totalSales = sales,
                                        purchases = purchases,
                                        shopExpenses = expenses,
                                        netProfit = netProfit,
                                        homeWithdrawals = home,
                                        profitRemaining = rem
                                    )

                                    DataBackupRestoreManager.shareBitmapAsImage(
                                        context = context,
                                        bitmap = bitmap,
                                        fileNamePrefix = "profit_loss_memo",
                                        title = "$dateStr - লাভ ক্ষতি স্টেটমেন্ট"
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("বন্ধ করুন", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Confirmation Alert before restoring
    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = FinancialNegative,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "আপনি কি নিশ্চিতভাবে রিস্টোর করতে চান?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "রিস্টোর সম্পন্ন হলে বর্তমান ডাটাবেজ মুছে গিয়ে ব্যাকআপ ফাইলের সকল কাস্টমার, হিসাব, লেনদেন ও ফর্দ প্রতিস্থাপিত হবে।",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            showRestoreConfirmDialog = false
                            val success = viewModel.restoreFullBackupFromJson(restoreJsonInput, overwriteExisting = true)
                            if (success) {
                                restoreJsonInput = ""
                                onDismiss()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialNegative)
                ) {
                    Text("হ্যাঁ, রিস্টোর নিশ্চিত করুন", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun BackupOptionCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = iconTint)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = actionLabel, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun MemoExportOptionCard(
    title: String,
    subtitle: String,
    onGenerateAndShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onGenerateAndShare,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
            ) {
                Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("শেয়ার", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            }
        }
    }
}
