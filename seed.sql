-- =============================================================================
-- LINHNAMSTORE - ADDITIONAL HIGH-QUALITY SEED DATA (31+ PRODUCTS)
-- Categories: STORAGE_DEVICE, MEMORY_CARD
-- This file seeds 31 premium products, 54 variants, and 15 customer reviews.
-- =============================================================================

-- -----------------------------------------------------
-- 1. SEED PRODUCTS
-- -----------------------------------------------------
INSERT INTO `Product` (`id`, `name`, `description`, `brandId`, `status`, `userId`, `category`)
VALUES
-- === SYNOLOGY NAS & NETWORKING (NETWORK_DEVICE) ===
('p1000001-0000-0000-0000-000000000001', 'Synology DiskStation DS224+ (2-Bay)', 'Thiết bị lưu trữ mạng NAS Synology DS224+ trang bị CPU Intel 4 nhân, RAM 2GB DDR4, hỗ trợ 2 khay ổ cứng giúp sao lưu, quản lý dữ liệu an toàn và đồng bộ hóa đám mây cá nhân.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p1000001-0000-0000-0000-000000000002', 'Synology DiskStation DS923+ (4-Bay)', 'Thiết bị lưu trữ mạng NAS Synology DS923+ 4 khay ổ cứng, hỗ trợ nâng cấp cổng 10GbE, RAM 4GB ECC DDR4, giải pháp sao lưu chuyên nghiệp cho doanh nghiệp vừa và nhỏ.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p1000001-0000-0000-0000-000000000003', 'Synology DiskStation DS723+ (2-Bay)', 'NAS Synology DS723+ thiết kế 2 khay nhỏ gọn nhưng hiệu năng mạnh mẽ, hỗ trợ khe cắm M.2 NVMe SSD làm bộ nhớ đệm cache và mở rộng thêm khay lưu trữ thông qua DX517.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p1000001-0000-0000-0000-000000000004', 'Synology DiskStation DS1522+ (5-Bay)', 'Thiết bị lưu trữ mạng NAS Synology DS1522+ sở hữu 5 khay ổ cứng, CPU AMD Ryzen R1600 mạnh mẽ, RAM 8GB DDR4 ECC, giải pháp hoàn hảo cho hệ thống ảo hóa và backup.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p1000001-0000-0000-0000-000000000005', 'Synology DiskStation DS223j (2-Bay)', 'Giải pháp đám mây cá nhân tiết kiệm chi phí với NAS Synology DS223j 2 khay ổ cứng, CPU Realtek 4 nhân, RAM 1GB DDR4, lý tưởng để lưu trữ ảnh gia đình và tài liệu.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p1000001-0000-0000-0000-000000000006', 'Synology DiskStation DS423j (4-Bay)', 'Thiết bị lưu trữ mạng 4 khay giá rẻ NAS Synology DS423j, RAM 1GB DDR4, hỗ trợ dung lượng tối đa lên đến 64TB, giải pháp sao lưu dữ liệu gia đình vô cùng kinh tế.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p1000001-0000-0000-0000-000000000007', 'Synology DiskStation DS223 (2-Bay)', 'NAS Synology DS223 2 khay ổ cứng, CPU Realtek 1.7GHz, RAM 2GB DDR4, quản lý dữ liệu thông minh, đồng bộ file đa nền tảng và hỗ trợ chia sẻ dữ liệu nhóm an toàn.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p1000001-0000-0000-0000-000000000008', 'Synology DiskStation DS423+ (4-Bay)', 'NAS Synology DS423+ 4 khay ổ cứng hiệu năng cao, trang bị 2 khe cắm SSD M.2 NVMe tích hợp cho tính năng bộ nhớ đệm cache và công cụ chuyển mã video thời gian thực.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p1000001-0000-0000-0000-000000000009', 'Synology RackStation RS822+ (4-Bay)', 'Thiết bị lưu trữ mạng gắn tủ rack NAS Synology RS822+ 1U 4 khay, bộ vi xử lý AMD Ryzen, hỗ trợ ảo hóa doanh nghiệp, sao lưu hệ thống dữ liệu hiệu suất vượt trội.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),

-- === WESTERN DIGITAL STORAGE (STORAGE_DEVICE) ===
('p1000001-0000-0000-0000-000000000010', 'WD Red Plus NAS HDD 3.5" SATA', 'Ổ cứng HDD Western Digital Red Plus chuyên dụng cho hệ thống NAS từ 1 đến 8 khay, công nghệ NASware 3.0 giúp tối ưu hóa hiệu suất, giảm thiểu lỗi và tăng độ bền bỉ.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p1000001-0000-0000-0000-000000000011', 'WD Red Pro Enterprise NAS HDD 3.5"', 'Ổ cứng HDD WD Red Pro cao cấp cho doanh nghiệp lớn, tốc độ vòng quay 7200RPM, bộ nhớ đệm lớn, được thiết kế cho hệ thống NAS lên đến 24 khay chạy liên tục 24/7.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p1000001-0000-0000-0000-000000000012', 'WD Purple Surveillance HDD 3.5"', 'Ổ cứng chuyên dụng camera giám sát WD Purple hỗ trợ ghi hình đồng thời từ tối đa 64 camera HD, hoạt động bền bỉ trong môi trường NVR/DVR ghi video liên tục.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p1000001-0000-0000-0000-000000000013', 'WD Blue PC HDD 3.5" SATA III', 'Ổ cứng HDD Western Digital Blue 3.5 inch cung cấp giải pháp lưu trữ dữ liệu dung lượng lớn, ổn định và tiết kiệm điện năng cho máy tính để bàn văn phòng.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p1000001-0000-0000-0000-000000000014', 'WD Black Performance HDD 3.5"', 'Ổ cứng HDD Western Digital Black hiệu năng cao 7200RPM, bộ nhớ đệm lớn, tối ưu hóa tốc độ tải game và các tác vụ xử lý đồ họa, chỉnh sửa video nặng.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p1000001-0000-0000-0000-000000000015', 'WD Elements Portable External HDD', 'Ổ cứng di động gắn ngoài WD Elements thiết kế siêu mỏng nhẹ, dung lượng lưu trữ lớn, kết nối USB 3.0 tốc độ cao, giải pháp sao lưu nhanh gọn khi di chuyển.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p1000001-0000-0000-0000-000000000016', 'WD My Passport Ultra USB-C HDD', 'Ổ cứng di động WD My Passport Ultra kết nối USB-C hiện đại, vỏ kim loại sang trọng, tích hợp phần mềm mã hóa mật khẩu bảo vệ dữ liệu cá nhân an toàn tuyệt đối.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p1000001-0000-0000-0000-000000000017', 'WD Blue SN580 PCIe Gen4 NVMe SSD', 'Ổ cứng SSD WD Blue SN580 M.2 PCIe Gen4 x4 mang lại hiệu năng vượt trội cho người sáng tạo nội dung, tốc độ đọc lên tới 4150MB/s giúp tối ưu hóa tiến độ công việc.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p1000001-0000-0000-0000-000000000018', 'WD Black SN850X Gaming NVMe SSD', 'Ổ cứng SSD WD Black SN850X tốc độ cực khủng lên đến 7300MB/s, có tùy chọn heatsink tản nhiệt hầm hố, dòng SSD chuyên game hàng đầu cho PC và máy PS5.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),

-- === SAMSUNG SSD & STORAGE (STORAGE_DEVICE) ===
('p1000001-0000-0000-0000-000000000019', 'Samsung 990 PRO M.2 NVMe SSD', 'Ổ cứng SSD Samsung 990 PRO PCIe 4.0 hàng đầu thế giới với tốc độ đọc ghi lên tới 7450/6900 MB/s, kiểm soát nhiệt thông minh và tiết kiệm điện năng vượt trội.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p1000001-0000-0000-0000-000000000020', 'Samsung 980 PRO M.2 NVMe SSD', 'Ổ cứng SSD Samsung 980 PRO PCIe 4.0 tốc độ đọc 7000MB/s, tương thích hoàn hảo với PS5, giải pháp nâng cấp lưu trữ đáng tin cậy cho game thủ chuyên nghiệp.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p1000001-0000-0000-0000-000000000021', 'Samsung 870 EVO 2.5" SATA III SSD', 'Ổ cứng SSD Samsung 870 EVO 2.5 inch SATA III tốc độ đọc ghi tối đa băng thông đạt 560/530 MB/s, độ bền bỉ huyền thoại, nâng cấp tối ưu cho PC và Laptop cũ.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p1000001-0000-0000-0000-000000000022', 'Samsung T7 Shield Portable SSD', 'Ổ cứng di động SSD Samsung T7 Shield siêu bền bỉ chống bụi nước IP65, vỏ cao su chống va đập từ độ cao 3m, tốc độ truyền dữ liệu nhanh chóng lên tới 1050MB/s.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p1000001-0000-0000-0000-000000000023', 'Samsung T9 Portable SSD USB 3.2', 'Ổ cứng di động siêu tốc độ SSD Samsung T9 giao tiếp USB 3.2 Gen 2x2 cho tốc độ cực đại 2000MB/s, thiết kế vân da sang trọng đẳng cấp cho nhà làm phim chuyên nghiệp.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),

-- === TP-LINK NETWORKING (NETWORK_DEVICE) ===
('p1000001-0000-0000-0000-000000000024', 'TP-Link Archer AX73 Wi-Fi 6 Router', 'Bộ định tuyến Wi-Fi 6 băng tần kép TP-Link Archer AX73 tốc độ AX5400, trang bị 6 anten độ lợi cao, hỗ trợ MU-MIMO và tính năng bảo mật HomeShield tiên tiến.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p1000001-0000-0000-0000-000000000025', 'TP-Link Deco X50 Mesh Wi-Fi 6', 'Hệ thống Mesh Wi-Fi 6 TP-Link Deco X50 loại bỏ hoàn toàn vùng chết Wi-Fi trong nhà, tốc độ AX3000, quản lý thông minh qua ứng dụng Deco trực quan.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p1000001-0000-0000-0000-000000000026', 'TP-Link TL-SG108 Gigabit Switch', 'Bộ chia mạng Switch 8 cổng Gigabit vỏ thép chắc chắn TP-Link TL-SG108, thiết kế cắm là chạy, tiết kiệm điện năng thông minh, lý tưởng cho gia đình và văn phòng.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p1000001-0000-0000-0000-000000000027', 'TP-Link Omada ER605 VPN Router', 'Bộ định tuyến VPN Gigabit tích hợp Omada ER605 lý tưởng cho doanh nghiệp nhỏ, hỗ trợ nhiều cổng WAN load balancing, cấu hình tập trung an toàn qua Omada Cloud.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p1000001-0000-0000-0000-000000000028', 'TP-Link Deco BE85 Mesh Wi-Fi 7', 'Hệ thống Mesh Wi-Fi 7 cao cấp TP-Link Deco BE85 tốc độ không tưởng BE22000, trang bị cổng 10G kết nối siêu tốc, công nghệ Multi-Link Operation đỉnh cao.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),

-- === SANDISK STORAGE (STORAGE_DEVICE) ===
('p1000001-0000-0000-0000-000000000029', 'SanDisk Extreme Portable SSD V2', 'Ổ cứng di động SSD SanDisk Extreme V2 tốc độ đọc 1050MB/s ghi 1000MB/s, chống nước bụi IP55, thiết kế có lỗ móc khóa tiện lợi, đồng hành hoàn hảo trong mọi chuyến đi.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p1000001-0000-0000-0000-000000000030', 'SanDisk Extreme PRO USB 3.2 Drive', 'USB 3.2 SanDisk Extreme PRO thiết kế dạng trượt bằng kim loại sang trọng, tốc độ đọc lên tới 420MB/s như một ổ cứng SSD thực thụ, truyền file cực nhanh.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p1000001-0000-0000-0000-000000000031', 'SanDisk Ultra Dual Drive Go Type-C', 'USB 2-trong-1 SanDisk Ultra Dual Drive Go với đầu cắm xoay linh hoạt USB Type-C và Type-A, dễ dàng chuyển đổi dữ liệu giữa điện thoại, máy tính bảng và PC.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD');

-- -----------------------------------------------------
-- 2. SEED PRODUCT VARIANTS
-- -----------------------------------------------------
INSERT INTO `ProductVariant` (`id`, `productId`, `price`, `imageUrl`, `status`, `sku`, `quantity`)
VALUES
-- DS224+ Variants
('v1000001-0000-0000-0000-000000000011', 'p1000001-0000-0000-0000-000000000001', 8990000.00, 'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-DS224-2G', 15),
('v1000001-0000-0000-0000-000000000012', 'p1000001-0000-0000-0000-000000000001', 10490000.00, 'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-DS224-6G', 8),

-- DS923+ Variants
('v1000001-0000-0000-0000-000000000021', 'p1000001-0000-0000-0000-000000000002', 17490000.00, 'https://images.unsplash.com/photo-1597852074816-d933c7d2b988?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-DS923-4G', 12),
('v1000001-0000-0000-0000-000000000022', 'p1000001-0000-0000-0000-000000000002', 19990000.00, 'https://images.unsplash.com/photo-1597852074816-d933c7d2b988?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-DS923-8G', 5),

-- DS723+ Variants
('v1000001-0000-0000-0000-000000000031', 'p1000001-0000-0000-0000-000000000003', 13990000.00, 'https://images.unsplash.com/photo-1600132806370-bf17e65e942f?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-DS723-2G', 10),

-- DS1522+ Variants
('v1000001-0000-0000-0000-000000000041', 'p1000001-0000-0000-0000-000000000004', 22500000.00, 'https://images.unsplash.com/photo-1618066782299-1a74070a75d5?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-DS1522-8G', 6),

-- DS223j Variants
('v1000001-0000-0000-0000-000000000051', 'p1000001-0000-0000-0000-000000000005', 5290000.00, 'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-DS223J-1G', 20),

-- DS423j Variants
('v1000001-0000-0000-0000-000000000061', 'p1000001-0000-0000-0000-000000000006', 9290000.00, 'https://images.unsplash.com/photo-1597852074816-d933c7d2b988?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-DS423J-1G', 14),

-- DS223 Variants
('v1000001-0000-0000-0000-000000000071', 'p1000001-0000-0000-0000-000000000007', 7490000.00, 'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-DS223-2G', 18),

-- DS423+ Variants
('v1000001-0000-0000-0000-000000000081', 'p1000001-0000-0000-0000-000000000008', 15990000.00, 'https://images.unsplash.com/photo-1618066782299-1a74070a75d5?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-DS423P-2G', 9),

-- RS822+ Variants
('v1000001-0000-0000-0000-000000000091', 'p1000001-0000-0000-0000-000000000009', 29990000.00, 'https://images.unsplash.com/photo-1600132806370-bf17e65e942f?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-RS822-STD', 4),

-- WD Red Plus Variants
('v1000001-0000-0000-0000-000000000101', 'p1000001-0000-0000-0000-000000000010', 3490000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-REDPLUS-4TB', 35),
('v1000001-0000-0000-0000-000000000102', 'p1000001-0000-0000-0000-000000000010', 5190000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-REDPLUS-6TB', 25),
('v1000001-0000-0000-0000-000000000103', 'p1000001-0000-0000-0000-000000000010', 6890000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-REDPLUS-8TB', 18),

-- WD Red Pro Variants
('v1000001-0000-0000-0000-000000000111', 'p1000001-0000-0000-0000-000000000011', 7990000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-REDPRO-10TB', 15),
('v1000001-0000-0000-0000-000000000112', 'p1000001-0000-0000-0000-000000000011', 11500000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-REDPRO-14TB', 10),

-- WD Purple Variants
('v1000001-0000-0000-0000-000000000121', 'p1000001-0000-0000-0000-000000000012', 2190000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-PURPLE-4TB', 40),
('v1000001-0000-0000-0000-000000000122', 'p1000001-0000-0000-0000-000000000012', 3890000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-PURPLE-6TB', 22),

-- WD Blue Variants
('v1000001-0000-0000-0000-000000000131', 'p1000001-0000-0000-0000-000000000013', 1290000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-BLUE-2TB', 50),
('v1000001-0000-0000-0000-000000000132', 'p1000001-0000-0000-0000-000000000013', 2490000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-BLUE-4TB', 30),

-- WD Black Variants
('v1000001-0000-0000-0000-000000000141', 'p1000001-0000-0000-0000-000000000014', 3290000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-BLACK-2TB', 12),
('v1000001-0000-0000-0000-000000000142', 'p1000001-0000-0000-0000-000000000014', 5890000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-BLACK-4TB', 8),

-- WD Elements Variants
('v1000001-0000-0000-0000-000000000151', 'p1000001-0000-0000-0000-000000000015', 1890000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-ELEM-2TB', 45),
('v1000001-0000-0000-0000-000000000152', 'p1000001-0000-0000-0000-000000000015', 2990000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-ELEM-4TB', 30),

-- WD My Passport Variants
('v1000001-0000-0000-0000-000000000161', 'p1000001-0000-0000-0000-000000000016', 2590000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-PASSPORT-2TB', 28),
('v1000001-0000-0000-0000-000000000162', 'p1000001-0000-0000-0000-000000000016', 3990000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-PASSPORT-4TB', 15),

-- WD SN580 Variants
('v1000001-0000-0000-0000-000000000171', 'p1000001-0000-0000-0000-000000000017', 1790000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-SN580-1TB', 40),
('v1000001-0000-0000-0000-000000000172', 'p1000001-0000-0000-0000-000000000017', 3190000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-SN580-2TB', 22),

-- WD SN850X Variants
('v1000001-0000-0000-0000-000000000181', 'p1000001-0000-0000-0000-000000000018', 2990000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-SN850X-1TB', 25),
('v1000001-0000-0000-0000-000000000182', 'p1000001-0000-0000-0000-000000000018', 4990000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-SN850X-2TB', 14),

-- Samsung 990 PRO Variants
('v1000001-0000-0000-0000-000000000191', 'p1000001-0000-0000-0000-000000000019', 3290000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-990PRO-1TB', 30),
('v1000001-0000-0000-0000-000000000192', 'p1000001-0000-0000-0000-000000000019', 5690000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-990PRO-2TB', 20),

-- Samsung 980 PRO Variants
('v1000001-0000-0000-0000-000000000201', 'p1000001-0000-0000-0000-000000000020', 2490000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-980PRO-1TB', 24),
('v1000001-0000-0000-0000-000000000202', 'p1000001-0000-0000-0000-000000000020', 4590000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-980PRO-2TB', 15),

-- Samsung 870 EVO Variants
('v1000001-0000-0000-0000-000000000211', 'p1000001-0000-0000-0000-000000000021', 1690000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-870EVO-500G', 35),
('v1000001-0000-0000-0000-000000000212', 'p1000001-0000-0000-0000-000000000021', 2790000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-870EVO-1TB', 22),

-- Samsung T7 Shield Variants
('v1000001-0000-0000-0000-000000000221', 'p1000001-0000-0000-0000-000000000022', 2890000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-T7SHIELD-1TB', 18),
('v1000001-0000-0000-0000-000000000222', 'p1000001-0000-0000-0000-000000000022', 4890000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-T7SHIELD-2TB', 12),

-- Samsung T9 Variants
('v1000001-0000-0000-0000-000000000231', 'p1000001-0000-0000-0000-000000000023', 4590000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-T9-1TB', 10),
('v1000001-0000-0000-0000-000000000232', 'p1000001-0000-0000-0000-000000000023', 7890000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-T9-2TB', 6),

-- TP-Link Archer AX73 Variants
('v1000001-0000-0000-0000-000000000241', 'p1000001-0000-0000-0000-000000000024', 2790000.00, 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-AX73-SINGLE', 25),

-- TP-Link Deco X50 Variants
('v1000001-0000-0000-0000-000000000251', 'p1000001-0000-0000-0000-000000000025', 4190000.00, 'https://images.unsplash.com/photo-1600132806608-231446b2e7af?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-DECOX50-2P', 15),
('v1000001-0000-0000-0000-000000000252', 'p1000001-0000-0000-0000-000000000025', 5990000.00, 'https://images.unsplash.com/photo-1600132806608-231446b2e7af?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-DECOX50-3P', 8),

-- TP-Link TL-SG108 Variants
('v1000001-0000-0000-0000-000000000261', 'p1000001-0000-0000-0000-000000000026', 590000.00, 'https://images.unsplash.com/photo-1597852074816-d933c7d2b988?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-SG108-STEEL', 50),

-- TP-Link ER605 Variants
('v1000001-0000-0000-0000-000000000271', 'p1000001-0000-0000-0000-000000000027', 1490000.00, 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-ER605-STD', 16),

-- TP-Link Deco BE85 Variants
('v1000001-0000-0000-0000-000000000281', 'p1000001-0000-0000-0000-000000000028', 18900000.00, 'https://images.unsplash.com/photo-1600132806608-231446b2e7af?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-BE85-2P', 5),

-- SanDisk Extreme Variants
('v1000001-0000-0000-0000-000000000291', 'p1000001-0000-0000-0000-000000000029', 2390000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-EXT-1TB', 30),
('v1000001-0000-0000-0000-000000000292', 'p1000001-0000-0000-0000-000000000029', 3990000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-EXT-2TB', 18),

-- SanDisk Extreme PRO Flash Variants
('v1000001-0000-0000-0000-000000000301', 'p1000001-0000-0000-0000-000000000030', 790000.00, 'https://images.unsplash.com/photo-1601524909162-be87252be298?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-EXTPRO-128G', 45),
('v1000001-0000-0000-0000-000000000302', 'p1000001-0000-0000-0000-000000000030', 1390000.00, 'https://images.unsplash.com/photo-1601524909162-be87252be298?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-EXTPRO-256G', 28),

-- SanDisk Ultra Dual Go Variants
('v1000001-0000-0000-0000-000000000311', 'p1000001-0000-0000-0000-000000000031', 280000.00, 'https://images.unsplash.com/photo-1601524909162-be87252be298?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-DUALGO-64G', 60),
('v1000001-0000-0000-0000-000000000312', 'p1000001-0000-0000-0000-000000000031', 450000.00, 'https://images.unsplash.com/photo-1601524909162-be87252be298?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-DUALGO-128G', 40);

-- -----------------------------------------------------
-- 3. SEED PRODUCT REVIEWS FOR POPULAR PRODUCTS
-- -----------------------------------------------------
INSERT INTO `ProductReview` (`id`, `productId`, `userId`, `rating`, `comment`, `status`, `createdAt`, `updatedAt`)
VALUES
-- DS224+ Reviews
('r1000001-0000-0000-0000-000000000001', 'p1000001-0000-0000-0000-000000000001', '22222222-2222-2222-2222-222222222222', 5, 'Thiết bị chạy cực êm, quản lý ảnh gia đình thông minh qua ứng dụng Synology Photos rất thích.', 'APPROVED', '2026-05-18 09:20:00', '2026-05-18 09:20:00'),
('r1000001-0000-0000-0000-000000000002', 'p1000001-0000-0000-0000-000000000001', '33333333-3333-3333-3333-333333333333', 4, 'Hàng chính hãng fullbox, bảo hành chu đáo. CPU Intel J4125 chạy Docker mượt mà.', 'APPROVED', '2026-05-19 14:15:00', '2026-05-19 14:15:00'),

-- DS923+ Reviews
('r1000001-0000-0000-0000-000000000003', 'p1000001-0000-0000-0000-000000000002', '44444444-4444-4444-4444-444444444444', 5, 'Giải pháp sao lưu hoàn hảo cho công ty thiết kế của mình. Cài đặt đơn giản, hỗ trợ RAID 5 an toàn.', 'APPROVED', '2026-05-20 11:30:00', '2026-05-20 11:30:00'),

-- WD Red Plus Reviews
('r1000001-0000-0000-0000-000000000004', 'p1000001-0000-0000-0000-000000000010', '55555555-5555-5555-5555-555555555555', 5, 'Ổ chuyên dụng NAS chạy mát mẻ, êm hơn hẳn dòng thường. Sẽ tiếp tục ủng hộ shop.', 'APPROVED', '2026-05-15 10:05:00', '2026-05-15 10:05:00'),
('r1000001-0000-0000-0000-000000000005', 'p1000001-0000-0000-0000-000000000010', '22222222-2222-2222-2222-222222222222', 4, 'Đã mua 2 ổ 4TB chạy RAID 1 trên NAS Synology, hiệu năng đọc ghi rất ổn định.', 'APPROVED', '2026-05-17 16:45:00', '2026-05-17 16:45:00'),

-- WD Black SN850X Reviews
('r1000001-0000-0000-0000-000000000006', 'p1000001-0000-0000-0000-000000000018', '33333333-3333-3333-3333-333333333333', 5, 'Tốc độ cực nhanh, lắp vào PS5 load game nhanh kinh khủng, khuyên mua dòng có heatsink nhé.', 'APPROVED', '2026-05-19 20:10:00', '2026-05-19 20:10:00'),

-- Samsung 990 PRO Reviews
('r1000001-0000-0000-0000-000000000007', 'p1000001-0000-0000-0000-000000000019', '44444444-4444-4444-4444-444444444444', 5, 'Ổ SSD xịn nhất hiện nay, tốc độ ghi chép dữ liệu khổng lồ vô cùng ấn tượng. Giá cao nhưng xứng tiền.', 'APPROVED', '2026-05-20 08:35:00', '2026-05-20 08:35:00'),

-- Samsung T7 Shield Reviews
('r1000001-0000-0000-0000-000000000008', 'p1000001-0000-0000-0000-000000000022', '55555555-5555-5555-5555-555555555555', 5, 'Vỏ cao su chống va đập siêu tốt, mình vô tình làm rơi từ bàn làm việc xuống đất mà không hề hấn gì.', 'APPROVED', '2026-05-18 15:40:00', '2026-05-18 15:40:00'),

-- TP-Link Archer AX73 Reviews
('r1000001-0000-0000-0000-000000000009', 'p1000001-0000-0000-0000-000000000024', '22222222-2222-2222-2222-222222222222', 4, 'Sóng wifi 6 phủ ngập tràn căn hộ 90m2 của mình, nhiều thiết bị kết nối cùng lúc vẫn không bị trễ.', 'APPROVED', '2026-05-16 11:25:00', '2026-05-16 11:25:00'),

-- SanDisk Extreme Reviews
('r1000001-0000-0000-0000-000000000010', 'p1000001-0000-0000-0000-000000000029', '33333333-3333-3333-3333-333333333333', 5, 'Nhỏ gọn như bao diêm, có thể treo vào móc khóa cực kỳ tiện lợi khi di chuyển.', 'APPROVED', '2026-05-19 09:50:00', '2026-05-19 09:50:00');
