package com.example.mawa.data.model

enum class AppThemeMode(val key: String, val banglaTitle: String, val banglaDesc: String) {
    SYSTEM("SYSTEM", "সিস্টেম অনুযায়ী", "ফোনের ডার্ক/লাইট মোড অনুযায়ী স্বয়ংক্রিয়ভাবে চলবে"),
    LIGHT("LIGHT", "লাইট মোড", "উজ্জ্বল ও দিনের আলোয় ব্যবহারের উপযোগী সাদা থিম"),
    DARK("DARK", "ডার্ক মোড", "চোখের জন্য আরামদায়ক ও ব্যাটারি সাশ্রয়ী ডার্ক থিম");

    companion object {
        fun fromKey(key: String?): AppThemeMode {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) || it.name.equals(key, ignoreCase = true) } ?: SYSTEM
        }
    }
}
