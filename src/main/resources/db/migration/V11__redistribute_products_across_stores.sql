-- Если все товары пока привязаны к одному магазину, распределяем их
-- по активным магазинам по кругу (для удобного старта мульти-магазинной модели).
-- Если товары уже распределены вручную (больше 1 магазина) — ничего не меняем.

DO $$
DECLARE
    active_store_count INT;
    distinct_product_store_count INT;
BEGIN
    SELECT COUNT(*) INTO active_store_count
    FROM stores
    WHERE active = TRUE;

    IF active_store_count <= 1 THEN
        RETURN;
    END IF;

    SELECT COUNT(DISTINCT store_id) INTO distinct_product_store_count
    FROM products;

    IF distinct_product_store_count <> 1 THEN
        RETURN;
    END IF;

    WITH ranked_products AS (
        SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
        FROM products
    ),
    ranked_stores AS (
        SELECT id, ROW_NUMBER() OVER (ORDER BY sort_order ASC, id ASC) AS rn
        FROM stores
        WHERE active = TRUE
    ),
    store_count AS (
        SELECT COUNT(*) AS cnt FROM ranked_stores
    )
    UPDATE products p
    SET store_id = rs.id
    FROM ranked_products rp
    CROSS JOIN store_count sc
    JOIN ranked_stores rs ON rs.rn = ((rp.rn - 1) % sc.cnt) + 1
    WHERE p.id = rp.id;
END $$;
