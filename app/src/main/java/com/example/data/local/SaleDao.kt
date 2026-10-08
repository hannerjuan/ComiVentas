package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Query("SELECT * FROM sales WHERE monthYearKey = :monthKey ORDER BY dateMillis DESC")
    fun getSalesForMonth(monthKey: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales ORDER BY dateMillis DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT DISTINCT monthYearKey FROM sales ORDER BY monthYearKey DESC")
    fun getDistinctSaleMonths(): Flow<List<String>>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    fun getSaleById(id: Long): Flow<SaleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Delete
    suspend fun deleteSale(sale: SaleEntity)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSaleById(id: Long)

    @Query("SELECT SUM(commissionAmount) FROM sales WHERE monthYearKey = :monthKey AND status != 'ANULADA'")
    fun getTotalActiveCommissionForMonth(monthKey: String): Flow<Double?>

    @Query("SELECT SUM(commissionAmount) FROM sales WHERE monthYearKey = :monthKey AND status = 'APROBADA'")
    fun getApprovedCommissionForMonth(monthKey: String): Flow<Double?>

    @Query("SELECT COUNT(*) FROM sales WHERE monthYearKey = :monthKey AND status != 'ANULADA'")
    fun getActiveSalesCountForMonth(monthKey: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM sales WHERE monthYearKey = :monthKey")
    fun getTotalSalesCountForMonth(monthKey: String): Flow<Int>
}
