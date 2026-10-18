package com.example.mawa.util

/**
 * Intelligent parser for natural language grocery and shopping list entries in Bengali & English.
 *
 * Supported formats:
 * - "মাছ ২০০" -> Product: মাছ, Rate/Price: 200, Qty: 1
 * - "মাছ ২০০ টাকা" -> Product: মাছ, Rate/Price: 200, Qty: 1
 * - "পিয়াজ দুই কেজি" -> Product: পিয়াজ, Qty: 2.0, Unit: কেজি
 * - "পেঁয়াজ ২ কেজি" -> Product: পেঁয়াজ, Qty: 2.0, Unit: কেজি
 * - "৫ কেজি চাল" -> Product: চাল, Qty: 5.0, Unit: কেজি
 * - "১২টি ডিম" -> Product: ডিম, Qty: 12.0, Unit: টি
 * - "ডিম ১২ টা" -> Product: ডিম, Qty: 12.0, Unit: টি
 * - "সয়াবিন তেল ২ লিটার" -> Product: সয়াবিন তেল, Qty: 2.0, Unit: লিটার
 * - "আলু ৫০ টাকার" -> Product: আলু, Rate: 50, Qty: 1
 * - "গরুর মাংস ১ কেজি ৮০০ টাকা" -> Product: গরুর মাংস, Qty: 1.0, Unit: কেজি, Rate: 800
 * - "চা" -> Product: চা, Qty: 1.0, Unit: প্যাকেট
 */
data class ParsedFordiItem(
    val rawInput: String,
    val productName: String,
    val quantity: Double,
    val unit: String,
    val estimatedRate: Double,
    val isPriceSpecified: Boolean = false
)

object BengaliFordiParser {

    private val BENGALI_DIGITS = mapOf(
        '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
        '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
    )

    private val WORD_TO_NUMBER = mapOf(
        "এক" to 1.0, "একটা" to 1.0, "একটি" to 1.0,
        "দেড়" to 1.5, "দেড়" to 1.5,
        "দুই" to 2.0, "দু" to 2.0, "দুটো" to 2.0, "দুটি" to 2.0,
        "আড়াই" to 2.5, "আড়াই" to 2.5,
        "তিন" to 3.0, "তিনটি" to 3.0, "তিনটা" to 3.0,
        "চার" to 4.0, "চারটি" to 4.0, "চারটা" to 4.0,
        "পাঁচ" to 5.0, "পাঁচটি" to 5.0, "পাঁচটা" to 5.0,
        "ছয়" to 6.0, "ছয়" to 6.0, "ছয়টি" to 6.0, "ছয়টা" to 6.0,
        "সাত" to 7.0, "সাতটি" to 7.0, "সাতটা" to 7.0,
        "আট" to 8.0, "আটটি" to 8.0, "আটটা" to 8.0,
        "নয়" to 9.0, "নয়" to 9.0, "নয়টি" to 9.0, "নয়টা" to 9.0,
        "দশ" to 10.0, "দশটি" to 10.0, "দশটা" to 10.0,
        "এগারো" to 11.0, "বারো" to 12.0, "তেরো" to 13.0, "চৌদ্দ" to 14.0, "পনেরো" to 15.0,
        "বিশ" to 20.0, "কুড়ি" to 20.0, "ত্রিশ" to 30.0, "চল্লিশ" to 40.0, "পঞ্চাশ" to 50.0,
        "একশত" to 100.0, "একশো" to 100.0, "দেড়শো" to 150.0, "দেড়শত" to 150.0,
        "দুইশত" to 200.0, "দুইশো" to 200.0, "আড়াইশো" to 250.0, "তিনশো" to 300.0,
        "চারশো" to 400.0, "পাঁচশো" to 500.0, "ছয়শো" to 600.0, "সাতশো" to 700.0,
        "আটশো" to 800.0, "নয়শো" to 900.0, "হাজার" to 1000.0, "এক হাজার" to 1000.0,
        "হাফ" to 0.5, "আধা" to 0.5, "পোয়া" to 0.25, "পোয়া" to 0.25
    )

