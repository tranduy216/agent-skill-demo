package com.demo.models

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

enum class UserRole { ADMIN, USER }

object Users : LongIdTable("users") {
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = enumerationByName<UserRole>("role", 10)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

data class UserDTO(
    val id: Long,
    val username: String,
    val role: UserRole,
    val createdAt: LocalDateTime
)
