package module.bussiness.order.repository.impl;

import entity.OrderCartEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import module.bussiness.cart.dto.CreateCartDto;
import module.bussiness.cart.dto.UpdateCartDto;
import module.core.sql.ConnecDb;
import module.bussiness.order.repository.interfaces.IOrderCartRepository;

public class OrderCartRepository implements IOrderCartRepository {

    @Override
    public OrderCartEntity registerCart(CreateCartDto dto) {
        String sql = "INSERT INTO OrderCart (id, \"userId\", \"createdAt\", \"updatedAt\") VALUES (?, ?, NOW(), NOW())";

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            String id = UUID.randomUUID().toString();
            LocalDateTime now = LocalDateTime.now();
            ps.setString(1, id);
            ps.setString(2, dto.getUserId());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Failed to register cart: no rows inserted");
            }

            return new OrderCartEntity(id, dto.getUserId(), now, now);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to register cart", e);
        }
    }

    @Override
    public List<OrderCartEntity> findAll() {
        String sql = "SELECT id, \"userId\", \"createdAt\", \"updatedAt\" FROM OrderCart ORDER BY \"createdAt\" DESC";
        List<OrderCartEntity> carts = new ArrayList<>();

        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                carts.add(mapResultSetToOrderCart(rs));
            }
            return carts;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find carts", e);
        }
    }

    @Override
    public OrderCartEntity findById(String id) {
        String sql = "SELECT id, \"userId\", \"createdAt\", \"updatedAt\" FROM OrderCart WHERE id = ?";

        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrderCart(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find cart by id", e);
        }
    }

    @Override
    public OrderCartEntity findByUserId(String userId) {
        String sql = "SELECT id, \"userId\", \"createdAt\", \"updatedAt\" FROM OrderCart WHERE \"userId\" = ? LIMIT 1";

        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrderCart(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find cart by user id", e);
        }
    }

    @Override
    public boolean update(String id, UpdateCartDto dto) {
        String sql = "UPDATE OrderCart SET \"userId\" = ?, \"createdAt\" = ?, \"updatedAt\" = NOW() WHERE id = ?";

        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getUserId());
            ps.setTimestamp(2, dto.getCreatedAt() == null ? null : Timestamp.valueOf(dto.getCreatedAt()));
            ps.setString(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update cart", e);
        }
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM OrderCart WHERE id = ?";

        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete cart", e);
        }
    }

    private OrderCartEntity mapResultSetToOrderCart(ResultSet rs) throws SQLException {
        Timestamp createdAtTs = rs.getTimestamp("createdAt");
        LocalDateTime createdAt = createdAtTs == null ? null : createdAtTs.toLocalDateTime();
        Timestamp updatedAtTs = rs.getTimestamp("updatedAt");
        LocalDateTime updatedAt = updatedAtTs == null ? null : updatedAtTs.toLocalDateTime();

        return new OrderCartEntity(
                rs.getString("id"),
                rs.getString("userId"),
                createdAt,
                updatedAt
        );
    }
}
