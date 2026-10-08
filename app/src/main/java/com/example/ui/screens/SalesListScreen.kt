package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SaleEntity
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.MonthlySummary
import com.example.ui.viewmodel.formatCurrency

@Composable
fun SalesListScreen(
    sales: List<SaleEntity>,
    summary: MonthlySummary,
    advisorName: String,
    campaignName: String,
    searchQuery: String,
    selectedStatus: String?,
    selectedBrand: String?,
    onSearchChange: (String) -> Unit,
    onStatusSelect: (String?) -> Unit,
    onBrandSelect: (String?) -> Unit,
    onStatusChange: (SaleEntity, String) -> Unit,
    onDeleteSale: (SaleEntity) -> Unit,
    onEditGoal: () -> Unit,
    onAddNewSale: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusOptions = listOf("TODAS", "APROBADA", "EN_VALIDACION", "ANULADA")
    val brandOptions = listOf("TODAS", "Samsung", "Xiaomi", "Apple", "Motorola", "Honor", "Oppo")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Unified single scrollable LazyColumn: The hero card, search, filters and list all scroll together!
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("sales_lazy_column"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Hero Commission Card
            item(key = "hero_card") {
                HeroCommissionCard(
                    summary = summary,
                    advisorName = advisorName,
                    campaignName = campaignName,
                    onEditGoal = onEditGoal
                )
            }

            // 2. Search Field
            item(key = "search_bar") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            text = "Buscar modelo, cliente o IMEI...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar búsqueda",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input_field")
                )
            }

            // 3. Status Filters (Horizontal Chip Row)
            item(key = "status_filters") {
                Column {
                    Text(
                        text = "Filtrar por estado:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(statusOptions) { status ->
                            val isSelected = (selectedStatus == status) || (selectedStatus == null && status == "TODAS")
                            FilterChip(
                                selected = isSelected,
                                onClick = { onStatusSelect(status) },
                                label = {
                                    Text(
                                        text = when (status) {
                                            "TODAS" -> "Todas (${summary.totalCount})"
                                            "APROBADA" -> "Aprobadas (${summary.approvedCount})"
                                            "EN_VALIDACION" -> "En Validación (${summary.pendingCount})"
                                            "ANULADA" -> "Anuladas (${summary.cancelledCount})"
                                            else -> status
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                                    selectedLabelColor = PrimaryBlue
                                ),
                                modifier = Modifier.testTag("filter_status_$status")
                            )
                        }
                    }
                }
            }

            // 4. Brand Filters (Horizontal Chip Row)
            item(key = "brand_filters") {
                Column {
                    Text(
                        text = "Filtrar por marca:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(brandOptions) { brand ->
                            val isSelected = (selectedBrand == brand) || (selectedBrand == null && brand == "TODAS")
                            FilterChip(
                                selected = isSelected,
                                onClick = { onBrandSelect(brand) },
                                label = {
                                    Text(
                                        text = brand,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("filter_brand_$brand")
                            )
                        }
                    }
                }
            }

            // 5. Section Header with count and commissions total
            item(key = "section_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ventas Registradas (${sales.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val subtotalComm = sales.filter { it.status == "APROBADA" }.sumOf { it.commission }
                    Text(
                        text = "Total: ${formatCurrency(subtotalComm)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                }
            }

            // 6. Sales List or Empty state
            if (sales.isEmpty()) {
                item(key = "empty_state") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .testTag("empty_sales_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No hay ventas registradas con estos filtros",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Presiona el botón de abajo para registrar tu primera venta del mes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onAddNewSale,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("empty_add_sale_button")
                            ) {
                                Text("Registrar Venta")
                            }
                        }
                    }
                }
            } else {
                items(
                    items = sales,
                    key = { it.id }
                ) { sale ->
                    SaleCard(
                        sale = sale,
                        onStatusChange = { newStatus -> onStatusChange(sale, newStatus) },
                        onDelete = { onDeleteSale(sale) }
                    )
                }
            }
        }
    }
}
