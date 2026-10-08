package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Paid
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
import com.example.data.local.BrandEntity
import com.example.ui.model.Formatters

private val BRAND_PALETTE = listOf(
    "#0C2340", // Samsung Navy
    "#555555", // Apple Dark Gray
    "#FF6900", // Xiaomi Orange
    "#001489", // Motorola Blue
    "#0091FF", // Honor Cyan
    "#00875A", // Oppo Emerald
    "#EAB308", // Realme Yellow
    "#8B5CF6", // Purple
    "#EC4899", // Magenta
    "#14B8A6", // Teal
    "#EF4444", // Red
    "#3B82F6"  // Electric Blue
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBrandDialog(
    initialBrand: BrandEntity? = null,
    onDismiss: () -> Unit,
    onSave: (BrandEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialBrand?.name ?: "") }
    var defaultCommissionText by remember {
        mutableStateOf(
            if (initialBrand != null) initialBrand.defaultCommission.toLong().toString() else "35000"
        )
    }
    var suggestedModels by remember { mutableStateOf(initialBrand?.suggestedModels ?: "") }
    var isEnabled by remember { mutableStateOf(initialBrand?.isEnabled ?: true) }
    var selectedColor by remember { mutableStateOf(initialBrand?.colorHex ?: "#0C2340") }
    var notes by remember { mutableStateOf(initialBrand?.notes ?: "") }

    var nameError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .testTag("add_edit_brand_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val brandColor = try {
                        Color(android.graphics.Color.parseColor(selectedColor))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    Surface(
                        shape = CircleShape,
                        color = brandColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BrandingWatermark,
                                contentDescription = null,
                                tint = brandColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = if (initialBrand == null) "Nueva Marca de Celulares" else "Editar Marca",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Configuración de marca y comisiones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                // Brand Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Nombre de la Marca *") },
                    placeholder = { Text("Ej. Samsung, Apple, Infinix...") },
                    leadingIcon = {
                        Icon(Icons.Default.BrandingWatermark, contentDescription = null)
                    },
                    isError = nameError,
                    supportingText = {
                        if (nameError) Text("El nombre de la marca es obligatorio", color = MaterialTheme.colorScheme.error)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("brand_name_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                // Default Commission Rate
                OutlinedTextField(
                    value = defaultCommissionText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) defaultCommissionText = input
                    },
                    label = { Text("Comisión Base Predeterminada ($ COP) *") },
                    placeholder = { Text("Ej. 40000") },
                    leadingIcon = {
                        Icon(Icons.Default.Paid, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        val parsed = defaultCommissionText.toDoubleOrNull() ?: 0.0
                        Text(
                            text = Formatters.formatCurrency(parsed),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("brand_default_commission_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                // Color Picker
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Color Identificador de la Marca",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BRAND_PALETTE.take(6).forEach { hex ->
                            ColorCircle(
                                colorHex = hex,
                                isSelected = selectedColor.equals(hex, ignoreCase = true),
                                onSelect = { selectedColor = hex }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BRAND_PALETTE.drop(6).forEach { hex ->
                            ColorCircle(
                                colorHex = hex,
                                isSelected = selectedColor.equals(hex, ignoreCase = true),
                                onSelect = { selectedColor = hex }
                            )
                        }
                    }
                }

                // Suggested Models List
                OutlinedTextField(
                    value = suggestedModels,
                    onValueChange = { suggestedModels = it },
                    label = { Text("Modelos Populares / Referencias") },
                    placeholder = { Text("Ej: Galaxy S24 Ultra, Galaxy A55, Galaxy A35...") },
                    leadingIcon = {
                        Icon(Icons.Default.Devices, contentDescription = null)
                    },
                    supportingText = {
                        Text("Separados por coma. Aparecerán al registrar ventas de esta marca.")
                    },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("brand_models_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                // Active Switch
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Marca Activa",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isEnabled) "Disponible para registrar ventas y comisiones" else "Oculta temporalmente",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            modifier = Modifier.testTag("brand_enabled_switch")
                        )
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas u Observaciones (Opcional)") },
                    leadingIcon = {
                        Icon(Icons.Default.Notes, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                                return@Button
                            }
                            val commAmount = defaultCommissionText.toDoubleOrNull() ?: 35000.0
                            val brandToSave = initialBrand?.copy(
                                name = name.trim(),
                                defaultCommission = commAmount,
                                isEnabled = isEnabled,
                                suggestedModels = suggestedModels.trim(),
                                colorHex = selectedColor,
                                notes = notes.trim()
                            ) ?: BrandEntity(
                                name = name.trim(),
                                defaultCommission = commAmount,
                                isEnabled = isEnabled,
                                suggestedModels = suggestedModels.trim(),
                                colorHex = selectedColor,
                                notes = notes.trim()
                            )
                            onSave(brandToSave)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_brand_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorCircle(
    colorHex: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val color = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onSelect)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.3f),
                shape = CircleShape
            )
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
