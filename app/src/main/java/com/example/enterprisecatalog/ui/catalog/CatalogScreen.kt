package com.example.enterprisecatalog.ui.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.enterprisecatalog.data.local.DataStoreManager
import com.example.enterprisecatalog.data.repository.EnterpriseRepository
import com.example.enterprisecatalog.ui.common.EmptyPlaceholder
import com.example.enterprisecatalog.ui.common.ErrorPlaceholder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    enterpriseRepository: EnterpriseRepository,
    dataStoreManager: DataStoreManager,
    onToggleTheme: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToFavorites: () -> Unit = {}
) {
    val viewModel: CatalogViewModel = viewModel {
        CatalogViewModel(enterpriseRepository, createSavedStateHandle())
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val darkTheme by dataStoreManager.getDarkThemeFlow().collectAsStateWithLifecycle(false)
    val searchHistory by dataStoreManager.getSearchHistoryFlow().collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()

    var searchFocused by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Каталог предприятий") },
                actions = {
                    IconButton(onClick = onNavigateToFavorites) {
                        Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Избранное")
                    }
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
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SearchBarField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        onFocusChange = { searchFocused = it }
                    )
                }

                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                if (uiState.categories.isNotEmpty()) {
                    CategoryChips(
                        categories = uiState.categories,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = { viewModel.onCategorySelected(it) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    when {
                        uiState.error != null -> ErrorPlaceholder(
                            message = uiState.error!!,
                            onRetry = { viewModel.loadEnterprises() },
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
                                    onClick = {
                                        scope.launch { dataStoreManager.addSearchQuery(enterprise.name) }
                                        viewModel.selectEnterprise(enterprise)
                                        showBottomSheet = true
                                    },
                                    isFavorite = uiState.favoriteIds.contains(enterprise.id),
                                    onToggleFavorite = { viewModel.toggleFavorite(enterprise) }
                                )
                            }
                        }
                    }

                    if (searchFocused && searchHistory.isNotEmpty()) {
                        SearchHistoryCard(
                            history = searchHistory,
                            onSelect = { query ->
                                viewModel.onSearchQueryChange(query)
                                searchFocused = false
                            },
                            onClear = { scope.launch { dataStoreManager.clearSearchHistory() } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet && uiState.selectedEnterprise != null) {
        val enterprise = uiState.selectedEnterprise!!
        EnterpriseBottomSheet(
            enterprise = enterprise,
            isAdmin = false,
            onDismiss = {
                showBottomSheet = false
                viewModel.selectEnterprise(null)
            },
            isFavorite = uiState.favoriteIds.contains(enterprise.id),
            onToggleFavorite = { viewModel.toggleFavorite(enterprise) }
        )
    }
}
