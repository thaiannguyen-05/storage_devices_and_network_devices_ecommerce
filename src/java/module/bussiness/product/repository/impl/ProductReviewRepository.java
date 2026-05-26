package module.bussiness.product.repository.impl;

import entity.ProductReviewEntity;
import entity.ReviewView;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import module.bussiness.product.repository.interfaces.IProductReviewRepository;
import module.core.sql.JdbcHelper;

public class ProductReviewRepository implements IProductReviewRepository {
    @Override
    public void insert(ProductReviewEntity review) {
        JdbcHelper.executeUpdate(
                "INSERT INTO `ProductReview` (id, productId, userId, rating, comment, status, createdAt, updatedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                review.getId(), review.getProductId(), review.getUserId(), review.getRating(), review.getComment(),
                review.getStatus(), review.getCreatedAt(), review.getUpdatedAt());
    }

    @Override
    public List<ReviewView> findByProductIdApproved(String productId) {
        return JdbcHelper.executeQuery(
                "SELECT r.id, r.rating, r.comment, r.createdAt, COALESCE(u.name, u.email, 'Customer') AS reviewerName "
                + "FROM `ProductReview` r "
                + "JOIN `User` u ON u.id = r.userId "
                + "WHERE r.productId = ? AND r.status = 'APPROVED' "
                + "ORDER BY r.createdAt DESC",
                rs -> mapView(rs), productId);
    }

    @Override
    public ProductReviewEntity findByUserIdAndProductId(String userId, String productId) {
        List<ProductReviewEntity> rows = JdbcHelper.executeQuery(
                "SELECT * FROM `ProductReview` WHERE userId = ? AND productId = ? LIMIT 1",
                rs -> mapEntity(rs), userId, productId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public int countByProductId(String productId) {
        return JdbcHelper.count("SELECT COUNT(*) FROM `ProductReview` WHERE productId = ? AND status = 'APPROVED'", productId);
    }

    @Override
    public double calculateAverageRating(String productId) {
        List<Double> rows = JdbcHelper.executeQuery(
                "SELECT COALESCE(AVG(rating), 0) AS averageRating FROM `ProductReview` WHERE productId = ? AND status = 'APPROVED'",
                rs -> rs.getDouble("averageRating"), productId);
        return rows.isEmpty() ? 0 : rows.get(0);
    }

    @Override
    public boolean existsByUserIdAndProductId(String userId, String productId) {
        return JdbcHelper.count("SELECT COUNT(*) FROM `ProductReview` WHERE userId = ? AND productId = ?", userId, productId) > 0;
    }

    private ReviewView mapView(ResultSet rs) throws SQLException {
        return new ReviewView(
                rs.getString("id"),
                rs.getInt("rating"),
                rs.getString("comment"),
                toLocalDateTime(rs.getTimestamp("createdAt")),
                rs.getString("reviewerName"));
    }

    private ProductReviewEntity mapEntity(ResultSet rs) throws SQLException {
        return new ProductReviewEntity(
                rs.getString("id"),
                rs.getString("productId"),
                rs.getString("userId"),
                rs.getInt("rating"),
                rs.getString("comment"),
                rs.getString("status"),
                toLocalDateTime(rs.getTimestamp("createdAt")),
                toLocalDateTime(rs.getTimestamp("updatedAt")));
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
