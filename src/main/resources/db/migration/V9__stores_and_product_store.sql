CREATE TABLE stores (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    address VARCHAR(500) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO stores (name, slug, address, sort_order, active) VALUES
('Симферополь, ул. Гагарина, 1', 'simferopol-gagarina', 'ул. Ю. Гагарина, 1', 10, TRUE),
('Симферополь, пр. Кирова, 15', 'simferopol-kirova', 'пр-т Кирова, 15', 20, TRUE),
('Евпатория, ул. Революции, 7', 'evpatoria-rev', 'ул. Революции, 7', 30, TRUE);

ALTER TABLE products ADD COLUMN store_id BIGINT NULL;
UPDATE products SET store_id = (SELECT id FROM stores ORDER BY id LIMIT 1) WHERE store_id IS NULL;
ALTER TABLE products ALTER COLUMN store_id SET NOT NULL;
ALTER TABLE products
    ADD CONSTRAINT fk_products_store FOREIGN KEY (store_id) REFERENCES stores (id);
CREATE INDEX idx_products_store ON products (store_id);
