package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val brand: String,
    val model: String,
    val price: Double,
    val commission: Double,
    val saleDate: String, // YYYY-MM-DD
    val monthKey: String, // YYYY-MM
    val customerName: String = "",
    val imeiOrContract: String = "",
    val status: String = "APROBADA", // APROBADA, EN_VALIDACION, ANULADA
    val notes: String = ""
)

@Entity(tableName = "commission_rules")
data class CommissionRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val brand: String,
    val fixedCommission: Double,
    val percentageCommission: Double = 0.0
)

@Entity(tableName = "advisor_profile")
data class AdvisorProfileEntity(
    @PrimaryKey
    val id: Long = 1,
    val advisorName: String = "Asesor Comercial",
    val campaignName: String = "Campaña Móvil Postpago",
    val monthlyTargetAmount: Double = 2500000.0,
    val monthlyTargetUnits: Int = 30
)
