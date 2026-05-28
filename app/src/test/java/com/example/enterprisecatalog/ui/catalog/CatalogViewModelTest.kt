package com.example.enterprisecatalog.ui.catalog

import androidx.lifecycle.SavedStateHandle
import com.example.enterprisecatalog.data.model.ApiResult
import com.example.enterprisecatalog.data.model.Enterprise
import com.example.enterprisecatalog.data.repository.EnterpriseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository: EnterpriseRepository = mock()
    private lateinit var viewModel: CatalogViewModel

    private val enterprises = listOf(
        Enterprise("1", "ПАО «Газпром»", "Производство", "Описание", "Москва", "+7", "g@g.ru", "gazprom.ru", "2024-01-01"),
        Enterprise("2", "ООО «Яндекс»", "IT", "Описание", "Москва", "+7", "y@y.ru", "yandex.ru", "2024-01-01"),
        Enterprise("3", "Государственный Эрмитаж", "Культура", "Описание", "СПб", "+7", "h@h.ru", "hermitage.ru", "2024-01-01")
    )

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(dispatcher)
        whenever(repository.getEnterprises(anyOrNull(), anyOrNull())).thenReturn(ApiResult.Success(enterprises))
        whenever(repository.getCategories()).thenReturn(ApiResult.Success(listOf("Производство", "IT", "Культура")))
        viewModel = CatalogViewModel(repository, SavedStateHandle())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load fills enterprises list`() {
        assertEquals(3, viewModel.uiState.value.enterprises.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `initial load fills categories`() {
        assertEquals(3, viewModel.uiState.value.categories.size)
    }

    @Test
    fun `selectEnterprise updates selectedEnterprise`() {
        viewModel.selectEnterprise(enterprises[0])
        assertEquals(enterprises[0], viewModel.uiState.value.selectedEnterprise)
    }

    @Test
    fun `selectEnterprise with null clears selection`() {
        viewModel.selectEnterprise(enterprises[0])
        viewModel.selectEnterprise(null)
        assertNull(viewModel.uiState.value.selectedEnterprise)
    }

    @Test
    fun `onCategorySelected sets selectedCategory`() {
        viewModel.onCategorySelected("IT")
        assertEquals("IT", viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `onCategorySelected with null clears category`() {
        viewModel.onCategorySelected("IT")
        viewModel.onCategorySelected(null)
        assertNull(viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `initial searchQuery is empty`() {
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `error state shows when repository returns error`() = runTest {
        whenever(repository.getEnterprises(anyOrNull(), anyOrNull())).thenReturn(ApiResult.Error("Нет сети"))
        viewModel.loadEnterprises()
        assertEquals("Нет сети", viewModel.uiState.value.error)
    }
}
