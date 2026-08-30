package com.example.mawa.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mawa.data.model.AppMode
import com.example.mawa.ui.components.BackupRestoreDialog
import com.example.mawa.ui.components.MawaBottomBar
import com.example.mawa.ui.components.MawaSideDrawer
import com.example.mawa.ui.components.MawaTab
import com.example.mawa.ui.components.SupabaseCloudDialog
import com.example.mawa.ui.components.UserModePreferenceDialog
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
                                    }
                                )
                            }
                            MawaTab.FORDI -> {
                                FordiScreen(
                                    viewModel = viewModel,
                                    onOpenDrawer = {
                                        coroutineScope.launch { drawerState.open() }
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
            }
        }
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
}
