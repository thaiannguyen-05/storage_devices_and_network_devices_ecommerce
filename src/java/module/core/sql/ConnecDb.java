/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package module.core.sql;

import module.core.config.ConfigService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 *
 * @author An
 */
public class ConnecDb {

    private static HikariDataSource dataSource;

    static {
        String host = ConfigService.get("DB_HOST");
        int port = ConfigService.getInt("DB_PORT", 3306);
        String dbName = ConfigService.get("DB_NAME");
        String user = ConfigService.get("DB_USER");
        String password = ConfigService.get("DB_PASSWORD");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?useSSL=false&serverTimezone=UTC";

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);

        config.setConnectionTimeout(3000);

        dataSource = new HikariDataSource(config);

        System.out.println("✅ DB Pool initialized");
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
