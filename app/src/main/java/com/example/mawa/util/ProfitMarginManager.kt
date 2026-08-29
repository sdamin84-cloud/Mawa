package com.example.mawa.util

import android.content.Context
import android.content.SharedPreferences

object ProfitMarginManager {
    private const val PREFS_NAME = "mawa_profit_settings_prefs"
    private const val KEY_PROFIT_MARGIN_PERCENT = "profit_margin_percent"
    const val DEFAULT_PROFIT_MARGIN = 10.0 // ডিফল্ট ১০% শতকরা লাভ

    val PRESET_MARGINS = listOf(5.0, 8.0, 10.0, 12.0, 15.0, 20.0)

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getProfitMargin(context: Context): Double {
        val prefs = getPrefs(context)
        val value = prefs.getFloat(KEY_PROFIT_MARGIN_PERCENT, DEFAULT_PROFIT_MARGIN.toFloat())
        return if (value > 0) value.toDouble() else DEFAULT_PROFIT_MARGIN
    }

    fun setProfitMargin(context: Context, percentage: Double) {
        val valid = if (percentage > 0) percentage else DEFAULT_PROFIT_MARGIN
        getPrefs(context).edit().putFloat(KEY_PROFIT_MARGIN_PERCENT, valid.toFloat()).apply()
    }

    fun calculateProfit(turnoverOrSales: Double, marginPercentage: Double): Double {
        if (turnoverOrSales <= 0 || marginPercentage <= 0) return 0.0
        return turnoverOrSales * (marginPercentage / 100.0)
    }
}
