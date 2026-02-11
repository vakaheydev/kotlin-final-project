package vaka.com

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.launch
import vaka.com.config.configureDatabase
import vaka.com.config.configureMonitoring
import vaka.com.config.configureRouting
import vaka.com.config.configureSecurity
import vaka.com.config.configureSerialization
import vaka.com.config.configureSwagger
import vaka.com.config.initializeData
import vaka.com.service.KafkaConsumerService

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureDatabase()
    initializeData() // Создаем админа по умолчанию
    configureSerialization()
    configureSecurity()
    configureMonitoring()
    configureSwagger()
    configureRouting()

    // Запускаем Kafka consumer в фоновом режиме
    val config = environment.config
    val kafkaConsumer = KafkaConsumerService(
        bootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: config.propertyOrNull("kafka.bootstrapServers")?.getString() ?: "localhost:9092",
        topic = System.getenv("KAFKA_TOPIC") ?: config.propertyOrNull("kafka.topic")?.getString() ?: "order-events"
    )

    launch {
        kafkaConsumer.startConsuming()
    }

    // Graceful shutdown
    monitor.subscribe(ApplicationStopped) {
        kafkaConsumer.close()
    }
}

