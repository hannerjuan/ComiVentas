package com.example.data.repository

import com.example.data.local.BrandEntity
import com.example.data.local.CommissionRuleEntity

object DefaultTariffPresets {

    data class BrandPreset(
        val brand: String,
        val defaultCommission: Double,
        val models: List<ModelPreset>
    )

    data class ModelPreset(
        val modelName: String,
        val specialCommission: Double? = null
    )

    val POPULAR_BRANDS = listOf(
        BrandPreset(
            brand = "Samsung",
            defaultCommission = 40000.0,
            models = listOf(
                ModelPreset("Galaxy S24 Ultra 5G", 85000.0),
                ModelPreset("Galaxy S24+ 5G", 70000.0),
                ModelPreset("Galaxy S24 5G", 65000.0),
                ModelPreset("Galaxy S23 FE 5G", 50000.0),
                ModelPreset("Galaxy A55 5G", 45000.0),
                ModelPreset("Galaxy A35 5G", 40000.0),
                ModelPreset("Galaxy A25 5G", 38000.0),
                ModelPreset("Galaxy A15 LTE", 32000.0),
                ModelPreset("Galaxy A05s", 28000.0)
            )
        ),
        BrandPreset(
            brand = "Apple",
            defaultCommission = 50000.0,
            models = listOf(
                ModelPreset("iPhone 15 Pro Max", 95000.0),
                ModelPreset("iPhone 15 Pro", 85000.0),
                ModelPreset("iPhone 15 Plus", 75000.0),
                ModelPreset("iPhone 15", 70000.0),
                ModelPreset("iPhone 14", 55000.0),
                ModelPreset("iPhone 13", 50000.0),
                ModelPreset("iPhone 11", 40000.0)
            )
        ),
        BrandPreset(
            brand = "Xiaomi",
            defaultCommission = 35000.0,
            models = listOf(
                ModelPreset("Xiaomi 14 Ultra", 80000.0),
                ModelPreset("Redmi Note 13 Pro+ 5G", 50000.0),
                ModelPreset("Redmi Note 13 Pro 4G", 42000.0),
                ModelPreset("Redmi Note 13", 35000.0),
                ModelPreset("POCO X6 Pro 5G", 45000.0),
                ModelPreset("Redmi 13C", 28000.0)
            )
        ),
        BrandPreset(
            brand = "Motorola",
            defaultCommission = 32000.0,
            models = listOf(
                ModelPreset("Motorola Edge 50 Ultra", 75000.0),
                ModelPreset("Motorola Edge 50 Pro", 60000.0),
                ModelPreset("Moto G84 5G", 38000.0),
                ModelPreset("Moto G54 5G", 34000.0),
                ModelPreset("Moto G24 Power", 30000.0),
                ModelPreset("Moto G04", 25000.0)
            )
        ),
        BrandPreset(
            brand = "Honor",
            defaultCommission = 32000.0,
            models = listOf(
                ModelPreset("Honor Magic 6 Pro", 80000.0),
                ModelPreset("Honor 200 Pro", 65000.0),
                ModelPreset("Honor Magic 6 Lite 5G", 42000.0),
                ModelPreset("Honor X8b", 32000.0),
                ModelPreset("Honor X6a", 26000.0)
            )
        ),
        BrandPreset(
            brand = "Oppo",
            defaultCommission = 30000.0,
            models = listOf(
                ModelPreset("Oppo Reno 11 5G", 45000.0),
                ModelPreset("Oppo A79 5G", 35000.0),
                ModelPreset("Oppo A58", 30000.0),
                ModelPreset("Oppo A38", 26000.0)
            )
        ),
        BrandPreset(
            brand = "Realme",
            defaultCommission = 30000.0,
            models = listOf(
                ModelPreset("Realme 12 Pro+ 5G", 52000.0),
                ModelPreset("Realme 11 5G", 38000.0),
                ModelPreset("Realme C67", 30000.0),
                ModelPreset("Realme C53", 26000.0)
            )
        )
    )

    fun generateDefaultRulesForMonth(monthKey: String): List<CommissionRuleEntity> {
        val rules = mutableListOf<CommissionRuleEntity>()
        POPULAR_BRANDS.forEach { brandPreset ->
            // Base rule for the brand
            rules.add(
                CommissionRuleEntity(
                    monthYearKey = monthKey,
                    brand = brandPreset.brand,
                    modelReference = null,
                    commissionAmount = brandPreset.defaultCommission,
                    note = "Comisión base por venta ${brandPreset.brand}"
                )
            )
            // Model-specific overrides if defined
            brandPreset.models.forEach { modelPreset ->
                if (modelPreset.specialCommission != null && modelPreset.specialCommission != brandPreset.defaultCommission) {
                    rules.add(
                        CommissionRuleEntity(
                            monthYearKey = monthKey,
                            brand = brandPreset.brand,
                            modelReference = modelPreset.modelName,
                            commissionAmount = modelPreset.specialCommission,
                            note = "Comisión destacada para ${modelPreset.modelName}"
                        )
                    )
                }
            }
        }
        return rules
    }

    fun generateDefaultBrands(): List<BrandEntity> {
        val brandColors = mapOf(
            "Samsung" to "#0C2340",
            "Apple" to "#555555",
            "Xiaomi" to "#FF6900",
            "Motorola" to "#001489",
            "Honor" to "#0091FF",
            "Oppo" to "#00875A",
            "Realme" to "#EAB308"
        )
        return POPULAR_BRANDS.map { preset ->
            BrandEntity(
                name = preset.brand,
                defaultCommission = preset.defaultCommission,
                isEnabled = true,
                suggestedModels = preset.models.joinToString(", ") { it.modelName },
                colorHex = brandColors[preset.brand] ?: "#1E88E5",
                notes = "Marca líder configurada por defecto"
            )
        }
    }
}
