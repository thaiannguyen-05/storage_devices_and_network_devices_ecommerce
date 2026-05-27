package module.bussiness.product.repository.interfaces;

import entity.ProductVariantEntity;
import java.util.List;

public interface IVariantRepository {
    void insert(ProductVariantEntity variant);
    ProductVariantEntity findById(String id);
    ProductVariantEntity findBySku(String sku);
    List<ProductVariantEntity> findByProductId(String productId);
    java.util.Map<String, List<ProductVariantEntity>> findByProductIds(List<String> productIds);
    void update(ProductVariantEntity variant);
    void delete(String id);
}
