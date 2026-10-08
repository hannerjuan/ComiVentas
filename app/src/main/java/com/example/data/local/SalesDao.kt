package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesDao {
    @Query("SELECT * FROM sales ORDER BY saleDate DESC, id DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE monthKey = :monthKey ORDER BY saleDate DESC, id DESC")
    fun getSalesByMonth(monthKey: String): Flow<List<SaleEntity>>

    @Query("SELECT DISTINCT monthKey FROM sales ORDER BY monthKey DESC")
    fun getDistinctMonthKeys(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Delete
    suspend fun deleteSale(sale: SaleEntity)

    // Commission Rules
    @Query("SELECT * FROM commission_rules ORDER BY brand ASC")
    fun getAllCommissionRules(): Flow<List<CommissionRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommissionRule(rule: CommissionRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRules(rules: List<CommissionRuleEntity>)

    @Update
    suspend fun updateCommissionRule(rule: CommissionRuleEntity)

    @Delete
    suspend fun deleteCommissionRule(rule: CommissionRuleEntity)

    // Profile
    @Query("SELECT * FROM advisor_profile WHERE id = 1")
    fun getAdvisorProfile(): Flow<AdvisorProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAdvisorProfile(profile: AdvisorProfileEntity)
}
