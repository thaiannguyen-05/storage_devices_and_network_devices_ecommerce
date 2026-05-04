package entity;

import java.time.LocalDateTime;

public class OutBoxEntity {
    private String id;
    private String code;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String type;
    private String userId;

    public OutBoxEntity() {
    }

    public OutBoxEntity(String id, String code, String status, LocalDateTime createdAt, LocalDateTime updatedAt, String type, String userId) {
        this.id = id;
        this.code = code;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.type = type;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
