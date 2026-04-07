package module.bussiness.cart.dto;

public class CreateCartDto {
    private String userId;

    public CreateCartDto() {
    }

    public CreateCartDto(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
