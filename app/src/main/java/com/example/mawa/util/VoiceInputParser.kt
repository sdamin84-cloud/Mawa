package com.example.mawa.util

import com.example.mawa.data.local.entity.TransactionType

data class VoiceParsedResult(
    val rawText: String,
    val amount: Double?,
    val detectedType: TransactionType,
    val detectedCategory: String,
    val note: String,
    val customerOrItemName: String?
)

object VoiceInputParser {

    private val BENGALI_DIGITS = mapOf(
        '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
        '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
    )

    private val WORD_TO_NUMBER = mapOf(
        "দশ" to 10.0, "বিশ" to 20.0, "কুড়ি" to 20.0, "ত্রিশ" to 30.0, "চল্লিশ" to 40.0,
        "পঞ্চাশ" to 50.0, "ষাট" to 60.0, "সত্তর" to 70.0, "আশি" to 80.0, "নব্বই" to 90.0,
        "একশত" to 100.0, "একশো" to 100.0, "দেড়শত" to 150.0, "দেড়শো" to 150.0,
        "দুইশত" to 200.0, "দুইশো" to 200.0, "আড়াইশো" to 250.0, "আড়াইশত" to 250.0,
        "তিনশত" to 300.0, "তিনশো" to 300.0, "চারশত" to 400.0, "চারশো" to 400.0,
        "পাঁচশত" to 500.0, "পাঁচশো" to 500.0, "ছয়শত" to 600.0, "ছয়শো" to 600.0,
        "সাতশত" to 700.0, "সাতশো" to 700.0, "আটশত" to 800.0, "আটশো" to 800.0,
        "নয়শত" to 900.0, "নয়শো" to 900.0, "এক হাজার" to 1000.0, "হাজার" to 1000.0,
        "দুই হাজার" to 2000.0, "পাঁচ হাজার" to 5000.0, "দশ হাজার" to 10000.0
    )

