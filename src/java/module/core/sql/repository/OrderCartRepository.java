package module.core.sql.repository;

import entity.OrderCartEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import module.bussiness.cart.dto.CreateCartDto;
import module.core.sql.ConnecDb;
import module.core.sql.interfaces.IOrderCartRepository;

public class OrderCartRepository implements IOrderCartRepository {

    @Override
    public OrderCartEntity registerCart(CreateCartDto dto) {
        String sql = "INSERT INTO `OrderCart` (`userId`) VALUES (?)";

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getUserId());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Failed to register cart: no rows inserted");
            }

            return new OrderCartEntity(null, dto.getUserId(), null);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to register cart", e);
        }
    }
}
