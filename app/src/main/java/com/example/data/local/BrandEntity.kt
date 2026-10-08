package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "brands",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class BrandEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // e.g., "Samsung", "Apple", "Xiaomi", "Motorola", "Honor", "Oppo", "Realme"
    val defaultCommission: Double = 35000.0,
    val isEnabled: Boolean = true,
    val suggestedModels: String = "", // Comma-separated list of popular models
    val colorHex: String = "#1E88E5",
    val notes: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)
