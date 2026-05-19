SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `Payment`;
DROP TABLE IF EXISTS `PasswordResetToken`;
DROP TABLE IF EXISTS `EmailVerificationCode`;
DROP TABLE IF EXISTS `Session`;
DROP TABLE IF EXISTS `SavedProduct`;
DROP TABLE IF EXISTS `Contact`;
DROP TABLE IF EXISTS `ItemCart`;
DROP TABLE IF EXISTS `OrderCart`;
DROP TABLE IF EXISTS `Voucher`;
DROP TABLE IF EXISTS `Order`;
DROP TABLE IF EXISTS `ProductVariant`;
DROP TABLE IF EXISTS `Product`;
DROP TABLE IF EXISTS `Brand`;
DROP TABLE IF EXISTS `OutBox`;
DROP TABLE IF EXISTS `User`;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `User` (
    `id` CHAR(36) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `dateOfBirth` DATE NOT NULL,
    `hashPassword` VARCHAR(255) NOT NULL,
    `status` ENUM('PENDING', 'ACTIVE', 'INACTIVE', 'BANNED') NOT NULL DEFAULT 'PENDING',
    `role` ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER',
    `email` VARCHAR(255) NOT NULL,
    `createdAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `user_email_unique` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `OutBox` (
    `id` CHAR(36) NOT NULL,
    `code` VARCHAR(255) NOT NULL,
    `status` ENUM('PENDING', 'PROCESSED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    `createdAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `expiresAt` DATETIME NULL,
    `usedAt` DATETIME NULL,
    `type` VARCHAR(100) NOT NULL,
    `userId` CHAR(36) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `outbox_userid_index` (`userId`),
    KEY `outbox_userid_type_status_createdat_index` (`userId`, `type`, `status`, `createdAt`),
    KEY `outbox_userid_type_code_usedat_expiresat_index` (`userId`, `type`, `code`, `usedAt`, `expiresAt`),
    CONSTRAINT `outbox_userid_foreign` FOREIGN KEY (`userId`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Brand` (
    `id` CHAR(36) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `userId` CHAR(36) NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `status` ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    `createdAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `brand_userid_index` (`userId`),
    CONSTRAINT `brand_userid_foreign` FOREIGN KEY (`userId`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Product` (
    `id` CHAR(36) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `brandId` CHAR(36) NOT NULL,
    `status` ENUM('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    `userId` CHAR(36) NOT NULL,
    `createdAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `category` ENUM('STORAGE_DEVICE', 'NETWORK_DEVICE', 'ACCESSORY') NOT NULL,
    PRIMARY KEY (`id`),
    KEY `product_brandid_index` (`brandId`),
    KEY `product_userid_index` (`userId`),
    CONSTRAINT `product_brandid_foreign` FOREIGN KEY (`brandId`) REFERENCES `Brand` (`id`),
    CONSTRAINT `product_userid_foreign` FOREIGN KEY (`userId`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `ProductVariant` (
    `id` CHAR(36) NOT NULL,
    `productId` CHAR(36) NOT NULL,
    `price` DECIMAL(12,2) NOT NULL,
    `imageUrl` VARCHAR(255) NOT NULL,
    `status` ENUM('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK') NOT NULL DEFAULT 'ACTIVE',
    `createdAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `sku` VARCHAR(255) NOT NULL,
    `quantity` INT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `productvariant_sku_unique` (`sku`),
    KEY `productvariant_productid_index` (`productId`),
    CONSTRAINT `productvariant_productid_foreign` FOREIGN KEY (`productId`) REFERENCES `Product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Order` (
    `id` CHAR(36) NOT NULL,
    `userId` CHAR(36) NOT NULL,
    `productId` CHAR(36) NOT NULL,
    `variantId` CHAR(36) NULL,
    `quantity` INT NOT NULL DEFAULT 1,
    `phone` VARCHAR(20) NULL,
    `address` VARCHAR(500) NULL,
    `customerName` VARCHAR(255) NULL,
    `email` VARCHAR(255) NULL,
    `note` VARCHAR(1000) NULL,
    `paymentMethod` VARCHAR(50) NULL,
    `voucherId` CHAR(36) NULL,
    `totalAmount` DECIMAL(12,2) NULL,
    `createdAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `status` ENUM('PENDING', 'CONFIRMED', 'SHIPPING', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    PRIMARY KEY (`id`),
    KEY `order_userid_index` (`userId`),
    KEY `order_productid_index` (`productId`),
    KEY `order_variantid_index` (`variantId`),
    CONSTRAINT `order_userid_foreign` FOREIGN KEY (`userId`) REFERENCES `User` (`id`),
    CONSTRAINT `order_productid_foreign` FOREIGN KEY (`productId`) REFERENCES `Product` (`id`),
    CONSTRAINT `order_variantid_foreign` FOREIGN KEY (`variantId`) REFERENCES `ProductVariant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Payment` (
    `id` CHAR(36) NOT NULL,
    `orderId` CHAR(36) NOT NULL,
    `userId` CHAR(36) NOT NULL,
    `amount` DECIMAL(12,2) NOT NULL,
    `accessKey` VARCHAR(255) NOT NULL,
    `partnerCode` VARCHAR(255) NOT NULL,
    `redirectUrl` VARCHAR(255) NOT NULL,
    `ipnUrl` VARCHAR(255) NOT NULL,
    `extraData` VARCHAR(255) NOT NULL,
    `requestType` VARCHAR(255) NOT NULL,
    `signature` VARCHAR(255) NOT NULL,
    `status` ENUM('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    `createdAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `payment_orderid_index` (`orderId`),
    KEY `payment_userid_index` (`userId`),
    CONSTRAINT `payment_orderid_foreign` FOREIGN KEY (`orderId`) REFERENCES `Order` (`id`),
    CONSTRAINT `payment_userid_foreign` FOREIGN KEY (`userId`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Voucher` (
    `id` CHAR(36) NOT NULL,
    `percent` DECIMAL(5,2) NOT NULL,
    `userId` CHAR(36) NOT NULL,
    `expTime` DATE NOT NULL,
    `createdAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `quantity` INT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `voucher_userid_index` (`userId`),
    CONSTRAINT `voucher_userid_foreign` FOREIGN KEY (`userId`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `OrderCart` (
    `id` CHAR(36) NOT NULL,
    `userId` CHAR(36) NOT NULL,
    `createdAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `ordercart_userid_unique` (`userId`),
    KEY `ordercart_userid_index` (`userId`),
    CONSTRAINT `ordercart_userid_foreign` FOREIGN KEY (`userId`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `ItemCart` (
    `id` CHAR(36) NOT NULL,
    `cartId` CHAR(36) NOT NULL,
    `productId` CHAR(36) NOT NULL,
    `variantId` CHAR(36) NULL,
    `quantity` INT NOT NULL,
    `createdAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `itemcart_cartid_productid_variantid_unique` (`cartId`, `productId`, `variantId`),
    KEY `itemcart_cartid_index` (`cartId`),
    KEY `itemcart_productid_index` (`productId`),
    KEY `itemcart_variantid_index` (`variantId`),
    CONSTRAINT `itemcart_cartid_foreign` FOREIGN KEY (`cartId`) REFERENCES `OrderCart` (`id`) ON DELETE CASCADE,
    CONSTRAINT `itemcart_productid_foreign` FOREIGN KEY (`productId`) REFERENCES `Product` (`id`),
    CONSTRAINT `itemcart_variantid_foreign` FOREIGN KEY (`variantId`) REFERENCES `ProductVariant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `SavedProduct` (
    `id` CHAR(36) NOT NULL,
    `userId` CHAR(36) NOT NULL,
    `productId` CHAR(36) NOT NULL,
    `quantity` INT NOT NULL,
    `createdAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `savedproduct_userid_index` (`userId`),
    KEY `savedproduct_productid_index` (`productId`),
    CONSTRAINT `savedproduct_userid_foreign` FOREIGN KEY (`userId`) REFERENCES `User` (`id`),
    CONSTRAINT `savedproduct_productid_foreign` FOREIGN KEY (`productId`) REFERENCES `Product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Contact` (
    `id` CHAR(36) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `content` TEXT NOT NULL,
    `createdAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Session` (
    `id` CHAR(36) NOT NULL,
    `hashRefreshToken` VARCHAR(255) NOT NULL,
    `userId` CHAR(36) NOT NULL,
    `ip` VARCHAR(45) NOT NULL,
    `createdAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `session_userid_index` (`userId`),
    KEY `session_userid_ip_createdat_index` (`userId`, `ip`, `createdAt`),
    CONSTRAINT `session_userid_foreign` FOREIGN KEY (`userId`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Seed data for storefront demo
-- Admin account:
--   email: admin@linhnamstore.local
--   password: Admin@123!
-- -----------------------------------------------------

INSERT INTO `User` (`id`, `name`, `dateOfBirth`, `hashPassword`, `status`, `role`, `email`)
VALUES
('11111111-1111-1111-1111-111111111111', 'LinhNamStore Admin', '1998-06-15', 'pbkdf2:120000:iM2RxQ8TJc0L0FI1xu3eyg==:ErcoDn/QVGXRqs/54LGi5b22XPrpI7zPGyx2q/3nVDU=', 'ACTIVE', 'ADMIN', 'admin@linhnamstore.local');

INSERT INTO `OrderCart` (`id`, `userId`)
VALUES
('c1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111');

INSERT INTO `Brand` (`id`, `name`, `userId`, `description`, `status`)
VALUES
('b1111111-1111-1111-1111-111111111111', 'Samsung',  '11111111-1111-1111-1111-111111111111', 'Consumer and professional SSD solutions.', 'ACTIVE'),
('b2222222-2222-2222-2222-222222222222', 'Western Digital', '11111111-1111-1111-1111-111111111111', 'HDD, NAS and enterprise storage devices.', 'ACTIVE'),
('b3333333-3333-3333-3333-333333333333', 'Synology', '11111111-1111-1111-1111-111111111111', 'NAS and data management platforms.', 'ACTIVE'),
('b4444444-4444-4444-4444-444444444444', 'TP-Link', '11111111-1111-1111-1111-111111111111', 'Networking equipment for home and office.', 'ACTIVE'),
('b5555555-5555-5555-5555-555555555555', 'SanDisk', '11111111-1111-1111-1111-111111111111', 'Flash storage and memory accessories.', 'ACTIVE');

INSERT INTO `Product` (`id`, `name`, `description`, `brandId`, `status`, `userId`, `category`)
VALUES
('p1111111-1111-1111-1111-111111111111', 'Samsung 990 PRO NVMe SSD', 'PCIe 4.0 NVMe SSD for gaming, workstation and creator workloads.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'STORAGE_DEVICE'),
('p2222222-2222-2222-2222-222222222222', 'WD Red Plus NAS HDD', 'Reliable NAS hard drive tuned for 24/7 multi-bay storage systems.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'STORAGE_DEVICE'),
('p3333333-3333-3333-3333-333333333333', 'Synology DiskStation DS923+', '4-bay NAS for backup, collaboration and private cloud workloads.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NETWORK_DEVICE'),
('p4444444-4444-4444-4444-444444444444', 'TP-Link Archer AX73 Wi-Fi 6 Router', 'Dual-band Wi-Fi 6 router for apartments, homes and small offices.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NETWORK_DEVICE'),
('p5555555-5555-5555-5555-555555555555', 'SanDisk Extreme Portable SSD', 'Portable USB-C SSD with high speed backup for travel and field work.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'STORAGE_DEVICE'),
('p6666666-6666-6666-6666-666666666666', 'TP-Link TL-SG108 Gigabit Switch', '8-port unmanaged switch for desktop networking and lab environments.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NETWORK_DEVICE'),
('p7777777-7777-7777-7777-777777777777', 'SanDisk Ultra microSDXC UHS-I', 'Memory card for cameras, phones, drones and mobile storage expansion.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'ACCESSORY'),
('p8888888-8888-8888-8888-888888888888', 'WD Elements Desktop External HDD', 'Desktop external hard drive for large media libraries and backups.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'STORAGE_DEVICE'),
('p9999999-9999-9999-9999-999999999999', 'Synology HAT3300 Plus NAS HDD', 'Synology validated hard drive line for dependable NAS deployments.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'STORAGE_DEVICE'),
('paaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Samsung BAR Plus USB 3.1 Flash Drive', 'Metal flash drive for quick document transfer and daily backup tasks.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'ACCESSORY');

INSERT INTO `ProductVariant` (`id`, `productId`, `price`, `imageUrl`, `status`, `sku`, `quantity`)
VALUES
('v1111111-1111-1111-1111-111111111111', 'p1111111-1111-1111-1111-111111111111', 3490000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-990PRO-1TB', 24),
('v1111111-1111-1111-1111-111111111112', 'p1111111-1111-1111-1111-111111111111', 5990000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-990PRO-2TB', 18),
('v1111111-1111-1111-1111-111111111113', 'p1111111-1111-1111-1111-111111111111', 11290000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-990PRO-4TB', 9),

('v2222222-2222-2222-2222-222222222221', 'p2222222-2222-2222-2222-222222222222', 2690000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDR-REDPLUS-4TB', 30),
('v2222222-2222-2222-2222-222222222222', 'p2222222-2222-2222-2222-222222222222', 3990000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDR-REDPLUS-6TB', 21),
('v2222222-2222-2222-2222-222222222223', 'p2222222-2222-2222-2222-222222222222', 6290000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDR-REDPLUS-10TB', 12),

('v3333333-3333-3333-3333-333333333331', 'p3333333-3333-3333-3333-333333333333', 16890000.00, 'https://images.unsplash.com/photo-1555617981-dac3880eac6e?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-DS923PLUS-4BAY', 7),
('v3333333-3333-3333-3333-333333333332', 'p3333333-3333-3333-3333-333333333333', 18990000.00, 'https://images.unsplash.com/photo-1555617981-dac3880eac6e?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-DS923PLUS-8GB', 5),

('v4444444-4444-4444-4444-444444444441', 'p4444444-4444-4444-4444-444444444444', 2890000.00, 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-AX73-AX5400', 16),
('v4444444-4444-4444-4444-444444444442', 'p4444444-4444-4444-4444-444444444444', 3290000.00, 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-AX73-AX5400-MESH', 8),

('v5555555-5555-5555-5555-555555555551', 'p5555555-5555-5555-5555-555555555555', 2490000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-EXTPSSD-1TB', 27),
('v5555555-5555-5555-5555-555555555552', 'p5555555-5555-5555-5555-555555555555', 3990000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-EXTPSSD-2TB', 14),
('v5555555-5555-5555-5555-555555555553', 'p5555555-5555-5555-5555-555555555555', 7490000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'OUT_OF_STOCK', 'SDK-EXTPSSD-4TB', 0),

('v6666666-6666-6666-6666-666666666661', 'p6666666-6666-6666-6666-666666666666', 690000.00, 'https://images.unsplash.com/photo-1617777938240-9a1d8e51a47d?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-SG108-METAL', 34),
('v6666666-6666-6666-6666-666666666662', 'p6666666-6666-6666-6666-666666666666', 890000.00, 'https://images.unsplash.com/photo-1617777938240-9a1d8e51a47d?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-SG108-RACKKIT', 11),

('v7777777-7777-7777-7777-777777777771', 'p7777777-7777-7777-7777-777777777777', 229000.00, 'https://images.unsplash.com/photo-1587033411391-5d9e51cce126?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-MICROSD-128GB', 42),
('v7777777-7777-7777-7777-777777777772', 'p7777777-7777-7777-7777-777777777777', 399000.00, 'https://images.unsplash.com/photo-1587033411391-5d9e51cce126?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-MICROSD-256GB', 28),
('v7777777-7777-7777-7777-777777777773', 'p7777777-7777-7777-7777-777777777777', 749000.00, 'https://images.unsplash.com/photo-1587033411391-5d9e51cce126?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-MICROSD-512GB', 17),

('v8888888-8888-8888-8888-888888888881', 'p8888888-8888-8888-8888-888888888888', 3190000.00, 'https://images.unsplash.com/photo-1619451681329-8f9f0b5f7329?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDE-ELEMENTS-8TB', 19),
('v8888888-8888-8888-8888-888888888882', 'p8888888-8888-8888-8888-888888888888', 4390000.00, 'https://images.unsplash.com/photo-1619451681329-8f9f0b5f7329?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDE-ELEMENTS-12TB', 10),

('v9999999-9999-9999-9999-999999999991', 'p9999999-9999-9999-9999-999999999999', 2490000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-HAT3300-4TB', 18),
('v9999999-9999-9999-9999-999999999992', 'p9999999-9999-9999-9999-999999999999', 3590000.00, 'https://images.unsplash.com/photo-1531492746076-161ca9bcad58?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-HAT3300-6TB', 12),

('vaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'paaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 259000.00, 'https://images.unsplash.com/photo-1580894732444-8ecded7900cd?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-BARPLUS-64GB', 48),
('vaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'paaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 389000.00, 'https://images.unsplash.com/photo-1580894732444-8ecded7900cd?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-BARPLUS-128GB', 40),
('vaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'paaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 699000.00, 'https://images.unsplash.com/photo-1580894732444-8ecded7900cd?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-BARPLUS-256GB', 22);

-- Additional storefront products to satisfy the 30+ product requirement.
INSERT INTO `Product` (`id`, `name`, `description`, `brandId`, `status`, `userId`, `category`)
VALUES
('p0000011-0000-0000-0000-000000000011', 'Samsung 870 EVO SATA SSD', 'Reliable SATA SSD for desktop and laptop upgrades.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'STORAGE_DEVICE'),
('p0000012-0000-0000-0000-000000000012', 'Samsung T7 Shield Portable SSD', 'Rugged portable SSD with USB-C connectivity.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'STORAGE_DEVICE'),
('p0000013-0000-0000-0000-000000000013', 'WD Blue SN580 NVMe SSD', 'Mainstream NVMe SSD for daily productivity builds.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'STORAGE_DEVICE'),
('p0000014-0000-0000-0000-000000000014', 'WD Black SN850X NVMe SSD', 'High performance gaming NVMe SSD.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'STORAGE_DEVICE'),
('p0000015-0000-0000-0000-000000000015', 'SanDisk Ultra Fit USB 3.2', 'Compact USB flash drive for laptops and media players.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'STORAGE_DEVICE'),
('p0000016-0000-0000-0000-000000000016', 'SanDisk Extreme PRO USB-C SSD', 'Fast external SSD for creators and mobile backup.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'STORAGE_DEVICE'),
('p0000017-0000-0000-0000-000000000017', 'Synology BeeDrive Portable Backup', 'Personal backup drive for computers and mobile devices.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'STORAGE_DEVICE'),
('p0000018-0000-0000-0000-000000000018', 'Synology SAT5210 SATA SSD', 'Enterprise SATA SSD for NAS cache and storage pools.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'STORAGE_DEVICE'),
('p0000019-0000-0000-0000-000000000019', 'WD My Passport Portable HDD', 'Portable HDD for simple encrypted backups.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'STORAGE_DEVICE'),
('p0000020-0000-0000-0000-000000000020', 'Samsung PM9A1 OEM NVMe SSD', 'OEM NVMe SSD for workstation builds.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'STORAGE_DEVICE'),
('p0000021-0000-0000-0000-000000000021', 'TP-Link Archer AX55 Wi-Fi 6 Router', 'Wi-Fi 6 router for home and small office coverage.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NETWORK_DEVICE'),
('p0000022-0000-0000-0000-000000000022', 'TP-Link Deco X50 Mesh Wi-Fi', 'Mesh Wi-Fi 6 kit for whole-home coverage.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NETWORK_DEVICE'),
('p0000023-0000-0000-0000-000000000023', 'TP-Link EAP610 Access Point', 'Ceiling mount Wi-Fi 6 access point for offices.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NETWORK_DEVICE'),
('p0000024-0000-0000-0000-000000000024', 'TP-Link TL-SG1016D Switch', '16-port unmanaged gigabit switch.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NETWORK_DEVICE'),
('p0000025-0000-0000-0000-000000000025', 'Synology RT6600ax Router', 'Tri-band Wi-Fi 6 router with advanced network controls.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NETWORK_DEVICE'),
('p0000026-0000-0000-0000-000000000026', 'SanDisk SD UHS-I Card Reader', 'USB card reader for SD and microSD workflows.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'ACCESSORY'),
('p0000027-0000-0000-0000-000000000027', 'TP-Link Cat6 Patch Cable', 'Durable Cat6 cable for gigabit network devices.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'ACCESSORY'),
('p0000028-0000-0000-0000-000000000028', 'Synology NAS Drive Tray', 'Replacement drive tray for selected NAS models.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'ACCESSORY'),
('p0000029-0000-0000-0000-000000000029', 'Samsung USB-C to USB-A Adapter', 'Compact adapter for USB-C storage devices.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'ACCESSORY'),
('p0000030-0000-0000-0000-000000000030', 'WD External Drive Case', 'Protective case for portable storage drives.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'ACCESSORY');

INSERT INTO `ProductVariant` (`id`, `productId`, `price`, `imageUrl`, `status`, `sku`, `quantity`)
VALUES
('v0000011-0000-0000-0000-000000000111', 'p0000011-0000-0000-0000-000000000011', 1590000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-870EVO-500GB', 26),
('v0000011-0000-0000-0000-000000000112', 'p0000011-0000-0000-0000-000000000011', 2490000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-870EVO-1TB', 18),
('v0000012-0000-0000-0000-000000000121', 'p0000012-0000-0000-0000-000000000012', 2790000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-T7SHIELD-1TB', 20),
('v0000012-0000-0000-0000-000000000122', 'p0000012-0000-0000-0000-000000000012', 4590000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-T7SHIELD-2TB', 13),
('v0000013-0000-0000-0000-000000000131', 'p0000013-0000-0000-0000-000000000013', 1390000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDB-SN580-500GB', 32),
('v0000013-0000-0000-0000-000000000132', 'p0000013-0000-0000-0000-000000000013', 2190000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDB-SN580-1TB', 24),
('v0000014-0000-0000-0000-000000000141', 'p0000014-0000-0000-0000-000000000014', 2990000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDB-SN850X-1TB', 15),
('v0000014-0000-0000-0000-000000000142', 'p0000014-0000-0000-0000-000000000014', 5190000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDB-SN850X-2TB', 9),
('v0000015-0000-0000-0000-000000000151', 'p0000015-0000-0000-0000-000000000015', 229000.00, 'https://images.unsplash.com/photo-1580894732444-8ecded7900cd?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-ULTRAFIT-64GB', 45),
('v0000015-0000-0000-0000-000000000152', 'p0000015-0000-0000-0000-000000000015', 379000.00, 'https://images.unsplash.com/photo-1580894732444-8ecded7900cd?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-ULTRAFIT-128GB', 39),
('v0000016-0000-0000-0000-000000000161', 'p0000016-0000-0000-0000-000000000016', 3590000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-EXPRO-1TB', 14),
('v0000016-0000-0000-0000-000000000162', 'p0000016-0000-0000-0000-000000000016', 6690000.00, 'https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-EXPRO-2TB', 8),
('v0000017-0000-0000-0000-000000000171', 'p0000017-0000-0000-0000-000000000017', 3290000.00, 'https://images.unsplash.com/photo-1619451681329-8f9f0b5f7329?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-BEEDRIVE-1TB', 16),
('v0000017-0000-0000-0000-000000000172', 'p0000017-0000-0000-0000-000000000017', 5290000.00, 'https://images.unsplash.com/photo-1619451681329-8f9f0b5f7329?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-BEEDRIVE-2TB', 10),
('v0000018-0000-0000-0000-000000000181', 'p0000018-0000-0000-0000-000000000018', 4890000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-SAT5210-960GB', 12),
('v0000018-0000-0000-0000-000000000182', 'p0000018-0000-0000-0000-000000000018', 8990000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-SAT5210-1920GB', 6),
('v0000019-0000-0000-0000-000000000191', 'p0000019-0000-0000-0000-000000000019', 1790000.00, 'https://images.unsplash.com/photo-1619451681329-8f9f0b5f7329?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDP-MYPASSPORT-2TB', 22),
('v0000019-0000-0000-0000-000000000192', 'p0000019-0000-0000-0000-000000000019', 2890000.00, 'https://images.unsplash.com/photo-1619451681329-8f9f0b5f7329?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDP-MYPASSPORT-4TB', 15),
('v0000020-0000-0000-0000-000000000201', 'p0000020-0000-0000-0000-000000000020', 2690000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-PM9A1-1TB', 17),
('v0000020-0000-0000-0000-000000000202', 'p0000020-0000-0000-0000-000000000020', 4690000.00, 'https://images.unsplash.com/photo-1591799265444-d66432b91588?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-PM9A1-2TB', 9),
('v0000021-0000-0000-0000-000000000211', 'p0000021-0000-0000-0000-000000000021', 1890000.00, 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-AX55-AX3000', 25),
('v0000021-0000-0000-0000-000000000212', 'p0000021-0000-0000-0000-000000000021', 2290000.00, 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-AX55-EXT', 12),
('v0000022-0000-0000-0000-000000000221', 'p0000022-0000-0000-0000-000000000022', 4290000.00, 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-DECOX50-2PK', 13),
('v0000022-0000-0000-0000-000000000222', 'p0000022-0000-0000-0000-000000000022', 5990000.00, 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-DECOX50-3PK', 7),
('v0000023-0000-0000-0000-000000000231', 'p0000023-0000-0000-0000-000000000023', 2390000.00, 'https://images.unsplash.com/photo-1617777938240-9a1d8e51a47d?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-EAP610-CEILING', 18),
('v0000023-0000-0000-0000-000000000232', 'p0000023-0000-0000-0000-000000000023', 2790000.00, 'https://images.unsplash.com/photo-1617777938240-9a1d8e51a47d?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-EAP610-POE', 10),
('v0000024-0000-0000-0000-000000000241', 'p0000024-0000-0000-0000-000000000024', 1390000.00, 'https://images.unsplash.com/photo-1617777938240-9a1d8e51a47d?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-SG1016D-16P', 21),
('v0000024-0000-0000-0000-000000000242', 'p0000024-0000-0000-0000-000000000024', 1690000.00, 'https://images.unsplash.com/photo-1617777938240-9a1d8e51a47d?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-SG1016D-RACK', 11),
('v0000025-0000-0000-0000-000000000251', 'p0000025-0000-0000-0000-000000000025', 7890000.00, 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-RT6600AX-STD', 8),
('v0000025-0000-0000-0000-000000000252', 'p0000025-0000-0000-0000-000000000025', 8490000.00, 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-RT6600AX-BUNDLE', 5),
('v0000026-0000-0000-0000-000000000261', 'p0000026-0000-0000-0000-000000000026', 249000.00, 'https://images.unsplash.com/photo-1587033411391-5d9e51cce126?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-READER-SD', 37),
('v0000026-0000-0000-0000-000000000262', 'p0000026-0000-0000-0000-000000000026', 349000.00, 'https://images.unsplash.com/photo-1587033411391-5d9e51cce126?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-READER-USBC', 28),
('v0000027-0000-0000-0000-000000000271', 'p0000027-0000-0000-0000-000000000027', 79000.00, 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-CAT6-1M', 90),
('v0000027-0000-0000-0000-000000000272', 'p0000027-0000-0000-0000-000000000027', 129000.00, 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-CAT6-3M', 70),
('v0000028-0000-0000-0000-000000000281', 'p0000028-0000-0000-0000-000000000028', 390000.00, 'https://images.unsplash.com/photo-1555617981-dac3880eac6e?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-TRAY-TYPE-D6', 18),
('v0000028-0000-0000-0000-000000000282', 'p0000028-0000-0000-0000-000000000028', 490000.00, 'https://images.unsplash.com/photo-1555617981-dac3880eac6e?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-TRAY-LOCKKIT', 14),
('v0000029-0000-0000-0000-000000000291', 'p0000029-0000-0000-0000-000000000029', 159000.00, 'https://images.unsplash.com/photo-1580894732444-8ecded7900cd?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-USBC-USBA-BLK', 60),
('v0000029-0000-0000-0000-000000000292', 'p0000029-0000-0000-0000-000000000029', 189000.00, 'https://images.unsplash.com/photo-1580894732444-8ecded7900cd?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-USBC-USBA-2PK', 42),
('v0000030-0000-0000-0000-000000000301', 'p0000030-0000-0000-0000-000000000030', 199000.00, 'https://images.unsplash.com/photo-1619451681329-8f9f0b5f7329?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-CASE-2TB', 33),
('v0000030-0000-0000-0000-000000000302', 'p0000030-0000-0000-0000-000000000030', 249000.00, 'https://images.unsplash.com/photo-1619451681329-8f9f0b5f7329?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-CASE-4TB', 25);
