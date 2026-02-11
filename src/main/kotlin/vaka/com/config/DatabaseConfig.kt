package org.example.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.example.data.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val config = environment.config

    // Читаем из переменных окружения или из config файла
    val dbDriver = System.getenv("DB_DRIVER") ?: config.propertyOrNull("database.driver")?.getString() ?: "org.postgresql.Driver"
    val dbUrl = System.getenv("DB_URL") ?: config.propertyOrNull("database.url")?.getString() ?: "jdbc:postgresql://localhost:5433/shop_db"
    val dbUser = System.getenv("DB_USER") ?: config.propertyOrNull("database.user")?.getString() ?: "postgres"
    val dbPassword = System.getenv("DB_PASSWORD") ?: config.propertyOrNull("database.password")?.getString() ?: "postgres"
    val maxPoolSize = System.getenv("DB_MAX_POOL_SIZE")?.toIntOrNull() ?: config.propertyOrNull("database.maxPoolSize")?.getString()?.toIntOrNull() ?: 10

    log.info("Database configuration: driver=$dbDriver, url=$dbUrl, user=$dbUser, maxPoolSize=$maxPoolSize")

    val dbConfig = HikariConfig().apply {
        driverClassName = dbDriver
        jdbcUrl = dbUrl
        username = dbUser
        password = dbPassword
        maximumPoolSize = maxPoolSize
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val dataSource = HikariDataSource(dbConfig)
    Database.connect(dataSource)

    // Создаем таблицы, если их нет
    transaction {
        SchemaUtils.create(Users, Products, Orders, OrderItems, AuditLogs)
    }
}

