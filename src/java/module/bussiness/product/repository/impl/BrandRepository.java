package module.bussiness.product.repository.impl;

import entity.BrandEntity;
import module.bussiness.product.repository.interfaces.IBrandRepository;
import module.core.sql.ConnecDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BrandRepository implements IBrandRepository {
    @Override
    public List<BrandEntity> findAll() {
        String sql = "SELECT id, name, userId, description, status FROM Brand ORDER BY createdAt DESC";
        List<BrandEntity> brands = new ArrayList<>();

        try (Connection con = ConnecDb.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                brands.add(mapResultSetToBrand(rs));
            }
            return brands;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get brands", e);
        }
    }

    @Override
    public BrandEntity findById(String id) {
        String sql = "SELECT id, name, userId, description, status FROM Brand WHERE id = ?";
        try (Connection con = ConnecDb.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapResultSetToBrand(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get brand by id", e);
        }
    }

    private BrandEntity mapResultSetToBrand(ResultSet rs) throws SQLException {
        BrandEntity item = new BrandEntity();
        item.setId(rs.getString("id"));
        item.setName(rs.getString("name"));
        item.setUserId(rs.getString("userId"));
        item.setDescription(rs.getString("description"));
        item.setStatus(rs.getString("status"));
        return item;
    }
}
