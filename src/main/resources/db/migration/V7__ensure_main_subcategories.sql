-- Гарантируем подкатегории для основных веток каталога.
-- В первую очередь: Электроника -> Телефоны/Ноутбуки/Компьютеры.

INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Электроника', 'electronics', NULL, 20
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'electronics');

INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Телефоны', 'electronics-phones', c.id, 1
FROM categories c
WHERE c.slug = 'electronics'
  AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'electronics-phones');

INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Ноутбуки', 'electronics-laptops', c.id, 2
FROM categories c
WHERE c.slug = 'electronics'
  AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'electronics-laptops');

INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Компьютеры', 'electronics-computers', c.id, 3
FROM categories c
WHERE c.slug = 'electronics'
  AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'electronics-computers');
