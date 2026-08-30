package com.example.mawa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_cash_records")
data class DailyCashEntity(
    @PrimaryKey
    val dateKey: String, // Format: "dd-MM-yyyy" (e.g. "29-08-2026")
    val dateMillis: Long,
    val sabekCash: Double = 0.0,
    val closingCash: Double = 0.0, // হাতে থাকা সমাপনী ক্যাশ
    val isClosed: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
