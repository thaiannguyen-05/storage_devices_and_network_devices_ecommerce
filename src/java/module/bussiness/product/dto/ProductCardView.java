package module.bussiness.product.dto;

public class ProductCardView {
    private String id;
    private String name;
    private String category;
    private String status;
    private String brandId;
    private String imageUrl;
    private String priceText;
    private long priceValue;
    private int quantity;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getPriceText() { return priceText; }
    public void setPriceText(String priceText) { this.priceText = priceText; }
    public long getPriceValue() { return priceValue; }
    public void setPriceValue(long priceValue) { this.priceValue = priceValue; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
