package com.example.mawa.util

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class FordiQuickProduct(
    val productName: String,
    val unit: String = "কেজি",
    val defaultRate: Double = 0.0,
    val productId: Long? = null,
    val usageCount: Int = 1
)

object FordiSuggestionManager {

    private const val PREF_NAME = "mawa_fordi_suggestions_pref"
    private const val KEY_HISTORY = "history_items_json"

    // Default Bengali grocery essentials (matches screenshot items exactly)
    val DEFAULT_QUICK_ITEMS = listOf(
        FordiQuickProduct("কাঁচা মরিচ", "গ্রাম", 35.0),
        FordiQuickProduct("গোল আলু (নতুন)", "কেজি", 45.0),
        FordiQuickProduct("মার্কস গুঁড়ো দুধ (৫০০ গ্রাম)", "প্যাকেট", 440.0),
        FordiQuickProduct("দেশি পেঁয়াজ", "কেজি", 85.0),
        FordiQuickProduct("ইস্পাহানি মির্জাপুর চা (৪০০ গ্রাম)", "প্যাকেট", 230.0),
        FordiQuickProduct("সয়াবিন তেল (১ লিটার)", "লিটার", 185.0),
        FordiQuickProduct("দেশি মসুর ডাল", "কেজি", 130.0),
        FordiQuickProduct("সাদা চিনি", "কেজি", 135.0),
        FordiQuickProduct("ফার্মের ডিম (১ ডজন)", "টি", 150.0),
        FordiQuickProduct("আয়োডিনযুক্ত লবণ", "প্যাকেট", 42.0),
        FordiQuickProduct("রসুন", "গ্রাম", 110.0),
        FordiQuickProduct("আদা", "গ্রাম", 60.0),
        FordiQuickProduct("টমেটো", "কেজি", 70.0),
        FordiQuickProduct("মিনিকেট চাল", "কেজি", 72.0),
        FordiQuickProduct("হলুদ গুঁড়া", "প্যাকেট", 65.0),
        FordiQuickProduct("মরিচ গুঁড়া", "প্যাকেট", 75.0),
        FordiQuickProduct("লাইফবয় সাবান", "পিস", 50.0)
    )

    fun recordItemAdded(
        context: Context,
        productName: String,
        unit: String,
        rate: Double,
        productId: Long? = null
    ) {
        val cleanName = productName.trim()
        if (cleanName.isBlank()) return

        val list = getHistory(context).toMutableList()
        val existingIndex = list.indexOfFirst { it.productName.equals(cleanName, ignoreCase = true) }

        if (existingIndex >= 0) {
            val old = list[existingIndex]
            list[existingIndex] = old.copy(
                unit = if (unit.isNotBlank()) unit else old.unit,
                defaultRate = if (rate > 0) rate else old.defaultRate,
                productId = productId ?: old.productId,
                usageCount = old.usageCount + 1
            )
        } else {
            list.add(
                0,
                FordiQuickProduct(
                    productName = cleanName,
                    unit = unit.ifBlank { "কেজি" },
                    defaultRate = rate,
                    productId = productId,
                    usageCount = 1
                )
            )
        }

        // Keep top 60 recent items
        val trimmed = list.take(60)
        saveHistory(context, trimmed)
    }

    /**
     * Get items for horizontal quick bar: combines user's recent history with popular essentials.
     */
    fun getQuickAddSuggestions(context: Context): List<FordiQuickProduct> {
        val history = getHistory(context)
        val combined = (history + DEFAULT_QUICK_ITEMS)
            .distinctBy { it.productName.trim().lowercase() }
        return combined.take(25)
    }

    /**
     * Filters suggestions based on typed query (e.g. typing 'চা' matches 'চা', 'ইস্পাহানি চা', etc.)
     */
    fun filterSuggestions(
        context: Context,
        query: String,
        catalogProducts: List<com.example.mawa.data.local.entity.ProductEntity> = emptyList()
    ): List<FordiQuickProduct> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return emptyList()

        // 1. From catalog products
        val fromCatalog = catalogProducts
            .filter { it.name.lowercase().contains(q) }
            .map {
                FordiQuickProduct(
                    productName = it.name,
                    unit = it.unit.ifBlank { "পিস" },
                    defaultRate = it.defaultPurchasePrice,
                    productId = it.id,
                    usageCount = 5
                )
            }

        // 2. From history & defaults
        val historyAndDefaults = (getHistory(context) + DEFAULT_QUICK_ITEMS)
            .filter { it.productName.lowercase().contains(q) }

        return (fromCatalog + historyAndDefaults)
            .distinctBy { it.productName.trim().lowercase() }
            .take(8)
    }

    private fun getHistory(context: Context): List<FordiQuickProduct> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_HISTORY, null) ?: return emptyList()

        return try {
            val array = JSONArray(jsonStr)
            val result = mutableListOf<FordiQuickProduct>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    FordiQuickProduct(
                        productName = obj.getString("productName"),
                        unit = obj.optString("unit", "কেজি"),
                        defaultRate = obj.optDouble("defaultRate", 0.0),
                        productId = if (obj.has("productId")) obj.optLong("productId") else null,
                        usageCount = obj.optInt("usageCount", 1)
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveHistory(context: Context, items: List<FordiQuickProduct>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("productName", item.productName)
            obj.put("unit", item.unit)
            obj.put("defaultRate", item.defaultRate)
            item.productId?.let { obj.put("productId", it) }
            obj.put("usageCount", item.usageCount)
            array.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }
}
