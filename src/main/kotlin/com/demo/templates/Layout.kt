package com.demo.templates

import com.demo.models.UserRole
import com.demo.plugins.UserSession
import kotlinx.html.*

fun HTML.layout(session: UserSession?, title: String = "AI Assistant", content: FlowContent.() -> Unit) {
    head {
        meta { charset = "utf-8" }
        meta { name = "viewport"; this.content = "width=device-width, initial-scale=1" }
        title { +title }
        link(rel = "stylesheet", href = "/static/css/style.css")
        script(src = "/static/js/htmx.min.js") {}
    }
    body {
        attributes["hx-boost"] = "false"

        if (session != null) {
            nav {
                div("container") {
                    a(href = "/", classes = "brand") { +"🤖 AI Assistant" }
                    div("nav-links") {
                        a(href = "/") { +"Dashboard" }
                        if (session.role == UserRole.ADMIN) {
                            a(href = "/rag") { +"RAG Documents" }
                        }
                        a(href = "/qa") { +"Q&A" }
                        a(href = "/proposals") { +"Proposals" }
                        a(href = "/logs") { +"Logs" }
                        span("role-badge") { +session.role.name }
                        a(href = "/logout") { +"Logout (${session.username})" }
                    }
                }
            }
        }

        main {
            div("container") {
                content()
            }
        }
    }
}

fun HTML.errorPage(title: String, message: String) {
    layout(null, title) {
        div("card text-center") {
            style = "margin-top: 3rem;"
            h2 { +title }
            p("text-secondary") { +message }
            br
            a(href = "/", classes = "btn btn-primary") { +"Go Home" }
        }
    }
}
