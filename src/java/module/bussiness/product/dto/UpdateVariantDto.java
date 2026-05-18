package module.bussiness.product.dto;

public class UpdateVariantDto extends CreateVariantDto {
    private String id;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
