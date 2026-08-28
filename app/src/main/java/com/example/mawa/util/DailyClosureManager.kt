package com.example.mawa.util

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DailyClosureRecord(
    val dateKey: String,
    val closedTimestamp: Long = System.currentTimeMillis(),
    val openingBalance: Double = 0.0,
    val totalCashSales: Double = 0.0,
    val totalBakiSales: Double = 0.0,
    val totalBakiCollection: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val expectedCashInHand: Double = 0.0,
    val actualPhysicalCash: Double = 0.0,
    val discrepancy: Double = 0.0,
    val note: String = "",
    val isLocked: Boolean = true
)

object DailyClosureManager {

    private const val PREFS_NAME = "mawa_daily_closures_prefs"
    private const val KEY_PREFIX = "closure_"
    private const val PREF_REMINDER_ENABLED = "daily_closing_reminder_enabled"
    private const val PREF_REMINDER_HOUR = "daily_closing_reminder_hour"
    private const val PREF_REMINDER_MINUTE = "daily_closing_reminder_minute"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getDateKey(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(timestamp))
    }

    fun isDayLocked(context: Context, timestamp: Long): Boolean {
        val record = getClosureRecord(context, getDateKey(timestamp))
        return record?.isLocked == true
    }

    fun getClosureRecord(context: Context, dateKey: String): DailyClosureRecord? {
        val jsonStr = getPrefs(context).getString(KEY_PREFIX + dateKey, null) ?: return null
        return try {
            val obj = JSONObject(jsonStr)
            DailyClosureRecord(
                dateKey = obj.optString("dateKey", dateKey),
                closedTimestamp = obj.optLong("closedTimestamp", System.currentTimeMillis()),
                openingBalance = obj.optDouble("openingBalance", 0.0),
                totalCashSales = obj.optDouble("totalCashSales", 0.0),
                totalBakiSales = obj.optDouble("totalBakiSales", 0.0),
                totalBakiCollection = obj.optDouble("totalBakiCollection", 0.0),
                totalExpenses = obj.optDouble("totalExpenses", 0.0),
                totalPurchases = obj.optDouble("totalPurchases", 0.0),
                expectedCashInHand = obj.optDouble("expectedCashInHand", 0.0),
                actualPhysicalCash = obj.optDouble("actualPhysicalCash", 0.0),
                discrepancy = obj.optDouble("discrepancy", 0.0),
                note = obj.optString("note", ""),
                isLocked = obj.optBoolean("isLocked", true)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun saveClosureRecord(context: Context, record: DailyClosureRecord) {
        val obj = JSONObject().apply {
            put("dateKey", record.dateKey)
            put("closedTimestamp", record.closedTimestamp)
            put("openingBalance", record.openingBalance)
            put("totalCashSales", record.totalCashSales)
            put("totalBakiSales", record.totalBakiSales)
            put("totalBakiCollection", record.totalBakiCollection)
            put("totalExpenses", record.totalExpenses)
            put("totalPurchases", record.totalPurchases)
            put("expectedCashInHand", record.expectedCashInHand)
            put("actualPhysicalCash", record.actualPhysicalCash)
            put("discrepancy", record.discrepancy)
            put("note", record.note)
            put("isLocked", record.isLocked)
        }

        getPrefs(context).edit().putString(KEY_PREFIX + record.dateKey, obj.toString()).apply()
    }

    fun unlockDay(context: Context, dateKey: String) {
        val record = getClosureRecord(context, dateKey)
        if (record != null) {
            saveClosureRecord(context, record.copy(isLocked = false))
        }
    }

    fun isReminderEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(PREF_REMINDER_ENABLED, true)
    }

    fun setReminderEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(PREF_REMINDER_ENABLED, enabled).apply()
    }

    fun getReminderTime(context: Context): Pair<Int, Int> {
        val hour = getPrefs(context).getInt(PREF_REMINDER_HOUR, 22) // default 10 PM (22:00)
        val minute = getPrefs(context).getInt(PREF_REMINDER_MINUTE, 0)
        return Pair(hour, minute)
    }

    fun setReminderTime(context: Context, hour: Int, minute: Int) {
        getPrefs(context).edit()
            .putInt(PREF_REMINDER_HOUR, hour)
            .putInt(PREF_REMINDER_MINUTE, minute)
            .apply()
    }
}
