package com.example.mawa.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mawa.data.local.MawaDatabase
import com.example.mawa.data.local.entity.CashTallyEntity
import com.example.mawa.data.local.entity.CompanyOrderEntity
import com.example.mawa.data.local.entity.CustomerEntity
import com.example.mawa.data.local.entity.FordiItemEntity
import com.example.mawa.data.local.entity.PersonalTransactionEntity
import com.example.mawa.data.local.entity.PersonalTransactionType
import com.example.mawa.data.local.entity.ProductEntity
import com.example.mawa.data.local.entity.ShopSettingsEntity
import com.example.mawa.data.local.entity.SupplierCompanyEntity
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType
import com.example.mawa.data.model.AccountingSummary
import com.example.mawa.data.model.AppMode
import com.example.mawa.data.model.AppThemeMode
import com.example.mawa.data.model.CustomerWithBalance
import com.example.mawa.data.model.PersonalSummary
import com.example.mawa.data.model.ProductStats
import com.example.mawa.data.model.SimilarNameGroup
import com.example.mawa.data.model.TimeFilter
import com.example.mawa.data.remote.supabase.CloudBackupItem
import com.example.mawa.data.remote.supabase.CloudOperationResult
import com.example.mawa.data.remote.supabase.SupabaseAuthManager
import com.example.mawa.data.remote.supabase.SupabaseAuthResult
import com.example.mawa.data.remote.supabase.SupabaseDbManager
import com.example.mawa.data.remote.supabase.SupabaseUser
import com.example.mawa.data.remote.firebase.FirebaseAuthManager
import com.example.mawa.data.remote.firebase.FirebaseAuthResult
import com.example.mawa.data.remote.firebase.FirebaseBackupItem
import com.example.mawa.data.remote.firebase.FirebaseDbManager
import com.example.mawa.data.remote.firebase.FirebaseOperationResult
import com.example.mawa.data.remote.firebase.FirebaseUserProfile
import com.example.mawa.data.repository.MawaRepository
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.CashChimePlayer
import com.example.mawa.util.SimilarNameDetector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MawaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MawaDatabase.getDatabase(application)
    val repository = MawaRepository(database)

    // Theme Mode Preference
    private val appPrefs = application.getSharedPreferences("mawa_app_prefs", Context.MODE_PRIVATE)
    private val _themeMode = MutableStateFlow(
        AppThemeMode.fromKey(appPrefs.getString("app_theme_mode", AppThemeMode.LIGHT.key))
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        appPrefs.edit().putString("app_theme_mode", mode.key).apply()
    }

    // Cash Chime Sound & Daily Backup Reminder Preferences
    private val _isCashSoundEnabled = MutableStateFlow(
        appPrefs.getBoolean("is_cash_sound_enabled", true)
    )
    val isCashSoundEnabled: StateFlow<Boolean> = _isCashSoundEnabled.asStateFlow()

    fun setCashSoundEnabled(enabled: Boolean) {
        _isCashSoundEnabled.value = enabled
        appPrefs.edit().putBoolean("is_cash_sound_enabled", enabled).apply()
    }

    private val _isDailyBackupReminderEnabled = MutableStateFlow(
        appPrefs.getBoolean("is_backup_reminder_enabled", true)
    )
    val isDailyBackupReminderEnabled: StateFlow<Boolean> = _isDailyBackupReminderEnabled.asStateFlow()

    fun setDailyBackupReminderEnabled(enabled: Boolean) {
        _isDailyBackupReminderEnabled.value = enabled
        appPrefs.edit().putBoolean("is_backup_reminder_enabled", enabled).apply()
    }

    // Supabase Cloud Integration
    val supabaseAuthManager = SupabaseAuthManager(application)
    val supabaseDbManager = SupabaseDbManager(supabaseAuthManager)
    val mawaSyncManager = com.example.mawa.data.remote.supabase.MawaSyncManager(
        application,
        repository,
        supabaseAuthManager,
        supabaseDbManager
    )

    val currentUser: StateFlow<SupabaseUser?> = supabaseAuthManager.currentUser
    val isAuthLoading: StateFlow<Boolean> = supabaseAuthManager.isLoading

    private val _cloudBackups = MutableStateFlow<List<CloudBackupItem>>(emptyList())
    val cloudBackups: StateFlow<List<CloudBackupItem>> = _cloudBackups.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    // Google Firebase Integration
    val firebaseAuthManager = FirebaseAuthManager(application)
    val firebaseDbManager = FirebaseDbManager(application, firebaseAuthManager)

    val currentFirebaseUser: StateFlow<FirebaseUserProfile?> = firebaseAuthManager.currentUser
    val isFirebaseAuthLoading: StateFlow<Boolean> = firebaseAuthManager.isLoading

    private val _firebaseBackups = MutableStateFlow<List<FirebaseBackupItem>>(emptyList())
    val firebaseBackups: StateFlow<List<FirebaseBackupItem>> = _firebaseBackups.asStateFlow()

    private val _isFirebaseSyncing = MutableStateFlow(false)
    val isFirebaseSyncing: StateFlow<Boolean> = _isFirebaseSyncing.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.seedInitialDataIfEmpty()
                repository.seedInitialCompanyOrdersIfEmpty()
            } catch (e: Throwable) {
                android.util.Log.e("MawaViewModel", "Seed error", e)
            }
        }
        viewModelScope.launch {
            try {
                currentUser.collect { user ->
                    if (user != null) {
                        loadCloudBackups()
                        // Instant live sync upon session detection
                        mawaSyncManager.syncImmediatelyOnLogin { _, _ ->
                            loadCloudBackups()
                        }
                    } else {
                        _cloudBackups.value = emptyList()
                    }
                }
            } catch (e: Throwable) {
                android.util.Log.e("MawaViewModel", "Cloud backups collect error", e)
            }
        }
        viewModelScope.launch {
            try {
                currentFirebaseUser.collect { user ->
                    if (user != null) {
                        loadFirebaseBackups()
                    } else {
                        _firebaseBackups.value = emptyList()
                    }
                }
            } catch (e: Throwable) {
                android.util.Log.e("MawaViewModel", "Firebase user collect error", e)
            }
        }
    }

    fun notifyDataChanged() {
        mawaSyncManager.scheduleDebouncedAutoSync(500L)
    }

    // Selected Date for Home Screen (allows navigating to past days)
    private val _selectedHomeDateMillis = MutableStateFlow(System.currentTimeMillis())
    val selectedHomeDateMillis: StateFlow<Long> = _selectedHomeDateMillis.asStateFlow()

    fun setSelectedHomeDate(millis: Long) {
        _selectedHomeDateMillis.value = millis
    }

    fun goToPreviousDay() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _selectedHomeDateMillis.value
            add(Calendar.DAY_OF_YEAR, -1)
        }
        _selectedHomeDateMillis.value = cal.timeInMillis
    }

    fun goToNextDay() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _selectedHomeDateMillis.value
            add(Calendar.DAY_OF_YEAR, 1)
        }
        _selectedHomeDateMillis.value = cal.timeInMillis
    }

    fun resetToToday() {
        _selectedHomeDateMillis.value = System.currentTimeMillis()
    }

    val accountingSummary: StateFlow<AccountingSummary> = repository.accountingSummary
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AccountingSummary()
        )

    val selectedDateAccountingSummary: StateFlow<AccountingSummary> = combine(
        repository.allTransactions,
        repository.shopSettings,
        repository.allCustomers,
        repository.allDailyCash,
        _selectedHomeDateMillis
    ) { transactions, settings, customers, dailyCashList, dateMillis ->
        repository.calculateSummaryForDate(transactions, settings, customers, dateMillis, dailyCashList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountingSummary()
    )

    val allCustomers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val customersWithBalance: StateFlow<List<CustomerWithBalance>> = repository.customersWithBalance
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentTransactions: StateFlow<List<TransactionEntity>> = repository.recentTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedDateTransactions: StateFlow<List<TransactionEntity>> = combine(
        repository.allTransactions,
        _selectedHomeDateMillis
    ) { transactions, dateMillis ->
        val start = MawaRepository.getStartOfDayMillis(dateMillis)
        val end = MawaRepository.getEndOfDayMillis(dateMillis)
        transactions.filter { it.timestamp in start..end }.sortedByDescending { it.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeProducts: StateFlow<List<ProductEntity>> = repository.activeProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pendingFordiItems: StateFlow<List<FordiItemEntity>> = repository.pendingFordiItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allFordiItems: StateFlow<List<FordiItemEntity>> = repository.allFordiItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPurchases: StateFlow<List<TransactionEntity>> = repository.allPurchases
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val shopSettings: StateFlow<ShopSettingsEntity?> = repository.shopSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val currentAppMode: StateFlow<AppMode> = repository.shopSettings
        .map { settings -> AppMode.fromKey(settings?.appMode) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppMode.BOTH
        )

    val isModeConfigured: StateFlow<Boolean> = repository.shopSettings
        .map { settings -> settings?.isModeConfigured ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val allSupplierCompanies: StateFlow<List<SupplierCompanyEntity>> = repository.allSupplierCompanies
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCashTallies: StateFlow<List<CashTallyEntity>> = repository.allCashTallies
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCompanyOrders: StateFlow<List<CompanyOrderEntity>> = repository.allCompanyOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addCompanyOrder(
        companyName: String,
        amount: Double,
        note: String,
        dateString: String = "",
        orderDate: Long = System.currentTimeMillis()
    ) {
        val resolvedDateString = dateString.ifBlank { com.example.mawa.util.BengaliUtils.getFormattedTodayDate(orderDate) }
        viewModelScope.launch {
            repository.saveCompanyOrder(
                CompanyOrderEntity(
                    companyName = companyName.trim(),
                    totalAmount = amount,
                    orderDate = orderDate,
                    orderDateString = resolvedDateString,
                    note = note.trim(),
                    status = "PENDING"
                )
            )
            notifyDataChanged()
        }
    }

    fun confirmCompanyOrder(order: CompanyOrderEntity, recordAsPurchase: Boolean = true) {
        viewModelScope.launch {
            repository.confirmCompanyOrder(order.id, order, recordAsPurchase)
            notifyDataChanged()
        }
    }

    fun cancelCompanyOrder(orderId: Long) {
        viewModelScope.launch {
            repository.cancelCompanyOrder(orderId)
            notifyDataChanged()
        }
    }

    fun deleteCompanyOrder(orderId: Long) {
        viewModelScope.launch {
            repository.deleteCompanyOrder(orderId)
            notifyDataChanged()
        }
    }

    val pendingSupplierPayments: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .map { list ->
            list.filter {
                (it.type == TransactionType.EXPENSE_SHOP || it.type == TransactionType.PURCHASE_DIRECT || it.type == TransactionType.PURCHASE_FORDI) &&
                it.paymentStatus == "DUE_PAYMENT"
            }.sortedByDescending { it.timestamp }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _dismissedSimilarClusters = MutableStateFlow<Set<Set<String>>>(emptySet())

    val detectedSimilarNameGroups: StateFlow<List<SimilarNameGroup>> = combine(
        repository.allTransactions,
        repository.allFordiItems,
        repository.allProducts,
        repository.allSupplierCompanies,
        _dismissedSimilarClusters
    ) { txs, fordis, prods, companies, dismissed ->
        val groups = SimilarNameDetector.findSimilarGroups(txs, fordis, prods, companies)
        groups.filter { group ->
            val set = group.variations.toSet()
            dismissed.none { d -> d == set || d.containsAll(set) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Personal Mode Flows
    val allPersonalTransactions: StateFlow<List<PersonalTransactionEntity>> = repository.allPersonalTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _personalTimeFilter = MutableStateFlow(TimeFilter.TODAY)
    val personalTimeFilter: StateFlow<TimeFilter> = _personalTimeFilter.asStateFlow()

    val personalSummary: StateFlow<PersonalSummary> = combine(
        allPersonalTransactions,
        _personalTimeFilter
    ) { transactions, filter ->
        repository.calculatePersonalSummary(transactions, filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PersonalSummary()
    )

    // Time filter for Reports & Home accounting
    private val _reportTimeFilter = MutableStateFlow(TimeFilter.THIS_MONTH)
    val reportTimeFilter: StateFlow<TimeFilter> = _reportTimeFilter.asStateFlow()

    private val _homeExpenseTimeFilter = MutableStateFlow(TimeFilter.THIS_MONTH)
    val homeExpenseTimeFilter: StateFlow<TimeFilter> = _homeExpenseTimeFilter.asStateFlow()

    val filteredTransactionsForReport: StateFlow<List<TransactionEntity>> = _reportTimeFilter
        .flatMapLatest { filter -> repository.getTransactionsForPeriod(filter) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredHomeExpenses: StateFlow<List<TransactionEntity>> = _homeExpenseTimeFilter
        .flatMapLatest { filter -> repository.getHomeExpenses(filter) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // User feedback message flow
    private val _feedbackMessage = MutableSharedFlow<String>()
    val feedbackMessage: SharedFlow<String> = _feedbackMessage.asSharedFlow()

    fun setReportTimeFilter(filter: TimeFilter) {
        _reportTimeFilter.value = filter
    }

    fun setHomeExpenseTimeFilter(filter: TimeFilter) {
        _homeExpenseTimeFilter.value = filter
    }

    fun recordSale(
        isCash: Boolean,
        amount: Double,
        customerId: Long? = null,
        customerName: String? = null,
        note: String = "",
        timestamp: Long = System.currentTimeMillis(),
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (amount <= 0) return@launch
            repository.recordSale(isCash, amount, customerId, customerName, note, timestamp)
            CashChimePlayer.playCashChime(getApplication())
            val typeStr = if (isCash) "নগদ বিক্রি" else "বাকি বিক্রি"
            _feedbackMessage.emit("$typeStr সফলভাবে সংরক্ষিত হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun recordBaki(
        customerId: Long,
        customerName: String,
        amount: Double,
        note: String = "",
        timestamp: Long = System.currentTimeMillis(),
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (amount <= 0) return@launch
            repository.recordBakiEntry(customerId, customerName, amount, note, timestamp)
            CashChimePlayer.playCashChime(getApplication())
            _feedbackMessage.emit("$customerName-কে বাকি ৳$amount দেওয়া হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun recordJoma(
        customerId: Long,
        customerName: String,
        amount: Double,
        note: String = "",
        timestamp: Long = System.currentTimeMillis(),
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (amount <= 0) return@launch
            repository.recordJomaEntry(customerId, customerName, amount, note, timestamp)
            CashChimePlayer.playCashChime(getApplication())
            _feedbackMessage.emit("$customerName-এর থেকে জমা ৳$amount পাওয়া গেছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun recordExpense(
        amount: Double,
        description: String,
        isHome: Boolean,
        timestamp: Long = System.currentTimeMillis(),
        paymentStatus: String = "PAID",
        promisedPaymentDate: Long = 0L,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (amount <= 0) return@launch
            repository.recordExpense(amount, description, isHome, timestamp, paymentStatus, promisedPaymentDate)
            val targetStr = if (isHome) "বাড়ির জন্য" else "দোকানের"
            val statusNote = if (paymentStatus == "DUE_PAYMENT") " (বিকালে পেমেন্ট বাকি)" else ""
            _feedbackMessage.emit("$targetStr খরচ ৳$amount সংরক্ষিত হয়েছে$statusNote")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun recordHomeGoodsWithdrawal(
        productName: String,
        productId: Long? = null,
        quantity: Double,
        unit: String,
        rate: Double,
        isDeductFromCash: Boolean,
        note: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (productId != null) {
                val currentProducts = activeProducts.value
                val existing = currentProducts.find { it.id == productId }
                if (existing != null && existing.stockQuantity > 0) {
                    val newStock = (existing.stockQuantity - quantity).coerceAtLeast(0.0)
                    repository.updateProduct(existing.copy(stockQuantity = newStock))
                }
            }
            repository.recordHomeGoodsWithdrawal(productName, productId, quantity, unit, rate, isDeductFromCash, note)
            val cashNote = if (isDeductFromCash) " (ক্যাশ থেকে কর্তিত)" else " (ক্যাশ অপরিবর্তিত)"
            _feedbackMessage.emit("$productName ($quantity $unit) সংসারের খরচে যুক্ত হয়েছে$cashNote")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun recordDirectPurchase(
        productName: String,
        productId: Long? = null,
        quantity: Double,
        unit: String,
        rate: Double,
        total: Double = 0.0,
        note: String = "",
        timestamp: Long = System.currentTimeMillis(),
        paymentStatus: String = "PAID",
        promisedPaymentDate: Long = 0L,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val finalTotal = if (total > 0) total else (quantity * rate)
            if (finalTotal <= 0) return@launch
            repository.recordDirectPurchase(productName, productId, quantity, unit, rate, finalTotal, note, timestamp, paymentStatus, promisedPaymentDate)
            val statusNote = if (paymentStatus == "DUE_PAYMENT") " (বিকালে পেমেন্ট বাকি)" else ""
            _feedbackMessage.emit("মাল ক্রয় ৳$finalTotal সংরক্ষিত হয়েছে$statusNote")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun convertFordiToPurchase(
        item: FordiItemEntity,
        actualQty: Double,
        actualRate: Double,
        paymentStatus: String = "PAID",
        promisedPaymentDate: Long = 0L,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (actualQty <= 0 || actualRate <= 0) return@launch
            repository.convertFordiToPurchase(item, actualQty, actualRate, paymentStatus, promisedPaymentDate)
            val total = actualQty * actualRate
            val statusNote = if (paymentStatus == "DUE_PAYMENT") " (বিকালে পেমেন্ট বাকি)" else ""
            _feedbackMessage.emit("${item.productName} ক্রয় ৳$total যুক্ত হয়েছে$statusNote")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun saveSupplierCompany(company: SupplierCompanyEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveSupplierCompany(company)
            _feedbackMessage.emit("${company.companyName} সংরক্ষিত হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun deleteSupplierCompany(id: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteSupplierCompany(id)
            _feedbackMessage.emit("কোম্পানি ডিলিট হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun updateTransactionPaymentStatus(id: Long, status: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateTransactionPaymentStatus(id, status)
            val msg = if (status == "PAID") "পেমেন্ট নগদ পরিশোধ সম্পন্ন হয়েছে" else "পেমেন্ট আপডেট হয়েছে"
            _feedbackMessage.emit(msg)
            notifyDataChanged()
            onSuccess()
        }
    }

    fun mergeEntityNames(oldName: String, newName: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.mergeEntityNames(oldName, newName)
            _feedbackMessage.emit("'$oldName' সফলভাবে '$newName' নামে একীভূত করা হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun markSimilarGroupAsSeparate(group: SimilarNameGroup, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _dismissedSimilarClusters.value = _dismissedSimilarClusters.value + setOf(group.variations.toSet())
            _feedbackMessage.emit("✓ '${group.primaryName}' ও অন্যান্য ভ্যারিয়েশন আলাদা হিসেবে নিশ্চিত করা হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun convertMultipleFordiToPurchases(
        items: List<FordiItemEntity>,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (items.isEmpty()) return@launch
            val totalAmount = items.sumOf { (if (it.actualQuantity > 0) it.actualQuantity else it.plannedQuantity) * (if (it.actualRate > 0) it.actualRate else it.purchaseRate) }
            repository.convertMultipleFordiToPurchases(items)
            _feedbackMessage.emit("${items.size}টি পণ্য সফলভাবে খরচে যুক্ত হয়েছে (মোট ৳${totalAmount.toInt()})")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun updateFordiItem(
        item: FordiItemEntity,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.updateFordiItem(item)
            _feedbackMessage.emit("${item.productName} আপডেট হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun clearPendingFordi(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearPendingFordi()
            _feedbackMessage.emit("নতুন ফর্দ তৈরি করার জন্য তালিকা পরিষ্কার করা হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun reAddPurchasedItemsToFordi(
        items: List<FordiItemEntity>,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (items.isEmpty()) return@launch
            repository.reAddPurchasedItemsToFordi(items)
            _feedbackMessage.emit("${items.size}টি পণ্য নতুন ফর্দে যোগ করা হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun addFordiItem(
        productName: String,
        productId: Long? = null,
        plannedQty: Double,
        unit: String,
        purchaseRate: Double,
        sellingRate: Double,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (productName.isBlank() || plannedQty <= 0) return@launch
            repository.addFordiItem(productName, productId, plannedQty, unit, purchaseRate, sellingRate)
            _feedbackMessage.emit("ফর্দে $productName যুক্ত হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun deleteFordiItem(id: Long) {
        viewModelScope.launch {
            repository.deleteFordiItem(id)
            _feedbackMessage.emit("ফর্দ থেকে সরানো হয়েছে")
            notifyDataChanged()
        }
    }

    fun clearCompletedFordi() {
        viewModelScope.launch {
            repository.clearCompletedFordi()
            _feedbackMessage.emit("কেনা ফর্দ তালিকা পরিষ্কার করা হয়েছে")
            notifyDataChanged()
        }
    }

    fun clearAllFordiItems(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteAllFordiItems()
            _feedbackMessage.emit("সম্পূর্ণ ফর্দ তালিকা খালি করা হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun addMultipleFordiTemplateItems(
        items: List<com.example.mawa.util.FordiTemplateItem>,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (items.isEmpty()) return@launch
            val entities = items.map {
                FordiItemEntity(
                    productName = it.productName,
                    plannedQuantity = if (it.plannedQuantity > 0) it.plannedQuantity else 1.0,
                    unit = it.unit.ifBlank { "টি" },
                    purchaseRate = it.purchaseRate,
                    sellingRate = 0.0
                )
            }
            repository.reAddPurchasedItemsToFordi(entities)
            _feedbackMessage.emit("${items.size}টি আইটেম ফর্দে যোগ হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun addCustomer(
        name: String,
        phone: String,
        address: String,
        openingBalance: Double,
        creditLimit: Double = 0.0,
        promisedPaymentDate: Long = 0L,
        categoryTag: String = "REGULAR",
        nidOrGuarantor: String = "",
        note: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            val customer = CustomerEntity(
                name = name.trim(),
                phone = phone.trim(),
                address = address.trim(),
                openingBalance = openingBalance,
                creditLimit = creditLimit,
                promisedPaymentDate = promisedPaymentDate,
                categoryTag = categoryTag,
                nidOrGuarantor = nidOrGuarantor.trim(),
                note = note.trim()
            )
            val newId = repository.addCustomer(customer)
            mawaSyncManager.pushCustomerLive(customer.copy(id = newId))
            _feedbackMessage.emit("কাস্টমার $name যুক্ত হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun updateCustomerPromiseDate(
        customerId: Long,
        promiseDateMillis: Long,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val customer = allCustomers.value.find { it.id == customerId } ?: return@launch
            val updated = customer.copy(promisedPaymentDate = promiseDateMillis)
            repository.updateCustomer(updated)
            mawaSyncManager.pushCustomerLive(updated)
            _feedbackMessage.emit("টাকা দেওয়ার তারিখ নির্ধারিত হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun settleCustomerAccountWithDiscount(
        customerId: Long,
        customerName: String,
        cashPaid: Double,
        discountGiven: Double,
        note: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (cashPaid > 0) {
                repository.recordJomaEntry(
                    customerId = customerId,
                    customerName = customerName,
                    amount = cashPaid,
                    note = note.ifBlank { "বাকি নিষ্পত্তি (নগদ জমা)" }
                )
            }
            if (discountGiven > 0) {
                repository.recordJomaEntry(
                    customerId = customerId,
                    customerName = customerName,
                    amount = discountGiven,
                    note = "হিসাব রফা ছাড় / ডিসকাউন্ট (৳${discountGiven.toInt()})"
                )
            }
            _feedbackMessage.emit("$customerName-এর খাতা নিষ্পত্তি সম্পন্ন হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun updateCustomer(customer: CustomerEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
            mawaSyncManager.pushCustomerLive(customer)
            _feedbackMessage.emit("${customer.name} তথ্য আপডেট হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun deleteCustomer(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomer(id)
            mawaSyncManager.deleteCustomerLive(id)
            _feedbackMessage.emit("কাস্টমার মুছে ফেলা হয়েছে")
            notifyDataChanged()
        }
    }

    fun addProduct(
        name: String,
        banglaName: String,
        unit: String,
        purchasePrice: Double,
        sellingPrice: Double,
        category: String,
        barcode: String = "",
        stockQuantity: Double = 0.0,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            val product = ProductEntity(
                name = name.trim(),
                banglaName = banglaName.ifBlank { name }.trim(),
                barcode = barcode.trim(),
                unit = unit.trim(),
                defaultPurchasePrice = purchasePrice,
                defaultSellingPrice = sellingPrice,
                stockQuantity = stockQuantity,
                category = category.ifBlank { "সাধারণ" }.trim()
            )
            repository.addProduct(product)
            mawaSyncManager.pushProductLive(product)
            _feedbackMessage.emit("পণ্য $name যুক্ত হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun updateProduct(product: ProductEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateProduct(product)
            mawaSyncManager.pushProductLive(product)
            _feedbackMessage.emit("${product.name} আপডেট হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            repository.deleteProduct(id)
            mawaSyncManager.deleteProductLive(id)
            _feedbackMessage.emit("পণ্য মুছে ফেলা হয়েছে")
            notifyDataChanged()
        }
    }

    fun seedStandardProductsIfEmpty(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.seedStandardProductsIfEmpty()
            _feedbackMessage.emit("স্ট্যান্ডার্ড মুদি ও কাঁচাবাজার স্টক লোড হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun updateSettings(settings: ShopSettingsEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            _feedbackMessage.emit("দোকানের সেটিংস সংরক্ষিত হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun updateOpeningBalance(amount: Double, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val current = shopSettings.value ?: ShopSettingsEntity(
                id = 1,
                shopName = "মাওয়া ডিজিটাল খাতা",
                ownerName = "দোকানদার",
                openingBalance = amount,
                currencySymbol = "৳"
            )
            repository.updateSettings(current.copy(openingBalance = amount))
            _feedbackMessage.emit("সাবেক ক্যাশ ৳$amount আপডেট হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            mawaSyncManager.deleteTransactionLive(id)
            _feedbackMessage.emit("লেনদেন বাতিল করা হয়েছে")
            notifyDataChanged()
        }
    }

    // Product Statistics Calculation
    fun getProductStats(product: ProductEntity, transactions: List<TransactionEntity>): ProductStats {
        val productPurchases = transactions.filter {
            (it.productId == product.id || it.productName.equals(product.name, ignoreCase = true) || it.productName.equals(product.banglaName, ignoreCase = true)) &&
                    (it.type == TransactionType.PURCHASE_FORDI || it.type == TransactionType.PURCHASE_DIRECT)
        }

        val totalQty = productPurchases.sumOf { it.quantity }
        val totalAmount = productPurchases.sumOf { it.amount }
        val count = productPurchases.size

        val rates = productPurchases.mapNotNull {
            if (it.rate > 0) it.rate else if (it.quantity > 0) it.amount / it.quantity else null
        }

        val avgRate = if (totalQty > 0) totalAmount / totalQty else product.defaultPurchasePrice
        val latestRate = rates.firstOrNull() ?: product.defaultPurchasePrice
        val highestRate = rates.maxOrNull() ?: product.defaultPurchasePrice
        val lowestRate = rates.minOrNull() ?: product.defaultPurchasePrice
        val margin = if (product.defaultSellingPrice > 0) product.defaultSellingPrice - avgRate else 0.0

        return ProductStats(
            productId = product.id,
            productName = product.name,
            unit = product.unit,
            totalPurchasedQty = totalQty,
            totalPurchasedAmount = totalAmount,
            purchaseCount = count,
            avgPurchasePrice = avgRate,
            latestPurchasePrice = latestRate,
            highestPurchasePrice = highestRate,
            lowestPurchasePrice = lowestRate,
            sellingPrice = product.defaultSellingPrice,
            estimatedMargin = margin,
            purchaseHistory = productPurchases
        )
    }

    // Duplicate detection
    fun findPotentialDuplicates(products: List<ProductEntity>): List<Pair<ProductEntity, ProductEntity>> {
        val duplicates = mutableListOf<Pair<ProductEntity, ProductEntity>>()
        for (i in products.indices) {
            for (j in i + 1 until products.size) {
                val p1 = products[i]
                val p2 = products[j]
                val n1 = p1.name.lowercase().trim().replace(" ", "")
                val n2 = p2.name.lowercase().trim().replace(" ", "")

                val isMatch = n1 == n2 ||
                        (n1.contains(n2) && n2.length >= 3) ||
                        (n2.contains(n1) && n1.length >= 3) ||
                        p1.banglaName.trim() == p2.banglaName.trim()

                if (isMatch) {
                    duplicates.add(Pair(p1, p2))
                }
            }
        }
        return duplicates
    }

    fun mergeProducts(canonical: ProductEntity, duplicateId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.mergeProducts(canonical, duplicateId)
            _feedbackMessage.emit("পণ্য সফলভাবে একত্রিত করা হয়েছে")
            onSuccess()
        }
    }

    // --- Personal Mode Operations ---

    fun setPersonalTimeFilter(filter: TimeFilter) {
        _personalTimeFilter.value = filter
    }

    fun setAppMode(mode: AppMode, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateAppMode(mode)
            val msg = when (mode) {
                AppMode.BOTH -> "মোড: দোকান ও ব্যক্তিগত দুটোই চালু হয়েছে"
                AppMode.PERSONAL_ONLY -> "মোড: শুধু ব্যক্তিগত হিসাব চালু হয়েছে"
                AppMode.BUSINESS_ONLY -> "মোড: শুধু ব্যবসার হিসাব চালু হয়েছে"
            }
            _feedbackMessage.emit(msg)
            onSuccess()
        }
    }

    fun recordPersonalTransaction(
        type: PersonalTransactionType,
        amount: Double,
        title: String,
        category: String,
        note: String = "",
        timestamp: Long = System.currentTimeMillis(),
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (amount <= 0 || title.isBlank()) return@launch
            repository.recordPersonalTransaction(
                type = type,
                amount = amount,
                title = title,
                category = category,
                note = note,
                timestamp = timestamp
            )
            val typeStr = when (type) {
                PersonalTransactionType.EXPENSE -> "ব্যক্তিগত খরচ"
                PersonalTransactionType.INCOME -> "আয়"
                PersonalTransactionType.SAVINGS -> "সঞ্চয়"
            }
            _feedbackMessage.emit("$typeStr ৳$amount যুক্ত হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun updatePersonalTransaction(
        transaction: PersonalTransactionEntity,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.updatePersonalTransaction(transaction)
            _feedbackMessage.emit("হিসাব আপডেট করা হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun deletePersonalTransaction(id: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deletePersonalTransaction(id)
            _feedbackMessage.emit("হিসাব মুছে ফেলা হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    // --- Quick Baki & Joma Actions ---

    fun quickAddBaki(customerId: Long, customerName: String, amount: Double, note: String = "", onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (amount <= 0) return@launch
            repository.addQuickBaki(customerId, customerName, amount, note)
            _feedbackMessage.emit("$customerName-এর খতিয়ানে ৳$amount বাকি যোগ হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    fun quickAddJoma(customerId: Long, customerName: String, amount: Double, note: String = "", onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (amount <= 0) return@launch
            repository.addQuickJoma(customerId, customerName, amount, note)
            _feedbackMessage.emit("$customerName-এর থেকে ৳$amount জমা পাওয়া গেছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    // --- Backup & Restore Engine ---

    suspend fun exportFullBackupJson(): String {
        val backupData = repository.getFullBackupData()
        return com.example.mawa.util.DataBackupRestoreManager.exportToJsonString(backupData)
    }

    fun parseBackupPreview(jsonString: String): com.example.mawa.util.FullBackupData? {
        return try {
            com.example.mawa.util.DataBackupRestoreManager.parseFromJsonString(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun restoreFullBackupFromJson(
        jsonString: String, 
        overwriteExisting: Boolean = true,
        onComplete: (Boolean, String, com.example.mawa.util.FullBackupData?) -> Unit = { _, _, _ -> }
    ): Boolean {
        return try {
            val backupData = com.example.mawa.util.DataBackupRestoreManager.parseFromJsonString(jsonString)
            repository.restoreFullBackup(backupData, overwriteExisting)
            notifyDataChanged()
            
            // If the restored backup contains transactions, update selected date to latest transaction or today
            val latestTxTimestamp = backupData.transactions.maxOfOrNull { it.timestamp }
            if (latestTxTimestamp != null && latestTxTimestamp > 0) {
                _selectedHomeDateMillis.value = latestTxTimestamp
            }

            val summaryMsg = "ডাটা সফলভাবে রিস্টোর হয়েছে! (কাস্টমার: ${backupData.customers.size} জন, লেনদেন: ${backupData.transactions.size} টি, ফর্দ: ${backupData.fordiItems.size} টি, পণ্য: ${backupData.products.size} টি)"
            _feedbackMessage.emit(summaryMsg)
            onComplete(true, summaryMsg, backupData)
            true
        } catch (e: Exception) {
            val err = "রিস্টোর ব্যর্থ হয়েছে: ${e.localizedMessage ?: "অজ্ঞাত ত্রুটি"}"
            _feedbackMessage.emit(err)
            onComplete(false, err, null)
            false
        }
    }

    fun clearAllSampleData(clearSettings: Boolean = false, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearAllData(clearSettings)
            mawaSyncManager.clearAllCloudRecordsLive()
            _feedbackMessage.emit("সকল ডামি ও স্যাম্পল ডাটা মুছে ফেলা হয়েছে")
            notifyDataChanged()
            onSuccess()
        }
    }

    suspend fun importCustomersFromCsv(csvText: String): Int {
        return try {
            val customers = com.example.mawa.util.DataBackupRestoreManager.parseCustomersFromCsv(csvText)
            if (customers.isNotEmpty()) {
                repository.importCustomers(customers)
                _feedbackMessage.emit("${customers.size} জন কাস্টমার সফলভাবে ইম্পোর্ট হয়েছে!")
            } else {
                _feedbackMessage.emit("ফাইল থেকে কোনো কাস্টমার তথ্য পাওয়া যায়নি")
            }
            customers.size
        } catch (e: Exception) {
            _feedbackMessage.emit("CSV ইম্পোর্ট ব্যর্থ: ${e.localizedMessage}")
            0
        }
    }

    private val _lastCloudSyncTime = MutableStateFlow<String?>(null)
    val lastCloudSyncTime: StateFlow<String?> = _lastCloudSyncTime.asStateFlow()

    // --- Supabase Cloud Operations ---

    fun loginWithSupabase(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val res = supabaseAuthManager.signIn(email, pass)) {
                is SupabaseAuthResult.Success -> {
                    _feedbackMessage.emit("লগইন সফল! ক্লাউড ডাটা তাৎক্ষণিকভাবে লোড হচ্ছে...")
                    loadCloudBackups()
                    // Immediately sync & restore all Supabase data so it shows up in place instantly
                    mawaSyncManager.syncImmediatelyOnLogin { success, msg ->
                        loadCloudBackups()
                        val finalMsg = if (success) "লগইন ও লাইভ ডাটা লোড সফল! $msg" else res.message
                        viewModelScope.launch {
                            _feedbackMessage.emit(finalMsg)
                        }
                        onResult(true, finalMsg)
                    }
                }
                is SupabaseAuthResult.Error -> {
                    _feedbackMessage.emit(res.message)
                    onResult(false, res.message)
                }
                is SupabaseAuthResult.PasswordResetSent -> {
                    onResult(true, res.message)
                }
            }
        }
    }

    fun autoLoadLatestCloudDataIfEmpty() {
        viewModelScope.launch {
            if (!supabaseAuthManager.isLoggedIn()) return@launch
            val fullData = repository.getFullBackupData()
            val isLocalEmpty = fullData.customers.isEmpty() && fullData.transactions.isEmpty() && fullData.fordiItems.isEmpty()
            if (isLocalEmpty) {
                syncAndRestoreFromCloud { _, _ -> }
            }
        }
    }

    fun syncAndRestoreFromCloud(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        if (!supabaseAuthManager.isLoggedIn()) {
            onResult(false, "ক্লাউড সিঙ্কের জন্য আগে লগইন করুন")
            return
        }
        _isCloudSyncing.value = true
        mawaSyncManager.triggerSync(object : com.example.mawa.data.remote.supabase.MawaSyncManager.SyncCallback {
            override fun onSuccess(message: String) {
                _isCloudSyncing.value = false
                val timeNow = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.US).format(java.util.Date())
                _lastCloudSyncTime.value = timeNow
                viewModelScope.launch {
                    loadCloudBackups()
                    _feedbackMessage.emit(message)
                }
                onResult(true, message)
            }

            override fun onError(error: String) {
                _isCloudSyncing.value = false
                viewModelScope.launch {
                    _feedbackMessage.emit(error)
                }
                onResult(false, error)
            }
        })
    }

    fun triggerCloudSync(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        syncAndRestoreFromCloud(onResult)
    }

    fun saveDailySabekCash(dateKey: String, dateMillis: Long, sabekAmount: Double) {
        viewModelScope.launch {
            repository.saveDailySabekCash(dateKey, dateMillis, sabekAmount)
            _feedbackMessage.emit("সাবেক ক্যাশ ৳$sabekAmount সংরক্ষিত হয়েছে")
            notifyDataChanged()
        }
    }

    fun saveDailyClosingCash(dateKey: String, dateMillis: Long, closingAmount: Double, isClosed: Boolean = true) {
        viewModelScope.launch {
            repository.saveDailyClosingCash(dateKey, dateMillis, closingAmount, isClosed)
            _feedbackMessage.emit("দিনের ক্লোজিং ক্যাশ ৳$closingAmount সংরক্ষিত হয়েছে")
            notifyDataChanged()
        }
    }

    // --- Cash Tally (নোট গণনা ও এন্ট্রি) ---
    fun getCashTalliesForDateFlow(dateKey: String): Flow<List<CashTallyEntity>> =
        repository.getCashTalliesForDateFlow(dateKey)

    fun saveCashTally(
        tally: CashTallyEntity,
        updateClosingCash: Boolean = true,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            repository.saveCashTally(tally)
            if (updateClosingCash && tally.totalAmount > 0) {
                repository.saveDailyClosingCash(
                    dateKey = tally.dateKey,
                    dateMillis = tally.dateMillis,
                    closingAmount = tally.totalAmount,
                    isClosed = true
                )
            }
            _feedbackMessage.emit("নোট গণনার হিসাব (${BengaliUtils.formatTaka(tally.totalAmount)}) সংরক্ষিত হয়েছে")
            notifyDataChanged()
            onComplete?.invoke()
        }
    }

    fun deleteCashTally(id: Long) {
        viewModelScope.launch {
            repository.deleteCashTally(id)
            _feedbackMessage.emit("নোট গণনার রেকর্ড মুছে ফেলা হয়েছে")
            notifyDataChanged()
        }
    }

    fun registerWithSupabase(email: String, pass: String, name: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val res = supabaseAuthManager.signUp(email, pass, name)) {
                is SupabaseAuthResult.Success -> {
                    _feedbackMessage.emit(res.message)
                    loadCloudBackups()
                    onResult(true, res.message)
                }
                is SupabaseAuthResult.Error -> {
                    _feedbackMessage.emit(res.message)
                    onResult(false, res.message)
                }
                is SupabaseAuthResult.PasswordResetSent -> {
                    onResult(true, res.message)
                }
            }
        }
    }

    fun resetSupabasePassword(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val res = supabaseAuthManager.resetPassword(email)) {
                is SupabaseAuthResult.PasswordResetSent -> {
                    _feedbackMessage.emit(res.message)
                    onResult(true, res.message)
                }
                is SupabaseAuthResult.Error -> {
                    _feedbackMessage.emit(res.message)
                    onResult(false, res.message)
                }
                is SupabaseAuthResult.Success -> {
                    onResult(true, res.message)
                }
            }
        }
    }

    fun updateSupabasePassword(newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val res = supabaseAuthManager.updatePassword(newPass)) {
                is SupabaseAuthResult.Success -> {
                    _feedbackMessage.emit(res.message)
                    onResult(true, res.message)
                }
                is SupabaseAuthResult.Error -> {
                    _feedbackMessage.emit(res.message)
                    onResult(false, res.message)
                }
                is SupabaseAuthResult.PasswordResetSent -> {
                    onResult(true, res.message)
                }
            }
        }
    }

    fun logoutSupabase() {
        viewModelScope.launch {
            supabaseAuthManager.signOut()
            _cloudBackups.value = emptyList()
            _feedbackMessage.emit("সুপাবেজ ক্লাউড থেকে লগআউট করা হয়েছে")
        }
    }

    fun loadCloudBackups() {
        viewModelScope.launch {
            if (!supabaseAuthManager.isLoggedIn()) return@launch
            when (val res = supabaseDbManager.fetchCloudBackups()) {
                is CloudOperationResult.Success -> {
                    _cloudBackups.value = res.data
                }
                is CloudOperationResult.Error -> {
                    Log.e("MawaVM", "Error loading backups: ${res.message}")
                }
            }
        }
    }

    fun uploadBackupToSupabase(backupName: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            val fullData = repository.getFullBackupData()
            when (val res = supabaseDbManager.uploadBackupToCloud(backupName, fullData)) {
                is CloudOperationResult.Success -> {
                    _isCloudSyncing.value = false
                    _feedbackMessage.emit(res.message)
                    loadCloudBackups()
                    onResult(true, res.message)
                }
                is CloudOperationResult.Error -> {
                    _isCloudSyncing.value = false
                    _feedbackMessage.emit(res.message)
                    onResult(false, res.message)
                }
            }
        }
    }

    fun restoreCloudBackup(backupItem: CloudBackupItem, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            try {
                if (backupItem.dataJson.isBlank()) {
                    _isCloudSyncing.value = false
                    onResult(false, "ব্যাকআপের ডাটা ফাঁকা")
                    return@launch
                }
                val backupData = com.example.mawa.util.DataBackupRestoreManager.parseFromJsonString(backupItem.dataJson)
                repository.restoreFullBackup(backupData, overwriteExisting = true)
                _isCloudSyncing.value = false
                val msg = "সুপাবেজ ক্লাউড ব্যাকআপ '${backupItem.backupName}' সফলভাবে রিস্টোর হয়েছে!"
                _feedbackMessage.emit(msg)
                onResult(true, msg)
            } catch (e: Exception) {
                _isCloudSyncing.value = false
                val msg = "রিস্টোর ব্যর্থ: ${e.localizedMessage}"
                _feedbackMessage.emit(msg)
                onResult(false, msg)
            }
        }
    }

    fun deleteCloudBackup(backupId: Long, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val res = supabaseDbManager.deleteCloudBackup(backupId)) {
                is CloudOperationResult.Success -> {
                    loadCloudBackups()
                    _feedbackMessage.emit("ক্লাউড ব্যাকআপ সফলভাবে মুছে ফেলা হয়েছে")
                    onResult(true, "সফল")
                }
                is CloudOperationResult.Error -> {
                    _feedbackMessage.emit(res.message)
                    onResult(false, res.message)
                }
            }
        }
    }

    fun syncAllLocalRecordsToSupabase(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            val fullData = repository.getFullBackupData()
            when (val res = supabaseDbManager.syncAllRecordsToCloud(fullData)) {
                is CloudOperationResult.Success -> {
                    _isCloudSyncing.value = false
                    _feedbackMessage.emit(res.message)
                    onResult(true, res.message)
                }
                is CloudOperationResult.Error -> {
                    _isCloudSyncing.value = false
                    _feedbackMessage.emit(res.message)
                    onResult(false, res.message)
                }
            }
        }
    }

    // ==========================================
    // GOOGLE FIREBASE CLOUD METHODS
    // ==========================================

    fun signInFirebaseWithGoogle(activityContext: Context, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val res = firebaseAuthManager.signInWithGoogle(activityContext)) {
                is FirebaseAuthResult.Success -> {
                    _feedbackMessage.emit(res.message)
                    loadFirebaseBackups()
                    onResult(true, res.message)
                }
                is FirebaseAuthResult.Error -> {
                    _feedbackMessage.emit(res.message)
                    onResult(false, res.message)
                }
            }
        }
    }

    fun signInFirebaseWithEmail(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val res = firebaseAuthManager.signInWithEmail(email, pass)) {
                is FirebaseAuthResult.Success -> {
                    _feedbackMessage.emit(res.message)
                    loadFirebaseBackups()
                    onResult(true, res.message)
                }
                is FirebaseAuthResult.Error -> {
                    _feedbackMessage.emit(res.message)
                    onResult(false, res.message)
                }
            }
        }
    }

    fun signUpFirebaseWithEmail(email: String, pass: String, name: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val res = firebaseAuthManager.signUpWithEmail(email, pass, name)) {
                is FirebaseAuthResult.Success -> {
                    _feedbackMessage.emit(res.message)
                    loadFirebaseBackups()
                    onResult(true, res.message)
                }
                is FirebaseAuthResult.Error -> {
                    _feedbackMessage.emit(res.message)
                    onResult(false, res.message)
                }
            }
        }
    }

    fun signOutFirebase() {
        firebaseAuthManager.signOut()
        _firebaseBackups.value = emptyList()
        viewModelScope.launch {
            _feedbackMessage.emit("ফায়ারবেস ক্লাউড থেকে সাইন-আউট করা হয়েছে")
        }
    }

    fun loadFirebaseBackups() {
        viewModelScope.launch {
            if (!firebaseAuthManager.isLoggedIn()) return@launch
            when (val res = firebaseDbManager.fetchBackupsFromFirebase()) {
                is FirebaseOperationResult.Success -> {
                    _firebaseBackups.value = res.data
                }
                is FirebaseOperationResult.Error -> {
                    Log.e("MawaVM", "Firebase backups error: ${res.message}")
                }
            }
        }
    }

    fun uploadBackupToFirebase(backupName: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isFirebaseSyncing.value = true
            val fullData = repository.getFullBackupData()
            when (val res = firebaseDbManager.uploadBackupToFirebase(backupName, fullData)) {
                is FirebaseOperationResult.Success -> {
                    _isFirebaseSyncing.value = false
                    _feedbackMessage.emit(res.message)
                    loadFirebaseBackups()
                    onResult(true, res.message)
                }
                is FirebaseOperationResult.Error -> {
                    _isFirebaseSyncing.value = false
                    _feedbackMessage.emit(res.message)
                    onResult(false, res.message)
                }
            }
        }
    }

    fun restoreFirebaseBackup(backupItem: FirebaseBackupItem, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isFirebaseSyncing.value = true
            when (val res = firebaseDbManager.restoreBackupFromFirebase(backupItem.id)) {
                is FirebaseOperationResult.Success -> {
                    repository.restoreFullBackup(res.data, overwriteExisting = true)
                    notifyDataChanged()
                    _isFirebaseSyncing.value = false
                    val msg = "ফায়ারবেস ব্যাকআপ '${backupItem.backupName}' সফলভাবে রিস্টোর হয়েছে!"
                    _feedbackMessage.emit(msg)
                    onResult(true, msg)
                }
                is FirebaseOperationResult.Error -> {
                    _isFirebaseSyncing.value = false
                    _feedbackMessage.emit(res.message)
                    onResult(false, res.message)
                }
            }
        }
    }

    fun deleteFirebaseBackup(backupId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val res = firebaseDbManager.deleteBackupFromFirebase(backupId)) {
                is FirebaseOperationResult.Success -> {
                    loadFirebaseBackups()
                    _feedbackMessage.emit("ফায়ারবেস ব্যাকআপ সফলভাবে মুছে ফেলা হয়েছে")
                    onResult(true, "সফল")
                }
                is FirebaseOperationResult.Error -> {
                    _feedbackMessage.emit(res.message)
                    onResult(false, res.message)
                }
            }
        }
    }

    fun syncAllLocalRecordsToFirebase(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isFirebaseSyncing.value = true
            val fullData = repository.getFullBackupData()
            when (val res = firebaseDbManager.syncAllRecordsToFirebase(fullData)) {
                is FirebaseOperationResult.Success -> {
                    _isFirebaseSyncing.value = false
                    _feedbackMessage.emit(res.message)
                    onResult(true, res.message)
                }
                is FirebaseOperationResult.Error -> {
                    _isFirebaseSyncing.value = false
                    _feedbackMessage.emit(res.message)
                    onResult(false, res.message)
                }
            }
        }
    }
}

