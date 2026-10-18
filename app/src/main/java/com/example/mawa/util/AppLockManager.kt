package com.example.mawa.util

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

/**
 * AppLockManager:
 * Manages 4-digit secret PIN protection for MAWA Retail Finance App.
 * Secures confidential shop cash, profits, customer ledgers, and transactions.
 * Features:
 * - SHA-256 hashed PIN storage
 * - Session lock / unlock state
 * - Emergency recovery via Shopkeeper Mobile Number or Security Question
 */
object AppLockManager {

    private const val PREFS_NAME = "mawa_security_lock_prefs"
    private const val KEY_PIN_HASH = "key_pin_sha256"
    private const val KEY_LOCK_ENABLED = "key_is_app_lock_enabled"
    private const val KEY_RECOVERY_PHONE = "key_recovery_phone"
    private const val KEY_SECURITY_QUESTION = "key_security_question"
    private const val KEY_SECURITY_ANSWER_HASH = "key_security_answer_hash"
    private const val KEY_AUTO_LOCK_BACKGROUND = "key_auto_lock_background"

    // In-memory session state: whether app has been unlocked during current run
    var isUnlockedInSession: Boolean = false
        private set

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.trim().toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Checks if PIN has been set
     */
    fun isPinSet(context: Context): Boolean {
        val hash = getPrefs(context).getString(KEY_PIN_HASH, null)
        return !hash.isNullOrBlank()
    }

    /**
     * Checks if App PIN Lock is currently enabled
     */
    fun isLockEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_LOCK_ENABLED, false) && isPinSet(context)
    }

    /**
     * Sets App Lock enabled / disabled
     */
    fun setLockEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_LOCK_ENABLED, enabled).apply()
        if (enabled) {
            isUnlockedInSession = true // Don't immediately lock on turning on
        }
    }

    /**
     * Saves new 4-digit PIN with optional recovery phone
     */
    fun savePin(context: Context, pin: String, recoveryPhone: String = "", question: String = "", answer: String = "") {
        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            throw IllegalArgumentException("PIN must be exactly 4 digits")
        }
        val editor = getPrefs(context).edit()
            .putString(KEY_PIN_HASH, sha256(pin))
            .putBoolean(KEY_LOCK_ENABLED, true)

        if (recoveryPhone.isNotBlank()) {
            editor.putString(KEY_RECOVERY_PHONE, recoveryPhone.trim())
        }
        if (question.isNotBlank() && answer.isNotBlank()) {
            editor.putString(KEY_SECURITY_QUESTION, question.trim())
            editor.putString(KEY_SECURITY_ANSWER_HASH, sha256(answer.lowercase().trim()))
        }
        editor.apply()
        isUnlockedInSession = true
    }

    /**
     * Verifies entered PIN against stored hash
     */
    fun verifyPin(context: Context, enteredPin: String): Boolean {
        val storedHash = getPrefs(context).getString(KEY_PIN_HASH, null) ?: return false
        val matches = storedHash == sha256(enteredPin.trim())
        if (matches) {
            isUnlockedInSession = true
        }
        return matches
    }

    /**
     * Manually locks the app now
     */
    fun lockNow() {
        isUnlockedInSession = false
    }

    /**
     * Marks session as unlocked (e.g. after successful pin entry)
     */
    fun setUnlocked() {
        isUnlockedInSession = true
    }

    /**
     * Removes PIN and disables lock
     */
    fun removePin(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_LOCK_ENABLED, false)
            .apply()
        isUnlockedInSession = true
    }

    /**
     * Retrieves recovery phone
     */
    fun getRecoveryPhone(context: Context): String {
        return getPrefs(context).getString(KEY_RECOVERY_PHONE, "") ?: ""
    }

    /**
     * Retrieves security question
     */
    fun getSecurityQuestion(context: Context): String {
        return getPrefs(context).getString(KEY_SECURITY_QUESTION, "দোকানের মালিকের মোবাইল নম্বর কোনটি?") ?: ""
    }

    /**
     * Verifies security recovery (by recovery phone or security answer)
     */
    fun verifyRecovery(context: Context, input: String): Boolean {
        val cleanInput = input.trim()
        if (cleanInput.isBlank()) return false

        // Check recovery phone
        val phone = getRecoveryPhone(context)
        if (phone.isNotBlank()) {
            val normalizedPhone = phone.replace("-", "").replace(" ", "").trim()
            val normalizedInput = cleanInput.replace("-", "").replace(" ", "").trim()
            if (normalizedPhone.endsWith(normalizedInput) || normalizedInput.endsWith(normalizedPhone)) {
                return true
            }
        }

        // Check security answer
        val answerHash = getPrefs(context).getString(KEY_SECURITY_ANSWER_HASH, null)
        if (answerHash != null && answerHash == sha256(cleanInput.lowercase())) {
            return true
        }

        return false
    }

    /**
     * Emergency PIN reset using recovery credential
     */
    fun resetPin(context: Context, newPin: String) {
        if (newPin.length != 4 || !newPin.all { it.isDigit() }) {
            throw IllegalArgumentException("PIN must be exactly 4 digits")
        }
        getPrefs(context).edit()
            .putString(KEY_PIN_HASH, sha256(newPin))
            .putBoolean(KEY_LOCK_ENABLED, true)
            .apply()
        isUnlockedInSession = true
    }
}
