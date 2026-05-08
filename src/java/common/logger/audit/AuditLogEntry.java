package common.logger.audit;

import java.util.LinkedHashMap;
import java.util.Map;

public class AuditLogEntry {
    private String requestId;
    private String timestamp;
    private String method;
    private String uri;
    private String queryString;
    private String contentType;
    private Map<String, String[]> parameters = new LinkedHashMap<>();
    private String requestBody;
    private int responseStatus;
    private String responseBody;
    private long durationMs;
    private String clientIp;
    private String userAgent;
    private String authUserEmail;
    private String authUserRole;
    private boolean error;
    private String errorMessage;

    public Map<String, Object> toMap() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", timestamp);
        payload.put("type", "AUDIT");
        payload.put("requestId", requestId);
        payload.put("method", method);
        payload.put("uri", uri);
        payload.put("queryString", queryString);
        payload.put("contentType", contentType);
        payload.put("parameters", parameters);
        payload.put("requestBody", requestBody);
        payload.put("responseStatus", responseStatus);
        payload.put("responseBody", responseBody);
        payload.put("durationMs", durationMs);
        payload.put("clientIp", clientIp);
        payload.put("userAgent", userAgent);
        payload.put("authUserEmail", authUserEmail);
        payload.put("authUserRole", authUserRole);
        payload.put("isError", error);
        payload.put("errorMessage", errorMessage);
        return payload;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Map<String, String[]> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String[]> parameters) {
        this.parameters = parameters == null ? new LinkedHashMap<>() : parameters;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(int responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getAuthUserEmail() {
        return authUserEmail;
    }

    public void setAuthUserEmail(String authUserEmail) {
        this.authUserEmail = authUserEmail;
    }

    public String getAuthUserRole() {
        return authUserRole;
    }

    public void setAuthUserRole(String authUserRole) {
        this.authUserRole = authUserRole;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
