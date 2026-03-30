package com.demo.plugins

import com.demo.models.UserRole
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

data class UserSession(
    val userId: Long,
    val username: String,
    val role: UserRole
) : Principal

fun Application.configureSecurity() {
    install(Sessions) {
        cookie<UserSession>("USER_SESSION") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 3600
        }
    }

    install(Authentication) {
        session<UserSession>("auth-session") {
            validate { session -> session }
            challenge { call.respondRedirect("/login") }
        }
    }
}

fun UserSession.isAdmin() = role == UserRole.ADMIN
