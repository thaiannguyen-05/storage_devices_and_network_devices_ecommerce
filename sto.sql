CREATE TABLE `User`(
    `id` CHAR(36) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `dateOfBirth` DATE NOT NULL,
    `hashPassword` VARCHAR(255) NOT NULL,
    `status` ENUM('PENDING','ACTIVE','INACTIVE','BANNED') NOT NULL,
    `role` ENUM('ADMIN','USER') NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `createdAt` DATE NOT NULL,
    `updatedAt` DATE NOT NULL,
    PRIMARY KEY(`id`)
);
ALTER TABLE
    `User` ADD INDEX `user_email_index`(`email`);
ALTER TABLE
    `User` ADD UNIQUE `user_email_unique`(`email`);
CREATE TABLE `Session`(
    `id` CHAR(36) NOT NULL,
    `hashRefreshToken` VARCHAR(255) NOT NULL,
    `userId` VARCHAR(255) NOT NULL,
    `createdAt` DATE NOT NULL,
    `updatedAt` DATE NOT NULL,
    PRIMARY KEY(`id`)
);
ALTER TABLE
    `Session` ADD INDEX `session_userid_index`(`userId`);
ALTER TABLE
    `Session` ADD UNIQUE `session_userid_unique`(`userId`);
CREATE TABLE `OutBox`(
    `id` CHAR(36) NOT NULL,
    `payload` JSON NOT NULL,
    `status` ENUM('') NOT NULL,
    `createdAt` DATE NOT NULL,
    `updatedAt` DATE NOT NULL,
    PRIMARY KEY(`id`)
);
CREATE TABLE `Brand`(
    `id` CHAR(36) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `userId` VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `status` ENUM('') NOT NULL,
    `createdAt` DATE NOT NULL,
    `updatedAt` DATE NOT NULL,
    PRIMARY KEY(`id`)
);
ALTER TABLE
    `Brand` ADD INDEX `brand_userid_index`(`userId`);
CREATE TABLE `Product`(
    `id` CHAR(36) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `brandId` CHAR(36) NOT NULL,
    `status` ENUM('') NOT NULL,
    `userId` CHAR(36) NOT NULL,
    `createdAt` DATE NOT NULL,
    `updatedAt` DATE NOT NULL,
    `category` ENUM('') NOT NULL,
    PRIMARY KEY(`id`)
);
ALTER TABLE
    `Product` ADD INDEX `product_brandid_index`(`brandId`);
ALTER TABLE
    `Product` ADD INDEX `product_userid_index`(`userId`);
CREATE TABLE `ProductVariant`(
    `id` CHAR(36) NOT NULL,
    `productId` CHAR(36) NOT NULL,
    `price` DECIMAL(8, 2) NOT NULL,
    `imageUrl` VARCHAR(255) NOT NULL,
    `status` ENUM('') NOT NULL,
    `createdAt` DATE NOT NULL,
    `updatedAt` DATE NOT NULL,
    `sku` VARCHAR(255) NOT NULL,
    `quantity` INT NOT NULL,
    PRIMARY KEY(`id`)
);
ALTER TABLE
    `ProductVariant` ADD INDEX `productvariant_productid_index`(`productId`);
CREATE TABLE `Payment`(
    `id` CHAR(36) NOT NULL,
    `orderId` VARCHAR(255) NOT NULL,
    `userId` CHAR(36) NOT NULL,
    `amount` DECIMAL(8, 2) NOT NULL,
    `access_key` VARCHAR(255) NOT NULL,
    `partner_code` VARCHAR(255) NOT NULL,
    `redirect_url` VARCHAR(255) NOT NULL,
    `ipn_url` VARCHAR(255) NOT NULL,
    `extra_data` VARCHAR(255) NOT NULL,
    `request_type` VARCHAR(255) NOT NULL,
    `signature` VARCHAR(255) NOT NULL,
    `status` ENUM('') NOT NULL,
    `createdAt` DATE NOT NULL,
    `updatedAt` DATE NOT NULL,
    PRIMARY KEY(`id`)
);
ALTER TABLE
    `Payment` ADD INDEX `payment_orderid_index`(`orderId`);
