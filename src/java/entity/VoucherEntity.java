package entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Voucher")
public class VoucherEntity {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "percent")
    private Double percent;

    @Column(name = "userId")
    private String userId;

    @Column(name = "expTime")
    private LocalDate expTime;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "quantity")
    private Integer quantity;

    public VoucherEntity() {
    }

    public VoucherEntity(String id, Double percent, String userId, LocalDate expTime, LocalDateTime createdAt,
            Integer quantity) {
        this.id = id;
        this.percent = percent;
        this.userId = userId;
        this.expTime = expTime;
        this.createdAt = createdAt;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getExpTime() {
        return expTime;
    }

    public void setExpTime(LocalDate expTime) {
        this.expTime = expTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
