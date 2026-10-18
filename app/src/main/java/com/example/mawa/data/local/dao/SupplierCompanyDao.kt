package com.example.mawa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mawa.data.local.entity.SupplierCompanyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierCompanyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCompany(company: SupplierCompanyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanies(companies: List<SupplierCompanyEntity>)

    @Update
    suspend fun updateCompany(company: SupplierCompanyEntity)

    @Delete
    suspend fun deleteCompany(company: SupplierCompanyEntity)

    @Query("DELETE FROM supplier_companies WHERE id = :id")
    suspend fun deleteCompanyById(id: Long)

    @Query("SELECT * FROM supplier_companies ORDER BY companyName ASC")
    fun getAllCompanies(): Flow<List<SupplierCompanyEntity>>

    @Query("SELECT * FROM supplier_companies")
    suspend fun getAllCompaniesDirect(): List<SupplierCompanyEntity>

    @Query("SELECT * FROM supplier_companies WHERE id = :id LIMIT 1")
    suspend fun getCompanyById(id: Long): SupplierCompanyEntity?

    @Query("SELECT * FROM supplier_companies WHERE LOWER(TRIM(companyName)) = LOWER(TRIM(:name)) LIMIT 1")
    suspend fun getCompanyByName(name: String): SupplierCompanyEntity?

    @Query("SELECT * FROM supplier_companies WHERE orderDay = :day ORDER BY companyName ASC")
    fun getCompaniesByOrderDay(day: String): Flow<List<SupplierCompanyEntity>>
}
