package com.example.mawa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mawa.data.local.entity.PersonalTransactionEntity
import com.example.mawa.data.local.entity.PersonalTransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalTransactionDao {

    @Query("SELECT * FROM personal_transactions ORDER BY timestamp DESC")
    fun getAllPersonalTransactions(): Flow<List<PersonalTransactionEntity>>

    @Query("SELECT * FROM personal_transactions WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getPersonalTransactionsBetween(startTime: Long, endTime: Long): Flow<List<PersonalTransactionEntity>>

    @Query("SELECT * FROM personal_transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getPersonalTransactionsByType(type: PersonalTransactionType): Flow<List<PersonalTransactionEntity>>

    @Query("SELECT * FROM personal_transactions WHERE type = :type AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getPersonalTransactionsByTypeAndRange(type: PersonalTransactionType, startTime: Long, endTime: Long): Flow<List<PersonalTransactionEntity>>

    @Query("SELECT * FROM personal_transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentPersonalTransactions(limit: Int): Flow<List<PersonalTransactionEntity>>

    @Query("SELECT * FROM personal_transactions WHERE id = :id")
    suspend fun getPersonalTransactionById(id: Long): PersonalTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: PersonalTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<PersonalTransactionEntity>)

    @Update
    suspend fun update(transaction: PersonalTransactionEntity)

    @Delete
    suspend fun delete(transaction: PersonalTransactionEntity)

    @Query("DELETE FROM personal_transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM personal_transactions")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM personal_transactions")
    suspend fun getCount(): Int
}
