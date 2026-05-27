# Каталог предприятий

Курсовая работа по дисциплине «Разработка клиент-серверных мобильных приложений»

---

## 🛠 Стек технологий

### Android-клиент

| Компонент | Технология |
|-----------|-----------|
| Язык | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Архитектура | MVVM (ViewModel + StateFlow) |
| Навигация | Navigation Compose |
| HTTP | Retrofit 2 + OkHttp |
| Сериализация | kotlinx-serialization |
| Хранение | DataStore Preferences |
| Асинхронность | Kotlin Coroutines |

### Сервер

| Компонент | Технология |
|-----------|-----------|
| Фреймворк | Ktor 3.0.3 (Netty) |
| ORM | Exposed 0.55.0 |
| База данных | SQLite (локально) / PostgreSQL (Docker) |
| Аутентификация | JWT HMAC256 + BCrypt |
| Контейнеризация | Docker + Docker Compose |

---

## 📁 Структура проекта

```
enterprise_catalog/
├── docker-compose.yml
├── app/
│   └── src/main/java/com/example/enterprisecatalog/
│       ├── data/
│       │   ├── api/              # Retrofit-интерфейс + ApiClient + AuthInterceptor
│       │   ├── local/            # DataStore (токен, роль, тема, история поиска)
│       │   ├── model/            # DTO-модели, sealed class ApiResult
│       │   └── repository/       # AuthRepository, EnterpriseRepository
│       ├── navigation/           # NavGraph, Screen (маршруты)
│       └── ui/
│           ├── auth/             # Вход, регистрация + ViewModel
│           ├── catalog/          # Каталог, поиск, фильтры, BottomSheet
│           ├── admin/            # Административная панель + статистика
│           ├── edit/             # Создание / редактирование предприятия
│           ├── profile/          # Профиль пользователя
│           ├── splash/           # Заставка с определением роли
│           └── theme/            # Цвета, типографика, Material 3 тема
└── ktor-server/
    ├── Dockerfile
    ├── deploy.sh
    ├── enterprise-catalog.service
    └── src/main/kotlin/com/example/server/
        ├── Application.kt
        ├── DatabaseFactory.kt
        ├── models/               # Exposed-таблицы, DTO
        ├── plugins/              # JWT, CORS, сериализация, статус-страницы
        └── routes/               # AuthRoutes, EnterpriseRoutes
```

---

## ✨ Функциональность

- 🔐 **Авторизация** — регистрация и вход, две роли: USER и ADMIN, токен хранится в DataStore
- 🏢 **Каталог** — список предприятий с карточками, детальный просмотр через BottomSheet
- 🔍 **Поиск** — debounce 500 мс по названию, описанию и адресу, история последних 10 запросов
- 🏷 **Фильтрация** — по категории специализации через горизонтальные чипы
- ✏️ **Управление** — создание, редактирование, удаление предприятий (только ADMIN)
- 📊 **Статистика** — количество предприятий по категориям (только ADMIN)
- 🌙 **Тёмная тема** — переключение из профиля, сохраняется между сессиями

---

## 🏗 Архитектура

```
Android (MVVM)  ──HTTP/REST──▶  Ktor Server  ──Exposed ORM──▶  SQLite / PostgreSQL
                  JWT Bearer
```

**Клиент:** каждый запрос автоматически получает заголовок `Authorization: Bearer <token>` через OkHttp `Interceptor`. Состояние экранов управляется через `StateFlow` во `ViewModel`. Сохранение состояния при пересоздании — через `SavedStateHandle`.

**Сервер:** публичные эндпоинты доступны без токена. Защищённые обёрнуты в `authenticate(JWT_AUTH)`, внутри дополнительно проверяется роль из payload токена. Пароли хранятся исключительно в виде BCrypt-хешей.

---

## 🗄 База данных

### Таблица `users`

| Поле | Тип | Описание |
|------|-----|----------|
| id | VARCHAR(36) | UUID пользователя |
| name | VARCHAR(255) | Полное имя |
| login | VARCHAR(100) | Логин (уникальный) |
| email | VARCHAR(255) | Email (уникальный) |
| password_hash | VARCHAR(255) | BCrypt-хеш пароля |
| role | VARCHAR(20) | Роль: USER или ADMIN |

### Таблица `enterprises`

| Поле | Тип | Описание |
|------|-----|----------|
| id | VARCHAR(36) | UUID предприятия |
| name | VARCHAR(255) | Наименование |
| specialization | VARCHAR(100) | Категория |
| description | TEXT | Описание деятельности |
| address | VARCHAR(500) | Адрес |
| phone | VARCHAR(50) | Телефон |
| email | VARCHAR(255) | Email |
| website | VARCHAR(255) | Сайт |
| created_at | VARCHAR(50) | Дата создания |

---

## 🚀 Запуск сервера

### 1. Сборка JAR

```bash
./gradlew :ktor-server:shadowJar
```

### 2. Сборка Docker-образа

```bash
docker build -t enterprise-catalog:latest ./ktor-server
```

### 3. Запуск стека (PostgreSQL + сервер)

```bash
docker compose up --build
```

Сервер будет доступен на `http://localhost:8080`

При первом запуске автоматически создаются таблицы и загружаются тестовые данные (2 пользователя, 6 предприятий).

### Переменные окружения

| Переменная | Описание |
|-----------|----------|
| `DB_URL` | JDBC-строка подключения (если не задана — используется SQLite) |
| `DB_USER` | Пользователь БД |
| `DB_PASSWORD` | Пароль БД |

---

## 🌐 Доступ с реальных устройств (ngrok)

Для подключения реального Android-устройства к локальному серверу используется [ngrok](https://ngrok.com) — инструмент создания публичного HTTPS-туннеля к локальному порту.

```bash
ngrok http 8080
```

После запуска ngrok предоставляет публичный URL вида:
```
https://xxx.ngrok-free.app → http://localhost:8080
```

Этот URL прописывается в `BuildConfig.BASE_URL` приложения. Все запросы с телефона проходят через туннель к локальному серверу.

Веб-интерфейс для мониторинга запросов: `http://127.0.0.1:4040`

---

## 🔑 Тестовые аккаунты

| Логин | Пароль | Роль |
|-------|--------|------|
| `admin` | `admin123` | ADMIN |
| `user` | `user123` | USER |

---

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

---

## 📱 Экраны приложения

| Экран | Описание |
|-------|----------|
| Заставка | Проверяет токен, перенаправляет по роли |
| Вход | Авторизация по логину и паролю |
| Регистрация | Создание аккаунта с выбором роли USER / ADMIN |
| Каталог | Список предприятий, поиск с историей, фильтры по категориям |
| Административная панель | Каталог с CRUD-операциями + статистика по категориям |
| Редактирование | Форма создания и редактирования предприятия |
| Профиль | Имя, логин, email, роль, переключатель темы, выход |
