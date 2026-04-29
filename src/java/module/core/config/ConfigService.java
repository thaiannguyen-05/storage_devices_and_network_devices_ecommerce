/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package module.core.config;
import io.github.cdimascio.dotenv.Dotenv;

/**
 *
 * @author An
 */
public class ConfigService {
     private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

     private static final Dotenv dotenvProject = Dotenv.configure()
            .directory("D:/Learn/BaiTapLon_Java_Web/storage_devices_and_network_devices_ecommerce")
            .filename(".env")
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    public static String get(String key) {
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue;
        }

        String value = dotenv.get(key);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }

        return dotenvProject.get(key);
    }

    public static String getOrDefault(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
