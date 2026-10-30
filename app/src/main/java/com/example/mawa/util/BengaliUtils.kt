package com.example.mawa.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object BengaliUtils {

    private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    private val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    private val banglaMonths = arrayOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    private val banglaWeekdays = arrayOf(
        "", "রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার"
    )

    val banglaWeekdaysOrdered = listOf(
        "শনিবার", "রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার"
    )

    fun toBanglaDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            val idx = ch - '0'
            if (idx in 0..9) {
                sb.append(bengaliDigits[idx])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toBanglaDigits(number: Int): String {
        return toBanglaDigits(number.toLong())
    }

    fun toBanglaDigits(number: Long): String {
        return toBanglaDigits(number.toString())
    }

    fun toBanglaDigits(number: Double): String {
        val format = if (number % 1.0 == 0.0) {
            DecimalFormat("#,##,##0").format(number.toLong())
        } else {
            DecimalFormat("#,##,##0.00").format(number)
        }
        return toBanglaDigits(format)
    }

    fun formatTaka(amount: Double, useBanglaDigits: Boolean = !LanguageManager.isEnglish): String {
        val isNegative = amount < 0
        val absAmount = Math.abs(amount)
        val format = if (absAmount % 1.0 == 0.0) {
            DecimalFormat("#,##,##0").format(absAmount.toLong())
        } else {
            DecimalFormat("#,##,##0.00").format(absAmount)
        }
        
        val formattedNumber = if (useBanglaDigits) toBanglaDigits(format) else format
        return if (isNegative) "−৳$formattedNumber" else "৳$formattedNumber"
    }

    fun formatNumber(number: Long, useBanglaDigits: Boolean = !LanguageManager.isEnglish): String {
        return if (useBanglaDigits) toBanglaDigits(number) else number.toString()
    }

    fun formatNumber(number: Int, useBanglaDigits: Boolean = !LanguageManager.isEnglish): String {
        return if (useBanglaDigits) toBanglaDigits(number) else number.toString()
    }

    fun formatNumber(number: Double, useBanglaDigits: Boolean = !LanguageManager.isEnglish): String {
        val format = if (number % 1.0 == 0.0) {
            DecimalFormat("#,##,##0").format(number.toLong())
        } else {
            DecimalFormat("#,##,##0.00").format(number)
        }
        return if (useBanglaDigits) toBanglaDigits(format) else format
    }

    fun formatQuantity(quantity: Double, unit: String, isEnglish: Boolean = LanguageManager.isEnglish): String {
        val formattedQty = if (quantity % 1.0 == 0.0) {
            if (isEnglish) quantity.toLong().toString() else toBanglaDigits(quantity.toLong().toString())
        } else {
            val str = String.format(Locale.US, "%.1f", quantity)
            if (isEnglish) str else toBanglaDigits(str)
        }
        return "$formattedQty $unit"
    }

    fun getGreeting(cal: Calendar = Calendar.getInstance(), isEnglish: Boolean = LanguageManager.isEnglish): String {
        return if (isEnglish) {
            when (cal.get(Calendar.HOUR_OF_DAY)) {
                in 5..11 -> "Good Morning"
                in 12..15 -> "Good Afternoon"
                in 16..19 -> "Good Evening"
                else -> "Good Night"
            }
        } else {
            when (cal.get(Calendar.HOUR_OF_DAY)) {
                in 5..11 -> "শুভ সকাল"
                in 12..15 -> "শুভ দুপুর"
                in 16..19 -> "শুভ সন্ধ্যা"
                else -> "শুভ রাত্রি"
            }
        }
    }

    fun formatDateKey(timeMillis: Long = System.currentTimeMillis()): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(timeMillis))
    }

    private val englishMonths = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private val englishWeekdays = arrayOf(
        "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    fun getFormattedTodayDate(timeMillis: Long = System.currentTimeMillis(), isEnglish: Boolean = LanguageManager.isEnglish): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMillis

        return if (isEnglish) {
            val day = cal.get(Calendar.DAY_OF_MONTH).toString()
            val month = englishMonths[cal.get(Calendar.MONTH)]
            val weekday = englishWeekdays[cal.get(Calendar.DAY_OF_WEEK)]
            "$day $month · $weekday"
        } else {
            val day = toBanglaDigits(cal.get(Calendar.DAY_OF_MONTH).toString())
            val month = banglaMonths[cal.get(Calendar.MONTH)]
            val weekday = banglaWeekdays[cal.get(Calendar.DAY_OF_WEEK)]
            "$day $month · $weekday"
        }
    }

    fun getBengaliDayOfWeek(timeMillis: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMillis
        val dayIndex = cal.get(Calendar.DAY_OF_WEEK)
        return if (dayIndex in banglaWeekdays.indices) banglaWeekdays[dayIndex] else ""
    }

    fun formatTransactionTime(timeMillis: Long, isEnglish: Boolean = LanguageManager.isEnglish): String {
        val now = System.currentTimeMillis()
        val calNow = Calendar.getInstance()
        calNow.timeInMillis = now

        val calTx = Calendar.getInstance()
        calTx.timeInMillis = timeMillis

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val timeStr = timeFormat.format(Date(timeMillis))
        val formattedTime = if (isEnglish) {
            timeStr
        } else {
            toBanglaDigits(timeStr)
                .replace("AM", "সকাল/দুপুর")
                .replace("PM", "বিকাল/রাত")
        }

        val isSameDay = calNow.get(Calendar.YEAR) == calTx.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calTx.get(Calendar.DAY_OF_YEAR)

        if (isSameDay) {
            return if (isEnglish) "Today, $formattedTime" else "আজ, $formattedTime"
        }

        calNow.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = calNow.get(Calendar.YEAR) == calTx.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calTx.get(Calendar.DAY_OF_YEAR)

        if (isYesterday) {
            return if (isEnglish) "Yesterday, $formattedTime" else "গতকাল, $formattedTime"
        }

        return if (isEnglish) {
            val day = calTx.get(Calendar.DAY_OF_MONTH).toString()
            val month = englishMonths[calTx.get(Calendar.MONTH)]
            "$day $month, $formattedTime"
        } else {
            val day = toBanglaDigits(calTx.get(Calendar.DAY_OF_MONTH).toString())
            val month = banglaMonths[calTx.get(Calendar.MONTH)]
            "$day $month, $formattedTime"
        }
    }

    fun formatTransactionDateOnly(timeMillis: Long, isEnglish: Boolean = LanguageManager.isEnglish): String {
        if (timeMillis <= 0) return ""
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMillis
        return if (isEnglish) {
            val day = cal.get(Calendar.DAY_OF_MONTH).toString()
            val month = englishMonths[cal.get(Calendar.MONTH)]
            val year = cal.get(Calendar.YEAR).toString()
            "$day $month, $year"
        } else {
            val day = toBanglaDigits(cal.get(Calendar.DAY_OF_MONTH).toString())
            val month = banglaMonths[cal.get(Calendar.MONTH)]
            val year = toBanglaDigits(cal.get(Calendar.YEAR).toString())
            "$day $month, $year"
        }
    }

    fun toBengaliDigits(input: String): String = toBanglaDigits(input)
    fun toBengaliDigits(number: Long): String = toBanglaDigits(number)
    fun toBengaliDigits(number: Double): String = toBanglaDigits(number)
    fun toEnglishDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            val idx = bengaliDigits.indexOf(ch)
            if (idx != -1) {
                sb.append(englishDigits[idx])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }
    fun formatNumber(number: Double): String = formatTaka(number)
    fun formatNumber(number: Long): String = toBanglaDigits(number)

    fun formatDateForExport(timeMillis: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(timeMillis))
    }

    fun formatDateTimeForExport(timeMillis: Long): String {
        return SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US).format(Date(timeMillis))
    }
}
