package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BrandEntity
import com.example.data.local.CommissionRuleEntity
import com.example.data.local.MonthlyGoalEntity
import com.example.data.local.SaleEntity
import com.example.data.repository.CommissionsRepository
import com.example.ui.model.Formatters
import com.example.ui.model.MonthHistoryItem
import com.example.ui.model.MonthlySummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class CommissionsViewModel(
    application: Application,
    private val repository: CommissionsRepository
) : AndroidViewModel(application) {

    private val _selectedMonthKey = MutableStateFlow(Formatters.getCurrentMonthKey())
    val selectedMonthKey: StateFlow<String> = _selectedMonthKey.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("TODOS") // TODOS, APROBADA, EN_VALIDACION, ANULADA
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _brandFilter = MutableStateFlow("TODAS")
    val brandFilter: StateFlow<String> = _brandFilter.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty(_selectedMonthKey.value)
        }
    }

    val allBrands: StateFlow<List<BrandEntity>> = repository.getAllBrands()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeBrands: StateFlow<List<BrandEntity>> = repository.getActiveBrands()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val rulesForSelectedMonth: StateFlow<List<CommissionRuleEntity>> = _selectedMonthKey
        .flatMapLatest { monthKey ->
            repository.getRulesForMonth(monthKey)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentGoal: StateFlow<MonthlyGoalEntity?> = _selectedMonthKey
        .flatMapLatest { monthKey ->
            repository.getGoalForMonth(monthKey)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val rawSalesForSelectedMonth: StateFlow<List<SaleEntity>> = _selectedMonthKey
        .flatMapLatest { monthKey ->
            repository.getSalesForMonth(monthKey)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredSales: StateFlow<List<SaleEntity>> = combine(
        rawSalesForSelectedMonth,
        _searchQuery,
        _statusFilter,
        _brandFilter
    ) { sales, query, status, brand ->
        sales.filter { sale ->
            val matchesQuery = query.isBlank() ||
                    sale.modelReference.contains(query, ignoreCase = true) ||
                    sale.brand.contains(query, ignoreCase = true) ||
                    sale.orderNumber.contains(query, ignoreCase = true) ||
                    sale.clientName.contains(query, ignoreCase = true) ||
                    sale.clientPhone.contains(query, ignoreCase = true)

            val matchesStatus = status == "TODOS" || sale.status == status
            val matchesBrand = brand == "TODAS" || sale.brand.equals(brand, ignoreCase = true)

            matchesQuery && matchesStatus && matchesBrand
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allSales: StateFlow<List<SaleEntity>> = repository.getAllSales()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val availableMonths: StateFlow<List<String>> = combine(
        repository.getDistinctSaleMonths(),
        repository.getDistinctRuleMonths(),
        _selectedMonthKey
    ) { saleMonths, ruleMonths, current ->
        val set = sortedSetOf(Comparator.reverseOrder<String>())
        set.addAll(saleMonths)
        set.addAll(ruleMonths)
        set.add(current)
        set.add(Formatters.getCurrentMonthKey())
        set.toList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(Formatters.getCurrentMonthKey())
    )

    val monthlySummary: StateFlow<MonthlySummary> = combine(
        rawSalesForSelectedMonth,
        currentGoal,
        _selectedMonthKey
    ) { sales, goal, monthKey ->
        val activeSales = sales.filter { it.status != "ANULADA" }
        val approvedSales = sales.filter { it.status == "APROBADA" }
        val pendingSales = sales.filter { it.status == "EN_VALIDACION" }
        val cancelledSales = sales.filter { it.status == "ANULADA" }

        val totalComm = activeSales.sumOf { it.commissionAmount }
        val approvedComm = approvedSales.sumOf { it.commissionAmount }
        val pendingComm = pendingSales.sumOf { it.commissionAmount }

        val brandCounts = mutableMapOf<String, Int>()
        val brandComms = mutableMapOf<String, Double>()
        val modelCounts = mutableMapOf<String, Int>()

        activeSales.forEach { sale ->
            brandCounts[sale.brand] = (brandCounts[sale.brand] ?: 0) + 1
            brandComms[sale.brand] = (brandComms[sale.brand] ?: 0.0) + sale.commissionAmount
            val modelKey = "${sale.brand} ${sale.modelReference}"
            modelCounts[modelKey] = (modelCounts[modelKey] ?: 0) + 1
        }

        val topBrand = brandCounts.maxByOrNull { it.value }?.key ?: "-"
        val topModel = modelCounts.maxByOrNull { it.value }?.key ?: "-"
        val avgComm = if (activeSales.isNotEmpty()) totalComm / activeSales.size else 0.0

        MonthlySummary(
            monthKey = monthKey,
            totalSales = sales.size,
            approvedSales = approvedSales.size,
            pendingSales = pendingSales.size,
            cancelledSales = cancelledSales.size,
            totalCommission = totalComm,
            approvedCommission = approvedComm,
            pendingCommission = pendingComm,
            goalSalesCount = goal?.targetSalesCount ?: 30,
            goalCommissionAmount = goal?.targetCommissionAmount ?: 1500000.0,
            topBrand = topBrand,
            topModel = topModel,
            avgCommission = avgComm,
            brandCounts = brandCounts,
            brandCommissions = brandComms
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlySummary(monthKey = Formatters.getCurrentMonthKey())
    )

    val historyList: StateFlow<List<MonthHistoryItem>> = combine(
        allSales,
        availableMonths
    ) { sales, months ->
        months.map { mKey ->
            val monthSales = sales.filter { it.monthYearKey == mKey }
            val approved = monthSales.filter { it.status == "APROBADA" }
            val totalComm = monthSales.filter { it.status != "ANULADA" }.sumOf { it.commissionAmount }
            val approvedComm = approved.sumOf { it.commissionAmount }
            val topBrand = monthSales.groupBy { it.brand }.maxByOrNull { it.value.size }?.key ?: "-"
            val goal = 25 // standard reference
            val percent = if (goal > 0) ((approved.size.toFloat() / goal) * 100).toInt() else 0

            MonthHistoryItem(
                monthKey = mKey,
                displayMonth = Formatters.formatMonthName(mKey),
                totalSales = monthSales.size,
                approvedSales = approved.size,
                totalCommission = totalComm,
                approvedCommission = approvedComm,
                targetPercent = percent,
                topBrand = topBrand
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectMonth(monthKey: String) {
        _selectedMonthKey.value = monthKey
        viewModelScope.launch {
            repository.loadSuggestedRulesForMonth(monthKey)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: String) {
        _statusFilter.value = status
    }

    fun setBrandFilter(brand: String) {
        _brandFilter.value = brand
    }

    fun addSale(sale: SaleEntity) {
        viewModelScope.launch {
            repository.insertSale(sale)
        }
    }

    fun updateSale(sale: SaleEntity) {
        viewModelScope.launch {
            repository.updateSale(sale)
        }
    }

    fun deleteSale(sale: SaleEntity) {
        viewModelScope.launch {
            repository.deleteSale(sale)
        }
    }

    suspend fun calculateEstimatedCommission(brand: String, model: String): Double {
        return repository.calculateEstimatedCommission(_selectedMonthKey.value, brand, model)
    }

    fun saveRule(rule: CommissionRuleEntity) {
        viewModelScope.launch {
            repository.insertRule(rule)
        }
    }

    fun updateRule(rule: CommissionRuleEntity) {
        viewModelScope.launch {
            repository.updateRule(rule)
        }
    }

    fun deleteRule(rule: CommissionRuleEntity) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }

    fun copyRulesFromPreviousMonth() {
        val current = _selectedMonthKey.value
        val cal = Calendar.getInstance()
        try {
            val parts = current.split("-")
            cal.set(Calendar.YEAR, parts[0].toInt())
            cal.set(Calendar.MONTH, parts[1].toInt() - 1)
            cal.add(Calendar.MONTH, -1)
            val prevSdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val prevKey = prevSdf.format(cal.time)

            viewModelScope.launch {
                repository.copyRules(prevKey, current)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    fun loadDefaultSuggestedRules() {
        viewModelScope.launch {
            repository.loadSuggestedRulesForMonth(_selectedMonthKey.value)
        }
    }

    fun saveMonthlyGoal(targetSales: Int, targetCommission: Double, advisor: String, campaign: String) {
        viewModelScope.launch {
            repository.saveGoal(
                MonthlyGoalEntity(
                    monthYearKey = _selectedMonthKey.value,
                    targetSalesCount = targetSales,
                    targetCommissionAmount = targetCommission,
                    advisorName = advisor,
                    campaignName = campaign
                )
            )
        }
    }

    fun addBrand(brand: BrandEntity) {
        viewModelScope.launch {
            repository.insertBrand(brand)
        }
    }

    fun updateBrand(brand: BrandEntity) {
        viewModelScope.launch {
            repository.updateBrand(brand)
        }
    }

    fun deleteBrand(brand: BrandEntity) {
        viewModelScope.launch {
            repository.deleteBrand(brand)
        }
    }

    fun resetBrandsToDefaults() {
        viewModelScope.launch {
            repository.resetBrandsToDefaults()
        }
    }

    fun setBrandMonthlyBaseRate(brand: String, rate: Double, note: String = "") {
        viewModelScope.launch {
            repository.setBrandMonthlyBaseRate(_selectedMonthKey.value, brand, rate, note)
        }
    }

    fun setBrandMonthlyBaseRateForMonth(monthKey: String, brand: String, rate: Double, note: String = "") {
        viewModelScope.launch {
            repository.setBrandMonthlyBaseRate(monthKey, brand, rate, note)
        }
    }

    fun applyDefaultBrandRatesToMonth(monthKey: String = _selectedMonthKey.value) {
        viewModelScope.launch {
            repository.applyDefaultBrandRatesToMonth(monthKey)
        }
    }

    fun applyRateAdjustmentToMonth(
        monthKey: String = _selectedMonthKey.value,
        percentIncrease: Double?,
        fixedBonus: Double?
    ) {
        viewModelScope.launch {
            repository.applyAdjustmentToMonth(monthKey, percentIncrease, fixedBonus)
        }
    }

    fun buildShareReport(): String {
        val summary = monthlySummary.value
        val monthName = Formatters.formatMonthName(summary.monthKey)
        val advisor = currentGoal.value?.advisorName ?: "Asesor Call Center"
        val campaign = currentGoal.value?.campaignName ?: "Venta Móvil"

        return buildString {
            appendLine("📊 REPORTE DE VENTAS Y COMISIONES")
            appendLine("👤 Asesor: $advisor")
            appendLine("🏢 Campaña: $campaign")
            appendLine("📅 Mes: $monthName")
            appendLine("--------------------------------")
            appendLine("✅ Ventas Aprobadas: ${summary.approvedSales} / Meta: ${summary.goalSalesCount}")
            appendLine("⏳ Ventas en Validación: ${summary.pendingSales}")
            appendLine("❌ Anuladas: ${summary.cancelledSales}")
            appendLine("📱 Total Equipos Vendidos: ${summary.totalSales}")
            appendLine("💰 Comisión Aprobada: ${Formatters.formatCurrency(summary.approvedCommission)}")
            appendLine("💵 Comisión Total Estimada: ${Formatters.formatCurrency(summary.totalCommission)}")
            appendLine("🏆 Marca más vendida: ${summary.topBrand}")
            appendLine("--------------------------------")
            appendLine("Generado con ComiVentas Móvil")
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getInstance(application)
                    val repository = CommissionsRepository(
                        saleDao = db.saleDao(),
                        ruleDao = db.commissionRuleDao(),
                        goalDao = db.monthlyGoalDao(),
                        brandDao = db.brandDao()
                    )
                    return CommissionsViewModel(application, repository) as T
                }
            }
    }
}
