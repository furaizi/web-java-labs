# Схема БД Cosmo Cats (3NF)

База даних орієнтована на PostgreSQL 15+. Дані розбиті на окремі таблиці відповідно до третьої нормальної форми, щоб уникнути дублювання і аномалій при оновленні.

## Основні таблиці

**categories** - довідник категорій товарів.
Поля:

* id (bigint, PK, sequence category_seq)
* name (varchar(128), not null, unique)
* description (varchar(512))
* created_at, updated_at (timestamptz, default now())

**products** - товари, прив’язані до категорій.
Поля:

* id (bigint, PK, sequence product_seq)
* category_id (bigint, FK -> categories.id)
* sku (varchar(32), not null, unique)
* name (varchar(255), not null, unique в межах категорії)
* description (text)
* price_amount (numeric(12,2), not null, >= 0)
* currency_code (char(3), not null, ISO 4217)
* stock_quantity (integer, not null, >= 0)
* created_at, updated_at (timestamptz)

**customers** - покупці, винесені в окрему таблицю, щоб не дублювати дані в замовленнях.
Поля:

* id (bigint, PK, sequence customer_seq)
* email (varchar(128), not null, unique)
* full_name (varchar(255), not null)
* phone (varchar(32))
* created_at, updated_at (timestamptz)

**orders** - замовлення клієнтів.
Поля:

* id (bigint, PK, sequence order_seq)
* customer_id (bigint, FK -> customers.id)
* order_number (varchar(64), not null, unique)
* status (varchar(32), not null)
* currency_code (char(3), not null)
* total_amount (numeric(14,2), not null, >= 0)
* created_at, updated_at (timestamptz)

**order_items** - позиції в межах замовлення, що зв’язують замовлення з товарами.
Поля:

* id (bigint, PK, sequence order_item_seq)
* order_id (bigint, FK -> orders.id)
* product_id (bigint, FK -> products.id)
* quantity (integer, not null, > 0)
* unit_price (numeric(12,2), not null, >= 0)
* currency_code (char(3), not null)
* created_at (timestamptz)

## Зв’язки

* category 1->N products
* customer 1->N orders
* order 1->N order_items
* product 1->N order_items

## Індекси та обмеження

Унікальні обмеження: categories.name, products.sku, customers.email, orders.order_number, а також products(category_id, name).

Індекси для оптимізації JOIN та пошуку: products(category_id), products(name), orders(customer_id), order_items(order_id), order_items(product_id).

Додаткові перевірки: ціни та залишки не можуть бути від’ємними, quantity > 0, currency_code складається з 3 великих літер.

## Liquibase міграції

* db/changelog/db.changelog-master.yml - основний changelog
* db/changelog/001-init-schema.yml - створення послідовностей, таблиць і базових обмежень
* db/changelog/002-product-constraints.yml - унікальність назви товару в межах категорії та індекс по назві
