package module.bussiness.product.repository.impl;

import entity.ProductVariantEntity;
import module.bussiness.product.repository.interfaces.IProductVariantRepository;
import module.core.sql.ConnecDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductVariantRepository implements IProductVariantRepository {
    @Override
    public List<ProductVariantEntity> findAll() {
        String sql = "SELECT id, productId, price, imageUrl, status, sku, quantity FROM ProductVariant ORDER BY createdAt DESC";
        List<ProductVariantEntity> variants = new ArrayList<>();

        try (Connection con = ConnecDb.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                variants.add(mapResultSetToVariant(rs));
            }
            return variants;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get product variants", e);
        }
    }

    @Override
    public ProductVariantEntity findById(String id) {
        String sql = "SELECT id, productId, price, imageUrl, status, sku, quantity FROM ProductVariant WHERE id = ?";
        try (Connection con = ConnecDb.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapResultSetToVariant(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get product variant by id", e);
        }
    }

    @Override
    public ProductVariantEntity findFirstByProductId(String productId) {
        String sql = "SELECT id, productId, price, imageUrl, status, sku, quantity FROM ProductVariant WHERE productId = ? ORDER BY createdAt DESC LIMIT 1";
        try (Connection con = ConnecDb.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapResultSetToVariant(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get first product variant by product id", e);
        }
    }

    @Override
    public List<ProductVariantEntity> findByProductId(String productId) {
        String sql = "SELECT id, productId, price, imageUrl, status, sku, quantity FROM ProductVariant WHERE productId = ? ORDER BY createdAt DESC";
        List<ProductVariantEntity> variants = new ArrayList<>();
        try (Connection con = ConnecDb.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    variants.add(mapResultSetToVariant(rs));
                }
            }
            return variants;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get product variants by product id", e);
        }
    }

    private ProductVariantEntity mapResultSetToVariant(ResultSet rs) throws SQLException {
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
