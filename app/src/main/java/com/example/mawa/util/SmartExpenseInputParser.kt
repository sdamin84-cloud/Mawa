package com.example.mawa.util

/**
 * Data class representing a parsed expense item containing both product/expense name and price.
 */
data class ParsedExpense(
    val name: String,
    val amount: Double,
    val amountString: String
)

/**
 * Intelligent parser that extracts product/item name and price from a single input string.
 * Supports both Bengali digits (০-৯) and English digits (0-9).
 * E.g., "মাছ ৩৫০" -> name: "মাছ", amount: 350.0
 * E.g., "৩৫০ মাছ" -> name: "মাছ", amount: 350.0
 * E.g., "চাল ১২০০ টাকা" -> name: "চাল", amount: 1200.0
 */
object SmartExpenseInputParser {

    fun parse(rawInput: String): ParsedExpense? {
        if (rawInput.isBlank()) return null

        // Convert any Bengali digits to English digits
        val normalized = BengaliUtils.toEnglishDigits(rawInput)

        // Find numbers with optional decimals
        val numberRegex = Regex("""(\d+(?:\.\d+)?)""")
        val match = numberRegex.find(normalized) ?: return null

        val amountStr = match.groupValues[1]
        val amount = amountStr.toDoubleOrNull() ?: return null
        if (amount <= 0) return null

        // Strip the number and common currency markers (টাকা, tk, ৳) to obtain the item name
        var cleanName = normalized
            .replaceRange(match.range, " ")
            .replace(Regex("""(?i)\b(টাকা|ট|tk|taka|৳)\b"""), " ")
            .replace("৳", " ")
            .replace(Regex("""[+*=/\\_]"""), " ")
            .trim()

        // Normalize multiple spaces into single space
        cleanName = cleanName.replace(Regex("""\s+"""), " ")

        // If no product name is present (e.g. user just typed "350" or "৩৫০"), don't treat as smart pair
        if (cleanName.isBlank()) {
            return null
        }

        val formattedAmountStr = if (amount % 1.0 == 0.0) {
            amount.toLong().toString()
        } else {
            amount.toString()
        }

        return ParsedExpense(
            name = cleanName,
            amount = amount,
            amountString = formattedAmountStr
        )
    }
}
