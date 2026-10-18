package com.example.mawa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mawa.data.local.entity.CashTallyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashTallyDao {
    @Query("SELECT * FROM cash_tally_records WHERE dateKey = :dateKey ORDER BY timestamp DESC")
    fun getTalliesForDateFlow(dateKey: String): Flow<List<CashTallyEntity>>

    @Query("SELECT * FROM cash_tally_records WHERE dateKey = :dateKey ORDER BY timestamp DESC LIMIT 1")
    fun getLatestTallyForDateFlow(dateKey: String): Flow<CashTallyEntity?>

    @Query("SELECT * FROM cash_tally_records WHERE dateKey = :dateKey ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestTallyForDate(dateKey: String): CashTallyEntity?

    @Query("SELECT * FROM cash_tally_records ORDER BY timestamp DESC")
    fun getAllTalliesFlow(): Flow<List<CashTallyEntity>>

    @Query("SELECT * FROM cash_tally_records ORDER BY timestamp DESC")
    suspend fun getAllTalliesDirect(): List<CashTallyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTally(tally: CashTallyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTallies(tallies: List<CashTallyEntity>)

    @Update
    suspend fun updateTally(tally: CashTallyEntity)

    @Delete
    suspend fun deleteTally(tally: CashTallyEntity)

    @Query("DELETE FROM cash_tally_records WHERE id = :id")
    suspend fun deleteTallyById(id: Long)

    @Query("DELETE FROM cash_tally_records")
    suspend fun deleteAllTallies()
}
