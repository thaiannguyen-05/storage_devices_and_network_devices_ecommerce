package module.bussiness.product.repository.impl;

import entity.ProductEntity;
import java.util.List;
import module.bussiness.product.repository.interfaces.IProductRepository;
import module.core.sql.JdbcHelper;

public class ProductRepository implements IProductRepository {
    @Override
    public void insert(ProductEntity p) {
        JdbcHelper.executeUpdate("INSERT INTO Product (id, name, description, brandId, status, userId, category) VALUES (?, ?, ?, ?, ?, ?, ?)",
                p.getId(), p.getName(), p.getDescription(), p.getBrandId(), p.getStatus(), p.getUserId(), p.getCategory());
    }

    @Override
    public ProductEntity findById(String id) {
        List<ProductEntity> rows = JdbcHelper.executeQuery("SELECT * FROM Product WHERE id = ?", rs -> map(rs), id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public List<ProductEntity> findActive(int offset, int limit) {
        return JdbcHelper.executeQuery("SELECT * FROM Product WHERE status = 'ACTIVE' ORDER BY createdAt DESC LIMIT ? OFFSET ?",
                rs -> map(rs), limit, offset);
    }

    @Override
    public List<ProductEntity> search(String keyword, int offset, int limit) {
        String like = "%" + (keyword == null ? "" : keyword) + "%";
        return JdbcHelper.executeQuery("SELECT * FROM Product WHERE status = 'ACTIVE' AND (name LIKE ? OR description LIKE ?) ORDER BY createdAt DESC LIMIT ? OFFSET ?",
                rs -> map(rs), like, like, limit, offset);
    }

    @Override
    public List<ProductEntity> findByCategory(String category, int offset, int limit) {
        return JdbcHelper.executeQuery("SELECT * FROM Product WHERE status = 'ACTIVE' AND category = ? ORDER BY createdAt DESC LIMIT ? OFFSET ?",
                rs -> map(rs), category, limit, offset);
    }

    @Override
    public List<ProductEntity> findBestSelling(int limit) {
        return JdbcHelper.executeQuery("SELECT p.* FROM Product p LEFT JOIN `Order` o ON p.id = o.productId WHERE p.status = 'ACTIVE' GROUP BY p.id ORDER BY COUNT(o.id) DESC, p.createdAt DESC LIMIT ?",
                rs -> map(rs), limit);
    }

    @Override
    public int countActive() {
        return JdbcHelper.count("SELECT COUNT(*) FROM Product WHERE status = 'ACTIVE'");
    }

    @Override
    public void update(ProductEntity p) {
        JdbcHelper.executeUpdate("UPDATE Product SET name = ?, description = ?, brandId = ?, status = ?, category = ? WHERE id = ?",
                p.getName(), p.getDescription(), p.getBrandId(), p.getStatus(), p.getCategory(), p.getId());
    }

    @Override
    public void delete(String id) {
        JdbcHelper.executeUpdate("DELETE FROM Product WHERE id = ?", id);
    }

    private ProductEntity map(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new ProductEntity(rs.getString("id"), rs.getString("name"), rs.getString("description"),
                rs.getString("brandId"), rs.getString("status"), rs.getString("userId"),
                rs.getTimestamp("createdAt").toLocalDateTime(), rs.getTimestamp("updatedAt").toLocalDateTime(),
                rs.getString("category"));
    }
}
