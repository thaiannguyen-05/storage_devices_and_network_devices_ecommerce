package module.bussiness.product.repository.impl;

import entity.ProductVariantEntity;
import java.util.*;
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
    public Map<String, List<ProductVariantEntity>> findByProductIds(List<String> productIds) {
        Map<String, List<ProductVariantEntity>> result = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) {
            return result;
        }
        StringBuilder sql = new StringBuilder("SELECT * FROM ProductVariant WHERE productId IN (");
        for (int i = 0; i < productIds.size(); i++) {
            sql.append("?");
            if (i < productIds.size() - 1) sql.append(",");
        }
        sql.append(") ORDER BY productId, price ASC");
        List<ProductVariantEntity> allVariants = JdbcHelper.executeQuery(sql.toString(), rs -> map(rs), productIds.toArray());
        for (ProductVariantEntity v : allVariants) {
            result.computeIfAbsent(v.getProductId(), k -> new ArrayList<>()).add(v);
        }
        return result;
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

    private java.time.LocalDateTime safeTimestamp(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        java.sql.Timestamp ts = rs.getTimestamp(column);
        return ts == null ? null : ts.toLocalDateTime();
    }

    private ProductVariantEntity map(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new ProductVariantEntity(rs.getString("id"), rs.getString("productId"), rs.getBigDecimal("price"),
                rs.getString("imageUrl"), rs.getString("status"), safeTimestamp(rs, "createdAt"),
                safeTimestamp(rs, "updatedAt"), rs.getString("sku"), rs.getInt("quantity"));
    }
}
