package com.example.mawa.data.repository

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
import com.example.mawa.data.model.CategorySpending
import com.example.mawa.data.model.CustomerWithBalance
import com.example.mawa.data.model.PersonalSummary
import com.example.mawa.data.model.ProductStats
import com.example.mawa.data.model.TimeFilter
import com.example.mawa.util.FullBackupData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar

class MawaRepository(private val database: MawaDatabase) {

    private val transactionDao = database.transactionDao()
    private val customerDao = database.customerDao()
    private val productDao = database.productDao()
    private val fordiDao = database.fordiDao()
    private val settingsDao = database.shopSettingsDao()
    private val personalTransactionDao = database.personalTransactionDao()

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val recentTransactions: Flow<List<TransactionEntity>> = transactionDao.getRecentTransactions(30)
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val activeProducts: Flow<List<ProductEntity>> = productDao.getAllActiveProducts()
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allFordiItems: Flow<List<FordiItemEntity>> = fordiDao.getAllFordiItems()
    val pendingFordiItems: Flow<List<FordiItemEntity>> = fordiDao.getPendingFordiItems()
    val allPurchases: Flow<List<TransactionEntity>> = transactionDao.getAllPurchases()
    val shopSettings: Flow<ShopSettingsEntity?> = settingsDao.getSettings()

    // Personal Transactions
    val allPersonalTransactions: Flow<List<PersonalTransactionEntity>> = personalTransactionDao.getAllPersonalTransactions()
    val recentPersonalTransactions: Flow<List<PersonalTransactionEntity>> = personalTransactionDao.getRecentPersonalTransactions(50)

    // Real-time calculated Accounting Summary
    val accountingSummary: Flow<AccountingSummary> = combine(
        allTransactions,
        shopSettings,
        allCustomers
    ) { transactions, settings, customers ->
        calculateSummary(transactions, settings, customers)
    }

    // Customers with real-time calculated balance
    val customersWithBalance: Flow<List<CustomerWithBalance>> = combine(
        allCustomers,
        allTransactions
    ) { customers, transactions ->
        val startOfToday = getStartOfDayMillis(System.currentTimeMillis())
        
        customers.map { customer ->
            val customerTransactions = transactions.filter { it.customerId == customer.id }
            
            val totalBakiGiven = customerTransactions
                .filter { it.type == TransactionType.SALE_BAKI }
                .sumOf { it.amount }
                
            val totalJomaReceived = customerTransactions
                .filter { it.type == TransactionType.BAKI_COLLECTION }
                .sumOf { it.amount }
                
            val currentBalance = customer.openingBalance + totalBakiGiven - totalJomaReceived
            val lastTx = customerTransactions.maxByOrNull { it.timestamp }
            val hasToday = customerTransactions.any { it.timestamp >= startOfToday }

            CustomerWithBalance(
                customer = customer,
                totalBakiGiven = totalBakiGiven,
                totalJomaReceived = totalJomaReceived,
                currentBalance = currentBalance,
                lastTransaction = lastTx,
                hasTransactionToday = hasToday
            )
        }
    }

