package vaka.com.integration

import org.junit.jupiter.api.BeforeAll
import org.testcontainers.utility.DockerImageName

// Базовый класс для интеграционных тестов
abstract class BaseIntegrationTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun configureTestcontainers() {
            // Настройки для Docker Desktop
            System.setProperty("testcontainers.reuse.enable", "false")
            System.setProperty("testcontainers.ryuk.disabled", "false")

            println("Docker environment configured for TestContainers")
        }
    }
}

