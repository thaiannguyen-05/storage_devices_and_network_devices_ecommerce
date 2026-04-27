package module.core.sql.repository;

import entity.UserEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;
import module.core.sql.ConnecDb;
import module.core.sql.interfaces.IUserRepository;
import module.core.user.dto.CreateUserDto;

public class UserRepository implements IUserRepository {

    @Override
    public UserEntity createUser(CreateUserDto dto) {
        String sql = "INSERT INTO `User` (`id`, `name`, `dateOfBirth`, `hashPassword`, `status`, `role`, `email`, `createdAt`, `updatedAt`) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            String id = UUID.randomUUID().toString();
            LocalDateTime now = LocalDateTime.now();
            ps.setString(1, id);
            ps.setString(2, dto.getName());
            ps.setObject(3, dto.getDateOfBirth());
            ps.setString(4, dto.getHashPassword());
            ps.setString(5, "PENDING");
            ps.setString(6, "USER");
            ps.setString(7, dto.getEmail());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Failed to create user: no rows inserted");
            }

            return new UserEntity(
                    id,
                    dto.getName(),
                    dto.getDateOfBirth(),
                    dto.getHashPassword(),
                    "PENDING",
                    "USER",
                    dto.getEmail(),
                    now,
                    now
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }
}
