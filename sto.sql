DROP TABLE IF EXISTS "ProductReview" CASCADE;
DROP TABLE IF EXISTS "Payment" CASCADE;
DROP TABLE IF EXISTS "PasswordResetToken" CASCADE;
DROP TABLE IF EXISTS "EmailVerificationCode" CASCADE;
DROP TABLE IF EXISTS "Session" CASCADE;
DROP TABLE IF EXISTS "SavedProduct" CASCADE;
DROP TABLE IF EXISTS "ItemCart" CASCADE;
DROP TABLE IF EXISTS "OrderCart" CASCADE;
DROP TABLE IF EXISTS "Voucher" CASCADE;
DROP TABLE IF EXISTS "Order" CASCADE;
DROP TABLE IF EXISTS "ProductVariant" CASCADE;
DROP TABLE IF EXISTS "Product" CASCADE;
DROP TABLE IF EXISTS "Brand" CASCADE;
DROP TABLE IF EXISTS "OutBox" CASCADE;
DROP TABLE IF EXISTS "User" CASCADE;

CREATE TABLE "User" (
    "id" CHAR(36) PRIMARY KEY,
    "name" VARCHAR(255) NOT NULL,
    "dateOfBirth" DATE NOT NULL,
    "hashPassword" VARCHAR(255) NOT NULL,
    "status" VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK ("status" IN ('PENDING', 'ACTIVE', 'INACTIVE', 'BANNED')),
    "role" VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK ("role" IN ('ADMIN', 'USER')),
    "email" VARCHAR(255) NOT NULL UNIQUE,
    "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "OutBox" (
    "id" CHAR(36) PRIMARY KEY,
    "code" VARCHAR(255) NOT NULL,
    "status" VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK ("status" IN ('PENDING', 'PROCESSED', 'FAILED')),
    "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "type" VARCHAR(100) NOT NULL,
    "userId" CHAR(36) NOT NULL REFERENCES "User" ("id")
);
CREATE INDEX "outbox_userid_index" ON "OutBox" ("userId");
CREATE INDEX "outbox_userid_type_status_createdat_index" ON "OutBox" ("userId", "type", "status", "createdAt");

