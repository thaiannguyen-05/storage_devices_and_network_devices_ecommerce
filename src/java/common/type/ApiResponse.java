package common.type;

public class ApiResponse<T> {
    private final boolean success;
    private final int code;
    private final T data;
    private final String message;
    private final String timestamp;

    public ApiResponse(boolean success, int code, T data, String message, String timestamp) {
        this.success = success;
        this.code = code;
        this.data = data;
        this.message = message;
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}