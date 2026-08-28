package com.example.mawa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val banglaName: String = "",
    val unit: String = "কেজি",
    val defaultPurchasePrice: Double = 0.0,
    val defaultSellingPrice: Double = 0.0,
    val category: String = "মুদি",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
