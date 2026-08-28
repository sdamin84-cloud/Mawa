package com.example.mawa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mawa.data.local.entity.FordiItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FordiDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFordiItem(item: FordiItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFordiItems(items: List<FordiItemEntity>)

    @Update
    suspend fun updateFordiItem(item: FordiItemEntity)

    @Delete
    suspend fun deleteFordiItem(item: FordiItemEntity)

    @Query("DELETE FROM fordi_items WHERE id = :id")
    suspend fun deleteFordiItemById(id: Long)

    @Query("DELETE FROM fordi_items WHERE isPurchased = 1")
    suspend fun clearCompletedFordi()

    @Query("DELETE FROM fordi_items WHERE isPurchased = 0")
    suspend fun clearPendingFordi()

    @Query("DELETE FROM fordi_items")
    suspend fun deleteAllFordiItems()

    @Query("SELECT * FROM fordi_items ORDER BY isPurchased ASC, id DESC")
    fun getAllFordiItems(): Flow<List<FordiItemEntity>>

    @Query("SELECT * FROM fordi_items WHERE isPurchased = 0 ORDER BY id DESC")
    fun getPendingFordiItems(): Flow<List<FordiItemEntity>>

    @Query("SELECT * FROM fordi_items WHERE isPurchased = 1 ORDER BY purchaseDate DESC")
    fun getPurchasedFordiItems(): Flow<List<FordiItemEntity>>

    @Query("SELECT COUNT(*) FROM fordi_items")
    suspend fun getFordiItemCount(): Int
}
