package org.example.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.example.config.userRole
import org.example.data.repository.OrderRepository
import org.example.domain.CreateProductRequest
import org.example.domain.UpdateProductRequest
import org.example.domain.UserRole
import org.example.service.ProductService

@Serializable
data class StatsResponse(
    val totalOrders: Long,
    val totalRevenue: String
)

fun Route.adminRoutes(productService: ProductService, orderRepository: OrderRepository) {
    authenticate("auth-jwt") {
        route("/admin") {
            // Добавление товара
            post("/products") {
                // Проверка роли админа
                if (call.userRole != UserRole.ADMIN) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Access denied"))
                    return@post
                }

                val request = call.receive<CreateProductRequest>()

                if (request.name.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Product name is required"))
                    return@post
                }

                val product = productService.createProduct(
                    request.name,
                    request.description,
                    request.price,
                    request.stock
                )

                call.respond(HttpStatusCode.Created, product)
            }

            // Обновление товара
            put("/products/{id}") {
                // Проверка роли админа
                if (call.userRole != UserRole.ADMIN) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Access denied"))
                    return@put
                }

                val id = call.parameters["id"]?.toLongOrNull()

                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid product ID"))
                    return@put
                }

                val request = call.receive<UpdateProductRequest>()

                val product = productService.updateProduct(
                    id,
                    request.name,
                    request.description,
                    request.price,
                    request.stock
                )

                if (product == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Product not found"))
                } else {
                    call.respond(HttpStatusCode.OK, product)
                }
            }

            // Удаление товара
            delete("/products/{id}") {
                // Проверка роли админа
                if (call.userRole != UserRole.ADMIN) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Access denied"))
                    return@delete
                }

                val id = call.parameters["id"]?.toLongOrNull()

                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid product ID"))
                    return@delete
                }

                val deleted = productService.deleteProduct(id)

                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Product deleted successfully"))
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Product not found"))
                }
            }

            // Статистика заказов
            get("/stats/orders") {
                // Проверка роли админа
                if (call.userRole != UserRole.ADMIN) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("Access denied"))
                    return@get
                }

                val totalOrders = orderRepository.countAll()
                val totalRevenue = orderRepository.getTotalRevenue()

                call.respond(HttpStatusCode.OK, StatsResponse(totalOrders, totalRevenue.toString()))
            }
        }
    }
}

