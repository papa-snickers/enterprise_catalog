package com.example.enterprisecatalog.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisecatalog.data.model.ApiResult
import com.example.enterprisecatalog.data.model.Enterprise
import com.example.enterprisecatalog.data.repository.EnterpriseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val favorites: List<Enterprise> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedEnterprise: Enterprise? = null
)

class FavoritesViewModel(private val repository: EnterpriseRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init { loadFavorites() }

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getFavorites()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(favorites = result.data, isLoading = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(error = result.message, isLoading = false)
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun removeFromFavorites(enterprise: Enterprise) {
        viewModelScope.launch {
            repository.removeFavorite(enterprise.id)
            _uiState.update { it.copy(favorites = it.favorites.filter { e -> e.id != enterprise.id }) }
        }
    }

    fun selectEnterprise(enterprise: Enterprise?) {
        _uiState.update { it.copy(selectedEnterprise = enterprise) }
    }
}
