package com.example.ui.screens

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.MonthlyGoalEntity
import com.example.ui.model.Formatters

@Composable
fun MonthlyGoalDialog(
    currentGoal: MonthlyGoalEntity?,
    monthKey: String,
    onDismiss: () -> Unit,
    onSave: (salesTarget: Int, commissionTarget: Double, advisor: String, campaign: String) -> Unit
) {
    var salesTargetText by remember {
        mutableStateOf(currentGoal?.targetSalesCount?.toString() ?: "30")
    }
    var commissionTargetText by remember {
        mutableStateOf(currentGoal?.targetCommissionAmount?.toLong()?.toString() ?: "1500000")
    }
    var advisorName by remember {
        mutableStateOf(currentGoal?.advisorName ?: "Asesor Call Center")
    }
    var campaignName by remember {
        mutableStateOf(currentGoal?.campaignName ?: "Venta Móvil & Portabilidad")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .testTag("goal_dialog"),
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
                            text = "Meta del Mes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Formatters.formatMonthName(monthKey),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Text(
                    text = "Configura tus objetivos mensuales para llevar control visual del progreso en tu turno de call center.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Sales target count
                OutlinedTextField(
                    value = salesTargetText,
                    onValueChange = { salesTargetText = it },
                    label = { Text("Meta de Celulares a Vender (Unidades)") },
                    leadingIcon = { Icon(Icons.Default.PhoneIphone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_sales_input")
                )

                // Commission target amount
                OutlinedTextField(
                    value = commissionTargetText,
                    onValueChange = { commissionTargetText = it },
                    label = { Text("Meta de Comisiones ($)") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_commission_input")
                )

                // Advisor Name
                OutlinedTextField(
                    value = advisorName,
                    onValueChange = { advisorName = it },
                    label = { Text("Nombre del Asesor") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Campaign
                OutlinedTextField(
                    value = campaignName,
                    onValueChange = { campaignName = it },
                    label = { Text("Campaña / Operador / Turno") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

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
                            val targetSales = salesTargetText.toIntOrNull() ?: 30
                            val targetComm = commissionTargetText.toDoubleOrNull() ?: 1500000.0
                            onSave(targetSales, targetComm, advisorName.trim(), campaignName.trim())
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_goal_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guardar Meta")
                    }
                }
            }
        }
    }
}
