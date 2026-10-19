package com.example.mawa.data.remote.supabase

import android.util.Log
import com.example.mawa.util.DataBackupRestoreManager
import com.example.mawa.util.FullBackupData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class SupabaseDbManager(
    private val authManager: SupabaseAuthManager
) {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getAuthHeaders(): Map<String, String> {
        val token = authManager.getAccessToken()
        return mapOf(
            "apikey" to SupabaseConfig.SUPABASE_ANON_KEY,
            "Authorization" to "Bearer $token",
            "Content-Type" to "application/json"
        )
    }

    /**
     * Upload full database backup to Supabase `user_backups` table
     */
    suspend fun uploadBackupToCloud(
        backupName: String,
        backupData: FullBackupData
    ): CloudOperationResult<CloudBackupItem> = withContext(Dispatchers.IO) {
        val userId = authManager.getUserId()
        if (userId.isNullOrBlank()) {
            return@withContext CloudOperationResult.Error("ক্লাউডে ব্যাকআপ রাখতে অনুগ্রহ করে আগে লগইন করুন")
        }

        try {
            val jsonString = DataBackupRestoreManager.exportToJsonString(backupData)
            val jsonObjectData = JSONObject(jsonString)
            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())

            val resolvedBackupName = backupName.ifBlank { "MAWA_BACKUP_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}" }
            val record = JSONObject().apply {
                put("user_id", userId)
                authManager.getUserEmail()?.let { put("email", it) }
                put("backup_name", resolvedBackupName)
                put("backup_data", jsonObjectData) // Used by MawaSyncManager
                put("data", jsonObjectData)        // Kept for backward compatibility
                put("updated_at", nowIso)
            }

            // If it's a live backup, try PATCH first to update the single master row without piling duplicate backups
            val isLiveSync = resolvedBackupName.startsWith("MAWA_LIVE") || resolvedBackupName.startsWith("MAWA_LOGIN")
            var responseSuccess = false
            var responseBody = ""

            if (isLiveSync) {
                val patchUrl = "${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_USER_BACKUPS}?user_id=eq.$userId&backup_name=eq.$resolvedBackupName"
                val patchRequest = Request.Builder()
                    .url(patchUrl)
                    .addHeader("Prefer", "return=representation")
                    .patch(record.toString().toRequestBody(jsonMediaType))
                getAuthHeaders().forEach { (k, v) -> patchRequest.addHeader(k, v) }

                try {
                    client.newCall(patchRequest.build()).execute().use { patchResp ->
                        val pBody = patchResp.body?.string() ?: ""
                        if (patchResp.isSuccessful && pBody.isNotBlank() && pBody != "[]") {
                            responseSuccess = true
                            responseBody = pBody
                        }
                    }
                } catch (e: Exception) {
                    Log.w("SupabaseDb", "Live patch fallback: ${e.message}")
                }
            }

            if (!responseSuccess) {
                val primaryUrl = "${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_USER_BACKUPS}?on_conflict=user_id,backup_name"
                val requestBuilder = Request.Builder()
                    .url(primaryUrl)
                    .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                    .post(record.toString().toRequestBody(jsonMediaType))

                getAuthHeaders().forEach { (k, v) -> requestBuilder.addHeader(k, v) }

                client.newCall(requestBuilder.build()).execute().use { response ->
                    responseBody = response.body?.string() ?: ""
                    responseSuccess = response.isSuccessful
                    Log.d("SupabaseDb", "uploadBackup response: ${response.code} body: $responseBody")
                }

                // If 42P10 constraint mismatch or error, fallback to user_id or generic upsert
                if (!responseSuccess && responseBody.contains("42P10")) {
                    val fallbackUrl = "${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_USER_BACKUPS}?on_conflict=user_id"
                    val fbReq = Request.Builder()
                        .url(fallbackUrl)
                        .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                        .post(record.toString().toRequestBody(jsonMediaType))
                    getAuthHeaders().forEach { (k, v) -> fbReq.addHeader(k, v) }
                    try {
                        client.newCall(fbReq.build()).execute().use { fbResp ->
                            val fbBody = fbResp.body?.string() ?: ""
                            if (fbResp.isSuccessful) {
                                responseSuccess = true
                                responseBody = fbBody
                            }
                        }
                    } catch (_: Exception) {}
                }
            }

            if (responseSuccess) {
                val item = try {
                    if (responseBody.startsWith("[")) {
                        val arr = JSONArray(responseBody)
                        if (arr.length() > 0) parseBackupItem(arr.getJSONObject(0)) else null
                    } else if (responseBody.startsWith("{")) {
                        parseBackupItem(JSONObject(responseBody))
                    } else null
                } catch (e: Exception) {
                    null
                } ?: CloudBackupItem(
                    userId = userId,
                    backupName = resolvedBackupName,
                    dataJson = jsonString,
                    updatedAt = nowIso
                )

                return@withContext CloudOperationResult.Success(
                    data = item,
                    message = "সুপাবেজ ক্লাউডে ব্যাকআপ সফলভাবে সংরক্ষিত হয়েছে!"
                )
            } else {
                return@withContext CloudOperationResult.Error(
                    message = "ক্লাউড ব্যাকআপ আপলোড ব্যর্থ: ${parseError(responseBody, 400)}"
                )
            }
        } catch (e: Exception) {
            Log.e("SupabaseDb", "uploadBackup error", e)
            return@withContext CloudOperationResult.Error(
                message = "নেটওয়ার্ক ত্রুটি: ${e.localizedMessage ?: "ক্লাউড কানেকশন সমস্যা"}",
                exception = e
            )
        }
    }

    /**
     * List all backups stored in Supabase `user_backups` table for this user
     */
    suspend fun fetchCloudBackups(): CloudOperationResult<List<CloudBackupItem>> = withContext(Dispatchers.IO) {
        val userId = authManager.getUserId()
        if (userId.isNullOrBlank()) {
            return@withContext CloudOperationResult.Error("লগইন করা নেই")
        }

        try {
            val url = "${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_USER_BACKUPS}?user_id=eq.$userId&order=updated_at.desc,id.desc&limit=15"
            val requestBuilder = Request.Builder()
                .url(url)
                .get()

            getAuthHeaders().forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            client.newCall(requestBuilder.build()).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                Log.d("SupabaseDb", "fetchCloudBackups code: ${response.code}")

                if (response.isSuccessful) {
                    val list = mutableListOf<CloudBackupItem>()
                    if (responseBody.startsWith("[")) {
                        val arr = JSONArray(responseBody)
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            list.add(parseBackupItem(obj))
                        }
                    }
                    return@withContext CloudOperationResult.Success(data = list)
                } else {
                    return@withContext CloudOperationResult.Error(
                        message = "ক্লাউড ব্যাকআপ তালিকা আনা যায়নি: ${parseError(responseBody, response.code)}"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseDb", "fetchCloudBackups error", e)
            return@withContext CloudOperationResult.Error(
                message = "নেটওয়ার্ক ত্রুটি: ${e.localizedMessage ?: "সার্ভার এরর"}",
                exception = e
            )
        }
    }

    /**
     * Delete a backup from Supabase `user_backups` table
     */
    suspend fun deleteCloudBackup(backupId: Long): CloudOperationResult<Unit> = withContext(Dispatchers.IO) {
        val userId = authManager.getUserId()
        if (userId.isNullOrBlank()) {
            return@withContext CloudOperationResult.Error("লগইন করা নেই")
        }

        try {
            val url = "${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_USER_BACKUPS}?id=eq.$backupId&user_id=eq.$userId"
            val requestBuilder = Request.Builder()
                .url(url)
                .delete()

            getAuthHeaders().forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            client.newCall(requestBuilder.build()).execute().use { response ->
                if (response.isSuccessful) {
                    return@withContext CloudOperationResult.Success(
                        data = Unit,
                        message = "ক্লাউড ব্যাকআপ সফলভাবে মুছে ফেলা হয়েছে"
                    )
                } else {
                    val responseBody = response.body?.string() ?: ""
                    return@withContext CloudOperationResult.Error(
                        message = "মুছতে ব্যর্থ: ${parseError(responseBody, response.code)}"
                    )
                }
            }
        } catch (e: Exception) {
            return@withContext CloudOperationResult.Error(message = "ত্রুটি: ${e.localizedMessage}")
        }
    }

    /**
     * Sync discrete records into Supabase `mawa_cloud_records` table
     */
    suspend fun syncAllRecordsToCloud(
        backupData: FullBackupData
    ): CloudOperationResult<Int> = withContext(Dispatchers.IO) {
        val userId = authManager.getUserId()
        if (userId.isNullOrBlank()) {
            return@withContext CloudOperationResult.Error("ক্লাউড সিঙ্কের জন্য আগে লগইন করুন")
        }

        try {
            val recordsArray = JSONArray()
            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())

            // 1. Shop Settings (SETTINGS)
            backupData.shopSettings?.let { s ->
                val sJson = JSONObject().apply {
                    put("shopName", s.shopName)
                    put("ownerName", s.ownerName)
                    put("phone", s.phone)
                    put("bkashNumber", s.bkashNumber)
                    put("nagadNumber", s.nagadNumber)
                    put("bankDetails", s.bankDetails)
                    put("qrCodeData", s.qrCodeData)
                    put("openingBalance", s.openingBalance)
                    put("appMode", s.appMode)
                }
                recordsArray.put(JSONObject().apply {
                    put("user_id", userId)
                    put("domain", "business")
                    put("entity_type", "SETTINGS")
                    put("entity_id", "shop_settings_${s.id}")
                    put("data", sJson)
                    put("updated_at", nowIso)
                })
            }

            // 2. Products (PRODUCT)
            backupData.products.forEach { p ->
                val pJson = JSONObject().apply {
                    put("id", p.id.toString())
                    put("name", p.name)
                    put("banglaName", p.banglaName)
                    put("category", p.category)
                    put("stockQuantity", p.stockQuantity)
                    put("unit", p.unit)
                    put("defaultPurchasePrice", p.defaultPurchasePrice)
                    put("lastPurchasePrice", p.defaultPurchasePrice)
                    put("defaultSellingPrice", p.defaultSellingPrice)
                    put("sellingPrice", p.defaultSellingPrice)
                    put("createdAt", p.createdAt)
                    put("updatedAt", p.createdAt)
                }
                recordsArray.put(JSONObject().apply {
                    put("user_id", userId)
                    put("domain", "business")
                    put("entity_type", "PRODUCT")
                    put("entity_id", "prod_${p.id}")
                    put("data", pJson)
                    put("updated_at", nowIso)
                })
            }

            // 3. Customers / Baki (BAKI)
            backupData.customers.forEach { c ->
                val custTxList = backupData.transactions.filter { it.customerId == c.id || it.customerName.equals(c.name, ignoreCase = true) }
                val totalGiven = custTxList.filter { it.type == com.example.mawa.data.local.entity.TransactionType.SALE_BAKI }.sumOf { it.amount }
                val totalPaid = custTxList.filter { it.type == com.example.mawa.data.local.entity.TransactionType.BAKI_COLLECTION }.sumOf { it.amount }
                val netDue = (c.openingBalance + totalGiven - totalPaid).coerceAtLeast(0.0)

                val cJson = JSONObject().apply {
                    put("id", c.id.toString())
                    put("customerName", c.name)
                    put("name", c.name)
                    put("phone", c.phone)
                    put("address", c.address)
                    put("details", c.address)
                    put("openingBalance", c.openingBalance)
                    put("amount", netDue)
                    put("note", c.note)
                    put("createdAt", c.createdAt)
                    put("updatedAt", c.createdAt)

                    val innerTxArr = JSONArray()
                    custTxList.forEach { ctx ->
                        val txO = JSONObject()
                        txO.put("id", ctx.id)
                        txO.put("type", ctx.type.name)
                        txO.put("amount", ctx.amount)
                        txO.put("note", ctx.note)
                        txO.put("date", SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(ctx.timestamp)))
                        txO.put("time", SimpleDateFormat("hh:mm a", Locale.US).format(Date(ctx.timestamp)))
                        txO.put("updatedAt", ctx.timestamp)
                        innerTxArr.put(txO)
                    }
                    put("transactions", innerTxArr)
                }
                recordsArray.put(JSONObject().apply {
                    put("user_id", userId)
                    put("domain", "business")
                    put("entity_type", "BAKI")
                    put("entity_id", "baki_${c.id}")
                    put("data", cJson)
                    put("updated_at", nowIso)
                })
            }

            // 4. Fordi (FORDI)
            backupData.fordiItems.groupBy { SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(it.createdAt)) }.forEach { (fDate, items) ->
                val fJson = JSONObject().apply {
                    put("id", "fordi_$fDate")
                    put("date", fDate)
                    put("updatedAt", items.firstOrNull()?.createdAt ?: System.currentTimeMillis())
                    put("postedToAccounting", items.all { it.isPurchased })

                    val itemsArr = JSONArray()
                    items.forEach { f ->
                        val fo = JSONObject()
                        fo.put("id", f.id)
                        fo.put("productName", f.productName)
                        fo.put("plannedQuantity", f.plannedQuantity)
                        fo.put("unit", f.unit)
                        fo.put("purchaseRate", f.purchaseRate)
                        fo.put("sellingRate", f.sellingRate)
                        fo.put("isChecked", f.isPurchased)
                        fo.put("isPurchased", f.isPurchased)
                        fo.put("actualQuantity", f.actualQuantity)
                        fo.put("actualPurchaseRate", f.actualRate)
                        fo.put("actualTotal", f.actualTotal)
                        itemsArr.put(fo)
                    }
                    put("items", itemsArr)
                }
                recordsArray.put(JSONObject().apply {
                    put("user_id", userId)
                    put("domain", "business")
                    put("entity_type", "FORDI")
                    put("entity_id", "fordi_$fDate")
                    put("data", fJson)
                    put("updated_at", nowIso)
                })
            }

            // 5. Daily Cash (DAILY_CASH)
            backupData.dailyCashRecords.forEach { dc ->
                val dcJson = JSONObject().apply {
                    put("dateKey", dc.dateKey)
                    put("dateMillis", dc.dateMillis)
                    put("sabekCash", dc.sabekCash)
                    put("closingCash", dc.closingCash)
                    put("isClosed", dc.isClosed)
                    put("updatedAt", dc.updatedAt)
                }
                recordsArray.put(JSONObject().apply {
                    put("user_id", userId)
                    put("domain", "business")
                    put("entity_type", "DAILY_CASH")
                    put("entity_id", "daily_cash_${dc.dateKey}")
                    put("data", dcJson)
                    put("updated_at", nowIso)
                })
            }

            // 6. Transactions (EXPENSE, SALE, BAKI_TX)
            backupData.transactions.forEach { t ->
                val tJson = JSONObject().apply {
                    put("id", t.id.toString())
                    put("type", t.type.name)
                    put("amount", t.amount)
                    put("timestamp", t.timestamp)
                    put("customerId", t.customerId)
                    put("customerName", t.customerName)
                    put("productName", t.productName)
                    put("note", t.note)
                    put("category", t.category)
                    put("date", SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(t.timestamp)))
                    put("time", SimpleDateFormat("hh:mm a", Locale.US).format(Date(t.timestamp)))
                    put("updatedAt", t.timestamp)
                }
                val entityType = when (t.type) {
                    com.example.mawa.data.local.entity.TransactionType.SALE_BAKI,
                    com.example.mawa.data.local.entity.TransactionType.BAKI_COLLECTION -> "BAKI_TX"
                    com.example.mawa.data.local.entity.TransactionType.EXPENSE_SHOP,
                    com.example.mawa.data.local.entity.TransactionType.EXPENSE_HOME,
                    com.example.mawa.data.local.entity.TransactionType.PURCHASE_DIRECT,
                    com.example.mawa.data.local.entity.TransactionType.PURCHASE_FORDI -> "EXPENSE"
                    else -> "SALE"
                }
                recordsArray.put(JSONObject().apply {
                    put("user_id", userId)
                    put("domain", "business")
                    put("entity_type", entityType)
                    put("entity_id", "tx_${t.id}")
                    put("data", tJson)
                    put("updated_at", nowIso)
                })
            }

            // 7. Personal transactions (PERSONAL_TX)
            backupData.personalTransactions.forEach { pt ->
                val ptJson = JSONObject().apply {
                    put("id", pt.id)
                    put("type", pt.type.name)
                    put("amount", pt.amount)
                    put("title", pt.title)
                    put("category", pt.category)
                    put("note", pt.note)
                    put("timestamp", pt.timestamp)
                    put("updatedAt", pt.timestamp)
                }
                recordsArray.put(JSONObject().apply {
                    put("user_id", userId)
                    put("domain", "personal")
                    put("entity_type", "PERSONAL_TX")
                    put("entity_id", "ptx_${pt.id}")
                    put("data", ptJson)
                    put("updated_at", nowIso)
                })
            }

            if (recordsArray.length() == 0) {
                return@withContext CloudOperationResult.Success(0, "সিঙ্ক করার মতো কোনো রেকর্ড পাওয়া যায়নি")
            }

            val primaryUrl = "${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_CLOUD_RECORDS}?on_conflict=user_id,domain,entity_type,entity_id"
            val requestBuilder = Request.Builder()
                .url(primaryUrl)
                .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
                .post(recordsArray.toString().toRequestBody(jsonMediaType))

            getAuthHeaders().forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            var isSuccess = false
            var respBody = ""
            var respCode = 0

            client.newCall(requestBuilder.build()).execute().use { response ->
                respBody = response.body?.string() ?: ""
                respCode = response.code
                isSuccess = response.isSuccessful
                Log.d("SupabaseDb", "syncAllRecords code: ${response.code}")
            }

            // Fallback for tables whose unique constraint is on (user_id, entity_type, entity_id)
            if (!isSuccess && respBody.contains("42P10")) {
                val fallbackUrl = "${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_CLOUD_RECORDS}?on_conflict=user_id,entity_type,entity_id"
                val fbReq = Request.Builder()
                    .url(fallbackUrl)
                    .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
                    .post(recordsArray.toString().toRequestBody(jsonMediaType))
                getAuthHeaders().forEach { (k, v) -> fbReq.addHeader(k, v) }
                try {
                    client.newCall(fbReq.build()).execute().use { fbResp ->
                        respBody = fbResp.body?.string() ?: ""
                        respCode = fbResp.code
                        isSuccess = fbResp.isSuccessful
                    }
                } catch (_: Exception) {}
            }

            if (isSuccess) {
                return@withContext CloudOperationResult.Success(
                    data = recordsArray.length(),
                    message = "${recordsArray.length()}টি রেকর্ড সফলভাবে সুপাবেজ ক্লাউডে সিঙ্ক হয়েছে!"
                )
            } else {
                return@withContext CloudOperationResult.Error(
                    message = "ক্লাউড সিঙ্ক ব্যর্থ: ${parseError(respBody, respCode)}"
                )
            }
        } catch (e: Exception) {
            Log.e("SupabaseDb", "syncAllRecords error", e)
            return@withContext CloudOperationResult.Error(
                message = "সিঙ্ক এরর: ${e.localizedMessage ?: "নেটওয়ার্ক সমস্যা"}"
            )
        }
    }

    /**
     * Fetch all incremental records from `mawa_cloud_records` table and assemble FullBackupData
     */
    suspend fun fetchAllIncrementalRecords(): CloudOperationResult<FullBackupData> = withContext(Dispatchers.IO) {
        val userId = authManager.getUserId()
        if (userId.isNullOrBlank()) {
            return@withContext CloudOperationResult.Error("লগইন করা নেই")
        }

        try {
            val url = "${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_CLOUD_RECORDS}?user_id=eq.$userId&order=updated_at.asc,id.asc"
            val requestBuilder = Request.Builder()
                .url(url)
                .get()

            getAuthHeaders().forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            client.newCall(requestBuilder.build()).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                Log.d("SupabaseDb", "fetchIncrementalRecords code: ${response.code}")

                if (response.isSuccessful) {
                    val arr = JSONArray(responseBody)
                    val customers = mutableListOf<com.example.mawa.data.local.entity.CustomerEntity>()
                    val transactions = mutableListOf<com.example.mawa.data.local.entity.TransactionEntity>()
                    val fordiItems = mutableListOf<com.example.mawa.data.local.entity.FordiItemEntity>()
                    val products = mutableListOf<com.example.mawa.data.local.entity.ProductEntity>()
                    val dailyCashList = mutableListOf<com.example.mawa.data.local.entity.DailyCashEntity>()
                    var shopSettings: com.example.mawa.data.local.entity.ShopSettingsEntity? = null
                    val personalTx = mutableListOf<com.example.mawa.data.local.entity.PersonalTransactionEntity>()

                    // Check if discrete transactions exist to avoid duplicate injection from BAKI models
                    val hasDiscreteTransactions = (0 until arr.length()).any {
                        val t = arr.getJSONObject(it).optString("entity_type", "").uppercase(Locale.US)
                        t in setOf("BAKI_TX", "EXPENSE", "EXPENSE_SHOP", "EXPENSE_HOME", "PURCHASE", "SALE")
                    }

                    // Group and intelligently reconcile records with the same entity_id (e.g. from 'business' and 'BUSINESS' domains)
                    // Comparing timestamps and contents to ensure neither sabekCash, closingCash, nor transaction info is lost!
                    val unifiedRecordMap = mutableMapOf<String, JSONObject>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val eId = obj.optString("entity_id", "").trim()
                        val eType = obj.optString("entity_type", "").uppercase(Locale.US)
                        val key = if (eId.isNotBlank()) "${eType}_$eId" else "raw_$i"

                        if (unifiedRecordMap.containsKey(key)) {
                            val existing = unifiedRecordMap[key]!!
                            unifiedRecordMap[key] = mergeDuplicateCloudEntities(existing, obj, eType)
                        } else {
                            unifiedRecordMap[key] = obj
                        }
                    }

                    for (obj in unifiedRecordMap.values) {
                        val entityType = obj.optString("entity_type", "").uppercase(Locale.US)
                        val dataObj = if (obj.has("data")) {
                            val d = obj.get("data")
                            if (d is JSONObject) d else JSONObject(d.toString())
                        } else JSONObject()

                        when (entityType) {
                            "DAILY_CASH" -> {
                                val dKey = dataObj.optString("dateKey", "")
                                if (dKey.isNotBlank()) {
                                    dailyCashList.add(com.example.mawa.data.local.entity.DailyCashEntity(
                                        dateKey = dKey,
                                        dateMillis = dataObj.optLong("dateMillis", System.currentTimeMillis()),
                                        sabekCash = dataObj.optDouble("sabekCash", 0.0),
                                        closingCash = dataObj.optDouble("closingCash", 0.0),
                                        isClosed = dataObj.optBoolean("isClosed", false),
                                        updatedAt = dataObj.optLong("updatedAt", System.currentTimeMillis())
                                    ))
                                }
                            }
                            "BAKI", "BAKI_CUSTOMER", "CUSTOMER" -> {
                                val custName = dataObj.optString("customerName", dataObj.optString("name", "গ্রাহক")).trim()
                                val phone = dataObj.optString("phone", dataObj.optString("mobile", "")).trim()
                                val address = dataObj.optString("details", dataObj.optString("address", "")).trim()
                                val openBal = dataObj.optDouble("openingBalance", dataObj.optDouble("amount", 0.0))
                                val createdAt = dataObj.optLong("createdAt", dataObj.optLong("updatedAt", System.currentTimeMillis()))
                                
                                val innerTxArr = dataObj.optJSONArray("transactions")
                                var hasSpecificTx = false
                                // Only parse inner transactions if no discrete transaction records exist in the cloud
                                if (!hasDiscreteTransactions && innerTxArr != null && innerTxArr.length() > 0) {
                                    for (j in 0 until innerTxArr.length()) {
                                        val txObj = innerTxArr.optJSONObject(j) ?: continue
                                        val txAmt = txObj.optDouble("amount", 0.0)
                                        if (txAmt <= 0) continue
                                        hasSpecificTx = true
                                        val typeStr = txObj.optString("type", "SALE_BAKI")
                                        val txType = if (typeStr.contains("JOMA") || typeStr.contains("COLLECTION") || typeStr.contains("PAYMENT") || typeStr.contains("RECEIVED")) {
                                            com.example.mawa.data.local.entity.TransactionType.BAKI_COLLECTION
                                        } else {
                                            com.example.mawa.data.local.entity.TransactionType.SALE_BAKI
                                        }
                                        val note = txObj.optString("note", if (txType == com.example.mawa.data.local.entity.TransactionType.SALE_BAKI) "বাকি বিক্রি" else "বাকি আদায়")
                                        val ts = txObj.optLong("updatedAt", txObj.optLong("timestamp", System.currentTimeMillis()))
                                        transactions.add(
                                            com.example.mawa.data.local.entity.TransactionEntity(
                                                type = txType,
                                                amount = txAmt,
                                                customerName = custName,
                                                note = note,
                                                category = "বাকি",
                                                timestamp = ts
                                            )
                                        )
                                    }
                                }

                                customers.add(com.example.mawa.data.local.entity.CustomerEntity(
                                    name = custName,
                                    phone = phone,
                                    address = address,
                                    openingBalance = if (!hasSpecificTx) openBal else 0.0,
                                    note = dataObj.optString("note", ""),
                                    createdAt = createdAt
                                ))
                            }
                            "BAKI_TX", "EXPENSE", "EXPENSE_SHOP", "EXPENSE_HOME", "PURCHASE", "SALE" -> {
                                val typeStr = dataObj.optString("type", entityType)
                                val txType = try {
                                    com.example.mawa.data.local.entity.TransactionType.valueOf(typeStr)
                                } catch (e: Exception) {
                                    when (entityType) {
                                        "BAKI_TX" -> com.example.mawa.data.local.entity.TransactionType.SALE_BAKI
                                        "EXPENSE_HOME" -> com.example.mawa.data.local.entity.TransactionType.EXPENSE_HOME
                                        "PURCHASE" -> com.example.mawa.data.local.entity.TransactionType.PURCHASE_DIRECT
                                        "EXPENSE", "EXPENSE_SHOP" -> com.example.mawa.data.local.entity.TransactionType.EXPENSE_SHOP
                                        else -> com.example.mawa.data.local.entity.TransactionType.SALE_CASH
                                    }
                                }
                                transactions.add(com.example.mawa.data.local.entity.TransactionEntity(
                                    type = txType,
                                    amount = dataObj.optDouble("amount", dataObj.optDouble("taka", 0.0)),
                                    timestamp = dataObj.optLong("timestamp", dataObj.optLong("updatedAt", System.currentTimeMillis())),
                                    customerId = if (dataObj.has("customerId") && !dataObj.isNull("customerId")) dataObj.optLong("customerId") else null,
                                    customerName = dataObj.optString("customerName", null),
                                    productName = dataObj.optString("productName", null),
                                    note = dataObj.optString("note", dataObj.optString("name", "")),
                                    category = dataObj.optString("category", "")
                                ))
                            }
                            "FORDI" -> {
                                val itemsArr = dataObj.optJSONArray("items")
                                if (itemsArr != null && itemsArr.length() > 0) {
                                    for (j in 0 until itemsArr.length()) {
                                        val fi = itemsArr.optJSONObject(j) ?: continue
                                        val pName = fi.optString("productName", fi.optString("name", "পণ্য")).trim()
                                        if (pName.isBlank()) continue
                                        fordiItems.add(com.example.mawa.data.local.entity.FordiItemEntity(
                                            productName = pName,
                                            plannedQuantity = fi.optDouble("plannedQuantity", fi.optDouble("quantity", 1.0)),
                                            unit = fi.optString("unit", "কেজি"),
                                            purchaseRate = fi.optDouble("purchaseRate", 0.0),
                                            sellingRate = fi.optDouble("sellingRate", 0.0),
                                            isPurchased = fi.optBoolean("isChecked", fi.optBoolean("isPurchased", false)),
                                            actualQuantity = fi.optDouble("actualQuantity", 0.0),
                                            actualRate = fi.optDouble("actualPurchaseRate", 0.0),
                                            actualTotal = fi.optDouble("actualTotal", 0.0),
                                            createdAt = dataObj.optLong("updatedAt", System.currentTimeMillis())
                                        ))
                                    }
                                } else {
                                    fordiItems.add(com.example.mawa.data.local.entity.FordiItemEntity(
                                        productName = dataObj.optString("productName", ""),
                                        plannedQuantity = dataObj.optDouble("plannedQuantity", 1.0),
                                        unit = dataObj.optString("unit", "কেজি"),
                                        purchaseRate = dataObj.optDouble("purchaseRate", 0.0),
                                        isPurchased = dataObj.optBoolean("isPurchased", false),
                                        actualTotal = dataObj.optDouble("actualTotal", 0.0)
                                    ))
                                }
                            }
                            "PRODUCT" -> {
                                val pPrice = dataObj.optDouble("lastPurchasePrice", dataObj.optDouble("defaultPurchasePrice", 0.0))
                                val sPrice = dataObj.optDouble("sellingPrice", dataObj.optDouble("defaultSellingPrice", 0.0))
                                products.add(com.example.mawa.data.local.entity.ProductEntity(
                                    name = dataObj.optString("name", ""),
                                    banglaName = dataObj.optString("banglaName", dataObj.optString("name", "")),
                                    category = dataObj.optString("category", "মুদি"),
                                    stockQuantity = dataObj.optDouble("stockQuantity", 0.0),
                                    unit = dataObj.optString("unit", "কেজি"),
                                    defaultPurchasePrice = pPrice,
                                    defaultSellingPrice = sPrice,
                                    createdAt = dataObj.optLong("createdAt", dataObj.optLong("updatedAt", System.currentTimeMillis()))
                                ))
                            }
                            "SETTINGS", "SHOP_SETTINGS" -> {
                                shopSettings = com.example.mawa.data.local.entity.ShopSettingsEntity(
                                    shopName = dataObj.optString("shopName", "আমার দোকান"),
                                    ownerName = dataObj.optString("ownerName", ""),
                                    phone = dataObj.optString("phone", ""),
                                    bkashNumber = dataObj.optString("bkashNumber", ""),
                                    nagadNumber = dataObj.optString("nagadNumber", ""),
                                    bankDetails = dataObj.optString("bankDetails", ""),
                                    qrCodeData = dataObj.optString("qrCodeData", ""),
                                    openingBalance = dataObj.optDouble("openingBalance", 0.0),
                                    appMode = dataObj.optString("appMode", "PRO")
                                )
                            }
                            "PERSONAL_TX", "PERSONAL" -> {
                                val pTypeStr = dataObj.optString("type", "EXPENSE")
                                val pType = try {
                                    com.example.mawa.data.local.entity.PersonalTransactionType.valueOf(pTypeStr)
                                } catch (e: Exception) {
                                    com.example.mawa.data.local.entity.PersonalTransactionType.EXPENSE
                                }
                                personalTx.add(com.example.mawa.data.local.entity.PersonalTransactionEntity(
                                    type = pType,
                                    amount = dataObj.optDouble("amount", 0.0),
                                    title = dataObj.optString("title", ""),
                                    category = dataObj.optString("category", ""),
                                    timestamp = dataObj.optLong("timestamp", dataObj.optLong("updatedAt", System.currentTimeMillis()))
                                ))
                            }
                        }
                    }

                    val backupData = FullBackupData(
                        shopSettings = shopSettings,
                        customers = customers.distinctBy { it.name.trim().lowercase() },
                        transactions = transactions.distinctBy { "${it.timestamp}_${it.amount}_${it.type}_${it.customerName}_${it.note}" },
                        fordiItems = fordiItems,
                        products = products.distinctBy { it.name.trim().lowercase() },
                        personalTransactions = personalTx,
                        dailyCashRecords = dailyCashList
                    )
                    return@withContext CloudOperationResult.Success(backupData)
                } else {
                    return@withContext CloudOperationResult.Error(
                        message = "রেকর্ডস আনা যায়নি: ${parseError(responseBody, response.code)}"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseDb", "fetchIncrementalRecords error", e)
            return@withContext CloudOperationResult.Error(
                message = "এরর: ${e.localizedMessage ?: "নেটওয়ার্ক সমস্যা"}"
            )
        }
    }

    /**
     * Compares two records with the same entity_id (e.g. from 'business' and 'BUSINESS' domains)
     * and intelligently merges them so no financial, customer, cash, or transactional data is lost.
     */
    private fun mergeDuplicateCloudEntities(
        existingObj: JSONObject,
        incomingObj: JSONObject,
        entityType: String
    ): JSONObject {
        val existingData = if (existingObj.has("data")) {
            val d = existingObj.get("data")
            if (d is JSONObject) d else JSONObject(d.toString())
        } else JSONObject()

        val incomingData = if (incomingObj.has("data")) {
            val d = incomingObj.get("data")
            if (d is JSONObject) d else JSONObject(d.toString())
        } else JSONObject()

        val existingUpdated = existingObj.optString("updated_at", "")
        val incomingUpdated = incomingObj.optString("updated_at", "")
        val isIncomingNewer = incomingUpdated > existingUpdated

        val mergedData = JSONObject()

        val baseData = if (isIncomingNewer) incomingData else existingData
        val secondaryData = if (isIncomingNewer) existingData else incomingData

        val secKeys = secondaryData.keys()
        while (secKeys.hasNext()) {
            val k = secKeys.next()
            mergedData.put(k, secondaryData.get(k))
        }
        val baseKeys = baseData.keys()
        while (baseKeys.hasNext()) {
            val k = baseKeys.next()
            mergedData.put(k, baseData.get(k))
        }

        // Deep field-level preservation
        when (entityType) {
            "DAILY_CASH" -> {
                val sabek1 = existingData.optDouble("sabekCash", 0.0)
                val sabek2 = incomingData.optDouble("sabekCash", 0.0)
                val closing1 = existingData.optDouble("closingCash", 0.0)
                val closing2 = incomingData.optDouble("closingCash", 0.0)
                val isClosed = existingData.optBoolean("isClosed", false) || incomingData.optBoolean("isClosed", false)

                mergedData.put("sabekCash", if (sabek1 > 0 && sabek2 <= 0) sabek1 else if (sabek2 > 0 && sabek1 <= 0) sabek2 else maxOf(sabek1, sabek2))
                mergedData.put("closingCash", if (closing1 > 0 && closing2 <= 0) closing1 else if (closing2 > 0 && closing1 <= 0) closing2 else maxOf(closing1, closing2))
                mergedData.put("isClosed", isClosed)
                val maxMillis = maxOf(existingData.optLong("dateMillis", 0L), incomingData.optLong("dateMillis", 0L))
                if (maxMillis > 0) mergedData.put("dateMillis", maxMillis)
            }
            "BAKI", "BAKI_CUSTOMER", "CUSTOMER" -> {
                val phone1 = existingData.optString("phone", "").trim()
                val phone2 = incomingData.optString("phone", "").trim()
                val addr1 = existingData.optString("address", existingData.optString("details", "")).trim()
                val addr2 = incomingData.optString("address", incomingData.optString("details", "")).trim()
                val bal1 = existingData.optDouble("openingBalance", existingData.optDouble("amount", 0.0))
                val bal2 = incomingData.optDouble("openingBalance", incomingData.optDouble("amount", 0.0))

                if (phone1.isNotBlank() && phone2.isBlank()) mergedData.put("phone", phone1)
                if (addr1.isNotBlank() && addr2.isBlank()) mergedData.put("address", addr1)
                if (bal1 > 0 && bal2 <= 0) mergedData.put("openingBalance", bal1)
                else if (bal2 > 0 && bal1 <= 0) mergedData.put("openingBalance", bal2)

                val tx1 = existingData.optJSONArray("transactions")
                val tx2 = incomingData.optJSONArray("transactions")
                if (tx1 != null && tx1.length() > 0 && (tx2 == null || tx2.length() == 0)) {
                    mergedData.put("transactions", tx1)
                } else if (tx2 != null && tx2.length() > 0 && (tx1 == null || tx1.length() == 0)) {
                    mergedData.put("transactions", tx2)
                }
            }
            "SETTINGS" -> {
                val ob1 = existingData.optDouble("openingBalance", 0.0)
                val ob2 = incomingData.optDouble("openingBalance", 0.0)
                mergedData.put("openingBalance", if (ob1 > 0 && ob2 <= 0) ob1 else if (ob2 > 0 && ob1 <= 0) ob2 else maxOf(ob1, ob2))
            }
            "PRODUCT" -> {
                val p1 = existingData.optDouble("defaultSellingPrice", existingData.optDouble("sellingPrice", 0.0))
                val p2 = incomingData.optDouble("defaultSellingPrice", incomingData.optDouble("sellingPrice", 0.0))
                if (p1 > 0 && p2 <= 0) mergedData.put("defaultSellingPrice", p1)
            }
        }

        return JSONObject().apply {
            put("entity_id", existingObj.optString("entity_id", incomingObj.optString("entity_id", "")))
            put("entity_type", entityType)
            put("domain", "business")
            put("data", mergedData)
            put("updated_at", if (isIncomingNewer) incomingUpdated else existingUpdated)
        }
    }

    private fun parseBackupItem(obj: JSONObject): CloudBackupItem {
        val id = obj.optLong("id", 0L)
        val uId = obj.optString("user_id", "")
        val name = obj.optString("backup_name", "অজানা ব্যাকআপ")
        val dataStr = when {
            obj.has("backup_data") && !obj.isNull("backup_data") -> obj.get("backup_data").toString()
            obj.has("data") && !obj.isNull("data") -> obj.get("data").toString()
            else -> ""
        }
        val updatedAt = obj.optString("updated_at", "")
        return CloudBackupItem(
            id = id,
            userId = uId,
            backupName = name,
            dataJson = dataStr,
            updatedAt = updatedAt
        )
    }

    /**
     * Upsert a single discrete record into Supabase `mawa_cloud_records` table instantly on mutation.
     */
    suspend fun upsertSingleRecord(
        domain: String,
        entityType: String,
        entityId: String,
        data: JSONObject
    ): CloudOperationResult<Boolean> = withContext(Dispatchers.IO) {
        val userId = authManager.getUserId()
        if (userId.isNullOrBlank()) {
            return@withContext CloudOperationResult.Error("ব্যবহারকারী লগইন নেই")
        }

        try {
            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
            val record = JSONObject().apply {
                put("user_id", userId)
                put("domain", domain)
                put("entity_type", entityType)
                put("entity_id", entityId)
                put("data", data)
                put("updated_at", nowIso)
            }

            val url = "${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_CLOUD_RECORDS}?on_conflict=user_id,domain,entity_type,entity_id"
            val request = Request.Builder()
                .url(url)
                .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
                .post(record.toString().toRequestBody(jsonMediaType))

            getAuthHeaders().forEach { (k, v) -> request.addHeader(k, v) }

            var isSuccess = false
            var errRespBody = ""
            var errRespCode = 0

            client.newCall(request.build()).execute().use { response ->
                if (response.isSuccessful || response.code in listOf(200, 201, 204)) {
                    isSuccess = true
                } else {
                    errRespBody = response.body?.string() ?: ""
                    errRespCode = response.code
                }
            }

            // Fallback 1: Unique index on (user_id, entity_type, entity_id) without domain
            if (!isSuccess && errRespBody.contains("42P10")) {
                val fallbackUrl1 = "${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_CLOUD_RECORDS}?on_conflict=user_id,entity_type,entity_id"
                val fbReq1 = Request.Builder()
                    .url(fallbackUrl1)
                    .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
                    .post(record.toString().toRequestBody(jsonMediaType))
                getAuthHeaders().forEach { (k, v) -> fbReq1.addHeader(k, v) }

                try {
                    client.newCall(fbReq1.build()).execute().use { fb1 ->
                        if (fb1.isSuccessful || fb1.code in listOf(200, 201, 204)) {
                            isSuccess = true
                        } else {
                            errRespBody = fb1.body?.string() ?: ""
                            errRespCode = fb1.code
                        }
                    }
                } catch (_: Exception) {}
            }

            // Fallback 2: Unique index on (user_id, entity_id)
            if (!isSuccess && errRespBody.contains("42P10")) {
                val fallbackUrl2 = "${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_CLOUD_RECORDS}?on_conflict=user_id,entity_id"
                val fbReq2 = Request.Builder()
                    .url(fallbackUrl2)
                    .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
                    .post(record.toString().toRequestBody(jsonMediaType))
                getAuthHeaders().forEach { (k, v) -> fbReq2.addHeader(k, v) }

                try {
                    client.newCall(fbReq2.build()).execute().use { fb2 ->
                        if (fb2.isSuccessful || fb2.code in listOf(200, 201, 204)) {
                            isSuccess = true
                        } else {
                            errRespBody = fb2.body?.string() ?: ""
                            errRespCode = fb2.code
                        }
                    }
                } catch (_: Exception) {}
            }

            // Fallback 3: Generic upsert
            if (!isSuccess) {
                val fallbackUrl3 = "${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_CLOUD_RECORDS}"
                val fbReq3 = Request.Builder()
                    .url(fallbackUrl3)
                    .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
                    .post(record.toString().toRequestBody(jsonMediaType))
                getAuthHeaders().forEach { (k, v) -> fbReq3.addHeader(k, v) }

                try {
                    client.newCall(fbReq3.build()).execute().use { fb3 ->
                        if (fb3.isSuccessful || fb3.code in listOf(200, 201, 204)) {
                            isSuccess = true
                        } else {
                            errRespBody = fb3.body?.string() ?: ""
                            errRespCode = fb3.code
                        }
                    }
                } catch (_: Exception) {}
            }

            if (isSuccess) {
                CloudOperationResult.Success(true, "সার্ভারে লাইভ সংরক্ষিত হয়েছে")
            } else {
                CloudOperationResult.Error("আপলোড ব্যর্থ: ${parseError(errRespBody, errRespCode)}")
            }
        } catch (e: Exception) {
            Log.e("SupabaseDb", "upsertSingleRecord error", e)
            CloudOperationResult.Error("ত্রুটি: ${e.localizedMessage}")
        }
    }

    /**
     * Delete a single record from Supabase `mawa_cloud_records` immediately when deleted locally.
     */
    suspend fun deleteCloudRecord(
        entityId: String,
        entityType: String? = null
    ): CloudOperationResult<Boolean> = withContext(Dispatchers.IO) {
        val userId = authManager.getUserId()
        if (userId.isNullOrBlank()) {
            return@withContext CloudOperationResult.Error("লগইন নেই")
        }

        try {
            var url = "${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_CLOUD_RECORDS}?user_id=eq.$userId&entity_id=eq.$entityId"
            if (!entityType.isNullOrBlank()) {
                url += "&entity_type=eq.$entityType"
            }

            val request = Request.Builder()
                .url(url)
                .delete()

            getAuthHeaders().forEach { (k, v) -> request.addHeader(k, v) }

            client.newCall(request.build()).execute().use { response ->
                if (response.isSuccessful || response.code in listOf(200, 204)) {
                    Log.d("SupabaseDb", "deleteCloudRecord success for entityId: $entityId")
                    CloudOperationResult.Success(true, "সার্ভার থেকে সফলভাবে মুছে ফেলা হয়েছে")
                } else {
                    val body = response.body?.string() ?: ""
                    CloudOperationResult.Error("সার্ভার থেকে মুছতে ব্যর্থ: ${parseError(body, response.code)}")
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseDb", "deleteCloudRecord error", e)
            CloudOperationResult.Error("মুছতে সমস্যা: ${e.localizedMessage}")
        }
    }

    /**
     * Delete all cloud records for current user (used when clearing sample data or resetting).
     */
    suspend fun deleteCloudRecordsForUser(): CloudOperationResult<Boolean> = withContext(Dispatchers.IO) {
        val userId = authManager.getUserId()
        if (userId.isNullOrBlank()) return@withContext CloudOperationResult.Error("লগইন নেই")

        try {
            val url = "${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_CLOUD_RECORDS}?user_id=eq.$userId"
            val request = Request.Builder().url(url).delete()
            getAuthHeaders().forEach { (k, v) -> request.addHeader(k, v) }

            client.newCall(request.build()).execute().use { response ->
                if (response.isSuccessful || response.code in listOf(200, 204)) {
                    CloudOperationResult.Success(true, "ক্লাউডের সকল রেকর্ড মুছে দেওয়া হয়েছে")
                } else {
                    CloudOperationResult.Error("মুছতে ব্যর্থ: ${response.code}")
                }
            }
        } catch (e: Exception) {
            CloudOperationResult.Error("ত্রুটি: ${e.localizedMessage}")
        }
    }

    /**
     * Prunes orphaned cloud records in `mawa_cloud_records` whose entity IDs are no longer in the local database.
     */
    suspend fun deleteOrphanedCloudRecords(validEntityIds: Set<String>): CloudOperationResult<Int> = withContext(Dispatchers.IO) {
        val userId = authManager.getUserId()
        if (userId.isNullOrBlank()) return@withContext CloudOperationResult.Success(0)

        try {
            val url = "${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_CLOUD_RECORDS}?user_id=eq.$userId&select=entity_id"
            val request = Request.Builder().url(url).get()
            getAuthHeaders().forEach { (k, v) -> request.addHeader(k, v) }

            val remoteIds = mutableListOf<String>()
            client.newCall(request.build()).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val arr = JSONArray(body)
                    for (i in 0 until arr.length()) {
                        val eId = arr.getJSONObject(i).optString("entity_id", "")
                        if (eId.isNotBlank()) remoteIds.add(eId)
                    }
                }
            }

            val orphanedIds = remoteIds.filter { it !in validEntityIds }
            var deletedCount = 0
            for (orphanId in orphanedIds) {
                deleteCloudRecord(orphanId)
                deletedCount++
            }
            CloudOperationResult.Success(deletedCount, "$deletedCount টি অপসারিত রেকর্ড সার্ভার থেকে মুছে দেওয়া হয়েছে")
        } catch (e: Exception) {
            Log.w("SupabaseDb", "deleteOrphanedCloudRecords warning: ${e.message}")
            CloudOperationResult.Success(0)
        }
    }

    private fun parseError(body: String, code: Int): String {
        return try {
            val obj = JSONObject(body)
            obj.optString("message", obj.optString("msg", obj.optString("hint", "কোড $code")))
        } catch (e: Exception) {
            "স্ট্যাটাস $code"
        }
    }
}
