package com.example.mawa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.example.mawa.util.AppLockManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.model.AppThemeMode
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialNeutral
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialPositiveContainer
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.FinancialWarningContainer
import com.example.ui.theme.MawaPrimary
import com.example.ui.theme.MawaPrimaryContainer

@Composable
fun MawaSideDrawer(
    viewModel: MawaViewModel,
    onClose: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigateProducts: () -> Unit,
    onNavigateDirectPurchases: () -> Unit,
    onNavigateHomeAccounting: () -> Unit,
    onNavigatePersonal: () -> Unit,
    onNavigateCashTally: () -> Unit = {},
    onOpenBackupRestore: () -> Unit = {},
    onOpenSupabaseCloud: () -> Unit = {},
    onOpenFirebaseCloud: () -> Unit = {},
    onNavigateAuth: () -> Unit = {},
    onOpenPaymentQr: () -> Unit = {},
    onOpenSecuritySettings: () -> Unit = {},
    onLockApp: () -> Unit = {},
    onOpenModePreference: () -> Unit,
    onOpenAccountingGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val appMode by viewModel.currentAppMode.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentFirebaseUser by viewModel.currentFirebaseUser.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val summary by viewModel.selectedDateAccountingSummary.collectAsStateWithLifecycle()
    var showThemeDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    ModalDrawerSheet(
        modifier = modifier
            .widthIn(max = 320.dp)
            .fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerTonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(scrollState)
        ) {
            // Header: Shop Identity Profile
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MawaPrimaryContainer
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MawaPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            val shopDisplayName = settings?.shopName?.takeIf { it.isNotBlank() && it != "মাওয়া ডিজিটাল খাতা" } ?: "আমার দোকান"
                            Text(
                                text = shopDisplayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            onClick = onOpenSettings,
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            modifier = Modifier.testTag("drawer_edit_profile_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "দোকান প্রোফাইল",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Navigation and Actions Section (সংক্ষিপ্ত, এক লাইনে ফিচার নাম)
            DrawerNavMenuItem(
                icon = Icons.Default.PointOfSale,
                iconTint = MawaPrimary,
                iconBg = MawaPrimaryContainer,
                title = "ক্যাশ হিসাব ও টালি",
                onClick = onNavigateCashTally,
                testTag = "drawer_cash_tally"
            )

            DrawerNavMenuItem(
                icon = Icons.Default.CloudSync,
                iconTint = Color(0xFF10B981),
                iconBg = Color(0xFF3ECF8E).copy(alpha = 0.18f),
                title = "ক্লাউড সিঙ্ক ও ব্যাকআপ",
                onClick = onOpenBackupRestore,
                testTag = "drawer_backup_cloud_hub"
            )

            DrawerNavMenuItem(
                icon = Icons.Default.Inventory,
                iconTint = FinancialPositive,
                iconBg = FinancialPositiveContainer,
                title = "পণ্য ও স্টক খাতা",
                onClick = onNavigateProducts,
                testTag = "drawer_products"
            )

            DrawerNavMenuItem(
                icon = Icons.Default.ShoppingCart,
                iconTint = FinancialNeutral,
                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                title = "সরাসরি পণ্য ক্রয় খাতা",
                onClick = onNavigateDirectPurchases,
                testTag = "drawer_direct_purchases"
            )

            DrawerNavMenuItem(
                icon = Icons.Default.Home,
                iconTint = FinancialWarning,
                iconBg = FinancialWarningContainer,
                title = "বাড়ির খরচের খাতা",
                onClick = onNavigateHomeAccounting,
                testTag = "drawer_home_accounting"
            )

            DrawerNavMenuItem(
                icon = Icons.Default.AccountBalanceWallet,
                iconTint = Color(0xFF00796B),
                iconBg = Color(0xFFE0F2F1),
                title = "ব্যক্তিগত খাতা",
                onClick = onNavigatePersonal,
                testTag = "drawer_personal"
            )

            DrawerNavMenuItem(
                icon = Icons.Default.QrCode2,
                iconTint = Color(0xFFE2136E),
                iconBg = Color(0xFFFCE7F3),
                title = "ডিজিটাল পেমেন্ট QR",
                onClick = onOpenPaymentQr,
                testTag = "drawer_payment_qr"
            )

            DrawerNavMenuItem(
                icon = Icons.Default.Security,
                iconTint = Color(0xFF0D9488),
                iconBg = Color(0xFFCCFBF1),
                title = "অ্যাপ নিরাপত্তা ও পিন",
                onClick = onOpenSecuritySettings,
                testTag = "drawer_security_settings"
            )

            if (AppLockManager.isLockEnabled(context)) {
                DrawerNavMenuItem(
                    icon = Icons.Default.Lock,
                    iconTint = FinancialPositive,
                    iconBg = FinancialPositiveContainer,
                    title = "অ্যাপ লক করুন",
                    onClick = {
                        AppLockManager.lockNow()
                        onLockApp()
                    },
                    testTag = "drawer_lock_now"
                )
            }

            DrawerNavMenuItem(
                icon = Icons.Default.Settings,
                iconTint = MawaPrimary,
                iconBg = MawaPrimaryContainer,
                title = "দোকানের সেটিংস",
                onClick = onOpenSettings,
                testTag = "drawer_settings"
            )

            DrawerNavMenuItem(
                icon = when (themeMode) {
                    AppThemeMode.LIGHT -> Icons.Default.LightMode
                    AppThemeMode.DARK -> Icons.Default.DarkMode
                    AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                },
                iconTint = MaterialTheme.colorScheme.primary,
                iconBg = MaterialTheme.colorScheme.primaryContainer,
                title = "অ্যাপ থিম পরিবর্তন",
                onClick = { showThemeDialog = true },
                testTag = "drawer_theme_toggle"
            )

            DrawerNavMenuItem(
                icon = Icons.Default.HelpOutline,
                iconTint = MawaPrimary,
                iconBg = MawaPrimaryContainer,
                title = "সহায়িকা ও নিয়মাবলী",
                onClick = onOpenAccountingGuide,
                testTag = "drawer_accounting_guide"
            )

            Spacer(modifier = Modifier.weight(1f, fill = false))
            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // App Version and Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MAWA সংস্করণ ২.০",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themeMode,
            onDismiss = { showThemeDialog = false },
            onSelectTheme = { selectedTheme ->
                viewModel.setThemeMode(selectedTheme)
            }
        )
    }
}

@Composable
private fun DrawerNavMenuItem(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
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

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
            modifier = Modifier.padding(start = 68.dp, end = 16.dp)
        )
    }
}
