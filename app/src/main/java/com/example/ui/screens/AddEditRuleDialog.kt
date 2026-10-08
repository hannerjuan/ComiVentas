package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.BrandEntity
import com.example.data.local.CommissionRuleEntity
import com.example.data.repository.DefaultTariffPresets
import com.example.ui.model.Formatters
import com.example.ui.theme.EmeraldSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRuleDialog(
    initialRule: CommissionRuleEntity? = null,
    monthKey: String,
    availableBrands: List<BrandEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (CommissionRuleEntity) -> Unit
) {
    val brands = if (availableBrands.isNotEmpty()) {
        availableBrands.map { it.name }
    } else {
        listOf("Samsung", "Apple", "Xiaomi", "Motorola", "Honor", "Oppo", "Realme", "Otro")
    }

    var brand by remember { mutableStateOf(initialRule?.brand ?: brands.firstOrNull() ?: "Samsung") }
    var isModelOverride by remember { mutableStateOf(!initialRule?.modelReference.isNullOrBlank()) }
    var modelReference by remember { mutableStateOf(initialRule?.modelReference ?: "") }
    var commissionText by remember {
        mutableStateOf(
            if (initialRule != null) initialRule.commissionAmount.toLong().toString() else "40000"
        )
    }
    var note by remember { mutableStateOf(initialRule?.note ?: "") }

    val suggestedModels = remember(brand, availableBrands) {
        val configuredBrand = availableBrands.firstOrNull { it.name.equals(brand, ignoreCase = true) }
        if (configuredBrand != null && configuredBrand.suggestedModels.isNotBlank()) {
            configuredBrand.suggestedModels.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            val preset = DefaultTariffPresets.POPULAR_BRANDS.firstOrNull { it.brand.equals(brand, ignoreCase = true) }
            preset?.models?.map { it.modelName } ?: emptyList()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .testTag("add_rule_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (initialRule == null) "Nueva Tarifa de Comisión" else "Modificar Tarifa",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Aplica para: ${Formatters.formatMonthName(monthKey)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                // Rule Type Toggle
                Text(
                    text = "Tipo de Regla",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isModelOverride,
                        onClick = {
                            isModelOverride = false
                            modelReference = ""
                        },
                        label = { Text("Base por Marca") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isModelOverride,
                        onClick = { isModelOverride = true },
                        label = { Text("Especial por Modelo") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Brand Selector Chips
                Text(
                    text = "Marca",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(brands) { b ->
                        FilterChip(
                            selected = brand == b,
                            onClick = { brand = b },
                            label = { Text(b) }
                        )
                    }
                }

                // Model override field
                if (isModelOverride) {
                    Text(
                        text = "Modelo / Referencia Específica",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = modelReference,
                        onValueChange = { modelReference = it },
                        label = { Text("Ej: Galaxy S24 Ultra, iPhone 15 Pro") },
                        leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rule_model_input")
                    )

                    if (suggestedModels.isNotEmpty()) {
                        Text(
                            text = "Sugerencias de $brand:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(suggestedModels) { m ->
                                SuggestionChip(
                                    onClick = { modelReference = m },
                                    label = { Text(m, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }

                // Commission Value
                Text(
                    text = "Valor de Comisión ($)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = commissionText,
                    onValueChange = { commissionText = it },
                    label = { Text("Comisión en pesos / moneda") },
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = EmeraldSuccess)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rule_commission_input")
                )

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Descripción / Nota (Opcional)") },
                    placeholder = { Text("Ej: Bono especial operador / spiff") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val amount = commissionText.toDoubleOrNull() ?: 0.0
                            val rule = initialRule?.copy(
                                brand = brand.trim(),
                                modelReference = if (isModelOverride) modelReference.trim().ifBlank { null } else null,
                                commissionAmount = amount,
                                note = note.trim()
                            ) ?: CommissionRuleEntity(
                                monthYearKey = monthKey,
                                brand = brand.trim(),
                                modelReference = if (isModelOverride) modelReference.trim().ifBlank { null } else null,
                                commissionAmount = amount,
                                note = note.trim()
                            )
                            onSave(rule)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_rule_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
