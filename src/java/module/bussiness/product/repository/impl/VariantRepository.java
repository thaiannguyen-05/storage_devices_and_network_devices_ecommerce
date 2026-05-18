package module.bussiness.product.repository.impl;

import entity.ProductVariantEntity;
import java.util.List;
import module.bussiness.product.repository.interfaces.IVariantRepository;
import module.core.sql.JdbcHelper;

public class VariantRepository implements IVariantRepository {
    @Override
    public void insert(ProductVariantEntity v) {
        JdbcHelper.executeUpdate("INSERT INTO ProductVariant (id, productId, price, imageUrl, status, sku, quantity) VALUES (?, ?, ?, ?, ?, ?, ?)",
                v.getId(), v.getProductId(), v.getPrice(), v.getImageUrl(), v.getStatus(), v.getSku(), v.getQuantity());
    }

    @Override
    public ProductVariantEntity findById(String id) {
        List<ProductVariantEntity> rows = JdbcHelper.executeQuery("SELECT * FROM ProductVariant WHERE id = ?", rs -> map(rs), id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public ProductVariantEntity findBySku(String sku) {
        List<ProductVariantEntity> rows = JdbcHelper.executeQuery("SELECT * FROM ProductVariant WHERE sku = ?", rs -> map(rs), sku);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public List<ProductVariantEntity> findByProductId(String productId) {
        return JdbcHelper.executeQuery("SELECT * FROM ProductVariant WHERE productId = ? ORDER BY price ASC", rs -> map(rs), productId);
    }

    @Override
    public void update(ProductVariantEntity v) {
        JdbcHelper.executeUpdate("UPDATE ProductVariant SET price = ?, imageUrl = ?, status = ?, sku = ?, quantity = ? WHERE id = ?",
                v.getPrice(), v.getImageUrl(), v.getStatus(), v.getSku(), v.getQuantity(), v.getId());
    }

    @Override
    public void delete(String id) {
        JdbcHelper.executeUpdate("DELETE FROM ProductVariant WHERE id = ?", id);
    }

    private ProductVariantEntity map(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new ProductVariantEntity(rs.getString("id"), rs.getString("productId"), rs.getBigDecimal("price"),
                rs.getString("imageUrl"), rs.getString("status"), rs.getTimestamp("createdAt").toLocalDateTime(),
                rs.getTimestamp("updatedAt").toLocalDateTime(), rs.getString("sku"), rs.getInt("quantity"));
    }
}
