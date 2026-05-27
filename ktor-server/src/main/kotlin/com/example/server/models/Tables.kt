package com.example.server.models

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = varchar("id", 36)
    val name = varchar("name", 255)
    val login = varchar("login", 100).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 20)
    override val primaryKey = PrimaryKey(id)
}

object Enterprises : Table("enterprises") {
    val id = varchar("id", 36)
    val name = varchar("name", 255)
    val specialization = varchar("specialization", 100)
    val description = text("description")
    val address = varchar("address", 500)
    val phone = varchar("phone", 50)
    val email = varchar("email", 255)
    val website = varchar("website", 255)
    val createdAt = varchar("created_at", 50)
    override val primaryKey = PrimaryKey(id)
}
