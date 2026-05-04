package module.core.auth;

public class AuthPayload {
    public static final String REQUEST_ATTRIBUTE = "authPayload";

    private final String userId;
    private final String email;
    private final String role;
    private final String sessionId;

    public AuthPayload(String userId, String email, String role, String sessionId) {
        this.userId = userId == null ? "" : userId.trim();
        this.email = email == null ? "" : email.trim();
        this.role = role == null ? "" : role.trim();
        this.sessionId = sessionId == null ? "" : sessionId.trim();
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getSessionId() {
        return sessionId;
    }
}
