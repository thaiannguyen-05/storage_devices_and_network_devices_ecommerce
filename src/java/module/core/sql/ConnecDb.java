package module.core.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import module.core.config.ConfigService;

public class ConnecDb {
    private static final String PG_DRIVER_CLASS = "org.postgresql.Driver";

    private static final HikariDataSource dataSource;

    static {
        String databaseUrl = ConfigService.get("DATABASE_URL");

        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            try {
                Class.forName(PG_DRIVER_CLASS);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("PostgreSQL JDBC driver not found on runtime classpath.", e);
            }

            HikariConfig config = new HikariConfig();
            config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
            config.addDataSourceProperty("url", databaseUrl.startsWith("jdbc:") ? databaseUrl : "jdbc:" + databaseUrl);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(10000);
            config.setValidationTimeout(5000);
            dataSource = new HikariDataSource(config);
        } else {
            // Fallback: individual DB_HOST/DB_PORT/DB_NAME
            String host = ConfigService.get("DB_HOST");
            int port = ConfigService.getInt("DB_PORT", 5432);
            String dbName = ConfigService.get("DB_NAME");
            String user = ConfigService.get("DB_USER");
            String password = ConfigService.get("DB_PASSWORD");

            try {
                Class.forName(PG_DRIVER_CLASS);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("PostgreSQL JDBC driver not found on runtime classpath.", e);
            }

            HikariConfig config = new HikariConfig();
            config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
            config.addDataSourceProperty("serverName", host);
            config.addDataSourceProperty("portNumber", port);
            config.addDataSourceProperty("databaseName", dbName);
            config.addDataSourceProperty("sslmode", "require");
            config.setUsername(user);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(10000);
            config.setValidationTimeout(5000);
            dataSource = new HikariDataSource(config);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
