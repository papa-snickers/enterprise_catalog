package com.example.server.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val name: String,
    val login: String,
    val email: String,
    val password: String,
    val role: String
)

@Serializable
data class LoginRequest(
    val login: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val role: String,
    val name: String,
    val email: String
)

@Serializable
data class Enterprise(
    val id: String,
    val name: String,
    val specialization: String,
    val description: String,
    val address: String,
    val phone: String,
    val email: String,
    val website: String,
    val createdAt: String
)

@Serializable
data class EnterpriseRequest(
    val name: String,
    val specialization: String,
    val description: String,
    val address: String,
    val phone: String,
    val email: String,
    val website: String
)

@Serializable
data class SuccessResponse(val success: Boolean = true)

@Serializable
data class ErrorResponse(val error: String)
