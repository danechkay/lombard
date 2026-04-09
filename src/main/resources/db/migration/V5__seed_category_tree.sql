-- Main categories
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Транспорт', 'transport', NULL, 10
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'transport');

INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Электроника', 'electronics', NULL, 20
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'electronics');

INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Личные вещи', 'personal-items', NULL, 30
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'personal-items');

INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Дом и дача', 'home-garden', NULL, 40
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'home-garden');

INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Запчасти и аксессуары', 'parts-accessories', NULL, 50
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'parts-accessories');

INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Товары для детей', 'kids', NULL, 60
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'kids');

INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Хобби и отдых', 'hobby-leisure', NULL, 70
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'hobby-leisure');

INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Животные', 'animals', NULL, 80
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'animals');

INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Для бизнеса', 'business', NULL, 90
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'business');

INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Услуги', 'services', NULL, 100
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'services');

-- Transport children
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Автомобили', 'transport-cars', c.id, 1 FROM categories c
WHERE c.slug = 'transport' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'transport-cars');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Мотоциклы', 'transport-motorcycles', c.id, 2 FROM categories c
WHERE c.slug = 'transport' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'transport-motorcycles');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Грузовики и спецтехника', 'transport-trucks', c.id, 3 FROM categories c
WHERE c.slug = 'transport' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'transport-trucks');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Водный транспорт', 'transport-water', c.id, 4 FROM categories c
WHERE c.slug = 'transport' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'transport-water');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Велосипеды', 'transport-bicycles', c.id, 5 FROM categories c
WHERE c.slug = 'transport' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'transport-bicycles');

-- Electronics children
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Телефоны', 'electronics-phones', c.id, 1 FROM categories c
WHERE c.slug = 'electronics' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'electronics-phones');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Ноутбуки', 'electronics-laptops', c.id, 2 FROM categories c
WHERE c.slug = 'electronics' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'electronics-laptops');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Настольные компьютеры', 'electronics-pc', c.id, 3 FROM categories c
WHERE c.slug = 'electronics' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'electronics-pc');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Игровые приставки', 'electronics-consoles', c.id, 4 FROM categories c
WHERE c.slug = 'electronics' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'electronics-consoles');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Планшеты и электронные книги', 'electronics-tablets', c.id, 5 FROM categories c
WHERE c.slug = 'electronics' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'electronics-tablets');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Фото и видео', 'electronics-photo-video', c.id, 6 FROM categories c
WHERE c.slug = 'electronics' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'electronics-photo-video');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Аудио и видеотехника', 'electronics-audio-video', c.id, 7 FROM categories c
WHERE c.slug = 'electronics' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'electronics-audio-video');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Бытовая техника', 'electronics-home-appliances', c.id, 8 FROM categories c
WHERE c.slug = 'electronics' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'electronics-home-appliances');

-- Personal items children
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Одежда', 'personal-clothes', c.id, 1 FROM categories c
WHERE c.slug = 'personal-items' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'personal-clothes');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Обувь', 'personal-shoes', c.id, 2 FROM categories c
WHERE c.slug = 'personal-items' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'personal-shoes');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Часы и украшения', 'personal-jewelry', c.id, 3 FROM categories c
WHERE c.slug = 'personal-items' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'personal-jewelry');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Красота и здоровье', 'personal-beauty', c.id, 4 FROM categories c
WHERE c.slug = 'personal-items' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'personal-beauty');

-- Home and garden children
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Мебель и интерьер', 'home-furniture', c.id, 1 FROM categories c
WHERE c.slug = 'home-garden' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'home-furniture');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Бытовая техника для дома', 'home-tech', c.id, 2 FROM categories c
WHERE c.slug = 'home-garden' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'home-tech');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Ремонт и строительство', 'home-repair', c.id, 3 FROM categories c
WHERE c.slug = 'home-garden' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'home-repair');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Посуда и товары для кухни', 'home-kitchen', c.id, 4 FROM categories c
WHERE c.slug = 'home-garden' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'home-kitchen');

-- Parts and accessories children
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Запчасти для автомобилей', 'parts-cars', c.id, 1 FROM categories c
WHERE c.slug = 'parts-accessories' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'parts-cars');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Шины, диски и колёса', 'parts-wheels', c.id, 2 FROM categories c
WHERE c.slug = 'parts-accessories' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'parts-wheels');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Аксессуары', 'parts-accessories-sub', c.id, 3 FROM categories c
WHERE c.slug = 'parts-accessories' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'parts-accessories-sub');

-- Kids children
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Детская одежда и обувь', 'kids-clothes', c.id, 1 FROM categories c
WHERE c.slug = 'kids' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'kids-clothes');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Товары для малышей', 'kids-baby', c.id, 2 FROM categories c
WHERE c.slug = 'kids' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'kids-baby');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Игрушки', 'kids-toys', c.id, 3 FROM categories c
WHERE c.slug = 'kids' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'kids-toys');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Детский транспорт', 'kids-transport', c.id, 4 FROM categories c
WHERE c.slug = 'kids' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'kids-transport');

-- Hobby children
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Билеты и путешествия', 'hobby-travel', c.id, 1 FROM categories c
WHERE c.slug = 'hobby-leisure' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'hobby-travel');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Велосипеды и спорт', 'hobby-sport', c.id, 2 FROM categories c
WHERE c.slug = 'hobby-leisure' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'hobby-sport');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Музыкальные инструменты', 'hobby-music', c.id, 3 FROM categories c
WHERE c.slug = 'hobby-leisure' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'hobby-music');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Книги и журналы', 'hobby-books', c.id, 4 FROM categories c
WHERE c.slug = 'hobby-leisure' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'hobby-books');

-- Animals children
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Собаки', 'animals-dogs', c.id, 1 FROM categories c
WHERE c.slug = 'animals' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'animals-dogs');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Кошки', 'animals-cats', c.id, 2 FROM categories c
WHERE c.slug = 'animals' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'animals-cats');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Птицы', 'animals-birds', c.id, 3 FROM categories c
WHERE c.slug = 'animals' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'animals-birds');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Товары для животных', 'animals-goods', c.id, 4 FROM categories c
WHERE c.slug = 'animals' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'animals-goods');

-- Business children
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Готовый бизнес', 'business-ready', c.id, 1 FROM categories c
WHERE c.slug = 'business' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'business-ready');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Оборудование для бизнеса', 'business-equipment', c.id, 2 FROM categories c
WHERE c.slug = 'business' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'business-equipment');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Коммерческий транспорт', 'business-transport', c.id, 3 FROM categories c
WHERE c.slug = 'business' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'business-transport');

-- Services children
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Ремонт техники', 'services-repair-tech', c.id, 1 FROM categories c
WHERE c.slug = 'services' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'services-repair-tech');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Ремонт и строительство', 'services-repair-build', c.id, 2 FROM categories c
WHERE c.slug = 'services' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'services-repair-build');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Красота и здоровье', 'services-beauty', c.id, 3 FROM categories c
WHERE c.slug = 'services' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'services-beauty');
INSERT INTO categories (name, slug, parent_id, sort_order)
SELECT 'Деловые услуги', 'services-business', c.id, 4 FROM categories c
WHERE c.slug = 'services' AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'services-business');
