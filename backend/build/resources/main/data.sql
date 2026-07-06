INSERT IGNORE INTO delivery_zones (id, pincode, is_active) VALUES (1, '700001', true);
INSERT IGNORE INTO delivery_zones (id, pincode, is_active) VALUES (2, '700019', true);
INSERT IGNORE INTO delivery_zones (id, pincode, is_active) VALUES (3, '700091', true);

INSERT IGNORE INTO products (id, name, description, price, stock, category, image_url) VALUES 
(1, 'Organic Bananas', 'Fresh bananas from local farms', 60.00, 100, 'Fresh Produce', 'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?q=80&w=300&auto=format&fit=crop'),
(2, 'Amul Butter', 'Classic salted butter', 54.00, 50, 'Cold Items', 'https://images.unsplash.com/photo-1588195538326-c5b1e9f80a1b?q=80&w=300&auto=format&fit=crop'),
(3, 'Brown Bread', 'Freshly baked whole wheat bread', 45.00, 30, 'Bakery', 'https://images.unsplash.com/photo-1509440159596-0249088772ff?q=80&w=300&auto=format&fit=crop'),
(4, 'Fresh Milk', 'Full cream daily fresh milk (1L)', 68.00, 200, 'Cold Items', 'https://images.unsplash.com/photo-1563636619-e9143da7973b?q=80&w=300&auto=format&fit=crop'),
(5, 'Farm Eggs', 'Pack of 6 organic brown eggs', 85.00, 40, 'Cold Items', 'https://images.unsplash.com/photo-1598965402089-897ce52e8355?q=80&w=300&auto=format&fit=crop'),
(6, 'Red Apples', 'Sweet and crunchy Washington apples', 120.00, 60, 'Fresh Produce', 'https://images.unsplash.com/photo-1567306301408-9b74779a11af?q=80&w=300&auto=format&fit=crop'),
(7, 'Coca Cola Can', 'Refreshing chilled beverage (300ml)', 40.00, 150, 'Beverages', 'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?q=80&w=300&auto=format&fit=crop'),
(8, 'Lays Classic Salted', 'Crispy potato chips (50g)', 20.00, 100, 'Snacks', 'https://images.unsplash.com/photo-1566478989037-eec170784d0b?q=80&w=300&auto=format&fit=crop'),
(9, 'Basmati Rice', 'Premium long grain rice (1kg)', 150.00, 40, 'Staples', 'https://images.unsplash.com/photo-1586201375761-83865001e31c?q=80&w=300&auto=format&fit=crop'),
(10, 'Onions', 'Fresh red onions (1kg)', 40.00, 80, 'Fresh Produce', 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?q=80&w=300&auto=format&fit=crop'),
(11, 'Tomatoes', 'Red juicy local tomatoes (1kg)', 30.00, 80, 'Fresh Produce', 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?q=80&w=300&auto=format&fit=crop'),
(12, 'Ice Cream Tub', 'Chocolate fudge ice cream (500ml)', 250.00, 20, 'Cold Items', 'https://images.unsplash.com/photo-1570197781417-0a5237500ed3?q=80&w=300&auto=format&fit=crop'),
(13, 'Orange Juice', '100% natural fruit juice (1L)', 110.00, 45, 'Beverages', 'https://images.unsplash.com/photo-1600271886742-f049cd451bba?q=80&w=300&auto=format&fit=crop'),
(14, 'Oreo Biscuits', 'Chocolate sandwich cookies', 35.00, 80, 'Snacks', 'https://images.unsplash.com/photo-1558961363-fa8fdf82db35?q=80&w=300&auto=format&fit=crop'),
(15, 'Aashirvaad Atta', 'Whole wheat flour (5kg)', 240.00, 30, 'Staples', 'https://images.unsplash.com/photo-1509440159596-0249088772ff?q=80&w=300&auto=format&fit=crop');
