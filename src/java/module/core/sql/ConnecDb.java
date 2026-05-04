package module.core.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnecDb {

    private static final HikariDataSource dataSource;

    static {
        String host = "20.189.125.186";
        int port = 3306;
        String dbName = "mydb";
        String user = "andev";
        String password = "andev123";

        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
                + "&useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci";

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);
        config.setValidationTimeout(5000);

        dataSource = new HikariDataSource(config);
        System.out.println("DB Pool initialized: host=" + host + ", port=" + port + ", db=" + dbName + ", user=" + user);
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
