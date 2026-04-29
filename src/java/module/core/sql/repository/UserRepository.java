package module.core.sql.repository;

import entity.UserEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import module.core.sql.ConnecDb;
import module.core.sql.interfaces.IUserRepository;
import module.core.user.dto.CreateUserDto;

public class UserRepository implements IUserRepository {

    @Override
    public UserEntity createUser(CreateUserDto dto) {
        String[] sqlCandidates = new String[] {
            "INSERT INTO `User` (`id`, `name`, `email`, `hashPassword`, `status`, `createdAt`, `updatedAt`) VALUES (UUID(), ?, ?, ?, 'ACTIVE', NOW(), NOW())",
            "INSERT INTO `User` (`name`, `email`, `hashPassword`, `status`, `createdAt`, `updatedAt`) VALUES (?, ?, ?, 'ACTIVE', NOW(), NOW())",
            "INSERT INTO `User` (`id`, `name`, `email`, `hashPassword`, `status`) VALUES (UUID(), ?, ?, ?, 'ACTIVE')",
            "INSERT INTO `User` (`name`, `email`, `hashPassword`, `status`) VALUES (?, ?, ?, 'ACTIVE')",
            "INSERT INTO `User` (`name`, `email`, `hashPassword`) VALUES (?, ?, ?)"
        };

        SQLException lastError = null;
        for (String sql : sqlCandidates) {
            try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, dto.getName());
                ps.setString(2, dto.getEmail());
                ps.setString(3, dto.getHashPassword());

                int affectedRows = ps.executeUpdate();
                if (affectedRows > 0) {
                    UserEntity created = new UserEntity();
                    created.setName(dto.getName());
                    created.setEmail(dto.getEmail());
                    created.setHashPassword(dto.getHashPassword());
                    return created;
                }
            } catch (SQLException e) {
                lastError = e;
            }
        }

        throw new RuntimeException("Failed to create user", lastError);
    }

    @Override
    public UserEntity findByEmail(String email) {
        String sql = "SELECT id, name, hashPassword, status, email, createdAt, updatedAt FROM `User` WHERE email = ? LIMIT 1";

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                UserEntity user = new UserEntity();
                user.setId(rs.getString("id"));
                user.setName(rs.getString("name"));
                user.setHashPassword(rs.getString("hashPassword"));
                user.setStatus(rs.getString("status"));
                user.setEmail(rs.getString("email"));
                java.sql.Timestamp createdTs = rs.getTimestamp("createdAt");
                if (createdTs != null) {
                    user.setCreatedAt(createdTs.toLocalDateTime());
                }
                java.sql.Timestamp updatedTs = rs.getTimestamp("updatedAt");
                if (updatedTs != null) {
                    user.setUpdatedAt(updatedTs.toLocalDateTime());
                }
                return user;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by email", e);
        }
    }

    @Override
    public void updateProfileByEmail(String email, String name) {
        String[] sqlCandidates = new String[] {
            "UPDATE `User` SET `name` = ?, `updatedAt` = NOW() WHERE `email` = ?",
            "UPDATE `User` SET `name` = ? WHERE `email` = ?"
        };

        SQLException lastError = null;
        for (String sql : sqlCandidates) {
            try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.executeUpdate();
                return;
            } catch (SQLException e) {
                lastError = e;
            }
        }

        throw new RuntimeException("Failed to update profile", lastError);
    }

    @Override
    public void updatePasswordByEmail(String email, String hashPassword) {
        String[] sqlCandidates = new String[] {
            "UPDATE `User` SET `hashPassword` = ?, `updatedAt` = NOW() WHERE `email` = ?",
            "UPDATE `User` SET `hashPassword` = ? WHERE `email` = ?"
        };

        SQLException lastError = null;
        for (String sql : sqlCandidates) {
            try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, hashPassword);
                ps.setString(2, email);
                ps.executeUpdate();
                return;
            } catch (SQLException e) {
                lastError = e;
            }
        }

        throw new RuntimeException("Failed to update password", lastError);
    }
}
