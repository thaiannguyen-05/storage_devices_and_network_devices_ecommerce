package module.bussiness.order.repository.impl;

import entity.OrderCartEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;
import module.bussiness.cart.dto.CreateCartDto;
import module.core.sql.ConnecDb;
import module.bussiness.order.repository.interfaces.IOrderCartRepository;

public class OrderCartRepository implements IOrderCartRepository {

    @Override
    public OrderCartEntity registerCart(CreateCartDto dto) {
        String sql = "INSERT INTO `OrderCart` (`id`, `userId`, `createdAt`) VALUES (?, ?, NOW())";

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            String id = UUID.randomUUID().toString();
            LocalDateTime now = LocalDateTime.now();
            ps.setString(1, id);
            ps.setString(2, dto.getUserId());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Failed to register cart: no rows inserted");
            }

            return new OrderCartEntity(id, dto.getUserId(), now);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to register cart", e);
        }
    }
}
