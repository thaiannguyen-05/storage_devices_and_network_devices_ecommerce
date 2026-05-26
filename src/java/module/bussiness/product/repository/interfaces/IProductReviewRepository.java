package module.bussiness.product.repository.interfaces;

import entity.ProductReviewEntity;
import entity.ReviewView;
import java.util.List;

public interface IProductReviewRepository {
    void insert(ProductReviewEntity review);
    List<ReviewView> findByProductIdApproved(String productId);
    ProductReviewEntity findByUserIdAndProductId(String userId, String productId);
    int countByProductId(String productId);
    double calculateAverageRating(String productId);
    boolean existsByUserIdAndProductId(String userId, String productId);
}
