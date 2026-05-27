package com.example.enterprisecatalog.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisecatalog.data.model.ApiResult
import com.example.enterprisecatalog.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successRole: String? = null
)

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(login: String, password: String) {
        _uiState.value = LoginUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = repository.login(login, password)) {
                is ApiResult.Success -> {
                    _uiState.value = LoginUiState(successRole = result.data.role)
                }
                is ApiResult.Error -> {
                    _uiState.value = LoginUiState(error = result.message)
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
