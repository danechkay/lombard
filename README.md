# Ламбарда — интернет-магазин (комиссионный магазин)

Веб-приложение на **Java 17** и **Spring Boot 3** для комиссионного магазина «Ламбарда»: каталог товаров, корзина, заказы, админ-панель с разграничением прав.

## Технологии

- **Backend:** Spring Boot 3.2, Spring Security, Spring Data JPA
- **База данных:** MySQL (или PostgreSQL — см. `application-dev.yml`)
- **Миграции:** Flyway
- **Шаблоны:** Thymeleaf
- **Сборка:** Maven

## Роли

| Роль       | Возможности |
|------------|-------------|
| Гость      | Просмотр каталога и карточки товара. Кнопка «Купить» ведёт на страницу входа. |
| Пользователь | Всё то же + корзина, оформление заказа, личный кабинет, история заказов. |
| Менеджер   | Всё то же + админка: товары (CRUD), категории, публикация/снятие с публикации. |
| Администратор | Всё у менеджера + управление пользователями (блокировка, смена роли), управление заказами (смена статуса). |

## Запуск

### 1. База данных

Создайте БД и пользователя (например, MySQL):

```sql
CREATE DATABASE lombard CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'lombard'@'localhost' IDENTIFIED BY 'lombard';
GRANT ALL ON lombard.* TO 'lombard'@'localhost';
```

Параметры подключения задаются в `src/main/resources/application.yml` (по умолчанию: `jdbc:mysql://localhost:3306/lombard`, пользователь/пароль `lombard`).

### 2. Сборка и запуск

Требуется **Java 17** и **Maven 3.8+** (или запуск из IDE).

```bash
cd C:\Users\danechka\Desktop\lombard
mvn clean package -DskipTests
java -jar target/lombard-1.0.0-SNAPSHOT.jar
```

Или без сборки JAR:

```bash
mvn spring-boot:run
```

В IDE: запустите класс `ru.lombard.LombardApplication`.

Приложение будет доступно по адресу: **http://localhost:8080**

### 2.1 Запуск React-фронтенда (новый этап переноса)

Ниже подробная инструкция, если вы впервые работаете с React.

#### Шаг 1. Установить Node.js

