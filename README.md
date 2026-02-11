# Online Shop Backend API

Backend-сервис для интернет-магазина, построенный на Ktor с использованием современных технологий.

## Технологический стек

- **Kotlin** - основной язык программирования
- **Ktor 3.0** - веб-фреймворк
- **PostgreSQL** - основная база данных
- **Exposed ORM** - для работы с БД
- **Redis** - кэширование данных
- **Apache Kafka** - очередь сообщений для асинхронной обработки
- **JWT** - аутентификация и авторизация
- **Docker & Docker Compose** - контейнеризация
- **JUnit & MockK** - тестирование

## Возможности

### Для пользователей:
- Регистрация и авторизация
- Просмотр каталога товаров
- Создание заказов
- Отмена заказов
- Просмотр истории покупок

### Для администраторов:
- Управление товарами (CRUD операции)
- Получение статистики по заказам
- Доступ ко всем функциям через защищенный API

## Архитектура

Проект следует многослойной архитектуре:

```
src/
├── main/
│   ├── kotlin/org/example/
│   │   ├── config/         # Конфигурация приложения
│   │   ├── data/
│   │   │   ├── repository/ # Слой работы с данными
│   │   │   └── tables/     # Схемы таблиц БД
│   │   ├── domain/         # Доменные модели
│   │   ├── routes/         # HTTP маршруты
│   │   ├── service/        # Бизнес-логика
│   │   └── Application.kt  # Точка входа
│   └── resources/
│       ├── application.conf
│       ├── logback.xml
│       └── openapi/        # Swagger документация
└── test/
    ├── e2e/                # End-to-end тесты
    ├── integration/        # Интеграционные тесты
    └── service/            # Unit тесты
```

## Запуск проекта

### Предварительные требования

- JDK 21
- Docker и Docker Compose
- Gradle (или использовать wrapper)

### Локальный запуск с Docker Compose

1. Клонируйте репозиторий:
```bash
git clone <repository-url>
cd kotlin-final-project
```

2. Запустите все сервисы:
```bash
docker-compose up -d
```

Это запустит:
- PostgreSQL (порт 5433) 
- Redis (порт 6379)
- Kafka + Zookeeper (порт 9092)
- Приложение (порт 8080)

3. Приложение будет доступно по адресу: `http://localhost:8080`

### Учётная запись администратора по умолчанию

При первом запуске автоматически создаётся администратор:
- **Email:** `admin@shop.com`
- **Пароль:** `admin123`

⚠️ **ВАЖНО:** Измените пароль в production! 

Подробнее см. [ADMIN_GUIDE.md](ADMIN_GUIDE.md)

### Локальный запуск для разработки

1. Запустите только инфраструктуру:
```bash
docker-compose up -d postgres redis zookeeper kafka
```

2. Запустите приложение:
```bash
./gradlew run
```

## API Документация

Swagger UI доступен по адресу: `http://localhost:8080/swagger`

### Основные эндпоинты

#### Аутентификация
- `POST /auth/register` - Регистрация
- `POST /auth/login` - Вход

#### Товары
- `GET /products` - Список товаров
- `GET /products/{id}` - Конкретный товар

#### Заказы (требуется авторизация)
- `POST /orders` - Создать заказ
- `GET /orders` - История заказов
- `DELETE /orders/{id}` - Отменить заказ

#### Админ (требуется роль ADMIN)
- `POST /admin/products` - Добавить товар
- `PUT /admin/products/{id}` - Обновить товар
- `DELETE /admin/products/{id}` - Удалить товар
- `GET /admin/stats/orders` - Статистика

## Тестирование

Запуск всех тестов:
```bash
./gradlew test
```

Проект включает:
- **Unit тесты** - тестирование сервисов и бизнес-логики
- **Integration тесты** - тестирование репозиториев с TestContainers
- **E2E тесты** - тестирование API эндпоинтов

## Основные функции

### Кэширование
Товары кэшируются в Redis с TTL 5 минут. Кэш автоматически инвалидируется при обновлении/удалении товара.

### Асинхронная обработка
При создании/отмене заказа событие отправляется в Kafka. Consumer обрабатывает события и:
- Логирует действия
- Отправляет уведомления (заглушка)

### Аудит
Все важные действия (создание/отмена заказов) логируются в таблицу `audit_logs`.

### Безопасность
- JWT токены для аутентификации
- Роли пользователей (USER, ADMIN)
- Защита админских эндпоинтов

## Конфигурация

Настройки находятся в `src/main/resources/application.conf`:

```hocon
database {
    url = "jdbc:postgresql://localhost:5433/shop_db"
    user = "postgres"
    password = "postgres"
}

redis {
    host = "localhost"
    port = 6379
}

kafka {
    bootstrapServers = "localhost:9092"
    topic = "order-events"
}

jwt {
    secret = "your-secret-key"
    issuer = "http://localhost:8080"
}
```

## CI/CD

GitHub Actions workflow автоматически:
- Собирает проект
- Запускает тесты
- Создает Docker образ

## Разработка

### Создание первого администратора

После запуска приложения зарегистрируйте пользователя и вручную обновите его роль в БД:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

### Просмотр логов

```bash
docker-compose logs -f app
```

### Остановка сервисов

```bash
docker-compose down
```

## Лицензия

MIT License

## Контакты

По вопросам обращайтесь: support@shop.com

