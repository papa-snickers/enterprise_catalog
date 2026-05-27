package com.example.server.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.Date

const val JWT_SECRET = "enterprise_catalog_secret_key_32ch"
private const val JWT_ISSUER = "enterprise-catalog"
private const val JWT_AUDIENCE = "enterprise-catalog-users"
const val JWT_AUTH = "jwt-auth"

fun Application.configureAuthentication() {
    install(Authentication) {
        jwt(JWT_AUTH) {
            realm = "Enterprise Catalog"
            verifier(
                JWT.require(Algorithm.HMAC256(JWT_SECRET))
                    .withIssuer(JWT_ISSUER)
                    .withAudience(JWT_AUDIENCE)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}

fun generateToken(userId: String, login: String, role: String): String {
    return JWT.create()
        .withIssuer(JWT_ISSUER)
        .withAudience(JWT_AUDIENCE)
        .withClaim("userId", userId)
        .withClaim("login", login)
        .withClaim("role", role)
        .withExpiresAt(Date(System.currentTimeMillis() + 86_400_000L))
        .sign(Algorithm.HMAC256(JWT_SECRET))
}
