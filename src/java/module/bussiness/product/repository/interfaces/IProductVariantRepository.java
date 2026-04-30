package module.bussiness.product.repository.interfaces;

import entity.ProductVariantEntity;

import java.util.List;

public interface IProductVariantRepository {
    List<ProductVariantEntity> findAll();
    ProductVariantEntity findById(String id);
    ProductVariantEntity findFirstByProductId(String productId);
    List<ProductVariantEntity> findByProductId(String productId);
}
