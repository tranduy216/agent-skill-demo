package com.demo.plugins

import com.demo.models.*
import com.demo.services.UserService
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val config = HikariConfig().apply {
        driverClassName = environment.config.property("database.driverClassName").getString()
        jdbcUrl = environment.config.property("database.jdbcURL").getString()
        maximumPoolSize = environment.config.property("database.maxPoolSize").getString().toInt()
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(Users, RagDocuments, AiProposals, ExecutionLogs)
    }

    UserService.initDefaultUsers()

    log.info("Database initialized successfully")
}
