package module.bussiness.cart.dto;

public class DeleteCartDto {
    private String id;

    public DeleteCartDto() {
    }

    public DeleteCartDto(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
