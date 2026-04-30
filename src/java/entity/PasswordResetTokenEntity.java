package entity;

import java.time.LocalDateTime;

public class PasswordResetTokenEntity {
    private String id;
    private String userId;
    private String tokenHash;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;

    public PasswordResetTokenEntity() {
    }

    public PasswordResetTokenEntity(String id, String userId, String tokenHash, LocalDateTime expiresAt, LocalDateTime usedAt, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
