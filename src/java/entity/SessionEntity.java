package entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Session")
public class SessionEntity {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "hashRefreshToken")
    private String hashRefreshToken;

    @Column(name = "userId")
    private String userId;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
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
