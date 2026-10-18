package com.example.mawa.util

import com.example.mawa.data.local.entity.FordiItemEntity
import com.example.mawa.data.local.entity.ProductEntity
import com.example.mawa.data.local.entity.SupplierCompanyEntity
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType
import com.example.mawa.data.model.SimilarNameGroup
import java.util.Locale

object SimilarNameDetector {

    private val STOP_WORDS = setOf(
        "খরচ", "দোকান", "মুদি", "বিল", "টাকা", "মাল", "ক্রয়", "বিক্রি",
        "অন্যান্য", "সাধারণ", "নাস্তা", "চা", "যাতায়াত", "দৈনিক", "মাসিক",
        "হিসাব", "খাতা", "পণ্য", "আইটেম"
    )

    private val COMMON_MODIFIERS = setOf(
        "দেশি", "বিদেশি", "লাল", "সাদা", "কালো", "বড়", "ছোট", "তাজা",
        "গুঁড়া", "খোলা", "প্যাকেট", "খাঁটি", "নতুন", "পুরাতন", "প্রিমিয়াম",
        "স্পেশাল"
    )

    /**
     * Detects clusters of similar names from transactions, fordi items, products, and saved supplier companies.
     */
    fun findSimilarGroups(
        transactions: List<TransactionEntity>,
        fordiItems: List<FordiItemEntity>,
        products: List<ProductEntity>,
        supplierCompanies: List<SupplierCompanyEntity>
    ): List<SimilarNameGroup> {
        val companyCounts = mutableMapOf<String, Int>()
        val productCounts = mutableMapOf<String, Int>()

        // 1. Collect company names
        for (tx in transactions) {
            if (tx.type == TransactionType.EXPENSE_SHOP ||
                tx.type == TransactionType.PURCHASE_DIRECT ||
                tx.type == TransactionType.PURCHASE_FORDI
            ) {
                val raw = tx.note.trim()
                val clean = cleanCompanyName(raw)
                if (isValidCandidate(clean)) {
                    companyCounts[clean] = (companyCounts[clean] ?: 0) + 1
                }
            }
            // Collect products
            val p = tx.productName?.trim()
            if (!p.isNullOrBlank() && isValidCandidate(p)) {
                productCounts[p] = (productCounts[p] ?: 0) + 1
            }
        }

        // Fordi items
        for (f in fordiItems) {
            val c = cleanCompanyName(f.companyName)
            if (isValidCandidate(c)) {
                companyCounts[c] = (companyCounts[c] ?: 0) + 1
            }
            val p = f.productName.trim()
            if (isValidCandidate(p)) {
                productCounts[p] = (productCounts[p] ?: 0) + 1
            }
        }

        // Registered supplier companies
        for (sc in supplierCompanies) {
            val c = cleanCompanyName(sc.companyName)
            if (isValidCandidate(c)) {
                companyCounts[c] = (companyCounts[c] ?: 0) + 1
            }
        }

        // Products
        for (pr in products) {
            val p = pr.name.trim()
            if (isValidCandidate(p)) {
                productCounts[p] = (productCounts[p] ?: 0) + 1
            }
        }

        val results = mutableListOf<SimilarNameGroup>()
        results.addAll(clusterNames(companyCounts, "কোম্পানি"))
        results.addAll(clusterNames(productCounts, "পণ্য"))

        return results.sortedByDescending { it.totalTransactions }
    }

    private fun cleanCompanyName(raw: String): String {
        return raw.replace(Regex("""^(খরচ|দোকান|মাল|ক্রয়|বাকি|বিল)\s*:\s*"""), "")
            .replace(Regex("""\s*\([^\)]*\)"""), "")
            .trim()
    }

    private fun isValidCandidate(name: String): Boolean {
        if (name.length < 2) return false
        if (STOP_WORDS.contains(name.lowercase(Locale.ROOT))) return false
        if (name.all { it.isDigit() || it == '.' || it == ',' || it == '৳' }) return false
        return true
    }

