package com.example.enterprisecatalog.data.repository

import com.example.enterprisecatalog.data.api.ApiService
import com.example.enterprisecatalog.data.model.*

class EnterpriseRepository(private val api: ApiService) {

    suspend fun getEnterprises(
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

    suspend fun getEnterprise(id: String): ApiResult<Enterprise> = safeApiCall {
        val response = api.getEnterprise(id)
        if (response.isSuccessful) ApiResult.Success(response.body()!!)
        else ApiResult.Error(parseError(response.errorBody()?.string()))
    }

    suspend fun createEnterprise(request: EnterpriseRequest): ApiResult<Enterprise> = safeApiCall {
        val response = api.createEnterprise(request)
        if (response.isSuccessful) ApiResult.Success(response.body()!!)
        else ApiResult.Error(parseError(response.errorBody()?.string()))
    }

    suspend fun updateEnterprise(id: String, request: EnterpriseRequest): ApiResult<Enterprise> = safeApiCall {
        val response = api.updateEnterprise(id, request)
        if (response.isSuccessful) ApiResult.Success(response.body()!!)
        else ApiResult.Error(parseError(response.errorBody()?.string()))
    }

    suspend fun deleteEnterprise(id: String): ApiResult<SuccessResponse> = safeApiCall {
        val response = api.deleteEnterprise(id)
        if (response.isSuccessful) ApiResult.Success(response.body()!!)
        else ApiResult.Error(parseError(response.errorBody()?.string()))
    }

    suspend fun getCategories(): ApiResult<List<String>> = safeApiCall {
        val response = api.getCategories()
        if (response.isSuccessful) ApiResult.Success(response.body()!!)
        else ApiResult.Error(parseError(response.errorBody()?.string()))
    }
}
