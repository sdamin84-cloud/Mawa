package com.example.mawa.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import com.example.mawa.util.AppLanguage
import com.example.mawa.util.LanguageManager
import com.example.mawa.util.LocalAppLanguage
import com.example.mawa.util.tr
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.mawa.ui.components.AppSecuritySettingsDialog
import com.example.mawa.ui.components.BluetoothThermalPrinterDialog
import com.example.mawa.ui.components.PaymentQrDialog
import com.example.mawa.util.AppLockManager
import com.example.mawa.util.CashChimePlayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.local.entity.ShopSettingsEntity
import com.example.mawa.data.model.AppThemeMode
import com.example.mawa.ui.components.MawaTopBar
import com.example.mawa.ui.components.ThemeSelectionDialog
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialPositiveContainer
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.FinancialWarningContainer
import com.example.ui.theme.MawaPrimary
import com.example.ui.theme.MawaPrimaryContainer

@Composable
fun MoreScreen(
    viewModel: MawaViewModel,
    onNavigateHomeAccounting: () -> Unit,
    onNavigateDirectPurchases: () -> Unit,
    onNavigateProducts: () -> Unit,
    onOpenHomeExpense: () -> Unit = {},
    onOpenSupabaseCloud: () -> Unit = {},
    onOpenFirebaseCloud: () -> Unit = {},
    onOpenBackupRestore: () -> Unit = {},
    onLockApp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val accountingSummary by viewModel.accountingSummary.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentFirebaseUser by viewModel.currentFirebaseUser.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isCashSoundEnabled by viewModel.isCashSoundEnabled.collectAsStateWithLifecycle()
    val isDailyBackupReminderEnabled by viewModel.isDailyBackupReminderEnabled.collectAsStateWithLifecycle()
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAccountingGuideDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPrinterDialog by remember { mutableStateOf(false) }
    var showPaymentQrDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        MawaTopBar(
            title = "আরও অপশন",
            subtitle = settings?.shopName ?: "দোকান ও অন্যান্য হিসাব"
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Shop Card Banner
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                val shopDisplayName = settings?.shopName?.takeIf { it.isNotBlank() && it != "মাওয়া ডিজিটাল খাতা" } ?: "আমার দোকান"
                                Text(
                                    text = shopDisplayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "MAWA অ্যাপে দোকান প্রোফাইল · সাবেক: ${BengaliUtils.formatTaka(settings?.openingBalance ?: 0.0)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { showSettingsDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_edit_shop_profile_more")
                        ) {
                            Text(
                                text = "দোকান প্রোফাইল",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Dedicated Home Expense Highlight Card (বাড়ির খরচ বাটন)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FinancialWarningContainer.copy(alpha = 0.4f)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, FinancialWarning.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                    .background(FinancialWarning),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "বাড়ির খরচ (সংসার / উত্তোলন)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = FinancialWarning
                                    )
                                    Text(
                                        text = "আজকের বাড়ির খরচ: ${BengaliUtils.formatTaka(accountingSummary.todayHomeWithdrawals)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onOpenHomeExpense,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("btn_more_add_home_expense"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FinancialWarning)
                            ) {
                                Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("বাড়ির খরচ লিখুন", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            }

                            Button(
                                onClick = onNavigateHomeAccounting,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("btn_more_view_home_accounting"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, FinancialWarning.copy(alpha = 0.5f))
                            ) {
                                Text("খরচের খাতা", fontWeight = FontWeight.Bold, color = FinancialWarning, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Menu Items List
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // App Theme Mode Toggle Item
                        MoreMenuItem(
                            icon = when (themeMode) {
                                AppThemeMode.LIGHT -> Icons.Default.LightMode
                                AppThemeMode.DARK -> Icons.Default.DarkMode
                                AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                            },
                            iconTint = MaterialTheme.colorScheme.primary,
                            iconBg = MaterialTheme.colorScheme.primaryContainer,
                            title = "অ্যাপ থিম (Light / Dark / System)",
                            subtitle = "বর্তমান মোড: ${themeMode.banglaTitle}",
                            onClick = { showThemeDialog = true },
                            testTag = "menu_theme_settings"
                        )

                        // Google Cloud Card in More Menu
                        MoreMenuItem(
                            icon = Icons.Default.CloudSync,
                            iconTint = Color(0xFFFF9100),
                            iconBg = Color(0xFFFF9100).copy(alpha = 0.18f),
                            title = "অনলাইন ব্যাকআপ (গুগল ড্রাইভ) ☁️",
                            subtitle = if (currentFirebaseUser != null) "লগইন: ${currentFirebaseUser?.displayName?.ifBlank { currentFirebaseUser?.email }}" else "গুগল অ্যাকাউন্টে সুরক্ষিত অনলাইন ব্যাকআপ",
                            onClick = onOpenFirebaseCloud,
                            testTag = "menu_firebase_cloud"
                        )

                        // Server Cloud Card in More Menu
                        MoreMenuItem(
                            icon = Icons.Default.Sync,
                            iconTint = Color(0xFF10B981),
                            iconBg = Color(0xFF3ECF8E).copy(alpha = 0.18f),
                            title = "অনলাইন ব্যাকআপ (সার্ভার) ⚡",
                            subtitle = if (currentUser != null) "লগইন আছে: ${currentUser?.displayName?.ifBlank { currentUser?.email }}" else "সার্ভারে অনলাইন ব্যাকআপ ও রিস্টোর",
                            onClick = onOpenSupabaseCloud,
                            testTag = "menu_supabase_cloud"
                        )

                        MoreMenuItem(
                            icon = Icons.Default.Backup,
                            iconTint = Color(0xFF00796B),
                            iconBg = Color(0xFFE0F2F1),
                            title = "ডাটা ব্যাকআপ ও রিস্টোর (JSON/CSV)",
                            subtitle = "ফাইলে সম্পূর্ণ ব্যাকআপ ও কাস্টমার তালিকা এক্সপোর্ট",
                            onClick = onOpenBackupRestore,
                            testTag = "menu_backup_restore"
                        )

                        MoreMenuItem(
                            icon = Icons.Default.Home,
                            iconTint = FinancialWarning,
                            iconBg = FinancialWarningContainer,
                            title = "বাড়ির খরচ ও হিসাব খাতা",
                            subtitle = "দোকান থেকে বাড়ির বাজার, চিকিৎসা ও সংসারের খরচের পূর্ণ খাতা",
                            onClick = onNavigateHomeAccounting,
                            testTag = "menu_home_accounting"
                        )

                        MoreMenuItem(
                            icon = Icons.Default.ShoppingCart,
                            iconTint = MaterialTheme.colorScheme.primary,
                            iconBg = MaterialTheme.colorScheme.primaryContainer,
                            title = "সরাসরি মাল কেনা",
                            subtitle = "ডিলার বা মহাজন থেকে সরাসরি কেনা ও ক্রয় ইতিহাস",
                            onClick = onNavigateDirectPurchases,
                            testTag = "menu_direct_purchase"
                        )

                        MoreMenuItem(
                            icon = Icons.Default.Inventory,
                            iconTint = FinancialPositive,
                            iconBg = MaterialTheme.colorScheme.surfaceVariant,
                            title = "পণ্য তালিকা ও হিসাব",
                            subtitle = "পণ্য ব্যবস্থাপনা, গড় ক্রয় দর ও লাভ মার্জিন বিশ্লেষণ",
                            onClick = onNavigateProducts,
                            testTag = "menu_products"
                        )

                        MoreMenuItem(
                            icon = Icons.Default.QrCode2,
                            iconTint = Color(0xFFE2136E),
                            iconBg = Color(0xFFFCE7F3),
                            title = "ডিজিটাল পেমেন্ট QR ও একাউন্ট",
                            subtitle = "bKash, Nagad নম্বর ও গ্রাহক স্ক্যান কিউআর কোড",
                            onClick = { showPaymentQrDialog = true },
                            testTag = "menu_payment_qr"
                        )

                        MoreMenuItem(
                            icon = Icons.Default.Security,
                            iconTint = Color(0xFF0D9488),
                            iconBg = Color(0xFFCCFBF1),
                            title = "অ্যাপ নিরাপত্তা ও ৪ সংখ্যার পিন",
                            subtitle = if (AppLockManager.isLockEnabled(context)) "পিন সক্রিয় রয়েছে · পরিবর্তন বা নিরাপত্তা সেটিংস" else "দোকানের ক্যাশ ও বাকি সুরক্ষায় পিন দিন",
                            onClick = { showSecurityDialog = true },
                            testTag = "menu_security_settings"
                        )

                        if (AppLockManager.isLockEnabled(context)) {
                            MoreMenuItem(
                                icon = Icons.Default.Lock,
                                iconTint = FinancialPositive,
                                iconBg = FinancialPositiveContainer,
                                title = "এখনই অ্যাপ লক করুন",
                                subtitle = "দোকানের ক্যাশ ও খাতা তাৎক্ষণিক লক করুন",
                                onClick = {
                                    AppLockManager.lockNow()
                                    onLockApp()
                                },
                                testTag = "menu_lock_now"
                            )
                        }

                        MoreMenuItem(
                            icon = Icons.Default.Print,
                            iconTint = Color(0xFF0D9488),
                            iconBg = Color(0xFFCCFBF1),
                            title = "ব্লুটুথ থার্মাল প্রিন্টার (POS)",
                            subtitle = "৫৮ মিমি / ৮০ মিমি পিওএস প্রিন্টার পেয়ার ও টেস্ট প্রিন্ট",
                            onClick = { showPrinterDialog = true },
                            testTag = "menu_thermal_printer"
                        )

                        MoreMenuItem(
                            icon = Icons.Default.Settings,
                            iconTint = MaterialTheme.colorScheme.onSurface,
                            iconBg = MaterialTheme.colorScheme.surfaceVariant,
                            title = "সাবেক ক্যাশ ও সেটিংস",
                            subtitle = "দোকানের নাম, সাউন্ড, রিমাইন্ডার ও ক্যাশ সেট করুন",
                            onClick = { showSettingsDialog = true },
                            testTag = "menu_settings"
                        )

                        MoreMenuItem(
                            icon = Icons.Default.HelpOutline,
                            iconTint = MaterialTheme.colorScheme.primary,
                            iconBg = MaterialTheme.colorScheme.primaryContainer,
                            title = "হিসাবের নিয়মাবলী",
                            subtitle = "লাভ-ক্ষতি, বিক্রি ও ক্যাশ মেলানোর গাইড",
                            onClick = { showAccountingGuideDialog = true },
                            showDivider = false,
                            testTag = "menu_guide"
                        )
                    }
                }
            }

            // App Identity & Info Footer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MAWA · ডিজিটাল খাতা ও দোকান হিসাব",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "মুদি ও রিটেইল দোকানের নিখুঁত ডিজিটাল ক্যাশ বুক",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Settings Dialog
    if (showSettingsDialog) {
        ShopSettingsDialog(
            currentSettings = settings,
            currentThemeMode = themeMode,
            isCashSoundEnabled = isCashSoundEnabled,
            isDailyBackupReminderEnabled = isDailyBackupReminderEnabled,
            onDismiss = { showSettingsDialog = false },
            onSave = { updated ->
                viewModel.updateSettings(updated)
                showSettingsDialog = false
            },
            onThemeChanged = { newTheme ->
                viewModel.setThemeMode(newTheme)
            },
            onCashSoundChanged = { enabled ->
                viewModel.setCashSoundEnabled(enabled)
            },
            onDailyBackupReminderChanged = { enabled ->
                viewModel.setDailyBackupReminderEnabled(enabled)
            },
            onOpenPrinterSettings = {
                showSettingsDialog = false
                showPrinterDialog = true
            }
        )
    }

    // Bluetooth Thermal POS Printer Dialog
    if (showPrinterDialog) {
        BluetoothThermalPrinterDialog(
            shopName = settings?.shopName?.ifBlank { "মাওয়া ডিজিটাল খাতা" } ?: "মাওয়া ডিজিটাল খাতা",
            shopPhone = settings?.phone ?: "",
            onDismiss = { showPrinterDialog = false }
        )
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themeMode,
            onDismiss = { showThemeDialog = false },
            onSelectTheme = { selectedTheme ->
                viewModel.setThemeMode(selectedTheme)
            }
        )
    }

    // Accounting Guide Dialog
    if (showAccountingGuideDialog) {
        AccountingGuideDialog(onDismiss = { showAccountingGuideDialog = false })
    }

    // Digital Payment QR Hub Dialog
    if (showPaymentQrDialog) {
        PaymentQrDialog(
            settings = settings,
            onDismiss = { showPaymentQrDialog = false },
            onEditShopProfile = {
                showPaymentQrDialog = false
                showSettingsDialog = true
            }
        )
    }

    // App Security PIN Lock Dialog
    if (showSecurityDialog) {
        AppSecuritySettingsDialog(
            shopPhone = settings?.phone ?: "",
            onDismiss = { showSecurityDialog = false },
            onLockNow = {
                showSecurityDialog = false
                AppLockManager.lockNow()
                onLockApp()
            }
        )
    }
}

