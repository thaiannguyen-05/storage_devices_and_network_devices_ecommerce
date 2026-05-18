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
}
