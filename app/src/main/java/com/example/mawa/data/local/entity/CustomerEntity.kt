package com.example.mawa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val openingBalance: Double = 0.0, // পূর্বের বাকি (Opening receivable)
    val creditLimit: Double = 0.0,    // সর্বোচ্চ বাকি সীমা (0 = No limit)
    val promisedPaymentDate: Long = 0L, // টাকা পরিশোধের প্রতিশ্রুত তারিখ (Timestamp)
    val categoryTag: String = "REGULAR", // "REGULAR", "VIP", "WHOLESALE", "HIGH_RISK"
    val nidOrGuarantor: String = "",    // জাতীয় পরিচয়পত্র / জামিনদার তথ্য
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
