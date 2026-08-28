package com.example.mawa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fordi_items")
data class FordiItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long? = null,
    val productName: String,
    val plannedQuantity: Double = 1.0,
    val unit: String = "কেজি",
    val purchaseRate: Double = 0.0,
    val sellingRate: Double = 0.0,
    val isPurchased: Boolean = false,
    val actualQuantity: Double = 0.0,
    val actualRate: Double = 0.0,
    val actualTotal: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val purchaseDate: Long? = null
)
