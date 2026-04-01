-- Удаляем ветки категорий, не относящиеся к ломбардному каталогу.
-- Сначала дочерние категории, затем корневые.
-- Защита: не удаляем категории, если на них есть товары.

WITH roots AS (
    SELECT id
    FROM categories
    WHERE slug IN ('personal-items', 'hobby-leisure', 'animals', 'kids', 'services')
),
children AS (
    SELECT c.id
    FROM categories c
    JOIN roots r ON c.parent_id = r.id
)
DELETE FROM categories c
WHERE c.id IN (SELECT id FROM children)
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.category_id = c.id);

WITH roots AS (
    SELECT id
    FROM categories
    WHERE slug IN ('personal-items', 'hobby-leisure', 'animals', 'kids', 'services')
)
DELETE FROM categories c
WHERE c.id IN (SELECT id FROM roots)
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.category_id = c.id)
  AND NOT EXISTS (SELECT 1 FROM categories ch WHERE ch.parent_id = c.id);
