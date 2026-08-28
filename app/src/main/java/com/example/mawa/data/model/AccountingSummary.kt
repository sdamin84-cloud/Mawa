package com.example.mawa.data.model

import com.example.mawa.data.local.entity.CustomerEntity
import com.example.mawa.data.local.entity.TransactionEntity

data class AccountingSummary(
    val openingBalance: Double = 0.0,          // সাবেক ক্যাশ
    val totalCashInHand: Double = 0.0,         // হাতে থাকার কথা (Total physical cash)
    val todayCashChange: Double = 0.0,         // আজকের পরিবর্তন (+/-)
    
    // Today's Breakdown
    val todayTotalSales: Double = 0.0,         // আজকের মোট বিক্রি (Cash Sales + Baki Sales)
    val todayCashSales: Double = 0.0,          // আজকের নগদ বিক্রি
    val todayBakiSales: Double = 0.0,          // আজকের বাকি বিক্রি
    val todayBakiCollection: Double = 0.0,     // আজকের বাকি আদায় / জমা (Cash in against debt)
    val todayPurchases: Double = 0.0,          // আজকের মাল কেনা (Fordi + Direct)
    val todayShopExpenses: Double = 0.0,       // আজকের দোকানের খরচ
    val todayHomeWithdrawals: Double = 0.0,    // আজকের বাড়ির জন্য নেওয়া
    
    // Period / Filtered Breakdown (For Reports / Home Accounting)
    val periodSales: Double = 0.0,
    val periodPurchases: Double = 0.0,
    val periodShopExpenses: Double = 0.0,
    val periodHomeWithdrawals: Double = 0.0,
    val periodEstimatedProfit: Double = 0.0,   // Period Sales - Period Purchases - Period Shop Expenses
    val periodProfitRemaining: Double = 0.0,   // Period Estimated Profit - Period Home Withdrawals
    
    // Baki Totals
    val totalOutstandingBaki: Double = 0.0,    // সর্বমোট বকেয়া বাকি (All customers)
    val todayNewBaki: Double = 0.0             // আজকের নতুন বাকি বিক্রি
)

data class CustomerWithBalance(
    val customer: CustomerEntity,
    val totalBakiGiven: Double = 0.0,
    val totalJomaReceived: Double = 0.0,
    val currentBalance: Double = 0.0,          // openingBalance + totalBaki - totalJoma
    val lastTransaction: TransactionEntity? = null,
    val hasTransactionToday: Boolean = false
)

data class ProductStats(
    val productId: Long,
    val productName: String,
    val unit: String,
    val totalPurchasedQty: Double = 0.0,
    val totalPurchasedAmount: Double = 0.0,
    val purchaseCount: Int = 0,
    val avgPurchasePrice: Double = 0.0,
    val latestPurchasePrice: Double = 0.0,
    val highestPurchasePrice: Double = 0.0,
    val lowestPurchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val estimatedMargin: Double = 0.0,         // sellingPrice - avgPurchasePrice
    val purchaseHistory: List<TransactionEntity> = emptyList()
)

enum class TimeFilter(val banglaLabel: String) {
    TODAY("আজ"),
    THIS_WEEK("এই সপ্তাহ"),
    THIS_MONTH("এই মাস"),
    ALL_TIME("সব সময়")
}
