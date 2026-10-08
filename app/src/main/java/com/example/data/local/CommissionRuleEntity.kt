package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "commission_rules",
    indices = [
        Index(value = ["monthYearKey", "brand", "modelReference"], unique = true)
    ]
)
data class CommissionRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val monthYearKey: String, // Format: "YYYY-MM"
    val brand: String, // e.g., "Samsung", "Apple", "Xiaomi"
    val modelReference: String? = null, // null or blank means base commission for the entire brand
    val commissionAmount: Double,
    val note: String = ""
)