@Composable
fun MoreMenuItem(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    testTag: String = ""
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant
            )
        }

        if (showDivider) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun ShopSettingsDialog(
    currentSettings: ShopSettingsEntity?,
    currentThemeMode: AppThemeMode = AppThemeMode.SYSTEM,
    isCashSoundEnabled: Boolean = true,
    isDailyBackupReminderEnabled: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (ShopSettingsEntity) -> Unit,
    onThemeChanged: (AppThemeMode) -> Unit = {},
    onCashSoundChanged: (Boolean) -> Unit = {},
    onDailyBackupReminderChanged: (Boolean) -> Unit = {},
    onOpenPrinterSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    var shopName by remember { mutableStateOf(currentSettings?.shopName ?: "মাওয়া জেনারেল স্টোর") }
    var ownerName by remember { mutableStateOf(currentSettings?.ownerName ?: "") }
    var phone by remember { mutableStateOf(currentSettings?.phone ?: "") }
    var bkashNumber by remember { mutableStateOf(currentSettings?.bkashNumber ?: "") }
    var nagadNumber by remember { mutableStateOf(currentSettings?.nagadNumber ?: "") }
    var bankDetails by remember { mutableStateOf(currentSettings?.bankDetails ?: "") }
    var openingCash by remember { mutableStateOf((currentSettings?.openingBalance ?: 0.0).toString()) }
    var selectedTheme by remember { mutableStateOf(currentThemeMode) }
    var cashSound by remember { mutableStateOf(isCashSoundEnabled) }
    var backupReminder by remember { mutableStateOf(isDailyBackupReminderEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Column {
                    Text(text = "দোকান প্রোফাইল ও সেটিংস", fontWeight = FontWeight.Bold)
                    Text(
                        text = "MAWA অ্যাপে আপনার নিজস্ব দোকানের পরিচিতি",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Language Selection Section
                val currentLang = LocalAppLanguage.current
                Text(
                    text = if (currentLang.isEnglish) "App Language" else "অ্যাপের ভাষা (Language)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                LanguageManager.setLanguage(context, AppLanguage.BANGLA)
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = if (!currentLang.isEnglish) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (!currentLang.isEnglish) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "বাংলা (Bangla)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (!currentLang.isEnglish) FontWeight.Bold else FontWeight.Normal,
                                color = if (!currentLang.isEnglish) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                LanguageManager.setLanguage(context, AppLanguage.ENGLISH)
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = if (currentLang.isEnglish) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (currentLang.isEnglish) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "English",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (currentLang.isEnglish) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentLang.isEnglish) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                // Theme Toggle Section
                Text(
                    text = tr("অ্যাপ থিম (Theme)", "App Theme"),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppThemeMode.entries.forEach { mode ->
                        val isSelected = mode == selectedTheme
                        val modeIcon = when (mode) {
                            AppThemeMode.LIGHT -> Icons.Default.LightMode
                            AppThemeMode.DARK -> Icons.Default.DarkMode
                            AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                        }
                        val shortLabel = when (mode) {
                            AppThemeMode.LIGHT -> "লাইট"
                            AppThemeMode.DARK -> "ডার্ক"
                            AppThemeMode.SYSTEM -> "সিস্টেম"
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedTheme = mode
                                    onThemeChanged(mode)
                                }
                                .testTag("btn_theme_${mode.name.lowercase()}"),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = modeIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = shortLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("দোকানের নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("মালিকের নাম") },
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
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bkashNumber,
                    onValueChange = { bkashNumber = it },
                    label = { Text("বিকাশ নম্বর (Personal / Merchant)") },
                    placeholder = { Text("যেমন: ০১৭১২-৩৪৫৬৭৮") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nagadNumber,
                    onValueChange = { nagadNumber = it },
                    label = { Text("নগদ নম্বর (Personal / Merchant)") },
                    placeholder = { Text("যেমন: ০১৮১২-৩৪৫৬৭৮") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bankDetails,
                    onValueChange = { bankDetails = it },
                    label = { Text("ব্যাংক একাউন্ট বিবরণ (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: ইসলামী ব্যাংক, গুলশান শাখা, A/C: ২০৫০...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = openingCash,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) openingCash = it },
                    label = { Text("সাবেক ক্যাশ / প্রারম্ভিক ক্যাশ (৳)") },
                    placeholder = { Text("0") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                // Hardware & Sound Features
                Text(
                    text = "সাউন্ড ও হার্ডওয়্যার সেটিংস",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Cash Chime Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ক্যাশ রেজিস্টার সাউন্ড", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Text("বেচা-বিক্রি ও জমার সময় মিষ্টি ঘণ্টার আওয়াজ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { CashChimePlayer.playCashChime(context) },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("শুনুন", fontSize = 11.sp)
                        }
                        Switch(
                            checked = cashSound,
                            onCheckedChange = {
                                cashSound = it
                                onCashSoundChanged(it)
                            },
                            modifier = Modifier.testTag("switch_cash_sound")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Daily Backup Reminder Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("দৈনিক ব্যাকআপ সতর্কতা", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Text("রাতে দোকান বন্ধের আগে ডাটা সেভ রিমাইন্ডার", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                    Switch(
                        checked = backupReminder,
                        onCheckedChange = {
                            backupReminder = it
                            onDailyBackupReminderChanged(it)
                        },
                        modifier = Modifier.testTag("switch_backup_reminder")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bluetooth POS Printer Setup Button
                Button(
                    onClick = onOpenPrinterSettings,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("থার্মাল প্রিন্টার পেয়ার ও টেস্ট (POS)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (shopName.isNotBlank()) {
                        val cash = openingCash.toDoubleOrNull() ?: 0.0
                        val qrPayload = com.example.mawa.util.QrCodeGenerator.buildPaymentQrPayload(
                            shopName = shopName.trim(),
                            phone = phone.trim(),
                            bkashNumber = bkashNumber.trim(),
                            nagadNumber = nagadNumber.trim(),
                            bankDetails = bankDetails.trim()
                        )
                        onThemeChanged(selectedTheme)
                        onSave(
                            ShopSettingsEntity(
                                id = currentSettings?.id ?: 1,
                                shopName = shopName.trim(),
                                ownerName = ownerName.trim(),
                                phone = phone.trim(),
                                bkashNumber = bkashNumber.trim(),
                                nagadNumber = nagadNumber.trim(),
                                bankDetails = bankDetails.trim(),
                                qrCodeData = qrPayload,
                                openingBalance = cash,
                                appMode = currentSettings?.appMode ?: "BOTH",
                                isModeConfigured = currentSettings?.isModeConfigured ?: true
                            )
                        )
                    }
                },
                enabled = shopName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
fun ShopSettingsDialog(
    currentSettings: ShopSettingsEntity?,
    onDismiss: () -> Unit,
    onSave: (ShopSettingsEntity) -> Unit
) {
    ShopSettingsDialog(
        currentSettings = currentSettings,
        currentThemeMode = AppThemeMode.SYSTEM,
        onDismiss = onDismiss,
        onSave = onSave,
        onThemeChanged = {}
    )
}

@Composable
fun AccountingGuideDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("মাওয়া হিসাব নির্দেশিকা", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "১. মোট বিক্রি:\nনগদ বিক্রি + বাকি বিক্রি। বাকি আদায় বিক্রি হিসেবে যুক্ত হয় না, কারণ এটি পূর্বের বিক্রির টাকা আদায়।",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "২. ব্যবসায়িক লাভ:\nমোট বিক্রি − মাল ক্রয় − দোকানের খরচ।",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "৩. বাড়ির খরচ:\nদোকান থেকে সংসারের জন্য টাকা নিলে তা ব্যবসায়িক ক্ষতি নয়, এটি মুনাফা থেকে ব্যক্তিগত উত্তোলন।",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "৪. ক্যাশ ইন হ্যান্ড:\nসাবেক ক্যাশ + নগদ বিক্রি + বাকি আদায় − মাল ক্রয় − দোকানের খরচ − বাড়ির খরচ।",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("বুঝেছি")
            }
        }
    )
}

