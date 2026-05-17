package module.bussiness.cart.repository.impl;

import entity.ItemCartEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import module.bussiness.cart.repository.interfaces.IItemCartRepository;
import module.core.sql.ConnecDb;

public class ItemCartRepository implements IItemCartRepository {

    @Override
    public List<ItemCartEntity> findByCartId(String cartId) {
        String sql = "SELECT id, \"cartId\", \"productId\", \"variantId\", quantity, \"createdAt\", \"updatedAt\" "
                + "FROM ItemCart WHERE \"cartId\" = ? ORDER BY \"createdAt\" ASC, \"updatedAt\" ASC";
        List<ItemCartEntity> items = new ArrayList<>();

        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cartId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToItemCart(rs));
                }
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find cart items", e);
        }
    }

    @Override
    public ItemCartEntity findByCartIdAndProductAndVariant(String cartId, String productId, String variantId) {
        String sql = "SELECT id, \"cartId\", \"productId\", \"variantId\", quantity, \"createdAt\", \"updatedAt\" "
                + "FROM ItemCart WHERE \"cartId\" = ? AND \"productId\" = ? AND COALESCE(\"variantId\", '') = COALESCE(?, '') LIMIT 1";

        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cartId);
            ps.setString(2, productId);
            ps.setString(3, emptyToNull(variantId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToItemCart(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find cart item", e);
        }
    }

    @Override
    public ItemCartEntity create(String cartId, String productId, String variantId, int quantity) {
        String sql = "INSERT INTO ItemCart (id, \"cartId\", \"productId\", \"variantId\", quantity, \"createdAt\", \"updatedAt\") "
                + "VALUES (?, ?, ?, ?, ?, NOW(), NOW())";

        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String id = UUID.randomUUID().toString();
            LocalDateTime now = LocalDateTime.now();
            ps.setString(1, id);
            ps.setString(2, cartId);
            ps.setString(3, productId);
            ps.setString(4, emptyToNull(variantId));
            ps.setInt(5, Math.max(1, quantity));
            ps.executeUpdate();
            return new ItemCartEntity(id, cartId, productId, emptyToNull(variantId), Math.max(1, quantity), now, now);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create cart item", e);
        }
    }

    @Override
    public boolean updateQuantity(String id, int quantity) {
        String sql = "UPDATE ItemCart SET quantity = ?, \"updatedAt\" = NOW() WHERE id = ?";

        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, quantity));
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update cart item quantity", e);
        }
    }

    @Override
    public boolean deleteByCartIdAndProductAndVariant(String cartId, String productId, String variantId) {
        String sql = "DELETE FROM ItemCart WHERE \"cartId\" = ? AND \"productId\" = ? AND COALESCE(\"variantId\", '') = COALESCE(?, '')";

        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cartId);
            ps.setString(2, productId);
            ps.setString(3, emptyToNull(variantId));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete cart item", e);
        }
    }

    @Override
    public boolean clearByCartId(String cartId) {
        String sql = "DELETE FROM ItemCart WHERE \"cartId\" = ?";

        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cartId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear cart items", e);
        }
    }

    private ItemCartEntity mapResultSetToItemCart(ResultSet rs) throws SQLException {
        Timestamp createdAtTs = rs.getTimestamp("createdAt");
        LocalDateTime createdAt = createdAtTs == null ? null : createdAtTs.toLocalDateTime();
        Timestamp updatedAtTs = rs.getTimestamp("updatedAt");
        LocalDateTime updatedAt = updatedAtTs == null ? null : updatedAtTs.toLocalDateTime();

        return new ItemCartEntity(
                rs.getString("id"),
                rs.getString("cartId"),
                rs.getString("productId"),
                rs.getString("variantId"),
                rs.getInt("quantity"),
                createdAt,
                updatedAt
        );
    }

    private String emptyToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
