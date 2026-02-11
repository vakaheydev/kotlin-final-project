package org.example.service

import io.mockk.*
import org.example.data.repository.AuditLogRepository
import org.example.data.repository.OrderRepository
import org.example.data.repository.ProductRepository
import org.example.domain.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class OrderServiceTest {

    private val orderRepository = mockk<OrderRepository>()
    private val productRepository = mockk<ProductRepository>()
    private val auditLogRepository = mockk<AuditLogRepository>(relaxed = true)
    private val kafkaProducerService = mockk<KafkaProducerService>(relaxed = true)
    private val cacheService = mockk<CacheService>(relaxed = true)

    private val orderService = OrderService(
        orderRepository,
        productRepository,
        auditLogRepository,
        kafkaProducerService,
        cacheService
    )

    @Test
    fun `createOrder should validate product stock`() {
        val userId = 1L
        val productId = 1L
        val product = Product(
            id = productId,
            name = "Test Product",
            description = "Description",
            price = "100.00",
            stock = 5,
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01"
        )

        every { productRepository.findById(productId) } returns product

        val request = CreateOrderRequest(
            items = listOf(OrderItemRequest(productId, 10)) // Запрашиваем больше, чем есть
        )

        val result = orderService.createOrder(userId, request)

        assertTrue(result.isFailure)
    }

    @Test
    fun `cancelOrder should log audit event`() {
        val userId = 1L
        val orderId = 1L

        every { orderRepository.cancel(orderId, userId) } returns true
        every { cacheService.delete(any()) } returns Unit
        every { kafkaProducerService.sendOrderEvent(any()) } returns Unit

        val result = orderService.cancelOrder(orderId, userId)

        assertTrue(result)
        verify { auditLogRepository.log(userId, "CANCEL_ORDER", "ORDER", orderId, any()) }
    }
}

