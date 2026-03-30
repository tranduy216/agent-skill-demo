package com.demo.services

import com.demo.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object LogService {

    fun getAll(): List<ExecutionLogDTO> = transaction {
        val logs = ExecutionLogs
            .innerJoin(AiProposals, { proposalId }, { AiProposals.id })
            .innerJoin(Users, { ExecutionLogs.executedBy }, { Users.id })
            .selectAll()
            .orderBy(ExecutionLogs.createdAt, SortOrder.DESC)
            .map { row ->
                ExecutionLogDTO(
                    id = row[ExecutionLogs.id].value,
                    proposalId = row[ExecutionLogs.proposalId],
                    proposalQuery = row[AiProposals.query],
                    executedBy = row[ExecutionLogs.executedBy],
                    executorName = row[Users.username],
                    action = row[ExecutionLogs.action],
                    result = row[ExecutionLogs.result],
                    success = row[ExecutionLogs.success],
                    createdAt = row[ExecutionLogs.createdAt]
                )
            }
        logs
    }
}
