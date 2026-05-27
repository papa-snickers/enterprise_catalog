# Каталог предприятий

Курсовая работа — клиент-серверное Android-приложение «Каталог предприятий».

## Стек технологий

| Компонент | Технология |
|-----------|-----------|
| Язык | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Архитектура | MVVM (ViewModel + StateFlow) |
| HTTP | Retrofit 2 + kotlinx-serialization |
| Навигация | Navigation Compose |
| Хранение | DataStore Preferences |
| Асинхронность | Kotlin Coroutines |
| Сервер | Ktor + Exposed ORM + SQLite |
| Аутентификация | JWT (HMAC256) + BCrypt |

## Запуск сервера

```bash
./gradlew :ktor-server:run
```

Сервер стартует на `http://localhost:8080` и автоматически:
- Создаёт таблицы в SQLite (`enterprise_catalog.db`)
- Загружает тестовые данные (если БД пустая)

## Тестовые аккаунты

| Логин | Пароль | Роль |
|-------|--------|------|
| `admin` | `admin123` | ADMIN |
| `user` | `user123` | USER |

## Запуск Android-приложения

1. Запустите сервер (см. выше)
2. Откройте проект в Android Studio
3. Запустите приложение на эмуляторе AVD (используется `10.0.2.2:8080` — адрес хоста из эмулятора)

## Структура проекта

```
course_work/
├── ktor-server/          # Серверная часть (Ktor + Exposed + SQLite)
│   └── src/main/kotlin/com/example/server/
│       ├── Application.kt          # Точка входа
│       ├── DatabaseFactory.kt      # Инициализация БД + сиды
│       ├── models/
│       │   ├── Tables.kt           # Exposed-таблицы
│       │   └── Dtos.kt             # Сериализуемые модели
│       ├── plugins/                # Ktor-плагины
│       └── routes/                 # REST-маршруты
└── app/                  # Android-приложение
    └── src/main/java/com/example/enterprisecatalog/
        ├── MainActivity.kt
        ├── data/
        │   ├── api/ApiService.kt           # Retrofit-интерфейс
        │   ├── local/DataStoreManager.kt   # DataStore
        │   └── repository/                 # Репозитории
        ├── ui/                             # Экраны и компоненты
        └── navigation/                     # Граф навигации
```

## API-эндпоинты

| Метод | Путь | Авторизация | Описание |
|-------|------|-------------|----------|
| POST | `/auth/register` | — | Регистрация |
| POST | `/auth/login` | — | Вход |
| GET | `/enterprises` | — | Список (`?query=`, `?category=`) |
| GET | `/enterprises/{id}` | — | Детали |
| POST | `/enterprises` | ADMIN | Создание |
| PUT | `/enterprises/{id}` | ADMIN | Редактирование |
| DELETE | `/enterprises/{id}` | ADMIN | Удаление |
| GET | `/categories` | — | Список специализаций |
