package module.core.auth.repository.impl;

import entity.UserEntity;
import java.util.UUID;
import java.util.List;
import module.core.auth.repository.interfaces.IAuthRepository;
import module.core.sql.JdbcHelper;

public class AuthRepository implements IAuthRepository {
    @Override
    public UserEntity findByEmail(String email) {
        List<UserEntity> rows = JdbcHelper.executeQuery(
                "SELECT * FROM `User` WHERE email = ? LIMIT 1",
                rs -> mapUser(rs),
                email);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public UserEntity findById(String id) {
        List<UserEntity> rows = JdbcHelper.executeQuery(
                "SELECT * FROM `User` WHERE id = ? LIMIT 1",
                rs -> mapUser(rs),
                id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private UserEntity mapUser(java.sql.ResultSet rs) throws java.sql.SQLException {
        java.sql.Date dob = rs.getDate("dateOfBirth");
        java.sql.Timestamp createdAt = rs.getTimestamp("createdAt");
        java.sql.Timestamp updatedAt = rs.getTimestamp("updatedAt");
        return new UserEntity(rs.getString("id"), rs.getString("name"),
                dob == null ? null : dob.toLocalDate(),
                rs.getString("hashPassword"), rs.getString("status"), rs.getString("role"), rs.getString("email"),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                updatedAt == null ? null : updatedAt.toLocalDateTime());
    }

    @Override
    public void saveSession(String id, String refreshTokenHash, String userId, String ip) {
        JdbcHelper.executeUpdate("INSERT INTO `Session` (id, hashRefreshToken, userId, ip) VALUES (?, ?, ?, ?)",
                id, refreshTokenHash, userId, ip);
    }

    @Override
    public void deleteSession(String sessionId) {
        JdbcHelper.executeUpdate("DELETE FROM `Session` WHERE id = ?", sessionId);
    }

    @Override
    public void updatePassword(String userId, String passwordHash) {
        JdbcHelper.executeUpdate("UPDATE `User` SET hashPassword = ? WHERE id = ?", passwordHash, userId);
    }

    @Override
    public void createCartForUser(String userId) {
        JdbcHelper.executeUpdate("INSERT INTO OrderCart (id, userId) VALUES (?, ?) ON DUPLICATE KEY UPDATE userId = VALUES(userId)",
                UUID.randomUUID().toString(), userId);
    }

    public UserEntity findAdmin() {
        List<UserEntity> rows = JdbcHelper.executeQuery(
                "SELECT * FROM `User` WHERE role = 'ADMIN' LIMIT 1",
                rs -> mapUser(rs));
        return rows.isEmpty() ? null : rows.get(0);
    }
}
