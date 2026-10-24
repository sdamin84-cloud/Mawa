package com.example.mawa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Order record for company orders (অর্ডার খাতা)
 * Used to track which company, how much money/taka goods ordered, notes,
 * and whether the goods have been received/confirmed.
 */
@Entity(tableName = "company_orders")
data class CompanyOrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyName: String,
    val totalAmount: Double,
    val orderDate: Long = System.currentTimeMillis(),
    val orderDateString: String = "",
    val note: String = "",
    val status: String = "PENDING", // "PENDING", "CONFIRMED", "CANCELLED"
    val confirmedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
