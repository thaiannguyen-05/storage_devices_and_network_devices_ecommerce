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
                rs -> new UserEntity(rs.getString("id"), rs.getString("name"), rs.getDate("dateOfBirth").toLocalDate(),
                        rs.getString("hashPassword"), rs.getString("status"), rs.getString("role"), rs.getString("email"),
                        rs.getTimestamp("createdAt").toLocalDateTime(), rs.getTimestamp("updatedAt").toLocalDateTime()),
                email);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public UserEntity findById(String id) {
        List<UserEntity> rows = JdbcHelper.executeQuery(
                "SELECT * FROM `User` WHERE id = ? LIMIT 1",
                rs -> new UserEntity(rs.getString("id"), rs.getString("name"), rs.getDate("dateOfBirth").toLocalDate(),
                        rs.getString("hashPassword"), rs.getString("status"), rs.getString("role"), rs.getString("email"),
                        rs.getTimestamp("createdAt").toLocalDateTime(), rs.getTimestamp("updatedAt").toLocalDateTime()),
                id);
        return rows.isEmpty() ? null : rows.get(0);
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
}
