package vaka.com.e2e

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import vaka.com.module
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductE2ETest {

    @Test
    fun `should get all products`() = testApplication {
        application {
            module()
        }

        val response = client.get("/products")

        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        assertTrue(body.startsWith("["))
    }

    @Test
    fun `should get product by id`() = testApplication {
        application {
            module()
        }

        // Получаем список продуктов
        val productsResponse = client.get("/products")
        val productsBody = productsResponse.bodyAsText()

        // Если есть продукты, проверяем получение по ID
        if (productsBody != "[]" && productsBody.contains("\"id\":")) {
            val productId = productsBody
                .substringAfter("\"id\":")
                .substringBefore(",")
                .trim()

            val response = client.get("/products/$productId")
            assertEquals(HttpStatusCode.OK, response.status)
        } else {
            // Если товаров нет - просто проверяем что список пустой
            assertEquals("[]", productsBody)
        }
    }
}

