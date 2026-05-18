package module.core.user.repository.impl;

import entity.UserEntity;
import java.util.List;
import java.util.UUID;
import module.core.sql.JdbcHelper;
import module.core.user.repository.interfaces.IUserRepository;

public class UserRepository implements IUserRepository {
    @Override
    public void insert(UserEntity user) {
        JdbcHelper.executeUpdate("INSERT INTO `User` (id, name, dateOfBirth, hashPassword, status, role, email) VALUES (?, ?, ?, ?, ?, ?, ?)",
                user.getId(), user.getName(), user.getDateOfBirth(), user.getHashPassword(), user.getStatus(), user.getRole(), user.getEmail());
    }

    @Override
    public void createCartForUser(String userId) {
        JdbcHelper.executeUpdate("INSERT INTO OrderCart (id, userId) VALUES (?, ?) ON DUPLICATE KEY UPDATE userId = VALUES(userId)",
                UUID.randomUUID().toString(), userId);
    }

    @Override
    public UserEntity findById(String id) {
        List<UserEntity> rows = JdbcHelper.executeQuery("SELECT * FROM `User` WHERE id = ?",
                rs -> map(rs), id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public UserEntity findByEmail(String email) {
        List<UserEntity> rows = JdbcHelper.executeQuery("SELECT * FROM `User` WHERE email = ?",
                rs -> map(rs), email);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public List<UserEntity> findAll(int offset, int limit) {
        return JdbcHelper.executeQuery("SELECT * FROM `User` ORDER BY createdAt DESC LIMIT ? OFFSET ?",
                rs -> map(rs), limit, offset);
    }

    @Override
    public int countAll() {
        return JdbcHelper.count("SELECT COUNT(*) FROM `User`");
    }

    @Override
    public void update(UserEntity user) {
        JdbcHelper.executeUpdate("UPDATE `User` SET name = ?, dateOfBirth = ?, status = ?, role = ?, email = ? WHERE id = ?",
                user.getName(), user.getDateOfBirth(), user.getStatus(), user.getRole(), user.getEmail(), user.getId());
    }

    @Override
    public void updateStatus(String id, String status) {
        JdbcHelper.executeUpdate("UPDATE `User` SET status = ? WHERE id = ?", status, id);
    }

    @Override
    public void delete(String id) {
        JdbcHelper.executeUpdate("DELETE FROM `User` WHERE id = ?", id);
    }

    private UserEntity map(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new UserEntity(rs.getString("id"), rs.getString("name"), rs.getDate("dateOfBirth").toLocalDate(),
                rs.getString("hashPassword"), rs.getString("status"), rs.getString("role"), rs.getString("email"),
                rs.getTimestamp("createdAt").toLocalDateTime(), rs.getTimestamp("updatedAt").toLocalDateTime());
    }
}
