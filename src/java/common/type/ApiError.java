package common.type;

public class ApiError {

    private boolean success;
    private int code;
    private String message;
    private String timestamp;
    private String path;
    private String method;
    private String requestId;

    public ApiError() {
    }

    public ApiError(boolean success, int code, String message,
            String timestamp, String path, String method, String requestId) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
        this.method = method;
        this.requestId = requestId;
    }

    // --- Getters & Setters ---
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