    fun calculateSummaryForDate(
        transactions: List<TransactionEntity>,
        settings: ShopSettingsEntity?,
        customers: List<CustomerEntity>,
        targetDateMillis: Long = System.currentTimeMillis()
    ): AccountingSummary {
        val openingBalance = settings?.openingBalance ?: 0.0
        val startOfTargetDay = getStartOfDayMillis(targetDateMillis)
        val endOfTargetDay = getEndOfDayMillis(targetDateMillis)

        var totalCash = openingBalance
        var todayCashChange = 0.0

        var todayCashSales = 0.0
        var todayBakiSales = 0.0
        var todayBakiCollection = 0.0
        var todayPurchases = 0.0
        var todayShopExpenses = 0.0
        var todayHomeWithdrawals = 0.0

        for (tx in transactions) {
            val isTargetDay = tx.timestamp in startOfTargetDay..endOfTargetDay
            val isBeforeOrOnTargetDay = tx.timestamp <= endOfTargetDay

            when (tx.type) {
                TransactionType.SALE_CASH -> {
                    if (isBeforeOrOnTargetDay) totalCash += tx.amount
                    if (isTargetDay) {
                        todayCashSales += tx.amount
                        todayCashChange += tx.amount
                    }
                }
                TransactionType.SALE_BAKI -> {
                    // Baki sale does not affect cash in hand directly
                    if (isTargetDay) {
                        todayBakiSales += tx.amount
                    }
                }
                TransactionType.BAKI_COLLECTION -> {
                    if (isBeforeOrOnTargetDay) totalCash += tx.amount
                    if (isTargetDay) {
                        todayBakiCollection += tx.amount
                        todayCashChange += tx.amount
                    }
                }
                TransactionType.PURCHASE_FORDI, TransactionType.PURCHASE_DIRECT -> {
                    if (isBeforeOrOnTargetDay) totalCash -= tx.amount
                    if (isTargetDay) {
                        todayPurchases += tx.amount
                        todayCashChange -= tx.amount
                    }
                }
                TransactionType.EXPENSE_SHOP -> {
                    if (isBeforeOrOnTargetDay) totalCash -= tx.amount
                    if (isTargetDay) {
                        todayShopExpenses += tx.amount
                        todayCashChange -= tx.amount
                    }
                }
                TransactionType.EXPENSE_HOME -> {
                    if (isBeforeOrOnTargetDay) totalCash -= tx.amount
                    if (isTargetDay) {
                        todayHomeWithdrawals += tx.amount
                        todayCashChange -= tx.amount
                    }
                }
                TransactionType.CASH_ADJUSTMENT -> {
                    if (isBeforeOrOnTargetDay) totalCash += tx.amount
                    if (isTargetDay) {
                        todayCashChange += tx.amount
                    }
                }
            }
        }

        // Calculate total outstanding baki across all customers up to target date
        var totalOutstandingBaki = 0.0
        for (customer in customers) {
            val custTx = transactions.filter { it.customerId == customer.id && it.timestamp <= endOfTargetDay }
            val baki = custTx.filter { it.type == TransactionType.SALE_BAKI }.sumOf { it.amount }
            val joma = custTx.filter { it.type == TransactionType.BAKI_COLLECTION }.sumOf { it.amount }
            totalOutstandingBaki += (customer.openingBalance + baki - joma)
        }

        return AccountingSummary(
            openingBalance = openingBalance,
            totalCashInHand = totalCash,
            todayCashChange = todayCashChange,
            todayTotalSales = todayCashSales + todayBakiSales,
            todayCashSales = todayCashSales,
            todayBakiSales = todayBakiSales,
            todayBakiCollection = todayBakiCollection,
            todayPurchases = todayPurchases,
            todayShopExpenses = todayShopExpenses,
            todayHomeWithdrawals = todayHomeWithdrawals,
            totalOutstandingBaki = totalOutstandingBaki,
            todayNewBaki = todayBakiSales
        )
    }

    private fun calculateSummary(
        transactions: List<TransactionEntity>,
        settings: ShopSettingsEntity?,
        customers: List<CustomerEntity>
    ): AccountingSummary {
        return calculateSummaryForDate(transactions, settings, customers, System.currentTimeMillis())
    }

    fun getTransactionsForPeriod(filter: TimeFilter): Flow<List<TransactionEntity>> {
        val (start, end) = getTimeRange(filter)
        return transactionDao.getTransactionsBetween(start, end)
    }

    fun getHomeExpenses(filter: TimeFilter): Flow<List<TransactionEntity>> {
        val (start, end) = getTimeRange(filter)
        return transactionDao.getHomeExpensesBetween(start, end)
    }

