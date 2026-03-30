package com.demo.services

import com.demo.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object RagService {

    fun create(domain: String, originalText: String, vectorValue: String, metadata: String): RagDocumentDTO = transaction {
        val id = RagDocuments.insertAndGetId {
            it[RagDocuments.domain] = domain
            it[RagDocuments.originalText] = originalText
            it[RagDocuments.vectorValue] = vectorValue
            it[RagDocuments.metadata] = metadata
        }
        getById(id.value)!!
    }

    fun getById(id: Long): RagDocumentDTO? = transaction {
        RagDocuments.select { RagDocuments.id eq id }.singleOrNull()?.toDTO()
    }

    fun getAll(): List<RagDocumentDTO> = transaction {
        RagDocuments.selectAll().orderBy(RagDocuments.createdAt, SortOrder.DESC).map { it.toDTO() }
    }

    fun update(id: Long, domain: String, originalText: String, vectorValue: String, metadata: String): Boolean = transaction {
        RagDocuments.update({ RagDocuments.id eq id }) {
            it[RagDocuments.domain] = domain
            it[RagDocuments.originalText] = originalText
            it[RagDocuments.vectorValue] = vectorValue
            it[RagDocuments.metadata] = metadata
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    fun delete(id: Long): Boolean = transaction {
        RagDocuments.deleteWhere { RagDocuments.id eq id } > 0
    }

    fun searchByDomain(domain: String): List<RagDocumentDTO> = transaction {
        RagDocuments.select { RagDocuments.domain like "%$domain%" }
            .orderBy(RagDocuments.createdAt, SortOrder.DESC)
            .map { it.toDTO() }
    }

    private fun ResultRow.toDTO() = RagDocumentDTO(
        id = this[RagDocuments.id].value,
        domain = this[RagDocuments.domain],
        originalText = this[RagDocuments.originalText],
        vectorValue = this[RagDocuments.vectorValue],
        metadata = this[RagDocuments.metadata],
        createdAt = this[RagDocuments.createdAt],
        updatedAt = this[RagDocuments.updatedAt]
    )
}
