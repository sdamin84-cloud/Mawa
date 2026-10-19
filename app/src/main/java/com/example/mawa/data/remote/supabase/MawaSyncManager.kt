package com.example.mawa.data.remote.supabase

import android.content.Context
import android.util.Log
import com.example.mawa.data.local.entity.CustomerEntity
import com.example.mawa.data.local.entity.DailyCashEntity
import com.example.mawa.data.local.entity.FordiItemEntity
import com.example.mawa.data.local.entity.ProductEntity
import com.example.mawa.data.local.entity.ShopSettingsEntity
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.repository.MawaRepository
import com.example.mawa.util.DataBackupRestoreManager
import com.example.mawa.util.FullBackupData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Cloud Sync Engine for MAWA Accounting System.
 * Connects to Supabase Database (user_backups & mawa_cloud_records).
 * Features:
 * - Instantaneous restore & pull on user login
 * - Real-time background sync loop for live multi-device updates
 * - Debounced auto-push on any local data mutation
 * - Full offline-first fallback
 */
class MawaSyncManager(
    private val context: Context,
    private val repository: MawaRepository,
    private val authManager: SupabaseAuthManager,
    private val dbManager: SupabaseDbManager
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<String?>(null)
    val lastSyncTimestamp: StateFlow<String?> = _lastSyncTimestamp.asStateFlow()

    private var debounceSyncJob: Job? = null
    private var realtimeLoopJob: Job? = null

    init {
        // Start background real-time sync loop
        startRealtimeSyncLoop()
    }

    interface SyncCallback {
        fun onSuccess(message: String)
        fun onError(error: String)
    }

    /**
     * Start periodic real-time sync polling loop (every 12 seconds if logged in).
     */
    fun startRealtimeSyncLoop() {
        realtimeLoopJob?.cancel()
        realtimeLoopJob = scope.launch {
            while (isActive) {
                delay(12_000L) // 12 seconds interval for live responsive updates
                if (authManager.isLoggedIn() && !_isSyncing.value) {
                    try {
                        Log.d("MawaSyncManager", "Realtime poller checking for remote updates...")
                        pullRemoteSnapshot { _, _ -> }
                    } catch (e: Exception) {
                        Log.w("MawaSyncManager", "Realtime poller loop error: ${e.message}")
                    }
                }
            }
        }
    }

    /**
     * Schedules a debounced auto-sync to push local changes to Supabase in real-time
     * and prune any server records that were deleted locally.
     */
    fun scheduleDebouncedAutoSync(delayMillis: Long = 1000L) {
        if (!authManager.isLoggedIn()) return

        debounceSyncJob?.cancel()
        debounceSyncJob = scope.launch {
            delay(delayMillis)
            try {
                if (authManager.isLoggedIn() && !_isSyncing.value) {
                    Log.d("MawaSyncManager", "Executing debounced auto-sync push to Supabase...")
                    val localBackup = repository.getFullBackupData()
                    dbManager.uploadBackupToCloud("MAWA_LIVE_BACKUP", localBackup)
                    dbManager.syncAllRecordsToCloud(localBackup)

                    // Prune server-side records that were deleted locally
                    val validEntityIds = mutableSetOf<String>()
                    localBackup.shopSettings?.let { validEntityIds.add("shop_settings_${it.id}") }
                    localBackup.customers.forEach { validEntityIds.add("baki_${it.id}") }
                    localBackup.transactions.forEach { validEntityIds.add("tx_${it.id}") }
                    localBackup.products.forEach { validEntityIds.add("prod_${it.id}") }
                    localBackup.dailyCashRecords.forEach { validEntityIds.add("daily_cash_${it.dateKey}") }
                    localBackup.personalTransactions.forEach { validEntityIds.add("ptx_${it.id}") }
                    localBackup.fordiItems.groupBy { SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(it.createdAt)) }.keys.forEach {
                        validEntityIds.add("fordi_$it")
                    }

                    dbManager.deleteOrphanedCloudRecords(validEntityIds)

                    val timeNow = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US).format(Date())
                    _lastSyncTimestamp.value = timeNow
                }
            } catch (e: Exception) {
                Log.w("MawaSyncManager", "Debounced auto-sync failed: ${e.message}")
            }
        }
    }

    /**
     * Pulls latest remote backup snapshot or records immediately on login.
     */
    fun syncImmediatelyOnLogin(onComplete: (Boolean, String) -> Unit) {
        scope.launch {
            _isSyncing.value = true
            try {
                Log.d("MawaSyncManager", "Performing immediate sync on login...")
                pullRemoteSnapshot { pullSuccess, pullMsg ->
                    scope.launch {
                        try {
                            val localBackup = repository.getFullBackupData()
                            // If local has new changes, push them as well
                            dbManager.uploadBackupToCloud("MAWA_LIVE_BACKUP", localBackup)
                            dbManager.syncAllRecordsToCloud(localBackup)

                            val timeNow = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US).format(Date())
                            _lastSyncTimestamp.value = timeNow
                            _isSyncing.value = false

                            withContext(Dispatchers.Main) {
                                onComplete(true, if (pullSuccess) pullMsg else "ক্লাউড সিঙ্ক সক্রিয় হয়েছে")
                            }
                        } catch (e: Exception) {
                            _isSyncing.value = false
                            withContext(Dispatchers.Main) {
                                onComplete(pullSuccess, pullMsg)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _isSyncing.value = false
                withContext(Dispatchers.Main) {
                    onComplete(false, "সিঙ্ক ত্রুটি: ${e.localizedMessage}")
                }
            }
        }
    }

    /**
     * Triggers the full 3-step synchronization:
     * 1. Pull latest snapshot from Supabase user_backups / mawa_cloud_records
     * 2. Push full local backup snapshot to user_backups
     * 3. Push granular records to mawa_cloud_records
     */
    fun triggerSync(callback: SyncCallback? = null) {
        if (_isSyncing.value) {
            callback?.onError("সিঙ্ক ইতিমধ্যে চলমান রয়েছে")
            return
        }

        if (!authManager.isLoggedIn()) {
            callback?.onError("ক্লাউড সিঙ্কের জন্য আগে লগইন করুন")
            return
        }

        scope.launch {
            _isSyncing.value = true
            try {
                Log.d("MawaSyncManager", "Starting 3-step sync...")

                // Step 1: Pull & merge remote snapshot
                pullRemoteSnapshot { pullSuccess, pullMsg ->
                    Log.d("MawaSyncManager", "Pull step completed: $pullSuccess, msg: $pullMsg")

                    // Step 2 & 3: Push snapshots & records
                    scope.launch {
                        try {
                            val localBackup = repository.getFullBackupData()

                            // Push full backup snapshot
                            val uploadRes = dbManager.uploadBackupToCloud("MAWA_SYNC_SNAPSHOT", localBackup)
                            Log.d("MawaSyncManager", "Upload backup snapshot result: $uploadRes")

                            // Push granular records
                            val syncRes = dbManager.syncAllRecordsToCloud(localBackup)
                            Log.d("MawaSyncManager", "Sync records result: $syncRes")

                            val timeNow = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US).format(Date())
                            _lastSyncTimestamp.value = timeNow
                            _isSyncing.value = false

                            withContext(Dispatchers.Main) {
                                callback?.onSuccess("ক্লাউড সিঙ্ক সফলভাবে সম্পন্ন হয়েছে!")
                            }
                        } catch (e: Exception) {
                            Log.e("MawaSyncManager", "Push steps error", e)
                            _isSyncing.value = false
                            withContext(Dispatchers.Main) {
                                callback?.onError("সিঙ্ক সম্পূর্ণ হতে সমস্যা হয়েছে: ${e.localizedMessage}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MawaSyncManager", "Sync error", e)
                _isSyncing.value = false
                withContext(Dispatchers.Main) {
                    callback?.onError("সিঙ্ক ব্যর্থ: ${e.localizedMessage}")
                }
            }
        }
    }

    /**
     * Pulls latest remote backup snapshot or records and merges into local database.
     */
    fun pullRemoteSnapshot(onComplete: (Boolean, String) -> Unit) {
        scope.launch {
            try {
                // 1. Try pulling user_backups first
                when (val res = dbManager.fetchCloudBackups()) {
                    is CloudOperationResult.Success -> {
                        if (res.data.isNotEmpty()) {
                            val latest = res.data.first()
                            if (latest.dataJson.isNotBlank()) {
                                val backupData = DataBackupRestoreManager.parseFromJsonString(latest.dataJson)
                                if (backupData.customers.isNotEmpty() || backupData.transactions.isNotEmpty() || backupData.fordiItems.isNotEmpty() || backupData.products.isNotEmpty()) {
                                    repository.restoreFullBackup(backupData, overwriteExisting = false)
                                    val msg = "সুপাবেজ ক্লাউড থেকে '${latest.backupName}' লোড হয়েছে"
                                    withContext(Dispatchers.Main) {
                                        onComplete(true, msg)
                                    }
                                    return@launch
                                }
                            }
                        }
                    }
                    is CloudOperationResult.Error -> {
                        Log.w("MawaSyncManager", "user_backups fetch failed: ${res.message}")
                    }
                }

                // 2. Fallback to mawa_cloud_records if user_backups was empty
                when (val incRes = dbManager.fetchAllIncrementalRecords()) {
                    is CloudOperationResult.Success -> {
                        val incData = incRes.data
                        if (incData.customers.isNotEmpty() || incData.transactions.isNotEmpty() || incData.fordiItems.isNotEmpty() || incData.products.isNotEmpty() || incData.dailyCashRecords.isNotEmpty()) {
                            repository.restoreFullBackup(incData, overwriteExisting = false)
                            withContext(Dispatchers.Main) {
                                onComplete(true, "ক্লাউড রেকর্ডস থেকে ডাটা মার্জ করা হয়েছে")
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                onComplete(true, "ক্লাউডে কোনো পূর্ববর্তী রেকর্ড নেই")
                            }
                        }
                    }
                    is CloudOperationResult.Error -> {
                        withContext(Dispatchers.Main) {
                            onComplete(false, incRes.message)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MawaSyncManager", "pullRemoteSnapshot error", e)
                withContext(Dispatchers.Main) {
                    onComplete(false, e.localizedMessage ?: "ডাটা পুল করতে ব্যর্থ")
                }
            }
        }
    }

    // =========================================================================
    //  GRANULAR LIVE REAL-TIME CRUD METHODS (Write to server / Delete from server)
    // =========================================================================

    fun pushCustomerLive(customer: CustomerEntity) {
        if (!authManager.isLoggedIn()) return
        scope.launch {
            try {
                val cJson = JSONObject().apply {
                    put("id", customer.id.toString())
                    put("customerName", customer.name)
                    put("name", customer.name)
                    put("phone", customer.phone)
                    put("address", customer.address)
                    put("details", customer.address)
                    put("openingBalance", customer.openingBalance)
                    put("note", customer.note)
                    put("createdAt", customer.createdAt)
                    put("updatedAt", System.currentTimeMillis())
                }
                dbManager.upsertSingleRecord(
                    domain = "business",
                    entityType = "BAKI",
                    entityId = "baki_${customer.id}",
                    data = cJson
                )
            } catch (e: Exception) {
                Log.w("MawaSyncManager", "pushCustomerLive error: ${e.message}")
            }
        }
    }

    fun deleteCustomerLive(customerId: Long) {
        if (!authManager.isLoggedIn()) return
        scope.launch {
            try {
                dbManager.deleteCloudRecord(
                    entityId = "baki_$customerId",
                    entityType = "BAKI"
                )
            } catch (e: Exception) {
                Log.w("MawaSyncManager", "deleteCustomerLive error: ${e.message}")
            }
        }
    }

    fun pushTransactionLive(tx: TransactionEntity) {
        if (!authManager.isLoggedIn()) return
        scope.launch {
            try {
                val tJson = JSONObject().apply {
                    put("id", tx.id.toString())
                    put("type", tx.type.name)
                    put("amount", tx.amount)
                    put("timestamp", tx.timestamp)
                    put("customerId", tx.customerId)
                    put("customerName", tx.customerName)
                    put("productName", tx.productName)
                    put("note", tx.note)
                    put("category", tx.category)
                    put("date", SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(tx.timestamp)))
                    put("time", SimpleDateFormat("hh:mm a", Locale.US).format(Date(tx.timestamp)))
                    put("updatedAt", tx.timestamp)
                }
                val entityType = when (tx.type) {
                    com.example.mawa.data.local.entity.TransactionType.SALE_BAKI,
                    com.example.mawa.data.local.entity.TransactionType.BAKI_COLLECTION -> "BAKI_TX"
                    com.example.mawa.data.local.entity.TransactionType.EXPENSE_SHOP,
                    com.example.mawa.data.local.entity.TransactionType.EXPENSE_HOME,
                    com.example.mawa.data.local.entity.TransactionType.PURCHASE_DIRECT,
                    com.example.mawa.data.local.entity.TransactionType.PURCHASE_FORDI -> "EXPENSE"
                    else -> "SALE"
                }
                dbManager.upsertSingleRecord(
                    domain = "business",
                    entityType = entityType,
                    entityId = "tx_${tx.id}",
                    data = tJson
                )
            } catch (e: Exception) {
                Log.w("MawaSyncManager", "pushTransactionLive error: ${e.message}")
            }
        }
    }

    fun deleteTransactionLive(transactionId: Long) {
        if (!authManager.isLoggedIn()) return
        scope.launch {
            try {
                dbManager.deleteCloudRecord(entityId = "tx_$transactionId")
            } catch (e: Exception) {
                Log.w("MawaSyncManager", "deleteTransactionLive error: ${e.message}")
            }
        }
    }

    fun pushProductLive(p: ProductEntity) {
        if (!authManager.isLoggedIn()) return
        scope.launch {
            try {
                val pJson = JSONObject().apply {
                    put("id", p.id.toString())
                    put("name", p.name)
                    put("banglaName", p.banglaName)
                    put("category", p.category)
                    put("stockQuantity", p.stockQuantity)
                    put("unit", p.unit)
                    put("defaultPurchasePrice", p.defaultPurchasePrice)
                    put("defaultSellingPrice", p.defaultSellingPrice)
                    put("createdAt", p.createdAt)
                    put("updatedAt", System.currentTimeMillis())
                }
                dbManager.upsertSingleRecord(
                    domain = "business",
                    entityType = "PRODUCT",
                    entityId = "prod_${p.id}",
                    data = pJson
                )
            } catch (e: Exception) {
                Log.w("MawaSyncManager", "pushProductLive error: ${e.message}")
            }
        }
    }

    fun deleteProductLive(productId: Long) {
        if (!authManager.isLoggedIn()) return
        scope.launch {
            try {
                dbManager.deleteCloudRecord(
                    entityId = "prod_$productId",
                    entityType = "PRODUCT"
                )
            } catch (e: Exception) {
                Log.w("MawaSyncManager", "deleteProductLive error: ${e.message}")
            }
        }
    }

    fun pushDailyCashLive(dc: DailyCashEntity) {
        if (!authManager.isLoggedIn()) return
        scope.launch {
            try {
                val dcJson = JSONObject().apply {
                    put("dateKey", dc.dateKey)
                    put("dateMillis", dc.dateMillis)
                    put("sabekCash", dc.sabekCash)
                    put("closingCash", dc.closingCash)
                    put("isClosed", dc.isClosed)
                    put("updatedAt", dc.updatedAt)
                }
                dbManager.upsertSingleRecord(
                    domain = "business",
                    entityType = "DAILY_CASH",
                    entityId = "daily_cash_${dc.dateKey}",
                    data = dcJson
                )
            } catch (e: Exception) {
                Log.w("MawaSyncManager", "pushDailyCashLive error: ${e.message}")
            }
        }
    }

    fun pushFordiLive(items: List<FordiItemEntity>) {
        if (!authManager.isLoggedIn() || items.isEmpty()) return
        scope.launch {
            try {
                val fDate = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(items.first().createdAt))
                val fJson = JSONObject().apply {
                    put("id", "fordi_$fDate")
                    put("date", fDate)
                    put("updatedAt", System.currentTimeMillis())
                    val itemsArr = JSONArray()
                    items.forEach { f ->
                        val fo = JSONObject().apply {
                            put("id", f.id)
                            put("productName", f.productName)
                            put("plannedQuantity", f.plannedQuantity)
                            put("unit", f.unit)
                            put("purchaseRate", f.purchaseRate)
                            put("isPurchased", f.isPurchased)
                            put("actualTotal", f.actualTotal)
                        }
                        itemsArr.put(fo)
                    }
                    put("items", itemsArr)
                }
                dbManager.upsertSingleRecord(
                    domain = "business",
                    entityType = "FORDI",
                    entityId = "fordi_$fDate",
                    data = fJson
                )
            } catch (e: Exception) {
                Log.w("MawaSyncManager", "pushFordiLive error: ${e.message}")
            }
        }
    }

    fun deleteFordiLive(fDate: String) {
        if (!authManager.isLoggedIn()) return
        scope.launch {
            try {
                dbManager.deleteCloudRecord(
                    entityId = "fordi_$fDate",
                    entityType = "FORDI"
                )
            } catch (e: Exception) {
                Log.w("MawaSyncManager", "deleteFordiLive error: ${e.message}")
            }
        }
    }

    fun clearAllCloudRecordsLive() {
        if (!authManager.isLoggedIn()) return
        scope.launch {
            try {
                dbManager.deleteCloudRecordsForUser()
            } catch (e: Exception) {
                Log.w("MawaSyncManager", "clearAllCloudRecordsLive error: ${e.message}")
            }
        }
    }
}
