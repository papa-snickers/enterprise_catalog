package com.example.enterprisecatalog.data.model

import org.junit.Assert.*
import org.junit.Test

class ApiResultTest {

    @Test
    fun `Success wraps data correctly`() {
        val result = ApiResult.Success("hello")
        assertEquals("hello", result.data)
    }

    @Test
    fun `Error wraps message correctly`() {
        val result = ApiResult.Error("Нет подключения")
        assertEquals("Нет подключения", result.message)
    }

    @Test
    fun `Loading is singleton`() {
        assertSame(ApiResult.Loading, ApiResult.Loading)
    }

    @Test
    fun `Success is not Error`() {
        val result: ApiResult<Int> = ApiResult.Success(1)
        assertFalse(result is ApiResult.Error)
    }

    @Test
    fun `Success with list data`() {
        val list = listOf("a", "b", "c")
        val result = ApiResult.Success(list)
        assertEquals(3, result.data.size)
        assertEquals("b", result.data[1])
    }
}
