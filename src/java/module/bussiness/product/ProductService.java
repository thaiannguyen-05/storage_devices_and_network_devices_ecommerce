package module.bussiness.product;

import module.core.sql.repository.BrandRepository;
import module.core.sql.repository.ProductRepository;
import module.core.sql.repository.ProductVariantRepository;

public class ProductService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final BrandRepository brandRepository;

    public ProductService() {
        this.productRepository = new ProductRepository();
        this.productVariantRepository = new ProductVariantRepository();
        this.brandRepository = new BrandRepository();
    }

    public ProductRepository getProductRepository() {
        return productRepository;
    }

    public ProductVariantRepository getProductVariantRepository() {
        return productVariantRepository;
    }

    public BrandRepository getBrandRepository() {
        return brandRepository;
    }
}
