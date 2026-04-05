package entity;

import java.time.LocalDateTime;

public class OutBoxEntity {
    private String id;
    private String payload;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OutBoxEntity() {
    }

    public OutBoxEntity(String id, String payload, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.payload = payload;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
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
}
