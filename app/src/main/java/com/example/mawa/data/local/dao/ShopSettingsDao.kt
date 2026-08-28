package com.example.mawa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mawa.data.local.entity.ShopSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: ShopSettingsEntity)

    @Update
    suspend fun updateSettings(settings: ShopSettingsEntity)

    @Query("SELECT * FROM shop_settings WHERE id = 1")
    fun getSettings(): Flow<ShopSettingsEntity?>

    @Query("SELECT * FROM shop_settings WHERE id = 1")
    suspend fun getSettingsDirect(): ShopSettingsEntity?
}
