package com.example.mawa.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.mawa.data.local.entity.CustomerEntity
import com.example.mawa.data.local.entity.FordiItemEntity
import com.example.mawa.data.local.entity.PersonalTransactionEntity
import com.example.mawa.data.local.entity.PersonalTransactionType
import com.example.mawa.data.local.entity.ProductEntity
import com.example.mawa.data.local.entity.ShopSettingsEntity
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType
import com.example.mawa.data.model.CustomerWithBalance
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FullBackupData(
    val exportDate: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0",
    val shopSettings: ShopSettingsEntity? = null,
    val customers: List<CustomerEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val fordiItems: List<FordiItemEntity> = emptyList(),
    val products: List<ProductEntity> = emptyList(),
    val personalTransactions: List<PersonalTransactionEntity> = emptyList()
)

object DataBackupRestoreManager {

    // ==========================================
    // 1. JSON BACKUP & RESTORE
    // ==========================================

    fun exportToJsonString(data: FullBackupData): String {
        val root = JSONObject()
        root.put("version", "1.0")
        root.put("appName", "MAWA Digital Khata")
        root.put("exportedAt", data.exportDate)
        root.put("exportDateReadable", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(data.exportDate)))

        // Shop Settings
        data.shopSettings?.let { s ->
            val sObj = JSONObject()
            sObj.put("id", s.id)
            sObj.put("shopName", s.shopName)
            sObj.put("ownerName", s.ownerName)
            sObj.put("phone", s.phone)
            sObj.put("openingBalance", s.openingBalance)
            sObj.put("currencySymbol", s.currencySymbol)
            sObj.put("appMode", s.appMode)
            sObj.put("isModeConfigured", s.isModeConfigured)
            sObj.put("createdAt", s.createdAt)
            root.put("shopSettings", sObj)
        }

