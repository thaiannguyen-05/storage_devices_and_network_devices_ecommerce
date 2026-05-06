-- Storefront seed data
-- Run this after tables are created from sto.sql
-- Admin account for foreign-key references:
--   email: admin@storeit.local
--   password: Admin@123!

START TRANSACTION;

INSERT INTO `User` (`id`, `name`, `dateOfBirth`, `hashPassword`, `status`, `role`, `email`, `createdAt`, `updatedAt`)
VALUES
('11111111-1111-1111-1111-111111111111', 'StoreIT Admin', '1998-06-15', 'pbkdf2:120000:iM2RxQ8TJc0L0FI1xu3eyg==:ErcoDn/QVGXRqs/54LGi5b22XPrpI7zPGyx2q/3nVDU=', 'ACTIVE', 'ADMIN', 'admin@storeit.local', NOW(), NOW())
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`dateOfBirth` = VALUES(`dateOfBirth`),
`status` = VALUES(`status`),
`role` = VALUES(`role`),
`updatedAt` = NOW();

INSERT INTO `OrderCart` (`id`, `userId`, `createdAt`, `updatedAt`)
VALUES
('c1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', NOW(), NOW())
ON DUPLICATE KEY UPDATE
`updatedAt` = NOW();

INSERT INTO `Brand` (`id`, `name`, `userId`, `description`, `status`, `createdAt`, `updatedAt`)
VALUES
('b1111111-1111-1111-1111-111111111111', 'Samsung', '11111111-1111-1111-1111-111111111111', 'Consumer and professional SSD solutions.', 'ACTIVE', NOW(), NOW()),
('b2222222-2222-2222-2222-222222222222', 'Western Digital', '11111111-1111-1111-1111-111111111111', 'HDD, NAS and enterprise storage devices.', 'ACTIVE', NOW(), NOW()),
('b3333333-3333-3333-3333-333333333333', 'Synology', '11111111-1111-1111-1111-111111111111', 'NAS and data management platforms.', 'ACTIVE', NOW(), NOW()),
('b4444444-4444-4444-4444-444444444444', 'TP-Link', '11111111-1111-1111-1111-111111111111', 'Networking equipment for home and office.', 'ACTIVE', NOW(), NOW()),
('b5555555-5555-5555-5555-555555555555', 'SanDisk', '11111111-1111-1111-1111-111111111111', 'Flash storage and memory accessories.', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`description` = VALUES(`description`),
`status` = VALUES(`status`),
`updatedAt` = NOW();

INSERT INTO `Product` (`id`, `name`, `description`, `brandId`, `status`, `userId`, `createdAt`, `updatedAt`, `category`)
VALUES
('p1111111-1111-1111-1111-111111111111', 'Samsung 990 PRO NVMe SSD', 'PCIe 4.0 NVMe SSD for gaming, workstation and creator workloads.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', NOW(), NOW(), 'STORAGE_DEVICE'),
('p2222222-2222-2222-2222-222222222222', 'WD Red Plus NAS HDD', 'Reliable NAS hard drive tuned for 24/7 multi-bay storage systems.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', NOW(), NOW(), 'STORAGE_DEVICE'),
('p3333333-3333-3333-3333-333333333333', 'Synology DiskStation DS923+', '4-bay NAS for backup, collaboration and private cloud workloads.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', NOW(), NOW(), 'NETWORK_DEVICE'),
('p4444444-4444-4444-4444-444444444444', 'TP-Link Archer AX73 Wi-Fi 6 Router', 'Dual-band Wi-Fi 6 router for apartments, homes and small offices.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', NOW(), NOW(), 'NETWORK_DEVICE'),
('p5555555-5555-5555-5555-555555555555', 'SanDisk Extreme Portable SSD', 'Portable USB-C SSD with high speed backup for travel and field work.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', NOW(), NOW(), 'STORAGE_DEVICE'),
('p6666666-6666-6666-6666-666666666666', 'TP-Link TL-SG108 Gigabit Switch', '8-port unmanaged switch for desktop networking and lab environments.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', NOW(), NOW(), 'NETWORK_DEVICE'),
('p7777777-7777-7777-7777-777777777777', 'SanDisk Ultra microSDXC UHS-I', 'Memory card for cameras, phones, drones and mobile storage expansion.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', NOW(), NOW(), 'ACCESSORY'),
('p8888888-8888-8888-8888-888888888888', 'WD Elements Desktop External HDD', 'Desktop external hard drive for large media libraries and backups.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', NOW(), NOW(), 'STORAGE_DEVICE'),
('p9999999-9999-9999-9999-999999999999', 'Synology HAT3300 Plus NAS HDD', 'Synology validated hard drive line for dependable NAS deployments.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', NOW(), NOW(), 'STORAGE_DEVICE'),
('paaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Samsung BAR Plus USB 3.1 Flash Drive', 'Metal flash drive for quick document transfer and daily backup tasks.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', NOW(), NOW(), 'ACCESSORY')
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`description` = VALUES(`description`),
`brandId` = VALUES(`brandId`),
`status` = VALUES(`status`),
`category` = VALUES(`category`),
`updatedAt` = NOW();

INSERT INTO `ProductVariant` (`id`, `productId`, `price`, `imageUrl`, `status`, `createdAt`, `updatedAt`, `sku`, `quantity`)
VALUES
('v1111111-1111-1111-1111-111111111111', 'p1111111-1111-1111-1111-111111111111', 2490000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SAM-990PRO-500GB', 20),
('v1111111-1111-1111-1111-111111111112', 'p1111111-1111-1111-1111-111111111111', 3490000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SAM-990PRO-1TB', 24),
('v1111111-1111-1111-1111-111111111113', 'p1111111-1111-1111-1111-111111111111', 5990000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SAM-990PRO-2TB', 18),
('v1111111-1111-1111-1111-111111111114', 'p1111111-1111-1111-1111-111111111111', 11290000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SAM-990PRO-4TB', 9),
('v1111111-1111-1111-1111-111111111115', 'p1111111-1111-1111-1111-111111111111', 12990000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'OUT_OF_STOCK', NOW(), NOW(), 'SAM-990PRO-4TB-HS', 0),

('v2222222-2222-2222-2222-222222222221', 'p2222222-2222-2222-2222-222222222222', 1990000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'WDR-REDPLUS-2TB', 26),
('v2222222-2222-2222-2222-222222222222', 'p2222222-2222-2222-2222-222222222222', 2690000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'WDR-REDPLUS-4TB', 30),
('v2222222-2222-2222-2222-222222222223', 'p2222222-2222-2222-2222-222222222222', 3990000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'WDR-REDPLUS-6TB', 21),
('v2222222-2222-2222-2222-222222222224', 'p2222222-2222-2222-2222-222222222222', 4990000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'WDR-REDPLUS-8TB', 15),
('v2222222-2222-2222-2222-222222222225', 'p2222222-2222-2222-2222-222222222222', 6290000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'WDR-REDPLUS-10TB', 12),

('v3333333-3333-3333-3333-333333333331', 'p3333333-3333-3333-3333-333333333333', 16890000.00, 'https://images.unsplash.com/photo-1555617981-dac3880eac6e?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SYN-DS923PLUS-4BAY', 7),
('v3333333-3333-3333-3333-333333333332', 'p3333333-3333-3333-3333-333333333333', 17990000.00, 'https://images.unsplash.com/photo-1555617981-dac3880eac6e?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SYN-DS923PLUS-4BAY-4GB', 6),
('v3333333-3333-3333-3333-333333333333', 'p3333333-3333-3333-3333-333333333333', 18990000.00, 'https://images.unsplash.com/photo-1555617981-dac3880eac6e?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SYN-DS923PLUS-8GB', 5),
('v3333333-3333-3333-3333-333333333334', 'p3333333-3333-3333-3333-333333333333', 20990000.00, 'https://images.unsplash.com/photo-1555617981-dac3880eac6e?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SYN-DS923PLUS-10GBE', 3),
('v3333333-3333-3333-3333-333333333335', 'p3333333-3333-3333-3333-333333333333', 22490000.00, 'https://images.unsplash.com/photo-1555617981-dac3880eac6e?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SYN-DS923PLUS-BUNDLE', 2),

('v4444444-4444-4444-4444-444444444441', 'p4444444-4444-4444-4444-444444444444', 2390000.00, 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'TPL-AX73-AX3000', 17),
('v4444444-4444-4444-4444-444444444442', 'p4444444-4444-4444-4444-444444444444', 2890000.00, 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'TPL-AX73-AX5400', 16),
('v4444444-4444-4444-4444-444444444443', 'p4444444-4444-4444-4444-444444444444', 3090000.00, 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'TPL-AX73-AX5400-MU', 13),
('v4444444-4444-4444-4444-444444444444', 'p4444444-4444-4444-4444-444444444444', 3290000.00, 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'TPL-AX73-AX5400-MESH', 8),
('v4444444-4444-4444-4444-444444444445', 'p4444444-4444-4444-4444-444444444444', 3590000.00, 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'TPL-AX73-AX5400-PRO', 5),

('v5555555-5555-5555-5555-555555555551', 'p5555555-5555-5555-5555-555555555555', 1790000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SDK-EXTPSSD-500GB', 25),
('v5555555-5555-5555-5555-555555555552', 'p5555555-5555-5555-5555-555555555555', 2490000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SDK-EXTPSSD-1TB', 27),
('v5555555-5555-5555-5555-555555555553', 'p5555555-5555-5555-5555-555555555555', 3990000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SDK-EXTPSSD-2TB', 14),
('v5555555-5555-5555-5555-555555555554', 'p5555555-5555-5555-5555-555555555555', 5690000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SDK-EXTPSSD-3TB', 6),
('v5555555-5555-5555-5555-555555555555', 'p5555555-5555-5555-5555-555555555555', 7490000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'OUT_OF_STOCK', NOW(), NOW(), 'SDK-EXTPSSD-4TB', 0),

('v6666666-6666-6666-6666-666666666661', 'p6666666-6666-6666-6666-666666666666', 690000.00, 'https://images.unsplash.com/photo-1617777938240-9a1d8e51a47d?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'TPL-SG108-METAL', 34),
('v6666666-6666-6666-6666-666666666662', 'p6666666-6666-6666-6666-666666666666', 790000.00, 'https://images.unsplash.com/photo-1617777938240-9a1d8e51a47d?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'TPL-SG108-DESK', 25),
('v6666666-6666-6666-6666-666666666663', 'p6666666-6666-6666-6666-666666666666', 890000.00, 'https://images.unsplash.com/photo-1617777938240-9a1d8e51a47d?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'TPL-SG108-RACKKIT', 11),
('v6666666-6666-6666-6666-666666666664', 'p6666666-6666-6666-6666-666666666666', 990000.00, 'https://images.unsplash.com/photo-1617777938240-9a1d8e51a47d?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'TPL-SG108-QOS', 9),
('v6666666-6666-6666-6666-666666666665', 'p6666666-6666-6666-6666-666666666666', 1190000.00, 'https://images.unsplash.com/photo-1617777938240-9a1d8e51a47d?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'TPL-SG108-SMART', 7),

('v7777777-7777-7777-7777-777777777771', 'p7777777-7777-7777-7777-777777777777', 129000.00, 'https://images.unsplash.com/photo-1587033411391-5d9e51cce126?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SDK-MICROSD-64GB', 55),
('v7777777-7777-7777-7777-777777777772', 'p7777777-7777-7777-7777-777777777777', 229000.00, 'https://images.unsplash.com/photo-1587033411391-5d9e51cce126?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SDK-MICROSD-128GB', 42),
('v7777777-7777-7777-7777-777777777773', 'p7777777-7777-7777-7777-777777777777', 399000.00, 'https://images.unsplash.com/photo-1587033411391-5d9e51cce126?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SDK-MICROSD-256GB', 28),
('v7777777-7777-7777-7777-777777777774', 'p7777777-7777-7777-7777-777777777777', 579000.00, 'https://images.unsplash.com/photo-1587033411391-5d9e51cce126?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SDK-MICROSD-400GB', 19),
('v7777777-7777-7777-7777-777777777775', 'p7777777-7777-7777-7777-777777777777', 749000.00, 'https://images.unsplash.com/photo-1587033411391-5d9e51cce126?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SDK-MICROSD-512GB', 17),

('v8888888-8888-8888-8888-888888888881', 'p8888888-8888-8888-8888-888888888888', 2290000.00, 'https://images.unsplash.com/photo-1619451681329-8f9f0b5f7329?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'WDE-ELEMENTS-4TB', 16),
('v8888888-8888-8888-8888-888888888882', 'p8888888-8888-8888-8888-888888888888', 2790000.00, 'https://images.unsplash.com/photo-1619451681329-8f9f0b5f7329?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'WDE-ELEMENTS-6TB', 14),
('v8888888-8888-8888-8888-888888888883', 'p8888888-8888-8888-8888-888888888888', 3190000.00, 'https://images.unsplash.com/photo-1619451681329-8f9f0b5f7329?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'WDE-ELEMENTS-8TB', 19),
('v8888888-8888-8888-8888-888888888884', 'p8888888-8888-8888-8888-888888888888', 3790000.00, 'https://images.unsplash.com/photo-1619451681329-8f9f0b5f7329?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'WDE-ELEMENTS-10TB', 11),
('v8888888-8888-8888-8888-888888888885', 'p8888888-8888-8888-8888-888888888888', 4390000.00, 'https://images.unsplash.com/photo-1619451681329-8f9f0b5f7329?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'WDE-ELEMENTS-12TB', 10),

('v9999999-9999-9999-9999-999999999991', 'p9999999-9999-9999-9999-999999999999', 1790000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SYN-HAT3300-2TB', 20),
('v9999999-9999-9999-9999-999999999992', 'p9999999-9999-9999-9999-999999999999', 2490000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SYN-HAT3300-4TB', 18),
('v9999999-9999-9999-9999-999999999993', 'p9999999-9999-9999-9999-999999999999', 3590000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SYN-HAT3300-6TB', 12),
('v9999999-9999-9999-9999-999999999994', 'p9999999-9999-9999-9999-999999999999', 4690000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SYN-HAT3300-8TB', 9),
('v9999999-9999-9999-9999-999999999995', 'p9999999-9999-9999-9999-999999999999', 5890000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SYN-HAT3300-10TB', 6),

('vaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'paaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 159000.00, 'https://images.unsplash.com/photo-1580894732444-8ecded7900cd?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SAM-BARPLUS-32GB', 60),
('vaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'paaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 259000.00, 'https://images.unsplash.com/photo-1580894732444-8ecded7900cd?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SAM-BARPLUS-64GB', 48),
('vaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'paaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 389000.00, 'https://images.unsplash.com/photo-1580894732444-8ecded7900cd?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SAM-BARPLUS-128GB', 40),
('vaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'paaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 529000.00, 'https://images.unsplash.com/photo-1580894732444-8ecded7900cd?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SAM-BARPLUS-200GB', 18),
('vaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 'paaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 699000.00, 'https://images.unsplash.com/photo-1580894732444-8ecded7900cd?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', NOW(), NOW(), 'SAM-BARPLUS-256GB', 22)
ON DUPLICATE KEY UPDATE
`productId` = VALUES(`productId`),
`price` = VALUES(`price`),
`imageUrl` = VALUES(`imageUrl`),
`status` = VALUES(`status`),
`quantity` = VALUES(`quantity`),
`updatedAt` = NOW();

COMMIT;
