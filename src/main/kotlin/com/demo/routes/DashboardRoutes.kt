package com.demo.routes

import com.demo.models.UserRole
import com.demo.plugins.UserSession
import com.demo.services.LogService
import com.demo.services.ProposalService
import com.demo.services.RagService
import com.demo.templates.layout
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*

fun Route.dashboardRoutes() {
    get("/") {
        val session = call.sessions.get<UserSession>()!!
        val proposals = ProposalService.getAll()
        val logs = LogService.getAll()
        val ragCount = RagService.getAll().size
        val pendingCount = proposals.count { it.status == com.demo.models.ProposalStatus.PENDING }

        call.respondHtml {
            layout(session, "Dashboard") {
                h1 { style = "margin: 1.5rem 0 1rem"; +"Dashboard" }

                div {
                    style = "display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem;"

                    div("card text-center") {
                        h3 { +"$ragCount" }
                        p("text-secondary") { +"RAG Documents" }
                    }
                    div("card text-center") {
                        h3 { +"${proposals.size}" }
                        p("text-secondary") { +"Total Proposals" }
                    }
                    div("card text-center") {
                        h3 { +"$pendingCount" }
                        p("text-secondary") { +"Pending Approval" }
                    }
                    div("card text-center") {
                        h3 { +"${logs.size}" }
                        p("text-secondary") { +"Execution Logs" }
                    }
                }

                div("card") {
                    h2 { +"Recent Proposals" }
                    if (proposals.isEmpty()) {
                        p("text-secondary") { +"No proposals yet. Go to Q&A to ask the AI something!" }
                    } else {
                        table {
                            thead {
                                tr {
                                    th { +"ID" }
                                    th { +"User" }
                                    th { +"Query" }
                                    th { +"Status" }
                                    th { +"Created" }
                                }
                            }
                            tbody {
                                proposals.take(5).forEach { proposal ->
                                    tr {
                                        td { +"#${proposal.id}" }
                                        td { +proposal.username }
                                        td("truncate") { +proposal.query }
                                        td {
                                            span("badge badge-${proposal.status.name.lowercase()}") {
                                                +proposal.status.name
                                            }
                                        }
                                        td { +proposal.createdAt.toString().take(16) }
                                    }
                                }
                            }
                        }
                    }
                }

                if (session.role == UserRole.ADMIN) {
                    div("card") {
                        h2 { +"Quick Actions" }
                        div("flex gap-1") {
                            a(href = "/rag", classes = "btn btn-primary") { +"Manage RAG Documents" }
                            a(href = "/proposals", classes = "btn btn-success") { +"Review Proposals" }
                            a(href = "/qa", classes = "btn btn-warning") { +"Ask AI" }
                        }
                    }
                }
            }
        }
    }
}