    private val KNOWN_UNITS = listOf(
        "কেজি", "কেজির", "কে", "kg",
        "গ্রাম", "গ্রামের", "gm", "g",
        "লিটার", "লি", "ltr", "l",
        "প্যাকেট", "প্যাক", "pkt",
        "হালি", "ডজন",
        "বস্তা", "ব্যাগ",
        "টি", "টা", "পিস", "টুকরা", "বোতল", "আটি", "মুঠা"
    )

    private val PIECE_ORIENTED_PRODUCTS = setOf(
        "ডিম", "লেবু", "সাবান", "কলা", "নারকেল", "নারিকেল", "লাউ", "মিষ্টি কুমড়া", "ফুলকপি", "বাঁধাকপি", "ডাব", "পাউরুটি"
    )

    private val LIQUID_PRODUCTS = setOf(
        "তেল", "সয়াবিন তেল", "সরিষার তেল", "দুধ", "ঘি", "ভিনেগার", "জুস", "পানি"
    )

    private val PACKET_PRODUCTS = setOf(
        "চা", "চা পাতা", "বিস্কুট", "নুডলস", "গুঁড়ো দুধ", "গুড়া দুধ", "মসলা", "হলুদ", "মরিচ গুঁড়া", "লবণ", "ডিটারজেন্ট", "মশার কয়েল"
    )

    /**
     * Converts a string with Bengali digits to English digits
     */
    fun toEnglishDigits(input: String): String {
        val sb = StringBuilder()
        for (c in input) {
            sb.append(BENGALI_DIGITS[c] ?: c)
        }
        return sb.toString()
    }

    /**
     * Primary parsing function
     */
    fun parse(rawInput: String): ParsedFordiItem {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) {
            return ParsedFordiItem(
                rawInput = "",
                productName = "",
                quantity = 1.0,
                unit = "টি",
                estimatedRate = 0.0
            )
        }

        var text = toEnglishDigits(trimmed)
        var parsedRate = 0.0
        var isPriceFound = false
        var parsedQty = 1.0
        var parsedUnit = ""

        // Check for "X টাকার" or "X টাকা" or "দর X" (put টাকার before টাকা so regex matches full word)
        val priceRegex = Regex("""(?i)(\d+(?:\.\d+)?|${WORD_TO_NUMBER.keys.joinToString("|")})\s*(?:টাকার|টাকা|দর|৳|tk|taka)""")
        val priceMatch = priceRegex.find(text)
        if (priceMatch != null) {
            val numStr = priceMatch.groupValues[1]
            parsedRate = parseNumberValue(numStr) ?: 0.0
            isPriceFound = true
            text = text.removeRange(priceMatch.range).trim()
        }

        // Check for combined patterns like "১২টি", "৫কেজি", "২লিটার"
        // Replace attached unit with space e.g. "১২টি" -> "১২ টি", "৫কেজি" -> "৫ কেজি"
        for (u in KNOWN_UNITS) {
            val attachedRegex = Regex("""(?i)([০-৯\d]+)($u)""")
            text = text.replace(attachedRegex, "$1 $2")
        }

        val tokens = text.split(Regex("""\s+""")).filter { it.isNotBlank() }

        val remainingTokens = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            val tokenEng = toEnglishDigits(token)

            // Check if current token is a number or word number
            val numValue = parseNumberValue(tokenEng) ?: parseNumberValue(token)

            if (numValue != null && i + 1 < tokens.size) {
                // Look ahead for unit in next token
                val nextToken = tokens[i + 1].lowercase()
                val matchedUnit = findUnit(nextToken)
                if (matchedUnit != null) {
                    parsedQty = numValue
                    parsedUnit = normalizeUnit(matchedUnit)
                    i += 2
                    continue
                }
            }

