package com.example.enterprisecatalog.ui.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.enterprisecatalog.data.repository.EnterpriseRepository
import com.example.enterprisecatalog.ui.catalog.EnterpriseBottomSheet
import com.example.enterprisecatalog.ui.catalog.EnterpriseCard
import com.example.enterprisecatalog.ui.common.EmptyPlaceholder
import com.example.enterprisecatalog.ui.common.ErrorPlaceholder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    enterpriseRepository: EnterpriseRepository,
    onBack: () -> Unit
) {
    val viewModel: FavoritesViewModel = viewModel {
        FavoritesViewModel(enterpriseRepository)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Избранное") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                uiState.error != null -> ErrorPlaceholder(
                    message = uiState.error!!,
                    onRetry = { viewModel.loadFavorites() },
                    modifier = Modifier.padding(32.dp)
                )
                uiState.favorites.isEmpty() -> EmptyPlaceholder(modifier = Modifier.padding(32.dp))
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.favorites, key = { it.id }) { enterprise ->
                        EnterpriseCard(
                            enterprise = enterprise,
                            onClick = {
                                viewModel.selectEnterprise(enterprise)
                                showBottomSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet && uiState.selectedEnterprise != null) {
        EnterpriseBottomSheet(
            enterprise = uiState.selectedEnterprise!!,
            isAdmin = false,
            onDismiss = {
                showBottomSheet = false
                viewModel.selectEnterprise(null)
            },
            isFavorite = true,
            onToggleFavorite = {
                viewModel.removeFromFavorites(uiState.selectedEnterprise!!)
                showBottomSheet = false
                viewModel.selectEnterprise(null)
            }
        )
    }
}
