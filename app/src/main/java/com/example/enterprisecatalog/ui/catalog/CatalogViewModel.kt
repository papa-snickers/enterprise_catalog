package com.example.enterprisecatalog.ui.catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisecatalog.data.model.ApiResult
import com.example.enterprisecatalog.data.model.Enterprise
import com.example.enterprisecatalog.data.repository.EnterpriseRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val enterprises: List<Enterprise> = emptyList(),
    val categories: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedEnterprise: Enterprise? = null,
    val selectedCategory: String? = null
)

class CatalogViewModel(
    private val repository: EnterpriseRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    val searchQuery: StateFlow<String> = savedStateHandle.getStateFlow("search_query", "")

    private var searchJob: Job? = null

    init {
        loadCategories()
        loadEnterprises()
    }

    fun onSearchQueryChange(query: String) {
        savedStateHandle["search_query"] = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            loadEnterprises()
        }
    }

    fun onCategorySelected(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadEnterprises()
    }

    fun loadEnterprises() {
        val q = searchQuery.value
        val cat = _uiState.value.selectedCategory
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getEnterprises(
                query = q.takeIf { it.isNotBlank() },
                category = cat
            )) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(enterprises = result.data, isLoading = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(error = result.message, isLoading = false)
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = repository.getCategories()) {
                is ApiResult.Success -> _uiState.update { it.copy(categories = result.data) }
                else -> {}
            }
        }
    }

    fun selectEnterprise(enterprise: Enterprise?) {
        _uiState.update { it.copy(selectedEnterprise = enterprise) }
    }
}
