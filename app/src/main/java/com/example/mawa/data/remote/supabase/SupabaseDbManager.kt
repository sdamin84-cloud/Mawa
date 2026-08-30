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

            val record = JSONObject().apply {
                put("user_id", userId)
                put("backup_name", backupName.ifBlank { "MAWA_BACKUP_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}" })
                put("data", jsonObjectData)
            }

            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_USER_BACKUPS}")
                .addHeader("Prefer", "return=representation")
                .post(record.toString().toRequestBody(jsonMediaType))

            getAuthHeaders().forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            client.newCall(requestBuilder.build()).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                Log.d("SupabaseDb", "uploadBackup response: ${response.code} body: $responseBody")

                if (response.isSuccessful) {
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
                        backupName = backupName,
                        dataJson = jsonString,
                        updatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
                    )

                    return@withContext CloudOperationResult.Success(
                        data = item,
                        message = "সুপাবেজ ক্লাউডে ব্যাকআপ সফলভাবে সংরক্ষিত হয়েছে!"
                    )
                } else {
                    return@withContext CloudOperationResult.Error(
                        message = "ক্লাউড ব্যাকআপ আপলোড ব্যর্থ: ${parseError(responseBody, response.code)}"
                    )
                }
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
            val url = "${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_USER_BACKUPS}?user_id=eq.$userId&order=id.desc"
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

            // Shop Settings
            backupData.shopSettings?.let { s ->
                val sJson = JSONObject().apply {
                    put("shopName", s.shopName)
                    put("ownerName", s.ownerName)
                    put("phone", s.phone)
                    put("openingBalance", s.openingBalance)
                    put("appMode", s.appMode)
                }
                recordsArray.put(JSONObject().apply {
                    put("user_id", userId)
                    put("domain", "BUSINESS")
                    put("entity_type", "SETTINGS")
                    put("entity_id", "shop_settings_${s.id}")
                    put("data", sJson)
                })
            }

            // Products
            backupData.products.forEach { p ->
                val pJson = JSONObject().apply {
                    put("name", p.name)
                    put("banglaName", p.banglaName)
                    put("category", p.category)
                    put("stockQuantity", p.stockQuantity)
                    put("unit", p.unit)
                    put("defaultPurchasePrice", p.defaultPurchasePrice)
                    put("defaultSellingPrice", p.defaultSellingPrice)
                }
                recordsArray.put(JSONObject().apply {
                    put("user_id", userId)
                    put("domain", "BUSINESS")
                    put("entity_type", "PRODUCT")
                    put("entity_id", "prod_${p.id}")
                    put("data", pJson)
                })
            }

            // Customers (BAKI_CUSTOMER)
            backupData.customers.forEach { c ->
                val cJson = JSONObject().apply {
                    put("name", c.name)
                    put("phone", c.phone)
                    put("address", c.address)
                    put("openingBalance", c.openingBalance)
                    put("note", c.note)
                    put("createdAt", c.createdAt)
                }
                recordsArray.put(JSONObject().apply {
                    put("user_id", userId)
                    put("domain", "BUSINESS")
                    put("entity_type", "BAKI_CUSTOMER")
                    put("entity_id", "customer_${c.id}")
                    put("data", cJson)
                })
            }

            // Transactions (BAKI_TX, EXPENSE, SALE)
            backupData.transactions.forEach { t ->
                val tJson = JSONObject().apply {
                    put("type", t.type.name)
                    put("amount", t.amount)
                    put("timestamp", t.timestamp)
                    put("customerId", t.customerId)
                    put("customerName", t.customerName)
                    put("note", t.note)
                    put("category", t.category)
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
                    put("domain", "BUSINESS")
                    put("entity_type", entityType)
                    put("entity_id", "tx_${t.id}")
                    put("data", tJson)
                })
            }

            // Fordi items (FORDI)
            backupData.fordiItems.forEach { f ->
                val fJson = JSONObject().apply {
                    put("productName", f.productName)
                    put("plannedQuantity", f.plannedQuantity)
                    put("unit", f.unit)
                    put("purchaseRate", f.purchaseRate)
                    put("isPurchased", f.isPurchased)
                    put("actualTotal", f.actualTotal)
                }
                recordsArray.put(JSONObject().apply {
                    put("user_id", userId)
                    put("domain", "BUSINESS")
                    put("entity_type", "FORDI")
                    put("entity_id", "fordi_${f.id}")
                    put("data", fJson)
                })
            }

            // Personal transactions
            backupData.personalTransactions.forEach { pt ->
                val ptJson = JSONObject().apply {
                    put("type", pt.type.name)
                    put("amount", pt.amount)
                    put("title", pt.title)
                    put("category", pt.category)
                    put("timestamp", pt.timestamp)
                }
                recordsArray.put(JSONObject().apply {
                    put("user_id", userId)
                    put("domain", "PERSONAL")
                    put("entity_type", "PERSONAL_TX")
                    put("entity_id", "ptx_${pt.id}")
                    put("data", ptJson)
                })
            }

            if (recordsArray.length() == 0) {
                return@withContext CloudOperationResult.Success(0, "সিঙ্ক করার মতো কোনো রেকর্ড পাওয়া যায়নি")
            }

            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_CLOUD_RECORDS}")
                .addHeader("Prefer", "return=minimal")
                .post(recordsArray.toString().toRequestBody(jsonMediaType))

            getAuthHeaders().forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            client.newCall(requestBuilder.build()).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                Log.d("SupabaseDb", "syncAllRecords code: ${response.code}")

                if (response.isSuccessful) {
                    return@withContext CloudOperationResult.Success(
                        data = recordsArray.length(),
                        message = "${recordsArray.length()}টি রেকর্ড সফলভাবে সুপাবেজ ক্লাউডে সিঙ্ক হয়েছে!"
                    )
                } else {
                    return@withContext CloudOperationResult.Error(
                        message = "ক্লাউড সিঙ্ক ব্যর্থ: ${parseError(responseBody, response.code)}"
                    )
                }
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
            val url = "${SupabaseConfig.SUPABASE_REST_URL}/${SupabaseConfig.TABLE_CLOUD_RECORDS}?user_id=eq.$userId&order=id.asc"
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
                    var shopSettings: com.example.mawa.data.local.entity.ShopSettingsEntity? = null
                    val personalTx = mutableListOf<com.example.mawa.data.local.entity.PersonalTransactionEntity>()

                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val entityType = obj.optString("entity_type", "")
                        val dataObj = if (obj.has("data")) {
                            val d = obj.get("data")
                            if (d is JSONObject) d else JSONObject(d.toString())
                        } else JSONObject()

                        when (entityType) {
                            "BAKI_CUSTOMER" -> {
                                customers.add(com.example.mawa.data.local.entity.CustomerEntity(
                                    name = dataObj.optString("name", "গ্রাহক"),
                                    phone = dataObj.optString("phone", ""),
                                    address = dataObj.optString("address", ""),
                                    openingBalance = dataObj.optDouble("openingBalance", 0.0),
                                    note = dataObj.optString("note", ""),
                                    createdAt = dataObj.optLong("createdAt", System.currentTimeMillis())
                                ))
                            }
                            "BAKI_TX", "EXPENSE", "SALE" -> {
                                val typeStr = dataObj.optString("type", "EXPENSE_SHOP")
                                val txType = try {
                                    com.example.mawa.data.local.entity.TransactionType.valueOf(typeStr)
                                } catch (e: Exception) {
                                    when (entityType) {
                                        "BAKI_TX" -> com.example.mawa.data.local.entity.TransactionType.SALE_BAKI
                                        "EXPENSE" -> com.example.mawa.data.local.entity.TransactionType.EXPENSE_SHOP
                                        else -> com.example.mawa.data.local.entity.TransactionType.SALE_CASH
                                    }
                                }
                                transactions.add(com.example.mawa.data.local.entity.TransactionEntity(
                                    type = txType,
                                    amount = dataObj.optDouble("amount", 0.0),
                                    timestamp = dataObj.optLong("timestamp", System.currentTimeMillis()),
                                    customerId = if (dataObj.has("customerId") && !dataObj.isNull("customerId")) dataObj.optLong("customerId") else null,
                                    customerName = dataObj.optString("customerName", null),
                                    note = dataObj.optString("note", ""),
                                    category = dataObj.optString("category", "")
                                ))
                            }
                            "FORDI" -> {
                                fordiItems.add(com.example.mawa.data.local.entity.FordiItemEntity(
                                    productName = dataObj.optString("productName", ""),
                                    plannedQuantity = dataObj.optDouble("plannedQuantity", 1.0),
                                    unit = dataObj.optString("unit", "কেজি"),
                                    purchaseRate = dataObj.optDouble("purchaseRate", 0.0),
                                    isPurchased = dataObj.optBoolean("isPurchased", false),
                                    actualTotal = dataObj.optDouble("actualTotal", 0.0)
                                ))
                            }
                            "PRODUCT" -> {
                                products.add(com.example.mawa.data.local.entity.ProductEntity(
                                    name = dataObj.optString("name", ""),
                                    banglaName = dataObj.optString("banglaName", ""),
                                    category = dataObj.optString("category", ""),
                                    stockQuantity = dataObj.optDouble("stockQuantity", 0.0),
                                    unit = dataObj.optString("unit", "কেজি"),
                                    defaultPurchasePrice = dataObj.optDouble("defaultPurchasePrice", 0.0),
                                    defaultSellingPrice = dataObj.optDouble("defaultSellingPrice", 0.0)
                                ))
                            }
                            "SETTINGS" -> {
                                shopSettings = com.example.mawa.data.local.entity.ShopSettingsEntity(
                                    shopName = dataObj.optString("shopName", "আমার দোকান"),
                                    ownerName = dataObj.optString("ownerName", ""),
                                    phone = dataObj.optString("phone", ""),
                                    openingBalance = dataObj.optDouble("openingBalance", 0.0),
                                    appMode = dataObj.optString("appMode", "PRO")
                                )
                            }
                            "PERSONAL_TX" -> {
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
                                    timestamp = dataObj.optLong("timestamp", System.currentTimeMillis())
                                ))
                            }
                        }
                    }

                    val backupData = FullBackupData(
                        shopSettings = shopSettings,
                        customers = customers,
                        transactions = transactions,
                        fordiItems = fordiItems,
                        products = products,
                        personalTransactions = personalTx
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

    private fun parseBackupItem(obj: JSONObject): CloudBackupItem {
        val id = obj.optLong("id", 0L)
        val uId = obj.optString("user_id", "")
        val name = obj.optString("backup_name", "অজানা ব্যাকআপ")
        val dataStr = if (obj.has("data")) {
            val d = obj.get("data")
            d.toString()
        } else ""
        val updatedAt = obj.optString("updated_at", "")
        return CloudBackupItem(
            id = id,
            userId = uId,
            backupName = name,
            dataJson = dataStr,
            updatedAt = updatedAt
        )
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
