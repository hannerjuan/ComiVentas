package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AdvisorProfileEntity
import com.example.data.local.CommissionRuleEntity
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.formatCurrency

@Composable
fun CommissionSettingsScreen(
    profile: AdvisorProfileEntity,
    rules: List<CommissionRuleEntity>,
    onUpdateProfile: (name: String, campaign: String, targetAmount: Double, targetUnits: Int) -> Unit,
    onAddOrUpdateRule: (CommissionRuleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var advisorName by remember(profile) { mutableStateOf(profile.advisorName) }
    var campaignName by remember(profile) { mutableStateOf(profile.campaignName) }
    var targetUnitsText by remember(profile) { mutableStateOf(profile.monthlyTargetUnits.toString()) }
    var targetAmountText by remember(profile) { mutableStateOf(profile.monthlyTargetAmount.toLong().toString()) }

    var isProfileSaved by remember { mutableStateOf(false) }

    // New rule inputs
    var showAddRuleDialog by remember { mutableStateOf(false) }
    var newBrand by remember { mutableStateOf("") }
    var newFixedCommText by remember { mutableStateOf("") }
    var newPercentCommText by remember { mutableStateOf("2.0") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("settings_screen_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Advisor Profile & Goal
        item {
            Text(
                text = "Perfil del Asesor y Metas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Configura tus metas mensuales y datos de campaña",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("profile_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "Nombre del Asesor", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = advisorName,
                        onValueChange = { advisorName = it },
                        modifier = Modifier.fillMaxWidth().testTag("advisor_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Text(text = "Campaña / Proyecto", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = campaignName,
                        onValueChange = { campaignName = it },
                        modifier = Modifier.fillMaxWidth().testTag("campaign_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Meta Unidades", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = targetUnitsText,
                                onValueChange = { targetUnitsText = it.filter { c -> c.isDigit() } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("target_units_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Meta Comisiones ($)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = targetAmountText,
                                onValueChange = { targetAmountText = it.filter { c -> c.isDigit() } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("target_amount_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            val units = targetUnitsText.toIntOrNull() ?: 30
                            val amount = targetAmountText.toDoubleOrNull() ?: 2500000.0
                            onUpdateProfile(advisorName.trim(), campaignName.trim(), amount, units)
                            isProfileSaved = true
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_profile_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isProfileSaved) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Guardado Correctamente")
                        } else {
                            Text("Guardar Cambios de Perfil")
                        }
                    }
                }
            }
        }

        // Section: Commission Rules per Brand
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Reglas de Comisiones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Fijo + porcentaje por marca vendida",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                FilledTonalButton(
                    onClick = { showAddRuleDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_rule_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nueva Regla", fontSize = 12.sp)
                }
            }
        }

        items(rules) { rule ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("rule_card_${rule.brand}"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandAvatar(brand = rule.brand)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = rule.brand,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Comisión fija: ${formatCurrency(rule.fixedCommission)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (rule.percentageCommission > 0) {
                                Text(
                                    text = "+ ${rule.percentageCommission}% del valor venta",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PrimaryBlue,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Surface(
                        color = GreenSuccess.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Activa",
                            color = GreenSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    if (showAddRuleDialog) {
        AlertDialog(
            onDismissRequest = { showAddRuleDialog = false },
            title = { Text("Nueva Regla de Comisión") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Marca:", style = MaterialTheme.typography.labelSmall)
                    OutlinedTextField(
                        value = newBrand,
                        onValueChange = { newBrand = it },
                        placeholder = { Text("Ej. Realme, Vivo, etc.") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "Comisión Fija ($ COP):", style = MaterialTheme.typography.labelSmall)
                    OutlinedTextField(
                        value = newFixedCommText,
                        onValueChange = { newFixedCommText = it.filter { c -> c.isDigit() } },
                        placeholder = { Text("30000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "Porcentaje de Venta (%):", style = MaterialTheme.typography.labelSmall)
                    OutlinedTextField(
                        value = newPercentCommText,
                        onValueChange = { newPercentCommText = it },
                        placeholder = { Text("2.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fixed = newFixedCommText.toDoubleOrNull() ?: 0.0
                        val percent = newPercentCommText.toDoubleOrNull() ?: 0.0
                        if (newBrand.isNotBlank()) {
                            onAddOrUpdateRule(
                                CommissionRuleEntity(
                                    brand = newBrand.trim(),
                                    fixedCommission = fixed,
                                    percentageCommission = percent
                                )
                            )
                            showAddRuleDialog = false
                            newBrand = ""
                            newFixedCommText = ""
                        }
                    }
                ) {
                    Text("Crear Regla")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRuleDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
