package com.example.mawa.util

import android.content.Context
import com.example.mawa.data.local.entity.FordiItemEntity
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class FordiTemplateItem(
    val productName: String,
    val plannedQuantity: Double,
    val unit: String,
    val purchaseRate: Double = 0.0
)

data class FordiTemplate(
    val id: String,
    val name: String,
    val createdAt: Long,
    val isDefault: Boolean = false,
    val items: List<FordiTemplateItem>
)

object FordiTemplateManager {

    private const val PREF_NAME = "mawa_fordi_templates_pref"
    private const val KEY_TEMPLATES = "saved_templates_json"

    val DEFAULT_TEMPLATES = listOf(
        FordiTemplate(
            id = "preset_kachabazar",
            name = "দৈনিক কাঁচাবাজার",
            createdAt = 1700000000000L,
            isDefault = true,
            items = listOf(
                FordiTemplateItem("গোল আলু (নতুন)", 2.0, "কেজি", 45.0),
                FordiTemplateItem("দেশি পেঁয়াজ", 1.0, "কেজি", 85.0),
                FordiTemplateItem("কাঁচা মরিচ", 250.0, "গ্রাম", 35.0),
                FordiTemplateItem("রসুন", 500.0, "গ্রাম", 110.0),
                FordiTemplateItem("আদা", 250.0, "গ্রাম", 60.0),
                FordiTemplateItem("টমেটো", 1.0, "কেজি", 70.0)
            )
        ),
        FordiTemplate(
            id = "preset_weekly_grocery",
            name = "সাপ্তাহিক মুদি বাজার",
            createdAt = 1700000001000L,
            isDefault = true,
            items = listOf(
                FordiTemplateItem("মিনিকেট চাল", 10.0, "কেজি", 72.0),
                FordiTemplateItem("সয়াবিন তেল (রূপচাঁদা)", 2.0, "লিটার", 185.0),
                FordiTemplateItem("দেশি মসুর ডাল", 1.0, "কেজি", 130.0),
                FordiTemplateItem("সাদা চিনি", 1.0, "কেজি", 135.0),
                FordiTemplateItem("আয়োডিনযুক্ত লবণ", 1.0, "প্যাকেট", 42.0),
                FordiTemplateItem("ডিম", 12.0, "টি", 13.0),
                FordiTemplateItem("ইস্পাহানি মির্জাপুর চা", 1.0, "প্যাকেট", 230.0),
                FordiTemplateItem("মার্কস গুঁড়ো দুধ (৫০০ গ্রাম)", 1.0, "প্যাকেট", 440.0)
            )
        ),
        FordiTemplate(
            id = "preset_monthly_family",
            name = "মাসিক পারিবারিক বাজার",
            createdAt = 1700000002000L,
            isDefault = true,
            items = listOf(
                FordiTemplateItem("নাজিরশাইল চাল (৫০ কেজি বস্তা)", 1.0, "বস্তা", 3600.0),
                FordiTemplateItem("সয়াবিন তেল (৫ লিটার জার)", 1.0, "বোতল", 890.0),
                FordiTemplateItem("মসুর ডাল (বড় দানা)", 3.0, "কেজি", 125.0),
                FordiTemplateItem("আটা (২ কেজি প্যাকেট)", 2.0, "প্যাকেট", 115.0),
                FordiTemplateItem("লবণ", 2.0, "প্যাকেট", 40.0),
                FordiTemplateItem("হুইল ডিটারজেন্ট পাউডার", 1.0, "প্যাকেট", 160.0),
                FordiTemplateItem("লাইফবয় সাবান (৪ প্যাক)", 1.0, "প্যাকেট", 190.0)
            )
        ),
        FordiTemplate(
            id = "preset_restaurant_supply",
            name = "হোটেল ও রেস্তোরাঁ সাপ্লাই",
            createdAt = 1700000003000L,
            isDefault = true,
            items = listOf(
                FordiTemplateItem("চিনিগুঁড়া পোলাও চাল", 10.0, "কেজি", 140.0),
                FordiTemplateItem("ব্রয়লার মুরগি", 5.0, "কেজি", 195.0),
                FordiTemplateItem("ফার্মের লাল ডিম (১০০ পিস)", 1.0, "বক্স", 1250.0),
                FordiTemplateItem("সয়াবিন তেল (১৬ লিটার ড্রাম)", 1.0, "ড্রাম", 2700.0),
                FordiTemplateItem("এলাচ ও গরম মসলা সেট", 1.0, "প্যাকেট", 350.0),
                FordiTemplateItem("পেঁয়াজ (পাকা)", 10.0, "কেজি", 80.0)
            )
        )
    )

    fun getAllTemplates(context: Context): List<FordiTemplate> {
        val userTemplates = getUserTemplates(context)
        return userTemplates + DEFAULT_TEMPLATES
    }

    fun saveCurrentFordiAsTemplate(
        context: Context,
        templateName: String,
        items: List<FordiItemEntity>
    ): FordiTemplate {
        val cleanName = templateName.trim().ifBlank { "আমার ফর্দ টেমপ্লেট" }
        val templateItems = items.map {
            FordiTemplateItem(
                productName = it.productName,
                plannedQuantity = if (it.plannedQuantity > 0) it.plannedQuantity else 1.0,
                unit = it.unit.ifBlank { "টি" },
                purchaseRate = it.purchaseRate
            )
        }

        val newTemplate = FordiTemplate(
            id = UUID.randomUUID().toString(),
            name = cleanName,
            createdAt = System.currentTimeMillis(),
            isDefault = false,
            items = templateItems
        )

        val currentList = getUserTemplates(context).toMutableList()
        currentList.add(0, newTemplate)
        saveUserTemplates(context, currentList)

        return newTemplate
    }

    fun deleteTemplate(context: Context, templateId: String) {
        val currentList = getUserTemplates(context).toMutableList()
        currentList.removeAll { it.id == templateId }
        saveUserTemplates(context, currentList)
    }

    private fun getUserTemplates(context: Context): List<FordiTemplate> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_TEMPLATES, null) ?: return emptyList()

        return try {
            val jsonArray = JSONArray(jsonStr)
            val result = mutableListOf<FordiTemplate>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                val itemsArray = obj.getJSONArray("items")
                val items = mutableListOf<FordiTemplateItem>()
                for (j in 0 until itemsArray.length()) {
                    val itObj = itemsArray.getJSONObject(j)
                    items.add(
                        FordiTemplateItem(
                            productName = itObj.getString("productName"),
                            plannedQuantity = itObj.optDouble("plannedQuantity", 1.0),
                            unit = itObj.optString("unit", "টি"),
                            purchaseRate = itObj.optDouble("purchaseRate", 0.0)
                        )
                    )
                }
                result.add(FordiTemplate(id = id, name = name, createdAt = createdAt, isDefault = false, items = items))
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveUserTemplates(context: Context, templates: List<FordiTemplate>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        for (t in templates) {
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("name", t.name)
            obj.put("createdAt", t.createdAt)
            val itemsArray = JSONArray()
            for (it in t.items) {
                val itObj = JSONObject()
                itObj.put("productName", it.productName)
                itObj.put("plannedQuantity", it.plannedQuantity)
                itObj.put("unit", it.unit)
                itObj.put("purchaseRate", it.purchaseRate)
                itemsArray.put(itObj)
            }
            obj.put("items", itemsArray)
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_TEMPLATES, jsonArray.toString()).apply()
    }
}
