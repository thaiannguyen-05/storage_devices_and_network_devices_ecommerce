package module.bussiness.cart.dto;

public class GetCartDto {
    private String id;

    public GetCartDto() {
    }

    public GetCartDto(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
