package com.demo.plugins

import com.demo.routes.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        authRoutes()

        authenticate("auth-session") {
            dashboardRoutes()
            ragRoutes()
            qaRoutes()
            proposalRoutes()
            logRoutes()
        }
    }
}
