package com.example.mawa.ui

import android.content.Context
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mawa.data.model.AppMode
import com.example.mawa.ui.components.AppLockScreen
import com.example.mawa.ui.components.AppSecuritySettingsDialog
import com.example.mawa.ui.components.BackupRestoreDialog
import com.example.mawa.ui.components.MawaBottomBar
import com.example.mawa.ui.components.MawaSideDrawer
import com.example.mawa.ui.components.MawaTab
import com.example.mawa.ui.components.PaymentQrDialog
import com.example.mawa.ui.components.SmartRetailCalculatorBottomSheet
import com.example.mawa.ui.components.SupabaseCloudDialog
import com.example.mawa.ui.components.UserModePreferenceDialog
import com.example.mawa.util.AppLockManager
import com.example.mawa.ui.screens.AccountingGuideDialog
import com.example.mawa.ui.screens.AuthScreen
import com.example.mawa.ui.screens.BakiScreen
import com.example.mawa.ui.screens.CashTallyScreen
import com.example.mawa.ui.screens.FordiScreen
import com.example.mawa.ui.screens.HomeAccountingScreen
import com.example.mawa.ui.screens.HomeScreen
import com.example.mawa.ui.screens.PersonalScreen
import com.example.mawa.ui.screens.ProductsScreen
import com.example.mawa.ui.screens.PurchaseScreen
import com.example.mawa.ui.screens.QuickExpenseDrawer
import com.example.mawa.ui.screens.QuickExpenseTarget
import com.example.mawa.ui.screens.ReportsScreen
import com.example.mawa.ui.screens.SalesScreen
import com.example.mawa.ui.screens.ShopSettingsDialog
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class AppSubScreen {
    NONE,
    SALES,
    HOME_ACCOUNTING,
    DIRECT_PURCHASES,
    PRODUCTS,
    CASH_TALLY,
    AUTH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MawaApp(
    viewModel: MawaViewModel = viewModel()
) {
    val appMode by viewModel.currentAppMode.collectAsStateWithLifecycle()
    val isModeConfigured by viewModel.isModeConfigured.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(MawaTab.HOME) }
    var currentSubScreen by remember { mutableStateOf(AppSubScreen.NONE) }
    var showExpenseDrawer by remember { mutableStateOf(false) }
    var expenseDrawerInitialTarget by remember { mutableStateOf(QuickExpenseTarget.SHOP) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAccountingGuideDialog by remember { mutableStateOf(false) }
    var showModePreferenceDialog by remember { mutableStateOf(false) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }
    var showSupabaseCloudDialog by remember { mutableStateOf(false) }
    var showPaymentQrDialog by remember { mutableStateOf(false) }
    var showSecuritySettingsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var isAppLocked by remember {
        mutableStateOf(AppLockManager.isLockEnabled(context) && !AppLockManager.isUnlockedInSession)
    }
    val calcPrefs = remember { context.getSharedPreferences("mawa_calculator_state_prefs", Context.MODE_PRIVATE) }
    var showCalculatorSheet by remember { 
        mutableStateOf(calcPrefs.getBoolean("pref_calc_always_on", false)) 
    }

    // Auto-adjust tab if mode changes
    LaunchedEffect(appMode) {
        if (appMode == AppMode.PERSONAL_ONLY && currentTab != MawaTab.PERSONAL && currentTab != MawaTab.HOME_ACCOUNTING && currentTab != MawaTab.REPORTS) {
            currentTab = MawaTab.PERSONAL
        } else if (appMode == AppMode.BUSINESS_ONLY && currentTab == MawaTab.PERSONAL) {
            currentTab = MawaTab.HOME
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    val expenseSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Collect feedback messages from ViewModel to show snackbars
    LaunchedEffect(viewModel) {
        viewModel.feedbackMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentSubScreen == AppSubScreen.NONE,
        drawerContent = {
            MawaSideDrawer(
                viewModel = viewModel,
                onClose = { coroutineScope.launch { drawerState.close() } },
                onOpenSettings = {
                    coroutineScope.launch { drawerState.close() }
                    showSettingsDialog = true
                },
                onNavigateProducts = {
                    coroutineScope.launch { drawerState.close() }
                    currentSubScreen = AppSubScreen.PRODUCTS
                },
                onNavigateDirectPurchases = {
                    coroutineScope.launch { drawerState.close() }
                    currentSubScreen = AppSubScreen.DIRECT_PURCHASES
                },
                onNavigateHomeAccounting = {
                    coroutineScope.launch { drawerState.close() }
                    currentTab = MawaTab.HOME_ACCOUNTING
                    currentSubScreen = AppSubScreen.NONE
                },
                onNavigatePersonal = {
                    coroutineScope.launch { drawerState.close() }
                    currentTab = MawaTab.PERSONAL
                    currentSubScreen = AppSubScreen.NONE
                },
                onNavigateCashTally = {
                    coroutineScope.launch { drawerState.close() }
                    currentSubScreen = AppSubScreen.CASH_TALLY
                },
                onOpenBackupRestore = {
                    coroutineScope.launch { drawerState.close() }
                    showBackupRestoreDialog = true
                },
                onOpenSupabaseCloud = {
                    coroutineScope.launch { drawerState.close() }
                    showSupabaseCloudDialog = true
                },
                onNavigateAuth = {
                    coroutineScope.launch { drawerState.close() }
                    currentSubScreen = AppSubScreen.AUTH
                },
                onOpenPaymentQr = {
                    coroutineScope.launch { drawerState.close() }
                    showPaymentQrDialog = true
                },
                onOpenSecuritySettings = {
                    coroutineScope.launch { drawerState.close() }
                    showSecuritySettingsDialog = true
                },
                onLockApp = {
                    coroutineScope.launch { drawerState.close() }
                    isAppLocked = true
                },
                onOpenModePreference = {
                    coroutineScope.launch { drawerState.close() }
                    showModePreferenceDialog = true
                },
                onOpenAccountingGuide = {
                    coroutineScope.launch { drawerState.close() }
                    showAccountingGuideDialog = true
                }
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                if (currentSubScreen == AppSubScreen.NONE) {
                    MawaBottomBar(
                        currentTab = currentTab,
                        appMode = appMode,
                        onTabSelected = { tab ->
                            currentTab = tab
                        },
                        onQuickExpenseClick = {
                            expenseDrawerInitialTarget = if (appMode == AppMode.PERSONAL_ONLY) QuickExpenseTarget.HOME else QuickExpenseTarget.SHOP
                            showExpenseDrawer = true
                        }
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentSubScreen) {
                    AppSubScreen.SALES -> {
                        SalesScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = AppSubScreen.NONE }
                        )
                    }
                    AppSubScreen.HOME_ACCOUNTING -> {
                        HomeAccountingScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = AppSubScreen.NONE },
                            onOpenAddExpense = {
                                expenseDrawerInitialTarget = QuickExpenseTarget.HOME
                                showExpenseDrawer = true
                            },
                            onOpenDrawer = {
                                coroutineScope.launch { drawerState.open() }
                            }
                        )
                    }
                    AppSubScreen.DIRECT_PURCHASES -> {
                        PurchaseScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = AppSubScreen.NONE }
                        )
                    }
                    AppSubScreen.PRODUCTS -> {
                        ProductsScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = AppSubScreen.NONE }
                        )
                    }
                    AppSubScreen.CASH_TALLY -> {
                        CashTallyScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = AppSubScreen.NONE },
                            onOpenDrawer = {
                                coroutineScope.launch { drawerState.open() }
                            }
                        )
                    }
                    AppSubScreen.AUTH -> {
                        AuthScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = AppSubScreen.NONE },
                            onOpenCloudSyncDialog = {
                                showSupabaseCloudDialog = true
                            }
                        )
                    }
                    AppSubScreen.NONE -> {
                        when (currentTab) {
                            MawaTab.HOME -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onOpenSales = { currentSubScreen = AppSubScreen.SALES },
                                    onOpenBaki = { currentTab = MawaTab.BAKI },
                                    onOpenJoma = { currentTab = MawaTab.BAKI },
                                    onOpenExpenseDrawer = {
                                        expenseDrawerInitialTarget = QuickExpenseTarget.SHOP
                                        showExpenseDrawer = true
                                    },
                                    onOpenFordi = { currentTab = MawaTab.FORDI },
                                    onOpenReports = { currentTab = MawaTab.REPORTS },
                                    onOpenDrawer = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    onOpenSupabaseCloud = {
                                        showSupabaseCloudDialog = true
                                    },
                                    onLockApp = {
                                        isAppLocked = true
                                    }
                                )
                            }
                            MawaTab.FORDI -> {
                                FordiScreen(
                                    viewModel = viewModel,
                                    onOpenDrawer = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    onOpenProducts = {
                                        currentSubScreen = AppSubScreen.PRODUCTS
                                    }
                                )
                            }
                            MawaTab.EXPENSE -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onOpenSales = { currentSubScreen = AppSubScreen.SALES },
                                    onOpenBaki = { currentTab = MawaTab.BAKI },
                                    onOpenJoma = { currentTab = MawaTab.BAKI },
                                    onOpenExpenseDrawer = {
                                        expenseDrawerInitialTarget = QuickExpenseTarget.SHOP
                                        showExpenseDrawer = true
                                    },
                                    onOpenFordi = { currentTab = MawaTab.FORDI },
                                    onOpenReports = { currentTab = MawaTab.REPORTS },
                                    onOpenDrawer = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    onOpenSupabaseCloud = {
                                        showSupabaseCloudDialog = true
                                    },
                                    onLockApp = {
                                        isAppLocked = true
                                    }
                                )
                            }
                            MawaTab.BAKI -> {
                                BakiScreen(
                                    viewModel = viewModel,
                                    onOpenDrawer = {
                                        coroutineScope.launch { drawerState.open() }
                                    }
                                )
                            }
                            MawaTab.PERSONAL -> {
                                PersonalScreen(
                                    viewModel = viewModel,
                                    onOpenDrawer = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    onSwitchToBusiness = {
                                        currentTab = MawaTab.HOME
                                    }
                                )
                            }
                            MawaTab.HOME_ACCOUNTING -> {
                                HomeAccountingScreen(
                                    viewModel = viewModel,
                                    onOpenAddExpense = {
                                        expenseDrawerInitialTarget = QuickExpenseTarget.HOME
                                        showExpenseDrawer = true
                                    },
                                    onOpenDrawer = {
                                        coroutineScope.launch { drawerState.open() }
                                    }
                                )
                            }
                            MawaTab.REPORTS -> {
                                ReportsScreen(
                                    viewModel = viewModel,
                                    onOpenDrawer = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    onNavigateToDayOnHome = { targetMillis ->
                                        viewModel.setSelectedHomeDate(targetMillis)
                                        currentTab = MawaTab.HOME
                                    },
                                    onNavigateCashTally = {
                                        currentSubScreen = AppSubScreen.CASH_TALLY
                                    },
                                    onOpenBackupRestore = {
                                        showBackupRestoreDialog = true
                                    }
                                )
                            }
                        }
                    }
                }

                // Floating Calculator Button (ভাসমান ক্যালকুলেটর বাটন - বর্ণিল ও ডায়নামিক)
                if (currentSubScreen == AppSubScreen.NONE) {
                    val calcInteractionSource = remember { MutableInteractionSource() }
                    val isCalcPressed by calcInteractionSource.collectIsPressedAsState()
                    val calcScale by animateFloatAsState(
                        targetValue = if (isCalcPressed) 0.88f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                        label = "calc_fab_scale"
                    )

                    val fabPrimary = MaterialTheme.colorScheme.primary
                    val fabSecondary = MaterialTheme.colorScheme.secondary

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp)
                            .scale(calcScale)
                            .shadow(
                                elevation = if (isCalcPressed) 2.dp else 6.dp,
                                shape = RoundedCornerShape(18.dp),
                                spotColor = fabPrimary.copy(alpha = 0.40f),
                                ambientColor = fabPrimary.copy(alpha = 0.20f)
                            )
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(
                                interactionSource = calcInteractionSource,
                                indication = null,
                                onClick = { showCalculatorSheet = true }
                            )
                            .testTag("floating_calculator_fab"),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            fabPrimary,
                                            fabSecondary
                                        )
                                    )
                                )
                                .padding(horizontal = 15.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = "ভাসমান ক্যালকুলেটর",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "হিসাব",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Smart Retail Calculator Modal Bottom Sheet
    if (showCalculatorSheet) {
        val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()
        SmartRetailCalculatorBottomSheet(
            customers = allCustomers,
            onDismiss = { showCalculatorSheet = false },
            onAddCashSale = { amount, note ->
                viewModel.recordSale(isCash = true, amount = amount, note = note)
            },
            onAddBakiSale = { custId, amount, note ->
                val custName = allCustomers.find { it.id == custId }?.name ?: "কাস্টমার"
                viewModel.recordSale(isCash = false, amount = amount, customerId = custId, customerName = custName, note = note)
            },
            onAddExpense = { amount, isShop, note ->
                viewModel.recordExpense(amount = amount, description = note, isHome = !isShop)
            },
            onAddFordiItem = { name, qty, unit, rate ->
                viewModel.addFordiItem(
                    productName = name,
                    plannedQty = qty,
                    unit = unit,
                    purchaseRate = rate,
                    sellingRate = rate * 1.15
                )
            },
            onApplyToCash = { amount ->
                val selectedMillis = viewModel.selectedHomeDateMillis.value
                val dateKey = BengaliUtils.formatDateKey(selectedMillis)
                viewModel.saveDailyClosingCash(dateKey, selectedMillis, amount, isClosed = true)
            }
        )
    }

    // Modal Quick Expense Drawer (Persists open for consecutive entries)
    if (showExpenseDrawer) {
        QuickExpenseDrawer(
            viewModel = viewModel,
            onDismiss = { showExpenseDrawer = false },
            initialTarget = expenseDrawerInitialTarget,
            sheetState = expenseSheetState
        )
    }

    // User Mode Preference Dialog
    if (showModePreferenceDialog) {
        UserModePreferenceDialog(
            currentMode = appMode,
            isFirstTime = false,
            onDismiss = { showModePreferenceDialog = false },
            onSelectMode = { selected ->
                viewModel.setAppMode(selected) {
                    showModePreferenceDialog = false
                }
            }
        )
    }

    // Shop Settings Dialog
    if (showSettingsDialog) {
        val settings = viewModel.shopSettings.value
        val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
        ShopSettingsDialog(
            currentSettings = settings,
            currentThemeMode = themeMode,
            onDismiss = { showSettingsDialog = false },
            onSave = { updated ->
                viewModel.updateSettings(updated)
                showSettingsDialog = false
            },
            onThemeChanged = { newTheme ->
                viewModel.setThemeMode(newTheme)
            }
        )
    }

    // Accounting Guide Dialog
    if (showAccountingGuideDialog) {
        AccountingGuideDialog(onDismiss = { showAccountingGuideDialog = false })
    }

    // Backup and Restore Dialog (JSON, CSV, Image Memos)
    if (showBackupRestoreDialog) {
        BackupRestoreDialog(
            viewModel = viewModel,
            onDismiss = { showBackupRestoreDialog = false },
            onOpenSupabaseCloud = { showSupabaseCloudDialog = true }
        )
    }

    // Supabase Cloud Sync & Backup Dialog
    if (showSupabaseCloudDialog) {
        SupabaseCloudDialog(
            viewModel = viewModel,
            onDismiss = { showSupabaseCloudDialog = false }
        )
    }

    // Digital Payment QR Code & Counter Collection Dialog
    if (showPaymentQrDialog) {
        val settings = viewModel.shopSettings.value
        PaymentQrDialog(
            settings = settings,
            onDismiss = { showPaymentQrDialog = false },
            onEditShopProfile = {
                showPaymentQrDialog = false
                showSettingsDialog = true
            }
        )
    }

    // App Security PIN Lock Settings Dialog
    if (showSecuritySettingsDialog) {
        val settings = viewModel.shopSettings.value
        AppSecuritySettingsDialog(
            shopPhone = settings?.phone ?: "",
            onDismiss = { showSecuritySettingsDialog = false },
            onLockNow = {
                showSecuritySettingsDialog = false
                AppLockManager.lockNow()
                isAppLocked = true
            }
        )
    }

    // Fullscreen App Security PIN Lock Screen
    if (isAppLocked) {
        val settings = viewModel.shopSettings.value
        AppLockScreen(
            shopName = settings?.shopName,
            onUnlocked = {
                isAppLocked = false
                AppLockManager.setUnlocked()
            }
        )
    }
}
