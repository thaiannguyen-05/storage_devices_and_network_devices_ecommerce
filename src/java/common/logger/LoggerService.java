package common.logger;

import common.logger.audit.AuditLogEntry;
import common.middleware.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggerService {
    private static final Object LOCK = new Object();
    private static volatile boolean handlersConfigured = false;

    private final Logger logger;

    private LoggerService(Class<?> clazz) {
        this.logger = Logger.getLogger(clazz.getName());
        configureHandlersIfNeeded();
    }

    public static LoggerService getLogger(Class<?> clazz) {
        return new LoggerService(clazz);
    }

    public void info(String message) {
        log(Level.INFO, null, message, null);
    }

    public void info(HttpServletRequest request, String message) {
        log(Level.INFO, request, message, null);
    }

    public void warn(String message) {
        log(Level.WARNING, null, message, null);
    }

    public void warn(HttpServletRequest request, String message) {
        log(Level.WARNING, request, message, null);
    }

    public void debug(String message) {
        log(Level.FINE, null, message, null);
    }

    public void debug(HttpServletRequest request, String message) {
        log(Level.FINE, request, message, null);
    }

    public void error(String message, Throwable throwable) {
        log(Level.SEVERE, null, message, throwable);
    }

    public void error(HttpServletRequest request, String message, Throwable throwable) {
        log(Level.SEVERE, request, message, throwable);
    }

    public void audit(AuditLogEntry entry) {
        if (entry == null) {
            return;
        }

        logger.log(Level.INFO, toJson(entry.toMap()));
    }

    private void log(Level level, HttpServletRequest request, String message, Throwable throwable) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", Instant.now().toString());
        payload.put("level", level.getName());
        payload.put("logger", logger.getName());
        payload.put("thread", Thread.currentThread().getName());
        payload.put("message", sanitize(message));

        if (request != null) {
            payload.put("requestId", resolveRequestId(request));
            payload.put("method", request.getMethod());
            payload.put("path", request.getRequestURI());
        }

        if (throwable != null) {
            payload.put("errorType", throwable.getClass().getName());
            payload.put("errorMessage", sanitize(throwable.getMessage()));
            payload.put("stackTrace", sanitize(stackTraceOf(throwable)));
        }

        logger.log(level, toJson(payload));
    }

    private String resolveRequestId(HttpServletRequest request) {
        Object requestIdAttr = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        if (requestIdAttr instanceof String value && !value.isBlank()) {
            return value;
        }

        String headerValue = request.getHeader(RequestIdFilter.REQUEST_ID_KEY);
        if (headerValue != null && !headerValue.isBlank()) {
            return headerValue;
        }

        return "N/A";
    }

    private void configureHandlersIfNeeded() {
        if (handlersConfigured) {
            return;
        }

        synchronized (LOCK) {
            if (handlersConfigured) {
                return;
            }

            try {
                Path logsDir = Path.of("logs");
                Files.createDirectories(logsDir);

                Logger root = Logger.getLogger("");
                for (Handler handler : root.getHandlers()) {
                    root.removeHandler(handler);
                }

                Formatter jsonFormatter = new JsonLineFormatter();

                ConsoleHandler consoleHandler = new ConsoleHandler();
                consoleHandler.setLevel(Level.ALL);
                consoleHandler.setFormatter(jsonFormatter);

                FileHandler fileHandler = new FileHandler("logs/application.log", true);
                fileHandler.setLevel(Level.ALL);
                fileHandler.setFormatter(jsonFormatter);

                root.addHandler(consoleHandler);
                root.addHandler(fileHandler);
                root.setLevel(Level.ALL);

                handlersConfigured = true;
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize JSON log handlers", e);
            }
        }
    }

    private String stackTraceOf(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.flush();
        return stringWriter.toString();
    }

    private String toJson(Map<String, Object> payload) {
        return toJsonObject(payload);
    }

    private String toJsonObject(Map<?, ?> payload) {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        boolean first = true;

        for (Map.Entry<?, ?> entry : payload.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;

            builder.append('"').append(escapeJson(String.valueOf(entry.getKey()))).append('"').append(':');
            builder.append(toJsonValue(entry.getValue()));
        }

        builder.append('}');
        return builder.toString();
    }

    private String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }

        if (value instanceof Map<?, ?> map) {
            return toJsonObject(map);
        }

        if (value instanceof Collection<?> collection) {
            return toJsonArray(collection.toArray());
        }

        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            Object[] values = new Object[length];
            for (int i = 0; i < length; i++) {
                values[i] = Array.get(value, i);
            }
            return toJsonArray(values);
        }

        return '"' + escapeJson(String.valueOf(value)) + '"';
    }

    private String toJsonArray(Object[] values) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(toJsonValue(values[i]));
        }
        builder.append(']');
        return builder.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String sanitize(String message) {
        if (message == null) {
            return "";
        }
        return message.trim();
    }

    private static class JsonLineFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return record.getMessage() + System.lineSeparator();
        }
    }
}
