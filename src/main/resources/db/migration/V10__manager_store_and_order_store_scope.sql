-- PostgreSQL (см. application.yml: jdbc:postgresql)

ALTER TABLE users
    ADD COLUMN store_id BIGINT NULL;

ALTER TABLE users
    ADD CONSTRAINT fk_users_store
        FOREIGN KEY (store_id) REFERENCES stores (id);

CREATE INDEX idx_users_store_id ON users (store_id);

UPDATE users u
SET store_id = s.id
FROM (
    SELECT id
    FROM stores
    WHERE active = TRUE
    ORDER BY sort_order ASC, id ASC
    LIMIT 1
) s
WHERE u.role = 'MANAGER'
  AND u.store_id IS NULL;

ALTER TABLE orders
    ADD COLUMN store_id BIGINT NULL;

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_store
        FOREIGN KEY (store_id) REFERENCES stores (id);

CREATE INDEX idx_orders_store_id ON orders (store_id);

UPDATE orders o
SET store_id = os.store_id
FROM (
    SELECT oi.order_id AS order_id, MIN(p.store_id) AS store_id
    FROM order_items oi
    JOIN products p ON p.id = oi.product_id
    GROUP BY oi.order_id
) os
WHERE os.order_id = o.id
  AND o.store_id IS NULL;

-- На случай заказов без позиций (аномалия): привязываем к первому магазину
UPDATE orders
SET store_id = (SELECT id FROM stores ORDER BY sort_order ASC, id ASC LIMIT 1)
WHERE store_id IS NULL;

ALTER TABLE orders
    ALTER COLUMN store_id SET NOT NULL;
