package com.example.enterprisecatalog.data.model

import kotlinx.serialization.Serializable

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
