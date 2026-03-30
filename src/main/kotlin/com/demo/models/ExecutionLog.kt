package com.demo.models

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object ExecutionLogs : LongIdTable("execution_logs") {
    val proposalId = long("proposal_id").references(AiProposals.id)
    val executedBy = long("executed_by").references(Users.id)
    val action = text("action")
    val result = text("result")
    val success = bool("success").default(true)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

data class ExecutionLogDTO(
    val id: Long,
    val proposalId: Long,
    val proposalQuery: String,
    val executedBy: Long,
    val executorName: String,
    val action: String,
    val result: String,
    val success: Boolean,
    val createdAt: LocalDateTime
)
