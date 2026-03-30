package com.demo.routes

import com.demo.plugins.UserSession
import com.demo.services.AiService
import com.demo.services.ProposalService
import com.demo.services.RagService
import com.demo.templates.layout
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*

fun Route.qaRoutes() {
    route("/qa") {
        get {
            val session = call.sessions.get<UserSession>()!!
            call.respondHtml {
                layout(session, "Q&A with AI") {
                    h1 { style = "margin: 1.5rem 0 1rem"; +"Q&A with AI" }

                    div("card") {
                        h2 { +"Ask a Question" }
                        form {
                            attributes["hx-post"] = "/qa/ask"
                            attributes["hx-target"] = "#qa-response"
                            attributes["hx-swap"] = "innerHTML"
                            attributes["hx-indicator"] = "#qa-spinner"

                            div("form-row") {
                                div("form-group") {
                                    label { +"LLM Model" }
                                    input(type = InputType.text, name = "modelName") {
                                        placeholder = "gpt-3.5-turbo"
                                        value = "gpt-3.5-turbo"
                                    }
                                }
                                div("form-group") {
                                    label { +"API Key" }
                                    input(type = InputType.password, name = "apiKey") {
                                        placeholder = "sk-... (leave empty for demo mode)"
                                    }
                                }
                            }
                            div("form-group") {
                                label { +"Your Question" }
                                textArea {
                                    name = "query"
                                    required = true
                                    placeholder = "Ask the AI to do something... e.g., 'Create a deployment plan for a microservice'"
                                    rows = "3"
                                }
                            }
                            div("flex gap-1 items-center") {
                                button(type = ButtonType.submit, classes = "btn btn-primary") {
                                    +"Ask AI"
                                }
                                span("htmx-indicator") {
                                    id = "qa-spinner"
                                    span("spinner") {}
                                    +" Thinking..."
                                }
                            }
                        }
                    }

                    div {
                        id = "qa-response"
                    }
                }
            }
        }

        post("/ask") {
            val session = call.sessions.get<UserSession>()!!
            val params = call.receiveParameters()
            val query = params["query"] ?: ""
            val modelName = params["modelName"]?.ifBlank { "gpt-3.5-turbo" } ?: "gpt-3.5-turbo"
            val apiKey = params["apiKey"] ?: ""

            // Get relevant RAG context
            val ragDocs = RagService.getAll()

            // Call AI service
            val aiResponse = AiService.askLlm(query, modelName, apiKey, ragDocs)
            val steps = AiService.extractSteps(aiResponse)
            val stepsText = steps.joinToString("\n")

            // Create proposal
            val proposal = ProposalService.create(
                userId = session.userId,
                query = query,
                modelName = modelName,
                proposedSteps = stepsText,
                aiResponse = aiResponse
            )

            call.respondHtml(HttpStatusCode.OK) {
                body {
                    div("card") {
                        h2 { +"AI Response" }
                        div("chat-container") {
                            div("chat-message chat-user") {
                                p { +query }
                            }
                            div("chat-message chat-ai") {
                                pre { +aiResponse }
                            }
                        }
                    }

                    div("card") {
                        h2 { +"Proposal #${proposal.id} Created" }
                        div("alert alert-info") {
                            +"This proposal requires admin approval before execution. "
                            a(href = "/proposals") { +"View Proposals →" }
                        }
                        if (steps.isNotEmpty()) {
                            h3 { +"Extracted Steps:" }
                            ol("steps-list") {
                                steps.forEach { step ->
                                    li { +step.replace(Regex("^\\d+\\.\\s*"), "") }
                                }
                            }
                        }
                        p("text-secondary") {
                            +"Status: "
                            span("badge badge-pending") { +proposal.status.name }
                            +" | Model: ${proposal.modelName}"
                        }
                    }
                }
            }
        }
    }
}
