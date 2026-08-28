package com.example.mawa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val banglaLabel: String) {
    SALE_CASH("নগদ বিক্রি"),         // নগদ বিক্রি (Adds to Cash, Adds to Sales)
    SALE_BAKI("বাকি বিক্রি"),         // বাকি বিক্রি (Adds to Baki receivable, Adds to Sales, DOES NOT change cash)
    BAKI_COLLECTION("বাকি জমা"),   // বাকি আদায় / জমা (Reduces Baki receivable, Adds to Cash, NOT a sale)
    PURCHASE_FORDI("ফর্দ থেকে কেনা"),    // ফর্দ থেকে মাল কেনা (Reduces Cash, Purchase record)
    PURCHASE_DIRECT("সরাসরি মাল কেনা"),   // সরাসরি ডিলার থেকে মাল কেনা (Reduces Cash, Purchase record)
    EXPENSE_SHOP("দোকানের খরচ"),      // দোকানের খরচ (Reduces Cash, Business expense)
    EXPENSE_HOME("বাড়ির খরচ/উত্তোলন"),      // বাড়ির জন্য খরচ / উত্তোলন (Reduces Cash, Personal withdrawal - NOT business expense)
    CASH_ADJUSTMENT("নগদ সমন্বয়")    // নগদ সমন্বয় / মূলধন যোগ (Adjusts Cash)
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    val customerId: Long? = null,
    val customerName: String? = null,
    val productId: Long? = null,
    val productName: String? = null,
    val quantity: Double = 0.0,
    val unit: String = "কেজি",
    val rate: Double = 0.0,
    val category: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
