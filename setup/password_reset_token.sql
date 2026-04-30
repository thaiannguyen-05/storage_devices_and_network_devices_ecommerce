CREATE TABLE IF NOT EXISTS `PasswordResetToken` (
  `id` VARCHAR(36) NOT NULL,
  `userId` VARCHAR(36) NOT NULL,
  `tokenHash` VARCHAR(64) NOT NULL,
  `expiresAt` DATETIME NOT NULL,
  `usedAt` DATETIME NULL,
  `createdAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_password_reset_token_hash` (`tokenHash`),
  KEY `idx_password_reset_user_id` (`userId`),
  CONSTRAINT `fk_password_reset_user`
    FOREIGN KEY (`userId`) REFERENCES `User`(`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
