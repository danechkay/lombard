ALTER TABLE orders
    ADD COLUMN pickup_code VARCHAR(32) NULL;

CREATE INDEX idx_orders_pickup_code ON orders (pickup_code);
