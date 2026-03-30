package com.demo.services

import com.demo.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object ProposalService {

    fun create(userId: Long, query: String, modelName: String, proposedSteps: String, aiResponse: String): AiProposalDTO = transaction {
        val id = AiProposals.insertAndGetId {
            it[AiProposals.userId] = userId
            it[AiProposals.query] = query
            it[AiProposals.modelName] = modelName
            it[AiProposals.proposedSteps] = proposedSteps
            it[AiProposals.aiResponse] = aiResponse
            it[status] = ProposalStatus.PENDING
        }
        getById(id.value)!!
    }

    fun getById(id: Long): AiProposalDTO? = transaction {
        AiProposals.innerJoin(Users, { AiProposals.userId }, { Users.id })
            .select { AiProposals.id eq id }
            .singleOrNull()?.toDTO()
    }

    fun getAll(): List<AiProposalDTO> = transaction {
        AiProposals.innerJoin(Users, { AiProposals.userId }, { Users.id })
            .selectAll()
            .orderBy(AiProposals.createdAt, SortOrder.DESC)
            .map { it.toDTO() }
    }

    fun approve(id: Long, adminId: Long): Boolean = transaction {
        AiProposals.update({ (AiProposals.id eq id) and (AiProposals.status eq ProposalStatus.PENDING) }) {
            it[status] = ProposalStatus.APPROVED
            it[reviewedBy] = adminId
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    fun reject(id: Long, adminId: Long): Boolean = transaction {
        AiProposals.update({ (AiProposals.id eq id) and (AiProposals.status eq ProposalStatus.PENDING) }) {
            it[status] = ProposalStatus.REJECTED
            it[reviewedBy] = adminId
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    fun execute(id: Long, adminId: Long): Boolean = transaction {
        val proposal = AiProposals.select {
            (AiProposals.id eq id) and (AiProposals.status eq ProposalStatus.APPROVED)
        }.singleOrNull() ?: return@transaction false

        AiProposals.update({ AiProposals.id eq id }) {
            it[status] = ProposalStatus.EXECUTED
            it[updatedAt] = LocalDateTime.now()
        }

        ExecutionLogs.insert {
            it[proposalId] = id
            it[executedBy] = adminId
            it[action] = "Executed proposal: ${proposal[AiProposals.query]}"
            it[result] = "Steps executed:\n${proposal[AiProposals.proposedSteps]}"
            it[success] = true
        }

        true
    }

    private fun ResultRow.toDTO(): AiProposalDTO {
        val reviewerId = this[AiProposals.reviewedBy]
        val reviewerName = if (reviewerId != null) {
            UserService.getById(reviewerId)?.username
        } else null

        return AiProposalDTO(
            id = this[AiProposals.id].value,
            userId = this[AiProposals.userId],
            username = this[Users.username],
            query = this[AiProposals.query],
            modelName = this[AiProposals.modelName],
            proposedSteps = this[AiProposals.proposedSteps],
            aiResponse = this[AiProposals.aiResponse],
            status = this[AiProposals.status],
            reviewedBy = reviewerId,
            reviewerName = reviewerName,
            createdAt = this[AiProposals.createdAt],
            updatedAt = this[AiProposals.updatedAt]
        )
    }
}
