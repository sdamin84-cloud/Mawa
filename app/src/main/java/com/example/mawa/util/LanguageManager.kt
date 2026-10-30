package com.example.mawa.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val labelBn: String, val labelEn: String) {
    BANGLA("bn", "বাংলা", "Bangla"),
    ENGLISH("en", "English", "English");

    val isEnglish: Boolean get() = this == ENGLISH
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.BANGLA }

object LanguageManager {
    private const val PREFS_NAME = "mawa_app_prefs"
    private const val KEY_LANGUAGE = "app_language"

    private val _currentLanguage = MutableStateFlow(AppLanguage.BANGLA)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    val isEnglish: Boolean get() = _currentLanguage.value == AppLanguage.ENGLISH

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedCode = prefs.getString(KEY_LANGUAGE, AppLanguage.BANGLA.code)
        _currentLanguage.value = if (savedCode == AppLanguage.ENGLISH.code) AppLanguage.ENGLISH else AppLanguage.BANGLA
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        _currentLanguage.value = language
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    fun toggleLanguage(context: Context): AppLanguage {
        val next = if (_currentLanguage.value == AppLanguage.BANGLA) AppLanguage.ENGLISH else AppLanguage.BANGLA
        setLanguage(context, next)
        return next
    }
}

/**
 * Quick composable translator helper
 */
@Composable
fun tr(bn: String, en: String): String {
    return if (LocalAppLanguage.current.isEnglish) en else bn
}

/**
 * Static translator helper when Composable context is unavailable
 */
object AppStrings {
    fun tr(bn: String, en: String, isEnglish: Boolean = LanguageManager.isEnglish): String {
        return if (isEnglish) en else bn
    }
}
