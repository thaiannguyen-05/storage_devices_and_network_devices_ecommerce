package entity;

import java.time.LocalDateTime;

public class SessionEntity {
    private String id;
    private String hashRefreshToken;
    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SessionEntity() {
    }

    public SessionEntity(String id, String hashRefreshToken, String userId, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.hashRefreshToken = hashRefreshToken;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHashRefreshToken() {
        return hashRefreshToken;
    }

    public void setHashRefreshToken(String hashRefreshToken) {
        this.hashRefreshToken = hashRefreshToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
