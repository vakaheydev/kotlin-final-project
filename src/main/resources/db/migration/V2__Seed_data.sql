INSERT INTO products (name, description, price, stock, created_at, updated_at) VALUES
    ('Ноутбук Lenovo ThinkPad', 'Профессиональный ноутбук для работы', 89999.99, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Клавиатура Logitech MX Keys', 'Беспроводная клавиатура', 12999.99, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Мышь Logitech MX Master 3', 'Эргономичная беспроводная мышь', 8999.99, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Монитор Dell 27"', '4K монитор для профессионалов', 45999.99, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Наушники Sony WH-1000XM5', 'Наушники с шумоподавлением', 29999.99, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Веб-камера Logitech C920', 'Full HD веб-камера', 7999.99, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('USB-хаб Anker 7-портовый', 'Расширитель USB портов', 2999.99, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('SSD Samsung 1TB', 'Внешний SSD накопитель', 11999.99, 35, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Роутер TP-Link AX3000', 'Wi-Fi 6 роутер', 8999.99, 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Микрофон Blue Yeti', 'USB микрофон для стриминга', 13999.99, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;