package vaka.com.service

import vaka.com.data.repository.AuditLogRepository
import vaka.com.data.repository.OrderRepository
import vaka.com.data.repository.ProductRepository
import vaka.com.domain.CreateOrderRequest
import vaka.com.domain.Order
import org.jetbrains.exposed.sql.transactions.transaction

class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val auditLogRepository: AuditLogRepository,
    private val kafkaProducerService: KafkaProducerService,
    private val cacheService: CacheService
) {

    fun createOrder(userId: Long, request: CreateOrderRequest): Result<Order> = transaction {
        try {
            // Проверяем наличие товаров и резервируем их
            for (item in request.items) {
                val product = productRepository.findById(item.productId)
                    ?: return@transaction Result.failure(Exception("Product ${item.productId} not found"))

                if (product.stock < item.quantity) {
                    return@transaction Result.failure(Exception("Not enough stock for product ${product.name}"))
                }
            }

            // Уменьшаем количество товара на складе
            for (item in request.items) {
                val success = productRepository.decreaseStock(item.productId, item.quantity)
                if (!success) {
                    return@transaction Result.failure(Exception("Failed to decrease stock"))
                }
                // Очищаем кэш товара
                cacheService.delete("product:${item.productId}")
            }

            // Создаем заказ
            val order = orderRepository.create(userId, request.items)

            // Логируем создание заказа
            auditLogRepository.log(
                userId = userId,
                action = "CREATE_ORDER",
                entityType = "ORDER",
                entityId = order.id,
                details = "Created order with ${request.items.size} items, total: ${order.totalPrice}"
            )

            // Кэшируем заказ
            cacheService.setJson("order:${order.id}", order, 600)

            // Отправляем событие в Kafka
            kafkaProducerService.sendOrderEvent(
                OrderEvent(
                    orderId = order.id,
                    userId = userId,
                    eventType = "ORDER_CREATED",
                    details = "Order created with total price ${order.totalPrice}"
                )
            )

            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getOrder(id: Long, userId: Long): Order? {
        val order = orderRepository.findById(id) ?: return null

        // Проверяем, что заказ принадлежит пользователю
        if (order.userId != userId) {
            return null
        }

        return order
    }

    fun getUserOrders(userId: Long): List<Order> {
        return orderRepository.findByUserId(userId)
    }

    fun cancelOrder(id: Long, userId: Long): Boolean = transaction {
        val cancelled = orderRepository.cancel(id, userId)

        if (cancelled) {
            // Логируем отмену
            auditLogRepository.log(
                userId = userId,
                action = "CANCEL_ORDER",
                entityType = "ORDER",
                entityId = id,
                details = "Order cancelled by user"
            )

            // Очищаем кэш
            cacheService.delete("order:$id")

            // Отправляем событие в Kafka
            kafkaProducerService.sendOrderEvent(
                OrderEvent(
                    orderId = id,
                    userId = userId,
                    eventType = "ORDER_CANCELLED",
                    details = "Order cancelled"
                )
            )
        }

        cancelled
    }
}

