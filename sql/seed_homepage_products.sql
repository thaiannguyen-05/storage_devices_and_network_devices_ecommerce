-- Seed homepage products for storage ecommerce UI
-- Idempotent inserts: only add rows when ID does not exist.

INSERT INTO product (id, name, description, brandId, status, userId, createdAt, updatedAt, category)
SELECT 'home_prod_001', 'Ổ cứng Seagate IronWolf 16TB', 'Ổ cứng NAS chuyên dụng, độ bền cao', 'BRAND_SEAGATE', 'ACTIVE', 'SYSTEM', CURDATE(), CURDATE(), 'HDD'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE id = 'home_prod_001');

INSERT INTO product (id, name, description, brandId, status, userId, createdAt, updatedAt, category)
SELECT 'home_prod_002', 'Ổ cứng SSD Samsung 990 PRO 2TB', 'SSD NVMe PCIe Gen4 tốc độ cao', 'BRAND_SAMSUNG', 'ACTIVE', 'SYSTEM', CURDATE(), CURDATE(), 'SSD'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE id = 'home_prod_002');

INSERT INTO product (id, name, description, brandId, status, userId, createdAt, updatedAt, category)
SELECT 'home_prod_003', 'Thiết bị NAS Synology DS923+', 'NAS 4-bay cho doanh nghiệp và gia đình', 'BRAND_SYNOLOGY', 'ACTIVE', 'SYSTEM', CURDATE(), CURDATE(), 'NAS'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE id = 'home_prod_003');

INSERT INTO product (id, name, description, brandId, status, userId, createdAt, updatedAt, category)
SELECT 'home_prod_004', 'Ổ cứng WD Blue 2TB 3.5 SATA', 'HDD phổ thông cho desktop', 'BRAND_WD', 'ACTIVE', 'SYSTEM', CURDATE(), CURDATE(), 'HDD'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE id = 'home_prod_004');

INSERT INTO product (id, name, description, brandId, status, userId, createdAt, updatedAt, category)
SELECT 'home_prod_005', 'USB 3.2 SanDisk Ultra Flair 128GB', 'USB lưu trữ tốc độ cao', 'BRAND_SANDISK', 'ACTIVE', 'SYSTEM', CURDATE(), CURDATE(), 'USB'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE id = 'home_prod_005');

INSERT INTO product (id, name, description, brandId, status, userId, createdAt, updatedAt, category)
SELECT 'home_prod_006', 'Thẻ nhớ SanDisk Extreme 256GB', 'Thẻ nhớ cho camera và thiết bị di động', 'BRAND_SANDISK', 'ACTIVE', 'SYSTEM', CURDATE(), CURDATE(), 'MEMORY'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE id = 'home_prod_006');

INSERT INTO productvariant (id, productId, price, imageUrl, status, createdAt, updatedAt, sku, quantity)
SELECT 'home_var_001', 'home_prod_001', 6990000, 'https://images.unsplash.com/photo-1610214401212-111a58bdfb39?w=800', 'ACTIVE', CURDATE(), CURDATE(), 'SKU-HOME-001', 16
WHERE NOT EXISTS (SELECT 1 FROM productvariant WHERE id = 'home_var_001');

INSERT INTO productvariant (id, productId, price, imageUrl, status, createdAt, updatedAt, sku, quantity)
SELECT 'home_var_002', 'home_prod_002', 4990000, 'https://images.unsplash.com/photo-1593642634443-44adaa06623a?w=800', 'ACTIVE', CURDATE(), CURDATE(), 'SKU-HOME-002', 21
WHERE NOT EXISTS (SELECT 1 FROM productvariant WHERE id = 'home_var_002');

INSERT INTO productvariant (id, productId, price, imageUrl, status, createdAt, updatedAt, sku, quantity)
SELECT 'home_var_003', 'home_prod_003', 16490000, 'https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=800', 'ACTIVE', CURDATE(), CURDATE(), 'SKU-HOME-003', 8
WHERE NOT EXISTS (SELECT 1 FROM productvariant WHERE id = 'home_var_003');

INSERT INTO productvariant (id, productId, price, imageUrl, status, createdAt, updatedAt, sku, quantity)
SELECT 'home_var_004', 'home_prod_004', 1790000, 'https://images.unsplash.com/photo-1587135991058-8816e2e7f53d?w=800', 'ACTIVE', CURDATE(), CURDATE(), 'SKU-HOME-004', 32
WHERE NOT EXISTS (SELECT 1 FROM productvariant WHERE id = 'home_var_004');

INSERT INTO productvariant (id, productId, price, imageUrl, status, createdAt, updatedAt, sku, quantity)
SELECT 'home_var_005', 'home_prod_005', 290000, 'https://images.unsplash.com/photo-1583225157630-7f7ecb0d4b66?w=800', 'ACTIVE', CURDATE(), CURDATE(), 'SKU-HOME-005', 54
WHERE NOT EXISTS (SELECT 1 FROM productvariant WHERE id = 'home_var_005');

INSERT INTO productvariant (id, productId, price, imageUrl, status, createdAt, updatedAt, sku, quantity)
SELECT 'home_var_006', 'home_prod_006', 890000, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?w=800', 'ACTIVE', CURDATE(), CURDATE(), 'SKU-HOME-006', 40
WHERE NOT EXISTS (SELECT 1 FROM productvariant WHERE id = 'home_var_006');