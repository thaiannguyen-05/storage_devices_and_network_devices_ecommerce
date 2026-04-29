package module.core.sql.repository;

import entity.ProductVariantEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import module.core.sql.ConnecDb;

public class ProductVariantRepository {

    public ProductVariantEntity findFirstByProductId(String productId) throws SQLException {
        String sql = "SELECT id, productId, price, imageUrl, status, sku, quantity "
                + "FROM productvariant WHERE productId = ? ORDER BY createdAt DESC LIMIT 1";

        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                ProductVariantEntity item = new ProductVariantEntity();
                item.setId(rs.getString("id"));
                item.setProductId(rs.getString("productId"));
                item.setPrice(rs.getBigDecimal("price"));
                item.setImageUrl(rs.getString("imageUrl"));
                item.setStatus(rs.getString("status"));
                item.setSku(rs.getString("sku"));
                item.setQuantity(rs.getInt("quantity"));
                return item;
            }
        }
    }

    public List<ProductVariantEntity> findByProductId(String productId) throws SQLException {
        String sql = "SELECT id, productId, price, imageUrl, status, sku, quantity "
                + "FROM productvariant WHERE productId = ? ORDER BY createdAt DESC";
        List<ProductVariantEntity> variants = new ArrayList<>();

        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductVariantEntity item = new ProductVariantEntity();
                    item.setId(rs.getString("id"));
                    item.setProductId(rs.getString("productId"));
                    item.setPrice(rs.getBigDecimal("price"));
                    item.setImageUrl(rs.getString("imageUrl"));
                    item.setStatus(rs.getString("status"));
                    item.setSku(rs.getString("sku"));
                    item.setQuantity(rs.getInt("quantity"));
                    variants.add(item);
                }
            }
        }
        return variants;
    }
}
