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

    public static Connection getConnection() throws SQLException {
        loadDriver();
        return DriverManager.getConnection(getJdbcUrl(), getUsername(), getPassword());
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
}
