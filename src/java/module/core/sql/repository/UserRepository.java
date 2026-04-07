package module.core.sql.repository;

import entity.UserEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import module.core.sql.ConnecDb;
import module.core.sql.interfaces.IUserRepository;
import module.core.user.dto.CreateUserDto;

public class UserRepository implements IUserRepository {

    @Override
    public UserEntity createUser(CreateUserDto dto) {
        String sql = "INSERT INTO `User` (`name`, `dateOfBirth`, `hashPassword`, `email`) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getName());
            ps.setObject(2, dto.getDateOfBirth());
            ps.setString(3, dto.getHashPassword());
            ps.setString(4, dto.getEmail());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Failed to create user: no rows inserted");
            }

            return new UserEntity(
                    null,
                    dto.getName(),
                    dto.getDateOfBirth(),
                    dto.getHashPassword(),
                    null,
                    dto.getEmail(),
                    null,
                    null
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }
}
