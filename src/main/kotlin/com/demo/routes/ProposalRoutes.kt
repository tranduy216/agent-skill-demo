package com.demo.routes

import com.demo.models.ProposalStatus
import com.demo.plugins.UserSession
import com.demo.plugins.isAdmin
import com.demo.services.ProposalService
import com.demo.templates.layout
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*

fun Route.proposalRoutes() {
    route("/proposals") {
        get {
            val session = call.sessions.get<UserSession>()!!
            val proposals = ProposalService.getAll()

            call.respondHtml {
                layout(session, "AI Proposals") {
                    h1 { style = "margin: 1.5rem 0 1rem"; +"AI Proposals" }

                    if (proposals.isEmpty()) {
                        div("card text-center") {
                            p("text-secondary") { +"No proposals yet. Go to Q&A to ask the AI something!" }
                            a(href = "/qa", classes = "btn btn-primary mt-1") { +"Go to Q&A" }
                        }
                    } else {
                        div {
                            id = "proposals-list"
                            proposals.forEach { proposal ->
                                div("card") {
                                    id = "proposal-${proposal.id}"
                                    div("flex justify-between items-center") {
                                        h2 { +"Proposal #${proposal.id}" }
                                        span("badge badge-${proposal.status.name.lowercase()}") {
                                            +proposal.status.name
                                        }
                                    }

                                    p {
                                        strong { +"Query: " }
                                        +proposal.query
                                    }
                                    p("text-secondary") {
                                        +"By: ${proposal.username} | Model: ${proposal.modelName} | ${proposal.createdAt.toString().take(16)}"
                                    }

                                    if (proposal.proposedSteps.isNotBlank()) {
                                        h3 { +"Proposed Steps:" }
                                        ol("steps-list") {
                                            proposal.proposedSteps.lines()
                                                .filter { it.isNotBlank() }
                                                .forEach { step ->
                                                    li { +step.replace(Regex("^\\d+\\.\\s*"), "") }
                                                }
                                        }
                                    }

                                    if (proposal.aiResponse.isNotBlank()) {
                                        details {
                                            summary { +"Full AI Response" }
                                            pre { +proposal.aiResponse }
                                        }
                                    }

                                    if (proposal.reviewerName != null) {
                                        p("text-secondary") { +"Reviewed by: ${proposal.reviewerName}" }
                                    }

                                    if (session.isAdmin() && proposal.status == ProposalStatus.PENDING) {
                                        div("flex gap-1 mt-1") {
                                            button(classes = "btn btn-success") {
                                                attributes["hx-post"] = "/proposals/${proposal.id}/approve"
                                                attributes["hx-target"] = "#proposal-${proposal.id}"
                                                attributes["hx-swap"] = "outerHTML"
                                                +"✓ Approve"
                                            }
                                            button(classes = "btn btn-danger") {
                                                attributes["hx-post"] = "/proposals/${proposal.id}/reject"
                                                attributes["hx-target"] = "#proposal-${proposal.id}"
                                                attributes["hx-swap"] = "outerHTML"
                                                +"✗ Reject"
                                            }
                                        }
                                    }

                                    if (session.isAdmin() && proposal.status == ProposalStatus.APPROVED) {
                                        div("mt-1") {
                                            button(classes = "btn btn-warning") {
                                                attributes["hx-post"] = "/proposals/${proposal.id}/execute"
                                                attributes["hx-target"] = "#proposal-${proposal.id}"
                                                attributes["hx-swap"] = "outerHTML"
                                                attributes["hx-confirm"] = "Execute this proposal? This action will be logged."
                                                +"▶ Execute"
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

        post("/{id}/approve") {
            val session = call.sessions.get<UserSession>()!!
            if (!session.isAdmin()) {
                call.respond(HttpStatusCode.Forbidden)
                return@post
            }
            val id = call.parameters["id"]?.toLongOrNull() ?: return@post
            ProposalService.approve(id, session.userId)
            val proposal = ProposalService.getById(id) ?: return@post

            call.respondHtml(HttpStatusCode.OK) {
                body {
                    proposalCard(proposal, session)
                }
            }
        }

        post("/{id}/reject") {
            val session = call.sessions.get<UserSession>()!!
            if (!session.isAdmin()) {
                call.respond(HttpStatusCode.Forbidden)
                return@post
            }
            val id = call.parameters["id"]?.toLongOrNull() ?: return@post
            ProposalService.reject(id, session.userId)
            val proposal = ProposalService.getById(id) ?: return@post

            call.respondHtml(HttpStatusCode.OK) {
                body {
                    proposalCard(proposal, session)
                }
            }
        }

        post("/{id}/execute") {
            val session = call.sessions.get<UserSession>()!!
            if (!session.isAdmin()) {
                call.respond(HttpStatusCode.Forbidden)
                return@post
            }
            val id = call.parameters["id"]?.toLongOrNull() ?: return@post
            ProposalService.execute(id, session.userId)
            val proposal = ProposalService.getById(id) ?: return@post

            call.respondHtml(HttpStatusCode.OK) {
                body {
                    proposalCard(proposal, session)
                }
            }
        }
    }
}

private fun FlowContent.proposalCard(proposal: com.demo.models.AiProposalDTO, session: UserSession) {
    div("card") {
        id = "proposal-${proposal.id}"
        div("flex justify-between items-center") {
            h2 { +"Proposal #${proposal.id}" }
            span("badge badge-${proposal.status.name.lowercase()}") {
                +proposal.status.name
            }
        }

        p {
            strong { +"Query: " }
            +proposal.query
        }
        p("text-secondary") {
            +"By: ${proposal.username} | Model: ${proposal.modelName} | ${proposal.createdAt.toString().take(16)}"
        }

        if (proposal.proposedSteps.isNotBlank()) {
            h3 { +"Proposed Steps:" }
            ol("steps-list") {
                proposal.proposedSteps.lines()
                    .filter { it.isNotBlank() }
                    .forEach { step ->
                        li { +step.replace(Regex("^\\d+\\.\\s*"), "") }
                    }
            }
        }

        if (proposal.aiResponse.isNotBlank()) {
            details {
                summary { +"Full AI Response" }
                pre { +proposal.aiResponse }
            }
        }

        if (proposal.reviewerName != null) {
            p("text-secondary") { +"Reviewed by: ${proposal.reviewerName}" }
        }

        if (session.isAdmin() && proposal.status == ProposalStatus.PENDING) {
            div("flex gap-1 mt-1") {
                button(classes = "btn btn-success") {
                    attributes["hx-post"] = "/proposals/${proposal.id}/approve"
                    attributes["hx-target"] = "#proposal-${proposal.id}"
                    attributes["hx-swap"] = "outerHTML"
                    +"✓ Approve"
                }
                button(classes = "btn btn-danger") {
                    attributes["hx-post"] = "/proposals/${proposal.id}/reject"
                    attributes["hx-target"] = "#proposal-${proposal.id}"
                    attributes["hx-swap"] = "outerHTML"
                    +"✗ Reject"
                }
            }
        }

        if (session.isAdmin() && proposal.status == ProposalStatus.APPROVED) {
            div("mt-1") {
                button(classes = "btn btn-warning") {
                    attributes["hx-post"] = "/proposals/${proposal.id}/execute"
                    attributes["hx-target"] = "#proposal-${proposal.id}"
                    attributes["hx-swap"] = "outerHTML"
                    attributes["hx-confirm"] = "Execute this proposal? This action will be logged."
                    +"▶ Execute"
                }
            }
        }
    }
}
