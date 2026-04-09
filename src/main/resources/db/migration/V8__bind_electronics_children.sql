-- Жёстко гарантируем структуру:
-- Электроника -> Телефоны / Ноутбуки / Компьютеры

INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Электроника', 'electronics', NULL, 20
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'electronics');

-- Если подкатегории уже есть, но не привязаны к электронике — перепривязываем.
UPDATE categories c
SET parent_id = p.id, sort_order = 1
FROM categories p
WHERE p.slug = 'electronics'
  AND c.slug = 'electronics-phones';

UPDATE categories c
SET parent_id = p.id, sort_order = 2
FROM categories p
WHERE p.slug = 'electronics'
  AND c.slug = 'electronics-laptops';

UPDATE categories c
SET parent_id = p.id, sort_order = 3
FROM categories p
WHERE p.slug = 'electronics'
  AND c.slug IN ('electronics-computers', 'electronics-pc');

-- Если подкатегорий нет — создаём.
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Телефоны', 'electronics-phones', p.id, 1
FROM categories p
WHERE p.slug = 'electronics'
  AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'electronics-phones');

INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Ноутбуки', 'electronics-laptops', p.id, 2
FROM categories p
WHERE p.slug = 'electronics'
  AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'electronics-laptops');

INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Компьютеры', 'electronics-computers', p.id, 3
FROM categories p
WHERE p.slug = 'electronics'
  AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'electronics-computers');
