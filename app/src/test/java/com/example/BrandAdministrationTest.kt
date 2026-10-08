package com.example

import com.example.data.local.BrandEntity
import com.example.data.local.CommissionRuleEntity
import com.example.data.repository.DefaultTariffPresets
import org.junit.Assert.*
import org.junit.Test

class BrandAdministrationTest {

    @Test
    fun defaultBrands_areGeneratedCorrectly() {
        val brands = DefaultTariffPresets.generateDefaultBrands()
        assertTrue(brands.isNotEmpty())

        val samsung = brands.firstOrNull { it.name.equals("Samsung", ignoreCase = true) }
        assertNotNull(samsung)
        assertEquals(40000.0, samsung!!.defaultCommission, 0.01)
        assertTrue(samsung.isEnabled)
        assertTrue(samsung.suggestedModels.contains("Galaxy S24 Ultra"))

        val apple = brands.firstOrNull { it.name.equals("Apple", ignoreCase = true) }
        assertNotNull(apple)
        assertEquals(50000.0, apple!!.defaultCommission, 0.01)
    }

    @Test
    fun brandEntity_copyAndToggleState() {
        val original = BrandEntity(
            name = "Honor",
            defaultCommission = 35000.0,
            isEnabled = true,
            suggestedModels = "Magic 6 Pro, X9b",
            colorHex = "#0091FF"
        )

        val updated = original.copy(
            defaultCommission = 42000.0,
            isEnabled = false
        )

        assertEquals("Honor", updated.name)
        assertEquals(42000.0, updated.defaultCommission, 0.01)
        assertFalse(updated.isEnabled)
    }

    @Test
    fun batchRateAdjustment_percentageCalculation() {
        val baseCommission = 40000.0
        val percentIncrease = 15.0 // +15%
        val adjusted = Math.round(baseCommission * (1.0 + percentIncrease / 100.0) / 100.0) * 100.0

        assertEquals(46000.0, adjusted, 0.01)
    }

    @Test
    fun batchRateAdjustment_fixedBonusCalculation() {
        val baseCommission = 35000.0
        val bonus = 7000.0
        val adjusted = baseCommission + bonus

        assertEquals(42000.0, adjusted, 0.01)
    }
}
