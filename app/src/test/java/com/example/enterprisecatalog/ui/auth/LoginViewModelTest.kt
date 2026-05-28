package com.example.enterprisecatalog.ui.auth

import com.example.enterprisecatalog.data.model.ApiResult
import com.example.enterprisecatalog.data.model.AuthResponse
import com.example.enterprisecatalog.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository: AuthRepository = mock()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = LoginViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNull(state.successRole)
    }

    @Test
    fun `login success sets successRole`() = runTest {
        val response = AuthResponse(token = "token", role = "ADMIN", name = "Администратор", email = "admin@test.ru")
        whenever(repository.login("admin", "admin123")).thenReturn(ApiResult.Success(response))

        viewModel.login("admin", "admin123")

        assertEquals("ADMIN", viewModel.uiState.value.successRole)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `login failure sets error`() = runTest {
        whenever(repository.login("wrong", "wrong")).thenReturn(ApiResult.Error("Неверный логин или пароль"))

        viewModel.login("wrong", "wrong")

        assertNotNull(viewModel.uiState.value.error)
        assertEquals("Неверный логин или пароль", viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.successRole)
    }

    @Test
    fun `clearError removes error message`() = runTest {
        whenever(repository.login(any(), any())).thenReturn(ApiResult.Error("Ошибка"))
        viewModel.login("x", "x")

        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `login sets USER role for regular user`() = runTest {
        val response = AuthResponse(token = "token2", role = "USER", name = "Иван", email = "user@test.ru")
        whenever(repository.login("user", "user123")).thenReturn(ApiResult.Success(response))

        viewModel.login("user", "user123")

        assertEquals("USER", viewModel.uiState.value.successRole)
    }
}
