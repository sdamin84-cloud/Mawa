package com.example.mawa.util

import android.content.Context
import android.content.Intent
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExportUtils {

    fun generateTransactionsCsv(
        transactions: List<TransactionEntity>,
        filterLabel: String
    ): String {
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        sb.append("তারিখ ও সময়,লেনদেনের ধরন,বিবরণ / গ্রাহক / পণ্য,পরিমাণ,একক,দর (৳),টাকা (৳),নোট\n")

        for (tx in transactions) {
            val dateStr = dateFormat.format(Date(tx.timestamp))
            val typeStr = when (tx.type) {
                TransactionType.SALE_CASH -> "নগদ বিক্রি"
                TransactionType.SALE_BAKI -> "বাকি বিক্রি"
                TransactionType.BAKI_COLLECTION -> "বাকি আদায়"
                TransactionType.PURCHASE_FORDI -> "ফর্দ থেকে ক্রয়"
                TransactionType.PURCHASE_DIRECT -> "সরাসরি মাল ক্রয়"
                TransactionType.EXPENSE_SHOP -> "দোকানের খরচ"
                TransactionType.EXPENSE_HOME -> "বাড়ির খরচ/উত্তোলন"
                TransactionType.CASH_ADJUSTMENT -> "নগদ সমন্বয়"
            }

            val desc = when {
                !tx.customerName.isNullOrBlank() -> tx.customerName
                !tx.productName.isNullOrBlank() -> tx.productName
                else -> tx.category.ifBlank { "সাধারণ" }
            }.replace(",", " ")

            val qty = if (tx.quantity > 0) tx.quantity.toString() else "-"
            val unit = if (tx.quantity > 0) tx.unit else "-"
            val rate = if (tx.rate > 0) tx.rate.toString() else "-"
            val amount = tx.amount.toString()
            val cleanNote = tx.note.replace("\n", " ").replace(",", " ")

            sb.append("\"$dateStr\",\"$typeStr\",\"$desc\",\"$qty\",\"$unit\",\"$rate\",\"$amount\",\"$cleanNote\"\n")
        }

        return sb.toString()
    }

    fun generateFordiCsv(items: List<com.example.mawa.data.local.entity.FordiItemEntity>): String {
        val sb = StringBuilder()
        sb.append("ক্রঃ নং,পণ্যের নাম,পরিমাণ,একক,দর (৳),মোট (৳),ক্রয় অবস্থা\n")
        items.forEachIndexed { index, item ->
            val status = if (item.isPurchased) "কেনা হয়েছে" else "বাকি আছে"
            val total = if (item.isPurchased) item.actualTotal else (item.plannedQuantity * item.purchaseRate)
            sb.append("\"${index + 1}\",\"${item.productName}\",\"${item.plannedQuantity}\",\"${item.unit}\",\"${item.purchaseRate}\",\"$total\",\"$status\"\n")
        }
        return sb.toString()
    }

    fun generateCustomerBakiSummaryCsv(customers: List<com.example.mawa.data.model.CustomerWithBalance>): String {
        val sb = StringBuilder()
        sb.append("ক্রঃ নং,কাস্টমার নাম,মোবাইল নম্বর,ঠিকানা,সাবেক বাকি (৳),মোট বাকি প্রদান (৳),মোট জমা আদায় (৳),বর্তমান বাকি (৳),বাকির সীমা (৳),টাকা দেওয়ার তারিখ,কাস্টমার ধরন,জামিনদার\n")
        customers.forEachIndexed { index, item ->
            val cust = item.customer
            val name = cust.name.replace(",", " ")
            val phone = cust.phone.replace(",", " ")
            val address = cust.address.replace(",", " ")
            val promiseDate = if (cust.promisedPaymentDate > 0) SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(cust.promisedPaymentDate)) else "-"
            val tag = when (cust.categoryTag) {
                "VIP" -> "VIP"
                "WHOLESALE" -> "পাইকারি"
                else -> "নিয়মিত"
            }
            val guarantor = cust.nidOrGuarantor.replace(",", " ")
            sb.append("\"${index + 1}\",\"$name\",\"$phone\",\"$address\",\"${cust.openingBalance}\",\"${item.totalBakiGiven}\",\"${item.totalJomaReceived}\",\"${item.currentBalance}\",\"${cust.creditLimit}\",\"$promiseDate\",\"$tag\",\"$guarantor\"\n")
        }
        return sb.toString()
    }

    fun generateMemoText(
        shopName: String,
        filterLabel: String,
        totalSales: Double,
        cashSales: Double,
        bakiSales: Double,
        bakiCollection: Double,
        purchases: Double,
        shopExpenses: Double,
        homeWithdrawals: Double,
        profit: Double,
        profitRemaining: Double
    ): String {
        val now = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        return """
========================================
             $shopName
       $filterLabel - হিসাব বিবরণী মেমো
========================================
প্রিন্ট তারিখ: $now
সময়কাল: $filterLabel
----------------------------------------
১. মোট বিক্রি (নগদ + বাকি):   ${BengaliUtils.formatTaka(totalSales)}
   • নগদ বিক্রি:             ${BengaliUtils.formatTaka(cashSales)}
   • বাকি বিক্রি:             ${BengaliUtils.formatTaka(bakiSales)}
----------------------------------------
২. বাকি আদায় (জমা):          ${BengaliUtils.formatTaka(bakiCollection)}
৩. মাল ক্রয় (ফর্দ + সরাসরি):  ${BengaliUtils.formatTaka(purchases)}
৪. দোকানের পরিচালনা খরচ:     ${BengaliUtils.formatTaka(shopExpenses)}
৫. বাড়ির জন্য নেওয়া (উত্তোলন):  ${BengaliUtils.formatTaka(homeWithdrawals)}
----------------------------------------
★ আনুমানিক ব্যবসায়িক লাভ:     ${BengaliUtils.formatTaka(profit)}
  (মোট বিক্রি − মাল ক্রয় − দোকানের খরচ)
★ বাড়ির খরচ বাদ দিয়ে অবশিষ্ট: ${BengaliUtils.formatTaka(profitRemaining)}
========================================
      MAWA স্মার্ট খাতা দ্বারা প্রস্তুতকৃত
========================================
        """.trimIndent()
    }

    fun shareText(context: Context, text: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, title)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
