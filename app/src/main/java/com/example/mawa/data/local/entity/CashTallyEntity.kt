package com.example.mawa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cash_tally_records")
data class CashTallyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateKey: String, // "dd-MM-yyyy"
    val dateMillis: Long,
    val count1000: Int = 0,
    val count500: Int = 0,
    val count200: Int = 0,
    val count100: Int = 0,
    val count50: Int = 0,
    val count20: Int = 0,
    val count10: Int = 0,
    val count5: Int = 0,
    val count2: Int = 0,
    val count1: Int = 0,
    val totalAmount: Double = 0.0,
    val systemBalance: Double = 0.0,
    val difference: Double = 0.0,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun totalNotesCount(): Int =
        count1000 + count500 + count200 + count100 + count50 + count20 + count10 + count5 + count2 + count1

    fun getDenominationsBreakdown(): List<Pair<Int, Int>> {
        val list = mutableListOf<Pair<Int, Int>>()
        if (count1000 > 0) list.add(1000 to count1000)
        if (count500 > 0) list.add(500 to count500)
        if (count200 > 0) list.add(200 to count200)
        if (count100 > 0) list.add(100 to count100)
        if (count50 > 0) list.add(50 to count50)
        if (count20 > 0) list.add(20 to count20)
        if (count10 > 0) list.add(10 to count10)
        if (count5 > 0) list.add(5 to count5)
        if (count2 > 0) list.add(2 to count2)
        if (count1 > 0) list.add(1 to count1)
        return list
    }
}
