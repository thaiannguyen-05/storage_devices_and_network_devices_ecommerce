package module.core.user.repository.impl;

import entity.UserEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import module.core.sql.ConnecDb;
import module.core.user.repository.interfaces.IUserRepository;
import module.core.user.dto.CreateUserDto;
import module.core.user.dto.UpdateUserDto;

public class UserRepository implements IUserRepository {

    @Override
    public UserEntity createUser(CreateUserDto dto) {
        String sql = "INSERT INTO User (id, name, \"dateOfBirth\", \"hashPassword\", status, role, email, \"createdAt\", \"updatedAt\") "
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

    @Override
    public List<UserEntity> findAll() {
        String sql = "SELECT id, name, \"dateOfBirth\", \"hashPassword\", status, role, email, \"createdAt\", \"updatedAt\" FROM User ORDER BY \"createdAt\" DESC";
        List<UserEntity> users = new ArrayList<>();

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find users", e);
        }
    }

    @Override
    public List<UserEntity> findAll(int page, int pageSize) {
        String sql = "SELECT id, name, \"dateOfBirth\", \"hashPassword\", status, role, email, \"createdAt\", \"updatedAt\" FROM User ORDER BY \"createdAt\" DESC LIMIT ? OFFSET ?";
        List<UserEntity> users = new ArrayList<>();

        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int offset = (safePage - 1) * safePageSize;

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, safePageSize);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find users with pagination", e);
        }
    }

    @Override
    public UserEntity findById(String id) {
        String sql = "SELECT id, name, \"dateOfBirth\", \"hashPassword\", status, role, email, \"createdAt\", \"updatedAt\" FROM User WHERE id = ?";

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id", e);
        }
    }

    @Override
    public UserEntity findByEmail(String email) {
        String sql = "SELECT id, name, \"dateOfBirth\", \"hashPassword\", status, role, email, \"createdAt\", \"updatedAt\" FROM User WHERE email = ? LIMIT 1";

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by email", e);
        }
    }

    @Override
    public boolean update(String id, UpdateUserDto dto) {
        String sql = "UPDATE User SET name = ?, \"dateOfBirth\" = ?, status = ?, role = ?, email = ?, \"updatedAt\" = NOW() WHERE id = ?";

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getName());
            ps.setObject(2, dto.getDateOfBirth());
            ps.setString(3, dto.getStatus());
            ps.setString(4, dto.getRole());
            ps.setString(5, dto.getEmail());
            ps.setString(6, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public boolean updatePasswordById(String id, String hashPassword) {
        String sql = "UPDATE User SET \"hashPassword\" = ?, \"updatedAt\" = NOW() WHERE id = ?";

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashPassword);
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user password", e);
        }
    }

    @Override
    public boolean activateById(String id) {
        String sql = "UPDATE User SET status = 'ACTIVE', \"updatedAt\" = NOW() WHERE id = ?";

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to activate user", e);
        }
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM User WHERE id = ?";

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    private UserEntity mapResultSetToUser(ResultSet rs) throws SQLException {
        LocalDate dateOfBirth = null;
        if (rs.getDate("dateOfBirth") != null) {
            dateOfBirth = rs.getDate("dateOfBirth").toLocalDate();
        }

        Timestamp createdAtTs = rs.getTimestamp("createdAt");
        LocalDateTime createdAt = createdAtTs != null ? createdAtTs.toLocalDateTime() : null;

        Timestamp updatedAtTs = rs.getTimestamp("updatedAt");
        LocalDateTime updatedAt = updatedAtTs != null ? updatedAtTs.toLocalDateTime() : null;

        return new UserEntity(
                rs.getString("id"),
                rs.getString("name"),
                dateOfBirth,
                rs.getString("hashPassword"),
                rs.getString("status"),
                rs.getString("role"),
                rs.getString("email"),
                createdAt,
                updatedAt
        );
    }
}
