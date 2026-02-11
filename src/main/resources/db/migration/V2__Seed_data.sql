-- Seed data for initial testing and demo

-- Insert sample products
INSERT INTO products (name, description, price, stock) VALUES
    ('Ноутбук Lenovo ThinkPad', 'Профессиональный ноутбук для работы', 89999.99, 15),
    ('Клавиатура Logitech MX Keys', 'Беспроводная клавиатура', 12999.99, 30),
    ('Мышь Logitech MX Master 3', 'Эргономичная беспроводная мышь', 8999.99, 25),
    ('Монитор Dell 27"', '4K монитор для профессионалов', 45999.99, 10),
    ('Наушники Sony WH-1000XM5', 'Наушники с шумоподавлением', 29999.99, 20),
    ('Веб-камера Logitech C920', 'Full HD веб-камера', 7999.99, 40),
    ('USB-хаб Anker 7-портовый', 'Расширитель USB портов', 2999.99, 50),
    ('SSD Samsung 1TB', 'Внешний SSD накопитель', 11999.99, 35),
    ('Роутер TP-Link AX3000', 'Wi-Fi 6 роутер', 8999.99, 18),
    ('Микрофон Blue Yeti', 'USB микрофон для стриминга', 13999.99, 12)
ON CONFLICT DO NOTHING;

-- Note: Admin user will be created automatically by DataInitializer.kt on first run
-- Default admin credentials: admin@shop.com / admin123

