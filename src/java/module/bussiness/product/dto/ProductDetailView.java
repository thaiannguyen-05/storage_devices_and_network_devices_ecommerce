package module.bussiness.product.dto;

public class ProductDetailView {
    private String id;
    private String name;
    private String description;
    private String category;
    private String status;
    private String brandId;
    private String imageUrl;
    private String priceText;
    private long priceValue;
    private int quantity;
    private double rating;
    private int reviewCount;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
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
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
}