        // Customers
        val custArr = JSONArray()
        data.customers.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("phone", c.phone)
            obj.put("address", c.address)
            obj.put("openingBalance", c.openingBalance)
            obj.put("note", c.note)
            obj.put("createdAt", c.createdAt)
            custArr.put(obj)
        }
        root.put("customers", custArr)

        // Transactions
        val txArr = JSONArray()
        data.transactions.forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("type", t.type.name)
            obj.put("amount", t.amount)
            obj.put("timestamp", t.timestamp)
            obj.put("customerId", t.customerId ?: -1L)
            obj.put("customerName", t.customerName ?: "")
            obj.put("productId", t.productId ?: -1L)
            obj.put("productName", t.productName ?: "")
            obj.put("quantity", t.quantity)
            obj.put("unit", t.unit)
            obj.put("rate", t.rate)
            obj.put("category", t.category)
            obj.put("note", t.note)
            txArr.put(obj)
        }
        root.put("transactions", txArr)

        // Fordi Items
        val fordiArr = JSONArray()
        data.fordiItems.forEach { f ->
            val obj = JSONObject()
            obj.put("id", f.id)
            obj.put("productId", f.productId ?: -1L)
            obj.put("productName", f.productName)
            obj.put("plannedQuantity", f.plannedQuantity)
            obj.put("unit", f.unit)
            obj.put("purchaseRate", f.purchaseRate)
            obj.put("sellingRate", f.sellingRate)
            obj.put("isPurchased", f.isPurchased)
            obj.put("actualQuantity", f.actualQuantity)
            obj.put("actualRate", f.actualRate)
            obj.put("actualTotal", f.actualTotal)
            obj.put("createdAt", f.createdAt)
            obj.put("purchaseDate", f.purchaseDate ?: 0L)
            fordiArr.put(obj)
        }
        root.put("fordiItems", fordiArr)

        // Products
        val prodArr = JSONArray()
        data.products.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("banglaName", p.banglaName)
            obj.put("unit", p.unit)
            obj.put("defaultPurchasePrice", p.defaultPurchasePrice)
            obj.put("defaultSellingPrice", p.defaultSellingPrice)
            obj.put("category", p.category)
            obj.put("createdAt", p.createdAt)
            obj.put("isActive", p.isActive)
            prodArr.put(obj)
        }
        root.put("products", prodArr)

        // Personal Transactions
        val ptxArr = JSONArray()
        data.personalTransactions.forEach { pt ->
            val obj = JSONObject()
            obj.put("id", pt.id)
            obj.put("type", pt.type.name)
            obj.put("amount", pt.amount)
            obj.put("title", pt.title)
            obj.put("category", pt.category)
            obj.put("note", pt.note)
            obj.put("timestamp", pt.timestamp)
            ptxArr.put(obj)
        }
        root.put("personalTransactions", ptxArr)

        return root.toString(2)
    }

    fun parseFromJsonString(jsonString: String): FullBackupData {
        val root = JSONObject(jsonString)
        val exportDate = root.optLong("exportedAt", System.currentTimeMillis())

        // Shop Settings
        var settings: ShopSettingsEntity? = null
        if (root.has("shopSettings")) {
            val s = root.getJSONObject("shopSettings")
            settings = ShopSettingsEntity(
                id = s.optInt("id", 1),
                shopName = s.optString("shopName", "মাওয়া ডিজিটাল খাতা"),
                ownerName = s.optString("ownerName", "দোকানদার"),
                openingBalance = s.optDouble("openingBalance", 0.0),
                currencySymbol = s.optString("currencySymbol", "৳"),
                phone = s.optString("phone", ""),
                createdAt = s.optLong("createdAt", System.currentTimeMillis()),
                appMode = s.optString("appMode", "BOTH"),
                isModeConfigured = s.optBoolean("isModeConfigured", true)
            )
        }

        // Customers
        val customers = mutableListOf<CustomerEntity>()
        if (root.has("customers")) {
            val arr = root.getJSONArray("customers")
            for (i in 0 until arr.length()) {
                val c = arr.getJSONObject(i)
                customers.add(
                    CustomerEntity(
                        id = c.optLong("id", 0L),
                        name = c.optString("name", "নামহীন"),
                        phone = c.optString("phone", ""),
                        address = c.optString("address", ""),
                        openingBalance = c.optDouble("openingBalance", 0.0),
                        note = c.optString("note", ""),
                        createdAt = c.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        // Transactions
        val transactions = mutableListOf<TransactionEntity>()
        if (root.has("transactions")) {
            val arr = root.getJSONArray("transactions")
            for (i in 0 until arr.length()) {
                val t = arr.getJSONObject(i)
                val typeStr = t.optString("type", "EXPENSE_SHOP")
                val txType = try {
                    TransactionType.valueOf(typeStr)
                } catch (e: Exception) {
                    TransactionType.EXPENSE_SHOP
                }
                val custId = t.optLong("customerId", -1L)
                val prodId = t.optLong("productId", -1L)

                transactions.add(
                    TransactionEntity(
                        id = t.optLong("id", 0L),
                        type = txType,
                        amount = t.optDouble("amount", 0.0),
                        timestamp = t.optLong("timestamp", System.currentTimeMillis()),
                        customerId = if (custId > 0) custId else null,
                        customerName = t.optString("customerName", "").takeIf { it.isNotBlank() },
                        productId = if (prodId > 0) prodId else null,
                        productName = t.optString("productName", "").takeIf { it.isNotBlank() },
                        quantity = t.optDouble("quantity", 0.0),
                        unit = t.optString("unit", "টি"),
                        rate = t.optDouble("rate", 0.0),
                        category = t.optString("category", "সাধারণ"),
                        note = t.optString("note", "")
                    )
                )
            }
        }

        // Fordi Items
        val fordiItems = mutableListOf<FordiItemEntity>()
        if (root.has("fordiItems")) {
            val arr = root.getJSONArray("fordiItems")
            for (i in 0 until arr.length()) {
                val f = arr.getJSONObject(i)
                val prodId = f.optLong("productId", -1L)
                fordiItems.add(
                    FordiItemEntity(
                        id = f.optLong("id", 0L),
                        productId = if (prodId > 0) prodId else null,
                        productName = f.optString("productName", "পণ্য"),
                        plannedQuantity = f.optDouble("plannedQuantity", 1.0),
                        unit = f.optString("unit", "কেজি"),
                        purchaseRate = f.optDouble("purchaseRate", 0.0),
                        sellingRate = f.optDouble("sellingRate", 0.0),
                        isPurchased = f.optBoolean("isPurchased", false),
                        actualQuantity = f.optDouble("actualQuantity", 0.0),
                        actualRate = f.optDouble("actualRate", 0.0),
                        actualTotal = f.optDouble("actualTotal", 0.0),
                        createdAt = f.optLong("createdAt", System.currentTimeMillis()),
                        purchaseDate = f.optLong("purchaseDate", 0L).takeIf { it > 0 }
                    )
                )
            }
        }

        // Products
        val products = mutableListOf<ProductEntity>()
        if (root.has("products")) {
            val arr = root.getJSONArray("products")
            for (i in 0 until arr.length()) {
                val p = arr.getJSONObject(i)
                products.add(
                    ProductEntity(
                        id = p.optLong("id", 0L),
                        name = p.optString("name", "পণ্য"),
                        banglaName = p.optString("banglaName", ""),
                        unit = p.optString("unit", "কেজি"),
                        defaultPurchasePrice = p.optDouble("defaultPurchasePrice", 0.0),
                        defaultSellingPrice = p.optDouble("defaultSellingPrice", 0.0),
                        category = p.optString("category", "মুদি"),
                        createdAt = p.optLong("createdAt", System.currentTimeMillis()),
                        isActive = p.optBoolean("isActive", true)
                    )
                )
            }
        }

        // Personal Transactions
        val personalTransactions = mutableListOf<PersonalTransactionEntity>()
        if (root.has("personalTransactions")) {
            val arr = root.getJSONArray("personalTransactions")
            for (i in 0 until arr.length()) {
                val pt = arr.getJSONObject(i)
                val typeStr = pt.optString("type", "EXPENSE")
                val pType = try {
                    PersonalTransactionType.valueOf(typeStr)
                } catch (e: Exception) {
                    PersonalTransactionType.EXPENSE
                }
                personalTransactions.add(
                    PersonalTransactionEntity(
                        id = pt.optLong("id", 0L),
                        type = pType,
                        amount = pt.optDouble("amount", 0.0),
                        title = pt.optString("title", "ব্যক্তিগত"),
                        category = pt.optString("category", "অন্যান্য"),
                        note = pt.optString("note", ""),
                        timestamp = pt.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        return FullBackupData(
            exportDate = exportDate,
            shopSettings = settings,
            customers = customers,
            transactions = transactions,
            fordiItems = fordiItems,
            products = products,
            personalTransactions = personalTransactions
        )
    }

    // ==========================================
    // 2. CSV EXPORT & IMPORT
    // ==========================================

    fun generateCustomersCsv(customers: List<CustomerWithBalance>): String {
        val sb = StringBuilder()
        sb.append("আইডি,কাস্টমার নাম,মোবাইল,ঠিকানা,পূর্বের বাকি (৳),মোট বাকি দেওয়া (৳),মোট জমা পাওয়া (৳),বর্তমান বাকি ব্যালেন্স (৳)\n")
        for (c in customers) {
            val name = c.customer.name.replace(",", " ")
            val phone = c.customer.phone.replace(",", " ")
            val address = c.customer.address.replace(",", " ")
            sb.append("${c.customer.id},\"$name\",\"$phone\",\"$address\",${c.customer.openingBalance},${c.totalBakiGiven},${c.totalJomaReceived},${c.currentBalance}\n")
        }
        return sb.toString()
    }

    fun generateFordiCsv(fordiItems: List<FordiItemEntity>): String {
        val sb = StringBuilder()
        sb.append("আইডি,পণ্যের নাম,পরিমাণ,একক,ক্রয় দর (৳),বিক্রয় দর (৳),কেনা হয়েছে কি,প্রকৃত মোট টাকা (৳)\n")
        for (f in fordiItems) {
            val name = f.productName.replace(",", " ")
            sb.append("${f.id},\"$name\",${f.plannedQuantity},\"${f.unit}\",${f.purchaseRate},${f.sellingRate},${if (f.isPurchased) "হ্যাঁ" else "না"},${f.actualTotal}\n")
        }
        return sb.toString()
    }

    fun parseCustomersFromCsv(csvText: String): List<CustomerEntity> {
        val list = mutableListOf<CustomerEntity>()
        val lines = csvText.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isBlank() || trimmed.startsWith("আইডি") || trimmed.startsWith("ID") || trimmed.startsWith("Name") || trimmed.startsWith("নাম")) continue
            val parts = trimmed.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
            if (parts.isNotEmpty()) {
                val name = (if (parts.size > 1) parts[1] else parts[0]).replace("\"", "").trim()
                val phone = (if (parts.size > 2) parts[2] else "").replace("\"", "").trim()
                val address = (if (parts.size > 3) parts[3] else "").replace("\"", "").trim()
                val opening = (if (parts.size > 4) parts[4] else "").replace("\"", "").trim().toDoubleOrNull() ?: 0.0
                if (name.isNotBlank()) {
                    list.add(
                        CustomerEntity(
                            name = name,
                            phone = phone,
                            address = address,
                            openingBalance = opening
                        )
                    )
                }
            }
        }
        return list
    }

    // ==========================================
    // 3. MODERN BRANDED MEMO IMAGE GENERATION (PNG)
    // ==========================================

    fun createCustomerBakiMemoBitmap(
        shopName: String,
        shopPhone: String,
        customerWithBalance: CustomerWithBalance,
        recentTransactions: List<TransactionEntity>
    ): Bitmap {
        val width = 900
        val height = 1200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply { color = Color.parseColor("#F8F9FD") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Card Container
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val cardRect = RectF(40f, 40f, width - 40f, height - 40f)
        canvas.drawRoundRect(cardRect, 30f, 30f, cardPaint)

        // Top Banner Header
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1B5E20") }
        val headerRect = RectF(40f, 40f, width - 40f, 220f)
        canvas.drawRoundRect(headerRect, 30f, 30f, headerPaint)
        canvas.drawRect(40f, 190f, width - 40f, 220f, headerPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 44f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(shopName.ifBlank { "মাওয়া স্মার্ট খাতা" }, width / 2f, 110f, titlePaint)

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E8F5E9")
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        val dateStr = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        canvas.drawText("কাস্টমার বাকি খতিয়ান ও হিসাব বিবরণী · $dateStr", width / 2f, 160f, subtitlePaint)
        if (shopPhone.isNotBlank()) {
            canvas.drawText("যোগাযোগ: $shopPhone", width / 2f, 195f, subtitlePaint)
        }

        // Customer Info Card
        val custInfoBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F1F5F9") }
        val custRect = RectF(70f, 250f, width - 70f, 390f)
        canvas.drawRoundRect(custRect, 20f, 20f, custInfoBg)

        val textDark = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 30f
            isFakeBoldText = true
        }
        val textMuted = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 24f
        }

        val cust = customerWithBalance.customer
        canvas.drawText("গ্রাহকের নাম: ${cust.name}", 95f, 298f, textDark)
        val phoneStr = if (cust.phone.isNotBlank()) "মোবাইল: ${cust.phone}" else "মোবাইল: প্রযোজ্য নয়"
        val addrStr = if (cust.address.isNotBlank()) "ঠিকানা: ${cust.address}" else ""
        canvas.drawText("$phoneStr  ${if (addrStr.isNotBlank()) " · $addrStr" else ""}", 95f, 342f, textMuted)

        // Balance Highlight Badge
        val isDue = customerWithBalance.currentBalance > 0
        val badgeColor = if (isDue) Color.parseColor("#DC2626") else Color.parseColor("#16A34A")
        val balanceBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = badgeColor }
        val balRect = RectF(70f, 415f, width - 70f, 545f)
        canvas.drawRoundRect(balRect, 24f, 24f, balanceBoxPaint)

        val balLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        val balValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 52f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(if (isDue) "বর্তমান মোট বকেয়া বাকি" else "বর্তমান মোট জমা ব্যালেন্স", width / 2f, 460f, balLabelPaint)
        canvas.drawText(BengaliUtils.formatTaka(customerWithBalance.currentBalance), width / 2f, 520f, balValPaint)

        // Table Header
        var currentY = 590f
        val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E2E8F0") }
        canvas.drawRoundRect(RectF(70f, currentY, width - 70f, currentY + 50f), 10f, 10f, tableHeaderPaint)

        val thText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 22f
            isFakeBoldText = true
        }
        canvas.drawText("তারিখ ও বিবরণ", 90f, currentY + 34f, thText)
        canvas.drawText("বাকি (+)", width - 340f, currentY + 34f, thText)
        canvas.drawText("জমা (−)", width - 180f, currentY + 34f, thText)

        currentY += 75f

        // Previous Balance Row
        if (cust.openingBalance > 0) {
            canvas.drawText("সাবেক / পূর্বের জের", 90f, currentY, textDark)
            canvas.drawText(BengaliUtils.formatTaka(cust.openingBalance), width - 340f, currentY, textDark)
            canvas.drawText("-", width - 160f, currentY, textMuted)
            currentY += 45f
            val linePaint = Paint().apply { color = Color.parseColor("#E2E8F0"); strokeWidth = 1.5f }
            canvas.drawLine(70f, currentY - 15f, width - 70f, currentY - 15f, linePaint)
        }

        // List Transactions
        val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        val displayTx = recentTransactions.take(8)
        for (tx in displayTx) {
            val timeStr = dateFormat.format(Date(tx.timestamp))
            val noteStr = if (tx.note.isNotBlank()) " (${tx.note})" else ""
            val desc = "${if (tx.type == TransactionType.SALE_BAKI) "বাকি খরিদ" else "নগদ জমা"}$noteStr"

            val itemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E293B"); textSize = 22f }
            val itemSub = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#64748B"); textSize = 18f }

            canvas.drawText(desc, 90f, currentY, itemPaint)
            canvas.drawText(timeStr, 90f, currentY + 24f, itemSub)

            if (tx.type == TransactionType.SALE_BAKI) {
                val redAmt = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#DC2626"); textSize = 24f; isFakeBoldText = true }
                canvas.drawText(BengaliUtils.formatTaka(tx.amount), width - 340f, currentY + 10f, redAmt)
                canvas.drawText("-", width - 160f, currentY + 10f, textMuted)
            } else {
                val greenAmt = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#16A34A"); textSize = 24f; isFakeBoldText = true }
                canvas.drawText("-", width - 320f, currentY + 10f, textMuted)
                canvas.drawText(BengaliUtils.formatTaka(tx.amount), width - 200f, currentY + 10f, greenAmt)
            }

            currentY += 60f
            val linePaint = Paint().apply { color = Color.parseColor("#F1F5F9"); strokeWidth = 1.5f }
            canvas.drawLine(70f, currentY - 10f, width - 70f, currentY - 10f, linePaint)
        }

        // Summary Totals Card
        val sumBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F8FAFC") }
        val sumRect = RectF(70f, height - 230f, width - 70f, height - 100f)
        canvas.drawRoundRect(sumRect, 16f, 16f, sumBoxPaint)

        val sumBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(sumRect, 16f, 16f, sumBorder)

        val sumValRed = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#DC2626"); textSize = 24f; isFakeBoldText = true }
        val sumValGreen = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#16A34A"); textSize = 24f; isFakeBoldText = true }

        canvas.drawText("মোট বাকি: ${BengaliUtils.formatTaka(customerWithBalance.totalBakiGiven + cust.openingBalance)}", 95f, height - 170f, sumValRed)
        canvas.drawText("মোট পরিশোধিত: ${BengaliUtils.formatTaka(customerWithBalance.totalJomaReceived)}", width - 420f, height - 170f, sumValGreen)

        // Footer Branding Watermark
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("MAWA ডিজিটাল ক্যাশ ও খতিয়ান দ্বারা স্বয়ংক্রিয়ভাবে প্রস্তুতকৃত মেমো", width / 2f, height - 60f, footerPaint)

        return bitmap
    }

    fun createDailyCashboxMemoBitmap(
        shopName: String,
        dateLabel: String,
        openingCash: Double,
        cashSales: Double,
        bakiCollection: Double,
        ownerDeposit: Double,
        purchases: Double,
        expenses: Double,
        ownerWithdrawal: Double,
        closingBalance: Double
    ): Bitmap {
        val width = 900
        val height = 1250
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply { color = Color.parseColor("#F4F6FB") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Card Container
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val cardRect = RectF(40f, 40f, width - 40f, height - 40f)
        canvas.drawRoundRect(cardRect, 30f, 30f, cardPaint)

        // Header Banner (Navy/Blue theme for Cashbox)
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0F172A") }
        val headerRect = RectF(40f, 40f, width - 40f, 220f)
        canvas.drawRoundRect(headerRect, 30f, 30f, headerPaint)
        canvas.drawRect(40f, 190f, width - 40f, 220f, headerPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 42f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(shopName.ifBlank { "মাওয়া স্মার্ট খাতা" }, width / 2f, 110f, titlePaint)

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("দৈনিক ক্যাশবক্স হিসাব ও মিলানোর রসিদ · $dateLabel", width / 2f, 165f, subtitlePaint)

        // 2 Column Header: পেলাম (Green) vs দিলাম (Red)
        val currentY = 260f
        val colWidth = (width - 160f) / 2f

        // Left Inflow Box (পেলাম)
        val inBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#ECFDF5") }
        canvas.drawRoundRect(RectF(70f, currentY, 70f + colWidth, currentY + 540f), 16f, 16f, inBg)

        val inHeader = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#065F46")
            textSize = 26f
            isFakeBoldText = true
        }
        canvas.drawText("★ ক্যাশ আগমন (পেলাম)", 90f, currentY + 45f, inHeader)

        val textRegular = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1F2937"); textSize = 22f }
        val textGreen = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#059669"); textSize = 22f; isFakeBoldText = true }

        var inY = currentY + 100f
        canvas.drawText("দিনের শুরুতে ক্যাশ:", 90f, inY, textRegular)
        canvas.drawText(BengaliUtils.formatTaka(openingCash), 90f, inY + 30f, textGreen)

        inY += 80f
        canvas.drawText("নগদ বেচা:", 90f, inY, textRegular)
        canvas.drawText(BengaliUtils.formatTaka(cashSales), 90f, inY + 30f, textGreen)

        inY += 80f
        canvas.drawText("বাকি আদায়:", 90f, inY, textRegular)
        canvas.drawText(BengaliUtils.formatTaka(bakiCollection), 90f, inY + 30f, textGreen)

        inY += 80f
        canvas.drawText("মালিক দিল (জমা):", 90f, inY, textRegular)
        canvas.drawText(BengaliUtils.formatTaka(ownerDeposit), 90f, inY + 30f, textGreen)

        val totalIn = openingCash + cashSales + bakiCollection + ownerDeposit
        inY += 80f
        val linePaintGreen = Paint().apply { color = Color.parseColor("#A7F3D0"); strokeWidth = 2f }
        canvas.drawLine(90f, inY, 70f + colWidth - 20f, inY, linePaintGreen)
        inY += 35f
        canvas.drawText("মোট আগমন: ${BengaliUtils.formatTaka(totalIn)}", 90f, inY, inHeader)

        // Right Outflow Box (দিলাম)
        val outX = 90f + colWidth
        val outBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FEF2F2") }
        canvas.drawRoundRect(RectF(outX, currentY, outX + colWidth, currentY + 540f), 16f, 16f, outBg)

        val outHeader = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#991B1B")
            textSize = 26f
            isFakeBoldText = true
        }
        canvas.drawText("★ ক্যাশ প্রদান (দিলাম)", outX + 20f, currentY + 45f, outHeader)

        val textRed = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#DC2626"); textSize = 22f; isFakeBoldText = true }

        var outY = currentY + 100f
        canvas.drawText("ক্যাশ মাল কেনা:", outX + 20f, outY, textRegular)
        canvas.drawText(BengaliUtils.formatTaka(purchases), outX + 20f, outY + 30f, textRed)

        outY += 80f
        canvas.drawText("দোকানের খরচ:", outX + 20f, outY, textRegular)
        canvas.drawText(BengaliUtils.formatTaka(expenses), outX + 20f, outY + 30f, textRed)

        outY += 80f
        canvas.drawText("মালিক নিল (সংসার):", outX + 20f, outY, textRegular)
        canvas.drawText(BengaliUtils.formatTaka(ownerWithdrawal), outX + 20f, outY + 30f, textRed)

        val totalOut = purchases + expenses + ownerWithdrawal
        outY += 160f
        val linePaintRed = Paint().apply { color = Color.parseColor("#FECACA"); strokeWidth = 2f }
        canvas.drawLine(outX + 20f, outY, outX + colWidth - 20f, outY, linePaintRed)
        outY += 35f
        canvas.drawText("মোট প্রদান: ${BengaliUtils.formatTaka(totalOut)}", outX + 20f, outY, outHeader)

        // Closing Balance Grand Card
        val closeY = currentY + 570f
        val closeBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E3A8A") }
        canvas.drawRoundRect(RectF(70f, closeY, width - 70f, closeY + 160f), 24f, 24f, closeBg)

        val closeLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#93C5FD")
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        val closeVal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 58f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("দিনের শেষ ক্যাশবক্স ব্যালেন্স (ক্যাশ ইন হ্যান্ড)", width / 2f, closeY + 55f, closeLabel)
        canvas.drawText(BengaliUtils.formatTaka(closingBalance), width / 2f, closeY + 125f, closeVal)

        // Footer Branding
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("MAWA ডিজিটাল ক্যাশ ও খতিয়ান · বিশ্বস্ত আধুনিক হিসাব ব্যবস্থা", width / 2f, height - 60f, footerPaint)

        return bitmap
    }

    fun createMonthlyReportMemoBitmap(
        shopName: String,
        periodLabel: String,
        totalSales: Double,
        purchases: Double,
        shopExpenses: Double,
        netProfit: Double,
        homeWithdrawals: Double,
        profitRemaining: Double
    ): Bitmap {
        val width = 900
        val height = 1200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply { color = Color.parseColor("#F8F9FD") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Card Container
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val cardRect = RectF(40f, 40f, width - 40f, height - 40f)
        canvas.drawRoundRect(cardRect, 30f, 30f, cardPaint)

        // Header Banner
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E1B4B") }
        val headerRect = RectF(40f, 40f, width - 40f, 220f)
        canvas.drawRoundRect(headerRect, 30f, 30f, headerPaint)
        canvas.drawRect(40f, 190f, width - 40f, 220f, headerPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 44f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(shopName.ifBlank { "মাওয়া স্মার্ট খাতা" }, width / 2f, 110f, titlePaint)

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C7D2FE")
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("লাভ-ক্ষতি ও ব্যবসা কর্মক্ষমতা স্টেটমেন্ট · $periodLabel", width / 2f, 165f, subtitlePaint)

        // Net Profit Highlight Card
        val profitBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (netProfit >= 0) Color.parseColor("#16A34A") else Color.parseColor("#DC2626")
        }
        val profitRect = RectF(70f, 250f, width - 70f, 390f)
        canvas.drawRoundRect(profitRect, 24f, 24f, profitBg)

        val pLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 24f; textAlign = Paint.Align.CENTER }
        val pValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 54f; isFakeBoldText = true; textAlign = Paint.Align.CENTER }
        canvas.drawText("ব্যবসায়িক নিট লাভ", width / 2f, 295f, pLabelPaint)
        canvas.drawText(BengaliUtils.formatTaka(netProfit), width / 2f, 355f, pValPaint)

        // Detailed Table Rows
        var currentY = 430f
        val textDark = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0F172A"); textSize = 26f }
        val textVal = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0F172A"); textSize = 26f; isFakeBoldText = true; textAlign = Paint.Align.RIGHT }
        val linePaint = Paint().apply { color = Color.parseColor("#E2E8F0"); strokeWidth = 1.5f }

        fun drawRow(label: String, amount: Double, colorHex: String, prefix: String = "") {
            canvas.drawText(label, 80f, currentY + 30f, textDark)
            val valPaint = Paint(textVal).apply { color = Color.parseColor(colorHex) }
            canvas.drawText("$prefix${BengaliUtils.formatTaka(amount)}", width - 80f, currentY + 30f, valPaint)
            currentY += 60f
            canvas.drawLine(80f, currentY, width - 80f, currentY, linePaint)
            currentY += 20f
        }

        drawRow("মোট বিক্রি (নগদ + বাকি)", totalSales, "#0F172A")
        drawRow("মোট মাল কেনা (ফর্দ + ডিলার)", purchases, "#DC2626", "−")
        drawRow("দোকানের পরিচালনা খরচ", shopExpenses, "#DC2626", "−")
        drawRow("বাড়ির জন্য নেওয়া (সংসার উত্তোলন)", homeWithdrawals, "#D97706", "−")

        // Final Retained Profit Box
        val remBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F1F5F9") }
        val remRect = RectF(70f, currentY + 10f, width - 70f, currentY + 130f)
        canvas.drawRoundRect(remRect, 20f, 20f, remBg)

        val remLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#475569"); textSize = 24f }
        val remVal = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1B5E20"); textSize = 34f; isFakeBoldText = true; textAlign = Paint.Align.RIGHT }
        canvas.drawText("বাড়ির খরচ বাদ দিয়ে অবশিষ্ট নিট মুনাফা:", 90f, currentY + 75f, remLabel)
        canvas.drawText(BengaliUtils.formatTaka(profitRemaining), width - 90f, currentY + 75f, remVal)

        // Footer Branding
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("MAWA ডিজিটাল খাতা ও হিসাব রিপোর্ট · নিরাপদ ও স্বয়ংক্রিয় ব্যাকআপ সমর্থিত", width / 2f, height - 60f, footerPaint)

        return bitmap
    }

    fun createFordiMemoBitmap(
        shopName: String,
        dateLabel: String,
        fordiItems: List<FordiItemEntity>
    ): Bitmap {
        return createFordiMemoBitmap(shopName = shopName, items = fordiItems, dateLabel = dateLabel)
    }

    fun createFordiMemoBitmap(
        shopName: String,
        items: List<FordiItemEntity>,
        dateLabel: String = BengaliUtils.formatDateForExport(System.currentTimeMillis())
    ): Bitmap {
        val fordiItems = items
        val width = 900
        val height = 1200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply { color = Color.parseColor("#F8F9FD") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Card Container
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val cardRect = RectF(40f, 40f, width - 40f, height - 40f)
        canvas.drawRoundRect(cardRect, 30f, 30f, cardPaint)

        // Header Banner
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#047857") }
        val headerRect = RectF(40f, 40f, width - 40f, 200f)
        canvas.drawRoundRect(headerRect, 30f, 30f, headerPaint)
        canvas.drawRect(40f, 170f, width - 40f, 200f, headerPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 44f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(shopName.ifBlank { "মাওয়া স্মার্ট খাতা" }, width / 2f, 100f, titlePaint)

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A7F3D0")
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("দোকানের বাজার ফর্দ ও মহাজন ক্রয় তালিকা · $dateLabel", width / 2f, 150f, subtitlePaint)

        // Table Header
        var currentY = 240f
        val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E2E8F0") }
        canvas.drawRoundRect(RectF(70f, currentY, width - 70f, currentY + 50f), 10f, 10f, tableHeaderPaint)

        val thText = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#334155"); textSize = 22f; isFakeBoldText = true }
        canvas.drawText("নং", 90f, currentY + 34f, thText)
        canvas.drawText("পণ্যের নাম", 150f, currentY + 34f, thText)
        canvas.drawText("পরিমাণ", width - 380f, currentY + 34f, thText)
        canvas.drawText("দর", width - 260f, currentY + 34f, thText)
        canvas.drawText("মোট (৳)", width - 150f, currentY + 34f, thText)

        currentY += 75f
        var totalAmount = 0.0

        for ((index, item) in fordiItems.take(12).withIndex()) {
            val itemTotal = if (item.isPurchased) item.actualTotal else (item.plannedQuantity * item.purchaseRate)
            totalAmount += itemTotal

            val textItem = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E293B"); textSize = 22f }
            val numStr = BengaliUtils.toBengaliDigits((index + 1).toString())
            canvas.drawText(numStr, 90f, currentY, textItem)
            canvas.drawText(item.productName, 150f, currentY, textItem)
            canvas.drawText("${BengaliUtils.formatNumber(item.plannedQuantity)} ${item.unit}", width - 380f, currentY, textItem)
            canvas.drawText(BengaliUtils.formatNumber(item.purchaseRate), width - 260f, currentY, textItem)

            val amtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#047857"); textSize = 22f; isFakeBoldText = true }
            canvas.drawText(BengaliUtils.formatNumber(itemTotal), width - 150f, currentY, amtPaint)

            currentY += 50f
            val linePaint = Paint().apply { color = Color.parseColor("#F1F5F9"); strokeWidth = 1.5f }
            canvas.drawLine(70f, currentY - 15f, width - 70f, currentY - 15f, linePaint)
        }

        // Total Footer Card
        val sumBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#ECFDF5") }
        val sumRect = RectF(70f, height - 200f, width - 70f, height - 100f)
        canvas.drawRoundRect(sumRect, 16f, 16f, sumBoxPaint)

        val sumText = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#065F46"); textSize = 26f; isFakeBoldText = true }
        canvas.drawText("ফর্দের সর্বমোট সম্ভাব্য কেনা খরচ:", 95f, height - 145f, sumText)
        val sumVal = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#047857"); textSize = 36f; isFakeBoldText = true; textAlign = Paint.Align.RIGHT }
        canvas.drawText(BengaliUtils.formatTaka(totalAmount), width - 95f, height - 145f, sumVal)

        // Footer Branding
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("MAWA ডিজিটাল খাতা · ফর্দ তালিকা প্রিন্ট ও শেয়ার রসিদ", width / 2f, height - 50f, footerPaint)

        return bitmap
    }

    // ==========================================
    // 4. FILE SHARING & IMAGE SAVING ENGINE
    // ==========================================

    fun shareBitmapAsImage(context: Context, bitmap: Bitmap, fileNamePrefix: String, title: String) {
        try {
            val cacheDir = File(context.cacheDir, "images")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val file = File(cacheDir, "${fileNamePrefix}_${System.currentTimeMillis()}.png")
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, title)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "ছবি শেয়ার করতে সমস্যা হয়েছে: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun shareTextFile(context: Context, content: String, fileName: String, mimeType: String, title: String) {
        try {
            val cacheDir = File(context.cacheDir, "reports")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val file = File(cacheDir, fileName)
            file.writeText(content)

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, content)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, title)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "ফাইল শেয়ার ব্যর্থ হয়েছে: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // ==========================================
    // 5. WHATSAPP & SMS QUICK REMINDER
    // ==========================================

    fun generateBengaliPaymentReminderText(
        shopName: String,
        customerName: String,
        balance: Double,
        shopPhone: String
    ): String {
        return """
শ্রদ্ধেয় $customerName ভাই,
$shopName-এ আপনার বকেয়া বাকি মোট: ${BengaliUtils.formatTaka(balance)}।

হিসাবের স্বচ্ছতা বজায় রাখতে অনুগ্রহ করে আপনার সুবিধাজনক সময়ে বকেয়া টাকা পরিশোধের ব্যবস্থা করবেন।
${if (shopPhone.isNotBlank()) "যেকোনো তথ্যে যোগাযোগ: $shopPhone" else ""}

ধন্যবাদান্তে,
$shopName
        """.trimIndent()
    }

    fun sendWhatsAppMessage(context: Context, phone: String, message: String) {
        try {
            val cleanPhone = phone.replace("[^0-9+]".toRegex(), "")
            val finalPhone = when {
                cleanPhone.startsWith("+88") -> cleanPhone.substring(1)
                cleanPhone.startsWith("01") -> "88$cleanPhone"
                else -> cleanPhone
            }

            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$finalPhone&text=${Uri.encode(message)}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            shareText(context, message, "বাকি তাগাদা বার্তা")
        }
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
