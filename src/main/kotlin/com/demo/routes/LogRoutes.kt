package com.demo.routes

import com.demo.plugins.UserSession
import com.demo.services.LogService
import com.demo.templates.layout
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*

fun Route.logRoutes() {
    get("/logs") {
        val session = call.sessions.get<UserSession>()!!
        val logs = LogService.getAll()

        call.respondHtml {
            layout(session, "Execution Logs") {
                h1 { style = "margin: 1.5rem 0 1rem"; +"Execution Logs" }

                div("card") {
                    if (logs.isEmpty()) {
                        p("text-secondary text-center") { +"No execution logs yet." }
                    } else {
                        table {
                            thead {
                                tr {
                                    th { +"ID" }
                                    th { +"Proposal" }
                                    th { +"Executed By" }
                                    th { +"Action" }
                                    th { +"Result" }
                                    th { +"Status" }
                                    th { +"Date" }
                                }
                            }
                            tbody {
                                logs.forEach { log ->
                                    tr {
                                        td { +"#${log.id}" }
                                        td {
                                            a(href = "/proposals") {
                                                +"#${log.proposalId}"
                                            }
                                            br
                                            span("text-secondary") {
                                                style = "font-size:0.75rem"
                                                +log.proposalQuery.take(50)
                                            }
                                        }
                                        td { +log.executorName }
                                        td("truncate") { +log.action }
                                        td {
                                            details {
                                                summary { +"View Result" }
                                                pre { +log.result }
                                            }
                                        }
                                        td {
                                            if (log.success) {
                                                span("badge badge-approved") { +"Success" }
                                            } else {
                                                span("badge badge-rejected") { +"Failed" }
                                            }
                                        }
                                        td { +log.createdAt.toString().take(16) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
