package module.core.auth.repository.impl;

import entity.SessionEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import module.core.auth.repository.interfaces.ISessionRepository;
import module.core.sql.ConnecDb;

public class SessionRepository implements ISessionRepository {

    @Override
    public SessionEntity create(String userId, String hashRefreshToken, String ip) {
        String sql = "INSERT INTO Session (id, \"hashRefreshToken\", \"userId\", ip, \"createdAt\", \"updatedAt\") VALUES (?, ?, ?, ?, NOW(), NOW())";
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, hashRefreshToken);
            ps.setString(3, userId);
            ps.setString(4, ip);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Failed to create session");
            }

            return new SessionEntity(id, hashRefreshToken, userId, ip, now, now);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create session", e);
        }
    }

    @Override
    public List<SessionEntity> findAll() {
        String sql = "SELECT id, \"hashRefreshToken\", \"userId\", ip, \"createdAt\", \"updatedAt\" FROM Session ORDER BY \"createdAt\" DESC";
        List<SessionEntity> sessions = new ArrayList<>();

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                sessions.add(map(rs));
            }
            return sessions;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find sessions", e);
        }
    }

    @Override
    public SessionEntity findById(String id) {
        String sql = "SELECT id, \"hashRefreshToken\", \"userId\", ip, \"createdAt\", \"updatedAt\" FROM Session WHERE id = ?";

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find session by id", e);
        }
    }

    @Override
    public SessionEntity findByHashRefreshToken(String hashRefreshToken) {
        String sql = "SELECT id, \"hashRefreshToken\", \"userId\", ip, \"createdAt\", \"updatedAt\" FROM Session WHERE \"hashRefreshToken\" = ? ORDER BY \"createdAt\" DESC LIMIT 1";

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashRefreshToken);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find session by refresh token", e);
        }
    }

    @Override
    public List<SessionEntity> findByUserIdAndIp(String userId, String ip) {
        String sql = "SELECT id, \"hashRefreshToken\", \"userId\", ip, \"createdAt\", \"updatedAt\" FROM Session WHERE \"userId\" = ? AND ip = ? ORDER BY \"createdAt\" DESC";
        List<SessionEntity> sessions = new ArrayList<>();

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, ip);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sessions.add(map(rs));
                }
            }
            return sessions;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find sessions by user id and IP", e);
        }
    }

    @Override
    public boolean updateHashRefreshToken(String id, String hashRefreshToken) {
        String sql = "UPDATE Session SET \"hashRefreshToken\" = ?, \"updatedAt\" = NOW() WHERE id = ?";

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashRefreshToken);
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update session refresh token", e);
        }
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM Session WHERE id = ?";

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete session", e);
        }
    }

    @Override
    public boolean deleteByUserId(String userId) {
        String sql = "DELETE FROM Session WHERE \"userId\" = ?";

        try (Connection conn = ConnecDb.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete sessions by user id", e);
        }
    }

    private SessionEntity map(ResultSet rs) throws SQLException {
        Timestamp createdAtTs = rs.getTimestamp("createdAt");
        Timestamp updatedAtTs = rs.getTimestamp("updatedAt");
        LocalDateTime createdAt = createdAtTs == null ? null : createdAtTs.toLocalDateTime();
        LocalDateTime updatedAt = updatedAtTs == null ? null : updatedAtTs.toLocalDateTime();

        return new SessionEntity(
                rs.getString("id"),
                rs.getString("hashRefreshToken"),
                rs.getString("userId"),
                rs.getString("ip"),
                createdAt,
                updatedAt
        );
    }
}
