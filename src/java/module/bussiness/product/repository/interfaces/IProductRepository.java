package module.bussiness.product.repository.interfaces;

import entity.ProductEntity;
import java.util.List;

public interface IProductRepository {
    void insert(ProductEntity product);
    ProductEntity findById(String id);
    List<ProductEntity> findActive(int offset, int limit);
    List<ProductEntity> search(String keyword, int offset, int limit);
    List<ProductEntity> findByCategory(String category, int offset, int limit);
    List<ProductEntity> findBestSelling(int limit);
    int countActive();
    void update(ProductEntity product);
    void delete(String id);
}