            // Check if unit is in this token or preceded by word
            val unitHere = findUnit(token)
            if (unitHere != null && remainingTokens.isNotEmpty()) {
                val prevToken = remainingTokens.last()
                val prevNum = parseNumberValue(toEnglishDigits(prevToken)) ?: parseNumberValue(prevToken)
                if (prevNum != null) {
                    parsedQty = prevNum
                    parsedUnit = normalizeUnit(unitHere)
                    remainingTokens.removeAt(remainingTokens.size - 1)
                    i++
                    continue
                }
            }

            if (numValue != null) {
                // If we don't have a unit or price yet
                // Example: "মাছ ২০০" -> 200 is standalone number without unit
                if (!isPriceFound && numValue >= 20.0) {
                    // In shopping context, numbers >= 20 without unit are almost universally price (e.g. মাছ ২০০, মাংস ৫০০)
                    parsedRate = numValue
                    isPriceFound = true
                } else if (parsedQty == 1.0 && parsedUnit.isEmpty()) {
                    // Small number without unit might be quantity (e.g. ডিম ১২, লেবু ৪)
                    parsedQty = numValue
                } else if (!isPriceFound) {
                    parsedRate = numValue
                    isPriceFound = true
                }
                i++
                continue
            }

            remainingTokens.add(token)
            i++
        }

        // Clean product name
        var productName = remainingTokens.joinToString(" ")
            .replace(Regex("""^[+\-•\s]+"""), "")
            .replace(Regex("""[,\.]+$"""), "")
            .trim()

        if (productName.isBlank()) {
            productName = trimmed
        }

        // Infer appropriate default unit if none was given
        if (parsedUnit.isBlank()) {
            parsedUnit = when {
                LIQUID_PRODUCTS.any { productName.contains(it) } -> "লিটার"
                PACKET_PRODUCTS.any { productName.contains(it) } -> "প্যাকেট"
                PIECE_ORIENTED_PRODUCTS.any { productName.contains(it) } -> "টি"
                isPriceFound && parsedQty == 1.0 -> "পিস"
                else -> "কেজি"
            }
        }

        // If unit is "হালি" or "ডজন", normalize or keep unit readable
        if (parsedUnit == "হালি" && parsedQty == 1.0) {
            // Keep unit as হালি or 4 টি
        }

        return ParsedFordiItem(
            rawInput = trimmed,
            productName = productName,
            quantity = if (parsedQty > 0) parsedQty else 1.0,
            unit = parsedUnit,
            estimatedRate = parsedRate,
            isPriceSpecified = isPriceFound
        )
    }

    private fun parseNumberValue(str: String): Double? {
        val eng = toEnglishDigits(str).trim()
        eng.toDoubleOrNull()?.let { return it }

        WORD_TO_NUMBER[str]?.let { return it }
        WORD_TO_NUMBER[eng]?.let { return it }

        return null
    }

    private fun findUnit(token: String): String? {
        val clean = token.lowercase().replace(Regex("""[.,]$"""), "")
        return KNOWN_UNITS.firstOrNull { clean == it || clean.startsWith(it) }
    }

    private fun normalizeUnit(unit: String): String {
        return when {
            unit.startsWith("কে") || unit.startsWith("kg") -> "কেজি"
            unit.startsWith("গ্রাম") || unit.startsWith("gm") || unit == "g" -> "গ্রাম"
            unit.startsWith("লি") || unit.startsWith("ltr") || unit == "l" -> "লিটার"
            unit.startsWith("প্যাক") || unit.startsWith("pkt") -> "প্যাকেট"
            unit == "টি" || unit == "টা" || unit == "পিস" || unit == "টুকরা" -> "টি"
            unit == "হালি" -> "হালি"
            unit == "ডজন" -> "ডজন"
            unit.startsWith("বস্তা") || unit.startsWith("ব্যাগ") -> "বস্তা"
            unit.startsWith("বোতল") -> "বোতল"
            else -> unit
        }
    }
}
