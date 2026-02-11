package org.example.integration

import org.junit.jupiter.api.BeforeAll
import org.testcontainers.utility.DockerImageName

/**
 * Базовый класс для integration тестов с TestContainers
 * Содержит общую конфигурацию для работы с Docker Desktop на Windows
 */
abstract class BaseIntegrationTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun configureTestcontainers() {
            // Настройка для работы с Docker Desktop на Windows
            System.setProperty("testcontainers.reuse.enable", "false")
            System.setProperty("testcontainers.ryuk.disabled", "false")

            // Логирование для диагностики
            println("Docker environment configured for TestContainers")
        }
    }
}

