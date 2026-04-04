/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package module.core.sql;
import module.core.config.ConfigService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author An
 */
public class ConnecDb {
    public static Connection getConnection() throws SQLException {
        String host = ConfigService.get("DB_HOST");
        int port = ConfigService.getInt("DB_PORT", 3306);
        String dbName = ConfigService.get("DB_NAME");
        String user = ConfigService.get("DB_USER");
        String password = ConfigService.get("DB_PASSWORD");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?useSSL=false&serverTimezone=UTC";

        return DriverManager.getConnection(url, user, password);
    }
}
