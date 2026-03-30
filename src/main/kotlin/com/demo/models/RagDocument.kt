package com.demo.models

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object RagDocuments : LongIdTable("rag_documents") {
    val domain = varchar("domain", 255)
    val originalText = text("original_text")
    val vectorValue = text("vector_value").default("[]")
    val metadata = text("metadata").default("{}")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

data class RagDocumentDTO(
    val id: Long,
    val domain: String,
    val originalText: String,
    val vectorValue: String,
    val metadata: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
