package com.example.enterprisecatalog.data.repository

import com.example.enterprisecatalog.data.api.ApiService
import com.example.enterprisecatalog.data.local.DataStoreManager
import com.example.enterprisecatalog.data.model.*
import kotlinx.serialization.json.Json
import java.io.IOException

// ── Shared helpers ────────────────────────────────────────────────────────────

suspend fun <T> safeApiCall(call: suspend () -> ApiResult<T>): ApiResult<T> {
    return try {
        call()
    } catch (e: IOException) {
        ApiResult.Error("Нет подключения к серверу")
    } catch (e: Exception) {
        ApiResult.Error("Ошибка: ${e.message}")
    }
}

fun parseError(body: String?): String {
    if (body == null) return "Неизвестная ошибка"
    return try {
        Json.decodeFromString<ErrorResponse>(body).error
    } catch (e: Exception) {
        "Ошибка сервера"
    }
}

// ── Auth Repository ───────────────────────────────────────────────────────────

open class AuthRepository(
    private val api: ApiService,
    private val dataStore: DataStoreManager
) {

    open suspend fun login(login: String, password: String): ApiResult<AuthResponse> {
        return safeApiCall {
            val response = api.login(LoginRequest(login, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                dataStore.saveAuthData(body.token, body.role, body.name, body.email)
                ApiResult.Success(body)
            } else {
                ApiResult.Error(parseError(response.errorBody()?.string()))
            }
        }
    }

    suspend fun register(
        name: String,
        login: String,
        email: String,
        password: String,
        role: String
    ): ApiResult<SuccessResponse> {
        return safeApiCall {
            val response = api.register(RegisterRequest(name, login, email, password, role))
            if (response.isSuccessful) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(parseError(response.errorBody()?.string()))
            }
        }
    }

    suspend fun logout() {
        dataStore.clearAuthData()
    }
}
