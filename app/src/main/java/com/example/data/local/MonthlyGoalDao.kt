package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyGoalDao {

    @Query("SELECT * FROM monthly_goals WHERE monthYearKey = :monthKey LIMIT 1")
    fun getGoalForMonth(monthKey: String): Flow<MonthlyGoalEntity?>

    @Query("SELECT * FROM monthly_goals WHERE monthYearKey = :monthKey LIMIT 1")
    suspend fun getGoalForMonthSnapshot(monthKey: String): MonthlyGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGoal(goal: MonthlyGoalEntity)
}