    fun getCustomerTransactions(customerId: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsForCustomer(customerId)
    }

    suspend fun getCustomerWithBalance(customerId: Long): CustomerWithBalance? = withContext(Dispatchers.IO) {
        val customer = customerDao.getCustomerByIdDirect(customerId) ?: return@withContext null
        val transactions = database.transactionDao().getAllTransactions()
        // Compute directly
        val custTx = database.transactionDao().getTransactionsForCustomerSince(customerId, 0)
        // Let's query
        null
    }

    suspend fun recordSale(
        isCash: Boolean,
        amount: Double,
        customerId: Long? = null,
        customerName: String? = null,
        note: String = "",
        timestamp: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val tx = TransactionEntity(
            type = if (isCash) TransactionType.SALE_CASH else TransactionType.SALE_BAKI,
            amount = amount,
            customerId = customerId,
            customerName = customerName,
            note = note,
            timestamp = timestamp
        )
        transactionDao.insertTransaction(tx)
    }

    suspend fun recordBakiEntry(
        customerId: Long,
        customerName: String,
        amount: Double,
        note: String = "",
        timestamp: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val tx = TransactionEntity(
            type = TransactionType.SALE_BAKI,
            amount = amount,
            customerId = customerId,
            customerName = customerName,
            note = note,
            timestamp = timestamp
        )
        transactionDao.insertTransaction(tx)
    }

    suspend fun recordJomaEntry(
        customerId: Long,
        customerName: String,
        amount: Double,
        note: String = "",
        timestamp: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val tx = TransactionEntity(
            type = TransactionType.BAKI_COLLECTION,
            amount = amount,
            customerId = customerId,
            customerName = customerName,
            note = note,
            timestamp = timestamp
        )
        transactionDao.insertTransaction(tx)
    }

    suspend fun recordExpense(
        amount: Double,
        description: String,
        isHome: Boolean,
        timestamp: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val tx = TransactionEntity(
            type = if (isHome) TransactionType.EXPENSE_HOME else TransactionType.EXPENSE_SHOP,
            amount = amount,
            note = description,
            timestamp = timestamp
        )
        transactionDao.insertTransaction(tx)
    }

    suspend fun recordDirectPurchase(
        productName: String,
        productId: Long? = null,
        quantity: Double,
        unit: String,
        rate: Double,
        total: Double,
        note: String = "",
        timestamp: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val tx = TransactionEntity(
            type = TransactionType.PURCHASE_DIRECT,
            amount = if (total > 0) total else (quantity * rate),
            productId = productId,
            productName = productName,
            quantity = quantity,
            unit = unit,
            rate = rate,
            note = note,
            timestamp = timestamp
        )
        transactionDao.insertTransaction(tx)
    }

    suspend fun convertFordiToPurchase(
        fordiItem: FordiItemEntity,
        actualQuantity: Double,
        actualRate: Double
    ) = withContext(Dispatchers.IO) {
        val actualTotal = actualQuantity * actualRate
        val now = System.currentTimeMillis()

        // 1. Update fordi item
        fordiDao.updateFordiItem(
            fordiItem.copy(
                isPurchased = true,
                actualQuantity = actualQuantity,
                actualRate = actualRate,
                actualTotal = actualTotal,
                purchaseDate = now
            )
        )

        // 2. Post actual purchase transaction
        val tx = TransactionEntity(
            type = TransactionType.PURCHASE_FORDI,
            amount = actualTotal,
            productId = fordiItem.productId,
            productName = fordiItem.productName,
            quantity = actualQuantity,
            unit = fordiItem.unit,
            rate = actualRate,
            note = "ফর্দ থেকে ক্রয়: ${fordiItem.productName}",
            timestamp = now
        )
        transactionDao.insertTransaction(tx)
    }

