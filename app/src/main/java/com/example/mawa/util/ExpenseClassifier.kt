package com.example.mawa.util

/**
 * Intelligent expense classification based on Bengali grocery shop keywords.
 * Directly translates legacy ExpenseModel classification into Kotlin/Compose architecture.
 */
object ExpenseClassifier {

    const val TYPE_PURCHASE = "PURCHASE"                 // পণ্য ক্রয় / মাল কেনা (স্টক)
    const val TYPE_OPERATING_EXPENSE = "OPERATING_EXPENSE" // দোকান পরিচালনা খরচ
    const val TYPE_SHOP = "SHOP"                         // সাধারণ দোকান খরচ
    const val TYPE_HOME = "HOME"                         // সংসার / বাড়ি খরচ
    const val TYPE_LEGACY_EXPENSE = "LEGACY_EXPENSE"     // অন্যান্য খরচ

    // 1. Resale Products / Stock Purchase keywords (Inventory/Goods for sale)
    val PURCHASE_KEYWORDS = arrayOf(
        "চাল", "আটা", "ময়দা", "ময়দা", "সুজি", "তেল", "সয়াবিন", "সয়াবিন", "সরিষা", "ঘি",
        "চিনি", "গুড়", "গুড়", "লবণ", "লবন", "ডাল", "মসুর", "মুগ", "ছোলা", "বুট",
        "ডিম", "দুধ", "মিল্ক", "গুঁড়ো দুধ", "মাখন",
        "আলু", "পেঁয়াজ", "পেয়াজ", "পিঁয়াজ", "পিয়াজ", "রসুন", "আদা", "মরিচ", "হলুদ", "মসলা", "জিরা", "ধনিয়া", "ধনিয়া", "এলাচ", "দারুচিনি",
        "বিস্কুট", "টোস্ট", "চানাচুর", "চিপস", "চকলেট", "কেক", "পাউরুটি", "বনরুটি", "বেকারি",
        "চা পাতা", "কফি", "সিগারেট", "তামাক", "পান", "সুপারি", "ম্যাচ", "দিয়াশলাই", "দিয়াশলাই",
        "সাবান", "ডিটারজেন্ট", "হুইল", "সার্ফ", "শ্যাম্পু", "টুথপেস্ট", "ব্রাশ",
        "কোল্ড ড্রিংক", "ড্রিংকস", "জুস", "আইসক্রিম", "পানি", "মিনারেল",
        "কয়েল", "কয়েল", "গুডনাইট", "ওষুধ", "স্যালাইন",
        "কাঁচামাল", "সবজি", "শাক", "মাছ", "মাংস", "মুরগি", "গরু", "খাসি",
        "বাজার", "মাল", "পণ্য", "স্টক", "পাইকারি", "মহাজন", "কোম্পানি", "ডিলার"
    )

    // 2. Operating Expenses keywords (Rent, Utility, Salary, Transport, Tea/Food, Maintenance, Taxes/Fees)
    val OPERATING_KEYWORDS = arrayOf(
        "দোকান ভাড়া", "মেস ভাড়া", "বাড়ি ভাড়া", "ভাড়া", "ভাড়া",
        "বিদ্যুৎ বিল", "কারেন্ট বিল", "বিদ্যুৎ", "কারেন্ট", "ইলেকট্রিক", "বিল",
        "কর্মচারী বেতন", "বেতন", "মজুরি", "কর্মচারী", "হাজিরা",
        "চা-নাস্তা", "চা নাস্তা", "চায়ের বিল", "আপ্যায়ন", "আপ্যায়ন", "টিফিন", "মিষ্টি", "নাস্তা", "খাবার",
        "যাতায়াত", "যাতায়াত", "গাড়ি ভাড়া", "ভ্যান ভাড়া", "রিকশা", "রিক্সা", "পরিবহন",
        "মেরামত", "সার্ভিস", "রিপেয়ার", "রং",
        "বিকাশ খরচ", "নগদ খরচ", "ব্যাংক", "সার্ভিস চার্জ", "চার্জ", "ট্যাক্স", "ভ্যাট", "চাঁদা",
        "পরিষ্কার", "ঝাড়ু", "ঝাড়ু", "পলিথিন", "ক্যালকুলেটর", "কাগজ", "কলম", "টিস্যু", "মেমো", "দোকান খরচ"
    )

    // 3. Home / Family Expenses keywords
    val HOME_KEYWORDS = arrayOf(
        "সংসার", "বাড়ি", "বাড়ি", "বাসা", "পরিবার", "হাতখরচ", "চিকিৎসা", "ডাক্তার",
        "সন্তান", "পড়াশোনা", "স্কুল", "টিউশন", "ব্যক্তিগত"
    )

    fun autoClassifyType(name: String?): String {
        if (name.isNullOrBlank()) {
            return TYPE_LEGACY_EXPENSE
        }
        val lower = name.trim().lowercase()

        // Check Home keywords
        for (hk in HOME_KEYWORDS) {
            if (lower.contains(hk)) {
                return TYPE_HOME
            }
        }

        // Check Resale / Stock Purchase keywords
        for (pk in PURCHASE_KEYWORDS) {
            if (lower.contains(pk)) {
                return TYPE_PURCHASE
            }
        }

        // Check Operating Expense keywords
        for (op in OPERATING_KEYWORDS) {
            if (lower.contains(op)) {
                return TYPE_OPERATING_EXPENSE
            }
        }

        return TYPE_LEGACY_EXPENSE
    }

    fun isHomeExpense(typeOrCategory: String, name: String = ""): Boolean {
        return typeOrCategory.equals(TYPE_HOME, ignoreCase = true) || autoClassifyType(name) == TYPE_HOME
    }

    fun isPurchase(typeOrCategory: String, name: String = ""): Boolean {
        return typeOrCategory.equals(TYPE_PURCHASE, ignoreCase = true) || autoClassifyType(name) == TYPE_PURCHASE
    }

    fun isOperatingExpense(typeOrCategory: String, name: String = ""): Boolean {
        return typeOrCategory.equals(TYPE_OPERATING_EXPENSE, ignoreCase = true) ||
                typeOrCategory.equals(TYPE_SHOP, ignoreCase = true) ||
                autoClassifyType(name) == TYPE_OPERATING_EXPENSE
    }
}
