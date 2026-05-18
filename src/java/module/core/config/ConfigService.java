package module.core.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigService {
    private static final ConfigService INSTANCE = new ConfigService();

    private final Map<String, String> values;

    private ConfigService() {
        this.values = Collections.unmodifiableMap(loadEnvFile());
    }

    public static ConfigService getInstance() {
        return INSTANCE;
    }

    public String get(String key) {
        String value = System.getenv(key);
        if (value != null) {
            return value;
        }
        return values.get(key);
    }

    public String get(String key, String fallback) {
        String value = get(key);
        return isBlank(value) ? fallback : value;
    }

    public int getInt(String key, int fallback) {
        String value = get(key);
        if (isBlank(value)) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public long getLong(String key, long fallback) {
        String value = get(key);
        if (isBlank(value)) {
            return fallback;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public String require(String key) {
        String value = get(key);
        if (isBlank(value)) {
            throw new IllegalStateException("Missing required config value: " + key);
        }
        return value;
    }

    private Map<String, String> loadEnvFile() {
        Map<String, String> loadedValues = new HashMap<String, String>();
        File envFile = findEnvFile();
        if (envFile != null && envFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                readLines(reader, loadedValues);
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to read .env file: " + envFile.getAbsolutePath(), ex);
            }
            return loadedValues;
        }

        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(".env");
        if (stream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                readLines(reader, loadedValues);
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to read .env classpath resource", ex);
            }
        }
        return loadedValues;
    }

    private void readLines(BufferedReader reader, Map<String, String> target) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            parseLine(line, target);
        }
    }

    private File findEnvFile() {
        String explicitPath = System.getProperty("app.env.path");
        if (!isBlank(explicitPath)) {
            File explicitFile = new File(explicitPath);
            if (explicitFile.isFile()) {
                return explicitFile;
            }
        }

        File current = new File(System.getProperty("user.dir"));
        while (current != null) {
            File candidate = new File(current, ".env");
            if (candidate.isFile()) {
                return candidate;
            }

            File webInfCandidate = new File(current, "web/WEB-INF/.env");
            if (webInfCandidate.isFile()) {
                return webInfCandidate;
            }

            current = current.getParentFile();
        }
        return null;
    }

    private void parseLine(String rawLine, Map<String, String> target) {
        String line = rawLine.trim();
        if (line.isEmpty() || line.startsWith("#")) {
            return;
        }
        if (line.startsWith("export ")) {
            line = line.substring("export ".length()).trim();
        }

        int separatorIndex = line.indexOf('=');
        if (separatorIndex <= 0) {
            return;
        }

        String key = line.substring(0, separatorIndex).trim();
        String value = line.substring(separatorIndex + 1).trim();
        target.put(key, unquote(value));
    }

    private String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
