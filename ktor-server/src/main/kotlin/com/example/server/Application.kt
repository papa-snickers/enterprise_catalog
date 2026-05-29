package com.example.server

import com.example.server.plugins.*
import com.example.server.routes.authRoutes
import com.example.server.routes.enterpriseRoutes
import com.example.server.routes.favoriteRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    configureSerialization()
    configureCors()
    configureAuthentication()
    configureStatusPages()
    install(CallLogging)
    routing {
        authRoutes()
        enterpriseRoutes()
        favoriteRoutes()
    }
}
