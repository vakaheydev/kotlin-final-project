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
            // Проверяем что все товары есть в наличии
            for (item in request.items) {
                val product = productRepository.findById(item.productId)
                    ?: return@transaction Result.failure(Exception("Product ${item.productId} not found"))

                if (product.stock < item.quantity) {
                    return@transaction Result.failure(Exception("Not enough stock for product ${product.name}"))
                }
            }

            // Списываем товары со склада
            for (item in request.items) {
                val success = productRepository.decreaseStock(item.productId, item.quantity)
                if (!success) {
                    return@transaction Result.failure(Exception("Failed to decrease stock"))
                }
                // Нужно сбросить кеш этого товара
                cacheService.delete("product:${item.productId}")
            }

            // Сохраняем заказ
            val order = orderRepository.create(userId, request.items)

            // Пишем в audit log
            auditLogRepository.log(
                userId = userId,
                action = "CREATE_ORDER",
                entityType = "ORDER",
                entityId = order.id,
                details = "Created order with ${request.items.size} items, total: ${order.totalPrice}"
            )

            // Кешируем заказ на 10 минут
            cacheService.setJson("order:${order.id}", order, 600)

            // Шлем событие в кафку
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
            // Сохраняем в audit
            auditLogRepository.log(
                userId = userId,
                action = "CANCEL_ORDER",
                entityType = "ORDER",
                entityId = id,
                details = "Order cancelled by user"
            )

            // Удаляем из кеша
            cacheService.delete("order:$id")

            // Уведомляем через кафку
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

