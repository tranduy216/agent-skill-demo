package com.demo.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.html.*
import com.demo.templates.errorPage

fun Application.configureHTTP() {
    install(StatusPages) {
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respondHtml(HttpStatusCode.NotFound) {
                errorPage("404 - Page Not Found", "The page you are looking for does not exist.")
            }
        }
        exception<Throwable> { call, cause ->
            call.respondHtml(HttpStatusCode.InternalServerError) {
                errorPage("500 - Server Error", cause.message ?: "An unexpected error occurred.")
            }
        }
    }

    routing {
        staticResources("/static", "static")
    }
}
