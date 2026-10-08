package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.GreenSuccessLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.formatCurrency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleDialog(
    initialMonthKey: String,
    onCalculateCommission: (String, Double) -> Double,
    onDismiss: () -> Unit,
    onSaveSale: (
        brand: String,
        model: String,
        price: Double,
        saleDate: String,
        customerName: String,
        imeiOrContract: String,
        status: String,
        notes: String
    ) -> Unit
) {
    val brands = listOf("Samsung", "Xiaomi", "Apple", "Motorola", "Honor", "Oppo", "Infinix / Tecno", "Otra")
    val statuses = listOf("APROBADA", "EN_VALIDACION", "ANULADA")

    var selectedBrand by remember { mutableStateOf(brands[0]) }
    var brandDropdownExpanded by remember { mutableStateOf(false) }

    var modelText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    var saleDateText by remember { mutableStateOf(todayStr) }

    var customerNameText by remember { mutableStateOf("") }
    var imeiText by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf(statuses[0]) }
    var notesText by remember { mutableStateOf("") }

    val numericPrice = priceText.toDoubleOrNull() ?: 0.0
    val estimatedCommission = remember(selectedBrand, numericPrice) {
        onCalculateCommission(selectedBrand, numericPrice)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("add_sale_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Registrar Nueva Venta",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ingresa los datos del equipo y cliente",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_add_sale_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Brand Selection Dropdown
                    Text(
                        text = "Marca del celular *",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedBrand,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar marca")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { brandDropdownExpanded = true }
                                .testTag("brand_dropdown_field"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        DropdownMenu(
                            expanded = brandDropdownExpanded,
                            onDismissRequest = { brandDropdownExpanded = false }
                        ) {
                            brands.forEach { brand ->
                                DropdownMenuItem(
                                    text = { Text(brand) },
                                    onClick = {
                                        selectedBrand = brand
                                        brandDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Model / Reference
                    Text(
                        text = "Modelo / Referencia *",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = modelText,
                        onValueChange = { modelText = it },
                        placeholder = { Text("Ej. Galaxy S24 FE 256GB") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("model_input_field"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Sale Price
                    Text(
                        text = "Precio de Venta ($ COP) *",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { char -> char.isDigit() } },
                        placeholder = { Text("Ej. 2499000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("price_input_field"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Live Commission Preview Banner
                    if (numericPrice > 0) {
                        Surface(
                            color = GreenSuccessLight,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = GreenSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Comisión calculada:",
                                        color = GreenSuccess,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    text = formatCurrency(estimatedCommission),
                                    color = GreenSuccess,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    // Sale Date
                    Text(
                        text = "Fecha de Venta (AAAA-MM-DD) *",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = saleDateText,
                        onValueChange = { saleDateText = it },
                        placeholder = { Text("2026-10-05") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("date_input_field"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Customer Name
                    Text(
                        text = "Nombre del Cliente",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = customerNameText,
                        onValueChange = { customerNameText = it },
                        placeholder = { Text("Ej. Sandra Milena Pérez") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("customer_input_field"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // IMEI / Contract number
                    Text(
                        text = "IMEI o N° de Contrato",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = imeiText,
                        onValueChange = { imeiText = it },
                        placeholder = { Text("Ej. 864201061234567") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("imei_input_field"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Status selection
                    Text(
                        text = "Estado Inicial",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        statuses.forEach { status ->
                            val isSelected = selectedStatus == status
                            OutlinedButton(
                                onClick = { selectedStatus = status },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) PrimaryBlue.copy(alpha = 0.1f) else Color.Transparent
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        if (isSelected) PrimaryBlue else Color.LightGray
                                    )
                                ),
                                modifier = Modifier.weight(1f).testTag("select_status_$status")
                            ) {
                                Text(
                                    text = when (status) {
                                        "APROBADA" -> "Aprobada"
                                        "EN_VALIDACION" -> "Validación"
                                        else -> "Anulada"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Notes
                    Text(
                        text = "Observaciones",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        placeholder = { Text("Ej. Portabilidad con plan de 50GB") },
                        modifier = Modifier.fillMaxWidth().testTag("notes_input_field"),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions: Cancel & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).testTag("cancel_sale_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancelar")
                    }

                    val canSave = modelText.isNotBlank() && numericPrice > 0
                    Button(
                        onClick = {
                            if (canSave) {
                                onSaveSale(
                                    selectedBrand,
                                    modelText.trim(),
                                    numericPrice,
                                    saleDateText.trim(),
                                    customerNameText.trim(),
                                    imeiText.trim(),
                                    selectedStatus,
                                    notesText.trim()
                                )
                                onDismiss()
                            }
                        },
                        enabled = canSave,
                        modifier = Modifier.weight(1f).testTag("save_sale_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Guardar Venta")
                    }
                }
            }
        }
    }
}
