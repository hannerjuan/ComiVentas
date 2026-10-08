package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    indices = [
        Index(value = ["monthYearKey"]),
        Index(value = ["brand"]),
        Index(value = ["dateMillis"])
    ]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateMillis: Long,
    val monthYearKey: String, // Format: "YYYY-MM" (e.g., "2026-09")
    val brand: String,
    val modelReference: String,
    val commissionAmount: Double,
    val salePrice: Double = 0.0,
    val orderNumber: String = "", // Radicado / MIN / Contrato
    val clientName: String = "",
    val clientPhone: String = "",
    val saleType: String = "Portabilidad", // Portabilidad, Línea Nueva, Renovación, Equipo Libre
    val status: String = "APROBADA", // APROBADA, EN_VALIDACION, ENTREGADA, ANULADA
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
