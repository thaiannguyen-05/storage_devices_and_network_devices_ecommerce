package module.bussiness.admin.dto;

import java.time.LocalDate;

public class AdminVoucherRequestDto {
    private String id;
    private Double percent;
    private LocalDate expTime;
    private Integer quantity;
    private String userId;

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

    public LocalDate getExpTime() {
        return expTime;
    }

    public void setExpTime(LocalDate expTime) {
        this.expTime = expTime;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
