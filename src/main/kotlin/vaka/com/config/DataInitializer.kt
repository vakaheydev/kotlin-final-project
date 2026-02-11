package org.example.config

import io.ktor.server.application.*
import org.example.data.repository.UserRepository
import org.example.domain.UserRole
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Инициализатор данных приложения.
 * Создает администратора по умолчанию при первом запуске, если его еще нет.
 */
fun Application.initializeData() {
    val config = environment.config

    // Читаем данные админа из конфигурации или используем значения по умолчанию
    val adminEmail = System.getenv("ADMIN_EMAIL")
        ?: config.propertyOrNull("admin.email")?.getString()
        ?: "admin@shop.com"

    val adminPassword = System.getenv("ADMIN_PASSWORD")
        ?: config.propertyOrNull("admin.password")?.getString()
        ?: "admin123"

    val userRepository = UserRepository()

    transaction {
        // Проверяем, существует ли уже администратор
        val existingAdmin = userRepository.findByEmail(adminEmail)

        if (existingAdmin == null) {
            // Создаем администратора по умолчанию
            val admin = userRepository.create(
                email = adminEmail,
                password = adminPassword,
                role = UserRole.ADMIN
            )

            if (admin != null) {
                log.info("✅ Default admin user created successfully:")
                log.info("   Email: $adminEmail")
                log.info("   Password: $adminPassword")
                log.info("   ⚠️  IMPORTANT: Change the default password in production!")
            } else {
                log.error("❌ Failed to create default admin user")
            }
        } else {
            log.info("ℹ️  Admin user already exists (${existingAdmin.email})")
        }
    }
}

