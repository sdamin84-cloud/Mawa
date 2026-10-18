package com.example.mawa.data.model

/**
 * Data class representing a cluster of similar company or product names detected in database history.
 * Example: "আবুল" and "আবুল খায়ের", or "অলি" and "অলিম্পিক", or "বন" and "বনরুটি"
 */
data class SimilarNameGroup(
    val primaryName: String,
    val variations: List<String>,
    val occurrences: Map<String, Int>,
    val totalTransactions: Int,
    val type: String // "কোম্পানি" বা "পণ্য"
)
