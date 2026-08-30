package com.example.mawa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mawa.data.local.entity.DailyCashEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyCashDao {
    @Query("SELECT * FROM daily_cash_records WHERE dateKey = :dateKey LIMIT 1")
    fun getDailyCashFlow(dateKey: String): Flow<DailyCashEntity?>

    @Query("SELECT * FROM daily_cash_records WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getDailyCashDirect(dateKey: String): DailyCashEntity?

    @Query("SELECT * FROM daily_cash_records ORDER BY dateMillis DESC")
    fun getAllDailyCashFlow(): Flow<List<DailyCashEntity>>

    @Query("SELECT * FROM daily_cash_records ORDER BY dateMillis DESC")
    suspend fun getAllDailyCashDirect(): List<DailyCashEntity>

    @Query("SELECT * FROM daily_cash_records WHERE dateMillis < :dateMillis AND closingCash > 0 ORDER BY dateMillis DESC LIMIT 1")
    suspend fun getPreviousDailyCash(dateMillis: Long): DailyCashEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyCash(record: DailyCashEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyCashList(records: List<DailyCashEntity>)

    @Query("DELETE FROM daily_cash_records")
    suspend fun deleteAllDailyCash()
}
