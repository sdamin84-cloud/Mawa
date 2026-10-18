package com.example.mawa.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

// =========================================================================
// 1. Personal Loan (ধার-দেনা ও হাওলাত)
// =========================================================================

enum class LoanType(val banglaLabel: String) {
    GIVEN("আমি পাবো (ধার দিয়েছি)"),
    TAKEN("আমি দেবো (ধার নিয়েছি)")
}

data class PersonalLoanItem(
    val id: String = UUID.randomUUID().toString(),
    val personName: String,
    val phone: String = "",
    val type: LoanType,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val dateMillis: Long = System.currentTimeMillis(),
    val dueDateMillis: Long = 0L,
    val note: String = "",
    val isSettled: Boolean = false
) {
    val remainingAmount: Double
        get() = (totalAmount - paidAmount).coerceAtLeast(0.0)

    val progressRatio: Float
        get() = if (totalAmount > 0) (paidAmount / totalAmount).toFloat().coerceIn(0f, 1f) else 0f
}

class PersonalLoanManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences("mawa_personal_loans_pref", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _loans = MutableStateFlow<List<PersonalLoanItem>>(emptyList())
    val loans: StateFlow<List<PersonalLoanItem>> = _loans.asStateFlow()

    init {
        loadLoans()
    }

    private fun loadLoans() {
        val json = prefs.getString("loans_list", null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<PersonalLoanItem>>() {}.type
                val list: List<PersonalLoanItem> = gson.fromJson(json, type) ?: emptyList()
                _loans.value = list.sortedByDescending { it.dateMillis }
                return
            } catch (_: Exception) {}
        }
        _loans.value = emptyList()
    }

    private fun saveLoans(list: List<PersonalLoanItem>) {
        _loans.value = list.sortedByDescending { it.dateMillis }
        val json = gson.toJson(list)
        prefs.edit().putString("loans_list", json).apply()
    }

    fun addLoan(
        personName: String,
        phone: String,
        type: LoanType,
        amount: Double,
        dateMillis: Long = System.currentTimeMillis(),
        dueDateMillis: Long = 0L,
        note: String = ""
    ) {
        val newItem = PersonalLoanItem(
            personName = personName.trim(),
            phone = phone.trim(),
            type = type,
            totalAmount = amount,
            paidAmount = 0.0,
            dateMillis = dateMillis,
            dueDateMillis = dueDateMillis,
            note = note.trim(),
            isSettled = false
        )
        saveLoans(_loans.value + newItem)
    }

    fun recordRepayment(id: String, paymentAmount: Double): PersonalLoanItem? {
        var updatedItem: PersonalLoanItem? = null
        val currentList = _loans.value.map { item ->
            if (item.id == id) {
                val newPaid = item.paidAmount + paymentAmount
                val settled = newPaid >= item.totalAmount
                val updated = item.copy(
                    paidAmount = newPaid,
                    isSettled = settled
                )
                updatedItem = updated
                updated
            } else {
                item
            }
        }
        saveLoans(currentList)
        return updatedItem
    }

    fun deleteLoan(id: String) {
        saveLoans(_loans.value.filterNot { it.id == id })
    }

    companion object {
        @Volatile
        private var instance: PersonalLoanManager? = null

        fun getInstance(context: Context): PersonalLoanManager {
            return instance ?: synchronized(this) {
                instance ?: PersonalLoanManager(context).also { instance = it }
            }
        }
    }
}

// =========================================================================
// 2. Household Bazaar List (বাজারের ফর্দ ও চেকলিস্ট)
// =========================================================================

data class BazaarItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val quantity: String = "",
    val estimatedPrice: Double = 0.0,
    val isPurchased: Boolean = false,
    val addedDateMillis: Long = System.currentTimeMillis()
)

class BazaarListManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences("mawa_household_bazaar_pref", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _items = MutableStateFlow<List<BazaarItem>>(emptyList())
    val items: StateFlow<List<BazaarItem>> = _items.asStateFlow()

    init {
        loadItems()
    }

    private fun loadItems() {
        val json = prefs.getString("bazaar_list", null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<BazaarItem>>() {}.type
                val list: List<BazaarItem> = gson.fromJson(json, type) ?: emptyList()
                _items.value = list
                return
            } catch (_: Exception) {}
        }
        _items.value = emptyList()
    }

    private fun saveItems(list: List<BazaarItem>) {
        _items.value = list
        val json = gson.toJson(list)
        prefs.edit().putString("bazaar_list", json).apply()
    }

    fun addItem(name: String, quantity: String = "", estimatedPrice: Double = 0.0) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        val newItem = BazaarItem(
            name = trimmed,
            quantity = quantity.trim(),
            estimatedPrice = estimatedPrice,
            isPurchased = false
        )
        saveItems(_items.value + newItem)
    }

    fun togglePurchased(id: String) {
        val current = _items.value.map { item ->
            if (item.id == id) item.copy(isPurchased = !item.isPurchased) else item
        }
        saveItems(current)
    }

    fun deleteItem(id: String) {
        saveItems(_items.value.filterNot { it.id == id })
    }

    fun clearCompleted() {
        saveItems(_items.value.filterNot { it.isPurchased })
    }

    fun clearAll() {
        saveItems(emptyList())
    }

    companion object {
        @Volatile
        private var instance: BazaarListManager? = null

        fun getInstance(context: Context): BazaarListManager {
            return instance ?: synchronized(this) {
                instance ?: BazaarListManager(context).also { instance = it }
            }
        }
    }
}

// =========================================================================
// 3. Household Recurring Bills (মাসিক গৃহস্থালি বিল ট্র্যাকার)
// =========================================================================

data class HouseholdBillItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val defaultAmount: Double,
    val dueDay: Int = 10,
    val lastPaidMonthYear: String = "", // Format: "MM-yyyy", e.g. "09-2026"
    val note: String = ""
) {
    fun isPaidThisMonth(currentMonthYear: String): Boolean {
        return lastPaidMonthYear == currentMonthYear
    }
}

class HouseholdBillsManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences("mawa_household_bills_pref", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _bills = MutableStateFlow<List<HouseholdBillItem>>(emptyList())
    val bills: StateFlow<List<HouseholdBillItem>> = _bills.asStateFlow()

    init {
        loadBills()
    }

    private fun loadBills() {
        val json = prefs.getString("bills_list", null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<HouseholdBillItem>>() {}.type
                val list: List<HouseholdBillItem> = gson.fromJson(json, type) ?: emptyList()
                _bills.value = list
                return
            } catch (_: Exception) {}
        }
        // Initial defaults for typical Bangladeshi household
        val defaults = listOf(
            HouseholdBillItem(title = "বাসা ভাড়া", defaultAmount = 12000.0, dueDay = 5),
            HouseholdBillItem(title = "বিদ্যুৎ বিল", defaultAmount = 1500.0, dueDay = 15),
            HouseholdBillItem(title = "গ্যাস বিল", defaultAmount = 1080.0, dueDay = 10),
            HouseholdBillItem(title = "ইন্টারনেট ও ওয়াইফাই", defaultAmount = 600.0, dueDay = 10),
            HouseholdBillItem(title = "পানি ও ময়লা বিল", defaultAmount = 450.0, dueDay = 10)
        )
        saveBills(defaults)
    }

    private fun saveBills(list: List<HouseholdBillItem>) {
        _bills.value = list
        val json = gson.toJson(list)
        prefs.edit().putString("bills_list", json).apply()
    }

    fun addBill(title: String, amount: Double, dueDay: Int = 10, note: String = "") {
        val trimmed = title.trim()
        if (trimmed.isBlank()) return
        val item = HouseholdBillItem(
            title = trimmed,
            defaultAmount = amount,
            dueDay = dueDay.coerceIn(1, 31),
            note = note.trim()
        )
        saveBills(_bills.value + item)
    }

    fun markPaid(id: String, monthYear: String) {
        val current = _bills.value.map { bill ->
            if (bill.id == id) bill.copy(lastPaidMonthYear = monthYear) else bill
        }
        saveBills(current)
    }

    fun markUnpaid(id: String) {
        val current = _bills.value.map { bill ->
            if (bill.id == id) bill.copy(lastPaidMonthYear = "") else bill
        }
        saveBills(current)
    }

    fun deleteBill(id: String) {
        saveBills(_bills.value.filterNot { it.id == id })
    }

    companion object {
        @Volatile
        private var instance: HouseholdBillsManager? = null

        fun getInstance(context: Context): HouseholdBillsManager {
            return instance ?: synchronized(this) {
                instance ?: HouseholdBillsManager(context).also { instance = it }
            }
        }

        fun getCurrentMonthYearKey(): String {
            val sdf = SimpleDateFormat("MM-yyyy", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}

// =========================================================================
// 4. Savings Goals (ব্যক্তিগত সঞ্চয় লক্ষ্যমাত্রা)
// =========================================================================

data class SavingsGoalItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val note: String = "",
    val targetDateMillis: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
) {
    val progressRatio: Float
        get() = if (targetAmount > 0) (savedAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f

    val remainingAmount: Double
        get() = (targetAmount - savedAmount).coerceAtLeast(0.0)

    val isCompleted: Boolean
        get() = targetAmount > 0 && savedAmount >= targetAmount
}

class SavingsGoalManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences("mawa_savings_goals_pref", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _goals = MutableStateFlow<List<SavingsGoalItem>>(emptyList())
    val goals: StateFlow<List<SavingsGoalItem>> = _goals.asStateFlow()

    init {
        loadGoals()
    }

    private fun loadGoals() {
        val json = prefs.getString("goals_list", null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<SavingsGoalItem>>() {}.type
                val list: List<SavingsGoalItem> = gson.fromJson(json, type) ?: emptyList()
                _goals.value = list
                return
            } catch (_: Exception) {}
        }
        val defaults = listOf(
            SavingsGoalItem(title = "জরুরি পারিবারিক ফান্ড", targetAmount = 50000.0, savedAmount = 15000.0, note = "জরুরি প্রয়োজনে চিকিৎসার জন্য"),
            SavingsGoalItem(title = "ঈদ বা উৎসব সঞ্চয়", targetAmount = 20000.0, savedAmount = 6000.0, note = "পরিবারের কেনাকাটা")
        )
        saveGoals(defaults)
    }

    private fun saveGoals(list: List<SavingsGoalItem>) {
        _goals.value = list
        val json = gson.toJson(list)
        prefs.edit().putString("goals_list", json).apply()
    }

    fun addGoal(title: String, targetAmount: Double, initialSaved: Double = 0.0, note: String = "", targetDateMillis: Long = 0L) {
        val trimmed = title.trim()
        if (trimmed.isBlank()) return
        val item = SavingsGoalItem(
            title = trimmed,
            targetAmount = targetAmount,
            savedAmount = initialSaved,
            note = note.trim(),
            targetDateMillis = targetDateMillis
        )
        saveGoals(_goals.value + item)
    }

    fun addSavings(id: String, amount: Double): SavingsGoalItem? {
        var updated: SavingsGoalItem? = null
        val current = _goals.value.map { goal ->
            if (goal.id == id) {
                val newSaved = goal.savedAmount + amount
                val newGoal = goal.copy(savedAmount = newSaved)
                updated = newGoal
                newGoal
            } else {
                goal
            }
        }
        saveGoals(current)
        return updated
    }

    fun deleteGoal(id: String) {
        saveGoals(_goals.value.filterNot { it.id == id })
    }

    companion object {
        @Volatile
        private var instance: SavingsGoalManager? = null

        fun getInstance(context: Context): SavingsGoalManager {
            return instance ?: synchronized(this) {
                instance ?: SavingsGoalManager(context).also { instance = it }
            }
        }
    }
}
