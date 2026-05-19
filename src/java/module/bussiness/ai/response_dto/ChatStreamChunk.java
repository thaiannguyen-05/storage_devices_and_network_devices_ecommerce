package module.bussiness.ai.response_dto;

public class ChatStreamChunk {
    private String token;
    private boolean done;
    private String error;

    public ChatStreamChunk() {
    }

    public ChatStreamChunk(String token, boolean done) {
        this.token = token;
        this.done = done;
    }

    public ChatStreamChunk(String token, boolean done, String error) {
        this.token = token;
        this.done = done;
        this.error = error;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
