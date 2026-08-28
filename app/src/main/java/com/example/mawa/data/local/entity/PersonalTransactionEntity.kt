package com.example.mawa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PersonalTransactionType(val banglaLabel: String) {
    EXPENSE("খরচ"),
    INCOME("আয়"),
    SAVINGS("সঞ্চয়")
}

@Entity(tableName = "personal_transactions")
data class PersonalTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: PersonalTransactionType,
    val amount: Double,
    val title: String,         // কী জন্য (যেমন: নাস্তা, রিকশা, টিউশন, ঔষধ ইত্যাদি)
    val category: String,      // খাবার, যাতায়াত, পড়াশোনা, চিকিৎসা, বিল, কেনাকাটা, অন্যান্য ইত্যাদি
    val note: String = "",     // নোট (ঐচ্ছিক)
    val timestamp: Long = System.currentTimeMillis()
)
