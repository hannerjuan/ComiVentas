package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
fun MonthSelectorBar(
    selectedMonthKey: String,
    availableMonths: List<String>,
    onSelectMonth: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentIndex = availableMonths.indexOf(selectedMonthKey)
    val hasPrev = currentIndex < availableMonths.size - 1
    val hasNext = currentIndex > 0

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    if (hasPrev) onSelectMonth(availableMonths[currentIndex + 1])
                },
                enabled = hasPrev,
                modifier = Modifier.testTag("prev_month_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Mes anterior",
                    tint = if (hasPrev) MaterialTheme.colorScheme.primary else Color.LightGray
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatMonthName(selectedMonthKey),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Periodo de liquidación",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = {
                    if (hasNext) onSelectMonth(availableMonths[currentIndex - 1])
                },
                enabled = hasNext,
                modifier = Modifier.testTag("next_month_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Mes siguiente",
                    tint = if (hasNext) MaterialTheme.colorScheme.primary else Color.LightGray
                )
            }
        }
    }
}

@Composable
fun HeroCommissionCard(
    summary: MonthlySummary,
    advisorName: String,
    campaignName: String,
    onEditGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isCollapsed by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_commission_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Advisor info and Collapse / Edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = advisorName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = campaignName,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEditGoal,
                        modifier = Modifier.size(32.dp).testTag("edit_goal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Meta",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { isCollapsed = !isCollapsed },
                        modifier = Modifier.size(32.dp).testTag("collapse_card_button")
                    ) {
                        Icon(
                            imageVector = if (isCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isCollapsed) "Expandir tarjeta" else "Contraer tarjeta",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main commission figure
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Comisión Aprobada",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatCurrency(summary.approvedCommissions),
                        color = Color.White,
                        fontSize = if (isCollapsed) 20.sp else 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${summary.approvedCount} / ${summary.targetUnits} ventas",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${(summary.targetAttainment * 100).toInt()}% de la meta",
                        color = if (summary.targetAttainment >= 1f) GreenSuccessLight else AmberWarningLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Expanded extra metrics
            AnimatedVisibility(visible = !isCollapsed) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    // Progress bar
                    LinearProgressIndicator(
                        progress = { summary.targetAttainment.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (summary.targetAttainment >= 1f) GreenSuccess else AmberWarning,
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "En Validación",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = formatCurrency(summary.pendingCommissions),
                                color = AmberWarningLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Ventas Aprobadas",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${summary.approvedCount}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total Facturado",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = formatCurrency(summary.totalRevenue),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SaleCard(
    sale: SaleEntity,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sale_card_${sale.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    BrandAvatar(brand = sale.brand)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = sale.model,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "${sale.brand} • ${sale.saleDate}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                // Commission earned highlight
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "+${formatCurrency(sale.commission)}",
                        fontWeight = FontWeight.Bold,
                        color = when (sale.status) {
                            "APROBADA" -> GreenSuccess
                            "EN_VALIDACION" -> AmberWarning
                            else -> RedCancel
                        },
                        fontSize = 15.sp
                    )
                    Text(
                        text = formatCurrency(sale.price),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details line: Customer, Status Badge, and options
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (sale.customerName.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = sale.customerName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(
                        status = sale.status,
                        onClick = { showMenu = true }
                    )

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(28.dp).testTag("sale_options_${sale.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opciones de venta",
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Marcar como Aprobada") },
                                onClick = {
                                    showMenu = false
                                    onStatusChange("APROBADA")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = GreenSuccess)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Marcar En Validación") },
                                onClick = {
                                    showMenu = false
                                    onStatusChange("EN_VALIDACION")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = AmberWarning)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Marcar como Anulada") },
                                onClick = {
                                    showMenu = false
                                    onStatusChange("ANULADA")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = RedCancel)
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Eliminar Registro", color = RedCancel) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = RedCancel)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String, onClick: () -> Unit) {
    val (bgColor, textColor, label) = when (status) {
        "APROBADA" -> Triple(GreenSuccessLight, GreenSuccess, "Aprobada")
        "EN_VALIDACION" -> Triple(AmberWarningLight, AmberWarning, "En Validación")
        else -> Triple(RedCancelLight, RedCancel, "Anulada")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("status_badge_$status")
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun BrandAvatar(brand: String) {
    val initial = brand.take(1).uppercase()
    val bg = when (brand.lowercase()) {
        "samsung" -> Color(0xFF1428A0)
        "xiaomi" -> Color(0xFFFF6900)
        "apple" -> Color(0xFF555555)
        "motorola" -> Color(0xFF001489)
        "honor" -> Color(0xFF0093D0)
        else -> PrimaryBlue
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

fun formatMonthName(monthKey: String): String {
    if (!monthKey.contains("-")) return monthKey
    val parts = monthKey.split("-")
    if (parts.size != 2) return monthKey
    val year = parts[0]
    val monthName = when (parts[1]) {
        "01" -> "Enero"
        "02" -> "Febrero"
        "03" -> "Marzo"
        "04" -> "Abril"
        "05" -> "Mayo"
        "06" -> "Junio"
        "07" -> "Julio"
        "08" -> "Agosto"
        "09" -> "Septiembre"
        "10" -> "Octubre"
        "11" -> "Noviembre"
        "12" -> "Diciembre"
        else -> parts[1]
    }
    return "$monthName $year"
}
