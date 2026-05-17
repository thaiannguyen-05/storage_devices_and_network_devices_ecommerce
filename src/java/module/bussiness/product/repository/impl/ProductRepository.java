package module.bussiness.product.repository.impl;
import entity.ProductEntity;
import module.bussiness.product.dto.CreateProduct;
import module.bussiness.product.dto.UpdateProduct;
import module.core.sql.ConnecDb;
import module.bussiness.product.repository.interfaces.IProductRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductRepository implements IProductRepository {
    @Override
    public List<ProductEntity> findAll() {
        String sql = "SELECT id , name , description, \"brandId\", status, \"userId\", category FROM \"Product\" ORDER BY \"createdAt\" DESC";
        List<ProductEntity> products = new ArrayList<>();

        try (Connection con = ConnecDb.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
            return products;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get products", e);
        }
    }

    @Override
    public ProductEntity findById(String id) {
        String sql = "SELECT id, name, description, \"brandId\", status, \"userId\", category FROM \"Product\" WHERE id = ?";
        try (Connection con = ConnecDb.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get product by id", e);
        }
    }

    @Override
    public boolean create(CreateProduct dto) {
        String sql = "INSERT INTO \"Product\" (id, name, description, \"brandId\", status, \"userId\", \"createdAt\", \"updatedAt\", category) "
                + "VALUES(?,?,?,?,?,?,NOW(),NOW(),?)";

        try (Connection con = ConnecDb.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String id = UUID.randomUUID().toString();
            ps.setString(1, id);
            ps.setString(2, dto.getName());
            ps.setString(3, dto.getDescription());
            ps.setString(4, dto.getBrandId());
            ps.setString(5, dto.getStatus());
            ps.setString(6, dto.getUserId());
            ps.setString(7, dto.getCategory());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create product", e);
        }
    }

    @Override
    public boolean update(String id, UpdateProduct dto) {
        String sql = "UPDATE \"Product\" SET name = ? , description = ? , \"brandId\" = ? , status = ?, category = ?, \"updatedAt\" = NOW() WHERE id = ?";

        try (Connection con = ConnecDb.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dto.getName());
            ps.setString(2, dto.getDescription());
            ps.setString(3, dto.getBrandId());
            ps.setString(4, dto.getStatus());
            ps.setString(5, dto.getCategory());
            ps.setString(6, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update product", e);
        }
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM \"Product\" WHERE id = ? ";
        try (Connection con = ConnecDb.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete product", e);
        }
    }

    private ProductEntity mapResultSetToProduct(ResultSet rs) throws SQLException {
        ProductEntity item = new ProductEntity();
        item.setId(rs.getString("id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setBrandId(rs.getString("brandId"));
        item.setStatus(rs.getString("status"));
        item.setUserId(rs.getString("userId"));
        item.setCategory(rs.getString("category"));
        return item;
    }
}