CREATE TABLE "EmailVerificationCode" (
    "id" CHAR(36) PRIMARY KEY,
    "userId" CHAR(36) NOT NULL REFERENCES "User" ("id"),
    "codeHash" VARCHAR(255) NOT NULL,
    "expiresAt" TIMESTAMP NOT NULL,
    "usedAt" TIMESTAMP NULL,
    "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX "emailverificationcode_userid_index" ON "EmailVerificationCode" ("userId");

CREATE TABLE "PasswordResetToken" (
    "id" CHAR(36) PRIMARY KEY,
    "userId" CHAR(36) NOT NULL REFERENCES "User" ("id"),
    "tokenHash" VARCHAR(255) NOT NULL,
    "expiresAt" TIMESTAMP NOT NULL,
    "usedAt" TIMESTAMP NULL,
    "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX "passwordresettoken_userid_index" ON "PasswordResetToken" ("userId");

CREATE TABLE "Brand" (
    "id" CHAR(36) PRIMARY KEY,
    "name" VARCHAR(255) NOT NULL,
    "userId" CHAR(36) NOT NULL REFERENCES "User" ("id"),
    "description" VARCHAR(255) NOT NULL,
    "status" VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK ("status" IN ('ACTIVE', 'INACTIVE')),
    "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX "brand_userid_index" ON "Brand" ("userId");

CREATE TABLE "Product" (
    "id" CHAR(36) PRIMARY KEY,
    "name" VARCHAR(255) NOT NULL,
    "description" VARCHAR(255) NOT NULL,
    "brandId" CHAR(36) NOT NULL REFERENCES "Brand" ("id"),
    "status" VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK ("status" IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED')),
    "userId" CHAR(36) NOT NULL REFERENCES "User" ("id"),
    "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "category" VARCHAR(30) NOT NULL CHECK ("category" IN ('STORAGE_DEVICE', 'NETWORK_DEVICE', 'ACCESSORY'))
);
CREATE INDEX "product_brandid_index" ON "Product" ("brandId");
CREATE INDEX "product_userid_index" ON "Product" ("userId");

CREATE TABLE "ProductVariant" (
    "id" CHAR(36) PRIMARY KEY,
    "productId" CHAR(36) NOT NULL REFERENCES "Product" ("id"),
    "price" NUMERIC(12,2) NOT NULL CHECK ("price" >= 0),
    "imageUrl" VARCHAR(1000) NOT NULL,
    "status" VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK ("status" IN ('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK')),
    "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "sku" VARCHAR(255) NOT NULL UNIQUE,
    "quantity" INTEGER NOT NULL DEFAULT 0 CHECK ("quantity" >= 0)
);
CREATE INDEX "productvariant_productid_index" ON "ProductVariant" ("productId");

CREATE TABLE "Order" (
    "id" CHAR(36) PRIMARY KEY,
    "userId" CHAR(36) NOT NULL REFERENCES "User" ("id"),
    "productId" CHAR(36) NOT NULL REFERENCES "Product" ("id"),
    "variantId" CHAR(36) NULL REFERENCES "ProductVariant" ("id") ON DELETE SET NULL,
    "quantity" INTEGER NOT NULL DEFAULT 1 CHECK ("quantity" > 0),
    "status" VARCHAR(40) NOT NULL DEFAULT 'PENDING' CHECK ("status" IN ('CHO_VAO_GIO', 'DA_DAT_HANG', 'DA_THANH_TOAN_THANH_CONG', 'PENDING', 'CONFIRMED', 'SHIPPING', 'COMPLETED', 'CANCELLED')),
    "phone" VARCHAR(50) NULL,
    "address" VARCHAR(500) NULL,
    "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX "order_userid_index" ON "Order" ("userId");
CREATE INDEX "order_productid_index" ON "Order" ("productId");
CREATE INDEX "order_variantid_index" ON "Order" ("variantId");
CREATE INDEX "order_userid_status_createdat_index" ON "Order" ("userId", "status", "createdAt");

CREATE TABLE "Payment" (
    "id" CHAR(36) PRIMARY KEY,
    "orderId" CHAR(36) NOT NULL REFERENCES "Order" ("id"),
    "userId" CHAR(36) NOT NULL REFERENCES "User" ("id"),
    "amount" NUMERIC(12,2) NOT NULL CHECK ("amount" >= 0),
    "accessKey" VARCHAR(255) NOT NULL,
    "partnerCode" VARCHAR(255) NOT NULL,
    "redirectUrl" VARCHAR(1000) NOT NULL,
    "ipnUrl" VARCHAR(1000) NOT NULL,
    "extraData" VARCHAR(255) NOT NULL,
    "requestType" VARCHAR(255) NOT NULL,
    "signature" VARCHAR(255) NOT NULL,
    "status" VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK ("status" IN ('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED')),
    "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX "payment_orderid_index" ON "Payment" ("orderId");
CREATE INDEX "payment_userid_index" ON "Payment" ("userId");

CREATE TABLE "Voucher" (
    "id" CHAR(36) PRIMARY KEY,
    "percent" NUMERIC(5,2) NOT NULL CHECK ("percent" >= 0 AND "percent" <= 100),
    "userId" CHAR(36) NOT NULL REFERENCES "User" ("id"),
    "expTime" DATE NOT NULL,
    "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "quantity" INTEGER NOT NULL DEFAULT 0 CHECK ("quantity" >= 0)
);
CREATE INDEX "voucher_userid_index" ON "Voucher" ("userId");

CREATE TABLE "OrderCart" (
    "id" CHAR(36) PRIMARY KEY,
    "userId" CHAR(36) NOT NULL UNIQUE REFERENCES "User" ("id"),
    "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX "ordercart_userid_index" ON "OrderCart" ("userId");

CREATE TABLE "ItemCart" (
    "id" CHAR(36) PRIMARY KEY,
    "cartId" CHAR(36) NOT NULL REFERENCES "OrderCart" ("id") ON DELETE CASCADE,
    "productId" CHAR(36) NOT NULL REFERENCES "Product" ("id"),
    "variantId" CHAR(36) NULL REFERENCES "ProductVariant" ("id") ON DELETE SET NULL,
    "quantity" INTEGER NOT NULL DEFAULT 1 CHECK ("quantity" > 0),
    "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX "itemcart_cartid_productid_variantid_unique" ON "ItemCart" ("cartId", "productId", COALESCE("variantId", ''));
CREATE INDEX "itemcart_cartid_index" ON "ItemCart" ("cartId");
CREATE INDEX "itemcart_productid_index" ON "ItemCart" ("productId");
CREATE INDEX "itemcart_variantid_index" ON "ItemCart" ("variantId");

CREATE TABLE "SavedProduct" (
    "id" CHAR(36) PRIMARY KEY,
    "productId" CHAR(36) NOT NULL REFERENCES "Product" ("id"),
    "quantity" INTEGER NOT NULL DEFAULT 1 CHECK ("quantity" > 0),
    "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX "savedproduct_productid_index" ON "SavedProduct" ("productId");

CREATE TABLE "Session" (
    "id" CHAR(36) PRIMARY KEY,
    "hashRefreshToken" VARCHAR(255) NOT NULL,
    "userId" CHAR(36) NOT NULL REFERENCES "User" ("id"),
    "ip" VARCHAR(45) NOT NULL,
    "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX "session_userid_index" ON "Session" ("userId");
CREATE INDEX "session_userid_ip_createdat_index" ON "Session" ("userId", "ip", "createdAt");

CREATE TABLE "Contact" (
    "id" CHAR(36) PRIMARY KEY,
    "fullName" VARCHAR(255) NOT NULL,
    "email" VARCHAR(255) NOT NULL,
    "subject" VARCHAR(255) NOT NULL,
    "message" TEXT NOT NULL,
    "status" VARCHAR(20) NOT NULL DEFAULT 'NEW' CHECK ("status" IN ('NEW', 'READ', 'RESPONDED', 'ARCHIVED')),
    "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX "contact_email_index" ON "Contact" ("email");

CREATE TABLE "ProductReview" (
    "id" BIGSERIAL PRIMARY KEY,
    "productId" CHAR(36) NOT NULL REFERENCES "Product" ("id") ON DELETE CASCADE,
    "reviewerName" VARCHAR(255) NOT NULL,
    "rating" INTEGER NOT NULL CHECK ("rating" BETWEEN 1 AND 5),
    "comment" TEXT NOT NULL DEFAULT '',
    "reviewedAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX "productreview_productid_index" ON "ProductReview" ("productId");
CREATE INDEX "productreview_reviewedat_index" ON "ProductReview" ("reviewedAt");

INSERT INTO "User" ("id", "name", "dateOfBirth", "hashPassword", "status", "role", "email") VALUES
('11111111-1111-1111-1111-111111111111', 'StoreIT Admin', '1998-06-15', 'pbkdf2:120000:iM2RxQ8TJc0L0FI1xu3eyg==:ErcoDn/QVGXRqs/54LGi5b22XPrpI7zPGyx2q/3nVDU=', 'ACTIVE', 'ADMIN', 'admin@storeit.local');

INSERT INTO "OrderCart" ("id", "userId") VALUES
('c1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111');

INSERT INTO "Brand" ("id", "name", "userId", "description", "status") VALUES
('b1111111-1111-1111-1111-111111111111', 'Samsung', '11111111-1111-1111-1111-111111111111', 'Consumer and professional SSD solutions.', 'ACTIVE'),
('b2222222-2222-2222-2222-222222222222', 'Western Digital', '11111111-1111-1111-1111-111111111111', 'HDD, NAS and enterprise storage devices.', 'ACTIVE'),
('b3333333-3333-3333-3333-333333333333', 'Synology', '11111111-1111-1111-1111-111111111111', 'NAS and data management platforms.', 'ACTIVE'),
('b4444444-4444-4444-4444-444444444444', 'TP-Link', '11111111-1111-1111-1111-111111111111', 'Networking equipment for home and office.', 'ACTIVE'),
('b5555555-5555-5555-5555-555555555555', 'SanDisk', '11111111-1111-1111-1111-111111111111', 'Flash storage and memory accessories.', 'ACTIVE');

INSERT INTO "Product" ("id", "name", "description", "brandId", "status", "userId", "category") VALUES
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

INSERT INTO "ProductVariant" ("id", "productId", "price", "imageUrl", "status", "sku", "quantity") VALUES
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
