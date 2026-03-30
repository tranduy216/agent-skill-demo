package com.demo.models

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

enum class ProposalStatus { PENDING, APPROVED, REJECTED, EXECUTED }

object AiProposals : LongIdTable("ai_proposals") {
    val userId = long("user_id").references(Users.id)
    val query = text("query")
    val modelName = varchar("model_name", 100)
    val proposedSteps = text("proposed_steps")
    val aiResponse = text("ai_response").default("")
    val status = enumerationByName<ProposalStatus>("status", 20).default(ProposalStatus.PENDING)
    val reviewedBy = long("reviewed_by").references(Users.id).nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

data class AiProposalDTO(
    val id: Long,
    val userId: Long,
    val username: String,
    val query: String,
    val modelName: String,
    val proposedSteps: String,
    val aiResponse: String,
    val status: ProposalStatus,
    val reviewedBy: Long?,
    val reviewerName: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
