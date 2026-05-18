package common.type;

import java.io.Serializable;

public class UserPayload implements Serializable {
    private String userId;
    private String email;
    private String role;
    private String name;

    public UserPayload() {
    }

    public UserPayload(String userId, String email, String role, String name) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
