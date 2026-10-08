package com.example.data.repository

import com.example.data.local.BrandDao
import com.example.data.local.BrandEntity
import com.example.data.local.CommissionRuleDao
import com.example.data.local.CommissionRuleEntity
import com.example.data.local.MonthlyGoalDao
import com.example.data.local.MonthlyGoalEntity
import com.example.data.local.SaleDao
import com.example.data.local.SaleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CommissionsRepository(
    private val saleDao: SaleDao,
    private val ruleDao: CommissionRuleDao,
    private val goalDao: MonthlyGoalDao,
    private val brandDao: BrandDao
) {
    fun getSalesForMonth(monthKey: String): Flow<List<SaleEntity>> =
        saleDao.getSalesForMonth(monthKey)

    fun getAllSales(): Flow<List<SaleEntity>> =
        saleDao.getAllSales()

    fun getDistinctSaleMonths(): Flow<List<String>> =
        saleDao.getDistinctSaleMonths()

    suspend fun insertSale(sale: SaleEntity): Long = withContext(Dispatchers.IO) {
        saleDao.insertSale(sale)
    }

    suspend fun updateSale(sale: SaleEntity) = withContext(Dispatchers.IO) {
        saleDao.updateSale(sale)
    }

    suspend fun deleteSale(sale: SaleEntity) = withContext(Dispatchers.IO) {
        saleDao.deleteSale(sale)
    }

    suspend fun deleteSaleById(id: Long) = withContext(Dispatchers.IO) {
        saleDao.deleteSaleById(id)
    }

    fun getRulesForMonth(monthKey: String): Flow<List<CommissionRuleEntity>> =
        ruleDao.getRulesForMonth(monthKey)

    fun getDistinctRuleMonths(): Flow<List<String>> =
        ruleDao.getDistinctRuleMonths()

    suspend fun insertRule(rule: CommissionRuleEntity): Long = withContext(Dispatchers.IO) {
        ruleDao.insertRule(rule)
    }

    suspend fun updateRule(rule: CommissionRuleEntity) = withContext(Dispatchers.IO) {
        ruleDao.updateRule(rule)
    }

    suspend fun deleteRule(rule: CommissionRuleEntity) = withContext(Dispatchers.IO) {
        ruleDao.deleteRule(rule)
    }

    suspend fun deleteRuleById(id: Long) = withContext(Dispatchers.IO) {
        ruleDao.deleteRuleById(id)
    }

    suspend fun loadSuggestedRulesForMonth(monthKey: String) = withContext(Dispatchers.IO) {
        val existing = ruleDao.getRulesForMonthSnapshot(monthKey)
        if (existing.isEmpty()) {
            val defaults = DefaultTariffPresets.generateDefaultRulesForMonth(monthKey)
            ruleDao.insertRules(defaults)
        }
    }

    suspend fun copyRules(fromMonth: String, toMonth: String) = withContext(Dispatchers.IO) {
        val rules = ruleDao.getRulesForMonthSnapshot(fromMonth)
        if (rules.isNotEmpty()) {
            val copied = rules.map {
                it.copy(id = 0, monthYearKey = toMonth)
            }
            ruleDao.insertRules(copied)
        }
    }

    // --- Brand Management ---
    fun getAllBrands(): Flow<List<BrandEntity>> = brandDao.getAllBrands()

    fun getActiveBrands(): Flow<List<BrandEntity>> = brandDao.getActiveBrands()

    suspend fun getAllBrandsSnapshot(): List<BrandEntity> = withContext(Dispatchers.IO) {
        brandDao.getAllBrandsSnapshot()
    }

    suspend fun insertBrand(brand: BrandEntity): Long = withContext(Dispatchers.IO) {
        brandDao.insertBrand(brand)
    }

    suspend fun updateBrand(brand: BrandEntity) = withContext(Dispatchers.IO) {
        brandDao.updateBrand(brand)
    }

    suspend fun deleteBrand(brand: BrandEntity) = withContext(Dispatchers.IO) {
        brandDao.deleteBrand(brand)
    }

    suspend fun resetBrandsToDefaults() = withContext(Dispatchers.IO) {
        val defaults = DefaultTariffPresets.generateDefaultBrands()
        defaults.forEach { brandDao.insertBrand(it) }
    }

    // Set or update base monthly commission for a brand
    suspend fun setBrandMonthlyBaseRate(
        monthKey: String,
        brand: String,
        commissionAmount: Double,
        note: String = ""
    ) = withContext(Dispatchers.IO) {
        val existingRules = ruleDao.getRulesForMonthSnapshot(monthKey)
        val existingBaseRule = existingRules.firstOrNull {
            it.brand.equals(brand.trim(), ignoreCase = true) && it.modelReference.isNullOrBlank()
        }

        if (existingBaseRule != null) {
            ruleDao.updateRule(
                existingBaseRule.copy(
                    commissionAmount = commissionAmount,
                    note = note.ifBlank { existingBaseRule.note }
                )
            )
        } else {
            ruleDao.insertRule(
                CommissionRuleEntity(
                    monthYearKey = monthKey,
                    brand = brand.trim(),
                    modelReference = null,
                    commissionAmount = commissionAmount,
                    note = note.ifBlank { "Comisión base $brand para $monthKey" }
                )
            )
        }
    }

    // Apply default commission rates from brand catalog to the given month
    suspend fun applyDefaultBrandRatesToMonth(monthKey: String) = withContext(Dispatchers.IO) {
        val activeBrands = brandDao.getAllBrandsSnapshot().filter { it.isEnabled }
        activeBrands.forEach { brand ->
            setBrandMonthlyBaseRate(
                monthKey = monthKey,
                brand = brand.name,
                commissionAmount = brand.defaultCommission,
                note = "Tarifa por defecto del catálogo"
            )
        }
    }

    // Apply mass percentage or fixed bonus adjustment to all base rules of a month
    suspend fun applyAdjustmentToMonth(
        monthKey: String,
        percentIncrease: Double?,
        fixedBonus: Double?
    ) = withContext(Dispatchers.IO) {
        val rules = ruleDao.getRulesForMonthSnapshot(monthKey)
        val updated = rules.map { rule ->
            var newAmount = rule.commissionAmount
            if (percentIncrease != null && percentIncrease != 0.0) {
                newAmount += newAmount * (percentIncrease / 100.0)
            }
            if (fixedBonus != null && fixedBonus != 0.0) {
                newAmount += fixedBonus
            }
            // Round to nearest 500 for clean currency display
            val rounded = (Math.round(newAmount / 500.0) * 500).toDouble().coerceAtLeast(0.0)
            rule.copy(commissionAmount = rounded)
        }
        ruleDao.insertRules(updated)
    }

    suspend fun calculateEstimatedCommission(
        monthKey: String,
        brand: String,
        modelReference: String
    ): Double = withContext(Dispatchers.IO) {
        val rules = ruleDao.getRulesForMonthSnapshot(monthKey)
        
        // 1. Check exact or model reference match
        val modelRule = rules.firstOrNull {
            it.brand.equals(brand.trim(), ignoreCase = true) &&
                    !it.modelReference.isNullOrBlank() &&
                    (it.modelReference.equals(modelReference.trim(), ignoreCase = true) ||
                            modelReference.contains(it.modelReference, ignoreCase = true))
        }
        if (modelRule != null) {
            return@withContext modelRule.commissionAmount
        }

        // 2. Check brand general base rule
        val brandRule = rules.firstOrNull {
            it.brand.equals(brand.trim(), ignoreCase = true) &&
                    it.modelReference.isNullOrBlank()
        }
        if (brandRule != null) {
            return@withContext brandRule.commissionAmount
        }

        // 3. Fallback to configured brand in DB
        val dbBrand = brandDao.getBrandByName(brand.trim())
        if (dbBrand != null) {
            return@withContext dbBrand.defaultCommission
        }

        // 4. Fallback to default presets if not in DB yet
        val defaultBrand = DefaultTariffPresets.POPULAR_BRANDS.firstOrNull {
            it.brand.equals(brand.trim(), ignoreCase = true)
        }
        val defaultModel = defaultBrand?.models?.firstOrNull {
            it.modelName.equals(modelReference.trim(), ignoreCase = true)
        }

        defaultModel?.specialCommission ?: defaultBrand?.defaultCommission ?: 30000.0
    }

    fun getGoalForMonth(monthKey: String): Flow<MonthlyGoalEntity?> =
        goalDao.getGoalForMonth(monthKey)

    suspend fun saveGoal(goal: MonthlyGoalEntity) = withContext(Dispatchers.IO) {
        goalDao.insertOrUpdateGoal(goal)
    }

    suspend fun seedInitialDataIfEmpty(currentMonthKey: String) = withContext(Dispatchers.IO) {
        if (brandDao.getBrandCount() == 0) {
            val defaultBrands = DefaultTariffPresets.generateDefaultBrands()
            brandDao.insertBrands(defaultBrands)
        }

        val existingRules = ruleDao.getRulesForMonthSnapshot(currentMonthKey)
        if (existingRules.isEmpty()) {
            val defaults = DefaultTariffPresets.generateDefaultRulesForMonth(currentMonthKey)
            ruleDao.insertRules(defaults)
        }

        val existingGoal = goalDao.getGoalForMonthSnapshot(currentMonthKey)
        if (existingGoal == null) {
            goalDao.insertOrUpdateGoal(
                MonthlyGoalEntity(
                    monthYearKey = currentMonthKey,
                    targetSalesCount = 25,
                    targetCommissionAmount = 1400000.0,
                    advisorName = "Asesor Call Center",
                    campaignName = "Portabilidad & Renovación Móvil"
                )
            )
        }

        val allSales = saleDao.getAllSales().firstOrNull()
        if (allSales.isNullOrEmpty()) {
            val cal = Calendar.getInstance()
            val now = System.currentTimeMillis()

            val demoSales = listOf(
                SaleEntity(
                    dateMillis = now - 2 * 3600 * 1000L,
                    monthYearKey = currentMonthKey,
                    brand = "Samsung",
                    modelReference = "Galaxy S24 Ultra 5G",
                    commissionAmount = 85000.0,
                    salePrice = 5499000.0,
                    orderNumber = "RAD-89412",
                    clientName = "Carlos Mendoza",
                    clientPhone = "3104589211",
                    saleType = "Portabilidad",
                    status = "APROBADA",
                    notes = "Cliente aceptó plan postpago ilimitado 5G con portabilidad exitosa."
                ),
                SaleEntity(
                    dateMillis = now - 18 * 3600 * 1000L,
                    monthYearKey = currentMonthKey,
                    brand = "Samsung",
                    modelReference = "Galaxy A55 5G",
                    commissionAmount = 45000.0,
                    salePrice = 1799000.0,
                    orderNumber = "RAD-89388",
                    clientName = "Lucia Ramírez",
                    clientPhone = "3156721098",
                    saleType = "Renovación",
                    status = "APROBADA",
                    notes = "Renovación anticipada a 24 meses."
                ),
                SaleEntity(
                    dateMillis = now - 42 * 3600 * 1000L,
                    monthYearKey = currentMonthKey,
                    brand = "Apple",
                    modelReference = "iPhone 15 Pro",
                    commissionAmount = 85000.0,
                    salePrice = 4899000.0,
                    orderNumber = "RAD-89310",
                    clientName = "Andrés Gómez",
                    clientPhone = "3209841234",
                    saleType = "Línea Nueva",
                    status = "EN_VALIDACION",
                    notes = "Pendiente aprobación de crédito en buró financiero."
                ),
                SaleEntity(
                    dateMillis = now - 65 * 3600 * 1000L,
                    monthYearKey = currentMonthKey,
                    brand = "Xiaomi",
                    modelReference = "Redmi Note 13 Pro+ 5G",
                    commissionAmount = 50000.0,
                    salePrice = 1699000.0,
                    orderNumber = "RAD-89240",
                    clientName = "Mariana Torres",
                    clientPhone = "3008912345",
                    saleType = "Portabilidad",
                    status = "APROBADA",
                    notes = "Portabilidad desde operador competidor."
                ),
                SaleEntity(
                    dateMillis = now - 90 * 3600 * 1000L,
                    monthYearKey = currentMonthKey,
                    brand = "Motorola",
                    modelReference = "Moto G84 5G",
                    commissionAmount = 38000.0,
                    salePrice = 1199000.0,
                    orderNumber = "RAD-89190",
                    clientName = "Javier Ortiz",
                    clientPhone = "3114567890",
                    saleType = "Portabilidad",
                    status = "APROBADA",
                    notes = "Venta telefónica de cierre rápido."
                ),
                SaleEntity(
                    dateMillis = now - 120 * 3600 * 1000L,
                    monthYearKey = currentMonthKey,
                    brand = "Samsung",
                    modelReference = "Galaxy S24 Ultra 5G",
                    commissionAmount = 85000.0,
                    salePrice = 5499000.0,
                    orderNumber = "RAD-89115",
                    clientName = "Santiago Vélez",
                    clientPhone = "3189923412",
                    saleType = "Renovación",
                    status = "APROBADA",
                    notes = "Gama alta premium con bonificación."
                )
            )

            demoSales.forEach { saleDao.insertSale(it) }

            // Also seed previous month data to illustrate monthly history right away!
            cal.time = Date()
            cal.add(Calendar.MONTH, -1)
            val prevMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val prevMonthKey = prevMonthFormat.format(cal.time)

            val prevDefaults = DefaultTariffPresets.generateDefaultRulesForMonth(prevMonthKey)
            ruleDao.insertRules(prevDefaults)

            goalDao.insertOrUpdateGoal(
                MonthlyGoalEntity(
                    monthYearKey = prevMonthKey,
                    targetSalesCount = 20,
                    targetCommissionAmount = 1000000.0,
                    advisorName = "Asesor Call Center",
                    campaignName = "Portabilidad & Renovación Móvil"
                )
            )

            val prevMonthSales = listOf(
                SaleEntity(
                    dateMillis = cal.timeInMillis,
                    monthYearKey = prevMonthKey,
                    brand = "Samsung",
                    modelReference = "Galaxy S24 Ultra 5G",
                    commissionAmount = 80000.0,
                    salePrice = 5399000.0,
                    orderNumber = "RAD-77210",
                    clientName = "David Ospina",
                    clientPhone = "3127894561",
                    saleType = "Portabilidad",
                    status = "APROBADA",
                    notes = "Mes anterior"
                ),
                SaleEntity(
                    dateMillis = cal.timeInMillis - 24 * 3600 * 1000L,
                    monthYearKey = prevMonthKey,
                    brand = "Apple",
                    modelReference = "iPhone 15",
                    commissionAmount = 70000.0,
                    salePrice = 3999000.0,
                    orderNumber = "RAD-77180",
                    clientName = "Paula Andrea",
                    clientPhone = "3145678901",
                    saleType = "Línea Nueva",
                    status = "APROBADA",
                    notes = "Mes anterior"
                ),
                SaleEntity(
                    dateMillis = cal.timeInMillis - 48 * 3600 * 1000L,
                    monthYearKey = prevMonthKey,
                    brand = "Xiaomi",
                    modelReference = "Redmi Note 13",
                    commissionAmount = 35000.0,
                    salePrice = 899000.0,
                    orderNumber = "RAD-77112",
                    clientName = "Mateo Ruiz",
                    clientPhone = "3103456789",
                    saleType = "Renovación",
                    status = "APROBADA",
                    notes = "Mes anterior"
                )
            )
            prevMonthSales.forEach { saleDao.insertSale(it) }
        }
    }
}
