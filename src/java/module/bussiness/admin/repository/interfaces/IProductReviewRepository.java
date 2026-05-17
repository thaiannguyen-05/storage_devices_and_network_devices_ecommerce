package module.bussiness.admin.repository.interfaces;

import entity.ProductReviewEntity;
import java.util.List;

public interface IProductReviewRepository {
    boolean isAvailable();
    List<ProductReviewEntity> findAll(String search, int page, int pageSize);
    int count(String search);
    boolean delete(String id);
    boolean update(String id, int rating, String comment);
}
