package com.example.mawa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing Company Profile & Sales Representative Contact Information
 */
@Entity(tableName = "supplier_companies")
data class SupplierCompanyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyName: String,
    val srName: String = "",
    val srPhone: String = "",
    val orderDay: String = "",       // e.g. "রবিবার", "সোমবার"
    val deliveryDay: String = "",    // e.g. "সোমবার", "মঙ্গলবার"
    val address: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
