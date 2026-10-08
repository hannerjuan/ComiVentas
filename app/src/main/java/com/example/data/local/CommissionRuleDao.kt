package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CommissionRuleDao {

    @Query("SELECT * FROM commission_rules WHERE monthYearKey = :monthKey ORDER BY brand ASC, modelReference ASC")
    fun getRulesForMonth(monthKey: String): Flow<List<CommissionRuleEntity>>

    @Query("SELECT * FROM commission_rules WHERE monthYearKey = :monthKey")
    suspend fun getRulesForMonthSnapshot(monthKey: String): List<CommissionRuleEntity>

    @Query("SELECT DISTINCT monthYearKey FROM commission_rules ORDER BY monthYearKey DESC")
    fun getDistinctRuleMonths(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: CommissionRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<CommissionRuleEntity>)

    @Update
    suspend fun updateRule(rule: CommissionRuleEntity)

    @Delete
    suspend fun deleteRule(rule: CommissionRuleEntity)

    @Query("DELETE FROM commission_rules WHERE id = :id")
    suspend fun deleteRuleById(id: Long)

    @Query("DELETE FROM commission_rules WHERE monthYearKey = :monthKey")
    suspend fun deleteRulesForMonth(monthKey: String)
}
