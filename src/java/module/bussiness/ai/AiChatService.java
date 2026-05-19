package module.bussiness.ai;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import module.bussiness.ai.dto.ChatRequestDto;
import module.bussiness.ai.dto.MessageDto;
import module.bussiness.ai.response_dto.ChatResponseDto;
import module.bussiness.ai.response_dto.ChatStreamChunk;

public class AiChatService {
    private final HttpClient httpClient = createHttpClient();
    private final AiShopContextService shopContextService = new AiShopContextService();

    public interface ChunkCallback {
        boolean onChunk(ChatStreamChunk chunk) throws IOException;
    }

    private HttpClient createHttpClient() {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(AiChatConfig.CONNECT_TIMEOUT_MS));
        if (AiChatConfig.ALLOW_INSECURE_SSL) {
            builder.sslContext(createInsecureSslContext());
        }
        return builder.build();
    }

    private SSLContext createInsecureSslContext() {
        try {
            TrustManager[] trustAllManagers = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
            };
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustAllManagers, new SecureRandom());
            return context;
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Khong the khoi tao SSL context cho AI client.", ex);
        }
    }

    public ChatResponseDto chat(ChatRequestDto dto, String userId) {
        ChatResponseDto response = new ChatResponseDto();
        String validationError = validateRequest(dto);
        if (validationError != null) {
            response.setSuccess(false);
            response.setErrorMessage(validationError);
            return response;
        }

        try {
            HttpResponse<String> apiResponse = httpClient.send(
                    buildRequest(AiChatConfig.CHAT_ENDPOINT, buildRequestBody(dto, userId, false).toString()).build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int status = apiResponse.statusCode();
            String responseBody = apiResponse.body();

            if (status >= 200 && status < 300) {
                response.setSuccess(true);
                response.setResponseText(extractText(responseBody));
                return response;
            }

            response.setSuccess(false);
            response.setErrorMessage(resolveErrorMessage(status, responseBody));
            return response;
        } catch (IOException ex) {
            response.setSuccess(false);
            response.setErrorMessage(buildConnectionErrorMessage(ex));
            return response;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            response.setSuccess(false);
            response.setErrorMessage("Ket noi toi dich vu AI bi gian doan.");
            return response;
        }
    }

    public void streamChat(ChatRequestDto dto, String userId, ChunkCallback callback) throws IOException {
        String validationError = validateRequest(dto);
        if (validationError != null) {
            callback.onChunk(new ChatStreamChunk("", true, validationError));
            return;
        }

        boolean doneSent = false;
        try {
            HttpRequest request = buildRequest(AiChatConfig.STREAM_ENDPOINT, buildRequestBody(dto, userId, true).toString())
                    .header("Accept", "text/event-stream")
                    .build();
            HttpResponse<InputStream> apiResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            int status = apiResponse.statusCode();

            if (status < 200 || status >= 300) {
                String errorBody = readFully(apiResponse.body());
                callback.onChunk(new ChatStreamChunk("", true, resolveErrorMessage(status, errorBody)));
                doneSent = true;
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(apiResponse.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }

                    String payload = line.substring(5).trim();
                    if (payload.isEmpty()) {
                        continue;
                    }
                    if ("[DONE]".equals(payload)) {
                        doneSent = true;
                        callback.onChunk(new ChatStreamChunk("", true));
                        return;
                    }

                    String token = extractText(payload);
                    boolean finished = isFinished(payload);
                    if (token != null && !token.isEmpty()) {
                        if (!callback.onChunk(new ChatStreamChunk(token, false))) {
                            return;
                        }
                    }
                    if (finished) {
                        doneSent = true;
                        callback.onChunk(new ChatStreamChunk("", true));
                        return;
                    }
                }
            }
        } catch (IOException ex) {
            callback.onChunk(new ChatStreamChunk("", true, buildConnectionErrorMessage(ex)));
            doneSent = true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            callback.onChunk(new ChatStreamChunk("", true, "Ket noi AI bi gian doan."));
            doneSent = true;
        } finally {
            if (!doneSent) {
                try {
                    callback.onChunk(new ChatStreamChunk("", true));
                } catch (IOException ignore) {
                    // Ignore client disconnect while closing the stream.
                }
            }
        }
    }

    private String validateRequest(ChatRequestDto dto) {
        if (dto == null || isBlank(dto.getPrompt())) {
            return "Vui long nhap noi dung tin nhan.";
        }
        if (isBlank(AiChatConfig.getApiKey())) {
            return "API key chua duoc cau hinh.";
        }
        return null;
    }

    private HttpRequest.Builder buildRequest(String endpoint, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofMillis(AiChatConfig.READ_TIMEOUT_MS))
                .header("Authorization", "Bearer " + AiChatConfig.getApiKey())
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
    }

    private JsonObject buildRequestBody(ChatRequestDto dto, String userId, boolean stream) {
        JsonObjectBuilder root = Json.createObjectBuilder();
        root.add("model", AiChatConfig.MODEL_NAME);
        root.add("messages", buildMessages(dto, userId));
        root.add("stream", stream);
        root.add("max_tokens", AiChatConfig.MAX_TOKENS);
        root.add("temperature", AiChatConfig.TEMPERATURE);
        return root.build();
    }

    private JsonArray buildMessages(ChatRequestDto dto, String userId) {
        JsonArrayBuilder messages = Json.createArrayBuilder();
        messages.add(toMessageObject("system", AiChatConfig.SYSTEM_PROMPT));
        String shopContext = shopContextService.buildContext(dto.getPrompt(), userId);
        if (!isBlank(shopContext)) {
            messages.add(toMessageObject("system", shopContext));
        }
        List<MessageDto> history = dto.getChatHistory();
        if (history != null && !history.isEmpty()) {
            int start = Math.max(0, history.size() - 20);
            for (int i = start; i < history.size(); i++) {
                MessageDto message = history.get(i);
                if (message == null || isBlank(message.getContent())) {
                    continue;
                }
                messages.add(toMessageObject(message.getRole(), message.getContent()));
            }
        }
        messages.add(toMessageObject("user", dto.getPrompt().trim()));
        return messages.build();
    }

    private JsonObject toMessageObject(String role, String content) {
        String normalizedRole = normalizeRole(role);
        return Json.createObjectBuilder()
                .add("role", normalizedRole)
                .add("content", content)
                .build();
    }

    private String normalizeRole(String role) {
        if ("assistant".equalsIgnoreCase(role) || "model".equalsIgnoreCase(role)) {
            return "assistant";
        }
        if ("system".equalsIgnoreCase(role)) {
            return "system";
        }
        return "user";
    }

    private String readFully(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private String extractText(String jsonText) {
        try (JsonReader reader = Json.createReader(new StringReader(jsonText))) {
            return extractTextFromObject(reader.readObject());
        } catch (RuntimeException ex) {
            return "";
        }
    }

    private String extractTextFromObject(JsonObject root) {
        JsonArray choices = root.getJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            return "";
        }
        JsonObject choice = choices.getJsonObject(0);

        JsonObject message = choice.getJsonObject("message");
        if (message != null) {
            return message.getString("content", "");
        }

        JsonObject delta = choice.getJsonObject("delta");
        if (delta != null) {
            return delta.getString("content", "");
        }

        return "";
    }

    private boolean isFinished(String jsonText) {
        try (JsonReader reader = Json.createReader(new StringReader(jsonText))) {
            JsonObject object = reader.readObject();
            JsonArray choices = object.getJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                return false;
            }
            JsonObject choice = choices.getJsonObject(0);
            String finishReason = choice.getString("finish_reason", "");
            return !finishReason.isEmpty() && !"null".equalsIgnoreCase(finishReason);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private String resolveErrorMessage(int status, String responseBody) {
        String apiMessage = extractApiError(responseBody);
        if (status == 401 || status == 403) {
            return "API key khong hop le.";
        }
        if (status == 429) {
            if (containsQuotaError(apiMessage)) {
                return "API AI hien khong con quota. Kiem tra billing, rate limit hoac doi API key khac.";
            }
            return "Dich vu AI dang qua tai, vui long thu lai sau.";
        }
        if (!isBlank(apiMessage)) {
            return apiMessage;
        }
        if (status >= 500) {
            return "Dich vu AI tam thoi khong san sang.";
        }
        return "Khong the xu ly yeu cau AI.";
    }

    private String extractApiError(String responseBody) {
        if (isBlank(responseBody)) {
            return "";
        }
        try (JsonReader reader = Json.createReader(new StringReader(responseBody))) {
            JsonObject object = reader.readObject();
            JsonObject error = object.getJsonObject("error");
            if (error == null) {
                return "";
            }
            return error.getString("message", "");
        } catch (RuntimeException ex) {
            return "";
        }
    }

    private boolean containsQuotaError(String message) {
        if (isBlank(message)) {
            return false;
        }
        String normalized = message.toLowerCase();
        return normalized.contains("quota")
                || normalized.contains("billing")
                || normalized.contains("resource_exhausted")
                || normalized.contains("rate limit")
                || normalized.contains("free_tier");
    }

    private String buildConnectionErrorMessage(IOException ex) {
        String detail = ex.getMessage();
        if (ex.getCause() != null && !isBlank(ex.getCause().getMessage())) {
            detail = ex.getCause().getMessage();
        }
        if (isBlank(detail)) {
            return "Khong the ket noi toi dich vu AI.";
        }
        return "Khong the ket noi toi dich vu AI: " + detail;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
