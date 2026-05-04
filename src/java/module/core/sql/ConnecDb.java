package module.core.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import module.core.config.ConfigService;

public class ConnecDb {

    private static final HikariDataSource dataSource;

    static {
        String host = ConfigService.get("DB_HOST");
        int port = ConfigService.getInt("DB_PORT", 3306);
        String dbName = ConfigService.get("DB_NAME");
        String user = ConfigService.get("DB_USER");
        String password = ConfigService.get("DB_PASSWORD");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);
        config.setValidationTimeout(5000);

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void main(String[] args) {
        try (Connection ignored = getConnection()) {
            System.out.println("Connection successful!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
