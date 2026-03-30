package com.demo.services

import com.demo.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

object UserService {

    fun createUser(username: String, password: String, role: UserRole): UserDTO? = transaction {
        val existing = Users.select { Users.username eq username }.singleOrNull()
        if (existing != null) return@transaction null

        val id = Users.insertAndGetId {
            it[Users.username] = username
            it[passwordHash] = BCrypt.hashpw(password, BCrypt.gensalt())
            it[Users.role] = role
        }
        getById(id.value)
    }

    fun authenticate(username: String, password: String): UserDTO? = transaction {
        val row = Users.select { Users.username eq username }.singleOrNull() ?: return@transaction null
        if (!BCrypt.checkpw(password, row[Users.passwordHash])) return@transaction null
        row.toUserDTO()
    }

    fun getById(id: Long): UserDTO? = transaction {
        Users.select { Users.id eq id }.singleOrNull()?.toUserDTO()
    }

    fun getAll(): List<UserDTO> = transaction {
        Users.selectAll().map { it.toUserDTO() }
    }

    private fun ResultRow.toUserDTO() = UserDTO(
        id = this[Users.id].value,
        username = this[Users.username],
        role = this[Users.role],
        createdAt = this[Users.createdAt]
    )

    fun initDefaultUsers() {
        if (getAll().isEmpty()) {
            createUser("admin", "admin123", UserRole.ADMIN)
            createUser("user", "user123", UserRole.USER)
        }
    }
}
