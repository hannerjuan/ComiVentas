package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CommissionRuleEntity
import com.example.ui.model.Formatters
import com.example.ui.theme.EmeraldSuccess

@Composable
fun CommissionRulesScreen(
    rules: List<CommissionRuleEntity>,
    currentMonthKey: String,
    onAddNewRule: () -> Unit,
    onEditRule: (CommissionRuleEntity) -> Unit,
    onDeleteRule: (CommissionRuleEntity) -> Unit,
    onCopyFromPreviousMonth: () -> Unit,
    onLoadSuggestedRules: () -> Unit,
    modifier: Modifier = Modifier
) {
    var filterType by remember { mutableStateOf("TODAS") } // TODAS, MARCA, MODELO
    var selectedBrandFilter by remember { mutableStateOf("TODAS") }

    val brands = listOf("TODAS", "Samsung", "Apple", "Xiaomi", "Motorola", "Honor", "Oppo", "Realme")

    val filteredRules = remember(rules, filterType, selectedBrandFilter) {
        rules.filter { rule ->
            val matchesType = when (filterType) {
                "MARCA" -> rule.modelReference.isNullOrBlank()
                "MODELO" -> !rule.modelReference.isNullOrBlank()
                else -> true
            }
            val matchesBrand = selectedBrandFilter == "TODAS" || rule.brand.equals(selectedBrandFilter, ignoreCase = true)
            matchesType && matchesBrand
        }
    }

    val brandBaseRules = remember(filteredRules) { filteredRules.filter { it.modelReference.isNullOrBlank() } }
    val modelSpecificRules = remember(filteredRules) { filteredRules.filter { !it.modelReference.isNullOrBlank() } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("commission_rules_screen")
    ) {
        // Top Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Matriz Tarifaria Mensual",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Mes: ${Formatters.formatMonthName(currentMonthKey)} (${rules.size} tarifas activas)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = onAddNewRule,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("add_rule_top_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nueva Tarifa", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons: Copy & Defaults
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCopyFromPreviousMonth,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_prev_rules_btn")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copiar Mes Anterior", fontSize = 11.sp, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = onLoadSuggestedRules,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("load_suggested_rules_btn")
                    ) {
                        Icon(Icons.Default.AutoMode, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cargar Sugeridas", fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }

        // Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterType == "TODAS",
                onClick = { filterType = "TODAS" },
                label = { Text("Todas (${rules.size})", fontSize = 12.sp) }
            )
            FilterChip(
                selected = filterType == "MARCA",
                onClick = { filterType = "MARCA" },
                label = { Text("Base por Marca", fontSize = 12.sp) }
            )
            FilterChip(
                selected = filterType == "MODELO",
                onClick = { filterType = "MODELO" },
                label = { Text("Especiales Modelo", fontSize = 12.sp) }
            )
        }

        // Brand quick chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(brands) { b ->
                FilterChip(
                    selected = selectedBrandFilter == b,
                    onClick = { selectedBrandFilter = b },
                    label = { Text(b, fontSize = 11.sp) }
                )
            }
        }

        // Rules List
        if (filteredRules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PriceChange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "No hay tarifas configuradas para este filtro",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Puedes cargar las tarifas sugeridas de marcas líderes (Samsung S24 Ultra, iPhone, Xiaomi, etc.) o agregar tus valores personalizados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onLoadSuggestedRules) {
                        Text("Cargar Tarifas Sugeridas del Mercado")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                // Section: Specific Model Overrides
                if (modelSpecificRules.isNotEmpty()) {
                    item {
                        Text(
                            text = "⭐ Tarifas Especiales por Modelo / Referencia (${modelSpecificRules.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(modelSpecificRules, key = { it.id }) { rule ->
                        CommissionRuleCard(
                            rule = rule,
                            onEdit = { onEditRule(rule) },
                            onDelete = { onDeleteRule(rule) }
                        )
                    }
                }

                // Section: Brand Baseline Rules
                if (brandBaseRules.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "🏷️ Comisiones Base por Marca (${brandBaseRules.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(brandBaseRules, key = { it.id }) { rule ->
                        CommissionRuleCard(
                            rule = rule,
                            onEdit = { onEditRule(rule) },
                            onDelete = { onDeleteRule(rule) }
                        )
                    }
                }
            }
        }
    }
}