ALTER TABLE
    `Payment` ADD INDEX `payment_userid_index`(`userId`);
CREATE TABLE `Order`(
    `id` CHAR(36) NOT NULL,
    `userId` CHAR(36) NOT NULL,
    `productId` CHAR(36) NOT NULL,
    `createdAt` DATE NOT NULL,
    `status` ENUM('') NOT NULL,
    PRIMARY KEY(`id`)
);
ALTER TABLE
    `Order` ADD INDEX `order_userid_index`(`userId`);
ALTER TABLE
    `Order` ADD INDEX `order_productid_index`(`productId`);
CREATE TABLE `Voucher`(
    `id` CHAR(36) NOT NULL,
    `percent` FLOAT(53) NOT NULL,
    `userId` CHAR(36) NOT NULL,
    `expTime` DATE NOT NULL,
    `createdAt` DATE NOT NULL,
    `quantity` INT NOT NULL,
    PRIMARY KEY(`id`)
);
ALTER TABLE
    `Voucher` ADD INDEX `voucher_userid_index`(`userId`);
CREATE TABLE `OrderCart`(
    `id` CHAR(36) NOT NULL,
    `userId` CHAR(36) NOT NULL,
    `createdAt` DATE NOT NULL,
    PRIMARY KEY(`id`)
);
ALTER TABLE
    `OrderCart` ADD INDEX `ordercart_userid_index`(`userId`);
CREATE TABLE `SavedProduct`(
    `id` CHAR(36) NOT NULL,
    `productId` CHAR(36) NOT NULL,
    `quantity` INT NOT NULL,
    `createdAt` DATE NOT NULL,
    PRIMARY KEY(`id`)
);
ALTER TABLE
    `SavedProduct` ADD INDEX `savedproduct_productid_index`(`productId`);
ALTER TABLE
    `Product` ADD CONSTRAINT `product_userid_foreign` FOREIGN KEY(`userId`) REFERENCES `SavedProduct`(`id`);
ALTER TABLE
    `Product` ADD CONSTRAINT `product_brandid_foreign` FOREIGN KEY(`brandId`) REFERENCES `Brand`(`userId`);
ALTER TABLE
    `Voucher` ADD CONSTRAINT `voucher_percent_foreign` FOREIGN KEY(`percent`) REFERENCES `User`(`hashPassword`);
ALTER TABLE
    `Brand` ADD CONSTRAINT `brand_id_foreign` FOREIGN KEY(`id`) REFERENCES `User`(`updatedAt`);
ALTER TABLE
    `OrderCart` ADD CONSTRAINT `ordercart_id_foreign` FOREIGN KEY(`id`) REFERENCES `User`(`createdAt`);
ALTER TABLE
    `SavedProduct` ADD CONSTRAINT `savedproduct_quantity_foreign` FOREIGN KEY(`quantity`) REFERENCES `OrderCart`(`userId`);
ALTER TABLE
    `Order` ADD CONSTRAINT `order_id_foreign` FOREIGN KEY(`id`) REFERENCES `User`(`email`);
ALTER TABLE
    `ProductVariant` ADD CONSTRAINT `productvariant_id_foreign` FOREIGN KEY(`id`) REFERENCES `Product`(`category`);
ALTER TABLE
    `ProductVariant` ADD CONSTRAINT `productvariant_sku_foreign` FOREIGN KEY(`sku`) REFERENCES `Order`(`userId`);
ALTER TABLE
    `OutBox` ADD CONSTRAINT `outbox_id_foreign` FOREIGN KEY(`id`) REFERENCES `User`(`updatedAt`);
ALTER TABLE
    `Voucher` ADD CONSTRAINT `voucher_percent_foreign` FOREIGN KEY(`percent`) REFERENCES `Order`(`userId`);
ALTER TABLE
    `Payment` ADD CONSTRAINT `payment_id_foreign` FOREIGN KEY(`id`) REFERENCES `Order`(`id`);
ALTER TABLE
    `Session` ADD CONSTRAINT `session_id_foreign` FOREIGN KEY(`id`) REFERENCES `User`(`id`);