package common.logger.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class BodyExtractor {
    public static final int REQUEST_BODY_LIMIT_BYTES = 10 * 1024;
    public static final int RESPONSE_BODY_LIMIT_BYTES = 50 * 1024;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern JSON_SENSITIVE_FIELD = Pattern.compile(
            "(?i)(\"[^\"]*(password|token|secret|key|creditCard)[^\"]*\"\\s*:\\s*\")([^\"]*)(\")"
    );
    private static final Pattern FORM_SENSITIVE_FIELD = Pattern.compile(
            "(?i)(^|&)([^=&]*(password|token|secret|key|creditCard)[^=&]*=)([^&]*)"
    );

    private BodyExtractor() {
    }

    public static String extractRequestBody(String method, String contentType, byte[] body, String characterEncoding) {
        if (isBlankMethod(method) || "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) {
            return "";
        }

        if (body == null || body.length == 0) {
            return "";
        }

        String normalizedContentType = normalize(contentType);
        if (normalizedContentType.startsWith("multipart/")) {
            return "[multipart content skipped]";
        }

        if (isBinaryContent(normalizedContentType)) {
            return "[binary content]";
        }

        String decoded = decode(body, characterEncoding);
        String limited = truncate(decoded, REQUEST_BODY_LIMIT_BYTES);

        if (normalizedContentType.contains("application/json")) {
            return maskSensitiveContent(prettyJson(limited));
        }

        if (normalizedContentType.contains("application/x-www-form-urlencoded")) {
            return maskSensitiveContent(limited);
        }

        return maskSensitiveContent(limited);
    }

    public static String extractResponseBody(String contentType, byte[] body, String characterEncoding) {
        if (body == null || body.length == 0) {
            return "";
        }

        String normalizedContentType = normalize(contentType);
        if (normalizedContentType.contains("text/html")) {
            return skippedHtmlMessage(body.length);
        }

        if (!isTextResponse(normalizedContentType)) {
            return isBinaryContent(normalizedContentType) ? "[binary response]" : "";
        }

        String decoded = decode(body, characterEncoding);
        return maskSensitiveContent(truncate(decoded, RESPONSE_BODY_LIMIT_BYTES));
    }

    public static Map<String, String[]> maskParameters(Map<String, String[]> parameters) {
        Map<String, String[]> masked = new LinkedHashMap<>();
        if (parameters == null) {
            return masked;
        }

        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            if (isSensitiveKey(key)) {
                masked.put(key, new String[]{"***"});
                continue;
            }

            String[] copiedValues = values == null ? new String[0] : values.clone();
            masked.put(key, copiedValues);
        }

        return masked;
    }

    public static Map<String, String[]> parseFormEncoded(String body, String characterEncoding) {
        Map<String, String[]> parameters = new LinkedHashMap<>();
        if (body == null || body.isBlank()) {
            return parameters;
        }

        for (String pair : body.split("&")) {
            if (pair.isBlank()) {
                continue;
            }

            String[] parts = pair.split("=", 2);
            String key = urlDecode(parts[0], characterEncoding);
            String value = parts.length > 1 ? urlDecode(parts[1], characterEncoding) : "";
            parameters.merge(key, new String[]{value}, BodyExtractor::appendValue);
        }

        return parameters;
    }

    public static String maskSensitiveContent(String body) {
        if (body == null || body.isBlank()) {
            return body == null ? "" : body;
        }

        String masked = JSON_SENSITIVE_FIELD.matcher(body).replaceAll("$1***$4");
        return FORM_SENSITIVE_FIELD.matcher(masked).replaceAll("$1$2***");
    }

    public static String truncate(String value, int maxBytes) {
        if (value == null) {
            return "";
        }

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) {
            return value;
        }

        return new String(bytes, 0, maxBytes, StandardCharsets.UTF_8)
                + " [truncated - size: " + bytes.length + " bytes]";
    }

    public static boolean isBinaryContent(String contentType) {
        String normalized = normalize(contentType);
        return normalized.startsWith("image/")
                || normalized.startsWith("audio/")
                || normalized.startsWith("video/")
                || normalized.contains("application/pdf")
                || normalized.contains("application/zip")
                || normalized.contains("application/octet-stream")
                || normalized.contains("font/");
    }

    private static boolean isTextResponse(String contentType) {
        String normalized = normalize(contentType);
        return normalized.contains("application/json")
                || normalized.startsWith("text/plain")
                || normalized.contains("application/xml")
                || normalized.contains("+json");
    }

    private static String skippedHtmlMessage(int size) {
        return "[html response skipped - size: " + size + " bytes]";
    }

    private static String prettyJson(String value) {
        try {
            Object parsed = OBJECT_MAPPER.readValue(value, Object.class);
            return OBJECT_MAPPER.writeValueAsString(parsed);
        } catch (Exception ignored) {
            return value;
        }
    }

    private static String decode(byte[] body, String characterEncoding) {
        Charset charset = StandardCharsets.UTF_8;
        if (characterEncoding != null && !characterEncoding.isBlank()) {
            try {
                charset = Charset.forName(characterEncoding);
            } catch (Exception ignored) {
                charset = StandardCharsets.UTF_8;
            }
        }
        return new String(body, charset);
    }

    private static String urlDecode(String value, String characterEncoding) {
        try {
            return URLDecoder.decode(value, characterEncoding == null || characterEncoding.isBlank()
                    ? StandardCharsets.UTF_8.name()
                    : characterEncoding);
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    private static String[] appendValue(String[] existing, String[] next) {
        String[] combined = new String[existing.length + next.length];
        System.arraycopy(existing, 0, combined, 0, existing.length);
        System.arraycopy(next, 0, combined, existing.length, next.length);
        return combined;
    }

    private static boolean isSensitiveKey(String key) {
        String normalized = normalize(key);
        return normalized.contains("password")
                || normalized.contains("token")
                || normalized.contains("secret")
                || normalized.contains("key")
                || normalized.contains("creditcard");
    }

    private static boolean isBlankMethod(String method) {
        return method == null || method.isBlank();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
