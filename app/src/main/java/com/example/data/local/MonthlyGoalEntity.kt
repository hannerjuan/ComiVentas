package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_goals")
data class MonthlyGoalEntity(
    @PrimaryKey
    val monthYearKey: String, // Format: "YYYY-MM"
    val targetSalesCount: Int = 30,
    val targetCommissionAmount: Double = 1500000.0,
    val advisorName: String = "Asesor Call Center",
    val campaignName: String = "Venta Telefónica Móvil"
)
