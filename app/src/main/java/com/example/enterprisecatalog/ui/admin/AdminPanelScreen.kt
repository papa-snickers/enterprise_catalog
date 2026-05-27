package com.example.enterprisecatalog.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.enterprisecatalog.data.local.DataStoreManager
import com.example.enterprisecatalog.data.model.Enterprise
import com.example.enterprisecatalog.data.repository.EnterpriseRepository
import com.example.enterprisecatalog.ui.catalog.*
import com.example.enterprisecatalog.ui.common.EmptyPlaceholder
import com.example.enterprisecatalog.ui.common.ErrorPlaceholder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    enterpriseRepository: EnterpriseRepository,
    dataStoreManager: DataStoreManager,
    onToggleTheme: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val viewModel: AdminViewModel = viewModel {
        AdminViewModel(enterpriseRepository, createSavedStateHandle())
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val darkTheme by dataStoreManager.getDarkThemeFlow().collectAsStateWithLifecycle(false)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var enterpriseToDelete by remember { mutableStateOf<String?>(null) }
    var searchFocused by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.deleteMessage) {
        if (uiState.deleteMessage != null) {
            snackbarHostState.showSnackbar(uiState.deleteMessage!!)
            viewModel.clearDeleteMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Панель администратора") },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = "Переключить тему"
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Профиль")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить предприятие")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Каталог") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Статистика") }
                )
            }

            when (selectedTab) {
                0 -> AdminCatalogTab(
                    uiState = uiState,
                    searchQuery = searchQuery,
                    searchFocused = searchFocused,
                    onSearchFocusChange = { searchFocused = it },
                    onSearchChange = { viewModel.onSearchQueryChange(it) },
                    onCategorySelected = { viewModel.onCategorySelected(it) },
                    onRetry = { viewModel.loadEnterprises() },
                    onCardClick = { enterprise ->
                        viewModel.selectEnterprise(enterprise)
                        showBottomSheet = true
                    },
                    onEdit = { onNavigateToEdit(it) },
                    onDelete = { enterpriseToDelete = it }
                )
                1 -> StatisticsTab(uiState = uiState)
            }
        }
    }

    // Bottom sheet
    if (showBottomSheet && uiState.selectedEnterprise != null) {
        EnterpriseBottomSheet(
            enterprise = uiState.selectedEnterprise!!,
            isAdmin = true,
            onDismiss = {
                showBottomSheet = false
                viewModel.selectEnterprise(null)
            },
            onEdit = { onNavigateToEdit(uiState.selectedEnterprise!!.id) },
            onDelete = { enterpriseToDelete = uiState.selectedEnterprise!!.id }
        )
    }

    // Delete confirmation dialog
    if (enterpriseToDelete != null) {
        AlertDialog(
            onDismissRequest = { enterpriseToDelete = null },
            title = { Text("Удалить предприятие?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEnterprise(enterpriseToDelete!!)
                        enterpriseToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { enterpriseToDelete = null }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun AdminCatalogTab(
    uiState: AdminUiState,
    searchQuery: String,
    searchFocused: Boolean,
    onSearchFocusChange: (Boolean) -> Unit,
    onSearchChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onRetry: () -> Unit,
    onCardClick: (Enterprise) -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            SearchBarField(
                value = searchQuery,
                onValueChange = onSearchChange,
                onFocusChange = onSearchFocusChange
            )
        }
        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        if (uiState.categories.isNotEmpty()) {
            CategoryChips(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = onCategorySelected,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.error != null -> ErrorPlaceholder(
                    message = uiState.error!!,
                    onRetry = onRetry,
                    modifier = Modifier.padding(32.dp)
                )
                !uiState.isLoading && uiState.enterprises.isEmpty() -> EmptyPlaceholder(
                    modifier = Modifier.padding(32.dp)
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.enterprises, key = { it.id }) { enterprise ->
                        EnterpriseCard(
                            enterprise = enterprise,
                            onClick = { onCardClick(enterprise) },
                            isAdmin = true,
                            onEdit = { onEdit(enterprise.id) },
                            onDelete = { onDelete(enterprise.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsTab(uiState: AdminUiState) {
    val byCategory = uiState.enterprises
        .groupBy { it.specialization }
        .map { (k, v) -> k to v.size }
        .sortedByDescending { it.second }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Всего предприятий", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "${uiState.enterprises.size}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        if (byCategory.isNotEmpty()) {
            item {
                Text(
                    text = "По специализации",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(byCategory) { (category, count) ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = category, style = MaterialTheme.typography.bodyMedium)
                        Badge { Text("$count") }
                    }
                }
            }
        }
    }
}