    /**
     * Parses spoken Bengali or English text into an actionable transaction structure.
     */
    fun parse(rawInput: String): VoiceParsedResult {
        val clean = rawInput.trim()
        if (clean.isBlank()) {
            return VoiceParsedResult(
                rawText = "",
                amount = null,
                detectedType = TransactionType.EXPENSE_SHOP,
                detectedCategory = "সাধারণ",
                note = "",
                customerOrItemName = null
            )
        }

        val extractedAmount = extractAmount(clean)
        val lower = clean.lowercase()

        // Detect type based on keywords
        val type: TransactionType
        val category: String
        var nameOrDesc: String? = null

        when {
            // বাকি জমা / কালেকশন
            lower.contains("জমা") || lower.contains("আদায়") || lower.contains("শোধ") || lower.contains("দিয়েছে") -> {
                type = TransactionType.BAKI_COLLECTION
                category = "বাকি আদায়"
                nameOrDesc = extractNameBeforeKeyword(clean, listOf("জমা", "আদায়", "শোধ", "দিয়েছে"))
            }
            // বাকি বিক্রি
            lower.contains("বাকি") || lower.contains("বাকী") || lower.contains("ধার") -> {
                type = TransactionType.SALE_BAKI
                category = "বাকি বিক্রি"
                nameOrDesc = extractNameBeforeKeyword(clean, listOf("বাকি", "বাকী", "ধার"))
            }
            // মাল ক্রয় / স্টক কেনা / ফর্দ
            lower.contains("মাল কেনা") || lower.contains("ক্রয়") || lower.contains("কিনলাম") ||
            lower.contains("মাল আনলাম") || lower.contains("কোম্পানি") || lower.contains("সাপ্লায়ার") || lower.contains("মহাজন") -> {
                type = TransactionType.PURCHASE_DIRECT
                category = "মাল ক্রয়"
                nameOrDesc = cleanNoteText(clean, listOf("মাল কেনা", "ক্রয়", "কিনলাম", "টাকা", "৳"))
            }
            // ব্যক্তিগত / সংসার খরচ
            lower.contains("সংসার") || lower.contains("বাড়ি") || lower.contains("বাসা") || lower.contains("উত্তোলন") ||
            lower.contains("ব্যক্তিগত") || lower.contains("বাজার") || lower.contains("বাচ্চার") -> {
                type = TransactionType.EXPENSE_HOME
                category = "সংসার / ব্যক্তিগত"
                nameOrDesc = cleanNoteText(clean, listOf("সংসার", "বাড়ি খরচ", "বাসা খরচ", "টাকা", "৳"))
            }
            // নগদ বিক্রি
            lower.contains("বিক্রি") || lower.contains("বেচা") || lower.contains("ক্যাশ বিক্রি") || lower.contains("নগদ") -> {
                type = TransactionType.SALE_CASH
                category = "নগদ বিক্রি"
                nameOrDesc = cleanNoteText(clean, listOf("বিক্রি", "বেচা", "ক্যাশ", "নগদ", "টাকা", "৳"))
            }
            // দোকান পরিচালনা খরচ (চা, বিদ্যুৎ, ভাড়া, বেতন, পরিবহন ইত্যাদি)
            else -> {
                type = TransactionType.EXPENSE_SHOP
                category = when {
                    lower.contains("চা") || lower.contains("নাস্তা") || lower.contains("আপ্যায়ন") || lower.contains("বিস্কুট") || lower.contains("পান") -> "চা ও আপ্যায়ন"
                    lower.contains("ভাড়া") -> "দোকান ভাড়া"
                    lower.contains("বিদ্যুৎ") || lower.contains("কারেন্ট") || lower.contains("বিল") -> "বিদ্যুৎ বিল"
                    lower.contains("বেতন") || lower.contains("মজুরি") || lower.contains("স্টাফ") -> "কর্মচারী বেতন"
                    lower.contains("ভ্যান") || lower.contains("গাড়ি") || lower.contains("রিকশা") || lower.contains("পরিবহন") -> "পরিবহন খরচ"
                    lower.contains("পলিথিন") || lower.contains("প্যাকেট") || lower.contains("ব্যাগ") -> "প্যাকেজিং"
                    lower.contains("মেরামত") || lower.contains("মিস্ত্রি") -> "মেরামত ও রক্ষণাবেক্ষণ"
                    else -> "দোকানের খরচ"
                }
                nameOrDesc = cleanNoteText(clean, listOf("টাকা", "খরচ", "৳"))
            }
        }

        val finalNote = if (!nameOrDesc.isNullOrBlank()) nameOrDesc else clean

        return VoiceParsedResult(
            rawText = clean,
            amount = extractedAmount,
            detectedType = type,
            detectedCategory = category,
            note = finalNote,
            customerOrItemName = nameOrDesc
        )
    }

    private fun extractAmount(text: String): Double? {
        // First check for written word numbers
        for ((word, value) in WORD_TO_NUMBER) {
            if (text.contains(word)) {
                // If there is also a numeric part, prefer numbers, else word
                val hasDigits = text.any { it.isDigit() || BENGALI_DIGITS.containsKey(it) }
                if (!hasDigits) return value
            }
        }

        // Convert Bengali digits to English digits
        val converted = text.map { BENGALI_DIGITS[it] ?: it }.joinToString("")

        // Regex for finding amounts (e.g. 500, 1200.50, 50,000)
        val regex = Regex("""\b\d+(?:\.\d+)?\b""")
        val match = regex.find(converted.replace(",", ""))
        return match?.value?.toDoubleOrNull()
    }

    private fun extractNameBeforeKeyword(text: String, keywords: List<String>): String? {
        for (kw in keywords) {
            val idx = text.indexOf(kw)
            if (idx > 0) {
                val candidate = text.substring(0, idx).trim()
                    .replace(Regex("""\d+"""), "")
                    .replace(Regex("""[০-৯]+"""), "")
                    .replace("টাকা", "")
                    .replace("এর", "")
                    .trim()
                if (candidate.isNotBlank() && candidate.length > 1) {
                    return candidate
                }
            }
        }
        return null
    }

    private fun cleanNoteText(text: String, removeKeywords: List<String>): String {
        var res = text
            .replace(Regex("""\d+"""), "")
            .replace(Regex("""[০-৯]+"""), "")
            .replace("টাকা", "")
            .replace("৳", "")

        for (kw in removeKeywords) {
            res = res.replace(kw, "")
        }

        return res.trim().replace(Regex("""\s+"""), " ")
    }
}
