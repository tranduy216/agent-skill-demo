package com.demo.routes

import com.demo.plugins.UserSession
import com.demo.services.UserService
import com.demo.templates.layout
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*

fun Route.authRoutes() {
    get("/login") {
        val session = call.sessions.get<UserSession>()
        if (session != null) {
            call.respondRedirect("/")
            return@get
        }
        call.respondHtml {
            layout(null, "Login") {
                div("login-container") {
                    div("card login-card") {
                        h2("text-center") { +"🤖 AI Assistant Login" }
                        div { id = "login-error" }
                        form {
                            method = FormMethod.post
                            action = "/login"
                            div("form-group") {
                                label { htmlFor = "username"; +"Username" }
                                input(type = InputType.text, name = "username") {
                                    id = "username"
                                    required = true
                                    placeholder = "Enter username"
                                }
                            }
                            div("form-group") {
                                label { htmlFor = "password"; +"Password" }
                                input(type = InputType.password, name = "password") {
                                    id = "password"
                                    required = true
                                    placeholder = "Enter password"
                                }
                            }
                            button(type = ButtonType.submit, classes = "btn btn-primary") {
                                style = "width: 100%"
                                +"Login"
                            }
                        }
                        p("text-center text-secondary mt-1") {
                            style = "font-size: 0.8rem;"
                            +"Default accounts: admin/admin123 (Admin) or user/user123 (User)"
                        }
                    }
                }
            }
        }
    }

    post("/login") {
        val params = call.receiveParameters()
        val username = params["username"] ?: ""
        val password = params["password"] ?: ""

        val user = UserService.authenticate(username, password)
        if (user != null) {
            call.sessions.set(UserSession(user.id, user.username, user.role))
            call.respondRedirect("/")
        } else {
            call.respondHtml(HttpStatusCode.Unauthorized) {
                layout(null, "Login") {
                    div("login-container") {
                        div("card login-card") {
                            h2("text-center") { +"🤖 AI Assistant Login" }
                            div("alert alert-error") { +"Invalid username or password" }
                            form {
                                method = FormMethod.post
                                action = "/login"
                                div("form-group") {
                                    label { htmlFor = "username"; +"Username" }
                                    input(type = InputType.text, name = "username") {
                                        id = "username"
                                        required = true
                                        value = username
                                    }
                                }
                                div("form-group") {
                                    label { htmlFor = "password"; +"Password" }
                                    input(type = InputType.password, name = "password") {
                                        id = "password"
                                        required = true
                                    }
                                }
                                button(type = ButtonType.submit, classes = "btn btn-primary") {
                                    style = "width: 100%"
                                    +"Login"
                                }
                            }
                            p("text-center text-secondary mt-1") {
                                style = "font-size: 0.8rem;"
                                +"Default accounts: admin/admin123 (Admin) or user/user123 (User)"
                            }
                        }
                    }
                }
            }
        }
    }

    get("/logout") {
        call.sessions.clear<UserSession>()
        call.respondRedirect("/login")
    }
}