    /**
     * Clusters similar names using graph connectivity
     */
    private fun clusterNames(counts: Map<String, Int>, type: String): List<SimilarNameGroup> {
        val names = counts.keys.toList()
        if (names.size < 2) return emptyList()

        val parent = mutableMapOf<String, String>()
        for (n in names) parent[n] = n

        fun find(i: String): String {
            var root = i
            while (root != parent[root]) {
                root = parent[root]!!
            }
            var curr = i
            while (curr != root) {
                val next = parent[curr]!!
                parent[curr] = root
                curr = next
            }
            return root
        }

        fun union(i: String, j: String) {
            val rootI = find(i)
            val rootJ = find(j)
            if (rootI != rootJ) {
                // Attach the shorter / less used to the more authoritative one
                val countI = counts[rootI] ?: 0
                val countJ = counts[rootJ] ?: 0
                if (countI >= countJ) {
                    parent[rootJ] = rootI
                } else {
                    parent[rootI] = rootJ
                }
            }
        }

        for (i in 0 until names.size) {
            val a = names[i]
            for (j in i + 1 until names.size) {
                val b = names[j]
                if (areSimilar(a, b)) {
                    union(a, b)
                }
            }
        }

        // Group by root
        val groups = mutableMapOf<String, MutableList<String>>()
        for (n in names) {
            val r = find(n)
            groups.getOrPut(r) { mutableListOf() }.add(n)
        }

        val result = mutableListOf<SimilarNameGroup>()
        for ((_, cluster) in groups) {
            if (cluster.size > 1) {
                // Determine primary name: one with highest frequency or longest formal name
                val sortedByFreq = cluster.sortedWith(
                    compareByDescending<String> { counts[it] ?: 0 }
                        .thenByDescending { it.length }
                )
                val primary = sortedByFreq.first()
                val occurrences = cluster.associateWith { counts[it] ?: 0 }
                val totalTxs = occurrences.values.sum()

                result.add(
                    SimilarNameGroup(
                        primaryName = primary,
                        variations = cluster,
                        occurrences = occurrences,
                        totalTransactions = totalTxs,
                        type = type
                    )
                )
            }
        }

        return result
    }

    /**
     * Returns true if two names are likely variations of each other.
     * Examples:
     * - "আবুল" and "আবুল খায়ের" (contains / startsWith)
     * - "অলি" and "অলিম্পিক" (startsWith with length >= 2)
     * - "বন" and "বনরুটি" (contains / startsWith)
     */
    fun areSimilar(name1: String, name2: String): Boolean {
        val s1 = name1.trim().lowercase(Locale.ROOT)
        val s2 = name2.trim().lowercase(Locale.ROOT)
        if (s1 == s2) return false
        if (s1.length < 2 || s2.length < 2) return false

        // 1. Starts with prefix match (e.g. "অলি" vs "অলিম্পিক", "বন" vs "বনরুটি")
        val shorter = if (s1.length <= s2.length) s1 else s2
        val longer = if (s1.length > s2.length) s1 else s2

        // Avoid false positives for very short common prefixes like "মা" in "মাখন" vs "মাছ"
        if (shorter.length >= 2 && longer.startsWith(shorter)) {
            // If shorter has >= 3 chars, or if it's 2 chars and separated by space or starts distinct brand
            if (shorter.length >= 3 || longer.startsWith("$shorter ") || longer.startsWith(shorter)) {
                return true
            }
        }

        // 2. Substring match (e.g. "আবুল" in "আবুল খায়ের" or "খায়ের" in "আবুল খায়ের")
        if (shorter.length >= 3 && longer.contains(shorter)) {
            return true
        }

        // 3. Multi-word overlap (e.g. "আকিজ ফুড" vs "আকিজ কোম্পানি")
        val words1 = s1.split(Regex("""\s+""")).filter { it.length >= 2 && !STOP_WORDS.contains(it) }
        val words2 = s2.split(Regex("""\s+""")).filter { it.length >= 2 && !STOP_WORDS.contains(it) }
        val commonWords = words1.intersect(words2.toSet())
        if (commonWords.isNotEmpty()) {
            val distinctiveCommonWords = commonWords.filter { !COMMON_MODIFIERS.contains(it) }
            if (distinctiveCommonWords.isNotEmpty()) {
                val maxCommonLen = distinctiveCommonWords.maxOf { it.length }
                if (maxCommonLen >= 3) {
                    return true
                }
            }
        }

        // 4. Check for distinct core grocery items sharing a common modifier (e.g. "দেশি চাল" vs "দেশি ডাল")
        val w1 = s1.split(Regex("""\s+""")).filter { it.isNotBlank() }
        val w2 = s2.split(Regex("""\s+""")).filter { it.isNotBlank() }
        val rem1 = w1.filter { !COMMON_MODIFIERS.contains(it) && !STOP_WORDS.contains(it) }
        val rem2 = w2.filter { !COMMON_MODIFIERS.contains(it) && !STOP_WORDS.contains(it) }
        val distinctGroceries = setOf(
            "চাল", "ডাল", "তেল", "লবণ", "নুন", "চিনি", "আটা", "ময়দা", "সুজি",
            "চা", "পান", "দুধ", "ঘি", "রসুন", "আদা", "পিঁয়াজ", "পেঁয়াজ", "মরিচ", "হলুদ",
            "ডিম", "মাছ", "মাংস", "আলু", "পটল", "লাউ", "শসা", "লেবু"
        )
        if (rem1.isNotEmpty() && rem2.isNotEmpty() && rem1 != rem2) {
            if (rem1.any { distinctGroceries.contains(it) } && rem2.any { distinctGroceries.contains(it) }) {
                return false
            }
        }

        // 5. Levenshtein edit distance for spelling mistakes (e.g. "বিস্কুট" vs "বিস্কিট")
        if (s1.length >= 4 && s2.length >= 4 && Math.abs(s1.length - s2.length) <= 2) {
            val dist = computeLevenshtein(s1, s2)
            if (dist <= 2) {
                return true
            }
        }

        return false
    }

    private fun computeLevenshtein(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }
}
