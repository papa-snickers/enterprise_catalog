# Каталог предприятий

Курсовая работа по дисциплине «Разработка клиент-серверных мобильных приложений»

## 🛠 Стек технологий

### Android-клиент

| Компонент | Технология |
|-----------|-----------|
| Язык | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Архитектура | MVVM (ViewModel + StateFlow) |
| Навигация | Navigation Compose |
| HTTP | Retrofit 2 + OkHttp + kotlinx-serialization |
| Хранение | DataStore Preferences |
| Асинхронность | Kotlin Coroutines |

### Сервер

| Компонент | Технология |
|-----------|-----------|
| Фреймворк | Ktor 3.0.3 (Netty) |
| ORM | Exposed 0.55.0 |
| База данных | SQLite (локально) / PostgreSQL (Docker, VPS) |
| Аутентификация | JWT HMAC256 + BCrypt |
| Контейнеризация | Docker + Docker Compose |

## 📁 Структура репозитория

```
enterprise_catalog/
├── docker-compose.yml            # PostgreSQL + сервер в Docker
├── app/                          # Android-приложение
│   └── src/main/java/com/example/enterprisecatalog/
│       ├── data/
│       │   ├── api/              # Retrofit-интерфейс + ApiClient
│       │   ├── local/            # DataStore (токен, тема, история поиска)
│       │   ├── model/            # DTO-модели, ApiResult
│       │   └── repository/       # AuthRepository, EnterpriseRepository
│       ├── navigation/           # Граф навигации, маршруты
│       └── ui/
│           ├── auth/             # Вход, регистрация
│           ├── catalog/          # Каталог, поиск, фильтры
│           ├── admin/            # Административная панель
│           ├── edit/             # Создание / редактирование предприятия
│           ├── profile/          # Профиль пользователя
│           └── theme/            # Material 3 тема (светлая / тёмная)
└── ktor-server/                  # Серверная часть
    ├── Dockerfile
    ├── deploy.sh                 # Скрипт деплоя на VPS
    ├── enterprise-catalog.service # systemd unit
    └── src/main/kotlin/com/example/server/
        ├── Application.kt        # Точка входа
        ├── DatabaseFactory.kt    # Подключение БД + сиды
        ├── models/               # Exposed-таблицы, DTO
        ├── plugins/              # JWT, CORS, сериализация, статус-страницы
        └── routes/               # Маршруты: auth, enterprises
```

## ✨ Функциональность

- 🔐 **Авторизация** — регистрация и вход с JWT-токеном, две роли: USER и ADMIN
- 🏢 **Каталог предприятий** — список с карточками, детальный просмотр через BottomSheet
- 🔍 **Поиск** — с задержкой debounce 500 мс, история последних 10 запросов
- 🏷 **Фильтрация** — по категории специализации через ChipGroup
- ✏️ **Управление данными** — создание, редактирование, удаление (только ADMIN)
- 📊 **Статистика** — количество предприятий по категориям (только ADMIN)
- 🌙 **Тёмная тема** — переключение из профиля, сохраняется в DataStore

## 🏗 Архитектура

```
Android (MVVM)  ──HTTP/REST──▶  Ktor Server  ──Exposed ORM──▶  SQLite / PostgreSQL
                   + JWT
```

**Клиент:** MVVM + Retrofit + Coroutines + DataStore + Navigation Compose

**Сервер:** Ktor + Exposed + BCrypt + Docker Compose

**Безопасность:** JWT HMAC256, срок действия токена 24 часа. Пароли хранятся в виде BCrypt-хешей. Защищённые эндпоинты проверяют роль из payload токена.

## 🗄 База данных

| Таблица | Назначение |
|---------|-----------|
| `users` | Пользователи (логин, хеш пароля, роль, имя, email) |
| `enterprises` | Предприятия (название, специализация, описание, контакты) |

## 🚀 Запуск сервера

### Вариант 1 — SQLite (быстрый старт, без установки)

```bash
./gradlew :ktor-server:run
```

### Вариант 2 — PostgreSQL локально

```bash
psql -U postgres -c "CREATE DATABASE enterprise_catalog;"

DB_URL="jdbc:postgresql://localhost:5432/enterprise_catalog" \
DB_USER="postgres" \
DB_PASSWORD="твой_пароль" \
./gradlew :ktor-server:run
```

### Вариант 3 — Docker Compose (PostgreSQL + сервер)

```bash
./gradlew :ktor-server:shadowJar
docker compose up --build
```

Сервер будет доступен на `http://localhost:8080`

При первом запуске автоматически создаются таблицы и тестовые данные.

### Переменные окружения

| Переменная | Описание | По умолчанию |
|-----------|----------|-------------|
| `DB_URL` | JDBC-строка подключения | SQLite (если не задана) |
| `DB_USER` | Пользователь БД | `catalog_user` |
| `DB_PASSWORD` | Пароль БД | `catalog_pass` |

## 🔑 Тестовые аккаунты

| Логин | Пароль | Роль |
|-------|--------|------|
| `admin` | `admin123` | ADMIN |
| `user` | `user123` | USER |

## 📡 API

| Метод | Эндпоинт | Авторизация | Описание |
|-------|----------|-------------|----------|
| POST | `/auth/register` | — | Регистрация |
| POST | `/auth/login` | — | Вход |
| GET | `/enterprises` | — | Список (`?query=`, `?category=`) |
| GET | `/enterprises/{id}` | — | Детали предприятия |
| POST | `/enterprises` | ADMIN | Создать предприятие |
| PUT | `/enterprises/{id}` | ADMIN | Обновить предприятие |
| DELETE | `/enterprises/{id}` | ADMIN | Удалить предприятие |
| GET | `/categories` | — | Список специализаций |

Все эндпоинты кроме `/auth/register` и `/auth/login` поддерживают заголовок `Authorization: Bearer <token>`

## 📱 Экраны приложения

| Экран | Описание |
|-------|----------|
| Заставка | Определяет роль и перенаправляет на нужный экран |
| Вход | Авторизация по логину и паролю |
| Регистрация | Создание аккаунта с выбором роли |
| Каталог | Список предприятий, поиск, фильтрация по категории |
| Административная панель | Каталог с CRUD + статистика по категориям |
| Редактирование | Создание и редактирование предприятия |
| Профиль | Данные пользователя, смена темы, выход |
