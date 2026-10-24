package com.example.mawa.data.remote.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.example.mawa.util.DataBackupRestoreManager
import com.example.mawa.util.FullBackupData
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseDbManager(
    private val context: Context,
    private val authManager: FirebaseAuthManager
) {
    private val db: FirebaseFirestore by lazy {
        val dbId = try {
            context.getString(R.string.firestore_database_id)
        } catch (e: Exception) {
            ""
        }
        if (dbId.isNotBlank()) {
            try {
                FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get named database $dbId, falling back to default", e)
                FirebaseFirestore.getInstance()
            }
        } else {
            FirebaseFirestore.getInstance()
        }
    }

    companion object {
        private const val TAG = "FirebaseDbManager"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_BACKUPS = "cloud_backups"
    }

    suspend fun uploadBackupToFirebase(
        backupName: String,
        backupData: FullBackupData
    ): FirebaseOperationResult<FirebaseBackupItem> {
        val uid = authManager.getUserId()
            ?: return FirebaseOperationResult.Error("ব্যবহারকারী লগইন করা নেই। অনুগ্রহ করে প্রথমে সাইন-ইন করুন।")

        return try {
            val jsonString = DataBackupRestoreManager.exportToJsonString(backupData)
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val readableDate = dateFormat.format(Date(now))
            val backupId = "backup_${now}"

            val totalBaki = backupData.customers.sumOf { it.openingBalance }
            val item = FirebaseBackupItem(
                id = backupId,
                userId = uid,
                backupName = backupName.ifBlank { "Mawa Auto Backup" },
                dataJson = jsonString,
                customerCount = backupData.customers.size,
                transactionCount = backupData.transactions.size,
                productCount = backupData.products.size,
                totalBaki = totalBaki,
                timestamp = now,
                formattedDate = readableDate
            )

            val backupMap = hashMapOf(
                "id" to item.id,
                "userId" to item.userId,
                "backupName" to item.backupName,
                "dataJson" to item.dataJson,
                "customerCount" to item.customerCount,
                "transactionCount" to item.transactionCount,
                "productCount" to item.productCount,
                "totalBaki" to item.totalBaki,
                "timestamp" to item.timestamp,
                "formattedDate" to item.formattedDate
            )

            // Save to Firestore subcollection: users/{uid}/cloud_backups/{backupId}
            db.collection(COLLECTION_USERS)
                .document(uid)
                .collection(COLLECTION_BACKUPS)
                .document(backupId)
                .set(backupMap)
                .await()

            // Also sync each entity into its own dedicated Firestore subcollection
            syncAllRecordsToFirebase(backupData)

            // Update parent user document metadata
            val userMeta = hashMapOf(
                "uid" to uid,
                "email" to (authManager.currentUser.value?.email ?: ""),
                "lastBackupName" to item.backupName,
                "lastBackupAt" to now,
                "totalCustomers" to item.customerCount,
                "totalTransactions" to item.transactionCount,
                "totalProducts" to item.productCount
            )
            db.collection(COLLECTION_USERS)
                .document(uid)
                .set(userMeta, SetOptions.merge())
                .await()

            FirebaseOperationResult.Success(
                item,
                "ফায়ারবেসে প্রতিটি কাস্টমার, লেনদেন, পণ্য ও ক্যাশ খাতা যার যার জায়গায় সঠিকভাবে বসেছে এবং ব্যাকআপ সংরক্ষিত হয়েছে!"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Firebase backup upload failed", e)
            FirebaseOperationResult.Error("ফায়ারবেসে ব্যাকআপ আপলোড ব্যর্থ: ${e.localizedMessage ?: "ত্রুটি"}", e)
        }
    }

    suspend fun syncAllRecordsToFirebase(backupData: FullBackupData): FirebaseOperationResult<Int> {
        val uid = authManager.getUserId()
            ?: return FirebaseOperationResult.Error("ব্যবহারকারী লগইন করা নেই")

        return try {
            val userDoc = db.collection(COLLECTION_USERS).document(uid)
            var totalCount = 0

            // 1. Customers batch
            val customerChunks = backupData.customers.chunked(450)
            for (chunk in customerChunks) {
                val batch = db.batch()
                for (c in chunk) {
                    val docRef = userDoc.collection("customers").document(c.id.toString())
                    val map = hashMapOf(
                        "id" to c.id,
                        "name" to c.name,
                        "phone" to c.phone,
                        "address" to c.address,
                        "openingBalance" to c.openingBalance,
                        "creditLimit" to c.creditLimit,
                        "categoryTag" to c.categoryTag,
                        "nidOrGuarantor" to c.nidOrGuarantor,
                        "note" to c.note,
                        "promisedPaymentDate" to c.promisedPaymentDate,
                        "createdAt" to c.createdAt,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    batch.set(docRef, map, SetOptions.merge())
                    totalCount++
                }
                batch.commit().await()
            }

            // 2. Transactions batch
            val txChunks = backupData.transactions.chunked(450)
            for (chunk in txChunks) {
                val batch = db.batch()
                for (tx in chunk) {
                    val docRef = userDoc.collection("transactions").document(tx.id.toString())
                    val map = hashMapOf(
                        "id" to tx.id,
                        "type" to tx.type.name,
                        "amount" to tx.amount,
                        "customerId" to tx.customerId,
                        "customerName" to tx.customerName,
                        "note" to tx.note,
                        "productName" to tx.productName,
                        "quantity" to tx.quantity,
                        "unit" to tx.unit,
                        "rate" to tx.rate,
                        "category" to tx.category,
                        "paymentStatus" to tx.paymentStatus,
                        "promisedPaymentDate" to tx.promisedPaymentDate,
                        "timestamp" to tx.timestamp,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    batch.set(docRef, map, SetOptions.merge())
                    totalCount++
                }
                batch.commit().await()
            }

            // 3. Products batch
            val productChunks = backupData.products.chunked(450)
            for (chunk in productChunks) {
                val batch = db.batch()
                for (p in chunk) {
                    val docRef = userDoc.collection("products").document(p.id.toString())
                    val map = hashMapOf(
                        "id" to p.id,
                        "name" to p.name,
                        "banglaName" to p.banglaName,
                        "defaultPurchasePrice" to p.defaultPurchasePrice,
                        "defaultSellingPrice" to p.defaultSellingPrice,
                        "stockQuantity" to p.stockQuantity,
                        "unit" to p.unit,
                        "category" to p.category,
                        "barcode" to p.barcode,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    batch.set(docRef, map, SetOptions.merge())
                    totalCount++
                }
                batch.commit().await()
            }

            // 4. Daily Cash Records
            val cashChunks = backupData.dailyCashRecords.chunked(450)
            for (chunk in cashChunks) {
                val batch = db.batch()
                for (dc in chunk) {
                    val docRef = userDoc.collection("daily_cash").document(dc.dateKey)
                    val map = hashMapOf(
                        "dateKey" to dc.dateKey,
                        "dateMillis" to dc.dateMillis,
                        "sabekCash" to dc.sabekCash,
                        "closingCash" to dc.closingCash,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    batch.set(docRef, map, SetOptions.merge())
                    totalCount++
                }
                batch.commit().await()
            }

            // 5. Supplier Companies
            val supplierChunks = backupData.supplierCompanies.chunked(450)
            for (chunk in supplierChunks) {
                val batch = db.batch()
                for (sc in chunk) {
                    val docRef = userDoc.collection("supplier_companies").document(sc.id.toString())
                    val map = hashMapOf(
                        "id" to sc.id,
                        "companyName" to sc.companyName,
                        "srName" to sc.srName,
                        "srPhone" to sc.srPhone,
                        "orderDay" to sc.orderDay,
                        "deliveryDay" to sc.deliveryDay,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    batch.set(docRef, map, SetOptions.merge())
                    totalCount++
                }
                batch.commit().await()
            }

            // 6. Shop Profile Settings
            backupData.shopSettings?.let { s ->
                val docRef = userDoc.collection("settings").document("shop_profile")
                val map = hashMapOf(
                    "shopName" to s.shopName,
                    "ownerName" to s.ownerName,
                    "phone" to s.phone,
                    "openingBalance" to s.openingBalance,
                    "bkashNumber" to s.bkashNumber,
                    "nagadNumber" to s.nagadNumber,
                    "bankDetails" to s.bankDetails,
                    "updatedAt" to System.currentTimeMillis()
                )
                docRef.set(map, SetOptions.merge()).await()
                totalCount++
            }

            FirebaseOperationResult.Success(
                totalCount,
                "ফায়ারবেসে মোট $totalCount টি রেকর্ড যার যার জায়গায় নিখুঁতভাবে সিঙ্ক হয়েছে"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync to Firebase collections failed", e)
            FirebaseOperationResult.Error("ফায়ারবেস কালেকশন সিঙ্ক ব্যর্থ: ${e.localizedMessage ?: "ত্রুটি"}", e)
        }
    }

    suspend fun fetchBackupsFromFirebase(): FirebaseOperationResult<List<FirebaseBackupItem>> {
        val uid = authManager.getUserId()
            ?: return FirebaseOperationResult.Error("ব্যবহারকারী লগইন করা নেই")

        return try {
            val snapshot = db.collection(COLLECTION_USERS)
                .document(uid)
                .collection(COLLECTION_BACKUPS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val list = mutableListOf<FirebaseBackupItem>()
            for (doc in snapshot.documents) {
                val id = doc.getString("id") ?: doc.id
                val backupName = doc.getString("backupName") ?: "Unnamed Backup"
                val dataJson = doc.getString("dataJson") ?: ""
                val customerCount = (doc.getLong("customerCount") ?: 0L).toInt()
                val transactionCount = (doc.getLong("transactionCount") ?: 0L).toInt()
                val productCount = (doc.getLong("productCount") ?: 0L).toInt()
                val totalBaki = doc.getDouble("totalBaki") ?: 0.0
                val timestamp = doc.getLong("timestamp") ?: 0L
                val formattedDate = doc.getString("formattedDate") ?: ""

                list.add(
                    FirebaseBackupItem(
                        id = id,
                        userId = uid,
                        backupName = backupName,
                        dataJson = dataJson,
                        customerCount = customerCount,
                        transactionCount = transactionCount,
                        productCount = productCount,
                        totalBaki = totalBaki,
                        timestamp = timestamp,
                        formattedDate = formattedDate
                    )
                )
            }

            FirebaseOperationResult.Success(list, "${list.size} টি ক্লাউড ব্যাকআপ পাওয়া গেছে")
        } catch (e: Exception) {
            Log.e(TAG, "Fetch Firebase backups failed", e)
            FirebaseOperationResult.Error("ফায়ারবেস থেকে ব্যাকআপ লোড করা যায়নি: ${e.localizedMessage ?: "ত্রুটি"}", e)
        }
    }

    suspend fun restoreBackupFromFirebase(backupId: String): FirebaseOperationResult<FullBackupData> {
        val uid = authManager.getUserId()
            ?: return FirebaseOperationResult.Error("ব্যবহারকারী লগইন করা নেই")

        return try {
            val doc = db.collection(COLLECTION_USERS)
                .document(uid)
                .collection(COLLECTION_BACKUPS)
                .document(backupId)
                .get()
                .await()

            if (!doc.exists()) {
                return FirebaseOperationResult.Error("ব্যাকআপ রেকর্ড পাওয়া যায়নি")
            }

            val dataJson = doc.getString("dataJson")
            if (dataJson.isNullOrBlank()) {
                return FirebaseOperationResult.Error("ব্যাকআপ ফাইলটিতে কোনো ডাটা নেই")
            }

            val parsedData = DataBackupRestoreManager.parseFromJsonString(dataJson)
            FirebaseOperationResult.Success(parsedData, "ফায়ারবেস ব্যাকআপ সফলভাবে উদ্ধার করা হয়েছে!")
        } catch (e: Exception) {
            Log.e(TAG, "Restore from Firebase failed", e)
            FirebaseOperationResult.Error("ফায়ারবেস থেকে ব্যাকআপ রিস্টোর ব্যর্থ: ${e.localizedMessage ?: "ত্রুটি"}", e)
        }
    }

    suspend fun deleteBackupFromFirebase(backupId: String): FirebaseOperationResult<Unit> {
        val uid = authManager.getUserId()
            ?: return FirebaseOperationResult.Error("ব্যবহারকারী লগইন করা নেই")

        return try {
            db.collection(COLLECTION_USERS)
                .document(uid)
                .collection(COLLECTION_BACKUPS)
                .document(backupId)
                .delete()
                .await()

            FirebaseOperationResult.Success(Unit, "ব্যাকআপ মুছে ফেলা হয়েছে")
        } catch (e: Exception) {
            Log.e(TAG, "Delete Firebase backup failed", e)
            FirebaseOperationResult.Error("ব্যাকআপ মোছা সম্ভব হয়নি: ${e.localizedMessage ?: "ত্রুটি"}", e)
        }
    }
}
