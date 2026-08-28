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

    fun formatTaka(amount: Double, useBanglaDigits: Boolean = true): String {
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

    fun formatQuantity(quantity: Double, unit: String): String {
        val formattedQty = if (quantity % 1.0 == 0.0) {
            toBanglaDigits(quantity.toLong().toString())
        } else {
            toBanglaDigits(String.format(Locale.US, "%.1f", quantity))
        }
        return "$formattedQty $unit"
    }

    fun getGreeting(cal: Calendar = Calendar.getInstance()): String {
        return when (cal.get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "শুভ সকাল"
            in 12..15 -> "শুভ দুপুর"
            in 16..19 -> "শুভ সন্ধ্যা"
            else -> "শুভ রাত্রি"
        }
    }

    fun getFormattedTodayDate(timeMillis: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMillis

        val day = toBanglaDigits(cal.get(Calendar.DAY_OF_MONTH).toString())
        val month = banglaMonths[cal.get(Calendar.MONTH)]
        val weekday = banglaWeekdays[cal.get(Calendar.DAY_OF_WEEK)]

        return "$day $month · $weekday"
    }

    fun formatTransactionTime(timeMillis: Long): String {
        val now = System.currentTimeMillis()
        val calNow = Calendar.getInstance()
        calNow.timeInMillis = now

        val calTx = Calendar.getInstance()
        calTx.timeInMillis = timeMillis

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val timeStr = timeFormat.format(Date(timeMillis))
        val banglaTime = toBanglaDigits(timeStr)
            .replace("AM", "সকাল/দুপুর")
            .replace("PM", "বিকাল/রাত")

        val isSameDay = calNow.get(Calendar.YEAR) == calTx.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calTx.get(Calendar.DAY_OF_YEAR)

        if (isSameDay) {
            return "আজ, $banglaTime"
        }

        calNow.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = calNow.get(Calendar.YEAR) == calTx.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calTx.get(Calendar.DAY_OF_YEAR)

        if (isYesterday) {
            return "গতকাল, $banglaTime"
        }

        val day = toBanglaDigits(calTx.get(Calendar.DAY_OF_MONTH).toString())
        val month = banglaMonths[calTx.get(Calendar.MONTH)]
        return "$day $month, $banglaTime"
    }

    fun formatTransactionDateOnly(timeMillis: Long): String {
        if (timeMillis <= 0) return ""
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMillis
        val day = toBanglaDigits(cal.get(Calendar.DAY_OF_MONTH).toString())
        val month = banglaMonths[cal.get(Calendar.MONTH)]
        val year = toBanglaDigits(cal.get(Calendar.YEAR).toString())
        return "$day $month, $year"
    }

    fun toBengaliDigits(input: String): String = toBanglaDigits(input)
    fun toBengaliDigits(number: Long): String = toBanglaDigits(number)
    fun toBengaliDigits(number: Double): String = toBanglaDigits(number)
    fun formatNumber(number: Double): String = formatTaka(number)
    fun formatNumber(number: Long): String = toBanglaDigits(number)

    fun formatDateForExport(timeMillis: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(timeMillis))
    }

    fun formatDateTimeForExport(timeMillis: Long): String {
        return SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US).format(Date(timeMillis))
    }
}
