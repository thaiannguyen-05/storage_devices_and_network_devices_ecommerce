package module.bussiness.order.repository.impl;

import entity.OrderEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import module.bussiness.order.repository.interfaces.IOrderRepository;
import module.core.sql.ConnecDb;

public class OrderRepository implements IOrderRepository {

    @Override
    public void upsertCartOrder(String userId, String productId, String variantId, int quantity, String status) {
        String findSql = "SELECT id, quantity FROM \"Order\" WHERE \"userId\" = ? AND \"productId\" = ? AND COALESCE(\"variantId\", '') = COALESCE(?, '') AND status = ? ORDER BY \"createdAt\" DESC LIMIT 1";
        String updateSql = "UPDATE \"Order\" SET quantity = ?, \"updatedAt\" = NOW() WHERE id = ?";
        String insertSql = "INSERT INTO \"Order\" (id, \"userId\", \"productId\", \"variantId\", quantity, status, \"createdAt\", \"updatedAt\") VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";

        try (Connection conn = ConnecDb.getConnection()) {
            String existingId = null;
            int existingQty = 0;

            try (PreparedStatement findPs = conn.prepareStatement(findSql)) {
                findPs.setString(1, userId);
                findPs.setString(2, productId);
                findPs.setString(3, emptyToNull(variantId));
                findPs.setString(4, status);
                try (ResultSet rs = findPs.executeQuery()) {
                    if (rs.next()) {
                        existingId = rs.getString("id");
                        existingQty = rs.getInt("quantity");
                    }
                }
            }

            if (existingId != null) {
                try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                    updatePs.setInt(1, Math.max(1, existingQty + Math.max(1, quantity)));
                    updatePs.setString(2, existingId);
                    updatePs.executeUpdate();
                }
                return;
            }

            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setString(1, UUID.randomUUID().toString());
                insertPs.setString(2, userId);
                insertPs.setString(3, productId);
                insertPs.setString(4, emptyToNull(variantId));
                insertPs.setInt(5, Math.max(1, quantity));
                insertPs.setString(6, status);
                insertPs.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to upsert cart order", e);
        }
    }

    @Override
    public void updateStatusByUserAndItem(String userId, String productId, String variantId, String fromStatus, String toStatus) {
        String sql = "UPDATE \"Order\" SET status = ?, \"updatedAt\" = NOW() WHERE \"userId\" = ? AND \"productId\" = ? AND COALESCE(\"variantId\", '') = COALESCE(?, '') AND status = ?";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, toStatus);
            ps.setString(2, userId);
            ps.setString(3, productId);
            ps.setString(4, emptyToNull(variantId));
            ps.setString(5, fromStatus);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update order status", e);
        }
    }

    @Override
    public void updateDeliveryInfo(String userId, String productId, String variantId, String status, String phone, String address) {
        String sql = "UPDATE \"Order\" SET phone = ?, address = ?, \"updatedAt\" = NOW() WHERE \"userId\" = ? AND \"productId\" = ? AND COALESCE(\"variantId\", '') = COALESCE(?, '') AND status = ?";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            ps.setString(2, address);
            ps.setString(3, userId);
            ps.setString(4, productId);
            ps.setString(5, emptyToNull(variantId));
            ps.setString(6, status);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update delivery info", e);
        }
    }

    @Override
    public void updateCartQuantity(String userId, String productId, String variantId, int quantity, String status) {
        String sql = "UPDATE \"Order\" SET quantity = ?, \"updatedAt\" = NOW() WHERE \"userId\" = ? AND \"productId\" = ? AND COALESCE(\"variantId\", '') = COALESCE(?, '') AND status = ?";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, quantity));
            ps.setString(2, userId);
            ps.setString(3, productId);
            ps.setString(4, emptyToNull(variantId));
            ps.setString(5, status);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update cart quantity", e);
        }
    }

    @Override
    public void removeCartOrder(String userId, String productId, String variantId, String status) {
        String sql = "DELETE FROM \"Order\" WHERE \"userId\" = ? AND \"productId\" = ? AND COALESCE(\"variantId\", '') = COALESCE(?, '') AND status = ?";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, productId);
            ps.setString(3, emptyToNull(variantId));
            ps.setString(4, status);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove cart order", e);
        }
    }

    @Override
    public void clearCartOrders(String userId, String status) {
        String sql = "DELETE FROM \"Order\" WHERE \"userId\" = ? AND status = ?";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, status);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear cart orders", e);
        }
    }

    @Override
    public List<OrderEntity> findByUserIdAndStatus(String userId, String status) {
        String sql = "SELECT id, \"userId\", \"productId\", \"variantId\", quantity, status, phone, address, \"createdAt\", \"updatedAt\" FROM \"Order\" WHERE \"userId\" = ? AND status = ? ORDER BY \"createdAt\" ASC, \"updatedAt\" ASC";
        List<OrderEntity> result = new ArrayList<>();
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderEntity item = new OrderEntity();
                    item.setId(rs.getString("id"));
                    item.setUserId(rs.getString("userId"));
                    item.setProductId(rs.getString("productId"));
                    item.setVariantId(rs.getString("variantId"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setStatus(rs.getString("status"));
                    item.setPhone(rs.getString("phone"));
                    item.setAddress(rs.getString("address"));
                    Timestamp createdAt = rs.getTimestamp("createdAt");
                    if (createdAt != null) {
                        item.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    Timestamp updatedAt = rs.getTimestamp("updatedAt");
                    if (updatedAt != null) {
                        item.setUpdatedAt(updatedAt.toLocalDateTime());
                    }
                    result.add(item);
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load orders by user and status", e);
        }
    }

    public void updateStatusByOrderId(String orderId, String status) {
        String sql = "UPDATE \"Order\" SET status = ?, \"updatedAt\" = NOW() WHERE id = ?";
        try (Connection conn = ConnecDb.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, orderId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update order status by orderId", e);
        }
    }

    private String emptyToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
