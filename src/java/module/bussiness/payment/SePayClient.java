package module.bussiness.payment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import module.core.config.ConfigService;

public class SePayClient {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String INIT_PATH = "/v1/checkout/init";
    private static final String DEFAULT_QUERY_PATH = "/v1/checkout/query";
    private static final String DEFAULT_CANCEL_PATH = "/v1/order/cancel";
    private static final String DEFAULT_VOID_PATH = "/v1/order/voidTransaction";

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String secretKey;

    public SePayClient() {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.baseUrl = normalizeBaseUrl(ConfigService.getOrDefault("SEPAY_URL", "https://pgapi.sepay.vn"));
        this.secretKey = ConfigService.getOrDefault("SEPAY_SECRET_KEY", "");
    }

    public Map<String, Object> initCheckout(Map<String, Object> payload) {
        return sendJsonPost(INIT_PATH, payload);
    }

    public Map<String, Object> queryTransaction(Map<String, Object> payload) {
        String queryPath = ConfigService.getOrDefault("SEPAY_QUERY_PATH", DEFAULT_QUERY_PATH);
        return sendJsonPost(queryPath, payload);
    }

    public Map<String, Object> cancelOrder(Map<String, Object> payload) {
        String cancelPath = ConfigService.getOrDefault("SEPAY_CANCEL_PATH", DEFAULT_CANCEL_PATH);
        return sendJsonPost(cancelPath, payload);
    }

    public Map<String, Object> voidTransaction(Map<String, Object> payload) {
        String voidPath = ConfigService.getOrDefault("SEPAY_VOID_PATH", DEFAULT_VOID_PATH);
        return sendJsonPost(voidPath, payload);
    }

    private Map<String, Object> sendJsonPost(String path, Map<String, Object> payload) {
        try {
            String requestBody = OBJECT_MAPPER.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(resolvePath(path)))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("X-SEPAY-SECRET", secretKey)
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            Map<String, Object> result = new HashMap<>();
            result.put("httpStatus", response.statusCode());

            Map<String, Object> responseBody = parseBody(response.body());
            result.put("body", responseBody);
            result.put("success", response.statusCode() >= 200 && response.statusCode() < 300);
            if (!result.containsKey("message") && responseBody.get("message") != null) {
                result.put("message", String.valueOf(responseBody.get("message")));
            }
            return result;
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "httpStatus", 502,
                    "message", "Không gọi được SePay API: " + e.getMessage()
            );
        }
    }

    private Map<String, Object> parseBody(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return new HashMap<>();
        }

        try {
            return OBJECT_MAPPER.readValue(responseBody, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            Map<String, Object> raw = new HashMap<>();
            raw.put("raw", responseBody);
            return raw;
        }
    }

    private String resolvePath(String path) {
        if (path == null || path.isBlank()) {
            return baseUrl;
        }

        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }

        if (path.startsWith("/")) {
            return baseUrl + path;
        }

        return baseUrl + "/" + path;
    }

    private String normalizeBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return "https://pgapi.sepay.vn";
        }

        String trimmed = value.trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
