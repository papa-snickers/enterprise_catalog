package com.example.server

import com.example.server.models.Enterprises
import com.example.server.models.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

object DatabaseFactory {

    fun init() {
        val dbUrl = System.getenv("DB_URL")

        if (dbUrl != null) {
            // VPS: PostgreSQL (задаётся через переменные окружения systemd)
            Database.connect(
                url = dbUrl,
                driver = "org.postgresql.Driver",
                user = System.getenv("DB_USER") ?: "catalog_user",
                password = System.getenv("DB_PASSWORD") ?: "catalog_pass"
            )
        } else {
            // Локальная разработка: SQLite (без установки)
            Database.connect(
                url = "jdbc:sqlite:enterprise_catalog.db",
                driver = "org.sqlite.JDBC"
            )
        }

        transaction {
            SchemaUtils.create(Users, Enterprises)
            seedIfEmpty()
        }
    }

    private fun seedIfEmpty() {
        if (Users.selectAll().count() > 0L) return

        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        // Тестовые пользователи
        Users.insert {
            it[id] = UUID.randomUUID().toString()
            it[name] = "Администратор"
            it[login] = "admin"
            it[email] = "admin@enterprise-catalog.ru"
            it[passwordHash] = BCrypt.hashpw("admin123", BCrypt.gensalt())
            it[role] = "ADMIN"
        }
        Users.insert {
            it[id] = UUID.randomUUID().toString()
            it[name] = "Иванов Иван"
            it[login] = "user"
            it[email] = "user@enterprise-catalog.ru"
            it[passwordHash] = BCrypt.hashpw("user123", BCrypt.gensalt())
            it[role] = "USER"
        }

        // Тестовые предприятия
        val testEnterprises = listOf(
            listOf(
                "ООО «Торговый дом Меркурий»",
                "Торговля",
                "Крупная розничная сеть продуктовых магазинов Поволжского региона. Более 50 торговых точек в Самаре и области. Широкий ассортимент товаров повседневного спроса, свежая выпечка собственного производства.",
                "г. Самара, ул. Молодогвардейская, д. 187",
                "+7 (846) 234-56-78",
                "info@mercury-trade.ru",
                "mercury-trade.ru"
            ),
            listOf(
                "ИП Сервисный центр «Мастер»",
                "Услуги",
                "Ремонт бытовой техники и электроники: холодильники, стиральные машины, телевизоры, смартфоны, планшеты. Выезд мастера на дом в день обращения. Гарантия на все виды работ — 12 месяцев.",
                "г. Самара, ул. Ново-Садовая, д. 106",
                "+7 (846) 300-12-34",
                "master@service-samara.ru",
                "master-service-samara.ru"
            ),
            listOf(
                "ООО «ЭкоСтрой»",
                "Производство",
                "Производство экологичных строительных материалов: кирпич, газобетонные блоки, утеплители из натуральных материалов. Сертифицированная продукция соответствует ГОСТ. Доставка по Самарской области.",
                "г. Тольятти, ул. Автозаводская, д. 55",
                "+7 (848) 450-78-90",
                "info@ecostroy-tlt.ru",
                "ecostroy-tlt.ru"
            ),
            listOf(
                "Кафе «Уют»",
                "Общепит",
                "Семейное кафе с уютной атмосферой и домашней кухней. Банкетный зал на 60 человек, бизнес-ланчи с 11 до 15, доставка блюд по центру города. Детское меню и специальное оформление праздников.",
                "г. Самара, пр. Карла Маркса, д. 210",
                "+7 (846) 100-22-33",
                "cafe.uyut@mail.ru",
                ""
            ),
            listOf(
                "ООО «ДигиталСофт»",
                "IT",
                "Разработка мобильных приложений (iOS, Android) и корпоративных веб-систем. Команда 35+ специалистов. Реализованы проекты для крупных предприятий Самарской области, банковского и страхового секторов.",
                "г. Самара, ул. Лесная, д. 23, оф. 401",
                "+7 (846) 555-77-88",
                "hello@digitalsoft-samara.ru",
                "digitalsoft-samara.ru"
            ),
            listOf(
                "ООО «Клининг Профи»",
                "Услуги",
                "Профессиональная уборка офисных и торговых помещений, жилых квартир. Химчистка мебели и ковров, мойка фасадных окон, уборка после ремонта. Штат 50+ сотрудников, экологически безопасные средства.",
                "г. Самара, ул. Революционная, д. 70",
                "+7 (846) 220-33-44",
                "info@cleaning-profi.ru",
                "cleaning-profi.ru"
            )
        )

        testEnterprises.forEach { e ->
            Enterprises.insert {
                it[id] = UUID.randomUUID().toString()
                it[name] = e[0]
                it[specialization] = e[1]
                it[description] = e[2]
                it[address] = e[3]
                it[phone] = e[4]
                it[email] = e[5]
                it[website] = e[6]
                it[createdAt] = now
            }
        }
    }
}
