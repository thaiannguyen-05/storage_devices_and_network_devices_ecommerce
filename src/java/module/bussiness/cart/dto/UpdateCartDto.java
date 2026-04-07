package module.bussiness.cart.dto;

import java.time.LocalDateTime;

public class UpdateCartDto {
    private String id;
    private String userId;
    private LocalDateTime createdAt;

    public UpdateCartDto() {
    }

    public UpdateCartDto(String id, String userId, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
