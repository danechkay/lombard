-- Заполняем коды выдачи для уже существующих заказов,
-- которые находятся в "оплачен/готов к выдаче", но без кода.
UPDATE orders
SET pickup_code = SUBSTRING(UPPER(MD5(id::text || '-' || clock_timestamp()::text || '-' || random()::text)) FROM 1 FOR 8)
WHERE pickup_code IS NULL
  AND order_status IN ('PAID', 'SHIPPED');
