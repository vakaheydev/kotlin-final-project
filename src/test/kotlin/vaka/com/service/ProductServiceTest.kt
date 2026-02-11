package org.example.service

import io.mockk.*
import kotlinx.serialization.json.Json
import org.example.data.repository.ProductRepository
import org.example.domain.Product
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ProductServiceTest {

    private val productRepository = mockk<ProductRepository>()
    private val cacheService = mockk<CacheService>(relaxed = true)
    private val productService = ProductService(productRepository, cacheService)

    @Test
    fun `getProduct should return product from repository when not cached`() {
        val productId = 1L
        val product = Product(
            id = productId,
            name = "Test Product",
            description = "Description",
            price = "100.00",
            stock = 10,
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01"
        )

        every { cacheService.get<Product>(any(), any()) } returns null
        every { productRepository.findById(productId) } returns product

        val result = productService.getProduct(productId)

        assertNotNull(result)
        assertEquals(product, result)
        verify { productRepository.findById(productId) }
        verify { cacheService.setJson("product:$productId", product, 300) }
    }

    @Test
    fun `createProduct should create product in repository`() {
        val name = "New Product"
        val description = "New Description"
        val price = "150.00"
        val stock = 20
        val product = Product(
            id = 1,
            name = name,
            description = description,
            price = price,
            stock = stock,
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01"
        )

        every { productRepository.create(name, description, BigDecimal(price), stock) } returns product

        val result = productService.createProduct(name, description, price, stock)

        assertEquals(product, result)
        verify { productRepository.create(name, description, BigDecimal(price), stock) }
    }

    @Test
    fun `deleteProduct should clear cache after deletion`() {
        val productId = 1L

        every { productRepository.delete(productId) } returns true
        every { cacheService.delete(any()) } returns Unit

        val result = productService.deleteProduct(productId)

        assertEquals(true, result)
        verify { cacheService.delete("product:$productId") }
    }
}

