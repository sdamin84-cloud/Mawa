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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
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
    onNavigateAuth: () -> Unit = {},
    onOpenModePreference: () -> Unit,
    onOpenAccountingGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.shopSettings.collectAsStateWithLifecycle()
    val appMode by viewModel.currentAppMode.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
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
                                    .size(46.dp)
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
                            Column {
                                Text(
                                    text = "MAWA",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MawaPrimary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "ডিজিটাল ক্যাশ খাতা",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "বন্ধ করুন",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = settings?.shopName ?: "মাওয়া জেনারেল স্টোর",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!settings?.ownerName.isNullOrBlank()) {
                        Text(
                            text = "মালিক: ${settings?.ownerName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!settings?.phone.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = settings?.phone ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Opening balance chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "সাবেক ক্যাশ: ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = BengaliUtils.formatTaka(settings?.openingBalance ?: 0.0),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MawaPrimary
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation and Actions Section
            DrawerNavMenuItem(
                icon = Icons.Default.PointOfSale,
                iconTint = MawaPrimary,
                iconBg = MawaPrimaryContainer,
                title = "ক্যাশ হিসাব ও টালি রিপোর্ট",
                subtitle = "ক্যাশবক্স মিলানো, জমা-খরচ ও নোট গণনা",
                onClick = onNavigateCashTally,
                testTag = "drawer_cash_tally"
            )

            DrawerNavMenuItem(
                icon = Icons.Default.Person,
                iconTint = Color(0xFF10B981),
                iconBg = Color(0xFF3ECF8E).copy(alpha = 0.18f),
                title = if (currentUser != null) "ক্লাউড একাউন্ট ও প্রোফাইল 👤" else "সুপাবেজ লগইন ও একাউন্ট 🔐",
                subtitle = if (currentUser != null) "লগইন: ${currentUser?.displayName?.ifBlank { currentUser?.email }}" else "লগইন, নতুন একাউন্ট ও পাসওয়ার্ড রিসেট",
                onClick = onNavigateAuth,
                testTag = "drawer_supabase_auth"
            )

            DrawerNavMenuItem(
                icon = Icons.Default.Sync,
                iconTint = Color(0xFF10B981),
                iconBg = Color(0xFF3ECF8E).copy(alpha = 0.18f),
                title = "সুপাবেজ ক্লাউড সিঙ্ক ☁️",
                subtitle = "অনলাইন ব্যাকআপ, রিস্টোর ও ক্লাউড ডাটাবেজ",
                onClick = onOpenSupabaseCloud,
                testTag = "drawer_supabase_cloud"
            )

            DrawerNavMenuItem(
                icon = Icons.Default.Backup,
                iconTint = Color(0xFF00796B),
                iconBg = Color(0xFFE0F2F1),
                title = "ডাটা ব্যাকআপ ও রিস্টোর",
                subtitle = "JSON, CSV ও মেমো ছবি (PNG) সেভ ও রিস্টোর",
                onClick = onOpenBackupRestore,
                testTag = "drawer_backup_restore"
            )

            DrawerNavMenuItem(
                icon = Icons.Default.AccountBalanceWallet,
                iconTint = Color(0xFF00796B),
                iconBg = Color(0xFFE0F2F1),
                title = "ব্যক্তিগত খরচের খাতা",
                subtitle = "দৈনন্দিন খরচ, আয় ও সঞ্চয়ের ডায়েরি",
                onClick = onNavigatePersonal,
                testTag = "drawer_personal"
            )

            DrawerNavMenuItem(
                icon = Icons.Default.Dashboard,
                iconTint = Color(0xFF6750A4),
                iconBg = Color(0xFFEDE7F6),
                title = "ব্যবহারকারী মোড নির্বাচন",
                subtitle = "ব্যবসা, ব্যক্তিগত বা উভয় মোড পরিবর্তন করুন",
                onClick = onOpenModePreference,
                testTag = "drawer_mode_preference"
            )

            DrawerNavMenuItem(
                icon = Icons.Default.Settings,
                iconTint = MawaPrimary,
                iconBg = MawaPrimaryContainer,
                title = "দোকানের সেটিংস ও ক্যাশ",
                subtitle = "দোকানের নাম, ঠিকানা ও সাবেক ক্যাশ বদলান",
                onClick = onOpenSettings,
                testTag = "drawer_settings"
            )

            DrawerNavMenuItem(
                icon = Icons.Default.Inventory,
                iconTint = FinancialPositive,
                iconBg = FinancialPositiveContainer,
                title = "পণ্য তালিকা ও স্টক ব্যবস্থাপনা",
                subtitle = "দোকানের পণ্য, বিক্রয় মূল্য ও মজুদ পরিচালনা",
                onClick = onNavigateProducts,
                testTag = "drawer_products"
            )

            DrawerNavMenuItem(
                icon = Icons.Default.ShoppingCart,
                iconTint = FinancialNeutral,
                iconBg = MaterialTheme.colorScheme.surfaceVariant,
                title = "সরাসরি পণ্য ক্রয় খাতা",
                subtitle = "পাইকারি বাজার থেকে নগদ মাল কেনা",
                onClick = onNavigateDirectPurchases,
                testTag = "drawer_direct_purchases"
            )

            DrawerNavMenuItem(
                icon = Icons.Default.Home,
                iconTint = FinancialWarning,
                iconBg = FinancialWarningContainer,
                title = "বাড়ির খরচের খাতা",
                subtitle = "দোকান থেকে সংসার ও পরিবারের খরচের হিসাব",
                onClick = onNavigateHomeAccounting,
                testTag = "drawer_home_accounting"
            )

            DrawerNavMenuItem(
                icon = Icons.Default.HelpOutline,
                iconTint = MawaPrimary,
                iconBg = MawaPrimaryContainer,
                title = "হিসাব মেলানোর নিয়ম ও সহায়িকা",
                subtitle = "দোকানের ক্যাশ ও খাতা মেলানোর ঐতিহ্যবাহী নিয়ম",
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
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "১০০% অফলাইন এবং নিরাপদ ডিজিটাল খাতা",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun DrawerNavMenuItem(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
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

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            modifier = Modifier.padding(start = 68.dp, end = 16.dp)
        )
    }
}
