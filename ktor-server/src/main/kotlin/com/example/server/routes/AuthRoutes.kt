package com.example.server.routes

import com.example.server.models.*
import com.example.server.plugins.generateToken
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

fun Route.authRoutes() {
    route("/auth") {
        post("/register") {
            val request = try {
                call.receive<RegisterRequest>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный формат запроса"))
                return@post
            }

            if (request.name.isBlank() || request.login.isBlank() ||
                request.email.isBlank() || request.password.isBlank()
            ) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Все поля обязательны"))
                return@post
            }

            val role = if (request.role.uppercase() == "ADMIN") "ADMIN" else "USER"

            try {
                val existing = transaction {
                    Users.selectAll()
                        .where { (Users.login eq request.login) or (Users.email eq request.email) }
                        .firstOrNull()
                }

                if (existing != null) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        ErrorResponse("Пользователь с таким логином или email уже существует")
                    )
                    return@post
                }

                transaction {
                    Users.insert {
                        it[id] = UUID.randomUUID().toString()
                        it[name] = request.name
                        it[login] = request.login
                        it[email] = request.email
                        it[passwordHash] = BCrypt.hashpw(request.password, BCrypt.gensalt())
                        it[Users.role] = role
                    }
                }

                call.respond(HttpStatusCode.Created, SuccessResponse())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Ошибка регистрации"))
            }
        }

        post("/login") {
            val request = try {
                call.receive<LoginRequest>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректный формат запроса"))
                return@post
            }

            val user = transaction {
                Users.selectAll().where { Users.login eq request.login }.firstOrNull()
            }

            if (user == null || !BCrypt.checkpw(request.password, user[Users.passwordHash])) {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Неверный логин или пароль"))
                return@post
            }

            val token = generateToken(user[Users.id], user[Users.login], user[Users.role])
            call.respond(
                AuthResponse(
                    token = token,
                    role = user[Users.role],
                    name = user[Users.name],
                    email = user[Users.email]
                )
            )
        }
    }
}
