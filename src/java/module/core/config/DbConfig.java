package module.core.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConfig {
    private static final ConfigService CONFIG = ConfigService.getInstance();
    public static final String DRIVER_CLASS = CONFIG.get("DB_DRIVER_CLASS", "com.mysql.cj.jdbc.Driver");
    private static volatile boolean driverLoaded;

    private DbConfig() {
    }

    private static volatile boolean schemaChecking = false;
    private static volatile boolean schemaChecked = false;

    public static Connection getConnection() throws SQLException {
        loadDriver();
        Connection conn = DriverManager.getConnection(getJdbcUrl(), getUsername(), getPassword());
        if (!schemaChecked && !schemaChecking) {
            schemaChecking = true;
            checkAndRepairSchema(conn);
            schemaChecking = false;
            schemaChecked = true;
        }
        return conn;
    }

    public static String getJdbcUrl() {
        String explicitUrl = CONFIG.get("DB_JDBC_URL");
        if (explicitUrl != null && !explicitUrl.trim().isEmpty()) {
            return explicitUrl;
        }
        String host = CONFIG.get("DB_HOST", "localhost");
        int port = CONFIG.getInt("DB_PORT", 3306);
        String database = CONFIG.require("DB_NAME");
        String params = CONFIG.get("DB_PARAMS", "useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC");
        return "jdbc:mysql://" + host + ":" + port + "/" + database + "?" + params;
    }

    public static String getUsername() {
        return CONFIG.get("DB_USER", "root");
    }

    public static String getPassword() {
        return CONFIG.get("DB_PASSWORD", "");
    }

    private static void loadDriver() {
        if (driverLoaded) {
            return;
        }
        synchronized (DbConfig.class) {
            if (driverLoaded) {
                return;
            }
            try {
                Class.forName(DRIVER_CLASS);
                driverLoaded = true;
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException("MySQL JDBC driver not found. Add MySQL Connector/J to WEB-INF/lib or GlassFish domain lib.", ex);
            }
        }
    }

    private static void checkAndRepairSchema(Connection conn) {
        try {
            java.util.Set<String> columns = new java.util.HashSet<>();
            try (java.sql.ResultSet rs = conn.getMetaData().getColumns(null, null, "Order", null)) {
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME").toLowerCase());
                }
            }
            
            if (columns.isEmpty()) {
                try (java.sql.Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM `Order`")) {
                    while (rs.next()) {
                        columns.add(rs.getString("Field").toLowerCase());
                    }
                }
            }
            
            System.out.println("[Schema Migrator] Existing columns in Order: " + columns);
            
            alterIfMissing(conn, columns, "variantId", "ALTER TABLE `Order` ADD COLUMN `variantId` CHAR(36) NULL");
            alterIfMissing(conn, columns, "quantity", "ALTER TABLE `Order` ADD COLUMN `quantity` INT NOT NULL DEFAULT 1");
            alterIfMissing(conn, columns, "phone", "ALTER TABLE `Order` ADD COLUMN `phone` VARCHAR(20) NULL");
            alterIfMissing(conn, columns, "address", "ALTER TABLE `Order` ADD COLUMN `address` VARCHAR(500) NULL");
            alterIfMissing(conn, columns, "updatedAt", "ALTER TABLE `Order` ADD COLUMN `updatedAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
            alterIfMissing(conn, columns, "customerName", "ALTER TABLE `Order` ADD COLUMN `customerName` VARCHAR(255) NULL");
            alterIfMissing(conn, columns, "email", "ALTER TABLE `Order` ADD COLUMN `email` VARCHAR(255) NULL");
            alterIfMissing(conn, columns, "note", "ALTER TABLE `Order` ADD COLUMN `note` TEXT NULL");
            alterIfMissing(conn, columns, "paymentMethod", "ALTER TABLE `Order` ADD COLUMN `paymentMethod` VARCHAR(50) NULL");
            alterIfMissing(conn, columns, "voucherId", "ALTER TABLE `Order` ADD COLUMN `voucherId` CHAR(36) NULL");
            alterIfMissing(conn, columns, "totalAmount", "ALTER TABLE `Order` ADD COLUMN `totalAmount` DECIMAL(12,2) NULL");
            ensureProductReviewSchema(conn);
            seedProductReviewsIfEmpty(conn);
            
            System.out.println("[Schema Migrator] Schema check and repair finished successfully!");
        } catch (Exception e) {
            System.err.println("[Schema Migrator] Error checking/repairing schema: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void alterIfMissing(Connection conn, java.util.Set<String> columns, String columnName, String sql) {
        if (!columns.contains(columnName.toLowerCase())) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("[Schema Migrator] Added column " + columnName + " to Order table.");
            } catch (Exception e) {
                System.err.println("[Schema Migrator] Failed to add column " + columnName + ": " + e.getMessage());
            }
        }
    }

    private static void ensureProductReviewSchema(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS `ProductReview` ("
                + "`id` CHAR(36) NOT NULL,"
                + "`productId` CHAR(36) NOT NULL,"
                + "`userId` CHAR(36) NOT NULL,"
                + "`rating` INT NOT NULL,"
                + "`comment` VARCHAR(1000) NULL,"
                + "`status` ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'APPROVED',"
                + "`createdAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "`updatedAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "PRIMARY KEY (`id`),"
                + "UNIQUE KEY `productreview_userid_productid_unique` (`userId`, `productId`),"
                + "KEY `productreview_product_status_createdat_index` (`productId`, `status`, `createdAt`),"
                + "CONSTRAINT `productreview_rating_check` CHECK (`rating` BETWEEN 1 AND 5),"
                + "CONSTRAINT `productreview_productid_foreign` FOREIGN KEY (`productId`) REFERENCES `Product` (`id`) ON DELETE CASCADE,"
                + "CONSTRAINT `productreview_userid_foreign` FOREIGN KEY (`userId`) REFERENCES `User` (`id`) ON DELETE CASCADE"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        try (java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("[Schema Migrator] Ensured ProductReview table exists.");
        } catch (Exception e) {
            System.err.println("[Schema Migrator] Failed to ensure ProductReview table: " + e.getMessage());
        }
    }

    private static void seedProductReviewsIfEmpty(Connection conn) {
        if (count(conn, "SELECT COUNT(*) FROM `ProductReview`") > 0) {
            return;
        }

        java.util.List<String> productIds = queryIds(conn, "SELECT id FROM `Product` WHERE status = 'ACTIVE' ORDER BY createdAt DESC LIMIT 12");
        java.util.List<String> userIds = queryIds(conn, "SELECT id FROM `User` WHERE status = 'ACTIVE' ORDER BY createdAt ASC LIMIT 5");
        if (productIds.isEmpty() || userIds.isEmpty()) {
            System.out.println("[Schema Migrator] Skipped ProductReview seed because Product or User data is empty.");
            return;
        }

        String[] comments = new String[] {
            "San pham chay on dinh, dung dung nhu mo ta.",
            "Dong goi can than, chat luong tot trong tam gia.",
            "Hieu nang tot, cai dat nhanh va de su dung.",
            "Da dung vai ngay, toc do va do on dinh deu tot.",
            "Phu hop nhu cau lam viec va sao luu du lieu.",
            "Hang dep, giao dien quan ly ro rang.",
            "Ket noi on dinh, khong gap loi trong qua trinh su dung.",
            "Gia hop ly so voi chat luong nhan duoc.",
            "San pham chac chan, hoat dong em.",
            "Se tiep tuc ung ho neu co nhu cau nang cap.",
            "Dung cho he thong gia dinh rat on.",
            "Trai nghiem tot, khong phat sinh loi."
        };

        String sql = "INSERT IGNORE INTO `ProductReview` "
                + "(`id`, `productId`, `userId`, `rating`, `comment`, `status`, `createdAt`, `updatedAt`) "
                + "VALUES (?, ?, ?, ?, ?, 'APPROVED', NOW(), NOW())";
        int inserted = 0;
        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < productIds.size(); i++) {
                ps.setString(1, java.util.UUID.randomUUID().toString());
                ps.setString(2, productIds.get(i));
                ps.setString(3, userIds.get(i % userIds.size()));
                ps.setInt(4, 3 + (i % 3));
                ps.setString(5, comments[i % comments.length]);
                inserted += ps.executeUpdate();
            }
            System.out.println("[Schema Migrator] Seeded " + inserted + " ProductReview rows.");
        } catch (Exception e) {
            System.err.println("[Schema Migrator] Failed to seed ProductReview data: " + e.getMessage());
        }
    }

    private static java.util.List<String> queryIds(Connection conn, String sql) {
        java.util.List<String> ids = new java.util.ArrayList<>();
        try (java.sql.Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
        } catch (Exception e) {
            System.err.println("[Schema Migrator] Failed query: " + e.getMessage());
        }
        return ids;
    }

    private static int count(Connection conn, String sql) {
        try (java.sql.Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            System.err.println("[Schema Migrator] Failed count: " + e.getMessage());
            return 0;
        }
    }
}
