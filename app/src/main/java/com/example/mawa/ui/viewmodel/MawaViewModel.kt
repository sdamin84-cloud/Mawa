package com.example.mawa.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mawa.data.local.MawaDatabase
import com.example.mawa.data.local.entity.CustomerEntity
import com.example.mawa.data.local.entity.FordiItemEntity
import com.example.mawa.data.local.entity.PersonalTransactionEntity
import com.example.mawa.data.local.entity.PersonalTransactionType
import com.example.mawa.data.local.entity.ProductEntity
import com.example.mawa.data.local.entity.ShopSettingsEntity
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType
import com.example.mawa.data.model.AccountingSummary
import com.example.mawa.data.model.AppMode
import com.example.mawa.data.model.CustomerWithBalance
import com.example.mawa.data.model.PersonalSummary
import com.example.mawa.data.model.ProductStats
import com.example.mawa.data.model.TimeFilter
import com.example.mawa.data.remote.supabase.CloudBackupItem
import com.example.mawa.data.remote.supabase.CloudOperationResult
import com.example.mawa.data.remote.supabase.SupabaseAuthManager
import com.example.mawa.data.remote.supabase.SupabaseAuthResult
import com.example.mawa.data.remote.supabase.SupabaseDbManager
import com.example.mawa.data.remote.supabase.SupabaseUser
import com.example.mawa.data.repository.MawaRepository
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

    // Supabase Cloud Integration
    val supabaseAuthManager = SupabaseAuthManager(application)
    val supabaseDbManager = SupabaseDbManager(supabaseAuthManager)

    val currentUser: StateFlow<SupabaseUser?> = supabaseAuthManager.currentUser
    val isAuthLoading: StateFlow<Boolean> = supabaseAuthManager.isLoading

    private val _cloudBackups = MutableStateFlow<List<CloudBackupItem>>(emptyList())
    val cloudBackups: StateFlow<List<CloudBackupItem>> = _cloudBackups.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    loadCloudBackups()
                } else {
                    _cloudBackups.value = emptyList()
                }
            }
        }
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
        _selectedHomeDateMillis
    ) { transactions, settings, customers, dateMillis ->
        repository.calculateSummaryForDate(transactions, settings, customers, dateMillis)
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
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (amount <= 0) return@launch
            repository.recordSale(isCash, amount, customerId, customerName, note)
            val typeStr = if (isCash) "নগদ বিক্রি" else "বাকি বিক্রি"
            _feedbackMessage.emit("$typeStr সফলভাবে সংরক্ষিত হয়েছে")
            onSuccess()
        }
    }

    fun recordBaki(
        customerId: Long,
        customerName: String,
        amount: Double,
        note: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (amount <= 0) return@launch
            repository.recordBakiEntry(customerId, customerName, amount, note)
            _feedbackMessage.emit("$customerName-কে বাকি ৳$amount দেওয়া হয়েছে")
            onSuccess()
        }
    }

    fun recordJoma(
        customerId: Long,
        customerName: String,
        amount: Double,
        note: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (amount <= 0) return@launch
            repository.recordJomaEntry(customerId, customerName, amount, note)
            _feedbackMessage.emit("$customerName-এর থেকে জমা ৳$amount পাওয়া গেছে")
            onSuccess()
        }
    }

    fun recordExpense(
        amount: Double,
        description: String,
        isHome: Boolean,
        timestamp: Long = System.currentTimeMillis(),
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (amount <= 0) return@launch
            repository.recordExpense(amount, description, isHome, timestamp)
            val targetStr = if (isHome) "বাড়ির জন্য" else "দোকানের"
            _feedbackMessage.emit("$targetStr খরচ ৳$amount সংরক্ষিত হয়েছে")
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
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val finalTotal = if (total > 0) total else (quantity * rate)
            if (finalTotal <= 0) return@launch
            repository.recordDirectPurchase(productName, productId, quantity, unit, rate, finalTotal, note, timestamp)
            _feedbackMessage.emit("মাল ক্রয় ৳$finalTotal সংরক্ষিত হয়েছে")
            onSuccess()
        }
    }

    fun convertFordiToPurchase(
        item: FordiItemEntity,
        actualQty: Double,
        actualRate: Double,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (actualQty <= 0 || actualRate <= 0) return@launch
            repository.convertFordiToPurchase(item, actualQty, actualRate)
            val total = actualQty * actualRate
            _feedbackMessage.emit("${item.productName} ক্রয় ৳$total যুক্ত হয়েছে")
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
            onSuccess()
        }
    }

    fun clearPendingFordi(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearPendingFordi()
            _feedbackMessage.emit("নতুন ফর্দ তৈরি করার জন্য তালিকা পরিষ্কার করা হয়েছে")
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
            onSuccess()
        }
    }

    fun deleteFordiItem(id: Long) {
        viewModelScope.launch {
            repository.deleteFordiItem(id)
            _feedbackMessage.emit("ফর্দ থেকে সরানো হয়েছে")
        }
    }

    fun clearCompletedFordi() {
        viewModelScope.launch {
            repository.clearCompletedFordi()
            _feedbackMessage.emit("কেনা ফর্দ তালিকা পরিষ্কার করা হয়েছে")
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
            repository.addCustomer(customer)
            _feedbackMessage.emit("কাস্টমার $name যুক্ত হয়েছে")
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
            _feedbackMessage.emit("টাকা দেওয়ার তারিখ নির্ধারিত হয়েছে")
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
            onSuccess()
        }
    }

    fun updateCustomer(customer: CustomerEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
            _feedbackMessage.emit("${customer.name} তথ্য আপডেট হয়েছে")
            onSuccess()
        }
    }

    fun deleteCustomer(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomer(id)
            _feedbackMessage.emit("কাস্টমার মুছে ফেলা হয়েছে")
        }
    }

    fun addProduct(
        name: String,
        banglaName: String,
        unit: String,
        purchasePrice: Double,
        sellingPrice: Double,
        category: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            val product = ProductEntity(
                name = name.trim(),
                banglaName = banglaName.ifBlank { name }.trim(),
                unit = unit.trim(),
                defaultPurchasePrice = purchasePrice,
                defaultSellingPrice = sellingPrice,
                category = category.ifBlank { "সাধারণ" }.trim()
            )
            repository.addProduct(product)
            _feedbackMessage.emit("পণ্য $name যুক্ত হয়েছে")
            onSuccess()
        }
    }

    fun updateProduct(product: ProductEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateProduct(product)
            _feedbackMessage.emit("${product.name} আপডেট হয়েছে")
            onSuccess()
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            repository.deleteProduct(id)
            _feedbackMessage.emit("পণ্য মুছে ফেলা হয়েছে")
        }
    }

    fun updateSettings(settings: ShopSettingsEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            _feedbackMessage.emit("দোকানের সেটিংস সংরক্ষিত হয়েছে")
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
            onSuccess()
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            _feedbackMessage.emit("লেনদেন বাতিল করা হয়েছে")
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
            onSuccess()
        }
    }

    fun deletePersonalTransaction(id: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deletePersonalTransaction(id)
            _feedbackMessage.emit("হিসাব মুছে ফেলা হয়েছে")
            onSuccess()
        }
    }

    // --- Quick Baki & Joma Actions ---

    fun quickAddBaki(customerId: Long, customerName: String, amount: Double, note: String = "", onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (amount <= 0) return@launch
            repository.addQuickBaki(customerId, customerName, amount, note)
            _feedbackMessage.emit("$customerName-এর খতিয়ানে ৳$amount বাকি যোগ হয়েছে")
            onSuccess()
        }
    }

    fun quickAddJoma(customerId: Long, customerName: String, amount: Double, note: String = "", onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (amount <= 0) return@launch
            repository.addQuickJoma(customerId, customerName, amount, note)
            _feedbackMessage.emit("$customerName-এর থেকে ৳$amount জমা পাওয়া গেছে")
            onSuccess()
        }
    }

    // --- Backup & Restore Engine ---

    suspend fun exportFullBackupJson(): String {
        val backupData = repository.getFullBackupData()
        return com.example.mawa.util.DataBackupRestoreManager.exportToJsonString(backupData)
    }

    suspend fun restoreFullBackupFromJson(jsonString: String, overwriteExisting: Boolean = true): Boolean {
        return try {
            val backupData = com.example.mawa.util.DataBackupRestoreManager.parseFromJsonString(jsonString)
            repository.restoreFullBackup(backupData, overwriteExisting)
            _feedbackMessage.emit("জেসন ব্যাকআপ থেকে সকল ডাটা সফলভাবে রিস্টোর হয়েছে!")
            true
        } catch (e: Exception) {
            _feedbackMessage.emit("রিস্টোর ব্যর্থ হয়েছে: ${e.localizedMessage}")
            false
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

    // --- Supabase Cloud Operations ---

    fun loginWithSupabase(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val res = supabaseAuthManager.signIn(email, pass)) {
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
}
