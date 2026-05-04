package entity;

import java.time.LocalDateTime;

public class SessionEntity {
    private String id;
    private String hashRefreshToken;
    private String userId;
    private String ip;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SessionEntity() {
    }

    public SessionEntity(String id, String hashRefreshToken, String userId, String ip, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.hashRefreshToken = hashRefreshToken;
        this.userId = userId;
        this.ip = ip;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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
