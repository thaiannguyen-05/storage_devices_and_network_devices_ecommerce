package module.bussiness.product;

import java.math.BigDecimal;

public class ProductCardView {
    private String id;
    private String name;
    private String imageUrl;
    private BigDecimal price;

    public ProductCardView(String id, String name, String imageUrl, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public BigDecimal getPrice() { return price; }
}
