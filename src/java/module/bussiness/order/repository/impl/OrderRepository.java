package module.bussiness.order.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import module.bussiness.order.repository.interfaces.IOrderRepository;
import module.core.sql.ConnecDb;

public class OrderRepository implements IOrderRepository {

    @Override
    public boolean updateStatus(String orderId, String status) {
        if (orderId == null || orderId.isBlank() || status == null || status.isBlank()) {
            return false;
        }

        String sql = "UPDATE `Order` SET status = ? WHERE id = ?";
        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update order status", e);
        }
    }
}
