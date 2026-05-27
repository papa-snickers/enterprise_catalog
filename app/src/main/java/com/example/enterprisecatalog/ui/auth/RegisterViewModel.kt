package com.example.enterprisecatalog.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisecatalog.data.model.ApiResult
import com.example.enterprisecatalog.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    // Field errors
    val nameError: String? = null,
    val loginError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun register(
        name: String,
        login: String,
        email: String,
        password: String,
        confirmPassword: String,
        role: String
    ) {
        // Validate
        val nameErr = if (name.isBlank()) "Имя не может быть пустым" else null
        val loginErr = if (login.length < 3) "Логин должен содержать минимум 3 символа" else null
        val emailErr = if (!email.contains('@') || !email.contains('.')) "Некорректный email" else null
        val passErr = if (password.length < 6) "Пароль должен содержать минимум 6 символов" else null
        val confirmErr = if (password != confirmPassword) "Пароли не совпадают" else null

        if (listOf(nameErr, loginErr, emailErr, passErr, confirmErr).any { it != null }) {
            _uiState.value = RegisterUiState(
                nameError = nameErr,
                loginError = loginErr,
                emailError = emailErr,
                passwordError = passErr,
                confirmPasswordError = confirmErr
            )
            return
        }

        _uiState.value = RegisterUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = repository.register(name, login, email, password, role)) {
                is ApiResult.Success -> {
                    _uiState.value = RegisterUiState(isSuccess = true)
                }
                is ApiResult.Error -> {
                    _uiState.value = RegisterUiState(error = result.message)
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
