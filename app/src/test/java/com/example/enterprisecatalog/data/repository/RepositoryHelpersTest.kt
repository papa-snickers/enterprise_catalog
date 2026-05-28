package com.example.enterprisecatalog.data.repository

import com.example.enterprisecatalog.data.model.ApiResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class RepositoryHelpersTest {

    @Test
    fun `safeApiCall returns Success on normal result`() = runTest {
        val result = safeApiCall { ApiResult.Success(42) }
        assertTrue(result is ApiResult.Success)
        assertEquals(42, (result as ApiResult.Success).data)
    }

    @Test
    fun `safeApiCall returns Error on IOException`() = runTest {
        val result = safeApiCall<Int> { throw IOException("timeout") }
        assertTrue(result is ApiResult.Error)
        assertEquals("Нет подключения к серверу", (result as ApiResult.Error).message)
    }

    @Test
    fun `safeApiCall returns Error on generic exception`() = runTest {
        val result = safeApiCall<Int> { throw RuntimeException("что-то пошло не так") }
        assertTrue(result is ApiResult.Error)
        assertTrue((result as ApiResult.Error).message.startsWith("Ошибка:"))
    }

    @Test
    fun `parseError returns message from valid JSON`() {
        val json = """{"error":"Неверный логин или пароль"}"""
        assertEquals("Неверный логин или пароль", parseError(json))
    }

    @Test
    fun `parseError returns default on null`() {
        assertEquals("Неизвестная ошибка", parseError(null))
    }

    @Test
    fun `parseError returns fallback on invalid JSON`() {
        assertEquals("Ошибка сервера", parseError("not json"))
    }
}
