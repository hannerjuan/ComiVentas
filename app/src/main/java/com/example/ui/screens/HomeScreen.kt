package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddSaleDialog by remember { mutableStateOf(false) }

    val selectedMonthKey by viewModel.selectedMonthKey.collectAsState()
    val availableMonths by viewModel.availableMonths.collectAsState()
    val summary by viewModel.monthlySummary.collectAsState()
    val advisorProfile by viewModel.advisorProfile.collectAsState()
    val filteredSales by viewModel.filteredSales.collectAsState()
    val allSales by viewModel.allSales.collectAsState()
    val rules by viewModel.commissionRules.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsState()
    val selectedBrand by viewModel.selectedBrandFilter.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "ComiVentas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = advisorProfile.campaignName,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.testTag("main_top_app_bar")
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Ventas") },
                    label = { Text("Ventas", fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_sales")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Historial") },
                    label = { Text("Historial", fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_history")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Métricas") },
                    label = { Text("Métricas", fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_metrics")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                    label = { Text("Ajustes", fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showAddSaleDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Registrar Venta", fontWeight = FontWeight.Bold) },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_sale")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Month Selector Bar (Shown on Ventas, Historial, Métricas)
            if (selectedTab != 3) {
                MonthSelectorBar(
                    selectedMonthKey = selectedMonthKey,
                    availableMonths = availableMonths,
                    onSelectMonth = { monthKey -> viewModel.selectMonth(monthKey) }
                )
            }

            when (selectedTab) {
                0 -> {
                    SalesListScreen(
                        sales = filteredSales,
                        summary = summary,
                        advisorName = advisorProfile.advisorName,
                        campaignName = advisorProfile.campaignName,
                        searchQuery = searchQuery,
                        selectedStatus = selectedStatus,
                        selectedBrand = selectedBrand,
                        onSearchChange = { q -> viewModel.setSearchQuery(q) },
                        onStatusSelect = { s -> viewModel.setStatusFilter(s) },
                        onBrandSelect = { b -> viewModel.setBrandFilter(b) },
                        onStatusChange = { sale, status -> viewModel.updateSaleStatus(sale, status) },
                        onDeleteSale = { sale -> viewModel.deleteSale(sale) },
                        onEditGoal = { selectedTab = 3 },
                        onAddNewSale = { showAddSaleDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
                1 -> {
                    HistoryScreen(
                        allSales = allSales,
                        onSelectMonth = { monthKey ->
                            viewModel.selectMonth(monthKey)
                            selectedTab = 0
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                2 -> {
                    SalesMetricsScreen(
                        sales = filteredSales,
                        summary = summary,
                        modifier = Modifier.weight(1f)
                    )
                }
                3 -> {
                    CommissionSettingsScreen(
                        profile = advisorProfile,
                        rules = rules,
                        onUpdateProfile = { name, campaign, amount, units ->
                            viewModel.updateAdvisorProfile(name, campaign, amount, units)
                        },
                        onAddOrUpdateRule = { rule ->
                            viewModel.addOrUpdateCommissionRule(rule)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showAddSaleDialog) {
        AddSaleDialog(
            initialMonthKey = selectedMonthKey,
            onCalculateCommission = { brand, price ->
                viewModel.calculateCommissionFor(brand, price)
            },
            onDismiss = { showAddSaleDialog = false },
            onSaveSale = { brand, model, price, saleDate, customerName, imei, status, notes ->
                viewModel.registerSale(
                    brand = brand,
                    model = model,
                    price = price,
                    saleDate = saleDate,
                    customerName = customerName,
                    imeiOrContract = imei,
                    status = status,
                    notes = notes
                )
            }
        )
    }
}
