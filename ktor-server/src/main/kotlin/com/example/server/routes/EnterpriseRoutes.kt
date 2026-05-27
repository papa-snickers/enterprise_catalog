package com.example.server.routes

import com.example.server.models.*
import com.example.server.plugins.JWT_AUTH
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

fun Route.enterpriseRoutes() {

    // ── Публичные маршруты ──────────────────────────────────────────────────

    get("/enterprises") {
        val queryParam = call.request.queryParameters["query"]
        val category = call.request.queryParameters["category"]

        val enterprises = transaction {
            Enterprises.selectAll()
                .apply {
                    if (!queryParam.isNullOrBlank()) {
                        val q = "%${queryParam.lowercase()}%"
                        andWhere {
                            (Enterprises.name.lowerCase() like q) or
                            (Enterprises.description.lowerCase() like q) or
                            (Enterprises.address.lowerCase() like q)
                        }
                    }
                    if (!category.isNullOrBlank()) {
                        andWhere { Enterprises.specialization eq category }
                    }
                }
                .map { it.toEnterprise() }
        }
        call.respond(enterprises)
    }

    get("/enterprises/{id}") {
        val id = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID обязателен"))

        val enterprise = transaction {
            Enterprises.selectAll().where { Enterprises.id eq id }.firstOrNull()?.toEnterprise()
        }

        if (enterprise == null) {
            call.respond(HttpStatusCode.NotFound, ErrorResponse("Предприятие не найдено"))
        } else {
            call.respond(enterprise)
        }
    }

    get("/categories") {
        val categories = transaction {
            Enterprises.select(Enterprises.specialization)
                .map { it[Enterprises.specialization] }
                .distinct()
                .sorted()
        }
        call.respond(categories)
    }

    // ── Защищённые маршруты (только ADMIN) ─────────────────────────────────

    authenticate(JWT_AUTH) {
        post("/enterprises") {
            call.requireAdmin() ?: return@post

            val request = try {
                call.receive<EnterpriseRequest>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный формат запроса"))
                return@post
            }

            if (request.name.isBlank() || request.specialization.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Название и специализация обязательны"))
                return@post
            }

            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val newId = UUID.randomUUID().toString()

            val enterprise = transaction {
                Enterprises.insert {
                    it[id] = newId
                    it[name] = request.name
                    it[specialization] = request.specialization
                    it[description] = request.description
                    it[address] = request.address
                    it[phone] = request.phone
                    it[email] = request.email
                    it[website] = request.website
                    it[createdAt] = now
                }
                Enterprises.selectAll().where { Enterprises.id eq newId }.first().toEnterprise()
            }
            call.respond(HttpStatusCode.Created, enterprise)
        }

        put("/enterprises/{id}") {
            call.requireAdmin() ?: return@put

            val id = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID обязателен"))

            val request = try {
                call.receive<EnterpriseRequest>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный формат запроса"))
                return@put
            }

            val updated = transaction {
                val count = Enterprises.update({ Enterprises.id eq id }) {
                    it[name] = request.name
                    it[specialization] = request.specialization
                    it[description] = request.description
                    it[address] = request.address
                    it[phone] = request.phone
                    it[email] = request.email
                    it[website] = request.website
                }
                if (count > 0) {
                    Enterprises.selectAll().where { Enterprises.id eq id }.firstOrNull()?.toEnterprise()
                } else null
            }

            if (updated == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Предприятие не найдено"))
            } else {
                call.respond(updated)
            }
        }

        delete("/enterprises/{id}") {
            call.requireAdmin() ?: return@delete

            val id = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID обязателен"))

            val deleted = transaction {
                Enterprises.deleteWhere { Enterprises.id eq id } > 0
            }

            if (deleted) {
                call.respond(SuccessResponse())
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Предприятие не найдено"))
            }
        }
    }
}

// ── Вспомогательные функции ─────────────────────────────────────────────────

private suspend fun ApplicationCall.requireAdmin(): Unit? {
    val principal = principal<JWTPrincipal>()
    val role = principal?.payload?.getClaim("role")?.asString()
    return if (role == "ADMIN") Unit
    else {
        respond(HttpStatusCode.Forbidden, ErrorResponse("Доступ запрещён"))
        null
    }
}

private fun ResultRow.toEnterprise() = Enterprise(
    id = this[Enterprises.id],
    name = this[Enterprises.name],
    specialization = this[Enterprises.specialization],
    description = this[Enterprises.description],
    address = this[Enterprises.address],
    phone = this[Enterprises.phone],
    email = this[Enterprises.email],
    website = this[Enterprises.website],
    createdAt = this[Enterprises.createdAt]
)
