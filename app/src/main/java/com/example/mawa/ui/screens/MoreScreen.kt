package com.example.mawa.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.local.entity.ShopSettingsEntity
import com.example.mawa.ui.components.MawaTopBar
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
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
    onOpenBackupRestore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val settings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val accountingSummary by viewModel.accountingSummary.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAccountingGuideDialog by remember { mutableStateOf(false) }

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
                                    .background(MawaPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = MawaPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = settings?.shopName ?: "মাওয়া জেনারেল স্টোর",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "সাবেক ক্যাশ: ${BengaliUtils.formatTaka(settings?.openingBalance ?: 0.0)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { showSettingsDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "পরিবর্তন",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
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
                        // Supabase Cloud Card in More Menu
                        MoreMenuItem(
                            icon = Icons.Default.Sync,
                            iconTint = Color(0xFF10B981),
                            iconBg = Color(0xFF3ECF8E).copy(alpha = 0.18f),
                            title = "সুপাবেজ ক্লাউড সিঙ্ক ও ব্যাকআপ ☁️",
                            subtitle = if (currentUser != null) "লগইন আছে: ${currentUser?.displayName?.ifBlank { currentUser?.email }}" else "অনলাইন ব্যাকআপ, রিস্টোর ও ক্লাউড ডাটাবেজ",
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
                            iconTint = MawaPrimary,
                            iconBg = MawaPrimaryContainer,
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
                            icon = Icons.Default.Settings,
                            iconTint = MaterialTheme.colorScheme.onSurface,
                            iconBg = MaterialTheme.colorScheme.surfaceVariant,
                            title = "সাবেক ক্যাশ ও সেটিংস",
                            subtitle = "দোকানের নাম ও প্রারম্ভিক ক্যাশ ব্যালেন্স সেট করুন",
                            onClick = { showSettingsDialog = true },
                            testTag = "menu_settings"
                        )

                        MoreMenuItem(
                            icon = Icons.Default.HelpOutline,
                            iconTint = MawaPrimary,
                            iconBg = MawaPrimaryContainer,
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
                        color = MawaPrimary
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
            onDismiss = { showSettingsDialog = false },
            onSave = { updated ->
                viewModel.updateSettings(updated)
                showSettingsDialog = false
            }
        )
    }

    // Accounting Guide Dialog
    if (showAccountingGuideDialog) {
        AccountingGuideDialog(onDismiss = { showAccountingGuideDialog = false })
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
    onDismiss: () -> Unit,
    onSave: (ShopSettingsEntity) -> Unit
) {
    var shopName by remember { mutableStateOf(currentSettings?.shopName ?: "মাওয়া জেনারেল স্টোর") }
    var ownerName by remember { mutableStateOf(currentSettings?.ownerName ?: "") }
    var phone by remember { mutableStateOf(currentSettings?.phone ?: "") }
    var openingCash by remember { mutableStateOf((currentSettings?.openingBalance ?: 0.0).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "দোকানের সেটিংস", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("দোকানের নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("মালিকের নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = openingCash,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) openingCash = it },
                    label = { Text("সাবেক ক্যাশ / প্রারম্ভিক ক্যাশ (৳)") },
                    placeholder = { Text("0") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (shopName.isNotBlank()) {
                        val cash = openingCash.toDoubleOrNull() ?: 0.0
                        onSave(
                            ShopSettingsEntity(
                                id = currentSettings?.id ?: 1,
                                shopName = shopName.trim(),
                                ownerName = ownerName.trim(),
                                phone = phone.trim(),
                                openingBalance = cash,
                                appMode = currentSettings?.appMode ?: "BOTH",
                                isModeConfigured = currentSettings?.isModeConfigured ?: true
                            )
                        )
                    }
                },
                enabled = shopName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
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
                colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
            ) {
                Text("বুঝেছি")
            }
        }
    )
}
