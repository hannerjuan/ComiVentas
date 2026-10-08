package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AdvisorProfileEntity
import com.example.data.local.CommissionRuleEntity
import com.example.data.local.SaleEntity
import com.example.data.repository.SalesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class MonthlySummary(
    val approvedCommissions: Double = 0.0,
    val pendingCommissions: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val approvedCount: Int = 0,
    val pendingCount: Int = 0,
    val cancelledCount: Int = 0,
    val totalCount: Int = 0,
    val targetUnits: Int = 30,
    val targetAttainment: Float = 0f
)

class MainViewModel(private val repository: SalesRepository) : ViewModel() {

    private val _selectedMonthKey = MutableStateFlow("2026-10")
    val selectedMonthKey: StateFlow<String> = _selectedMonthKey.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<String?>("TODAS")
    val selectedStatusFilter: StateFlow<String?> = _selectedStatusFilter.asStateFlow()

    private val _selectedBrandFilter = MutableStateFlow<String?>("TODAS")
    val selectedBrandFilter: StateFlow<String?> = _selectedBrandFilter.asStateFlow()

    val availableMonths: StateFlow<List<String>> = repository.getDistinctMonthKeys()
        .map { list ->
            val set = list.toMutableSet()
            set.add("2026-10")
            set.add("2026-09")
            set.sortedDescending()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("2026-10", "2026-09"))

    val advisorProfile: StateFlow<AdvisorProfileEntity> = repository.getAdvisorProfile()
        .map { it ?: AdvisorProfileEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdvisorProfileEntity())

    val commissionRules: StateFlow<List<CommissionRuleEntity>> = repository.getCommissionRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSales: StateFlow<List<SaleEntity>> = repository.getAllSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val salesForCurrentMonth: StateFlow<List<SaleEntity>> = _selectedMonthKey
        .flatMapLatest { monthKey -> repository.getSalesByMonth(monthKey) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSales: StateFlow<List<SaleEntity>> = combine(
        salesForCurrentMonth,
        _searchQuery,
        _selectedStatusFilter,
        _selectedBrandFilter
    ) { sales, query, status, brand ->
        sales.filter { sale ->
            val matchesQuery = query.isBlank() ||
                    sale.model.contains(query, ignoreCase = true) ||
                    sale.brand.contains(query, ignoreCase = true) ||
                    sale.customerName.contains(query, ignoreCase = true) ||
                    sale.imeiOrContract.contains(query, ignoreCase = true)

            val matchesStatus = status == null || status == "TODAS" || sale.status.equals(status, ignoreCase = true)
            val matchesBrand = brand == null || brand == "TODAS" || sale.brand.equals(brand, ignoreCase = true)

            matchesQuery && matchesStatus && matchesBrand
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlySummary: StateFlow<MonthlySummary> = combine(
        salesForCurrentMonth,
        advisorProfile
    ) { sales, profile ->
        val approved = sales.filter { it.status == "APROBADA" }
        val pending = sales.filter { it.status == "EN_VALIDACION" }
        val cancelled = sales.filter { it.status == "ANULADA" }

        val approvedComm = approved.sumOf { it.commission }
        val pendingComm = pending.sumOf { it.commission }
        val revenue = approved.sumOf { it.price }

        val targetUnits = if (profile.monthlyTargetUnits > 0) profile.monthlyTargetUnits else 30
        val attainment = (approved.size.toFloat() / targetUnits.toFloat()).coerceIn(0f, 2f)

        MonthlySummary(
            approvedCommissions = approvedComm,
            pendingCommissions = pendingComm,
            totalRevenue = revenue,
            approvedCount = approved.size,
            pendingCount = pending.size,
            cancelledCount = cancelled.size,
            totalCount = sales.size,
            targetUnits = targetUnits,
            targetAttainment = attainment
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlySummary())

    fun selectMonth(monthKey: String) {
        _selectedMonthKey.value = monthKey
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: String?) {
        _selectedStatusFilter.value = status
    }

    fun setBrandFilter(brand: String?) {
        _selectedBrandFilter.value = brand
    }

    fun calculateCommissionFor(brand: String, price: Double): Double {
        val rules = commissionRules.value
        val rule = rules.firstOrNull { it.brand.equals(brand, ignoreCase = true) }
        return if (rule != null) {
            rule.fixedCommission + (price * (rule.percentageCommission / 100.0))
        } else {
            price * 0.02
        }
    }

    fun registerSale(
        brand: String,
        model: String,
        price: Double,
        saleDate: String,
        customerName: String,
        imeiOrContract: String,
        status: String,
        notes: String
    ) {
        viewModelScope.launch {
            val monthKey = if (saleDate.length >= 7) saleDate.substring(0, 7) else _selectedMonthKey.value
            val commission = calculateCommissionFor(brand, price)
            val sale = SaleEntity(
                brand = brand,
                model = model,
                price = price,
                commission = commission,
                saleDate = saleDate,
                monthKey = monthKey,
                customerName = customerName,
                imeiOrContract = imeiOrContract,
                status = status,
                notes = notes
            )
            repository.addSale(sale)
        }
    }

    fun updateSaleStatus(sale: SaleEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateSale(sale.copy(status = newStatus))
        }
    }

    fun deleteSale(sale: SaleEntity) {
        viewModelScope.launch {
            repository.deleteSale(sale)
        }
    }

    fun updateAdvisorProfile(name: String, campaign: String, targetAmount: Double, targetUnits: Int) {
        viewModelScope.launch {
            repository.updateAdvisorProfile(
                AdvisorProfileEntity(
                    id = 1,
                    advisorName = name,
                    campaignName = campaign,
                    monthlyTargetAmount = targetAmount,
                    monthlyTargetUnits = targetUnits
                )
            )
        }
    }

    fun addOrUpdateCommissionRule(rule: CommissionRuleEntity) {
        viewModelScope.launch {
            if (rule.id == 0L) {
                repository.addCommissionRule(rule)
            } else {
                repository.updateCommissionRule(rule)
            }
        }
    }
}

class MainViewModelFactory(private val repository: SalesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}
