package entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SavedProduct")
public class SavedProductEntity {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "productId")
    private String productId;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    public SavedProductEntity() {
    }

    public SavedProductEntity(String id, String productId, Integer quantity, LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