1. Перейдите на сайт [https://nodejs.org](https://nodejs.org).
2. Скачайте версию **LTS** для Windows.
3. Установите с настройками по умолчанию.
4. Перезапустите терминал/IDE.
5. Проверьте установку:

```bash
node -v
npm -v
```

Если команды выводят версии, значит всё установлено корректно.

#### Шаг 2. Запустить backend (Spring Boot)

Из корня проекта:

```bash
cd C:\Users\danechka\Desktop\lombard
mvn spring-boot:run
```

Backend должен работать на `http://localhost:8080`.

#### Шаг 3. Установить зависимости React

Откройте второй терминал:

```bash
cd C:\Users\danechka\Desktop\lombard\frontend
npm install
```

#### Шаг 4. Запустить React в режиме разработки

```bash
npm run dev
```

Frontend будет доступен по адресу `http://localhost:5173`.

#### Шаг 5. Как работает связка frontend + backend

- React отправляет запросы на `/api/...`.
- Vite-прокси автоматически пересылает их на Spring Boot (`localhost:8080`).
- Авторизация сессией работает через cookie (`credentials: include`).
- Для входа используется Spring Security endpoint `/login`.
- Для выхода используется `/logout`.

#### Шаг 6. Что уже перенесено на React

- Каталог (`/`) с фильтрами и пагинацией.
- Карточка товара (`/product/:slug`).
- Корзина (`/cart`): список, изменение количества, удаление.
- Оформление заказа (`/checkout`).
- Авторизация (`/login`) и регистрация (`/register`).
- Личный кабинет (`/account`) и история заказов (`/account/orders`).
- Админка (`/admin`, `/admin/products`, `/admin/categories`, `/admin/orders`, `/admin/users`).

#### Шаг 7. Какие API добавлены в backend

- `GET /api/catalog`
- `GET /api/catalog/categories`
- `GET /api/catalog/product/{slug}`
- `GET /api/auth/me`
- `POST /api/auth/register`
- `GET /api/cart`
- `POST /api/cart/add`
- `POST /api/cart/update`
- `POST /api/cart/remove`
- `GET /api/order/checkout`
- `POST /api/order/place`
- `GET /api/account`
- `GET /api/account/orders`
- `GET /api/admin/products`
- `GET /api/admin/products/{id}`
- `POST /api/admin/products`
- `POST /api/admin/products/{id}`
- `POST /api/admin/products/{id}/publish`
- `POST /api/admin/products/{id}/unpublish`
- `GET /api/admin/categories`
- `POST /api/admin/categories`
- `POST /api/admin/categories/{id}`
- `POST /api/admin/categories/{id}/delete`
- `GET /api/admin/orders`
- `GET /api/admin/orders/{id}`
- `POST /api/admin/orders/{id}/status`
- `GET /api/admin/users`
- `POST /api/admin/users/{id}/block`
- `POST /api/admin/users/{id}/unblock`
- `POST /api/admin/users/{id}/role`

#### Шаг 8. Production-сборка React

```bash
cd C:\Users\danechka\Desktop\lombard\frontend
npm run build
```

Собранные файлы будут в папке `frontend/dist`.
На этапе разработки это не нужно, достаточно `npm run dev`.

### 3. Первый вход

При первом запуске создаются два пользователя (если БД пустая):

| Email               | Пароль   | Роль     |
|---------------------|----------|----------|
| admin@lombard.ru    | password | Администратор |
| manager@lombard.ru  | password | Менеджер |

**Рекомендуется сразу сменить пароли после первого входа.**

## Структура проекта

```
lombard/
├── src/main/java/ru/lombard/
│   ├── config/          # Security, WebMvc, DataInitializer, GlobalModelAdvice
│   ├── controller/      # Каталог, корзина, заказы, аккаунт, авторизация
│   ├── controller/admin/ # Админ: товары, категории, заказы, пользователи
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   └── service/
├── src/main/resources/
│   ├── db/migration/    # Flyway (схема БД)
│   ├── static/          # CSS, JS, изображения
│   └── templates/       # Thymeleaf (каталог, корзина, админка и т.д.)
├── application.yml
└── pom.xml
```

## Основные сценарии (по ТЗ)

1. **Публикация товара (Менеджер):** Вход → Администрирование → Товары → Добавить товар → заполнить форму, загрузить фото → Опубликовать (или сохранить черновик и опубликовать из списка).
2. **Покупка (Пользователь):** Вход → Каталог/поиск → В корзину → Корзина → Оформить заказ → подтверждение. Заказ создаётся со статусом «Новый», корзина очищается.
3. **Управление заказами (Админ):** Администрирование → Заказы → открыть заказ → изменить статус (Оплачен, Выдан, Отменён и т.д.).
4. **Управление пользователями (Админ):** Администрирование → Пользователи → блокировка, смена роли.

## Безопасность

- Пароли хранятся в виде BCrypt-хеша.
- Доступ к админ-разделам по ролям (Spring Security).
- CSRF-токены для форм включены по умолчанию.

## Критерии приёмки (Definition of Done)

- [x] Ролевая модель: админ видит админку и разделы пользователей/заказов, пользователь — нет.
- [x] Менеджер может создать товар с фото; товар отображается на сайте после публикации.
- [x] Авторизованный пользователь может добавить товар в корзину.
- [x] Корзина хранится в БД (между сессиями).
- [x] Оформление заказа создаёт запись в БД и очищает корзину.
- [x] README с описанием запуска и структуры.

---

Разработка по ТЗ интернет-магазина «Ламбарда». Все файлы проекта хранятся в папке `C:\Users\danechka\Desktop\lombard`.
