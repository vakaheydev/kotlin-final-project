package vaka.com.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import vaka.com.data.repository.UserRepository
import vaka.com.domain.AuthResponse
import vaka.com.domain.User
import vaka.com.domain.UserRole
import java.util.*

class AuthService(
    private val userRepository: UserRepository,
    private val jwtSecret: String,
    private val jwtIssuer: String,
    private val jwtAudience: String
) {

    fun register(email: String, password: String): AuthResponse? {
        // Проверяем, не существует ли уже пользователь
        if (userRepository.findByEmail(email) != null) {
            return null
        }

        val user = userRepository.create(email, password, UserRole.USER) ?: return null
        val token = generateToken(user)

        return AuthResponse(token, user)
    }

    fun login(email: String, password: String): AuthResponse? {
        val user = userRepository.verifyPassword(email, password) ?: return null
        val token = generateToken(user)

        return AuthResponse(token, user)
    }

    private fun generateToken(user: User): String {
        return JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withClaim("userId", user.id)
            .withClaim("email", user.email)
            .withClaim("role", user.role.name)
            .withExpiresAt(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24 часа
            .sign(Algorithm.HMAC256(jwtSecret))
    }
}

