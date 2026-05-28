package com.example.enterprisecatalog.data.model

import org.junit.Assert.*
import org.junit.Test

class EnterpriseModelTest {

    private val enterprise = Enterprise(
        id = "1",
        name = "ПАО «Газпром»",
        specialization = "Производство",
        description = "Газодобывающая компания",
        address = "г. Москва, ул. Наметкина, д. 16",
        phone = "+7 (495) 719-30-01",
        email = "gazprom@gazprom.ru",
        website = "gazprom.ru",
        createdAt = "2024-01-01T00:00:00"
    )

    @Test
    fun `Enterprise fields are set correctly`() {
        assertEquals("1", enterprise.id)
        assertEquals("ПАО «Газпром»", enterprise.name)
        assertEquals("Производство", enterprise.specialization)
    }

    @Test
    fun `Enterprise copy changes only specified field`() {
        val updated = enterprise.copy(name = "ПАО «Сбербанк»")
        assertEquals("ПАО «Сбербанк»", updated.name)
        assertEquals(enterprise.id, updated.id)
        assertEquals(enterprise.specialization, updated.specialization)
    }

    @Test
    fun `Two enterprises with same data are equal`() {
        val copy = enterprise.copy()
        assertEquals(enterprise, copy)
    }

    @Test
    fun `EnterpriseRequest contains all required fields`() {
        val request = EnterpriseRequest(
            name = "Тест",
            specialization = "IT",
            description = "Описание",
            address = "Москва",
            phone = "+7 999 000 00 00",
            email = "test@test.ru",
            website = "test.ru"
        )
        assertEquals("Тест", request.name)
        assertEquals("IT", request.specialization)
    }
}
