package module.core.sql.repository;

import entity.ProductReviewEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import module.core.sql.ConnecDb;

public class ProductReviewRepository {

    private void ensureTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS productreview ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "productId VARCHAR(64) NOT NULL,"
                + "reviewerName VARCHAR(255) NOT NULL,"
                + "rating INT NOT NULL,"
                + "comment TEXT NULL,"
                + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "INDEX idx_productreview_productid(productId)"
                + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        }
    }

    public List<ProductReviewEntity> findByProductId(String productId) throws SQLException {
        String sql = "SELECT id, productId, reviewerName, rating, comment, createdAt "
                + "FROM productreview WHERE productId = ? ORDER BY createdAt DESC";
        List<ProductReviewEntity> reviews = new ArrayList<>();
        try (Connection conn = ConnecDb.getConnection()) {
            ensureTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ProductReviewEntity item = new ProductReviewEntity();
                        item.setId(rs.getLong("id"));
                        item.setProductId(rs.getString("productId"));
                        item.setReviewerName(rs.getString("reviewerName"));
                        item.setRating(rs.getInt("rating"));
                        item.setComment(rs.getString("comment"));
                        item.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
                        reviews.add(item);
                    }
                }
            }
        }
        return reviews;
    }

    public boolean create(String productId, String reviewerName, int rating, String comment) throws SQLException {
        String sql = "INSERT INTO productreview(productId, reviewerName, rating, comment) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnecDb.getConnection()) {
            ensureTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, productId);
                ps.setString(2, reviewerName);
                ps.setInt(3, rating);
                ps.setString(4, comment);
                return ps.executeUpdate() > 0;
            }
        }
    }
}
