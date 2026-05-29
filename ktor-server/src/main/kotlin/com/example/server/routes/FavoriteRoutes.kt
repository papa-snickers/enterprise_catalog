package com.example.server.routes

import com.example.server.models.*
import com.example.server.plugins.JWT_AUTH
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

fun Route.favoriteRoutes() {
    authenticate(JWT_AUTH) {

        get("/favorites") {
            val userId = call.userId() ?: return@get
            val favorites = transaction {
                (Favorites innerJoin Enterprises)
                    .selectAll()
                    .where { Favorites.userId eq userId }
                    .map { it.toFavEnterprise() }
            }
            call.respond(favorites)
        }

        post("/enterprises/{id}/favorite") {
            val userId = call.userId() ?: return@post
            val enterpriseId = call.parameters["id"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID обязателен"))

            val exists = transaction {
                Enterprises.selectAll().where { Enterprises.id eq enterpriseId }.count() > 0
            }
            if (!exists) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Предприятие не найдено"))
                return@post
            }

            val alreadyFav = transaction {
                Favorites.selectAll()
                    .where { (Favorites.userId eq userId) and (Favorites.enterpriseId eq enterpriseId) }
                    .count() > 0
            }
            if (!alreadyFav) {
                transaction {
                    Favorites.insert {
                        it[id] = UUID.randomUUID().toString()
                        it[Favorites.userId] = userId
                        it[Favorites.enterpriseId] = enterpriseId
                    }
                }
            }
            call.respond(HttpStatusCode.Created, SuccessResponse())
        }

        delete("/enterprises/{id}/favorite") {
            val userId = call.userId() ?: return@delete
            val enterpriseId = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("ID обязателен"))

            transaction {
                Favorites.deleteWhere {
                    (Favorites.userId eq userId) and (Favorites.enterpriseId eq enterpriseId)
                }
            }
            call.respond(SuccessResponse())
        }
    }
}

private suspend fun ApplicationCall.userId(): String? {
    val id = principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()
    return if (id != null) id
    else { respond(HttpStatusCode.Unauthorized, ErrorResponse("Не авторизован")); null }
}

private fun ResultRow.toFavEnterprise() = Enterprise(
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
