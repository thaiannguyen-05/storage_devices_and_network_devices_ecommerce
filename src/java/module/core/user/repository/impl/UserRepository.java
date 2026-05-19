package module.core.user.repository.impl;

import entity.UserEntity;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    public List<UserEntity> findFiltered(String role, String status, String keyword, int offset, int limit) {
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder("SELECT * FROM `User` WHERE 1=1");
        appendFilters(sql, params, role, status, keyword);
        sql.append(" ORDER BY createdAt DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return JdbcHelper.executeQuery(sql.toString(), rs -> map(rs), params.toArray());
    }

    @Override
    public int countAll() {
        return JdbcHelper.count("SELECT COUNT(*) FROM `User`");
    }

    @Override
    public int countFiltered(String role, String status, String keyword) {
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM `User` WHERE 1=1");
        appendFilters(sql, params, role, status, keyword);
        return JdbcHelper.count(sql.toString(), params.toArray());
    }

    @Override
    public void update(UserEntity user) {
        JdbcHelper.executeUpdate("UPDATE `User` SET name = ?, dateOfBirth = ?, status = ?, role = ?, email = ? WHERE id = ?",
                user.getName(), user.getDateOfBirth(), user.getStatus(), user.getRole(), user.getEmail(), user.getId());
    }

    @Override
    public void updateRole(String id, String role) {
        JdbcHelper.executeUpdate("UPDATE `User` SET role = ? WHERE id = ?", role, id);
    }

    @Override
    public void updatePassword(String id, String hashPassword) {
        JdbcHelper.executeUpdate("UPDATE `User` SET hashPassword = ? WHERE id = ?", hashPassword, id);
    }

    @Override
    public void updateStatus(String id, String status) {
        JdbcHelper.executeUpdate("UPDATE `User` SET status = ? WHERE id = ?", status, id);
    }

    @Override
    public Map<String, Integer> countByRole() {
        return collectCounts("SELECT role AS groupingKey, COUNT(*) AS total FROM `User` GROUP BY role ORDER BY role");
    }

    @Override
    public Map<String, Integer> countByStatus() {
        return collectCounts("SELECT status AS groupingKey, COUNT(*) AS total FROM `User` GROUP BY status ORDER BY status");
    }

    @Override
    public void delete(String id) {
        JdbcHelper.executeUpdate("DELETE FROM `User` WHERE id = ?", id);
    }

    private UserEntity map(java.sql.ResultSet rs) throws java.sql.SQLException {
        Date dateOfBirth = rs.getDate("dateOfBirth");
        Timestamp createdAt = rs.getTimestamp("createdAt");
        Timestamp updatedAt = rs.getTimestamp("updatedAt");
        return new UserEntity(
                rs.getString("id"),
                rs.getString("name"),
                dateOfBirth == null ? LocalDate.of(2000, 1, 1) : dateOfBirth.toLocalDate(),
                rs.getString("hashPassword"),
                rs.getString("status"),
                rs.getString("role"),
                rs.getString("email"),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                updatedAt == null ? null : updatedAt.toLocalDateTime());
    }

    private void appendFilters(StringBuilder sql, List<Object> params, String role, String status, String keyword) {
        if (role != null && !role.trim().isEmpty()) {
            sql.append(" AND role = ?");
            params.add(role.trim());
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (id LIKE ? OR name LIKE ? OR email LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
    }

    private Map<String, Integer> collectCounts(String sql) {
        List<Map.Entry<String, Integer>> rows = JdbcHelper.executeQuery(
                sql, rs -> Map.entry(rs.getString("groupingKey"), rs.getInt("total")));
        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : rows) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
