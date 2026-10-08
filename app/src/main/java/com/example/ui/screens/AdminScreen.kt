package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BrandEntity
import com.example.data.local.CommissionRuleEntity
import com.example.data.local.MonthlyGoalEntity
import com.example.ui.model.Formatters
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessLight
import com.example.ui.theme.RoseError
import com.example.ui.theme.RoseErrorLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    selectedMonthKey: String,
    availableMonths: List<String>,
    brands: List<BrandEntity>,
    rules: List<CommissionRuleEntity>,
    currentGoal: MonthlyGoalEntity?,
    onMonthSelected: (String) -> Unit,
    onAddBrand: (BrandEntity) -> Unit,
    onUpdateBrand: (BrandEntity) -> Unit,
    onDeleteBrand: (BrandEntity) -> Unit,
    onResetBrandsToDefault: () -> Unit,
    onSaveRule: (CommissionRuleEntity) -> Unit,
    onUpdateRule: (CommissionRuleEntity) -> Unit,
    onDeleteRule: (CommissionRuleEntity) -> Unit,
    onSetBrandBaseRate: (brand: String, amount: Double) -> Unit,
    onApplyDefaultBrandRates: () -> Unit,
    onApplyRateAdjustment: (percent: Double?, fixedBonus: Double?) -> Unit,
    onCopyRulesFromPreviousMonth: () -> Unit,
    onSaveGoal: (targetSales: Int, targetCommission: Double, advisor: String, campaign: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var adminTab by remember { mutableIntStateOf(0) } // 0: Tasas Mensuales, 1: Marcas de Celulares, 2: Metas & Asesor

    // Dialog states
    var showAddBrandDialog by remember { mutableStateOf(false) }
    var brandToEdit by remember { mutableStateOf<BrandEntity?>(null) }
    var brandToDelete by remember { mutableStateOf<BrandEntity?>(null) }
    var showResetBrandsConfirm by remember { mutableStateOf(false) }

    var showAddRuleDialog by remember { mutableStateOf(false) }
    var ruleToEdit by remember { mutableStateOf<CommissionRuleEntity?>(null) }
    var ruleToDelete by remember { mutableStateOf<CommissionRuleEntity?>(null) }
    var showBatchAdjustDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            when (adminTab) {
                0 -> {
                    ExtendedFloatingActionButton(
                        onClick = { showAddRuleDialog = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Nueva Tarifa") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("admin_add_rule_fab")
                    )
                }
                1 -> {
                    ExtendedFloatingActionButton(
                        onClick = { showAddBrandDialog = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Nueva Marca") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("admin_add_brand_fab")
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Screen Header
            AdminHeaderCard()

            // Primary Admin Tabs
            PrimaryTabRow(
                selectedTabIndex = adminTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(
                    selected = adminTab == 0,
                    onClick = { adminTab = 0 },
                    text = { Text("Tasas Mensuales", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Paid, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_monthly_rates")
                )
                Tab(
                    selected = adminTab == 1,
                    onClick = { adminTab = 1 },
                    text = { Text("Marcas", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.BrandingWatermark, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_brands_admin")
                )
                Tab(
                    selected = adminTab == 2,
                    onClick = { adminTab = 2 },
                    text = { Text("Metas Call Center", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_goals_admin")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            when (adminTab) {
                0 -> {
                    MonthlyRatesTabContent(
                        selectedMonthKey = selectedMonthKey,
                        availableMonths = availableMonths,
                        brands = brands,
                        rules = rules,
                        onMonthSelected = onMonthSelected,
                        onSetBrandBaseRate = onSetBrandBaseRate,
                        onApplyDefaultBrandRates = onApplyDefaultBrandRates,
                        onOpenBatchAdjust = { showBatchAdjustDialog = true },
                        onCopyFromPreviousMonth = onCopyRulesFromPreviousMonth,
                        onAddSpecialModelRate = { showAddRuleDialog = true },
                        onEditRule = { ruleToEdit = it },
                        onDeleteRule = { ruleToDelete = it }
                    )
                }
                1 -> {
                    BrandsManagementTabContent(
                        brands = brands,
                        onAddBrand = { showAddBrandDialog = true },
                        onEditBrand = { brandToEdit = it },
                        onDeleteBrand = { brandToDelete = it },
                        onResetToDefaults = { showResetBrandsConfirm = true }
                    )
                }
                2 -> {
                    GoalsTabContent(
                        selectedMonthKey = selectedMonthKey,
                        goal = currentGoal,
                        onEditGoal = { showGoalDialog = true }
                    )
                }
            }
        }
    }

    // Add / Edit Brand Dialog
    if (showAddBrandDialog || brandToEdit != null) {
        AddEditBrandDialog(
            initialBrand = brandToEdit,
            onDismiss = {
                showAddBrandDialog = false
                brandToEdit = null
            },
            onSave = { brand ->
                if (brandToEdit != null) {
                    onUpdateBrand(brand)
                } else {
                    onAddBrand(brand)
                }
                showAddBrandDialog = false
                brandToEdit = null
            }
        )
    }

    // Delete Brand Confirmation Dialog
    if (brandToDelete != null) {
        AlertDialog(
            onDismissRequest = { brandToDelete = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = RoseError) },
            title = { Text("¿Eliminar marca ${brandToDelete?.name}?") },
            text = {
                Text("Se eliminará del catálogo de marcas. Las ventas anteriores registradas conservarán su información.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        brandToDelete?.let { onDeleteBrand(it) }
                        brandToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { brandToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Reset Brands Confirmation Dialog
    if (showResetBrandsConfirm) {
        AlertDialog(
            onDismissRequest = { showResetBrandsConfirm = false },
            icon = { Icon(Icons.Default.RestartAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Restaurar Catálogo de Marcas") },
            text = {
                Text("Esto restaurará las marcas predeterminadas del sistema (Samsung, Apple, Xiaomi, Motorola, Honor, Oppo, Realme) con sus modelos y comisiones base estándar.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetBrandsToDefault()
                        showResetBrandsConfirm = false
                    }
                ) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetBrandsConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Batch Adjust Rates Dialog
    if (showBatchAdjustDialog) {
        BatchAdjustRatesDialog(
            monthKey = selectedMonthKey,
            onDismiss = { showBatchAdjustDialog = false },
            onApply = { percent, fixedBonus ->
                onApplyRateAdjustment(percent, fixedBonus)
                showBatchAdjustDialog = false
            }
        )
    }

    // Add / Edit Special Rule Dialog
    if (showAddRuleDialog || ruleToEdit != null) {
        AddEditRuleDialog(
            initialRule = ruleToEdit,
            monthKey = selectedMonthKey,
            availableBrands = brands,
            onDismiss = {
                showAddRuleDialog = false
                ruleToEdit = null
            },
            onSave = { rule ->
                if (ruleToEdit != null) {
                    onUpdateRule(rule)
                } else {
                    onSaveRule(rule)
                }
                showAddRuleDialog = false
                ruleToEdit = null
            }
        )
    }

    // Delete Rule Confirmation Dialog
    if (ruleToDelete != null) {
        AlertDialog(
            onDismissRequest = { ruleToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = RoseError) },
            title = { Text("¿Eliminar tarifa?") },
            text = {
                val label = if (ruleToDelete?.modelReference.isNullOrBlank()) {
                    "tarifa base de ${ruleToDelete?.brand}"
                } else {
                    "tarifa especial de ${ruleToDelete?.brand} ${ruleToDelete?.modelReference}"
                }
                Text("¿Estás seguro de eliminar la $label?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        ruleToDelete?.let { onDeleteRule(it) }
                        ruleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { ruleToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Goal Dialog
    if (showGoalDialog) {
        MonthlyGoalDialog(
            monthKey = selectedMonthKey,
            currentGoal = currentGoal,
            onDismiss = { showGoalDialog = false },
            onSave = { sales, commission, advisor, campaign ->
                onSaveGoal(sales, commission, advisor, campaign)
                showGoalDialog = false
            }
        )
    }
}

@Composable
private fun AdminHeaderCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1E293B),
                            Color(0xFF1E3A8A).copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2563EB).copy(alpha = 0.35f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color(0xFF93C5FD),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Panel de Administración",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Configuración de marcas y tarifas mensuales de comisión",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 0: Tasas Mensuales
// ----------------------------------------------------
@Composable
private fun MonthlyRatesTabContent(
    selectedMonthKey: String,
    availableMonths: List<String>,
    brands: List<BrandEntity>,
    rules: List<CommissionRuleEntity>,
    onMonthSelected: (String) -> Unit,
    onSetBrandBaseRate: (brand: String, amount: Double) -> Unit,
    onApplyDefaultBrandRates: () -> Unit,
    onOpenBatchAdjust: () -> Unit,
    onCopyFromPreviousMonth: () -> Unit,
    onAddSpecialModelRate: () -> Unit,
    onEditRule: (CommissionRuleEntity) -> Unit,
    onDeleteRule: (CommissionRuleEntity) -> Unit
) {
    val activeBrands = brands.filter { it.isEnabled }
    val baseRules = rules.filter { it.modelReference.isNullOrBlank() }
    val specialRules = rules.filter { !it.modelReference.isNullOrBlank() }

    val baseRuleMap = remember(rules) {
        rules.filter { it.modelReference.isNullOrBlank() }
            .associateBy { it.brand.lowercase() }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // Month Selector Bar
        item {
            MonthSelectorBar(
                selectedMonthKey = selectedMonthKey,
                availableMonths = availableMonths,
                onMonthSelected = onMonthSelected
            )
        }

        // Summary KPI Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tarifas Base para ${Formatters.formatMonthName(selectedMonthKey)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${baseRules.size} de ${activeBrands.size} marcas configuradas • ${specialRules.size} modelos destacados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${baseRules.size}/${activeBrands.size}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Action Buttons Row (Horizontal Scroll)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenBatchAdjust,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajuste Masivo", fontSize = 12.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = onApplyDefaultBrandRates,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cargar Catálogo", fontSize = 12.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = onCopyFromPreviousMonth,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copiar Anterior", fontSize = 12.sp, maxLines = 1)
                }
            }
        }

        // Section Title: Base Brand Rates
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TASAS BASE POR MARCA",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Ajusta la comisión mensual",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // List of Active Brands with inline rate editing
        items(activeBrands, key = { "brand_${it.id}" }) { brand ->
            val existingRule = baseRuleMap[brand.name.lowercase()]
            val currentAmount = existingRule?.commissionAmount ?: brand.defaultCommission

            BrandRateConfigRow(
                brand = brand,
                currentAmount = currentAmount,
                hasCustomRuleForMonth = existingRule != null,
                onSaveRate = { newAmount ->
                    onSetBrandBaseRate(brand.name, newAmount)
                }
            )
        }

        // Section Title: Special Model Rules
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TARIFAS DESTACADAS POR MODELO",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                TextButton(
                    onClick = onAddSpecialModelRate,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir Modelo")
                }
            }
        }

        if (specialRules.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay tarifas por modelo específico este mes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Se aplicará la comisión base de cada marca a todos los celulares.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(specialRules, key = { "special_${it.id}" }) { rule ->
                CommissionRuleCard(
                    rule = rule,
                    onEdit = { onEditRule(rule) },
                    onDelete = { onDeleteRule(rule) }
                )
            }
        }
    }
}

@Composable
private fun BrandRateConfigRow(
    brand: BrandEntity,
    currentAmount: Double,
    hasCustomRuleForMonth: Boolean,
    onSaveRate: (Double) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember(currentAmount) { mutableStateOf(currentAmount.toLong().toString()) }

    val brandColor = try {
        Color(android.graphics.Color.parseColor(brand.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = brandColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, brandColor.copy(alpha = 0.35f)),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = brand.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = brandColor,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = brand.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (hasCustomRuleForMonth) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldSuccessLight
                                ) {
                                    Text(
                                        text = "Fijada en Mes",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSuccess,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "Por Defecto",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        if (brand.suggestedModels.isNotBlank()) {
                            Text(
                                text = brand.suggestedModels,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Current Rate and Quick Edit Toggle
                if (!isEditing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = Formatters.formatCurrency(currentAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldSuccess
                        )
                        IconButton(
                            onClick = {
                                editText = currentAmount.toLong().toString()
                                isEditing = true
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar tasa",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Inline Edit Form
            AnimatedVisibility(visible = isEditing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) editText = input
                            },
                            label = { Text("Nueva Tasa Base ($ COP)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        IconButton(
                            onClick = {
                                val parsed = editText.toDoubleOrNull() ?: currentAmount
                                onSaveRate(parsed)
                                isEditing = false
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Confirmar",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        IconButton(
                            onClick = { isEditing = false },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 1: Marcas de Celulares
// ----------------------------------------------------
@Composable
private fun BrandsManagementTabContent(
    brands: List<BrandEntity>,
    onAddBrand: () -> Unit,
    onEditBrand: (BrandEntity) -> Unit,
    onDeleteBrand: (BrandEntity) -> Unit,
    onResetToDefaults: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
    ) {
        // Banner info & Reset catalog button
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Catálogo de Marcas (${brands.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Configura marcas, colores identificadores, referencias y tarifas por defecto.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onResetToDefaults,
                        modifier = Modifier.testTag("reset_brands_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Restaurar predeterminados",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        if (brands.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No hay marcas configuradas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onResetToDefaults) {
                            Text("Cargar Marcas Predeterminadas")
                        }
                    }
                }
            }
        } else {
            items(brands, key = { "brand_admin_${it.id}" }) { brand ->
                BrandCardItem(
                    brand = brand,
                    onEdit = { onEditBrand(brand) },
                    onDelete = { onDeleteBrand(brand) }
                )
            }
        }
    }
}

@Composable
private fun BrandCardItem(
    brand: BrandEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val brandColor = try {
        Color(android.graphics.Color.parseColor(brand.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("brand_card_${brand.name}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = brandColor.copy(alpha = 0.18f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, brandColor.copy(alpha = 0.4f)),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = brand.name.take(2).uppercase(),
                                fontWeight = FontWeight.ExtraBold,
                                color = brandColor,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = brand.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (brand.isEnabled) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldSuccessLight
                                ) {
                                    Text(
                                        text = "Activa",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSuccess,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "Inactiva",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Comisión por defecto: ${Formatters.formatCurrency(brand.defaultCommission)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldSuccess
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar marca",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar marca",
                            tint = RoseError,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (brand.suggestedModels.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Modelos: ${brand.suggestedModels}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 2: Metas & Asesor Call Center
// ----------------------------------------------------
@Composable
private fun GoalsTabContent(
    selectedMonthKey: String,
    goal: MonthlyGoalEntity?,
    onEditGoal: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Headphones,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Parámetros del Asesor",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Mes: ${Formatters.formatMonthName(selectedMonthKey)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(onClick = onEditGoal) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar meta", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    HorizontalDivider()

                    GoalParamRow(
                        title = "Asesor Call Center",
                        value = goal?.advisorName ?: "Asesor Call Center",
                        icon = Icons.Default.Person
                    )

                    GoalParamRow(
                        title = "Campaña / Operador",
                        value = goal?.campaignName ?: "Venta Móvil Postpago",
                        icon = Icons.Default.Work
                    )

                    GoalParamRow(
                        title = "Meta Mensual de Ventas",
                        value = "${goal?.targetSalesCount ?: 25} equipos",
                        icon = Icons.Default.PhoneIphone
                    )

                    GoalParamRow(
                        title = "Meta de Comisión Mensual",
                        value = Formatters.formatCurrency(goal?.targetCommissionAmount ?: 1400000.0),
                        icon = Icons.Default.Paid
                    )

                    Button(
                        onClick = onEditGoal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Configurar Metas y Asesor")
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalParamRow(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
