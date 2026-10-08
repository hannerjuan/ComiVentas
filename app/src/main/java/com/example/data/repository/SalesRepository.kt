package com.example.data.repository

import com.example.data.local.AdvisorProfileEntity
import com.example.data.local.CommissionRuleEntity
import com.example.data.local.SaleEntity
import com.example.data.local.SalesDao
import kotlinx.coroutines.flow.Flow

class SalesRepository(private val salesDao: SalesDao) {

    fun getAllSales(): Flow<List<SaleEntity>> = salesDao.getAllSales()

    fun getSalesByMonth(monthKey: String): Flow<List<SaleEntity>> = salesDao.getSalesByMonth(monthKey)

    fun getDistinctMonthKeys(): Flow<List<String>> = salesDao.getDistinctMonthKeys()

    suspend fun addSale(sale: SaleEntity): Long = salesDao.insertSale(sale)

    suspend fun updateSale(sale: SaleEntity) = salesDao.updateSale(sale)

    suspend fun deleteSale(sale: SaleEntity) = salesDao.deleteSale(sale)

    fun getCommissionRules(): Flow<List<CommissionRuleEntity>> = salesDao.getAllCommissionRules()

    suspend fun addCommissionRule(rule: CommissionRuleEntity) = salesDao.insertCommissionRule(rule)

    suspend fun updateCommissionRule(rule: CommissionRuleEntity) = salesDao.updateCommissionRule(rule)

    suspend fun deleteCommissionRule(rule: CommissionRuleEntity) = salesDao.deleteCommissionRule(rule)

    fun getAdvisorProfile(): Flow<AdvisorProfileEntity?> = salesDao.getAdvisorProfile()

    suspend fun updateAdvisorProfile(profile: AdvisorProfileEntity) = salesDao.setAdvisorProfile(profile)
}
