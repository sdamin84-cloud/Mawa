package com.example.mawa.data.repository

import com.example.mawa.data.local.MawaDatabase
import com.example.mawa.data.local.entity.CashTallyEntity
import com.example.mawa.data.local.entity.CompanyOrderEntity
import com.example.mawa.data.local.entity.CustomerEntity
import com.example.mawa.data.local.entity.DailyCashEntity
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MawaRepository(private val database: MawaDatabase) {

    private val transactionDao = database.transactionDao()
    private val customerDao = database.customerDao()
    private val productDao = database.productDao()
    private val fordiDao = database.fordiDao()
    private val settingsDao = database.shopSettingsDao()
    private val personalTransactionDao = database.personalTransactionDao()
    private val dailyCashDao = database.dailyCashDao()
    private val supplierCompanyDao = database.supplierCompanyDao()
    private val cashTallyDao = database.cashTallyDao()
    private val companyOrderDao = database.companyOrderDao()

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val recentTransactions: Flow<List<TransactionEntity>> = transactionDao.getRecentTransactions(30)
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val activeProducts: Flow<List<ProductEntity>> = productDao.getAllActiveProducts()
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allFordiItems: Flow<List<FordiItemEntity>> = fordiDao.getAllFordiItems()
    val pendingFordiItems: Flow<List<FordiItemEntity>> = fordiDao.getPendingFordiItems()
    val allPurchases: Flow<List<TransactionEntity>> = transactionDao.getAllPurchases()
    val shopSettings: Flow<ShopSettingsEntity?> = settingsDao.getSettings()
    val allDailyCash: Flow<List<DailyCashEntity>> = dailyCashDao.getAllDailyCashFlow()
    val allSupplierCompanies: Flow<List<SupplierCompanyEntity>> = supplierCompanyDao.getAllCompanies()
    val allCashTallies: Flow<List<CashTallyEntity>> = cashTallyDao.getAllTalliesFlow()
    val allCompanyOrders: Flow<List<CompanyOrderEntity>> = companyOrderDao.getAllOrders()

    // Personal Transactions
    val allPersonalTransactions: Flow<List<PersonalTransactionEntity>> = personalTransactionDao.getAllPersonalTransactions()
    val recentPersonalTransactions: Flow<List<PersonalTransactionEntity>> = personalTransactionDao.getRecentPersonalTransactions(50)

    // Real-time calculated Accounting Summary
    val accountingSummary: Flow<AccountingSummary> = combine(
        allTransactions,
        shopSettings,
        allCustomers,
        allDailyCash
    ) { transactions, settings, customers, dailyCashList ->
        calculateSummary(transactions, settings, customers, dailyCashList)
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
        targetDateMillis: Long = System.currentTimeMillis(),
        dailyCashList: List<DailyCashEntity> = emptyList()
    ): AccountingSummary {
        val targetDateKey = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(targetDateMillis))
        val startOfTargetDay = getStartOfDayMillis(targetDateMillis)
        val endOfTargetDay = getEndOfDayMillis(targetDateMillis)

        val dayRecord = dailyCashList.find { it.dateKey == targetDateKey }
        val effectiveOpeningBalance = if (dayRecord != null && dayRecord.sabekCash > 0) {
            dayRecord.sabekCash
        } else {
            val prevRecord = dailyCashList.filter { it.dateMillis < startOfTargetDay && it.closingCash > 0 }.maxByOrNull { it.dateMillis }
            prevRecord?.closingCash ?: (settings?.openingBalance ?: 0.0)
        }

        var todayCashChange = 0.0
        var todayCashSales = 0.0
        var todayBakiSales = 0.0
        var todayBakiCollection = 0.0
        var todayPurchases = 0.0
        var todayShopExpenses = 0.0
        var todayHomeWithdrawals = 0.0

        for (tx in transactions) {
            val isTargetDay = tx.timestamp in startOfTargetDay..endOfTargetDay

            when (tx.type) {
                TransactionType.SALE_CASH -> {
                    if (isTargetDay) {
                        todayCashSales += tx.amount
                        todayCashChange += tx.amount
                    }
                }
                TransactionType.SALE_BAKI -> {
                    if (isTargetDay) {
                        todayBakiSales += tx.amount
                    }
                }
                TransactionType.BAKI_COLLECTION -> {
                    if (isTargetDay) {
                        todayBakiCollection += tx.amount
                        todayCashChange += tx.amount
                    }
                }
                TransactionType.PURCHASE_FORDI, TransactionType.PURCHASE_DIRECT -> {
                    if (isTargetDay) {
                        todayPurchases += tx.amount
                        if (tx.paymentStatus != "DUE_PAYMENT") {
                            todayCashChange -= tx.amount
                        }
                    }
                }
                TransactionType.EXPENSE_SHOP -> {
                    if (isTargetDay) {
                        todayShopExpenses += tx.amount
                        if (tx.paymentStatus != "DUE_PAYMENT") {
                            todayCashChange -= tx.amount
                        }
                    }
                }
                TransactionType.EXPENSE_HOME -> {
                    if (isTargetDay) {
                        todayHomeWithdrawals += tx.amount
                        todayCashChange -= tx.amount
                    }
                }
                TransactionType.CASH_ADJUSTMENT -> {
                    if (isTargetDay) {
                        todayCashChange += tx.amount
                    }
                }
            }
        }

        val todayExpensesTotal = todayPurchases + todayShopExpenses + todayHomeWithdrawals
        val autoCalculatedCashSales = if (dayRecord != null && dayRecord.closingCash > 0) {
            (dayRecord.closingCash + todayExpensesTotal - effectiveOpeningBalance - todayBakiCollection).coerceAtLeast(0.0)
        } else if (todayExpensesTotal > (effectiveOpeningBalance + todayCashSales + todayBakiCollection)) {
            (todayExpensesTotal - (effectiveOpeningBalance + todayBakiCollection)).coerceAtLeast(0.0)
        } else {
            0.0
        }

        val finalCashSales = maxOf(todayCashSales, autoCalculatedCashSales)
        val finalTotalSales = finalCashSales + todayBakiSales
        val finalCashChange = finalCashSales + todayBakiCollection - todayExpensesTotal
        val totalCashInHand = if (dayRecord != null && dayRecord.isClosed && dayRecord.closingCash > 0) {
            dayRecord.closingCash
        } else {
            (effectiveOpeningBalance + finalCashChange).coerceAtLeast(0.0)
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
            openingBalance = effectiveOpeningBalance,
            totalCashInHand = totalCashInHand,
            todayCashChange = finalCashChange,
            todayTotalSales = finalTotalSales,
            todayCashSales = finalCashSales,
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
        customers: List<CustomerEntity>,
        dailyCashList: List<DailyCashEntity> = emptyList()
    ): AccountingSummary {
        return calculateSummaryForDate(transactions, settings, customers, System.currentTimeMillis(), dailyCashList)
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
        timestamp: Long = System.currentTimeMillis(),
        paymentStatus: String = "PAID",
        promisedPaymentDate: Long = 0L
    ): Long = withContext(Dispatchers.IO) {
        val tx = TransactionEntity(
            type = if (isHome) TransactionType.EXPENSE_HOME else TransactionType.EXPENSE_SHOP,
            amount = amount,
            note = description,
            paymentStatus = paymentStatus,
            promisedPaymentDate = promisedPaymentDate,
            timestamp = timestamp
        )
        transactionDao.insertTransaction(tx)
    }

    suspend fun recordHomeGoodsWithdrawal(
        productName: String,
        productId: Long? = null,
        quantity: Double,
        unit: String,
        rate: Double,
        isDeductFromCash: Boolean,
        note: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val total = quantity * rate
        val tx = TransactionEntity(
            type = TransactionType.EXPENSE_HOME,
            amount = if (isDeductFromCash) total else 0.0,
            productId = productId,
            productName = productName,
            quantity = quantity,
            unit = unit,
            rate = rate,
            category = if (isDeductFromCash) "সংসার পণ্য (ক্যাশ কর্তন)" else "সংসার পণ্য (ক্যাশ অপরিবর্তিত)",
            note = note.ifBlank { "$productName ($quantity $unit) বাসার জন্য নেওয়া" },
            timestamp = System.currentTimeMillis()
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
        timestamp: Long = System.currentTimeMillis(),
        paymentStatus: String = "PAID",
        promisedPaymentDate: Long = 0L
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
            paymentStatus = paymentStatus,
            promisedPaymentDate = promisedPaymentDate,
            timestamp = timestamp
        )
        transactionDao.insertTransaction(tx)
    }

    suspend fun convertFordiToPurchase(
        fordiItem: FordiItemEntity,
        actualQuantity: Double,
        actualRate: Double,
        paymentStatus: String = "PAID",
        promisedPaymentDate: Long = 0L
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
            note = if (fordiItem.companyName.isNotBlank()) fordiItem.companyName else "ফর্দ থেকে ক্রয়: ${fordiItem.productName}",
            paymentStatus = paymentStatus,
            promisedPaymentDate = promisedPaymentDate,
            timestamp = now
        )
        transactionDao.insertTransaction(tx)
    }

    suspend fun updateTransactionPaymentStatus(id: Long, status: String) = withContext(Dispatchers.IO) {
        transactionDao.updatePaymentStatus(id, status)
    }

    suspend fun saveSupplierCompany(company: SupplierCompanyEntity): Long = withContext(Dispatchers.IO) {
        supplierCompanyDao.insertOrUpdateCompany(company)
    }

    suspend fun deleteSupplierCompany(id: Long) = withContext(Dispatchers.IO) {
        supplierCompanyDao.deleteCompanyById(id)
    }

    suspend fun getAllSupplierCompaniesDirect(): List<SupplierCompanyEntity> = withContext(Dispatchers.IO) {
        supplierCompanyDao.getAllCompaniesDirect()
    }

    // Company Orders (অর্ডার খাতা)
    suspend fun saveCompanyOrder(order: CompanyOrderEntity): Long = withContext(Dispatchers.IO) {
        companyOrderDao.insertOrder(order)
    }

    suspend fun confirmCompanyOrder(orderId: Long, order: CompanyOrderEntity, recordAsPurchase: Boolean = true) = withContext(Dispatchers.IO) {
        companyOrderDao.confirmOrder(orderId)
        if (recordAsPurchase && order.totalAmount > 0) {
            transactionDao.insertTransaction(
                TransactionEntity(
                    type = TransactionType.PURCHASE_DIRECT,
                    amount = order.totalAmount,
                    productName = "${order.companyName} অর্ডার মাল",
                    category = "কোম্পানি অর্ডার",
                    note = if (order.note.isNotBlank()) "অর্ডার গ্রহণ সম্পন্ন: ${order.note}" else "কোম্পানি অর্ডার গ্রহণ সম্পন্ন",
                    paymentStatus = "PAID",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun cancelCompanyOrder(orderId: Long) = withContext(Dispatchers.IO) {
        companyOrderDao.cancelOrder(orderId)
    }

    suspend fun deleteCompanyOrder(orderId: Long) = withContext(Dispatchers.IO) {
        companyOrderDao.deleteOrder(orderId)
    }

    suspend fun seedInitialCompanyOrdersIfEmpty() = withContext(Dispatchers.IO) {
        // No demo data - user will create their own orders
        clearDemoScheduleData()
    }

    suspend fun clearDemoScheduleData() = withContext(Dispatchers.IO) {
        try {
            // 1. Clear any demo company orders
            val allOrders = companyOrderDao.getAllOrdersDirect()
            val dummyOrders = allOrders.filter {
                (it.companyName == "প্রাণ ফুডস" || it.companyName == "মেঘনা গ্রুপ" || it.companyName == "আকিজ বেকারি") &&
                (it.note == "কম স্টক" || it.note == "বিশেষ মাল")
            }
            dummyOrders.forEach { companyOrderDao.deleteOrder(it.id) }

            // 2. Clear any demo companies created with dummy phone numbers
            val companies = supplierCompanyDao.getAllCompaniesDirect()
            val demoCompanies = companies.filter {
                it.srPhone.startsWith("0170000000") || it.srName.contains("(প্রাণ SR)")
            }
            demoCompanies.forEach { supplierCompanyDao.deleteCompanyById(it.id) }
        } catch (_: Exception) {}
    }

    suspend fun mergeEntityNames(oldName: String, newName: String) = withContext(Dispatchers.IO) {
        val cleanOld = oldName.trim()
        val cleanNew = newName.trim()
        if (cleanOld.isBlank() || cleanNew.isBlank() || cleanOld.equals(cleanNew, ignoreCase = true)) return@withContext

        // 1. Update transactions expense notes
        transactionDao.updateExpenseNote(cleanOld, cleanNew)

        // 2. Update transactions product names
        transactionDao.updateTransactionProductName(cleanOld, cleanNew)

        // 3. Update Fordi items
        fordiDao.updateFordiProductName(cleanOld, cleanNew)
        fordiDao.updateFordiCompanyName(cleanOld, cleanNew)

        // 4. Update Product memory
        productDao.updateProductName(cleanOld, cleanNew)

        // 5. Update Supplier Companies if matched
        val existingCompany = supplierCompanyDao.getCompanyByName(cleanOld)
        if (existingCompany != null) {
            supplierCompanyDao.updateCompany(existingCompany.copy(companyName = cleanNew))
        }
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

    suspend fun deleteAllFordiItems() = withContext(Dispatchers.IO) {
        fordiDao.deleteAllFordiItems()
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
        val existingProducts = productDao.getAllProductsDirect()
        if (existingProducts.isEmpty()) {
            val defaultProducts = listOf(
                ProductEntity(name = "মিনিকেট চাল", banglaName = "মিনিকেট চাল", category = "মুদি", unit = "কেজি", defaultPurchasePrice = 68.0, defaultSellingPrice = 72.0, stockQuantity = 25.0),
                ProductEntity(name = "নাজিরশাইল চাল", banglaName = "নাজিরশাইল চাল", category = "মুদি", unit = "কেজি", defaultPurchasePrice = 72.0, defaultSellingPrice = 78.0, stockQuantity = 15.0),
                ProductEntity(name = "সয়াবিন তেল (১ লিটার)", banglaName = "সয়াবিন তেল (রূপচাঁদা)", category = "মুদি", unit = "বোতল", defaultPurchasePrice = 180.0, defaultSellingPrice = 188.0, stockQuantity = 12.0),
                ProductEntity(name = "সয়াবিন তেল (৫ লিটার)", banglaName = "সয়াবিন তেল (৫ লিটার জার)", category = "মুদি", unit = "বোতল", defaultPurchasePrice = 870.0, defaultSellingPrice = 895.0, stockQuantity = 4.0),
                ProductEntity(name = "দেশি মসুর ডাল", banglaName = "দেশি মসুর ডাল", category = "মুদি", unit = "কেজি", defaultPurchasePrice = 120.0, defaultSellingPrice = 130.0, stockQuantity = 8.0),
                ProductEntity(name = "গোল আলু (নতুন)", banglaName = "গোল আলু", category = "কাঁচাবাজার", unit = "কেজি", defaultPurchasePrice = 40.0, defaultSellingPrice = 45.0, stockQuantity = 30.0),
                ProductEntity(name = "দেশি পেঁয়াজ", banglaName = "দেশি পেঁয়াজ", category = "কাঁচাবাজার", unit = "কেজি", defaultPurchasePrice = 75.0, defaultSellingPrice = 85.0, stockQuantity = 2.0),
                ProductEntity(name = "রসুন (দেশি)", banglaName = "রসুন", category = "কাঁচাবাজার", unit = "কেজি", defaultPurchasePrice = 200.0, defaultSellingPrice = 220.0, stockQuantity = 5.0),
                ProductEntity(name = "আদা", banglaName = "আদা", category = "কাঁচাবাজার", unit = "কেজি", defaultPurchasePrice = 220.0, defaultSellingPrice = 240.0, stockQuantity = 0.0),
                ProductEntity(name = "কাঁচা মরিচ", banglaName = "কাঁচা মরিচ", category = "কাঁচাবাজার", unit = "কেজি", defaultPurchasePrice = 130.0, defaultSellingPrice = 150.0, stockQuantity = 3.0),
                ProductEntity(name = "সাদা চিনি", banglaName = "চিনি", category = "মুদি", unit = "কেজি", defaultPurchasePrice = 128.0, defaultSellingPrice = 135.0, stockQuantity = 20.0),
                ProductEntity(name = "আয়োডিনযুক্ত লবণ", banglaName = "লবণ", category = "মুদি", unit = "প্যাকেট", defaultPurchasePrice = 38.0, defaultSellingPrice = 42.0, stockQuantity = 15.0),
                ProductEntity(name = "ফার্মের লাল ডিম", banglaName = "লাল ডিম", category = "ডিম", unit = "টি", defaultPurchasePrice = 12.0, defaultSellingPrice = 13.5, stockQuantity = 48.0),
                ProductEntity(name = "তীর আটা (২ কেজি)", banglaName = "তীর আটা প্যাকেট", category = "মুদি", unit = "প্যাকেট", defaultPurchasePrice = 110.0, defaultSellingPrice = 120.0, stockQuantity = 1.0),
                ProductEntity(name = "ইস্পাহানি মির্জাপুর চা", banglaName = "ইস্পাহানি চা", category = "মুদি", unit = "প্যাকেট", defaultPurchasePrice = 215.0, defaultSellingPrice = 230.0, stockQuantity = 6.0),
                ProductEntity(name = "মার্কস গুঁড়ো দুধ", banglaName = "মার্কস গুঁড়ো দুধ ৫০০ গ্রাম", category = "মুদি", unit = "প্যাকেট", defaultPurchasePrice = 420.0, defaultSellingPrice = 440.0, stockQuantity = 0.0)
            )
            defaultProducts.forEach { productDao.insertProduct(it) }
        }
    }

    suspend fun seedStandardProductsIfEmpty() = withContext(Dispatchers.IO) {
        val existingProducts = productDao.getAllProductsDirect()
        if (existingProducts.isEmpty()) {
            val defaultProducts = listOf(
                ProductEntity(name = "মিনিকেট চাল", banglaName = "মিনিকেট চাল", category = "মুদি", unit = "কেজি", defaultPurchasePrice = 68.0, defaultSellingPrice = 72.0, stockQuantity = 25.0),
                ProductEntity(name = "নাজিরশাইল চাল", banglaName = "নাজিরশাইল চাল", category = "মুদি", unit = "কেজি", defaultPurchasePrice = 72.0, defaultSellingPrice = 78.0, stockQuantity = 15.0),
                ProductEntity(name = "সয়াবিন তেল (১ লিটার)", banglaName = "সয়াবিন তেল (রূপচাঁদা)", category = "মুদি", unit = "বোতল", defaultPurchasePrice = 180.0, defaultSellingPrice = 188.0, stockQuantity = 12.0),
                ProductEntity(name = "সয়াবিন তেল (৫ লিটার)", banglaName = "সয়াবিন তেল (৫ লিটার জার)", category = "মুদি", unit = "বোতল", defaultPurchasePrice = 870.0, defaultSellingPrice = 895.0, stockQuantity = 4.0),
                ProductEntity(name = "দেশি মসুর ডাল", banglaName = "দেশি মসুর ডাল", category = "মুদি", unit = "কেজি", defaultPurchasePrice = 120.0, defaultSellingPrice = 130.0, stockQuantity = 8.0),
                ProductEntity(name = "গোল আলু (নতুন)", banglaName = "গোল আলু", category = "কাঁচাবাজার", unit = "কেজি", defaultPurchasePrice = 40.0, defaultSellingPrice = 45.0, stockQuantity = 30.0),
                ProductEntity(name = "দেশি পেঁয়াজ", banglaName = "দেশি পেঁয়াজ", category = "কাঁচাবাজার", unit = "কেজি", defaultPurchasePrice = 75.0, defaultSellingPrice = 85.0, stockQuantity = 2.0),
                ProductEntity(name = "রসুন (দেশি)", banglaName = "রসুন", category = "কাঁচাবাজার", unit = "কেজি", defaultPurchasePrice = 200.0, defaultSellingPrice = 220.0, stockQuantity = 5.0),
                ProductEntity(name = "আদা", banglaName = "আদা", category = "কাঁচাবাজার", unit = "কেজি", defaultPurchasePrice = 220.0, defaultSellingPrice = 240.0, stockQuantity = 0.0),
                ProductEntity(name = "কাঁচা মরিচ", banglaName = "কাঁচা মরিচ", category = "কাঁচাবাজার", unit = "কেজি", defaultPurchasePrice = 130.0, defaultSellingPrice = 150.0, stockQuantity = 3.0),
                ProductEntity(name = "সাদা চিনি", banglaName = "চিনি", category = "মুদি", unit = "কেজি", defaultPurchasePrice = 128.0, defaultSellingPrice = 135.0, stockQuantity = 20.0),
                ProductEntity(name = "আয়োডিনযুক্ত লবণ", banglaName = "লবণ", category = "মুদি", unit = "প্যাকেট", defaultPurchasePrice = 38.0, defaultSellingPrice = 42.0, stockQuantity = 15.0),
                ProductEntity(name = "ফার্মের লাল ডিম", banglaName = "লাল ডিম", category = "ডিম", unit = "টি", defaultPurchasePrice = 12.0, defaultSellingPrice = 13.5, stockQuantity = 48.0),
                ProductEntity(name = "তীর আটা (২ কেজি)", banglaName = "তীর আটা প্যাকেট", category = "মুদি", unit = "প্যাকেট", defaultPurchasePrice = 110.0, defaultSellingPrice = 120.0, stockQuantity = 1.0),
                ProductEntity(name = "ইস্পাহানি মির্জাপুর চা", banglaName = "ইস্পাহানি চা", category = "মুদি", unit = "প্যাকেট", defaultPurchasePrice = 215.0, defaultSellingPrice = 230.0, stockQuantity = 6.0),
                ProductEntity(name = "মার্কস গুঁড়ো দুধ", banglaName = "মার্কস গুঁড়ো দুধ ৫০০ গ্রাম", category = "মুদি", unit = "প্যাকেট", defaultPurchasePrice = 420.0, defaultSellingPrice = 440.0, stockQuantity = 0.0)
            )
            defaultProducts.forEach { productDao.insertProduct(it) }
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
        val dailyCashRecords = dailyCashDao.getAllDailyCashDirect()
        val supplierCompanies = supplierCompanyDao.getAllCompaniesDirect()
        val cashTallyRecords = cashTallyDao.getAllTalliesDirect()

        FullBackupData(
            exportDate = System.currentTimeMillis(),
            shopSettings = settings,
            customers = customers,
            transactions = transactions,
            fordiItems = fordiItems,
            products = products,
            personalTransactions = personalTransactions,
            dailyCashRecords = dailyCashRecords,
            supplierCompanies = supplierCompanies,
            cashTallyRecords = cashTallyRecords
        )
    }

    suspend fun restoreFullBackup(data: FullBackupData, overwriteExisting: Boolean = true) = withContext(Dispatchers.IO) {
        if (overwriteExisting) {
            transactionDao.deleteAllTransactions()
            customerDao.deleteAllCustomers()
            fordiDao.deleteAllFordiItems()
            productDao.deleteAllProducts()
            personalTransactionDao.deleteAll()
            dailyCashDao.deleteAllDailyCash()
            cashTallyDao.deleteAllTallies()
            val existingCompanies = supplierCompanyDao.getAllCompaniesDirect()
            existingCompanies.forEach { supplierCompanyDao.deleteCompany(it) }
        }

        data.shopSettings?.let {
            settingsDao.insertOrUpdateSettings(it)
        }

        // Restore Supplier Companies with deduplication
        if (data.supplierCompanies.isNotEmpty()) {
            if (overwriteExisting) {
                supplierCompanyDao.insertCompanies(data.supplierCompanies)
            } else {
                val existingNames = supplierCompanyDao.getAllCompaniesDirect().map { it.companyName.trim().lowercase() }.toSet()
                val toInsert = data.supplierCompanies.filter { !existingNames.contains(it.companyName.trim().lowercase()) }
                if (toInsert.isNotEmpty()) {
                    supplierCompanyDao.insertCompanies(toInsert)
                }
            }
        }

        // Restore Daily Cash Records with intelligent value preservation
        if (data.dailyCashRecords.isNotEmpty()) {
            if (overwriteExisting) {
                dailyCashDao.insertDailyCashList(data.dailyCashRecords)
            } else {
                val existingDailyCash = dailyCashDao.getAllDailyCashDirect().associateBy { it.dateKey }.toMutableMap()
                val dcToSave = mutableListOf<DailyCashEntity>()

                for (incoming in data.dailyCashRecords) {
                    val local = existingDailyCash[incoming.dateKey]
                    if (local == null) {
                        dcToSave.add(incoming)
                    } else {
                        // Merge without losing either sabekCash or closingCash!
                        val mergedSabek = if (local.sabekCash > 0) local.sabekCash else incoming.sabekCash
                        val mergedClosing = if (local.closingCash > 0) local.closingCash else incoming.closingCash
                        val mergedClosed = local.isClosed || incoming.isClosed
                        val mergedMillis = if (local.dateMillis > 0) local.dateMillis else incoming.dateMillis
                        val mergedUpdated = maxOf(local.updatedAt, incoming.updatedAt)

                        val merged = local.copy(
                            dateMillis = mergedMillis,
                            sabekCash = mergedSabek,
                            closingCash = mergedClosing,
                            isClosed = mergedClosed,
                            updatedAt = mergedUpdated
                        )
                        if (merged != local) {
                            dcToSave.add(merged)
                        }
                    }
                }
                if (dcToSave.isNotEmpty()) {
                    dailyCashDao.insertDailyCashList(dcToSave)
                }
            }
        }

        // Restore Cash Tally Records with deduplication
        if (data.cashTallyRecords.isNotEmpty()) {
            if (overwriteExisting) {
                cashTallyDao.insertTallies(data.cashTallyRecords)
            } else {
                val existingTallies = cashTallyDao.getAllTalliesDirect().map { it.dateMillis / 60000L }.toSet()
                val talliesToInsert = data.cashTallyRecords.filter { !existingTallies.contains(it.dateMillis / 60000L) }
                if (talliesToInsert.isNotEmpty()) {
                    cashTallyDao.insertTallies(talliesToInsert)
                }
            }
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
            if (overwriteExisting) {
                transactionDao.insertTransactions(finalTransactions)
            } else {
                // Deduplicate transactions against existing DB records using robust cross-app content signature
                val existingTxList = transactionDao.getAllTransactionsDirect().toMutableList()
                val existingTxIds = existingTxList.map { it.id }.toSet()
                
                fun isTxDuplicate(tx: TransactionEntity, existing: List<TransactionEntity>): Boolean {
                    if (tx.id > 0 && existingTxIds.contains(tx.id)) return true
                    val cName = tx.customerName?.trim()?.lowercase() ?: ""
                    val pName = tx.productName?.trim()?.lowercase() ?: ""
                    val noteTrim = tx.note.trim().lowercase()
                    val amtCents = (tx.amount * 100).toLong()
                    val dayBucket = tx.timestamp / 86400000L

                    return existing.any { ex ->
                        val exAmtCents = (ex.amount * 100).toLong()
                        if (exAmtCents != amtCents || ex.type != tx.type) return@any false

                        val exCName = ex.customerName?.trim()?.lowercase() ?: ""
                        val exPName = ex.productName?.trim()?.lowercase() ?: ""
                        val exNoteTrim = ex.note.trim().lowercase()
                        val exDayBucket = ex.timestamp / 86400000L

                        // 1. Exact match within 5 minutes
                        val timeDiff = kotlin.math.abs(ex.timestamp - tx.timestamp)
                        if (timeDiff <= 300_000L) {
                            if (cName.isNotBlank() && exCName.isNotBlank()) {
                                return@any cName == exCName
                            }
                            return@any true
                        }

                        // 2. Same day, same customer match
                        if (dayBucket == exDayBucket && cName.isNotBlank() && cName == exCName) {
                            return@any true
                        }

                        // 3. Same day cash sale match
                        if (dayBucket == exDayBucket && tx.type == com.example.mawa.data.local.entity.TransactionType.SALE_CASH && ex.type == com.example.mawa.data.local.entity.TransactionType.SALE_CASH) {
                            return@any true
                        }

                        // 4. Same day, same note match
                        if (dayBucket == exDayBucket && noteTrim.isNotBlank() && noteTrim == exNoteTrim) {
                            return@any true
                        }

                        false
                    }
                }

                val txToInsert = mutableListOf<TransactionEntity>()
                val combinedExisting = existingTxList.toMutableList()

                for (tx in finalTransactions) {
                    if (!isTxDuplicate(tx, combinedExisting)) {
                        txToInsert.add(tx)
                        combinedExisting.add(tx)
                    }
                }

                if (txToInsert.isNotEmpty()) {
                    transactionDao.insertTransactions(txToInsert)
                }
            }
        }

        // Restore Fordi Items with deduplication
        if (data.fordiItems.isNotEmpty()) {
            if (overwriteExisting) {
                fordiDao.insertFordiItems(data.fordiItems)
            } else {
                val existingFordi = fordiDao.getAllFordiItemsDirect()
                val existingFordiIds = existingFordi.map { it.id }.toSet()
                fun fordiSig(f: FordiItemEntity): String {
                    val pName = f.productName.trim().lowercase()
                    val pDate = f.createdAt / 86400000L // same day
                    return "${pName}_${pDate}_${f.plannedQuantity}_${f.purchaseRate}"
                }
                val existingFordiSigs = existingFordi.map { fordiSig(it) }.toMutableSet()
                val fordiToInsert = data.fordiItems.filter { f ->
                    val idMatch = f.id > 0 && existingFordiIds.contains(f.id)
                    val sigMatch = existingFordiSigs.contains(fordiSig(f))
                    if (!idMatch && !sigMatch) {
                        existingFordiSigs.add(fordiSig(f))
                        true
                    } else false
                }
                if (fordiToInsert.isNotEmpty()) {
                    fordiDao.insertFordiItems(fordiToInsert)
                }
            }
        }

        // Restore Products with deduplication
        if (data.products.isNotEmpty()) {
            if (overwriteExisting) {
                productDao.insertProducts(data.products)
            } else {
                val existingProducts = productDao.getAllProductsDirect()
                val existingProdNames = existingProducts.map { it.name.trim().lowercase() }.toSet()
                val productsToInsert = data.products.filter { p ->
                    !existingProdNames.contains(p.name.trim().lowercase())
                }
                if (productsToInsert.isNotEmpty()) {
                    productDao.insertProducts(productsToInsert)
                }
            }
        }

        // Restore Personal Transactions with deduplication
        if (data.personalTransactions.isNotEmpty()) {
            if (overwriteExisting) {
                personalTransactionDao.insertAll(data.personalTransactions)
            } else {
                val existingPersonal = personalTransactionDao.getAllPersonalTransactionsDirect()
                val existingPIds = existingPersonal.map { it.id }.toSet()
                fun pTxSig(pt: PersonalTransactionEntity): String {
                    val timeBucket = pt.timestamp / 2000L
                    return "${pt.type.name}_${pt.amount}_${timeBucket}_${pt.title.trim()}_${pt.category}"
                }
                val existingPSigs = existingPersonal.map { pTxSig(it) }.toMutableSet()
                val pTxToInsert = data.personalTransactions.filter { pt ->
                    val idMatch = pt.id > 0 && existingPIds.contains(pt.id)
                    val sigMatch = existingPSigs.contains(pTxSig(pt))
                    if (!idMatch && !sigMatch) {
                        existingPSigs.add(pTxSig(pt))
                        true
                    } else false
                }
                if (pTxToInsert.isNotEmpty()) {
                    personalTransactionDao.insertAll(pTxToInsert)
                }
            }
        }
    }

    suspend fun saveDailySabekCash(dateKey: String, dateMillis: Long, sabekAmount: Double) = withContext(Dispatchers.IO) {
        val existing = dailyCashDao.getDailyCashDirect(dateKey)
        val updated = existing?.copy(sabekCash = sabekAmount, updatedAt = System.currentTimeMillis())
            ?: DailyCashEntity(dateKey = dateKey, dateMillis = dateMillis, sabekCash = sabekAmount)
        dailyCashDao.insertOrUpdateDailyCash(updated)
    }

    suspend fun saveDailyClosingCash(dateKey: String, dateMillis: Long, closingAmount: Double, isClosed: Boolean = true) = withContext(Dispatchers.IO) {
        val existing = dailyCashDao.getDailyCashDirect(dateKey)
        val updated = existing?.copy(closingCash = closingAmount, isClosed = isClosed, updatedAt = System.currentTimeMillis())
            ?: DailyCashEntity(dateKey = dateKey, dateMillis = dateMillis, closingCash = closingAmount, isClosed = isClosed)
        dailyCashDao.insertOrUpdateDailyCash(updated)
    }

    suspend fun getDailyCash(dateKey: String): DailyCashEntity? = withContext(Dispatchers.IO) {
        dailyCashDao.getDailyCashDirect(dateKey)
    }

    fun getDailyCashFlow(dateKey: String): Flow<DailyCashEntity?> = dailyCashDao.getDailyCashFlow(dateKey)

    // --- Cash Tally Operations (নোট কাউন্ট ও সেভ) ---
    fun getCashTalliesForDateFlow(dateKey: String): Flow<List<CashTallyEntity>> = cashTallyDao.getTalliesForDateFlow(dateKey)
    fun getLatestCashTallyForDateFlow(dateKey: String): Flow<CashTallyEntity?> = cashTallyDao.getLatestTallyForDateFlow(dateKey)
    suspend fun getLatestCashTallyForDate(dateKey: String): CashTallyEntity? = withContext(Dispatchers.IO) { cashTallyDao.getLatestTallyForDate(dateKey) }
    suspend fun saveCashTally(tally: CashTallyEntity): Long = withContext(Dispatchers.IO) { cashTallyDao.insertTally(tally) }
    suspend fun deleteCashTally(id: Long) = withContext(Dispatchers.IO) { cashTallyDao.deleteTallyById(id) }

    suspend fun importCustomers(customers: List<CustomerEntity>) = withContext(Dispatchers.IO) {
        if (customers.isNotEmpty()) {
            val existing = customerDao.getAllCustomersDirect()
            val existingNames = existing.map { it.name.trim().lowercase() }.toSet()
            val toInsert = customers.filter { !existingNames.contains(it.name.trim().lowercase()) }
            if (toInsert.isNotEmpty()) {
                customerDao.insertCustomers(toInsert)
            }
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
