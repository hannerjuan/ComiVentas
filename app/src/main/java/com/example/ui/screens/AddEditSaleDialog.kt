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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.BrandEntity
import com.example.data.local.SaleEntity
import com.example.data.repository.DefaultTariffPresets
import com.example.ui.model.Formatters
import com.example.ui.theme.EmeraldSuccess
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSaleDialog(
    initialSale: SaleEntity? = null,
    currentMonthKey: String,
    availableBrands: List<BrandEntity> = emptyList(),
    onCalculateCommission: suspend (String, String) -> Double,
    onDismiss: () -> Unit,
    onSave: (SaleEntity) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val brands = if (availableBrands.isNotEmpty()) {
        availableBrands.map { it.name }
    } else {
        listOf("Samsung", "Apple", "Xiaomi", "Motorola", "Honor", "Oppo", "Realme", "Otro")
    }

    var brand by remember { mutableStateOf(initialSale?.brand ?: brands.firstOrNull() ?: "Samsung") }
    var modelReference by remember { mutableStateOf(initialSale?.modelReference ?: "Galaxy S24 Ultra 5G") }
    var commissionText by remember {
        mutableStateOf(
            if (initialSale != null) initialSale.commissionAmount.toLong().toString() else "85000"
        )
    }
    var salePriceText by remember {
        mutableStateOf(
            if (initialSale != null && initialSale.salePrice > 0) initialSale.salePrice.toLong().toString() else ""
        )
    }
    var orderNumber by remember { mutableStateOf(initialSale?.orderNumber ?: "") }
    var clientName by remember { mutableStateOf(initialSale?.clientName ?: "") }
    var clientPhone by remember { mutableStateOf(initialSale?.clientPhone ?: "") }
    var saleType by remember { mutableStateOf(initialSale?.saleType ?: "Portabilidad") }
    var status by remember { mutableStateOf(initialSale?.status ?: "APROBADA") }
    var notes by remember { mutableStateOf(initialSale?.notes ?: "") }

    val saleTypes = listOf("Portabilidad", "Línea Nueva", "Renovación", "Equipo Libre")
    val statuses = listOf("APROBADA", "EN_VALIDACION", "ANULADA")

    // Get suggestions for the current brand
    val suggestedModels = remember(brand, availableBrands) {
        val configuredBrand = availableBrands.firstOrNull { it.name.equals(brand, ignoreCase = true) }
        if (configuredBrand != null && configuredBrand.suggestedModels.isNotBlank()) {
            configuredBrand.suggestedModels.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            val preset = DefaultTariffPresets.POPULAR_BRANDS.firstOrNull { it.brand.equals(brand, ignoreCase = true) }
            preset?.models?.map { it.modelName } ?: emptyList()
        }
    }

    // Auto-calculate commission whenever brand or model changes if creating new sale
    fun recalculateCommission(b: String, m: String) {
        coroutineScope.launch {
            val calc = onCalculateCommission(b, m)
            commissionText = calc.toLong().toString()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("add_sale_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AddShoppingCart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = if (initialSale == null) "Registrar Venta" else "Editar Venta",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Text(
                    text = "Asignado al mes: ${Formatters.formatMonthName(currentMonthKey)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Brand Selector Chips
                    Text(
                        text = "Marca del Celular",
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
                                onClick = {
                                    brand = b
                                    // pick first suggested model for that brand
                                    val firstModel = DefaultTariffPresets.POPULAR_BRANDS
                                        .firstOrNull { it.brand.equals(b, ignoreCase = true) }
                                        ?.models?.firstOrNull()?.modelName ?: ""
                                    if (firstModel.isNotBlank()) {
                                        modelReference = firstModel
                                        recalculateCommission(b, firstModel)
                                    } else {
                                        recalculateCommission(b, modelReference)
                                    }
                                },
                                label = { Text(b) },
                                leadingIcon = {
                                    if (brand == b) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            )
                        }
                    }

                    // Model Reference & Suggestions
                    Text(
                        text = "Modelo / Referencia",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = modelReference,
                        onValueChange = {
                            modelReference = it
                            recalculateCommission(brand, it)
                        },
                        label = { Text("Ej: Galaxy S24 Ultra 5G, A55, iPhone 15") },
                        leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("model_reference_input")
                    )

                    // Quick model chips for the active brand
                    if (suggestedModels.isNotEmpty()) {
                        Text(
                            text = "Modelos populares de $brand:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(suggestedModels) { m ->
                                SuggestionChip(
                                    onClick = {
                                        modelReference = m
                                        recalculateCommission(brand, m)
                                    },
                                    label = { Text(m, fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    // Commission (Calculated & Editable)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = commissionText,
                            onValueChange = { commissionText = it },
                            label = { Text("Comisión Ganada ($)") },
                            leadingIcon = {
                                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = EmeraldSuccess)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("commission_amount_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldSuccess,
                                focusedLabelColor = EmeraldSuccess
                            )
                        )

                        OutlinedTextField(
                            value = salePriceText,
                            onValueChange = { salePriceText = it },
                            label = { Text("Precio Equipo (Opcional)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Order / MIN Number
                    OutlinedTextField(
                        value = orderNumber,
                        onValueChange = { orderNumber = it },
                        label = { Text("Número Radicado / MIN / Contrato") },
                        leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("order_number_input")
                    )

                    // Client details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = clientName,
                            onValueChange = { clientName = it },
                            label = { Text("Nombre Cliente") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )
                        OutlinedTextField(
                            value = clientPhone,
                            onValueChange = { clientPhone = it },
                            label = { Text("Teléfono") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Sale Type Chips
                    Text(
                        text = "Tipo de Operación Call Center",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        saleTypes.forEach { type ->
                            FilterChip(
                                selected = saleType == type,
                                onClick = { saleType = type },
                                label = { Text(type, fontSize = 12.sp) }
                            )
                        }
                    }

                    // Status selection
                    Text(
                        text = "Estado de la Venta",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        statuses.forEach { st ->
                            val label = when (st) {
                                "APROBADA" -> "Aprobada"
                                "EN_VALIDACION" -> "En Validación"
                                else -> "Anulada"
                            }
                            FilterChip(
                                selected = status == st,
                                onClick = { status = st },
                                label = { Text(label) }
                            )
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas / Observaciones del Asesor") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val commAmount = commissionText.toDoubleOrNull() ?: 0.0
                            val salePrice = salePriceText.toDoubleOrNull() ?: 0.0
                            val sale = initialSale?.copy(
                                brand = brand.trim(),
                                modelReference = modelReference.trim(),
                                commissionAmount = commAmount,
                                salePrice = salePrice,
                                orderNumber = orderNumber.trim(),
                                clientName = clientName.trim(),
                                clientPhone = clientPhone.trim(),
                                saleType = saleType,
                                status = status,
                                notes = notes.trim()
                            ) ?: SaleEntity(
                                dateMillis = System.currentTimeMillis(),
                                monthYearKey = currentMonthKey,
                                brand = brand.trim(),
                                modelReference = modelReference.trim().ifBlank { "Celular $brand" },
                                commissionAmount = commAmount,
                                salePrice = salePrice,
                                orderNumber = orderNumber.trim(),
                                clientName = clientName.trim(),
                                clientPhone = clientPhone.trim(),
                                saleType = saleType,
                                status = status,
                                notes = notes.trim()
                            )
                            onSave(sale)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_sale_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (initialSale == null) "Guardar Venta" else "Actualizar")
                    }
                }
            }
        }
    }
}
