package com.example.mawa.data.remote.supabase

import android.content.Context
import android.util.Log
import com.example.mawa.util.FullBackupData
import com.example.mawa.data.repository.MawaRepository
import com.example.mawa.util.DataBackupRestoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Cloud Sync Engine for MAWA Accounting System.
 * Connects to Supabase Database (user_backups & mawa_cloud_records)
 * Full offline-first design:
 * - Saves locally first
 * - Queues pending items
 * - Pushes & merges safely with RLS protection and deduplication
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

    interface SyncCallback {
        fun onSuccess(message: String)
        fun onError(error: String)
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
}
