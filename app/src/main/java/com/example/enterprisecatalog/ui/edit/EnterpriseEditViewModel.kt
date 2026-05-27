package com.example.enterprisecatalog.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.enterprisecatalog.data.model.ApiResult
import com.example.enterprisecatalog.data.model.EnterpriseRequest
import com.example.enterprisecatalog.data.repository.EnterpriseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    // Form fields
    val name: String = "",
    val specialization: String = "",
    val description: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    // Validation
    val nameError: String? = null,
    val specializationError: String? = null,
    val addressError: String? = null
)

class EnterpriseEditViewModel(
    private val repository: EnterpriseRepository,
    private val enterpriseId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    init {
        if (enterpriseId != null) loadEnterprise(enterpriseId)
    }

    private fun loadEnterprise(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.getEnterprise(id)) {
                is ApiResult.Success -> {
                    val e = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = e.name,
                            specialization = e.specialization,
                            description = e.description,
                            address = e.address,
                            phone = e.phone,
                            email = e.email,
                            website = e.website
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun updateField(field: String, value: String) {
        _uiState.update {
            when (field) {
                "name" -> it.copy(name = value, nameError = null)
                "specialization" -> it.copy(specialization = value, specializationError = null)
                "description" -> it.copy(description = value)
                "address" -> it.copy(address = value, addressError = null)
                "phone" -> it.copy(phone = value)
                "email" -> it.copy(email = value)
                "website" -> it.copy(website = value)
                else -> it
            }
        }
    }

    fun save() {
        val state = _uiState.value
        val nameErr = if (state.name.isBlank()) "Обязательное поле" else null
        val specErr = if (state.specialization.isBlank()) "Обязательное поле" else null
        val addrErr = if (state.address.isBlank()) "Обязательное поле" else null

        if (nameErr != null || specErr != null || addrErr != null) {
            _uiState.update {
                it.copy(
                    nameError = nameErr,
                    specializationError = specErr,
                    addressError = addrErr
                )
            }
            return
        }

        val request = EnterpriseRequest(
            name = state.name.trim(),
            specialization = state.specialization.trim(),
            description = state.description.trim(),
            address = state.address.trim(),
            phone = state.phone.trim(),
            email = state.email.trim(),
            website = state.website.trim()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = if (enterpriseId != null) {
                repository.updateEnterprise(enterpriseId, request)
            } else {
                repository.createEnterprise(request)
            }
            when (result) {
                is ApiResult.Success -> _uiState.update { it.copy(isSaving = false, isSuccess = true) }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSaving = false, error = result.message)
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
