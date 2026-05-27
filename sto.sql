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
DROP TABLE IF EXISTS `ProductReview`;
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
    `category` ENUM('HDD', 'SSD', 'USB', 'NAS', 'TAPE', 'ENCLOSURE', 'MEMORY_CARD') NOT NULL,
    PRIMARY KEY (`id`),
    KEY `product_brandid_index` (`brandId`),
    KEY `product_userid_index` (`userId`),
    CONSTRAINT `product_brandid_foreign` FOREIGN KEY (`brandId`) REFERENCES `Brand` (`id`),
    CONSTRAINT `product_userid_foreign` FOREIGN KEY (`userId`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `ProductReview` (
    `id` CHAR(36) NOT NULL,
    `productId` CHAR(36) NOT NULL,
    `userId` CHAR(36) NOT NULL,
    `rating` INT NOT NULL CHECK (`rating` BETWEEN 1 AND 5),
    `comment` VARCHAR(1000) NULL,
    `status` ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'APPROVED',
    `createdAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `productreview_userid_productid_unique` (`userId`, `productId`),
    KEY `productreview_product_status_createdat_index` (`productId`, `status`, `createdAt`),
    CONSTRAINT `productreview_productid_foreign` FOREIGN KEY (`productId`) REFERENCES `Product` (`id`) ON DELETE CASCADE,
    CONSTRAINT `productreview_userid_foreign` FOREIGN KEY (`userId`) REFERENCES `User` (`id`) ON DELETE CASCADE
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
('11111111-1111-1111-1111-111111111111', 'LinhNamStore Admin', '1998-06-15', 'pbkdf2:120000:iM2RxQ8TJc0L0FI1xu3eyg==:ErcoDn/QVGXRqs/54LGi5b22XPrpI7zPGyx2q/3nVDU=', 'ACTIVE', 'ADMIN', 'admin@linhnamstore.local'),
('22222222-2222-2222-2222-222222222222', 'Nguyễn Minh Anh', '1996-03-12', 'pbkdf2:120000:iM2RxQ8TJc0L0FI1xu3eyg==:ErcoDn/QVGXRqs/54LGi5b22XPrpI7zPGyx2q/3nVDU=', 'ACTIVE', 'USER', 'minhanh@linhnamstore.local'),
('33333333-3333-3333-3333-333333333333', 'Trần Quốc Huy', '1994-11-08', 'pbkdf2:120000:iM2RxQ8TJc0L0FI1xu3eyg==:ErcoDn/QVGXRqs/54LGi5b22XPrpI7zPGyx2q/3nVDU=', 'ACTIVE', 'USER', 'quochuy@linhnamstore.local'),
('44444444-4444-4444-4444-444444444444', 'Lê Hoàng Linh', '1999-07-24', 'pbkdf2:120000:iM2RxQ8TJc0L0FI1xu3eyg==:ErcoDn/QVGXRqs/54LGi5b22XPrpI7zPGyx2q/3nVDU=', 'ACTIVE', 'USER', 'hoanglinh@linhnamstore.local'),
('55555555-5555-5555-5555-555555555555', 'Phạm Thu Trang', '1997-01-30', 'pbkdf2:120000:iM2RxQ8TJc0L0FI1xu3eyg==:ErcoDn/QVGXRqs/54LGi5b22XPrpI7zPGyx2q/3nVDU=', 'ACTIVE', 'USER', 'thutrang@linhnamstore.local');

INSERT INTO `OrderCart` (`id`, `userId`)
VALUES
('c1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111'),
('c2222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222'),
('c3333333-3333-3333-3333-333333333333', '33333333-3333-3333-3333-333333333333'),
('c4444444-4444-4444-4444-444444444444', '44444444-4444-4444-4444-444444444444'),
('c5555555-5555-5555-5555-555555555555', '55555555-5555-5555-5555-555555555555');

INSERT INTO `Brand` (`id`, `name`, `userId`, `description`, `status`)
VALUES
('b1111111-1111-1111-1111-111111111111', 'Samsung',  '11111111-1111-1111-1111-111111111111', 'Consumer and professional SSD solutions.', 'ACTIVE'),
('b2222222-2222-2222-2222-222222222222', 'Western Digital', '11111111-1111-1111-1111-111111111111', 'HDD, NAS and enterprise storage devices.', 'ACTIVE'),
('b3333333-3333-3333-3333-333333333333', 'Synology', '11111111-1111-1111-1111-111111111111', 'NAS and data management platforms.', 'ACTIVE'),
('b4444444-4444-4444-4444-444444444444', 'TP-Link', '11111111-1111-1111-1111-111111111111', 'Networking equipment for home and office.', 'ACTIVE'),
('b5555555-5555-5555-5555-555555555555', 'SanDisk', '11111111-1111-1111-1111-111111111111', 'Flash storage and memory accessories.', 'ACTIVE');

INSERT INTO `Product` (`id`, `name`, `description`, `brandId`, `status`, `userId`, `category`)
VALUES
('p1111111-1111-1111-1111-111111111111', 'Samsung 990 PRO NVMe SSD', 'PCIe 4.0 NVMe SSD for gaming, workstation and creator workloads.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p2222222-2222-2222-2222-222222222222', 'WD Red Plus NAS HDD', 'Reliable NAS hard drive tuned for 24/7 multi-bay storage systems.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p3333333-3333-3333-3333-333333333333', 'Synology DiskStation DS923+', '4-bay NAS for backup, collaboration and private cloud workloads.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p4444444-4444-4444-4444-444444444444', 'TP-Link Archer AX73 Wi-Fi 6 Router', 'Dual-band Wi-Fi 6 router for apartments, homes and small offices.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p5555555-5555-5555-5555-555555555555', 'SanDisk Extreme Portable SSD', 'Portable USB-C SSD with high speed backup for travel and field work.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p6666666-6666-6666-6666-666666666666', 'TP-Link TL-SG108 Gigabit Switch', '8-port unmanaged switch for desktop networking and lab environments.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p7777777-7777-7777-7777-777777777777', 'SanDisk Ultra microSDXC UHS-I', 'Memory card for cameras, phones, drones and mobile storage expansion.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'MEMORY_CARD'),
('p8888888-8888-8888-8888-888888888888', 'WD Elements Desktop External HDD', 'Desktop external hard drive for large media libraries and backups.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p9999999-9999-9999-9999-999999999999', 'Synology HAT3300 Plus NAS HDD', 'Synology validated hard drive line for dependable NAS deployments.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('paaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Samsung BAR Plus USB 3.1 Flash Drive', 'Metal flash drive for quick document transfer and daily backup tasks.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'MEMORY_CARD');

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

('v9999999-9999-9999-9999-999999999991', 'p9999999-9999-9999-9999-999999999999', 2490000.00, 'https://images.unsplash.com/photo-1597173008730-f8b4ae7e368e?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-HAT3300-4TB', 18),
('v9999999-9999-9999-9999-999999999992', 'p9999999-9999-9999-9999-999999999999', 3590000.00, 'https://images.unsplash.com/photo-1597173008730-f8b4ae7e368e?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-HAT3300-6TB', 12),

('vaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'paaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 259000.00, 'https://images.unsplash.com/photo-1580894732444-8ecded7900cd?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-BARPLUS-64GB', 48),
('vaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'paaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 389000.00, 'https://images.unsplash.com/photo-1580894732444-8ecded7900cd?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-BARPLUS-128GB', 40),
('vaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'paaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 699000.00, 'https://images.unsplash.com/photo-1580894732444-8ecded7900cd?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-BARPLUS-256GB', 22);

-- Additional storefront products to satisfy the 30+ product requirement.
INSERT INTO `Product` (`id`, `name`, `description`, `brandId`, `status`, `userId`, `category`)
VALUES
('p0000011-0000-0000-0000-000000000011', 'Samsung 870 EVO SATA SSD', 'Reliable SATA SSD for desktop and laptop upgrades.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p0000012-0000-0000-0000-000000000012', 'Samsung T7 Shield Portable SSD', 'Rugged portable SSD with USB-C connectivity.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p0000013-0000-0000-0000-000000000013', 'WD Blue SN580 NVMe SSD', 'Mainstream NVMe SSD for daily productivity builds.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p0000014-0000-0000-0000-000000000014', 'WD Black SN850X NVMe SSD', 'High performance gaming NVMe SSD.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p0000015-0000-0000-0000-000000000015', 'SanDisk Ultra Fit USB 3.2', 'Compact USB flash drive for laptops and media players.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p0000016-0000-0000-0000-000000000016', 'SanDisk Extreme PRO USB-C SSD', 'Fast external SSD for creators and mobile backup.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p0000017-0000-0000-0000-000000000017', 'Synology BeeDrive Portable Backup', 'Personal backup drive for computers and mobile devices.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p0000018-0000-0000-0000-000000000018', 'Synology SAT5210 SATA SSD', 'Enterprise SATA SSD for NAS cache and storage pools.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p0000019-0000-0000-0000-000000000019', 'WD My Passport Portable HDD', 'Portable HDD for simple encrypted backups.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p0000020-0000-0000-0000-000000000020', 'Samsung PM9A1 OEM NVMe SSD', 'OEM NVMe SSD for workstation builds.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p0000021-0000-0000-0000-000000000021', 'TP-Link Archer AX55 Wi-Fi 6 Router', 'Wi-Fi 6 router for home and small office coverage.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p0000022-0000-0000-0000-000000000022', 'TP-Link Deco X50 Mesh Wi-Fi', 'Mesh Wi-Fi 6 kit for whole-home coverage.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p0000023-0000-0000-0000-000000000023', 'TP-Link EAP610 Access Point', 'Ceiling mount Wi-Fi 6 access point for offices.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p0000024-0000-0000-0000-000000000024', 'TP-Link TL-SG1016D Switch', '16-port unmanaged gigabit switch.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p0000025-0000-0000-0000-000000000025', 'Synology RT6600ax Router', 'Tri-band Wi-Fi 6 router with advanced network controls.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p0000026-0000-0000-0000-000000000026', 'SanDisk SD UHS-I Card Reader', 'USB card reader for SD and microSD workflows.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'MEMORY_CARD'),
('p0000027-0000-0000-0000-000000000027', 'TP-Link Cat6 Patch Cable', 'Durable Cat6 cable for gigabit network devices.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'MEMORY_CARD'),
('p0000028-0000-0000-0000-000000000028', 'Synology NAS Drive Tray', 'Replacement drive tray for selected NAS models.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'MEMORY_CARD'),
('p0000029-0000-0000-0000-000000000029', 'Samsung USB-C to USB-A Adapter', 'Compact adapter for USB-C storage devices.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'MEMORY_CARD'),
('p0000030-0000-0000-0000-000000000030', 'WD External Drive Case', 'Protective case for portable storage drives.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'MEMORY_CARD');

INSERT INTO `ProductVariant` (`id`, `productId`, `price`, `imageUrl`, `status`, `sku`, `quantity`)
VALUES
('v0000011-0000-0000-0000-000000000111', 'p0000011-0000-0000-0000-000000000011', 1590000.00, 'https://images.unsplash.com/photo-1564466809058-bf4114d55352?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-870EVO-500GB', 26),
('v0000011-0000-0000-0000-000000000112', 'p0000011-0000-0000-0000-000000000011', 2490000.00, 'https://images.unsplash.com/photo-1564466809058-bf4114d55352?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-870EVO-1TB', 18),
('v0000012-0000-0000-0000-000000000121', 'p0000012-0000-0000-0000-000000000012', 2790000.00, 'https://images.unsplash.com/photo-1625073583004-a6599f32793e?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-T7SHIELD-1TB', 20),
('v0000012-0000-0000-0000-000000000122', 'p0000012-0000-0000-0000-000000000012', 4590000.00, 'https://images.unsplash.com/photo-1625073583004-a6599f32793e?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-T7SHIELD-2TB', 13),
('v0000013-0000-0000-0000-000000000131', 'p0000013-0000-0000-0000-000000000013', 1390000.00, 'https://images.unsplash.com/photo-1593640408182-32c5949efe55?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDB-SN580-500GB', 32),
('v0000013-0000-0000-0000-000000000132', 'p0000013-0000-0000-0000-000000000013', 2190000.00, 'https://images.unsplash.com/photo-1593640408182-32c5949efe55?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDB-SN580-1TB', 24),
('v0000014-0000-0000-0000-000000000141', 'p0000014-0000-0000-0000-000000000014', 2990000.00, 'https://images.unsplash.com/photo-1596483758372-35e7c7d1a656?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDB-SN850X-1TB', 15),
('v0000014-0000-0000-0000-000000000142', 'p0000014-0000-0000-0000-000000000014', 5190000.00, 'https://images.unsplash.com/photo-1596483758372-35e7c7d1a656?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDB-SN850X-2TB', 9),
('v0000015-0000-0000-0000-000000000151', 'p0000015-0000-0000-0000-000000000015', 229000.00, 'https://images.unsplash.com/photo-1609091831891-913a32849e7b?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-ULTRAFIT-64GB', 45),
('v0000015-0000-0000-0000-000000000152', 'p0000015-0000-0000-0000-000000000015', 379000.00, 'https://images.unsplash.com/photo-1609091831891-913a32849e7b?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-ULTRAFIT-128GB', 39),
('v0000016-0000-0000-0000-000000000161', 'p0000016-0000-0000-0000-000000000016', 3590000.00, 'https://images.unsplash.com/photo-1619061657909-2a59c8d47a86?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-EXPRO-1TB', 14),
('v0000016-0000-0000-0000-000000000162', 'p0000016-0000-0000-0000-000000000016', 6690000.00, 'https://images.unsplash.com/photo-1619061657909-2a59c8d47a86?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-EXPRO-2TB', 8),
('v0000017-0000-0000-0000-000000000171', 'p0000017-0000-0000-0000-000000000017', 3290000.00, 'https://images.unsplash.com/photo-1589739124578-20f511b6a579?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-BEEDRIVE-1TB', 16),
('v0000017-0000-0000-0000-000000000172', 'p0000017-0000-0000-0000-000000000017', 5290000.00, 'https://images.unsplash.com/photo-1589739124578-20f511b6a579?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-BEEDRIVE-2TB', 10),
('v0000018-0000-0000-0000-000000000181', 'p0000018-0000-0000-0000-000000000018', 4890000.00, 'https://images.unsplash.com/photo-1633534588870-07e873f54d59?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-SAT5210-960GB', 12),
('v0000018-0000-0000-0000-000000000182', 'p0000018-0000-0000-0000-000000000018', 8990000.00, 'https://images.unsplash.com/photo-1633534588870-07e873f54d59?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-SAT5210-1920GB', 6),
('v0000019-0000-0000-0000-000000000191', 'p0000019-0000-0000-0000-000000000019', 1790000.00, 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDP-MYPASSPORT-2TB', 22),
('v0000019-0000-0000-0000-000000000192', 'p0000019-0000-0000-0000-000000000019', 2890000.00, 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDP-MYPASSPORT-4TB', 15),
('v0000020-0000-0000-0000-000000000201', 'p0000020-0000-0000-0000-000000000020', 2690000.00, 'https://images.unsplash.com/photo-1601144720089-6ea54c59978b?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-PM9A1-1TB', 17),
('v0000020-0000-0000-0000-000000000202', 'p0000020-0000-0000-0000-000000000020', 4690000.00, 'https://images.unsplash.com/photo-1601144720089-6ea54c59978b?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-PM9A1-2TB', 9),
('v0000021-0000-0000-0000-000000000211', 'p0000021-0000-0000-0000-000000000021', 1890000.00, 'https://images.unsplash.com/photo-1585652757146-e69d68196338?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-AX55-AX3000', 25),
('v0000021-0000-0000-0000-000000000212', 'p0000021-0000-0000-0000-000000000021', 2290000.00, 'https://images.unsplash.com/photo-1585652757146-e69d68196338?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-AX55-EXT', 12),
('v0000022-0000-0000-0000-000000000221', 'p0000022-0000-0000-0000-000000000022', 4290000.00, 'https://images.unsplash.com/photo-1544244015-0df0d890a5b0?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-DECOX50-2PK', 13),
('v0000022-0000-0000-0000-000000000222', 'p0000022-0000-0000-0000-000000000022', 5990000.00, 'https://images.unsplash.com/photo-1544244015-0df0d890a5b0?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-DECOX50-3PK', 7),
('v0000023-0000-0000-0000-000000000231', 'p0000023-0000-0000-0000-000000000023', 2390000.00, 'https://images.unsplash.com/photo-1587832074271-64295ace7758?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-EAP610-CEILING', 18),
('v0000023-0000-0000-0000-000000000232', 'p0000023-0000-0000-0000-000000000023', 2790000.00, 'https://images.unsplash.com/photo-1587832074271-64295ace7758?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-EAP610-POE', 10),
('v0000024-0000-0000-0000-000000000241', 'p0000024-0000-0000-0000-000000000024', 1390000.00, 'https://images.unsplash.com/photo-1616423635164-30556d5d3437?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-SG1016D-16P', 21),
('v0000024-0000-0000-0000-000000000242', 'p0000024-0000-0000-0000-000000000024', 1690000.00, 'https://images.unsplash.com/photo-1616423635164-30556d5d3437?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-SG1016D-RACK', 11),
('v0000025-0000-0000-0000-000000000251', 'p0000025-0000-0000-0000-000000000025', 7890000.00, 'https://images.unsplash.com/photo-1517673900852-bb8409fcc2c1?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-RT6600AX-STD', 8),
('v0000025-0000-0000-0000-000000000252', 'p0000025-0000-0000-0000-000000000025', 8490000.00, 'https://images.unsplash.com/photo-1517673900852-bb8409fcc2c1?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-RT6600AX-BUNDLE', 5),
('v0000026-0000-0000-0000-000000000261', 'p0000026-0000-0000-0000-000000000026', 249000.00, 'https://images.unsplash.com/photo-1523875262180-3b08be7b8359?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-READER-SD', 37),
('v0000026-0000-0000-0000-000000000262', 'p0000026-0000-0000-0000-000000000026', 349000.00, 'https://images.unsplash.com/photo-1523875262180-3b08be7b8359?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-READER-USBC', 28),
('v0000027-0000-0000-0000-000000000271', 'p0000027-0000-0000-0000-000000000027', 79000.00, 'https://images.unsplash.com/photo-1504384308090-c894fd630149?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-CAT6-1M', 90),
('v0000027-0000-0000-0000-000000000272', 'p0000027-0000-0000-0000-000000000027', 129000.00, 'https://images.unsplash.com/photo-1504384308090-c894fd630149?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-CAT6-3M', 70),
('v0000028-0000-0000-0000-000000000281', 'p0000028-0000-0000-0000-000000000028', 390000.00, 'https://images.unsplash.com/photo-1574254208806-5b7e8e77f926?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-TRAY-TYPE-D6', 18),
('v0000028-0000-0000-0000-000000000282', 'p0000028-0000-0000-0000-000000000028', 490000.00, 'https://images.unsplash.com/photo-1574254208806-5b7e8e77f926?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-TRAY-LOCKKIT', 14),
('v0000029-0000-0000-0000-000000000291', 'p0000029-0000-0000-0000-000000000029', 159000.00, 'https://images.unsplash.com/photo-1618591609711-956e5f6e7746?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-USBC-USBA-BLK', 60),
('v0000029-0000-0000-0000-000000000292', 'p0000029-0000-0000-0000-000000000029', 189000.00, 'https://images.unsplash.com/photo-1618591609711-956e5f6e7746?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-USBC-USBA-2PK', 42),
('v0000030-0000-0000-0000-000000000301', 'p0000030-0000-0000-0000-000000000030', 199000.00, 'https://images.unsplash.com/photo-1590393034759-f181435a43f5?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-CASE-2TB', 33),
('v0000030-0000-0000-0000-000000000302', 'p0000030-0000-0000-0000-000000000030', 249000.00, 'https://images.unsplash.com/photo-1590393034759-f181435a43f5?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDC-CASE-4TB', 25);

-- 22 more products to reach 52 total with unique images and proper variants
INSERT INTO `Product` (`id`, `name`, `description`, `brandId`, `status`, `userId`, `category`)
VALUES
('p0000031-0000-0000-0000-000000000031', 'Samsung 980 SSD', 'M.2 NVMe SSD for everyday computing without heatsink.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p0000032-0000-0000-0000-000000000032', 'WD Purple Surveillance HDD', 'Optimized for 24/7 DVR and NVR surveillance systems.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p0000033-0000-0000-0000-000000000033', 'Synology DiskStation DS224+', '2-bay NAS for home and small office data management.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p0000034-0000-0000-0000-000000000034', 'TP-Link Archer AXE75 Wi-Fi 6E Router', 'Tri-band Wi-Fi 6E router with 6 GHz band support.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p0000035-0000-0000-0000-000000000035', 'SanDisk Ultra Dual Drive USB-C', 'Dual connector flash drive for phone and computer transfer.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'MEMORY_CARD'),
('p0000036-0000-0000-0000-000000000036', 'Samsung T9 Portable SSD', 'USB 3.2 Gen 2x2 portable SSD for high-speed content workflows.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p0000037-0000-0000-0000-000000000037', 'WD Black D10 Game Drive', 'Desktop HDD tuned for game library storage and fast load times.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p0000038-0000-0000-0000-000000000038', 'Synology DiskStation DS423+', '4-bay NAS with M.2 cache slots for performance storage.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p0000039-0000-0000-0000-000000000039', 'TP-Link TL-SG105 Gigabit Switch', '5-port desktop unmanaged switch for quick network expansion.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p0000040-0000-0000-0000-000000000040', 'SanDisk PRO-CINEMA CFexpress Card', 'Professional CFexpress Type B card for cinema cameras.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'MEMORY_CARD'),
('p0000041-0000-0000-0000-000000000041', 'Samsung Portable SSD T5', 'Slim portable SSD with metal body for everyday data transport.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p0000042-0000-0000-0000-000000000042', 'WD Red Pro NAS HDD', 'Enterprise-grade NAS hard drive for heavy workloads and RAID.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p0000043-0000-0000-0000-000000000043', 'Synology WRX560 Wi-Fi Router', 'Wi-Fi 6 router with SRM for centralized network management.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p0000044-0000-0000-0000-000000000044', 'TP-Link Omada ER605 Router', 'Gigabit VPN router for Omada SDN business networks.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p0000045-0000-0000-0000-000000000045', 'SanDisk iXpand Flash Drive', 'Lightning connector flash drive for iPhone and iPad backup.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'MEMORY_CARD'),
('p0000046-0000-0000-0000-000000000046', 'Samsung 9100 PRO NVMe SSD', 'Next-gen PCIe 5.0 NVMe SSD for extreme workstation performance.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p0000047-0000-0000-0000-000000000047', 'WD Gold Enterprise HDD', 'Data center class hard drive for enterprise server environments.', 'b2222222-2222-2222-2222-222222222222', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'SSD'),
('p0000048-0000-0000-0000-000000000048', 'Synology DiskStation DS1621+', '6-bay Ryzen-powered NAS for virtualization and heavy workloads.', 'b3333333-3333-3333-3333-333333333333', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p0000049-0000-0000-0000-000000000049', 'TP-Link Archer GE810 Tri-Band Router', 'Gaming-optimized tri-band Wi-Fi 6 router with prioritized QoS.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS'),
('p0000050-0000-0000-0000-000000000050', 'SanDisk Extreme PRO SDXC UHS-II', 'Professional SD card for high-speed burst photography and 4K video.', 'b5555555-5555-5555-5555-555555555555', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'MEMORY_CARD'),
('p0000051-0000-0000-0000-000000000051', 'Samsung PRO Endurance microSD', 'High endurance microSD card designed for dashcams and CCTV.', 'b1111111-1111-1111-1111-111111111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'MEMORY_CARD'),
('p0000052-0000-0000-0000-000000000052', 'TP-Link Deco BE85 Mesh Wi-Fi 7', 'Next-gen Wi-Fi 7 mesh system with 10G ports for future-proof homes.', 'b4444444-4444-4444-4444-444444444444', 'ACTIVE', '11111111-1111-1111-1111-111111111111', 'NAS');

INSERT INTO `ProductVariant` (`id`, `productId`, `price`, `imageUrl`, `status`, `sku`, `quantity`)
VALUES
('v0000031-0000-0000-0000-000000000311', 'p0000031-0000-0000-0000-000000000031', 1190000.00, 'https://images.unsplash.com/photo-1598583172713-a7e9a5f3c887?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-980-500GB', 35),
('v0000031-0000-0000-0000-000000000312', 'p0000031-0000-0000-0000-000000000031', 1890000.00, 'https://images.unsplash.com/photo-1598583172713-a7e9a5f3c887?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-980-1TB', 22),
('v0000031-0000-0000-0000-000000000313', 'p0000031-0000-0000-0000-000000000031', 3590000.00, 'https://images.unsplash.com/photo-1598583172713-a7e9a5f3c887?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-980-2TB', 11),

('v0000032-0000-0000-0000-000000000321', 'p0000032-0000-0000-0000-000000000032', 2290000.00, 'https://images.unsplash.com/photo-1607754208768-3c5d3958133c?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDP-PURPLE-4TB', 28),
('v0000032-0000-0000-0000-000000000322', 'p0000032-0000-0000-0000-000000000032', 3490000.00, 'https://images.unsplash.com/photo-1607754208768-3c5d3958133c?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDP-PURPLE-6TB', 19),

('v0000033-0000-0000-0000-000000000331', 'p0000033-0000-0000-0000-000000000033', 11890000.00, 'https://images.unsplash.com/photo-1563297775-2d701057b0d6?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-DS224PLUS-2BAY', 10),
('v0000033-0000-0000-0000-000000000332', 'p0000033-0000-0000-0000-000000000033', 12990000.00, 'https://images.unsplash.com/photo-1563297775-2d701057b0d6?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-DS224PLUS-BUNDLE', 6),

('v0000034-0000-0000-0000-000000000341', 'p0000034-0000-0000-0000-000000000034', 4590000.00, 'https://images.unsplash.com/photo-1610124397931-34e5e5e476cc?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-AXE75-AXE5400', 12),
('v0000034-0000-0000-0000-000000000342', 'p0000034-0000-0000-0000-000000000034', 4990000.00, 'https://images.unsplash.com/photo-1610124397931-34e5e5e476cc?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-AXE75-GAMING', 8),

('v0000035-0000-0000-0000-000000000351', 'p0000035-0000-0000-0000-000000000035', 289000.00, 'https://images.unsplash.com/photo-1533158325197-2ce6e3e73538?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-IXDUAL-64GB', 50),
('v0000035-0000-0000-0000-000000000352', 'p0000035-0000-0000-0000-000000000035', 459000.00, 'https://images.unsplash.com/photo-1533158325197-2ce6e3e73538?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-IXDUAL-128GB', 38),
('v0000035-0000-0000-0000-000000000353', 'p0000035-0000-0000-0000-000000000035', 799000.00, 'https://images.unsplash.com/photo-1533158325197-2ce6e3e73538?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-IXDUAL-256GB', 25),

('v0000036-0000-0000-0000-000000000361', 'p0000036-0000-0000-0000-000000000036', 4290000.00, 'https://images.unsplash.com/photo-1574254208806-5b7e8e77f927?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-T9-1TB', 16),
('v0000036-0000-0000-0000-000000000362', 'p0000036-0000-0000-0000-000000000036', 7290000.00, 'https://images.unsplash.com/photo-1574254208806-5b7e8e77f927?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-T9-2TB', 10),
('v0000036-0000-0000-0000-000000000363', 'p0000036-0000-0000-0000-000000000036', 13490000.00, 'https://images.unsplash.com/photo-1574254208806-5b7e8e77f927?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-T9-4TB', 4),

('v0000037-0000-0000-0000-000000000371', 'p0000037-0000-0000-0000-000000000037', 3890000.00, 'https://images.unsplash.com/photo-1504384308090-c894fd630150?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDB-D10-8TB', 14),
('v0000037-0000-0000-0000-000000000372', 'p0000037-0000-0000-0000-000000000037', 5190000.00, 'https://images.unsplash.com/photo-1504384308090-c894fd630150?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDB-D10-12TB', 8),

('v0000038-0000-0000-0000-000000000381', 'p0000038-0000-0000-0000-000000000038', 15890000.00, 'https://images.unsplash.com/photo-1618591609711-956e5f6e7747?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-DS423PLUS-4BAY', 8),
('v0000038-0000-0000-0000-000000000382', 'p0000038-0000-0000-0000-000000000038', 17490000.00, 'https://images.unsplash.com/photo-1618591609711-956e5f6e7747?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-DS423PLUS-RAM8', 5),

('v0000039-0000-0000-0000-000000000391', 'p0000039-0000-0000-0000-000000000039', 389000.00, 'https://images.unsplash.com/photo-1590393034759-f181435a43f6?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-SG105-5P', 60),
('v0000039-0000-0000-0000-000000000392', 'p0000039-0000-0000-0000-000000000039', 489000.00, 'https://images.unsplash.com/photo-1590393034759-f181435a43f6?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-SG105-METAL', 45),

('v0000040-0000-0000-0000-000000000401', 'p0000040-0000-0000-0000-000000000040', 2890000.00, 'https://images.unsplash.com/photo-1598583172713-a7e9a5f3c888?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-CFEXP-128GB', 15),
('v0000040-0000-0000-0000-000000000402', 'p0000040-0000-0000-0000-000000000040', 5190000.00, 'https://images.unsplash.com/photo-1598583172713-a7e9a5f3c888?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-CFEXP-256GB', 8),

('v0000041-0000-0000-0000-000000000411', 'p0000041-0000-0000-0000-000000000041', 2190000.00, 'https://images.unsplash.com/photo-1607754208768-3c5d3958133d?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-T5-500GB', 24),
('v0000041-0000-0000-0000-000000000412', 'p0000041-0000-0000-0000-000000000041', 3290000.00, 'https://images.unsplash.com/photo-1607754208768-3c5d3958133d?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-T5-1TB', 17),
('v0000041-0000-0000-0000-000000000413', 'p0000041-0000-0000-0000-000000000041', 5990000.00, 'https://images.unsplash.com/photo-1607754208768-3c5d3958133d?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-T5-2TB', 9),

('v0000042-0000-0000-0000-000000000421', 'p0000042-0000-0000-0000-000000000042', 4590000.00, 'https://images.unsplash.com/photo-1563297775-2d701057b0d7?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDP-REDPRO-8TB', 16),
('v0000042-0000-0000-0000-000000000422', 'p0000042-0000-0000-0000-000000000042', 6290000.00, 'https://images.unsplash.com/photo-1563297775-2d701057b0d7?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDP-REDPRO-12TB', 9),
('v0000042-0000-0000-0000-000000000423', 'p0000042-0000-0000-0000-000000000042', 8990000.00, 'https://images.unsplash.com/photo-1563297775-2d701057b0d7?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDP-REDPRO-16TB', 5),

('v0000043-0000-0000-0000-000000000431', 'p0000043-0000-0000-0000-000000000043', 3890000.00, 'https://images.unsplash.com/photo-1610124397931-34e5e5e476dd?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-WRX560-STD', 12),
('v0000043-0000-0000-0000-000000000432', 'p0000043-0000-0000-0000-000000000043', 4290000.00, 'https://images.unsplash.com/photo-1610124397931-34e5e5e476dd?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-WRX560-MESH', 7),

('v0000044-0000-0000-0000-000000000441', 'p0000044-0000-0000-0000-000000000044', 2790000.00, 'https://images.unsplash.com/photo-1533158325197-2ce6e3e73539?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-ER605-VPN', 20),
('v0000044-0000-0000-0000-000000000442', 'p0000044-0000-0000-0000-000000000044', 3190000.00, 'https://images.unsplash.com/photo-1533158325197-2ce6e3e73539?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-ER605-SFP', 14),

('v0000045-0000-0000-0000-000000000451', 'p0000045-0000-0000-0000-000000000045', 349000.00, 'https://images.unsplash.com/photo-1574254208806-5b7e8e77f928?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-IXIPAD-64GB', 40),
('v0000045-0000-0000-0000-000000000452', 'p0000045-0000-0000-0000-000000000045', 559000.00, 'https://images.unsplash.com/photo-1574254208806-5b7e8e77f928?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-IXIPAD-128GB', 32),
('v0000045-0000-0000-0000-000000000453', 'p0000045-0000-0000-0000-000000000045', 959000.00, 'https://images.unsplash.com/photo-1574254208806-5b7e8e77f928?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-IXIPAD-256GB', 22),

('v0000046-0000-0000-0000-000000000461', 'p0000046-0000-0000-0000-000000000046', 5690000.00, 'https://images.unsplash.com/photo-1504384308090-c894fd630151?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-9100PRO-1TB', 13),
('v0000046-0000-0000-0000-000000000462', 'p0000046-0000-0000-0000-000000000046', 9990000.00, 'https://images.unsplash.com/photo-1504384308090-c894fd630151?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-9100PRO-2TB', 7),
('v0000046-0000-0000-0000-000000000463', 'p0000046-0000-0000-0000-000000000046', 18990000.00, 'https://images.unsplash.com/photo-1504384308090-c894fd630151?auto=format&fit=crop&w=1200&q=80', 'OUT_OF_STOCK', 'SAM-9100PRO-4TB', 0),

('v0000047-0000-0000-0000-000000000471', 'p0000047-0000-0000-0000-000000000047', 5490000.00, 'https://images.unsplash.com/photo-1618591609711-956e5f6e7748?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDG-GOLD-10TB', 12),
('v0000047-0000-0000-0000-000000000472', 'p0000047-0000-0000-0000-000000000047', 7990000.00, 'https://images.unsplash.com/photo-1618591609711-956e5f6e7748?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDG-GOLD-14TB', 7),
('v0000047-0000-0000-0000-000000000473', 'p0000047-0000-0000-0000-000000000047', 11490000.00, 'https://images.unsplash.com/photo-1618591609711-956e5f6e7748?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'WDG-GOLD-20TB', 3),

('v0000048-0000-0000-0000-000000000481', 'p0000048-0000-0000-0000-000000000048', 24890000.00, 'https://images.unsplash.com/photo-1590393034759-f181435a43f7?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-DS1621PLUS-6BAY', 5),
('v0000048-0000-0000-0000-000000000482', 'p0000048-0000-0000-0000-000000000048', 26490000.00, 'https://images.unsplash.com/photo-1590393034759-f181435a43f7?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SYN-DS1621PLUS-16GB', 3),

('v0000049-0000-0000-0000-000000000491', 'p0000049-0000-0000-0000-000000000049', 8590000.00, 'https://images.unsplash.com/photo-1598583172713-a7e9a5f3c889?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-GE810-TRIBAND', 9),
('v0000049-0000-0000-0000-000000000492', 'p0000049-0000-0000-0000-000000000049', 9290000.00, 'https://images.unsplash.com/photo-1598583172713-a7e9a5f3c889?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-GE810-PRO', 5),

('v0000050-0000-0000-0000-000000000501', 'p0000050-0000-0000-0000-000000000050', 1490000.00, 'https://images.unsplash.com/photo-1607754208768-3c5d3958133e?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-SDPRO-64GB', 30),
('v0000050-0000-0000-0000-000000000502', 'p0000050-0000-0000-0000-000000000050', 2590000.00, 'https://images.unsplash.com/photo-1607754208768-3c5d3958133e?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-SDPRO-128GB', 20),
('v0000050-0000-0000-0000-000000000503', 'p0000050-0000-0000-0000-000000000050', 4890000.00, 'https://images.unsplash.com/photo-1607754208768-3c5d3958133e?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SDK-SDPRO-256GB', 10),

('v0000051-0000-0000-0000-000000000511', 'p0000051-0000-0000-0000-000000000051', 389000.00, 'https://images.unsplash.com/photo-1563297775-2d701057b0d8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-ENDURANCE-32GB', 55),
('v0000051-0000-0000-0000-000000000512', 'p0000051-0000-0000-0000-000000000051', 649000.00, 'https://images.unsplash.com/photo-1563297775-2d701057b0d8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-ENDURANCE-64GB', 42),
('v0000051-0000-0000-0000-000000000513', 'p0000051-0000-0000-0000-000000000051', 1090000.00, 'https://images.unsplash.com/photo-1563297775-2d701057b0d8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-ENDURANCE-128GB', 28),
('v0000051-0000-0000-0000-000000000514', 'p0000051-0000-0000-0000-000000000051', 1890000.00, 'https://images.unsplash.com/photo-1563297775-2d701057b0d8?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'SAM-ENDURANCE-256GB', 18),

('v0000052-0000-0000-0000-000000000521', 'p0000052-0000-0000-0000-000000000052', 14890000.00, 'https://images.unsplash.com/photo-1610124397931-34e5e5e476ee?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-BE85-2PK', 6),
('v0000052-0000-0000-0000-000000000522', 'p0000052-0000-0000-0000-000000000052', 21490000.00, 'https://images.unsplash.com/photo-1610124397931-34e5e5e476ee?auto=format&fit=crop&w=1200&q=80', 'ACTIVE', 'TPL-BE85-3PK', 4);

-- More seed reviews for products 31-52
INSERT INTO `ProductReview` (`id`, `productId`, `userId`, `rating`, `comment`, `status`, `createdAt`, `updatedAt`)
VALUES
('r0000013-0000-0000-0000-000000000013', 'p0000031-0000-0000-0000-000000000031', '22222222-2222-2222-2222-222222222222', 5, 'SSD giá tốt, hiệu năng ổn cho laptop văn phòng.', 'APPROVED', '2026-05-14 10:00:00', '2026-05-14 10:00:00'),
('r0000014-0000-0000-0000-000000000014', 'p0000033-0000-0000-0000-000000000033', '33333333-3333-3333-3333-333333333333', 4, 'NAS 2-bay đủ dùng cho gia đình, cấu hình đơn giản.', 'APPROVED', '2026-05-14 11:30:00', '2026-05-14 11:30:00'),
('r0000015-0000-0000-0000-000000000015', 'p0000034-0000-0000-0000-000000000034', '44444444-4444-4444-4444-444444444444', 5, 'Wi-Fi 6E băng thông rộng, streaming 4K mượt mà.', 'APPROVED', '2026-05-15 09:15:00', '2026-05-15 09:15:00'),
('r0000016-0000-0000-0000-000000000016', 'p0000036-0000-0000-0000-000000000036', '55555555-5555-5555-5555-555555555555', 5, 'Ổ T9 siêu nhanh, copy file 50GB chưa đầy 2 phút.', 'APPROVED', '2026-05-15 14:45:00', '2026-05-15 14:45:00'),
('r0000017-0000-0000-0000-000000000017', 'p0000038-0000-0000-0000-000000000038', '22222222-2222-2222-2222-222222222222', 4, 'NAS 4-bay mạnh mẽ, M.2 cache giúp truy xuất nhanh hẳn.', 'APPROVED', '2026-05-16 08:20:00', '2026-05-16 08:20:00'),
('r0000018-0000-0000-0000-000000000018', 'p0000041-0000-0000-0000-000000000041', '33333333-3333-3333-3333-333333333333', 4, 'Thiết kế đẹp, vỏ nhôm chắc tay. Tốc độ USB 3.1 đúng chuẩn.', 'APPROVED', '2026-05-16 16:50:00', '2026-05-16 16:50:00'),
('r0000019-0000-0000-0000-000000000019', 'p0000042-0000-0000-0000-000000000042', '44444444-4444-4444-4444-444444444444', 5, 'WD Red Pro chạy êm, nhiệt thấp, RAID rebuild nhanh.', 'APPROVED', '2026-05-17 11:10:00', '2026-05-17 11:10:00'),
('r0000020-0000-0000-0000-000000000020', 'p0000046-0000-0000-0000-000000000046', '55555555-5555-5555-5555-555555555555', 5, 'PCIe 5.0 tốc độ khủng, render video nặng không ngán.', 'APPROVED', '2026-05-18 13:30:00', '2026-05-18 13:30:00'),
('r0000021-0000-0000-0000-000000000021', 'p0000049-0000-0000-0000-000000000049', '22222222-2222-2222-2222-222222222222', 4, 'Router gaming giảm ping rõ rệt, QoS hoạt động tốt.', 'APPROVED', '2026-05-19 15:00:00', '2026-05-19 15:00:00'),
('r0000022-0000-0000-0000-000000000022', 'p0000052-0000-0000-0000-000000000052', '33333333-3333-3333-3333-333333333333', 5, 'Wi-Fi 7 tương lai, port 10G sẵn sàng cho mạng nội bộ.', 'APPROVED', '2026-05-20 10:40:00', '2026-05-20 10:40:00');

INSERT INTO `ProductReview` (`id`, `productId`, `userId`, `rating`, `comment`, `status`, `createdAt`, `updatedAt`)
VALUES
('r0000001-0000-0000-0000-000000000001', 'p1111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', 5, 'SSD chạy rất nhanh, lắp vào máy trạm nhận ngay và nhiệt độ ổn định.', 'APPROVED', '2026-05-01 09:20:00', '2026-05-01 09:20:00'),
('r0000002-0000-0000-0000-000000000002', 'p1111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333', 4, 'Hiệu năng tốt, đóng gói chắc chắn. Giá hơi cao nhưng xứng đáng.', 'APPROVED', '2026-05-03 14:10:00', '2026-05-03 14:10:00'),
('r0000003-0000-0000-0000-000000000003', 'p2222222-2222-2222-2222-222222222222', '44444444-4444-4444-4444-444444444444', 5, 'Dùng cho NAS gia đình êm, nhiệt thấp và copy dữ liệu liên tục ổn.', 'APPROVED', '2026-05-04 11:45:00', '2026-05-04 11:45:00'),
('r0000004-0000-0000-0000-000000000004', 'p3333333-3333-3333-3333-333333333333', '55555555-5555-5555-5555-555555555555', 5, 'NAS dễ cấu hình, giao diện quản trị rõ ràng, backup ảnh rất tiện.', 'APPROVED', '2026-05-05 16:30:00', '2026-05-05 16:30:00'),
('r0000005-0000-0000-0000-000000000005', 'p4444444-4444-4444-4444-444444444444', '22222222-2222-2222-2222-222222222222', 4, 'Router phủ sóng tốt trong căn hộ, thiết lập nhanh qua ứng dụng.', 'APPROVED', '2026-05-06 10:05:00', '2026-05-06 10:05:00'),
('r0000006-0000-0000-0000-000000000006', 'p5555555-5555-5555-5555-555555555555', '33333333-3333-3333-3333-333333333333', 5, 'Ổ nhỏ gọn, tốc độ chép video cao và vỏ cứng cáp khi mang đi quay.', 'APPROVED', '2026-05-07 13:50:00', '2026-05-07 13:50:00'),
('r0000007-0000-0000-0000-000000000007', 'p6666666-6666-6666-6666-666666666666', '44444444-4444-4444-4444-444444444444', 4, 'Switch cắm là chạy, vỏ kim loại chắc và không nóng nhiều.', 'APPROVED', '2026-05-08 08:40:00', '2026-05-08 08:40:00'),
('r0000008-0000-0000-0000-000000000008', 'p7777777-7777-7777-7777-777777777777', '55555555-5555-5555-5555-555555555555', 4, 'Thẻ nhớ nhận đúng dung lượng, quay 4K trên máy ảnh không bị rớt khung.', 'APPROVED', '2026-05-09 18:15:00', '2026-05-09 18:15:00'),
('r0000009-0000-0000-0000-000000000009', 'p8888888-8888-8888-8888-888888888888', '22222222-2222-2222-2222-222222222222', 3, 'Dung lượng lớn, phù hợp backup định kỳ. Tốc độ đúng kỳ vọng với ổ HDD.', 'APPROVED', '2026-05-10 12:25:00', '2026-05-10 12:25:00'),
('r0000010-0000-0000-0000-000000000010', 'p9999999-9999-9999-9999-999999999999', '33333333-3333-3333-3333-333333333333', 5, 'Ổ chạy ổn trong NAS Synology, kiểm tra SMART không có lỗi.', 'APPROVED', '2026-05-11 15:35:00', '2026-05-11 15:35:00'),
('r0000011-0000-0000-0000-000000000011', 'paaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '44444444-4444-4444-4444-444444444444', 4, 'USB kim loại đẹp, tốc độ chép tài liệu nhanh và móc khóa tiện.', 'APPROVED', '2026-05-12 09:55:00', '2026-05-12 09:55:00'),
('r0000012-0000-0000-0000-000000000012', 'p0000012-0000-0000-0000-000000000012', '55555555-5555-5555-5555-555555555555', 5, 'Ổ portable chống sốc tốt, cắm MacBook và Windows đều nhận ngay.', 'APPROVED', '2026-05-13 17:05:00', '2026-05-13 17:05:00');
