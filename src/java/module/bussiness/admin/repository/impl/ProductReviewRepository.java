package module.bussiness.admin.repository.impl;

import entity.ProductReviewEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import module.bussiness.admin.repository.interfaces.IProductReviewRepository;
import module.core.sql.ConnecDb;

public class ProductReviewRepository implements IProductReviewRepository {
    private static final Logger LOGGER = Logger.getLogger(ProductReviewRepository.class.getName());

    @Override
    public boolean isAvailable() {
        String sql = "SELECT 1 FROM ProductReview LIMIT 1";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return true;
        } catch (SQLException e) {
            logSchemaWarning(e);
            return false;
        }
    }

    @Override
    public List<ProductReviewEntity> findAll(String search, int page, int pageSize) {
        List<ProductReviewEntity> reviews = new ArrayList<>();
        String where = reviewWhere(search);
        String sql = "SELECT id, \"productId\", \"reviewerName\", rating, comment, \"reviewedAt\" "
                + "FROM ProductReview " + where + " ORDER BY \"reviewedAt\" DESC LIMIT ? OFFSET ?";
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = bindSearch(ps, search, 1);
            ps.setInt(index++, safePageSize);
            ps.setInt(index, (safePage - 1) * safePageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logSchemaWarning(e);
        }
        return reviews;
    }

    @Override
    public int count(String search) {
        String where = reviewWhere(search);
        String sql = "SELECT COUNT(*) FROM ProductReview " + where;
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindSearch(ps, search, 1);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logSchemaWarning(e);
        }
        return 0;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM ProductReview WHERE id = ?";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logSchemaWarning(e);
            return false;
        }
    }

    @Override
    public boolean update(String id, int rating, String comment) {
        String sql = "UPDATE ProductReview SET rating = ?, comment = ? WHERE id = ?";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, Math.min(5, rating)));
            ps.setString(2, comment == null ? "" : comment.trim());
            ps.setString(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logSchemaWarning(e);
            return false;
        }
    }

    private String reviewWhere(String search) {
        if (search == null || search.trim().isEmpty()) {
            return "";
        }
        return "WHERE LOWER(reviewerName) LIKE ? OR LOWER(productId) LIKE ?";
    }

    private int bindSearch(PreparedStatement ps, String search, int start) throws SQLException {
        if (search == null || search.trim().isEmpty()) {
            return start;
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        ps.setString(start++, pattern);
        ps.setString(start++, pattern);
        return start;
    }

    private ProductReviewEntity mapRow(ResultSet rs) throws SQLException {
        ProductReviewEntity entity = new ProductReviewEntity();
        entity.setId(rs.getString("id"));
        entity.setProductId(rs.getString("productId"));
        entity.setReviewerName(rs.getString("reviewerName"));
        entity.setRating(rs.getInt("rating"));
        entity.setComment(rs.getString("comment"));
        Timestamp reviewedAt = rs.getTimestamp("reviewedAt");
        entity.setCreatedAt(reviewedAt == null ? null : reviewedAt.toLocalDateTime());
        return entity;
    }

    private void logSchemaWarning(SQLException e) {
        LOGGER.log(Level.WARNING, "ProductReview table unavailable for admin review management.", e);
    }
}
