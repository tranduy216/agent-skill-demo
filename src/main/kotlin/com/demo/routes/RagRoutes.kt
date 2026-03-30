package com.demo.routes

import com.demo.plugins.UserSession
import com.demo.plugins.isAdmin
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

fun Route.ragRoutes() {
    route("/rag") {
        get {
            val session = call.sessions.get<UserSession>()!!
            if (!session.isAdmin()) {
                call.respondRedirect("/")
                return@get
            }

            val documents = RagService.getAll()
            call.respondHtml {
                layout(session, "RAG Documents") {
                    div("page-header") {
                        h1 { +"RAG Documents" }
                        button(classes = "btn btn-primary") {
                            attributes["hx-get"] = "/rag/form"
                            attributes["hx-target"] = "#rag-form-container"
                            attributes["hx-swap"] = "innerHTML"
                            +"+ Add Document"
                        }
                    }

                    div { id = "rag-form-container" }

                    div("card") {
                        id = "rag-table"
                        if (documents.isEmpty()) {
                            p("text-secondary text-center") { +"No documents yet. Add your first RAG document!" }
                        } else {
                            table {
                                thead {
                                    tr {
                                        th { +"ID" }
                                        th { +"Domain" }
                                        th { +"Text" }
                                        th { +"Vector" }
                                        th { +"Metadata" }
                                        th { +"Updated" }
                                        th { +"Actions" }
                                    }
                                }
                                tbody {
                                    documents.forEach { doc ->
                                        tr {
                                            id = "rag-row-${doc.id}"
                                            td { +"#${doc.id}" }
                                            td { +doc.domain }
                                            td("truncate") { +doc.originalText }
                                            td("truncate") {
                                                style = "max-width:150px"
                                                +doc.vectorValue
                                            }
                                            td("truncate") {
                                                style = "max-width:150px"
                                                +doc.metadata
                                            }
                                            td { +doc.updatedAt.toString().take(16) }
                                            td {
                                                div("flex gap-1") {
                                                    button(classes = "btn btn-primary btn-sm") {
                                                        attributes["hx-get"] = "/rag/${doc.id}/edit"
                                                        attributes["hx-target"] = "#rag-form-container"
                                                        attributes["hx-swap"] = "innerHTML"
                                                        +"Edit"
                                                    }
                                                    button(classes = "btn btn-danger btn-sm") {
                                                        attributes["hx-delete"] = "/rag/${doc.id}"
                                                        attributes["hx-target"] = "#rag-row-${doc.id}"
                                                        attributes["hx-swap"] = "outerHTML"
                                                        attributes["hx-confirm"] = "Delete this document?"
                                                        +"Delete"
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
            }
        }

        // Form for creating new document
        get("/form") {
            val session = call.sessions.get<UserSession>()!!
            if (!session.isAdmin()) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }
            call.respondHtml(HttpStatusCode.OK) {
                body {
                    ragForm(null, null, null, null, null)
                }
            }
        }

        // Form for editing document
        get("/{id}/edit") {
            val session = call.sessions.get<UserSession>()!!
            if (!session.isAdmin()) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }
            val id = call.parameters["id"]?.toLongOrNull() ?: return@get
            val doc = RagService.getById(id) ?: return@get

            call.respondHtml(HttpStatusCode.OK) {
                body {
                    ragForm(doc.id, doc.domain, doc.originalText, doc.vectorValue, doc.metadata)
                }
            }
        }

        // Create
        post {
            val session = call.sessions.get<UserSession>()!!
            if (!session.isAdmin()) {
                call.respond(HttpStatusCode.Forbidden)
                return@post
            }
            val params = call.receiveParameters()
            RagService.create(
                domain = params["domain"] ?: "",
                originalText = params["originalText"] ?: "",
                vectorValue = params["vectorValue"] ?: "[]",
                metadata = params["metadata"] ?: "{}"
            )
            call.response.header("HX-Redirect", "/rag")
            call.respond(HttpStatusCode.OK)
        }

        // Update
        put("/{id}") {
            val session = call.sessions.get<UserSession>()!!
            if (!session.isAdmin()) {
                call.respond(HttpStatusCode.Forbidden)
                return@put
            }
            val id = call.parameters["id"]?.toLongOrNull() ?: return@put
            val params = call.receiveParameters()
            RagService.update(
                id = id,
                domain = params["domain"] ?: "",
                originalText = params["originalText"] ?: "",
                vectorValue = params["vectorValue"] ?: "[]",
                metadata = params["metadata"] ?: "{}"
            )
            call.response.header("HX-Redirect", "/rag")
            call.respond(HttpStatusCode.OK)
        }

        // Delete
        delete("/{id}") {
            val session = call.sessions.get<UserSession>()!!
            if (!session.isAdmin()) {
                call.respond(HttpStatusCode.Forbidden)
                return@delete
            }
            val id = call.parameters["id"]?.toLongOrNull() ?: return@delete
            RagService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}

private fun FlowContent.ragForm(id: Long?, domain: String?, text: String?, vector: String?, metadata: String?) {
    val isEdit = id != null
    div("card") {
        h2 { +(if (isEdit) "Edit Document #$id" else "Add New Document") }
        form {
            if (isEdit) {
                attributes["hx-put"] = "/rag/$id"
            } else {
                attributes["hx-post"] = "/rag"
            }
            attributes["hx-swap"] = "none"

            div("form-row") {
                div("form-group") {
                    label { +"Domain" }
                    input(type = InputType.text, name = "domain") {
                        required = true
                        placeholder = "e.g., finance, technology, health"
                        value = domain ?: ""
                    }
                }
            }
            div("form-group") {
                label { +"Original Text" }
                textArea {
                    name = "originalText"
                    required = true
                    placeholder = "Enter the document text content..."
                    +(text ?: "")
                }
            }
            div("form-row") {
                div("form-group") {
                    label { +"Vector Value (JSON array)" }
                    input(type = InputType.text, name = "vectorValue") {
                        placeholder = "[0.1, 0.2, 0.3, ...]"
                        value = vector ?: "[]"
                    }
                }
                div("form-group") {
                    label { +"Metadata (JSON)" }
                    input(type = InputType.text, name = "metadata") {
                        placeholder = "{\"source\": \"manual\"}"
                        value = metadata ?: "{}"
                    }
                }
            }
            div("flex gap-1") {
                button(type = ButtonType.submit, classes = "btn btn-primary") {
                    +(if (isEdit) "Update" else "Create")
                }
                button(type = ButtonType.button, classes = "btn btn-danger") {
                    attributes["onclick"] = "document.getElementById('rag-form-container').innerHTML=''"
                    +"Cancel"
                }
            }
        }
    }
}
