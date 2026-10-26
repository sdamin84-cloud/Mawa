package com.example.mawa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mawa.data.local.entity.CompanyOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyOrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: CompanyOrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<CompanyOrderEntity>)

    @Update
    suspend fun updateOrder(order: CompanyOrderEntity)

    @Query("SELECT * FROM company_orders ORDER BY id DESC")
    fun getAllOrders(): Flow<List<CompanyOrderEntity>>

    @Query("SELECT * FROM company_orders ORDER BY id DESC")
    suspend fun getAllOrdersDirect(): List<CompanyOrderEntity>

    @Query("SELECT * FROM company_orders WHERE status = :status ORDER BY id DESC")
    fun getOrdersByStatus(status: String): Flow<List<CompanyOrderEntity>>

    @Query("UPDATE company_orders SET status = 'CONFIRMED', confirmedAt = :confirmedTime WHERE id = :id")
    suspend fun confirmOrder(id: Long, confirmedTime: Long = System.currentTimeMillis())

    @Query("UPDATE company_orders SET status = 'CANCELLED' WHERE id = :id")
    suspend fun cancelOrder(id: Long)

    @Query("DELETE FROM company_orders WHERE id = :id")
    suspend fun deleteOrder(id: Long)

    @Query("SELECT COUNT(*) FROM company_orders")
    suspend fun getOrderCount(): Int
}
