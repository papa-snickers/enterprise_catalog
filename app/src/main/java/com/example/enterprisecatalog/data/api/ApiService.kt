package com.example.enterprisecatalog.data.api

import com.example.enterprisecatalog.data.local.DataStoreManager
import com.example.enterprisecatalog.data.model.*
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.example.enterprisecatalog.BuildConfig
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*

private val BASE_URL = BuildConfig.BASE_URL

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<SuccessResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("enterprises")
    suspend fun getEnterprises(
        @Query("query") query: String? = null,
        @Query("category") category: String? = null
    ): Response<List<Enterprise>>

    @GET("enterprises/{id}")
    suspend fun getEnterprise(@Path("id") id: String): Response<Enterprise>

    @POST("enterprises")
    suspend fun createEnterprise(@Body request: EnterpriseRequest): Response<Enterprise>

    @PUT("enterprises/{id}")
    suspend fun updateEnterprise(
        @Path("id") id: String,
        @Body request: EnterpriseRequest
    ): Response<Enterprise>

    @DELETE("enterprises/{id}")
    suspend fun deleteEnterprise(@Path("id") id: String): Response<SuccessResponse>

    @GET("categories")
    suspend fun getCategories(): Response<List<String>>
}

object ApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun create(dataStoreManager: DataStoreManager): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = okhttp3.Interceptor { chain ->
            val token = runBlocking { dataStoreManager.getToken() }
            val request = if (token != null) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }
}
