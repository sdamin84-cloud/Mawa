package com.example.mawa.util

import com.example.mawa.data.local.entity.SupplierCompanyEntity
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType
import java.util.Calendar
import java.util.concurrent.TimeUnit

enum class PredictionReasonType(val banglaLabel: String) {
    DAY_OF_WEEK_PATTERN("প্রতি সপ্তাহের এই বারে নেন"),
    DAILY_STREAK("প্রতিদিনের নিয়মিত পণ্য"),
    SCHEDULED_COMPANY("আজ শিডিউলকৃত কোম্পানি"),
    RECENT_CYCLE("নেওয়ার সময় হয়েছে")
}

data class PredictivePurchaseItem(
    val id: String,
    val name: String,
    val predictedAmount: Double,
    val previousRate: Double = 0.0,
    val previousQuantity: Double = 1.0,
    val unit: String = "পিস",
    val reasonType: PredictionReasonType,
    val reasonText: String,
    val isPurchaseType: Boolean = true, // true: পণ্য ক্রয়/স্টক, false: দোকান খরচ
    val lastPurchasedMillis: Long = 0L,
    val daysAgo: Int = 0,
    val companyEntity: SupplierCompanyEntity? = null
)

object SmartPredictivePurchaseEngine {

    /**
     * Analyzes historical transactions, supplier companies schedule, and target date
     * to predict likely company visits or product purchases for the selected date.
     */
    fun generatePredictions(
        transactions: List<TransactionEntity>,
        supplierCompanies: List<SupplierCompanyEntity>,
        targetDateMillis: Long = System.currentTimeMillis()
    ): List<PredictivePurchaseItem> {
        val predictions = mutableListOf<PredictivePurchaseItem>()

        val targetCal = Calendar.getInstance().apply { timeInMillis = targetDateMillis }
        val targetDayOfWeek = targetCal.get(Calendar.DAY_OF_WEEK) // Calendar.SATURDAY, etc.
        val targetDayBengali = BengaliUtils.getBengaliDayOfWeek(targetDateMillis)

        // Find today's recorded expense/purchase transactions so we don't predict what was already entered today!
        val startOfTargetDay = getStartOfDay(targetDateMillis)
        val endOfTargetDay = getEndOfDay(targetDateMillis)

        val alreadyEnteredToday = transactions.filter {
            it.timestamp in startOfTargetDay..endOfTargetDay &&
                    (it.type == TransactionType.PURCHASE_DIRECT ||
                            it.type == TransactionType.PURCHASE_FORDI ||
                            it.type == TransactionType.EXPENSE_SHOP)
        }.mapNotNull { tx ->
            val name = tx.productName?.trim()?.ifBlank { null }
                ?: tx.note.trim().ifBlank { null }
            name?.lowercase()
        }.toSet()

        // -------------------------------------------------------------
        // 1. Check Supplier Companies Scheduled for this Day of Week
        // -------------------------------------------------------------
        for (company in supplierCompanies) {
            val isScheduled = company.orderDay.contains(targetDayBengali, ignoreCase = true) ||
                    company.deliveryDay.contains(targetDayBengali, ignoreCase = true)
            if (isScheduled) {
                val cleanCompName = company.companyName.trim()
                if (alreadyEnteredToday.any { it.contains(cleanCompName.lowercase()) }) {
                    continue // Already bought/entered today
                }

                // Find historical purchases for this company
                val historyTxs = transactions.filter { tx ->
                    val txName = (tx.productName ?: tx.note).trim().lowercase()
                    txName.contains(cleanCompName.lowercase()) &&
                            (tx.type == TransactionType.PURCHASE_DIRECT ||
                                    tx.type == TransactionType.PURCHASE_FORDI ||
                                    tx.type == TransactionType.EXPENSE_SHOP)
                }.sortedByDescending { it.timestamp }

                val lastTx = historyTxs.firstOrNull()
                val avgAmount = if (historyTxs.isNotEmpty()) {
                    historyTxs.take(3).map { it.amount }.average()
                } else 0.0

                val isDelivery = company.deliveryDay.contains(targetDayBengali, ignoreCase = true)
                val actionNote = if (isDelivery) "আজ ডেলিভারি আসার শিডিউল" else "আজ অর্ডার নেওয়ার দিন"

                predictions.add(
                    PredictivePurchaseItem(
                        id = "comp_${company.id}_$cleanCompName",
                        name = cleanCompName,
                        predictedAmount = if (lastTx != null && lastTx.amount > 0) lastTx.amount else (if (avgAmount > 0) avgAmount else 0.0),
                        previousRate = lastTx?.rate ?: 0.0,
                        previousQuantity = lastTx?.quantity ?: 1.0,
                        unit = lastTx?.unit ?: "পিস",
                        reasonType = PredictionReasonType.SCHEDULED_COMPANY,
                        reasonText = "$targetDayBengali: $actionNote",
                        isPurchaseType = true,
                        lastPurchasedMillis = lastTx?.timestamp ?: 0L,
                        daysAgo = calculateDaysAgo(lastTx?.timestamp ?: 0L, targetDateMillis),
                        companyEntity = company
                    )
                )
            }
        }

        // -------------------------------------------------------------
        // 2. Analyze Historical Transactions for Daily & Day-of-Week Patterns
        // -------------------------------------------------------------
        // Group past expenses & purchases by product/company name (excluding target day)
        val pastTransactions = transactions.filter {
            it.timestamp < startOfTargetDay &&
                    (it.type == TransactionType.PURCHASE_DIRECT ||
                            it.type == TransactionType.PURCHASE_FORDI ||
                            it.type == TransactionType.EXPENSE_SHOP)
        }

        val groupedByName = mutableMapOf<String, MutableList<TransactionEntity>>()
        for (tx in pastTransactions) {
            val name = (tx.productName?.trim()?.ifBlank { null }
                ?: tx.note.trim().ifBlank { null }) ?: continue
            val clean = name.replace(Regex("""^(খরচ|দোকান|মাল|ক্রয়|বাকি|বিল)\s*:\s*"""), "").trim()
            if (clean.length >= 2) {
                groupedByName.getOrPut(clean) { mutableListOf() }.add(tx)
            }
        }

        val nowMillis = targetDateMillis

        for ((name, txList) in groupedByName) {
            val cleanLower = name.lowercase()
            // Skip if already in predictions or already entered today
            if (predictions.any { it.name.equals(name, ignoreCase = true) } ||
                alreadyEnteredToday.any { it.contains(cleanLower) }
            ) {
                continue
            }

            val sortedTx = txList.sortedByDescending { it.timestamp }
            val lastTx = sortedTx.first()
            val daysAgo = calculateDaysAgo(lastTx.timestamp, nowMillis)

            // Pattern A: Daily Essentials (e.g. bread, milk, egg, vegetables, ice, newspapers)
            // Purchased at least 3 times in the last 7 days OR daily streak
            val purchasesInLast7Days = sortedTx.count {
                it.timestamp >= (nowMillis - TimeUnit.DAYS.toMillis(7))
            }

            if (purchasesInLast7Days >= 3 || (daysAgo in 1..2 && sortedTx.size >= 4)) {
                // Highly probable daily essential!
                val recentAvgAmount = sortedTx.take(3).map { it.amount }.average()
                predictions.add(
                    PredictivePurchaseItem(
                        id = "daily_$name",
                        name = name,
                        predictedAmount = if (lastTx.amount > 0) lastTx.amount else recentAvgAmount,
                        previousRate = lastTx.rate,
                        previousQuantity = if (lastTx.quantity > 0) lastTx.quantity else 1.0,
                        unit = lastTx.unit.ifBlank { "কেজি" },
                        reasonType = PredictionReasonType.DAILY_STREAK,
                        reasonText = "প্রতিদিনের নিয়মিত পণ্য (গত ৭ দিনে ${BengaliUtils.toBanglaDigits(purchasesInLast7Days)} বার নেওয়া হয়েছে)",
                        isPurchaseType = lastTx.type != TransactionType.EXPENSE_SHOP,
                        lastPurchasedMillis = lastTx.timestamp,
                        daysAgo = daysAgo
                    )
                )
                continue
            }

            // Pattern B: Day of Week recurring purchases (e.g., Every Saturday or Monday)
            val sameDayCount = sortedTx.count { tx ->
                val c = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                c.get(Calendar.DAY_OF_WEEK) == targetDayOfWeek
            }

            // If taken on this specific day of week at least 2 times and constitutes a high ratio
            if (sameDayCount >= 2 && sortedTx.size >= 2) {
                val sameDayTxs = sortedTx.filter { tx ->
                    val c = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                    c.get(Calendar.DAY_OF_WEEK) == targetDayOfWeek
                }
                val lastSameDayTx = sameDayTxs.firstOrNull() ?: lastTx
                val sameDayAvg = sameDayTxs.map { it.amount }.average()

                predictions.add(
                    PredictivePurchaseItem(
                        id = "dow_$name",
                        name = name,
                        predictedAmount = if (lastSameDayTx.amount > 0) lastSameDayTx.amount else sameDayAvg,
                        previousRate = lastSameDayTx.rate,
                        previousQuantity = if (lastSameDayTx.quantity > 0) lastSameDayTx.quantity else 1.0,
                        unit = lastSameDayTx.unit.ifBlank { "পিস" },
                        reasonType = PredictionReasonType.DAY_OF_WEEK_PATTERN,
                        reasonText = "প্রতি $targetDayBengali নেন (${BengaliUtils.toBanglaDigits(sameDayCount)} বার রেকর্ড আছে)",
                        isPurchaseType = lastSameDayTx.type != TransactionType.EXPENSE_SHOP,
                        lastPurchasedMillis = lastSameDayTx.timestamp,
                        daysAgo = daysAgo
                    )
                )
            }
        }

        // Sort by priority: Scheduled Companies first, then Daily Essentials, then Day-of-week recurrence
        return predictions.sortedWith(
            compareBy<PredictivePurchaseItem> {
                when (it.reasonType) {
                    PredictionReasonType.SCHEDULED_COMPANY -> 0
                    PredictionReasonType.DAILY_STREAK -> 1
                    PredictionReasonType.DAY_OF_WEEK_PATTERN -> 2
                    PredictionReasonType.RECENT_CYCLE -> 3
                }
            }.thenByDescending { it.predictedAmount }
        )
    }

    private fun getStartOfDay(timeMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            this.timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun getEndOfDay(timeMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            this.timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    private fun calculateDaysAgo(pastMillis: Long, currentMillis: Long): Int {
        if (pastMillis <= 0L) return 0
        val diff = currentMillis - pastMillis
        return if (diff > 0) (diff / TimeUnit.DAYS.toMillis(1)).toInt() else 0
    }
}
