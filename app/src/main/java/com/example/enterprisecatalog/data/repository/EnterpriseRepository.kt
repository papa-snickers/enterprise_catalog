package com.example.enterprisecatalog.data.repository

import com.example.enterprisecatalog.data.api.ApiService
import com.example.enterprisecatalog.data.model.*

open class EnterpriseRepository(private val api: ApiService) {

    open suspend fun getEnterprises(
        query: String? = null,
        category: String? = null
    ): ApiResult<List<Enterprise>> = safeApiCall {
        val response = api.getEnterprises(
            query = query?.takeIf { it.isNotBlank() },
            category = category?.takeIf { it.isNotBlank() }
        )
        if (response.isSuccessful) ApiResult.Success(response.body()!!)
        else ApiResult.Error(parseError(response.errorBody()?.string()))
    }

    open suspend fun getEnterprise(id: String): ApiResult<Enterprise> = safeApiCall {
        val response = api.getEnterprise(id)
        if (response.isSuccessful) ApiResult.Success(response.body()!!)
        else ApiResult.Error(parseError(response.errorBody()?.string()))
    }

    open suspend fun createEnterprise(request: EnterpriseRequest): ApiResult<Enterprise> = safeApiCall {
        val response = api.createEnterprise(request)
        if (response.isSuccessful) ApiResult.Success(response.body()!!)
        else ApiResult.Error(parseError(response.errorBody()?.string()))
    }

    open suspend fun updateEnterprise(id: String, request: EnterpriseRequest): ApiResult<Enterprise> = safeApiCall {
        val response = api.updateEnterprise(id, request)
        if (response.isSuccessful) ApiResult.Success(response.body()!!)
        else ApiResult.Error(parseError(response.errorBody()?.string()))
    }

    open suspend fun deleteEnterprise(id: String): ApiResult<SuccessResponse> = safeApiCall {
        val response = api.deleteEnterprise(id)
        if (response.isSuccessful) ApiResult.Success(response.body()!!)
        else ApiResult.Error(parseError(response.errorBody()?.string()))
    }

    open suspend fun getCategories(): ApiResult<List<String>> = safeApiCall {
        val response = api.getCategories()
        if (response.isSuccessful) ApiResult.Success(response.body()!!)
        else ApiResult.Error(parseError(response.errorBody()?.string()))
    }

    open suspend fun getFavorites(): ApiResult<List<Enterprise>> = safeApiCall {
        val response = api.getFavorites()
        if (response.isSuccessful) ApiResult.Success(response.body()!!)
        else ApiResult.Error(parseError(response.errorBody()?.string()))
    }

    open suspend fun addFavorite(id: String): ApiResult<SuccessResponse> = safeApiCall {
        val response = api.addFavorite(id)
        if (response.isSuccessful) ApiResult.Success(response.body()!!)
        else ApiResult.Error(parseError(response.errorBody()?.string()))
    }

    open suspend fun removeFavorite(id: String): ApiResult<SuccessResponse> = safeApiCall {
        val response = api.removeFavorite(id)
        if (response.isSuccessful) ApiResult.Success(response.body()!!)
        else ApiResult.Error(parseError(response.errorBody()?.string()))
    }
}
