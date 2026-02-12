package vaka.com.e2e

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

// Базовый класс для E2E тестов
@Testcontainers
abstract class BaseE2ETest {

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("e2e_test_db")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @BeforeAll
        fun setupDatabase() {
            // Устанавливаем системные переменные для тестовой БД
            System.setProperty("DB_URL", postgres.jdbcUrl)
            System.setProperty("DB_USER", postgres.username)
            System.setProperty("DB_PASSWORD", postgres.password)
            System.setProperty("DB_DRIVER", postgres.driverClassName)

            println("E2E Test database configured: ${postgres.jdbcUrl}")
        }

        @JvmStatic
        @AfterAll
        fun teardown() {
            // Очищаем системные тестовые переменные после тестов
            System.clearProperty("DB_URL")
            System.clearProperty("DB_USER")
            System.clearProperty("DB_PASSWORD")
            System.clearProperty("DB_DRIVER")
        }
    }
}

