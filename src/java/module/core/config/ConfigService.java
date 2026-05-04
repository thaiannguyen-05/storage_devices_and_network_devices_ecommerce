package module.core.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;

public class ConfigService {
    private static final Dotenv dotenv = loadDotenv();

    public static String get(String key) {
        String value = readSystemEnv(key);
        if (value != null) {
            return value;
        }

        value = readSystemProperty(key);
        if (value != null) {
            return value;
        }

        return readDotenv(key);
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

    private static String readSystemEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private static String readSystemProperty(String key) {
        String value = System.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private static String readDotenv(String key) {
        String value = dotenv.get(key);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private static Dotenv loadDotenv() {
        String[] candidateDirs = new String[]{
            System.getProperty("user.dir"),
            System.getProperty("com.sun.aas.instanceRoot"),
            System.getProperty("com.sun.aas.installRoot")
        };

        for (String candidateDir : candidateDirs) {
            Dotenv loaded = tryLoadDotenv(candidateDir);
            if (loaded != null) {
                return loaded;
            }
        }

        return Dotenv.configure()
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
    }

    private static Dotenv tryLoadDotenv(String directory) {
        if (directory == null || directory.trim().isEmpty()) {
            return null;
        }

        File envFile = new File(directory, ".env");
        if (!envFile.isFile()) {
            return null;
        }

        return Dotenv.configure()
                .directory(envFile.getParentFile().getAbsolutePath())
                .filename(envFile.getName())
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
    }
}
