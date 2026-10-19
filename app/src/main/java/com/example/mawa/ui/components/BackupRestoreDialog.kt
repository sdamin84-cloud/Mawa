package com.example.mawa.ui.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.local.entity.TransactionType
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.DataBackupRestoreManager
import com.example.mawa.util.FullBackupData
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
    BACKUP("ব্যাকআপ ও সেভ"),
    RESTORE("ডাটা রিস্টোর"),
    CLOUD_SYNC("ক্লাউড সিঙ্ক ☁️"),
    MEMO_EXPORT("মেমো (PNG)")
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
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var restoreWithOverwrite by remember { mutableStateOf(true) }
    var showClearDataConfirmDialog by remember { mutableStateOf(false) }
    var restoreSuccessMessage by remember { mutableStateOf<String?>(null) }

    var isProcessing by remember { mutableStateOf(false) }

    // Cloud Auth Form States for CLOUD_SYNC tab
    var authEmail by remember { mutableStateOf("") }
    var authPassword by remember { mutableStateOf("") }
    var authName by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showSqlGuide by remember { mutableStateOf(true) }

    val customersWithBalance by viewModel.customersWithBalance.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val fordiItems by viewModel.allFordiItems.collectAsStateWithLifecycle()
    val shopSettings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()
    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val cloudBackups by viewModel.cloudBackups.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastCloudSyncTime.collectAsStateWithLifecycle()

    val shopName = shopSettings?.shopName?.ifBlank { "মাওয়া স্মার্ট খাতা" } ?: "মাওয়া স্মার্ট খাতা"

    // Parse preview of loaded JSON
    val parsedBackupPreview by remember(restoreJsonInput) {
        derivedStateOf {
            if (restoreJsonInput.isNotBlank()) {
                viewModel.parseBackupPreview(restoreJsonInput)
            } else {
                null
            }
        }
    }

    // Direct JSON File Save Launcher (Saves full JSON backup directly into user's mobile device storage)
    val jsonSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    isProcessing = true
                    val json = viewModel.exportFullBackupJson()
                    val success = DataBackupRestoreManager.writeTextToUri(context, uri, json)
                    if (success) {
                        Toast.makeText(context, "✅ সম্পূর্ণ ডাটার JSON ব্যাকআপ ফাইলটি মোবাইলে সফলভাবে ডাউনলোড ও সেভ হয়েছে!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "❌ ফাইল সেভ করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "ত্রুটি: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    // Direct Customers CSV Save Launcher
    val csvCustomerSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val csv = DataBackupRestoreManager.generateCustomersCsv(customersWithBalance)
                val success = DataBackupRestoreManager.writeTextToUri(context, uri, csv)
                if (success) {
                    Toast.makeText(context, "✅ কাস্টমার তালিকা CSV ফাইলটি ডিভাইসে সেভ হয়েছে!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "❌ ফাইল সেভ ব্যর্থ হয়েছে", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "ত্রুটি: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Direct Fordi CSV Save Launcher
    val csvFordiSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val csv = DataBackupRestoreManager.generateFordiCsv(fordiItems)
                val success = DataBackupRestoreManager.writeTextToUri(context, uri, csv)
                if (success) {
                    Toast.makeText(context, "✅ বাজার ফর্দ CSV ফাইলটি ডিভাইসে সেভ হয়েছে!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "❌ ফাইল সেভ ব্যর্থ হয়েছে", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "ত্রুটি: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // File Picker for JSON Backup
    val jsonFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                isProcessing = true
                val content = DataBackupRestoreManager.readTextFromUri(context, uri)
                if (content.isNotBlank()) {
                    restoreJsonInput = content
                    selectedFileName = uri.lastPathSegment ?: "backup.json"
                    Toast.makeText(context, "ব্যাকআপ ফাইল লোড হয়েছে!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "নির্বাচিত ফাইলটি ফাঁকা", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "ফাইল পড়তে সমস্যা: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                isProcessing = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
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
                                text = "JSON ফাইল আপলোড, রিস্টোর ও এক্সপোর্ট",
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

                // Tabs: [ ডাটা রিস্টোর ] | [ ডাটা ব্যাকআপ ] | [ মেমো ও ছবি ]
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

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Contents
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        BackupTab.RESTORE -> {
                            // --- TAB 1: RESTORE OPTIONS ---

                            // 1. JSON File Upload Card (PROMINENT BUTTON)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        jsonFilePickerLauncher.launch("*/*")
                                    },
                                colors = CardDefaults.cardColors(containerColor = MawaPrimaryContainer.copy(alpha = 0.45f)),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.5.dp, MawaPrimary)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(MawaPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FolderOpen,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "📁 JSON ফাইল আপলোড করুন",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (selectedFileName != null) "লোড হয়েছে: $selectedFileName" else "মোবাইল স্টোরেজ বা ডাউনলোড ফোল্ডার থেকে ফাইল বাছুন",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            jsonFilePickerLauncher.launch("*/*")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ফাইল বাছুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 2. Supabase Cloud Restore Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDismiss()
                                        onOpenSupabaseCloud()
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF3ECF8E).copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.2.dp, Color(0xFF10B981).copy(alpha = 0.5f))
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
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudDownload,
                                                contentDescription = null,
                                                tint = Color(0xFF047857),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "সুপাবেজ ক্লাউড থেকে রিস্টোর ☁️",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF047857)
                                            )
                                            Text(
                                                text = "অনলাইন ডাটাবেজে সংরক্ষিত ব্যাকআপ দেখুন",
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

                            // 3. BACKUP PREVIEW CARD (If JSON loaded)
                            if (parsedBackupPreview != null) {
                                val preview = parsedBackupPreview!!
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = FinancialPositiveContainer.copy(alpha = 0.5f)),
                                    border = BorderStroke(1.5.dp, FinancialPositive)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = FinancialPositive, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "সঠিক ব্যাকআপ ফাইল প্রস্তুত!",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = FinancialPositive
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))

                                        preview.shopSettings?.shopName?.let {
                                            Text("🏪 দোকান: $it", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("👥 কাস্টমার: ${preview.customers.size} জন", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            Text("📝 লেনদেন: ${preview.transactions.size} টি", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("🛒 বাজার ফর্দ: ${preview.fordiItems.size} টি", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            Text("📦 পণ্য তালিকা: ${preview.products.size} টি", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Restore Actions
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    restoreWithOverwrite = true
                                                    showRestoreConfirmDialog = true
                                                },
                                                modifier = Modifier.weight(1.2f),
                                                colors = ButtonDefaults.buttonColors(containerColor = FinancialNegative),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("মুছে রিস্টোর", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    restoreWithOverwrite = false
                                                    showRestoreConfirmDialog = true
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("যুক্ত করুন", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // 4. JSON Text Input Box
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "অথবা JSON টেক্সট পেস্ট করুন:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                TextButton(
                                    onClick = {
                                        val clipText = clipboardManager.getText()?.text ?: ""
                                        if (clipText.isNotBlank()) {
                                            restoreJsonInput = clipText
                                            Toast.makeText(context, "ক্লিপবোর্ড থেকে পেস্ট হয়েছে", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "ক্লিপবোর্ডে কোনো ডাটা নেই", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ক্লিপবোর্ড পেস্ট", fontSize = 12.sp)
                                }
                            }

                            OutlinedTextField(
                                value = restoreJsonInput,
                                onValueChange = { 
                                    restoreJsonInput = it 
                                    selectedFileName = null
                                },
                                placeholder = { Text("এখানে ব্যাকআপের সম্পূর্ণ JSON টেক্সট পেস্ট করতে পারেন...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .testTag("input_restore_json"),
                                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MawaPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (parsedBackupPreview == null && restoreJsonInput.isNotBlank()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = FinancialNegativeContainer.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "⚠️ টেক্সটটি সঠিক JSON ব্যাকআপ ফরম্যাটে নেই। অনুগ্রহ করে ফাইল আপলোড করুন অথবা সম্পূর্ণ জেসন টেক্সট দিন।",
                                        modifier = Modifier.padding(10.dp),
                                        fontSize = 12.sp,
                                        color = FinancialNegative
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            if (parsedBackupPreview == null) {
                                Button(
                                    onClick = {
                                        if (restoreJsonInput.isBlank()) {
                                            Toast.makeText(context, "অনুগ্রহ করে JSON ব্যাকআপ ফাইল বাছুন বা টেক্সট দিন", Toast.LENGTH_SHORT).show()
                                        } else {
                                            restoreWithOverwrite = true
                                            showRestoreConfirmDialog = true
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_trigger_restore"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = FinancialNegative),
                                    enabled = restoreJsonInput.isNotBlank()
                                ) {
                                    Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("রিস্টোর প্রক্রিয়া শুরু করুন", fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(14.dp))

                            // 5. WIPE SAMPLE / DUMMY DATA CARD
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteSweep,
                                            contentDescription = null,
                                            tint = FinancialNegative,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "স্যাম্পল / ডামি ডাটা মুছুন",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "সকল পরীক্ষামূলক ডামি কাস্টমার ও হিসাব খালি করুন",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            showClearDataConfirmDialog = true
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FinancialNegative),
                                        border = BorderStroke(1.dp, FinancialNegative.copy(alpha = 0.6f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("ডাটা মুছুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        BackupTab.BACKUP -> {
                            // --- TAB 2: DATA BACKUP & DIRECT DOWNLOAD OPTIONS ---

                            // 1. Hero Card: Direct JSON Full Backup Download to Mobile Storage
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                                border = BorderStroke(1.dp, MawaPrimary.copy(alpha = 0.35f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MawaPrimaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.SaveAlt, contentDescription = null, tint = MawaPrimary, modifier = Modifier.size(24.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "সম্পূর্ণ ডাটার JSON ব্যাকআপ ফাইল",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "দোকানের সকল বাকি, ক্যাশ হিসাব, ফর্দ ও সেটিংস মোবাইলের ফাইলে সেভ করুন।",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Option A: Master Backup File (Updates the same master file so user doesn't get endless cluttered JSON files)
                                    Button(
                                        onClick = {
                                            jsonSaveLauncher.launch("mawa_master_backup.json")
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("⭐ মাস্টার ব্যাকআপ ফাইল আপডেট করুন (সুপারিশকৃত)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }

                                    Text(
                                        text = "💡 এটি একই মাস্টার ফাইল আপডেট করে, ফলে মোবাইলের ফাইলে প্রতিবার নতুন করে জেসন জমে clutter বা বিশৃঙ্খলা হবে না।",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp, start = 4.dp, bottom = 8.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                                                jsonSaveLauncher.launch("mawa_backup_$dateStr.json")
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("তারিখসহ কপি", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                scope.launch {
                                                    isProcessing = true
                                                    val json = viewModel.exportFullBackupJson()
                                                    DataBackupRestoreManager.shareTextFile(
                                                        context = context,
                                                        content = json,
                                                        fileName = "mawa_master_backup.json",
                                                        mimeType = "application/json",
                                                        title = "MAWA ডিজিটাল খাতা সম্পূর্ণ ব্যাকআপ ফাইল"
                                                    )
                                                    isProcessing = false
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("শেয়ার করুন", fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Deduplication Protection Badge
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.12f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "স্মার্ট ডুপ্লিকেট রোধ সক্রিয়: জেসন ফাইল আপলোড করার পর ক্লাউড লগইন করলেও ডাটা ডাবল হবে না, স্বয়ংক্রিয়ভাবে নির্ভুল মার্জ হবে।",
                                                fontSize = 11.sp,
                                                color = Color(0xFF047857),
                                                fontWeight = FontWeight.Medium,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 2. Customers List CSV / Excel Export
                            BackupOptionCard(
                                icon = Icons.Default.TableChart,
                                iconTint = FinancialPositive,
                                iconBg = FinancialPositiveContainer,
                                title = "কাস্টমার বাকি তালিকা CSV / Excel",
                                subtitle = "সকল গ্রাহকের নাম, মোবাইল, পূর্বের বাকি ও বর্তমান বাকি এক্সেল বা সিএসভি ফাইলে নিন।",
                                primaryActionLabel = "📥 মোবাইলে CSV সেভ",
                                primaryActionIcon = Icons.Default.Download,
                                onPrimaryAction = {
                                    val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                                    csvCustomerSaveLauncher.launch("customers_$dateStr.csv")
                                },
                                secondaryActionLabel = "📤 শেয়ার",
                                onSecondaryAction = {
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

                            // 3. Fordi Items CSV Export
                            BackupOptionCard(
                                icon = Icons.Default.Receipt,
                                iconTint = FinancialWarning,
                                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                                title = "বাজারের ফর্দ তালিকা CSV এক্সপোর্ট",
                                subtitle = "দোকানের বর্তমান কেনাকাটার ফর্দ ও দরদাম এক্সেল ফাইলে সংরক্ষণ করুন।",
                                primaryActionLabel = "📥 ফর্দ CSV সেভ",
                                primaryActionIcon = Icons.Default.Download,
                                onPrimaryAction = {
                                    val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                                    csvFordiSaveLauncher.launch("fordi_list_$dateStr.csv")
                                },
                                secondaryActionLabel = "📤 শেয়ার",
                                onSecondaryAction = {
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

                            Spacer(modifier = Modifier.height(12.dp))

                            // 4. Quick Supabase Cloud Sync Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTab = BackupTab.CLOUD_SYNC
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF3ECF8E).copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.6f))
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
                                                text = "অনলাইন ক্লাউড লাইভ ব্যাকআপ ☁️",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF047857)
                                            )
                                            Text(
                                                text = if (currentUser != null) "লগইন আছে: ${currentUser?.email} (১-ট্যাপে ক্লাউড সিঙ্ক)" else "সার্ভারে লাইভ ক্লাউড ব্যাকআপ রাখতে একাউন্ট খুলুন",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { selectedTab = BackupTab.CLOUD_SYNC },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("সিঙ্ক হাব ➔", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        BackupTab.CLOUD_SYNC -> {
                            // --- TAB 3: UNIFIED SUPABASE CLOUD & AUTH HUB ---
                            if (currentUser != null) {
                                // Logged In User Card
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3ECF8E).copy(alpha = 0.12f)),
                                    border = BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.6f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(24.dp))
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = currentUser?.displayName?.ifBlank { "ব্যবহারকারী" } ?: "ব্যবসার মালিক",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF047857)
                                                    )
                                                    Text(
                                                        text = currentUser?.email ?: "",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = { viewModel.logoutSupabase() },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = FinancialNegative)
                                            ) {
                                                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("লগআউট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        HorizontalDivider(color = Color(0xFF10B981).copy(alpha = 0.3f))
                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (!lastSyncTime.isNullOrBlank()) "সর্বশেষ ক্লাউড সিঙ্ক: $lastSyncTime" else "এখনো ক্লাউডে সিঙ্ক করা হয়নি",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Big 1-Click Cloud Sync Button
                                        Button(
                                            onClick = { viewModel.syncAndRestoreFromCloud() },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            enabled = !isCloudSyncing
                                        ) {
                                            if (isCloudSyncing) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("ক্লাউডে সিঙ্ক হচ্ছে...", fontWeight = FontWeight.Bold, color = Color.White)
                                            } else {
                                                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("১-ট্যাপে সম্পূর্ণ ক্লাউড সিঙ্ক ও ব্যাকআপ করুন", fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // --- Supabase Realtime Live Sync & SQL Setup Guide Card ---
                                val supabaseSqlScript = """
-- ১. mawa_cloud_records টেবিল তৈরি (ফিল্ডভিত্তিক তাৎক্ষণিক লাইভ সিঙ্ক ও ডিলিটের জন্য)
CREATE TABLE IF NOT EXISTS public.mawa_cloud_records (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    domain TEXT NOT NULL DEFAULT 'business',
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    data JSONB NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT unique_user_domain_entity UNIQUE (user_id, domain, entity_type, entity_id)
);

-- ২. user_backups টেবিল তৈরি (সম্পূর্ণ ডাটাবেজ স্ন্যাপশট ব্যাকআপের জন্য)
CREATE TABLE IF NOT EXISTS public.user_backups (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    backup_name TEXT NOT NULL,
    backup_data JSONB NOT NULL,
    data JSONB NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT unique_user_backup_name UNIQUE (user_id, backup_name)
);

-- ৩. রিয়েলটাইম লাইভ ট্র্যাকিং সক্রিয় করুন (Realtime Live Sync)
ALTER PUBLICATION supabase_realtime ADD TABLE public.mawa_cloud_records;
ALTER PUBLICATION supabase_realtime ADD TABLE public.user_backups;
ALTER TABLE public.mawa_cloud_records REPLICA IDENTITY FULL;
ALTER TABLE public.user_backups REPLICA IDENTITY FULL;

-- ৪. ডাটা সিকিউরিটি পলিসি (Row Level Security - RLS)
ALTER TABLE public.mawa_cloud_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_backups ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can manage their records" ON public.mawa_cloud_records;
CREATE POLICY "Users can manage their records" 
ON public.mawa_cloud_records FOR ALL 
USING (auth.uid() = user_id) 
WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can manage their backups" ON public.user_backups;
CREATE POLICY "Users can manage their backups" 
ON public.user_backups FOR ALL 
USING (auth.uid() = user_id) 
WITH CHECK (auth.uid() = user_id);
                                """.trimIndent()

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "সুপাবেজে লাইভ রিয়েলটাইম করতে করণীয়",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF047857)
                                                )
                                            }
                                            TextButton(onClick = { showSqlGuide = !showSqlGuide }) {
                                                Text(if (showSqlGuide) "লুকান" else "দেখুন", fontSize = 12.sp)
                                            }
                                        }

                                        if (showSqlGuide) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "১. সুপাবেজে লিখার সাথে সাথে লাইভ করতে এবং ডুপ্লিকেট রোধ করতে Supabase Dashboard -> SQL Editor-এ নিচের স্ক্রিপ্টটি রান করুন:\n২. অ্যাপে ফিল্ড বা এন্ট্রি যোগ করলে ৫০০ms-এ সার্ভারে যাবে এবং মুছে ফেললে সাথে সাথে সার্ভার থেকেও মুছে যাবে।",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 16.sp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))

                                            Surface(
                                                color = Color(0xFF1E293B),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = supabaseSqlScript,
                                                    color = Color(0xFFE2E8F0),
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 10.sp,
                                                    lineHeight = 14.sp,
                                                    modifier = Modifier.padding(10.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(supabaseSqlScript))
                                                    Toast.makeText(context, "✅ সুপাবেজ SQL কোড ক্লিপবোর্ডে কপি হয়েছে!", Toast.LENGTH_LONG).show()
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("📋 SQL কোড কপি করুন", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Remote Cloud Backups List
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "অনলাইন ব্যাকআপ তালিকা (${cloudBackups.size} টি)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { viewModel.loadCloudBackups() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "রিফ্রেশ করুন", tint = MawaPrimary)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (cloudBackups.isEmpty()) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = "কোনো ক্লাউড ব্যাকআপ পাওয়া যায়নি। উপরের 'ক্লাউড সিঙ্ক' বাটনে ট্যাপ করে ব্যাকআপ তৈরি করুন।",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                } else {
                                    cloudBackups.forEach { backupItem ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "📦 ${backupItem.backupName}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "📅 ${backupItem.updatedAt.ifBlank { "সংরক্ষিত" }}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Button(
                                                        onClick = { viewModel.restoreCloudBackup(backupItem) { _, _ -> } },
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
                                                    ) {
                                                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("রিস্টোর", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }

                                                    IconButton(
                                                        onClick = { viewModel.deleteCloudBackup(backupItem.id) { _, _ -> } },
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = FinancialNegative)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Not Logged In - Quick Auth Form
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(MawaPrimaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = MawaPrimary, modifier = Modifier.size(20.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = if (isRegisterMode) "নতুন ক্লাউড একাউন্ট খুলুন" else "সুপাবেজ ক্লাউডে লগইন করুন",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "অনলাইন ডাটাবেজে স্বয়ংক্রিয় ব্যাকআপ ও যেকোনো ডিভাইস থেকে এক্সেসের সুবিধা",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        if (isRegisterMode) {
                                            OutlinedTextField(
                                                value = authName,
                                                onValueChange = { authName = it },
                                                label = { Text("দোকানের নাম / আপনার নাম") },
                                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }

                                        OutlinedTextField(
                                            value = authEmail,
                                            onValueChange = { authEmail = it },
                                            label = { Text("ইমেইল অ্যাড্রেস") },
                                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        OutlinedTextField(
                                            value = authPassword,
                                            onValueChange = { authPassword = it },
                                            label = { Text("পাসওয়ার্ড (কমপক্ষে ৬ অক্ষর)") },
                                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                            trailingIcon = {
                                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                    Icon(
                                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                        contentDescription = null
                                                    )
                                                }
                                            },
                                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Button(
                                            onClick = {
                                                if (authEmail.isBlank() || authPassword.isBlank()) {
                                                    Toast.makeText(context, "ইমেইল ও পাসওয়ার্ড প্রদান করুন", Toast.LENGTH_SHORT).show()
                                                    return@Button
                                                }
                                                if (isRegisterMode) {
                                                    viewModel.registerWithSupabase(authEmail.trim(), authPassword.trim(), authName.trim()) { _, _ -> }
                                                } else {
                                                    viewModel.loginWithSupabase(authEmail.trim(), authPassword.trim()) { _, _ -> }
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            enabled = !isAuthLoading
                                        ) {
                                            if (isAuthLoading) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                            } else {
                                                Text(
                                                    text = if (isRegisterMode) "একাউন্ট তৈরি করুন" else "লগইন করুন",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                                                Text(if (isRegisterMode) "আগের একাউন্ট আছে? লগইন করুন" else "নতুন একাউন্ট খুলতে চান? রেজিস্টার", fontSize = 12.sp)
                                            }

                                            if (!isRegisterMode) {
                                                TextButton(
                                                    onClick = {
                                                        if (authEmail.isBlank()) {
                                                            Toast.makeText(context, "পাসওয়ার্ড রিসেটের জন্য ইমেইল লিখুন", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            viewModel.resetSupabasePassword(authEmail.trim()) { _, _ -> }
                                                        }
                                                    }
                                                ) {
                                                    Text("ভুলে গেছেন?", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
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
                    tint = if (restoreWithOverwrite) FinancialNegative else MawaPrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (restoreWithOverwrite) "ডাটা প্রতিস্থাপন করে রিস্টোর করবেন?" else "বিদ্যমান ডাটার সাথে যুক্ত করবেন?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = if (restoreWithOverwrite) 
                        "বর্তমান ডাটাবেজের সকল পুরনো হিসাব ও ডামি ডাটা মুছে গিয়ে ব্যাকআপ ফাইলের কাস্টমার, লেনদেন, ফর্দ ও সেটিংস প্রতিস্থাপিত হবে।"
                        else "বর্তমান হিসাবগুলো অপরিবর্তিত থাকবে এবং ব্যাকআপের নতুন কাস্টমার ও লেনদেন যুক্ত হবে।",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirmDialog = false
                        isProcessing = true
                        scope.launch {
                            viewModel.restoreFullBackupFromJson(
                                jsonString = restoreJsonInput, 
                                overwriteExisting = restoreWithOverwrite
                            ) { success, msg, _ ->
                                isProcessing = false
                                if (success) {
                                    restoreSuccessMessage = msg
                                    restoreJsonInput = ""
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (restoreWithOverwrite) FinancialNegative else MawaPrimary)
                ) {
                    Text(if (restoreWithOverwrite) "হ্যাঁ, রিস্টোর নিশ্চিত করুন" else "হ্যাঁ, যুক্ত করুন", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Confirmation Alert before clearing dummy sample data
    if (showClearDataConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = FinancialNegative,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "সকল ডামি ও স্যাম্পল ডাটা মুছে ফেলবেন?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "অ্যাপের সকল পরীক্ষামূলক কাস্টমার, বাকি লেনদেন ও ফর্দ মুছে ডাটাবেজ সম্পূর্ণ খালি করা হবে। আপনি একদম নতুন করে নিজস্ব হিসাব লিখতে পারবেন।",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDataConfirmDialog = false
                        viewModel.clearAllSampleData {
                            Toast.makeText(context, "সকল ডামি ডাটা মুছে ফেলা হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialNegative)
                ) {
                    Text("হ্যাঁ, সব মুছুন", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirmDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Success Dialog after restore
    if (restoreSuccessMessage != null) {
        AlertDialog(
            onDismissRequest = { 
                restoreSuccessMessage = null
                onDismiss()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = FinancialPositive,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "রিস্টোর সফল হয়েছে! 🎉",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FinancialPositive
                )
            },
            text = {
                Text(
                    text = restoreSuccessMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        restoreSuccessMessage = null
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive)
                ) {
                    Text("ঠিক আছে", fontWeight = FontWeight.Bold, color = Color.White)
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
    primaryActionLabel: String,
    primaryActionIcon: ImageVector = Icons.Default.Download,
    onPrimaryAction: () -> Unit,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null
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
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
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

            Spacer(modifier = Modifier.height(12.dp))

            if (secondaryActionLabel != null && onSecondaryAction != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPrimaryAction,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = iconTint)
                    ) {
                        Icon(imageVector = primaryActionIcon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = primaryActionLabel, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onSecondaryAction,
                        modifier = Modifier.height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = iconTint)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = secondaryActionLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            } else {
                Button(
                    onClick = onPrimaryAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = iconTint)
                ) {
                    Icon(imageVector = primaryActionIcon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = primaryActionLabel, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                }
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
