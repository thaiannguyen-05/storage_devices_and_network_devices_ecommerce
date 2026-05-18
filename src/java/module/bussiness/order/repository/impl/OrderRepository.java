package module.bussiness.order.repository.impl;

import entity.OrderEntity;
import java.sql.Timestamp;
import java.util.List;
import module.bussiness.order.repository.interfaces.IOrderRepository;
import module.core.sql.JdbcHelper;

public class OrderRepository implements IOrderRepository {
    @Override
    public void insert(OrderEntity o) {
        JdbcHelper.executeUpdate(
            "INSERT INTO `Order` (id, userId, productId, variantId, quantity, phone, address, status, customerName, email, note, paymentMethod, voucherId, totalAmount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            o.getId(), o.getUserId(), o.getProductId(), o.getVariantId(), o.getQuantity(),
            o.getPhone(), o.getAddress(), o.getStatus(),
            o.getCustomerName(), o.getEmail(), o.getNote(), o.getPaymentMethod(), o.getVoucherId(), o.getTotalAmount());
    }

    @Override
    public OrderEntity findById(String id) {
        List<OrderEntity> rows = JdbcHelper.executeQuery(
            "SELECT o.*, p.name AS productName FROM `Order` o LEFT JOIN `Product` p ON o.productId = p.id WHERE o.id = ?",
            rs -> map(rs), id
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public List<OrderEntity> findByUserId(String userId, int offset, int limit) {
        return JdbcHelper.executeQuery(
            "SELECT o.*, p.name AS productName FROM `Order` o LEFT JOIN `Product` p ON o.productId = p.id WHERE o.userId = ? ORDER BY o.createdAt DESC LIMIT ? OFFSET ?",
            rs -> map(rs), userId, limit, offset
        );
    }

    @Override
    public List<OrderEntity> findAll(int offset, int limit) {
        return JdbcHelper.executeQuery(
            "SELECT o.*, p.name AS productName FROM `Order` o LEFT JOIN `Product` p ON o.productId = p.id ORDER BY o.createdAt DESC LIMIT ? OFFSET ?",
            rs -> map(rs), limit, offset
        );
    }

    @Override
    public int countAll() {
        return JdbcHelper.count("SELECT COUNT(*) FROM `Order`");
    }

    @Override
    public void updateStatus(String id, String status) {
        JdbcHelper.executeUpdate("UPDATE `Order` SET status = ? WHERE id = ?", status, id);
    }

    private OrderEntity map(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp updatedAt = rs.getTimestamp("updatedAt");
        OrderEntity order = new OrderEntity(rs.getString("id"), rs.getString("userId"), rs.getString("productId"),
                rs.getString("variantId"), rs.getInt("quantity"), rs.getTimestamp("createdAt").toLocalDateTime(),
                updatedAt == null ? null : updatedAt.toLocalDateTime(), rs.getString("status"));
        order.setPhone(rs.getString("phone"));
        order.setAddress(rs.getString("address"));
        order.setCustomerName(rs.getString("customerName"));
        order.setEmail(rs.getString("email"));
        order.setNote(rs.getString("note"));
        order.setPaymentMethod(rs.getString("paymentMethod"));
        order.setVoucherId(rs.getString("voucherId"));
        java.math.BigDecimal total = rs.getBigDecimal("totalAmount");
        order.setTotalAmount(total);
        try {
            order.setProductName(rs.getString("productName"));
        } catch (java.sql.SQLException e) {
            // ignore if not joined in some simple select queries
        }
        return order;
    }
}
