package com.example.ui.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val currencyFormat = DecimalFormat("$#,##0", DecimalFormatSymbols(Locale("es", "CO"))).apply {
        maximumFractionDigits = 0
    }

    private val numberFormat = DecimalFormat("#,##0", DecimalFormatSymbols(Locale("es", "CO")))

    fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount)
    }

    fun formatNumber(number: Int): String {
        return numberFormat.format(number)
    }

    fun formatDate(timeMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale("es", "CO"))
        return sdf.format(Date(timeMillis))
    }

    fun formatDateOnly(timeMillis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO"))
        return sdf.format(Date(timeMillis))
    }

    fun formatMonthName(monthKey: String): String {
        // monthKey is "YYYY-MM"
        return try {
            val parts = monthKey.split("-")
            val year = parts[0]
            val month = parts[1].toInt()
            val monthNames = listOf(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
            )
            "${monthNames[month - 1]} $year"
        } catch (e: Exception) {
            monthKey
        }
    }

    fun getCurrentMonthKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }
}

data class MonthlySummary(
    val monthKey: String,
    val totalSales: Int = 0,
    val approvedSales: Int = 0,
    val pendingSales: Int = 0,
    val cancelledSales: Int = 0,
    val totalCommission: Double = 0.0,
    val approvedCommission: Double = 0.0,
    val pendingCommission: Double = 0.0,
    val goalSalesCount: Int = 30,
    val goalCommissionAmount: Double = 1500000.0,
    val topBrand: String = "-",
    val topModel: String = "-",
    val avgCommission: Double = 0.0,
    val brandCounts: Map<String, Int> = emptyMap(),
    val brandCommissions: Map<String, Double> = emptyMap()
) {
    val salesProgress: Float
        get() = if (goalSalesCount > 0) (approvedSales.toFloat() / goalSalesCount.toFloat()).coerceIn(0f, 1f) else 0f

    val commissionProgress: Float
        get() = if (goalCommissionAmount > 0) (approvedCommission.toFloat() / goalCommissionAmount.toFloat()).coerceIn(0f, 1f) else 0f
}

data class MonthHistoryItem(
    val monthKey: String,
    val displayMonth: String,
    val totalSales: Int,
    val approvedSales: Int,
    val totalCommission: Double,
    val approvedCommission: Double,
    val targetPercent: Int,
    val topBrand: String
)
