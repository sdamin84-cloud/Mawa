package com.example.mawa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_settings")
data class ShopSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val shopName: String = "মাওয়া ডিজিটাল খাতা",
    val ownerName: String = "দোকানদার",
    val openingBalance: Double = 0.0, // সাবেক ক্যাশ (Opening physical cash in hand)
    val currencySymbol: String = "৳",
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val appMode: String = "BOTH",
    val isModeConfigured: Boolean = false
)
