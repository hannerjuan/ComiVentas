package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SaleEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MonthlySummary
import com.example.ui.viewmodel.formatCurrency

@Composable
fun SalesMetricsScreen(
    sales: List<SaleEntity>,
    summary: MonthlySummary,
    modifier: Modifier = Modifier
) {
    val approvedSales = sales.filter { it.status == "APROBADA" }
    val brandSales = approvedSales.groupBy { it.brand }
        .mapValues { (_, list) ->
            val units = list.size
            val commissions = list.sumOf { it.commission }
            val revenue = list.sumOf { it.price }
            Triple(units, commissions, revenue)
        }
        .toList()
        .sortedByDescending { it.second.second }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("metrics_screen_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary KPIs
        item {
            Text(
                text = "Rendimiento del Mes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Comisión Aprobada",
                    value = formatCurrency(summary.approvedCommissions),
                    subtitle = "${summary.approvedCount} ventas logradas",
                    color = GreenSuccess,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Cumplimiento",
                    value = "${(summary.targetAttainment * 100).toInt()}%",
                    subtitle = "Meta: ${summary.targetUnits} unidades",
                    color = if (summary.targetAttainment >= 1f) PrimaryBlue else AmberWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "En Validación",
                    value = formatCurrency(summary.pendingCommissions),
                    subtitle = "${summary.pendingCount} ventas en trámite",
                    color = AmberWarning,
                    modifier = Modifier.weight(1f)
                )

                val avgCommission = if (summary.approvedCount > 0) {
                    summary.approvedCommissions / summary.approvedCount
                } else 0.0

                MetricCard(
                    title = "Promedio por Venta",
                    value = formatCurrency(avgCommission),
                    subtitle = "Ticket promedio comisionable",
                    color = PurpleAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Brand Breakdown
        item {
            Text(
                text = "Desglose por Marca",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (brandSales.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Aún no hay ventas aprobadas en este mes para generar estadísticas.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(brandSales) { (brand, stats) ->
                val (units, comm, rev) = stats
                val percentageOfTotal = if (summary.approvedCommissions > 0) {
                    (comm / summary.approvedCommissions).toFloat()
                } else 0f

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("brand_metric_$brand"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BrandAvatar(brand = brand)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = brand,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$units equipos vendidos • Facturado: ${formatCurrency(rev)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatCurrency(comm),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = GreenSuccess
                                )
                                Text(
                                    text = "${(percentageOfTotal * 100).toInt()}% del total",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { percentageOfTotal.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = PrimaryBlue,
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}