    suspend fun convertMultipleFordiToPurchases(items: List<FordiItemEntity>) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        for (item in items) {
            val qty = if (item.actualQuantity > 0) item.actualQuantity else item.plannedQuantity
            val rate = if (item.actualRate > 0) item.actualRate else item.purchaseRate
            val lineTotal = qty * rate

            fordiDao.updateFordiItem(
                item.copy(
                    isPurchased = true,
                    actualQuantity = qty,
                    actualRate = rate,
                    actualTotal = lineTotal,
                    purchaseDate = now
                )
            )

            val tx = TransactionEntity(
                type = TransactionType.PURCHASE_FORDI,
                amount = lineTotal,
                productId = item.productId,
                productName = item.productName,
                quantity = qty,
                unit = item.unit,
                rate = rate,
                note = "ফর্দ থেকে ক্রয়: ${item.productName}",
                timestamp = now
            )
            transactionDao.insertTransaction(tx)
        }
    }

    suspend fun clearPendingFordi() = withContext(Dispatchers.IO) {
        fordiDao.clearPendingFordi()
    }

    suspend fun reAddPurchasedItemsToFordi(items: List<FordiItemEntity>) = withContext(Dispatchers.IO) {
        val newItems = items.map { item ->
            FordiItemEntity(
                productId = item.productId,
                productName = item.productName,
                plannedQuantity = if (item.plannedQuantity > 0) item.plannedQuantity else (if (item.actualQuantity > 0) item.actualQuantity else 1.0),
                unit = item.unit,
                purchaseRate = if (item.purchaseRate > 0) item.purchaseRate else item.actualRate,
                sellingRate = item.sellingRate,
                isPurchased = false,
                createdAt = System.currentTimeMillis()
            )
        }
        fordiDao.insertFordiItems(newItems)
    }

    suspend fun addFordiItem(
        productName: String,
        productId: Long? = null,
        plannedQuantity: Double,
        unit: String,
        purchaseRate: Double,
        sellingRate: Double
    ): Long = withContext(Dispatchers.IO) {
        val item = FordiItemEntity(
            productId = productId,
            productName = productName,
            plannedQuantity = plannedQuantity,
            unit = unit,
            purchaseRate = purchaseRate,
            sellingRate = sellingRate
        )
        fordiDao.insertFordiItem(item)
    }

    suspend fun updateFordiItem(item: FordiItemEntity) = withContext(Dispatchers.IO) {
        fordiDao.updateFordiItem(item)
    }

    suspend fun deleteFordiItem(id: Long) = withContext(Dispatchers.IO) {
        fordiDao.deleteFordiItemById(id)
    }

    suspend fun clearCompletedFordi() = withContext(Dispatchers.IO) {
        fordiDao.clearCompletedFordi()
    }

    suspend fun addCustomer(customer: CustomerEntity): Long = withContext(Dispatchers.IO) {
        customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: CustomerEntity) = withContext(Dispatchers.IO) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(id: Long) = withContext(Dispatchers.IO) {
        customerDao.deleteCustomerById(id)
    }

    suspend fun addProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(id: Long) = withContext(Dispatchers.IO) {
        productDao.deleteProductById(id)
    }

    suspend fun updateSettings(settings: ShopSettingsEntity) = withContext(Dispatchers.IO) {
        settingsDao.insertOrUpdateSettings(settings)
    }

    suspend fun calculateProductStats(productId: Long, productName: String, unit: String, sellingPrice: Double): ProductStats = withContext(Dispatchers.IO) {
        // Collect product transactions from DB
        var totalQty = 0.0
        var totalAmount = 0.0
        var count = 0
        var latestRate = 0.0
        var highestRate = 0.0
        var lowestRate = Double.MAX_VALUE
        val history = mutableListOf<TransactionEntity>()

        // Find by ID or name
        // Direct query or filter
        // We will pass from ViewModel
        ProductStats(
            productId = productId,
            productName = productName,
            unit = unit,
            sellingPrice = sellingPrice
        )
    }

    suspend fun mergeProducts(canonical: ProductEntity, duplicateId: Long) = withContext(Dispatchers.IO) {
        // Deactivate duplicate product
        val duplicate = productDao.getProductByIdDirect(duplicateId)
        if (duplicate != null) {
            productDao.updateProduct(duplicate.copy(isActive = false))
        }
        // Update product entity with canonical values
        productDao.updateProduct(canonical)
    }

    suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun clearAllData(clearSettings: Boolean = false) = withContext(Dispatchers.IO) {
        transactionDao.deleteAllTransactions()
        customerDao.deleteAllCustomers()
        fordiDao.deleteAllFordiItems()
        productDao.deleteAllProducts()
        personalTransactionDao.deleteAll()
        if (clearSettings) {
            settingsDao.insertOrUpdateSettings(
                ShopSettingsEntity(
                    id = 1,
                    shopName = "মাওয়া ডিজিটাল খাতা",
                    ownerName = "দোকানদার",
                    openingBalance = 0.0,
                    currencySymbol = "৳",
                    appMode = "BOTH",
                    isModeConfigured = false
                )
            )
        }
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val settings = settingsDao.getSettingsDirect()
        if (settings == null) {
            settingsDao.insertOrUpdateSettings(
                ShopSettingsEntity(
                    id = 1,
                    shopName = "মাওয়া ডিজিটাল খাতা",
                    ownerName = "দোকানদার",
                    openingBalance = 0.0,
                    currencySymbol = "৳",
                    appMode = "BOTH",
                    isModeConfigured = false
                )
            )
        }
    }

    // --- Personal Mode Methods ---

    fun getPersonalTransactionsForPeriod(filter: TimeFilter): Flow<List<PersonalTransactionEntity>> {
        val (start, end) = getTimeRange(filter)
        return personalTransactionDao.getPersonalTransactionsBetween(start, end)
    }

    fun calculatePersonalSummary(
        allPersonalList: List<PersonalTransactionEntity>,
        periodFilter: TimeFilter
    ): PersonalSummary {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startOfThisMonth = getStartOfDayMillis(cal.timeInMillis)
        val endOfThisMonth = getEndOfDayMillis(now)

        // 1. Calculate this month's expense
        val thisMonthExpense = allPersonalList
            .filter { it.type == PersonalTransactionType.EXPENSE && it.timestamp in startOfThisMonth..endOfThisMonth }
            .sumOf { it.amount }

        // 2. Filter for selected period
        val (start, end) = getTimeRange(periodFilter)
        val periodList = allPersonalList.filter { it.timestamp in start..end }

        var totalIncome = 0.0
        var totalExpense = 0.0
        var totalSavings = 0.0

        for (tx in periodList) {
            when (tx.type) {
                PersonalTransactionType.INCOME -> totalIncome += tx.amount
                PersonalTransactionType.EXPENSE -> totalExpense += tx.amount
                PersonalTransactionType.SAVINGS -> totalSavings += tx.amount
            }
        }

        val netBalance = totalIncome - totalExpense - totalSavings

        // 3. Category spending breakdown for expenses in this period
        val expenseTxList = periodList.filter { it.type == PersonalTransactionType.EXPENSE }
        val categoryGroups = expenseTxList.groupBy { it.category.ifBlank { "অন্যান্য" } }
        
        val categoryBreakdown = categoryGroups.map { (category, txList) ->
            val catTotal = txList.sumOf { it.amount }
            val pct = if (totalExpense > 0) ((catTotal / totalExpense) * 100f).toFloat() else 0f
            CategorySpending(
                category = category,
                totalAmount = catTotal,
                percentage = pct,
                transactionCount = txList.size
            )
        }.sortedByDescending { it.totalAmount }

        return PersonalSummary(
            thisMonthExpense = thisMonthExpense,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            totalSavings = totalSavings,
            netBalance = netBalance,
            categoryBreakdown = categoryBreakdown,
            periodTransactions = periodList
        )
    }

    suspend fun recordPersonalTransaction(
        type: PersonalTransactionType,
        amount: Double,
        title: String,
        category: String,
        note: String = "",
        timestamp: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val tx = PersonalTransactionEntity(
            type = type,
            amount = amount,
            title = title.trim(),
            category = category.trim().ifBlank { "অন্যান্য" },
            note = note.trim(),
            timestamp = timestamp
        )
        personalTransactionDao.insert(tx)
    }

    suspend fun updatePersonalTransaction(transaction: PersonalTransactionEntity) = withContext(Dispatchers.IO) {
        personalTransactionDao.update(transaction)
    }

    suspend fun deletePersonalTransaction(id: Long) = withContext(Dispatchers.IO) {
        personalTransactionDao.deleteById(id)
    }

    suspend fun updateAppMode(appMode: AppMode) = withContext(Dispatchers.IO) {
        val current = settingsDao.getSettingsDirect() ?: ShopSettingsEntity()
        settingsDao.insertOrUpdateSettings(
            current.copy(
                appMode = appMode.key,
                isModeConfigured = true
            )
        )
    }

    // --- Quick Baki / Joma Actions for Baki Khata ---

    suspend fun addQuickBaki(customerId: Long, customerName: String, amount: Double, note: String = ""): Long = withContext(Dispatchers.IO) {
        val tx = TransactionEntity(
            type = TransactionType.SALE_BAKI,
            amount = amount,
            customerId = customerId,
            customerName = customerName,
            note = note.trim().ifBlank { "বাকি বিক্রয়" },
            category = "বাকি",
            timestamp = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(tx)
    }

    suspend fun addQuickJoma(customerId: Long, customerName: String, amount: Double, note: String = ""): Long = withContext(Dispatchers.IO) {
        val tx = TransactionEntity(
            type = TransactionType.BAKI_COLLECTION,
            amount = amount,
            customerId = customerId,
            customerName = customerName,
            note = note.trim().ifBlank { "বাকি আদায়" },
            category = "আদায়",
            timestamp = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(tx)
    }

    // --- Full Data Export & Restore ---

    suspend fun getFullBackupData(): FullBackupData = withContext(Dispatchers.IO) {
        val settings = settingsDao.getSettingsDirect()
        val customers = customerDao.getAllCustomers().first()
        val transactions = transactionDao.getAllTransactions().first()
        val fordiItems = fordiDao.getAllFordiItems().first()
        val products = productDao.getAllProducts().first()
        val personalTransactions = personalTransactionDao.getAllPersonalTransactions().first()

        FullBackupData(
            exportDate = System.currentTimeMillis(),
            shopSettings = settings,
            customers = customers,
            transactions = transactions,
            fordiItems = fordiItems,
            products = products,
            personalTransactions = personalTransactions
        )
    }

    suspend fun restoreFullBackup(data: FullBackupData, overwriteExisting: Boolean = true) = withContext(Dispatchers.IO) {
        if (overwriteExisting) {
            transactionDao.deleteAllTransactions()
            customerDao.deleteAllCustomers()
            fordiDao.deleteAllFordiItems()
            productDao.deleteAllProducts()
            personalTransactionDao.deleteAll()
        }

        data.shopSettings?.let {
            settingsDao.insertOrUpdateSettings(it)
        }

        // Prepare customers to insert
        val existingCustomers: List<CustomerEntity> = if (overwriteExisting) emptyList() else customerDao.getAllCustomersDirect()
        val customerNameMap = existingCustomers.associateBy { it.name.trim().lowercase() }.toMutableMap()

        val customersToInsert = mutableListOf<CustomerEntity>()
        data.customers.forEach { c ->
            val key = c.name.trim().lowercase()
            if (overwriteExisting || !customerNameMap.containsKey(key)) {
                customersToInsert.add(c)
            }
        }

        if (customersToInsert.isNotEmpty()) {
            customerDao.insertCustomers(customersToInsert)
        }

        // Reload customer map after insertion
        val currentAllCustomers = customerDao.getAllCustomersDirect()
        val updatedCustomerMap = currentAllCustomers.associateBy { it.name.trim().lowercase() }.toMutableMap()
        val updatedCustomerIdMap = currentAllCustomers.associateBy { it.id }.toMutableMap()

        // Check if any transactions have customer names not in customer table
        val missingCustomers = mutableListOf<CustomerEntity>()
        data.transactions.forEach { t ->
            val cName = t.customerName?.trim()
            if (!cName.isNullOrBlank() && !updatedCustomerMap.containsKey(cName.lowercase())) {
                val newCust = CustomerEntity(
                    name = cName,
                    phone = "",
                    address = "",
                    openingBalance = 0.0
                )
                missingCustomers.add(newCust)
                updatedCustomerMap[cName.lowercase()] = newCust
            }
        }
        if (missingCustomers.isNotEmpty()) {
            customerDao.insertCustomers(missingCustomers)
            val refreshedCustomers = customerDao.getAllCustomersDirect()
            refreshedCustomers.forEach {
                updatedCustomerMap[it.name.trim().lowercase()] = it
                updatedCustomerIdMap[it.id] = it
            }
        }

        // Fix transactions to ensure valid customer references
        val finalTransactions = data.transactions.map { t ->
            var finalCustId = t.customerId
            val cName = t.customerName?.trim()
            if (!cName.isNullOrBlank() && (finalCustId == null || finalCustId <= 0 || !updatedCustomerIdMap.containsKey(finalCustId))) {
                finalCustId = updatedCustomerMap[cName.lowercase()]?.id
            }
            if (finalCustId != null && finalCustId > 0 && cName.isNullOrBlank()) {
                val foundName = updatedCustomerIdMap[finalCustId]?.name
                t.copy(customerId = finalCustId, customerName = foundName)
            } else {
                t.copy(customerId = finalCustId)
            }
        }

        if (finalTransactions.isNotEmpty()) {
            transactionDao.insertTransactions(finalTransactions)
        }

        if (data.fordiItems.isNotEmpty()) {
            fordiDao.insertFordiItems(data.fordiItems)
        }

        if (data.products.isNotEmpty()) {
            productDao.insertProducts(data.products)
        }

        if (data.personalTransactions.isNotEmpty()) {
            personalTransactionDao.insertAll(data.personalTransactions)
        }
    }

    suspend fun importCustomers(customers: List<CustomerEntity>) = withContext(Dispatchers.IO) {
        if (customers.isNotEmpty()) {
            customerDao.insertCustomers(customers)
        }
    }

    companion object {
        fun getStartOfDayMillis(millis: Long): Long {
            val cal = Calendar.getInstance()
            cal.timeInMillis = millis
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        fun getEndOfDayMillis(millis: Long): Long {
            val cal = Calendar.getInstance()
            cal.timeInMillis = millis
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            return cal.timeInMillis
        }

        fun getTimeRange(filter: TimeFilter): Pair<Long, Long> {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()
            cal.timeInMillis = now

            return when (filter) {
                TimeFilter.TODAY -> {
                    Pair(getStartOfDayMillis(now), getEndOfDayMillis(now))
                }
                TimeFilter.THIS_WEEK -> {
                    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                    val startOfWeek = getStartOfDayMillis(cal.timeInMillis)
                    Pair(startOfWeek, getEndOfDayMillis(now))
                }
                TimeFilter.THIS_MONTH -> {
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    val startOfMonth = getStartOfDayMillis(cal.timeInMillis)
                    Pair(startOfMonth, getEndOfDayMillis(now))
                }
                TimeFilter.ALL_TIME -> {
                    Pair(0L, Long.MAX_VALUE)
                }
            }
        }
    }
}
