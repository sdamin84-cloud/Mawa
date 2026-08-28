package com.example.mawa.data.model

enum class AppMode(val key: String, val banglaTitle: String, val banglaDesc: String) {
    BOTH("BOTH", "দুটোই", "দোকান ও ব্যক্তিগত হিসাব একসাথে"),
    PERSONAL_ONLY("PERSONAL_ONLY", "ব্যক্তিগত হিসাব", "দৈনন্দিন সাধারণ খরচ, আয় ও সঞ্চয়"),
    BUSINESS_ONLY("BUSINESS_ONLY", "ব্যবসার হিসাব", "দোকানের ক্যাশ, খাতা ও ফর্দ");

    companion object {
        fun fromKey(key: String?): AppMode {
            return values().firstOrNull { it.key == key || it.name == key } ?: BOTH
        }
    }
}
